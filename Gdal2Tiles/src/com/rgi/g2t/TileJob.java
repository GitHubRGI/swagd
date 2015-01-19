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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import com.rgi.common.tile.store.TileStore;

public class TileJob implements Runnable {
	private static final int TILESIZE = 256;
	private static final String TILEEXT = "png";

	private File file = null;
	private String fileName = null;
	private Settings settings = null;
	private Profile profileSetting = null;
	private TileProfile tileProfile = null;
	private SpatialReference inputSRS = null;
	private Dataset inputDS = null;
	private String inputWKT = null;
	private SpatialReference outputSRS = null;
	private Dataset outputDS = null;
	private int minZoom = 0;
	private int maxZoom = 0;
	private Color noDataColor = null;

	// adfGeoTransform[0] /* top left x */
	// adfGeoTransform[1] /* w-e pixel resolution */
	// adfGeoTransform[2] /* 0 */
	// adfGeoTransform[3] /* top left y */
	// adfGeoTransform[4] /* 0 */
	// adfGeoTransform[5] /* n-s pixel resolution (negative value) */
	private double[] inputGT = null;
	private double[] outputGT = null;

	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private List<Future<?>> tasks = new ArrayList<>();

	private TileStore tileStore = null;

	private TaskMonitor monitor;
	private double workTotal = 0;
	private int workUnits = 0;

	public TileJob(File file, TileStore tileStore, Settings settings, TaskMonitor monitor) {
		this.file = file;
		this.tileStore = tileStore;
		this.settings = settings;
		this.monitor = monitor;
	}

	@Override
	public void run() {
		osr.UseExceptions();
		gdal.AllRegister();
		try {
			tile(file);
			System.out.println("Done.");
		} catch (TilingException te) {
			System.err.println("Unable to complete tiling job: "
					+ te.getMessage());
			te.printStackTrace();
		}
	}

