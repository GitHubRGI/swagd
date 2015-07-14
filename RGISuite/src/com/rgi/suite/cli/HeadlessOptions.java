package com.rgi.suite.cli;

import com.rgi.suite.cli.tilestoreadapter.HeadlessTileStoreAdapter;
import org.gdal.osr.SpatialReference;
import org.kohsuke.args4j.Option;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business Logic for Running SWAGD in a headless environment. contains logic to
 * support Args4J command line arguments
 * -h                                     : show help
 * --inputsrs <epsg int>                  : Input Spatial reference system EPSG identifier (ie 4326)
 * --outputsrs <epsg int value>           : Desired output SRS EPSG identifier,eg 3857
 * -c (--compression) <jpeg               : Compression type for image tiling,default is jpeg
 * -d (--description) <text description>  : Tile set description
 * -f (--format) <image/png or image/jpg> : Image format for tiling operations,default is png (options are png,jpeg,etc.)
 * -h (--height) <Height>                 : Tile height in pixels; default is 256
 * -i (-in) <Input File Path>             : REQUIRED! Input source for tiling/Packaging operation
 * -ti (--intileset, --intilesetname) <text>   : Tile Set name for GeoPackages (input), default is short name of output geopackage.
 * -to (--outtileset, --outtilesetname)    :Tile set name for geopackage output
 * -o (-out) <Output File Path>           : Full output path for tiling/Packaging operation
 * -q (--compressionQuality) <1-100>                 : Compression compressionQuality for jpeg compression, between 0-100
 * -w (--width) <1-MAGIC_MAX_VALUE>                 : Tile width in pixels; default is 256
 *
 * @author matthew.moran
 */
public class HeadlessOptions
{
	public static final  int MAGIC_MAX_VALUE     = 10000;
	private static final int DEFAULT_DIMENSION   = 256;
	public static final  int GLOBAL_WEB_MERCATOR = 3857;
	public static final  int GLOBAL_GEODETIC     = 4326;

	// set to true when isValid is called
	private HeadlessOptionsValidator validator;
	private boolean                  isValid;
	// variables to hold command line arguments
	private File                     inputFile;
	private File                     outputFile;
	private int    outputSrs          = HeadlessOptions.GLOBAL_WEB_MERCATOR;
	private int    inputSrs           = HeadlessOptions.GLOBAL_GEODETIC;
	private String tileSetNameIn      = "";
	private String tileSetNameOut     = "";
	private String tileSetDescription = "";
	private MimeType imageFormat;
	private int    tileWidth          = HeadlessOptions.DEFAULT_DIMENSION;
	private int    tileHeight         = HeadlessOptions.DEFAULT_DIMENSION;
	private String compressionType    = "jpeg";
	@SuppressWarnings("MagicNumber")
	private int    compressionQuality = 75;
	private final Logger logger;

	/**
	 * Constructor
	 *
	 * @param logger - logger to write progress out to
	 */
	public HeadlessOptions(final Logger logger)
	{
		this.logger = logger;
		try
		{
			this.imageFormat = new MimeType("image/jpeg");
		}
		catch(final MimeTypeParseException error)
		{
			this.logger.log(Level.SEVERE, "Error creating default image format: " + error.getMessage());
		}
	}

	/**
	 * path to the input file/directory for packaging/tiling.
	 *
	 * @param filePath - path to the input file or directory to read from
	 * @throws IllegalArgumentException if filePath leads to a non-existent file, is null, or is empty.
	 */
	@Option(required = true, name = "-i", aliases = "-in", metaVar = "<Input File Path>", usage = "Input source for tiling orPackaging operation")
	public void setInputFile(final String filePath)
	{
		final String path = HeadlessOptions.getFullPath(filePath);
		final File   file = new File(path);
		if(file.exists())
		{
			this.inputFile = new File(path);
		}
		else
		{
			throw new IllegalArgumentException(String.format("FilePath: %s does not exist, input files must exist!",
															 filePath));
		}
	}

	/**
	 * path to the output file/directory for packaging/tiling
	 *
	 * @param filePath - output file path
	 * @throws IllegalArgumentException - if output path  is null or empty
	 */
	@Option(required = true, name = "-o", aliases = "-out", metaVar = "<Output File Path>", usage = "Full output path for tiling or Packaging operation")
	public void setOutputFile(final String filePath)
	{
		final String path = HeadlessOptions.getFullPath(filePath);
		this.outputFile = new File(path);
	}

