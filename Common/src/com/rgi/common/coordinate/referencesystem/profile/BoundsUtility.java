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
     * @param bounds the bounding box of the coordinates
     * @param coordinate the coordinate being tested if within the bounding box
     * @param origin the origin the tiles are numbered
     * @return true if the coordinate is within the bounding box; otherwise returns false;
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
     * @param bounds the bounding box of the information
     * @param origin the origin of the tiles
     * @return the coordinate of the bounding box corner based on the Tile Origin (example: TileOrigin.LowerLeft returns LowerLeft corner of bounding box)
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
