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
