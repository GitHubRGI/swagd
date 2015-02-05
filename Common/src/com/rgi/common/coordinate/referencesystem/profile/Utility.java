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
 * This class's functionality could be contained in CrsProfile if it were a
 * class rather than an interface.
 *
 * @author Luke Lambert
 *
 */
public class Utility
{
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
            case LowerLeft:  onFarEdge = coordinate.getY() >= bounds.getMaxY() ||
                                         coordinate.getX() >= bounds.getMaxX();
                             break;

            case LowerRight: onFarEdge = coordinate.getY() >= bounds.getMaxY() ||
                                         coordinate.getX() <= bounds.getMinX();
                             break;

            case UpperLeft:  onFarEdge = coordinate.getY() <= bounds.getMinY() ||
                                         coordinate.getX() >= bounds.getMaxX();
                             break;

            case UpperRight: onFarEdge = coordinate.getY() <= bounds.getMinY() ||
                                         coordinate.getX() <= bounds.getMinX();
                             break;

            default: break; // This can't be reached.  All enumeration cases are present in the switch.
        }

        return !onFarEdge && bounds.contains(coordinate);
    }

    public static Coordinate<Double> tileCorner(final BoundingBox bounds, final TileOrigin origin)
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