	/**
	 * EPSG output SRS number
	 *
	 * @param outSrs - input SRS
	 * @throws IllegalArgumentException if unrecognized spatial reference.
	 */
	@Option(name = "--outputsrs", metaVar = "<epsg srs int>", usage = "Desired output SRS EPSG identifier, eg 3857")
	public void setOutputSrs(final int outSrs)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		try
		{
			if(srs.ImportFromEPSG(outSrs) == 0)
			{
				this.outputSrs = outSrs;
			}
		}
		catch(final RuntimeException ignored)
		{
			throw new IllegalArgumentException(String.format("Error: Output SRS %d is not a GDAL supported EPSG value.",
															 outSrs));
		}

	}

	/**
	 * EPSG input SRS number, required if input is a tms cache
	 *
	 * @param inSrs - input SRS
	 * @throws IllegalArgumentException if unrecognized spatial reference.
	 */
	@Option(name = "--inputsrs", metaVar = "<epsg srs int>", usage = "Input Spatial reference system EPSG identifier (ie 4326)")
	public void setInputSRS(final int inSrs)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		try
		{
			if(srs.ImportFromEPSG(inSrs) == 0)
			{
				this.inputSrs = inSrs;
			}
		}
		catch(final RuntimeException ignored)
		{
			throw new IllegalArgumentException(String.format("Error: input SRS %d is not a GDAL supported EPSG value.",
															 inSrs));
		}
	}

	/**
	 * Tile set Name
	 *
	 * @param name - name of the tileset to read from
	 * @throws IllegalArgumentException - name is null, or longer than 10000 characters
	 */
	@Option(name = "-ti", metaVar = "<Tile Set Name>", aliases = {"--intileset", "--intilesetname"},
			usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	public void setTileSetNameIn(final String name)
	{
		if(name != null && name.length() <= HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.tileSetNameIn = name;
		}
		else
		{
			throw new IllegalArgumentException("Provided Name is invalid, must be a "
											   + "non-null string shorter than MAGIC_MAX_VALUE characters");
		}
	}

	/**
	 * Tile set Name
	 *
	 * @param name - tile set name to write out to
	 * @throws IllegalArgumentException - name is null, or longer than 10000 characters
	 */
	@Option(name = "-to", metaVar = "<Tile Set Name>", aliases = {"--outtileset", "--outtilesetname"},
			usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	public void setTileSetNameOut(final String name)
	{
		if(name != null && name.length() <= HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.tileSetNameOut = name;
		}
		else
		{
			throw new IllegalArgumentException("Provided Name is invalid, must be a "
											   + "non-null string shorter than MAGIC_MAX_VALUE characters");
		}
	}

	/**
	 * Tile Set Description
	 *
	 * @param description - test description of tileset
	 */
	@Option(name = "-d", aliases = "--description", metaVar = "<text tile set description>", usage = "Tile set description")
	public void setTileSetDescription(final String description)
	{
		if(description != null && description.length() <= HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.tileSetDescription = description;
		}
		else
		{
			throw new IllegalArgumentException("Provided Description is invalid, must be a "
											   + "non-null string shorter than MAGIC_MAX_VALUE characters");
		}
	}

	/**
	 * tile width
	 *
	 * @param width - tile width
	 * @throws IllegalArgumentException value must be between 1 - MAGIC_MAX_VALUE
	 */
	@Option(name = "-w", aliases = "--width", metaVar = "<1-9999>", usage = "Tile width in pixels; default is 256")
	public void setTileWidth(final int width)
	{
		if(width > 0 && width < HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.tileWidth = width;
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting tile Width to %d, "
															 +
															 "value must be greater than 0 and less than MAGIC_MAX_VALUE",
															 width));
		}
	}

	/**
	 * Tile Height
	 *
	 * @param height - tile height
	 * @throws IllegalArgumentException - value must be between 1 and MAGIC_MAX_VALUE
	 */
	@Option(name = "-h", aliases = "--height", metaVar = "<1-9999>", usage = "Tile height in pixels; default is 256")
	public void setTileHeight(final int height)
	{
		if(height > 0 && height < HeadlessOptions.MAGIC_MAX_VALUE)
		{
			this.tileHeight = height;
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting tile height to %d, "
															 +
															 "value must be greater than 0 and less than MAGIC_MAX_VALUE",
															 height));
		}
	}

	// image settings
	@SuppressWarnings("HardcodedFileSeparator")
	@Option(name = "-f", aliases = "--format", metaVar = "image/png or image/jpeg", usage = "Image format for tiling operations, default is png (options are png, jpeg,etc.)")
	private void setImageFormat(final String formatString) throws MimeTypeParseException
	{
		if(formatString.equalsIgnoreCase("image/jpeg")
		   || formatString.equalsIgnoreCase("image/png"))
		{
			this.imageFormat = new MimeType(formatString.toLowerCase());
		}
		else
		{
			//noinspection HardcodedFileSeparator
			throw new IllegalArgumentException(String.format("error setting image format to %s! must be 'image/jpeg' or 'image/png'",
															 formatString));
		}
	}

	@Option(name = "-c", aliases = "--compression", metaVar = "<jpeg>", usage = "Compression type for image tiling, default is jpeg")
	public void setCompressionType(final String compressionType)
	{
		if(compressionType.equalsIgnoreCase("jpeg"))
		{
			this.compressionType = compressionType.toLowerCase();
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting image compression to %s! must be jpeg",
															 compressionType));
		}
	}

	/**
	 * quality of compression as a percentage (1-100%)
	 *
	 * @param compressionQuality - quality of compression as a percentage
	 */
	@Option(name = "-q", aliases = "--quality", metaVar = "<1-100>", usage = "Compression quality for jpeg compression, between 0-100")
	public void setCompressionQuality(final int compressionQuality)
	{
		if(compressionQuality > 0 && compressionQuality <= 100)
		{
			this.compressionQuality = compressionQuality;
		}
		else
		{
			throw new IllegalArgumentException("Error: Compression Quality must be between 1-100");
		}
	}

	//Getters
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

	public int getInputSrs()
	{
		return this.inputSrs;
	}

	public File getInputFile()
	{
		return this.inputFile;
	}

	public MimeType getImageFormat()
	{
		return this.imageFormat;
	}

	/**
	 * returns the tilestore adaptor for input
	 *
	 * @return - adaptor, or null if validation has not yet occured, OR if an error was thrown.
	 * @throws IllegalArgumentException
	 */
	public HeadlessTileStoreAdapter getInputAdapter()
	{
		if(this.validator != null)
		{
			return this.validator.getInputAdapter();
		}
		throw new IllegalArgumentException("Validation has not occurred");
	}

	/**
	 * returns the tilestore adaptor for output
	 *
	 * @return - adaptor, or null if validation has not yet occured, OR if an error was thrown.
	 * @throws IllegalArgumentException - if validation failed or has not yet occured
	 */
	public HeadlessTileStoreAdapter getOutputAdapter()
	{
		if(this.validator != null)
		{
			return this.validator.getOutputAdapter();
		}
		throw new IllegalArgumentException("Validation has not occurred");
	}

	public File getOutputFile()
	{
		return this.outputFile;
	}

	public String getTileSetNameIn()
	{
		return this.tileSetNameIn;
	}

	public String getTileSetNameOut()
	{
		return this.tileSetNameOut;
	}

	public String getTileSetDescription()
	{
		return this.tileSetDescription;
	}

	public String getCompressionType()
	{
		return this.compressionType;
	}

	public int getCompressionQuality()
	{
		return this.compressionQuality;
	}

	public boolean isValid()
	{
		if(this.validator == null)
		{
			this.validator = new HeadlessOptionsValidator(this, this.logger);
		}
		return this.validator.isValid();
	}

	/**
	 * returns a full path to the specified file, replacing cmdline based shortcuts
	 *
	 * @param filePath - filepath to replace local charaters in
	 * @return absolute path of string
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("AccessOfSystemProperties")
	private static String getFullPath(final String filePath)
	{
		if(filePath != null && !filePath.isEmpty())
		{
			String path = filePath;
			if(path.startsWith('~' + File.separator))
			{
				path = String.format("%s%s", System.getProperty("user.home"), path.substring(1));
			}
			else if(path.startsWith('.' + File.separator))
			{
				path = String.format("%s%s", System.getProperty("user.dir"), path.substring(1));
			}
			return path;
		}
		throw new IllegalArgumentException("File Path provided is null or empty!");
	}
}
