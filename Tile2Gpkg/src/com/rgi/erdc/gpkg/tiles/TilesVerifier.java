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

package com.rgi.erdc.gpkg.tiles;

import static com.rgi.erdc.gpkg.verification.Assert.assertTrue;
import static com.rgi.erdc.gpkg.verification.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.rgi.erdc.gpkg.DatabaseUtility;
import com.rgi.erdc.gpkg.verification.AssertionError;
import com.rgi.erdc.gpkg.verification.ColumnDefinition;
import com.rgi.erdc.gpkg.verification.ForeignKeyDefinition;
import com.rgi.erdc.gpkg.verification.Requirement;
import com.rgi.erdc.gpkg.verification.Severity;
import com.rgi.erdc.gpkg.verification.TableDefinition;
import com.rgi.erdc.gpkg.verification.Verifier;
import com.rgi.util.jdbc.ResultSetStream;

public class TilesVerifier extends Verifier
{
    public static final double EPSILON = 0.001;
    private Set<String> pyramidUserDataTables;

    public TilesVerifier(final Connection sqliteConnection)
    {
        super(sqliteConnection);

        final String query1 = "SELECT tbl_name FROM sqlite_master " +
                              "WHERE tbl_name NOT LIKE 'gpkg_%' "   +
                                             "AND (type = 'table' OR type = 'view');";
        try(Statement stmt1                  = this.getSqliteConnection().createStatement();
            ResultSet possiblePyramidTables  = stmt1.executeQuery(query1);)
        {
           this.pyramidUserDataTables = ResultSetStream.getStream(possiblePyramidTables)
                                                       .map(resultSet ->
                                                       {   try
                                                           {
                                                               final TableDefinition possiblePyramidTable = new TilePyramidUserDataTableDefinition(resultSet.getString("tbl_name"));
                                                               this.verifyTable(possiblePyramidTable);
                                                               return possiblePyramidTable.getName();
                                                           }
                                                           catch(final SQLException | AssertionError ex)
                                                           {
                                                               return null;
                                                           }
                                                       })
                                                       .filter(entry -> entry != null)
                                                       .collect(Collectors.toSet());
        }
        catch(SQLException ex)
        {
            this.pyramidUserDataTables = Collections.emptySet();
        }
    }

    /**Requirement 33
     * <blockquote>
     * The <code>gpkg_contents</code> table SHALL contain a row with
     * a <code>data_type</code> column value of “tiles” for each
     * tile pyramid user data table or view.
     * </blockquote>
     * @throws AssertionError
     */
    @Requirement(number = 33,
                 text = "The gpkg_contents table SHALL contain a row with a "
                         + "data_type column value of \"tiles\" for each "
                         + "tile pyramid user data table or view.",
                 severity = Severity.Error)
    public void Requirement33() throws AssertionError
    {
        final String query2 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
        try(Statement stmt2                  = this.getSqliteConnection().createStatement();
            ResultSet contentsPyramidTables  = stmt2.executeQuery(query2))

            {
            //collect the tiles table from the contents table
            final Set<String> tableNamesContents = ResultSetStream.getStream(contentsPyramidTables)
                                                                           .map(resultSet -> {try
                                                                           {
                                                                               return resultSet.getString("table_name");
                                                                           }
                                                                           catch(final SQLException ex)
                                                                           {
                                                                               return null;
                                                                           }

                                                                           })
                                                                          .filter(name -> name != null)
                                                                          .collect(Collectors.toSet());

            for(final String tableName: this.pyramidUserDataTables)
            {
                assertTrue(String.format("The Tile Pyramid User Data table that is not refrenced in gpkg_contents table is: %s.  "
                                       + "This table needs to be referenced in the gpkg_contents table.", tableName), tableNamesContents.contains(tableName));
            }
         }
    catch (SQLException ex)
    {
        fail(ex.getMessage());
    }
    }

