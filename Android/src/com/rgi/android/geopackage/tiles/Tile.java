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

/**
 * An object representation of an entry in a tile pyramid user data table
 * @author Luke Lambert
 *
 */
public class Tile
{
    /**
     * Constructor
     *
     * @param identifier
     *             Auto-incremented primary key
     * @param zoomLevel
     *             zoom level
     * @param column
     *             X component of the tile's Cartesian coordinate
     * @param row
     *             Y component of the tile's Cartesian coordinate
     * @param imageData
     *             Bytes of an image file
     */
    protected Tile(final int    identifier,
                   final int    zoomLevel,
                   final int    column,
                   final int    row,
                   final byte[] imageData)
    {
        if(zoomLevel < 0)
        {
            throw new IllegalArgumentException("Zoom level must be 0 or greater");
        }

        if(row < 0)
        {
            throw new IllegalArgumentException("Row must be 0 or greater");
        }

        if(column < 0)
        {
            throw new IllegalArgumentException("Column must be 0 or greater");
        }

        if(imageData == null)
        {
            throw new IllegalArgumentException("Data cannot be null");
        }

        this.identifier = identifier;
        this.zoomLevel  = zoomLevel;
        this.row        = row;
        this.column     = column;
        this.imageData  = imageData;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the zoomLevel
     */
    public int getZoomLevel()
    {
        return this.zoomLevel;
    }

    /**
     * @return the column
     */
    public int getColumn()
    {
        return this.column;
    }

    /**
     * @return the row
     */
    public int getRow()
    {
        return this.row;
    }

    /**
     * @return the imageData
     */
    public byte[] getImageData()
    {
        return this.imageData;
    }

    private final int    identifier;
    private final int    zoomLevel;
    private final int    column;
    private final int    row;
    private final byte[] imageData;
}
