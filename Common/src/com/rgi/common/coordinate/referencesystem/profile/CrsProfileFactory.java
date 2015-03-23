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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rgi.common.coordinate.CoordinateReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class CrsProfileFactory
{
    /**
     * @return A {@link Set} of {@link CoordinateReferenceSystem} objects supported by the {@link CrsProfileFactory}
     */
    public static Set<CoordinateReferenceSystem> getSupportedCoordinateReferenceSystems()
    {
        return profileMap.keySet();
    }

    /**
     * Attempts to create a CrsProfile object based on an authority and
     * identifier. A RuntimeException is thrown if the combination of
     * authority and identifier isn't supported.
     *
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG")
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     * @return Returns a {@link CrsProfile} object
     */
    public static CrsProfile create(final String authority,
                                    final int    identifier)
    {
        return CrsProfileFactory.create(new CoordinateReferenceSystem(authority, identifier));
    }

    /**
     * Attempts to create a CrsProfile object based on a coordinate reference
     * system object. A RuntimeException is thrown if coordinate reference
     * system isn't supported.
     *
     * @param coordinateReferenceSystem
     *             A coordinate reference system object
     * @return Returns a {@link CrsProfile} object
     */
    public static CrsProfile create(final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        if(profileMap.containsKey(coordinateReferenceSystem))
        {
            return profileMap.get(coordinateReferenceSystem);
        }

        throw new RuntimeException(String.format("Unsupported spatial reference system: %s", coordinateReferenceSystem));
    }

    /**
     * Global Geodetic Coordinate Reference System (EPSG:4326)
     */
    public static final CoordinateReferenceSystem GlobalGeodetic = new CoordinateReferenceSystem("EPSG", 4326);

    /**
     * Spherical Mercator Coordinate Reference System (EPSG:3857)
     */
    public static final CoordinateReferenceSystem SphericalMercator = new CoordinateReferenceSystem("EPSG", 3857);

    /**
     * Ellipsoidal Mercator Coordinate Reference System (EPSG:3395)
     */
    public static final CoordinateReferenceSystem EllipsoidalMercator = new CoordinateReferenceSystem("EPSG", 3395);

    private static final Map<CoordinateReferenceSystem, CrsProfile> profileMap;

    private static final GlobalGeodeticCrsProfile      GlobalGeodeticCrsProfile      = new GlobalGeodeticCrsProfile();
    private static final SphericalMercatorCrsProfile   SphericalMercatorCrsProfile   = new SphericalMercatorCrsProfile();
    private static final EllipsoidalMercatorCrsProfile EllipsoidalMercatorCrsProfile = new EllipsoidalMercatorCrsProfile();
    //private static final ScaledWorldMercatorCrsProfile ScaledWorldMercatorCrsProfile = new ScaledWorldMercatorCrsProfile();

    static
    {
        profileMap = new HashMap<>();

        profileMap.put(GlobalGeodetic,      GlobalGeodeticCrsProfile);
        profileMap.put(SphericalMercator,   SphericalMercatorCrsProfile);
        profileMap.put(EllipsoidalMercator, EllipsoidalMercatorCrsProfile);

        profileMap.put(new CoordinateReferenceSystem("EPSG",  900913), SphericalMercatorCrsProfile);
        profileMap.put(new CoordinateReferenceSystem("EPSG",    3785), SphericalMercatorCrsProfile);
        profileMap.put(new CoordinateReferenceSystem("OSGEO",   4100), SphericalMercatorCrsProfile);

        // "EPSG:9804": // Mercator (variant A)
        // "EPSG:9805": // Mercator (variant B)
        // new ScaledWorldMercatorCrsProfile();
    }





}
