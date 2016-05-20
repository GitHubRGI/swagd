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

package com.rgi.geopackage.features.geometry.zm;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.GeometryType;
import com.rgi.geopackage.features.geometry.xy.Envelope;

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
public class WkbPolygonZM extends WkbCurvePolygonZM
{
    /**
     * Constructor
     *
     * @param exteriorRing
     *             external hull of the polygon
     * @param interiorRings
     *             'holes' in the polygon
     */
    public WkbPolygonZM(final LinearRingZM    exteriorRing,
                        final LinearRingZM... interiorRings)
    {
        this(exteriorRing,
             Arrays.asList(interiorRings));
    }

    /**
     * Constructor
     *
     * @param exteriorRing
     *             external hull of the polygon
     * @param interiorRings
     *             'holes' in the polygon
     */
    public WkbPolygonZM(final LinearRingZM             exteriorRing,
                        final Collection<LinearRingZM> interiorRings)
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
    public boolean equals(final Object obj)
    {
        if(this == obj)
        {
            return true;
        }

        if(obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        final WkbPolygonZM other = (WkbPolygonZM) obj;

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
        return GeometryTypeDimensionalityBase + GeometryType.Polygon.getCode();
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
    public EnvelopeZM createEnvelopeZM()
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

    public LinearRingZM getExteriorRing()
    {
        return this.exteriorRing;
    }

    public List<LinearRingZM> getInteriorRings()
    {
        return Collections.unmodifiableList(this.interiorRings);
    }

    /**
     * Assumes the ByteOutputStream's byte order has been properly set
     *
     * @param byteBuffer
     *             buffer to be read from
     * @return a new WkbPolygonZM
     */
    public static WkbPolygonZM readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryTypeDimensionalityBase + GeometryType.Polygon.getCode());

        final long ringCount = Integer.toUnsignedLong(byteBuffer.getInt());

        if(ringCount == 0)
        {
            return new WkbPolygonZM(new LinearRingZM());    // Empty polygon
        }

        final LinearRingZM exteriorRing = LinearRingZM.readWellKnownBinary(byteBuffer);

        final Collection<LinearRingZM> interiorRings = new LinkedList<>();

        for(long ringIndex = 1; ringIndex < ringCount; ++ringIndex)
        {
            interiorRings.add(LinearRingZM.readWellKnownBinary(byteBuffer));
        }

        return new WkbPolygonZM(exteriorRing, interiorRings);
    }

    private final LinearRingZM       exteriorRing;
    private final List<LinearRingZM> interiorRings;
}
