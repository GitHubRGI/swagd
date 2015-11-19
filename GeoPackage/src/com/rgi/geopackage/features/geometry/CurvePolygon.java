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

import com.rgi.geopackage.features.BinaryHeader;
import com.rgi.geopackage.features.GeometryType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A planar surface defined by an exterior ring and zero or more interior ring.
 * Each ring is defined by a Curve instance.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class CurvePolygon extends Surface
{
    public CurvePolygon(final BinaryHeader header)
    {
        super(header);
    }

    @Override
    public int getTypeCode()
    {
        return GeometryType.CurvePolygon.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.CurvePolygon.toString();
    }

    @Override
    protected void writeWkbGeometry(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }
}
