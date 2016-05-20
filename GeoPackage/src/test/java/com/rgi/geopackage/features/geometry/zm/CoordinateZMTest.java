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

package com.rgi.geopackage.features.geometry.zm;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.Contents;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class CoordinateZMTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new CoordinateZM(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals()
    {
        final CoordinateZM coord = new CoordinateZM(0.0, 0.0, 0.0, 0.0);

        //noinspection EqualsWithItself,SimplifiableJUnitAssertion
        assertTrue("Equals failed on self reference",
                   coord.equals(coord));
    }

    /**
     * Test equals with null
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsWithNull()
    {
        final CoordinateZM coord = new CoordinateZM(0.0, 0.0, 0.0, 0.0);

        //noinspection EqualsWithItself
        assertFalse("Equals should have failed on null comparison",
                    coord.equals(null));
    }

    /**
     * Test equals with a different object type
     */
    @Test
    public void testEqualsWithDifferentObjectType()
    {
        final CoordinateZM coord = new CoordinateZM(0.0, 0.0, 0.0, 0.0);

        //noinspection EqualsWithItself,UnnecessaryBoxing,EqualsBetweenInconvertibleTypes
        assertFalse("Equals should fail on a different object type",
                    coord.equals(Integer.valueOf(0)));
    }

    /**
     * Test equals
     */
    @Test
    public void testEqualsTrue()
    {
        final CoordinateZM coord1 = new CoordinateZM(0.0, 0.0, 0.0, 0.0);
        final CoordinateZM coord2 = new CoordinateZM(0.0, 0.0, 0.0, 0.0);

        //noinspection SimplifiableJUnitAssertion
        assertTrue("Equals failed to return true",
                   coord1.equals(coord2));
    }

    /**
     * Test hashCode()
     */
    @Test
    public void testHashCode()
    {
        final CoordinateZM coord = new CoordinateZM(1.0, 1.0, 1.0, 1.0);

        assertEquals("Hash code failed",
                     2080374784,
                     coord.hashCode());
    }

    /**
     * Test toString()
     */
    @Test
    public void testToString()
    {
        final CoordinateZM coord = new CoordinateZM(1.0, 1.0, 1.0, 1.0);

        assertEquals("To string failed",
                     String.format("(%f, %f, %f, %f)",
                                   coord.getX(),
                                   coord.getY(),
                                   coord.getZ(),
                                   coord.getM()),
                     coord.toString());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        assertFalse("isEmpty failed",
                    new CoordinateZM(1.0, 1.0, 1.0, 1.0).isEmpty());

        assertTrue("isEmpty failed",
                   new CoordinateZM(Double.NaN,
                                    Double.NaN,
                                    Double.NaN,
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
                   new CoordinateZM(1.0, 1.0, 1.0, 1.0).getContents());

        assertSame("getContents failed",
                   Contents.Empty,
                   new CoordinateZM(Double.NaN,
                                    Double.NaN,
                                    Double.NaN,
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
        final double z = 1.0;
        final double m = 1.0;

        final EnvelopeZM envelope = new CoordinateZM(x, y, z, m).createEnvelope();

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

        assertEquals("createEnvelope failed",
                     z,
                     envelope.getMinimumZ(),
                     0.0);

        assertEquals("createEnvelope failed",
                     z,
                     envelope.getMaximumZ(),
                     0.0);

        assertEquals("createEnvelope failed",
                     m,
                     envelope.getMinimumM(),
                     0.0);

        assertEquals("createEnvelope failed",
                     m,
                     envelope.getMaximumM(),
                     0.0);

        final EnvelopeZM emptyEnvelope = new CoordinateZM(Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN).createEnvelope();

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumZ()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumZ()));

        assertTrue("createEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumM()));

        assertTrue("createEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumM()));
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
            final double z = 1.0;
            final double m = 1.0;

            new CoordinateZM(x, y, z, m).writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            assertEquals("writeWellKnownBinary incorrectly wrote x",
                         x,
                         byteBuffer.getDouble(),
                         0.0);

            assertEquals("writeWellKnownBinary incorrectly wrote y",
                         y,
                         byteBuffer.getDouble(),
                         0.0);

            assertEquals("writeWellKnownBinary incorrectly wrote z",
                         z,
                         byteBuffer.getDouble(),
                         0.0);

            assertEquals("writeWellKnownBinary incorrectly wrote m",
                         m,
                         byteBuffer.getDouble(),
                         0.0);
        }
    }
}
