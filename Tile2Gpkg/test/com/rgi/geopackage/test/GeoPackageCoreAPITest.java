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
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
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

}
