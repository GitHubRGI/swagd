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

package com.rgi.geopackage.tiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.verification.Assert;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.Verifier;

/**
 * @author Jenifer Cochran
 *
 */
public class TilesVerifier extends Verifier
{
    private class TileData implements Comparable<TileData>
    {
        int     matrixWidth;
        int     matrixHeight;
        int     zoomLevel;
        Integer tileID;
        int     tileRow;
        int     tileColumn;
        double  pixelXSize;
        double  pixelYSize;
        int     tileWidth;
        int     tileHeight;

        public String columnInvalidToString()
        {
            return String.format("      Tile id: %d, column: %2d (max: %d)", this.tileID, this.tileColumn, this.matrixWidth-1);
        }

        public String rowInvalidToString()
        {
            return String.format("      Tile id: %d, row: %2d (max: %d)", this.tileID, this.tileRow, this.matrixHeight-1);
        }

        @Override
        public int compareTo(final TileData other)
        {
           return this.tileID.compareTo(other.tileID);
        }
    }
    /**
     * this Epsilon is the greatest difference we allow
     * when comparing doubles
     */
    public static final double EPSILON = 0.0001;
    private Set<String> allPyramidUserDataTables;
    private Set<String> pyramidTablesInContents;
    private Set<String> pyramidTablesInTileMatrix;
    private boolean hasTileMatrixTable;
    private boolean hasTileMatrixSetTable;

    /**
     * @param sqliteConnection
     *            the connection to the database
     * @throws SQLException
     *             throws if the method {@link DatabaseUtility#tableOrViewExists(Connection, String) tableOrViewExists} throws
     */
    public TilesVerifier(final Connection sqliteConnection) throws SQLException
    {
        super(sqliteConnection);

        final String queryTables = "SELECT tbl_name FROM sqlite_master " +
                                   "WHERE tbl_name NOT LIKE 'gpkg_%' "   +
                                                      "AND (type = 'table' OR type = 'view');";
        try(Statement createStmt             = this.getSqliteConnection().createStatement();
            ResultSet possiblePyramidTables  = createStmt.executeQuery(queryTables);)
        {
           this.allPyramidUserDataTables = ResultSetStream.getStream(possiblePyramidTables)
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
                                                          .filter(Objects::nonNull)
                                                          .collect(Collectors.toSet());
        }
        catch(final SQLException ex)
        {
            this.allPyramidUserDataTables = Collections.emptySet();
        }

        final String query2 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
        try(Statement createStmt2       = this.getSqliteConnection().createStatement();
            ResultSet contentsPyramidTables = createStmt2.executeQuery(query2))
        {
        this.pyramidTablesInContents = ResultSetStream.getStream(contentsPyramidTables)
                                                      .map(resultSet -> {  try
                                                                             {
                                                                                return resultSet.getString("table_name");
                                                                             }
                                                                             catch(final SQLException ex)
                                                                             {
                                                                                 return null;
                                                                             }
                                                                          })
                                                      .filter(Objects::nonNull)
                                                      .collect(Collectors.toSet());
        }
        catch(final SQLException ex)
        {
            this.pyramidTablesInContents = Collections.emptySet();
        }

        final String query3 = "SELECT DISTINCT table_name FROM gpkg_tile_matrix;";

        try(Statement createStmt3             = this.getSqliteConnection().createStatement();
            ResultSet tileMatrixPyramidTables = createStmt3.executeQuery(query3))
        {
        this.pyramidTablesInTileMatrix = ResultSetStream.getStream(tileMatrixPyramidTables)
                                                        .map(resultSet -> {  try
                                                                                 {
                                                                                   final String pyramidName = resultSet.getString("table_name");
                                                                                   return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(),
                                                                                                                            pyramidName) ? pyramidName
                                                                                                                                         : null;
                                                                             }
                                                                             catch(final SQLException ex)
                                                                             {
                                                                                 return null;
                                                                             }
                                                                          })
                                                         .filter(Objects::nonNull)
                                                         .collect(Collectors.toSet());
        }
        catch(final SQLException ex)
        {
            this.pyramidTablesInTileMatrix = Collections.emptySet();
        }

