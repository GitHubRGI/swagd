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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile profile's coordinate reference system");
        }

        final double tileSubdivision = Math.pow(2.0, zoomLevel);

        final double tileWidth  = EarthEquatorialCircumfrence / tileSubdivision;
        final double tileHeight = EarthEquatorialCircumfrence / tileSubdivision;

        double   verticalOriginShift =  origin.getDeltaY() * (EarthEquatorialCircumfrence / 2.0);
        double horizontalOriginShift = -origin.getDeltaX() * (EarthEquatorialCircumfrence / 2.0);

        return new AbsoluteTileCoordinate((int)((coordinate.getY() -   verticalOriginShift) * tileHeight),
                                          (int)((coordinate.getX() - horizontalOriginShift) * tileWidth),
                                          zoomLevel,
                                          origin);
    }

    @Override
    public CrsCoordinate absoluteToCrsCoordinate(final AbsoluteTileCoordinate coordinate)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Tile coordinate may not be null");
        }

        final int tileRows    = (int)Math.pow(2.0, coordinate.getZoomLevel());
        final int tileColumns = (int)Math.pow(2.0, coordinate.getZoomLevel());

        final double tileHeight = EarthEquatorialCircumfrence / tileRows;
        final double tileWidth  = EarthEquatorialCircumfrence / tileColumns;

        final double originShift = (EarthEquatorialCircumfrence / 2.0);

        final int maxTileOrdinate = tileRows    - 1;
        final int maxTileAbscissa = tileColumns - 1;

        // If the the origin is the same along an axis the xor value will be 0 which cancels out the final (delta) term.
        final int tileY = coordinate.getY() + (coordinate.getOrigin().getDeltaY() ==  1 ? maxTileOrdinate - 2*coordinate.getY() : 0);
        final int tileX = coordinate.getX() + (coordinate.getOrigin().getDeltaX() == -1 ? maxTileAbscissa - 2*coordinate.getX() : 0);

        return new CrsCoordinate((tileY * tileHeight) - originShift,
                                 (tileX * tileWidth)  - originShift,
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

    @Override
    public Coordinate<Double> toGlobalGeodetic(Coordinate<Double> coordinate)
    {
        // TODO algorithm documentation
        return new Coordinate<>(Math.toDegrees(2 * Math.atan(Math.exp(coordinate.getY() / EarthEquatorialRadius)) - Math.PI / 2),
                                Math.toDegrees(coordinate.getX() / EarthEquatorialRadius));
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
     * Datum's spheroid's semi-major axis (radius of earth) in meters
     */
    public static final double EarthEquatorialRadius = 6378137.0;

    /**
     * Earth's equatorial circumference (based on the datum's spheroid's semi-major axis, radius) in meters
     */
    public static final double EarthEquatorialCircumfrence = 2.0 * Math.PI * EarthEquatorialRadius;

    private final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);
}
