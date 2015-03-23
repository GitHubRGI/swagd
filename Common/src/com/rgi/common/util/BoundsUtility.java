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


package com.rgi.common.util;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.TileOrigin;

/**
 * {@link BoundingBox} utilities specific to a {@link TileOrigin}
 *
 * @author Luke Lambert
 *
 */
public class BoundsUtility
{
    /**
     * Checks to see if a coordinate is contained in a bounding box without
     * lying on the edges opposite a tile origin
     *
     * @param bounds
     *             An area described by a {@link BoundingBox}
     * @param coordinate
     *             The coordinate being tested against the bounding box
     * @param origin
     *             Indication of which sides of the bounding box should be
     *             treated as inclusive. The sides opposite the corner are
     *             considered to be outside of the bounds.
     * @return True if the coordinate is fully within the bounding box or lies
     *             on the sides adjacent to the origin; otherwise returns false
     */
    public static boolean contains(final BoundingBox bounds, final Coordinate<Double> coordinate, final TileOrigin origin)
    {
        if(bounds == null)
        {
            throw new IllegalArgumentException("Bounding box may not be null");
        }

        if(coordinate == null)
        {
            throw new IllegalArgumentException("Meter coordinate may not be null");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        boolean onFarEdge = false;

        // TODO is there a more clever way to go about this?
        switch(origin)
        {
            case LowerLeft:  onFarEdge = coordinate.getY() == bounds.getMaxY() || coordinate.getX() == bounds.getMaxX(); break;
            case LowerRight: onFarEdge = coordinate.getY() == bounds.getMaxY() || coordinate.getX() == bounds.getMinX(); break;
            case UpperLeft:  onFarEdge = coordinate.getY() == bounds.getMinY() || coordinate.getX() == bounds.getMaxX(); break;
            case UpperRight: onFarEdge = coordinate.getY() == bounds.getMinY() || coordinate.getX() == bounds.getMinX(); break;

            default: break; // This can't be reached.  All enumeration cases are present in the switch.
        }

        return !onFarEdge && bounds.contains(coordinate);
    }

    /**
     * Gets the corner of a bounding box that corresponds to the tile origin
     *
     * @param bounds
     *             An area described by a {@link BoundingBox}
     * @param origin
     *             Representation of the bounding box's corner
     * @return The coordinate of the bounding box corner based on the tile
     *             origin. e.g.: {@link TileOrigin#LowerLeft} returns lower
     *             left corner of the bounding box
     */
    public static Coordinate<Double> boundsCorner(final BoundingBox bounds, final TileOrigin origin)
    {
        if(bounds == null)
        {
            throw new IllegalArgumentException("Bounding box may not be null");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        switch(origin)
        {
            case LowerLeft:  return bounds.getBottomLeft();
            case LowerRight: return bounds.getBottomRight();
            case UpperLeft:  return bounds.getTopLeft();
            case UpperRight: return bounds.getTopRight();

            default: throw new IllegalArgumentException("Unrecognized tile origin"); // This can't be reached.  All enumeration cases are present in the switch.
        }
    }
}
