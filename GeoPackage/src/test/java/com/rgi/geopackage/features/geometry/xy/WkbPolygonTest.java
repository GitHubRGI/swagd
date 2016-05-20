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
public class WkbPolygonTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                      new Coordinate(0.0, 1.0),
                                      new Coordinate(1.0, 1.0)),
                       new LinearRing(new Coordinate(0.5, 0.0),
                                      new Coordinate(0.0, 0.5),
                                      new Coordinate(0.5, 0.5)));
    }

    /**
     * Test the coordinate constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void coordinateConstructor()
    {
        new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                      new Coordinate(1.0, 1.0),
                                      new Coordinate(0.0, 1.0)),
                       Arrays.asList(new LinearRing(new Coordinate(0.5, 0.0),
                                                    new Coordinate(0.0, 0.5),
                                                    new Coordinate(0.5, 0.5))));
    }

    /**
     * Test the collection constructor for failure on a null coordinate
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullCollection()
    {
        new WkbPolygon(null);
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
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

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
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

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
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

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
        final WkbPolygon polygon1 = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                  new Coordinate(0.0, 1.0),
                                                                  new Coordinate(1.0, 1.0)),
                                                   new LinearRing(new Coordinate(0.5, 0.0),
                                                                  new Coordinate(0.0, 0.5),
                                                                  new Coordinate(0.5, 0.5)));
        final WkbPolygon polygon2 = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                  new Coordinate(0.0, 1.0),
                                                                  new Coordinate(1.0, 1.0)),
                                                   new LinearRing(new Coordinate(0.5, 0.0),
                                                                  new Coordinate(0.0, 0.5),
                                                                  new Coordinate(0.5, 0.5)));

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
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

        assertEquals("Hash code failed",
                     2049870847,
                     polygon.hashCode());
    }

    /**
     * Test isEmpty()
     */
    @Test
    public void testIsEmpty()
    {
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

        assertFalse("isEmpty failed",
                    polygon.isEmpty());

        final WkbPolygon emptyPolygon = new WkbPolygon(new LinearRing());

        assertTrue("isEmpty failed",
                   emptyPolygon.isEmpty());
    }

    /**
     * Test getContents()
     */
    @Test
    public void getContents()
    {
        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                 new Coordinate(0.0, 1.0),
                                                                 new Coordinate(1.0, 1.0)),
                                                  new LinearRing(new Coordinate(0.5, 0.0),
                                                                 new Coordinate(0.0, 0.5),
                                                                 new Coordinate(0.5, 0.5)));

        assertSame("getContents failed",
                   Contents.NotEmpty,
                   polygon.getContents());

        final WkbPolygon emptyPolygon = new WkbPolygon(new LinearRing());

        assertSame("getContents failed",
                   Contents.Empty,
                   emptyPolygon.getContents());
    }

    /**
     * Test createEnvelope()
     */
    @Test
    public void createEnvelope()
    {
        final double x = 1.0;
        final double y = 1.0;

        final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(x, y),
                                                                 new Coordinate(x, y),
                                                                 new Coordinate(x, y)));

        final Envelope envelope = polygon.createEnvelope();

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

        final Envelope emptyEnvelope = new WkbPolygon(new LinearRing()).createEnvelope();

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
            final WkbPolygon polygon = new WkbPolygon(new LinearRing(new Coordinate(1.0, 0.0),
                                                                     new Coordinate(0.0, 1.0),
                                                                     new Coordinate(1.0, 1.0)),
                                                      new LinearRing(new Coordinate(0.5, 0.0),
                                                                     new Coordinate(0.0, 0.5),
                                                                     new Coordinate(0.5, 0.5)));
            polygon.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPolygon found = WkbPolygon.readWellKnownBinary(byteBuffer);

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
                     new WkbPolygon(new LinearRing()).getGeometryTypeName());
    }

    /**
     * Test rings
     */
    @Test
    public void rings()
    {
        final LinearRing exteriorRing = new LinearRing(new Coordinate(1.0, 0.0),
                                                       new Coordinate(0.0, 1.0),
                                                       new Coordinate(1.0, 1.0));

        final Collection<LinearRing> interiorRing = Arrays.asList(new LinearRing(new Coordinate(0.5, 0.0),
                                                                                 new Coordinate(0.0, 0.5),
                                                                                 new Coordinate(0.5, 0.5)));

        final WkbPolygon polygon = new WkbPolygon(exteriorRing, interiorRing);

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
            final WkbPolygon polygon = new WkbPolygon(new LinearRing());
            polygon.writeWellKnownBinary(output);

            final ByteBuffer byteBuffer = ByteBuffer.wrap(output.array());

            final WkbPolygon found = WkbPolygon.readWellKnownBinary(byteBuffer);

            assertEquals("writeWellKnownBinary failed",
                         polygon,
                         found);
        }
    }
}
