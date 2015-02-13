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

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class SphericalMercatorCrsProfile extends ProportionalCrsProfile
{
    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return SphericalMercatorCrsProfile.CoordinateReferenceSystem;
    }

    @Override
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate)
    {
        // TODO algorithm documentation
        return new Coordinate<>(Math.toDegrees(2 * Math.atan(Math.exp(coordinate.getY() / EarthEquatorialRadius)) - Math.PI / 2),
                                Math.toDegrees(coordinate.getX() / EarthEquatorialRadius));
    }

    @Override
    public BoundingBox getBounds()
    {
        return Bounds;
    }

    /**
     * Datum's spheroid's semi-major axis (radius of earth) in meters
     */
    public static final double EarthEquatorialRadius = 6378137.0;

    /**
     * The Bounding Box of Spherical Mercator Coordinate Reference System Profile
     */
    public static final BoundingBox Bounds = new BoundingBox(-Math.PI * EarthEquatorialRadius,
                                                             -Math.PI * EarthEquatorialRadius,
                                                              Math.PI * EarthEquatorialRadius,
                                                              Math.PI * EarthEquatorialRadius);

    /**
     * Earth's equatorial circumference (based on the datum's spheroid's semi-major axis, radius) in meters
     */
    public static final double EarthEquatorialCircumfrence = 2.0 * Math.PI * EarthEquatorialRadius;

    private final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

}
