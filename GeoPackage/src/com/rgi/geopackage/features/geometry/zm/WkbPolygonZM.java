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
    public WkbPolygonZM(final LinearRingZM    exteriorRing,
                        final LinearRingZM... interiorRings)
    {
        this(exteriorRing,
             Arrays.asList(interiorRings));
    }

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
    public long getTypeCode()
    {
        return GeometryTypeDimensionalityBase + GeometryType.Polygon.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.Polygon + "ZM";
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
    public void writeWellKnownBinary(final ByteBuffer byteBuffer)
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }

    public LinearRingZM getExteriorRing()
    {
        return this.exteriorRing;
    }

    public List<LinearRingZM> getInteriorRings()
    {
        return Collections.unmodifiableList(this.interiorRings);
    }

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
