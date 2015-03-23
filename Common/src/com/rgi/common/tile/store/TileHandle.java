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

package com.rgi.common.tile.store;

import java.awt.image.BufferedImage;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * A handle to access tile data.  Implementations are strongly encouraged to
 * lazy-load or calculate its properties, and then cache the result.
 *
 * @author Luke Lambert
 *
 */
public interface TileHandle
{
    /**
     * @return Returns the tile's zoom level
     */
    public int getZoomLevel();

    /**
     * @return Returns the column (x) of the tile. Column values are relative
     *             to the tile scheme to which this tile belongs
     */
    public int getColumn();

    /**
     * @return Returns the row (y) of the tile. Row values are relative
     *             to the tile scheme to which this tile belongs
     */
    public int getRow();

    /**
     * @return Returns the maximum number of columns and rows at this tile's
     *             zoom level
     * @throws TileStoreException
     *             Occurs when the requested zoom level doesn't exist for the
     *             tile handle's containing tile set
     */
    public TileMatrixDimensions getMatrix() throws TileStoreException;

    /**
     * @return Returns the real world coordinate of this tile's origin in the
     *             unit of its enclosing tile set
     * @throws TileStoreException
     *             Occurs when there's an error in converting a tile coordinate
     *             to one of the associated coordinate reference system
     */
    public CrsCoordinate getCrsCoordinate() throws TileStoreException;

    /**
     * @param corner
     *             Selects the corner of the tile to represent as the CRS coordinate
     * @return Returns the real world coordinate of this tile based on the
     *             corner parameter in the unit of its enclosing tile set
     * @throws TileStoreException
     *             Occurs when there's an error in converting a tile coordinate
     *             to one of the associated coordinate reference system
     */
    public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException;

    /**
     * @return Returns the bounding box of a tile in real world CRS units
     * @throws TileStoreException
     *             A TileStoreException is thrown when unable to get the tile's
     *             row, column, or matrix that is needed to calculate the
     *             Bounds
     */
    public BoundingBox getBounds() throws TileStoreException;

    /**
     * @return Returns the tile's image data
     * @throws TileStoreException
     *             A TileStoreException occurs if unable to retrieve the
     *             specified tile
     */
    public BufferedImage getImage() throws TileStoreException;
}
