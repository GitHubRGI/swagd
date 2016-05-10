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
public class WkbMultiPolygonMTest
{
    /**
     * Test the ellipsis constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void ellipsisConstructor()
    {
        new WkbMultiPolygonM(new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                             new CoordinateM(0.0, 1.0, 0.0),
                                                             new CoordinateM(1.0, 1.0, 0.0)),
                                             new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                             new CoordinateM(0.0, 0.5, 0.0),
                                                             new CoordinateM(0.5, 0.5, 0.0))),
                             new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                             new CoordinateM(0.0, 1.0, 0.0),
                                                             new CoordinateM(1.0, 1.0, 0.0)),
                                             new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                             new CoordinateM(0.0, 0.5, 0.0),
                                                             new CoordinateM(0.5, 0.5, 0.0))));
    }

    /**
     * Test the collection constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void collectionConstructor()
    {
        new WkbMultiPolygonM(Arrays.asList(new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 1.0, 0.0),
                                                                           new CoordinateM(1.0, 1.0, 0.0)),
                                                           new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 0.5, 0.0),
                                                                           new CoordinateM(0.5, 0.5, 0.0))),
                                           new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 1.0, 0.0),
                                                                           new CoordinateM(1.0, 1.0, 0.0)),
                                                           new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 0.5, 0.0),
                                                                           new CoordinateM(0.5, 0.5, 0.0)))));
    }

    /**
     * Test the collection constructor with a null collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNull()
    {
        //noinspection CastToConcreteClass
        new WkbMultiPolygonM((WkbPolygonM)null);
    }

    /**
     * Test the collection constructor with a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNullGeometry()
    {
        //noinspection CastToConcreteClass
        new WkbMultiPolygonM(Arrays.asList((WkbPolygonM)null));
    }

    /**
     * Test getTypeCode()
     */
    @Test
    public void getTypeCode()
    {
        assertEquals("getTypeCode() returned the wrong value",
                     GeometryTypeDimensionalityBase + GeometryType.MultiPolygon.getCode(),
                     new WkbMultiPolygonM().getTypeCode());
    }

    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName() returned the wrong value",
                     GeometryType.MultiPolygon.toString(),
                     new WkbMultiPolygonM().getGeometryTypeName());
    }

    /**
     * Test getLineStrings()
     */
    @Test
    public void getLineStrings()
    {
        final List<WkbPolygonM> polygons = Arrays.asList(new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                                         new CoordinateM(0.0, 1.0, 0.0),
                                                                                         new CoordinateM(1.0, 1.0, 0.0)),
                                                                         new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                                         new CoordinateM(0.0, 0.5, 0.0),
                                                                                         new CoordinateM(0.5, 0.5, 0.0))),
                                                         new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                                         new CoordinateM(0.0, 1.0, 0.0),
                                                                                         new CoordinateM(1.0, 1.0, 0.0)),
                                                                         new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                                         new CoordinateM(0.0, 0.5, 0.0),
                                                                                         new CoordinateM(0.5, 0.5, 0.0))));

        assertEquals("getLineStrings() returned the wrong value",
                     polygons,
                     new WkbMultiPolygonM(polygons).getPolygons());
    }

    /**
     * Test the serialization of write/read well known binary
     */
    @Test
    public void writeReadWellKnownBinary()
    {
        final WkbMultiPolygonM polygons = new WkbMultiPolygonM(new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                                               new CoordinateM(0.0, 1.0, 0.0),
                                                                                               new CoordinateM(1.0, 1.0, 0.0)),
                                                                               new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                                               new CoordinateM(0.0, 0.5, 0.0),
                                                                                               new CoordinateM(0.5, 0.5, 0.0))),
                                                               new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                                               new CoordinateM(0.0, 1.0, 0.0),
                                                                                               new CoordinateM(1.0, 1.0, 0.0)),
                                                                               new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                                               new CoordinateM(0.0, 0.5, 0.0),
                                                                                               new CoordinateM(0.5, 0.5, 0.0))));

        try(final ByteOutputStream output = new ByteOutputStream())
        {
            polygons.writeWellKnownBinary(output);

            final WkbMultiPolygonM read = WkbMultiPolygonM.readWellKnownBinary(ByteBuffer.wrap(output.array()));

            assertEquals("error in well known binary (WKB) reading/writing",
                         polygons,
                         read);
        }
    }
}
