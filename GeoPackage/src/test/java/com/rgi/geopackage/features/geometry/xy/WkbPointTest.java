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
import com.rgi.geopackage.features.Contents;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class WkbPointTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new WkbPoint(0.0, 0.0);
    }

    /**
     * Test the coordinate constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void coordinateConstructor()
    {
        new WkbPoint(new Coordinate(0.0, 0.0));
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        //noinspection CastToConcreteClass
        new WkbPoint(null);
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCoordinate()
    {
        new WkbLineString(Arrays.asList(new Coordinate(0.0, 0.0),
                                          new Coordinate(0.0, 0.0),
                                          null));
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals()
    {
        final WkbPoint point = new WkbPoint(0.0, 0.0);

        //noinspection EqualsWithItself,SimplifiableJUnitAssertion
        assertTrue("Equals failed on self reference",
                   point.equals(point));
    }

    /**
     * Test equals with null
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsWithNull()
    {
        final WkbPoint point = new WkbPoint(0.0, 0.0);

        //noinspection EqualsWithItself
        assertFalse("Equals should have failed on null comparison",
                    point.equals(null));
    }

    /**
     * Test equals with a different object type
     */
    @Test
    public void testEqualsWithDifferentObjectType()
    {
        final WkbPoint point = new WkbPoint(0.0, 0.0);

        //noinspection EqualsWithItself,UnnecessaryBoxing,EqualsBetweenInconvertibleTypes
        assertFalse("Equals should fail on a different object type",
                    point.equals(Integer.valueOf(0)));
    }

    /**
     * Test equals
     */
    @Test
    public void testEqualsTrue()
    {
        final WkbPoint point1 = new WkbPoint(0.0, 0.0);
        final WkbPoint point2 = new WkbPoint(0.0, 0.0);

        //noinspection SimplifiableJUnitAssertion
        assertTrue("Equals failed to return true",
                   point1.equals(point2));
    }

    /**
     * Test hashCode()
     */
    @Test
    public void testHashCode()
    {
        final WkbPoint point = new WkbPoint(1.0, 1.0);

        assertEquals("Hash code failed",
                     -33554432,
                     point.hashCode());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        assertFalse("isEmpty failed",
                    new WkbPoint(1.0, 1.0).isEmpty());

        assertTrue("isEmpty failed",
                   new WkbPoint(Double.NaN,
                                Double.NaN).isEmpty());
    }

    /**
     * Test getContents()
     */
    @Test
    public void getContents()
    {
        assertSame("getContents failed",
                   Contents.NotEmpty,
                   new WkbPoint(1.0, 1.0).getContents());

        assertSame("getContents failed",
                   Contents.Empty,
                   new WkbPoint(Double.NaN,
                                Double.NaN).getContents());
    }

    /**
     * Test createEnvelope()
     */
    @Test
    public void createEnvelope()
    {
        final double x = 1.0;
        final double y = 1.0;

        final Envelope envelope = new WkbPoint(x, y).createEnvelope();

        assertEquals("createEnvelope failed",
                     x,
                     envelope.getMinimumX(),
                     0.0);

        assertEquals("createEnvelope failed",
                     x,
                     envelope.getMaximumX(),
                     0.0);

        assertEquals("createEnvelope failed",
                     y,
                     envelope.getMinimumY(),
                     0.0);

        assertEquals("createEnvelope failed",
                     y,
                     envelope.getMaximumY(),
                     0.0);

        final Envelope emptyEnvelope = new WkbPoint(Double.NaN,
                                                    Double.NaN).createEnvelope();

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumY()));
    }

    /**
     * Test writeWellKnownBinary
     */
    @Test
    public void writeWellKnownText()
    {
        try(final ByteOutputStream output = new ByteOutputStream())
        {
            final double x = 1.0;
            final double y = 1.0;

            final WkbPoint point = new WkbPoint(x, y);
            point.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPoint found = WkbPoint.readWellKnownBinary(byteBuffer);

            assertEquals("writeWellKnownBinary failed",
                         point,
                         found);
        }
    }
}
