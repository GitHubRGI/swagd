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

package com.rgi.geopackage.features.geometry;

import com.rgi.geopackage.features.GeometryType;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A restricted form of MultiCurve where each Curve in the collection must be
 * of type LineString.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WktMultiLineString extends WktMultiCurve<WktLineString>
{
    public WktMultiLineString(final WktLineString... lineStrings)
    {
        this(Arrays.asList(lineStrings));
    }

    public WktMultiLineString(final Collection<WktLineString> lineStrings)
    {
        super(lineStrings);
    }

    @Override
    public long getTypeCode()
    {
        return GeometryType.MultiLineString.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.MultiLineString.toString();
    }

    @Override
    public void writeWellKnownBinary(final ByteBuffer byteBuffer)
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }

    public List<WktLineString> getLineStrings()
    {
        return this.getGeometries();
    }

    public static WktMultiLineString readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryType.MultiLineString.getCode());

        final long lineStringCount = Integer.toUnsignedLong(byteBuffer.getInt());

        final Collection<WktLineString> lineStrings = new LinkedList<>();

        for(long lineStringIndex = 0; lineStringIndex < lineStringCount; ++lineStringIndex)
        {
            lineStrings.add(WktLineString.readWellKnownBinary(byteBuffer));
        }

        return new WktMultiLineString(lineStrings);
    }
}
