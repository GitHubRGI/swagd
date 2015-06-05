/**
 *
 */
package com.rgi.suite;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.gdal.osr.SpatialReference;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import utility.GdalUtility;

import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.TaskMonitor;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.util.FileUtility;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.packager.Packager;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.store.tiles.geopackage.GeoPackageWriter;
import com.rgi.store.tiles.tms.TmsWriter;

/**
 * Business Logic for Running SWAGD in a headless environment. contains logic to support Args4J command line arguments,
 * as well as implementing the runnable interface so it can be run in another thread
 *
 * @author matthew.moran
 *
 */
public class HeadlessOptions implements Runnable {

	/**
	 * Private enumerator to help reduce redundant checks of input/output data.
	 * @author matthew.moran
	 */
	private enum TileFormat {
		RAW, // raw image
		TMS, // tms store
		GPKG, // gpkg
		ERR // error
	}

	/**
	 * TaskMonitor Implementation for the purpose of running tiling in the headless environment.
	 * @author matthew.moran
	 */
	private class HeadlessTaskMonitor implements TaskMonitor{
		private int maximum = 0;
		private int step = 1;
		private int last = 0;
		@Override
		public void setMaximum(int maximum) {
			this.maximum = maximum;
			if(maximum >=25) {
				this.step = maximum/25;
			}
		}

		@Override
		public void setProgress(int value) {
			final int percent  = (int) ((float)(value)/(float)(this.maximum)*100.00);
			if(value > (this.last+this.step))
			{
				final StringBuilder bar = new StringBuilder("[");
				for(int i = 0; i < 25; i++){
					if( i < (percent/4)){
						bar.append("=");
					}else if( i == (percent/4)){
						bar.append(">");
					}else{
						bar.append(" ");
					}
				}
				bar.append(String.format("]  - %d / %d ",value,this.maximum));
				//return carriage and write over progress bar.
				System.out.print("\r" + bar.toString());
				this.last = value;
			}

		}

	}

	/**
	 * Constructor
	 */
	public HeadlessOptions()
	{
		//setting default values
		this.outputSrs = 3857;
		this.inputSRS = 4326;
		this.tileSetName = "default";
		this.tileWidth = 256;
		this.tileHeight = 256;
		this.imageFormat = "png";
		this.compressionType = "jpeg";
		this.quality = 100;
		this.tileSetDescription = "default description";
	}
	// set to true when validate is called
	private TileFormat inputType;
	private TileFormat outputType;

	// Arguments for command line, these are ALWAYS required (input and output
	// path).
	@Argument(required = true, index = 0, usage = "Input source for tiling/Packaging operation")
	private File inputFile;
	@Argument(required = true, index = 1, usage = "Full output path for tiling/Packaging operation")
	private File outputFile;

	// operation to perform
	@Option(name = "-t", aliases = { "--tile" }, usage = "Tile image into desired format", forbids = { "-p" })
	private boolean isTiling;
	@Option(name = "-p", aliases = { "--package" }, usage = "Tile image into desired format", forbids = { "-t" })
	private boolean isPackaging;

	// input/output srs
	@Option(name = "--ouputsrs", usage = "Desired output SRS EPSG identifier, eg 3857")
	private int outputSrs = 3857;
	@Option(name = "-intputsrs", usage = "Input Spatial reference system EPSG identifier (ie 4326)")
	private int inputSRS = 4326;

