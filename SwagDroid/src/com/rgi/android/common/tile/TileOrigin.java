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

package com.rgi.android.common.tile;

import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Luke Lambert
 *
 */
public enum TileOrigin
{
    /**
     * The TileOrigin of a tile is the UpperLeft corner
     */
    UpperLeft (0, 1),

    /**
     * The TileOrigin of a tile is the LowerLeft corner
     */
    LowerLeft (0, 0),

    /**
     * The TileOrigin of a tile is the UpperRight corner
     */
    UpperRight(1, 1),

    /**
     * The TileOrigin of a tile is the LowerRight corner
     */
    LowerRight(1, 0);

    TileOrigin(final int horizontal, final int vertical)
    {
        this.horizontal = horizontal;
        this.vertical   = vertical;
    }

    /**
     * @return vertical field of the TileOrigin
     */
    public int getVertical()
    {
        return this.vertical;
    }

    /**
     * @return horizontal field of the TileOrigin
     */
    public int getHorizontal()
    {
        return this.horizontal;
    }

    /**
     * Transforms a coordinate from one tile origin to its equivalent
     * coordinate in a different tile origin
     *
     * @param toOrigin
     *             The origin that the coordinate will be transformed to
     * @param tileX
     *             Tile column
     * @param tileY
     *             Tile row
     * @param matrixDimensions
     *             The dimensions of the tile matrix
     * @return Transformed tile coordinate
     */
    public Coordinate<Integer> transform(final TileOrigin toOrigin, final int tileX, final int tileY, final TileMatrixDimensions matrixDimensions)
    {
        if(toOrigin == null)
        {
            throw new IllegalArgumentException("Requested tile origin may not be null");
        }

        if(matrixDimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        return new Coordinate<Integer>(this.transformHorizontal(toOrigin, tileX, matrixDimensions.getWidth()),
                                       this.transformVertical  (toOrigin, tileY, matrixDimensions.getHeight()));
    }

    private int transformHorizontal(final TileOrigin toOrigin, final int tileX, final int tileMatrixWidth)
    {
        return transform(this.getHorizontal(), toOrigin.getHorizontal(), tileX, tileMatrixWidth);
    }

    private int transformVertical(final TileOrigin toOrigin, final int tileY, final int tileMatrixHeight)
    {
        return transform(this.getVertical(), toOrigin.getVertical(), tileY, tileMatrixHeight);
    }

    private static int transform(final int fromDirection, final int toDirection, final int tileCoordinate, final int tileMatrixDimension)
    {
        final int maxTileCoordinate = tileMatrixDimension - 1;

        return tileCoordinate + (fromDirection ^ toDirection) * (maxTileCoordinate - 2*tileCoordinate);
    }

    private final int horizontal;
    private final int vertical;
}
