/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.rgi.g2t;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Steven D. Lander
 *
 */
public class GdalTileJob implements Runnable {

	private final TileStoreWriter writer;
	private final CrsProfile crsProfile;
	private final File file;
	private final Path outputFolder;
	private final Dimensions<Integer> tileDimensions;
	private final TaskMonitor monitor;
	private final Color noDataColor;

	private final int tileSize = 256;
	private final String resamplingAlgorithm = "average";
	private final int gdalGeoQuerySize = 4 * this.tileSize;
	private final TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * @param file
	 * @param writer
	 * @param tileDimensions
	 * @param noDataColor
	 * @param monitor
	 */
	public GdalTileJob(final File file,
					   final TileStoreWriter writer,
					   final Dimensions<Integer> tileDimensions,
					   final Color noDataColor,
					   final TaskMonitor monitor)
	{
		this.file = file;
		this.writer = writer;
		this.tileDimensions = tileDimensions;
		this.monitor = monitor;
		this.noDataColor = noDataColor;
		this.crsProfile = CrsProfileFactory.create(writer.getCoordinateReferenceSystem());
		this.outputFolder = Paths.get("/data/tiles/swagd");
	}

	@Override
	public void run() {
		try
		{
			final Dataset inputDataset = this.openInput();
			final Dataset outputDataset = this.openOutput(inputDataset, this.openInputSrs(inputDataset));
			final BoundingBox outputBounds = this.getOutputBounds(outputDataset);
			final List<Range<Coordinate<Integer>>> ranges = this.calculateTileRangesForAllZooms(outputBounds);
			// Generate base tiles
			try
			{
				final int maxZoom = this.maximalZoomForPixelSize(outputDataset, outputBounds, ranges);
				this.generateBaseTiles(outputDataset, ranges.get(maxZoom), maxZoom);
				System.out.println("Base tiles finished generating.");
			}
			catch(TilingException | TileStoreException  ex1)
			{
				ex1.printStackTrace();
			}
			// Generate overview tiles
			try
			{
				final int minZoom = this.minimalZoomForPixelSize(outputDataset, outputBounds, ranges);
				this.generateOverviewTiles();
			}
			catch(final TileStoreException ex1)
			{
				ex1.printStackTrace();
			}
		}
		catch(final TilingException ex1)
		{
			// TODO: handle tiling failure
			ex1.printStackTrace();
		}
	}

	private Dataset openInput() throws TilingException
	{
		osr.UseExceptions();
		// Register gdal for use
		gdal.AllRegister();
		// TODO: Check memory driver in case gdal is configured incorrectly?
		final Dataset dataset = gdal.Open(this.file.toPath().toString(), gdalconstConstants.GA_ReadOnly);
		// TODO: What happens if it cannot open this?
		if (dataset.GetRasterBand(1).GetColorTable() != null)
		{
			// TODO: make a temp vrt with gdal_translate to expand this to RGB/RGBA
			System.out.println("expand this raster to RGB/RGBA");
		}
		if (dataset.GetRasterCount() == 0)
		{
			throw new TilingException("Input file has no raster band.");
		}
		final SpatialReference inputSrs = this.openInputSrs(dataset);
		// We cannot tile an image with no geo referencing information
		if (this.datasetHasNoGeoReference(dataset))
        {
            throw new TilingException("Input raster image has no georeference.");
        }

		return dataset;
	}

	private Dataset openOutput(final Dataset inputDataset, final SpatialReference inputSrs) throws TilingException
	{
		final Dataset outputDataset;
		// Get the output SRS
		final SpatialReference outputSrs = this.openOutputSrs(this.crsProfile.getCoordinateReferenceSystem().getIdentifier());
		// If input srs and output srs are not the same, reproject by making a VRT
		if (inputSrs.ExportToProj4() != outputSrs.ExportToProj4() || inputDataset.GetGCPCount() == 0)
		{
			// Create a warped VRT
			outputDataset = gdal.AutoCreateWarpedVRT(inputDataset, inputSrs.ExportToWkt(), outputSrs.ExportToWkt());
		}
		else
		{
			// The input and output projections are the same, no reprojection needed
			outputDataset = inputDataset;
		}
		//return this.correctNoData(outputDataset, this.getNoDataValues(inputDataset));
		return this.correctNoDataSimple(outputDataset);
	}

