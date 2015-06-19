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

package geopackage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import utility.TestUtility;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.GeoPackage.OpenMode;
import com.rgi.android.geopackage.core.Content;
import com.rgi.android.geopackage.core.SpatialReferenceSystem;
import com.rgi.android.geopackage.tiles.TileSet;
import com.rgi.android.geopackage.utility.DatabaseVersion;
import com.rgi.android.geopackage.verification.ConformanceException;
import com.rgi.android.geopackage.verification.VerificationLevel;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class GeoPackageCoreAPITest
{
    private final Random randomGenerator = new Random();

    /**Tests if the GeoPackage class when accepting a file {using the method create(File file)}, the file is the same when using the getFile() method.
    *
    * @throws SQLException
    * @throws Exception
    */

    @Test
    public void createMethodSettingFile() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(3);

        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            assertTrue("The file given to the GeoPackage using the method create(File file) does not return the same file",
                       gpkg.getFile().equals(testFile));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**Tests if the GeoPackage.create(File file) will throw a FileAlreadyExists exception
     * when given a file that has already been created.
     * @throws FileAlreadyExistsException
     * @throws Exception
     */
    @Test(expected = IOException.class) //TODO if this test fails it could be due to the fact that this use to be a FileAlreadyExistsException
    public void createMethod2FileExistsExceptionThrown() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        testFile.createNewFile();

        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            fail("This test should throw a FileAlreadyExistsException when trying to create a file that already exists.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**Opens a GeoPackage that has not been created, should throw a FileNotFoundException.
     *
     * @throws FileNotFoundException
     * @throws Exception
     */
    @Test(expected = FileNotFoundException.class)
    public void OpenAGeoPackageFileNotFoundExceptionThrown() throws FileNotFoundException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);

        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Open);

        try
        {
            fail("The GeoPackage should throw a FileNotFoundException when trying to open a GeoPackage with a file that hasn't been created.");
        }
        finally
        {
           gpkg.close();
           TestUtility.deleteFile(testFile);
        }
    }

    /** Gives a GeoPackage a null file to ensure it throws an IllegalArgumentException.
     *
     * @throws IllegalArgumentException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void CreateAGeoPackageIllegalArgumentExceptionForFiles() throws IllegalArgumentException, Exception
    {
        final GeoPackage gpkg = new GeoPackage(null);

        try
        {
            fail("The GeoPackage should throw an IllegalArgumentException for trying to create a file that is null.");
        }
        finally
        {
            gpkg.close();
        }
    }


    /**
     * Creates a GeoPackage with a file and checks to make sure that the Application Id is set correctly.
     *
     *  The Application Id of GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
     * application id field of the SQLite database header to indicate a
     * GeoPackage version 1.0 file.
     *
     * @throws Exception
     */
     // TODO The current version of SQLite being used doesn't support app id
