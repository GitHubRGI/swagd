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
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.util.BoundsUtility;

/**
 * Ellipsoidal Mercator implementation of a coordinate reference system profile
 *
 * @author Luke Lambert
 *
 */
public class EllipsoidalMercatorCrsProfile implements CrsProfile
{
    /**
     * Constructor
     */
    public EllipsoidalMercatorCrsProfile()
    {
        this(1.0,
             new CoordinateReferenceSystem("EPSG", 3395));
    }

    /**
     * Constructor used to build slight variants of the Ellipsoidal Mercator projections
     *
     * @param earthEquatorialRadiusScaleFactor
     *             The scale factor for the equatorial radius the earth
     * @param coordinateReferenceSystem
     *             The coordinate reference system that corresponds to this
     *             variant of the Ellipsoidal Mercator projection
     */
    protected EllipsoidalMercatorCrsProfile(final double earthEquatorialRadiusScaleFactor, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        this.coordinateReferenceSystem = coordinateReferenceSystem;

        this.earthEquatorialRadiusScaleFactor = earthEquatorialRadiusScaleFactor;
        this.scaledEarthEquatorialRadius      = UnscaledEarthEquatorialRadius * earthEquatorialRadiusScaleFactor;
        this.earthEquatorialCircumfrence      = 2.0 * Math.PI * this.scaledEarthEquatorialRadius;

        final double scaledEarthPolarRadius = UnscaledEarthPolarRadius * this.earthEquatorialRadiusScaleFactor; // TODO IS THIS RIGHT? Verify!
        //final double earthPolarCircumfrence = 2.0 * Math.PI * scaledEarthPolarRadius;

        this.crsBounds = new BoundingBox(-Math.PI * this.scaledEarthEquatorialRadius,
                                         -Math.PI * scaledEarthPolarRadius,
                                          Math.PI * this.scaledEarthEquatorialRadius,
                                          Math.PI * scaledEarthPolarRadius);
    }

    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate        coordinate,
                                                   final BoundingBox          bounds,
                                                   final TileMatrixDimensions dimensions,
                                                   final TileOrigin           tileOrigin)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Meter coordinate may not be null");
        }

        if(bounds == null)
        {
            throw new IllegalArgumentException("Bounds may not be null");
        }

        if(dimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile profile's coordinate reference system");
        }

        if(!BoundsUtility.contains(this.crsBounds, coordinate, tileOrigin))
        {
            throw new IllegalArgumentException("Coordinate is outside the crsBounds of this coordinate reference system");
        }

        // TODO
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int                  column,
                                             final int                  row,
                                             final BoundingBox          bounds,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin)
    {
        if(bounds == null)
        {
            throw new IllegalArgumentException("Bounds may not be null");
        }

        if(dimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        if(!dimensions.contains(row, column))
        {
            throw new IllegalArgumentException("The row and column must be within the tile matrix dimensions");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        // TODO
        throw new RuntimeException("Method not implemented");

        //final double tileSubdivision = Math.pow(2.0, absoluteTileCoordinate.getZoomLevel());
        //
        //final Double metersX = ((absoluteTileCoordinate.getX() * this.scaledEarthEquatorialRadius) / tileSubdivision) - (this.scaledEarthEquatorialRadius / 2.0);
        //
        //final double metersY = ;
        //
        //return new CrsCoordinate(metersY,
        //                             metersX,
        //                             absoluteTileCoordinate.getZoomLevel(),
        //                             this.getCoordinateReferenceSystem());
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    @Override
    public String getName()
    {
        return "World Mercator";
    }

    @Override
    public String getWellKnownText()
    {
        return "PROJCS[\"WGS 84 / World Mercator\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"central_meridian\",0],PARAMETER[\"scale_factor\",1],PARAMETER[\"false_easting\",0],PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"3395\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";
    }

    @Override
    public String getDescription()
    {
        return "World (Ellipsoidal) Mercator";
    }

    @Override
    public BoundingBox getBounds()
    {
        return this.crsBounds;
    }

    @Override
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate)
    {
        return new Coordinate<>(metersToLon(coordinate.getX()),
                                metersToLat(coordinate.getY()));
    }

    @Override
    public int getPrecision()
    {
        return 2;
    }

    /**
     * Converts a meters X coordinate of WGS 84
     * Ellipsoid World Mercator EPSG(3395) to its
     * corresponding longitude value in degrees.
     *
     * Formula:
     *         Longitude(in radians) = meters/UnscaledEarthEquatorialRadius
     *
     * @param meters
     *             Meters in the in EPSG:3395 coordinate reference system
     * @return longitude in Degrees
     */
    private static double metersToLon(final double meters)
    {
        return Math.toDegrees(meters/UnscaledEarthEquatorialRadius);
    }

    /**
     * Converts a meters Y coordinate to of WGS 84
     * Ellipsoid World Mercator EPSG(3395) to its
     * corresponding latitude value in degrees.
     *
     * This value is found through the Inverse Mapping Conversion
     * function.
     *
     * @param meters
     *             Meters in the in EPSG:3395 coordinate reference system
     * @return latitude in Degrees
     */
    private static double metersToLat(final double meters)
    {
        return Math.toDegrees(inverseMappingConversion(meters));
    }

    /**
     * <b>Inverse Hyperbolic Tangent formula:</b>
     *
     * <pre>
     * atanh = 0.5 * ln[(1 + x) / (1 - x)]
     * </pre>
     *
     * @param x
     *             in degrees or radians
     * @return the corresponding length from the angle x
     */
    private static double inverseHyperbolicTangent(final double x)
    {
        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }

    /**
     * Converts a y in meters to the latitude in radians using the inverse
     * mapping equation
     * <p>
     * <h1><b>Recursion formula:</b></h1><p>
     *         <body><pre>s(1)   = tanh(y/a)</pre>
     *               <pre>s(n+1) = tanh[y/a + e*atanh(e)*s(n)]   for n = 1, 2, 3... </pre></body>
     * <h2><b>Formula for Latitude:</b></h2>
     *         <body><pre>Latitude(in radian) = arcsin(s(n+1))</pre></body>
     *         <p>
     * Where s(n+1) is determined by the conversion factor difference of 0.00000001.
     * The difference is calculated by the following formula:<pre> s(n+1) - s(n)</pre>
     * <p>
     * The difference 0.00000001 was determined as the level of accuracy the formula
     * would need to achieve to be acceptable.
     * <p>
     * atanh is the inverse hyperbolic tangent
     *
     * @param meters for the latitude in WGS 3395
     * @return latitude in radians
     */
    private static double inverseMappingConversion(final double meters)
    {
        // s(1) calculation set to previous
        double previous = Math.tanh(meters/UnscaledEarthEquatorialRadius);
        // Arbitrary initializations of next and difference
        double next = 0;
        double difference = Double.MAX_VALUE;
        final double epsilon = 0.00000001;

        // This will loop until the conversion factor is to the level of
        // accuracy determined by the conversion factor difference
        while(Math.abs(difference) > epsilon)
        {
           // s(n+1) calculated by the recursion formula s(n)
           next = Math.tanh(((meters/UnscaledEarthEquatorialRadius) + (Eccentricity*inverseHyperbolicTangent(Eccentricity*previous))));

           difference = next - previous; // Calculate conversion factor
           previous = next;              // Set s(n) to s(n+1)
        }

        // Latitude = arcsine(s(n+1)) Latitude formula
        final double yRadians = Math.asin(next);

        return yRadians;
    }

    /**
     * Datum's (WGS 84) spheroid's semi-major axis (radius of earth) in meters
     */
    public static final double UnscaledEarthEquatorialRadius = 6378137.0;

    /**
     * Datum's (WGS 84) spheroid's inverse flattening in meters
     */
    public static final double InverseFlattening = 298.257223563;

    /**
     * Flattening in meters
     */
    public static final double Flattening = 1.0/InverseFlattening;

    /**
     * Earth's (unscaled) polar radius.
     *
     * The datum's (WGS 84) spheroid is specified by the equatorial radius (a)
     * and the inverse flattening (1/f).  The polar radius (b) is derived by
     * the relation of the equatorial radius to the inverse flattening:
     * <pre>
     *     a - b
     * f = -----
     *       a
     *
     *         a
     * 1/f = -----
     *       a - b
     *
     * :math:
     *
     *          a
     * b = a - ----
     *         1/f
     * </pre>
     */
    public static final double UnscaledEarthPolarRadius = UnscaledEarthEquatorialRadius - (UnscaledEarthEquatorialRadius/InverseFlattening);

    /**
     * Ellipsoidal eccentricity
     *
     * Defined by it's relationship to the ellipsoidal flattening (f):
     * <pre>
     * e^2 = f(2 - f)
     *
     *     or
     *          ________
     * e = +- \/f(2 - f)
     * </pre>
     *
     * @see <a href="https://en.wikipedia.org/wiki/Flattening#Identities_involving_flattening">Identities involving flattening</a>
     */
    public static final double Eccentricity = Math.sqrt(Flattening * (2 - Flattening));

    /**
     * Used to unify calculations for scaled and unscaled ellipsoidal mercator projections
     */
    private final double earthEquatorialRadiusScaleFactor;

    /**
     * Scaled earth radius.  Use this for all calculations that use the radius of the earth.
     */
    private final double scaledEarthEquatorialRadius;

    /**
     * Earth's equatorial circumference (based on the datum's spheroid's semi-major axis, raidus) in meters
     */
    @SuppressWarnings("unused")
    private final double earthEquatorialCircumfrence;

    private final CoordinateReferenceSystem coordinateReferenceSystem;

    private final BoundingBox crsBounds;
}