	private void tile(File inputFile) throws TilingException {
		profileSetting = Settings.Profile.valueOf(settings
				.get(Setting.TileProfile));
		tileProfile = TileProfileFactory.create(profileSetting.getAuthority(),
				profileSetting.getID());

		inputDS = null;
		try {
			inputDS = gdal.Open(inputFile.getAbsolutePath(),
					gdalconstConstants.GA_ReadOnly);
			if (inputDS == null) {
				System.err.println("GDALOpen failed: " + gdal.GetLastErrorNo());
				System.err.println(gdal.GetLastErrorMsg());
				return;
			}
		} catch (Exception e) {
			System.err.println("Unable to open input file: " + e.getMessage());
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
		if (Arrays.equals(inputGT,
				new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 })
				&& inputDS.GetGCPCount() == 0) {
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
		Coordinate<Double> imageUpperLeft = new Coordinate<Double>(outputGT[0], outputGT[3]);
		Coordinate<Double> imageLowerRight =
				new Coordinate<Double>(outputGT[0] + outputGT[1] * outputDS.getRasterXSize(),
									   outputGT[3] + outputGT[5] * outputDS.getRasterYSize());

		// generate tiles in compliance with TMS spec
		// zoom level resolutions are in powers of 2
		// since we know that 3857 uses meters, we can proceed.
		// TODO: get user's output preference coordinate system
		// TODO: get bounds for user's preferred output coordinate system

		double log = Math.log(2);
		BoundingBox worldBounds = tileProfile.getBounds();
		double totalWorld = worldBounds.getWidth();
		double fractionalZoom = Math.log(worldBounds.getWidth()
				/ (outputGT[1] * TILESIZE))
				/ log; // fractional native zoom level for X
		// if all resolutions are identical, default to zooming larger
		maxZoom = (int) Math.ceil(fractionalZoom);
		double zoomResolution = totalWorld / (Math.pow(2, maxZoom) * TILESIZE);
		double zoomDiff = outputGT[1] - zoomResolution;
		int tempZoom = (int) Math.floor(fractionalZoom);
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		if ((zoomResolution - outputGT[1]) < zoomDiff) {
			maxZoom = tempZoom;
			zoomDiff = outputGT[1] - zoomResolution;
		}
		totalWorld = worldBounds.getHeight();
		fractionalZoom = Math.log(totalWorld / ((-outputGT[5]) * TILESIZE))
				/ log; // fractional native zoom level for X
		tempZoom = (int) Math.ceil(fractionalZoom);
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		if (((-outputGT[5]) - zoomResolution) < zoomDiff) {
			maxZoom = tempZoom;
			zoomDiff = zoomResolution - (-outputGT[5]);
		}
		tempZoom = (int) Math.floor(fractionalZoom);
		zoomResolution = totalWorld / (Math.pow(2, tempZoom) * TILESIZE);
		if ((zoomResolution - (-outputGT[5])) < zoomDiff) {
			maxZoom = tempZoom;
		}

		// next, find the zoom level where the entire image fits one tile
		minZoom = (int) Math.floor(Math.log(worldBounds.getWidth()
				/ (outputGT[1] * outputDS.getRasterXSize()))
				/ log);
		tempZoom = (int) Math.floor(Math.log(worldBounds.getHeight()
				/ ((-outputGT[5]) * outputDS.getRasterYSize()))
				/ log);
		if (tempZoom < minZoom) {
			minZoom = tempZoom;
		}

		// TODO: get user preference for origin, use preference to calculate
		// bounds
		TileOrigin origin = TileOrigin
				.valueOf(settings.get(Setting.TileOrigin));

		int numTilesWidth = 1;
		int numTilesHeight = 1;

		// decrease minimum zoom until the entire image fits entirely within a
		// single tile
		Coordinate<Integer> minZoomUpperLeftTileCoordinate = null;
		Coordinate<Integer> minZoomLowerRightTileCoordinate = null;
		do {
			// TileMatrix minMatrix = new TileMatrix(profile, minZoom, origin);
			TileSet matrixBounds = new TileSet(minZoom, origin);
			int zoomLevelSize = (int) Math.pow(2, minZoom);
			minZoomUpperLeftTileCoordinate = transform(worldBounds,
					imageUpperLeft, matrixBounds);
			minZoomLowerRightTileCoordinate = transform(worldBounds,
					imageLowerRight, matrixBounds);
			numTilesWidth = Math.abs(minZoomUpperLeftTileCoordinate.getX()
					- minZoomLowerRightTileCoordinate.getX()) + 1;
			numTilesHeight = Math.abs(minZoomUpperLeftTileCoordinate.getY()
					- minZoomLowerRightTileCoordinate.getY()) + 1;
			if (numTilesWidth > 1 || numTilesHeight > 1) {
				--minZoom;
			}
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

		int zoomLevelSize = (int) Math.pow(2, maxZoom);

		// srs units (e.g. meters) per pixel = (world size / num tiles) / pixels
		// per tile
		double rx = (tileProfile.getBounds().getWidth() / zoomLevelSize)
				/ TILESIZE;
		double ry = (tileProfile.getBounds().getHeight() / zoomLevelSize)
				/ TILESIZE;

		// pixels = (pixels * meters per pixel) / meters per pixel
		// w' = (w * r) / r'
		int scaledWidth = (int) ((outputDS.getRasterXSize() * outputGT[1]) / rx);
		int scaledHeight = (int) ((outputDS.getRasterYSize() * (-outputGT[5])) / ry);

		// TileMatrixSet maxTileSet = new TileMatrixSet(maxMatrix);
		// Tile upperLeftTile = tileStore.addTile(upperLeftTileCoordinate);
		Dimension2D tileBounds = tileProfile.getTileDimensions(maxZoom);

		// pixels = (meters - meters) / meters per pixel
		int offsetX = (int) ((outputGT[0] - tileBounds.getWidth()) / rx);
		int offsetY = (int) ((tileBounds.getHeight() - outputGT[3]) / ry);

		for (int x = 0; x < numTilesWidth; ++x) {
			int tileX = upperLeftTileCoordinate.getX()
					+ (x * origin.getDeltaX());
			for (int y = 0; y < numTilesHeight; ++y) {
				int tileY = upperLeftTileCoordinate.getY()
						+ (y * origin.getDeltaY());
				BufferedImage tileImage = new BufferedImage(TILESIZE, TILESIZE,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = tileImage.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setColor(noDataColor);
				g.fillRect(0, 0, TILESIZE, TILESIZE);
				g.drawImage(source, offsetX - (x * TILESIZE), offsetY
						- (y * TILESIZE), scaledWidth, scaledHeight, null);
				try {
					tileStore.addTile(tileProfile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(tileY, tileX, maxZoom, origin)), maxZoom, tileImage);
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

			zoomLevelSize = (int) Math.pow(2, z);
			// srs units (e.g. meters) per pixel = (world size / num tiles) /
			// pixels per tile
			rx = (tileProfile.getBounds().getWidth() / zoomLevelSize)
					/ TILESIZE;
			ry = (tileProfile.getBounds().getHeight() / zoomLevelSize)
					/ TILESIZE;

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
				int tileX = upperLeftTileCoordinate.getX()
						+ (x * origin.getDeltaX());
				for (int y = 0; y < numTilesHeight; ++y) {
					int tileY = upperLeftTileCoordinate.getY()
							+ (y * origin.getDeltaY());
					BufferedImage tileImage = new BufferedImage(TILESIZE,
							TILESIZE, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = tileImage.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);

					if (direct) {
						g.drawImage(source, offsetX - (x * TILESIZE), offsetY
								- (y * TILESIZE), scaledWidth, scaledHeight,
								null);
					} else {
						// generate tile using next highest zoom level's tiles.
						BufferedImage preScaled = new BufferedImage(
								2 * TILESIZE, 2 * TILESIZE,
								BufferedImage.TYPE_INT_ARGB);
						Graphics2D g_scaled = preScaled.createGraphics();
						for (int zx = 0; zx < 2; ++zx) {
							for (int zy = 0; zy < 2; ++zy) {
								AbsoluteTileCoordinate tileCoordinate = new AbsoluteTileCoordinate(
										(2 * tileX) + f(zx, origin.getDeltaX()),
										(2 * tileY) + f(zy, origin.getDeltaY()),
										z, origin);
								Tile upperTile;
								try {
									upperTile = new Tile(tileCoordinate, tileStore.getTile(tileProfile.absoluteToCrsCoordinate(tileCoordinate), tileCoordinate.getZoomLevel()));
								} catch (Exception e) {
									throw new TilingException(
											"Problem getting tile", e);
								}
								if (upperTile == null && compliant) {
									upperTile = createCompliant(z + 1,
											tileCoordinate, origin, maxZoom);
								}
								if (upperTile == null) {
									g_scaled.setColor(noDataColor);
									g_scaled.fillRect(zx * TILESIZE, zy
											* TILESIZE, TILESIZE, TILESIZE);
								} else {
									g_scaled.drawImage(
											upperTile.getImageContents(), zx
													* TILESIZE, zy * TILESIZE,
											TILESIZE, TILESIZE, null);
								}
							}
						}
						g.drawImage(preScaled, 0, 0, TILESIZE, TILESIZE, null);
					}
					try {
						tileStore.addTile(tileProfile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(tileY, tileX, z, origin)), z, tileImage);
					} catch (Exception e) {
						throw new TilingException("Problem adding tile", e);
					}
				}
			}
		}
	}

	private int f(int z, int d) {
		return d > 0 ? z : z > 0 ? 0 : 1;
	}

	@SuppressWarnings("unchecked")
	public <T extends Number, V extends Number> Coordinate<V> transform(
			BoundingBox worldBounds, Coordinate<T> input, TileSet tileSet) {
		double inDX = worldBounds.getWidth();
		double inDY = worldBounds.getHeight();
		double outDX = tileSet.getWidth();
		double outDY = tileSet.getHeight();
		int outputX = (int) (((input.getX().doubleValue() - worldBounds
				.getMinX().doubleValue()) / inDX) * outDX) + tileSet.getWest();
		int outputY = (int) (((input.getY().doubleValue() - worldBounds
				.getMinY().doubleValue()) / inDY) * outDY) + tileSet.getSouth();

		return (Coordinate<V>) new Coordinate<Integer>(outputX, outputY);
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
						upperTile = new Tile(tileCoordinate, tileStore.getTile(tileProfile.absoluteToCrsCoordinate(tileCoordinate), tileCoordinate.getZoomLevel()));
					} catch (Exception e) {
						throw new TilingException("Unable to get tile", e);
					}
					if (upperTile == null) {
						upperTile = createCompliant(z + 1, tileCoordinate,
								origin, maxZoom);
					}
					g.drawImage(upperTile.getImageContents(), zx * TILESIZE, zy
							* TILESIZE, TILESIZE, TILESIZE, null);
				}
			}
		} else {
			g.setColor(noDataColor);
			g.fillRect(0, 0, TILESIZE, TILESIZE);
		}
		// writeTile(tile);
		try {
			final AbsoluteTileCoordinate tileCoordinate = new AbsoluteTileCoordinate(position.getY(), position.getX(), maxZoom, origin);
		    tileStore.addTile(tileProfile.absoluteToCrsCoordinate(tileCoordinate), maxZoom, tileImage);
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
