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

/**
 * A planar surface defined by an exterior ring and zero or more interior ring.
 * Each ring is defined by a Curve instance.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
// TODO - this class shouldn't be abstract, but there's some confusion here.
// OGC 06-103r4 (OpenGIS® Implementation Standard for Geographic information
// - Simple feature access - Part 1: Common architecture) lists CurvePolygon
// as for "future use". Wikipedia's article says that CurvePolygons are based
// on CircularString, and the GeoPackage spec says they're based on "Curve". In
// any event, this type is a GeoPackage extension geometry type. It's only
// included because the spec object diagram and Annex E suggests that Polygon
// (a core type) is based on CurvePolygon which is additionally confusing.
public abstract class WkbCurvePolygonM extends WkbSurfaceM
{
//    @Override
//    public int getTypeCode()
//    {
//        return WktGeometryM.GeometryTypeDimensionalityBase + GeometryType.CurvePolygon.getCode();
//    }
//
//    @Override
//    public String getGeometryTypeName()
//    {
//        return GeometryType.CurvePolygon.toString() + "M";
//    }
//
//    @Override
//    public boolean isEmpty()
//    {
//        return false;
//    }
//
//    @Override
//    public void writeWellKnownBinary(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
//    {
//        throw new UnsupportedOperationException("pending implementation");
//    }
}
