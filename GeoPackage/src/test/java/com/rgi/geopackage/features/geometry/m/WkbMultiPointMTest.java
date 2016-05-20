/*
 * The MIT License (MIT)
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

package com.rgi.geopackage.features.geometry.m;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.GeometryType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static com.rgi.geopackage.features.geometry.m.WkbGeometryM.GeometryTypeDimensionalityBase;
import static org.junit.Assert.assertEquals;

/**
 * @author Luke Lambert
 */
public class WkbMultiPointMTest
{
    /**
     * Test the ellipsis constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void ellipsisConstructor()
    {
        new WkbMultiPointM(new WkbPointM(0.0, 0.0, 0.0),
                           new WkbPointM(0.0, 0.0, 0.0),
                           new WkbPointM(0.0, 0.0, 0.0),
                           new WkbPointM(0.0, 0.0, 0.0));
    }

    /**
     * Test the collection constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void collectionConstructor()
    {
        new WkbMultiPointM(Arrays.asList(new WkbPointM(0.0, 0.0, 0.0),
                                         new WkbPointM(0.0, 0.0, 0.0),
                                         new WkbPointM(0.0, 0.0, 0.0),
                                         new WkbPointM(0.0, 0.0, 0.0)));
    }

    /**
     * Test the collection constructor with a null collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNull()
    {
        //noinspection CastToConcreteClass
        new WkbMultiPointM((WkbPointM)null);
    }

    /**
     * Test the collection constructor with a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNullGeometry()
    {
        //noinspection CastToConcreteClass
        new WkbMultiPointM(Arrays.asList((WkbPointM)null));
    }

    /**
     * Test getTypeCode()
     */
    @Test
    public void getTypeCode()
    {
        assertEquals("getTypeCode() returned the wrong value",
                     GeometryTypeDimensionalityBase + GeometryType.MultiPoint.getCode(),
                     new WkbMultiPointM().getTypeCode());
    }

    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName() returned the wrong value",
                     GeometryType.MultiPoint.toString(),
                     new WkbMultiPointM().getGeometryTypeName());
    }

    /**
     * Test getLineStrings()
     */
    @Test
    public void getLineStrings()
    {
        final List<WkbPointM> points = Arrays.asList(new WkbPointM(0.0, 0.0, 0.0),
                                                     new WkbPointM(0.0, 0.0, 0.0),
                                                     new WkbPointM(0.0, 0.0, 0.0),
                                                     new WkbPointM(0.0, 0.0, 0.0));

        assertEquals("getLineStrings() returned the wrong value",
                     points,
                     new WkbMultiPointM(points).getPoints());
    }

    /**
     * Test the serialization of write/read well known binary
     */
    @Test
    public void writeReadWellKnownBinary()
    {
        final WkbMultiPointM points = new WkbMultiPointM(new WkbPointM(0.0, 0.0, 0.0),
                                                         new WkbPointM(0.0, 0.0, 0.0),
                                                         new WkbPointM(0.0, 0.0, 0.0),
                                                         new WkbPointM(0.0, 0.0, 0.0));

        try(final ByteOutputStream output = new ByteOutputStream())
        {
            points.writeWellKnownBinary(output);

            final WkbMultiPointM read = WkbMultiPointM.readWellKnownBinary(ByteBuffer.wrap(output.array()));

            assertEquals("error in well known binary (WKB) reading/writing",
                         points,
                         read);
        }
    }
}
