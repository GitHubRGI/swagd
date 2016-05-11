package com.rgi.geopackage.features.geometry.z;

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

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.Contents;
import com.rgi.geopackage.features.GeometryType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.rgi.geopackage.features.geometry.z.WkbGeometryZ.GeometryTypeDimensionalityBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class WkbPointZTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new WkbPointZ(0.0, 0.0, 0.0);
    }

    /**
     * Test the coordinate constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void coordinateConstructor()
    {
        new WkbPointZ(new CoordinateZ(0.0, 0.0, 0.0));
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        //noinspection CastToConcreteClass
        new WkbPointZ(null);
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
     * Test equals
     */
    @Test
    public void testEquals()
    {
        final WkbPointZ point = new WkbPointZ(0.0, 0.0, 0.0);

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
        final WkbPointZ point = new WkbPointZ(0.0, 0.0, 0.0);

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
        final WkbPointZ point = new WkbPointZ(0.0, 0.0, 0.0);

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
        final WkbPointZ point1 = new WkbPointZ(0.0, 0.0, 0.0);
        final WkbPointZ point2 = new WkbPointZ(0.0, 0.0, 0.0);

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
        final WkbPointZ point = new WkbPointZ(1.0, 1.0, 1.0);

        assertEquals("Hash code failed",
                     32505856,
                     point.hashCode());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        assertFalse("isEmpty failed",
                    new WkbPointZ(1.0, 1.0, 1.0).isEmpty());

        assertTrue("isEmpty failed",
                   new WkbPointZ(Double.NaN,
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
                   new WkbPointZ(1.0, 1.0, 1.0).getContents());

        assertSame("getContents failed",
                   Contents.Empty,
                   new WkbPointZ(Double.NaN,
                                 Double.NaN,
                                 Double.NaN).getContents());
    }

    /**
     * Test createEnvelopeZ()
     */
    @Test
    public void createEnvelope()
    {
        final double x = 1.0;
        final double y = 1.0;
        final double z = 1.0;

        @SuppressWarnings("CastToConcreteClass")
        final EnvelopeZ envelope = (EnvelopeZ)new WkbPointZ(x, y, z).createEnvelope();

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

        @SuppressWarnings("CastToConcreteClass")
        final EnvelopeZ emptyEnvelope = (EnvelopeZ)new WkbPointZ(Double.NaN,
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
    }

    /**
     * Test createEnvelopeZ()
     */
    @Test
    public void createEnvelopeZ()
    {
        final double x = 1.0;
        final double y = 1.0;
        final double z = 1.0;

        final EnvelopeZ envelope = new WkbPointZ(x, y, z).createEnvelopeZ();

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

        final EnvelopeZ emptyEnvelope = new WkbPointZ(Double.NaN,
                                                      Double.NaN,
                                                      Double.NaN).createEnvelopeZ();

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

            final WkbPointZ point = new WkbPointZ(x, y, z);
            point.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPointZ found = WkbPointZ.readWellKnownBinary(byteBuffer);

            assertEquals("writeWellKnownBinary failed",
                         point,
                         found);
        }
    }

    /**
     * Test accessors
     */
    @Test
    public void accessors()
    {
        final double x = 0.0;
        final double y = 0.0;
        final double z = 0.0;

        final WkbPointZ point = new WkbPointZ(x, y, z);

        assertEquals("getX() returned the wrong value",
                     x,
                     point.getX(),
                     0.0);

        assertEquals("getY() returned the wrong value",
                     y,
                     point.getY(),
                     0.0);

        assertEquals("getZ() returned the wrong value",
                     z,
                     point.getZ(),
                     0.0);
    }

    /**
     * Test getTypeCode()
     */
    @Test
    public void getTypeCode()
    {
        assertEquals("getTypeCode() returned the wrong value",
                     GeometryTypeDimensionalityBase + GeometryType.Point.getCode(),
                     new WkbPointZ(0.0, 0.0, 0.0).getTypeCode());
    }


    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName() returned the wrong value",
                     GeometryType.Point.toString(),
                     new WkbPointZ(0.0, 0.0, 0.0).getGeometryTypeName());
    }
}
