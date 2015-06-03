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

import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.android.common.tile.scheme.TileMatrixDimensions;

/**
 *
 * @author Mary Carome
 *
 */
@SuppressWarnings({ "static-method" })
public class TileOriginTest
{
    /**
     * Tests that transform throws an IllegalArgumentException when given a null
     * value for TileOrigin
     */
    @Test(expected = IllegalArgumentException.class)
    public void transformIllegalArgumentException1()
    {
        final int x = 10;
        final int y = 12;
        final TileOrigin origin = TileOrigin.LowerLeft;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(10, 12);
        origin.transform(null, x, y, dimensions);
        fail("Expected TileOrigin method transform(TileOrigin, int, int TileMatrixDimensions) to throw an IllegalArgumentException when given a null value");
    }

    /**
     * Tests that transform throws an IllegalArgumentException when given a null
     * value for TileOrigin
     */
    @Test(expected = IllegalArgumentException.class)
    public void transformIllegalArgumentException()
    {
        final int x = 12;
        final int y = 10;
        final TileOrigin origin = TileOrigin.LowerLeft;
        origin.transform(origin, x, y, null);
        fail("Expected TileOrigin method transform(TileOrigin, int, int TileMatrixDimensions) to throw an IllegalArgumentException when given a null value");
    }
}
