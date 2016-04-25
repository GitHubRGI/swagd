/* The MIT License (MIT)
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A restricted form of CurvePolygon where each ring is defined as a simple,
 * closed LineString.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbPolygon extends WkbCurvePolygon
{
    public WkbPolygon(final LinearRing    exteriorRing,
                      final LinearRing... interiorRings)
    {
        this(exteriorRing,
             Arrays.asList(interiorRings));
    }

    public WkbPolygon(final LinearRing             exteriorRing,
                      final Collection<LinearRing> interiorRings)
    {
        if(exteriorRing == null)
        {
            throw new IllegalArgumentException("The exterior ring may not be null");
        }

        this.exteriorRing = exteriorRing;

        this.interiorRings = interiorRings == null ||
                             interiorRings.isEmpty() ? Collections.emptyList()
                                                     : new ArrayList<>(interiorRings);
    }

    @Override
    public long getTypeCode()
    {
        return GeometryType.Polygon.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.Polygon.toString();
    }

    @Override
    public boolean isEmpty()
    {
        return this.exteriorRing.isEmpty();
    }

    @Override
    public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
    {
        this.writeWellKnownBinaryHeader(byteOutputStream); // Checks byteOutputStream for null

        final int ringCount = this.interiorRings.size() + (this.exteriorRing.isEmpty() ? 0 : 1);

        byteOutputStream.write(ringCount);

        if(ringCount > 0)
        {
            this.exteriorRing.writeWellKnownBinary(byteOutputStream);

            this.interiorRings.forEach(linearRing -> linearRing.writeWellKnownBinary(byteOutputStream));
        }
    }

    @Override
    public Envelope createEnvelope()
    {
        return this.exteriorRing.createEnvelope();
    }

    public LinearRing getExteriorRing()
    {
        return this.exteriorRing;
    }

    public List<LinearRing> getInteriorRings()
    {
        return Collections.unmodifiableList(this.interiorRings);
    }

    public static WkbPolygon readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryType.Polygon.getCode());

        final long ringCount = Integer.toUnsignedLong(byteBuffer.getInt());

        if(ringCount == 0)
        {
            return new WkbPolygon(new LinearRing());    // Empty polygon
        }

        final LinearRing exteriorRing = LinearRing.readWellKnownBinary(byteBuffer);

        final Collection<LinearRing> interiorRings = new LinkedList<>();

        for(long ringIndex = 1; ringIndex < ringCount; ++ringIndex)
        {
            interiorRings.add(LinearRing.readWellKnownBinary(byteBuffer));
        }

        return new WkbPolygon(exteriorRing, interiorRings);
    }

    private final LinearRing       exteriorRing;
    private final List<LinearRing> interiorRings;
}
