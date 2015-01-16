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

package com.rgi.common.tile.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 * Methods based on the previous Python implementation in tiles2gpkg_parallel.py
 *
 */
public class SphericalMercatorTileProfile implements TileProfile
{
    @Override
    public AbsoluteTileCoordinate crsToAbsoluteTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel, final TileOrigin origin)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Meter coordinate may not be null");
        }

        if(zoomLevel < 0 || zoomLevel > 31)
        {
            throw new IllegalArgumentException("Zoom level must be in the range [0, 32)");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        if(coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile profile's coordinate reference system");
        }

        final double tileSubdivision = Math.pow(2.0, zoomLevel);

        // Round off the fractional tile
        return new AbsoluteTileCoordinate((int)((coordinate.getY() + EarthEquatorialCircumfrence/2.0) / EarthEquatorialCircumfrence * tileSubdivision),
                                          (int)((coordinate.getX() + EarthEquatorialCircumfrence/2.0) / EarthEquatorialCircumfrence * tileSubdivision),
                                          zoomLevel,
                                          TileOrigin.LowerLeft).transform(origin);
    }

    @Override
    public CrsCoordinate absoluteToCrsCoordinate(final AbsoluteTileCoordinate absoluteTileCoordinate)
    {
        if(absoluteTileCoordinate == null)
        {
            throw new IllegalArgumentException("Tile coordinate may not be null");
        }

        final double tileSubdivision = Math.pow(2.0, absoluteTileCoordinate.getZoomLevel());

        final AbsoluteTileCoordinate transformed = absoluteTileCoordinate.transform(TileOrigin.LowerLeft);

        return new CrsCoordinate(((transformed.getY() * EarthEquatorialCircumfrence) / tileSubdivision) - (EarthEquatorialCircumfrence / 2.0),
                                 ((transformed.getX() * EarthEquatorialCircumfrence) / tileSubdivision) - (EarthEquatorialCircumfrence / 2.0),
                                 this.getCoordinateReferenceSystem());
    }

    @Override
    public Dimension2D getTileDimensions(final int zoomLevel)
    {
        final double dimension = EarthEquatorialCircumfrence / Math.pow(2.0, zoomLevel);

        return new Dimension2D(dimension, dimension);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return SphericalMercatorTileProfile.CoordinateReferenceSystem;
    }

    public static Coordinate<Double> metersToGeographic(final Coordinate<Double> coordinate)
	{
		return new Coordinate<>(metersToLat(coordinate.getY()),
		                        metersToLon(coordinate.getX()));
	}

	private static double metersToLon(final Double meters)
	{
		// TODO use Math.toRadians()/Math.toDegrees()
	    return (meters / (Math.PI * EarthEquatorialRadius)) * 180.0;
	}

	private static double metersToLat(final Double meters)
	{
		// TODO use Math.toRadians()/Math.toDegrees()
	    return Math.toDegrees(2 * Math.atan(Math.exp(metersToLon(meters) * Math.PI / 180.0)) - Math.PI / 2);
	}

	@Override
	public BoundingBox getBounds()
    {
	    return new BoundingBox(-Math.PI * EarthEquatorialRadius,
	                           -Math.PI * EarthEquatorialRadius,
	                            Math.PI * EarthEquatorialRadius,
	                            Math.PI * EarthEquatorialRadius);
    }

    /**
     * Datum's spheroid's semi-major axis (raidus of earth) in meters
     */
    public static final double EarthEquatorialRadius = 6378137.0;

    /**
     * Earth's equatorial circumfrence (based on the datum's spheroid's semi-major axis, raidus) in meters
     */
    public static final double EarthEquatorialCircumfrence = 2.0 * Math.PI * EarthEquatorialRadius;

    public final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);
}
