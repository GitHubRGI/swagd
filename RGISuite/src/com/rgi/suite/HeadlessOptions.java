/**
 *
 */
package com.rgi.suite;

import java.io.File;

import org.gdal.osr.SpatialReference;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;

import com.rgi.common.util.FileUtility;

import utility.GdalUtility;

/**
 * Options class for running the swagd program from the command line. used in
 * conjunction with args4j to parse the arguments and options.
 *
 * @author matthew.moran
 *
 */
public class HeadlessOptions {

	// used to keep track of input/output types.
	private enum TileFormat {
		RAW, // raw image
		TMS, // tms store
		GPKG, // gpkg
		ERR // error
	}

	// set to true when validate is called
	private final boolean isValid = false;
	private TileFormat inputType;
	private TileFormat outputType;

	// Arguments for command line, these are ALWAYS required (input and output
	// path).
	@Argument(required = true, index = 0, usage = "Input source for tiling/Packaging operation")
	private String inputPath;
	@Argument(required = true, index = 1, usage = "Full output path for tiling/Packaging operation")
	private String outputPath;

	// operation to perform
	@Option(name = "-t", aliases = { "--tile" }, usage = "Tile image into desired format", forbids = { "-p" })
	private boolean isTiling;
	@Option(name = "-p", aliases = { "--package" }, usage = "Tile image into desired format", forbids = { "-t" })
	private boolean isPackaging;

	// input/output srs
	@Option(name = "--ouputsrs", usage = "Desired output SRS EPSG identifier, eg 3857")
	private final int outputSrs = 3857;
	@Option(name = "-intputsrs", usage = "Input Spatial reference system EPSG identifier (ie 4326)")
	private final int inputSRS = 4326;

	// tileset names
	@Option(name = "-n", aliases = { "--tileset", "--tilesetname" }, usage = "Input Tile Set for GeoPackagees, default is short name of output geopackage.")
	private String tileSetName;

	// tile settings
	@Option(name = "-w", aliases = { "--width" }, usage = "Tile width in pixels; default is 256")
	private final int tileWidth = 256;
	@Option(name = "-h", aliases = { "--height" }, usage = "Tile height in pixels; default is 256")
	private final int tileHeight = 256;

	// image settings
	@Option(name = "-f", aliases = { "--format" }, usage = "Image format for tiling operations, default is png (options are png, jpeg,etc.)")
	private final String imageFormat = "png";
	@Option(name = "-c", aliases = { "--compression" }, usage = "Compression type for image tiling, default is jpeg")
	private final String compressionType = "jpeg";
	@Option(name = "-q", aliases = { "--quality" }, usage = "Compression quality for jpeg compression, between 0-100")
	private final int quality = 100;

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
			return false;
		}
	}

	public void Run() {
		// TODO
	}

	/**
	 * Validates settings for the action of Tiling/packaging an image.
	 *
	 * @return
	 */
	private boolean validateTiling() {
		this.inputType = TileFormat.ERR;
		final boolean outputValid = false;
		final boolean outputSRS = true; // we are hard coding this
		System.out
		.println(String
				.format("Validating Tiling Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
						this.inputPath, this.outputPath, this.outputSrs));
		try {
			// Validate input File settings.
			// verify existence, not a directory, then verify its a valid gdal
			// format
			System.out.println("Validating input Data...");
			final File inFile = new File(this.inputPath);
			if (inFile.exists() && !inFile.isDirectory()) {
				// check gdal compatibility by grabbing the SRS
				final SpatialReference srs = GdalUtility
						.getSpatialReference(inFile);
				if (srs != null) {
					System.out.println("Tiling Input Image File is Valid!");
					this.inputType = TileFormat.RAW;
				} else {
					System.out
					.println("Input Image File is not a valid GDAL supported format");
				}
			}
			// if everything is kosher, we can run the tiling
			if (inputValid && outputValid && outputSRS) {
				return true;
			}
			return false;

		} catch (final Exception Ex) {
			System.out.println("Error Validating Tiling Options:");
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
	 * Image Format -
	 *
	 * Compression Type -
	 *
	 * Compression Quality -
	 *
	 * @return
	 */
	private boolean ValidateOutputImageSettings(){
		if(this.tileWidth != 256 || this.tileHeight != 256)
		{
			System.out.println("Error: Tile width and height must be 256");
			return false;
		}
		if(this.imageFormat.equalsIgnoreCase("jpeg"))
		{

		}
		else if(this.imageFormat.equalsIgnoreCase("png"))
		{

		}
	}

	/**
	 * validates the input and output SRS's. Input SRS is only validated in the
	 * case of a TMS store being the input.(ignored otherwise) output SRS it
	 * validates vs 3857 TODO: check vs supported SRS types.
	 *
	 * @return true/false if SRS inputs are valid.
	 */
	private boolean ValidateSRSOptions() {
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		final bool
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
			final File outFile = new File(this.outputPath);
			if (outFile.exists() && outFile.isDirectory()
					&& outFile.list().length == 0) // existing, empty directory
			{
				System.out.println("Tiling Output Directory is a Valid! ");
				this.outputType = TileFormat.TMS;
			} else if (!outFile.exists()) {
				if (this.outputPath.endsWith("gpkg")) // non existant geopackage
				{
					System.out
					.println("Output GeoPackage name is a Valid, tiling into geopackage..");
					// set TileSet name if none is provided
					if (this.tileSetName.isEmpty()) {
						this.tileSetName = FileUtility
								.nameWithoutExtension(outFile);
					}
					this.outputType = TileFormat.GPKG;
				} else if (outFile.getName().lastIndexOf('.') == -1) // non-existent
					// directory
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
		boolean inputValid = false;
		boolean outputValid = false;
		final boolean outputSRS = true; // we are hard coding this
		System.out
		.println(String
				.format("Validating Packaging Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
						this.inputPath, this.outputPath, this.outputSrs));
		try {

			// Validate input Directory settings.
			// Must be TMS directory, we only check existence.
			System.out.println("Validating input Data...");
			final File inFile = new File(this.inputPath);
			if (inFile.exists() && inFile.isDirectory()) {
				// check gdal compatibility by grabbing the SRS
				final SpatialReference srs = GdalUtility
						.getSpatialReference(inFile);
				if (srs != null) {
					System.out.println("Packaging Input Image File is Valid!");
					inputValid = true;
				} else {
					System.out.println("Input TMS store is not a valid");
				}
			}
			// Validate output Path argument:
			// output path must be a non existent *.gpkg
			// TODO: check if we need to create the non-existent objects.
			System.out.println("Checking Output Path..");
			final File outFile = new File(this.outputPath);
			if (!outFile.exists()) {
				if (this.outputPath.endsWith("gpkg")) {
					System.out
					.println("Output GeoPackage name is a Valid, tiling into geopackage..");
					outputValid = true;
				} else {
					System.out.println("Output Path is not valid");
				}
			} else {
				System.out.println("Output geopackage already exists!");
			}
			// if everything is kosher, we can run the tiling
			if (inputValid && outputValid && outputSRS) {
				return true;
			}
			return false;

		} catch (final Exception Ex) {
			System.err.println(Ex.getMessage());
			return false;
		}
	}

	/** Getters and setters for the various arguments above */
	public boolean getValid() {
		return this.isValid;
	}

	public boolean isTiling() {
		return this.isTiling;
	}

	public boolean isPackaging() {
		return this.isPackaging;
	}

	public String getInputPath() {
		return this.inputPath;
	}

	public String getOutputPath() {
		return this.outputPath;
	}

	public String getOutputSpatialReference() {
		return this.outputSrs;
	}

}
