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
 * @see "http://www.geopackage.org/spec/#geometry_types"
 *
 * @author Luke Lambert
 *
 */
public enum GeometryType
{
    // http://www.geopackage.org/spec/#geometry_types_core

    Geometry          ( 0, false),
    Point             ( 1, false),
    LineString        ( 2, false),
    Polygon           ( 3, false),
    MultiPoint        ( 4, false),
    MultiLineString   ( 5, false),
    MultiPolygon      ( 6, false),
    GeometryCollection( 7, false),

    // http://www.geopackage.org/spec/#geometry_types_extension

    CircularString    ( 8, true),
    CompoundCurve     ( 9, true),
    CurvePolygon      (10, true),
    MultiCurve        (11, true),
    MultiSurface      (12, true),
    Curve             (13, true),
    Surface           (14, true);

    GeometryType(final int code, final boolean extension)
    {
        this.code      = code;
        this.extension = extension;
    }

    /**
     * @return the code
     */
    public long getCode()
    {
        return this.code;
    }

    /**
     * @return the extension
     */
    public boolean isExtension()
    {
        return this.extension;
    }

    @Override
    public String toString()
    {
        return this.name().toUpperCase();
    }

    private final long    code;
    private final boolean extension;
}
