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

    public Contents getContents()
    {
        return (Double.isNaN(this.x) &&
                Double.isNaN(this.y) &&
                Double.isNaN(this.m)) ? Contents.Empty
                                      : Contents.NotEmpty;
    }

    public EnvelopeM getEnvelope()
    {
        return new EnvelopeM(this.x,
                             this.x,
                             this.y,
                             this.y,
                             this.m,
                             this.m);
    }

    private final double x;
    private final double y;
    private final double m;
}
