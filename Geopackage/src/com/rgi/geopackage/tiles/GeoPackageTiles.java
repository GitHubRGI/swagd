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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.coordinate.referencesystem.profile.Utility;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.verification.FailedRequirement;

public class GeoPackageTiles
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param core
     *             Access to GeoPackage's "core" methods
     */
    public GeoPackageTiles(final Connection databaseConnection, final GeoPackageCore core)
    {
        this.databaseConnection = databaseConnection;
        this.core               = core;
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @return The tile GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements() throws SQLException
    {
        return new TilesVerifier(this.databaseConnection).getFailedRequirements();
    }

    /**
     * Creates a user defined tiles table, and adds a corresponding entry to the content table
     *
     * @param tableName
     *             The name of the tiles table. The table name must begin with a letter (A..Z, a..z) or an underscore (_) and may only be followed by letters, underscores, or numbers, and may not begin with the prefix "gpkg_"
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     * @return Returns a newly created user defined tiles table
     * @throws SQLException
     */
    public TileSet addTileSet(final String                 tableName,
                              final String                 identifier,
                              final String                 description,
                              final BoundingBox            boundingBox,
                              final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Tile set name may not be null");
        }

        if(!tableName.matches("^[_a-zA-Z]\\w*"))
        {
            throw new IllegalArgumentException("The tile set's table name must begin with a letter (A..Z, a..z) or an underscore (_) and may only be followed by letters, underscores, or numbers");
        }

        if(tableName.startsWith("gpkg_"))
        {
            throw new IllegalArgumentException("The tile set's name may not start with the reserved prefix 'gpkg_'");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be mull.");
        }

        final TileSet existingContent = this.getTileSet(tableName);

        if(existingContent != null)
        {
            if(existingContent.equals(tableName,
                                      TileSet.TileContentType,
                                      identifier,
                                      description,
                                      boundingBox,
                                      spatialReferenceSystem.getOrganizationSrsId()))
            {
                return existingContent;
            }

            throw new IllegalArgumentException("An entry in the content table already exists with this table name, but has different values for its other fields");
        }

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
            throw new IllegalArgumentException("A table already exists with this tile set's table name");
        }

        try
        {
            this.createTilesTablesNoCommit(); // Create the tile metadata tables

            // Create the tile set table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getTileSetCreationSql(tableName));
            }

            // Add tile set to the content table
            this.core.addContent(tableName,
                                 TileSet.TileContentType,
                                 identifier,
                                 description,
                                 boundingBox,
                                 spatialReferenceSystem);

            this.addTileMatrixSetNoCommit(tableName,
                                          boundingBox,
                                          spatialReferenceSystem); // Add tile matrix set metadata

            this.databaseConnection.commit();

            return this.getTileSet(tableName);
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * The zoom levels that a tile set has values for
     *
     * @param tileSet
     *             A handle to a set of tiles
     * @return Returns all of the zoom levels that apply for tileSet
     * @throws SQLException
     */
    public Set<Integer> getTileZoomLevels(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String zoomLevelQuerySql = String.format("SELECT zoom_level FROM %s WHERE table_name = ?;",
                                                       GeoPackageTiles.MatrixTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(zoomLevelQuerySql))
        {
            preparedStatement.setString(1, tileSet.getTableName());

            try(ResultSet results = preparedStatement.executeQuery())
            {
                final Set<Integer> zoomLevels = new HashSet<>();

                while(results.next())
                {
                    zoomLevels.add(results.getInt(1));
                }

                return zoomLevels;
            }
        }
    }

     /**
     * Gets all entries in the GeoPackage's contents table with the "tiles"
     * data_type
     *
     * @return Returns a collection of {@link TileSet}s
     * @throws SQLException
     */
    public Collection<TileSet> getTileSets() throws SQLException
    {
        return this.getTileSets(null);
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "tiles"
     * data_type that also match the supplied spatial reference system
     *
     * @param matchingSpatialReferenceSystem
     *             Spatial reference system that returned {@link TileSet}s much refer to
     * @return Returns a collection of {@link TileSet}s
     * @throws SQLException
     */
    public Collection<TileSet> getTileSets(final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        return this.core.getContent(TileSet.TileContentType,
                                    (tableName, dataType, identifier, description, lastChange, boundingBox, spatialReferenceSystem) -> new TileSet(tableName, identifier, description, lastChange, boundingBox, spatialReferenceSystem),
                                    matchingSpatialReferenceSystem);
    }

    /**
     * Adds a tile matrix
     *
     * @param tileSet
     *             A handle to a tile set
     * @param zoomLevel
     *             The zoom level of the associated tile set (0 <= zoomLevel <= max_level)
     * @param matrixWidth
     *             The number of columns (>= 1) for this tile at this zoom level
     * @param matrixHeight
     *             The number of rows (>= 1) for this tile at this zoom level
     * @param tileWidth
     *             The tile width in pixels (>= 1) at this zoom level
     * @param tileHeight
     *             The tile height in pixels (>= 1) at this zoom level
     * @param pixelXSize
     *             The width of the associated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (> 0)
     * @param pixelYSize
     *             The height of the associated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (> 0)
     * @return Returns the newly added tile matrix
     * @throws SQLException
     */
    public TileMatrix addTileMatrix(final TileSet tileSet,
                                    final int     zoomLevel,
                                    final int     matrixWidth,
                                    final int     matrixHeight,
                                    final int     tileWidth,
                                    final int     tileHeight,
                                    final double  pixelXSize,
                                    final double  pixelYSize) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("The tile set may not be null");
        }

        if(zoomLevel < 0)
        {
            throw new IllegalArgumentException("Zoom level must be greater than or equal to 0");
        }

        if(matrixWidth <= 0)
        {
            throw new IllegalArgumentException("Matrix width must be greater than 0");
        }

        if(matrixHeight <= 0)
        {
            throw new IllegalArgumentException("Matrix height must be greater than 0");
        }

        if(tileWidth <= 0)
        {
            throw new IllegalArgumentException("Tile width must be greater than 0");
        }

        if(tileHeight <= 0)
        {
            throw new IllegalArgumentException("Matrix height must be greater than 0");
        }

        if(pixelXSize <= 0.0)
        {
            throw new IllegalArgumentException("Pixel X size must be greater than 0.0");
        }

        if(pixelYSize <= 0.0)
        {
            throw new IllegalArgumentException("Pixel Y size must be greater than 0.0");
        }

        final TileMatrix tileMatrix = this.getTileMatrix(tileSet, zoomLevel);

        if(tileMatrix != null)
        {
            if(!tileMatrix.equals(tileSet.getTableName(),
                                  zoomLevel,
                                  matrixWidth,
                                  matrixHeight,
                                  tileWidth,
                                  tileHeight,
                                  pixelXSize,
                                  pixelYSize))
            {
                throw new IllegalArgumentException("An entry in the content table already exists with this table name, but has different values for its other fields");
            }

            return tileMatrix;
        }

        final TileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileSet);

        if(tileMatrixSet == null)
        {
            throw new IllegalArgumentException("Cannot add a tile matrix to a tile set with no tile matrix set.");  // TODO do we need to expose addTileMatrixSet() to help avoid ever getting here? a tile matrix set is created automatically by this API on tile set creation, and the verifier insures that there's one for every tile set.
        }

        if(matrixHeight * tileHeight * pixelYSize != tileMatrixSet.getBoundingBox().getHeight())    // TODO instead of testing for equality, test with an EPSILON tolerance ?
        {
            throw new IllegalArgumentException("The geographic height of the tile matrix [matrix height * tile height (pixels) * pixel y size (srs units per pixel)] differs from the minimum bounds for this tile set specified by the tile matrix set");
        }

        if(matrixWidth * tileWidth * pixelXSize != tileMatrixSet.getBoundingBox().getWidth())    // TODO instead of testing for equality, test with an EPSILON tolerance ?
        {
            throw new IllegalArgumentException("The geographic width of the tile matrix [matrix width * tile width (pixels) * pixel x size (srs units per pixel)] differs from the minimum bounds for this tile set specified by the tile matrix set");
        }

        try
        {
            final String insertTileMatrix = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                                          GeoPackageTiles.MatrixTableName,
                                                          "table_name",
                                                          "zoom_level",
                                                          "matrix_width",
                                                          "matrix_height",
                                                          "tile_width",
                                                          "tile_height",
                                                          "pixel_x_size",
                                                          "pixel_y_size");

            try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertTileMatrix))
            {
                preparedStatement.setString(1, tileSet.getTableName());
                preparedStatement.setInt   (2, zoomLevel);
                preparedStatement.setInt   (3, matrixWidth);
                preparedStatement.setInt   (4, matrixHeight);
                preparedStatement.setInt   (5, tileWidth);
                preparedStatement.setInt   (6, tileHeight);
                preparedStatement.setDouble(7, pixelXSize);
                preparedStatement.setDouble(8, pixelYSize);

                preparedStatement.executeUpdate();
            }

            this.databaseConnection.commit();

            return new TileMatrix(tileSet.getTableName(),
                                  zoomLevel,
                                  matrixWidth,
                                  matrixHeight,
                                  tileWidth,
                                  tileHeight,
                                  pixelXSize,
                                  pixelYSize);
        }
        catch(final SQLException ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Add a tile to the GeoPackage
     *
     * @param tileSet
     *             Tile set that which the tiles and tile metadata are associated
     * @param tileMatrix
     *             Tile matrix associated with the tile set at the corresponding zoom level
     * @param coordinate
     *             The coordinate of the tile, relative to the tile set
     * @param imageData
     *             The bytes of the image file
     * @throws SQLException
     * @throws TileReferentialIntegrityException
     */
    public Tile addTile(final TileSet                tileSet,
                        final TileMatrix             tileMatrix,
                        final RelativeTileCoordinate coordinate,
                        final byte[]                 imageData) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null");
        }

        if(tileMatrix == null)
        {
            throw new IllegalArgumentException("Tile matrix may not be null");
        }

        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(imageData == null || imageData.length == 0) // TODO the standard restricts the image types to image/jpeg, image/png and image/x-webp (by extension only: http://www.geopackage.org/spec/#extension_tiles_webp)
                                                       // TODO It'd be desirable to check the height/width of the image against the values described by the tile matrix, but this is difficult to do with a string of bytes.  One solution would be to changed to a java BufferedImage rather than raw bytes, but this *might* unnecessarily confine extension writers to to formats that fit into Java.ImageIO
        {
            throw new IllegalArgumentException("Image data may not be null or empty");
        }

        if(coordinate.getZoomLevel() != tileMatrix.getZoomLevel())
        {
            throw new IllegalArgumentException("The zoom level of the tile coordinate must match the zoom level of the tile matrix");
        }

        final int row    = coordinate.getRow();
        final int column = coordinate.getColumn();

        // Verify row and column are within the tile metadata's range
        if(row < 0 || row > tileMatrix.getMatrixHeight())
        {
            throw new IllegalArgumentException(String.format("tile row must be in the range [0, %d] (tile matrix metadata's matrix height - 1)",
                                                             tileMatrix.getMatrixHeight()-1));
        }
        if(column < 0 || column > tileMatrix.getMatrixWidth())
        {
            throw new IllegalArgumentException(String.format("tile column must be in the range [0, %d] (tile matrix metadata's matrix width - 1)",
                                                             tileMatrix.getMatrixWidth()-1));
        }

        final String insertTileSql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                                                   tileSet.getTableName(),
                                                   "zoom_level",
                                                   "tile_column",
                                                   "tile_row",
                                                   "tile_data");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertTileSql))
        {
            preparedStatement.setInt  (1, coordinate.getZoomLevel());
            preparedStatement.setInt  (2, coordinate.getColumn());
            preparedStatement.setInt  (3, coordinate.getRow());
            preparedStatement.setBytes(4, imageData);  // .setBlob() does not work as advertised in the sqlite-jdbc driver.  See this post by the developer: https://groups.google.com/d/msg/xerial/FfNOo-dPlsE/hUQWDPrZvSYJ

            preparedStatement.executeUpdate();
        }

        this.databaseConnection.commit();

        return this.getTile(tileSet, coordinate);
    }

    /**
     * Add a tile to the GeoPackage
     *
     * @param tileSet
     *             Tile set that which the tiles and tile metadata are associated
     * @param tileMatrix
     *             Tile matrix associated with the tile set at the corresponding zoom level
     * @param coordinate
     *             The coordinate of the tile in units of the tile set's spatial reference system
     * @param zoomLevel
     *             Zoom level
     * @param imageData
     *             The bytes of the image file
     * @throws SQLException
     * @throws TileReferentialIntegrityException
     */
    public Tile addTile(final TileSet       tileSet,
                        final TileMatrix    tileMatrix,
                        final CrsCoordinate coordinate,
                        final int           zoomLevel,
                        final byte[]        imageData) throws SQLException
    {
        final RelativeTileCoordinate relativeCoordinate = this.crsToRelativeTileCoordinate(tileSet, coordinate, zoomLevel);
        return relativeCoordinate != null ? this.addTile(tileSet,
                                                         tileMatrix,
                                                         relativeCoordinate,
                                                         imageData)
                                          : null;
    }

