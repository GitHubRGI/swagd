package com.rgi.suite.cli;

import com.rgi.common.util.FileUtility;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by matthew.moran on 6/30/15.
 */
public class HeadlessValidatorTest
{
	public HeadlessValidatorTest()
	{
		super();
	}

	@Rule
	final public TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void destroyTempFolder()
	{
		this.tempFolder.delete();
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void validateRequiredOutputSRS() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.tif").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(this.tempFolder, ".gpkg");
		String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile, "--outputsrs", "4326", "--inputsrs", "4326"};
		parser.parseArgument(args);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream console = System.out;
		try
		{
			System.setOut(new PrintStream(bytes));
			if (opts.isValid())
			{
				fail("Should have returned invalid due to invalid output srs");
			}
		}
		finally
		{
			System.setOut(console);
		}

		assertTrue(bytes.toString().contains("For tiling into geopackage, output SRS must be 3857"));
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void validateRequiredInputsTileSetNameMissingGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.tif").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(this.tempFolder, ".gpkg");
		String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream console = System.out;
		try
		{
			System.setOut(new PrintStream(bytes));
			if (opts.isValid())
			{
				assertTrue(opts.getTileSetNameOut().equals(FileUtility.nameWithoutExtension(opts.getOutputFile())));
			}
			else
			{
				fail("Should have returned valid");
			}
		}
		finally
		{
			System.setOut(console);
		}
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidTilingTMS() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.tif").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(this.tempFolder, ".TMS"); //tms extension can be anything but .gpkg
		String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		if (opts.isValid())
		{
			assertTrue(opts.getOutputType() == TileFormat.TMS);
			assertTrue(opts.getInputType() == TileFormat.RAW);
		}
		else
		{
			fail("Unable to validate settings");
		}
	}


	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidTilingNonExistantGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.tif").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(this.tempFolder, ".gpkg");
		String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		if (opts.isValid())
		{
			assertTrue(opts.getOutputType() == TileFormat.GPKG);
			assertTrue(opts.getInputType() == TileFormat.RAW);
		}
		else
		{
			fail("Unable to Validate settings");
		}
	}
	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputInvalidEmptyGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.tif").getPath().toString();
		final String outputFile = HeadlessTestUtility.getRandomFile(6,".gpkg",this.tempFolder).getAbsolutePath();
		String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		if (opts.isValid())
		{

			fail("Should not pass validation due to the geopackage being an empty file");
		}
		else
		{
			assertTrue(opts.getOutputType() == TileFormat.ERR);
			assertTrue(opts.getInputType() == TileFormat.RAW);
		}
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void validateFailTilingSameTilesetNameGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{

		//Content information
		final String tableName = "testRaster";
		//set up streams
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream console = System.out;
		try
		{
			final HeadlessOptions opts = new HeadlessOptions();
			final CmdLineParser parser = new CmdLineParser(opts);
			final String inputFile = this.getClass().getResource("../../../../testRaster.tif").getPath().toString();
			final String outputFile = this.getClass().getResource("../../../../testRaster.gpkg").getPath().toString();
			String[] args = {"-op", "tile", "-in", inputFile, "-out", outputFile, "-to", tableName};
			parser.parseArgument(args);
			System.setOut(new PrintStream(bytes));
			if (opts.isValid())
			{
				fail("Should not have returned valid, as the table name already exists");
			}
			else
			{
				assertTrue(opts.getOutputType() == TileFormat.ERR);
				assertTrue(opts.getInputType() == TileFormat.RAW);
			}
		}
		catch (Exception e)
		{
			fail("Exception: \n" + e.getStackTrace());
		}
		finally
		{
			System.setOut(console);
		}
	}
	/**
	 *
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidTMStoGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster_TMS").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(tempFolder, ".gpkg");
		String[] args = {"-op", "package", "-in", inputFile, "-out", outputFile,"-to","testRaster","--inputsrs","3857"};
		parser.parseArgument(args);
		if (opts.isValid())
		{
			assertTrue("Correct output type parsed from filetype",opts.getOutputType() == TileFormat.GPKG);
			assertTrue("Correct input type parsed from filetype",opts.getInputType() == TileFormat.TMS);
			assertFalse("correct operation parsed",opts.isTiling());
		}
		else
		{
			fail("package settings failed to validate.");
		}
	}




	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidTMStoTMS() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster_TMS").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(tempFolder, "");
		String[] args = {"-op", "package", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		if (opts.isValid())
		{

			assertTrue(opts.getOutputType() == TileFormat.TMS);
			assertTrue(opts.getInputType() == TileFormat.TMS);
			assertFalse(opts.isTiling());
		}
		else
		{
			fail("package settings failed to validate.");
		}
	}
	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void validateFailNoTilesetGPKGtoTMS() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.gpkg").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(tempFolder, "");
		String[] args = {"-op", "package", "-in", inputFile, "-out", outputFile,};
		parser.parseArgument(args);
		if (opts.isValid())
		{

			fail("Validation passed when it should have failed due to no input tileset provided");
		}
		else
		{
			assertTrue("Input validation failed due to missing tileset",opts.getInputType() == TileFormat.ERR);
		}
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidGPKGtoTMS() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.gpkg").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(tempFolder, "");
		String[] args = {"-op", "package", "-in", inputFile, "-out", outputFile,"-ti","testRaster"};
		parser.parseArgument(args);
		if (opts.isValid())
		{

			assertTrue(opts.getOutputType() == TileFormat.TMS);
			assertTrue(opts.getInputType() == TileFormat.GPKG);
			assertFalse(opts.isTiling());
		}
		else
		{
			fail("Package settings failed to validate");
		}
	}
	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void inputValidGPKGtoGPKG() throws CmdLineException, IllegalArgumentException, IOException
	{
		final HeadlessOptions opts = new HeadlessOptions();
		final CmdLineParser parser = new CmdLineParser(opts);
		final String inputFile = getClass().getResource("../../../../testRaster.gpkg").getPath().toString();
		final String outputFile = HeadlessTestUtility.getNonExistantFileString(tempFolder, ".gpkg");
		String[] args = {"-op", "package", "-in", inputFile, "-out", outputFile,"-ti","testRaster","-to","outTable"};
		parser.parseArgument(args);
		if (opts.isValid())
		{

			assertTrue(opts.getOutputType() == TileFormat.GPKG);
			assertTrue(opts.getInputType() == TileFormat.GPKG);
			assertFalse(opts.isTiling());
		}
		else
		{
			fail("Package settings failed to validate");
		}
	}

}
