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

package com.rgi.geopackage.features.geometry.z;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.geometry.xy.Envelope;
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
public class WkbPolygonZ extends WkbCurvePolygonZ
{
    public WkbPolygonZ(final LinearRingZ    exteriorRing,
                       final LinearRingZ... interiorRings)
    {
        this(exteriorRing,
             Arrays.asList(interiorRings));
    }

    public WkbPolygonZ(final LinearRingZ             exteriorRing,
                       final Collection<LinearRingZ> interiorRings)
    {
        if(exteriorRing == null)
        {
            throw new IllegalArgumentException("The exterior ring may not be null");
        }

        this.exteriorRing = exteriorRing;

        this.interiorRings = interiorRings == null ? Collections.emptyList()
                                                   : new ArrayList<>(interiorRings);
    }

    @Override
    public boolean equals(final Object o)
    {
        if(this == o)
        {
            return true;
        }

        if(o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        final WkbPolygonZ other = (WkbPolygonZ)o;

        return this.exteriorRing.equals(other.exteriorRing) &&
               this.interiorRings.equals(other.interiorRings);
    }

    @Override
    public int hashCode()
    {
        int result = this.exteriorRing.hashCode();
        result = 31 * result + this.interiorRings.hashCode();
        return result;
    }

    @Override
    public long getTypeCode()
    {
        return WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Polygon.getCode();
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
    public Envelope createEnvelope()
    {
        return this.exteriorRing.createEnvelope();
    }

    @Override
    public EnvelopeZ createEnvelopeZ()
    {
        return this.exteriorRing.createEnvelope();
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

    public LinearRingZ getExteriorRing()
    {
        return this.exteriorRing;
    }

    public List<LinearRingZ> getInteriorRings()
    {
        return Collections.unmodifiableList(this.interiorRings);
    }

    public static WkbPolygonZ readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryTypeDimensionalityBase + GeometryType.Polygon.getCode());

        final long ringCount = Integer.toUnsignedLong(byteBuffer.getInt());

        if(ringCount == 0)
        {
            return new WkbPolygonZ(new LinearRingZ());    // Empty polygon
        }

        final LinearRingZ exteriorRing = LinearRingZ.readWellKnownBinary(byteBuffer);

        final Collection<LinearRingZ> interiorRings = new LinkedList<>();

        for(long ringIndex = 1; ringIndex < ringCount; ++ringIndex)
        {
            interiorRings.add(LinearRingZ.readWellKnownBinary(byteBuffer));
        }

        return new WkbPolygonZ(exteriorRing, interiorRings);
    }

    private final LinearRingZ       exteriorRing;
    private final List<LinearRingZ> interiorRings;
}
