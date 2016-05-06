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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class GeometryTypeTest
{
    /**
     * Verify enum values
     */
    @Test
    public void verifyValues()
    {
        assertTrue("Geometry           has incorrect values", GeometryType.Geometry          .getCode() ==  0 && !GeometryType.Geometry          .isExtension());
        assertTrue("Point              has incorrect values", GeometryType.Point             .getCode() ==  1 && !GeometryType.Point             .isExtension());
        assertTrue("LineString         has incorrect values", GeometryType.LineString        .getCode() ==  2 && !GeometryType.LineString        .isExtension());
        assertTrue("Polygon            has incorrect values", GeometryType.Polygon           .getCode() ==  3 && !GeometryType.Polygon           .isExtension());
        assertTrue("MultiPoint         has incorrect values", GeometryType.MultiPoint        .getCode() ==  4 && !GeometryType.MultiPoint        .isExtension());
        assertTrue("MultiLineString    has incorrect values", GeometryType.MultiLineString   .getCode() ==  5 && !GeometryType.MultiLineString   .isExtension());
        assertTrue("MultiPolygon       has incorrect values", GeometryType.MultiPolygon      .getCode() ==  6 && !GeometryType.MultiPolygon      .isExtension());
        assertTrue("GeometryCollection has incorrect values", GeometryType.GeometryCollection.getCode() ==  7 && !GeometryType.GeometryCollection.isExtension());
        assertTrue("CircularString     has incorrect values", GeometryType.CircularString    .getCode() ==  8 &&  GeometryType.CircularString    .isExtension());
        assertTrue("CompoundCurve      has incorrect values", GeometryType.CompoundCurve     .getCode() ==  9 &&  GeometryType.CompoundCurve     .isExtension());
        assertTrue("CurvePolygon       has incorrect values", GeometryType.CurvePolygon      .getCode() == 10 &&  GeometryType.CurvePolygon      .isExtension());
        assertTrue("MultiCurve         has incorrect values", GeometryType.MultiCurve        .getCode() == 11 &&  GeometryType.MultiCurve        .isExtension());
        assertTrue("MultiSurface       has incorrect values", GeometryType.MultiSurface      .getCode() == 12 &&  GeometryType.MultiSurface      .isExtension());
        assertTrue("Curve              has incorrect values", GeometryType.Curve             .getCode() == 13 &&  GeometryType.Curve             .isExtension());
        assertTrue("Surface            has incorrect values", GeometryType.Surface           .getCode() == 14 &&  GeometryType.Surface           .isExtension());
    }
}