	private SpatialReference openInputSrs(final Dataset dataset) throws TilingException
	{
		final SpatialReference srs = new SpatialReference();
		// Get the well-known-text of this dataset
		String wkt = dataset.GetProjection();
		if (wkt.isEmpty() && dataset.GetGCPCount() != 0)
		{
			// If the wkt is empty and there are GCPs...
			wkt = dataset.GetGCPProjection();
		}
		if (!wkt.isEmpty())
		{
			// Initialize the srs from the non-empt wkt string
			srs.ImportFromWkt(wkt);
			return srs;
		}
		throw new TilingException("Cannot get source file spatial reference system.");
	}

	private SpatialReference openOutputSrs(final int identifier)
	{
		final SpatialReference srs = new SpatialReference();
		// Import from an EPSG code, i.e., 3857, 4326, 900913
		srs.ImportFromEPSG(identifier);
		return srs;
	}

	private boolean datasetHasNoGeoReference(final Dataset dataset)
	{
		// Specify what an empty georeference is
		final double[] emptyGeoReference = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
		// Compare dataset geotransform to an empty geotransform and ensure there are no GCPs
		return Arrays.equals(dataset.GetGeoTransform(), emptyGeoReference) && dataset.GetGCPCount() == 0;
	}

	private Double[] getNoDataValues(final Dataset dataset)
	{
		// Initialize a new double array of size 3
		final Double[] noDataValues = new Double[3];
		// Get the nodata value for each band
		IntStream.range(1,  dataset.GetRasterCount() + 1).forEach(band ->
		{
			final Double[] noDataValue = new Double[1];
			dataset.GetRasterBand(band).GetNoDataValue(noDataValue);
			if (noDataValue.length != 0 && noDataValue[0] != null)
			{
				// Assumes only one value coming back from the band
				noDataValues[band-1] = noDataValue[0];
			}
		});
		// Is array still using the initialized values?
		if (noDataValues[0] == null && noDataValues[1] == null && noDataValues[2] == null)
		{
			return new Double[0];
		}
		// TODO: Is it possible to see a raster from GDAL with 2 bands? I think
		// only Mono and RGB options are possible
		if (noDataValues[0] != null)
		{
			noDataValues[1] = noDataValues[0];
			noDataValues[2] = noDataValues[0];
		}
		return noDataValues;
	}

