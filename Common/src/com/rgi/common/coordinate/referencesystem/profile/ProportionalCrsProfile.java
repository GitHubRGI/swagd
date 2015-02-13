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

public abstract class ProportionalCrsProfile implements CrsProfile
{
    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate coordinate, final BoundingBox bounds, final TileMatrixDimensions dimensions, final TileOrigin tileOrigin)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int row, final int column, final BoundingBox bounds, final TileMatrixDimensions dimensions, final TileOrigin tileOrigin)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
