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

package com.rgi.geopackage.features.geometry.z;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.GeometryType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class WkbLineStringZTest
{
    /**
     * Test the ellipsis constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void ellipsisConstructor()
    {
        new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                           new CoordinateZ(0.0, 0.0, 0.0),
                           new CoordinateZ(0.0, 0.0, 0.0));
    }

    /**
     * Test the collection constructor for failure on a null coordinate collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        new WkbLineStringZ((Collection<CoordinateZ>)null);
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCoordinate()
    {
        new WkbLineStringZ(Arrays.asList(new CoordinateZ(0.0, 0.0, 0.0),
                                         new CoordinateZ(0.0, 0.0, 0.0),
                                         null));
    }

    /**
     * Test equals with the same object
     */
    @Test
    public void testEqualsSameObject()
    {
        final WkbLineStringZ ring = new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0));

        //noinspection EqualsWithItself,SimplifiableJUnitAssertion
        assertTrue("equals returned false for testing an object against itself",
                   ring.equals(ring));
    }

    /**
     * Test equals with null
     */
    @Test
    public void testEqualsWithNull()
    {
        final WkbLineStringZ ring = new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0));

        //noinspection SimplifiableJUnitAssertion,ObjectEqualsNull
        assertFalse("equals returned true for testing against null",
                   ring.equals(null));
    }

    /**
     * Test equals with a different type
     */
    @Test
    public void testEqualsWithDifferentType()
    {
        final WkbLineStringZ ring = new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0),
                                                       new CoordinateZ(0.0, 0.0, 0.0));

        //noinspection UnnecessaryBoxing,EqualsBetweenInconvertibleTypes
        assertFalse("equals returned true for testing with an inconvertible type",
                    ring.equals(Integer.valueOf(0)));
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals()
    {
        final WkbLineStringZ ring1 = new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                                                        new CoordinateZ(0.0, 0.0, 0.0),
                                                        new CoordinateZ(0.0, 0.0, 0.0));

        final WkbLineStringZ ring2 = new WkbLineStringZ(new CoordinateZ(0.0, 0.0, 0.0),
                                                        new CoordinateZ(0.0, 0.0, 0.0),
                                                        new CoordinateZ(0.0, 0.0, 0.0));

        assertEquals("equals returned false for testing equivalent objects",
                     ring1,
                     ring2);
    }

    /**
     * Test hashCode()
     */
    @Test
    public void testHashCode()
    {
        assertEquals("hashCode returned the wrong value",
                     778531935,
                     new WkbLineStringZ(new CoordinateZ(1.0, 3.0, 31.0),
                                        new CoordinateZ(1.0, 3.0, 31.0),
                                        new CoordinateZ(1.0, 3.0, 31.0)).hashCode());
    }

    /**
     * Test getCoordinates()
     */
    @Test
    public void getCoordinates()
    {
        final List<CoordinateZ> coordinates = Arrays.asList(new CoordinateZ(1.0, 3.0, 31.0),
                                                            new CoordinateZ(1.0, 3.0, 31.0),
                                                            new CoordinateZ(1.0, 3.0, 31.0));

        assertEquals("getCoordinates return the wrong value",
                     coordinates,
                     new WkbLineStringZ(coordinates).getCoordinates());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        assertTrue("isEmpty() should have returned true",
                   new WkbLineStringZ().isEmpty());
    }

    /**
     * Test createEnvelopeZ()
     */
    @Test
    public void createEnvelopeZ()
    {
        final EnvelopeZ emptyEnvelope = new WkbLineStringZ().createEnvelopeZ();

        assertTrue("envelope minimum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("envelope minimum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("envelope minimum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumZ()));

        assertTrue("envelope maximum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("envelope maximum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("envelope maximum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumZ()));

        final double min = 0.0;
        final double max = 1.0;

        final EnvelopeZ envelope = new WkbLineStringZ(new CoordinateZ(min, min, min),
                                                      new CoordinateZ(max, max, max)).createEnvelopeZ();

        assertEquals("combine() picked the wrong minimum x value",
                     min,
                     envelope.getMinimumX(),
                     0.0);

        assertEquals("combine() picked the wrong minimum y value",
                     min,
                     envelope.getMinimumY(),
                     0.0);

        assertEquals("combine() picked the wrong minimum z value",
                     min,
                     envelope.getMinimumZ(),
                     0.0);

        assertEquals("combine() picked the wrong maximum x value",
                     max,
                     envelope.getMaximumX(),
                     0.0);

        assertEquals("combine() picked the wrong maximum y value",
                     max,
                     envelope.getMaximumY(),
                     0.0);

        assertEquals("combine() picked the wrong maximum z value",
                     max,
                     envelope.getMaximumZ(),
                     0.0);
    }

    /**
     * Test the serialization of write/read well known binary
     */
    @Test
    public void writeReadWellKnownBinary()
    {
        final WkbLineStringZ ring = new WkbLineStringZ(new CoordinateZ(1.0, 3.0, 31.0),
                                                         new CoordinateZ(1.0, 3.0, 31.0),
                                                         new CoordinateZ(1.0, 3.0, 31.0));

        try(final ByteOutputStream output = new ByteOutputStream())
        {
            ring.writeWellKnownBinary(output);

            final WkbLineStringZ foundRing = WkbLineStringZ.readWellKnownBinary(ByteBuffer.wrap(output.array()));

            assertEquals("error in well known binary (WKB) reading/writing",
                         ring,
                         foundRing);
        }
    }

    /**
     * Test writeWellKnownBinary with a null {@link ByteOutputStream}
     */
    @Test(expected = IllegalArgumentException.class)
    public void writeWellKnownBinaryWithNull()
    {
        new WkbLineStringZ().writeWellKnownBinary(null);

    }

    /**
     * Test readWellKnownBinary with a null {@link ByteBuffer}
     */
    @Test(expected = IllegalArgumentException.class)
    public void readWellKnownBinaryWithNull()
    {
        WkbLineStringZ.readWellKnownBinary(null);

    }

    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName returned the wrong value",
                     GeometryType.LineString.toString(),
                     new WkbLineStringZ().getGeometryTypeName());
    }

    /**
     * Test createEnvelope()
     */
    @Test
    public void createEnvelope()
    {
        @SuppressWarnings("CastToConcreteClass")
        final EnvelopeZ emptyEnvelope = (EnvelopeZ)new WkbLineStringZ().createEnvelope();

        assertTrue("envelope minimum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("envelope minimum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("envelope minimum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumZ()));

        assertTrue("envelope maximum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("envelope maximum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("envelope maximum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumZ()));

        final double min = 0.0;
        final double max = 1.0;

        final EnvelopeZ envelope = new WkbLineStringZ(new CoordinateZ(min, min, min),
                                                      new CoordinateZ(max, max, max)).createEnvelopeZ();

        assertEquals("combine() picked the wrong minimum x value",
                     min,
                     envelope.getMinimumX(),
                     0.0);

        assertEquals("combine() picked the wrong minimum y value",
                     min,
                     envelope.getMinimumY(),
                     0.0);

        assertEquals("combine() picked the wrong minimum z value",
                     min,
                     envelope.getMinimumZ(),
                     0.0);

        assertEquals("combine() picked the wrong maximum x value",
                     max,
                     envelope.getMaximumX(),
                     0.0);

        assertEquals("combine() picked the wrong maximum y value",
                     max,
                     envelope.getMaximumY(),
                     0.0);

        assertEquals("combine() picked the wrong maximum z value",
                     max,
                     envelope.getMaximumZ(),
                     0.0);
    }
}
