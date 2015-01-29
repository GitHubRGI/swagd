/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.g2t;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.task.Settings;
import com.rgi.common.task.Settings.Profile;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.Tile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.profile.TileProfileFactory;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;

public class TileJob implements Runnable {
	private static final int TILESIZE = 256;
	//private static final String TILEEXT = "png";

	private File file;
	//private String fileName;
	private Settings settings;
	private Profile profileSetting;
	private TileProfile tileProfile;
	private SpatialReference inputSRS;
	private Dataset inputDS;
	private String inputWKT;
	private SpatialReference outputSRS;
	private Dataset outputDS;
	private int minZoom = 0;
	private int maxZoom = 0;
	private Color noDataColor;

	// adfGeoTransform[0] /* top left x */
	// adfGeoTransform[1] /* w-e pixel resolution */
	// adfGeoTransform[2] /* 0 */
	// adfGeoTransform[3] /* top left y */
	// adfGeoTransform[4] /* 0 */
	// adfGeoTransform[5] /* n-s pixel resolution (negative value) */
	//private double[] inputGT;
	//private double[] outputGT;

	//private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	//private List<Future<?>> tasks = new ArrayList<>();

	private final TileStoreReader tileStoreReader;
	private final TileStoreWriter tileStoreWriter;

	//private TaskMonitor monitor;  //commented out because findbugs says this is an unused field
	//private double workTotal = 0;
	//private int workUnits = 0;

	public TileJob(File file, TileStoreReader tileStoreReader, TileStoreWriter tileStoreWriter, Settings settings, TaskMonitor monitor) {
		this.file = file;
		this.tileStoreReader = tileStoreReader;
		this.tileStoreWriter = tileStoreWriter;
		this.settings = settings;
//		this.monitor = monitor; //commented out because it is never read
	}

	@Override
	public void run() {
		osr.UseExceptions();
		gdal.AllRegister();
		try {
			tile(file);
			System.out.println("Done.");
		} catch (TilingException te) {
			System.err.println("Unable to complete tiling job: " + te.getMessage());
			te.printStackTrace();
		}
	}

