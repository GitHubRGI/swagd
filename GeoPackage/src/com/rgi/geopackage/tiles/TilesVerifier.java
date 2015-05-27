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

package com.rgi.geopackage.tiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.verification.Assert;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

/**
 * @author Jenifer Cochran
 *
 */
public class TilesVerifier extends Verifier
{
    /**
     * This Epsilon is the greatest difference we allow when comparing doubles
     */
    public static final double EPSILON = 0.0001;

    /**
     * Constructor
     *
     * @param sqliteConnection
     *            the connection to the database
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @throws SQLException
     *             throws if the method {@link DatabaseUtility#tableOrViewExists(Connection, String) tableOrViewExists} throws
     */
    public TilesVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        final String queryTables = "SELECT tbl_name FROM sqlite_master " +
                                   "WHERE tbl_name NOT LIKE 'gpkg_%' "   +
                                                      "AND (type = 'table' OR type = 'view');";

        try(Statement createStmt             = this.getSqliteConnection().createStatement();
            ResultSet possiblePyramidTables  = createStmt.executeQuery(queryTables);)
        {
           this.allPyramidUserDataTables = ResultSetStream.getStream(possiblePyramidTables)
                                                          .map(resultSet -> { try
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

        final String query2 = String.format("SELECT table_name FROM %s WHERE data_type = 'tiles';",
                                            GeoPackageCore.ContentsTableName);

        try(Statement createStmt2           = this.getSqliteConnection().createStatement();
            ResultSet contentsPyramidTables = createStmt2.executeQuery(query2))
        {
            this.pyramidTablesInContents = ResultSetStream.getStream(contentsPyramidTables)
                                                          .map(resultSet -> { try
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


        this.hasTileMatrixSetTable = this.tileMatrixSetTableExists();
        this.hasTileMatrixTable    = this.tileMatrixTableExists();

        if(this.hasTileMatrixTable)
        {
            final String query3 = String.format("SELECT DISTINCT table_name FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement createStmt3             = this.getSqliteConnection().createStatement();
                ResultSet tileMatrixPyramidTables = createStmt3.executeQuery(query3))
            {
                this.pyramidTablesInTileMatrix = ResultSetStream.getStream(tileMatrixPyramidTables)
                                                                .map(resultSet -> { try
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
        }
        else
        {
            this.pyramidTablesInTileMatrix = Collections.emptySet();
        }

    }

    /**
     * Requirement 34
     *
     * <blockquote>
     * The <code>gpkg_contents</code> table SHALL contain a row with a <code>
     * data_type</code> column value of 'tiles' for each tile pyramid user data
     * table or view.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 34",
                 text      = "The gpkg_contents table SHALL contain a row with a "
                             + "data_type column value of \"tiles\" for each "
                             + "tile pyramid user data table or view.")
    public void Requirement34() throws AssertionError
    {
       for(final String tableName: this.allPyramidUserDataTables)
       {
           Assert.assertTrue(String.format("The Tile Pyramid User Data table that is not refrenced in %2$s table is: %1$s. This table needs to be referenced in the %2$s table.",
                                           tableName,
                                           GeoPackageCore.ContentsTableName),
                             this.pyramidTablesInContents.contains(tableName),
                             Severity.Warning);
       }
    }

    /**
     * Requirement 35
     *
     * <blockquote>
     * In a GeoPackage that contains a tile pyramid user data table that
     * contains tile data, by default, zoom level pixel sizes for that table
     * SHALL vary by a factor of 2 between adjacent zoom levels in the tile
     * matrix metadata table.
     * </blockquote>
     *
     * @throws AssertionError  throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement(reference = "Requirement 35",
                 text      = "In a GeoPackage that contains a tile pyramid user data table "
                             + "that contains tile data, by default, zoom level pixel sizes for that "
                             + "table SHALL vary by a factor of 2 between zoom levels in tile matrix metadata table.")
    public void Requirement35() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String tableName : this.allPyramidUserDataTables)
            {
                final String query1 = String.format("SELECT table_name, "
                                                         + "zoom_level, "
                                                         + "pixel_x_size, "
                                                         + "pixel_y_size,"
                                                         + "matrix_width,"
                                                         + "matrix_height,"
                                                         + "tile_width,"
                                                         + "tile_height "
                                                  + "FROM %s "
                                                  + "WHERE table_name = ? "
                                                  + "ORDER BY zoom_level ASC;", GeoPackageTiles.MatrixTableName);

                try(final PreparedStatement stmt = this.getSqliteConnection().prepareStatement(query1))
                {

                    stmt.setString(1, tableName);

                    try(final ResultSet         pixelInfo = stmt.executeQuery())
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
                                                  TilesVerifier.isEqual((current.pixelYSize / 2.0), next.pixelYSize),
                                                  Severity.Warning);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Requirement 36
     *
     * <blockquote>
     * In a GeoPackage that contains a tile pyramid user data table that
     * contains tile data that is not <a
     * href="http://www.ietf.org/rfc/rfc2046.txt">MIME type</a>
     * <a href="http://www.jpeg.org/public/jfif.pdf">image/jpeg</a>, by default
     * SHALL store that tile data in <a
     * href="http://www.iana.org/assignments/media-types/index.html"> MIME type
     * </a> <a href="http://libpng.org/pub/png/">image/png</a>.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 36",
                 text      = "In a GeoPackage that contains a tile pyramid user data table that contains tile data SHALL store that tile data in MIME type image/jpeg or image/png")
    public void Requirement36() throws AssertionError
    {
        Assert.assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full.name(),
                          this.verificationLevel == VerificationLevel.Full,
                          Severity.Skipped);

        for(final String tableName : this.allPyramidUserDataTables)
        {
            final String selectTileDataQuery = String.format("SELECT tile_data, id FROM %s;", tableName);

            try(final PreparedStatement stmt              = this.getSqliteConnection().prepareStatement(selectTileDataQuery);
                final ResultSet         tileDataResultSet = stmt.executeQuery())
            {
                 final List<String> errorMessage = ResultSetStream.getStream(tileDataResultSet)
                                                                  .map(resultSet -> { try
                                                                                      {
                                                                                          final byte[] tileData = resultSet.getBytes("tile_data");

                                                                                          return TilesVerifier.verifyData(tileData) ? null
                                                                                                                                    : String.format("%d", resultSet.getInt("id"));

                                                                                      }
                                                                                      catch(final SQLException | IOException ex)
                                                                                      {
                                                                                         return ex.getMessage();
                                                                                      }
                                                                                    })
                                                                  .filter(Objects::nonNull)
                                                                  .collect(Collectors.toList());

                 Assert.assertTrue(String.format("The following columns named \"id\" in table '%s' are not in the correct image format:\n%s.",
                                                 tableName,
                                                 errorMessage.stream().collect(Collectors.joining(", "))),
                                   errorMessage.isEmpty(),
                                   Severity.Warning);
            }

            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(), Severity.Error);
            }
        }
    }

    /**
     * Requirement 37
     *
     * <blockquote>
     * In a GeoPackage that contains a tile pyramid user data table that
     * contains tile data that is not <a
     * href="http://www.iana.org/assignments/media-types/index.html">MIME type
     * </a> <a href="http://libpng.org/pub/png/">image/png</a>, by default
     * SHALL store that tile data in <a
     * href="http://www.ietf.org/rfc/rfc2046.txt">MIME type</a> <a
     * href="http://www.jpeg.org/public/jfif.pdf">image/jpeg</a>.
     * </blockquote>
     */
    @Requirement(reference = "Requirement 37",
                 text      = "In a GeoPackage that contains a tile pyramid user data table that "
                             + "contains tile data that is not MIME type image png, "
                             + "by default SHALL store that tile data in MIME type image jpeg")
    public void Requirement37()
    {
        // This requirement is tested through Requirement 35 test in TilesVerifier.
    }

    /**
     * Requirement 38
     *
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain
     * <code>gpkg_tile_matrix_set</code> table or view per <a
     * href="http://www.geopackage.org/spec/#tile_matrix_set_data_table_definition">
     * Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_tile_matrix_set_cols">Tile
     * Matrix Set Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_tile_matrix_set_sql">
     * gpkg_tile_matrix_set Table Creation SQL</a>.
     * </blockquote>
     *
     * @throws SQLException throws if an SQLException occurs
     * @throws AssertionError  throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 38",
                 text      = "A GeoPackage that contains a tile pyramid user data table SHALL "
                             + "contain gpkg_tile_matrix_set table or view per Table Definition, "
                             + "Tile Matrix Set Table or View Definition and gpkg_tile_matrix_set Table Creation SQL. ")
    public void Requirement38() throws AssertionError, SQLException
    {
        if(!this.allPyramidUserDataTables.isEmpty())
        {

            Assert.assertTrue(String.format("The GeoPackage does not contain a %1$s. Every GeoPackage with a Pyramid User Data Table must also have a %1$s table.",
                                            GeoPackageTiles.MatrixSetTableName),
                              this.hasTileMatrixSetTable,
                              Severity.Error);

            this.verifyTable(TilesVerifier.TileMatrixSetTableDefinition);
        }
    }

    /**
     * Requirement 39
     *
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix_set</code> <code>table_name</code>
     * column SHALL reference values in the gpkg_contents table_name column for
     * rows with a data type of "tiles".
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 39",
                 text      = "Values of the gpkg_tile_matrix_set table_name column "
                             + "SHALL reference values in the gpkg_contents table_name "
                             + "column for rows with a data type of \"tiles\".")
    public void Requirement39() throws AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String queryMatrixSetPyramid = String.format("SELECT table_name FROM %s;", GeoPackageTiles.MatrixSetTableName);

            try(Statement stmt                      = this.getSqliteConnection().createStatement();
                 ResultSet tileTablesInTileMatrixSet = stmt.executeQuery(queryMatrixSetPyramid))
            {
                final Set<String> tileMatrixSetTables = ResultSetStream.getStream(tileTablesInTileMatrixSet)
                                                                       .map(resultSet -> { try
                                                                                           {
                                                                                               return resultSet.getString("table_name");
                                                                                           }
                                                                                           catch(final SQLException ex1)
                                                                                           {
                                                                                               return null;
                                                                                           }
                                                                                        })
                                                                       .filter(Objects::nonNull)
                                                                       .collect(Collectors.toSet());

                for(final String table: this.pyramidTablesInContents)
                {
                    Assert.assertTrue(String.format("The table_name %1$s in the %2$s is not referenced in the %3$s table. Either delete the table %1$s or create a record for that table in the %3$s table",
                                                    table,
                                                    GeoPackageTiles.MatrixSetTableName,
                                                    GeoPackageCore.ContentsTableName),
                                      tileMatrixSetTables.contains(table),
                                      Severity.Warning);
                }
            }
            catch(final Exception ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 40
     *
     * <blockquote>
     * The gpkg_tile_matrix_set table or view SHALL contain one row record for
     * each tile pyramid user data table.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 40",
                 text      = "The gpkg_tile_matrix_set table or view SHALL "
                             + "contain one row record for each tile "
                             + "pyramid user data table.")
    public void Requirement40() throws AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String queryMatrixSet =  String.format("SELECT table_name FROM %s;", GeoPackageTiles.MatrixSetTableName);

            try(Statement stmt                        = this.getSqliteConnection().createStatement();
                 ResultSet tileTablesInTileMatrixSet   = stmt.executeQuery(queryMatrixSet))
            {
                final Set<String> tileMatrixSetTables = ResultSetStream.getStream(tileTablesInTileMatrixSet)
                                                                       .map(resultSet -> { try
                                                                                           {
                                                                                               return resultSet.getString("table_name");
                                                                                           }
                                                                                           catch(final SQLException ex1)
                                                                                           {
                                                                                               return null;
                                                                                           }
                                                                                         })
                                                                       .filter(Objects::nonNull)
                                                                       .collect(Collectors.toSet());
                for(final String table: this.allPyramidUserDataTables)
                {
                    Assert.assertTrue(String.format("The Pyramid User Data Table %s is not referenced in  %s.",
                                                    table,
                                                    GeoPackageTiles.MatrixSetTableName),
                                      tileMatrixSetTables.contains(table),
                                      Severity.Error);
                }
            }
            catch(final Exception ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 41
     *
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix_set</code> <code>srs_id</code>
     * column SHALL reference values in the <code>gpkg_spatial_ref_sys</code>
     * <code> srs_id</code> column.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 41",
                  text      = "Values of the gpkg_tile_matrix_set srs_id column "
                              + "SHALL reference values in the gpkg_spatial_ref_sys srs_id column. ")
    public void Requirement41() throws AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String query1 = String.format("SELECT srs_id from %s AS tms " +
                                                "WHERE srs_id NOT IN" +
                                                             "(SELECT srs_id " +
                                                             "FROM %s);",
                                              GeoPackageTiles.MatrixSetTableName,
                                              GeoPackageCore.SpatialRefSysTableName);

            try(Statement stmt            = this.getSqliteConnection().createStatement();
                ResultSet unreferencedSRS = stmt.executeQuery(query1))
            {
                if(unreferencedSRS.next())
                {
                    Assert.fail(String.format("The %s table contains a reference to an srs_id that is not defined in the %s Table. Unreferenced srs_id: %s",
                                              GeoPackageTiles.MatrixSetTableName,
                                              GeoPackageCore.SpatialRefSysTableName,
                                              unreferencedSRS.getInt("srs_id")),
                                Severity.Error);
                }
            }
            catch(final Exception ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 42
     *
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain
     * a <code>gpkg_tile_matrix</code> table or view per clause 2.2.7.1.1 <a
     * href="http://www.geopackage.org/spec/#tile_matrix_data_table_definition">
     * Table Definition</a>, Table <a
     * href="http://www.geopackage.org/spec/#gpkg_tile_matrix_cols">Tile Matrix
     * Metadata Table or View Definition</a> and Table <a
     * href="http://www.geopackage.org/spec/#gpkg_tile_matrix_sql">
     * gpkg_tile_matrix Table Creation SQL</a>.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 42",
                  text      = "A GeoPackage that contains a tile pyramid user data table "
                              + "SHALL contain a gpkg_tile_matrix table or view per clause "
                              + "2.2.7.1.1 Table Definition, Table Tile Matrix Metadata Table "
                              + "or View Definition and Table gpkg_tile_matrix Table Creation SQL. ")
    public void Requirement42() throws AssertionError, SQLException
    {
        if(!this.allPyramidUserDataTables.isEmpty())
        {
            Assert.assertTrue(String.format("The GeoPackage does not contain a %1$s table. Every GeoPackage with a Pyramid User Data Table must also have a %1$s table.",
                                            GeoPackageTiles.MatrixTableName),
                              this.hasTileMatrixTable,
                              Severity.Error);

            this.verifyTable(TilesVerifier.TileMatrixTableDefinition);
        }
    }

    /**
     * Requirement 43
     *
     * <blockquote>
     * Values of the <code>gpkg_tile_matrix</code> <code>table_name</code>
     * column SHALL reference values in the <code>gpkg_contents</code> <code>
     * table_name</code> column for rows with a <code>data_type</code> of
     * 'tiles'.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 43",
                  text      = "Values of the gpkg_tile_matrix table_name column "
                              + "SHALL reference values in the gpkg_contents table_name "
                              + "column for rows with a data_type of 'tiles'. ")
    public void Requirement43() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT table_name FROM %s AS tm " +
                                               "WHERE table_name NOT IN"                        +
                                                                   "(SELECT table_name  "        +
                                                                    "FROM %s AS gc "  +
                                                                    "WHERE tm.table_name = gc.table_name AND gc.data_type = 'tiles');",
                                              GeoPackageTiles.MatrixTableName,
                                              GeoPackageCore.ContentsTableName);

            try(Statement stmt               = this.getSqliteConnection().createStatement();
                ResultSet unreferencedTables = stmt.executeQuery(query))
            {
                if(unreferencedTables.next())
                {
                    Assert.fail(String.format("There are Pyramid user data tables in %s table_name field such that the table_name does not"
                                               + " reference values in the %s table_name column for rows with a data type of 'tiles'."
                                               + " Unreferenced table: %s",
                                               GeoPackageTiles.MatrixTableName,
                                               GeoPackageCore.ContentsTableName,
                                               unreferencedTables.getString("table_name")),
                                Severity.Warning);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }


    /**
     * Requirement 44
     *
     * <blockquote>
     * The <code>gpkg_tile_matrix</code> table or view SHALL contain one row
     * record for each zoom level that contains one or more tiles in each tile
     * pyramid user data table or view.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 44",
                  text      = "The gpkg_tile_matrix table or view SHALL contain "
                              + "one row record for each zoom level that contains "
                              + "one or more tiles in each tile pyramid user data table or view. ")
    public void Requirement44() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for(final String tableName : this.allPyramidUserDataTables)
            {
                final String query1 = String.format("SELECT DISTINCT zoom_level FROM %s WHERE table_name = ? ORDER BY zoom_level;", GeoPackageTiles.MatrixTableName);
                final String query2 = String.format("SELECT DISTINCT zoom_level FROM %s                      ORDER BY zoom_level;", tableName);

                try(PreparedStatement stmt1 = this.getSqliteConnection().prepareStatement(query1))
                {
                    stmt1.setString(1, tableName);

                    try(ResultSet         gm_zoomLevels = stmt1.executeQuery();
                        PreparedStatement stmt2         = this.getSqliteConnection().prepareStatement(query2);
                        ResultSet         py_zoomLevels = stmt2.executeQuery())
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
                            Assert.assertTrue(String.format("The %s does not contain a row record for zoom level %d in the Pyramid User Data Table %s.",
                                                            GeoPackageTiles.MatrixTableName,
                                                            zoom,
                                                            tableName),
                                       tileMatrixZooms.contains(zoom),
                                       Severity.Error);
                        }
                    }
                }
                catch(final Exception ex)
                {
                    Assert.fail(ex.getMessage(),
                                Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 45
     *
     * <blockquote>
     * The <code>zoom_level</code> column value in a <code>gpkg_tile_matrix
     * </code> table row SHALL not be negative.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 45",
                  text      = "The zoom_level column value in a gpkg_tile_matrix table row SHALL not be negative.")
    public void Requirement45() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(zoom_level) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt     = this.getSqliteConnection().createStatement();
                ResultSet minZoom  = stmt.executeQuery(query))
            {
                final int minZoomLevel = minZoom.getInt("min(zoom_level)");

                if(!minZoom.wasNull())
                {
                    Assert.assertTrue(String.format("The zoom_level in %s must be greater than 0. Invalid zoom_level: %d", GeoPackageTiles.MatrixTableName, minZoomLevel),
                                      minZoomLevel >= 0,
                                      Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 46
     *
     * <blockquote>
     * <code>matrix_width</code> column value in a <code>gpkg_tile_matrix
     * </code> table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 46",
                  text      = "The matrix_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement46() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(matrix_width) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt             = this.getSqliteConnection().createStatement();
                ResultSet minMatrixWidthRS = stmt.executeQuery(query);)
            {
                final int minMatrixWidth = minMatrixWidthRS.getInt("min(matrix_width)");

                if(!minMatrixWidthRS.wasNull())
                {
                    Assert.assertTrue(String.format("The matrix_width in %s must be greater than 0. Invalid matrix_width: %d",
                                                    GeoPackageTiles.MatrixTableName,
                                                    minMatrixWidth),
                                      minMatrixWidth > 0,
                                      Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 47
     *
     * <blockquote>
     * <code>matrix_height</code> column value in a <code>gpkg_tile_matrix
     * </code> table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 47",
                  text      = "The matrix_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement47() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(matrix_height) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt              = this.getSqliteConnection().createStatement();
                 ResultSet minMatrixHeightRS = stmt.executeQuery(query);)
            {
                final int minMatrixHeight = minMatrixHeightRS.getInt("min(matrix_height)");

                if(!minMatrixHeightRS.wasNull())
                {
                  Assert.assertTrue(String.format("The matrix_height in %s must be greater than 0. Invalid matrix_height: %d",
                                                  GeoPackageTiles.MatrixTableName,
                                                  minMatrixHeight),
                                    minMatrixHeight > 0,
                                    Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 48
     *
     * <blockquote>
     * <code>tile_width</code> column value in a <code>gpkg_tile_matrix</code>
     * table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 48",
                  text      = "The tile_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement48() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(tile_width) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt           = this.getSqliteConnection().createStatement();
                 ResultSet minTileWidthRS = stmt.executeQuery(query);)
            {
                final int minTileWidth = minTileWidthRS.getInt("min(tile_width)");

                if(!minTileWidthRS.wasNull())
                {
                    Assert.assertTrue(String.format("The tile_width in %s must be greater than 0. Invalid tile_width: %d",
                                                    GeoPackageTiles.MatrixTableName,
                                                    minTileWidth),
                                      minTileWidth > 0,
                                      Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 49
     *
     * <blockquote>
     * <code>tile_height</code> column value in a <code>gpkg_tile_matrix</code>
     * table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 49",
                 text      = "The tile_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement49() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(tile_height) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet minTileHeightRS = stmt.executeQuery(query);)
            {
                final int testMinTileHeight = minTileHeightRS.getInt("min(tile_height)");

                if(!minTileHeightRS.wasNull())
                {
                    Assert.assertTrue(String.format("The tile_height in %s must be greater than 0. Invalid tile_height: %d",
                                                    GeoPackageTiles.MatrixTableName,
                                                    testMinTileHeight),
                                      testMinTileHeight > 0,
                                      Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 50
     *
     * <blockquote>
     * <code>pixel_x_size</code> column value in a <code>gpkg_tile_matrix
     * </code> table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 50",
                  text      = "The pixel_x_size column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement50() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(pixel_x_size) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(Statement stmt            = this.getSqliteConnection().createStatement();
                 ResultSet minPixelXSizeRS = stmt.executeQuery(query);)
            {

                final double minPixelXSize = minPixelXSizeRS.getDouble("min(pixel_x_size)");

                if(!minPixelXSizeRS.wasNull())
                {
                    Assert.assertTrue(String.format("The pixel_x_size in %s must be greater than 0. Invalid pixel_x_size: %f",
                                                    GeoPackageTiles.MatrixTableName,
                                                    minPixelXSize),
                                      minPixelXSize > 0,
                                      Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 51
     *
     * <blockquote>
     * <code>pixel_y_size</code> column value in a <code>gpkg_tile_matrix
     * </code> table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 51",
                  text      = "The pixel_y_size column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void Requirement51() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
           final String query = String.format("SELECT min(pixel_y_size) FROM %s;", GeoPackageTiles.MatrixTableName);

           try(Statement stmt            = this.getSqliteConnection().createStatement();
                ResultSet minPixelYSizeRS = stmt.executeQuery(query);)
           {
               final double minPixelYSize = minPixelYSizeRS.getDouble("min(pixel_y_size)");

               if(!minPixelYSizeRS.wasNull())
               {
                   Assert.assertTrue(String.format("The pixel_y_size in %s must be greater than 0. Invalid pixel_y_size: %f",
                                                   GeoPackageTiles.MatrixTableName,
                                                    minPixelYSize),
                                     minPixelYSize > 0,
                                     Severity.Error);
               }
           }
           catch(final SQLException ex)
           {
               Assert.fail(ex.getMessage(),
                           Severity.Error);
           }
        }
    }

    /**
     * Requirement 52
     *
     * <blockquote>
     * The <code>pixel_x_size</code> and <code>pixel_y_size</code> column
     * values for <code>zoom_level</code> column values in a <code>
     * gpkg_tile_matrix</code> table sorted in ascending order SHALL be sorted
     * in descending order.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 52",
                  text      = "The pixel_x_size and pixel_y_size column values for zoom_level "
                              + "column values in a gpkg_tile_matrix table sorted in ascending "
                              + "order SHALL be sorted in descending order.")
    public void Requirement52() throws AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidTable : this.allPyramidUserDataTables)
            {
                final String query2 = String.format("SELECT pixel_x_size, pixel_y_size "
                                                        + "FROM %s WHERE table_name = ? ORDER BY zoom_level ASC;",
                                                    GeoPackageTiles.MatrixTableName);

                Double pixelX2 = null;
                Double pixelY2 = null;

                try(PreparedStatement stmt2        = this.getSqliteConnection().prepareStatement(query2))
                {
                    stmt2.setString(1, pyramidTable);

                    try(ResultSet zoomPixxPixy = stmt2.executeQuery())
                    {
                        while (zoomPixxPixy.next())
                        {
                            final Double pixelX = zoomPixxPixy.getDouble("pixel_x_size");
                            final Double pixelY = zoomPixxPixy.getDouble("pixel_y_size");

                            if(pixelX2 != null && pixelY2 != null)
                            {
                                Assert.assertTrue(String.format("Pixel sizes for tile matrix user data tables do not increase while the zoom level decrease. Invalid pixel_x_size %s. Invalid pixel_y_size: %s.",
                                                                pixelX.toString(),
                                                                pixelY.toString()),
                                                  pixelX2 > pixelX && pixelY2 > pixelY,
                                                  Severity.Error);

                                pixelX2 = pixelX;
                                pixelY2 = pixelY;
                            }
                            else if(zoomPixxPixy.next())
                            {
                                pixelX2 = zoomPixxPixy.getDouble("pixel_x_size");
                                pixelY2 = zoomPixxPixy.getDouble("pixel_y_size");

                                Assert.assertTrue(String.format("Pixel sizes for tile matrix user data tables do not increase while the zoom level decrease. Invalid pixel_x_size %s. Invalid pixel_y_size: %s.",
                                                                pixelX2.toString(),
                                                                pixelY2.toString()),
                                                  pixelX > pixelX2 && pixelY > pixelY2,
                                                  Severity.Error);
                            }
                        }
                    }
                }
                catch(final Exception ex)
                {
                    Assert.fail(ex.getMessage(),
                                Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 53
     *
     * <blockquote>
     * Each tile matrix set in a GeoPackage SHALL be stored in a different tile
     * pyramid user data table or updateable view with a unique name that SHALL
     * have a column named "id" with column type INTEGER and <em>PRIMARY KEY
     * AUTOINCREMENT</em> column constraints per Clause 2.2.8.1.1 <a
     * href="http://www.geopackage.org/spec/#tiles_user_tables_data_table_definition">
     * Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#example_tiles_table_cols"> Tiles
     * Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#example_tiles_table_insert_sql">
     * EXAMPLE: tiles table Insert Statement (Informative)</a>.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws when an SQLException occurs
     */
    @Requirement (reference = "Requirement 53",
                  text      = "Each tile matrix set in a GeoPackage SHALL "
                              + "be stored in a different tile pyramid user "
                              + "data table or updateable view with a unique "
                              + "name that SHALL have a column named \"id\" with"
                              + " column type INTEGER and PRIMARY KEY AUTOINCREMENT"
                              + " column constraints per Clause 2.2.8.1.1 Table Definition,"
                              + " Tiles Table or View Definition and EXAMPLE: tiles table "
                              + "Insert Statement (Informative). ")
    public void Requirement53() throws AssertionError, SQLException
    {
        // Verify the tables are defined correctly
        for(final String table: this.pyramidTablesInContents)
        {
            if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), table))
            {
                this.verifyTable(new TilePyramidUserDataTableDefinition(table));
            }
            else
            {
                throw new AssertionError(String.format("The tiles table %1$s does not exist even though it is defined in the %2$s table. Either create the table %1$s or delete the record in %2$s table referring to table %1$s.",
                                                        table,
                                                        GeoPackageCore.ContentsTableName),
                                         Severity.Error);
            }
        }

        // Ensure that the pyramid tables are referenced in tile matrix set
        if(this.hasTileMatrixSetTable)
        {
            final String query2 = String.format("SELECT DISTINCT table_name "
                                              + "FROM %s WHERE data_type = 'tiles' "
                                                                 +  "AND table_name NOT IN"
                                                                             + " (SELECT DISTINCT table_name "
                                                                             + " FROM %s);",
                                               GeoPackageCore.ContentsTableName,
                                               GeoPackageTiles.MatrixSetTableName);

            try(PreparedStatement stmt2                         = this.getSqliteConnection().prepareStatement(query2);
                ResultSet         unreferencedPyramidTableInTMS = stmt2.executeQuery())
            {
                // Verify that all the pyramid user data tables are referenced in the Tile Matrix Set table
                if(unreferencedPyramidTableInTMS.next())
                {
                    Assert.fail(String.format("There are Pyramid User Data Tables that do not contain a record in the %s. Unreferenced Pyramid table: %s",
                                              GeoPackageTiles.MatrixSetTableName,
                                              unreferencedPyramidTableInTMS.getString("table_name")),
                                Severity.Error);
                }
            }
            catch(final SQLException ex)
            {
                Assert.fail(ex.getMessage(),
                            Severity.Error);
            }
        }
    }

    /**
     * Requirement 54
     *
     * <blockquote>
     * For each distinct <code>table_name</code> from the <code>
     * gpkg_tile_matrix</code> (tm) table, the tile pyramid (tp) user data
     * table <code>zoom_level</code> column value in a GeoPackage SHALL be in
     * the range min(tm.zoom_level) <= tp.zoom_level <= max(tm.zoom_level).
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 54",
                  text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, "
                              + "the tile pyramid (tp) user data table zoom_level column value in a "
                              + "GeoPackage SHALL be in the range min(tm.zoom_level) less than or equal "
                              + "to tp.zoom_level less than or equal to max(tm.zoom_level).")
    public void Requirement54() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidName: this.pyramidTablesInTileMatrix)
            {
                final String query2 = String.format("SELECT MIN(zoom_level) AS min_gtm_zoom, MAX(zoom_level) AS max_gtm_zoom FROM %s WHERE table_name = ?",
                                                    GeoPackageTiles.MatrixTableName);

                try(PreparedStatement stmt2 = this.getSqliteConnection().prepareStatement(query2))
                {
                    stmt2.setString(1, pyramidName);

                    try(ResultSet minMaxZoom = stmt2.executeQuery())
                    {
                        final int minZoom = minMaxZoom.getInt("min_gtm_zoom");
                        final int maxZoom = minMaxZoom.getInt("max_gtm_zoom");

                        if(!minMaxZoom.wasNull())
                        {
                            final String query3 = String.format("SELECT id FROM %s WHERE zoom_level < ? OR zoom_level > ?", pyramidName);

                            try(PreparedStatement stmt3        = this.getSqliteConnection().prepareStatement(query3))
                            {
                                stmt3.setInt(1, minZoom);
                                stmt3.setInt(2, maxZoom);

                                try(ResultSet invalidZooms = stmt3.executeQuery())
                                {
                                    if(invalidZooms.next())
                                    {
                                        Assert.fail(String.format("There are zoom_levels in the Pyramid User Data Table: %1$s  such that the zoom level "
                                                                    + "is bigger than the maximum zoom level: %2$d or smaller than the minimum zoom_level: %3$d"
                                                                    + " that was determined by the %4$s Table.  Invalid tile with an id of %5$d from table %6$s",
                                                                  pyramidName,
                                                                  maxZoom,
                                                                  minZoom,
                                                                  GeoPackageTiles.MatrixTableName,
                                                                  invalidZooms.getInt("id"),
                                                                  pyramidName),
                                                     Severity.Error);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Requirement 55
     *
     * <blockquote>
     * For each distinct <code>table_name</code> from the <code>
     * gpkg_tile_matrix</code> (tm) table, the tile pyramid (tp) user data
     * table <code>tile_column</code> column value in a GeoPackage SHALL be in
     * the range 0 <= tp.tile_column <= tm.matrix_width - 1 where the tm and tp
     * <code>zoom_level</code> column values are equal.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement(reference = "Requirement 55",
                 text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, "
                             + "the tile pyramid (tp) user data table tile_column column value in a "
                             + "GeoPackage SHALL be in the range 0 <= tp.tile_column <= tm.matrix_width - 1 "
                             + "where the tm and tp zoom_level column values are equal. ")
    public void Requirement55() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidName : this.pyramidTablesInTileMatrix)
            {
                // This query will only pull the incorrect values for the
                // pyramid user data table's column width, the value
                // of the tile_column value for the pyramid user data table
                // SHOULD be null otherwise those fields are in violation
                // of the range

                final String query2 = String.format("SELECT zoom_level as zl, "     +
                                                           "matrix_width as width " +
                                                    "FROM   %1$s "        +
                                                    "WHERE  table_name = ? "       +
                                                    "AND"                             +
                                                        "("                           +
                                                             "zoom_level in (SELECT zoom_level FROM %2$s WHERE tile_column < 0) "                    +
                                                             "OR "    +
                                                             "("      +
                                                                   "EXISTS(SELECT NULL FROM %2$s WHERE zoom_level = zl AND tile_column > width - 1)" +
                                                             " )"     +
                                                       " );",
                                                    GeoPackageTiles.MatrixTableName,
                                                    pyramidName);

                try(final PreparedStatement stmt2            = this.getSqliteConnection().prepareStatement(query2))
                {
                    stmt2.setString(1, pyramidName);

                    try(final ResultSet incorrectColumns = stmt2.executeQuery())
                    {
                        final List<TileData> incorrectColumnSet = JdbcUtility.map(incorrectColumns,
                                                                                  resultSet -> { final TileData tileData = new TileData();

                                                                                                 tileData.matrixWidth = resultSet.getInt("width");
                                                                                                 tileData.zoomLevel    = resultSet.getInt("zl");

                                                                                                 return tileData;
                                                                                               });

                        Assert.assertTrue(String.format("The table '%s' there are tiles with a tile_column values oustide the ranges for a particular zoom_level. \n%s",
                                                        pyramidName,
                                                        incorrectColumnSet.stream()
                                                                          .map(tileData -> String.format("\tZoom level %d has tile_column values outside of the range: [0, %d].",
                                                                                                         tileData.zoomLevel, tileData.matrixWidth - 1))
                                                                          .collect(Collectors.joining("\n"))),
                                                                           incorrectColumnSet.isEmpty(),
                                          Severity.Warning);
                        }
                }
            }
        }
    }

    /**
     * Requirement 56
     *
     * <blockquote>
     * For each distinct <code>table_name</code> from the <code>
     * gpkg_tile_matrix</code> (tm) table, the tile pyramid (tp) user data
     * table <code>tile_row</code> column value in a GeoPackage SHALL be in the
     * range 0 <= tp.tile_row <= tm.matrix_height - 1 where the tm and tp
     * <code>zoom_level</code> column values are equal.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 56",
                  text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, the tile pyramid (tp) "
                              + "user data table tile_row column value in a GeoPackage SHALL be in the range 0 <= tp.tile_row <= tm.matrix_height - 1 "
                              + "where the tm and tp zoom_level column values are equal. ")
    public void Requirement56() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidName: this.pyramidTablesInTileMatrix)
            {
                // this query will only pull the incorrect values for the
                // pyramid user data table's column height, the value
                // of the tile_row value for the pyramid user data table
                // SHOULD be null otherwise those fields are in violation
                // of the range
                final String query2 = String.format("SELECT zoom_level as zl, "       +
                                                           "matrix_height as height " +
                                                    "FROM   %1$s "                    +
                                                    "WHERE  table_name = ? "          +
                                                    "AND"                             +
                                                        "("                           +
                                                             "zoom_level in (SELECT zoom_level FROM %2$s WHERE tile_row < 0) "                    +
                                                             "OR "    +
                                                             "("      +
                                                                   "EXISTS(SELECT NULL FROM %2$s WHERE zoom_level = zl AND tile_row > height - 1)" +
                                                             " )"     +
                                                       " );",
                                                    GeoPackageTiles.MatrixTableName,
                                                    pyramidName);

                try(final PreparedStatement stmt2 = this.getSqliteConnection().prepareStatement(query2))
                {
                    stmt2.setString(1, pyramidName);

                    try(final ResultSet incorrectTileRow = stmt2.executeQuery())
                    {
                        final List<TileData> incorrectTileRowSet = JdbcUtility.map(incorrectTileRow,
                                                                                   resultSet -> { final TileData tileData = new TileData();

                                                                                                  tileData.matrixHeight = resultSet.getInt("height");
                                                                                                  tileData.zoomLevel    = resultSet.getInt("zl");

                                                                                                  return tileData;
                                                                                                });

                        Assert.assertTrue(String.format("The table '%s' there are tiles with a tile_row values oustide the ranges for a particular zoom_level. \n%s",
                                                         pyramidName,
                                                         incorrectTileRowSet.stream()
                                                                            .map(tileData -> String.format("\tZoom level %d has tile_row values outside of the range: [0, %d].",
                                                                                                           tileData.zoomLevel,
                                                                                                           tileData.matrixHeight - 1))
                                                                            .collect(Collectors.joining("\n"))),
                                          incorrectTileRowSet.isEmpty(),
                                          Severity.Warning);
                    }
                }
            }
        }
    }

    /**
     * Reference 2.2.6.1.1 Table 8. TileMatrix Set Table or View Definition para. 1
     * <blockquote>
     * The gpkg_tile_matrix_set table or updateable view defines the minimum bounding box (<code>min_x, min_y, max_x, max_y</code>)
     * and spatial reference system (<code>srs_id</code>) for all content in a tile pyramid user data table.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Reference 2.2.6.1.1 Table 8.",
                  text      = "The gpkg_tile_matrix_set table or updateable view defines the minimum bounding box (min_x, min_y, max_x, max_y)"
                               +" and spatial reference system (srs_id) for all content in a tile pyramid user data table. ")
    public void reference22611Table8() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidTable: this.allPyramidUserDataTables)
            {

                final String tileRowMaxQuery = String.format("SELECT matrix_height as height, " +
                                                              "zoom_level as zoom, "      +
                                                              "table_name "                +
                                                       "FROM gpkg_tile_matrix "           +
                                                       "WHERE table_name = ? "            +
                                                       "AND ( "                           +
                                                               "EXISTS( SELECT NULL FROM %1$s WHERE tile_row = (height - 1) AND zoom_level = zoom ) " +
                                                            ");",
                                                      pyramidTable);

                final String tileColumnMaxQuery = String.format("SELECT matrix_width as width, " +
                                                                 "zoom_level as zoom, "      +
                                                                 "table_name "                +
                                                          "FROM gpkg_tile_matrix "           +
                                                          "WHERE table_name = ? "           +
                                                          "AND ( "                           +
                                                                  "EXISTS( SELECT NULL FROM %1$s WHERE tile_column = (width - 1) AND zoom_level = zoom ) " +
                                                               ");",
                                                          pyramidTable);

                final String tileRowMinQuery = String.format("SELECT tile_row FROM %s WHERE tile_row = 0;", pyramidTable);

                final String tileColumnMinQuery = String.format("SELECT tile_column FROM %s WHERE tile_column = 0;", pyramidTable);


                try(PreparedStatement maxRowStatement    = this.getSqliteConnection().prepareStatement(tileRowMaxQuery);
                    PreparedStatement maxColumnStatement = this.getSqliteConnection().prepareStatement(tileColumnMaxQuery))
                {
                    maxRowStatement.setString(1, pyramidTable);
                    maxColumnStatement.setString(1, pyramidTable);

                    try(Statement minRowStatement = this.getSqliteConnection().createStatement();
                        Statement minColumnStatement = this.getSqliteConnection().createStatement();

                        ResultSet maxRowResults = maxRowStatement.executeQuery();
                        ResultSet maxColumnResults = maxColumnStatement.executeQuery();
                        ResultSet minRowResult    = minRowStatement.executeQuery(tileRowMinQuery);
                        ResultSet minColumnResult = minColumnStatement.executeQuery(tileColumnMinQuery))
                    {
                        final ArrayList<String> errors = new ArrayList<>();

                        if(!minColumnResult.isBeforeFirst())
                        {
                            errors.add("minimum column (0)");
                        }

                        if(!minRowResult.isBeforeFirst())
                        {
                            errors.add("minimum row (0)");
                        }

                        if(!maxColumnResults.isBeforeFirst())
                        {
                            errors.add("maximum column (matrix_width - 1)");
                        }

                        if(!maxRowResults.isBeforeFirst())
                        {
                            errors.add("maximum row (matrix_height - 1)");
                        }

                        Assert.assertTrue(String.format("There must be at least one tile in the minimum and maximum row and column in tile pyramid user data table '%s'.  The table has no tile for %s, at any zoom level.  The table's contents do not agree with its associated minimum bounding box defined by gpkg_tile_matrix_set.",
                                                       pyramidTable,
                                                       String.join(", ", errors)),
                                          errors.isEmpty(),
                                          Severity.Warning);
                    }
                }
            }
        }
    }

    /**
     * Reference 2.2.6.1.2 para. 1
     * <blockquote>
     * The minimum bounding box defined in the gpkg_tile_matrix_set table or view for a tile pyramid
     * user data table SHALL be exact so that the bounding box coordinates for individual tiles in a
     * tile pyramid MAY be calculated based on the column values for the user data table in the
     * gpkg_tile_matrix table or view.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Reference 2.2.6.1.2 para 1.",
                  text      = "The minimum bounding box defined in the gpkg_tile_matrix_set table or view for a "
                            + "tile pyramid user data table SHALL be exact so that the bounding box coordinates "
                            + "for individual tiles in a tile pyramid MAY be calculated based on the column values "
                            + "for the user data table in the gpkg_tile_matrix table or view. ")
    public void reference() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable && this.hasTileMatrixSetTable)
        {
            for(final String tableName : this.allPyramidUserDataTables)
            {
                final String query1 = String.format("SELECT table_name, "
                                                         + "zoom_level, "
                                                         + "pixel_x_size, "
                                                         + "pixel_y_size,"
                                                         + "matrix_width,"
                                                         + "matrix_height,"
                                                         + "tile_width,"
                                                         + "tile_height "
                                                  + "FROM %s "
                                                  + "WHERE table_name = ? "
                                                  + "ORDER BY zoom_level ASC;", GeoPackageTiles.MatrixTableName);

                try(final PreparedStatement stmt = this.getSqliteConnection().prepareStatement(query1))
                {

                    stmt.setString(1, tableName);

                    try(final ResultSet         pixelInfo = stmt.executeQuery())
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

                    final String query2 = String.format("SELECT min_x, min_y, max_x, max_y FROM %s WHERE table_name = ?",
                                                        GeoPackageTiles.MatrixSetTableName);

                        try(final PreparedStatement stmt2 = this.getSqliteConnection().prepareStatement(query2))
                        {
                            stmt2.setString(1, tableName);
                            try(ResultSet boundingBoxRS = stmt2.executeQuery())
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

                                    Assert.assertTrue(String.format("The pixel_x_size and pixel_y_size should satisfy these two equations:"
                                                                    + "\n\tpixel_x_size = (bounding box width  / matrix_width)  / tile_width "
                                                                    + "AND \n\tpixel_y_size = (bounding box height / matrix_height)/ tile_height.  "
                                                                    + "\nBased on these two equations, the following pixel values are invalid for the table '%s'.:\n%s ",
                                                                    tableName,
                                                                    invalidPixelValues.stream()
                                                                                      .map(tileData -> String.format("\tInvalid pixel_x_size: %f, Invalid pixel_y_size: %f at zoom_level %d",
                                                                                                                     tileData.pixelXSize,
                                                                                                                     tileData.pixelYSize,
                                                                                                                     tileData.zoomLevel))
                                                                                      .collect(Collectors.joining("\n"))),
                                                      invalidPixelValues.isEmpty(),
                                                      Severity.Warning);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean verifyData(final byte[] tileData) throws IOException
    {
        if(tileData == null)
        {
            return false;
        }

        try(ByteArrayInputStream        byteArray  = new ByteArrayInputStream(tileData);
            MemoryCacheImageInputStream cacheImage = new MemoryCacheImageInputStream(byteArray))
        {
            return TilesVerifier.canReadImage(pngImageReaders, cacheImage) || TilesVerifier.canReadImage(jpegImageReaders, cacheImage);
        }
    }

    private static boolean validPixelValues(final TileData tileData, final BoundingBox boundingBox)
    {

        return isEqual(tileData.pixelXSize, (boundingBox.getWidth()  / tileData.matrixWidth)  / tileData.tileWidth) &&
               isEqual(tileData.pixelYSize, (boundingBox.getHeight() / tileData.matrixHeight) / tileData.tileHeight);
    }

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
     *
     * @return true if the gpkg_tile_matrix table exists
     * @throws AssertionError
     *             throws an assertion error if the gpkg_tile_matrix table
     *             doesn't exist and the GeoPackage contains a tiles table
     */
    private boolean tileMatrixTableExists() throws SQLException
    {
       return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixTableName);
    }

    private boolean tileMatrixSetTableExists() throws SQLException
    {
       return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixSetTableName);
    }

    private static boolean canReadImage(final Collection<ImageReader> imageReaders, final ImageInputStream image)
    {
        return imageReaders.stream()
                           .anyMatch(imageReader -> { try
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
                                                          catch(final Exception e)
                                                          {
                                                              e.printStackTrace();
                                                          }
                                                      }
                                                    });
    }

    private class TileData implements Comparable<TileData>
    {
        int     matrixWidth;
        int     matrixHeight;
        int     zoomLevel;
        Integer tileID;
        double  pixelXSize;
        double  pixelYSize;
        int     tileWidth;
        int     tileHeight;

        @Override
        public int compareTo(final TileData other)
        {
           return this.tileID.compareTo(other.tileID);
        }
    }

    private Set<String> allPyramidUserDataTables;
    private Set<String> pyramidTablesInContents;
    private Set<String> pyramidTablesInTileMatrix;
    private boolean     hasTileMatrixTable;
    private boolean     hasTileMatrixSetTable;

    private static final TableDefinition TileMatrixSetTableDefinition;
    private static final TableDefinition TileMatrixTableDefinition;

    private static final Collection<ImageReader> jpegImageReaders;
    private static final Collection<ImageReader> pngImageReaders;

    static
    {
        jpegImageReaders = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/jpeg"));
        pngImageReaders  = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/png"));

        final Map<String, ColumnDefinition> tileMatrixSetColumns = new HashMap<>();

        tileMatrixSetColumns.put("table_name",  new ColumnDefinition("TEXT",     true, true,  true,  null));
        tileMatrixSetColumns.put("srs_id",      new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixSetColumns.put("min_x",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("min_y",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("max_x",       new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixSetColumns.put("max_y",       new ColumnDefinition("DOUBLE",   true, false, false, null));

        TileMatrixSetTableDefinition = new TableDefinition(GeoPackageTiles.MatrixSetTableName,
                                                           tileMatrixSetColumns,
                                                           new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageCore.SpatialRefSysTableName, "srs_id", "srs_id"),
                                                                                 new ForeignKeyDefinition(GeoPackageCore.ContentsTableName, "table_name", "table_name"))));
        final Map<String, ColumnDefinition> tileMatrixColumns = new HashMap<>();

        tileMatrixColumns.put("table_name",     new ColumnDefinition("TEXT",     true, true,  true,  null));
        tileMatrixColumns.put("zoom_level",     new ColumnDefinition("INTEGER",  true, true,  true,  null));
        tileMatrixColumns.put("matrix_width",   new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("matrix_height",  new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("tile_width",     new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("tile_height",    new ColumnDefinition("INTEGER",  true, false, false, null));
        tileMatrixColumns.put("pixel_x_size",   new ColumnDefinition("DOUBLE",   true, false, false, null));
        tileMatrixColumns.put("pixel_y_size",   new ColumnDefinition("DOUBLE",   true, false, false, null));

        TileMatrixTableDefinition = new TableDefinition(GeoPackageTiles.MatrixTableName,
                                                        tileMatrixColumns,
                                                        new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageCore.ContentsTableName, "table_name", "table_name"))));


    }
}
