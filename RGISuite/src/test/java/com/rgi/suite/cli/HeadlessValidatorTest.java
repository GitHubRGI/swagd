package com.rgi.suite.cli;

import com.rgi.common.util.FileUtility;
import com.rgi.suite.cli.tilestoreadapter.GPKGTileStoreAdapter;
import com.rgi.suite.cli.tilestoreadapter.RawImageTileStoreAdapter;
import com.rgi.suite.cli.tilestoreadapter.TMSTileStoreAdapter;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by matthew.moran on 6/30/15.
 */
public class HeadlessValidatorTest
{
    private final Logger logger = Logger.getLogger("RGISuite.logger");

    @Before
    public void setUp()
    {
        this.logger.setLevel( Level.ALL );
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter( new SimpleFormatter() );
        this.logger.addHandler( handler );
    }

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
     * @throws IOException
     */
    /*@Test
    public void validateRequiredOutputSRS() throws CmdLineException, IOException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = this.getClass().getResource( "../../../../testRaster.tif" ).getPath();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, ".gpkg" );
        final String[] args =
                {"-in", inputFile, "-out", outputFile, "--outputsrs", "4326", "--inputsrs", "4326"};
        parser.parseArgument( args );
        final ByteArrayOutputStream bytes   = new ByteArrayOutputStream();
        final PrintStream           console = System.out;
        try
        {
            System.setOut( new PrintStream( bytes ) );
            if( opts.isValid() )
            {
                Assert.fail( "Should have returned invalid due to invalid output srs" );
            }
        }
        finally
        {
            System.setOut( console );
        }

        assertTrue( "log output valid!",
                    bytes.toString().contains( "For tiling into geopackage, output SRS must be 3857" ) );
    }
    */

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void validateRequiredInputsTileSetNameMissingGPKG() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = TestUtility.loadFileFromDisk("testRaster.tif").toString();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, ".gpkg" );
        final String[]        args       = {"-in", inputFile, "-out", outputFile,};
        parser.parseArgument( args );
        final ByteArrayOutputStream bytes   = new ByteArrayOutputStream();
        final PrintStream           console = System.out;
        try
        {
            System.setOut( new PrintStream( bytes ) );
            if( opts.isValid() )
            {
                assertEquals( "Tileset name set correctly", opts.getTileSetNameOut(),
                              FileUtility.nameWithoutExtension( opts.getOutputFile() ) );
            }
            else
            {
                Assert.fail( "Should have returned valid" );
            }
        }
        finally
        {
            System.setOut( console );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void inputValidTilingTMS() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts      = new HeadlessOptions( this.logger );
        final CmdLineParser   parser    = new CmdLineParser( opts );
        final String          inputFile = TestUtility.loadFileFromDisk("testRaster.tif").toString();
        final String outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder,
                                                                                ".TMS" ); //tms extension can be anything but .gpkg
        final String[] args = {"-in", inputFile, "-out", outputFile,};
        parser.parseArgument( args );
        if( opts.isValid() )
        {
            assertTrue( "Output validated correctly", opts.getOutputAdapter() instanceof TMSTileStoreAdapter );
            assertTrue( "Input validated correctly", opts.getInputAdapter() instanceof RawImageTileStoreAdapter );
        }
        else
        {
            Assert.fail( "Unable to isValid settings" );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void inputValidTilingNonExistantGPKG() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = TestUtility.loadFileFromDisk("testRaster.tif").toString();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, ".gpkg" );
        final String[]        args       = {"-in", inputFile, "-out", outputFile,};
        parser.parseArgument( args );
        if( opts.isValid() )
        {
            assertTrue( "Output validated correctly", opts.getOutputAdapter() instanceof GPKGTileStoreAdapter );
            assertTrue( "Input validated correctly", opts.getInputAdapter() instanceof RawImageTileStoreAdapter );
        }
        else
        {
            Assert.fail( "Unable to Validate settings" );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void inputInvalidEmptyGPKG() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts      = new HeadlessOptions( this.logger );
        final CmdLineParser   parser    = new CmdLineParser( opts );
        final String          inputFile = TestUtility.loadFileFromDisk("testRaster.tif").toString();
        final String outputFile =
                HeadlessTestUtility.getRandomFile( 6, ".gpkg", this.tempFolder ).getAbsolutePath();
        final String[] args = {"-in", inputFile, "-out", outputFile,};
        parser.parseArgument( args );
        if( opts.isValid() )
        {

            Assert.fail( "Should not pass validation due to the geopackage being an empty file" );
        }
        else
        {
            assertNull( "Output validation failed correctly", opts.getOutputAdapter() );
            assertTrue( "Input validated correctly", opts.getInputAdapter() instanceof RawImageTileStoreAdapter );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void validateFailTilingSameTilesetNameGPKG() throws CmdLineException, IOException, URISyntaxException
    {

        //Content information
        //set up streams
        final ByteArrayOutputStream bytes   = new ByteArrayOutputStream();
        final PrintStream           console = System.out;
        try
        {
            final HeadlessOptions opts = new HeadlessOptions( this.logger );
            final CmdLineParser parser = new CmdLineParser( opts );
            final String inputFile = TestUtility.loadFileFromDisk("testRaster.tif").toString();
            final String outputFile = TestUtility.loadFileFromDisk("testRaster.gpkg").toString();
            final String tableName = "testRaster";
            final String[] args = {"-in", inputFile, "-out", outputFile, "-to", tableName};
            parser.parseArgument( args );
            System.setOut( new PrintStream( bytes ) );
            if( opts.isValid() )
            {
                Assert.fail( "Should not have returned valid, as the table name already exists" );
            }
            else
            {

                assertNull( "Output validation failed correctly.", opts.getOutputAdapter() );
                assertTrue( "Input Validation succesful", opts.getInputAdapter() instanceof RawImageTileStoreAdapter );
            }
        }
        catch( final CmdLineException error )
        {
            Assert.fail( "Exception: " + error.getMessage() );
        }
        finally
        {
            System.setOut( console );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    //TODO: Test is broken on Windows
    //@Test
    //public void inputValidTMStoGPKG() throws CmdLineException, IOException
    //{
    //    final HeadlessOptions opts       = new HeadlessOptions( this.logger );
    //    final CmdLineParser   parser     = new CmdLineParser( opts );
    //    final String          inputFile  = ClassLoader.getSystemResource("testRaster_TMS").getPath();
    //    final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, ".gpkg" );
    //    final String[] args =
    //            {"-in", inputFile, "-out", outputFile, "-to", "testRaster", "--inputsrs", "3857"};
    //    parser.parseArgument( args );
    //    if( opts.isValid() )
    //    {
    //        assertTrue( "Correct output type parsed from filetype",
    //                    opts.getOutputAdapter() instanceof GPKGTileStoreAdapter );
    //        assertTrue( "Correct input type parsed from filetype",
    //                    opts.getInputAdapter() instanceof TMSTileStoreAdapter );
    //    }
    //    else
    //    {
    //        Assert.fail( "package settings failed to isValid." );
    //    }
    //}

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    //TODO: Test is broken on Windows platform
    //@Test
    //public void inputValidTMStoTMS() throws CmdLineException, IOException
    //{
    //    final HeadlessOptions opts       = new HeadlessOptions( this.logger );
    //    final CmdLineParser   parser     = new CmdLineParser( opts );
    //    final String          inputFile  = this.getClass().getResource( "../../../../testRaster_TMS" ).getPath();
    //    final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, "" );
    //    final String[]        args       = {"-in", inputFile, "-out", outputFile,};
    //    parser.parseArgument( args );
    //    if( opts.isValid() )
    //    {

    //        assertTrue( "TMS determined as output type!", opts.getOutputAdapter() instanceof TMSTileStoreAdapter );
    //        assertTrue( "TMS determined as output type!", opts.getInputAdapter() instanceof TMSTileStoreAdapter );

    //    }
    //    else
    //    {
    //        Assert.fail( "package settings failed to isValid." );
    //    }
    //}

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void validateFailNoTilesetGPKGtoTMS() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = TestUtility.loadFileFromDisk("testRaster.gpkg").toString();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, "" );
        final String[]        args       = {"-in", inputFile, "-out", outputFile,};
        parser.parseArgument( args );
        if( opts.isValid() )
        {

            Assert.fail( "Validation passed when it should have failed due to no input tileset provided" );
        }
        else
        {
            assertNull( "Input validation failed due to missing tileset", opts.getInputAdapter() );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void inputValidGPKGtoTMS() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = TestUtility.loadFileFromDisk("testRaster.gpkg").toString();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, "" );
        final String[]        args       = {"-in", inputFile, "-out", outputFile, "-ti", "testRaster"};
        parser.parseArgument( args );
        if( opts.isValid() )
        {

            assertTrue( "TMS determined as output type!", opts.getOutputAdapter() instanceof TMSTileStoreAdapter );
            assertTrue( "GPKG determined as input type!", opts.getInputAdapter() instanceof GPKGTileStoreAdapter );
        }
        else
        {
            Assert.fail( "Package settings failed to isValid" );
        }
    }

    /**
     * @throws CmdLineException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    @Test
    public void inputValidGPKGtoGPKG() throws CmdLineException, IOException, URISyntaxException
    {
        final HeadlessOptions opts       = new HeadlessOptions( this.logger );
        final CmdLineParser   parser     = new CmdLineParser( opts );
        final String          inputFile  = TestUtility.loadFileFromDisk("testRaster.gpkg").toString();
        final String          outputFile = HeadlessTestUtility.getNonExistantFileString( this.tempFolder, ".gpkg" );
        final String[] args =
                {"-in", inputFile, "-out", outputFile, "-ti", "testRaster", "-to", "outTable"};
        parser.parseArgument( args );
        if( opts.isValid() )
        {

            assertTrue( "GPKG determined as output type!", opts.getOutputAdapter() instanceof GPKGTileStoreAdapter );
            assertTrue( "GPKG determined as input type!", opts.getInputAdapter() instanceof GPKGTileStoreAdapter );
        }
        else
        {
            Assert.fail( "Package settings failed to isValid" );
        }
    }

}
