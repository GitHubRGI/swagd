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
package common.tile.scheme;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.android.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
public class TileMatrixDimensionsTest
{

    @Test
    public void verifyTileMatrixDimensions()
    {
        final int height = 12;
        final int width  = 77;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height);
        assertTrue(String.format("TileMatrixDimensions did not return the expected values.\nActual: (%d, %d).\nReturned: (%d, %d).",
                                  dimensions.getWidth(),
                                  dimensions.getHeight(),
                                  width,
                                  height),
                   dimensions.getHeight() == height &&
                   dimensions.getWidth()  == width);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new TileMatrixDimensions(0, 10);
        fail("Expected an IllegalArgumentException to be thrown when width is less than or equal to 0.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new TileMatrixDimensions(10, 0);
        fail("Expected an IllegalArgumentException to be thrown when height is less than or equal to 0.");
    }

    @Test
    public void containsTrue()
    {
        final int height = 12;
        final int width  = 77;

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height);

        this.assertContains(dimensions, width - 1, height - 1, true); //test top

        this.assertContains(dimensions, 0, 0, true);  //bottom

        this.assertContains(dimensions, width/2, height/2, true); //middle
    }

    @Test
    public void containsFalse()
    {
        final int height = 12;
        final int width  = 77;

        final TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height);

        this.assertContains(dimensions, -1, 0, false);  //test left

        this.assertContains(dimensions, 0, -1, false);  //test down

        this.assertContains(dimensions, width - 1, height, false); //test up

        this.assertContains(dimensions, width, height - 1, false); //test right
    }

    private void assertContains(final TileMatrixDimensions dimensions, final int column, final int row, final boolean expectedOutcome)
    {
        assertTrue(String.format("Expected the method contains to return true for the following values.\nDimensions: (%d, %d)\nTile Coordinate(%d, %d).",
                                  dimensions.getWidth(),
                                  dimensions.getHeight(),
                                  column,
                                  row),
                  dimensions.contains(column, row) == expectedOutcome);
    }

}
