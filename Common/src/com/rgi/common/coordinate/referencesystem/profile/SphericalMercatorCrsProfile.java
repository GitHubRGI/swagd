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
        return new Coordinate<>(Math.toDegrees(coordinate.getX() / EarthEquatorialRadius),
                                Math.toDegrees(2 * Math.atan(Math.exp(coordinate.getY() / EarthEquatorialRadius)) - Math.PI / 2));
    }

    @Override
    public BoundingBox getBounds()
    {
        return Bounds;
    }

    @Override
    public String getName()
    {
        return "Web Mercator";
    }

    @Override
    public String getWellKnownText()
    {
        return "PROJCS[\"WGS 84 / Pseudo-Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"X\",EAST],AXIS[\"Y\",NORTH],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"],AUTHORITY[\"EPSG\",\"3857\"]]";
    }

    @Override
    public String getDescription()
    {
        return "Projection used in many popular web mapping applications (Google/Bing/OpenStreetMap/etc). Sometimes known as EPSG:900913.";
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

    @Override
    public int getPrecision()
    {
        return 2;
    }
}
