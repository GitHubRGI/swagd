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

package com.rgi.common.tile.scheme;

/**
 * Encapsulation of the dimensions of a tile matrix.  A tile matrix is a
 * rectangular set of tiles for a particular zoom level of a tile set.
 *
 * @author Luke Lambert
 *
 */
public class TileMatrixDimensions
{
    /**
     * Constructor
     *
     * @param width
     *             The number of columns in the matrix
     * @param height
     *             The number of rows in the matrix (rows)
     */
    public TileMatrixDimensions(final int width, final int height)
    {
        if(height <= 0)
        {
            throw new IllegalArgumentException("Height must be greater than 0");
        }

        if(width <= 0)
        {
            throw new IllegalArgumentException("Height must be greater than 0");
        }

        this.height = height;
        this.width  = width;
    }

    /**
     * @return the height (number of rows)
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * @return the width (number of columns)
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Tests if a tile coordinate (column, row) is within the bounds of this
     * tile matrix
     *
     * @param column
     *             The horizontal portion of a tile coordinate
     * @param row
     *             The vertical portion of a tile coordinate
     *
     * @return Returns true if the row and column are within the matrix dimensions
     */
    public boolean contains(final int column, final int row)
    {
        return column >= 0          &&
               row    >= 0          &&
               column < this.width  &&
               row    < this.height;
    }

    private final int height;
    private final int width;
}