//    public Tile updateTile(final TileSet tileSet,
//                           final Tile    tile,
//                           final byte[]  imageData) throws SQLException
//    {
//        if(tileSet == null)
//        {
//            throw new IllegalArgumentException("Tile set may not be null");
//        }
//
//        if(tile == null)
//        {
//           throw new IllegalArgumentException("Tile may not be null");
//        }
//
//        if(imageData == null)
//        {
//           throw new IllegalArgumentException("Image data may not be null");
//        }
//
//        final String updateTileSql = String.format("UPDATE %s SET %s = ? WHERE %s = ? AND %s = ? AND %s = ?",
//                                                   tileSet.getTableName(),
//                                                   "tile_data",
//                                                   "zoom_level",
//                                                   "tile_column",
//                                                   "tile_row");
//
//        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(updateTileSql))
//        {
//            preparedStatement.setBytes(1, imageData);  // .setBlob() does not work as advertised in the sqlite-jdbc driver.  See this post by the developer: https://groups.google.com/d/msg/xerial/FfNOo-dPlsE/hUQWDPrZvSYJ
//            preparedStatement.setInt  (2, tile.getZoomLevel());
//            preparedStatement.setInt  (3, tile.getColumn());
//            preparedStatement.setInt  (4, tile.getRow());
//
//            preparedStatement.executeUpdate();
//        }
//
//        this.databaseConnection.commit();
//
//        return new Tile(tile.getIdentifier(),
//                        tile.getZoomLevel(),
//                        tile.getRow(),
//                        tile.getColumn(),
//                        imageData);
//    }

    /**
     * Gets tile coordinates for every tile in a tile set. A tile set need not
     * have an entry for every possible position in its respective tile
     * matrices.
     *
     * @param tileSet
     *             Handle to the tile set that the requested tiles should belong
     * @return Returns a {@link Stream} of {@link RelativeTileCoordinate}s representing every tile that the specific tile set contains.
     * @throws SQLException
     */
    public Stream<RelativeTileCoordinate> getTiles(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s, %s FROM %s;",
                                               "zoom_level",
                                               "tile_column",
                                               "tile_row",
                                               tileSet.getTableName());

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(tileQuery))
        {
            return ResultSetStream.getStream(preparedStatement.executeQuery(),
                                             resultSet -> { try
                                                            {
                                                                return new RelativeTileCoordinate(resultSet.getInt(3), resultSet.getInt(2), resultSet.getInt(1));
                                                            }
                                                            catch(final Exception ex)
                                                            {
                                                                return null;
                                                            }
                                                          })
                                  .filter(Objects::nonNull);
        }
    }

    /**
     * Gets a tile
     *
     * @param tileSet
     *             Handle to the tile set that the requested tile should belong
     * @param coordinate
     *            Coordinate relative to the tile set, of the requested tile
     * @return Returns the requested tile, or null if it's not found
     * @throws SQLException
     */
    public Tile getTile(final TileSet                tileSet,
                        final RelativeTileCoordinate coordinate) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        if(coordinate == null)
        {
            throw new IllegalArgumentException("Requested tile cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?;",
                                               "id",
                                               "zoom_level",
                                               "tile_column",
                                               "tile_row",
                                               "tile_data",
                                               tileSet.getTableName());

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(tileQuery))
        {
            preparedStatement.setInt(1, coordinate.getZoomLevel());
            preparedStatement.setInt(2, coordinate.getColumn());
            preparedStatement.setInt(3, coordinate.getRow());

            try(ResultSet tileResult = preparedStatement.executeQuery())
            {
                if(tileResult.isBeforeFirst())
                {
                    return new Tile(tileResult.getInt(1),    // id
                                    tileResult.getInt(2),    // zoom level
                                    tileResult.getInt(4),    // row
                                    tileResult.getInt(3),    // column
                                    tileResult.getBytes(5)); // data
                }

                return null; // No tile exists for this coordinate and zoom level
            }
        }
    }

     /**
     * Gets a tile
     *
     * @param tileSet
     *             Handle to the tile set that the requested tile should belong
     * @param coordinate
     *            Coordinate, in the units of the tile set's spatial reference system, of the requested tile
     * @param zoomLevel
     *            Zoom level
     * @return Returns the requested tile, or null if it's not found
     * @throws SQLException
     */
    public Tile getTile(final TileSet       tileSet,
                        final CrsCoordinate coordinate,
                        final int           zoomLevel) throws SQLException
    {
        final RelativeTileCoordinate relativeCoordiante = this.crsToRelativeTileCoordinate(tileSet, coordinate, zoomLevel);
        return relativeCoordiante != null ? this.getTile(tileSet, relativeCoordiante)
                                          : null;
    }

    /**
     * Gets a tile set's tile matrix
     *
     * @param tileSet
     *             A handle to a set of tiles
     * @return Returns a tile set's tile matrix set
     * @throws SQLException
     */
    public TileMatrixSet getTileMatrixSet(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageTiles.MatrixTableName))
        {
            return null;
        }

        final String querySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ?;",
                                              "table_name",
                                              "srs_id",
                                              "min_x",
                                              "min_y",
                                              "max_x",
                                              "max_y",
                                              GeoPackageTiles.MatrixSetTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(querySql))
        {
            preparedStatement.setString(1, tileSet.getTableName());

            try(ResultSet result = preparedStatement.executeQuery())
            {
                return new TileMatrixSet(result.getString(1),                                   // table name
                                         this.core.getSpatialReferenceSystem(result.getInt(2)), // srs id
                                         new BoundingBox(result.getDouble(4),                   // min y
                                                         result.getDouble(3),                   // min x
                                                         result.getDouble(6),                   // max y
                                                         result.getDouble(5)));                 // max x
            }
        }
    }

    /**
     * Gets a tile set object based on its table name
     *
     * @param tileSetTableName
     *             Name of a tile set table
     * @return Returns a TileSet or null if there isn't with the supplied table name
     * @throws SQLException
     */
    public TileSet getTileSet(final String tileSetTableName) throws SQLException
    {
        return this.core.getContent(tileSetTableName,
                                    (tableName, dataType, identifier, description, lastChange, boundingBox, spatialReferenceSystem) -> new TileSet(tableName, identifier, description, lastChange, boundingBox, spatialReferenceSystem));
    }

    /**
     * Adds a tile matrix associated with a tile set
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @param tableName
     *             Name of the tile set
     * @param boundingBox
     *             Bounding box of the tile matrix set
     * @param spatialReferenceSystem
     *             Spatial reference system of the tile matrix set
     *
     * @throws SQLException
     */
    private void addTileMatrixSetNoCommit(final String                 tableName,
                                          final BoundingBox            boundingBox,
                                          final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Table name cannot null or empty");
        }

        if(spatialReferenceSystem == null)
        {
            throw new IllegalArgumentException("Spatial reference system cannot be null");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be null");
        }

        final String insertTileMatrixSetSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                                                            GeoPackageTiles.MatrixSetTableName,
                                                            "table_name",
                                                            "srs_id",
                                                            "min_x",
                                                            "min_y",
                                                            "max_x",
                                                            "max_y");

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertTileMatrixSetSql))
        {
            preparedStatement.setString(1, tableName);
            preparedStatement.setInt   (2, spatialReferenceSystem.getIdentifier());
            preparedStatement.setDouble(3, boundingBox.getMinX());
            preparedStatement.setDouble(4, boundingBox.getMinY());
            preparedStatement.setDouble(5, boundingBox.getMaxX());
            preparedStatement.setDouble(6, boundingBox.getMaxY());

            preparedStatement.executeUpdate();
        }
    }

    /**
     * Get a tile set's tile matrix
     *
     * @param tileSet
     *             A handle to a set of tiles
     * @param zoomLevel
     *             Zoom level of the tile matrix
     * @return Returns a tile set's tile matrix that corresponds to the input level, or null if one doesn't exist
     * @throws SQLException
     */
    public TileMatrix getTileMatrix(final TileSet tileSet, final int zoomLevel) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ? AND zoom_level = ?;",
                                               "matrix_width",
                                               "matrix_height",
                                               "tile_width",
                                               "tile_height",
                                               "pixel_x_size",
                                               "pixel_y_size",
                                               GeoPackageTiles.MatrixTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(tileQuery))
        {
            preparedStatement.setString(1, tileSet.getTableName());
            preparedStatement.setInt   (2, zoomLevel);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    return new TileMatrix(tileSet.getTableName(),
                                          zoomLevel,
                                          result.getInt   (1),  // matrix width
                                          result.getInt   (2),  // matrix height
                                          result.getInt   (3),  // tile width
                                          result.getInt   (4),  // tile height
                                          result.getDouble(5),  // pixel x size
                                          result.getDouble(6)); // pizel y size
                }

                return null; // No matrix exists for this table name and zoom level
            }
        }
    }

    /**
     * Gets the tile matrices associated with a tile set
     *
     * @param tileSet
     *             A handle to a set of tiles
     * @return Returns every tile matrix associated with a tile set in ascending order by zoom level
     * @throws SQLException
     */
    public List<TileMatrix> getTileMatrices(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ? ORDER BY %1$s ASC;",
                                               "zoom_level",
                                               "matrix_width",
                                               "matrix_height",
                                               "tile_width",
                                               "tile_height",
                                               "pixel_x_size",
                                               "pixel_y_size",
                                               GeoPackageTiles.MatrixTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(tileQuery))
        {
            preparedStatement.setString(1, tileSet.getTableName());

            try(ResultSet results = preparedStatement.executeQuery())
            {
                final List<TileMatrix> tileMatrices = new ArrayList<>();

                while(results.next())
                {
                    tileMatrices.add(new TileMatrix(tileSet.getTableName(),
                                                    results.getInt   (1),   // zoom level
                                                    results.getInt   (2),   // matrix width
                                                    results.getInt   (3),   // matrix height
                                                    results.getInt   (4),   // tile width
                                                    results.getInt   (5),   // tile height
                                                    results.getDouble(6),   // pixel x size
                                                    results.getDouble(7))); // pizel y size
                }

                return tileMatrices;
            }
        }
    }

    /**
     * Convert a CRS coordinate to a tile coordinate relative to a tile set
     *
     * @param tileSet
     *             A handle to a set of tiles
     * @param crsCoordinate
     *             A coordinate with a specified coordinate reference system
     * @param zoomLevel
     *            Zoom level
     * @return Returns a tile coordinate relative and specific to the input tile set.  The input CRS coordinate would be contained in the the associated tile bounds.
     * @throws SQLException
     */
    public RelativeTileCoordinate crsToRelativeTileCoordinate(final TileSet       tileSet,
                                                              final CrsCoordinate crsCoordinate,
                                                              final int           zoomLevel) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null");
        }

        if(crsCoordinate == null)
        {
            throw new IllegalArgumentException("CRS coordinate may not be null");
        }

        final CoordinateReferenceSystem crs = crsCoordinate.getCoordinateReferenceSystem();
        final SpatialReferenceSystem    srs = this.core.getSpatialReferenceSystem(tileSet.getSpatialReferenceSystemIdentifier());

        if(!crs.getAuthority().equalsIgnoreCase(srs.getOrganization()) ||
           crs.getIdentifier() != srs.getIdentifier())
        {
            throw new IllegalArgumentException("Coordinate transformation is not currently supported.  The incoming spatial reference system must match that of the tile set's");
        }

        final TileMatrix tileMatrix = this.getTileMatrix(tileSet, zoomLevel);

        if(tileMatrix == null)
        {
            return null;    // No tile matrix for the requested zoom level
        }
        final CrsProfile    crsProfile    = CrsProfileFactory.create(crs);
        final TileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileSet);
        final BoundingBox   tileSetBounds = roundBounds(tileMatrixSet.getBoundingBox(), crsProfile);
        
        if(!Utility.contains(tileSetBounds, crsCoordinate, GeoPackageTiles.Origin))
        {
            return null;    // The requested SRS coordinate is outside the bounds of our data
        }

        final Coordinate<Double> boundsCorner = Utility.boundsCorner(tileSetBounds, GeoPackageTiles.Origin);
        

        final double tileHeightInSrs = tileMatrix.getPixelYSize() * tileMatrix.getTileHeight();
        final double tileWidthInSrs  = tileMatrix.getPixelXSize() * tileMatrix.getTileWidth();

        final double normalizedSrsTileCoordinateY = Math.abs(crsCoordinate.getY() - boundsCorner.getY());
        final double normalizedSrsTileCoordinateX = Math.abs(crsCoordinate.getX() - boundsCorner.getX());

        final int tileY = (int)Math.floor(normalizedSrsTileCoordinateY / tileHeightInSrs);
        final int tileX = (int)Math.floor(normalizedSrsTileCoordinateX / tileWidthInSrs);

        return new RelativeTileCoordinate(tileY,
                                          tileX,
                                          zoomLevel);
    }

    /**
     * Creates the tables required for storing tiles
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @throws SQLException
     */
    protected void createTilesTablesNoCommit() throws SQLException
    {
        // Create the tile matrix set table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageTiles.MatrixSetTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getTileMatrixSetCreationSql());
            }
        }

        // Create the tile matrix table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageTiles.MatrixTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getTileMatrixCreationSql());
            }
        }
    }

    @SuppressWarnings("static-method")
    protected String getTileMatrixSetCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_tile_matrix_set_sql
        // http://www.geopackage.org/spec/#_tile_matrix_set
        return "CREATE TABLE " + GeoPackageTiles.MatrixSetTableName  + "\n" +
               "(table_name TEXT    NOT NULL PRIMARY KEY, -- Tile Pyramid User Data Table Name\n"                                       +
               " srs_id     INTEGER NOT NULL,             -- Spatial Reference System ID: gpkg_spatial_ref_sys.srs_id\n"                +
               " min_x      DOUBLE  NOT NULL,             -- Bounding box minimum easting or longitude for all content in table_name\n" +
               " min_y      DOUBLE  NOT NULL,             -- Bounding box minimum northing or latitude for all content in table_name\n" +
               " max_x      DOUBLE  NOT NULL,             -- Bounding box maximum easting or longitude for all content in table_name\n" +
               " max_y      DOUBLE  NOT NULL,             -- Bounding box maximum northing or latitude for all content in table_name\n" +
               " CONSTRAINT fk_gtms_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name),"                          +
               " CONSTRAINT fk_gtms_srs FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys (srs_id));";
    }

    @SuppressWarnings("static-method")
    protected String getTileMatrixCreationSql()
    {
        // http://www.geopackage.org/spec/#tile_matrix
        // http://www.geopackage.org/spec/#gpkg_tile_matrix_sql
        return "CREATE TABLE " + GeoPackageTiles.MatrixTableName + "\n" +
                "(table_name    TEXT    NOT NULL, -- Tile Pyramid User Data Table Name\n"                            +
                " zoom_level    INTEGER NOT NULL, -- 0 <= zoom_level <= max_level for table_name\n"                  +
                " matrix_width  INTEGER NOT NULL, -- Number of columns (>= 1) in tile matrix at this zoom level\n"   +
                " matrix_height INTEGER NOT NULL, -- Number of rows (>= 1) in tile matrix at this zoom level\n"      +
                " tile_width    INTEGER NOT NULL, -- Tile width in pixels (>= 1) for this zoom level\n"              +
                " tile_height   INTEGER NOT NULL, -- Tile height in pixels (>= 1) for this zoom level\n"             +
                " pixel_x_size  DOUBLE  NOT NULL, -- In t_table_name srid units or default meters for srid 0 (>0)\n" +
                " pixel_y_size  DOUBLE  NOT NULL, -- In t_table_name srid units or default meters for srid 0 (>0)\n" +
                " CONSTRAINT pk_ttm PRIMARY KEY (table_name, zoom_level),"                                           +
                " CONSTRAINT fk_tmm_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getTileSetCreationSql(final String tileTableName)
    {
        // http://www.geopackage.org/spec/#tiles_user_tables
        // http://www.geopackage.org/spec/#_sample_tile_pyramid_informative
        return "CREATE TABLE " + tileTableName + "\n" +
               "(id          INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n"                                                                            +
               " zoom_level  INTEGER NOT NULL,                  -- min(zoom_level) <= zoom_level <= max(zoom_level) for t_table_name\n"                                    +
               " tile_column INTEGER NOT NULL,                  -- 0 to tile_matrix matrix_width - 1\n"                                                                    +
               " tile_row    INTEGER NOT NULL,                  -- 0 to tile_matrix matrix_height - 1\n"                                                                   +
               " tile_data   BLOB    NOT NULL,                  -- Of an image MIME type specified in clauses Tile Encoding PNG, Tile Encoding JPEG, Tile Encoding WEBP\n" +
               " UNIQUE (zoom_level, tile_column, tile_row));";
    }
    /**
     * Truncates the bounds to the appropriate level of accuracy
     * (2 decimal places for meters, 7 decimal places for degrees)
     * @param bounds
     * @param crs
     * @return
     */
    private static BoundingBox roundBounds(BoundingBox bounds, CrsProfile crs)
    {
        int percision = crs.requiredPercision();
        return new BoundingBox(roundPercision(bounds.getMinY(), percision),
                               roundPercision(bounds.getMinX(), percision),
                               roundPercision(bounds.getMaxY(), percision),
                               roundPercision(bounds.getMaxX(), percision));
    }
    /**
     * Rounds the number to level of precision need for the appropriate level of accuracy
     * @param number
     * @return the number rounded to the level of precision number of decimal places
     */
    private static double roundPercision(double number, int percision)
    {
        double multiplyBy = Math.pow(10, percision);
        return Math.round(number*multiplyBy)/multiplyBy;
    }
    

    private final GeoPackageCore core;
    private final Connection     databaseConnection;

    public final static TileOrigin Origin = TileOrigin.UpperLeft;   // http://www.geopackage.org/spec/#clause_tile_matrix_table_data_values

    public final static String MatrixSetTableName = "gpkg_tile_matrix_set";
    public final static String MatrixTableName    = "gpkg_tile_matrix";
}
