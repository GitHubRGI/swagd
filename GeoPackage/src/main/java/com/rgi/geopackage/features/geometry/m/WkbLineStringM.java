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

package com.rgi.geopackage.features.geometry.m;

import com.rgi.geopackage.features.GeometryType;
import com.rgi.geopackage.features.geometry.xy.Envelope;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * A Curve that connects two or more points in space.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbLineStringM extends WkbCurveM
{
    public WkbLineStringM(final CoordinateM... points)
    {
        this(new LinearRingM(points));
    }

    public WkbLineStringM(final Collection<CoordinateM> points)
    {
        this(new LinearRingM(points));
    }

    private WkbLineStringM(final LinearRingM linearString)
    {
        this.linearString = linearString;
    }

    @Override
    public long getTypeCode()
    {
        return GeometryTypeDimensionalityBase + GeometryType.LineString.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.LineString + "M";
    }

    @Override
    public boolean isEmpty()
    {
        return this.linearString.isEmpty();
    }

    @Override
    public Envelope createEnvelope()
    {
        return this.linearString.createEnvelope();
    }

    @Override
    public EnvelopeM createEnvelopeM()
    {
        return this.linearString.createEnvelope();
    }

    @Override
    public void writeWellKnownBinary(final ByteBuffer byteBuffer)
    {
        this.writeWellKnownBinaryHeader(byteBuffer); // Checks byteBuffer for null
        this.linearString.writeWellKnownBinary(byteBuffer);
    }

    public static WkbLineStringM readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryTypeDimensionalityBase + GeometryType.LineString.getCode());

        return new WkbLineStringM(LinearRingM.readWellKnownBinary(byteBuffer));
    }

    private final LinearRingM linearString;
}