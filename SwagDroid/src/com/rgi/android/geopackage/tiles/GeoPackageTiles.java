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

package com.rgi.android.geopackage.tiles;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.coordinate.CoordinateReferenceSystem;
import com.rgi.android.common.coordinate.CrsCoordinate;
import com.rgi.android.common.tile.TileOrigin;
import com.rgi.android.common.util.BoundsUtility;
import com.rgi.android.common.util.jdbc.JdbcUtility;
import com.rgi.android.common.util.jdbc.PreparedStatementConsumer;
import com.rgi.android.common.util.jdbc.ResultSetFunction;
import com.rgi.android.geopackage.core.ContentFactory;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.core.SpatialReferenceSystem;
import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.verification.VerificationIssue;
import com.rgi.android.geopackage.verification.VerificationLevel;

/**
 * @author Luke Lambert
 *
 */
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
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return The tile GeoPackage requirements this GeoPackage fails to conform to
     * @throws SQLException throws if {@link TilesVerifier#TilesVerifier Verifier Constructor} throws
     */
    public Collection<VerificationIssue> getVerificationIssues(final VerificationLevel verificationLevel) throws SQLException
    {
        return new TilesVerifier(this.databaseConnection, verificationLevel).getVerificationIssues();
    }

    /**
     * Creates a user defined tiles table, and adds a corresponding entry to the
     * content table
     *
     * @param tableName
     *            The name of the tiles table. The table name must begin with a
     *            letter (A..Z, a..z) or an underscore (_) and may only be
     *            followed by letters, underscores, or numbers, and may not
     *            begin with the prefix "gpkg_"
     * @param identifier
     *            A human-readable identifier (e.g. short name) for the
     *            tableName content
     * @param description
     *            A human-readable description for the tableName content
     * @param boundingBox
     *            Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *            Spatial Reference System (SRS)
     * @return Returns a newly created user defined tiles table
     * @throws SQLException
     *             throws if the method {@link #getTileSet(String) getTileSet}
     *             or the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)
     *             tableOrViewExists} or if the database cannot roll back the
     *             changes after a different exception throws will throw an SQLException
     *
     */
    public TileSet addTileSet(final String                 tableName,
                              final String                 identifier,
                              final String                 description,
                              final BoundingBox            boundingBox,
                              final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        GeoPackageCore.validateNewContentTableName(tableName);

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be null.");
        }

        final TileSet existingContent = this.getTileSet(tableName);

        if(existingContent != null)
        {
            if(existingContent.equals(tableName,
                                      TileSet.TileContentType,
                                      identifier,
                                      description,
                                      boundingBox,
                                      spatialReferenceSystem.getIdentifier()))
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
            JdbcUtility.update(this.databaseConnection, this.getTileSetCreationSql(tableName));

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
        catch(final SQLException ex)
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
     *              SQLException thrown by automatic close() invocation on preparedStatement or various other SQLExceptions
     */
    public Set<Integer> getTileZoomLevels(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String zoomLevelQuerySql = String.format("SELECT zoom_level FROM %s WHERE table_name = ?;",
                                                       GeoPackageTiles.MatrixTableName);

        return new HashSet<Integer>(JdbcUtility.select(this.databaseConnection,
                                                       zoomLevelQuerySql,
                                                       new PreparedStatementConsumer()
                                                       {
                                                           @Override
                                                           public void accept(final PreparedStatement preparedStatement) throws SQLException
                                                           {
                                                               preparedStatement.setString(1, tileSet.getTableName());
                                                           }
                                                       },
                                                       new ResultSetFunction<Integer>()
                                                       {
                                                           @Override
                                                           public Integer apply(final ResultSet resultSet) throws SQLException
                                                           {
                                                               return resultSet.getInt(1);
                                                           }
                                                       }));
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "tiles"
     * data_type
     *
     * @return Returns a collection of {@link TileSet}s
     * @throws SQLException
     *             throws if the method
     *             {@link #getTileSets(SpatialReferenceSystem) getTileSets}
     *             throws
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
     *            Spatial reference system that returned {@link TileSet}s refer
     *            to
     * @return Returns a collection of {@link TileSet}s
     * @throws SQLException
     *             Throws if there's an SQL error
     */
    public Collection<TileSet> getTileSets(final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        return this.core.getContent(TileSet.TileContentType,
                                    new ContentFactory<TileSet>()
                                    {
                                        @Override
                                        public TileSet create(final String      tableName,
                                                              final String      dataType,
                                                              final String      identifier,
                                                              final String      description,
                                                              final String      lastChange,
                                                              final BoundingBox boundingBox,
                                                              final Integer     spatialReferenceSystemIdentifier)
                                        {
                                            return new TileSet(tableName,
                                                               identifier,
                                                               description,
                                                               lastChange,
                                                               boundingBox,
                                                               spatialReferenceSystemIdentifier);
                                        }
                                    },
                                    matchingSpatialReferenceSystem);
    }

    /**
     * Adds a tile matrix
     *
     * @param tileSet
     *             A handle to a tile set
     * @param zoomLevel
     *             The zoom level of the associated tile set (0 <= zoomLevel <=
     *             max_level)
     * @param matrixWidth
     *            The number of columns (>= 1) for this tile at this zoom level
     * @param matrixHeight
     *             The number of rows (>= 1) for this tile at this zoom level
     * @param tileWidth
     *             The tile width in pixels (>= 1) at this zoom level
     * @param tileHeight
     *             The tile height in pixels (>= 1) at this zoom level
     * @param pixelXSize
     *             The width of the associated tile set's spatial reference
     *             system or default meters for an undefined geographic
     *             coordinate reference system (SRS id 0) (> 0)
     * @param pixelYSize
     *             The height of the associated tile set's spatial reference
     *             system or default meters for an undefined geographic
     *             coordinate reference system (SRS id 0) (> 0)
     * @return Returns the newly added tile matrix
     * @throws SQLException
     *             throws when the method {@link #getTileMatrix(TileSet, int)
     *             getTileMatrix(TileSet, int)} or the method
     *             {@link #getTileMatrixSet(TileSet) getTileMatrixSet} or the
     *             database cannot roll back the changes after a different
     *             exception is thrown, an SQLException is thrown
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

//        final SpatialReferenceSystem srs = this.core.getSpatialReferenceSystem(tileSet.getSpatialReferenceSystemIdentifier());
//
//        int precision = CrsProfileFactory.create(srs.getOrganization(), srs.getOrganizationSrsId()).getPrecision();
        // TODO is there another way we can get the precision ? above causes runtime error for non-supported crs
        final int precision = 7;

        //should not be used. if its smaller we are ok.
        if(!compare(matrixWidth * tileWidth * pixelXSize,
                    tileMatrixSet.getBoundingBox().getWidth(),
                    precision))
        {
            throw new IllegalArgumentException("The geographic width of the tile matrix [matrix width * tile width (pixels) * pixel x size (srs units per pixel)] differs from the minimum bounds for this tile set specified by the tile matrix set");
        }


        if(!compare(matrixHeight * tileHeight * pixelYSize,
                    tileMatrixSet.getBoundingBox().getHeight(),
                    precision))
        {
            throw new IllegalArgumentException("The geographic height of the tile matrix [matrix height * tile height (pixels) * pixel y size (srs units per pixel)] differs from the minimum bounds for this tile set specified by the tile matrix set");
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

            JdbcUtility.update(this.databaseConnection,
                               insertTileMatrix,
                               new PreparedStatementConsumer()
                               {
                                   @Override
                                   public void accept(final PreparedStatement preparedStatement) throws SQLException
                                   {
                                       preparedStatement.setString(1, tileSet.getTableName());
                                       preparedStatement.setInt   (2, zoomLevel);
                                       preparedStatement.setInt   (3, matrixWidth);
                                       preparedStatement.setInt   (4, matrixHeight);
                                       preparedStatement.setInt   (5, tileWidth);
                                       preparedStatement.setInt   (6, tileHeight);
                                       preparedStatement.setDouble(7, pixelXSize);
                                       preparedStatement.setDouble(8, pixelYSize);
                                   }
                               });

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
     *            Tile set that which the tiles and tile metadata are associated
     * @param tileMatrix
     *            Tile matrix associated with the tile set at the corresponding
     *            zoom level
     * @param column
     *             The 'x' portion of the coordinate
     * @param row
     *             The 'y' portion of the coordinate
     * @param imageData
     *            The bytes of the image file
     * @return The Tile added to the GeoPackage with the properties of the
     *         parameters
     * @throws SQLException
     *             SQLException thrown by automatic close() invocation on
     *             preparedStatement or if the Database is unable to commit the
     *             changes or if the method
     *             {@link #getTile(TileSet, int, int, int) getTile}
     *             throws an SQLException or other various SQLExceptions
     */
    public Tile addTile(final TileSet    tileSet,
                        final TileMatrix tileMatrix,
                        final int        column,
                        final int        row,
                        final byte[]     imageData) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null");
        }

        if(tileMatrix == null)
        {
            throw new IllegalArgumentException("Tile matrix may not be null");
        }

        if(imageData == null || imageData.length == 0) // TODO the standard restricts the image types to image/jpeg, image/png and image/x-webp (by extension only: http://www.geopackage.org/spec/#extension_tiles_webp)
                                                       // TODO It'd be desirable to check the height/width of the image against the values described by the tile matrix, but this is difficult to do with a string of bytes.  One solution would be to changed to a java BufferedImage rather than raw bytes, but this *might* unnecessarily confine extension writers to to formats that fit into Java.ImageIO
        {
            throw new IllegalArgumentException("Image data may not be null or empty");
        }

        // Verify row and column are within the tile metadata's range
        if(row < 0 || row >= tileMatrix.getMatrixHeight())
        {
            throw new IllegalArgumentException(String.format("Tile row %d is outside of the valid row range [0, %d] (0 to tile matrix metadata's matrix height - 1)",
                                                             row,
                                                             tileMatrix.getMatrixHeight()-1));
        }
        if(column < 0 || column >= tileMatrix.getMatrixWidth())
        {
            throw new IllegalArgumentException(String.format("Tile column %d is outside of the valid column range [0, %d] (0 to tile matrix metadata's matrix width - 1)",
                                                             column,
                                                             tileMatrix.getMatrixWidth()-1));
        }

        final String insertTileSql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                                                   tileSet.getTableName(),
                                                   "zoom_level",
                                                   "tile_column",
                                                   "tile_row",
                                                   "tile_data");

        // TODO connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) isn't implemented by the current sqlite jdbc driver :(
//        final int tileIdentifier = JdbcUtility.update(this.databaseConnection,
//                                                      insertTileSql,
//                                                      new PreparedStatementConsumer()
//                                                      {
//                                                          @Override
//                                                          public void accept(final PreparedStatement preparedStatement) throws SQLException
//                                                          {
//                                                              preparedStatement.setInt  (1, tileMatrix.getZoomLevel());
//                                                                             preparedStatement.setInt  (2, column);
//                                                                             preparedStatement.setInt  (3, row);
//                                                                             preparedStatement.setBytes(4, imageData);  // .setBlob() didn't work as advertised in the sqlite-jdbc driver.  not sure about sqldroid
//                                                          }
//                                                      },
//                                                      new ResultSetFunction<Integer>()
//                                                      {
//                                                          @Override
//                                                          public Integer apply(final ResultSet resultSet) throws SQLException
//                                                          {
//                                                              return resultSet.getInt(1);    // tile identifier
//                                                          }
//                                                      });
//
//        this.databaseConnection.commit();
//
//        return new Tile(tileIdentifier,
//                        tileMatrix.getZoomLevel(),
//                        column,
//                        row,
//                        imageData);

        JdbcUtility.update(this.databaseConnection,
                           insertTileSql,
                           new PreparedStatementConsumer()
                           {
                               @Override
                               public void accept(final PreparedStatement preparedStatement) throws SQLException
                               {
                                   preparedStatement.setInt  (1, tileMatrix.getZoomLevel());
                                   preparedStatement.setInt  (2, column);
                                   preparedStatement.setInt  (3, row);
                                   preparedStatement.setBytes(4, imageData);  // .setBlob() didn't work as advertised in the sqlite-jdbc driver.  not sure about sqldroid
                               }
                           });

        this.databaseConnection.commit();

        return this.getTile(tileSet,
                            column,
                            row,
                            tileMatrix.getZoomLevel());
    }

    /**
     * Add a tile to the GeoPackage
     *
     * @param tileSet
     *            Tile set that which the tiles and tile metadata are associated
     * @param tileMatrix
     *            Tile matrix associated with the tile set at the corresponding
     *            zoom level
     * @param coordinate
     *            The coordinate of the tile in units of the tile set's spatial
     *            reference system
     * @param precision
     *            Specifies a tolerance for coordinate value testings to a number of decimal places
     * @param imageData
     *            The bytes of the image file
     * @return returns a Tile added to the GeoPackage with the properties of the
     *         parameters
     * @throws SQLException
     *             is thrown if the following methods throw
     *             {@link #crsToTileCoordinate(TileSet, CrsCoordinate, int, int)
     *             crsToRelativeTileCoordinate} or
     *             {@link #addTile(TileSet, TileMatrix, int, int, byte[])
     *             addTile} throws an SQLException
     */
    public Tile addTile(final TileSet       tileSet,
                        final TileMatrix    tileMatrix,
                        final CrsCoordinate coordinate,
                        final int           precision,
                        final byte[]        imageData) throws SQLException
    {
        final Coordinate<Integer> tileCoordinate = this.crsToTileCoordinate(tileSet,
                                                                            coordinate,
                                                                            precision,
                                                                            tileMatrix.getZoomLevel());
        return this.addTile(tileSet,
                            tileMatrix,
                            tileCoordinate.getX(),
                            tileCoordinate.getY(),
                            imageData);
    }

    /**
     * Gets tile coordinates for every tile in a tile set. A tile set need not
     * have an entry for every possible position in its respective tile
     * matrices.
     *
     * @param tileSet
     *            Handle to the tile set that the requested tiles should belong
     * @return Returns a {@link Collection} of {@link TileCoordinate}s
     *         representing every tile that the specific tile set contains.
     * @throws SQLException
     *             when SQLException thrown by automatic close() invocation on
     *             preparedStatement or if other SQLExceptions occur
     */
    public Collection<TileCoordinate> getTiles(final TileSet tileSet) throws SQLException
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

        return JdbcUtility.select(this.databaseConnection,
                                  tileQuery,
                                  null,
                                  new ResultSetFunction<TileCoordinate>()
                                  {
                                      @Override
                                      public TileCoordinate apply(final ResultSet resultSet) throws SQLException
                                      {
                                          return new TileCoordinate(resultSet.getInt(2),
                                                                    resultSet.getInt(3),
                                                                    resultSet.getInt(1));
                                      }
                                  });
    }

    /**
     * Gets a stream of every tile in the tile store for a given zoom level.
     * The zoom level need not  have an entry for every possible position in
     * its respective tile matrices. If there are no tiles at this zoom level,
     * an empty stream will be returned.
     *
     * @param tileSet
     *            Handle to the tile set that the requested tiles should belong
     * @param zoomLevel
     *            The zoom level of the requested tiles
     * @return Returns a {@link Collection} of relative tile {@link Coordinate}s
     *         representing every tile that the specific tile set contains.
     * @throws SQLException
     *             when SQLException thrown by automatic close() invocation on
     *             preparedStatement or if other SQLExceptions occur
     */
    public Collection<Coordinate<Integer>> getTiles(final TileSet tileSet, final int zoomLevel) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s FROM %s WHERE zoom_level = ?;",
                                               "tile_column",
                                               "tile_row",
                                               tileSet.getTableName());

        return JdbcUtility.select(this.databaseConnection,
                                  tileQuery,
                                  new PreparedStatementConsumer()
                                  {
                                      @Override
                                      public void accept(final PreparedStatement preparedStatement) throws SQLException
                                      {
                                          preparedStatement.setInt(1, zoomLevel);
                                      }
                                  },
                                  new ResultSetFunction<Coordinate<Integer>>()
                                  {
                                      @Override
                                      public Coordinate<Integer> apply(final ResultSet resultSet) throws SQLException
                                      {
                                          return new Coordinate<Integer>(resultSet.getInt(1), resultSet.getInt(2));
                                      }
                                  });
    }

    /**
     * Gets a tile
     *
     * @param tileSet
     *            Handle to the tile set that the requested tile should belong
     * @param column
     *             The 'x' portion of the coordinate
     * @param row
     *             The 'y' portion of the coordinate
     * @param zoomLevel
     *             The zoom level associated with the coordinate
     * @return Returns the requested tile, or null if it's not found
     * @throws SQLException
     *             SQLException thrown by automatic close() invocation on
     *             preparedStatement or if other SQLExceptions occur when adding
     *             the Tile data to the database
     */
    public Tile getTile(final TileSet tileSet,
                        final int     column,
                        final int     row,
                        final int     zoomLevel) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String tileQuery = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?;",
                                               "id",
                                               "zoom_level",
                                               "tile_column",
                                               "tile_row",
                                               "tile_data",
                                               tileSet.getTableName());

        return JdbcUtility.selectOne(this.databaseConnection,
                                     tileQuery,
                                     new PreparedStatementConsumer()
                                     {
                                         @Override
                                         public void accept(final PreparedStatement preparedStatement) throws SQLException
                                         {
                                             preparedStatement.setInt(1, zoomLevel);
                                             preparedStatement.setInt(2, column);
                                             preparedStatement.setInt(3, row);
                                         }
                                     },
                                     new ResultSetFunction<Tile>()
                                     {
                                         @Override
                                         public com.rgi.android.geopackage.tiles.Tile apply(final ResultSet resultSet) throws SQLException
                                         {
                                             return new Tile(resultSet.getInt(1),    // id
                                                             resultSet.getInt(2),    // zoom level
                                                             resultSet.getInt(3),    // column
                                                             resultSet.getInt(4),    // row
                                                             resultSet.getBytes(5)); // data
                                         }
                                     });
    }

    /**
     * Gets a tile
     *
     * @param tileSet
     *            Handle to the tile set that the requested tile should belong
     * @param coordinate
     *            Coordinate, in the units of the tile set's spatial reference
     *            system, of the requested tile
     * @param precision
     *            Specifies a tolerance for coordinate value testings to a number of decimal places
     * @param zoomLevel
     *            Zoom level
     * @return Returns the requested tile, or null if it's not found
     * @throws SQLException
     *             throws when the method
     *             {@link #crsToTileCoordinate(TileSet, CrsCoordinate, int, int)
     *             crsToRelativeTileCoordinate} or the method
     *             {@link #getTile(TileSet, int, int, int)} throws an
     *             SQLException
     */
    public Tile getTile(final TileSet       tileSet,
                        final CrsCoordinate coordinate,
                        final int           precision,
                        final int           zoomLevel) throws SQLException
    {
        final Coordinate<Integer> tileCoordinate = this.crsToTileCoordinate(tileSet,
                                                                             coordinate,
                                                                             precision,
                                                                             zoomLevel);

        return this.getTile(tileSet,
                            tileCoordinate.getX(),
                            tileCoordinate.getY(),
                            zoomLevel);
    }

    /**
     * Gets a tile set's tile matrix
     *
     * @param tileSet
     *            A handle to a set of tiles
     * @return Returns a tile set's tile matrix set
     * @throws SQLException
     *             SQLException thrown by automatic close() invocation on
     *             preparedStatement or if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             throws or other SQLExceptions occur when retrieving
     *             information from the database
     */
    public TileMatrixSet getTileMatrixSet(final TileSet tileSet) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set cannot be null");
        }

        final String querySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ?;",
                                              "table_name",
                                              "srs_id",
                                              "min_x",
                                              "min_y",
                                              "max_x",
                                              "max_y",
                                              GeoPackageTiles.MatrixSetTableName);

        return JdbcUtility.selectOne(this.databaseConnection,
                                     querySql,
                                     new PreparedStatementConsumer()
                                     {
                                         @Override
                                         public void accept(final PreparedStatement preparedStatement) throws SQLException
                                         {
                                             preparedStatement.setString(1, tileSet.getTableName());
                                         }
                                     },
                                     new ResultSetFunction<TileMatrixSet>()
                                     {
                                         @Override
                                         public com.rgi.android.geopackage.tiles.TileMatrixSet apply(final ResultSet resultSet) throws SQLException
                                         {
                                             return new TileMatrixSet(resultSet.getString(1),                                   // table name
                                                                      GeoPackageTiles.this.core.getSpatialReferenceSystem(resultSet.getInt(2)), // srs id
                                                                      new BoundingBox(resultSet.getDouble(3),                   // min x
                                                                                      resultSet.getDouble(4),                   // min y
                                                                                      resultSet.getDouble(5),                   // max x
                                                                                      resultSet.getDouble(6)));
                                         }
                                     });
    }

    /**
     * Gets a tile set object based on its table name
     *
     * @param tileSetTableName
     *            Name of a tile set table
     * @return Returns a TileSet or null if there isn't with the supplied table
     *         name
     * @throws SQLException
     *             throws if the method
     *             {@link GeoPackageCore#getContent}
     *             throws an SQLException
     */
    public TileSet getTileSet(final String tileSetTableName) throws SQLException
    {
        return this.core.getContent(tileSetTableName,
                                    new ContentFactory<TileSet>()
                                    {
                                        @Override
                                        public TileSet create(final String tableName,
                                                              final String dataType,
                                                              final String identifier,
                                                              final String description,
                                                              final String lastChange,
                                                              final BoundingBox boundingBox,
                                                              final Integer spatialReferenceSystemIdentifier)
                                        {
                                            return new TileSet(tableName,
                                                               identifier,
                                                               description,
                                                               lastChange,
                                                               boundingBox,
                                                               spatialReferenceSystemIdentifier);
                                        }
                                    });
    }

    /**
     * Adds a tile matrix associated with a tile set <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @param tableName
     *            Name of the tile set
     * @param boundingBox
     *            Bounding box of the tile matrix set
     * @param spatialReferenceSystem
     *            Spatial reference system of the tile matrix set
     * @throws SQLException
     *             thrown by automatic close() invocation on preparedStatement
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

        JdbcUtility.update(this.databaseConnection,
                           insertTileMatrixSetSql,
                           new PreparedStatementConsumer()
                           {
                               @Override
                               public void accept(final PreparedStatement preparedStatement) throws SQLException
                               {
                                   preparedStatement.setString(1, tableName);
                                   preparedStatement.setInt   (2, spatialReferenceSystem.getIdentifier());
                                   preparedStatement.setDouble(3, boundingBox.getMinX());
                                   preparedStatement.setDouble(4, boundingBox.getMinY());
                                   preparedStatement.setDouble(5, boundingBox.getMaxX());
                                   preparedStatement.setDouble(6, boundingBox.getMaxY());
                               }
                           });
    }

    /**
     * Get a tile set's tile matrix
     *
     * @param tileSet
     *            A handle to a set of tiles
     * @param zoomLevel
     *            Zoom level of the tile matrix
     * @return Returns a tile set's tile matrix that corresponds to the input
     *         level, or null if one doesn't exist
     * @throws SQLException
     *             SQLException thrown by automatic close() invocation on
     *             preparedStatement or when an SQLException occurs retrieving
     *             information from the database
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

        return JdbcUtility.selectOne(this.databaseConnection,
                                     tileQuery,
                                     new PreparedStatementConsumer()
                                     {
                                         @Override
                                         public void accept(final PreparedStatement preparedStatement) throws SQLException
                                         {
                                             preparedStatement.setString(1, tileSet.getTableName());
                                             preparedStatement.setInt   (2, zoomLevel);
                                         }
                                     },
                                     new ResultSetFunction<TileMatrix>()
                                     {
                                         @Override
                                         public com.rgi.android.geopackage.tiles.TileMatrix apply(final ResultSet resultSet) throws SQLException
                                         {
                                             return new TileMatrix(tileSet.getTableName(),
                                                                   zoomLevel,
                                                                   resultSet.getInt   (1),  // matrix width
                                                                   resultSet.getInt   (2),  // matrix height
                                                                   resultSet.getInt   (3),  // tile width
                                                                   resultSet.getInt   (4),  // tile height
                                                                   resultSet.getDouble(5),  // pixel x size
                                                                   resultSet.getDouble(6)); // pixel y size
                                         }
                                     });
    }

    /**
     * Gets the tile matrices associated with a tile set
     *
     * @param tileSet
     *            A handle to a set of tiles
     * @return Returns every tile matrix associated with a tile set in ascending
     *         order by zoom level
     * @throws SQLException
     *             SQLException thrown by automatic close() invocation on
     *             preparedStatement or when an SQLException occurs retrieving
     *             information from the database
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

        return JdbcUtility.select(this.databaseConnection,
                                  tileQuery,
                                  new PreparedStatementConsumer()
                                  {
                                      @Override
                                      public void accept(final PreparedStatement preparedStatement) throws SQLException
                                      {
                                          preparedStatement.setString(1, tileSet.getTableName());
                                      }
                                  },
                                  new ResultSetFunction<TileMatrix>()
                                  {
                                      @Override
                                      public com.rgi.android.geopackage.tiles.TileMatrix apply(final ResultSet resultSet) throws SQLException
                                      {
                                          return new TileMatrix(tileSet.getTableName(),
                                                                resultSet.getInt   (1),   // zoom level
                                                                resultSet.getInt   (2),   // matrix width
                                                                resultSet.getInt   (3),   // matrix height
                                                                resultSet.getInt   (4),   // tile width
                                                                resultSet.getInt   (5),   // tile height
                                                                resultSet.getDouble(6),   // pixel x size
                                                                resultSet.getDouble(7));  // pixel y size;
                                      }
                                  });
    }

    /**
     * Convert a CRS coordinate to a tile coordinate relative to a tile set
     *
     * @param tileSet
     *            A handle to a set of tiles
     * @param crsCoordinate
     *            A coordinate with a specified coordinate reference system
     * @param precision
     *            Specifies a tolerance for coordinate value testings to a number of decimal places
     * @param zoomLevel
     *            Zoom level
     * @return Returns a tile coordinate relative and specific to the input tile
     *         set. The input CRS coordinate would be contained in the the
     *         associated tile bounds.
     * @throws SQLException
     *             throws if the method {@link #getTileMatrix(TileSet, int)
     *             getTileMatrix} or the method
     *             {@link GeoPackageCore#getSpatialReferenceSystem(int)
     *             getSpatialReferenceSystem} or the method
     *             {@link #getTileMatrixSet(TileSet) getTileMatrixSet} throws
     */
    public Coordinate<Integer> crsToTileCoordinate(final TileSet       tileSet,
                                                   final CrsCoordinate crsCoordinate,
                                                   final int           precision,
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

        if(srs == null)
        {
            throw new IllegalArgumentException("Spatial Reference System may not be null."); //added due to coverity scan
        }

        if(!crs.getAuthority().equalsIgnoreCase(srs.getOrganization()) ||
           crs.getIdentifier() != srs.getOrganizationSrsId())
        {
            throw new IllegalArgumentException("Coordinate transformation is not currently supported.  The incoming spatial reference system must match that of the tile set's");
        }

        final TileMatrix tileMatrix = this.getTileMatrix(tileSet, zoomLevel);

        if(tileMatrix == null)
        {
            throw new IllegalArgumentException("Invalid zoom level for this tile set");
        }

        final TileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileSet);

        final BoundingBox tileSetBounds = tileMatrixSet.getBoundingBox();

        if(!BoundsUtility.contains(roundBounds(tileSetBounds, precision), crsCoordinate, GeoPackageTiles.Origin))
        {
            throw new IllegalArgumentException("The requested geographic coordinate is outside the bounds of the tile set");
        }

        final Coordinate<Double> boundsCorner = BoundsUtility.boundsCorner(roundBounds(tileSetBounds, precision), GeoPackageTiles.Origin);

        final double tileWidthInSrs  = tileMatrix.getPixelXSize() * tileMatrix.getTileWidth();
        final double tileHeightInSrs = tileMatrix.getPixelYSize() * tileMatrix.getTileHeight();

        final double normalizedSrsTileCoordinateX = Math.abs(crsCoordinate.getX() - boundsCorner.getX());
        final double normalizedSrsTileCoordinateY = Math.abs(crsCoordinate.getY() - boundsCorner.getY());

        final int divisor = 1000000000; // Round to integer extent

        final int tileX = (int)Math.floor(Math.round((normalizedSrsTileCoordinateX / tileWidthInSrs)  * divisor) / divisor);
        final int tileY = (int)Math.floor(Math.round((normalizedSrsTileCoordinateY / tileHeightInSrs) * divisor) / divisor);

        return new Coordinate<Integer>(tileX, tileY);
    }

    /**
     * Converts a tile coordinate, relative to the input tile set, to a
     * geographic point.  {@link GeoPackageTiles#Origin} is used as the
     * representative point of the tile.
     *
     * @param tileSet
     *            Handle to the tile set that the requested tile should belong
     * @param column
     *             The 'x' portion of the coordinate
     * @param row
     *             The 'y' portion of the coordinate
     * @param zoomLevel
     *             The zoom level associated with the coordinate
     * @return A {@link CrsCoordinate} point, using
     *             {@link GeoPackageTiles#Origin} as the representative corner
     *             of the tile.
     * @throws SQLException
     *             When there is an SQL failure in getting the tile matrix, the
     *             spatial reference system, or the tile matrix set of the
     *             input tile set.
     */
    public CrsCoordinate tileToCrsCoordinate(final TileSet tileSet,
                                             final int     column,
                                             final int     row,
                                             final int     zoomLevel) throws SQLException
    {
        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null");
        }

        if(column < 0)
        {
            throw new IllegalArgumentException("Column must be 0 or greater;");
        }

        if(row < 0)
        {
            throw new IllegalArgumentException("Row must be 0 or greater;");
        }

        final TileMatrix tileMatrix = this.getTileMatrix(tileSet, zoomLevel);

        if(tileMatrix == null)
        {
            throw new IllegalArgumentException("Invalid zoom level for this tile set");
        }

        final double tileWidthInSrs  = tileMatrix.getPixelXSize() * tileMatrix.getTileWidth();  // We could also divide the tile set bounds by the tile matrix width/height
        final double tileHeightInSrs = tileMatrix.getPixelYSize() * tileMatrix.getTileHeight();

        final SpatialReferenceSystem srs = this.core.getSpatialReferenceSystem(tileSet.getSpatialReferenceSystemIdentifier());

        if(srs == null)
        {
            throw new IllegalArgumentException("Spatial Reference System may not be null");//added due to coverity scan
        }

        final TileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileSet);

        final BoundingBox tileSetBounds = tileMatrixSet.getBoundingBox();

        final Coordinate<Double> boundsCorner = tileSetBounds.getTopLeft();

        return new CrsCoordinate(boundsCorner.getX() + (column * tileWidthInSrs),
                                 boundsCorner.getY() - (row    * tileHeightInSrs),
                                 new CoordinateReferenceSystem(srs.getOrganization(), srs.getOrganizationSrsId()));
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
            JdbcUtility.update(this.databaseConnection, this.getTileMatrixSetCreationSql());
        }

        // Create the tile matrix table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageTiles.MatrixTableName))
        {
            JdbcUtility.update(this.databaseConnection, this.getTileMatrixCreationSql());
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
     * Rounds the bounds to the appropriate level of accuracy
     * (2 decimal places for meters, 7 decimal places for degrees)
     * @param bounds
     *             A {@link BoundingBox} that needs to be rounded
     * @param precision
     *             The Coordinate Reference System of the bounds (to determine level of precision)
     * @return A {@link BoundingBox} with the minimum values rounded down, and the maximum values rounded up to the specified level of precision
     */
    private static BoundingBox roundBounds(final BoundingBox bounds, final int precision)
    {
        final double divisor = Math.pow(10, precision);

        return new BoundingBox(Math.floor(bounds.getMinX()*divisor) / divisor,
                               Math.floor(bounds.getMinY()*divisor) / divisor,
                               Math.ceil (bounds.getMaxX()*divisor) / divisor,
                               Math.ceil (bounds.getMaxY()*divisor) / divisor);
    }

    private static boolean compare(final double left, final double right, final int decimalPlaces)
    {
        final double divisor = Math.pow(10.0, decimalPlaces);

        return Math.abs(left - right) < (1/divisor);
    }

    private final GeoPackageCore core;
    private final Connection     databaseConnection;

    /**
     * The TileOrigin for GeoPackage's is UpperLeft
     * http://www.geopackage.org/spec/#clause_tile_matrix_table_data_values
     */
    public final static TileOrigin Origin = TileOrigin.UpperLeft;   // http://www.geopackage.org/spec/#clause_tile_matrix_table_data_values

    /**
     * The String name "gpkg_tile_matrix_set" of the database Tiles table
     * containing the Bounding Box information
     * http://www.geopackage.org/spec/#_tile_matrix_set
     */
    public final static String MatrixSetTableName = "gpkg_tile_matrix_set";
    /**
     * The String name "gpkg_tile_matrix" of the database Tiles table containing
     * the pixel x and y values, TileMatrixDimensions at a particular zoom level
     * http://www.geopackage.org/spec/#tile_matrix
     */
    public final static String MatrixTableName = "gpkg_tile_matrix";

    /**
     * Tile coordinate
     *
     */
    public class TileCoordinate
    {
        /**
         * @param column
         *         X portion of the coordinate
         * @param row
         *         Y portion of the coordinate
         * @param zoomLevel
         *         zoom level of the tile
         */
        private TileCoordinate(final int column, final int row, final int zoomLevel)
        {
            this.column    = column;
            this.row       = row;
            this.zoomLevel = zoomLevel;
        }

        /**
         * @return Returns the column (X) portion of the coordinate
         */
        public int getColumn()
        {
            return this.column;
        }

        /**
         * @return Returns the row (Y) portion of the coordinate
         */
        public int getRow()
        {
            return this.row;
        }

        /**
         * @return Returns the zoom level of the tile
         */
        public int getZoomLevel()
        {
            return this.zoomLevel;
        }

        private final int column;
        private final int row;
        private final int zoomLevel;
    }
}