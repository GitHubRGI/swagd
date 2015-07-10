package com.rgi.suite.cli;

import com.rgi.common.util.FileUtility;
import com.rgi.geopackage.GeoPackage;
import org.gdal.osr.SpatialReference;
import utility.GdalUtility;

/**
 * Created by matthew.moran on 6/30/15.
 */
public class HeadlessOptionsValidator
{
	HeadlessOptions opts;
	private TileFormat inputType;
	private TileFormat outputType;

	HeadlessOptionsValidator(HeadlessOptions options)
	{
		this.opts = options;
	}

	public TileFormat getInputType()
	{
		return this.inputType;
	}
	public TileFormat getOutputType()
	{
		return this.outputType;
	}

	/**
	 * Validates options parsed from the command line, returns
	 *
	 * @return - True/False on whether the given arguments are valid (ie, if
	 * tiling and packaging are both flagged, that is an invalid
	 * selection.
	 */
	public boolean validate()
	{
		System.out.println("Validating input settings...");
		if(this.opts == null)
		{
			System.out.println("No arguments provided");
			return false;
		}
		try
		{
			// if everything is kosher, we can run the tiling
			if (this.validateInputFile() && this.validateOutputFile()
					&& this.validateOutputImageSettings()
					&& this.validateSRSOptions())
			{
				System.out.println("Tiling settings are OK!");
				return true;
			}
			//if we get here something is extremely wrong, freak out
			System.out.println("Error: Tiling Settings NOT OK, options provided are not Valid!");
			return false;
		}
		catch (Exception e)
		{
			System.out.println("Error Validating: \n\r" + e.getMessage());
			return false;
		}
	}

