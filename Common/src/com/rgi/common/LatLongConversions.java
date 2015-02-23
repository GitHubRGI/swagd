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

package com.rgi.common;

import com.rgi.common.coordinate.Coordinate;

/**
 *
 * @author Jenifer Cochran
 *
 */
public class LatLongConversions
{
    //WGS 84 semi-major axis of the earth ellipsoid
    //These are the EPSG conversions for World Mercator
    private static double semiMajorAxis = 6378137.00;
    //WGS 84 ellipsoid eccentricity of the earth ellipsoid
    private static double eccentricity = 0.08181919084262149;

    /**
     * Convert Latitude and longitude to meters
     * for WGS 84 Ellipsoid World mercator EPSG(3395)
     * @param lon Longitude in Degrees
     * @param lat Latitude in Degrees
     * @return MeterCoordiante
     */
    public static Coordinate<Double> latLongToMeters(final double lon, final double lat)
    {
        //Converting the degrees to radian
        final Double latInRadian = Math.toRadians(lat);
        final Double lonInRadian = Math.toRadians(lon);

        final double xmeter = semiMajorAxis * lonInRadian;
        final double ymeter = semiMajorAxis * atanh(Math.sin(latInRadian)) - semiMajorAxis*eccentricity*atanh(eccentricity*(Math.sin(latInRadian)));
        
        //initialize the meter's coordinate
        return new Coordinate<>(xmeter, ymeter);
    }
    /**
     * Converting meters of WGS 84 Ellipsoid World Mercator
     * EPSG(3395) to latitude and longitude
     * @param metersX x value in meters of WGS 84 Ellipsoid World Mercator
     * @param metersY y value in meters of WGS 84 Ellipsoid World Mercator
     * @return MeterCoordinate
     */
    public static Coordinate<Double> metersToLatLong(final double metersX, final double metersY)
    {
        final double longitudeInRadian = metersX/semiMajorAxis;
        final double latitudeInRadian = inverseMappingConversion(metersY);

        return new Coordinate<>(Math.toDegrees(longitudeInRadian), Math.toDegrees(latitudeInRadian));
    }

    /**
     * Converts a y in meters to the latitude in radians using the inverse
     * mapping equation
     * long = x/a
     * s(1) = tanh(y/a)
     * s(n+1) = tanh[y/a + e*atanh(e)*s(n)]
     * @param metersY s(n+1) with the conversion factor difference of 0.000000001
     * @return
     */
    private static double inverseMappingConversion(final double metersY)
    {
        double previous = Math.tanh(metersY/semiMajorAxis);

        double next = 0;
        double difference = 100000000;

        // This will keep going until the conversion factor is to the 8 significant digits
        while(Math.abs(difference) > 0.00000001)
        {
           //s(n+1)                                     s(n)
           next = Math.tanh(((metersY/semiMajorAxis) + (eccentricity*atanh(eccentricity*previous))));
           //calculate conversion factor
           difference = next - previous;
           //set s(n) to s(n+1)
           previous = next;
        }

        //latitude = arcsine(s(n+1))
        final double yInRadian = Math.asin(next);

        return yInRadian;
    }

    /**
     * Inverse Hyperbolic Tangent equation
     *
     * @param x
     * @return
     */
    static double atanh(final double x)
    {
        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }
}

