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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import utility.TestUtility;
import android.graphics.Bitmap;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.coordinate.CoordinateReferenceSystem;
import com.rgi.android.common.coordinate.CrsCoordinate;
import com.rgi.android.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.android.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.android.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.android.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.android.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.android.common.tile.scheme.TileMatrixDimensions;
import com.rgi.android.common.tile.scheme.TileScheme;
import com.rgi.android.common.tile.scheme.ZoomTimesTwo;
import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.GeoPackage.OpenMode;
import com.rgi.android.geopackage.core.SpatialReferenceSystem;
import com.rgi.android.geopackage.tiles.Tile;
import com.rgi.android.geopackage.tiles.TileMatrix;
import com.rgi.android.geopackage.tiles.TileMatrixSet;
import com.rgi.android.geopackage.tiles.TileSet;
import com.rgi.android.geopackage.verification.ConformanceException;

/**
 * @author Jenifer Cochran
 */
@SuppressWarnings("static-method")
public class GeoPackageTilesAPITest
{
    /**
     * This tests if a GeoPackage can add a tile set successfully without throwing errors.
     *
     * @throws SQLException throws when an SQLException occurs
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addTileSet() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final TileSet tileSet = gpkg.tiles()
                                       .addTileSet("pyramid",
                                                   "title",
                                                   "tiles",
                                                   new BoundingBox(0.0, 0.0, 50.0, 60.0),
                                                   gpkg.core().getSpatialReferenceSystem("EPSG", 4326));


           final int matrixHeight = 2;
           final int matrixWidth = 4;
           final int tileHeight = 512;
           final int tileWidth = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       0,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);
        }
        finally
        {
            gpkg.close();
        }

        final String query = "SELECT table_name FROM gpkg_tile_matrix_set WHERE table_name = 'pyramid';";

        final Connection con       = TestUtility.getConnection(testFile);

        try
        {
            final Statement  stmt      = con.createStatement();

            try
            {
                final ResultSet  tileName  = stmt.executeQuery(query);

                try
                {
                    Assert.assertTrue("The GeoPackage did not set the table_name into the gpkg_tile_matrix_set when adding a new set of tiles.", tileName.next());
                    final String tableName = tileName.getString("table_name");
                    Assert.assertTrue("The GeoPackage did not insert the correct table name into the gpkg_tile_matrix_set when adding a new set of tiles.", tableName.equals("pyramid"));
                }
                finally
                {
                    tileName.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when given a null value for tileSetEntry
     *
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetWithNullTileSetEntry() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            gpkg.tiles().addTile(null, gpkg.tiles().getTileMatrix(tileSet, 0), 0, 0, GeoPackageTilesAPITest.createImageBytes());
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a null value for tileSetEntry.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when given
     * a tilesetentry with a null value for the boundingbox
     *
     * @throws Exception
     *             throws if an exception occurs
     * */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetWithNullBoundingBox() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles()
                .addTileSet("tableName",
                            "ident",
                            "desc",
                            null,
                            gpkg.core().getSpatialReferenceSystem(4236));


            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a null value for BoundingBox.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

   /**
     * Tests if the GeoPackage will throw an IllegalArgumentException
     * If it gives tries to create a TileSet with a null SRS value
     * @throws Exception throws when exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetWithNullSRS() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles()
                .addTileSet("name",
                            "ident",
                            "desc",
                            new BoundingBox(0.0,0.0,0.0,0.0),
                            null);

            Assert.fail("GeoPackage should have thrown an IllegalArgumentException when TileEntrySet is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Test if the GeoPackage will add a Tile set with a new Spatial Reference System (one created by user).
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addTileSetWithNewSpatialReferenceSystem() throws Exception
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
        }
        finally
        {
            gpkg.close();
        }

        final String query = "SELECT srs_name FROM gpkg_spatial_ref_sys "+
                             "WHERE srs_name     = 'scaled world mercator' AND "+
                                   "organization = 'org'                   AND "+
                                   "definition   = 'definition'            AND "+
                                   "description  = 'description';";

        final Connection con     = TestUtility.getConnection(testFile);

        try
        {
            final Statement  stmt    = con.createStatement();

            try
            {
                final ResultSet  srsInfo = stmt.executeQuery(query);

                try
                {
                    Assert.assertTrue("The Spatial Reference System added to the GeoPackage by the user did not contain the same information given.", srsInfo.next());
                }
                finally
                {
                    srsInfo.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
     }


    /**
     * Tests if given a GeoPackage with tiles already inside it can add another Tile Set without throwing an error and verify that it entered the correct information.
     *
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addTileSetToExistingGpkgWithTilesInside() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        //create a geopackage with tiles inside
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetONE",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 60.0, 60.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;


            gpkg.tiles().addTileMatrix(tileSet,
                                       0,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            //open a file with tiles inside and add more tiles
            final GeoPackage gpkgWithTiles = new GeoPackage(testFile, OpenMode.Open);

            try
            {
                final TileSet tileSetEntry2 = gpkgWithTiles.tiles()
                                                           .addTileSet("newTileSetTWO",
                                                                       "title2",
                                                                       "tiles",
                                                                       new BoundingBox(0.0, 0.0, 70.0, 50.0),
                                                                       gpkgWithTiles.core().getSpatialReferenceSystem("EPSG", 4326));

                final double pixelXSize =  (tileSetEntry2.getBoundingBox().getWidth()/matrixWidth)/tileWidth;
                final double pixelYSize =  (tileSetEntry2.getBoundingBox().getHeight()/matrixHeight)/tileHeight;

                gpkgWithTiles.tiles().addTileMatrix(tileSetEntry2,
                                                    0,
                                                    matrixWidth,
                                                    matrixHeight,
                                                    tileWidth,
                                                    tileHeight,
                                                    pixelXSize,
                                                    pixelYSize);
            }
            finally
            {
                gpkgWithTiles.close();
            }
        }
        finally
        {
            gpkg.close();
        }
        //make sure the information was added to contents table and tile matrix set table
        final String query = "SELECT cnts.table_name FROM gpkg_contents        AS cnts WHERE cnts.table_name"+
                             " IN(SELECT tms.table_name  FROM gpkg_tile_matrix_set AS tms  WHERE cnts.table_name = tms.table_name);";

        final Connection con            = TestUtility.getConnection(testFile);

        try
        {
            final Statement  stmt           = con.createStatement();

            try
            {
                final ResultSet  tileTableNames = stmt.executeQuery(query);

                try
                {
                    if(!tileTableNames.next())
                    {
                        Assert.fail("The two tiles tables where not successfully added to both the gpkg_contents table and the gpkg_tile_matrix_set.");
                    }
                    while (tileTableNames.next())
                    {
                        final String tilesTableName = tileTableNames.getString("table_name");
                        Assert.assertTrue("The tiles table names did not match what was being added to the GeoPackage",
                                    tilesTableName.equals("newTileSetTWO") || tilesTableName.equals("tileSetONE"));
                    }
                }
                finally
                {
                    tileTableNames.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }



    /**
     * Tests if a GeoPackage will throw an error when adding a tileset with the same name as another tileset in the GeoPackage.
     *
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetWithRepeatedTileSetName() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("repeated_name",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            gpkg.tiles().addTileMatrix(tileSet, 0, 2, 2, 2, 2, 2, 2);

            final TileSet tileSetEntry2 = gpkg.tiles()
                                              .addTileSet("repeated_name",
                                                          "title2",
                                                          "tiles",
                                                          new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                          gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            gpkg.tiles().addTileMatrix(tileSetEntry2, 0, 2, 2, 2, 2, 2, 2);

            Assert.fail("The GeoPackage should throw an IllegalArgumentException when a user gives a Tile Set Name that already exists.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage can add 2 Tile Matrix entries with
     * the two different tile pyramids can be entered into one gpkg
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addTileSetToExistingTilesTable() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "tiles",
                                                    "desc",
                                                    new BoundingBox(0.0, 0.0, 70.0, 70.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));


            final ArrayList<TileSet> tileSetContnentEntries = new ArrayList<TileSet>();

            tileSetContnentEntries.add(tileSet);
            tileSetContnentEntries.add(tileSet);

            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       0,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);
            final int matrixHeight2 = 4;
            final int matrixWidth2 = 4;
            final int tileHeight2 = 256;
            final int tileWidth2 = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       1,
                                       matrixWidth2,
                                       matrixHeight2,
                                       tileWidth2,
                                       tileHeight2,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth2)/tileWidth2,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight2)/tileHeight2);

            for(final TileSet gpkgEntry : gpkg.tiles().getTileSets())
            {
                Assert.assertTrue("The tile entry's information in the GeoPackage does not match what was originally given to a GeoPackage",
                                  FunctionalUtility.anyMatch(tileSetContnentEntries,
                                                             new Predicate<TileSet>(){
                                                                                         @Override
                                                                                        public boolean apply(final TileSet tileEntry)
                                                                                         {
                                                                                             return  tileEntry.getBoundingBox().equals(gpkgEntry.getBoundingBox())  &&
                                                                                                     tileEntry.getDataType()   .equals(gpkgEntry.getDataType())    &&
                                                                                                     tileEntry.getDescription().equals(gpkgEntry.getDescription()) &&
                                                                                                     tileEntry.getIdentifier() .equals(gpkgEntry.getIdentifier())  &&
                                                                                                     tileEntry.getTableName()  .equals(gpkgEntry.getTableName())   &&
                                                                                                     tileEntry.getSpatialReferenceSystemIdentifier().equals(gpkgEntry.getSpatialReferenceSystemIdentifier());
                                                                                         }
                                                                                       }));
            }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This ensures that when a user tries to add the same tileSet two times
     * that the TileSet object that is returned is the one that already exists
     * in the GeoPackage and verifies its contents
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile
     */
    @Test
    public void addSameTileSetTwice() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final String      tableName   = "tableName";
            final String      identifier  = "identifier";
            final String      description = "description";
            final BoundingBox boundingBox = new BoundingBox(2.0,1.0,4.0,3.0);