	private void tile(File inputFile) throws TilingException {
		profileSetting = Settings.Profile.valueOf(this.settings.get(Setting.TileProfile));
		tileProfile = TileProfileFactory.create(profileSetting.getAuthority(),
												profileSetting.getID());

		inputDS = null;
		try {
			inputDS = gdal.Open(inputFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
			if (inputDS == null) {
				System.err.println("GDALOpen failed: " + gdal.GetLastErrorNo());
				System.err.println(gdal.GetLastErrorMsg());
				return;
			}
		} catch (Exception e) {
			System.err.println("Unable to open input file: " + e.getMessage());
			return; 
		}

		if (inputDS.getRasterCount() == 0) {
			throw new IllegalArgumentException("Input file contains no rasters");
		}

		// try to determine input SRS
		inputWKT = inputDS.GetProjection();
		if (inputWKT == null && inputDS.GetGCPCount() != 0) {
			inputWKT = inputDS.GetGCPProjection();
		}
		inputSRS = null;
		if (inputWKT != null) {
			inputSRS = new SpatialReference();
			inputSRS.ImportFromWkt(inputWKT);
		}

		double[] inputGT = inputDS.GetGeoTransform();
		if (inputGT[2] != 0 || inputGT[4] != 0) {
			throw new IllegalArgumentException(
					"Georeference of the input file contains rotation or skew. Such file is not supported.");
		}

		noDataColor = settings.getColor(Setting.NoDataColor);

		outputSRS = null;
		if (Arrays.equals(inputGT, new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 }) &&
			inputDS.GetGCPCount() == 0) {
			throw new IllegalArgumentException(
					"Unable to generate non-raster tiles from non-georeferenced input");
		}

		outputSRS = new SpatialReference();
		outputSRS.ImportFromEPSG(profileSetting.getID());

		outputDS = null;

		// confirm reprojection is needed:
		// no input projection? nothing to reproject!
		// input matches output projection? no need to reproject
		if (inputSRS != null
				&& !inputSRS.ExportToProj4().equals(outputSRS.ExportToProj4())) {
			// TODO: get user preference resample quality
			int resample = gdalconstConstants.GRA_Bilinear;
			outputDS = gdal.AutoCreateWarpedVRT(inputDS, inputWKT,
					outputSRS.ExportToWkt(), resample);
			if (outputDS == null) {
				System.err.println("AutoCreateWarpedVRT returned null!");
				return;
			}
		} else {
			outputDS = inputDS;
		}
		double[] outputGT = outputDS.GetGeoTransform();
		BufferedImage source = convert(outputDS);

		// image georeference points
		Coordinate<Double> imageUpperLeft = new Coordinate<>(outputGT[0], outputGT[3]);
		Coordinate<Double> imageLowerRight =
				new Coordinate<>(outputGT[0] + outputGT[1] * outputDS.getRasterXSize(),
									   outputGT[3] + outputGT[5] * outputDS.getRasterYSize());

		// generate tiles in compliance with TMS spec
		// zoom level resolutions are in powers of 2
		// since we know that 3857 uses meters, we can proceed.
		// TODO: get user's output preference coordinate system
		// TODO: get bounds for user's preferred output coordinate system

		// we'll be doing a lot of log2(x) (i.e. log base 2 of x). Since there isn't a
		// built-in function for this, we'll do log10(x)/log10(2). Rather than
		// calculate log10(2) over and over, we'll do it once and store it here.
		double log = Math.log(2);
		BoundingBox worldBounds = tileProfile.getBounds();
		// we'll be doing these calculations once for width and once for height
		// but we'll start with width
		double totalWorld = worldBounds.getWidth();
		// the resolution in srs units per pixel of the reprojected source image
		// probably won't perfectly match the resolution of one of our zoom levels,
		// so we need to determine which zoom level it's closest to, so we can
		// scale down or up with the least loss or addition of data.
		// Since zoom level resolutions will always be related to the number of
		// tiles at a zoom level, and number of tiles will always be a power of 2,
		// we can reverse the calculation to get a fractional zoom level.
		// Resolution = Size of the World / ( Number of Tiles * Pixels per Tile )
		// and Number of Tiles(z) = 2 ^ z
		// so
		// z = log2( Size of the World / (Resolution * Pixels per Tile) )
		double fractionalZoom = Math.log(worldBounds.getWidth()
				/ (outputGT[1] * TILESIZE))
				/ log; // fractional native zoom level for X
		// now the 2 closest zoom levels will be the ceiling and floor of our
		// fractional zoom level. We'll calculate the resolution for each zoom
		// level, and the difference between the zoom level resolution and
		// our source image resolution. Whichever zoom level has the smallest
		// difference in resolution from our source resolution will become our
		// max zoom level.
		// if all resolutions are identical, default to zooming larger
		// take the ceiling of our fractional zoom level
		maxZoom = (int) Math.ceil(fractionalZoom);
		// calculate the resolution for that zoom level
		double zoomResolution = totalWorld / (Math.pow(2, maxZoom) * TILESIZE);
		// take the difference between the zoom level resolution and the source resolution
		// the zoom level resolution should be smaller due to the larger number of tiles.
		double zoomDiff = outputGT[1] - zoomResolution;
		// take the floor of our fractional zoom level
		int tempZoom = (int) Math.floor(fractionalZoom);
		// calculate the resolution for that zoom level
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		// take the difference in resolutions and compare to our previous difference
		if ((zoomResolution - outputGT[1]) < zoomDiff) {
			// a smaller difference means we've found a preferred zoom level
			maxZoom = tempZoom;
			// store the difference for future comparison
			zoomDiff = outputGT[1] - zoomResolution;
		}
		// repeat the above calculations, but this time for height
		totalWorld = worldBounds.getHeight();
		// keep in mind that the resolution for Y is reported as negative,
		// calculate the fractional zoom level for the source resolution
		fractionalZoom = Math.log(totalWorld / ((-outputGT[5]) * TILESIZE))
				/ log; // fractional native zoom level for X
		// take the ceiling of our fractional zoom level
		tempZoom = (int) Math.ceil(fractionalZoom);
		// calculate the resolution
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		// take the difference (again, negative resolution for Y from GDAL)
		// and compare to our previous smallest difference
		if (((-outputGT[5]) - zoomResolution) < zoomDiff) {
			// a smaller difference means we've found a preferred zoom level
			maxZoom = tempZoom;
			// store the difference for future comparison
			zoomDiff = zoomResolution - (-outputGT[5]);
		}
		// take the floor of our fractional zoom level
		tempZoom = (int) Math.floor(fractionalZoom);
		// calculate the resolution for that zoom level
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		// take the difference (again, negative resolution for Y from GDAL)
		// and compare to our previous smallest difference.
		if ((zoomResolution - (-outputGT[5])) < zoomDiff) {
			// a smaller difference means we've found a preferred zoom level
			maxZoom = tempZoom;
		}

		// next, find the zoom level where the entire image fits one tile.
		// similar to the above calculation for closest zoom level, but
		// this time instead of choosing between ceiling and floor
		// zoom levels, we'll always choose the floor. As before, the
		// calculation for fractional zoom level is:
		// Resolution = Size of the World / ( Number of Tiles * Pixels per Tile )
		// where, again, Number of Tiles is 2 ^ z
		// This time Resolution = Size of the Image / Pixels per Tile
		// Plugging in this resolution, Pixels per Tile multiplies out,
		// and the calculation reduces to:
		// z = log2( Size of the World / Size of the Image )
		// again, do it once for width and once for height
		minZoom = (int) Math.floor(Math.log(worldBounds.getWidth()
				/ (outputGT[1] * outputDS.getRasterXSize()))
				/ log);
		tempZoom = (int) Math.floor(Math.log(worldBounds.getHeight()
				/ ((-outputGT[5]) * outputDS.getRasterYSize()))
				/ log);
		// choose the lower of the calculated zoom levels
		if (tempZoom < minZoom) {
			minZoom = tempZoom;
		}

		// TODO: get user preference for origin, use preference to calculate
		// bounds
		TileOrigin origin = TileOrigin.valueOf(settings.get(Setting.TileOrigin));

		int numTilesWidth = 1;
		int numTilesHeight = 1;

		// We know our minimum zoom is of sufficient resolution that our
		// source image will fit entirely within the tile size, but it
		// might not be aligned appropriately to actually fall entirely
		// within the bounds of a single tile. As such, we need to
		// decrease the minimum zoom until the entire image fits
		// entirely within the bounds of a single tile
		Coordinate<Integer> minZoomUpperLeftTileCoordinate = null;
		Coordinate<Integer> minZoomLowerRightTileCoordinate = null;
		do {
			// Calculate the tile set parameters for the zoom level
			TileSet matrixBounds = new TileSet(minZoom, origin);
			// Determine into which tile in the tile set the upper left
			// corner of our source image will fall
			minZoomUpperLeftTileCoordinate = transform(worldBounds,
					imageUpperLeft, matrixBounds);
			// Determine into which tile in the tile set the lower right
			// corner of our source image will fall
			minZoomLowerRightTileCoordinate = transform(worldBounds,
					imageLowerRight, matrixBounds);
			// calculate the number of tiles these coordinates span
			// we take the absolute value so we don't have to care about
			// origin, and whether the coordinates increase or decrease
			// left to right and top to bottom. Add one because the
			// max coordinate should be inclusive of its tile, not exclusive
			numTilesWidth = Math.abs(minZoomUpperLeftTileCoordinate.getX()
					- minZoomLowerRightTileCoordinate.getX()) + 1;
			numTilesHeight = Math.abs(minZoomUpperLeftTileCoordinate.getY()
					- minZoomLowerRightTileCoordinate.getY()) + 1;
			// if the number of tiles needed for either height or width
			// exceeds 1, decrease the minimum zoom level and try again.
			if (numTilesWidth > 1 || numTilesHeight > 1) {
				--minZoom;
			}
			// If we've done our work right this should happen no more
			// than once, but just in case, we'll put it in a loop.
		} while (numTilesWidth > 1 || numTilesHeight > 1);

		// TODO: get user preference for generating a GPKG-compliant set of
		// tiles.
		// GPKG compliance means that empty tiles are generated so that the
		// tiles for each zoom level
		// fill the bounds of the lowest zoom level.
		boolean direct = false;
		boolean compliant = false;

		// generate base tile set
		// TileMatrix maxMatrix = new TileMatrix(profile, maxZoom,
		// Origin.BottomLeft);
		TileSet matrixBounds = new TileSet(maxZoom, origin);
		Coordinate<Integer> upperLeftTileCoordinate = transform(worldBounds,
				imageUpperLeft, matrixBounds);
		Coordinate<Integer> lowerRightTileCoordinate = transform(worldBounds,
				imageLowerRight, matrixBounds);
		numTilesWidth = Math.abs(lowerRightTileCoordinate.getX()
				- upperLeftTileCoordinate.getX()) + 1;
		numTilesHeight = Math.abs(lowerRightTileCoordinate.getY()
				- upperLeftTileCoordinate.getY()) + 1;

		int zoomLevelTiles = (int) Math.pow(2, maxZoom);

		// srs units (e.g. meters) per pixel = (world size / num tiles) / pixels
		// per tile
		double rx = (tileProfile.getBounds().getWidth() / zoomLevelTiles) / TILESIZE;
		double ry = (tileProfile.getBounds().getHeight() / zoomLevelTiles) / TILESIZE;

		// pixels = (pixels * meters per pixel) / meters per pixel
		// w' = (w * r) / r'
		int scaledWidth = (int) ((outputDS.getRasterXSize() * outputGT[1]) / rx);
		int scaledHeight = (int) ((outputDS.getRasterYSize() * (-outputGT[5])) / ry);

		// TileMatrixSet maxTileSet = new TileMatrixSet(maxMatrix);
		// Tile upperLeftTile = tileStoreWriter.addTile(upperLeftTileCoordinate);
		Dimension2D tileBounds = tileProfile.getTileDimensions(maxZoom);

		// pixels = (meters - meters) / meters per pixel
		int offsetX = (int) ((outputGT[0] - tileBounds.getWidth()) / rx);
		int offsetY = (int) ((tileBounds.getHeight() - outputGT[3]) / ry);

		for (int x = 0; x < numTilesWidth; ++x) {
			int tileX = upperLeftTileCoordinate.getX() + (x * origin.getDeltaX());
			for (int y = 0; y < numTilesHeight; ++y) {
				int tileY = upperLeftTileCoordinate.getY() + (y * origin.getDeltaY());
				BufferedImage tileImage = new BufferedImage(TILESIZE, TILESIZE, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = tileImage.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								   RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setColor(this.noDataColor);
				g.fillRect(0, 0, TILESIZE, TILESIZE);
				g.drawImage(source, offsetX - (x * TILESIZE),
							offsetY	- (y * TILESIZE), scaledWidth, scaledHeight, null);
				try {
					tileStoreWriter.addTile(
							tileProfile.absoluteToCrsCoordinate(
									new AbsoluteTileCoordinate(tileY, tileX, maxZoom, origin)), maxZoom, tileImage);
				} catch (Exception e) {
					throw new TilingException("Unable to add tile", e);
				}
			}
		}

		for (int z = maxZoom - 1; z >= minZoom; --z) {
			// TileMatrix matrix = new TileMatrix(profile, z, origin);
			matrixBounds = new TileSet(z, origin);
			upperLeftTileCoordinate = transform(worldBounds, imageUpperLeft,
					matrixBounds);
			lowerRightTileCoordinate = transform(worldBounds, imageLowerRight,
					matrixBounds);
			numTilesWidth = Math.abs(lowerRightTileCoordinate.getX()
					- upperLeftTileCoordinate.getX()) + 1;
			numTilesHeight = Math.abs(lowerRightTileCoordinate.getY()
					- upperLeftTileCoordinate.getY()) + 1;

			zoomLevelTiles = (int) Math.pow(2, z);
			// srs units (e.g. meters) per pixel = (world size / num tiles) /
			// pixels per tile
			rx = (tileProfile.getBounds().getWidth() / zoomLevelTiles) / TILESIZE;
			ry = (tileProfile.getBounds().getHeight() / zoomLevelTiles) / TILESIZE;

			// pixels = (pixels * meters per pixel) / meters per pixel
			// w' = (w * r) / r'
			scaledWidth = (int) ((outputDS.getRasterXSize() * outputGT[1]) / rx);
			scaledHeight = (int) ((outputDS.getRasterYSize() * (-outputGT[5])) / ry);

			// upperLeftTile = maxTileSet.addTile(upperLeftTileCoordinate);
			tileBounds = tileProfile.getTileDimensions(maxZoom);
			offsetX = (int) ((outputGT[0] - tileBounds.getWidth()) / rx);
			offsetY = (int) ((tileBounds.getHeight() - outputGT[3]) / ry);

			// TileMatrixSet tileSet = new TileMatrixSet(matrix);
			for (int x = 0; x < numTilesWidth; ++x) {
				int tileX = upperLeftTileCoordinate.getX() + (x * origin.getDeltaX());
				for (int y = 0; y < numTilesHeight; ++y) {
					int tileY = upperLeftTileCoordinate.getY() + (y * origin.getDeltaY());
					BufferedImage tileImage = new BufferedImage(TILESIZE, TILESIZE, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = tileImage.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
									   RenderingHints.VALUE_INTERPOLATION_BILINEAR);

					if (direct) {
						g.drawImage(source, offsetX - (x * TILESIZE),
									offsetY - (y * TILESIZE), scaledWidth, scaledHeight, null);
					} else {
						// generate tile using next highest zoom level's tiles.
						BufferedImage preScaled = new BufferedImage(2 * TILESIZE, 2 * TILESIZE, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g_scaled = preScaled.createGraphics();
						for (int zx = 0; zx < 2; ++zx) {
							for (int zy = 0; zy < 2; ++zy) {
								// The coordinate of the tile that we need to stitch into our new tile is
								// double our current coordinate, plus the appropriate offset based on the
								// iterator and origin.
//								AbsoluteTileCoordinate tileCoordinate =
//										new AbsoluteTileCoordinate((2 * tileX) + f(zx, origin.getDeltaX()), 
//																   (2 * tileY) + f(zy, origin.getDeltaY()),
//																   z, origin); //commented out because findbugs says its DLS: dead store to local variable
//								Tile upperTile;            //commented out because findbugs says its DLS: Dead store to local variable
//								try {
//									upperTile = new Tile(tileCoordinate,
//														 tileStoreReader.getTile(tileProfile.absoluteToCrsCoordinate(tileCoordinate),
//																 		   tileCoordinate.getZoomLevel()));
//								} catch (Exception e) {
//									throw new TilingException("Problem getting tile", e);
//								}
								//Commented out because it is dead code and fixbugs report says it is also redundant nullcheck of null value
//								if (upperTile == null && compliant) {
//									upperTile = createCompliant(z + 1, tileCoordinate, origin, maxZoom);
//								}
//								if (upperTile == null) {
//									g_scaled.setColor(noDataColor);
//									g_scaled.fillRect(zx * TILESIZE, zy * TILESIZE, TILESIZE, TILESIZE);
//								} else {
//									g_scaled.drawImage(upperTile.getImageContents(),
//													   zx * TILESIZE, zy * TILESIZE,
//													   TILESIZE, TILESIZE, null);
//								}
							}
						}
						g.drawImage(preScaled, 0, 0, TILESIZE, TILESIZE, null);
					}
					try {
						tileStoreWriter.addTile(
								tileProfile.absoluteToCrsCoordinate(
										new AbsoluteTileCoordinate(tileY, tileX, z, origin)), z, tileImage);
					} catch (Exception e) {
						throw new TilingException("Problem adding tile", e);
					}
				}
			}
		}
	}

	// TODO Attn Duff: documentation please
	// What, this wasn't immediately obvious? :D
	// the short answer: given a z value of 0 or 1, and a d value of -1 or 1,
	// inverts z if d is -1.
	// the long answer:
	// When stitching tiles from a higher zoom level
	// together to create a tile for this zoom level,
	// we need to get the correct tile coordinates
	// based on the tile set's origin. The origin contains
	// a delta value (d) for the x and y directions, to inform
	// us whether it is a left-to-right or right-to-left
	// system, and top-to-bottom or bottom-to-top system.
	// we always iterate left-to-right and top-to-bottom (i.e.
	// upper left origin) but if our origin is lower right (dx=-1, dy=-1),
	// then we need to transform our iterators like so:
	// +---+---+     +---+---+
	// |0,0|0,1|     |1,1|1,0|
	// +---+---+ --> +---+---+
	// |1,0|1,1|     |0,1|0,0|
	// +---+---+     +---+---+
	// e.g.: getting source tiles to build tile 4,4:
	// z=n	         z=n+1         z=n+1
	//               upper left    lower right
	// +-------+     +---+---+     +---+---+
	// |       |     |8,8|9,8|     |9,9|8,9|
	// |  4,4  |     +---+---+     +---+---+
	// |       |     |8,9|9,9|     |9,8|8,8|
	// +-------+     +---+---+     +---+---+
	/**
	 * given a z value of 0 or 1, and a d value of -1 or 1,
	 * inverts z if d is -1.
	 * @param z the iterator, 0 or 1
	 * @param d the origin delta, -1 or 1
	 * @return the transformed z value
	 */
	private static int f(int z, int d) {
		return d > 0 ? z : z > 0 ? 0 : 1;
	}

	// TODO This can be done via tile store/scheme/profile
	public static <T extends Number> Coordinate<Integer> transform(
			BoundingBox worldBounds, Coordinate<T> input, TileSet tileSet) {
		double inDX = worldBounds.getWidth();
		double inDY = worldBounds.getHeight();
		double outDX = tileSet.getWidth();
		double outDY = tileSet.getHeight();
		int outputX = (int) (((input.getX().doubleValue() - worldBounds
				.getMinX().doubleValue()) / inDX) * outDX) + tileSet.getWest();
		int outputY = (int) (((input.getY().doubleValue() - worldBounds
				.getMinY().doubleValue()) / inDY) * outDY) + tileSet.getSouth();

		return new Coordinate<>(outputX, outputY);
	}

	private Tile createCompliant(int z, Coordinate<Integer> position,
			TileOrigin origin, int maxZoom) throws TilingException {
		// TileMatrixSet tileSet = tileSets.get(z);
		BufferedImage tileImage = new BufferedImage(TILESIZE, TILESIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tileImage.createGraphics();
		if (z < maxZoom) {
			// TileMatrixSet upperTileSet = tileSets.get(z+1);
			for (int zx = 0; zx < 2; ++zx) {
				for (int zy = 0; zy < 2; ++zy) {
					AbsoluteTileCoordinate tileCoordinate = new AbsoluteTileCoordinate(
							(2 * position.getX()) + f(zx, origin.getDeltaX()),
							(2 * position.getY()) + f(zy, origin.getDeltaY()),
							z, origin);
					Tile upperTile;
					try {
						upperTile = new Tile(tileCoordinate, this.tileStoreReader.getTile(this.tileProfile.absoluteToCrsCoordinate(tileCoordinate), tileCoordinate.getZoomLevel()));
					} catch (Exception e) {
						throw new TilingException("Unable to get tile", e);
					}
//Commented out because it is dead code and fixbugs report says it is also redundant nullcheck of null value 
//                   if (upperTile == null) {                                
//						upperTile = createCompliant(z + 1, tileCoordinate,
//								origin, maxZoom);
//					}
					g.drawImage(upperTile.getImageContents(), zx * TILESIZE, zy
							* TILESIZE, TILESIZE, TILESIZE, null);
				}
			}
		} else {
			g.setColor(this.noDataColor);
			g.fillRect(0, 0, TILESIZE, TILESIZE);
		}
		// writeTile(tile);
		try {
			final AbsoluteTileCoordinate tileCoordinate = new AbsoluteTileCoordinate(position.getY(), position.getX(), maxZoom, origin);
		    tileStoreWriter.addTile(tileProfile.absoluteToCrsCoordinate(tileCoordinate), maxZoom, tileImage);
			return new Tile(tileCoordinate, tileImage);
		} catch (Exception e) {
			throw new TilingException("Unable to add tile", e);
		}
	}

	public static BufferedImage convert(Dataset poDataset) {
		Band poBand = null;

		int bandCount = poDataset.getRasterCount();
		ByteBuffer[] bands = new ByteBuffer[bandCount];
		int[] banks = new int[bandCount];
		int[] offsets = new int[bandCount];

		int xsize = poDataset.getRasterXSize();
		int ysize = poDataset.getRasterYSize();
		int pixels = xsize * ysize;
		int buf_type = 0, buf_size = 0;

		for (int band = 0; band < bandCount; band++) {
			/* Bands are not 0-base indexed, so we must add 1 */
			poBand = poDataset.GetRasterBand(band + 1);

			buf_type = poBand.getDataType();
			buf_size = pixels * gdal.GetDataTypeSize(buf_type) / 8;

			ByteBuffer data = ByteBuffer.allocateDirect(buf_size);
			data.order(ByteOrder.nativeOrder());

			int returnVal = 0;
			try {
				returnVal = poBand.ReadRaster_Direct(0, 0, poBand.getXSize(),
						poBand.getYSize(), xsize, ysize, buf_type, data);
			} catch (Exception ex) {
				throw new IllegalArgumentException(
						"Could not read raster data.", ex);
			}
			if (returnVal == gdalconstConstants.CE_None) {
				bands[band] = data;
			} else {
				throw new IllegalArgumentException("No data read!");
			}
			banks[band] = band;
			offsets[band] = 0;
		}

		DataBuffer imgBuffer = null;
		SampleModel sampleModel = null;
		int data_type = 0, buffer_type = 0;

		if (buf_type == gdalconstConstants.GDT_Byte) {
			byte[][] bytes = new byte[bandCount][];
			for (int i = 0; i < bandCount; i++) {
				bytes[i] = new byte[pixels];
				bands[i].get(bytes[i]);
			}
			imgBuffer = new DataBufferByte(bytes, pixels);
			buffer_type = DataBuffer.TYPE_BYTE;
			sampleModel = new BandedSampleModel(buffer_type, xsize, ysize,
					xsize, banks, offsets);
			data_type = (poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) ? BufferedImage.TYPE_BYTE_INDEXED
					: BufferedImage.TYPE_BYTE_GRAY;
		} else if (buf_type == gdalconstConstants.GDT_Int16) {
			short[][] shorts = new short[bandCount][];
			for (int i = 0; i < bandCount; i++) {
				shorts[i] = new short[pixels];
				bands[i].asShortBuffer().get(shorts[i]);
			}
			imgBuffer = new DataBufferShort(shorts, pixels);
			buffer_type = DataBuffer.TYPE_USHORT;
			sampleModel = new BandedSampleModel(buffer_type, xsize, ysize,
					xsize, banks, offsets);
			data_type = BufferedImage.TYPE_USHORT_GRAY;
		} else if (buf_type == gdalconstConstants.GDT_Int32) {
			int[][] ints = new int[bandCount][];
			for (int i = 0; i < bandCount; i++) {
				ints[i] = new int[pixels];
				bands[i].asIntBuffer().get(ints[i]);
			}
			imgBuffer = new DataBufferInt(ints, pixels);
			buffer_type = DataBuffer.TYPE_INT;
			sampleModel = new BandedSampleModel(buffer_type, xsize, ysize,
					xsize, banks, offsets);
			data_type = BufferedImage.TYPE_CUSTOM;
		}

		WritableRaster raster = Raster.createWritableRaster(sampleModel,
				imgBuffer, null);
		BufferedImage img = null;
		ColorModel cm = null;

		if (poBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) {
			data_type = BufferedImage.TYPE_BYTE_INDEXED;
			cm = poBand.GetRasterColorTable().getIndexColorModel(
					gdal.GetDataTypeSize(buf_type));
			img = new BufferedImage(cm, raster, false, null);
		} else {
			System.out.println("band count: " + bandCount);
			ColorSpace cs = null;
			if (bandCount > 2) {
				cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
				if (bandCount == 4) {
					cm = new ComponentColorModel(cs, true, false,
							Transparency.TRANSLUCENT, buffer_type);
				} else {
					cm = new ComponentColorModel(cs, false, false,
							Transparency.OPAQUE, buffer_type);
				}
				img = new BufferedImage(cm, raster, true, null);
			} else {
				img = new BufferedImage(xsize, ysize, data_type);
				img.setData(raster);
			}
		}
		return img;
	}
}