	private Dataset correctNoDataSimple(final Dataset dataset)
	{
		boolean datasetHasAlphaBand = false;
		// Iterate through the bands to see if any are tagged alpha
		final int[] bands = IntStream.range(1, dataset.GetRasterCount()).toArray();
		for (final int nBand : bands)
		{
			final Band band = dataset.GetRasterBand(nBand);
			if (band.GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
			{
				datasetHasAlphaBand = true;
			}
		}
		// If the dataset actually has an alpha band, return it
		if (datasetHasAlphaBand)
		{
			return dataset;
		}
		// Dataset has no alpha and is NOT a VRT
		if (!dataset.GetDriver().getShortName().equalsIgnoreCase("VRT"))
		{
			// Create a vrt of this dataset
			final Dataset vrtCopy = gdal.AutoCreateWarpedVRT(dataset);
			// Add an alpha band
			vrtCopy.AddBand(gdalconstConstants.GDT_Byte);
			// A new band added is always the last, per docs
			vrtCopy.GetRasterBand(vrtCopy.GetRasterCount()).SetColorInterpretation(gdalconstConstants.GCI_AlphaBand);
			return vrtCopy;
		}
		// Dataset has no alpha and IS a VRT
		dataset.AddBand(gdalconstConstants.GDT_Byte);
		dataset.GetRasterBand(dataset.GetRasterCount()).SetColorInterpretation(gdalconstConstants.GCI_AlphaBand);
		return dataset;
	}

	private int getAlphaBandIndex(final Dataset dataset) throws TilingException
	{
		final int[] bands = IntStream.range(1, dataset.GetRasterCount()).toArray();
		for (final int nBand : bands)
		{
			final Band band = dataset.GetRasterBand(nBand);
			if (band.GetColorInterpretation() == gdalconstConstants.GCI_AlphaBand)
			{
				return nBand;
			}
		}
		throw new TilingException("No Alpha band detected.  Call getAlphaBandIndex after correctNoDataSimple");
	}

	/*
	private Dataset correctNoData(final Dataset dataset, final Double[] noDataValues) throws TilingException
	{
		if (noDataValues.length > 0)
		{
			try
			{
				final File tempFile = this.tempFolder.newFile();
				// Create a vrt copy of dataset saved to tempfile
				dataset.GetDriver().CreateCopy(tempFile.toPath().toString(), dataset);
				// Open the tempfile as a text file
				final Document vrtXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempFile.toPath().toString());
				vrtXml.normalize();
				// Insert noDataValues into the tempfile in the VRTDataset root element
				final Node gdalWarpOptions = vrtXml.getElementsByTagName("GDALWarpOptions").item(0);
				// Add the Option node for INIT_DEST
				final Element initDestOption = vrtXml.createElement("Option");
				initDestOption.setAttribute("name", "INIT_DEST");
				initDestOption.appendChild(vrtXml.createTextNode("NODATA"));
				gdalWarpOptions.appendChild(initDestOption);
				// Add the Option element for UNIFIED_SRC_NODATA
				final Element unifiedSrcNodataOption = vrtXml.createElement("Option");
				unifiedSrcNodataOption.setAttribute("name", "UNIFIED_SRC_NODATA");
				unifiedSrcNodataOption.appendChild(vrtXml.createTextNode("YES"));
				gdalWarpOptions.appendChild(unifiedSrcNodataOption);
                // Get the node containing the band list mappings, we are going to remove
				// and replace them. There should only be one BandList node...
				final Node bandList = vrtXml.getElementsByTagName("BandList").item(0);
				// Remove all the current band info, it should be empty
				IntStream.range(1, bandList.getChildNodes().getLength()).forEach(childNodeNumber ->
				{
					// Keep removing the first until they are all gone
					bandList.removeChild(bandList.getChildNodes().item(0));
				});
				// Add the new band/nodata info
				IntStream.range(1, noDataValues.length).forEach(bandNumber ->
				{
					final Element bandMapping = vrtXml.createElement("BandMapping");
					bandMapping.setAttribute("src", String.format("{0}", bandNumber));
					bandMapping.setAttribute("dst", String.format("{0}", bandNumber));
					// SrcNoDataReal
					final Element srcNoDataReal = vrtXml.createElement("SrcNoDataReal");
					srcNoDataReal.appendChild(vrtXml.createTextNode(noDataValues[bandNumber-1].toString()));
					bandMapping.appendChild(srcNoDataReal);
					// SrcNoDataImag
					final Element srcNoDataImag = vrtXml.createElement("SrcNoDataImag");
					srcNoDataImag.appendChild(vrtXml.createTextNode("0"));
					bandMapping.appendChild(srcNoDataImag);
					// DstNoDataReal
					final Element dstNoDataReal = vrtXml.createElement("DstNoDataReal");
					dstNoDataReal.appendChild(vrtXml.createTextNode(noDataValues[bandNumber-1].toString()));
					//dstNoDataReal.setTextContent(noDataValues[bandNumber-1].toString());
					bandMapping.appendChild(dstNoDataReal);
					// DstNoDataImag
					final Element dstNoDataImag = vrtXml.createElement("DstNoDataImag");
					dstNoDataImag.appendChild(vrtXml.createTextNode("0"));
					bandMapping.appendChild(dstNoDataImag);
				});
				this.saveXmlToDisk(vrtXml, tempFile);
				// Open tempfile using gdal.Open() and return
				final Dataset resultDataset = gdal.Open(tempFile.toPath().toString());
				resultDataset.SetMetadataItem("NODATA_VALUES", String.format("{0} {1} {2}", noDataValues[0], noDataValues[1], noDataValues[2]));
				return resultDataset;
			}
			catch (SAXException | IOException | ParserConfigurationException ex1)
			{
				ex1.printStackTrace();
				throw new TilingException("Could not correct output dataset NODATA values.");
			}
		}
		return this.correctNoDataMono(dataset);
	}

	private Dataset correctNoDataMono(final Dataset dataset) throws TilingException
	{
		// Correction of AutoCreateWarpedVRT images for Mono and RGB files without NODATA
		// Equivalent to gdalwarp -dstapha
		if (dataset.getRasterCount() != 1 && dataset.getRasterCount() != 3)
		{
			return dataset;
		}
		try
		{
			//final File tempFile = this.tempFolder.newFile();
			final File tempFile = File.createTempFile("swagd" + Long.toString(System.nanoTime()), ".vrt");
			dataset.GetDriver().CreateCopy(tempFile.toPath().toString(), dataset);
			//final DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final Document vrtXml = factory.newDocumentBuilder().parse(tempFile.toPath().toString());
			vrtXml.normalize();
			final Element vrtXmlElement = vrtXml.getDocumentElement();
			// Create a new raster band element for the alpha band
			final Element vrtRasterBand = vrtXml.createElement("VRTRasterBand");
			vrtRasterBand.setAttribute("dataType", "Byte");
			vrtRasterBand.setAttribute("band", String.format("%d", dataset.GetRasterCount()+1));
			vrtRasterBand.setAttribute("subClass", "VRTWarpedRasterBand");
			// Create the sub element for vrtRasterBand
			final Element colorInterp = vrtXml.createElement("ColorInterp");
			//colorInterp.setTextContent("Alpha");
			colorInterp.appendChild(vrtXml.createTextNode("Alpha"));
			vrtRasterBand.appendChild(colorInterp);
			vrtXmlElement.appendChild(vrtRasterBand);
			// Get the GDAL Warp options node
			final Node gdalWarpOptions = vrtXml.getElementsByTagName("GDALWarpOptions").item(0);
			// Set the init dest option
			//final Element initDestOption = vrtXml.createElement("Option");
			//initDestOption.setAttribute("name", "INIT_DEST");
			//initDestOption.setTextContent("0"); //TODO: Ensure this omits quotation marks
			//initDestOption.appendChild(vrtXml.createTextNode("0"));
			//gdalWarpOptions.appendChild(initDestOption);
			// Save xml
			this.saveXmlToDisk(vrtXml, tempFile);
			// Open tempfile using gdal.Open() and return
			final Dataset resultDataset = gdal.Open(tempFile.toPath().toString());
			return resultDataset;
		}
		catch (SAXException | IOException | ParserConfigurationException ex1)
		{
			ex1.printStackTrace();
			throw new TilingException("Error encountered adding XML to VRT.");
		}
	}
	*/

	private void saveXmlToDisk(final Document xml, final File location) throws TilingException
	{
		// Write the tempfile changes to disk
		try
		{
			// normalize
			xml.normalize();
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			// Create a tempfile
			final StreamResult result = new StreamResult(location);
			final DOMSource source = new DOMSource(xml);
			// Apply the transformer
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
		}
		catch(final TransformerException ex1)
		{
			ex1.printStackTrace();
			throw new TilingException("Error saving modified VRT to disk.");
		}
		catch(final Exception ex1)
		{
			ex1.printStackTrace();
			throw new TilingException("Generic exception caught during XML transformation.");
		}
	}

	private int getRasterBandCount(final Dataset dataset, final Band alphaBand)
	{
		// TODO: The bitwise calc functionality needs to be verified from the python functionality
		final boolean bitwiseAlpha = (alphaBand.GetMaskFlags() & gdalconstConstants.GMF_ALPHA) != 0;
		if (bitwiseAlpha || dataset.GetRasterCount() == 4 || dataset.GetRasterCount() == 2)
		{
			return dataset.GetRasterCount() - 1;
		}
		return dataset.GetRasterCount();
	}

	private BoundingBox getOutputBounds(final Dataset dataset) throws TilingException
	{
		final double[] outputGeotransform = dataset.GetGeoTransform();
		// Report error in case rotation/skew is in geotransform (only for raster profile)
		if (outputGeotransform[2] != 0 && outputGeotransform[4] != 0)
		{
			throw new TilingException("Georeference of the raster contains rotation or skew. " +
					"Such raster is not supported. Please use gdalwarp first.");
		}
		final double minX = outputGeotransform[0];
		final double maxX = outputGeotransform[0] + dataset.GetRasterXSize() * outputGeotransform[1];
		final double maxY = outputGeotransform[3];
		final double minY = outputGeotransform[3] - dataset.GetRasterYSize() * outputGeotransform[1];
		return new BoundingBox(minX, minY, maxX, maxY);
	}

	private List<Range<Coordinate<Integer>>> calculateTileRangesForAllZooms(final BoundingBox outputBounds)
	{
		final List<Range<Coordinate<Integer>>> tilesRangeByZoom = new ArrayList<Range<Coordinate<Integer>>>();
		// Get the crs coordinates of the output bounds
		final CrsCoordinate topLeft = new CrsCoordinate(outputBounds.getTopLeft(), this.crsProfile.getCoordinateReferenceSystem());
		final CrsCoordinate bottomRight = new CrsCoordinate(outputBounds.getBottomRight(), this.crsProfile.getCoordinateReferenceSystem());
		// Get the tile coordinates of the bounding box for each zoom level
		IntStream.range(0, 32).forEach(zoom ->
		{
			final TileMatrixDimensions tileMatrixDimensions = this.writer.getTileScheme().dimensions(zoom);
			final Coordinate<Integer> topLeftTile = this.crsProfile.crsToTileCoordinate(topLeft, this.crsProfile.getBounds(), tileMatrixDimensions, this.writer.getTileOrigin());
			final Coordinate<Integer> bottomRightTile = this.crsProfile.crsToTileCoordinate(bottomRight, this.crsProfile.getBounds(), tileMatrixDimensions, this.writer.getTileOrigin());
			tilesRangeByZoom.add(zoom, new Range<Coordinate<Integer>>(topLeftTile, bottomRightTile));
		});
		return tilesRangeByZoom;
	}

	private int minimalZoomForPixelSize(final Dataset dataset, final BoundingBox outputBounds, final List<Range<Coordinate<Integer>>> tileRanges) throws TileStoreException
	{
		final double pixelSize = dataset.GetGeoTransform()[1];
		final double zoomPixelSize = (pixelSize * Math.max(dataset.GetRasterXSize(), dataset.GetRasterYSize()) / this.tileSize);
		try
		{
			return this.zoomLevelForPixelSize(zoomPixelSize, outputBounds, tileRanges);
		}
		catch(final NumberFormatException nfe)
		{
			System.out.println("Could not determine minimal zoom, defaulting to 0.");
		}
		// In the worst case, zoom level zero will have only one tile...
		return 0;
	}

	private int maximalZoomForPixelSize(final Dataset dataset, final BoundingBox outputBounds, final List<Range<Coordinate<Integer>>> tileRanges) throws TileStoreException, TilingException
	{
		// default resolution, get the closest possible zoom level up on the raster resolution
		final double zoomPixelSize = dataset.GetGeoTransform()[1];
		try
		{
			return this.zoomLevelForPixelSize(zoomPixelSize, outputBounds, tileRanges);
		}
		catch(final NumberFormatException nfe)
		{
			// A default base level would be bad in all cases
			throw new TilingException("Could not determine output raster base zoom level.");
		}
	}

	private int zoomLevelForPixelSize(final double zoomPixelSize, final BoundingBox outputBounds, final List<Range<Coordinate<Integer>>> tileRanges) throws TileStoreException
	{
		final int[] zooms = IntStream.range(0, 31).toArray();
		for(final int zoom : zooms)
		{
			// Get the tile coordinates of the top-left and bottom-right tiles
			final Coordinate<Integer> topLeftTile = this.writer.crsToTileCoordinate(new CrsCoordinate(outputBounds.getTopLeft(), this.crsProfile.getCoordinateReferenceSystem()), zoom);
			final Coordinate<Integer> bottomRightTile = this.writer.crsToTileCoordinate(new CrsCoordinate(outputBounds.getBottomRight(), this.crsProfile.getCoordinateReferenceSystem()), zoom);
			// Convert tile coordinates to crs coordinates: this will give us correct units-of-measure-per-pixel
			// This is tile data *plus* padding to the full tile grid
			final TileMatrixDimensions tileMatrixDimensions = this.writer.getTileScheme().dimensions(zoom);
			final CrsCoordinate topLeftCrsFull = this.crsProfile.tileToCrsCoordinate(topLeftTile.getX(),
																					 topLeftTile.getY() + 1,
																					 this.crsProfile.getBounds(),
																					 tileMatrixDimensions,
																					 this.writer.getTileOrigin());
			final CrsCoordinate bottomRightCrsFull = this.crsProfile.tileToCrsCoordinate(bottomRightTile.getX() + 1,
																					     bottomRightTile.getY(),
																					     this.crsProfile.getBounds(),
																					     tileMatrixDimensions,
																					     this.writer.getTileOrigin());
			// bounding box is made with minx, miny, maxx, maxy
			final double width = (new BoundingBox(topLeftCrsFull.getX(), bottomRightCrsFull.getY(), bottomRightCrsFull.getX(), topLeftCrsFull.getY())).getWidth();
			// get how many tiles wide this zoom will be so that number can be multiplied by tile size
			final int zoomTilesWide = tileRanges.get(zoom).getMaximum().getX() - tileRanges.get(zoom).getMinimum().getX() + 1;
			final double zoomResolution = width / (zoomTilesWide * this.tileSize);
			if (zoomPixelSize > zoomResolution)
			{
				return zoom == 0 ? 0 : zoom - 1;
			}
		}
		throw new NumberFormatException("Could not determine zoom level for pixel size: " + String.valueOf(zoomPixelSize));
	}

	private void generateBaseTiles(final Dataset dataset, final Range<Coordinate<Integer>> baseZoomRange, final int baseZoom) throws TilingException
	{
		// Create a tile folder name
		final String inputRasterName = this.file.getName();
		final String outputTileFolderName = inputRasterName.substring(0, inputRasterName.lastIndexOf("."));
		final Path outputFolderPath = Paths.get(this.outputFolder.toString(), outputTileFolderName);
		final int tileBandCount = dataset.GetRasterCount();
		final int dataBandCount = tileBandCount - 1; // This assumes there is an alpha band
		if (outputFolderPath.toFile().exists())
		{
			// TODO: Handle this better by renaming
			throw new TilingException("Output folder exists...");
		}
		final Coordinate<Integer> topLeftCoordinate = baseZoomRange.getMinimum();
		final Coordinate<Integer> bottomRightCoordinate = baseZoomRange.getMaximum();
		// Set x/y min/max values
		final int tileMinX = topLeftCoordinate.getX();
		final int tileMaxX = bottomRightCoordinate.getX();
		final int tileMinY = bottomRightCoordinate.getY();
		final int tileMaxY = topLeftCoordinate.getY();
		// Calculate a total tile count: (BR.X - TL.X) * (TL.Y - BR.Y)
		//final int totalXTiles = 1 + Math.abs((tileMaxX - tileMinX));
		//final int totalYTiles = 1 + Math.abs((tileMaxY - tileMinY));
		//final int totalTileCount = (totalXTiles * totalYTiles);
		// Create a for loop for tile y, counting down
		for (int tileY = tileMaxY; tileY >= tileMinY; tileY--)
		{
			// Create a for loop for tile x, counting up
			// This makes queries start at top-left of the data
			for (int tileX = tileMinX; tileX <= tileMaxX; tileX++)
			{
				// Resolve a tile path and name
				//final Path tilePath = this.location;
				final Path tilePath = Paths.get(outputFolderPath.toString(),
												String.valueOf(baseZoom),
												String.valueOf(tileX),
												String.valueOf(tileY) + "." + "png"); //imageOutputFormat.getSubType());
				// TODO: Figure out if clobbering should occur
				// Create directories if they dont exist
				tilePath.getParent().toFile().mkdirs();
				// Get bounding box of the tile
				final TileMatrixDimensions tileMatrixDimensions = this.writer.getTileScheme().dimensions(baseZoom);
				final CrsCoordinate tileTopLeftCorner = this.crsProfile.tileToCrsCoordinate(tileX, tileY + 1, this.crsProfile.getBounds(), tileMatrixDimensions, this.writer.getTileOrigin());
				final CrsCoordinate tileBottomRightCorner = this.crsProfile.tileToCrsCoordinate(tileX + 1, tileY, this.crsProfile.getBounds(), tileMatrixDimensions, this.writer.getTileOrigin());
				final BoundingBox tileBBox = new BoundingBox(tileTopLeftCorner.getX(), tileBottomRightCorner.getY(), tileBottomRightCorner.getX(), tileTopLeftCorner.getY());
				// Perform a geoquery
				// TODO: I don't like this.  The geoquery logic should be split out into different functions.
				//  	 Also, the variable names dont mean anything.
				final double[] geoTransform = dataset.GetGeoTransform();
				int readX = (int)((tileBBox.getMinX() - geoTransform[0]) / geoTransform[1] + 0.001);
				int readY = (int)((tileBBox.getMaxY() - geoTransform[3]) / geoTransform[5] + 0.001);
				int readXSize = (int)((tileBBox.getMaxX() - tileBBox.getMinX()) / geoTransform[1] + 0.5);
				int readYSize = (int)((tileBBox.getMinY() - tileBBox.getMaxY()) / geoTransform[5] + 0.5);
				int writeXSize = this.gdalGeoQuerySize;
				int writeYSize = this.gdalGeoQuerySize;
				int writeX = 0;
				int writeY = 0;
				if (readX < 0)
				{
					final int readXShift = Math.abs(readX);
					writeX = (int)(writeXSize * ((double)readXShift / readXSize));
					writeXSize -= writeX;
					readXSize -= (int)(readXSize * ((double)readXShift) / readXSize);
					readX = 0;
				}
				if (readX + readXSize > dataset.GetRasterXSize())
				{
					writeXSize = (int)(writeXSize * ((double)(dataset.GetRasterXSize() - readX) / readXSize));
					readXSize = dataset.GetRasterXSize() - readX;
				}
				if (readY < 0)
				{
					final int readYShift = Math.abs(readY);
					writeY = (int)(writeYSize * ((double)readYShift / readYSize));
					writeYSize -= writeY;
					readYSize -= (int)(readYSize * ((double)readYShift / readYSize));
					readY = 0;
				}
				if (readY + readYSize > dataset.GetRasterYSize())
				{
					writeYSize = (int)(writeYSize * ((double)(dataset.GetRasterYSize() - readY) / readYSize));
					readYSize = dataset.GetRasterYSize() - readY;
				}
				// Create the tile in memory (member var of the memory driver does not seem to work...)
				final Dataset tileDataInMemory = gdal.GetDriverByName("MEM").Create("", this.tileSize, this.tileSize, tileBandCount);
				// Create a list of the bands to query on the raster
				// Create the array containing the results of the read raster query
				final int[] bandList = IntStream.range(1, dataBandCount + 1).toArray();
				final byte[] regularArrayOut = new byte[writeXSize * writeYSize * dataBandCount];
				// Read dat raster using a Byte array type (GDT_Byte)
				final int foo = dataset.ReadRaster(readX, readY, readXSize, readYSize, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, regularArrayOut, bandList);
				if (foo == gdalconstConstants.CE_Failure)
                {
                    System.out.println("fail");
                }
				if (foo == gdalconstConstants.CE_None)
                {
                    System.out.println("none");
                }
				// Create the alpha array out for just one band
				final byte[] alphaRegularArrayOut = new byte[writeXSize * writeYSize];
				// Get alpha as well
				//this.getAlphaBand(dataset).ReadRaster(readX, readY, readXSize, readYSize, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, alphaRegularArrayOut);
				dataset.GetRasterBand(dataset.GetRasterCount()).ReadRaster(readX, readY, readXSize, readYSize, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, alphaRegularArrayOut);
				// TODO: logic goes here in the case that the querysize == tile size (gdalconstConstants.GRA_NearestNeighbour) (write directly)
				// Time to start writing the tile
				final Dataset querySizeDataInMemory = gdal.GetDriverByName("MEM").Create("", this.gdalGeoQuerySize, this.gdalGeoQuerySize, tileBandCount);
				// Python bindings use a call with just 6 params, I am just guessing at which of the many java overloads is needed here....
				//querySizeDataInMemory.WriteRaster(readX, readY, writeX, writeY, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, regularArrayOut, bandList);
				querySizeDataInMemory.WriteRaster(writeX, writeY, writeXSize, writeYSize, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, regularArrayOut, bandList);
				// Same for alpha
				final int[] tileBandAlpha = {tileBandCount};
				querySizeDataInMemory.WriteRaster(writeX, writeY, writeXSize, writeYSize, writeXSize, writeYSize, gdalconstConstants.GDT_Byte, alphaRegularArrayOut, tileBandAlpha);
				// Scale each band of tileDataInMemory down to the tile size (down from the query size)
				this.scaleQueryToTileSize(querySizeDataInMemory, tileDataInMemory);
				// Write tile to disk, strict=0 (false)
				gdal.GetDriverByName("PNG").CreateCopy(tilePath.toString(), tileDataInMemory, 0);
				//System.out.println("check");
			}
		}
	}

	private void scaleQueryToTileSize(final Dataset queryDataset, final Dataset tileDataInMemory) throws TilingException
	{
		final int querySize = queryDataset.GetRasterXSize();
		final int tileSize = tileDataInMemory.GetRasterXSize();
		final int tileBands = tileDataInMemory.GetRasterCount();
		// This is *just* for the average resampling algorithm only (gdalconstConstants.GRA_Average)
		for (final int band : IntStream.range(1, tileBands+1).toArray())
		{
			final int resolution = gdal.RegenerateOverview(queryDataset.GetRasterBand(band), tileDataInMemory.GetRasterBand(band), "average");
			if (resolution != 0)
			{
				throw new TilingException("Could not RegenerateOverview on band: " + String.valueOf(band));
			}
		}
		// TODO: Implement for all other algorithms
	}

	private void generateOverviewTiles()
	{
		// TODO:
	}
}