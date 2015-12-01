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

import com.rgi.geopackage.features.Contents;

/**
 * Proxy for member coordinates in GeoPackage geometries
 *
 * @author Luke Lambert
 */
public class Coordinate
{
    public Coordinate(final double x,
                      final double y,
                      final Double z,
                      final Double m)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.m = m;
    }

    @Override
    public String toString()
    {
        return String.format("(%f, %f, %s, %s)",
                             this.x,
                             this.y,
                             this.z == null ? "<null>" : this.z.toString(),
                             this.m == null ? "<null>" : this.m.toString());
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public Double getZ()
    {
        return this.z;
    }

    public Double getM()
    {
        return this.m;
    }

    public boolean hasZ()
    {
        return this.z != null;
    }

    public boolean hasM()
    {
        return this.m != null;
    }

    public Contents getContents()
    {
        if(Double.isNaN(this.x) &&
           Double.isNaN(this.y) &&
           (this.hasZ() && Double.isNaN(this.z)) &&
           (this.hasM() && Double.isNaN(this.m)))
        {
            return Contents.Empty;
        }

        return Contents.NotEmpty;
    }

    private final double x;
    private final double y;
    private final Double z;
    private final Double m;
}
