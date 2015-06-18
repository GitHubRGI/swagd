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

package com.rgi.android.common.tile.scheme;

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