    /**<div class="title">Requirement 34</div>
     *<blockquote>
     *In a GeoPackage that contains a tile pyramid user data table
     *that contains tile data, by default, zoom level pixel sizes
     *for that table SHALL vary by a factor of 2 between adjacent
     *zoom levels in the tile matrix metadata table.
     *</blockquote>
     *</div>
     * @throws AssertionError
     */
    @Requirement(number = 34,
                 text = "In a GeoPackage that contains a tile pyramid user data table"
                         + "that contains tile data, by default, zoom level pixel sizes for that"
                         + "table SHALL vary by a factor of 2 between zoom levels in tile matrix metadata table.",
                 severity = Severity.Error)
    public void Requirement34() throws AssertionError
    {
        //TODO: consider the possibility the tiles table is empty but there is a tile matrix table in gpkg
        if(this.TileMatrixTableExists())
        {
            for (String tableName : this.pyramidUserDataTables)
            {
                final String query1 = String.format("SELECT table_name, "
                                                         + "zoom_level, "
                                                         + "pixel_x_size, "
                                                         + "pixel_y_size "
                                                  + "FROM gpkg_tile_matrix "
                                                  + "WHERE table_name = '%s' "
                                                  + "ORDER BY zoom_level ASC;", tableName);

                try (Statement stmt = this.getSqliteConnection().createStatement(); ResultSet pixelInfo = stmt.executeQuery(query1))
                {

                    Double pixelX2    = null;
                    Double pixelY2    = null;
                    int zoomLevelNext = -1000;// arbitrary number to
                                              // initialize the variable

                    while (pixelInfo.next())
                    {
                        Double pixelX           = pixelInfo.getDouble("pixel_x_size");
                        Double pixelY           = pixelInfo.getDouble("pixel_y_size");
                        int    zoomLevelCurrent = pixelInfo.getInt("zoom_level");

                        if (pixelX2 != null && pixelY2 != null && zoomLevelCurrent == zoomLevelNext + 1)
                        {
                            assertTrue(String.format("Pixel sizes for tile matrix user data tables do not vary by factors of 2"
                                                       + " between adjacent zoom levels in the tile matrix metadata table: %s, %s",
                                                     pixelX.toString(), pixelY.toString()),
                                       equal((pixelX2 / 2), pixelX) && equal((pixelY2 / 2), pixelY));

                            pixelX2 = pixelX;
                            pixelY2 = pixelY;
                        }
                        else if (pixelInfo.next())
                        {
                            pixelX2       = pixelInfo.getDouble("pixel_x_size");
                            pixelY2       = pixelInfo.getDouble("pixel_y_size");
                            zoomLevelNext = pixelInfo.getInt("zoom_level");

                            if (zoomLevelCurrent + 1 == zoomLevelNext)
                            {

                                assertTrue(String.format("Pixel sizes for tile matrix user data tables do not vary by factors of 2 "
                                                          + "between adjacent zoom levels in the tile matrix metadata table: %s, %s.  For zoom levels %d and %d",
                                                          pixelX2.toString(), pixelY2.toString(), zoomLevelCurrent, zoomLevelNext),
                                          equal((pixelX / 2), pixelX2) && equal((pixelY / 2), pixelY2));
                            }
                        }
                    }
                }
                catch (SQLException e)
                {
                    fail(e.getMessage());
                }
            }
        }
    }
    /**
     * <div class="title">
     * Requirement 35
     * </div>
     * <blockquote>
     * In a GeoPackage that contains a tile pyramid user data table that
     * contains tile data that is not <a href="http://www.ietf.org/rfc/rfc2046.txt">MIME type</a>
     * <a href="http://www.jpeg.org/public/jfif.pdf">image/jpeg</a> {or image/png}, by default
     * SHALL store that tile data in <a href="http://www.iana.org/assignments/media-types/index.html">
     * MIME type</a> <a href="http://libpng.org/pub/png/">image/png</a> {or image/jpeg}.
     * </blockquote>
     * </div>
     * @throws AssertionError
     */
    //TODO: Image issues  >:(
    @Requirement(number = 35,
                 text = "In a GeoPackage that contains a tile pyramid user data table that "
                         + "contains tile data that is not MIME type image/{jpeg or png}, "
                         + "by default SHALL store that tile data in MIME type image/{png or jpeg}",
                 severity = Severity.Error)
    public void Requirement35() throws AssertionError
    {
            for (String tableName : this.pyramidUserDataTables)
            {

                final String selectTileDataQuery = String.format("SELECT tile_data FROM %s;", tableName);

                try (Statement stmt              = this.getSqliteConnection().createStatement();
                     ResultSet tileDataResultSet = stmt.executeQuery(selectTileDataQuery))
                {
                    //TODO: working on it

//                    final Set<MemoryCacheImageInputStream> allTileData = ResultSetStream.getStream(tileDataResultSet)
//                                                                   .map(resultSet ->
//                                                                                   {
//                                                                                       try
//                                                                                       {
//                                                                                           byte[] tileData = resultSet.getBytes("tile_data");
//                                                                                           return new MemoryCacheImageInputStream(new ByteArrayInputStream(tileData));
//                                                                                       }
//                                                                                       catch (final SQLException ex1)
//                                                                                       {
//                                                                                           return null;
//                                                                                       }
//                                                                                    })
//                                                                                    .filter(entry -> entry != null)
//                                                                                    .collect(Collectors.toSet());
//                        for(MemoryCacheImageInputStream image: allTileData)
//                        {
//                             ImageIO.getImageReaders(image);
//
//                            final Iterator<ImageReader> jpegImageReader = ImageIO.getImageReadersByMIMEType("image/jpeg");
//                            final Iterator<ImageReader> pngImageReader  = ImageIO.getImageReadersByMIMEType("image/png");
//                             while(jpegImageReader.hasNext())
//                             {
//                                 jpegImageReader.next().setInput(allTileData);
//                                 //StreamSupport.stream(Spliterators.spliteratorUnknownSize(jpegImageReader, Spliterator.ORDERED), false).collect(Collectors.toCollection());
//
//
//                             }
//
//                                //final String imageType = iterator.next().getFormatName();
//                               // assertTrue(String.format("The Tile matrix user data table that contains tile data that is not MIME type image/jpeg by default "
//                                 //                      + "does not contain tile data in MIME type image/png. Bad Image Type Detected: %s", imageType),
//                                   //                       imageType.toLowerCase().equals("png") || imageType.toLowerCase().equals("jpeg"));
//                        }

                    }
                catch (SQLException ex)
                {
                    fail(ex.getMessage());
                }
                }

    }
    /**
    * <div class="title">
    * Requirement 36
    * </div>
    * <blockquote>
    * In a GeoPackage that contains a tile pyramid user data table
    * that contains tile data that is not <a href="http://www.iana.org/assignments/media-types/index.html">
    * MIME type</a><a href="http://libpng.org/pub/png/"> image/png</a>,
    *  by default SHALL store that tile data in <a href="http://www.ietf.org/rfc/rfc2046.txt">
    *  MIME type</a> <a href="http://www.jpeg.org/public/jfif.pdf">image/jpeg</a>.
    * </blockquote>
    * </div>
    */
    @Requirement(number = 36,
                 text = "In a GeoPackage that contains a tile pyramid user data table that "
                         + "contains tile data that is not MIME type image png, "
                         + "by default SHALL store that tile data in MIME type image jpeg",
                 severity = Severity.Warning)
    public void Requirement36()
    {
        // This requirement is tested through Requirement 35 test in TilesVerifier.
    }

