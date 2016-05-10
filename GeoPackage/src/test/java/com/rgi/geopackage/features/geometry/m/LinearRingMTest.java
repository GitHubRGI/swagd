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
public class LinearRingMTest
{
    /**
     * Test the ellipsis constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void ellipsisConstructor()
    {
        new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                        new CoordinateM(0.0, 0.0, 0.0),
                        new CoordinateM(0.0, 0.0, 0.0));
    }

    /**
     * Test the collection constructor for failure on a null coordinate collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        new LinearRingM((Collection<CoordinateM>)null);
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCoordinate()
    {
        new LinearRingM(Arrays.asList(new CoordinateM(0.0, 0.0, 0.0),
                                      new CoordinateM(0.0, 0.0, 0.0),
                                      null));
    }

    /**
     * Test equals with the same object
     */
    @Test
    public void testEqualsSameObject()
    {
        final LinearRingM ring = new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                                                 new CoordinateM(0.0, 0.0, 0.0),
                                                 new CoordinateM(0.0, 0.0, 0.0));

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
        final LinearRingM ring = new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                                                   new CoordinateM(0.0, 0.0, 0.0),
                                                   new CoordinateM(0.0, 0.0, 0.0));

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
        final LinearRingM ring = new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                                                   new CoordinateM(0.0, 0.0, 0.0),
                                                   new CoordinateM(0.0, 0.0, 0.0));

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
        final LinearRingM ring1 = new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                                                  new CoordinateM(0.0, 0.0, 0.0),
                                                  new CoordinateM(0.0, 0.0, 0.0));

        final LinearRingM ring2 = new LinearRingM(new CoordinateM(0.0, 0.0, 0.0),
                                                  new CoordinateM(0.0, 0.0, 0.0),
                                                  new CoordinateM(0.0, 0.0, 0.0));

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
                     -62426017,
                     new LinearRingM(new CoordinateM(1.0, 3.0, 31.0),
                                     new CoordinateM(1.0, 3.0, 31.0),
                                     new CoordinateM(1.0, 3.0, 31.0)).hashCode());
    }

    /**
     * Test getCoordinates()
     */
    @Test
    public void getCoordinates()
    {
        final List<CoordinateM> coordinates = Arrays.asList(new CoordinateM(1.0, 3.0, 31.0),
                                                            new CoordinateM(1.0, 3.0, 31.0),
                                                            new CoordinateM(1.0, 3.0, 31.0));

        assertEquals("getCoordinates return the wrong value",
                     coordinates,
                     new LinearRingM(coordinates).getCoordinates());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        assertTrue("isEmpty() should have returned true",
                   new LinearRingM().isEmpty());
    }

    /**
     * Test createEnvelope()
     */
    @Test
    public void createEnvelope()
    {
        final EnvelopeM emptyEnvelope = new LinearRingM().createEnvelope();

        assertTrue("envelope minimum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("envelope minimum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("envelope minimum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMinimumM()));

        assertTrue("envelope maximum x value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("envelope maximum y value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("envelope maximum z value is wrong",
                   Double.isNaN(emptyEnvelope.getMaximumM()));

        final double min = 0.0;
        final double max = 1.0;

        final EnvelopeM envelope = new LinearRingM(new CoordinateM(min, min, min),
                                                   new CoordinateM(max, max, max)).createEnvelope();

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
                     envelope.getMinimumM(),
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
                     envelope.getMaximumM(),
                     0.0);
    }

    /**
     * Test the serialization of write/read well known binary
     */
    @Test
    public void writeReadWellKnownBinary()
    {
        final LinearRingM ring = new LinearRingM(new CoordinateM(1.0, 3.0, 31.0),
                                                 new CoordinateM(1.0, 3.0, 31.0),
                                                 new CoordinateM(1.0, 3.0, 31.0));

        try(final ByteOutputStream output = new ByteOutputStream())
        {
            ring.writeWellKnownBinary(output);

            final LinearRingM foundRing = LinearRingM.readWellKnownBinary(ByteBuffer.wrap(output.array()));

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
        new LinearRingM().writeWellKnownBinary(null);

    }

    /**
     * Test readWellKnownBinary with a null {@link ByteBuffer}
     */
    @Test(expected = IllegalArgumentException.class)
    public void readWellKnownBinaryWithNull()
    {
        LinearRingM.readWellKnownBinary(null);

    }
}
