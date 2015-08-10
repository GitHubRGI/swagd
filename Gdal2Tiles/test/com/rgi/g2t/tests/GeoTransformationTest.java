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

package com.rgi.g2t.tests;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.g2t.GeoTransformation;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GeoTransformationTest
{
    /**
     * Tests constructor throws an IllegalArgumentException when
     * given a null array
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException1()
    {
        @SuppressWarnings("unused")
        final GeoTransformation geoTransformation = new GeoTransformation(null);

        fail("Expected GeoTransformation constructor to throw an IllegalArgumentException when the given array is null.");
    }

    /**
     * Tests constructor throws and IllegalArgumentException when
     * given an array not of length 6
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentExcpetion2()
    {
        final double[] affineTransform = new double[3];
        @SuppressWarnings("unused")
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        fail("Expected GeoTransformation constructor to throw an IllegalArgumentException when the given array's length is not 6.");
    }

    /**
     * Tests the constructor correctly sets the value of the affine transformation array,
     * the top left coordinate, and the pixel resolution
     */
    @SuppressWarnings({"static-method", "MagicNumber"})
    @Test
    public void constructorTest()
    {
        final double[] affineTransform = {0, 1, 2, 3, 4, 5};
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        assertTrue("GeoTransformation constructor did not properly set the affine transformation array",
                Arrays.equals(affineTransform, geoTransformation.getAffineTransform()));

        assertEquals("GeoTransformation constructor did not properly set the top left coordinate.",
                new Coordinate<>(0.0, 3.0),
                geoTransformation.getTopLeft());

        assertTrue("GeoTransformation constructor did not properly set the pixel resolution.",
                   geoTransformation.getPixelResolution().getHeight().equals(-5.0) &&
                   geoTransformation.getPixelResolution().getWidth().equals(1.0));
    }

    /**
     * Tests isNorthUp
     */
    @SuppressWarnings("static-method")
    @Test
    public void testIsNorthUp1()
    {
        final double[] affineTransform = {0, 1, 2, 3, 4, 5};
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        assertFalse("GeoTransformation method isNorthUp returned true instead of false.",
                    geoTransformation.isNorthUp());
    }

    /**
     * Tests is NorthUp
     */
    @SuppressWarnings("static-method")
    @Test
    public void testIsNorthUp2()
    {
        final double[] affineTransform = {0, 1, 0, 3, 4, 5};
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        assertFalse("GeoTransformation method isNorthUp returned true instead of false.",
                    geoTransformation.isNorthUp());
    }

    /**
     * Tests is NorthUp
     */
    @SuppressWarnings("static-method")
    @Test
    public void testIsNorthUp3()
    {
        final double[] affineTransform = {0, 1, 2, 3, 0, 5};
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        assertFalse("GeoTransformation method isNorthUp returned true instead of false.",
                    geoTransformation.isNorthUp());
    }

    /**
     * Tests is NorthUp
     */
    @SuppressWarnings("static-method")
    @Test
    public void testIsNorthUp4()
    {
        final double[] affineTransform = {2, 1, 0, 3, 0, 5};
        final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);

        assertTrue("GeoTransformation method isNorthUp returned true instead of false.",
                geoTransformation.isNorthUp());
    }

    @SuppressWarnings({"static-method", "MagicNumber"})
    @Test
    public void testGetBounds()
    {
        gdal.AllRegister();
        final Dataset data = gdal.GetDriverByName("MEM").Create("data", 100, 300, 0);
        final double[] affineTransform = {0, 1, 2, 3, 4, 5};
        final BoundingBox box = new BoundingBox(0, 3-5 * 300.0, 100, 3);

        try
        {
            final GeoTransformation geoTransformation = new GeoTransformation(affineTransform);
            assertEquals("GeoTransformation method getBounds(Dataset) did not return the correct BoundingBox",
                         box,
                         geoTransformation.getBounds(data));
        }
        finally
        {
            data.delete();
        }

    }
}
