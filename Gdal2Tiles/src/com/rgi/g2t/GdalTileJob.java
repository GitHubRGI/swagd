package com.rgi.g2t;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.activation.MimeType;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.task.Settings;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Steven D. Lander
 *
 */
public class GdalTileJob implements Runnable {
	
	private final TileStoreWriter writer;
	private final CrsProfile crsProfile;
	private final Path location;
	private final MimeType imageOutputFormat;
	private final Settings settings;
	
	private final int tileSize = 256;
	private final String resamplingAlgorithm = "average";
	private final Driver memoryDriver = gdal.GetDriverByName("MEM");
	private final int gdalGeoQuerySize = 4 * tileSize;
	private final TemporaryFolder tempFolder = new TemporaryFolder();
	
	public GdalTileJob(final TileStoreWriter writer,
					   final CrsProfile crsProfile,
					   final Path location,
					   final MimeType imageOutputFormat,
					   final Settings settings)
	{
		this.writer = writer;
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
		// Get NODATA values
		final Double[] noDataValues = this.getNoDataValues(dataset);
		final SpatialReference inputSrs = this.openInputSrs(dataset);
		// We cannot tile an image with no geo referencing information
		if (this.datasetHasNoGeoReference(dataset)) throw new TilingException("Input raster image has no georeference.");
		
		return dataset;
	}

	private Dataset openOutput(final Dataset inputDataset, final SpatialReference inputSrs)
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
		outputDataset = this.correctNoData(dataset, noDataValues);
		return outputDataset;
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
			// Assumes only one value coming back from the band
			noDataValues[band-1] = noDataValue[0];
		});
		return noDataValues;
	}
	
	private Dataset correctNoData(final Dataset dataset, final Double[] noDataValues) throws TilingException
	{
		final File tempFile;
		if (noDataValues.length > 0)
		{
			try
			{
				// Create a tempfile
				tempFile = this.tempFolder.newFile();
				// Create a vrt copy of dataset saved to tempfile
				dataset.GetDriver().CreateCopy(tempFile.toPath().toString(), dataset);
				// Open the tempfile as a text file
				final Document vrtXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempFile.toPath().toString());
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
				gdalWarpOptions.appendChild(unifiedSrcNodataOption);g
				// Replace BandMapping tag for nodata bands
				IntStream.range();
				// Write the tempfile changes to disk
				// Open tempfile using gdal.Open() and return
			}
			catch (SAXException | IOException | ParserConfigurationException ex1)
			{
				ex1.printStackTrace();
				throw new TilingException("Could not correct output dataset NODATA values.");
			}
		}
		if (noDataValues.length == 0 && (dataset.GetRasterCount() == 1 || dataset.GetRasterCount() == 3))
		{
			// Correction of AutoCreateWarpedVRT for Mono (1 band) and RGB (3 bands) without NODATA
			// Same as gdalwarp -dstalpha
		}
	}
	
	private void generateBaseTiles(final Dataset dataset, final TileStoreWriter tileStoreWriter)
	{
		// TODO;
	}
	
	private void generateOverviewTiles(final TileStoreWriter tileStoreWriter)
	{
		// TODO:
	}
}