package com.rgi.suite.cli;

import com.rgi.common.util.FileUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.suite.cli.tilestoreadapter.GPKGTileStoreAdapter;
import com.rgi.suite.cli.tilestoreadapter.HeadlessTileStoreAdapter;
import com.rgi.suite.cli.tilestoreadapter.RawImageTileStoreAdapter;
import com.rgi.suite.cli.tilestoreadapter.TMSTileStoreAdapter;
import org.gdal.osr.SpatialReference;
import utility.GdalUtility;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates the provided headless options object to ensure that packaging or tiling won't break.
 *
 * @author Matthew.Moran
 */
public class HeadlessOptionsValidator
{
	private final HeadlessOptions          opts;
	private       HeadlessTileStoreAdapter inputAdapter;
	private       HeadlessTileStoreAdapter outputAdapter;
	private final Logger                   logger;

	HeadlessOptionsValidator(final HeadlessOptions options, final Logger logger)
	{
		this.logger = logger;
		this.opts = options;
	}

	public HeadlessTileStoreAdapter getInputAdapter()
	{
		return this.inputAdapter;
	}

	public HeadlessTileStoreAdapter getOutputAdapter()
	{
		return this.outputAdapter;
	}

	/**
	 * Validates options parsed from the command line, returns
	 *
	 * @return - True/False on whether the given arguments are valid (ie, if
	 * tiling and packaging are both flagged, that is an invalid
	 * selection.
	 */
	public boolean isValid()
	{
		if(this.opts == null)
		{
			this.logger.log(Level.SEVERE, "No arguments provided");
			return false;
		}
		if(this.opts.getShowHelp())
		{
			return false;
		}
		this.logger.log(Level.INFO, "Validating arguments provided.");
		try
		{
			// if everything is kosher, we can run the tiling
			if(this.isinputFileValid() && this.isOutputFileValid()
			   && this.isImageSettingsValid()
			   && this.isSRSOptionsValid())
			{
				this.logger.log(Level.INFO, "Options Provided passed Validation.");
				return true;
			}
			//if we get here something is extremely wrong, freak out
			this.logger.log(Level.SEVERE, "Error: Options Provided did not pass Validation.");
			return false;
		}
		catch(final RuntimeException error)
		{
			this.logger.log(Level.SEVERE, "Error: Exception thrown while validating." + error.getMessage());
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
	private boolean isImageSettingsValid()
	{
		//isValid tile size, arbitrarily 10000 pixels because i needed a number
		if(this.opts.getTileWidth() <= 0 || this.opts.getTileWidth() > HeadlessOptions.MAGIC_MAX_VALUE
		   || this.opts.getTileHeight() <= 0 || this.opts.getTileHeight() > HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.logger.log(Level.SEVERE, "Error: Tile Width and Height must be Greater than 0 and Less than 10000");
			return false;
		}
		if(this.opts.getImageFormat() != null)
		{
			if(!this.opts.getCompressionType().equalsIgnoreCase("jpeg"))
			{
				this.logger.log(Level.SEVERE,
								"Error: jpeg compression type must be 'jpeg', and compression quality must be between 0 and 100");
				return false;
			}
			return true;
		}
		this.logger.log(Level.SEVERE, "Error: Failed to isValid image settings, Image format is null");
		return false;
	}

	/**
	 * validates the input and output SRS's. Input SRS is only validated in the
	 * case of a TMS store being the input.(ignored otherwise) output SRS it
	 * validates vs 3857
	 *
	 * @return true/false if SRS inputs are valid.
	 */
	private boolean isSRSOptionsValid()
	{
		if(this.opts.getOutputAdapter() instanceof GPKGTileStoreAdapter)
		{
			if(this.opts.getOutputSrs() == HeadlessOptions.GLOBAL_WEB_MERCATOR)
			{
				return true;
			}
			this.logger.log(Level.SEVERE, "Error: For tiling into geopackage, output SRS must be 3857.");
			return false;
		}
		return true;
	}

	private boolean isinputFileValid()
	{
		this.logger.log(Level.INFO, "Validating input Data.");
		if(!this.opts.getInputFile().exists())
		{
			this.logger.log(Level.SEVERE, "Error: No input file provided.");
			return false;
		}
		//gpkg will handle the gpkg filetype, raw will accept all filetypes, tms will only take directories
		final boolean valid = this.isGPKGInputValid() || this.isRawInputValid() || this.isTMSInputValid();
		if(!valid)
		{
			this.logger.log(Level.SEVERE, "Error: Failed to validate input File.");
		}
		return valid;
	}

	/**
	 * validates input if its in RAW image format
	 *
	 * @return boolean if raw input is valid
	 */
	private boolean isRawInputValid()
	{
		// Validate input File settings.
		// verify existence, not a directory, then verify its a valid gdal
		// format
		if(this.opts.getInputFile().exists() &&
		   !this.opts.getInputFile().isDirectory() && !this.opts.getInputFile().getName().toLowerCase().endsWith("gpkg"))
		{
			// check gdal compatibility by grabbing the SRS
			final SpatialReference srs = GdalUtility
												 .getSpatialReference(this.opts.getInputFile());
			if(srs != null)
			{
				this.inputAdapter = new RawImageTileStoreAdapter();
				return true;
			}
			else
			{
				this.logger.log(Level.SEVERE, "Error: Input Image File is not a valid GDAL supported format.");
				return false;
			}
		}
		return false;
	}

	/**
	 * validates gpkg inputs
	 *
	 * @return true false if gpgk input is valid
	 */
	private boolean isGPKGInputValid()
	{
		if(this.opts.getInputFile().getName().toLowerCase()
					.endsWith("gpkg"))
		{
			if(this.opts.getTileSetNameIn().isEmpty())
			{
				this.logger.log(Level.INFO, "Error: No input TileSet Name specified.");
				return false;
			}
			else
			{
				try(GeoPackage gpkg = new GeoPackage(this.opts.getInputFile()))
				{
					if(gpkg.tiles().getTileSets().stream()
						   .anyMatch(tileSet -> tileSet.getTableName().equals(this.opts.getTileSetNameIn())))
					{
						this.inputAdapter = new GPKGTileStoreAdapter();
						return true;
					}
					else
					{
						this.logger.log(Level.INFO, "Error: Input Tileset not found.");
					}
				}
				catch(final SQLException | ClassNotFoundException | IOException | ConformanceException sqlError)
				{
					this.logger.log(Level.SEVERE,
									"Error:  Exception while parsing gpkg input file." + sqlError.getMessage());
				}

			}
		}
		return false;
	}

	/**
	 * validates input file for tms structure
	 * note: we dont actually read the images for TMS
	 *
	 * @return true false if tms input is valid
	 */
	private boolean isTMSInputValid()
	{
		if(this.opts.getInputFile().isDirectory() && this.opts.getInputFile().length() > 0L) //non-emtpy directory.
		{
			this.inputAdapter = new TMSTileStoreAdapter();
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
	private boolean isOutputFileValid()
	{
		if(this.opts.getOutputFile() == null)
		{
			this.logger.log(Level.SEVERE, "Error: No Output File Provided.");
			return false;
		}
		final boolean valid = this.isGPKGOutputValid() || this.isTMSOutputValid();
		if(!valid)
		{
			this.logger.log(Level.SEVERE, "Error: Could not validate output File.");
		}
		return valid;
	}

	/**
	 * checks if the output is a geopackage, and if so if its valid
	 *
	 * @return true false geopackage output valid
	 */
	private boolean isGPKGOutputValid()
	{
		if(this.opts.getOutputFile().getName().toLowerCase().endsWith(".gpkg"))
		{
			if(this.opts.getTileSetNameOut().isEmpty())
			{
				this.logger.log(Level.INFO, "Warning: Tileset name not provided, defaulting to file short name.");
				this.opts.setTileSetNameOut(FileUtility
													.nameWithoutExtension(this.opts.getOutputFile()));
			}
			if(this.opts.getOutputFile().exists())
			{
				try(GeoPackage gpkg = new GeoPackage(this.opts.getOutputFile()))
				{
					if(!gpkg.tiles().getTileSets().stream()
							.anyMatch(tileSet -> tileSet.getTableName().equals(this.opts.getTileSetNameOut())))
					{
						this.outputAdapter = new GPKGTileStoreAdapter();
						return true;
					}
				}
				catch(final SQLException | ClassNotFoundException | IOException | ConformanceException sqlError)
				{
					this.logger.log(Level.SEVERE,
									"Error:  Exception while parsing gpkg output file." + sqlError.getMessage());
				}
			}
			else //file doesnt exist, we can make a new one.
			{
				this.outputAdapter = new GPKGTileStoreAdapter();
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if the output could be a valid TMS tile.
	 *
	 * @return true false tms output is valid
	 */
	private boolean isTMSOutputValid()
	{
		try
		{
			if(this.opts.getOutputFile().exists())
			{
				if(this.opts.getOutputFile().isDirectory()
				   && this.opts.getOutputFile().list().length == 0) // existing, empty directory
				{
					this.outputAdapter = new TMSTileStoreAdapter();
					return true;
				}
				return false;
			}
			else
			{
				//non-existent, anything but .gpkg is a go
				if(!this.opts.getOutputFile().getName().toLowerCase().endsWith(".gpkg"))
				{
					this.outputAdapter = new TMSTileStoreAdapter();
					return true;
				}
				return false;
			}
		}
		catch(final RuntimeException error)
		{
			this.logger.log(Level.SEVERE, "Error: Exception while validating TMS output. " + error.getMessage());
			return false;
		}
	}
}
