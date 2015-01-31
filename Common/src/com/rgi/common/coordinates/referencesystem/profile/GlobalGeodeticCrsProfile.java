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

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Luke Lambert
 *
 */
public class GlobalGeodeticCrsProfile implements CrsProfile
{
    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate        coordinate,
                                                   final TileMatrixDimensions dimensions,
                                                   final TileOrigin           tileOrigin)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Meter coordinate may not be null");
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

        // TODO tile origin transform from TileOrigin.LowerLeft?

        return new Coordinate<>((int)((coordinate.getY() + Bounds.getHeight()/2.0) / Bounds.getHeight() * dimensions.getHeight()),
                                (int)((coordinate.getX() + Bounds.getWidth() /2.0) / Bounds.getWidth()  * dimensions.getWidth()));
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int                  row,
                                             final int                  column,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin)
    {
        if(row < 0)
        {
            throw new IllegalArgumentException("Row must be greater than 0");
        }

        if(column < 0)
        {
            throw new IllegalArgumentException("Column must be greater than 0");
        }

        if(dimensions == null)
        {
            throw new IllegalArgumentException("Tile matrix dimensions may not be null");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        return new CrsCoordinate((   row * Bounds.getHeight() / dimensions.getHeight()) - Bounds.getHeight() / 2.0,
                                 (column * Bounds.getWidth()  / dimensions.getHeight()) - Bounds.getWidth()  / 2.0,
                                 this.getCoordinateReferenceSystem());
    }

    @Override
    public Dimensions getTileDimensions(final TileMatrixDimensions dimensions)
    {
        final double height = GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight();
        final double width  = GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth();

        return new Dimensions(height, width);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return GlobalGeodeticCrsProfile.CoordinateReferenceSystem;
    }

    public static Coordinate<Double> coordinateToGeographic(final Coordinate<Double> coordinate)
    {
        return coordinate;
    }

    @Override
    public BoundingBox getBounds()
    {
        return GlobalGeodeticCrsProfile.Bounds;
    }

    @Override
    public Coordinate<Double> toGlobalGeodetic(Coordinate<Double> coordinate)
    {
        return coordinate;
    }

    private final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);

    /**
     * The Unprojected bounds of this tile profile, in degrees
     *
     * TODO: what defines the bounds?  The information doesn't seem to be specified in the datum
     */
    public static final BoundingBox Bounds = new BoundingBox(-90.0, -180.0, 90.0, 180.0);
}
