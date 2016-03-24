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

import com.rgi.geopackage.features.GeometryType;

import java.nio.ByteBuffer;

/**
 * A single location in space. Each point has an X and Y coordinate. A point
 * MAY optionally also have a Z and/or an M value.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbPoint extends WkbGeometry
{
    public WkbPoint(final double x,
                    final double y)
    {
        this(new Coordinate(x, y));
    }

    public WkbPoint(final Coordinate coordinate)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        this.coordinate = coordinate;
    }

    @Override
    public long getTypeCode()
    {
        return GeometryType.Point.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.Point.toString();
    }

    @Override
    public boolean isEmpty()
    {
        return this.coordinate.isEmpty();
    }

    @Override
    public void writeWellKnownBinary(final ByteBuffer byteBuffer)
    {
        writeByteOrder(byteBuffer);
        this.writeTypeCode(byteBuffer);

        this.coordinate.writeWellKnownBinary(byteBuffer);
    }

    @Override
    public Envelope createEnvelope()
    {
        return this.coordinate.createEnvelope();
    }

    public static WkbPoint readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryType.Point.getCode());

        return new WkbPoint(byteBuffer.getDouble(),
                            byteBuffer.getDouble());
    }

    public double getX()
    {
        return this.coordinate.getX();
    }

    public double getY()
    {
        return this.coordinate.getY();
    }

    private final Coordinate coordinate;
}