            final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(0);

            final TileSet tileSet = gpkg.tiles().addTileSet(tableName,
                                                      identifier,
                                                      description,
                                                      boundingBox,
                                                      srs);

            final TileSet sameTileSet = gpkg.tiles().addTileSet(tableName,
                                                          identifier,
                                                          description,
                                                          boundingBox,
                                                          srs);

            Assert.assertTrue("The GeoPackage did not return the same tile set when trying to add the same tile set twice.",
                              sameTileSet.equals(tileSet.getTableName(),
                                                    tileSet.getDataType(),
                                                    tileSet.getIdentifier(),
                                                    tileSet.getDescription(),
                                                    tileSet.getBoundingBox(),
                                                    tileSet.getSpatialReferenceSystemIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Expects GeoPackage to throw an IllegalArgumentException when giving
     * addTileSet a parameter with a null value for bounding box
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetBadTableName() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles()
                .addTileSet("TableName",
                            "identifier",
                            "definition",
                            null,
                            gpkg.core().getSpatialReferenceSystem(-1));

            Assert.fail("Expected an IllegalArgumentException when giving a null value for bounding box for addTileSet");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Expects GeoPackage to throw an IllegalArgumentException when giving
     * addTileSet a parameter with a null value for bounding box
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetBadSRS() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles()
                .addTileSet("TableName",
                            "identifier",
                            "definition",
                            new BoundingBox(0.0,0.0,0.0,0.0),
                            null);
            Assert.fail("Expected an IllegalArgumentException when giving a null value for bounding box for addTileSet");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Expects GeoPackage to throw an IllegalArgumentException when giving
     * addTileSet a parameter with a null value for bounding box
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileSetBadBoundingBox() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles()
                .addTileSet("TableName",
                            "identifier",
                            "definition",
                            new BoundingBox(0.0,0.0,0.0,0.0),
                            null);
            Assert.fail("Expected an IllegalArgumentException when giving a null value for bounding box for addTileSet");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when The table name is an empty string
     * for TileSet.
     * @throws Exception throws if an exception occurs
     */
     @Test(expected = IllegalArgumentException.class)
     public void addTileSetContentEntryInvalidTableName() throws Exception
     {
         final File testFile = TestUtility.getRandomFile(5);
         final GeoPackage gpkg = new GeoPackage(testFile);

         try
         {
             gpkg.tiles()
                 .addTileSet("",
                             "ident",
                             "desc",
                             new BoundingBox(0.0,0.0,0.0,0.0),
                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

             Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when given a TileSet with an empty string for the table name.");
         }
         finally
         {
             gpkg.close();
             TestUtility.deleteFile(testFile);
         }
     }

    /**
     * Tests if GeoPackageTiles throws an IllegalArgumentException when giving a
     * table name with symbols
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile
     */
     @Test(expected = IllegalArgumentException.class)
     public void addTileIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException
     {
         final File testFile = TestUtility.getRandomFile(18);
         final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

         try
         {
             gpkg.tiles().addTileSet("badTableName^", "identifier", "description", new BoundingBox(0.0,0.0,2.0,2.0), gpkg.core().getSpatialReferenceSystem(0));
             fail("Expected to get an IllegalArgumentException for giving an illegal tablename (with symbols not allowed by GeoPackage)");
         }
         finally
         {
             gpkg.close();
             TestUtility.deleteFile(testFile);
         }
     }

    /**
     * Tests if GeoPackageTiles throws an IllegalArgumentException when giving a
     * table name starting with gpkg
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
     @Test(expected = IllegalArgumentException.class)
     public void addTileIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
     {
         final File testFile = TestUtility.getRandomFile(18);
         final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

         try
         {
             gpkg.tiles().addTileSet("gpkg_bad_tablename", "identifier", "description", new BoundingBox(0.0,0.0,2.0,2.0), gpkg.core().getSpatialReferenceSystem(0));
             fail("Expected to get an IllegalArgumentException for giving an illegal tablename (starting with gpkg_ which is not allowed by GeoPackage)");
         }
         finally
         {
             gpkg.close();
             TestUtility.deleteFile(testFile);
         }
     }

    /**
     * Tests if GeoPackageTiles throws an IllegalArgumentException when giving a
     * table name with a null value
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
     @Test(expected = IllegalArgumentException.class)
     public void addTileIllegalArgumentException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
     {
         final File testFile = TestUtility.getRandomFile(18);
         final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

         try
         {
             gpkg.tiles().addTileSet(null, "identifier", "description", new BoundingBox(0.0,0.0,2.0,2.0), gpkg.core().getSpatialReferenceSystem(0));
             fail("Expected to get an IllegalArgumentException for giving an illegal tablename (a null value)");
         }
         finally
         {
             gpkg.close();
             TestUtility.deleteFile(testFile);
         }
     }


    /**
     * Tests if a GeoPackage will return the same tileSets that was given to the GeoPackage when adding tileSets.
     * @throws Exception if an exception occurs
     */
    @Test
    public void getTileSetsFromGpkg() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "tiles",
                                                    "desc",
                                                    new BoundingBox(0.0, 0.0, 90.0, 50.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileSet tileSet2 = gpkg.tiles()
                                         .addTileSet("SecondTileSet",
                                                     "ident",
                                                     "descrip",
                                                     new BoundingBox(1.0,1.0,122.0,111.0),
                                                     gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final ArrayList<TileSet> tileSetContnentEntries = new ArrayList<TileSet>();

            tileSetContnentEntries.add(tileSet);
            tileSetContnentEntries.add(tileSet2);

            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;


            gpkg.tiles().addTileMatrix(tileSet,
                                       0,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);


            final int matrixHeight2 = 4;
            final int matrixWidth2 = 4;

            gpkg.tiles().addTileMatrix(tileSet2,
                                       1,
                                       matrixWidth2,
                                       matrixHeight2,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet2.getBoundingBox().getWidth()/matrixWidth2)/tileWidth,
                                       (tileSet2.getBoundingBox().getHeight()/matrixHeight2)/tileHeight);

            final Collection<TileSet> tileSetsFromGpkg = gpkg.tiles().getTileSets();

            Assert.assertTrue("The number of tileSets added to a GeoPackage do not match with how many is retrieved from a GeoPacakage.",tileSetContnentEntries.size() == tileSetsFromGpkg.size());

            for(final TileSet gpkgEntry : tileSetsFromGpkg)
            {
                Assert.assertTrue("The tile entry's information in the GeoPackage does not match what was originally given to a GeoPackage",
                                  FunctionalUtility.anyMatch(tileSetContnentEntries,
                                                             new Predicate<TileSet>(){
                                                                                          @Override
                                                                                        public boolean apply(final TileSet tileEntry)
                                                                                          {
                                                                                              return tileEntry.getBoundingBox().equals(gpkgEntry.getBoundingBox()) &&
                                                                                                     tileEntry.getDataType()   .equals(gpkgEntry.getDataType())    &&
                                                                                                     tileEntry.getDescription().equals(gpkgEntry.getDescription()) &&
                                                                                                     tileEntry.getIdentifier() .equals(gpkgEntry.getIdentifier())  &&
                                                                                                     tileEntry.getTableName()  .equals(gpkgEntry.getTableName())   &&
                                                                                                     tileEntry.getSpatialReferenceSystemIdentifier().equals(gpkgEntry.getSpatialReferenceSystemIdentifier());
                                                                                          }
                                                                                      }));
            }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will find no tile Sets when searching with an SRS that is not in the GeoPackage.
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getTileSetWithNewSRS() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
          final Collection<TileSet> gpkgTileSets = gpkg.tiles().getTileSets(gpkg.core().addSpatialReferenceSystem("name", "org", 123, "def", "desc"));
          Assert.assertTrue("Should not have found any tile sets because there weren't any in "
                   + "GeoPackage that matched the SpatialReferenceSystem given.", gpkgTileSets.size() == 0);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the getTileSet returns null when the tile table does not exist
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileSetVerifyReturnNull()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().getTileSet("table_not_here");

            Assert.assertTrue("GeoPackage expected to return null when the tile set does not exist in GeoPackage",tileSet == null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the getTileSet returns the expected values.
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileSetVerifyReturnCorrectTileSet()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet         = gpkg.tiles().addTileSet("ttable","identifier", "Desc", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final TileSet returnedTileSet = gpkg.tiles().getTileSet("ttable");

            Assert.assertTrue("GeoPackage did not return the same values given to tile set",
                       tileSet.getBoundingBox().equals(returnedTileSet.getBoundingBox()) &&
                       tileSet.getDescription().equals(returnedTileSet.getDescription()) &&
                       tileSet.getDataType()   .equals(returnedTileSet.getDataType())    &&
                       tileSet.getIdentifier() .equals(returnedTileSet.getIdentifier())  &&
                       tileSet.getLastChange() .equals(returnedTileSet.getLastChange())  &&
                       tileSet.getTableName()  .equals(returnedTileSet.getTableName())   &&
                       tileSet.getSpatialReferenceSystemIdentifier().equals(returnedTileSet.getSpatialReferenceSystemIdentifier()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * Tests if the GeoPackage can detect there are zoom levels for
     * a tile that is not represented in the Tile Matrix Table.
     * Should throw a IllegalArgumentException.
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTilesIllegalArgumentException() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                     new BoundingBox(0.0,0.0,0.0,0.0),
                                                     gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 18, 20, 20, 2,2, 1, 1);
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, new byte[] {1, 2, 3, 4});

            Assert.fail("GeoPackage should throw a IllegalArgumentExceptionException when Tile Matrix Table "
               + "does not contain a record for the zoom level of a tile in the Pyramid User Data Table.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can detect the tile_row
     * is larger than the matrix_height -1. Which is a violation
     * of the GeoPackage Specifications. Requirement 55.
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTilesIllegalArgumentException2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                    new BoundingBox(0.0,0.0,0.0,0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 0, 2, 2, 2, 2, 1, 1);
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 10, new byte[] {1, 2, 3, 4});

            Assert.fail("GeoPackage should throw a IllegalArgumentException when tile_row "
               + "is larger than matrix_height - 1 when zoom levels are equal.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can detect the tile_row
     * is less than 0. Which is a violation
     * of the GeoPackage Specifications. Requirement 55.
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTilesIllegalArgumentException3() throws Exception
    {
        final File      testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                    new BoundingBox(0.0,0.0,0.0,0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 0, 2, 2, 2, 2, 1, 1);
            gpkg.tiles().addTile(tileSet, tileMatrix, 0, -1, new byte[] {1, 2, 3, 4});

            Assert.fail("GeoPackage should throw a IllegalArgumentException when tile_row "
               + "is less than 0.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can detect the tile_column
     * is larger than matrix_width -1. Which is a violation
     * of the GeoPackage Specifications. Requirement 54.
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTilesIllegalArgumentException4() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                    new BoundingBox(0.0,0.0,0.0,0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 0, 2, 2, 2, 2, 1, 1);
            gpkg.tiles().addTile(tileSet,tileMatrix, 10, 0, new byte[] {1, 2, 3, 4});

            Assert.fail("GeoPackage should throw a IllegalArgumentException when tile_column "
               + "is larger than matrix_width -1.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can detect the tile_column
     * is less than 0. Which is a violation
     * of the GeoPackage Specifications. Requirement 54.
     * @throws Exception throws when exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTilesIllegalArgumentException5() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                    new BoundingBox(0.0,0.0,0.0,0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet, 0, 2, 2, 2, 2, 1, 1);
            gpkg.tiles().addTile(tileSet, tileMatrix, -1, 0, new byte[] {1, 2, 3, 4});

            Assert.fail("GeoPackage should throw a IllegalArgumentException when tile_column "
               + "is less than 0.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This adds a tile to a GeoPackage and verifies that the Tile object added
     * into the GeoPackage is the same Tile object returned.
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             throws if the image from the tile is not able to be read
     */
    @Test
    public void addTileMethodByCrsTileCoordinate() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, -80.0, 180.0, 80.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int zoomLevel = 2;
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int tileWidth = 256;
            final int tileHeight = 256;
            final double pixelXSize = (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth;
            final double pixelYSize = (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight;

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet,
                                                               zoomLevel,
                                                               matrixWidth,
                                                               matrixHeight,
                                                               tileWidth,
                                                               tileHeight,
                                                               pixelXSize,
                                                               pixelYSize);

            final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);

            final CrsProfile crsProfile = CrsProfileFactory.create(coordinateReferenceSystem);

            final CrsCoordinate crsCoordinate = new CrsCoordinate(0.0, -60.0, coordinateReferenceSystem);

            final Tile tileAdded = gpkg.tiles().addTile(tileSet, tileMatrix, crsCoordinate, crsProfile.getPrecision(), GeoPackageTilesAPITest.createImageBytes());

            final Tile tileFound = gpkg.tiles().getTile(tileSet, crsCoordinate, crsProfile.getPrecision(), zoomLevel);

            Assert.assertTrue("The GeoPackage did not return the tile Expected.",
                       tileAdded.getColumn()     == tileFound.getColumn()     &&
                       tileAdded.getIdentifier() == tileFound.getIdentifier() &&
                       tileAdded.getRow()        == tileFound.getRow()        &&
                       tileAdded.getZoomLevel()  == tileFound.getZoomLevel()  &&
                       Arrays.equals(tileAdded.getImageData(), tileFound.getImageData()));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Test if the GeoPackage can successfully add non empty tiles to a GeoPackage  without throwing an error.
     *
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addNonEmptyTile() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 20.0, 50.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;


            final TileMatrix tileMatrix =  gpkg.tiles().addTileMatrix(tileSet,
                                                                      2,
                                                                      matrixWidth,
                                                                      matrixHeight,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, new byte[] {1, 2, 3, 4});
        }
        finally
        {
            gpkg.close();
        }

        //use a query to test if the tile was inserted into database and to correct if the image is the same
        final String query = "SELECT tile_data FROM tileSetName WHERE zoom_level = 2 AND tile_column = 0 AND tile_row =0;";

        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            final Statement  stmt     = con.createStatement();

            try
            {
                final ResultSet  tileData = stmt.executeQuery(query);
                try
                {
                    // assert the image was inputed into the file
                    Assert.assertTrue("The GeoPackage did not successfully write the tile_data into the GeoPackage", tileData.next());
                    final byte[] bytes = tileData.getBytes("tile_data");

                    // compare images
                    Assert.assertTrue("The GeoPackage tile_data does not match the tile_data of the one given", Arrays.equals(bytes, new byte[] {1, 2, 3, 4}));
                }
                finally
                {
                    tileData.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an SQLException when adding a
     * duplicate tile to the GeoPackage.
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IllegalArgumentException
     *             throws if an illegal argument occurs to a method
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = SQLException.class)
    public void addDuplicateTiles()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "ident",
                                                            "description",
                                                            new BoundingBox(1.1, 1.1, 100.1, 100.1),
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixHeight = 2;
            final int matrixWidth  = 2;
            final int tileHeight   = 256;
            final int tileWidth    = 256;


            final TileMatrix matrixSet = gpkg.tiles().addTileMatrix(tileSet,
                                                                    1,
                                                                    matrixWidth,
                                                                    matrixHeight,
                                                                    tileWidth,
                                                                    tileHeight,
                                                                    (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                    (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final int column = 1;
            final int row    = 0;
            final byte[] imageData = new byte[]{1, 2, 3, 4};
            //add tile twice
            gpkg.tiles().addTile(tileSet, matrixSet, column, row, imageData);
            gpkg.tiles().addTile(tileSet, matrixSet, column, row, imageData);//see if it will add the same tile twice

            Assert.fail("Expected GeoPackage to throw an SQLException due to a unique constraint violation (zoom level, tile column, and tile row)."
               + " Was able to add a duplicate tile.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage throws an IllegalArgumentException when trying to
     * add a tile with a parameter that is null (image data)
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addBadTile()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            //add tile to gpkg
            final TileMatrix tileMatrix1 = gpkg.tiles().addTileMatrix(tileSet, 4, 10, 10, 1, 1, 1.0, 1.0);
            gpkg.tiles().addTile(tileSet, tileMatrix1, 4, 0, null);

            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when adding a null parameter to a Tile object (image data)");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage throws an IllegalArgumentException when trying to
     * add a tile with a parameter that is empty (image data)
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addBadTile2()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            //add tile to gpkg
            final TileMatrix tileMatrix1 = gpkg.tiles().addTileMatrix(tileSet, 4, 10, 10, 1, 1, 1.0, 1.0);
            gpkg.tiles().addTile(tileSet,tileMatrix1, 4, 0, new byte[]{});

            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when adding an empty parameter to Tile (image data)");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage throws an IllegalArgumentException when trying to
     * add a tile with a parameter that is null (tileMatrix)
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addBadTile4()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            //add tile to gpkg
            gpkg.tiles().addTile(tileSet, null, 4, 0, new byte[]{1,2,3,4});

            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when adding a null parameter to a addTile method (tileMatrix)");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage get tile will retrieve the correct tile with get tile method.
     * @throws  Exception throws if an exception occurs
     */
    @Test
    public void getTile() throws Exception
    {
        //create tiles and file
        final File testFile = TestUtility.getRandomFile(6);
        final byte[] originalTile1 = new byte[] {1, 2, 3, 4};
        final byte[] originalTile2 = new byte[] {1, 2, 3, 4};
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 90.0, 80.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int zoom1 = 4;
            final int zoom2 = 8;

            //add tile to gpkg
            final int matrixHeight = 2;
            final int matrixWidth = 4;
            final int tileHeight = 512;
            final int tileWidth = 256;

            final TileMatrix tileMatrix1 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      zoom1,
                                                                      matrixWidth,
                                                                      matrixHeight,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final int matrixHeight2 = 4;
            final int matrixWidth2 = 8;
            final int tileHeight2 = 512;
            final int tileWidth2 = 256;

            final TileMatrix tileMatrix2 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      zoom2,
                                                                      matrixWidth2,
                                                                      matrixHeight2,
                                                                      tileWidth2,
                                                                      tileHeight2,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth2)/tileWidth2,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight2)/tileHeight2);

            final Coordinate<Integer> tile1 = new Coordinate<Integer>(3, 0);
            final Coordinate<Integer> tile2 = new Coordinate<Integer>(7, 0);

            gpkg.tiles().addTile(tileSet, tileMatrix1, tile1.getX(), tile1.getY(), originalTile1);
            gpkg.tiles().addTile(tileSet, tileMatrix2, tile2.getX(), tile2.getY(), originalTile2);

            //Retrieve tile from gpkg
            final Tile gpkgTile1 = gpkg.tiles().getTile(tileSet, tile1.getX(), tile1.getY(), zoom1);
            final Tile gpkgTile2 = gpkg.tiles().getTile(tileSet, tile2.getX(), tile2.getY(), zoom2);

            Assert.assertTrue("GeoPackage did not return the image expected when using getTile method.",
                       Arrays.equals(gpkgTile1.getImageData(), originalTile1));
            Assert.assertTrue("GeoPackage did not return the image expected when using getTile method.",
                       Arrays.equals(gpkgTile2.getImageData(), originalTile2));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage get tile will retrieve the correct tile with get tile method.
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getTile2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            //Retrieve tile from gpkg
            final Tile gpkgTile1 = gpkg.tiles().getTile(tileSet, 4, 0, 4);

            Assert.assertTrue("GeoPackage did not null when the tile doesn't exist in the getTile method.",
                       gpkgTile1 == null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage get tile will retrieve the correct tile with get tile method.
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getTile3() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 80.0, 50.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixHeight = 2;
            final int matrixWidth = 3;
            final int tileHeight = 512;
            final int tileWidth = 256;

            final int zoom = 0;

            final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet,
                                                                     0,
                                                                     matrixWidth,
                                                                     matrixHeight,
                                                                     tileWidth,
                                                                     tileHeight,
                                                                     (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                     (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            //Tile coords

            final Coordinate<Integer> coord1 = new Coordinate<Integer>(2, 1);
            final byte[] imageData = new byte[]{1,2,3,4};

            //Retrieve tile from gpkg
            final Tile gpkgTileAdded    = gpkg.tiles().addTile(tileSet, tileMatrix, coord1.getX(), coord1.getY(), imageData);
            final Tile gpkgTileRecieved = gpkg.tiles().getTile(tileSet, coord1.getX(), coord1.getY(), zoom);

            Assert.assertTrue("GeoPackage did not return the same tile added to the gpkg.",
                              gpkgTileAdded.getColumn()                 == gpkgTileRecieved.getColumn()            &&
                              gpkgTileAdded.getRow()                    == gpkgTileRecieved.getRow()               &&
                              gpkgTileAdded.getIdentifier()             ==(gpkgTileRecieved.getIdentifier())       &&
                              gpkgTileAdded.getColumn()                 == gpkgTileRecieved.getColumn()            &&
                              gpkgTileAdded.getRow()                    == gpkgTileRecieved.getRow()               &&
                              gpkgTileAdded.getZoomLevel()              == gpkgTileRecieved.getZoomLevel()         &&
                              Arrays.equals(gpkgTileAdded.getImageData(), gpkgTileRecieved.getImageData()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will return null when the tile being searched for does not exist.
     *
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getTileThatIsNotInGpkg() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(4);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    null,
                                                    null,
                                                    new BoundingBox(0.0, 0.0, 80.0, 80.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixWidth = 3;
            final int matrixHeight = 6;
            final int tileWidth = 256;
            final int tileHeight = 256;
            // add tile to gpkg
            gpkg.tiles().addTileMatrix(tileSet,
                                       2,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            Assert.assertTrue("GeoPackage should have returned null for a missing tile.",
                       gpkg.tiles().getTile(tileSet, 0, 0, 0) == null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage will throw an IllegalArgumentException when using getTile method with null value for table name.
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileWithNullTileEntrySet() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(5);

        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().getTile(null, 2, 2, 0);
            Assert.fail("GeoPackage did not throw an IllegalArgumentException when giving a null value to table name (using getTile method)");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This adds a tile to a GeoPackage and verifies that the Tile object added
     * into the GeoPackage is the same Tile object returned.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             throws if an image cannot be read from or written
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileRelativeTileCoordinateNonExistent() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(18);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, -80.0, 180.0, 80.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int zoomLevel = 2;

            final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);

            final CrsCoordinate crsCoordinate = new CrsCoordinate(0.0, -60.0, coordinateReferenceSystem);

            gpkg.tiles().getTile(tileSet, crsCoordinate, CrsProfileFactory.create(coordinateReferenceSystem).getPrecision(), zoomLevel);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will return the all and the correct zoom levels in a GeoPackage
     *
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getZoomLevels() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                          .addTileSet("tableName",
                                                      "ident",
                                                      "desc",
                                                      new BoundingBox(5.0,5.0,50.0,50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
           // Add tile matrices that represent zoom levels 0 and 12
            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;

           gpkg.tiles().addTileMatrix(tileSet,
                                      0,
                                      matrixWidth,
                                      matrixHeight,
                                      tileWidth,
                                      tileHeight,
                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

           gpkg.tiles().addTileMatrix(tileSet,
                                      12,
                                      matrixWidth,
                                      matrixHeight,
                                      tileWidth,
                                      tileHeight,
                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

           final Set<Integer> zooms  = gpkg.tiles().getTileZoomLevels(tileSet);

           final ArrayList<Integer> expectedZooms = new ArrayList<Integer>();

           expectedZooms.add(Integer.valueOf(12));
           expectedZooms.add(Integer.valueOf(0));

           for(final Integer zoom : zooms)
           {
               Assert.assertTrue("The GeoPackage's get zoom levels method did not return expected values.",
                                  FunctionalUtility.anyMatch(expectedZooms,
                                                             new Predicate<Integer>(){
                                                                                          @Override
                                                                                        public boolean apply(final Integer currentZoom)
                                                                                          {
                                                                                              return currentZoom.equals(zoom);
                                                                                          }
                                                                                      }));
           }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when given a
     * TileSet null for getZoomLevels()
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void getZoomLevelsNullTileSetContentEntry()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().getTileZoomLevels(null);
            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when givinga null parameter to getTileZoomLevels");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

     /**
      * Tests if GeoPackage will throw an IllegalArgumentException
      * when giving a null parameter to getRowCount
      * @throws Exception throws if an exception occurs
      */
    @Test(expected = IllegalArgumentException.class)
    public void getRowCountNullContentEntry() throws  Exception
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.core().getRowCount(null);
            Assert.fail("GeoPackage should have thrown an IllegalArgumentException.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Verifies that the GeoPackage counts the correct number of rows
     * with the method getRowCount
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void getRowCountVerify() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "ident",
                                                    "desc",
                                                    new BoundingBox(0.0,0.0,80.0,50.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            //create two TileMatrices to represent the tiles
            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;

            final TileMatrix tileMatrix1 = gpkg.tiles().addTileMatrix(tileSet,
                                                                    1,
                                                                    matrixWidth,
                                                                    matrixHeight,
                                                                    tileWidth,
                                                                    tileHeight,
                                                                    (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                    (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final int matrixHeight2 = 4;
            final int matrixWidth2 = 4;

            final TileMatrix tileMatrix2 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      0,
                                                                      matrixWidth2,
                                                                      matrixHeight2,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth2)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight2)/tileHeight);
            //add two tiles
            gpkg.tiles().addTile(tileSet, tileMatrix2, 0, 0, new byte[] {1, 2, 3, 4});
            gpkg.tiles().addTile(tileSet, tileMatrix1, 0, 0, new byte[] {1, 2, 3, 4});

            final long count = gpkg.core().getRowCount(tileSet);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "Expected a different value from GeoPackage on getRowCount. expected: 2 actual: %d", count),
                              count == 2);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a null parameter to the method getTileMatrixSetEntry();
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileMatrixSetEntryNullTileSetContentEntry()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().getTileMatrixSet(null);

            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a null parameter for TileSet");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage returns the expected tileMatrices using the
     * getTIleMatrices(TileSet tileSet) method
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             throws if an image cannot be read from or written
     */
    @Test
    public void getTileMatricesVerify() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tables", "identifier", "description", new BoundingBox(0.0,0.0,80.0,80.0), gpkg.core().getSpatialReferenceSystem(-1));

            final int matrixHeight = 2;
            final int matrixWidth  = 4;
            final int tileHeight   = 512;
            final int tileWidth    = 256;

            final TileMatrix tileMatrix  = gpkg.tiles().addTileMatrix(tileSet,
                                                                      0,
                                                                      matrixWidth,
                                                                      matrixHeight,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final int matrixHeight2 = 4;
            final int matrixWidth2  = 8;
            final int tileHeight2   = 512;
            final int tileWidth2    = 256;

            final TileMatrix tileMatrix2 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      3,
                                                                      matrixWidth2,
                                                                      matrixHeight2,
                                                                      tileWidth2,
                                                                      tileHeight2,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth2)/tileWidth2,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight2)/tileHeight2);

            gpkg.tiles().addTile(tileSet, tileMatrix, 0, 0, GeoPackageTilesAPITest.createImageBytes());
            gpkg.tiles().addTile(tileSet, tileMatrix, 1, 0, GeoPackageTilesAPITest.createImageBytes());

            final ArrayList<TileMatrix> expectedTileMatrix = new ArrayList<TileMatrix>();
            expectedTileMatrix.add(tileMatrix);
            expectedTileMatrix.add(tileMatrix2);

            final List<TileMatrix> gpkgTileMatrices = gpkg.tiles().getTileMatrices(tileSet);
            Assert.assertTrue("Expected the GeoPackage to return two Tile Matrices.",gpkgTileMatrices.size() == 2);

            for(final TileMatrix gpkgTileMatrix : gpkg.tiles().getTileMatrices(tileSet))
            {
                Assert.assertTrue("The tile entry's information in the GeoPackage does not match what was originally given to a GeoPackage",
                                  FunctionalUtility.anyMatch(expectedTileMatrix,
                                                             new Predicate<TileMatrix>()
                                                             {
                                                                 @Override
                                                                 public boolean apply(final TileMatrix expectedTM)
                                                                 {
                                                                     return expectedTM.getTableName()    .equals(gpkgTileMatrix.getTableName())   &&
                                                                            expectedTM.getMatrixHeight() ==      gpkgTileMatrix.getMatrixHeight() &&
                                                                            expectedTM.getMatrixWidth()  ==      gpkgTileMatrix.getMatrixWidth()  &&
                                                                            expectedTM.getPixelXSize()   ==      gpkgTileMatrix.getPixelXSize()   &&
                                                                            expectedTM.getPixelYSize()   ==      gpkgTileMatrix.getPixelYSize()   &&
                                                                            expectedTM.getZoomLevel()    ==      gpkgTileMatrix.getZoomLevel();
                                                                 }
                                                             }));
            }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will return null if no TileMatrix Entries are
     * found in the GeoPackage that matches the TileSet given.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileMatricesNonExistant() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final TileSet tileSet = gpkg.tiles()
                                       .addTileSet("tables",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,0.0,0.0),
                                                   gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

               Assert.assertTrue("Expected the GeoPackage to return null when no tile Matrices are found", gpkg.tiles().getTileMatrices(tileSet).size() == 0);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a matrix width that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            gpkg.tiles().addTileMatrix(tileSet, 0, 0, 5, 6, 7, 8, 9);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a matrix width that is <= 0");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a matrix height that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException2()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("name",
                                                    "identifier",
                                                    "description",
                                                    new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                    gpkg.core().getSpatialReferenceSystem(-1));

            gpkg.tiles().addTileMatrix(tileSet, 0, 4, 0, 6, 7, 8, 9);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a matrix height that is <= 0");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a tile width that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException3()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0, 0.0, 0.0, 0.0), gpkg.core().getSpatialReferenceSystem(-1));
            gpkg.tiles().addTileMatrix(tileSet, 0, 4, 5, 0, 7, 8, 9);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a tile width that is <= 0");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a tile height that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException4()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            gpkg.tiles().addTileMatrix(tileSet, 0, 4, 5, 6, 0, 8, 9);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a tile height that is <= 0");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a pixelXsize that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException5()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            gpkg.tiles().addTileMatrix(tileSet, 0, 4, 5, 6, 7, 0, 9);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a pixelXsize that is <= 0");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a TileMatrix with a pixelYSize that is <=0
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatricesIllegalArgumentException6()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));
            gpkg.tiles().addTileMatrix(tileSet, 0, 4, 5, 6, 7, 8, 0);
            Assert.fail("Expected GeoPackage to throw an IllegalArgumentException when giving a Tile Matrix a pixelYSize that is <= 0");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage Tiles would throw an IllegalArgumentException when
     * attempting to add a Tile Matrix corresponding to the same tile set and
     * zoom level but have differing other fields
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatrixSameZoomDifferentOtherFields()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,0.0,0.0), gpkg.core().getSpatialReferenceSystem(-1));

            gpkg.tiles().addTileMatrix(tileSet, 0, 2, 3, 4, 5, 6, 7);
            gpkg.tiles().addTileMatrix(tileSet, 0, 3, 2, 5, 4, 7, 6);
            Assert.fail("Expected GeoPackage Tiles to throw an IllegalArgumentException when addint a Tile Matrix with the same tile set and zoom level information but differing other fields");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage returns the same TileMatrix when trying to add
     * the same TileMatrix twice (verifies the values are the same)
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void addTileMatrixTwiceVerify()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("name", "identifier", "description", new BoundingBox(0.0,0.0,90.0,90.0), gpkg.core().getSpatialReferenceSystem(-1));
            final int matrixHeight = 2;
            final int matrixWidth = 2;
            final int tileHeight = 256;
            final int tileWidth = 256;

            final TileMatrix tileMatrix1 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      0,
                                                                      matrixWidth,
                                                                      matrixHeight,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final TileMatrix tileMatrix2 = gpkg.tiles().addTileMatrix(tileSet,
                                                                      0,
                                                                      matrixWidth,
                                                                      matrixHeight,
                                                                      tileWidth,
                                                                      tileHeight,
                                                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

           Assert.assertTrue("Expected the GeoPackage to return the existing Tile Matrix.",tileMatrix1.equals(tileMatrix2.getTableName(),
                                                                                                              tileMatrix2.getZoomLevel(),
                                                                                                              tileMatrix2.getMatrixWidth(),
                                                                                                              tileMatrix2.getMatrixHeight(),
                                                                                                              tileMatrix2.getTileWidth(),
                                                                                                              tileMatrix2.getTileHeight(),
                                                                                                              tileMatrix2.getPixelXSize(),
                                                                                                              tileMatrix2.getPixelYSize()));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage returns the same TileMatrix when trying to add
     * the same TileMatrix twice (verifies the values are the same)
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatrixNullTileSet()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().addTileMatrix(null, 0, 2, 3, 4, 5, 6, 7);
            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when giving a null parameter TileSet to addTileMatrix");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when a user
     * tries to add a negative value for zoom level (when adding a tile Matrix
     * entry)
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatrixWithNegativeZoomLevel()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(2.0,1.0,4.0,3.0),
                                                      gpkg.core().getSpatialReferenceSystem(0));
            gpkg.tiles().addTileMatrix(tileSet, -1, 2, 4, 6, 8, 10, 12);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if given a non empty tile Matrix Metadata information can be added without throwing an error.
     *
     * @throws SQLException throws if an SQLException occurs
     * @throws Exception throws if an exception occurs
     */
    @Test
    public void addNonEmptyTileMatrix() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            //add information to gpkg
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tileSetName",
                                                    "title",
                                                    "tiles",
                                                    new BoundingBox(0.0, 0.0, 80.0, 80.0),
                                                    gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixWidth = 4;
            final int matrixHeight = 8;
            final int tileWidth = 256;
            final int tileHeight = 512;

            gpkg.tiles().addTileMatrix(tileSet,
                                       1,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);
        }
        finally
        {
            gpkg.close();
        }
        //test if information added is accurate
        final int matrixWidth = 4;
        final int matrixHeight = 8;
        final int tileWidth = 256;
        final int tileHeight = 512;

        final String query = String.format(Locale.getDefault(),
                                           "SELECT table_name FROM gpkg_tile_matrix "
                                           + "WHERE zoom_level    = %d AND "
                                           + "      matrix_height = %d AND "
                                           + "       matrix_width = %d AND "
                                           + "        tile_height = %d AND "
                                           + "         tile_width = %d;",
                                           1,
                                           matrixHeight,
                                           matrixWidth,
                                           tileHeight,
                                           tileWidth);

        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            final Statement stmt = con.createStatement();

            try
            {
                final ResultSet tableName = stmt.executeQuery(query);

                try
                {
                    Assert.assertTrue("The GeoPackage did not enter the correct record into the gpkg_tile_matrix table", tableName.getString("table_name").equals("tileSetName"));
                }
                finally
                {
                    tableName.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
     }

    /**
     * Tests if GeoPackage Tiles will throw an IllegalArgumentException when the
     * pixelXSize is not correctly calculated
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatrixIllegalBounds() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                  .addTileSet("tableName",
                                              "identifier",
                                              "description",
                                              new BoundingBox(0.0,0.0,180.0,90.0),
                                              gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int zoomLevel = 5;
            final int matrixWidth = 10;
            final int matrixHeight = 11;
            final int tileWidth = 256;
            final int tileHeight = 512;
            final double pixelXSize = 500.23123;//invalid pixelx size
            final double pixelYSize = tileSet.getBoundingBox().getHeight()/matrixHeight/tileHeight;
            gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);

            fail("Expected GeoPackageTiles to throw an IllegalArgtumentException when pixelXSize != boundingBoxHeight/matrixHeight/tileHeight.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Tiles will throw an IllegalArgumentException when the
     * pixelYSize is not correctly calculated
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void addTileMatrixIllegalBounds2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                  .addTileSet("tableName",
                                              "identifier",
                                              "description",
                                              new BoundingBox(0.0,0.0,180.0,90.0),
                                              gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int zoomLevel = 5;
            final int matrixWidth = 10;
            final int matrixHeight = 11;
            final int tileWidth = 256;
            final int tileHeight = 512;
            final double pixelXSize = tileSet.getBoundingBox().getWidth()/matrixWidth/tileWidth;
            final double pixelYSize = 500.23123;//invalid pixel y size
            gpkg.tiles().addTileMatrix(tileSet, zoomLevel, matrixWidth, matrixHeight, tileWidth, tileHeight, pixelXSize, pixelYSize);

            fail("Expected GeoPackageTiles to throw an IllegalArgtumentException when pixelXSize != boundingBoxWidth/matrixWidth/tileWidth.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Tests if a GeoPackage will throw an IllegalArgumentException when giving
     * a null parameter to getTileMatrices
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileMatricesNullParameter() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().getTileMatrices(null);
            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when giving getTileMatrices a TileSet that is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage getTIleMatrix can retrieve the correct TileMatrix
     * from the GeoPackage.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileMatrixVerify()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(6);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("tableName",
                                                    "identifier",
                                                    "description",
                                                    new BoundingBox(0.0,0.0,100.0,100.0),
                                                    gpkg.core().getSpatialReferenceSystem(-1));
            final int matrixHeight = 2;
            final int matrixWidth = 6;
            final int tileHeight = 512;
            final int tileWidth = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       1,
                                       matrixWidth,
                                       matrixHeight,
                                       tileWidth,
                                       tileHeight,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);

            final int matrixHeight2 = 1;
            final int matrixWidth2 = 3;
            final int tileHeight2 = 512;
            final int tileWidth2 = 256;

            final TileMatrix tileMatrix         = gpkg.tiles().addTileMatrix(tileSet,
                                                                             0,
                                                                             matrixWidth2,
                                                                             matrixHeight2,
                                                                             tileWidth2,
                                                                             tileHeight2,
                                                                             (tileSet.getBoundingBox().getWidth()/matrixWidth2)/tileWidth2,
                                                                             (tileSet.getBoundingBox().getHeight()/matrixHeight2)/tileHeight2);

            final TileMatrix returnedTileMatrix = gpkg.tiles().getTileMatrix(tileSet, 0);

            Assert.assertTrue("GeoPackage did not return the TileMatrix expected", tileMatrix.getMatrixHeight() ==      returnedTileMatrix.getMatrixHeight() &&
                                                                                   tileMatrix.getMatrixWidth()  ==      returnedTileMatrix.getMatrixWidth()  &&
                                                                                   tileMatrix.getPixelXSize()   ==      returnedTileMatrix.getPixelXSize()   &&
                                                                                   tileMatrix.getPixelYSize()   ==      returnedTileMatrix.getPixelYSize()   &&
                                                                                   tileMatrix.getTableName()    .equals(returnedTileMatrix.getTableName())   &&
                                                                                   tileMatrix.getTileHeight()   ==      returnedTileMatrix.getTileHeight()   &&
                                                                                   tileMatrix.getTileWidth()    ==      returnedTileMatrix.getTileWidth()    &&
                                                                                   tileMatrix.getZoomLevel()    ==      returnedTileMatrix.getZoomLevel());
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage returns null if the TileMatrix entry does not
     * exist in the GeoPackage file.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileMatrixNonExistant()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("TableName",
                                                    "identifier",
                                                    "description",
                                                    new BoundingBox(0.0,0.0,0.0,0.0),
                                                    gpkg.core().getSpatialReferenceSystem(-1));

            Assert.assertTrue("GeoPackage was supposed to return null when there is a nonexistant TileMatrix entry at that zoom level and TileSet",
                       null == gpkg.tiles().getTileMatrix(tileSet, 0));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw an IllegalArgumentException when
     * giving a null parameter to getTileMatrix.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void getTileMatrixNullParameter()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().getTileMatrix(null, 8);
            Assert.fail("GeoPackage should have thrown an IllegalArgumentException when giving a null parameter for TileSet in the method getTileMatrix");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if getTileMatrixSet retrieves the values that is expected
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void getTileMatrixSetVerify()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            //values for tileMatrixSet
            final String                 tableName   = "tableName";
            final String                 identifier  = "identifier";
            final String                 description = "description";
            final BoundingBox            bBox        = new BoundingBox(2.0, 1.0, 4.0, 3.0);
            final SpatialReferenceSystem srs         = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            //add tileSet and tileMatrixSet to gpkg
            final TileSet       tileSet       = gpkg.tiles().addTileSet(tableName, identifier, description, bBox, srs);
            final TileMatrixSet tileMatrixSet = gpkg.tiles().getTileMatrixSet(tileSet);

            Assert.assertTrue("Expected different values from getTileMatrixSet for SpatialReferenceSystem or BoundingBox or TableName.",
                                               tileMatrixSet.getBoundingBox()           .equals(bBox) &&
                                               tileMatrixSet.getSpatialReferenceSystem().equals(srs) &&
                                               tileMatrixSet.getTableName()             .equals(tableName));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage will throw a GeoPackage Conformance Exception
     * when given a GeoPackage that violates a requirement with a severity equal
     * to Error
     * @throws SQLException throws if an SQLException occurs
     * @throws Exception throws if an exception occurs
     */
    @Test(expected = ConformanceException.class)
    public void geoPackageConformanceException() throws SQLException, Exception
    {
        final File testFile = TestUtility.getRandomFile(19);
        testFile.createNewFile();
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Open);

        try
        {
            Assert.fail("GeoPackage did not throw a geoPackageConformanceException as expected.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Geodetic crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateUpperRightGeodetic() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 1;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(-45.234567, 45.213192, geodeticRefSys);//upper right tile

        final File testFile = TestUtility.getRandomFile(8);

        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, 0.0, 0.0, 85.0511287798066),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                       zoomLevel,
                       matrixWidth,
                       matrixHeight,
                       pixelXSize,
                       pixelYSize,
                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 1. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 1);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Geodetic crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateUpperLeftGeodetic() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 1;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(-180, 85, geodeticRefSys);//upper left tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            new BoundingBox(-180.0, 0.0, 0.0, 85.0511287798066),
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                      zoomLevel,
                                      matrixWidth,
                                      matrixHeight,
                                      pixelXSize,
                                      pixelYSize,
                                      (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                      (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 0. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                       relativeCoord.getY() == 0 && relativeCoord.getX() == 0);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Geodetic crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateLowerLeftGeodetic() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 1;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(-90, 41, geodeticRefSys);//lower left tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, 0.0, 0.0, 85.0511287798066),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth() /matrixWidth )/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 1, Expected Column: 0. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                       relativeCoord.getY() == 1 && relativeCoord.getX() == 1);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Geodetic crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateLowerRightGeodetic() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 1;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(-0.000001, 12, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, 0.0, 0.0, 85.0511287798066),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 1, Expected Column: 1. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 1 && relativeCoord.getX() == 1);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Global Mercator crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateUpperLeftGlobalMercator() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final EllipsoidalMercatorCrsProfile mercator = new EllipsoidalMercatorCrsProfile();

        final int zoomLevel = 6;

        final CoordinateReferenceSystem globalMercator   = new CoordinateReferenceSystem("EPSG", 3395);
        final Coordinate<Double>        coordInMeters    = mercator.fromGlobalGeodetic(new Coordinate<Double>(-45.0, 5.0));
        final CrsCoordinate             crsMercatorCoord = new CrsCoordinate(coordInMeters.getX(), coordInMeters.getY(), globalMercator);

        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Coordinate<Double> minBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>(-90.0, -60.0));
            final Coordinate<Double> maxBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>( 5.0,   10.0));

            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(minBoundingBoxCoord.getX(), minBoundingBoxCoord.getY(), maxBoundingBoxCoord.getX(), maxBoundingBoxCoord.getY()),
                                                      gpkg.core().addSpatialReferenceSystem("EPSG/World Mercator",
                                                                                            "EPSG",
                                                                                            3395,
                                                                                            "definition",
                                                                                            "description"));

            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth() /matrixWidth )/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsMercatorCoord, CrsProfileFactory.create(globalMercator).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The GeoPackage did not return the expected row and column from the conversion crs to relative tile coordinate.\nExpected Row: 0, Expected Column: 0.\nActual Row: %d, Actual Column: %d.",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 0);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Global Mercator crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateUpperRightGlobalMercator() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final EllipsoidalMercatorCrsProfile mercator = new EllipsoidalMercatorCrsProfile();

        final int zoomLevel = 6;

        final CoordinateReferenceSystem globalMercator   = new CoordinateReferenceSystem("EPSG", 3395);
        final Coordinate<Double>        coordInMeters    = mercator.fromGlobalGeodetic(new Coordinate<Double>(-42.0, 5.0));
        final CrsCoordinate             crsMercatorCoord = new CrsCoordinate(coordInMeters.getX(), coordInMeters.getY(), globalMercator);

        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Coordinate<Double> minBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>(-90.0, -60.0));
            final Coordinate<Double> maxBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>( 5.0,   10.0));

            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(minBoundingBoxCoord.getX(), minBoundingBoxCoord.getY(), maxBoundingBoxCoord.getX(), maxBoundingBoxCoord.getY()),
                                                      gpkg.core().addSpatialReferenceSystem("EPSG/World Mercator",
                                                                                            "EPSG",
                                                                                            3395,
                                                                                            "definition",
                                                                                            "description"));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsMercatorCoord, CrsProfileFactory.create(globalMercator).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The GeoPackage did not return the expected row and column from the conversion crs to relative tile coordinate.\nExpected Row: 0, Expected Column: 1.\nActual Row: %d, Actual Column: %d.",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 1);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Tests if the GeoPackage can convert an Global Mercator crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateLowerLeftGlobalMercator() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final EllipsoidalMercatorCrsProfile mercator = new EllipsoidalMercatorCrsProfile();

        final int zoomLevel = 6;

        final CoordinateReferenceSystem globalMercator   = new CoordinateReferenceSystem("EPSG", 3395);
        final Coordinate<Double>        coordInMeters    = mercator.fromGlobalGeodetic(new Coordinate<Double>(-47.0, -45.0));
        final CrsCoordinate             crsMercatorCoord = new CrsCoordinate(coordInMeters.getX(), coordInMeters.getY(), globalMercator);

        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Coordinate<Double> minBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>(-90.0, -60.0));
            final Coordinate<Double> maxBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>( 5.0,   10.0));

            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(minBoundingBoxCoord.getX(), minBoundingBoxCoord.getY(), maxBoundingBoxCoord.getX(), maxBoundingBoxCoord.getY()),
                                                      gpkg.core().addSpatialReferenceSystem("EPSG/World Mercator",
                                                                                            "EPSG",
                                                                                            3395,
                                                                                            "definition",
                                                                                            "description"));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsMercatorCoord, CrsProfileFactory.create(globalMercator).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The GeoPackage did not return the expected row and column from the conversion crs to relative tile coordinate.\nExpected Row: 1, Expected Column: 0.\nActual Row: %d, Actual Column: %d.",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 1 && relativeCoord.getX() == 0);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage can convert an Global Mercator crsCoordinate to a
     * relative tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateLowerRightGlobalMercator() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final EllipsoidalMercatorCrsProfile mercator = new EllipsoidalMercatorCrsProfile();

        final int zoomLevel = 6;

        final CoordinateReferenceSystem globalMercator   = new CoordinateReferenceSystem("EPSG", 3395);
        final Coordinate<Double>        coordInMeters    = mercator.fromGlobalGeodetic(new Coordinate<Double>(4.999, -55.0));
        final CrsCoordinate             crsMercatorCoord = new CrsCoordinate(coordInMeters.getX(), coordInMeters.getY(), globalMercator);

        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Coordinate<Double> minBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>(-90.0, -60.0));
            final Coordinate<Double> maxBoundingBoxCoord = mercator.fromGlobalGeodetic(new Coordinate<Double>(  5.0,  10.0));

            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            new BoundingBox(minBoundingBoxCoord.getX(), minBoundingBoxCoord.getY(), maxBoundingBoxCoord.getX(), maxBoundingBoxCoord.getY()),
                                                            gpkg.core().addSpatialReferenceSystem("EPSG/World Mercator",
                                                                                                  "EPSG",
                                                                                                  3395,
                                                                                                  "definition",
                                                                                                  "description"));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsMercatorCoord, CrsProfileFactory.create(globalMercator).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The GeoPackage did not return the expected row and column from the conversion crs to relative tile coordinate.\nExpected Row: 1, Expected Column: 1.\nActual Row: %d, Actual Column: %d.",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 1 && relativeCoord.getX() == 1);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage can translate a crs to a relative tile coordinate
     * when there are multiple zoom levels and when there are more tiles at the
     * higher zoom
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordinateMultipleZoomLevels() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 5;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(-27.5, -1.25, geodeticRefSys);

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            new BoundingBox(-100.0, -60.0, 100.0, 60.0),
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            final int matrixWidth1  = 16;
            final int matrixHeight1 = 24;
            final int pixelXSize   = 256;
            final int pixelYSize   = 512;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth1,
                                       matrixHeight1,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth() /matrixWidth1 )/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight1)/pixelYSize);

            final int matrixWidth2  = 4;
            final int matrixHeight2 = 6;
            final int zoomLevel2 = 3;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel2,
                                       matrixWidth2,
                                       matrixHeight2,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth() /matrixWidth2 )/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight2)/pixelYSize);
            final int matrixWidth3  = 8;
            final int matrixHeight3 = 12;
            final int zoomLevel3 = 4;
            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel3,
                                       matrixWidth3,
                                       matrixHeight3,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth() /matrixWidth3 )/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight3)/pixelYSize);


            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 12, Expected Column: 5. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 12 && relativeCoord.getX() == 5);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will maintain the conversions
     * from tile coordinate to crs coordinate back to tile coordinate
     *
     * this test originated from tiling a GeoPackage that had
     * a tile matrix set that lied on the grid exactly
     *
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws SQLException
     *             if an SQLException occurs
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile
     */
    @Test
    public void tileCoordinateToCrsBackToTileCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final BoundingBox bBox = new BoundingBox(10018754.1713946, -10018754.1713946, 20037508.3427892, 0.0);//data from a GeoPackage where the bounding box lied on the grid
        final SphericalMercatorCrsProfile spherMerc = new SphericalMercatorCrsProfile();

        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            final SpatialReferenceSystem srs = gpkg.core()
                                             .addSpatialReferenceSystem(spherMerc.getName(),
                                                                        spherMerc.getCoordinateReferenceSystem().getAuthority(),
                                                                        spherMerc.getCoordinateReferenceSystem().getIdentifier(),
                                                                        "definition", spherMerc.getDescription());


            final TileSet tileSet = gpkg.tiles()
                                        .addTileSet("pyramid",
                                                    "title",
                                                    "tiles",
                                                    bBox,
                                                    srs);

            final TileScheme scheme = new ZoomTimesTwo(2, 9, 1, 1);
            //populate the TileMatrices
            for(final int zoom: scheme.getZoomLevels())
            {
                final TileMatrixDimensions dimensions = scheme.dimensions(zoom);
                final int matrixWidth = dimensions.getWidth();
                final int matrixHeight = dimensions.getHeight();
                final int tileWidth = 256;
                final int tileHeight = 256;


                gpkg.tiles().addTileMatrix(tileSet,
                                           zoom,
                                           matrixWidth,
                                           matrixHeight,
                                           tileWidth,
                                           tileHeight,
                                           (tileSet.getBoundingBox().getWidth()/matrixWidth)/tileWidth,
                                           (tileSet.getBoundingBox().getHeight()/matrixHeight)/tileHeight);
            }
            //Test that the conversion from tileCoordinate-> CrsCoordinate -> tileCoordinate is as expected(original tileCoordinate)
            for(final int zoomLevel: scheme.getZoomLevels())
            {
                for(int row = 0; row < scheme.dimensions(zoomLevel).getHeight(); row++)
                {
                    for(int column = 0;  column < scheme.dimensions(zoomLevel).getWidth(); column++)
                    {
                        final CrsCoordinate crsCoordinate = gpkg.tiles().tileToCrsCoordinate(tileSet, column, row, zoomLevel);
                        final Coordinate<Integer> tileCoordinate = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoordinate, spherMerc.getPrecision(), zoomLevel);
                        final Coordinate<Integer> expectedTileCoordinate = new Coordinate<Integer>(column, row);
                        assertEquals(expectedTileCoordinate, tileCoordinate);

                    }
                }
            }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies in the middle of four tiles.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(76.4875, 36.45, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(-180.0, 0.0, 90.0, 85.05),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 20;
            final int matrixHeight = 7;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 4, Expected Column: 18. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 4 && relativeCoord.getX() == 18);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies between two tiles on top of each
     * other
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(10, 25, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 0. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 1 && relativeCoord.getX() == 0);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies on the left border
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(0, 40, geodeticRefSys);//upper Left tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 0. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 0);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies on the right border
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase4() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(29.9, 30, geodeticRefSys);//upper right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 1. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 1);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies on the top border
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase5() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, 50, geodeticRefSys);//upper right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 0, Expected Column: 1. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 0 && relativeCoord.getX() == 1);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests the validity of the transformation of crs to relative tile
     * coordinate when the crs coordinate lies on the bottom border
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test
    public void crsToRelativeTileCoordEdgeCase6() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, 0.01, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 1, Expected Column: 1. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 1 && relativeCoord.getX() == 1);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Test if a crsCoordinate can be translated to a tile coordinate
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */

    @Test
    public void crsToRelativeTileCoordinateEdgeCase7()throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 0;
        final CrsCoordinate        coordinate = new CrsCoordinate((GlobalGeodeticCrsProfile.Bounds.getMinX()+(2*(GlobalGeodeticCrsProfile.Bounds.getWidth()))  / 8),
                (GlobalGeodeticCrsProfile.Bounds.getMaxY()-(6*(GlobalGeodeticCrsProfile.Bounds.getHeight())) / 9),
                "epsg",
                4326);
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                            "identifier",
                                                            "description",
                                                            GlobalGeodeticCrsProfile.Bounds,
                                                            gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 8;
            final int matrixHeight = 9;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            final Coordinate<Integer> relativeCoord = gpkg.tiles().crsToTileCoordinate(tileSet, coordinate, CrsProfileFactory.create("EPSG", 4326).getPrecision(), zoomLevel);

            Assert.assertTrue(String.format(Locale.getDefault(),
                                            "The crsToRelativeTileCoordinate did not return the expected values.\nExpected Row: 6, Expected Column: 2. \nActual Row: %d, Actual Column: %d",
                                            relativeCoord.getY(),
                                            relativeCoord.getX()),
                              relativeCoord.getY() == 6 && relativeCoord.getX() == 2);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }

    }

    /**
     * Tests if a GeoPackage will throw the appropriate exception when giving
     * the method a null value for crsCoordinate.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            gpkg.tiles().crsToTileCoordinate(tileSet, null, CrsProfileFactory.create("EPSG", 4326).getPrecision(), 0);

            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to input a crs tile coordinate that was null to the method crsToRelativeTileCoordinate.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a GeoPackage will throw the appropriate exception when giving
     * the method a null value for crsCoordinate.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final int zoomLevel = 1;
            final CoordinateReferenceSystem coordinateReferenceSystem = new CoordinateReferenceSystem("Police", 99);
            final CrsCoordinate           crsCoord                     = new CrsCoordinate(15, 20, coordinateReferenceSystem);

            gpkg.tiles().crsToTileCoordinate(null, crsCoord, 2, zoomLevel);

            Assert.fail("Expected the GeoPackage to throw an IllegalArgumentException when trying to input a tileSet that was null to the method crsToRelativeTileCoordinate.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests that the appropriate exception is thrown when trying to find a
     * crs coordinate from a different SRS from the tiles.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, 50, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem(-1));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.fail("Expected the GoePackage to throw an exception when the crs coordinate and the tiles are from two different projections.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests that the appropriate exception is thrown when trying to find a
     * crs coordinate from with a zoom level that is not in the matrix table
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException4() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, 50, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;
            final int differentZoomLevel = 12;

            gpkg.tiles().addTileMatrix(tileSet,
                                       differentZoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests that the appropriate exception is thrown when trying to find a
     * crs coordinate is not within bounds
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException5() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG",4326);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, -50, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * This tests that the appropriate exception is thrown when trying to find a
     * crs coordinate from a different SRS from the tiles.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws ClassNotFoundException
     *             if the connection to the database cannot be made
     * @throws ConformanceException
     *             throws if it does not meet all the requirements
     * @throws IOException
     *             if an error occurs from reading or writing a Tile or File
     */
    @Test(expected = IllegalArgumentException.class)
    public void crsToRelativeTileCoordException6() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final int zoomLevel = 15;
        final CoordinateReferenceSystem geodeticRefSys = new CoordinateReferenceSystem("EPSG", 3857);
        final CrsCoordinate crsCoord = new CrsCoordinate(20, 50, geodeticRefSys);//lower right tile

        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                      "identifier",
                                                      "description",
                                                      new BoundingBox(0.0, 0.0, 30.0, 50.0),
                                                      gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            final int matrixWidth = 2;
            final int matrixHeight = 2;
            final int pixelXSize = 256;
            final int pixelYSize = 256;

            gpkg.tiles().addTileMatrix(tileSet,
                                       zoomLevel,
                                       matrixWidth,
                                       matrixHeight,
                                       pixelXSize,
                                       pixelYSize,
                                       (tileSet.getBoundingBox().getWidth()/matrixWidth)/pixelXSize,
                                       (tileSet.getBoundingBox().getHeight()/matrixHeight)/pixelYSize);

            gpkg.tiles().crsToTileCoordinate(tileSet, crsCoord, CrsProfileFactory.create(geodeticRefSys).getPrecision(), zoomLevel);

            Assert.fail("Expected the GoePackage to throw an exception when the crs coordinate and the tiles are from two different projections.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a tileCoordinate can be converted to the correct CRS Coordinate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test
    public void tileToCrsCoordinate() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final BoundingBox bBox         =  new BoundingBox(0, 0.0, 180.0,90.0);
            final int         row          = 3;
            final int         column       = 5;
            final int         zoomLevel    = 4;
            final int         matrixWidth  = 6;
            final int         matrixHeight = 4;
            final TileMatrix  tileMatrix   = createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);

            final CrsCoordinate crsCoordReturned = gpkg.tiles().tileToCrsCoordinate(gpkg.tiles().getTileSet(tileMatrix.getTableName()), column, row, zoomLevel);
            final CrsCoordinate crsCoordExpected = new CrsCoordinate(bBox.getMinX() + column * (bBox.getWidth()  / matrixWidth),
                                                                     bBox.getMaxY() - row    * (bBox.getHeight() / matrixHeight),
                                                                     new GlobalGeodeticCrsProfile().getCoordinateReferenceSystem());

            this.assertCoordinatesEqual(crsCoordReturned, crsCoordExpected);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a tileCoordinate can be converted to the correct CRS Coordinate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test
    public void tileToCrsCoordinate2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final SphericalMercatorCrsProfile spherMercator = new SphericalMercatorCrsProfile();
            final BoundingBox bBox = new BoundingBox(spherMercator.getBounds().getMinX()/2,
                                                     spherMercator.getBounds().getMinY()/3,
                                                     spherMercator.getBounds().getMaxX(),
                                                     spherMercator.getBounds().getMaxY()/2);
            final int row          = 5;
            final int column       = 1;
            final int zoomLevel    = 4;
            final int matrixWidth  = 13;
            final int matrixHeight = 8;

            final SpatialReferenceSystem srs = gpkg.core().addSpatialReferenceSystem(spherMercator.getName(),
                                                                                     spherMercator.getCoordinateReferenceSystem().getAuthority(),
                                                                                     spherMercator.getCoordinateReferenceSystem().getIdentifier(),
                                                                                     spherMercator.getWellKnownText(),
                                                                                     spherMercator.getDescription());

            final TileMatrix  tileMatrix   = createTileSetAndTileMatrix(gpkg, srs, bBox, zoomLevel, matrixWidth, matrixHeight, 256, 256, "tableName");

            final CrsCoordinate crsCoordExpected =  new CrsCoordinate(bBox.getMinX() + column * (bBox.getWidth()/matrixWidth),
                                                                      bBox.getMaxY() - row    * (bBox.getHeight()/matrixHeight),
                                                                      spherMercator.getCoordinateReferenceSystem());

            final CrsCoordinate crsCoordReturned = gpkg.tiles().tileToCrsCoordinate(gpkg.tiles().getTileSet(tileMatrix.getTableName()), column, row, zoomLevel);


            this.assertCoordinatesEqual(crsCoordReturned, crsCoordExpected);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if a tileCoordinate can be converted to the correct CRS Coordinate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test
    public void tileToCrsCoordinate3() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            final BoundingBox bBox         =  new BoundingBox(-22.1258, -15.325, 43.125, 78.248);
            final int         row          = 2;
            final int         column       = 7;
            final int         zoomLevel    = 4;
            final int         matrixWidth  = 13;
            final int         matrixHeight = 8;
            final TileMatrix  tileMatrix   = createTileSetAndTileMatrix(gpkg, bBox, zoomLevel, matrixWidth, matrixHeight);

            final CrsCoordinate crsCoordReturned = gpkg.tiles().tileToCrsCoordinate(gpkg.tiles().getTileSet(tileMatrix.getTableName()), column, row, zoomLevel);
            final CrsCoordinate crsCoordExpected = new CrsCoordinate(bBox.getMinX() + column*(bBox.getWidth()/matrixWidth),
                                                                     bBox.getMaxY() - row*  (bBox.getHeight()/matrixHeight),
                                                                     new GlobalGeodeticCrsProfile().getCoordinateReferenceSystem());

            this.assertCoordinatesEqual(crsCoordReturned, crsCoordExpected);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if an IllegalArgumentException when appropriate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            gpkg.tiles().tileToCrsCoordinate(null, 0, 0, 0);
            fail("Expected an IllegalArgumentException when giving a null value for tileSet");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if an IllegalArgumentException when appropriate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                     "identifier",
                                                     "description",
                                                     new BoundingBox(0.0,0.0,0.0,0.0),
                                                     gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            gpkg.tiles().tileToCrsCoordinate(tileSet, -1, 0, 0);
            fail("Expected an IllegalArgumentException when giving a negative value for column");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if an IllegalArgumentException when appropriate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException3() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                     "identifier",
                                                     "description",
                                                     new BoundingBox(0.0,0.0,0.0,0.0),
                                                     gpkg.core().getSpatialReferenceSystem("EPSG", 4326));
            gpkg.tiles().tileToCrsCoordinate(tileSet, 0, -1, 0);
            fail("Expected an IllegalArgumentException when giving a negative value for row");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if an IllegalArgumentException when appropriate
     * @throws ClassNotFoundException throws
     * @throws SQLException throws
     * @throws ConformanceException throws
     * @throws IOException throws
     */
    @Test(expected = IllegalArgumentException.class)
    public void tileToCrsCoordinateIllegalArgumentException4() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final TileSet tileSet = gpkg.tiles().addTileSet("tableName",
                                                     "identifier",
                                                     "description",
                                                     new BoundingBox(0.0,0.0,0.0,0.0),
                                                     gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

            gpkg.tiles().tileToCrsCoordinate(tileSet, 0, 0, 0);
            fail("Expected an IllegalArgumentException when giving a zoom that does not have a Tile Matrix associated with it.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    private void assertCoordinatesEqual(final CrsCoordinate crsCoordReturned, final CrsCoordinate crsCoordExpected)
    {
        assertEquals(String.format(Locale.getDefault(),
                                   "The coordinate returned was not the values expected.\nActual Coordinate: (%f, %f) Crs: %s %d\nReturned Coordinate: (%f, %f) Crs: %s %d",
                                   crsCoordReturned.getX(),
                                   crsCoordReturned.getY(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getAuthority(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getIdentifier(),
                                   crsCoordExpected.getX(),
                                   crsCoordReturned.getY(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getAuthority(),
                                   crsCoordReturned.getCoordinateReferenceSystem().getIdentifier()),
                      crsCoordExpected,
                      crsCoordReturned);
    }

    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight) throws SQLException
    {
        return createTileSetAndTileMatrix(gpkg, gpkg.core().getSpatialReferenceSystem("EPSG", 4326), bBox, zoomLevel, matrixWidth, matrixHeight, 256, 256, "tableName");
    }

    private static TileMatrix createTileSetAndTileMatrix(final GeoPackage gpkg, final SpatialReferenceSystem srs, final BoundingBox bBox, final int zoomLevel, final int matrixWidth, final int matrixHeight, final int tileWidth, final int tileHeight, final String identifierTableName) throws SQLException
    {
        //create a tileSet
        final TileSet tileSet = gpkg.tiles()
                                    .addTileSet(identifierTableName,
                                                identifierTableName,
                                                "description",
                                                bBox,
                                                srs);
        //create matrix
        return  gpkg.tiles().addTileMatrix(tileSet,
                                           zoomLevel,
                                           matrixWidth,
                                           matrixHeight,
                                           tileWidth,
                                           tileHeight,
                                           bBox.getWidth()  / matrixWidth / tileWidth,
                                           bBox.getHeight() / matrixHeight / tileHeight);
    }

    private static byte[] createImageBytes() throws IOException
    {
        //return ImageUtility.bufferedImageToBytes(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB), "PNG");

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try
        {
            Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                  .compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            final byte[] bytes = byteArrayOutputStream.toByteArray();
            return bytes;
        }
        finally
        {
            byteArrayOutputStream.close();
        }
    }
}
