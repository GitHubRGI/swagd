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

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.UniqueDefinition;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.rgi.geopackage.verification.Assert.assertTrue;
import static com.rgi.geopackage.verification.Assert.fail;

/**
 * @author Jenifer Cochran
 * @author Luke Lambert
 *
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
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
    public TilesVerifier(final Connection        sqliteConnection,
                         final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        this.hasTileMatrixTable    = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixTableName);
        this.hasTileMatrixSetTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageTiles.MatrixSetTableName);

        this.tileTableNames = JdbcUtility.selectFilter(this.getSqliteConnection(),
                                                       "SELECT tbl_name FROM sqlite_master WHERE tbl_name NOT LIKE 'gpkg_%' AND (type = 'table' OR type = 'view');",
                                                       null,
                                                       resultSet -> resultSet.getString("tbl_name"),
                                                       tableName -> { try
                                                                      {
                                                                          this.verifyTable(tableName,
                                                                                           TilePyramidUserDataTableColumns,
                                                                                           TilePyramidUserDataTableForeignKeys,
                                                                                           TilePyramidUserDataTableUniqueColumnGroups);
                                                                          return true;
                                                                      }
                                                                      catch(final SQLException | AssertionError ignored)
                                                                      {
                                                                          return false;
                                                                      }
                                                                    });

        this.contentsTileTableNames = JdbcUtility.select(this.getSqliteConnection(),
                                                         String.format("SELECT table_name FROM %s WHERE data_type = 'tiles';", GeoPackageCore.ContentsTableName),
                                                         null,
                                                         resultSet -> resultSet.getString("table_name"));



        this.tileTablesInTileMatrix = this.hasTileMatrixTable ? JdbcUtility.selectFilter(this.getSqliteConnection(),
                                                                                      String.format("SELECT DISTINCT table_name FROM %s;", GeoPackageTiles.MatrixTableName),
                                                                                      null,
                                                                                      resultSet -> resultSet.getString("table_name"),
                                                                                      pyramidName -> DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), pyramidName))
                                                              : Collections.emptyList();
    }

    /**
     * Requirement 34
     *
     * <blockquote>
     * The {@code gpkg_contents} table SHALL contain a row with a {@code
     * data_type} column value of 'tiles' for each tile pyramid user data table
     * or view.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 34",
                 text      = "The gpkg_contents table SHALL contain a row with a data_type column value of \"tiles\" for each tile pyramid user data table or view.")
    public void requirement34() throws AssertionError
    {
        final Collection<String> missingTileTableNames = this.tileTableNames
                                                             .stream()
                                                             .filter(tableName -> !this.contentsTileTableNames.contains(tableName))
                                                             .collect(Collectors.toList());

        assertTrue(String.format("The following table(s) match the specification for a tile pyramid user data table, but are not refrenced in %s: %s.",
                                 GeoPackageCore.ContentsTableName,
                                 String.join(", ", missingTileTableNames)),
                   missingTileTableNames.isEmpty(),
                   Severity.Warning);
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
     * @throws SQLException throws if an SQLException occurs
     * @throws AssertionError  throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 35",
                 text      = "In a GeoPackage that contains a tile pyramid user data table that contains tile data, by default, zoom level pixel sizes for that table SHALL vary by a factor of 2 between zoom levels in tile matrix metadata table.")
    public void requirement35() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final Map<String, Collection<Pair<Integer, Integer>>> tableNamesAndBadAdjacentZoomLevels = new HashMap<>();

            for(final String tableName : this.tileTableNames)
            {
                final Collection<Pair<Integer, Integer>> badAdjacentZoomLevels = new LinkedList<>();

                final String query = String.format("SELECT zoom_level,\n" +
                                                   "       pixel_x_size,\n" +
                                                   "       pixel_y_size\n" +
                                                   "FROM %s\n" +
                                                   "WHERE table_name = ? " +
                                                   "ORDER BY zoom_level ASC;",
                                                   GeoPackageTiles.MatrixTableName);

                final List<Pair<Integer, Pair<Double, Double>>> zoomLevelPixelSizes = JdbcUtility.select(this.getSqliteConnection(),
                                                                                                         query,
                                                                                                         preparedStatement -> preparedStatement.setString(1, tableName),
                                                                                                         resultSet -> Pair.of(resultSet.getInt("zoom_level"),
                                                                                                                              Pair.of(resultSet.getDouble("pixel_x_size"),
                                                                                                                                      resultSet.getDouble("pixel_y_size"))));

                for(int zoomLevelIndex = 0; zoomLevelIndex < zoomLevelPixelSizes.size()-1; ++zoomLevelIndex)
                {
                    final Pair<Integer, Pair<Double, Double>> currentZoomLevelPixelSize = zoomLevelPixelSizes.get(zoomLevelIndex);
                    final Pair<Integer, Pair<Double, Double>> nextZoomLevelPixelSize    = zoomLevelPixelSizes.get(zoomLevelIndex + 1);

                    final int currentZoomLevel = currentZoomLevelPixelSize.getLeft();
                    final int nextZoomLevel    = nextZoomLevelPixelSize.getLeft();

                    final double currentPixelXSize = currentZoomLevelPixelSize.getRight().getLeft();
                    final double currentPixelYSize = currentZoomLevelPixelSize.getRight().getRight();
                    final double nextPixelXSize    = nextZoomLevelPixelSize.getRight().getLeft();
                    final double nextPixelYSize    = nextZoomLevelPixelSize.getRight().getRight();

                    if(currentZoomLevel == nextZoomLevel - 1)
                    {
                        //noinspection MagicNumber
                        if(!TilesVerifier.isEqual((currentPixelXSize / 2.0), nextPixelXSize) ||
                           !TilesVerifier.isEqual((currentPixelYSize / 2.0), nextPixelYSize))
                        {
                            badAdjacentZoomLevels.add(Pair.of(currentZoomLevel, nextZoomLevel));
                        }
                    }
                }

                if(!badAdjacentZoomLevels.isEmpty())
                {
                    tableNamesAndBadAdjacentZoomLevels.put(tableName, badAdjacentZoomLevels);
                }
            }

            assertTrue(String.format("The following tile table(s) have adjacent zoom levels with pixel sizes that do not vary by a factor of 2:\n%s",
                                     tableNamesAndBadAdjacentZoomLevels.entrySet()
                                                                       .stream()
                                                                       .map(entrySet -> String.format("%s: %s",
                                                                                                      entrySet.getKey(),
                                                                                                      entrySet.getValue()
                                                                                                              .stream()
                                                                                                              .map(adjacentZoomLevels -> String.format("(%d, %d)",
                                                                                                                                                       adjacentZoomLevels.getLeft(),
                                                                                                                                                       adjacentZoomLevels.getRight()))
                                                                                                              .collect(Collectors.joining(", "))))
                                                                       .collect(Collectors.joining("\n"))),
                                     tableNamesAndBadAdjacentZoomLevels.isEmpty(),
                                     Severity.Warning);
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
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 36",
                 text      = "In a GeoPackage that contains a tile pyramid user data table that contains tile data SHALL store that tile data in MIME type image/jpeg or image/png")
    public void requirement36() throws SQLException, AssertionError
    {
        assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full.name(),
                   this.verificationLevel == VerificationLevel.Full,
                   Severity.Skipped);

        final Map<String, Collection<Integer>> tableNamesAndBadTileIds = new HashMap<>();

        for(final String tableName : this.tileTableNames)
        {
            final Collection<Integer> badIds = JdbcUtility.filterSelect(this.getSqliteConnection(),
                                                                        String.format("SELECT tile_data, id FROM %s;", tableName),
                                                                        null,
                                                                        resultSet -> !TilesVerifier.isAcceptedImageFormat(resultSet.getBytes("tile_data")),
                                                                        resultSet -> resultSet.getInt("id"));

            if(!badIds.isEmpty())
            {
                tableNamesAndBadTileIds.put(tableName, badIds);
            }
        }

        assertTrue(String.format("The following tile table(s) and id(s) correspond to images in an incorrect format:\n%s",
                                 tableNamesAndBadTileIds.entrySet()
                                                        .stream()
                                                        .map(entrySet -> String.format("%s: %s",
                                                                                       entrySet.getKey(),
                                                                                       entrySet.getValue()
                                                                                               .stream()
                                                                                               .map(Object::toString)
                                                                                               .collect(Collectors.joining(", "))))
                                                        .collect(Collectors.joining("\n"))),
                   tableNamesAndBadTileIds.isEmpty(),
                   Severity.Warning);
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
                 text      = "In a GeoPackage that contains a tile pyramid user data table that contains tile data that is not MIME type image png, by default SHALL store that tile data in MIME type image jpeg")
    public void requirement37()
    {
        // This requirement is tested through Requirement 35 test in TilesVerifier.
    }

    /**
     * Requirement 38
     *
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain
     * {@code gpkg_tile_matrix_set} table or view per <a href=
     * "http://www.geopackage.org/spec/#tile_matrix_set_data_table_definition">
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
                 text      = "A GeoPackage that contains a tile pyramid user data table SHALL contain gpkg_tile_matrix_set table or view per Table Definition, Tile Matrix Set Table or View Definition and gpkg_tile_matrix_set Table Creation SQL. ")
    public void requirement38() throws AssertionError, SQLException
    {
        if(!this.tileTableNames.isEmpty())
        {
            assertTrue(String.format("Missing %s table.",
                                     GeoPackageTiles.MatrixSetTableName),
                       this.hasTileMatrixSetTable,
                       Severity.Error);

            try
            {
                final Map<String, ColumnDefinition> tileMatrixSetColumns = new HashMap<>();

                tileMatrixSetColumns.put("table_name", new ColumnDefinition("TEXT",    true, true,  true,  null));
                tileMatrixSetColumns.put("srs_id",     new ColumnDefinition("INTEGER", true, false, false, null));
                tileMatrixSetColumns.put("min_x",      new ColumnDefinition("DOUBLE",  true, false, false, null));
                tileMatrixSetColumns.put("min_y",      new ColumnDefinition("DOUBLE",  true, false, false, null));
                tileMatrixSetColumns.put("max_x",      new ColumnDefinition("DOUBLE",  true, false, false, null));
                tileMatrixSetColumns.put("max_y",      new ColumnDefinition("DOUBLE",  true, false, false, null));

                this.verifyTable(GeoPackageTiles.MatrixSetTableName,
                                 tileMatrixSetColumns,
                                 new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_spatial_ref_sys", "srs_id",     "srs_id"),
                                                             new ForeignKeyDefinition("gpkg_contents",        "table_name", "table_name"))),
                                 Collections.emptyList());
            }
            catch(final Throwable th)
            {
                fail(String.format("Bad %s table definition: %s",
                                   GeoPackageTiles.MatrixSetTableName,
                                   th.getMessage()),
                     Severity.Error);
            }
        }
    }

    /**
     * Requirement 39
     *
     * <blockquote>
     * Values of the {@code gpkg_tile_matrix_set} {@code table_name} column
     * SHALL reference values in the gpkg_contents table_name column for rows
     * with a data type of "tiles".
     * </blockquote>
     *
     * @throws SQLException throws if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 39",
                 text      = "Values of the gpkg_tile_matrix_set table_name column SHALL reference values in the gpkg_contents table_name column for rows with a data type of \"tiles\".")
    public void requirement39() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final Collection<String> tableNames = JdbcUtility.select(this.getSqliteConnection(),
                                                                     String.format("SELECT table_name FROM %s;", GeoPackageTiles.MatrixSetTableName),
                                                                     null,
                                                                     resultSet -> resultSet.getString("table_name"));

            final Collection<String> missingTableReference = tableNames.stream()
                                                                       .filter(tableName -> !this.contentsTileTableNames.contains(tableName))
                                                                       .collect(Collectors.toList());

            assertTrue(String.format("The following table name(s) in %s are not referenced in the %s table: %s",
                                     GeoPackageTiles.MatrixSetTableName,
                                     GeoPackageCore.ContentsTableName,
                                     String.join(", ", missingTableReference)),
                       missingTableReference.isEmpty(),
                       Severity.Warning);
        }
    }

    /**
     * Requirement 40
     *
     * <blockquote>
     * The {@code gpkg_tile_matrix_set} table or view SHALL contain one row
     * record for each tile pyramid user data table.
     * </blockquote>
     *
     * @throws SQLException throws if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 40",
                 text      = "The gpkg_tile_matrix_set table or view SHALL contain one row record for each tile pyramid user data table.")
    public void requirement40() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final Collection<String> tableNames = JdbcUtility.select(this.getSqliteConnection(),
                                                                     String.format("SELECT table_name FROM %s;", GeoPackageTiles.MatrixSetTableName),
                                                                     null,
                                                                     resultSet -> resultSet.getString("table_name"));

            final Collection<String> missingTableReferences = this.tileTableNames
                                                                  .stream()
                                                                  .filter(tableName -> !tableNames.contains(tableName))
                                                                  .collect(Collectors.toList());

            assertTrue(String.format("The following pyramid user data tables are not referenced in %s: %s",
                                     GeoPackageTiles.MatrixSetTableName,
                                     String.join(", ", missingTableReferences)),
                       missingTableReferences.isEmpty(),
                       Severity.Error);
        }
    }

    /**
     * Requirement 41
     *
     * <blockquote>
     * Values of the {@code gpkg_tile_matrix_set} {@code srs_id} column SHALL
     * reference values in the {@code gpkg_spatial_ref_sys} {@code srs_id}
     * column.
     * </blockquote>
     *
     * @throws SQLException throws if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 41",
                  text      = "Values of the gpkg_tile_matrix_set srs_id column SHALL reference values in the gpkg_spatial_ref_sys srs_id column.")
    public void requirement41() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixSetTable)
        {
            final String query1 = String.format("SELECT srs_id from %s " +
                                                "WHERE srs_id NOT IN" +
                                                             "(SELECT srs_id " +
                                                             "FROM %s);",
                                              GeoPackageTiles.MatrixSetTableName,
                                              GeoPackageCore.SpatialRefSysTableName);

            final Collection<Integer> undefinedSrses = JdbcUtility.select(this.getSqliteConnection(),
                                                                          query1,
                                                                          null,
                                                                          resultSet -> resultSet.getInt("srs_id"));

            assertTrue(String.format("The %s table contains references to srs_ids not defined in the %s: %s",
                                     GeoPackageTiles.MatrixSetTableName,
                                     GeoPackageCore.SpatialRefSysTableName,
                                     undefinedSrses.stream()
                                                   .map(Object::toString)
                                                   .collect(Collectors.joining(", "))),
                       undefinedSrses.isEmpty(),
                       Severity.Error);
        }
    }

    /**
     * Requirement 42
     *
     * <blockquote>
     * A GeoPackage that contains a tile pyramid user data table SHALL contain
     * a {@code gpkg_tile_matrix} table or view per clause 2.2.7.1.1 <a href=
     * "http://www.geopackage.org/spec/#tile_matrix_data_table_definition">
     * Table Definition</a>, Table <a href=
     * "http://www.geopackage.org/spec/#gpkg_tile_matrix_cols">Tile Matrix
     * Metadata Table or View Definition</a> and Table <a href=
     * "http://www.geopackage.org/spec/#gpkg_tile_matrix_sql">
     * gpkg_tile_matrix Table Creation SQL</a>.
     * </blockquote>
     *
     * @throws AssertionError
     *             throws if the GeoPackage fails to meet the requirement
     * @throws SQLException
     *             throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 42",
                  text      = "A GeoPackage that contains a tile pyramid user data table SHALL contain a gpkg_tile_matrix table or view per clause 2.2.7.1.1 Table Definition, Table Tile Matrix Metadata Table or View Definition and Table gpkg_tile_matrix Table Creation SQL.")
    public void requirement42() throws AssertionError, SQLException
    {
        if(!this.tileTableNames.isEmpty())
        {
            assertTrue(String.format("Missing %s definition.",
                                     GeoPackageTiles.MatrixTableName),
                       this.hasTileMatrixTable,
                       Severity.Error);

            try
            {
                final Map<String, ColumnDefinition> tileMatrixColumns = new HashMap<>();

                tileMatrixColumns.put("table_name",     new ColumnDefinition("TEXT",    true, true,  true,  null));
                tileMatrixColumns.put("zoom_level",     new ColumnDefinition("INTEGER", true, true,  true,  null));
                tileMatrixColumns.put("matrix_width",   new ColumnDefinition("INTEGER", true, false, false, null));
                tileMatrixColumns.put("matrix_height",  new ColumnDefinition("INTEGER", true, false, false, null));
                tileMatrixColumns.put("tile_width",     new ColumnDefinition("INTEGER", true, false, false, null));
                tileMatrixColumns.put("tile_height",    new ColumnDefinition("INTEGER", true, false, false, null));
                tileMatrixColumns.put("pixel_x_size",   new ColumnDefinition("DOUBLE",  true, false, false, null));
                tileMatrixColumns.put("pixel_y_size",   new ColumnDefinition("DOUBLE",  true, false, false, null));

                this.verifyTable(GeoPackageTiles.MatrixTableName,
                                 tileMatrixColumns,
                                 new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_contents", "table_name", "table_name"))),
                                 Collections.emptyList());
            }
            catch(final Throwable th)
            {
                fail(String.format("Bad %s table definition: %s",
                                   GeoPackageTiles.MatrixTableName,
                                   th.getMessage()),
                     Severity.Error);
            }
        }
    }

    /**
     * Requirement 43
     *
     * <blockquote>
     * Values of the {@code gpkg_tile_matrix} {@code table_name} column SHALL
     * reference values in the {@code gpkg_contents} {@code table_name} column
     * for rows with a {@code data_type} of 'tiles'.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 43",
                  text      = "Values of the gpkg_tile_matrix table_name column SHALL reference values in the gpkg_contents table_name column for rows with a data_type of 'tiles'.")
    public void requirement43() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT table_name FROM %s AS tm\n" +
                                               "WHERE table_name\n"                +
                                               "NOT IN (SELECT table_name\n"       +
                                               "        FROM %s AS gc\n"           +
                                               "        WHERE tm.table_name = gc.table_name AND gc.data_type = 'tiles');",
                                              GeoPackageTiles.MatrixTableName,
                                              GeoPackageCore.ContentsTableName);

            final Collection<String> unreferencedTables = JdbcUtility.select(this.getSqliteConnection(),
                                                                             query,
                                                                             null,
                                                                             resultSet -> resultSet.getString("table_name"));

            assertTrue(String.format("The following table_name values in the %s table do not reference entries in the %s table: %s",
                                        GeoPackageTiles.MatrixTableName,
                                        GeoPackageCore.ContentsTableName,
                                        String.join(", ", unreferencedTables)),
                       unreferencedTables.isEmpty(),
                       Severity.Warning);
        }
    }

    /**
     * Requirement 44
     *
     * <blockquote>
     * The {@code gpkg_tile_matrix} table or view SHALL contain one row
     * record for each zoom level that contains one or more tiles in each tile
     * pyramid user data table or view.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 44",
                  text      = "The gpkg_tile_matrix table or view SHALL contain one row record for each zoom level that contains one or more tiles in each tile pyramid user data table or view.")
    public void requirement44() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            for(final String tableName : this.tileTableNames)
            {
                final Collection<Integer> tileMatrixZooms = JdbcUtility.select(this.getSqliteConnection(),
                                                                               String.format("SELECT DISTINCT zoom_level FROM %s WHERE table_name = ? ORDER BY zoom_level;", GeoPackageTiles.MatrixTableName),
                                                                               preparedStatement -> preparedStatement.setString(1, tableName),
                                                                               resultSet -> resultSet.getInt("zoom_level"));

                final Collection<Integer> tilePyramidZooms = JdbcUtility.select(this.getSqliteConnection(),
                                                                                String.format("SELECT DISTINCT zoom_level FROM %s ORDER BY zoom_level;", tableName),
                                                                                null,
                                                                                resultSet -> resultSet.getInt("zoom_level"));

                for(final Integer zoom: tilePyramidZooms)
                {
                    assertTrue(String.format("The %s does not contain a row record for zoom level %d in the Pyramid User Data Table %s.",
                                                    GeoPackageTiles.MatrixTableName,
                                                    zoom,
                                                    tableName),
                               tileMatrixZooms.contains(zoom),
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 45
     *
     * <blockquote>
     * The width of a tile matrix (the difference between {@code min_x} and
     * {@code max_x} in {@code gpkg_tile_matrix_set}) SHALL equal the product
     * of {@code matrix_width}, {@code tile_width}, and {@code pixel_x_size}
     * for that zoom level. Similarly, height of a tile matrix (the difference
     * between {@code min_y} and {@code max_y} in {@code gpkg_tile_matrix_set})
     * SHALL equal the product of {@code matrix_height}, {@code tile_height},
     * and {@code pixel_y_size} for that zoom level.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 45",
                  text      = "The minimum bounding box defined in the gpkg_tile_matrix_set table or view for a tile pyramid user data table SHALL be exact so that the bounding box coordinates for individual tiles in a tile pyramid MAY be calculated based on the column values for the user data table in the gpkg_tile_matrix table or view.")
    public void requirement45() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable &&
           this.hasTileMatrixSetTable)
        {
            final String query1 = String.format("SELECT zoom_level, "   +
                                                    "       pixel_x_size, " +
                                                    "       pixel_y_size,"  +
                                                    "       matrix_width,"  +
                                                    "       matrix_height," +
                                                    "       tile_width,"    +
                                                    "       tile_height "   +
                                                    "FROM %s "              +
                                                    "WHERE table_name = ? " +
                                                    "ORDER BY zoom_level ASC;",
                                                    GeoPackageTiles.MatrixTableName);

            final Map<String, Collection<Integer>> tableNamesWithBadZooms = new HashMap<>();

            for(final String tableName : this.tileTableNames)
            {
                final BoundingBox boundingBox = JdbcUtility.selectOne(this.getSqliteConnection(),
                                                                      String.format("SELECT min_x, min_y, max_x, max_y FROM %s WHERE table_name = ?", GeoPackageTiles.MatrixSetTableName),
                                                                      preparedStatement -> preparedStatement.setString(1, tableName),
                                                                      resultSet -> new BoundingBox(resultSet.getDouble("min_x"),
                                                                                                   resultSet.getDouble("min_y"),
                                                                                                   resultSet.getDouble("max_x"),
                                                                                                   resultSet.getDouble("max_y")));

                if(boundingBox != null)
                {
                    final Collection<Integer> zoomLevels = JdbcUtility.filterSelect(this.getSqliteConnection(),
                                                                                    query1,
                                                                                    preparedStatement -> preparedStatement.setString(1, tableName),
                                                                                    resultSet -> {  final double pixelXSize   = resultSet.getDouble("pixel_x_size");
                                                                                                    final double pixelYSize   = resultSet.getDouble("pixel_y_size");
                                                                                                    final double matrixWidth  = resultSet.getInt   ("matrix_width");
                                                                                                    final double matrixHeight = resultSet.getInt   ("matrix_height");
                                                                                                    final double tileWidth    = resultSet.getInt   ("tile_width");
                                                                                                    final double tileHeight   = resultSet.getInt   ("tile_height");

                                                                                                    return !isEqual(pixelXSize, (boundingBox.getWidth()  / matrixWidth)  / tileWidth) ||
                                                                                                           !isEqual(pixelYSize, (boundingBox.getHeight() / matrixHeight) / tileHeight);
                                                                                                 },
                                                                                    resultSet -> resultSet.getInt("zoom_level"));

                    if(!zoomLevels.isEmpty())
                    {
                        tableNamesWithBadZooms.put(tableName, zoomLevels);
                    }
                }
            }

            assertTrue(String.format("The follow tiles tables have zoom levels with pixel_x_size and pixel_y_size that fail to satisfy these two equations: pixel_x_size = (bounding box width  / matrix_width) / tile_width AND tpixel_y_size = (bounding box height / matrix_height) / tile_height:\n%s",
                                     tableNamesWithBadZooms.entrySet()
                                                           .stream()
                                                           .map(entrySet -> String.format("%s: %s",
                                                                                          entrySet.getKey(),
                                                                                          entrySet.getValue()
                                                                                                  .stream()
                                                                                                  .map(Object::toString)
                                                                                                  .collect(Collectors.joining(", "))))
                                                           .collect(Collectors.joining("\n"))),
                                     tableNamesWithBadZooms.isEmpty(),
                                     Severity.Warning);
        }
    }



    /**
     * Requirement 46
     *
     * <blockquote>
     * The {@code zoom_level} column value in a {@code gpkg_tile_matrix} table
     * row SHALL not be negative.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 46",
                  text      = "The zoom_level column value in a gpkg_tile_matrix table row SHALL not be negative.")
    public void requirement46() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final Collection<String> tableNamesWithNegativeZoomLevels = JdbcUtility.select(this.getSqliteConnection(),
                                                                                           String.format("SELECT DISTINCT table_name FROM %s WHERE zoom_level < 0;", GeoPackageTiles.MatrixTableName),
                                                                                           null,
                                                                                           resultSet -> resultSet.getString("table_name"));

            assertTrue(String.format("The following tables have negative zoom level entires in the %s table: %s",
                       GeoPackageTiles.MatrixTableName,
                       String.join(", ", tableNamesWithNegativeZoomLevels)),
                       tableNamesWithNegativeZoomLevels.isEmpty(),
                       Severity.Error);
        }
    }

    /**
     * Requirement 47
     *
     * <blockquote>
     * {@code matrix_width} column value in a {@code gpkg_tile_matrix} table
     * row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 47",
                  text      = "The matrix_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement47() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(matrix_width) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(final Statement stmt             = this.getSqliteConnection().createStatement();
                final ResultSet minMatrixWidthRS = stmt.executeQuery(query))
            {
                final int minMatrixWidth = minMatrixWidthRS.getInt("min(matrix_width)");

                if(!minMatrixWidthRS.wasNull())
                {
                    assertTrue(String.format("The matrix_width in %s must be greater than 0. Invalid matrix_width: %d",
                                             GeoPackageTiles.MatrixTableName,
                                             minMatrixWidth),
                               minMatrixWidth > 0,
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 48
     *
     * <blockquote>
     * {@code matrix_height} column value in a {@code gpkg_tile_matrix} table
     * row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 48",
                  text      = "The matrix_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement48() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(matrix_height) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(final Statement stmt              = this.getSqliteConnection().createStatement();
                final ResultSet minMatrixHeightRS = stmt.executeQuery(query))
            {
                final int minMatrixHeight = minMatrixHeightRS.getInt("min(matrix_height)");

                if(!minMatrixHeightRS.wasNull())
                {
                    assertTrue(String.format("The matrix_height in %s must be greater than 0. Invalid matrix_height: %d",
                                             GeoPackageTiles.MatrixTableName,
                                             minMatrixHeight),
                               minMatrixHeight > 0,
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 49
     *
     * <blockquote>
     * {@code tile_width} column value in a {@code gpkg_tile_matrix}
     * table row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 49",
                  text      = "The tile_width column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement49() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(tile_width) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(final Statement stmt           = this.getSqliteConnection().createStatement();
                final ResultSet minTileWidthRS = stmt.executeQuery(query))
            {
                final int minTileWidth = minTileWidthRS.getInt("min(tile_width)");

                if(!minTileWidthRS.wasNull())
                {
                    assertTrue(String.format("The tile_width in %s must be greater than 0. Invalid tile_width: %d",
                                             GeoPackageTiles.MatrixTableName,
                                             minTileWidth),
                               minTileWidth > 0,
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 50
     *
     * <blockquote>
     * {@code tile_height} column value in a {@code gpkg_tile_matrix} table row
     * SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 50",
                 text      = "The tile_height column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement50() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(tile_height) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(final Statement stmt            = this.getSqliteConnection().createStatement();
                final ResultSet minTileHeightRS = stmt.executeQuery(query))
            {
                final int testMinTileHeight = minTileHeightRS.getInt("min(tile_height)");

                if(!minTileHeightRS.wasNull())
                {
                    assertTrue(String.format("The tile_height in %s must be greater than 0. Invalid tile_height: %d",
                                             GeoPackageTiles.MatrixTableName,
                                             testMinTileHeight),
                               testMinTileHeight > 0,
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 51
     *
     * <blockquote>
     * {@code pixel_x_size} column value in a {@code gpkg_tile_matrix} table
     * row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 51",
                  text      = "The pixel_x_size column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement51() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final String query = String.format("SELECT min(pixel_x_size) FROM %s;", GeoPackageTiles.MatrixTableName);

            try(final Statement stmt            = this.getSqliteConnection().createStatement();
                final ResultSet minPixelXSizeRS = stmt.executeQuery(query))
            {

                final double minPixelXSize = minPixelXSizeRS.getDouble("min(pixel_x_size)");

                if(!minPixelXSizeRS.wasNull())
                {
                    assertTrue(String.format("The pixel_x_size in %s must be greater than 0. Invalid pixel_x_size: %f",
                                             GeoPackageTiles.MatrixTableName,
                                             minPixelXSize),
                               minPixelXSize > 0,
                               Severity.Error);
                }
            }
        }
    }

    /**
     * Requirement 52
     *
     * <blockquote>
     * {@code pixel_y_size} column value in a {@code gpkg_tile_matrix} table
     * row SHALL be greater than 0.
     * </blockquote>
     *
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 52",
                  text      = "The pixel_y_size column value in a gpkg_tile_matrix table row SHALL be greater than 0.")
    public void requirement52() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
           final String query = String.format("SELECT min(pixel_y_size) FROM %s;", GeoPackageTiles.MatrixTableName);

           try(final Statement stmt            = this.getSqliteConnection().createStatement();
               final ResultSet minPixelYSizeRS = stmt.executeQuery(query))
           {
               final double minPixelYSize = minPixelYSizeRS.getDouble("min(pixel_y_size)");

               if(!minPixelYSizeRS.wasNull())
               {
                   assertTrue(String.format("The pixel_y_size in %s must be greater than 0. Invalid pixel_y_size: %f",
                                            GeoPackageTiles.MatrixTableName,
                                            minPixelYSize),
                              minPixelYSize > 0,
                              Severity.Error);
               }
           }
        }
    }

    /**
     * Requirement 53
     *
     * <blockquote>
     * The {@code pixel_x_size} and {@code pixel_y_size} column values for
     * {@code zoom_level} column values in a {@code gpkg_tile_matrix} table
     * sorted in ascending order SHALL be sorted in descending order.
     * </blockquote>
     *
     * @throws SQLException
     *             if there is a database error
     * @throws AssertionError
     *             if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 53",
                  text      = "The pixel_x_size and pixel_y_size column values for zoom_level column values in a gpkg_tile_matrix table sorted in ascending order SHALL be sorted in descending order.")
    public void requirement53() throws SQLException, AssertionError
    {
        if(this.hasTileMatrixTable)
        {
            final Map<String, Collection<Pair<Integer, Integer>>> tableNamesAndZoomLevelPairs = new HashMap<>();

            try(final PreparedStatement statement = this.getSqliteConnection().prepareStatement(String.format("SELECT zoom_level, pixel_x_size, pixel_y_size FROM %s WHERE table_name = ? ORDER BY zoom_level ASC;", GeoPackageTiles.MatrixTableName)))
            {
                for(final String tableName : this.tileTableNames)
                {
                    statement.setString(1, tableName);

                    final Collection<Pair<Integer, Integer>> badZoomLevelPairs = new LinkedList<>();

                    try(final ResultSet resultSet = statement.executeQuery())
                    {
                        if(resultSet.isBeforeFirst())
                        {
                            resultSet.next();

                            int    lastZoomLevel = resultSet.getInt   ("zoom_level");
                            double lastPixelX    = resultSet.getDouble("pixel_x_size");
                            double lastPixelY    = resultSet.getDouble("pixel_y_size");

                            while(resultSet.next())
                            {
                                final int    zoomLevel = resultSet.getInt   ("zoom_level");
                                final double pixelX    = resultSet.getDouble("pixel_x_size");
                                final double pixelY    = resultSet.getDouble("pixel_y_size");

                                if(pixelX > lastPixelX && pixelY > lastPixelY)
                                {
                                    badZoomLevelPairs.add(Pair.of(lastZoomLevel, zoomLevel));
                                }

                                lastZoomLevel = zoomLevel;
                                lastPixelX    = pixelX;
                                lastPixelY    = pixelY;
                            }
                        }
                    }

                    if(!badZoomLevelPairs.isEmpty())
                    {
                        tableNamesAndZoomLevelPairs.put(tableName, badZoomLevelPairs);
                    }
                }
            }

            assertTrue(String.format("The following tiles tables have adjacent zoom levels with pixel sizes that do not increase:\n%s",
                                     tableNamesAndZoomLevelPairs.entrySet()
                                                                .stream()
                                                                .map(entrySet -> String.format("%s: %s",
                                                                                               entrySet.getKey(),
                                                                                               entrySet.getValue()
                                                                                                       .stream()
                                                                                                       .map(pair -> String.format("(%s, %s)",
                                                                                                                                  pair.getLeft(),
                                                                                                                                  pair.getRight()))
                                                                                                       .collect(Collectors.joining(", "))))
                                                                .collect(Collectors.joining("\n"))),
                       tableNamesAndZoomLevelPairs.isEmpty(),
                       Severity.Warning);
        }
    }

    /**
     * Requirement 54
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
     * @throws SQLException if there is a database error
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     */
    @Requirement (reference = "Requirement 54",
                  text      = "Each tile matrix set in a GeoPackage SHALL "
                              + "be stored in a different tile pyramid user "
                              + "data table or updateable view with a unique "
                              + "name that SHALL have a column named \"id\" with"
                              + " column type INTEGER and PRIMARY KEY AUTOINCREMENT"
                              + " column constraints per Clause 2.2.8.1.1 Table Definition,"
                              + " Tiles Table or View Definition and EXAMPLE: tiles table "
                              + "Insert Statement (Informative).")
    public void requirement54() throws SQLException, AssertionError
    {
        // Verify the tables are defined correctly
        for(final String tableName: this.contentsTileTableNames)
        {
            this.verifyTable(tableName,
                             TilePyramidUserDataTableColumns,
                             TilePyramidUserDataTableForeignKeys,
                             TilePyramidUserDataTableUniqueColumnGroups);
        }
    }

    /**
     * Requirement 55
     *
     * <blockquote>
     * For each distinct {@code table_name} from the {@code
     * gpkg_tile_matrix} (tm) table, the tile pyramid (tp) user data
     * table {@code zoom_level} column value in a GeoPackage SHALL be in
     * the range min(tm.zoom_level) <= tp.zoom_level <= max(tm.zoom_level).
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 55",
                  text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, "
                              + "the tile pyramid (tp) user data table zoom_level column value in a "
                              + "GeoPackage SHALL be in the range min(tm.zoom_level) less than or equal "
                              + "to tp.zoom_level less than or equal to max(tm.zoom_level).")
    public void requirement55() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            for(final String pyramidName: this.tileTablesInTileMatrix)
            {
                final String query2 = String.format("SELECT MIN(zoom_level) AS min_gtm_zoom, MAX(zoom_level) AS max_gtm_zoom FROM %s WHERE table_name = ?",
                                                    GeoPackageTiles.MatrixTableName);

                try(final PreparedStatement stmt2 = this.getSqliteConnection().prepareStatement(query2))
                {
                    stmt2.setString(1, pyramidName);

                    try(final ResultSet minMaxZoom = stmt2.executeQuery())
                    {
                        final int minZoom = minMaxZoom.getInt("min_gtm_zoom");
                        final int maxZoom = minMaxZoom.getInt("max_gtm_zoom");

                        if(!minMaxZoom.wasNull())
                        {
                            final String query3 = String.format("SELECT id FROM %s WHERE zoom_level < ? OR zoom_level > ?", pyramidName);

                            try(final PreparedStatement stmt3        = this.getSqliteConnection().prepareStatement(query3))
                            {
                                stmt3.setInt(1, minZoom);
                                stmt3.setInt(2, maxZoom);

                                try(final ResultSet invalidZooms = stmt3.executeQuery())
                                {
                                    if(invalidZooms.next())
                                    {
                                        fail(String.format("There are zoom_levels in the Pyramid User Data Table: %1$s  such that the zoom level "
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
     * Requirement 56
     *
     * <blockquote>
     * For each distinct {@code table_name} from the {@code
     * gpkg_tile_matrix} (tm) table, the tile pyramid (tp) user data
     * table {@code tile_column} column value in a GeoPackage SHALL be in
     * the range 0 <= tp.tile_column <= tm.matrix_width - 1 where the tm and tp
     * {@code zoom_level} column values are equal.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement(reference = "Requirement 56",
                 text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, the tile pyramid (tp) user data table tile_column column value in a GeoPackage SHALL be in the range 0 <= tp.tile_column <= tm.matrix_width - 1 where the tm and tp zoom_level column values are equal. ")
    public void requirement56() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            final Map<String, Collection<Integer>> tableNamesAndZoomLevels = new HashMap<>();

            for(final String tableName : this.tileTablesInTileMatrix)
            {
                // This query will only pull the incorrect values for the
                // pyramid user data table's column width, the value
                // of the tile_column value for the pyramid user data table
                // SHOULD be null otherwise those fields are in violation
                // of the range
                final String query = String.format("SELECT zoom_level as zl, matrix_width as width " +
                                                   "FROM   %1$s "        +
                                                   "WHERE  table_name = ? "       +
                                                   "AND (zoom_level in (SELECT zoom_level FROM %2$s WHERE tile_column < 0) " +
                                                   "OR  (EXISTS(SELECT NULL FROM %2$s WHERE zoom_level = zl AND tile_column > width - 1)));",
                                                   GeoPackageTiles.MatrixTableName,
                                                   tableName);

                final Collection<Integer> zoomLevels = JdbcUtility.select(this.getSqliteConnection(),
                                                                          query,
                                                                          preparedStatement -> preparedStatement.setString(1, tableName),
                                                                          resultSet -> resultSet.getInt("zl"));

                if(!zoomLevels.isEmpty())
                {
                    tableNamesAndZoomLevels.put(tableName, zoomLevels);
                }
            }

            assertTrue(String.format("The following tiles tables contain zoom levels with tile_column values oustide of the range [0, matrix_width-1]:\n%s",
                                     tableNamesAndZoomLevels.entrySet()
                                                            .stream()
                                                            .map(entrySet -> String.format("%s: %s",
                                                                                           entrySet.getKey(),
                                                                                           entrySet.getValue()
                                                                                                   .stream()
                                                                                                   .map(Object::toString)
                                                                                                   .collect(Collectors.joining(", "))))
                                                            .collect(Collectors.joining("\n"))),
                         tableNamesAndZoomLevels.isEmpty(),
                         Severity.Warning);
        }
    }

    /**
     * Requirement 57
     *
     * <blockquote>
     * For each distinct {@code table_name} from the {@code
     * gpkg_tile_matrix} (tm) table, the tile pyramid (tp) user data
     * table {@code tile_row} column value in a GeoPackage SHALL be in the
     * range 0 <= tp.tile_row <= tm.matrix_height - 1 where the tm and tp
     * {@code zoom_level} column values are equal.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet the requirement
     * @throws SQLException throws if an SQLException occurs
     */
    @Requirement (reference = "Requirement 57",
                  text      = "For each distinct table_name from the gpkg_tile_matrix (tm) table, the tile pyramid (tp) user data table tile_row column value in a GeoPackage SHALL be in the range 0 <= tp.tile_row <= tm.matrix_height - 1 where the tm and tp zoom_level column values are equal.")
    public void requirement57() throws AssertionError, SQLException
    {
        if(this.hasTileMatrixTable)
        {
            final Map<String, Collection<Integer>> tableNamesAndZoomLevels = new HashMap<>();

            for(final String tableName : this.tileTablesInTileMatrix)
            {
                // This query will only pull the incorrect values for the
                // pyramid user data table's column height, the value
                // of the tile_row value for the pyramid user data table
                // SHOULD be null otherwise those fields are in violation
                // of the range
                final String query = String.format("SELECT zoom_level as zl, matrix_height as height " +
                                                   "FROM   %1$s "        +
                                                   "WHERE  table_name = ? "       +
                                                   "AND (zoom_level in (SELECT zoom_level FROM %2$s WHERE tile_row < 0) " +
                                                   "OR  (EXISTS(SELECT NULL FROM %2$s WHERE zoom_level = zl AND tile_row > height - 1)));",
                                                   GeoPackageTiles.MatrixTableName,
                                                   tableName);

                final Collection<Integer> zoomLevels = JdbcUtility.select(this.getSqliteConnection(),
                                                                          query,
                                                                          preparedStatement -> preparedStatement.setString(1, tableName),
                                                                          resultSet -> resultSet.getInt("zl"));

                if(!zoomLevels.isEmpty())
                {
                    tableNamesAndZoomLevels.put(tableName, zoomLevels);
                }
            }

            assertTrue(String.format("The following tiles tables contain zoom levels with tile_column values oustide of the range [0, matrix_width-1]:\n%s",
                                     tableNamesAndZoomLevels.entrySet()
                                                            .stream()
                                                            .map(entrySet -> String.format("%s: %s",
                                                                                           entrySet.getKey(),
                                                                                           entrySet.getValue()
                                                                                                   .stream()
                                                                                                   .map(Object::toString)
                                                                                                   .collect(Collectors.joining(", "))))
                                                            .collect(Collectors.joining("\n"))),
                         tableNamesAndZoomLevels.isEmpty(),
                         Severity.Warning);
        }
    }

    private static boolean isAcceptedImageFormat(final byte[] tileData)
    {
        if(tileData == null)
        {
            return false;
        }

        try(final ByteArrayInputStream        byteArray  = new ByteArrayInputStream(tileData);
            final MemoryCacheImageInputStream cacheImage = new MemoryCacheImageInputStream(byteArray))
        {
            return TilesVerifier.canReadImage(pngImageReaders, cacheImage) || TilesVerifier.canReadImage(jpegImageReaders, cacheImage);
        }
        catch(final IOException ignored)
        {
            return false;
        }
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

    private static boolean canReadImage(final Collection<ImageReader> imageReaders, final ImageInputStream image)
    {
        return imageReaders.stream()
                           .anyMatch(imageReader -> { try
                                                      {
                                                          image.mark();
                                                          return imageReader.getOriginatingProvider().canDecodeInput(image);
                                                      }
                                                      catch(final IOException ignored)
                                                      {
                                                          return false;
                                                      }
                                                      finally
                                                      {
                                                          try
                                                          {
                                                              image.reset();
                                                          }
                                                          catch(final IOException ignored)
                                                          {

                                                          }
                                                      }
                                                    });
    }

    private final boolean            hasTileMatrixTable;
    private final boolean            hasTileMatrixSetTable;
    private final Collection<String> tileTableNames;
    private final Collection<String> contentsTileTableNames;
    private final Collection<String> tileTablesInTileMatrix;

    private static final Collection<ImageReader> jpegImageReaders;
    private static final Collection<ImageReader> pngImageReaders;

    private static final Map<String, ColumnDefinition> TilePyramidUserDataTableColumns;
    private static final Set<ForeignKeyDefinition>     TilePyramidUserDataTableForeignKeys;
    private static final Set<UniqueDefinition>         TilePyramidUserDataTableUniqueColumnGroups;


    static
    {
        jpegImageReaders = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/jpeg"));
        pngImageReaders  = TilesVerifier.iteratorToCollection(ImageIO.getImageReadersByMIMEType("image/png"));

        TilePyramidUserDataTableColumns = new HashMap<>();

        TilePyramidUserDataTableColumns.put("id",          new ColumnDefinition("INTEGER", false, true,  true,  null));
        TilePyramidUserDataTableColumns.put("zoom_level",  new ColumnDefinition("INTEGER", true,  false, false, null));
        TilePyramidUserDataTableColumns.put("tile_column", new ColumnDefinition("INTEGER", true,  false, false, null));
        TilePyramidUserDataTableColumns.put("tile_row",    new ColumnDefinition("INTEGER", true,  false, false, null));
        TilePyramidUserDataTableColumns.put("tile_data",   new ColumnDefinition("BLOB",    true,  false, false, null));

        TilePyramidUserDataTableForeignKeys = Collections.emptySet();
        TilePyramidUserDataTableUniqueColumnGroups =  new HashSet<>(Arrays.asList(new UniqueDefinition("zoom_level", "tile_column", "tile_row")));
    }
}
