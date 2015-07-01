package com.rgi.suite.cli;

import com.rgi.common.util.FileUtility;
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
		try
		{
			if (this.opts.isTiling())
			{
				// validate tiling parameters
				return this.validateTiling();
			}
			else
			{
				// validate packaging parameters
				return this.validatePackaging();
			}
		}
		catch (ValidationException e)
		{
			System.out.println("Error Validating: \n\r" + e.getMessage());
			return false;
		}
	}

	/**
	 * Validates settings for the action of Tiling/packaging an image.
	 * <p>
	 * input must be a raw image, output can be a TMS or gdal package. each of
	 * these output types has corresponding settings, which are validated in
	 * turn.
	 *
	 * @return
	 */
	private boolean validateTiling() throws ValidationException
	{
		this.inputType = TileFormat.ERR;
		System.out
				.println(String
						.format("Validating Tiling Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
								this.opts.getInputFile().getName(),
								this.opts.getOutputFile().getName(), this.opts.getOutputSrs()));
		try
		{
			// Validate input File settings.
			// verify existence, not a directory, then verify its a valid gdal
			// format
			System.out.println("Validating input Data...");
			if (this.opts.getInputFile().exists() && !this.opts.getInputFile().isDirectory())
			{
				// check gdal compatibility by grabbing the SRS
				final SpatialReference srs = GdalUtility
						.getSpatialReference(this.opts.getInputFile());
				if (srs != null)
				{
					System.out.println("Tiling Input Image File is Valid!");
					this.inputType = TileFormat.RAW;
				}
				else
				{
					throw new ValidationException("Input Image File is not a valid GDAL supported format");
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
			throw new ValidationException("Tiling Settings NOT OK, input types found invalid!");

		}
		catch (ValidationException e)
		{
			throw e; //bubble up validation exceptions
		}
		catch (final Exception Ex)
		{
			//handle other exceptions
			System.out.println(Ex.getMessage());
			System.out.println(Ex.getStackTrace());
			return false;
		}
	}

	/**
	 * Validates if the settings are ok for a packaging operation. the settings
	 * must match the following parameters
	 * <p>
	 * InputPath - must be an existing TMS store.
	 * <p>
	 * OutputPath- must be a non-existent *.gpkg file path.
	 * <p>
	 * OutputSRS - must be 3857 for geopackages.
	 *
	 * @return
	 */
	private boolean validatePackaging() throws ValidationException
	{
		this.inputType = TileFormat.ERR;
		System.out
				.println(String
						.format("Validating Packaging Parameters: Input Data: %s, Output Path: %s, Output SRS: ",
								this.opts.getInputFile().getName(),
								this.opts.getOutputFile().getName(), this.opts.getOutputSrs()));
		try
		{
			// Validate input Directory settings.
			// Must be TMS directory, we only check existence.
			System.out.println("Validating input Data...");
			if (this.opts.getInputFile().exists())
			{
				if (this.opts.getInputFile().isDirectory())
				{
					System.out.println("Packaging Input Directory exists");
					this.inputType = TileFormat.TMS;
				}
				else if (this.opts.getOutputFile().getName().toLowerCase()
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
			throw new ValidationException("Error: Packaging Settings NOT OK, inputs invalid!");
		}
		catch(ValidationException e)
		{
			throw e; //bubble up validation exceptions
		}
		catch(final Exception Ex)
		{
			//handle other exceptions
			System.out.println(Ex.getMessage());
			System.out.println(Ex.getStackTrace());
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
	private boolean validateOutputImageSettings() throws ValidationException
	{
		//validate tile size, arbitrarily 10000 pixels because i needed a number
		if (this.opts.getTileWidth() <= 0 || this.opts.getTileWidth() > 10000
				|| this.opts.getTileHeight() <= 0 || this.opts.getTileHeight() > 10000)
		{
			//TODO: update
			throw new ValidationException("Error: Tile Width and Height must be Greater than 0 and Less than 10000");
		}
		if (this.opts.getImageFormat() != null)
		{
			if (!this.opts.getCompressionType().equalsIgnoreCase("jpeg"))
			{
				throw new ValidationException("Error: jpeg compression type must be 'jpeg', and compression quality must be between 0 and 100");
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
	private boolean validateSRSOptions() throws ValidationException
	{
		if (this.opts.getOutputType() == TileFormat.GPKG)
		{
			if (this.opts.getOutputSrs() == 3857)
			{
				return true;
			}
			throw new ValidationException("for tiling into geopackage, output SRS must be 3857");
		}
		return true;
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
		try
		{
			System.out.println("Checking Output Path..");
			if (this.opts.getOutputFile().exists() && this.opts.getOutputFile().isDirectory()
					&& this.opts.getOutputFile().list().length == 0) // existing, empty
			// directory
			{
				System.out.println("Tiling Output Directory is a Valid! ");
				this.outputType =TileFormat.TMS;
			}
			else if (!this.opts.getOutputFile().exists())
			{
				if (this.opts.getOutputFile().getName().toLowerCase().endsWith("gpkg")) //geopackage file
				{
					System.out
							.println("Output GeoPackage name is a Valid, tiling into geopackage..");
					// set TileSet name if none is provided
					if (this.opts.getTileSetName().isEmpty())
					{
						System.out.println("Tileset name not provided, defaulting to file name");
						this.opts.setTileSetName(FileUtility
								.nameWithoutExtension(this.opts.getOutputFile()));
					}
					this.outputType=TileFormat.GPKG;
				}
				else if (this.opts.getOutputFile().getName().lastIndexOf('.') == -1)
				{
					System.out
							.println("Output Directory is valid, tiling into TMS format..");
					this.outputType= TileFormat.TMS;
				}
			}
			return this.opts.getOutputType() != TileFormat.ERR;
		}
		catch (final Exception Ex)
		{
			System.err.println(Ex.getMessage());
			this.outputType=TileFormat.ERR;
			return false;
		}
	}
}
