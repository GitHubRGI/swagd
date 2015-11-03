/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.suite.cli;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Test Class for the HeadlessOptions Class
 *
 * @author Matthew.Moran
 */
public class HeadlessOptionsTest
{
	private final Logger logger;

	HeadlessOptionsTest()
	{
		this.logger = Logger.getLogger( "RGISuite.logger" );
		this.logger.setLevel( Level.ALL );
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter( new SimpleFormatter() );
		this.logger.addHandler( handler );
	}

	// TODO
	@Rule
	public final TemporaryFolder tempFolder = new TemporaryFolder();

	@After
	public void destroyTempFolder()
	{
		this.tempFolder.delete();
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 */
	@Test(expected = CmdLineException.class)
	public void invalidArgsGibberish() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String[]        args   = {"-sdfsdf"};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon receiving invalid arguments" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTilingInputFile() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				Paths.get( this.tempFolder.getRoot().getAbsolutePath(), HeadlessTestUtility.getRandomString( 8 ),
						   ".test" ).toString();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the input file" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTilingOutputFile() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String   outputFile = ""; //empty output file fails
		final String[] args       = {"-in", inputFile, "-out", outputFile};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the Ouput file" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsOutputSrs() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args =
				{"-in", inputFile, "-out", outputFile, "--outputsrs", "12345"}; //srs 12345 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the Ouputsrs" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsInputSrs() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args =
				{"-in", inputFile, "-out", outputFile, "--inputsrs", "12345"}; //srs 12345 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the input srs" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTileWidthMax() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args =
				{"-in", inputFile, "-out", outputFile, "-w", "10000"}; //width 10000 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the tile width (too high)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTileWidthMin() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "-w", "0"}; //width 0 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the tile width (too low)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTileHeightMax() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args =
				{"-in", inputFile, "-out", outputFile, "-h", "10000"}; //height 10000 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the tile height (too high)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsTileHeightMin() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "-h", "0"}; //height 0 should fail
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the tile height (too low)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsImageFormat() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "--format", "unreal"};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the image format" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsCompressionType() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "--compression", "such"};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the compression type" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsImageQualityMax() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "-q", "121"};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the image quality (too high)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void invalidArgsImageQualityMin() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "-q", "0"};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception upon being unable to parse the image quality (too low)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = CmdLineException.class)
	public void missingRequiredInputsOperation() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception for missing required input (operation not specified)" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = CmdLineException.class)
	public void missingRequiredInputsInputFile() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-out", outputFile};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception for missing required input inputFile" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = CmdLineException.class)
	public void missingRequiredInputsOutputFile() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile};
		parser.parseArgument( args );
		Assert.fail( "parsing should have thrown exception for missing required input OutputFile" );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void malformedInputHomeDir() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final File testFile =
				File.createTempFile( "/testfile", ".tiff", new File( System.getProperty( "user.home" ) ) );
		try
		{
			final String inputFile = "~/" + testFile.getName();
			final String outputFile =
					HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
			final String[] args = {"-in", inputFile, "-out", outputFile};
			parser.parseArgument( args );
			Assert.assertEquals( "file path expanded correctly", opts.getInputFile().getPath(), testFile.getPath() );
		}
		finally
		{
			testFile.delete();
		}
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void malformedInputImageFormatCase() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "--format", "image/JPeg"};
		parser.parseArgument( args );
		Assert.assertEquals( "image format set correctly", "image/jpeg", opts.getImageFormat().toString() );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void malformedInputCompressionCase() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final String inputFile =
				HeadlessTestUtility.getRandomFile( 4, ".tif", this.tempFolder ).getAbsolutePath();
		final String outputFile =
				HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
		final String[] args = {"-in", inputFile, "-out", outputFile, "--compression", "JPeg"};
		parser.parseArgument( args );
		Assert.assertEquals( "Compression type set correctly!", "jpeg", opts.getCompressionType() );
	}

	/**
	 * @throws CmdLineException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void malformedInputCurrentDir() throws CmdLineException, IOException
	{
		final HeadlessOptions opts   = new HeadlessOptions( this.logger );
		final CmdLineParser   parser = new CmdLineParser( opts );
		final File testFile =
				File.createTempFile( "/testfile", ".tiff", new File( System.getProperty( "user.dir" ) ) );
		try
		{
			final String inputFile = "./" + testFile.getName();
			final String outputFile =
					HeadlessTestUtility.getRandomFile( 4, ".gpkg", this.tempFolder ).getAbsolutePath();
			final String[] args = {"-in", inputFile, "-out", outputFile};
			parser.parseArgument( args );
			Assert.assertEquals( "path correctly replaced ~ for homedir", opts.getInputFile().getPath(),
								 testFile.getPath() );
		}
		finally
		{
			testFile.delete();
		}
	}

}
