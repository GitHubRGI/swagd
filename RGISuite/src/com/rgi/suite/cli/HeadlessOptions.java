/**
 *
 */
package com.rgi.suite.cli;

import org.gdal.osr.SpatialReference;
import org.kohsuke.args4j.Option;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;




/**
 * Business Logic for Running SWAGD in a headless environment. contains logic to
 * support Args4J command line arguments, as well as implementing the runnable
 * interface so it can be run in another thread
 *		-h                                     : show help
 *		--inputsrs <epsg int>                  : Input Spatial reference system EPSG identifier (ie 4326)
 *		--outputsrs <epsg int value>           : Desired output SRS EPSG identifier,eg 3857
 *		-c (--compression) <jpeg               : Compression type for image tiling,default is jpeg
 *		-d (--description) <text description>  : Tile set description
 *		-f (--format) <image/png or image/jpg> : Image format for tiling operations,default is png (options are png,jpeg,etc.)
 *		-h (--height) <Height>                 : Tile height in pixels; default is 256
 *		-i (-in) <Input File Path>             : REQUIRED! Input source for tiling/Packaging operation
 *		-ti (--intileset, --intilesetname) <text>   : Tile Set name for GeoPackages (input), default is short name of output geopackage.
 *		-to (--outtileset, --outtilesetname)    :Tile set name for geopackage output
 *		-o (-out) <Output File Path>           : Full output path for tiling/Packaging operation
 *		-q (--quality) <1-100>                 : Compression quality for jpeg compression, between 0-100
 *		-w (--width) <1-10000>                 : Tile width in pixels; default is 256
 *
 * @author matthew.moran
 */
public class HeadlessOptions
{

	// set to true when validate is called
	private HeadlessOptionsValidator validator = null;
	private boolean isValid = false;

	// variables to hold command line arguments
	private File inputFile;
	private File outputFile;
	private int outputSrs = 3857;
	private int inputSrs = 4326;
	private String tileSetNameIn = "";
	private String tileSetNameOut = "";
	private String tileSetDescription = "";
	private MimeType imageFormat;
	private int tileWidth = 256;
	private int tileHeight = 256;
	private String compressionType = "jpeg";
	private int quality = 75;
	private boolean isTiling = false;

	public HeadlessOptions()
	{
		try
		{
			this.imageFormat = new MimeType("image/jpeg");
		}
		catch (final MimeTypeParseException e)
		{
			System.out.println("Error creating default image format: "+ e.getMessage());
		}
	}

	/**
	 * path to the input file/directory for packaging/tiling.
	 *
	 * @param filePath
	 * @throws IllegalArgumentException if filePath leads to a non-existent file, is null, or is empty.
	 */
	@Option(required = true, name = "-i",aliases = {"-in"}, metaVar = "<Input File Path>", usage = "Input source for tiling/Packaging operation")
	public void setInputFile(final String filePath)
	{
		final String path = getFullPath(filePath);
		final File f = new File(path);
		if (f.exists())
		{
			this.inputFile = new File(path);
		}
		else
		{
			throw new IllegalArgumentException(String.format("FilePath: %s does not exist, input files must exist! \n", filePath));
		}
	}

	/**
	 * path to the output file/directory for packaging/tiling
	 *
	 * @param filePath
	 * @throws IllegalArgumentException - if output path  is null or empty
	 */
	@Option(required = true, name = "-o",aliases= {"-out"}, metaVar = "<Output File Path>", usage = "Full output path for tiling/Packaging operation")
	public void setOutputFile(final String filePath)
	{
		final String path = getFullPath(filePath);
		this.outputFile = new File(path);
	}

	/**
	 * EPSG output SRS number
	 *
	 * @param i
	 * @throws IllegalArgumentException if unrecognized spatial reference.
	 */
	@Option(name = "--outputsrs", metaVar = "<epsg srs int>", usage = "Desired output SRS EPSG identifier, eg 3857")
	public void setOutputSrs(final int i)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		try
		{
			if (srs.ImportFromEPSG(i) == 0)
			{
				this.outputSrs = i;
			}
			else
			{
				throw new IllegalArgumentException(String.format("Error: Output SRS %d is not a GDAL supported EPSG value.", i));
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(String.format("Error: Output SRS %d is not a GDAL supported EPSG value.", i));
		}


	}

