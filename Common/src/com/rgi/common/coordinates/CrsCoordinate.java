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

package com.rgi.common.coordinates;

import com.rgi.common.CoordinateReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class CrsCoordinate extends Coordinate<Double>
{
    public CrsCoordinate(final double y, final double x, final String crsAuthority, final int crsIdentifier)
    {
        this(y, x, new CoordinateReferenceSystem(crsAuthority, crsIdentifier));
    }

    public CrsCoordinate(final double y, final double x, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        super(y, x);

        if(coordinateReferenceSystem == null)
        {
            throw new IllegalArgumentException("Coordinate Reference System may not be null");
        }
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    /**
     * @return the coordinateReferenceSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    final CoordinateReferenceSystem coordinateReferenceSystem;
}
