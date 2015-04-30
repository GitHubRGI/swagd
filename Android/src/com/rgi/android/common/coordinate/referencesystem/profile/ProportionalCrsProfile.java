/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.android.common.coordinate.referencesystem.profile;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.coordinate.CrsCoordinate;
import com.rgi.android.common.tile.TileOrigin;
import com.rgi.android.common.tile.scheme.TileMatrixDimensions;
import com.rgi.android.common.util.BoundsUtility;

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

        final int divisor = 1000000000; // Round to integer extent

        final int tileX = (int)Math.floor(Math.round((normalizedSrsTileCoordinateX / tileCrsWidth)  * divisor) / divisor);
        final int tileY = (int)Math.floor(Math.round((normalizedSrsTileCoordinateY / tileCrsHeight) * divisor) / divisor);


        return new Coordinate<Integer>(tileX, tileY);
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

        return new CrsCoordinate(boundsCorner.getX() + (tileCoordinate.getX() + tileOrigin.getHorizontal()) * (tileCrsWidth),
                                 boundsCorner.getY() + (tileCoordinate.getY() + tileOrigin.getVertical())   * (tileCrsHeight),
                                 this.getCoordinateReferenceSystem());
    }

    @Override
    public BoundingBox getTileBounds(final int                  column,
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

        return new BoundingBox(boundsCorner.getX() + (tileCoordinate.getX() +     tileOrigin.getHorizontal()) * (tileCrsWidth),
                               boundsCorner.getY() + (tileCoordinate.getY() +     tileOrigin.getVertical())   * (tileCrsHeight),
                               boundsCorner.getX() + (tileCoordinate.getX() + 1 + tileOrigin.getHorizontal()) * (tileCrsWidth),
                               boundsCorner.getY() + (tileCoordinate.getY() + 1 + tileOrigin.getVertical())   * (tileCrsHeight));
    }
}
