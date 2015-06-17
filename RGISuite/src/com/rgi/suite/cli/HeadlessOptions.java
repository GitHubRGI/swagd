/**
 *
 */
package com.rgi.suite.cli;

import java.io.File;

import org.gdal.osr.SpatialReference;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import utility.GdalUtility;

import com.rgi.common.util.FileUtility;

/**
 * Business Logic for Running SWAGD in a headless environment. contains logic to
 * support Args4J command line arguments, as well as implementing the runnable
 * interface so it can be run in another thread
 *
 * @author matthew.moran
 *
 */
public class HeadlessOptions
{

	// set to true when validate is called
	private TileFormat inputType;
	private TileFormat outputType;

	// Arguments for command line, these are ALWAYS required (input and output
	// path).
	private File inputFile;
	@Argument(required = true, index = 0, usage = "Input source for tiling/Packaging operation")

	@Argument(required = true, index = 1, usage = "Full output path for tiling/Packaging operation")
	private File outputFile;

	// operation to perform
	@Option(name = "-t", aliases =
	{ "--tile" }, usage = "Tile image into desired format", forbids =
	{ "-p" })
	private boolean isTiling;
	@Option(name = "-p", aliases =
	{ "--package" }, usage = "Tile image into desired format", forbids =
	{ "-t" })
	private boolean isPackaging;

	// input/output srs
	@Option(name = "--ouputsrs", usage = "Desired output SRS EPSG identifier, eg 3857")
	private final int outputSrs = 3857;
	@Option(name = "-intputsrs", usage = "Input Spatial reference system EPSG identifier (ie 4326)")
	private int inputSRS = 4326;

