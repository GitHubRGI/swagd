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
package com.rgi.common.coordinate.referencesystem.profile;

/**
 * Parameterized variants of the Ellipsoidal Mercator projections
 *
 * @author Luke Lambert
 *
 */
//public class ScaledWorldMercatorCrsProfile extends EllipsoidalMercatorCrsProfile
//{
//
//    public ScaledWorldMercatorCrsProfile()
//    {
//        super(EarthEquatorialRadiusScaleFactor);
//    }
//
//    public static final double EarthEquatorialRadiusScaleFactor = 0.8037989097479776; // Scaled World Mercator - Design 3 webMerc_memo#21_tile_pyramid_design_continued.doc.
//
//
//    // webMerc_memo#21_tile_pyramid_design_continued.doc
//
//    // Design 0
//    // *   Latitude of true scale is the Equator (0 degrees N, exactly).
//    // *   The circumference of the Equator is 2*PI*a , where a = 6378137 meters (exactly).  This works out to 40,075,016.6855785 meters.
//    // *   Scale reduction factor at the Equator is 1.000 (exactly), i.e. no reduction.
//    // *   Compliant with EPSG::9804 (Mercator Variant A) using above scale reduction factor
//    // *   Compliant with EPSG::9805 (Mercator Variant B) using above latitude of true scale
//    // *   Compliant with EPSG::3395 (except for latitude extent).
//    // *   Level-0 tile is 180 degreesW to 180 degreesE (exactly) and between +-85.0840590501104 in latitude.
//
//    // Design 3
//    // * Latitude of true scale is chosen such that its parallel circle has circumference 3*2^30 centimeters, which works out to 32,212,254.72 meters (exactly)
//    // * Latitude of true scale is approximately 36.59764049041025 degrees
//    // * Scale reduction factor at the Equator is approximately 0.8037989097479776
//    // * Compliant with EPSG::9804 (Mercator Variant A) using above scale reduction factor
//    // * Compliant with EPSG::9805 (Mercator Variant A) using above latitude of true scale
//    // * Level-0 tile is 180 degreesW to 180 degreesE (exactly) and between +-85.0840590501104 degrees in latitude.
//    //
//    // Design 4
//    // * Latitude of true scale is chosen such that its parallel circle has circumference 2^35 mm exactly which works out to 34,359,738.368 meters.
//    // * Latitude of true scale is +-31.0606963703645 degrees
//    // * Scale reduction factor at the Equator is 0.857385503731176
//    // * Compliant with EPSG::9804 (Mercator Variant A) using above scale reduction factor
//    // * Compliant with EPSG::9805 (Mercator Variant B) using above latitude of true scale
//    // * Level-0 tile is 180 degreesW to 180 degreesE (exactly) and between +-85.0840590501104 degrees in latitude.
//}
