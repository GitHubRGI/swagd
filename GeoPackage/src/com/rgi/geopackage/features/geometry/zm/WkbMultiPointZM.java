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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A restricted form of GeometryCollection where each Geometry in the
 * collection must be of type Point.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbMultiPointZM extends WkbGeometryCollectionZM<WkbPointZM>
{
    public WkbMultiPointZM(final WkbPointZM... points)
    {
        this(Arrays.asList(points));
    }

    public WkbMultiPointZM(final Collection<WkbPointZM> points)
    {
        super(points);
    }

    @Override
    public long getTypeCode()
    {
        return GeometryTypeDimensionalityBase + GeometryType.MultiPoint.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.MultiPoint + "ZM";
    }

    @Override
    public void writeWellKnownBinary(final ByteBuffer buffer)
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }

    public List<WkbPointZM> getPoints()
    {
        return this.getGeometries();
    }

    public static WkbMultiPointZM readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryTypeDimensionalityBase + GeometryType.MultiPoint.getCode());

        final long pointCount = Integer.toUnsignedLong(byteBuffer.getInt());

        final Collection<WkbPointZM> points = new LinkedList<>();

        for(long pointIndex = 0; pointIndex < pointCount; ++pointIndex)
        {
            points.add(WkbPointZM.readWellKnownBinary(byteBuffer));
        }

        return new WkbMultiPointZM(points);
    }
}