	// tileset names
	@Option(name = "-n", aliases =
	{ "--tileset", "--tilesetname" }, usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	private String tileSetName;
	@Option(name = "-d", aliases =
	{ "--description" }, usage = "Tile set description")
	private final String tileSetDescription = "";

	// tile settings
	@Option(name = "-w", aliases =
	{ "--width" }, usage = "Tile width in pixels; default is 256")
	private final int tileWidth = 256;
	@Option(name = "-h", aliases =
	{ "--height" }, usage = "Tile height in pixels; default is 256")
	private final int tileHeight = 256;

	// image settings
	@Option(name = "-f", aliases =
	{ "--format" }, usage = "Image format for tiling operations, default is png (options are png, jpeg,etc.)")
	private final String imageFormat = "png";
	@Option(name = "-c", aliases =
	{ "--compression" }, usage = "Compression type for image tiling, default is jpeg")
	private String compressionType = "jpeg";
	@Option(name = "-q", aliases =
	{ "--quality" }, usage = "Compression quality for jpeg compression, between 0-100")
	private final int quality = 100;



	/**
	 * Validates options parsed from the command line, returns
	 *
	 * @return - True/False on whether the given arguments are valid (ie, if
	 *         tiling and packaging are both flagged, that is an invalid
	 *         selection.
	 */
	public boolean validate()
	{
		System.out.println("Validating input settings...");

		if (this.isTiling && this.isPackaging)
		{
			// if trying to do both, options are invalid, return false
			System.out
					.println("Invalid Settings: Cannot specify both tiling and packaging operations.");
			return false;
		} else if (this.isTiling)
		{
			// validate tiling parameters
			return this.validateTiling();
		} else if (this.isPackaging)
		{
			// validate packaging parameters
			return this.validatePackaging();
		} else
		{ // if not packaging or tiling, inputs are by definition invalid
			System.out
					.println("No operation specified, please specifiy tiling or packaging operation using '-t' or '-p'");
			return false;
		}
	}

	/**
	 * Validates settings for the action of Tiling/packaging an image.
	 *
	 * input must be a raw image, output can be a TMS or gdal package. each of
	 * these output types has corresponding settings, which are validated in
	 * turn.
	 *
	 * @return
	 */
	private boolean validateTiling()
	{
		this.setInputType(TileFormat.ERR);
		System.out
				.println(String
						.format("Validating Tiling Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
								this.getInputFile().getName(),
								this.getOutputFile().getName(), this.getOutputSrs()));
		try
		{
			// Validate input File settings.
			// verify existence, not a directory, then verify its a valid gdal
			// format
			System.out.println("Validating input Data...");
			if (this.getInputFile().exists() && !this.getInputFile().isDirectory())
			{
				// check gdal compatibility by grabbing the SRS
				final SpatialReference srs = GdalUtility
						.getSpatialReference(this.getInputFile());
				if (srs != null)
				{
					System.out.println("Tiling Input Image File is Valid!");
					this.inputType = TileFormat.RAW;
				} else
				{
					System.out
							.println("Input Image File is not a valid GDAL supported format");
				}
			}
			// if everything is kosher, we can run the tiling
			if (this.inputType != TileFormat.ERR && this.validateOutputFile()
					&& this.validateOutputImageSettings()
					&& this.validateSRSOptions())
			{
				System.out.println("Tiling settings are OK!");
				return true;
			}
			System.out.println("Error: Tiling Settings NOT OK!");
			return false;

		} catch (final Exception Ex)
		{
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
	 * OutputSRS - must be 3857 for geopackages.
	 *
	 * @return
	 */
	private boolean validatePackaging()
	{
		this.inputType = TileFormat.ERR;
		System.out
				.println(String
						.format("Validating Packaging Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
								this.getInputFile().getName(),
								this.getOutputFile().getName(), this.getOutputSrs()));
		try
		{
			// Validate input Directory settings.
			// Must be TMS directory, we only check existence.
			System.out.println("Validating input Data...");
			if (this.getInputFile().exists())
			{
				if (this.getInputFile().isDirectory())
				{
					System.out.println("Packaging Input Directory exists");
					this.inputType = TileFormat.TMS;
				} else if (this.getOutputFile().getName().toLowerCase()
						.endsWith("gpkg"))
				{
					System.out.println("Packaging Input GeoPackage exists");
					this.inputType = TileFormat.GPKG;
				}
			}
			// if everything is kosher, we can run the tiling
			if (this.inputType != TileFormat.ERR && this.validateOutputFile()
					&& this.validateOutputImageSettings()
					&& this.validateSRSOptions())
			{
				System.out.println("Packaging settings are OK!");
				return true;
			}
			System.out.println("Error: Packaging Settings NOT OK!");
			return false;
		} catch (final Exception Ex)
		{
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
	private boolean validateOutputImageSettings()
	{
		//validate tile size, arbitrarily 10000 pixels because i needed a number
		if (this.getTileWidth() <=0 || this.getTileWidth() > 10000
				|| this.getTileHeight() <= 0 || this.getTileHeight() > 10000)
		{
			//TODO: update
			System.out.println("Error: Tile width and height must be 256");
			return false;
		}
		if (this.getImageFormat().equalsIgnoreCase("jpeg")
				|| this.getImageFormat().equalsIgnoreCase("png"))
		{
			if (!this.getCompressionType().equalsIgnoreCase("jpeg"))
			{
				System.out
						.println("Error: jpeg compression type must be 'jpeg', and compression quality must be between 0 and 100");
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
		// validate output SRS
		if(this.getOutputType() == TileFormat.GPKG)
		{
			if( this.getOutputSrs() == 3857)
			{
				return true;
			}
			return false;
		}
		else if(this.getInputSrs() > 0 && this.getOutputSrs() > 0)
		 {
			return true; //setter sets them to a valid default, so they may have to pay attention to that.
		}
	}

	/**
	 * Validates the output File Path to determine A) output type and B) verify
	 * that a valid output file path has been supplied. Also sets the default
	 * tileSet name if not set via command line.
	 *
	 * @return boolean validity of output file.
	 */
	private boolean validateOutputFile()
	{
		this.setOutputType(TileFormat.ERR);
		try
		{
			System.out.println("Checking Output Path..");
			if (this.getOutputFile().exists() && this.getOutputFile().isDirectory()
					&& this.getOutputFile().list().length == 0) // existing, empty
															// directory
			{
				System.out.println("Tiling Output Directory is a Valid! ");
				this.setOutputType(TileFormat.TMS);
			} else if (!this.getOutputFile().exists())
			{
				if (this.getOutputFile().getName().toLowerCase().endsWith("gpkg")) //geopackage file
				{
					System.out
							.println("Output GeoPackage name is a Valid, tiling into geopackage..");
					// set TileSet name if none is provided
					if (this.getTileSetName().isEmpty())
					{
						System.out.println("Tileset name not provided, defaulting to file name");
						this.setTileSetName(FileUtility
								.nameWithoutExtension(this.getOutputFile()));
					}
					this.setOutputType(TileFormat.GPKG);
				} else if (this.getOutputFile().getName().lastIndexOf('.') == -1)
				{
					System.out
							.println("Output Directory is valid, tiling into TMS format..");
					this.setOutputType(TileFormat.TMS);
				}
			}
			return this.getOutputType() != TileFormat.ERR;
		} catch (final Exception Ex)
		{
			System.err.println(Ex.getMessage());
			this.setOutputType(TileFormat.ERR);
			return false;
		}
	}



	/**
	 * Getters for Headless Options
	 */

	public int getTileWidth()
	{
		return this.tileWidth;
	}

	public int getTileHeight()
	{
		return this.tileHeight;
	}

	public int getOutputSrs()
	{
		return this.outputSrs;
	}

	private int getInputSrs()
	{
		return this.inputSRS;
	}

	public File getInputFile()
	{
		return this.inputFile;
	}

	public String getImageFormat()
	{
		return this.imageFormat;
	}

	public TileFormat getInputType()
	{
		return this.inputType;
	}

	public TileFormat getOutputType()
	{
		return this.outputType;
	}

	public File getOutputFile()
	{
		return this.outputFile;
	}

	public String getTileSetName()
	{
		return this.tileSetName;
	}

	public String getTileSetDescription()
	{
		return this.tileSetDescription;
	}

	public String getCompressionType()
	{
		return this.compressionType;
	}


	/**
	 * Setters For Headless Options
	 */

	private void setInputType(final TileFormat type)
	{
		this.inputType = type;
	}

	private void setOutputType(final TileFormat type)
	{
		this.outputType = type;
	}

	public void setinputFile(String filePath)
	{
		if(filePath.startsWith("~" + File.separator))
		{
			filePath = System.getProperty("user.home") + filePath.substring(1);
		}
		this.inputFile = new File(filePath);
	}

	public void setoutputFile(final String filePath)
	{
		if(filePath.startsWith("~" + File.separator))
		{
			filePath = System.getProperty("user.home") + filePath.substring(1);
		}
		this.outputFile = new File(filePath);
	}

	public void setCompressionType(final String compressionType)
	{
		if(compressionType.equalsIgnoreCase("jpeg"))
		{
			this.compressionType = compressionType.toLowerCase();
		}
		else
		{
			this.compressionType = "jpeg";
			System.out.println("WARNING: Compression type must be jpeg. forcing option.");
		}
	}

	private void setImageFormat(final String formatString)
	{
		if (formatString.equalsIgnoreCase("jpeg")
				|| formatString.equalsIgnoreCase("png"))
		{

		}


	}

	public void setTileHeight(final int i)
	{
		if(i > 0 && i < 10000)
		{
			this.tileHeight = i;
		}
		else
		{
			System.out.println(String.format("error setting tile height to %d, "
					+ "value must be greater than 0 and less than 10000",i));
		}

	}

	public void setTileWidth(final int i)
	{
		if(i > 0 && i < 10000)
		{
			this.tileHeight = i;
		}
		else
		{
			System.out.println(String.format("error setting tile Width to %d, "
					+ "value must be greater than 0 and less than 10000",i));
		}
	}

	public void setOutputSrs(final int i)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		if (srs.ImportFromEPSG(i) == 0) {
			this.inputSRS = i;
		}
		else
		{
			this.outputSrs = -1;
			System.out
			.println(String.format("Error: Output SRS %d is not a GDAL supported EPSG value.",i));
		}

	}

	public void setInputSRS(final int i)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		if (srs.ImportFromEPSG(i) == 0) {
			this.inputSRS = i;
		}
		else
		{
			this.inputSRS = -1;
				System.out
				.println(String.format("Error: Input SRS %d is not a GDAL supported EPSG value.",i));
		}

	}

	public void setTileSetName(final String name)
	{
		if(name != null && name.length() <= 255)
		{
			this.tileSetName = name;
		}
		else
		{
			System.out.println("Provided TileSet Name is invalid.");
		}
	}

	public void setTileSetDescription(final String description)
	{
		if(description != null  && description.length() <= 1000)
		{
			this.tileSetName = description;
		}
		else
		{
			System.out.println("Tile set description is invalid");
		}

	}

}
