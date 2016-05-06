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

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.Contents;

/**
 * Proxy for member coordinates in GeoPackage geometries
 *
 * @author Luke Lambert
 */
public class CoordinateM
{
    public CoordinateM(final double x,
                       final double y,
                       final double m)
    {
        this.x = x;
        this.y = y;
        this.m = m;
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

        final CoordinateM other = (CoordinateM)o;

        return Double.compare(other.x, this.x) == 0 &&
               Double.compare(other.y, this.y) == 0 &&
               Double.compare(other.m, this.m) == 0;
    }

    @Override
    public int hashCode()
    {
        final long longBitsX = Double.doubleToLongBits(this.x);
        int result = (int) (longBitsX ^ (longBitsX >>> 32));
        final long longBitsY = Double.doubleToLongBits(this.y);
        result = 31 * result + (int) (longBitsY ^ (longBitsY >>> 32));
        final long longBitsM = Double.doubleToLongBits(this.m);
        result = 31 * result + (int) (longBitsM ^ (longBitsM >>> 32));

        return result;
    }

    @Override
    public String toString()
    {
        return String.format("(%f, %f, %fm)",
                             this.x,
                             this.y,
                             this.m);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public Double getM()
    {
        return this.m;
    }

    public boolean isEmpty()
    {
        return Double.isNaN(this.x) &&
               Double.isNaN(this.y) &&
               Double.isNaN(this.m);
    }

    public Contents getContents()
    {
        return (Double.isNaN(this.x) &&
                Double.isNaN(this.y) &&
                Double.isNaN(this.m)) ? Contents.Empty
                                      : Contents.NotEmpty;
    }

    public EnvelopeM createEnvelope()
    {
        return new EnvelopeM(this.x,
                             this.y, this.m, this.x,
                             this.y,
                             this.m);
    }

    public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
    {
        byteOutputStream.write(this.x);
        byteOutputStream.write(this.y);
        byteOutputStream.write(this.m);
    }

    private final double x;
    private final double y;
    private final double m;
}
