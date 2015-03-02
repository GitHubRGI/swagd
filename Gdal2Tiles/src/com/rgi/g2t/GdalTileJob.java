package com.rgi.g2t;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.activation.MimeType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.rgi.common.BoundingBox;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.task.Settings;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Steven D. Lander
 *
 */
public class GdalTileJob implements Runnable {
	
	private final TileStoreWriter writer;
	private final TileScheme tileScheme;
	private final CrsProfile crsProfile;
	private final Path location;
	private final MimeType imageOutputFormat;
	private final Settings settings;
	
	private final int tileSize = 256;
	private final String resamplingAlgorithm = "average";
	private final Driver memoryDriver = gdal.GetDriverByName("MEM");
	private final int gdalGeoQuerySize = 4 * tileSize;
	private final TemporaryFolder tempFolder = new TemporaryFolder();
	
	/**
	 * @param writer
	 * @param tileScheme 
	 * @param crsProfile
	 * @param location
	 * @param imageOutputFormat
	 * @param settings
	 */
	public GdalTileJob(final TileStoreWriter writer,
					   final TileScheme tileScheme,
					   final CrsProfile crsProfile,
					   final Path location,
					   final MimeType imageOutputFormat,
					   final Settings settings)
	{
		this.writer = writer;
		this.tileScheme = tileScheme;
		this.crsProfile = crsProfile;
		this.location = location;
		this.imageOutputFormat = imageOutputFormat;
		this.settings = settings;
		// TODO: Implement different resampling methods?
	}

	@Override
	public void run() {
		try
		{
			final Dataset inputDataset = this.openInput();
			final Dataset outputDataset = this.openOutput(inputDataset, this.openInputSrs(inputDataset));
			
			//this.generateBaseTiles(dataset, tileStoreWriter);
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
		Dataset dataset = gdal.Open(this.location.toString(), gdalconstConstants.GA_ReadOnly);
		// TODO: What happens if it cannot open this?
		if (dataset.GetRasterBand(1).GetColorTable() != null)
		{
			// TODO: make a temp vrt with gdal_translate to expand this to RGB/RGBA
		}
		final SpatialReference inputSrs = this.openInputSrs(dataset);
		// We cannot tile an image with no geo referencing information
		if (this.datasetHasNoGeoReference(dataset)) throw new TilingException("Input raster image has no georeference.");
		
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
		return this.correctNoData(outputDataset, this.getNoDataValues(inputDataset));
	}
	
	private SpatialReference openInputSrs(final Dataset dataset) throws TilingException
	{
		final SpatialReference srs = new SpatialReference();
		// Get the well-known-text of this dataset
		String wkt = dataset.GetGCPProjection();
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
		Double[] noDataValues = new Double[3];
		// Get the nodata value for each band
		IntStream.range(1,  dataset.GetRasterCount() + 1).forEach(band ->
		{
			Double[] noDataValue = new Double[1];
			dataset.GetRasterBand(band).GetNoDataValue(noDataValue);
			if (noDataValue.length != 0 && noDataValue != null)
			{
				// Assumes only one value coming back from the band
				noDataValues[band-1] = noDataValue[0];
			}
		});
		// TODO: Is it possible to see a raster from GDAL with 2 bands? I think
		// only Mono and RGB options are possible
		if (noDataValues.length == 1)
		{
			noDataValues[1] = noDataValues[0];
			noDataValues[2] = noDataValues[0];
		}
		return noDataValues;
	}
	
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
				// Insert noDataValues into the tempfile in the VRTDataset root element
				final Node gdalWarpOptions = vrtXml.getElementsByTagName("GDALWarpOptions").item(0);
				// Add the Option node for INIT_DEST
				final Element initDestOption = vrtXml.createElement("Option");
				initDestOption.setAttribute("name", "INIT_DEST");
				//initDestOption.appendChild(vrtXml.createTextNode("NODATA"));
				initDestOption.setTextContent("NODATA");
				gdalWarpOptions.appendChild(initDestOption);
				// Add the Option element for UNIFIED_SRC_NODATA
				final Element unifiedSrcNodataOption = vrtXml.createElement("Option");
				unifiedSrcNodataOption.setAttribute("name", "UNIFIED_SRC_NODATA");
				//unifiedSrcNodataOption.appendChild(vrtXml.createTextNode("YES"));
				unifiedSrcNodataOption.setTextContent("YES");
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
					srcNoDataReal.setTextContent(noDataValues[bandNumber-1].toString());
					bandMapping.appendChild(srcNoDataReal);
					// SrcNoDataImag
					final Element srcNoDataImag = vrtXml.createElement("SrcNoDataImag");
					srcNoDataImag.setTextContent("0");
					bandMapping.appendChild(srcNoDataImag);
					// DstNoDataReal
					final Element dstNoDataReal = vrtXml.createElement("DstNoDataReal");
					dstNoDataReal.setTextContent(noDataValues[bandNumber-1].toString());
					bandMapping.appendChild(dstNoDataReal);
					// DstNoDataImag
					final Element dstNoDataImag = vrtXml.createElement("DstNoDataImag");
					dstNoDataImag.setTextContent("0");
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
			final File tempFile = this.tempFolder.newFile();
			dataset.GetDriver().CreateCopy(tempFile.toPath().toString(), dataset);
			final Document vrtXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempFile.toPath().toString());
			// Create a new raster band element for the alpha band
			final Element vrtRasterBand = vrtXml.createElement("VRTRasterBand");
			vrtRasterBand.setAttribute("dataType", "Byte");
			vrtRasterBand.setAttribute("band", String.format("{0}", dataset.GetRasterCount()+1));
			vrtRasterBand.setAttribute("subClass", "VRTWarpedRasterBand");
			// Create the sub element for vrtRasterBand
			final Element colorInterp = vrtXml.createElement("ColorInterp");
			colorInterp.setTextContent("Alpha");
			vrtRasterBand.appendChild(colorInterp);
			vrtXml.appendChild(vrtRasterBand);
			// Get the GDAL Warp options node
			final Node gdalWarpOptions = vrtXml.getElementsByTagName("GDALWarpOptions").item(0);
			// Set the init dest option
			final Element initDestOption = vrtXml.createElement("Option");
			initDestOption.setAttribute("name", "INIT_DEST");
			initDestOption.setTextContent("0"); //TODO: Ensure this omits quotation marks
			gdalWarpOptions.appendChild(initDestOption);
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
	