        this.hasTileMatrixSetTable = this.tileMatrixSetTableExists();
        this.hasTileMatrixTable    = this.tileMatrixTableExists();
    }

    /**Requirement 33
     * <blockquote>
     * The <code>gpkg_contents</code> table SHALL contain a row with
     * a <code>data_type</code> column value of 'tiles' for each
     * tile pyramid user data table or view.
     * </blockquote>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(number   = 33,
                 text     = "The gpkg_contents table SHALL contain a row with a "
                             + "data_type column value of \"tiles\" for each "
                             + "tile pyramid user data table or view.",
                 severity = Severity.Warning)
    public void Requirement33() throws AssertionError
    {
       for(final String tableName: this.allPyramidUserDataTables)
       {
           Assert.assertTrue(String.format("The Tile Pyramid User Data table that is not refrenced in gpkg_contents table is: %s.  "
                                  + "This table needs to be referenced in the gpkg_contents table.", tableName),
                      this.pyramidTablesInContents.contains(tableName));
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
     * @throws AssertionError  throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement(number = 34,
                 text = "In a GeoPackage that contains a tile pyramid user data table"
                         + "that contains tile data, by default, zoom level pixel sizes for that "
                         + "table SHALL vary by a factor of 2 between zoom levels in tile matrix metadata table.",
                 severity = Severity.Warning)
    public void Requirement34() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for (final String tableName : this.allPyramidUserDataTables)
            {
                final String query1 = String.format("SELECT table_name, "
                                                         + "zoom_level, "
                                                         + "pixel_x_size, "
                                                         + "pixel_y_size,"
                                                         + "matrix_width,"
                                                         + "matrix_height,"
                                                         + "tile_width,"
                                                         + "tile_height "
                                                  + "FROM gpkg_tile_matrix "
                                                  + "WHERE table_name = '%s' "
                                                  + "ORDER BY zoom_level ASC;", tableName);

                try (Statement stmt      = this.getSqliteConnection().createStatement();
                     ResultSet pixelInfo = stmt.executeQuery(query1))
                {

                    final List<TileData> tileDataSet = ResultSetStream.getStream(pixelInfo)
                                                                .map(resultSet -> { try
                                                                                    {
                                                                                       final TileData tileData = new TileData();
                                                                                       tileData.pixelXSize   = resultSet.getDouble("pixel_x_size");
                                                                                       tileData.pixelYSize   = resultSet.getDouble("pixel_y_size");
                                                                                       tileData.zoomLevel    = resultSet.getInt("zoom_level");
                                                                                       tileData.matrixHeight = resultSet.getInt("matrix_height");
                                                                                       tileData.matrixWidth  = resultSet.getInt("matrix_width");
                                                                                       tileData.tileHeight   = resultSet.getInt("tile_height");
                                                                                       tileData.tileWidth    = resultSet.getInt("tile_width");

                                                                                       return tileData;
                                                                                    }
                                                                                    catch(final SQLException ex)
                                                                                    {
                                                                                        return null;
                                                                                    }
                                                                                  })
                                                               .filter(Objects::nonNull)
                                                               .collect(Collectors.toList());


                    for(int index = 0; index < tileDataSet.size()-1; ++index)
                    {
                        final TileData current = tileDataSet.get(index);
                        final TileData next    = tileDataSet.get(index + 1);

                        if(current.zoomLevel == next.zoomLevel - 1)
                        {
                            Assert.assertTrue(String.format("Pixel sizes for tile matrix user data tables do not vary by factors of 2"
                                                            + " between adjacent zoom levels in the tile matrix metadata table: %f, %f",
                                                            next.pixelXSize,
                                                            next.pixelYSize),
                                              TilesVerifier.isEqual((current.pixelXSize / 2.0), next.pixelXSize) &&
                                              TilesVerifier.isEqual((current.pixelYSize / 2.0), next.pixelYSize));
                        }
                    }

                    //This tests if the pixel x values and pixel y values are valid based on their bounding box
                    //in the tile matrix set
                    if(this.hasTileMatrixSetTable)
                    {
                        final String query2 =String.format("SELECT min_x, min_y, max_x, max_y FROM %s WHERE table_name = '%s'", GeoPackageTiles.MatrixSetTableName, tableName);

                        try(Statement stmt2         = this.getSqliteConnection().createStatement();
                            ResultSet boundingBoxRS = stmt2.executeQuery(query2))
                        {
                            if(boundingBoxRS.next())
                            {
                                final double minX = boundingBoxRS.getDouble("min_x");
                                final double minY = boundingBoxRS.getDouble("min_y");
                                final double maxX = boundingBoxRS.getDouble("max_x");
                                final double maxY = boundingBoxRS.getDouble("max_y");

                                final BoundingBox boundingBox = new BoundingBox(minX, minY, maxX, maxY);

                                final List<TileData> invalidPixelValues = tileDataSet.stream()
                                                                               .filter(tileData -> !validPixelValues(tileData, boundingBox))
                                                                               .collect(Collectors.toList());

                                Assert.assertTrue(String.format("Based on the bounding box given in the Tile Matrix Set for table %s, "
                                                                           + "tile_height, "
                                                                           + "tile_width, "
                                                                           + "matrix_width, and "
                                                                           + "matrix_height, "
                                                                + "the following pixel values are invalid.\n%s",
                                                                tableName,
                                                                invalidPixelValues.stream()
                                                                                  .map(tileData -> String.format("Invalid pixel_x_size: %f, Invalid pixel_y_size: %f at zoom_level %d",
                                                                                                                 tileData.pixelXSize,
                                                                                                                 tileData.pixelYSize,
                                                                                                                 tileData.zoomLevel))
                                                                                  .collect(Collectors.joining("\n"))),
                                                  invalidPixelValues.isEmpty());
                            }
                        }
                    }
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
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(number   = 35,
                 text     = "In a GeoPackage that contains a tile pyramid user data table that "
                             + "contains tile data that is not MIME type image/{jpeg or png}, "
                             + "by default SHALL store that tile data in MIME type image/{png or jpeg}",
                 severity = Severity.Warning)
    public void Requirement35() throws AssertionError
    {
        final Collection<ImageReader> jpegImageReaders = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/jpeg"));
        final Collection<ImageReader> pngImageReaders  = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/png"));

        for (final String tableName : this.allPyramidUserDataTables)
        {
            final String selectTileDataQuery = String.format("SELECT tile_data, id FROM %s;", tableName);

            try (Statement stmt              = this.getSqliteConnection().createStatement();
                 ResultSet tileDataResultSet = stmt.executeQuery(selectTileDataQuery))
            {

                final Map<Integer, byte[]> allTileData =  ResultSetStream.getStream(tileDataResultSet)
                                                                         .map(resultSet -> { try
                                                                                             {
                                                                                                 final int    tileId   = resultSet.getInt("id");
                                                                                                 final byte[] tileData = resultSet.getBytes("tile_data");
                                                                                                 return new AbstractMap.SimpleImmutableEntry<>(tileId, tileData);
                                                                                             }
                                                                                             catch (final Exception ex1)
                                                                                             {
                                                                                                 return null;
                                                                                             }
                                                                                           })
                                                                         .filter(Objects::nonNull)
                                                                         .collect(Collectors.toMap(entry -> entry.getKey(),
                                                                                                   entry -> entry.getValue()));


                for(final Integer imageID : allTileData.keySet())
                {
                    try(ByteArrayInputStream        byteArray  = new ByteArrayInputStream(allTileData.get(imageID));
                        MemoryCacheImageInputStream cacheImage = new MemoryCacheImageInputStream(byteArray))
                    {
                       Assert.assertTrue(String.format("The tile id: %d in the table: %s is not in the correct format.  The image must be of MIME type image/jpeg or image/png.",
                                                       imageID,
                                                       tableName),
                                         TilesVerifier.canReadImage(pngImageReaders, cacheImage) ||
                                         TilesVerifier.canReadImage(jpegImageReaders, cacheImage));
                    }
                    catch(final IOException ex)
                    {
                        Assert.fail(ex.getMessage());
                    }
                }
            }
            catch (final SQLException ex)
            {
                Assert.fail(ex.getMessage());
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
    @Requirement(number   = 36,
                 text     = "In a GeoPackage that contains a tile pyramid user data table that "
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
     * @throws SQLException throws if an SQLException occurs
     * @throws AssertionError  throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(number  = 37,
                 text    = "A GeoPackage that contains a tile pyramid user data table SHALL "
                            + "contain gpkg_tile_matrix_set table or view per Table Definition, "
                            + "Tile Matrix Set Table or View Definition and gpkg_tile_matrix_set Table Creation SQL. ",
                severity = Severity.Error)
    public void Requirement37() throws AssertionError, SQLException
    {
        if(!this.allPyramidUserDataTables.isEmpty())
        {
            Assert.assertTrue("The GeoPackage does not contain a gpkg_tile_matrix_set table. Every GeoPackage with a Pyramid User "
                                + "Data Table must also have a gpkg_tile_matrix_set table.",
                              this.hasTileMatrixSetTable);

            this.verifyTable(TilesVerifier.TileMatrixSetTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 38</div>
     * <blockquote>
     *  Values of the <code>gpkg_tile_matrix_set</code> <code>table_name</code>
     *   column SHALL reference values in the gpkg_contents table_name column
     *   for rows with a data type of "tiles".
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(number  = 38,
                 text    = "Values of the gpkg_tile_matrix_set table_name column "
                            + "SHALL reference values in the gpkg_contents table_name "
                            + "column for rows with a data type of \"tiles\".",
                severity = Severity.Warning)
    public void Requirement38() throws AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String queryMatrixSetPyramid = "SELECT table_name FROM gpkg_tile_matrix_set;";

            try (Statement stmt                      = this.getSqliteConnection().createStatement();
                 ResultSet tileTablesInTileMatrixSet = stmt.executeQuery(queryMatrixSetPyramid))
            {
               final Set<String> tileMatrixSetTables = ResultSetStream.getStream(tileTablesInTileMatrixSet)
                                                                      .map(resultSet -> { try
                                                                                          {
                                                                                              return resultSet.getString("table_name");
                                                                                          }
                                                                                          catch (final SQLException ex1)
                                                                                          {
                                                                                              return null;
                                                                                          }
                                                                                       })
                                                                      .filter(Objects::nonNull)
                                                                      .collect(Collectors.toSet());

              for(final String table: this.pyramidTablesInContents)
              {
                  Assert.assertTrue(String.format("The table_name %s in the gpkg_tile_matrix_set is not referenced in the gpkg_contents table. Either delete the table %s "
                                                      + "or create a record for that table in the gpkg_contents table",
                                                  table,
                                                  table),
                                    tileMatrixSetTables.contains(table));
              }


            }
            catch (final Exception ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 39</div>
     * <blockquote>
     * The gpkg_tile_matrix_set table or view SHALL contain one row record for each tile pyramid user data table.
     * </blockquote>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     *
     */
    @Requirement(number   = 39,
                 text     = "The gpkg_tile_matrix_set table or view SHALL "
                            + "contain one row record for each tile "
                            + "pyramid user data table. ",
                 severity = Severity.Error)
    public void Requirement39() throws AssertionError
    {
        if (this.hasTileMatrixSetTable)
        {
            final String queryMatrixSet = "SELECT table_name FROM gpkg_tile_matrix_set;";

            try (Statement stmt                        = this.getSqliteConnection().createStatement();
                 ResultSet tileTablesInTileMatrixSet   = stmt.executeQuery(queryMatrixSet))
            {
                final Set<String> tileMatrixSetTables = ResultSetStream.getStream(tileTablesInTileMatrixSet)
                                                                       .map(resultSet -> { try
                                                                                           {
                                                                                               return resultSet.getString("table_name");
                                                                                           }
                                                                                           catch (final SQLException ex1)
                                                                                           {
                                                                                               return null;
                                                                                           }
                                                                                         })
                                                                       .filter(Objects::nonNull)
                                                                       .collect(Collectors.toSet());
                for(final String table: this.allPyramidUserDataTables)
                {
                    Assert.assertTrue(String.format("The Pyramid User Data Table %s is not referenced in the gpkg_tile_matrix_set.", table),
                           tileMatrixSetTables.contains(table));
                }
            }
            catch (final Exception ex)
            {
                Assert.fail(ex.getMessage());
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
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 40,
                  text     = "Values of the gpkg_tile_matrix_set srs_id column "
                              + "SHALL reference values in the gpkg_spatial_ref_sys srs_id column. ",
                  severity = Severity.Error)
    public void Requirement40() throws AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String query1 = "SELECT srs_id from gpkg_tile_matrix_set AS tms " +
                                   "WHERE srs_id NOT IN" +
                                                 "(SELECT srs_id " +
                                                  "FROM gpkg_spatial_ref_sys);";

            try (Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet unreferencedSRS = stmt.executeQuery(query1))
            {
                if (unreferencedSRS.next())
                {
                    Assert.fail(String.format("The gpkg_tile_matrix_set table contains a reference to an srs_id that is not defined in the gpkg_spatial_ref_sys Table. "
                                        + "Unreferenced srs_id: %s",
                                       unreferencedSRS.getInt("srs_id")));
                }
            }
            catch (final Exception ex)
            {
                Assert.fail(ex.getMessage());
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
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (number  = 41,
                  text    = "A GeoPackage that contains a tile pyramid user data table "
                            + "SHALL contain a gpkg_tile_matrix table or view per clause "
                            + "2.2.7.1.1 Table Definition, Table Tile Matrix Metadata Table "
                            + "or View Definition and Table gpkg_tile_matrix Table Creation SQL. ",
                 severity = Severity.Error)
    public void Requirement41() throws AssertionError, SQLException
    {
        if(!this.allPyramidUserDataTables.isEmpty())
        {
            Assert.assertTrue("The GeoPackage does not contain a gpkg_tile_matrix table. Every GeoPackage with a Pyramid User "
                                + "Data Table must also have a gpkg_tile_matrix table.",
                              this.hasTileMatrixTable);

            this.verifyTable(TilesVerifier.TileMatrixTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 42</div>
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix</code>
     * <code>table_name</code> column SHALL reference
     * values in the <code>gpkg_contents</code> <code>
     * table_name</code> column for rows with a <code>
     * data_type</code> of 'tiles'.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number    = 42,
                  text      = "Values of the gpkg_tile_matrix table_name column "
                              + "SHALL reference values in the gpkg_contents table_name "
                              + "column for rows with a data_type of 'tiles'. ",
                 severity   = Severity.Warning)
    public void Requirement42() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT table_name FROM gpkg_tile_matrix AS tm " +
                                               "WHERE table_name NOT IN"                        +
                                                                   "(SELECT table_name  "        +
                                                                    "FROM gpkg_contents AS gc "  +
                                                                    "WHERE tm.table_name = gc.table_name AND gc.data_type = 'tiles');");

            try(Statement stmt               = this.getSqliteConnection().createStatement();
                ResultSet unreferencedTables = stmt.executeQuery(query))
            {
                    if (unreferencedTables.next())
                    {
                        Assert.fail(String.format("There are Pyramid user data tables in gpkg_tile_matrix table_name field such that the table_name does not"
                                           +  " reference values in the gpkg_contents table_name column for rows with a data type of 'tiles'."
                                           +  " Unreferenced table: %s",
                                           unreferencedTables.getString("table_name")));
                    }
            }
            catch (final SQLException ex)
            {
                Assert.fail(ex.getMessage());
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
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number    = 43,
                  text      = "The gpkg_tile_matrix table or view SHALL contain "
                              + "one row record for each zoom level that contains "
                              + "one or more tiles in each tile pyramid user data table or view. ",
                  severity  = Severity.Error)
    public void Requirement43() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for (final String tableName : this.allPyramidUserDataTables)
            {
                final String query1      = String.format("SELECT DISTINCT zoom_level FROM gpkg_tile_matrix WHERE table_name ='%s' ORDER BY zoom_level;", tableName);
                final String query2      = String.format("SELECT DISTINCT zoom_level FROM %s                                      ORDER BY zoom_level;", tableName);

                try (Statement stmt1                     = this.getSqliteConnection().createStatement();
                     ResultSet gm_zoomLevels             = stmt1.executeQuery(query1);

                     Statement stmt2                     = this.getSqliteConnection().createStatement();
                     ResultSet py_zoomLevels             = stmt2.executeQuery(query2))
                {
                    final Set<Integer> tileMatrixZooms = ResultSetStream.getStream(gm_zoomLevels)
                                                                  .map(resultSet -> { try
                                                                                      {
                                                                                          return resultSet.getInt("zoom_level");
                                                                                      }
                                                                                      catch(final SQLException ex)
                                                                                      {
                                                                                          return null;
                                                                                      }
                                                                                    })
                                                                  .filter(Objects::nonNull)
                                                                  .collect(Collectors.toSet());

                    final Set<Integer> tilePyramidZooms = ResultSetStream.getStream(py_zoomLevels)
                                                                    .map(resultSet -> { try
                                                                                        {
                                                                                            return resultSet.getInt("zoom_level");
                                                                                        }
                                                                                        catch(final SQLException ex)
                                                                                        {
                                                                                            return null;
                                                                                        }
                                                                                      })
                                                                    .filter(Objects::nonNull)
                                                                    .collect(Collectors.toSet());
                    for(final Integer zoom: tilePyramidZooms)
                    {
                        Assert.assertTrue(String.format("The gpkg_tile_matrix does not contain a row record for zoom level %d in the Pyramid User Data Table %s.",
                                                 zoom,
                                                 tableName),
                                   tileMatrixZooms.contains(zoom));
                    }
                }
                catch (final Exception ex)
                {
                    Assert.fail(ex.getMessage());
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 44</div>
     * <blockquote>
     * The <code>zoom_level</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL not be negative.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 44,
                  text     = "The zoom_level column value in a gpkg_tile_matrix table row SHALL not be negative." ,
                  severity = Severity.Error)
    public void Requirement44() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(zoom_level) FROM gpkg_tile_matrix;");

            try (Statement stmt     = this.getSqliteConnection().createStatement();
                 ResultSet minZoom  = stmt.executeQuery(query))
            {
                final int minZoomLevel = minZoom.getInt("min(zoom_level)");

                if(!minZoom.wasNull())
                {
                    Assert.assertTrue(String.format("The zoom_level in gpkg_tile_matrix must be greater than 0. Invalid zoom_level: %d", minZoomLevel),
                               minZoomLevel >= 0);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 45</div>
     * <blockquote>
     * <code>matrix_width</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 45,
                  text     = "The matrix_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.",
                  severity = Severity.Error)
    public void Requirement45() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = "SELECT min(matrix_width) FROM gpkg_tile_matrix;";

            try (Statement stmt             = this.getSqliteConnection().createStatement();
                 ResultSet minMatrixWidthRS = stmt.executeQuery(query);)
            {
                final int minMatrixWidth = minMatrixWidthRS.getInt("min(matrix_width)");

                if(!minMatrixWidthRS.wasNull())
                {
                    Assert.assertTrue(String.format("The matrix_width in gpkg_tile_matrix must be greater than 0. Invalid matrix_width: %d", minMatrixWidth),
                               minMatrixWidth > 0);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 46</div>
     * <blockquote>
     * <code>matrix_height</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 46,
                  text     = "The matrix_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.",
                  severity = Severity.Error)
    public void Requirement46() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = "SELECT min(matrix_height) FROM gpkg_tile_matrix;";

            try (Statement stmt              = this.getSqliteConnection().createStatement();
                 ResultSet minMatrixHeightRS = stmt.executeQuery(query);)
            {
                final int minMatrixHeight = minMatrixHeightRS.getInt("min(matrix_height)");

                if(!minMatrixHeightRS.wasNull())
                {
                  Assert.assertTrue(String.format("The matrix_height in gpkg_tile_matrix must be greater than 0. Invalid matrix_height: %d", minMatrixHeight),
                                    minMatrixHeight > 0);
                }
            }
            catch(final SQLException ex)
            {
               Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 47</div>
     * <blockquote>
     * <code>tile_width</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 47,
                  text     = "The tile_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.",
                  severity = Severity.Error)
    public void Requirement47() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = "SELECT min(tile_width) FROM gpkg_tile_matrix;";

            try (Statement stmt           = this.getSqliteConnection().createStatement();
                 ResultSet minTileWidthRS = stmt.executeQuery(query);)
            {
                final int minTileWidth = minTileWidthRS.getInt("min(tile_width)");

                if (!minTileWidthRS.wasNull())
                {
                    Assert.assertTrue(String.format("The tile_width in gpkg_tile_matrix must be greater than 0. Invalid tile_width: %d", minTileWidth),
                                      minTileWidth > 0);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 47</div>
     * <blockquote>
     * <code>tile_height</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(number = 48,
                 text = "The tile_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.",
                 severity = Severity.Error)
    public void Requirement48() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = "SELECT min(tile_height) FROM gpkg_tile_matrix;";

            try (Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet minTileHeightRS = stmt.executeQuery(query);)
            {
                final int testMinTileHeight = minTileHeightRS.getInt("min(tile_height)");

                if (!minTileHeightRS.wasNull())
                {
                    Assert.assertTrue(String.format("The tile_height in gpkg_tile_matrix must be greater than 0. Invalid tile_height: %d",
                                                    testMinTileHeight),
                                      testMinTileHeight > 0);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 49</div>
     * <blockquote>
     * <code>pixel_x_size</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number = 49,
                  text   =  "The pixel_x_size column value in a gpkg_tile_matrix table row SHALL be greater than 0." ,
                  severity = Severity.Error)
    public void Requirement49() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = "SELECT min(pixel_x_size) FROM gpkg_tile_matrix;";

            try (Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet minPixelXSizeRS = stmt.executeQuery(query);)
            {

                final double minPixelXSize = minPixelXSizeRS.getDouble("min(pixel_x_size)");

                if (!minPixelXSizeRS.wasNull())
                {
                    Assert.assertTrue(String.format("The pixel_x_size in gpkg_tile_matrix must be greater than 0. Invalid pixel_x_size: %f",
                                                    minPixelXSize),
                                      minPixelXSize > 0);

                }

            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 50</div>
     * <blockquote>
     * <code>pixel_y_size</code> column value in a <code>gpkg_tile_matrix</code> table row SHALL be greater than 0.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number   = 50,
                  text     = "The pixel_y_size column value in a gpkg_tile_matrix table row SHALL be greater than 0.",
                  severity = Severity.Error)
    public void Requirement50() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
           final String query = "SELECT min(pixel_y_size) FROM gpkg_tile_matrix;";

           try (Statement stmt            = this.getSqliteConnection().createStatement();
                ResultSet minPixelYSizeRS = stmt.executeQuery(query);)
           {
               final double minPixelYSize = minPixelYSizeRS.getDouble("min(pixel_y_size)");

               if (!minPixelYSizeRS.wasNull())
               {
                   Assert.assertTrue(String.format("The pixel_y_size in gpkg_tile_matrix must be greater than 0. Invalid pixel_y_size: %f",
                                                    minPixelYSize),
                                     minPixelYSize > 0);
               }
           }
           catch(final SQLException ex)
           {
               Assert.fail(ex.getMessage());
           }
        }
    }

    /**
     * <div class="title">Requirement 51</div>
     * <blockquote>
     * The <code>pixel_x_size</code> and <code>pixel_y_size</code>
     * column values for <code>zoom_level</code> column values in a
     * <code>gpkg_tile_matrix</code> table sorted in ascending order
     * SHALL be sorted in descending order.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (number = 51,
                  text   = "The pixel_x_size and pixel_y_size column values for zoom_level "
                          + "column values in a gpkg_tile_matrix table sorted in ascending "
                          + "order SHALL be sorted in descending order.",
                  severity = Severity.Error)
    public void Requirement51() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for (final String pyramidTable : this.allPyramidUserDataTables)
            {
                final String query2 = String.format("SELECT pixel_x_size, pixel_y_size "
                                                        + "FROM gpkg_tile_matrix WHERE table_name = '%s' ORDER BY zoom_level ASC;",
                                                    pyramidTable);
                Double pixelX2 = null;
                Double pixelY2 = null;

                try (Statement stmt2        = this.getSqliteConnection().createStatement();
                     ResultSet zoomPixxPixy = stmt2.executeQuery(query2))
                {
                    while (zoomPixxPixy.next())
                    {
                        final Double pixelX = zoomPixxPixy.getDouble("pixel_x_size");
                        final Double pixelY = zoomPixxPixy.getDouble("pixel_y_size");

                        if (pixelX2 != null && pixelY2 != null)
                        {
                            Assert.assertTrue(String.format("Pixel sizes for tile matrix user data tables do not increase while "
                                                       + "the zoom level decrease. Invalid pixel_x_size %s. Invalid pixel_y_size: %s.",
                                                     pixelX.toString(), pixelY.toString()),
                                       pixelX2 > pixelX && pixelY2 > pixelY);

                            pixelX2 = pixelX;
                            pixelY2 = pixelY;
                        }
                        else if (zoomPixxPixy.next())
                        {
                            pixelX2 = zoomPixxPixy.getDouble("pixel_x_size");
                            pixelY2 = zoomPixxPixy.getDouble("pixel_y_size");

                            Assert.assertTrue(String.format("Pixel sizes for tile matrix user data tables do not increase while "
                                                      + "the zoom level decrease. Invalid pixel_x_size %s. Invalid pixel_y_size: %s.",
                                                     pixelX2.toString(), pixelY2.toString()),
                                      pixelX > pixelX2 && pixelY > pixelY2);
                        }
                    }
                }
                catch (final Exception ex)
                {
                    Assert.fail(ex.getMessage());
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 52</div>
     * <blockquote>
     * Each tile matrix set in a GeoPackage SHALL be stored in a
     * different tile pyramid user data table or updateable view
     *  with a unique name that SHALL have a column named "id" with
     *  column type INTEGER and <em>PRIMARY KEY AUTOINCREMENT</em>
     *  column constraints per Clause 2.2.8.1.1
     *  <a href="http://www.geopackage.org/spec/#tiles_user_tables_data_table_definition">
     *  Table Definition</a>, <a href="http://www.geopackage.org/spec/#example_tiles_table_cols">
     *  Tiles Table or View Definition</a> and
     *  <a href="http://www.geopackage.org/spec/#example_tiles_table_insert_sql">
     *  EXAMPLE: tiles table Insert Statement (Informative)</a>.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws when an SQLException occurs
     */
    @Requirement (number = 52,
                  text   = "Each tile matrix set in a GeoPackage SHALL "
                           + "be stored in a different tile pyramid user "
                           + "data table or updateable view with a unique "
                           + "name that SHALL have a column named \"id\" with"
                           + " column type INTEGER and PRIMARY KEY AUTOINCREMENT"
                           + " column constraints per Clause 2.2.8.1.1 Table Definition,"
                           + " Tiles Table or View Definition and EXAMPLE: tiles table "
                           + "Insert Statement (Informative). ",
                  severity = Severity.Error)
    public void Requirement52() throws AssertionError, SQLException
    {
       //verify the tables are defined correctly
        for(final String table: this.pyramidTablesInContents)
        {
            if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), table))
            {
               this.verifyTable(new TilePyramidUserDataTableDefinition(table));
            }
            else
            {
                throw new AssertionError(String.format("The tiles table %s does not exist even though it is defined in the gpkg_contents table. "
                                                         + "Either create the table %s or delete the record in gpkg_contents table referring to table %s.",
                                                        table,
                                                        table,
                                                        table));
            }
        }
        //Ensure that the pyramid tables are referenced in tile matrix set
        if(this.hasTileMatrixSetTable)
        {
            final String query2 = "SELECT DISTINCT table_name "
                                  + "FROM gpkg_contents WHERE data_type = 'tiles' "
                                                     +  "AND table_name NOT IN"
                                                                             + " (SELECT DISTINCT table_name "
                                                                             + " FROM gpkg_tile_matrix_set);";

            try(Statement      stmt2                         = this.getSqliteConnection().createStatement();
                ResultSet      unreferencedPyramidTableInTMS = stmt2.executeQuery(query2))
                 {
                   //verify that all the pyramid user data tables are referenced in the Tile Matrix Set table
                   if(unreferencedPyramidTableInTMS.next())
                   {
                       Assert.fail(String.format("There are Pyramid User Data Tables that do not contain a record in the gpkg_tile_matrix_set."
                                                   + " Unreferenced Pyramid table: %s",
                                                 unreferencedPyramidTableInTMS.getString("table_name")));
                   }
                 }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage());
            }
        }
    }

    /**
     * <div class="title">Requirement 53</div>
     * <blockquote>
     * For each distinct <code>table_name</code> from the <code>gpkg_tile_matrix</code>
     *  (tm) table, the tile pyramid (tp) user data table <code>zoom_level</code>
     *  column value in a GeoPackage SHALL be in the range min(tm.zoom_level) <= tp.zoom_level <= max(tm.zoom_level).
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (number = 53,
                  text   = "For each distinct table_name from the gpkg_tile_matrix (tm) table, "
                          + "the tile pyramid (tp) user data table zoom_level column value in a "
                          + "GeoPackage SHALL be in the range min(tm.zoom_level) less than or equal "
                          + "to tp.zoom_level less than or equal to max(tm.zoom_level).",
                  severity = Severity.Error)
    public void Requirement53() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidName: this.pyramidTablesInTileMatrix)
            {
                final String query2      = String.format("SELECT MIN(zoom_level) AS min_gtm_zoom, MAX(zoom_level) "
                                                           + "AS max_gtm_zoom FROM gpkg_tile_matrix WHERE table_name = '%s'",
                                                         pyramidName);

                try (Statement stmt2      = this.getSqliteConnection().createStatement();
                     ResultSet minMaxZoom = stmt2.executeQuery(query2))
                {
                    final int minZoom = minMaxZoom.getInt("min_gtm_zoom");
                    final int maxZoom = minMaxZoom.getInt("max_gtm_zoom");

                    if (!minMaxZoom.wasNull())
                    {
                        final String query3 = String.format("SELECT id FROM %s WHERE zoom_level < %d OR zoom_level > %d", pyramidName, minZoom, maxZoom);

                        try (Statement stmt3        = this.getSqliteConnection().createStatement();
                             ResultSet invalidZooms = stmt3.executeQuery(query3))
                        {
                            if (invalidZooms.next())
                            {
                                Assert.fail(String.format("There are zoom_levels in the Pyramid User Data Table: %s  such that the zoom level "
                                                            + "is bigger than the maximum zoom level: %d or smaller than the minimum zoom_level: %d"
                                                            + " that was determined by the gpkg_tile_matrix Table.  Invalid tile with an id of %d from table %s",
                                                          pyramidName,
                                                          maxZoom,
                                                          minZoom,
                                                          invalidZooms.getInt("id"),
                                                          pyramidName));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 54</div> <blockquote> For each distinct
     * <code>table_name</code> from the <code>gpkg_tile_matrix</code> (tm)
     * table, the tile pyramid (tp) user data table <code>tile_column</code>
     * column value in a GeoPackage SHALL be in the range 0 <= tp.tile_column <=
     * tm.matrix_width � 1 where the tm and tp <code>zoom_level</code> column
     * values are equal. </blockquote> </div>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement(number  = 54,
                 text    = "For each distinct table_name from the gpkg_tile_matrix (tm) table, "
                             + "the tile pyramid (tp) user data table tile_column column value in a "
                             + "GeoPackage SHALL be in the range 0 <= tp.tile_column <= tm.matrix_width - 1 "
                             + "where the tm and tp zoom_level column values are equal. ",
                severity = Severity.Warning)
    public void Requirement54() throws AssertionError, SQLException
    {
        if (this.hasTileMatrixTable)
        {
            for(final String pyramidName : this.pyramidTablesInTileMatrix)
            {
            // this query will only pull the incorrect values for the
            // pyramid user data table's column width, the value
            // of the tile_column value for the pyramid user data table
            // SHOULD be null otherwise those fields are in violation
            // of the range
            final String query2 = String.format("SELECT DISTINCT gtmm.zoom_level   AS udt_zoom,"
                                                              + "gtmm.matrix_width AS gtmm_width,"
                                                              + "udt.id            AS udt_id, "
                                                              + "udt.tile_column   AS udt_column "
                                                     + "FROM gpkg_tile_matrix AS gtmm "
                                                     + "LEFT OUTER JOIN %s AS udt ON"
                                                                           + " udt.zoom_level  = gtmm.zoom_level  AND"
                                                                           + " gtmm.table_name = '%s'             AND"
                                                                           + " (udt_column < 0 OR udt_column > (gtmm_width - 1));",
                                                  pyramidName, pyramidName);

                try (Statement stmt2            = this.getSqliteConnection().createStatement();
                     ResultSet incorrectColumns = stmt2.executeQuery(query2))
                {
                    final Map<Integer, List<TileData>> incorrectColumnSet = ResultSetStream.getStream(incorrectColumns)
                                                                                           .map(resultSet -> { try
                                                                                                                 {
                                                                                                                      final TileData tileData   = new TileData();
                                                                                                                      tileData.tileColumn = resultSet.getInt("udt_column");
                                                                                                                      //if tileColumn is null, then it is a a correct value
                                                                                                                      if(resultSet.wasNull())
                                                                                                                      {
                                                                                                                          return null;
                                                                                                                      }
                                                                                                                      tileData.matrixWidth = resultSet.getInt("gtmm_width");
                                                                                                                      tileData.zoomLevel   = resultSet.getInt("udt_zoom");
                                                                                                                      tileData.tileID      = resultSet.getInt("udt_id");
                                                                                          
                                                                                                                      return tileData;
                                                                                          
                                                                                                                 }
                                                                                                                 catch(final SQLException ex)
                                                                                                               {
                                                                                                                      return null;
                                                                                                               }
                                                                                                               })
                                                                                           .filter(Objects::nonNull)
                                                                                           .collect(Collectors.groupingBy(tileData -> tileData.zoomLevel));
                    Assert.assertTrue(String.format("  The following tiles in table '%s' have incorrect tile column values: %s",
                                                    pyramidName,
                                                    incorrectColumnSet.entrySet()
                                                                      .stream()
                                                                      .map(tileData -> String.format("\n    Zoom level %d:\n%s",
                                                                                                       tileData.getKey(),
                                                                                                       tileData.getValue()
                                                                                                               .stream()
                                                                                                               .sorted()
                                                                                                               .map(tileInfo ->tileInfo.columnInvalidToString())
                                                                                                               .collect(Collectors.joining("\n"))))
                                                                      .collect(Collectors.joining("\n"))),
                                     incorrectColumnSet.isEmpty());

                }
            }
        }
    }

    /**
     * <div class="title">Requirement 55</div>
     * <blockquote>
     * For each distinct <code>table_name</code> from the <code>gpkg_tile_matrix</code>
     * (tm) table, the tile pyramid (tp) user data table <code>tile_row</code> column
     * value in a GeoPackage SHALL be in the range 0 <= tp.tile_row <= tm.matrix_height ï¿½ 1
     * where the tm and tp <code>zoom_level</code> column values are equal.
     * </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (number   = 55,
                  text     = "For each distinct table_name from the gpkg_tile_matrix (tm) table, the tile pyramid (tp) "
                                + "user data table tile_row column value in a GeoPackage SHALL be in the range 0 <= tp.tile_row <= tm.matrix_height - 1 "
                                + "where the tm and tp zoom_level column values are equal. ",
                  severity = Severity.Warning)
    public void Requirement55() throws AssertionError, SQLException
    {
        if (this.hasTileMatrixTable)
        {
            for(final String pyramidName: this.pyramidTablesInTileMatrix)
               {
                // this query will only pull the incorrect values for the
                // pyramid user data table's column height, the value
                // of the tile_row value for the pyramid user data table
                // SHOULD be null otherwise those fields are in violation
                // of the range
                final String query2 = String.format("SELECT DISTINCT gtmm.zoom_level    AS gtmm_zoom, "
                                                                  + "gtmm.matrix_height AS gtmm_height,"
                                                                  + "udt.zoom_level     AS udt_zoom, "
                                                                  + "udt.tile_row       AS udt_row, "
                                                                  + "udt.id             AS udt_id "
                                                        + "FROM gpkg_tile_matrix AS gtmm "
                                                        + "LEFT OUTER JOIN %s AS udt ON "
                                                                    + "udt.zoom_level = gtmm.zoom_level "
                                                                    + "AND gtmm.table_name = '%s' "
                                                                    + "AND (udt_row < 0 OR udt_row > (gtmm_height- 1));",
                                                    pyramidName,
                                                    pyramidName);

                try (Statement stmt2            = this.getSqliteConnection().createStatement();
                     ResultSet incorrectTileRow = stmt2.executeQuery(query2))
                {


                    final Map<Integer, List<TileData>> incorrectTileRowSet = ResultSetStream.getStream(incorrectTileRow)
                                                                                            .map(resultSet -> {    try
                                                                                                                   {
                                                                                                                       final TileData tileData = new TileData();
                                                                                                                       tileData.tileRow = resultSet.getInt("udt_row");
                                                                                                                       //if tileRow is null, then it is a a correct value
                                                                                                                       if(resultSet.wasNull())
                                                                                                                       {
                                                                                                                           return null;
                                                                                                                       }
                                                                                                                       tileData.matrixHeight = resultSet.getInt("gtmm_height");
                                                                                                                       tileData.zoomLevel = resultSet.getInt("udt_zoom");
                                                                                                                       tileData.tileID     = resultSet.getInt("udt_id");

                                                                                                                       return tileData;

                                                                                                                  }
                                                                                                                  catch(final SQLException ex)
                                                                                                                  {
                                                                                                                        return null;
                                                                                                                  }
                                                                                                            })
                                                                                            .filter(Objects::nonNull)
                                                                                            .collect(Collectors.groupingBy(tileData -> tileData.zoomLevel));

                    Assert.assertTrue(String.format("  The following tiles in table '%s' have incorrect tile row values: %s",
                                                     pyramidName,
                                                     incorrectTileRowSet.entrySet()
                                                                         .stream()
                                                                         .map(tileData -> String.format("\n    Zoom level %d:\n%s",
                                                                                                   tileData.getKey(),
                                                                                                   tileData.getValue().stream()
                                                                                                                      .sorted()
                                                                                                                      .map(tileInfo -> tileInfo.rowInvalidToString())
                                                                                                                      .collect(Collectors.joining("\n"))))
                                                                         .collect(Collectors.joining("\n"))),
                                    incorrectTileRowSet.isEmpty());

                }
            }
        }
    }

    private static boolean validPixelValues(final TileData tileData, final BoundingBox boundingBox)
    {

        return isEqual(tileData.pixelXSize, (boundingBox.getWidth()/tileData.matrixWidth)/tileData.tileWidth) &&
               isEqual(tileData.pixelYSize, (boundingBox.getHeight()/tileData.matrixHeight)/tileData.tileHeight);
    }

    /**
     * This method determines if the two doubles are
     * equal based upon the maximum level of allowable
     * differenced determined by the Epsilon value 0.001
     * @param first
     * @param second
     * @return
     */
    private static boolean isEqual(final double first, final double second)
    {
        return Math.abs(first - second) < TilesVerifier.EPSILON;
    }

    private static <T> Collection<T> iteratorToCollection(final Iterator<T> iterator)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * This Verifies if the Tile Matrix Table exists.
     * @return true if the gpkg_tile_matrix table exists
     * @throws AssertionError throws an assertion error if the gpkg_tile_matrix table
     *  doesn't exist and the GeoPackage contains a tiles table
     */
    private boolean tileMatrixTableExists() throws SQLException
    {
       return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixTableName);
    }

    /**
     * This Verifies if the Tile Matrix Set Table exists in the
     * GeoPackage.
     * @return true if the gpkg_tile_matrix Set table exists
     * @throws AssertionError throws an assertion error if the gpkg_tile_matrix_set table
     *  doesn't exist and the GeoPackage contains a tiles table
     * @throws SQLException
     */
    private boolean tileMatrixSetTableExists() throws SQLException
    {
       return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixSetTableName);
    }

    private static boolean canReadImage(final Collection<ImageReader> imageReaders, final ImageInputStream image)
    {
        return imageReaders.stream()
                           .anyMatch(imageReader -> {  try
                                                       {
                                                           image.mark();
                                                           return imageReader.getOriginatingProvider().canDecodeInput(image);
                                                       }
                                                       catch(final Exception ex)
                                                       {
                                                           return false;
                                                       }
                                                       finally
                                                       {
                                                            try
                                                            {
                                                                image.reset();
                                                            }
                                                            catch (final Exception e)
                                                            {
                                                                e.printStackTrace();
                                                            }
                                                       }
                                                    });
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