//    @Test
//    public void verifyApplicationId() throws Exception
//    {
//        final File testFile = TestUtility.getRandomFile(12);
//        final GeoPackage gpkg = new GeoPackage(testFile);
//
//        try
//        {
//            assertTrue(String.format(Locale.getDefault(),
//                                     "The GeoPackage Application Id is incorrect. Application Id Expected (in int):  %d  Application Id recieved: %d",
//                                            geoPackageApplicationId, gpkg.getApplicationId()),
//                                     gpkg.getApplicationId() == geoPackageApplicationId);
//        }
//        finally
//        {
//           gpkg.close();
//           TestUtility.deleteFile(testFile);
//        }
//    }

    /**
     * Creates a GeoPackage with the method create(File file) and verifies that the sqlite version is correct.
     *
     * The Sqlite version required for a GeoPackage shall contain SQLite 3 format.
     *
     * @throws Exception
     */
    @Test
    public void verifySqliteVersion() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);

        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            // get the first number in the sqlite version and make sure it is a version 3
            final DatabaseVersion sqliteVersion = gpkg.getSqliteVersion();

            assertTrue(String.format("The GeoPackage Sqlite Version is incorrect. Sqlite Version Id Expected: %d.x SqliteVersion recieved: %s",
                                     geoPackageSqliteMajorVersion,
                                     sqliteVersion.toString()),
                       sqliteVersion.getMajor() == geoPackageSqliteMajorVersion);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Test if the SQLiteVerison is the same even when it is called twice.
     *
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void getSqliteVersionFromFileWithSQLiteVersion() throws SQLException, Exception
    {
        final File testFile      = TestUtility.getRandomFile(6);
        final int  randomInteger = this.randomGenerator.nextInt();

        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rw");
            try
            {
                // https://www.sqlite.org/fileformat2.html
                // Bytes 96 -> 100 are an int representing the sqlite version
                // used random integer
                randomAccessFile.seek(96);
                randomAccessFile.writeInt(randomInteger);
                randomAccessFile.close();

                final GeoPackage gpkg2 = new GeoPackage(testFile, OpenMode.Open);

                try
                {
                    gpkg2.getSqliteVersion();
                    final DatabaseVersion sqliteVersionChanged = gpkg2.getSqliteVersion();

                    assertTrue(String.format("The method getSqliteVersion() did not detect the same Version expected. Expected: %s   Actual: %s",
                                             this.sqliteVersionToString(randomInteger),
                                             sqliteVersionChanged),
                              sqliteVersionChanged.toString()
                                                  .equals(this.sqliteVersionToString(randomInteger)));
                }
                finally
                {
                    gpkg2.close();
                }
            }
            finally
            {
                randomAccessFile.close();
            }
        }
        finally
        {
           gpkg.close();
           TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when trying to add
     * the same content twice
     * @throws Exception
     */
    @Test
    public void addExistingContentVerify() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);

        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            //Content information
            final String                  tableName   = "tableName";
            final String                  identifier  = "identifier";
            final String                  description = "description";
            final BoundingBox             boundingBox = new BoundingBox(0.0,0.0,0.0,0.0);
            final SpatialReferenceSystem  srs         = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            //add the content to gpkg
            final TileSet tileSet  = gpkg.tiles().addTileSet(tableName, identifier, description, boundingBox, srs);
            final String  dataType = tileSet.getDataType();

             //try to add the same content twice
            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, srs);

            assertTrue("The GeoPackage returned false when it should have returned true when the content entries were equal.",
                       content.getBoundingBox()           .equals(tileSet.getBoundingBox()) &&
                       content.getDataType()              .equals(tileSet.getDataType())    &&
                       content.getDescription()           .equals(tileSet.getDescription()) &&
                       content.getIdentifier()            .equals(tileSet.getIdentifier())  &&
                       content.getLastChange()            .equals(tileSet.getLastChange())  &&
                       content.getSpatialReferenceSystemIdentifier().equals(tileSet.getSpatialReferenceSystemIdentifier()));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when trying to add
     * content with the same tablename but differing other fields
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingContentWithSameTableNameAndDifferentOtherValues() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            //Content information
            final String                  tableName   = "tableName";
            final SpatialReferenceSystem  srs         = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

             //try to add the same content twice
            gpkg.core().addContent(tableName, "dataType",  "identifier",  "description", new BoundingBox(0.0, 0.0, 0.0, 0.0), srs);
            gpkg.core().addContent(tableName, "dataType2", "identifier2", "description", new BoundingBox(0.0, 0.0, 0.0, 0.0), srs);

            fail("Expected GeoPackage Core to throw an IllegalArgumentException when trying to add content with the same tablename but differing other fields");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for name.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadName() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem(null, "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for name.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with an empty string for name.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadName2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("", "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for name.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for organization.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadOrganization() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("srsName", null, 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for organization.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a empty string for organization.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadOrganization2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for organization.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for definition.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadDefinition() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "organization", 123, null, "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create a SRS with a null parameter for definition.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with an empty string for definition.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadDefinition2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "organization", 123, "", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for definition.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage returns the same SRS when
     * adding the exact same SRS twice
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test
    public void createSpatialReferenceSystemSameSRS() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String name           = "srsName";
            final String organization   = "organization";
            final int    organizationId = 123;
            final String description    = "description";
            final String definition     = "definition";

            final SpatialReferenceSystem firstSRS  = gpkg.core().addSpatialReferenceSystem(name, organization, organizationId, definition, description);
            final SpatialReferenceSystem secondSRS = gpkg.core().addSpatialReferenceSystem(name, organization, organizationId, definition, description);

            assertTrue("GeoPackage did not return the same SRS objects as expected.", firstSRS.equals(secondSRS));
        }
        finally
        {
           gpkg.close();
           TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests to see if a null description can be inserted into the GeoPackage and
     * verifies that getSpatialReferenceSystem returns the same SRS as it was
     * given when adding a tile set
     */
    @Test
    public void addSpatialReferenceSystemNullDescription() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String organization = "org";
            final int    id           = 123;

            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name",
                                                                                         organization,
                                                                                         id,
                                                                                         "definition",
                                                                                         null);

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(organization, id);

            assertTrue("The GeoPackage get Spatial Reference System does not give back the value expeced.",
                       testSrs.equals(gpkgSrs));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two SRS's such that they are the same Organization and Organization Id but other fields differ.
     * In this case the Definitions are different.
     *
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields() throws RuntimeException, SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                  "org",
                                                  9804,
                                                  "definition",
                                                  "description");
            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                  "org",
                                                  9804,
                                                  "different definition",
                                                  "description");

            fail("The GeoPackage should throw a RuntimeException when adding Spatial Reference System with the same SRS id but have different definition fields.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two SRS's such that they are the same Organization and Organization Id but other fields differ.
     * In this case the names are different.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields2() throws RuntimeException, SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

          gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                "org",
                                                9804,
                                                "definition",
                                                "description");

          gpkg.core().addSpatialReferenceSystem("scaled different mercator",
                                                "org",
                                                9804,
                                                "definition",
                                                "description");


          fail("The GeoPackage should throw a IllegalArgumentException when adding a Spatial Reference System with the same SRS id but have different names.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two SRS's such that they are the same Organization and Organization Id but other fields differ.
     * In this case the names's are different.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields3() throws RuntimeException, SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                  "org",
                                                  9804,
                                                  "definition",
                                                  "description");


            gpkg.core().addSpatialReferenceSystem("different name",
                                                  "org",
                                                  9804,
                                                  "definition",
                                                  "description");

            fail("The GeoPackage should throw a IllegalArgumentException when adding a Spatial Reference System "
                    + "with the same and SRS identifier but diffent names");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage getters are returning the right values
     * for a Spatial Reference System object.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test
    public void addSRSVerify() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String name         = "scaled world mercator";
            final String organization = "org";
            final int    orgId        = 9;
            final String definition   = "definition";
            final String description  = "description";

            final SpatialReferenceSystem srs = gpkg.core().addSpatialReferenceSystem(name,
                                                                                     organization,
                                                                                     orgId,
                                                                                     definition,
                                                                                     description);

              assertTrue("GeoPackage did not return expected values for the SRS given",
                         srs.getName()             .equals(name)         &&
                         srs.getOrganization()     .equals(organization) &&
                         srs.getOrganizationSrsId() ==  orgId            &&
                         srs.getDefinition()       .equals(definition)   &&
                         srs.getDescription()      .equals(description));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage SpatialReferenceSystem object returns
     * true when evaluating two identical SRS with the equals and HashCode methods
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test
    public void addExistingSRSTwiceVerifyHashCodeAndEquals() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String name         = "scaled world mercator";
            final String organization = "org";
            final int    orgId        = 9;
            final String definition   = "definition";
            final String description  = "description";

            final SpatialReferenceSystem srs1 = gpkg.core().addSpatialReferenceSystem(name, organization, orgId, definition, description);
            final SpatialReferenceSystem srs2 = gpkg.core().addSpatialReferenceSystem(name, organization, orgId, definition, description);

            assertTrue("The GeoPackage returned false when it should have returned true for two equal SRS objects. ",srs1.equals(srs2));
            assertTrue("The HashCode for the same srs differed.",srs1.hashCode() == srs2.hashCode());

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage returns the correct specified SRS identified by the srs Id.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void getSpatialReferenceSystem() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name", "org", 111, "def", "desc");

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem("org", 111);

            assertTrue("The GeoPackage did not return expected result for SpatialReferenceSystem in method getSpatialReferenceSystem.",
                         gpkgSrs.equals(testSrs));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage returns the null when SRS does not exist in GeoPackage.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void getSpatialReferenceSystem2() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(555);

            assertTrue("The GeoPackage did not return null for SpatialReferenceSystem that did not exist in method getSpatialReferenceSystem.",
                        gpkgSrs == null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Asks the GeoPackage to retrieve a Spatial Reference System
     * that doesn't exist in the GeoPackage and verifies the GeoPackage
     * returns a null value.
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getSpatialReferenceSystem3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem("org", 222);
            assertTrue("The GeoPackage did not return a null value for spatial reference system as expected "
                           + "using the method getSpatialReferenceSystem(String, int) when searching for a spatial "
                           + "reference system that did not exist in the GeoPackage.",
                       gpkgSrs == null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests if the GeoPackage can return a spatial
     * reference system object that was created by the user
     *  and verify that it is the expected values when using the method
     * getSpatialReferenceSystem(String, int) also ensures
     * that the search for the srs is case insensitive
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getSpatialReferenceSystem4() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String name              = "name";
            final String organization      = "organization";
            final int    organizationSrsId = 333;
            final String definition        = "definition";
            final String description       = "description";

            final SpatialReferenceSystem srsAdded = gpkg.core().addSpatialReferenceSystem(name,
                                                                                          organization,
                                                                                          organizationSrsId,
                                                                                          definition,
                                                                                          description);

            final SpatialReferenceSystem srsFound = gpkg.core().getSpatialReferenceSystem("oRgaNiZaTiOn",
                                                                                          organizationSrsId);

            assertTrue("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
                        + "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.",
                       srsFound.equals(srsAdded));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests if the GeoPackage can return a spatial
     * reference system object from the default values already in the GeoPackage
     * and verify it is the expected values when using the method
     * getSpatialReferenceSystem(String, int)
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getSpatialReferenceSystem5() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

            final SpatialReferenceSystem srsAdded = gpkg.core().getSpatialReferenceSystem(0);

            final SpatialReferenceSystem srsFound = gpkg.core().getSpatialReferenceSystem("NONE",
                                                                                     0);

            assertTrue("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
                        + "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.",
                       srsFound.equals(srsAdded));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }



    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for tableName");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent(null, "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for tableName");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName3() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("123", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that does not begin with letters or underscore");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName4() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("gpkg_table", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that begins with gpkg");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }
    /**
     * Tests if GeoPackage Core would throw
     * an Illegal Argument Exception when passing
     * a null value for Bounding box
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected= IllegalArgumentException.class)
    public void addContentBadBoundingBox() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("tablename", "tiles", "identifier", "description", null, gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            fail("Expected GeoPackageCore to throw an IllegalArgumentException when giving a null balue for bounding box");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving an empty string for getContnent's tablename
     * parameter
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadTableName() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().getContent("", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is an empty string");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving an null value for getContnent's tablename
     * parameter
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadTableName2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().getContent(null, null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is null");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving a null value for getContnent's contentFactory
     * parameter
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadContentFactory() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().getContent("tables", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for contentFactory");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when given a bad data type when adding content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadDataType() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("thetable", "", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for data type");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when given a bad data type when adding content
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadDataType2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().addContent("thetable", null, "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for data type");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when giving a bad data type in getContent
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadDataType() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().getContent(null, null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing null for the dataType in getContent method");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when giving a bad data type in getContent
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadDataType2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.core().getContent("", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing an empty string for the dataType in getContent method");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when given a null
     * parameter for getContent.
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentNullParameter() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().getContent("tiles", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException when given a null parameter for ContentFactory in getContent");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * table name doesn't match.
     *
     * @throws SQLException
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws ConformanceException
     */
    @Test
    public void equalsContent() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final String tableName = "tableName";
        final GeoPackage gpkg = this.createGeoPackage(tableName, "columnName", testFile);

        try
        {
            final String dataType  = "tiles";
            final String identifier = "identifier";
            final String description = "description";
            final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 0.0, 0.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem);

            assertTrue("Returned true when it should have returned false when using the Contents equals method.",
                       !content.equals("different Table Name",
                                           dataType,
                                           identifier,
                                          description,
                                          boundingBox,
                                          spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * bounding boxes don't match.
     *
     * @throws SQLException
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws ConformanceException
     */
    @Test
    public void equalsContent2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final String tableName = "tableName";
        final GeoPackage gpkg = this.createGeoPackage(tableName, "columnName", testFile);


        try
        {
            final String dataType  = "tiles";
            final String identifier = "identifier";
            final String description = "description";
            final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 0.0, 0.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);



            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem);

            assertTrue("Returned true when it should have returned false when using the Contents equals method.",
                        !content.equals(tableName,
                                        dataType,
                                        identifier,
                                        description,
                                        new BoundingBox(2.0, 1.0, 4.0, 3.0),
                                        spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
           gpkg.close();
           TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * description doesn't match.
     *
     * @throws SQLException
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws ConformanceException
     */
    @Test
    public void equalsContent3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final String tableName = "tableName";
        final GeoPackage gpkg = this.createGeoPackage(tableName, "columnName", testFile);

        try
        {
            final String dataType  = "tiles";
            final String identifier = "identifier";
            final String description = "description";
            final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 0.0, 0.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem);

            assertTrue("Returned true when it should have returned false when using the Contents equals method.",
                       !content.equals(tableName,
                                          dataType,
                                          identifier,
                                          "different description",
                                          boundingBox,
                                          spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * spatial reference systems don't match.
     *
     * @throws SQLException
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws ConformanceException
     */
    @Test
    public void equalsContent4() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final String tableName = "tableName";
        final GeoPackage gpkg = this.createGeoPackage(tableName, "columnName", testFile);

        try
        {
            final String dataType  = "tiles";
            final String identifier = "identifier";
            final String description = "description";
            final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 0.0, 0.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem);

            assertTrue("Returned true when it should have returned false when using the Contents equals method.",
                       !content.equals(tableName,
                                           dataType,
                                           identifier,
                                          description,
                                          boundingBox,
                                          gpkg.core()
                                              .getSpatialReferenceSystem(0)
                                              .getIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * description doesn't match.
     *
     * @throws SQLException
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws ConformanceException
     */
    @Test
    public void equalsContent5() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final String tableName = "tableName";
        final GeoPackage gpkg = this.createGeoPackage(tableName, "columnName", testFile);

        try
        {
            final String dataType  = "tiles";
            final String identifier = null;
            final String description = null;
            final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 0.0, 0.0);
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            final Content content = gpkg.core().addContent(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem);

            assertTrue("Returned true when it should have returned false when using the Contents equals method.",
                       !content.equals(tableName,
                                        dataType,
                                          null,
                                          "different description",
                                          boundingBox,
                                          spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    private GeoPackage createGeoPackage(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.close();
            this.createTable(tableName, columnName, testFile);

            return new GeoPackage(testFile, VerificationLevel.None, OpenMode.Open);
        }
        finally
        {
            gpkg.close();
        }
    }

    private void createTable(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, SQLException
    {
        final String createTable = String.format("CREATE TABLE %s ( %s TEXT," +
                                                             "other_column INTEGER NOT NULL," +
                                                             "more_columns INTEGER NOT NULL," +
                                                             "last_Column TEXT NOT NULL)",
                                            tableName,
                                            columnName);

        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            final Statement stmt = con.createStatement();

            try
            {
                stmt.execute(createTable);

            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
        }
    }

    private String sqliteVersionToString(final int randomInt)
    {
        // Major/minor/revision, https://www.sqlite.org/fileformat2.html
        final int major = randomInt / 1000000;
        final int minor = (randomInt - (major * 1000000)) / 1000;
        final int revision = randomInt - ((major * 1000000) + (minor * 1000));

        return String.format(Locale.getDefault(), "%d.%d.%d", major, minor, revision);
    }

    /**
     * The Sqlite version required for a GeoPackage shall contain SQLite 3
     * format
     */
    private final static int geoPackageSqliteMajorVersion = 3;

    /**
     * A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
     * application id field of the SQLite database header to indicate a
     * GeoPackage version 1.0 file. When converting 0x47503130 to an integer it
     * results in 1196437808
     */
    private final static int geoPackageApplicationId = 1196437808;


}
