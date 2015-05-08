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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.rgi.android.common.tile.scheme.TileMatrixDimensions;
import com.rgi.android.common.tile.scheme.ZoomTimesTwo;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
public class ZoomTimesTwoTest
{

    @Test
    public void verifyZoomTimesTwoDimensions()
    {
        final int minZoom = 4;
        final int maxZoom = 9;

        final int minZoomWidth  = 2;
        final int minZoomHeight = 3;

        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, minZoomWidth, minZoomHeight);

        final Map<Integer, TileMatrixDimensions> expectedDimensions = new HashMap<Integer, TileMatrixDimensions>();
        expectedDimensions.put(minZoom,     new TileMatrixDimensions(minZoomWidth,    minZoomHeight));
        expectedDimensions.put(minZoom + 1, new TileMatrixDimensions(minZoomWidth*2,  minZoomHeight*2));
        expectedDimensions.put(minZoom + 2, new TileMatrixDimensions(minZoomWidth*4,  minZoomHeight*4));
        expectedDimensions.put(minZoom + 3, new TileMatrixDimensions(minZoomWidth*8,  minZoomHeight*8));
        expectedDimensions.put(minZoom + 4, new TileMatrixDimensions(minZoomWidth*16, minZoomHeight*16));
        expectedDimensions.put(minZoom + 5, new TileMatrixDimensions(minZoomWidth*32, minZoomHeight*32));

        for(final Integer zoom: expectedDimensions.keySet())
        {
            final TileMatrixDimensions returnedDimensions = tileScheme.dimensions(zoom);
            assertDimensions(expectedDimensions.get(zoom), returnedDimensions);
        }
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new ZoomTimesTwo(-1, 0 , 1, 1); //negative min zoom level
        fail("Expected an IllegalArgumentException when the minimum zoom level was less than 0.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new ZoomTimesTwo(0, -1, 1, 1); //negative max zoom level
        fail("Expected an IllegalArgumentException when the maximum zoom level was less than 0.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        new ZoomTimesTwo(2, 3, 1, 0); //matrix height < 1
        fail("Expected an IllegalArgumentException when the matrix height was less than 1.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        new ZoomTimesTwo(2, 3, 0, 1); //matrix width < 1
        fail("Expected an IllegalArgumentException when the matrix width was less than 1.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        new ZoomTimesTwo(10, 2, 1, 1); //min zoom > max zoom
        fail("Expected an IllegalArgumentException when the minimum zoom is greater than maximum zoom.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException6()
    {
        new ZoomTimesTwo(1, 21, 1028, 100); // (2^21 - 1)* 1028 > Integer.MaxValue -> matrix width and zoom causes overflow
        fail("Expected an IllegalArgumentException when the combination of initial width and maximum zoom level will cause an integer overflow for tile numbering.");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException7()
    {
        new ZoomTimesTwo(1, 21, 100, 1028); // (2^21 - 1)* 1028 > Integer.MaxValue -> matrix height and zoom causes overflow
        fail("Expected an IllegalArgumentException when the combination of initial height and maximum zoom level will cause an integer overflow for tile numbering.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException8()
    {
        final int minZoom = 0;
        final int maxZoom = 12;

        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, 2, 3);
        tileScheme.dimensions(minZoom - 1);

        fail("Expected an IllegalArgumentException when requesting for a dimensions less than the minimum zoom level.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException9()
    {
        final int minZoom = 0;
        final int maxZoom = 12;

        final ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, 2, 3);
        tileScheme.dimensions(maxZoom + 1);

        fail("Expected an IllegalArgumentException when requesting for a dimensions greater than the maximum zoom level.");
    }

    private static void assertDimensions(final TileMatrixDimensions expectedTileMatrixDimensions, final TileMatrixDimensions returnedDimensions)
    {
        assertTrue(String.format("TileMatrixDimensions returned from the method dimensions() were not the expected values.\nActual: (%d, %d).\nReturned: (%d, %d).",
                                 returnedDimensions.getWidth(),
                                 returnedDimensions.getHeight(),
                                 expectedTileMatrixDimensions.getWidth(),
                                 expectedTileMatrixDimensions.getHeight()),
                  expectedTileMatrixDimensions.getWidth()  == returnedDimensions.getWidth() &&
                  expectedTileMatrixDimensions.getHeight() == returnedDimensions.getHeight());
    }
}