	/**
	 * Validates image settings for output. settings involved are:
	 * <p>
	 * TileWidth - width = 256
	 * <p>
	 * TileHeight -height = 256
	 * <p>
	 * Image Format - must be either jpeg, or png
	 * <p>
	 * Compression Type - only supports jpeg
	 * <p>
	 * Compression Quality - int between 0 and 100
	 *
	 * @return boolean validity of inputs.
	 */
	private boolean validateOutputImageSettings()
	{
		//validate tile size, arbitrarily 10000 pixels because i needed a number
		if (this.opts.getTileWidth() <= 0 || this.opts.getTileWidth() > 10000
				|| this.opts.getTileHeight() <= 0 || this.opts.getTileHeight() > 10000)
		{
			//TODO: update
			System.out.println("Error: Tile Width and Height must be Greater than 0 and Less than 10000");
			return false;
		}
		if (this.opts.getImageFormat() != null)
		{
			if (!this.opts.getCompressionType().equalsIgnoreCase("jpeg"))
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
	 * validates vs 3857
	 *
	 * @return true/false if SRS inputs are valid.
	 */
	private boolean validateSRSOptions()
	{
		if (this.opts.getOutputType() == TileFormat.GPKG)
		{
			if (this.opts.getOutputSrs() == 3857)
			{
				return true;
			}
			System.out.println("Error: For tiling into geopackage, output SRS must be 3857");
			return false;
		}
		return true;
	}

	private boolean validateInputFile()
	{
		System.out.println("Validating input Data...");
		if(!this.opts.getInputFile().exists())
		{
			System.out.println("no input file provided");
			return false;
		}
		this.inputType = TileFormat.ERR;
		//gpkg will handle the gpkg filetype, raw will accept all filetypes, tms will only take directories
		return this.validateGPKGInput() || this.validateRawInput() || this.validateTMSInput();

	}

	/**
	 * validates input if its in RAW image format
	 * @return
	 */
	private boolean validateRawInput()
	{
		// Validate input File settings.
		// verify existence, not a directory, then verify its a valid gdal
		// format
		if (this.opts.getInputFile().exists() &&
				!this.opts.getInputFile().isDirectory())
		{
			// check gdal compatibility by grabbing the SRS
			final SpatialReference srs = GdalUtility
					.getSpatialReference(this.opts.getInputFile());
			if (srs != null)
			{
				System.out.println("Tiling Input Raw Image File is Valid!");
				this.inputType = TileFormat.RAW;
				return true;
			}
			else
			{
				System.out.println("Error: Input Image File is not a valid GDAL supported format!");
				return false;
			}
		}
		return false;
	}

	/**
	 * validates gpkg inputs
	 * @return
	 */
	private boolean validateGPKGInput()
	{
		if ( this.opts.getInputFile().getName().toLowerCase()
				.endsWith("gpkg"))
		{
			System.out.println("Packaging Input GeoPackage exists");
			if(this.opts.getTileSetNameIn().equals(""))
			{
				System.out.println("Error: No input TileSet Name specified");
				return false;
			}
			else
			{
				try (GeoPackage gpkg = new GeoPackage(this.opts.getInputFile()))
				{
					if (gpkg.tiles().getTileSets().stream()
							.anyMatch(tileSet -> tileSet.getTableName().equals(this.opts.getTileSetNameIn())))
					{
						System.out.println("Input GeoPackage is valid, TileSet was found!");
						this.inputType = TileFormat.GPKG;
						return true;
					}
					else
					{
						System.out.println("Error: Input Tileset not found");
					}
				}
				catch (Exception e)
				{
					System.out.println("Error: Parsing gpkg for existing tableNames.. ");
				}
			}
		}
		return false;
	}

	/**
	 * validates input file for tms structure
	 * note: we dont actually read the images for TMS
	 * @return
	 */
	private boolean validateTMSInput()
	{
		if (this.opts.getInputFile().isDirectory() &&  this.opts.getInputFile().length() > 0) //non-emtpy directory.
		{
			System.out.println("Packaging Input Directory exists");
			this.inputType = TileFormat.TMS;
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
	private boolean validateOutputFile()
	{
		this.outputType= TileFormat.ERR;
		if(this.opts.getOutputFile() != null)
		{
			return this.checkGeoPackageOutput() || this.checkTMSOutput();
		}
		return false;
	}

	/**
	 * checks if the output is a geopackage, and if so if its valid
	 * @return
	 */
	private boolean checkGeoPackageOutput()
	{
		if (this.opts.getOutputFile().getName().toLowerCase().endsWith(".gpkg"))
		{
			System.out.println(String.format("Output file provided is %s", this.opts.getOutputFile().getPath()));
			if (this.opts.getTileSetNameOut().isEmpty())
			{
				System.out.println("Tileset name not provided, defaulting to file name");
				this.opts.setTileSetNameOut(FileUtility
						.nameWithoutExtension(this.opts.getOutputFile()));
			}

			if (!this.opts.getOutputFile().exists()) //geopackage file
			{
				System.out
						.println("Output GeoPackage name is a Valid,checking tilesetName..");
				// set TileSet name if none is provided
				this.outputType = TileFormat.GPKG;
				return true;
			}
			else
			{
				try (GeoPackage gpkg = new GeoPackage(this.opts.getOutputFile()))
				{
					if (!gpkg.tiles().getTileSets().stream()
							.anyMatch(tileSet -> tileSet.getTableName().equals(this.opts.getTileSetNameOut())))
					{
						System.out.println("Output Geopackage is valid, tilesetName is unique!");
						this.outputType = TileFormat.GPKG;
						return true;
					}
				}
				catch (Exception e)
				{
					System.out.println("Error: Parsing gpkg for existing tableNames.. ");
				}
			}
		}
		return false;
	}

	/**
	 * checks if the output could be a valid TMS tile.
	 * @return
	 */
	private boolean checkTMSOutput()
	{
		try
		{
			System.out.println("Checking Output Path..");
			if (this.opts.getOutputFile().exists())
			{
				if(this.opts.getOutputFile().isDirectory()
						&& this.opts.getOutputFile().list().length == 0) // existing, empty directory
				{
					System.out.println("Tiling Output Directory is a Valid! ");
					this.outputType = TileFormat.TMS;
					return true;
				}
				return false;
			}
			else
			{
				//non-existent, anything but .gpkg is a go
				if(!this.opts.getOutputFile().getName().toLowerCase().endsWith(".gpkg"))
				{
					System.out.println("Output Directory is valid, tiling into TMS format..");
					this.outputType = TileFormat.TMS;
					return true;
				}
				return false;
			}
		}
		catch (final Exception Ex)
		{
			System.err.println(Ex.getMessage());
			this.outputType=TileFormat.ERR;
			return false;
		}
	}
}
