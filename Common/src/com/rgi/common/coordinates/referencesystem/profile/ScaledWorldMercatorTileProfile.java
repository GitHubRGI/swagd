/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.common.coordinates.referencesystem.profile;

/**
 * @author Luke Lambert
 *
 */
//public class ScaledWorldMercatorTileProfile extends EllipsoidalMercatorTileProfile
//{
//
//    public ScaledWorldMercatorTileProfile()
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
