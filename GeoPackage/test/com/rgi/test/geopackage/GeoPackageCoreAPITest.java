/* The MIT License (MIT)
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.test.geopackage;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.utility.DatabaseVersion;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jenifer Cochran
 */
@SuppressWarnings("javadoc")
public class GeoPackageCoreAPITest
{
    @BeforeClass
    public static void setUp() throws ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver
    }

    /**
     * Tests if the GeoPackage class when accepting a file {using the method create(File file)}, the file is the same when using the getFile() method.
     */
    @Test
    public void createMethodSettingFile() throws SQLException, ConformanceException, IOException, ClassNotFoundException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            assertEquals("The file given to the GeoPackage using the method create(File file) does not return the same file", gpkg.getFile(), testFile);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage.create(File file) will throw a FileAlreadyExists exception
     * when given a file that has already been created.
     */
    @Test(expected = FileAlreadyExistsException.class)
    public void createMethod2FileExistsExceptionThrown() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final GeoPackage ignored = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            fail("This test should throw a FileAlreadyExistsException when trying to create a file that already exists.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Opens a GeoPackage that has not been created, should throw a FileNotFoundException.
     */
    @Test(expected = FileNotFoundException.class)
    @SuppressWarnings("ExpectedExceptionNeverThrown") // Intellij bug?
    public void openNonexistantFile() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage ignored = new GeoPackage(testFile, GeoPackage.OpenMode.Open))
        {
            fail("The GeoPackage should throw a FileNotFoundException when trying to open a GeoPackage with a file that hasn't been created.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage a null file to ensure it throws an IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createAGeoPackageIllegalArgumentExceptionForFiles() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        try(final GeoPackage ignored = new GeoPackage(null))
        {
            fail("The GeoPackage should throw an IllegalArgumentException for trying to create a file that is null.");
        }
    }


    /**
     * Creates a GeoPackage with a file and checks to make sure that the Application Id is set correctly.
     *  The Application Id of GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
     * application id field of the SQLite database header to indicate a
     * GeoPackage version 1.0 file.
     */
    @Test
    public void verifyApplicationId() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            assertEquals(String.format("The GeoPackage Application Id is incorrect. Application Id Expected (in int):  %d  Application Id recieved: %d",
                                       geoPackageApplicationId,
                                       gpkg.getApplicationId()),
                         geoPackageApplicationId,
                         gpkg.getApplicationId());
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Creates a GeoPackage with the method create(File file) and verifies that the SQLite version is correct.
     * The SQLite version required for a GeoPackage shall contain SQLite 3 format.
     */
    @Test
    public void verifySqliteVersion() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            // get the first number in the SQLite version and make sure it is a version 3
            final DatabaseVersion sqliteVersion = gpkg.getSqliteVersion();

            assertEquals(String.format("The GeoPackage Sqlite Version is incorrect. Sqlite Version Id Expected: %d.x SqliteVersion recieved: %s",
                                       geoPackageSqliteMajorVersion,
                                       sqliteVersion.toString()),
                         geoPackageSqliteMajorVersion,
                         sqliteVersion.getMajor());
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Test if the SQLite version is the same even when it is called twice.
     */
    @Test
    @SuppressWarnings("OverlyBroadThrowsClause") // Complains about IOException vs FileNotFound exception. Using one rather than the other causes circular warnings
    public void getSqliteVersionFromFileWithSQLiteVersion() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage ignored = new GeoPackage(testFile))
        {
            try(RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rw"))
            {
                // https://www.sqlite.org/fileformat2.html
                // Bytes 96 -> 100 are an int representing the SQLite version
                // used random integer
                randomAccessFile.seek(96);
                final int versionNumber = 42;
                randomAccessFile.writeInt(versionNumber);
                randomAccessFile.close();

                try(final GeoPackage gpkg2 = new GeoPackage(testFile, GeoPackage.OpenMode.Open))
                {
                    gpkg2.getSqliteVersion();
                    final DatabaseVersion sqliteVersionChanged = gpkg2.getSqliteVersion();

                    assertEquals(String.format("The method getSqliteVersion() did not detect the same Version expected. Expected: %s   Actual: %s",
                                               GeoPackageCoreAPITest.sqliteVersionToString(versionNumber),
                                               sqliteVersionChanged), sqliteVersionChanged.toString(), GeoPackageCoreAPITest.sqliteVersionToString(versionNumber));
                }
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when trying to add
     * the same content twice
     */
    @Test
    public void addExistingContentVerify() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
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
                       content.getMinimumX()   .equals(tileSet.getMinimumX())    &&
                       content.getMaximumX()   .equals(tileSet.getMaximumX())    &&
                       content.getMinimumY()   .equals(tileSet.getMinimumY())    &&
                       content.getMaximumY()   .equals(tileSet.getMaximumY())    &&
                       content.getDataType()   .equals(tileSet.getDataType())    &&
                       content.getDescription().equals(tileSet.getDescription()) &&
                       content.getIdentifier() .equals(tileSet.getIdentifier())  &&
                       content.getLastChange() .equals(tileSet.getLastChange())  &&
                       content.getSpatialReferenceSystemIdentifier().equals(tileSet.getSpatialReferenceSystemIdentifier()));

        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when trying to add
     * content with the same table name but differing other fields
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingContentWithSameTableNameAndDifferentOtherValues() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
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
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadName() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem(null, "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for name.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with an empty string for name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadName2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("", "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for name.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for organization.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadOrganization() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", null, 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for organization.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a empty string for organization.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadOrganization2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for organization.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with a null parameter for definition.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadDefinition() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "organization", 123, null, "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create a SRS with a null parameter for definition.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with an empty string for definition.
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemBadDefinition2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", "organization", 123, "", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for definition.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage returns the same SRS when
     * adding the exact same SRS twice
     */
    @Test
    public void createSpatialReferenceSystemSameSRS() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name           = "srsName";
            final String organization   = "organization";
            final int    organizationId = 123;
            final String description    = "description";
            final String definition     = "definition";

            final SpatialReferenceSystem firstSRS  = gpkg.core().addSpatialReferenceSystem(name, organization, organizationId, definition, description);
            final SpatialReferenceSystem secondSRS = gpkg.core().addSpatialReferenceSystem(name, organization, organizationId, definition, description);

            assertEquals("GeoPackage did not return the same SRS objects as expected.", firstSRS, secondSRS);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests to see if a null description can be inserted into the GeoPackage and
     * verifies that getSpatialReferenceSystem returns the same SRS as it was
     * given when adding a tile set
     */
    @Test
    public void addSpatialReferenceSystemNullDescription() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String organization = "org";
            final int    id           = 123;

            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name",
                                                                                         organization,
                                                                                         id,
                                                                                         "definition",
                                                                                         null);

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(organization, id);

            assertEquals("The GeoPackage get Spatial Reference System does not give back the value expeced.", testSrs, gpkgSrs);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two spatial reference systems such that they are the
     * same Organization and Organization Id but other fields differ. In this
     * case the Definitions are different.
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two spatial reference systems such that they are the
     * same Organization and Organization Id but other fields differ. In this
     * case the names are different.
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Gives a GeoPackage two spatial reference systems such that they are the
     * same Organization and Organization Id but other fields differ. In this
     * case the names's are different.
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields3() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage getters are returning the right values
     * for a Spatial Reference System object.
     */
    @Test
    public void addSRSVerify() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
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
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage SpatialReferenceSystem object returns
     * true when evaluating two identical SRS with the equals and HashCode methods
     */
    @Test
    public void addExistingSRSTwiceVerifyHashCodeAndEquals() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name         = "scaled world mercator";
            final String organization = "org";
            final int    orgId        = 9;
            final String definition   = "definition";
            final String description  = "description";

            final SpatialReferenceSystem srs1 = gpkg.core().addSpatialReferenceSystem(name, organization, orgId, definition, description);
            final SpatialReferenceSystem srs2 = gpkg.core().addSpatialReferenceSystem(name, organization, orgId, definition, description);

            assertEquals("The GeoPackage returned false when it should have returned true for two equal SRS objects. ", srs1, srs2);
            assertEquals("The HashCode for the same srs differed.", srs1.hashCode(), srs2.hashCode());

        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage returns the correct specified SRS identified by the srs Id.
     */
    @Test
    public void getSpatialReferenceSystem() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name", "org", 111, "def", "desc");

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem("org", 111);

            assertEquals("The GeoPackage did not return expected result for SpatialReferenceSystem in method getSpatialReferenceSystem.", gpkgSrs, testSrs);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage returns the null when SRS does not exist in GeoPackage.
     */
    @Test
    public void getSpatialReferenceSystem2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(555);

            assertNull("The GeoPackage did not return null for SpatialReferenceSystem that did not exist in method getSpatialReferenceSystem.", gpkgSrs);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Asks the GeoPackage to retrieve a Spatial Reference System
     * that doesn't exist in the GeoPackage and verifies the GeoPackage
     * returns a null value.
     */
    @Test
    public void getSpatialReferenceSystem3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem("org", 222);
            assertNull("The GeoPackage did not return a null value for spatial reference system as expected "
                               + "using the method getSpatialReferenceSystem(String, int) when searching for a spatial "
                               + "reference system that did not exist in the GeoPackage.", gpkgSrs);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests if the GeoPackage can return a spatial
     * reference system object that was created by the user
     *  and verify that it is the expected values when using the method
     * getSpatialReferenceSystem(String, int) also ensures
     * that the search for the srs is case insensitive
     */
    @Test
    public void getSpatialReferenceSystem4() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
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

            assertEquals("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
                                 + "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.", srsFound, srsAdded);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests if the GeoPackage can return a spatial
     * reference system object from the default values already in the GeoPackage
     * and verify it is the expected values when using the method
     * getSpatialReferenceSystem(String, int)
     */
    @Test
    public void getSpatialReferenceSystem5() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {

            final SpatialReferenceSystem srsAdded = gpkg.core().getSpatialReferenceSystem(0);

            final SpatialReferenceSystem srsFound = gpkg.core().getSpatialReferenceSystem("NONE",
                                                                                     0);

            assertEquals("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
                                 + "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.", srsFound, srsAdded);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }



    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for tableName");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent(null, "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for tableName");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName3() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("123", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that does not begin with letters or underscore");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackageCore throws an IllegalArgumentException
     * when adding content with a bad Table Name value for the
     * Content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadTableName4() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("gpkg_table", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that begins with gpkg");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }
    /**
     * Tests if GeoPackage Core would throw
     * an Illegal Argument Exception when passing
     * a null value for Bounding box
     */
    @Test(expected= IllegalArgumentException.class)
    public void addContentBadBoundingBox() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("tablename", "tiles", "identifier", "description", null, gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            fail("Expected GeoPackageCore to throw an IllegalArgumentException when giving a null balue for bounding box");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving an empty string for the table name parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadTableName() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().getContent("", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is an empty string");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving an null value for the table name parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadTableName2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().getContent(null, null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is null");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests GeoPackageCore if it throws an IllegalArgumentException
     * when giving a null value for the contentFactory parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadContentFactory() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().getContent("tables", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for contentFactory");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when given a bad data type when adding content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadDataType() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("thetable", "", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for data type");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when given a bad data type when adding content
     */
    @Test(expected = IllegalArgumentException.class)
    public void addContentBadDataType2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().addContent("thetable", null, "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for data type");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when giving a bad data type in getContent
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadDataType() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().getContent(null, null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing null for the dataType in getContent method");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackageCore throws an IllegalArgumentException
     * when giving a bad data type in getContent
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentBadDataType2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.core().getContent("", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing an empty string for the dataType in getContent method");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when given a null
     * parameter for getContent.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getContentNullParameter() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().getContent("tiles", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException when given a null parameter for ContentFactory in getContent");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * table name doesn't match.
     */
    @Test
    public void equivalentContent() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        final String tableName = "tableName";

        try(final GeoPackage gpkg = GeoPackageCoreAPITest.createGeoPackage(tableName, "columnName", testFile))
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
                                       boundingBox.getMinimumX(),
                                       boundingBox.getMinimumY(), boundingBox.getMaximumX(),
                                       boundingBox.getMaximumY(),
                                       spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * bounding boxes don't match.
     */
    @Test
    public void equivalentContent2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        final String tableName = "tableName";


        try(final GeoPackage gpkg = GeoPackageCoreAPITest.createGeoPackage(tableName, "columnName", testFile))
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
                                        2.0,
                                        4.0, 1.0,
                                        3.0,
                                        spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * description doesn't match.
     */
    @Test
    public void equivalentContent3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        final String tableName = "tableName";

        try(final GeoPackage gpkg = GeoPackageCoreAPITest.createGeoPackage(tableName, "columnName", testFile))
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
                                       boundingBox.getMinimumX(),
                                       boundingBox.getMinimumY(), boundingBox.getMaximumX(),
                                       boundingBox.getMaximumY(),
                                       spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * spatial reference systems don't match.
     */
    @Test
    public void equivalentContent4() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        final String tableName = "tableName";

        try(final GeoPackage gpkg = GeoPackageCoreAPITest.createGeoPackage(tableName, "columnName", testFile))
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
                                       boundingBox.getMinimumX(),
                                       boundingBox.getMinimumY(), boundingBox.getMaximumX(),
                                       boundingBox.getMaximumY(),
                                       gpkg.core()
                                           .getSpatialReferenceSystem(0)
                                           .getIdentifier()));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This Tests the Contents equal method and
     * ensures it returns false when the content's
     * description doesn't match.
     */
    @Test
    public void equivalentContent5() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        final String tableName = "tableName";

        try(final GeoPackage gpkg = GeoPackageCoreAPITest.createGeoPackage(tableName, "columnName", testFile))
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
                                       boundingBox.getMinimumX(),
                                       boundingBox.getMinimumY(), boundingBox.getMaximumX(),
                                       boundingBox.getMaximumY(),
                                       spatialReferenceSystem.getIdentifier()));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    private static GeoPackage createGeoPackage(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        try(final GeoPackage gpkg = new GeoPackage(testFile, GeoPackage.OpenMode.Create))
        {
            gpkg.close();
            GeoPackageCoreAPITest.createTable(tableName, columnName, testFile);

            return new GeoPackage(testFile, VerificationLevel.None, GeoPackage.OpenMode.Open);
        }
    }

    @SuppressWarnings("JDBCExecuteWithNonConstantString")
    private static void createTable(final String tableName, final String columnName, final File testFile) throws SQLException
    {
        final String createTable = String.format("CREATE TABLE %s ( %s TEXT," +
                                                             "other_column INTEGER NOT NULL," +
                                                             "more_columns INTEGER NOT NULL," +
                                                             "last_Column TEXT NOT NULL)",
                                                 tableName,
                                                 columnName);

        try(final Connection con = getConnection(testFile);
            final Statement stmt = con.createStatement())
        {
            stmt.execute(createTable);
        }
    }

    private static Connection getConnection(final File testFile) throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + testFile.getPath()); // Initialize the database connection
    }

    private static String sqliteVersionToString(final int randomInt)
    {
        // Major/minor/revision, https://www.sqlite.org/fileformat2.html
        final int major    = randomInt / 1000000;
        final int minor    = (randomInt - (major * 1000000)) / 1000;
        final int revision = randomInt - ((major * 1000000) + (minor * 1000));

        return String.format("%d.%d.%d", major, minor, revision);
    }

    /**
     * The SQLite version required for a GeoPackage shall contain SQLite 3
     * format
     */
    private static final int geoPackageSqliteMajorVersion = 3;

    /**
     * A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
     * application id field of the SQLite database header to indicate a
     * GeoPackage version 1.0 file. When converting 0x47503130 to an integer it
     * results in 1196437808
     */
    private static final int geoPackageApplicationId = 1196437808;


}
