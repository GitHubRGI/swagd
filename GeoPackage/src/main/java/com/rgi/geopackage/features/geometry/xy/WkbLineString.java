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
import java.util.Collection;

/**
 * A Curve that connects two or more points in space.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbLineString extends WkbCurve
{
    public WkbLineString(final Coordinate... points)
    {
        this(new LinearRing(points));
    }

    public WkbLineString(final Collection<Coordinate> points)
    {
        this(new LinearRing(points));
    }

    private WkbLineString(final LinearRing linearString)
    {
        this.linearString = linearString;
    }

    @Override
    public long getTypeCode()
    {
        return GeometryType.LineString.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.LineString.toString();
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
    public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
    {
        this.writeWellKnownBinaryHeader(byteOutputStream); // Checks byteOutputStream for null
        this.linearString.writeWellKnownBinary(byteOutputStream);
    }


    public static WkbLineString readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryType.LineString.getCode());

        return new WkbLineString(LinearRing.readWellKnownBinary(byteBuffer));
    }

    private final LinearRing linearString;
}
