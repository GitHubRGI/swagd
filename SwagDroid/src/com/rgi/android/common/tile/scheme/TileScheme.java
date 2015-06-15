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

import java.util.Collection;

/**
 * Mechanism to describe the number of tiles in a tile set, at a given zoom
 * level
 *
 * @author Luke Lambert
 *
 */
public interface TileScheme
{
    /**
     * Calculates the height and width of the tile matrix for a given zoom level
     *
     * @param zoomLevel
     *             Zoom level
     * @return Returns a {@link TileMatrixDimensions} specifying the width and
     *             height of a tile matrix at the given zoom level
     */
    public TileMatrixDimensions dimensions(final int zoomLevel);

    /**
     * @return Returns a {@link Collection} of all unique valid zoom levels for
     *             this tile scheme
     */
    public Collection<Integer> getZoomLevels();
}
