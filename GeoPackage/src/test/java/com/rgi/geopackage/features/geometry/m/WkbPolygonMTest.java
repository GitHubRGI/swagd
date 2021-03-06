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
import com.rgi.geopackage.features.Contents;
import com.rgi.geopackage.features.GeometryType;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class WkbPolygonMTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                        new CoordinateM(0.0, 1.0, 0.0),
                                        new CoordinateM(1.0, 1.0, 0.0)),
                        new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                        new CoordinateM(0.0, 0.5, 0.0),
                                        new CoordinateM(0.5, 0.5, 0.0)));
    }

    /**
     * Test the coordinate constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void coordinateConstructor()
    {
        new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                        new CoordinateM(1.0, 1.0, 0.0),
                                        new CoordinateM(0.0, 1.0, 0.0)),
                        Arrays.asList(new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                      new CoordinateM(0.0, 0.5, 0.0),
                                                      new CoordinateM(0.5, 0.5, 0.0))));
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        new WkbPolygonM(null);
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCoordinate()
    {
        new WkbLineStringM(Arrays.asList(new CoordinateM(0.0, 0.0, 0.0),
                                         new CoordinateM(0.0, 0.0, 0.0),
                                         null));
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        //noinspection EqualsWithItself,SimplifiableJUnitAssertion
        assertTrue("Equals failed on self reference",
                   polygon.equals(polygon));
    }

    /**
     * Test equals with null
     */
    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void testEqualsWithNull()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        //noinspection EqualsWithItself
        assertFalse("Equals should have failed on null comparison",
                    polygon.equals(null));
    }

    /**
     * Test equals with a different object type
     */
    @Test
    public void testEqualsWithDifferentObjectType()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        //noinspection EqualsWithItself,UnnecessaryBoxing,EqualsBetweenInconvertibleTypes
        assertFalse("Equals should fail on a different object type",
                    polygon.equals(Integer.valueOf(0)));
    }

    /**
     * Test equals
     */
    @Test
    public void testEqualsTrue()
    {
        final WkbPolygonM polygon1 = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                     new CoordinateM(0.0, 1.0, 0.0),
                                                                     new CoordinateM(1.0, 1.0, 0.0)),
                                                     new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                     new CoordinateM(0.0, 0.5, 0.0),
                                                                     new CoordinateM(0.5, 0.5, 0.0)));
        final WkbPolygonM polygon2 = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                     new CoordinateM(0.0, 1.0, 0.0),
                                                                     new CoordinateM(1.0, 1.0, 0.0)),
                                                     new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                     new CoordinateM(0.0, 0.5, 0.0),
                                                                     new CoordinateM(0.5, 0.5, 0.0)));

        //noinspection SimplifiableJUnitAssertion
        assertTrue("Equals failed to return true",
                   polygon1.equals(polygon2));
    }

    /**
     * Test hashCode()
     */
    @Test
    public void testHashCode()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        assertEquals("Hash code failed",
                     -907113473,
                     polygon.hashCode());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        assertFalse("isEmpty failed",
                    polygon.isEmpty());

        final WkbPolygonM emptyPolygon = new WkbPolygonM(new LinearRingM());

        assertTrue("isEmpty failed",
                   emptyPolygon.isEmpty());
    }

    /**
     * Test getContents()
     */
    @Test
    public void getContents()
    {
        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 1.0, 0.0),
                                                                    new CoordinateM(1.0, 1.0, 0.0)),
                                                    new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                    new CoordinateM(0.0, 0.5, 0.0),
                                                                    new CoordinateM(0.5, 0.5, 0.0)));

        assertSame("getContents failed",
                   Contents.NotEmpty,
                   polygon.getContents());

        final WkbPolygonM emptyPolygon = new WkbPolygonM(new LinearRingM());

        assertSame("getContents failed",
                   Contents.Empty,
                   emptyPolygon.getContents());
    }

    /**
     * Test createEnvelopeM()
     */
    @Test
    public void createEnvelopeM()
    {
        final double x = 1.0;
        final double y = 1.0;
        final double m = 1.0;

        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(x, y, m),
                                                                    new CoordinateM(x, y, m),
                                                                    new CoordinateM(x, y, m)));

        final EnvelopeM envelope = polygon.createEnvelopeM();

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
                     m,
                     envelope.getMinimumM(),
                     0.0);

        assertEquals("createEnvelope failed",
                     m,
                     envelope.getMaximumM(),
                     0.0);

        final EnvelopeM emptyEnvelope = new WkbPolygonM(new LinearRingM()).createEnvelopeM();

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumM()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumM()));
    }

    /**
     * Test createEnvelope()
     */
    @Test
    public void createEnvelope()
    {
        final double x = 1.0;
        final double y = 1.0;
        final double m = 1.0;

        final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(x, y, m),
                                                                    new CoordinateM(x, y, m),
                                                                    new CoordinateM(x, y, m)));

        @SuppressWarnings("CastToConcreteClass")
        final EnvelopeM envelope = (EnvelopeM)polygon.createEnvelope();

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
                     m,
                     envelope.getMinimumM(),
                     0.0);

        assertEquals("createEnvelope failed",
                     m,
                     envelope.getMaximumM(),
                     0.0);

        @SuppressWarnings("CastToConcreteClass")
        final EnvelopeM emptyEnvelope = (EnvelopeM)new WkbPolygonM(new LinearRingM()).createEnvelope();

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumX()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMaximumY()));

        assertTrue("createemptyEnvelope failed",
                   Double.isNaN(emptyEnvelope.getMinimumM()));

        assertTrue("createemptyEnvelope failed",
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
            final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 1.0, 0.0),
                                                                           new CoordinateM(1.0, 1.0, 0.0)),
                                                          new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                           new CoordinateM(0.0, 0.5, 0.0),
                                                                           new CoordinateM(0.5, 0.5, 0.0)));
            polygon.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPolygonM found = WkbPolygonM.readWellKnownBinary(byteBuffer);

            assertEquals("writeWellKnownBinary failed",
                         polygon,
                         found);
        }
    }

    /**
     * Test getGeometryTypeName()
     */
    @Test
    public void getGeometryTypeName()
    {
        assertEquals("getGeometryTypeName() returned the wrong value",
                     GeometryType.Polygon.toString(),
                     new WkbPolygonM(new LinearRingM()).getGeometryTypeName());
    }

    /**
     * Test rings
     */
    @Test
    public void rings()
    {
        final LinearRingM exteriorRing = new LinearRingM(new CoordinateM(1.0, 0.0, 0.0),
                                                         new CoordinateM(0.0, 1.0, 0.0),
                                                         new CoordinateM(1.0, 1.0, 0.0));

        final Collection<LinearRingM> interiorRing = Arrays.asList(new LinearRingM(new CoordinateM(0.5, 0.0, 0.0),
                                                                                   new CoordinateM(0.0, 0.5, 0.0),
                                                                                   new CoordinateM(0.5, 0.5, 0.0)));

        final WkbPolygonM polygon = new WkbPolygonM(exteriorRing, interiorRing);

        assertEquals("getExteriorRing() returned the wrong value",
                     exteriorRing,
                     polygon.getExteriorRing());

        assertEquals("getInteriorRings() returned the wrong value",
                     interiorRing,
                     polygon.getInteriorRings());
    }

    /**
     * Test writeWellKnownBinary with no rings
     */
    @Test
    public void writeWellKnownTextNoRings()
    {
        try(final ByteOutputStream output = new ByteOutputStream())
        {
            final WkbPolygonM polygon = new WkbPolygonM(new LinearRingM());
            polygon.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPolygonM found = WkbPolygonM.readWellKnownBinary(byteBuffer);

            assertEquals("writeWellKnownBinary failed",
                         polygon,
                         found);
        }
    }
}
