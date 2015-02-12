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
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
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

        if(!Utility.contains(Bounds, coordinate, tileOrigin))
        {
            throw new IllegalArgumentException("Coordinate is outside the bounds of this coordinate reference system");
        }

        final Coordinate<Double> tileCorner = Utility.boundsCorner(Bounds, tileOrigin);

        final Dimensions tileDimensions = this.getTileDimensions(dimensions);

        final double normalizedSrsTileCoordinateY = Math.abs(coordinate.getY() - tileCorner.getY());
        final double normalizedSrsTileCoordinateX = Math.abs(coordinate.getX() - tileCorner.getX());

        final int tileY = (int)Math.floor(normalizedSrsTileCoordinateY / tileDimensions.getHeight());
        final int tileX = (int)Math.floor(normalizedSrsTileCoordinateX / tileDimensions.getWidth());

        return new Coordinate<>(tileY, tileX);
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int                  row,
                                             final int                  column,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin)
    {
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

        final Dimensions tileCrsDimensions = this.getTileDimensions(dimensions);

        final Coordinate<Integer> tileCoordinate = tileOrigin.transform(TileOrigin.LowerLeft,
                                                                        row,
                                                                        column,
                                                                        dimensions);
        final double originShiftY = Bounds.getHeight() / 2.0;
        final double originShiftX = Bounds.getWidth()  / 2.0;

        return new CrsCoordinate(((tileCoordinate.getY() + tileOrigin.getVertical())   * tileCrsDimensions.getHeight()) - originShiftY,
                                 ((tileCoordinate.getX() + tileOrigin.getHorizontal()) * tileCrsDimensions.getWidth())  - originShiftX,
                                 this.getCoordinateReferenceSystem());
    }

//    @Override
//    public Dimensions getTileDimensions(final TileMatrixDimensions dimensions)
//    {
//        final double height = GlobalGeodeticCrsProfile.Bounds.getHeight() / dimensions.getHeight();
//        final double width  = GlobalGeodeticCrsProfile.Bounds.getWidth()  / dimensions.getWidth();
//
//        return new Dimensions(height, width);
//    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return GlobalGeodeticCrsProfile.CoordinateReferenceSystem;
    }

    /**
     * @param coordinate coordinate in current Coordinate Reference System
     * @return coordinate in Global Geodetic
     */
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
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate)
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
