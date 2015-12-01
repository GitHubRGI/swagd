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

package com.rgi.geopackage.features;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A single location in space. Each point has an X and Y coordinate. A point
 * MAY optionally also have a Z and/or an M value.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class Point extends Geometry
{
    protected Point(final BinaryHeader header,
                    final Coordinate   coordinate)
    {
        super(header);

        if(this.header.isEmpty())
        {
            if(!Double.isNaN(coordinate.getX()) ||
               !Double.isNaN(coordinate.getY()) ||
               (coordinate.getZ() != null && !Double.isNaN(coordinate.getZ())) ||
               (coordinate.getM() != null && !Double.isNaN(coordinate.getM())))
            {
                throw new IllegalArgumentException("Empty points may not have coordinate values other than NaN");
            }
        }

        this.coordinate = coordinate;
    }

    @Override
    public int getTypeCode()
    {
        return GeometryType.Point.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.Point.toString();
    }

    @Override
    public boolean hasZ()
    {
        return this.coordinate.hasZ();
    }

    @Override
    public boolean hasM()
    {
        return this.coordinate.hasM();
    }

    @Override
    public void writeWkbGeometry(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }

    public double getX()
    {
        return this.coordinate.getX();
    }

    public double getY()
    {
        return this.coordinate.getX();
    }

    public double getZ()
    {
        return this.coordinate.getX();
    }

    public double getM()
    {
        return this.coordinate.getX();
    }

    private final Coordinate coordinate;
}