	private void saveXmlToDisk(final Document xml, final File location) throws TilingException
	{
		// Write the tempfile changes to disk
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			// Create a tempfile
			StreamResult result = new StreamResult(location);
			// Apply the transformer
			transformer.transform(new DOMSource(xml), result);
		}
		catch(TransformerException ex1)
		{
			ex1.printStackTrace();
			throw new TilingException("Error saving modified VRT to disk.");
		}
	}
	
	private Band getAlphaBand(final Dataset dataset)
	{
		return dataset.GetRasterBand(1).GetMaskBand();
	}
	
	private int getRasterBandCount(final Dataset dataset, final Band alphaBand)
	{
		// TODO: The bitwise calc functionality needs to be verified from the python functionality
		final boolean bitwiseAlpha = (alphaBand.GetMaskFlags() & gdalconstConstants.GMF_ALPHA) != 0;
		if (bitwiseAlpha || dataset.GetRasterCount() == 4 || dataset.GetRasterCount() == 2)
		{
			return dataset.GetRasterCount() + 1;
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
			final Coordinate<Integer> topLeftTile = this.crsProfile.crsToTileCoordinate(topLeft, this.crsProfile.getBounds(), this.tileScheme.dimensions(zoom), this.writer.getTileOrigin());
			final Coordinate<Integer> bottomRightTile = this.crsProfile.crsToTileCoordinate(bottomRight, this.crsProfile.getBounds(), this.tileScheme.dimensions(zoom), this.writer.getTileOrigin());
			tilesRangeByZoom.add(zoom, new Range<Coordinate<Integer>>(topLeftTile, bottomRightTile));
		});
		return tilesRangeByZoom;
	}
	
	private int minimalZoomForPixelSize(final Dataset dataset, final BoundingBox outputBounds, final List<Range<Coordinate<Integer>>> tileRanges) throws TileStoreException
	{
		final double pixelSize = dataset.GetGeoTransform()[1];
		final double zoomPixelSize = (pixelSize * Math.max(dataset.GetRasterXSize(), dataset.GetRasterYSize()) / this.tileSize);
		int[] zooms = IntStream.range(0, 31).toArray();
		for(int zoom : zooms)
		{
			// Get the tile coordinates of the top-left and bottom-right tiles
			final Coordinate<Integer> topLeftTile = this.writer.crsToTileCoordinate(new CrsCoordinate(outputBounds.getTopLeft(), this.crsProfile.getCoordinateReferenceSystem()), zoom);
			final Coordinate<Integer> bottomRightTile = this.writer.crsToTileCoordinate(new CrsCoordinate(outputBounds.getBottomRight(), this.crsProfile.getCoordinateReferenceSystem()), zoom);
			// Convert tile coordinates to crs coordinates: this will give us correct units-of-measure-per-pixel
			// This is tile data *plus* padding to the full tile grid
			final CrsCoordinate topLeftCrsFull = this.crsProfile.tileToCrsCoordinate(topLeftTile.getY(),
																					 topLeftTile.getX(),
																					 this.crsProfile.getBounds(),
																					 this.tileScheme.dimensions(zoom),
																					 this.writer.getTileOrigin());
			final CrsCoordinate bottomRightCrsFull = this.crsProfile.tileToCrsCoordinate(bottomRightTile.getY(),
																					 bottomRightTile.getX(),
																					 this.crsProfile.getBounds(),
																					 this.tileScheme.dimensions(zoom),
																					 this.writer.getTileOrigin());
			final double width = (new BoundingBox(topLeftCrsFull.getX(), bottomRightCrsFull.getY(), bottomRightCrsFull.getX(), topLeftCrsFull.getY())).getWidth();
			// get how many tiles wide this zoom will be so that number can be multiplied by tile size
			int zoomTilesWide = tileRanges.get(zoom).getMaximum().getX() - tileRanges.get(zoom).getMinimum().getX() + 1;
			double zoomResolution = width / (zoomTilesWide * this.tileSize);
			if (zoomPixelSize > zoomResolution)
			{
				return zoom == 0 ? 0 : zoom - 1;
			}
		}
		// In the worst case, zoom level zero will have only one tile...
		return 0;
	}
	
	private void generateBaseTiles(final Dataset dataset)
	{
		// TODO;
	}
	
	private void generateOverviewTiles()
	{
		// TODO:
	}
}