    /**
     * <div class="title">Requirement 37</div>
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain
     * <code>gpkg_tile_matrix_set</code> table or view per <a href="http://www.geopackage.org/spec/#tile_matrix_set_data_table_definition">Table Definition</a>,
     *  <a href="http://www.geopackage.org/spec/#gpkg_tile_matrix_set_cols">Tile Matrix Set Table or View Definition</a> and
     *  <a href="http://www.geopackage.org/spec/#gpkg_tile_matrix_set_sql">gpkg_tile_matrix_set Table Creation SQL</a>.
     * </blockquote>
     * </div>
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 37, text = "A GeoPackage that contains a tile pyramid user data table SHALL "
                                   + "contain gpkg_tile_matrix_set table or view per Table Definition, "
                                   + "Tile Matrix Set Table or View Definition and gpkg_tile_matrix_set Table Creation SQL. ", severity = Severity.Error)
    public void Requirement37() throws AssertionError, SQLException
    {
        this.verifyTable(TilesVerifier.TileMatrixSetTableDefinition);
    }

    /**
     * <div class="title">Requirement 38</div>
     * <blockquote>
     *  Values of the <code>gpkg_tile_matrix_set</code> <code>table_name</code>
     *   column SHALL reference values in the gpkg_contents table_name column
     *   for rows with a data type of "tiles".
     * </blockquote>
     * </div>
     * @throws AssertionError
     */
    @Requirement(number  = 38,
                 text    = "Values of the gpkg_tile_matrix_set table_name column "
                            + "SHALL reference values in the gpkg_contents table_name "
                            + "column for rows with a data type of \"tiles\".",
                severity = Severity.Error)
    public void Requirement38() throws AssertionError
    {
        if(this.TileMatrixSetTableExists())
        {
            final String query1 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
            final String query2 = "SELECT table_name FROM gpkg_tile_matrix_set;";

            try (Statement stmt                     = this.getSqliteConnection().createStatement();
                 ResultSet tileTablesInTileMatrixSet = stmt.executeQuery(query1);

                 Statement stmt2                       = this.getSqliteConnection().createStatement();
                 ResultSet pyramidUserTablesInContents = stmt2.executeQuery(query2))
            {
                final Set<String> tileMatrixSetTables = ResultSetStream.getStream(tileTablesInTileMatrixSet)
                                                                       .map(resultSet ->
                                                                                       {
                                                                                           try
                                                                                           {
                                                                                               return resultSet.getString("table_name");
                                                                                           }
                                                                                           catch (final SQLException ex1)
                                                                                           {
                                                                                               return null;
                                                                                           }
                                                                                        })
                                                                                        .filter(entry -> entry != null)
                                                                                        .collect(Collectors.toSet());

              final Set<String> pyramidTablesInContents = ResultSetStream.getStream(pyramidUserTablesInContents)
                                                                         .map(resultSet ->
                                                                         {
                                                                             try
                                                                             {
                                                                                 return resultSet.getString("table_name");
                                                                             }
                                                                             catch (final SQLException ex1)
                                                                             {
                                                                                 return null;
                                                                             }
                                                                          })
                                                                          .filter(entry -> entry != null)
                                                                          .collect(Collectors.toSet());

              assertTrue("There are table_name values in the gpkg_tile_matrix_set that are not in the gpkg_contents table.",
                         pyramidTablesInContents.containsAll(tileMatrixSetTables));


            }
            catch (final Exception ex)
            {
                fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 39</div>
     * <blockquote>
     * The gpkg_tile_matrix_set table or view SHALL contain one row record for each tile pyramid user data table.
     * </blockquote>
     * </div>
     * <b>Note:</b>
     * <div class = "Note">
     * <blockquote>
     * If Requirement 33 fails this Requirement could produce a false positive.  This Requirement depends
     * on Requirement 33 to be correct to be an accurate assessment of passing or failing Requirement 39.
     * </blockquote>
     * </div>
     * @throws AssertionError
     *
     */
    @Requirement(number   = 39,
                 text     = "The gpkg_tile_matrix_set table or view SHALL "
                            + "contain one row record for each tile "
                            + "pyramid user data table. ",
                 severity = Severity.Error)
    public void Requirement39() throws AssertionError
    {
      //TODO consider testing against the ALLpyramid user data tables field variable
        //because this only checks against the ones in the gpkg contents table
        if (this.TileMatrixSetTableExists() && !this.pyramidUserDataTables.isEmpty())
        {
            final String query2 = String.format("SELECT  table_name FROM gpkg_contents        AS gc   WHERE  table_name NOT IN  "
                                             + "(SELECT table_name FROM gpkg_tile_matrix_set  AS tms  WHERE  tms.table_name = gc.table_name "
                                                                                                    + " AND  gc.data_type   = 'tiles' )");

            try (Statement stmt2             = this.getSqliteConnection().createStatement();
                 ResultSet unreferencedTable = stmt2.executeQuery(query2))
            {
                if (unreferencedTable.next())
                {
                    fail(String.format("The gpkg_tile_matrix_set table does not contains a row record "
                                       + "for the tile pyramid user data table: %s",
                                       unreferencedTable.getString("table_name")));
                }
            }
            catch (final Exception ex)
            {
                fail(ex.getMessage());
            }
        }

    }

    /**
     * <div class="title">Requirement 40</div>
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix_set </code>  <code> srs_id</code> column
     *  SHALL reference values in the <code>gpkg_spatial_ref_sys </code>  <code> srs_id</code> column.
     * </blockquote>
     * </div>
     * @throws AssertionError
     * @throws
     */
    @Requirement (number   = 40,
                  text     = "Values of the gpkg_tile_matrix_set srs_id column "
                              + "SHALL reference values in the gpkg_spatial_ref_sys srs_id column. ",
                  severity = Severity.Error)
    public void Requirement40() throws AssertionError
    {
        if(this.TileMatrixSetTableExists())
        {
            final String query1 = "SELECT srs_id from gpkg_tile_matrix_set AS tms " +
                                   "WHERE srs_id NOT IN" +
                                                 "(SELECT srs_id " +
                                                  "FROM gpkg_spatial_ref_sys);";

            try (Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet unreferencedsrs = stmt.executeQuery(query1))
            {
                if (unreferencedsrs.next())
                {
                    fail(String.format("The gpkg_tile_matrix_set table contains a reference to an srs_id that is not defined in the gpkg_spatial_ref_sys Table. "
                                        + "Unreferenced srs_id: %s",
                                       unreferencedsrs.getInt("srs_id")));
                }
            }
            catch (final Exception ex)
            {
                fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 41</div>
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain a
     * <code>gpkg_tile_matrix</code> table or view per clause 2.2.7.1.1
     * <a href="http://www.geopackage.org/spec/#tile_matrix_data_table_definition">Table Definition</a>, Table
     * <a href="http://www.geopackage.org/spec/#gpkg_tile_matrix_cols">Tile Matrix Metadata Table or View Definition</a>
     * and Table <a href="http://www.geopackage.org/spec/#gpkg_tile_matrix_sql">gpkg_tile_matrix Table Creation SQL</a>.
     * </blockquote>
     * </div>
     * @throws AssertionError
     * @throws SQLException
     */
    @Requirement (number  = 41,
                  text    = "A GeoPackage that contains a tile pyramid user data table "
                            + "SHALL contain a gpkg_tile_matrix table or view per clause "
                            + "2.2.7.1.1 Table Definition, Table Tile Matrix Metadata Table "
                            + "or View Definition and Table gpkg_tile_matrix Table Creation SQL. ",
                 severity = Severity.Error)
    public void Requirement41() throws AssertionError, SQLException
    {
        this.verifyTable(TilesVerifier.TileMatrixTableDefinition);
    }

    /**
     * <div class="title">Requirement 42</div>
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix</code>
     * <code>table_name</code> column SHALL reference
     * values in the <code>gpkg_contents</code> <code>
     * table_name</code> column for rows with a <code>
     * data_type</code> of “tiles”.
     * </blockquote>
     * </div>
     * @throws AssertionError
     */
    @Requirement (number    = 42,
                  text      = "Values of the gpkg_tile_matrix table_name column "
                              + "SHALL reference values in the gpkg_contents table_name "
                              + "column for rows with a data_type of “tiles”. ",
                 severity   = Severity.Error)
    public void Requirement42() throws AssertionError
    {
        if(this.TileMatrixTableExists() && !this.pyramidUserDataTables.isEmpty())
        {
            final String query2 = String.format("SELECT table_name FROM gpkg_tile_matrix AS tm " +
                                                "WHERE table_name NOT IN"                        +
                                                                   "(SELECT table_name  "        +
                                                                    "FROM gpkg_contents AS gc "  +
                                                                    "WHERE tm.table_name = gc.table_name AND gc.data_type = 'tiles');");

            try(Statement stmt2              = this.getSqliteConnection().createStatement();
                ResultSet unreferencedTables = stmt2.executeQuery(query2))
            {
                    if (unreferencedTables.next())
                    {
                        fail(String.format("There are Pyramid user data tables in gpkg_tile_matrix table_name field such that the table_name does not"
                                           +  " reference values in the gpkg_contents table_name column for rows with a data type of 'tiles'."
                                           +  " Unreferenced table: %s",
                                           unreferencedTables.getString("table_name")));
                    }
            }
            catch (SQLException ex)
            {
                fail(ex.getMessage());
            }
        }
    }


    /**
     * <div class="title">Requirement 43</div>
     * <blockquote>
     * The <code>gpkg_tile_matrix</code> table or view SHALL contain one row record for
     * each zoom level that contains one or more tiles in each tile pyramid user data table or view.
     * </blockquote>
     * </div>
     * @throws AssertionError
     */
    @Requirement (number    = 43,
                  text      = "The gpkg_tile_matrix table or view SHALL contain "
                              + "one row record for each zoom level that contains "
                              + "one or more tiles in each tile pyramid user data table or view. ",
                  severity  = Severity.Error)
    public void Requirement43() throws AssertionError
    {
        if(this.TileMatrixTableExists())
        {
            for (String tableName : this.pyramidUserDataTables)
            {
                final String query1      = String.format("SELECT DISTINCT zoom_level FROM gpkg_tile_matrix ORDER BY ASC WHERE table_name ='%s'", tableName);
                final String query2      = String.format("SELECT DISTINCT zoom_level FROM %s               ORDER BY ASC", tableName);

                try (Statement stmt1                     = this.getSqliteConnection().createStatement();
                     ResultSet gm_zoomLevels             = stmt1.executeQuery(query1);

                     Statement stmt2                     = this.getSqliteConnection().createStatement();
                     ResultSet py_zoomLevels             = stmt2.executeQuery(query2))
                {
                    Set<Integer> tileMatrixZooms = ResultSetStream.getStream(gm_zoomLevels)
                                                                  .map(resultSet ->
                                                                  {   try
                                                                      {
                                                                          return resultSet.getInt("zoom_level");
                                                                      }
                                                                      catch(final SQLException ex)
                                                                      {
                                                                          return null;
                                                                      }
                                                                  })
                                                                  .filter(entry -> entry != null)
                                                                  .collect(Collectors.toSet());

                    Set<Integer> tilePyramidZooms = ResultSetStream.getStream(gm_zoomLevels)
                                                                    .map(resultSet ->
                                                                    {   try
                                                                        {
                                                                            return resultSet.getInt("zoom_level");
                                                                        }
                                                                        catch(final SQLException ex)
                                                                        {
                                                                            return null;
                                                                        }
                                                                    })
                                                                    .filter(entry -> entry != null)
                                                                    .collect(Collectors.toSet());
                    assertTrue(String.format("The gpkg_tile_matrix does not contain a row record for every zoom level in the Pyramid User Data Table %s.",
                                             tableName),
                               tileMatrixZooms.containsAll(tilePyramidZooms));
                }
                catch (final Exception ex)
                {
                    fail(ex.getMessage());
                }
            }
        }
    }


    /**
     * This method determines if the two doubles are
     * equal based upon the maximum level of allowable
     * differenced determined by the Epsilon value 0.001
     * @param first
     * @param second
     * @return
     */
    private static boolean equal(double first, double second)
    {
        return Math.abs(first - second) < TilesVerifier.EPSILON;
    }

    /**
     * This Verifies if the Tile Matrix Table exists.
     * @return true if the gpkg_tile_matrix table exists
     * @throws AssertionError throws an assertion error if the gpkg_tile_matrix table
     *  doesn't exist and the GeoPackage contains a tiles table
     */
    private boolean TileMatrixTableExists() throws AssertionError
    {
        try
        {
            if (!this.pyramidUserDataTables.isEmpty() && !DatabaseUtility.tableOrViewExists(getSqliteConnection(), GeoPackageTiles.MatrixTableName))
            {
                throw new AssertionError(String.format("Every GeoPackage with a Pyramid User Data Table must have the %s table to be a valid GeoPackage.",
                                                       GeoPackageTiles.MatrixTableName));
            }

            return DatabaseUtility.tableOrViewExists(getSqliteConnection(), GeoPackageTiles.MatrixTableName);
        }
        catch (SQLException ex)
        {
            fail(ex.getMessage());
        }
        throw new AssertionError("Was Unable to verify if there was a Tile Matrix Table.");
    }

    /**
     * This Verifies if the Tile Matrix Set Table exists in the
     * GeoPackage.
     * @return true if the gpkg_tile_matrix Set table exists
     * @throws AssertionError throws an assertion error if the gpkg_tile_matrix_set table
     *  doesn't exist and the GeoPackage contains a tiles table
     * @throws SQLException
     */
    private boolean TileMatrixSetTableExists() throws AssertionError
    {
        try
        {
            if (!this.pyramidUserDataTables.isEmpty() && !DatabaseUtility.tableOrViewExists(getSqliteConnection(), GeoPackageTiles.MatrixSetTableName))
            {
                throw new AssertionError(String.format("Every GeoPackage with a Pyramid User Data Table must have the %s table to be a valid GeoPackage.",
                                                       GeoPackageTiles.MatrixSetTableName));
            }
            return DatabaseUtility.tableOrViewExists(getSqliteConnection(), GeoPackageTiles.MatrixSetTableName);
        }
        catch (SQLException ex)
        {
            fail(ex.getMessage());
        }
        throw new AssertionError("Was Unable to verify if there was a Tile Matrix Set Table.");
    }

    private static final TableDefinition TileMatrixSetTableDefinition;
    private static final TableDefinition TileMatrixTableDefinition;

    static
    {
        final Map<String, ColumnDefinition> tileMatrixSetColumns = new HashMap<>();

        tileMatrixSetColumns.put("table_name",  new ColumnDefinition("TEXT",     true, true,  true,  null));
        tileMatrixSetColumns.put("srs_id",      new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixSetColumns.put("min_x",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("min_y",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("max_x",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("max_y",       new ColumnDefinition("DOUBLE",   true, false, false, null));

        TileMatrixSetTableDefinition = new TableDefinition("gpkg_tile_matrix_set",
                                                     tileMatrixSetColumns,
                                                     new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_spatial_ref_sys", "srs_id", "srs_id"),
                                                                                 new ForeignKeyDefinition("gpkg_contents", "table_name", "table_name"))));
        final Map<String, ColumnDefinition> tileMatrixColumns = new HashMap<>();

        tileMatrixColumns.put("table_name",     new ColumnDefinition("TEXT",     true, true,  true,  null));
        tileMatrixColumns.put("zoom_level",     new ColumnDefinition("INTEGER",  true, true,  true,  null));
        tileMatrixColumns.put("matrix_width",   new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("matrix_height",  new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("tile_width",     new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("tile_height",    new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("pixel_x_size",   new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixColumns.put("pixel_y_size",   new ColumnDefinition("DOUBLE",   true, false, false, null));

        TileMatrixTableDefinition = new TableDefinition("gpkg_tile_matrix",
                                                        tileMatrixColumns,
                                                        new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_contents", "table_name", "table_name"))));

    }

}
