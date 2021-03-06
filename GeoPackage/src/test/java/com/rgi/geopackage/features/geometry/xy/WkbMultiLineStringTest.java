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

package com.rgi.geopackage.features.geometry.xy;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.GeometryType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Luke Lambert
 */
public class WkbMultiLineStringTest
{
    /**
     * Test the ellipsis constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void ellipsisConstructor()
    {
        new WkbMultiLineString(new WkbLineString(new Coordinate(0.0, 0.0),
                                                 new Coordinate(0.0, 0.0)),
                               new WkbLineString(new Coordinate(0.0, 0.0),
                                                 new Coordinate(0.0, 0.0)));
    }

    /**
     * Test the collection constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void collectionConstructor()
    {
        new WkbMultiLineString(Arrays.asList(new WkbLineString(new Coordinate(0.0, 0.0),
                                                               new Coordinate(0.0, 0.0)),
                                             new WkbLineString(new Coordinate(0.0, 0.0),
                                                               new Coordinate(0.0, 0.0))));
    }

    /**
     * Test the collection constructor with a null collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNull()
    {
        //noinspection CastToConcreteClass
        new WkbMultiLineString((Collection<WkbLineString>)null);
    }

    /**
     * Test the collection constructor with a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void collectionConstructorWithNullGeometry()
    {
        //noinspection CastToConcreteClass
        new WkbMultiLineString(Arrays.asList((WkbLineString)null));
    }

    /**
     * Test getTypeCode()
     */
    @Test
    public void getTypeCode()
    {
        assertEquals("getTypeCode() returned the wrong value",
                     GeometryType.MultiLineString.getCode(),
                     new WkbMultiLineString().getTypeCode());
    }

    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName() returned the wrong value",
                     GeometryType.MultiLineString.toString(),
                     new WkbMultiLineString().getGeometryTypeName());
    }

    /**
     * Test getLineStrings()
     */
    @Test
    public void getLineStrings()
    {
        final List<WkbLineString> strings = Arrays.asList(new WkbLineString(new Coordinate(0.0, 0.0),
                                                                            new Coordinate(0.0, 0.0)),
                                                          new WkbLineString(new Coordinate(0.0, 0.0),
                                                                            new Coordinate(0.0, 0.0)));

        assertEquals("getLineStrings() returned the wrong value",
                     strings,
                     new WkbMultiLineString(strings).getLineStrings());
    }

    /**
     * Test the serialization of write/read well known binary
     */
    @Test
    public void writeReadWellKnownBinary()
    {
        final WkbMultiLineString multiLineString = new WkbMultiLineString(Arrays.asList(new WkbLineString(new Coordinate(0.0, 0.0),
                                                                                                          new Coordinate(0.0, 0.0)),
                                                                                        new WkbLineString(new Coordinate(0.0, 0.0),
                                                                                                          new Coordinate(0.0, 0.0))));

        try(final ByteOutputStream output = new ByteOutputStream())
        {
            multiLineString.writeWellKnownBinary(output);

            final WkbMultiLineString read = WkbMultiLineString.readWellKnownBinary(ByteBuffer.wrap(output.array()));

            assertEquals("error in well known binary (WKB) reading/writing",
                         multiLineString,
                         read);
        }
    }
}
