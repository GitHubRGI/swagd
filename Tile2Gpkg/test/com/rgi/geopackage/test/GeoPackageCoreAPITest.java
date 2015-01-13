/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.geopackage.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class GeoPackageCoreAPITest
{

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();

    
    /**Tests if the GeoPackage class when accepting a file {using the method create(File file)}, the file is the same when using the getFile() method.
    *
    * @throws SQLException
    * @throws Exception
    */
   @Test
   public void createMethodSettingFile() throws SQLException, Exception
   {
       final File testFile = this.getRandomFile(3);

       try(GeoPackage gpkg = new GeoPackage(testFile))
       {
           assertTrue("The file given to the GeoPackage using the method create(File file) does not return the same file",
                      gpkg.getFile().equals(testFile));
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
       }
   }

   /**Tests if the GeoPackage.create(File file) will throw a FileAlreadyExists exception
    * when given a file that has already been created.
    * @throws FileAlreadyExistsException
    * @throws Exception
    */
   @Test(expected = FileAlreadyExistsException.class)
   public void createMethod2FileExistsExceptionThrown() throws FileAlreadyExistsException, Exception
   {
       final File testFile = this.getRandomFile(10);
       testFile.createNewFile();

       try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
       {
           fail("This test should throw a FileAlreadyExistsException when trying to create a file that already exists.");
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
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
       final File testFile = this.getRandomFile(5);

       try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Open))
       {
           fail("The GeoPackage should throw a FileNotFoundException when trying to open a GeoPackage with a file that hasn't been created.");
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
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
       try(GeoPackage gpkg = new GeoPackage(null))
       {
           fail("The GeoPackage should throw an IllegalArgumentException for trying to create a file that is null.");
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
   @Test
   public void verifyApplicationId() throws Exception
   {
       final File testFile = this.getRandomFile(12);

       try(GeoPackage gpkg = new GeoPackage(testFile))
       {
           assertTrue(String.format("The GeoPackage Application Id is incorrect. Application Id Expected (in int):  %d  Application Id recieved: %d",
                                           geoPackageApplicationId, gpkg.getApplicationId()), gpkg.getApplicationId() == geoPackageApplicationId);
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
       }
   }

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
       final File testFile = this.getRandomFile(12);

       try(GeoPackage gpkg = new GeoPackage(testFile, true))
       {
           // get the first number in the sqlite version and make sure it is a
           // version 3
           String sqliteVersion = gpkg.getSqliteVersion();
                  sqliteVersion = sqliteVersion.substring(0, sqliteVersion.indexOf('.'));

           assertTrue(String.format("The GeoPackage Sqlite Version is incorrect."
                                  + " Sqlite Version Id Expected:  %s"
                                  + " SqliteVersion recieved: %s", geopackageSqliteVersion, sqliteVersion),
                                           geopackageSqliteVersion.equals(sqliteVersion));
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
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
       final File testFile      = this.getRandomFile(6);
       final int  randomInteger = this.randomGenerator.nextInt();

       try(GeoPackage gpkg = new GeoPackage(testFile))
       {
           try(RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rw"))
           {
               // https://www.sqlite.org/fileformat2.html
               // Bytes 96 -> 100 are an int representing the sqlite version
               // used random integer
               randomAccessFile.seek(96);
               randomAccessFile.writeInt(randomInteger);
               randomAccessFile.close();

               try(GeoPackage gpkg2 = new GeoPackage(testFile, OpenMode.Open))
               {
                   gpkg2.getSqliteVersion();
                   final String sqliteVersionChanged = gpkg2.getSqliteVersion();

                   assertTrue(String.format("The method getSqliteVersion() did not detect the same Version expected. Expected: %s   Actual: %s", this.sqliteVersionToString(randomInteger), sqliteVersionChanged),
                                             sqliteVersionChanged.equals(this.sqliteVersionToString(randomInteger)));
               }
           }
       }
       finally
       {
           if(testFile.exists())
           {
               if(!testFile.delete())
               {
                   throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
               }
           }
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
        final File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            //Content information
            final String                  tableName   = "tableName";
            final String                  identifier  = "identifier";
            final String                  description = "description";
            final BoundingBox             boundingBox = new BoundingBox(0.0,0.0,0.0,0.0);
            final SpatialReferenceSystem  srs         = gpkg.core().getSpatialReferenceSystem(4326);

            //add the content to gpkg
            final TileSet tileSet  = gpkg.tiles().addTileSet(tableName, identifier, description, boundingBox, srs);
            final String  dataType = tileSet.getDataType();

            gpkg.close();//close the geopackage

            //create GeoPackage Core object
            final GeoPackageCore gpkgCore = new GeoPackageCore(con);
             //try to add the same content twice
            final Content content = gpkgCore.addContent(tableName, dataType, identifier, description, boundingBox, srs);

            assertTrue(content.getBoundingBox()           .equals(tileSet.getBoundingBox()) &&
                       content.getDataType()              .equals(tileSet.getDataType())    &&
                       content.getDescription()           .equals(tileSet.getDescription()) &&
                       content.getIdentifier()            .equals(tileSet.getIdentifier())  &&
                       content.getLastChange()            .equals(tileSet.getLastChange())  &&
                       content.getSpatialReferenceSystem().equals(tileSet.getSpatialReferenceSystem()));

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            //Content information
            final String                  tableName   = "tableName";
            final SpatialReferenceSystem  srs         = gpkg.core().getSpatialReferenceSystem(4326);

            gpkg.close();//close the geopackage

            //create GeoPackage Core object
            final GeoPackageCore gpkgCore = new GeoPackageCore(con);
             //try to add the same content twice
            gpkgCore.addContent(tableName, "dataType",  "identifier",  "description", new BoundingBox(null,null,null,null), srs);
            gpkgCore.addContent(tableName, "dataType2", "identifier2", "description", new BoundingBox(null,null,null,null), srs);

            fail("Expected GeoPackage Core to throw an IllegalArgumentException when trying to add content with the same tablename but differing other fields");

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadName() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem(null, 123, "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for name.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadName2() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("", 123, "organization", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for name.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadOrganization() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", 123, null, 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with a null parameter for organization.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadOrganization2() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", 123, "", 123, "definition", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for organization.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadDefinition() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", 123, "organization", 123, null, "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create a SRS with a null parameter for definition.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemBadDefinition2() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("srsName", 123, "organization", 123, "", "description");
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with an empty string for definition.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    /**
     * Tests if a GeoPackage throws an IllegalArgumentException when
     * creating a SRS with the same identifier but differing other
     * fields.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test(expected = IllegalArgumentException.class)
    public void createSpatialReferenceSystemNotUnique() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name           = "srsName";
            final int    identifier     = 123;
            final String organization   = "organization";
            final int    organizationId = 123;
            final int    diffOrgId      = 124;
            final String description    = "description";
            final String definition     = "definition";

            gpkg.core().addSpatialReferenceSystem(name, identifier, organization, organizationId, definition, description);
            gpkg.core().addSpatialReferenceSystem(name, identifier, organization, diffOrgId,      definition, description);
            fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to create an SRS with same identifier but differing other fields.");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void createSpatialReferenceSystemSameSRS() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name           = "srsName";
            final int    identifier     = 123;
            final String organization   = "organization";
            final int    organizationId = 123;
            final String description    = "description";
            final String definition     = "definition";

            final SpatialReferenceSystem firstSRS  = gpkg.core().addSpatialReferenceSystem(name, identifier, organization, organizationId, definition, description);
            final SpatialReferenceSystem secondSRS = gpkg.core().addSpatialReferenceSystem(name, identifier, organization, organizationId, definition, description);

            assertTrue("GeoPackage did not return the same SRS objects as expected.", firstSRS.equals(secondSRS));
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name",
                                                                                         123,
                                                                                         "org",
                                                                                         123,
                                                                                         "definition",
                                                                                         null);

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(123);

            assertTrue("The GeoPackage get Spatial Reference System does not give back the value expeced.",
                       testSrs.equals(gpkgSrs));
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {

            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                           9804,
                                           "org",
                                           9804,
                                           "definition",
                                           "description");
            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                           9804,
                                           "org",
                                           9804,
                                           "different definition",
                                           "description");

            fail("The GeoPackage should throw a RuntimeException when adding Spatial Reference System with the same SRS id but have different definition fields.");
        }
        finally
        {

            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {

          gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                         9804,
                                         "org",
                                         9804,
                                         "definition",
                                         "description");

          gpkg.core().addSpatialReferenceSystem("scaled different mercator",
                                         9804,
                                         "org",
                                         9804,
                                         "definition",
                                         "description");


            fail("The GeoPackage should throw a IllegalArgumentException when adding a Spatial Reference System with the same SRS id but have different names.");
        }
        finally
        {

            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                  9804,
                                                  "org",
                                                  9804,
                                                  "definition",
                                                  "description");


            gpkg.core().addSpatialReferenceSystem("different name",
                                                  9804,
                                                  "org",
                                                  9804,
                                                  "definition",
                                                  "description");

            fail("The GeoPackage should throw a IllegalArgumentException when adding a Spatial Reference System "
                    + "with the same and SRS identifier but diffent names");

        }
        finally
        {

            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    /**
     * Gives a GeoPackage two SRS's such that they are the same identifier but other fields differ.
     * In this case the organizations's are different.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExistingSrsWithSameSrsIdAndDifferingOtherFields4() throws RuntimeException, SQLException, Exception
    {
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
              gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                    9804,
                                                    "org",
                                                    9804,
                                                    "definition",
                                                    "description");


               gpkg.core().addSpatialReferenceSystem("scaled world mercator",
                                                     9804,
                                                     "different organization",
                                                     9804,
                                                     "definition",
                                                     "description");

            fail("The GeoPackage should throw a IllegalArgumentException when adding a Spatial Reference System "
                    + "with the same and SRS identifier but diffent organization");

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void addSRSVerify() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name         = "scaled world mercator";
            final int    identifier   = 9804;
            final String organization = "org";
            final int    orgId        = 9;
            final String definition   = "definition";
            final String description  = "description";

            final SpatialReferenceSystem srs = gpkg.core().addSpatialReferenceSystem(name,
                                                                        identifier,
                                                                        organization,
                                                                        orgId,
                                                                        definition,
                                                                        description);

              assertTrue("GeoPackage did not return expected values for the SRS given",
                                 srs.getName()             .equals(name)         &&
                                 srs.getIdentifier()        == identifier        &&
                                 srs.getOrganization()     .equals(organization) &&
                                 srs.getOrganizationSrsId() ==  orgId            &&
                                 srs.getDefinition()       .equals(definition)   &&
                                 srs.getDescription()      .equals(description));

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void addExistingSRSTwiceVerifyHashCodeAndEquals() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final String name         = "scaled world mercator";
            final int    identifier   = 9804;
            final String organization = "org";
            final int    orgId        = 9;
            final String definition   = "definition";
            final String description  = "description";

            final SpatialReferenceSystem srs1 = gpkg.core().addSpatialReferenceSystem(name, identifier, organization, orgId, definition, description);
            final SpatialReferenceSystem srs2 = gpkg.core().addSpatialReferenceSystem(name, identifier, organization, orgId, definition, description);

            assertTrue("The GeoPackage returned false when it should have returned true for two equal SRS objects. ",srs1.equals(srs2));
            assertTrue("The HashCode for the same srs differed.",srs1.hashCode() == srs2.hashCode());

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    /**
     * Tests the GeoPackage SRS equals and hashCode methods
     * return false when two SRS's are different.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws FileNotFoundException
     */
    @Test
    public void addSRSAndCompareEqualsAndHashCodeTwoDifferentSRS() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
          final SpatialReferenceSystem srs1 = gpkg.core().addSpatialReferenceSystem("name",   123, "organization",   123,  "definition", "description");
          final SpatialReferenceSystem srs2 = gpkg.core().addSpatialReferenceSystem("name",   122, "organization",   123,  "definition", "description");

          assertTrue("GeoPackage returned true when it should have returned false when two different SRS compared with the equals method",!srs1.equals(srs2));
          assertTrue("GeoPackage returned the same HashCode for two different SRS's",srs1.hashCode() !=  srs2.hashCode());

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    @Test
    public void addSRSAndCompareEqualsTwoDifferentSRS() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
          final SpatialReferenceSystem srs1 = gpkg.core().addSpatialReferenceSystem("name",  123, "organization", 123, "definition", "description");
          final SpatialReferenceSystem srs2 = gpkg.core().addSpatialReferenceSystem("name2", 122, "organization", 123, "definition", "description");

          assertTrue("GeoPackage returned true when it should have returned false when two different SRS compared with the equals method",!srs1.equals(srs2));
          assertTrue("GeoPackage returned the same HashCode for two different SRS's",srs1.hashCode() !=  srs2.hashCode());

        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem testSrs = gpkg.core().addSpatialReferenceSystem("name",555,"org",111, "def","desc");

            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(555);

            assertTrue("The GeoPackage did not return expected result for SpatialReferenceSystem in method getSpatialReferenceSystem.",
                         gpkgSrs.equals(testSrs));
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(5);

        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem(555);

            assertTrue("The GeoPackage did not return null for SpatialReferenceSystem that did not exist in method getSpatialReferenceSystem.",
                        gpkgSrs == null);
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void getSpatialReferenceSystem3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
    	final File testFile = this.getRandomFile(7);
    	
    	try(GeoPackage gpkg = new GeoPackage(testFile))
    	{
    		final SpatialReferenceSystem gpkgSrs = gpkg.core().getSpatialReferenceSystem("org", 222);
    		assertTrue("The GeoPackage did not return a null value for spatial reference system as expected "
    				   	+ "using the method getSpatialReferenceSystem(String, int) when searching for a spatial "
    				   	+ "reference system that did not exist in the GeoPackage.",
    				   gpkgSrs == null);
    	}
    	finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void getSpatialReferenceSystem4() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
    	final File testFile = this.getRandomFile(9);
    	
    	try(GeoPackage gpkg = new GeoPackage(testFile))
    	{
    		String name              = "name";
    		int    identifier        = 222;
    		String organization      = "organization";
    		int    organizationSrsId = 333;
    		String definition        = "definition";
    		String description       = "description";
    		
    		SpatialReferenceSystem srsAdded = gpkg.core().addSpatialReferenceSystem(name, 
    																				identifier, 
    																				organization,
    																				organizationSrsId, 
    																				definition, 
    																				description);
    		
    		SpatialReferenceSystem srsFound = gpkg.core().getSpatialReferenceSystem("oRgaNiZaTiOn", 
    																				organizationSrsId);
    		
    		assertTrue("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
    					+ "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.",
    				   srsFound.equals(srsAdded));
    	}
    	finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void getSpatialReferenceSystem5() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
    	final File testFile = this.getRandomFile(9);
    	
    	try(GeoPackage gpkg = new GeoPackage(testFile))
    	{
    		
    		SpatialReferenceSystem srsAdded = gpkg.core().getSpatialReferenceSystem(0);
    		
    		SpatialReferenceSystem srsFound = gpkg.core().getSpatialReferenceSystem("NONE", 
    																				 0);
    		
    		assertTrue("The GeoPackage did not return the expected values for the Spatial Reference System Object when "
    					+ "asking to retrieve the SRS object through the getSpatialReferenceSystem(String, int) method.",
    				   srsFound.equals(srsAdded));
    	}
    	finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent("", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for tableName");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent(null, "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for tableName");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent("123", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that does not begin with letters or underscore");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent("gpkg_table", "dataType", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that begins with gpkg");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.getContent("", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is an empty string");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.getContent(null, null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a table name that is null");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.getContent("tables", null);
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for contentFactory");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent("thetable", "", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method an empty string for data type");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
            Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();

            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.addContent("thetable", null, "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException for passing the method a null value for data type");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(12);
        try (GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
             Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();
            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.getContent(null, null, gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing null for the dataType in getContent method");
        }
        finally
        {
            if (testFile.exists())
            {
                if (!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
        final File testFile = this.getRandomFile(12);
        try (GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);
             Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            gpkg.close();
            final GeoPackageCore gpkgCore = new GeoPackageCore(con);

            gpkgCore.getContent("", null, gpkgCore.getSpatialReferenceSystem(-1));
            fail("Expected GeoPackage Core to throw an IllegalArgumentException when passing an empty string for the dataType in getContent method");
        }
        finally
        {
            if (testFile.exists())
            {
                if (!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
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
    public void getContentNullParameter() throws FileAlreadyExistsException, ClassNotFoundException, SQLException, ConformanceException, FileNotFoundException
    {
        final File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
            gpkg.core().getContent("tiles", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected the GeoPackage to throw an IllegalArgumentException when given a null parameter for ContentFactory in getContent");
        }
        finally
        {
            if(testFile.exists())
            {
                if(!testFile.delete())
                {
                    throw new RuntimeException(String.format("Unable to delete testFile. testFile: %s", testFile));
                }
            }
        }
    }

    private Connection getConnection(final String filePath) throws Exception
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }
    private String getRanString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(this.getRanString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }
    
    private String sqliteVersionToString(final int randomInt)
    {
        // Major/minor/revision, https://www.sqlite.org/fileformat2.html
        final int major = randomInt / 1000000;
        final int minor = (randomInt - (major * 1000000)) / 1000;
        final int revision = randomInt - ((major * 1000000) + (minor * 1000));

        return String.format("%d.%d.%d", major, minor, revision);
    }
    /**
     * The Sqlite version required for a GeoPackage shall contain SQLite 3
     * format
     */
    private final static String geopackageSqliteVersion = "3";
    /**
     * A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
     * application id field of the SQLite database header to indicate a
     * GeoPackage version 1.0 file. When converting 0x47503130 to an integer it
     * results in 1196437808
     */
    private final static int geoPackageApplicationId = 1196437808;


}