	// tileset names
	@Option(name = "-n", aliases={ "--tileset", "--tilesetname" }, usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	private String tileSetName;
	@Option(name = "-d", aliases={"--description"},usage = "Tile set description")
	private String tileSetDescription = "";

	// tile settings
	@Option(name = "-w", aliases = { "--width" }, usage = "Tile width in pixels; default is 256")
	private int tileWidth = 256;
	@Option(name = "-h", aliases = { "--height" }, usage = "Tile height in pixels; default is 256")
	private int tileHeight = 256;

	// image settings
	@Option(name = "-f", aliases = { "--format" }, usage = "Image format for tiling operations, default is png (options are png, jpeg,etc.)")
	private String imageFormat = "png";
	@Option(name = "-c", aliases = { "--compression" }, usage = "Compression type for image tiling, default is jpeg")
	private String compressionType = "jpeg";
	@Option(name = "-q", aliases = { "--quality" }, usage = "Compression quality for jpeg compression, between 0-100")
	private int quality = 100;



	@Override
	public void run() {
		final TaskMonitor taskMonitor = new HeadlessTaskMonitor();
		final Dimensions<Integer> tileDimensions = new Dimensions<>(this.tileWidth, this.tileHeight);
		final Color noDataColor = new Color(0, 0, 0, 0);
		final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG",this.outputSrs);

		TileStoreWriter tileStoreWriter = null;

		try(final TileStoreReader tileStoreReader = new RawImageTileReader(this.inputFile, tileDimensions, noDataColor, crs))
		{
			final MimeType imageType = new MimeType("image",this.imageFormat);
			switch(this.outputType)
			{
			case TMS:
				tileStoreWriter = new TmsWriter(crs, this.outputFile.toPath(), imageType);
				break;
			case GPKG:
				tileStoreWriter = new GeoPackageWriter(this.outputFile,
						tileStoreReader.getCoordinateReferenceSystem(),
						this.tileSetName,
						this.tileSetName,
						this.tileSetDescription,
						tileStoreReader.getBounds(),
						getRelativeZoomTimesTwoTileScheme(tileStoreReader),
						imageType,
						this.getImageWriteParameter());
				break;
			default:
				throw new Exception("output Type must be TMS or GPKG!");
			}
			//kick of packager operation.
			new Packager(taskMonitor,
					tileStoreReader,
					tileStoreWriter).execute();
		}
		catch(final Exception ex)
		{
			System.err.println(ex.getMessage());
		}finally
		{
			try{
				if(tileStoreWriter != null)
				{
					tileStoreWriter.close();
				}
			}catch(final Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 *
	 *
	 * @return - True/False on whether the given arguments are valid (ie, if
	 *         tiling and packaging are both flagged, that is an invalid
	 *         selection.
	 */
	public boolean validate() {
		System.out.println("Validating input settings...");

		if (this.isTiling && this.isPackaging) {
			// if trying to do both, options are invalid, return false
			System.out
			.println("Invalid Settings: Cannot specify both tiling and packaging operations.");
			return false;
		} else if (this.isTiling) {
			// validate tiling parameters
			return validateTiling();
		} else if (this.isPackaging) {
			// validate packaging parameters
			return validatePackaging();
		} else { // if not packaging or tiling, inputs are by definition invalid
			System.out.println("No operation specified, please specifiy tiling or packaging operation using '-t' or '-p'");
			return false;
		}
	}
	/**
	 * Validates settings for the action of Tiling/packaging an image.
	 *
	 * input must be a raw image, output can be a TMS or gdal package. each of these output types has
	 * corresponding settings, which are validated in turn.
	 *
	 * @return
	 */
	private boolean validateTiling() {
		this.inputType = TileFormat.ERR;
		System.out
		.println(String
				.format("Validating Tiling Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
						this.inputFile.getName(), this.outputFile.getName(), this.outputSrs));
		try {
			// Validate input File settings.
			// verify existence, not a directory, then verify its a valid gdal
			// format
			System.out.println("Validating input Data...");
			if (this.inputFile.exists() && !this.inputFile.isDirectory()) {
				// check gdal compatibility by grabbing the SRS
				final SpatialReference srs = GdalUtility
						.getSpatialReference(this.inputFile);
				if (srs != null) {
					System.out.println("Tiling Input Image File is Valid!");
					this.inputType = TileFormat.RAW;
				} else {
					System.out
					.println("Input Image File is not a valid GDAL supported format");
				}
			}
			// if everything is kosher, we can run the tiling
			if(this.inputType != TileFormat.ERR && this.validateOutputFile()
					&& this.validateOutputImageSettings() && this.validateSRSOptions())
			{
				System.out.println("Tiling settings are OK!");
				return true;
			}
			System.out.println("Error: Tiling Settings NOT OK!");
			return false;

		} catch (final Exception Ex) {
			System.out.println("Error Validating Tiling Options:");
			System.err.println(Ex.getMessage());
			return false;
		}
	}

	/**
	 * Validates if the settings are ok for a packaging operation. the settings
	 * must match the following parameters
	 *
	 * InputPath - must be an existing TMS store.
	 *
	 * OutputPath- must be a non-existent *.gpkg file path.
	 *
	 * OutputSRS - TODO, locked at true for now
	 *
	 * @return
	 */
	private boolean validatePackaging() {
		this.inputType = TileFormat.ERR;
		System.out
		.println(String
				.format("Validating Packaging Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
						this.inputFile.getName(), this.outputFile.getName(), this.outputSrs));
		try
		{
			// Validate input Directory settings.
			// Must be TMS directory, we only check existence.
			System.out.println("Validating input Data...");
			if (this.inputFile.exists() ){
				if(this.inputFile.isDirectory()) {
					System.out.println("Packaging Input Directory exists");
					this.inputType=TileFormat.TMS;
				}
				else if(this.outputFile.getName().toLowerCase().endsWith("gpkg")){
					System.out.println("Packaging Input GeoPackage exists");
					this.inputType=TileFormat.GPKG;
				}
			}
			// if everything is kosher, we can run the tiling
			if(this.inputType != TileFormat.ERR && this.validateOutputFile()
					&& this.validateOutputImageSettings() && this.validateSRSOptions())
			{
				System.out.println("Packaging settings are OK!");
				return true;
			}
			System.out.println("Error: Packaging Settings NOT OK!");
			return false;
		} catch (final Exception Ex) {
			System.err.println(Ex.getMessage());
			return false;
		}
	}

	/**
	 * Validates image settings for output. settings involved are:
	 *
	 * TileWidth - width = 256
	 *
	 * TileHeight -height = 256
	 *
	 * Image Format - must be either jpeg, or png
	 *
	 * Compression Type - only supports jpeg
	 *
	 * Compression Quality - int between 0 and 100
	 *
	 * @return boolean validity of inputs.
	 */
	private boolean validateOutputImageSettings(){
		if(this.tileWidth != 256 || this.tileHeight != 256)
		{
			System.out.println("Error: Tile width and height must be 256");
			return false;
		}
		if(this.imageFormat.equalsIgnoreCase("jpeg") || this.imageFormat.equalsIgnoreCase("png"))
		{
			if(!(this.compressionType.equalsIgnoreCase("jpeg")) ||
					this.quality < 0 || this.quality > 100)
			{
				System.out.println("Error: jpeg compression type must be 'jpeg', and compression quality must be between 0 and 100");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * validates the input and output SRS's. Input SRS is only validated in the
	 * case of a TMS store being the input.(ignored otherwise) output SRS it
	 * validates vs 3857 TODO: check vs supported SRS types.
	 *
	 * @return true/false if SRS inputs are valid.
	 */
	private boolean validateSRSOptions() {
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		if (srs.ImportFromEPSG(this.inputSRS) != 0) {
			System.out
			.println("Error: Input SRS is not a GDAL supported EPSG value.");
			return false;
		}
		// validate output SRS
		if (this.outputSrs == 3857) {
			return true;
		}
		return false;
	}

	/**
	 * Validates the output File Path to determine A) output type and B) verify
	 * that a valid output file path has been supplied. Also sets the default
	 * tileSet name if not set via command line.
	 *
	 * @return boolean validity of output file.
	 */
	private boolean validateOutputFile() {
		this.outputType = TileFormat.ERR;
		try {
			System.out.println("Checking Output Path..");
			if (this.outputFile.exists() && this.outputFile.isDirectory()
					&& this.outputFile.list().length == 0) // existing, empty directory
			{
				System.out.println("Tiling Output Directory is a Valid! ");
				this.outputType = TileFormat.TMS;
			} else if (!this.outputFile.exists()) {
				if (this.outputFile.getName().toLowerCase().endsWith("gpkg")) // non existant geopackage
				{
					System.out
					.println("Output GeoPackage name is a Valid, tiling into geopackage..");
					// set TileSet name if none is provided
					if (this.tileSetName.isEmpty()) {
						this.tileSetName = FileUtility
								.nameWithoutExtension(this.outputFile);
					}
					this.outputType = TileFormat.GPKG;
				} else if (this.outputFile.getName().lastIndexOf('.') == -1) // non-existent directory
				{
					System.out
					.println("Output Directory is valid, tiling into TMS format..");
					this.outputType = TileFormat.TMS;
				}
			}
			return this.outputType != TileFormat.ERR;
		} catch (final Exception Ex) {
			System.err.println(Ex.getMessage());
			this.outputType = TileFormat.ERR;
			return false;
		}
	}

	/**
	 * returns an image writer for the supplied image type.
	 * @return
	 */
	private ImageWriter getImageWriter()
	{
		MimeType mimeType;
		try {
			mimeType = new MimeType("image",this.imageFormat);
			return ImageIO.getImageWritersByMIMEType(mimeType.toString()).next();

		} catch (final MimeTypeParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}


	}

	/**
	 * created an image writer parameter object.
	 * @return
	 */
	protected ImageWriteParam getImageWriteParameter()
	{
		final ImageWriteParam imageWriteParameter = this.getImageWriter().getDefaultWriteParam();

		final Float  compressionQualityValue = (float) ((this.quality)/100.00);

		if(this.compressionType != null && imageWriteParameter.canWriteCompressed())
		{
			imageWriteParameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			imageWriteParameter.setCompressionType(this.compressionType);

			if(compressionQualityValue != null)
			{
				imageWriteParameter.setCompressionQuality(compressionQualityValue);
			}

			return imageWriteParameter;
		}

		return null;
	}
	private static TileScheme getRelativeZoomTimesTwoTileScheme(final TileStoreReader tileStoreReader) throws TileStoreException
	{
		final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();

		if(zoomLevels.size() == 0)
		{
			throw new TileStoreException("Input tile store contains no zoom levels");
		}

		final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);

		final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());

		final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
		final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);

		final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;
		final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;

		return new ZoomTimesTwo(zoomLevelRange.getMinimum(),
				zoomLevelRange.getMaximum(),
				minZoomLevelMatrixWidth,
				minZoomLevelMatrixHeight);
	}
}
