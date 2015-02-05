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

package com.rgi.common.tile;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Luke Lambert
 *
 */
public enum TileOrigin
{
    UpperLeft (1, 0),
    LowerLeft (0, 0),
    UpperRight(1, 1),
    LowerRight(0, 1);

    TileOrigin(final int vertical, final int horizontal)
    {
        this.horizontal = horizontal;
        this.vertical   = vertical;
    }

    private final int horizontal;
    private final int vertical;

    public Coordinate<Integer> transform(final TileOrigin toOrigin, final Coordinate<Integer> tileCoordinate, final TileMatrixDimensions matrixDimensions)
    {
        if(toOrigin == null)
        {
            throw new IllegalArgumentException("Requested tile origin may not be null");
        }

        if(tileCoordinate == null)
        {
            throw new IllegalArgumentException("Tile coordinate may not be null");
        }

        if(matrixDimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        return new Coordinate<>(this.transformVertical  (toOrigin, tileCoordinate.getY().intValue(), matrixDimensions.getHeight()),
                                this.transformHorizontal(toOrigin, tileCoordinate.getX().intValue(), matrixDimensions.getWidth()));
    }

    /**
     * @return the horizontal
     */
    public int getHorizontal()
    {
        return this.horizontal;
    }

    /**
     * @return the vertical
     */
    public int getVertical()
    {
        return this.vertical;
    }

    public Coordinate<Integer> transform(final TileOrigin toOrigin, final int tileY, final int tileX, final TileMatrixDimensions matrixDimensions)
    {
        if(toOrigin == null)
        {
            throw new IllegalArgumentException("Requested tile origin may not be null");
        }

        if(matrixDimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        return new Coordinate<>(this.transformVertical  (toOrigin, tileY, matrixDimensions.getHeight()),
                                this.transformHorizontal(toOrigin, tileX, matrixDimensions.getWidth()));
    }

    public int transformHorizontal(final TileOrigin toOrigin, final int tileX, final int tileMatrixWidth)
    {
        return transform(this.horizontal, toOrigin.horizontal, tileX, tileMatrixWidth);
    }

    public int transformVertical(final TileOrigin toOrigin, final int tileY, final int tileMatrixHeight)
    {
        return transform(this.vertical, toOrigin.vertical, tileY, tileMatrixHeight);
    }

    private static int transform(final int fromDirection, final int toDirection, final int tileCoordinate, final int tileMatrixDimension)
    {
        final int maxTileCoordinate = tileMatrixDimension - 1;

        return tileCoordinate + (fromDirection ^ toDirection) * (maxTileCoordinate - 2*tileCoordinate);
    }
}
