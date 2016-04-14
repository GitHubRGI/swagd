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

/**
 * Tile coordinate
 *
 * @author Luke Lambert
 */
public final class TileCoordinate
{
    /**
     * Constructor
     *
     * @param column
     *         X portion of the coordinate
     * @param row
     *         Y portion of the coordinate
     * @param zoomLevel
     *         zoom level of the tile
     */
    TileCoordinate(final int column, final int row, final int zoomLevel)
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