	/**
	 * EPSG input SRS number, required if input is a tms cache
	 *
	 * @param i
	 * @throws IllegalArgumentException if unrecognized spatial reference.
	 */
	@Option(name = "--inputsrs", metaVar = "<epsg srs int>", usage = "Input Spatial reference system EPSG identifier (ie 4326)")
	public void setInputSRS(final int i)
	{
		// special case for TMS.
		final SpatialReference srs = new SpatialReference();
		try
		{
			if(srs.ImportFromEPSG(i) == 0)
			{
				this.inputSrs = i;
			}
			else
			{
				throw new IllegalArgumentException(String.format("Error: input SRS %d is not a GDAL supported EPSG value.", i));
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(String.format("Error: input SRS %d is not a GDAL supported EPSG value.", i));
		}
	}

	/**
	 * Tile set Name
	 *
	 * @param name
	 * @throws IllegalArgumentException - name is null, or longer than 10000 characters
	 */
	@Option(name = "-ti", metaVar = "<Tile Set Name>", aliases = {"--intileset", "--intilesetname"},
			usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	public void setTileSetNameIn(final String name)
	{
		if(name != null && name.length() <= 10000)
		{
			this.tileSetNameIn = name;
		}
		else
		{
			throw new IllegalArgumentException("Provided Name is invalid, must be a "
					+ "non-null string shorter than 10000 characters");
		}
	}

	/**
	 * Tile set Name
	 *
	 * @param name
	 * @throws IllegalArgumentException - name is null, or longer than 10000 characters
	 */
	@Option(name = "-to", metaVar = "<Tile Set Name>", aliases = {"--outtileset", "--outtilesetname"},
			usage = "Input Tile Set for GeoPackages, default is short name of output geopackage.")
	public void setTileSetNameOut(final String name)
	{
		if (name != null && name.length() <= 10000)
		{
			this.tileSetNameOut = name;
		}
		else
		{
			throw new IllegalArgumentException("Provided Name is invalid, must be a "
					+ "non-null string shorter than 10000 characters");
		}
	}
	/**
	 * Tile Set Description
	 *
	 * @param description
	 */
	@Option(name = "-d", aliases =
			{"--description"},metaVar="<text tile set description>", usage = "Tile set description")
	public void setTileSetDescription(final String description)
	{
		if (description != null && description.length() <= 10000)
		{
			this.tileSetDescription = description;
		}
		else
		{
			throw new IllegalArgumentException("Provided Description is invalid, must be a "
					+ "non-null string shorter than 10000 characters");
		}
	}

	/**
	 * tile width
	 *
	 * @param i
	 * @throws IllegalArgumentException value must be between 1 - 10000
	 */
	@Option(name = "-w", aliases =
			{"--width"}, metaVar = "<1-9999>", usage = "Tile width in pixels; default is 256")
	public void setTileWidth(final int i)
	{
		if (i > 0 && i < 10000)
		{
			this.tileWidth = i;
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting tile Width to %d, "
					+ "value must be greater than 0 and less than 10000", i));
		}
	}

	/**
	 * Tile Height
	 *
	 * @param i
	 * @throws IllegalArgumentException - value must be between 1 and 10000
	 */
	@Option(name = "-h", aliases =
			{"--height"}, metaVar = "<1-9999>", usage = "Tile height in pixels; default is 256")
	public void setTileHeight(final int i)
	{
		if (i > 0 && i < 10000)
		{
			this.tileHeight = i;
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting tile height to %d, "
					+ "value must be greater than 0 and less than 10000", i));
		}
	}


	// image settings
	@Option(name = "-f", aliases ={"--format"}, metaVar ="image/png or image/jpeg",
			usage = "Image format for tiling operations, default is png (options are png, jpeg,etc.)")
	private void setImageFormat(final String formatString) throws MimeTypeParseException,IllegalArgumentException
	{


		if (formatString.equalsIgnoreCase("image/jpeg")
				|| formatString.equalsIgnoreCase("image/png"))
		{
			this.imageFormat = new MimeType(formatString.toLowerCase());
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting image format to %s! must be 'image/jpeg' or 'image/png'", formatString));
		}
	}

	@Option(name = "-c", aliases = {"--compression"}, metaVar="<jpeg>",
			usage = "Compression type for image tiling, default is jpeg")
	public void setCompressionType(final String compressionType)
	{
		if (compressionType.equalsIgnoreCase("jpeg"))
		{
			this.compressionType = compressionType.toLowerCase();
		}
		else
		{
			throw new IllegalArgumentException(String.format("error setting image compression to %s! must be jpeg", compressionType));
		}
	}

	@Option(name = "-q", aliases =
			{"--quality"}, metaVar="<1-99>", usage = "Compression quality for jpeg compression, between 0-100")
	public void setCompressionQuality(final int i)
	{
		if (i > 0 && i <= 100)
		{
			this.quality = i;
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

	public TileFormat getInputType()
	{
		if(this.validator != null)
		{
			return this.validator.getInputType();
		}
		return TileFormat.ERR;
	}

	public TileFormat getOutputType()
	{
		if(this.validator!= null)
		{
			return this.validator.getOutputType();
		}
		return TileFormat.ERR;
	}

	public File getOutputFile()
	{
		return this.outputFile;
	}

	public String getTileSetNameIn()
	{
		return this.tileSetNameIn;
	}

	public String getTileSetNameOut(){return this.tileSetNameOut;}

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
		return this.quality;
	}

	public boolean isValid()
	{
		if(this.validator == null)
		{
			this.validator = new HeadlessOptionsValidator(this);
		}
		return this.validator.validate();
	}



	/**
	 * returns a full path to the specified file, replacing cmdline based shortcuts
	 *
	 * @param filePath
	 * @return
	 */
	private static String getFullPath(final String filePath) throws IllegalArgumentException
	{
		if (filePath != null && !filePath.isEmpty())
		{
			String path = filePath;
			if (path.startsWith("~" + File.separator))
			{
				path = System.getProperty("user.home") + path.substring(1);
			}
			else if(path.startsWith("." + File.separator))
			{
				path = System.getProperty("user.dir") + path.substring(1);
			}
			return path;
		}
		throw new IllegalArgumentException("File Path provided is null or empty!");
	}
}
