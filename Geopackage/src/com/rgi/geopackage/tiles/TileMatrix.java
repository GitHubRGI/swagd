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


/**
 * @author Luke Lambert
 *
 */

public class TileMatrix
{
    /**
     * Constructor
     *
     * @param tableName
     *             Name of the tile set table that this tile matrix corresponds to
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
     *             The width of the associated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (> 0) (SRS units per pixel)
     * @param pixelYSize
     *             The height of the associated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (> 0) (SRS units per pixel)
     */
    protected TileMatrix(final String tableName,
                         final int    zoomLevel,
                         final int    matrixWidth,
                         final int    matrixHeight,
                         final int    tileWidth,
                         final int    tileHeight,
                         final double pixelXSize,
                         final double pixelYSize)
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Table name may not be null or empty");
        }

        if(zoomLevel < 0)
        {
            throw new IllegalArgumentException("Zoom level may not be less than 0");
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

        this.tableName    = tableName;
        this.zoomLevel    = zoomLevel;
        this.matrixWidth  = matrixWidth;
        this.matrixHeight = matrixHeight;
        this.tileWidth    = tileWidth;
        this.tileHeight   = tileHeight;
        this.pixelXSize   = pixelXSize;
        this.pixelYSize   = pixelYSize;
    }

    /**
     * @return the table name of the associated tile set
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return the zoom level of the associated tile set (0 <= zoomLevel <= max_level)
     */
    public int getZoomLevel()
    {
        return this.zoomLevel;
    }

    /**
     * @return the number of columns (>= 1) for this tile at this zoom level
     */
    public int getMatrixWidth()
    {
        return this.matrixWidth;
    }

    /**
     * @return the number of rows (>= 1) for this tile at this zoom level
     */
    public int getMatrixHeight()
    {
        return this.matrixHeight;
    }

    /**
     * @return the tile width in pixels (>= 1) at this zoom level
     */
    public int getTileWidth()
    {
        return this.tileWidth;
    }

    /**
     * @return the tile height in pixels (>= 1) at this zoom level
     */
    public int getTileHeight()
    {
        return this.tileHeight;
    }

    /**
     * @return the width of the assicated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (SRS units per pixel)
     */
    public double getPixelXSize()
    {
        return this.pixelXSize;
    }

    /**
     * @return the height of the assicated tile set's spatial reference system or default meters for an undefined geographic coordinate reference system (SRS id 0) (SRS units per pixel)
     */
    public double getPixelYSize()
    {
        return this.pixelYSize;
    }

    public boolean equals(final String inTableName,
                          final int    inZoomLevel,
                          final int    inMatrixWidth,
                          final int    inMatrixHeight,
                          final int    inTileWidth,
                          final int    inTileHeight,
                          final double inPixelXSize,
                          final double inPixelYSize)
    {
        return this.tableName.equals(inTableName)  &&
               this.zoomLevel    == inZoomLevel    &&
               this.matrixWidth  == inMatrixWidth  &&
               this.matrixHeight == inMatrixHeight &&
               this.tileWidth    == inTileWidth    &&
               this.tileHeight   == inTileHeight   &&
               this.pixelXSize   == inPixelXSize   &&
               this.pixelYSize   == inPixelYSize;
    }

    private final String tableName;
    private final int    zoomLevel;
    private final int    matrixWidth;
    private final int    matrixHeight;
    private final int    tileWidth;
    private final int    tileHeight;
    private final double pixelXSize;
    private final double pixelYSize;
}
