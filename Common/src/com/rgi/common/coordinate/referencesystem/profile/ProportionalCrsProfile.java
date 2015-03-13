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
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.util.BoundsUtility;

/**
 * Calculates profile information for coordinate reference systems that are proportional.
 *
 */
public abstract class ProportionalCrsProfile implements CrsProfile
{
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

        if(!BoundsUtility.contains(bounds, coordinate, tileOrigin))
        {
            throw new IllegalArgumentException("Coordinate is outside the bounds of this coordinate reference system");
        }

        final Coordinate<Double> tileCorner = BoundsUtility.boundsCorner(bounds, tileOrigin);

        final double tileCrsWidth  = bounds.getWidth()  / dimensions.getWidth();
        final double tileCrsHeight = bounds.getHeight() / dimensions.getHeight();

        final double normalizedSrsTileCoordinateX = Math.abs(coordinate.getX() - tileCorner.getX());
        final double normalizedSrsTileCoordinateY = Math.abs(coordinate.getY() - tileCorner.getY());

        int divisor = 1000000000;//round to integer extent
        
        final int tileX = (int)Math.floor(Math.round((normalizedSrsTileCoordinateX / tileCrsWidth)*divisor)/divisor);
        final int tileY = (int)Math.floor(Math.round((normalizedSrsTileCoordinateY / tileCrsHeight)*divisor)/divisor);


        return new Coordinate<>(tileX, tileY);
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

        // This got commented out because occasionally it final makes sense for
        // a coordinate outside final of the bounds (+1) in order to calculate
        // a tile's bounding box.
        //if(!dimensions.contains(row, column))
        //{
        //    throw new IllegalArgumentException("The row and column must be within the tile matrix dimensions");
        //}

        if(column < 0)
        {
            throw new IllegalArgumentException("Column must be 0 or greater;");
        }

        if(row < 0)
        {
            throw new IllegalArgumentException("Row must be 0 or greater;");
        }

        if(tileOrigin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        final double tileCrsWidth  = bounds.getWidth()  / dimensions.getWidth();
        final double tileCrsHeight = bounds.getHeight() / dimensions.getHeight();


        final Coordinate<Integer> tileCoordinate = tileOrigin.transform(TileOrigin.LowerLeft,
                                                                        column,
                                                                        row,
                                                                        dimensions);

        final Coordinate<Double> boundsCorner = bounds.getBottomLeft();

        return new CrsCoordinate(boundsCorner.getX() + (tileCoordinate.getX() + tileOrigin.getHorizontal())*(tileCrsWidth),
                                 boundsCorner.getY() + (tileCoordinate.getY() + tileOrigin.getVertical())  *(tileCrsHeight),
                                 this.getCoordinateReferenceSystem());
    }
}
