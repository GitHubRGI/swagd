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

package com.rgi.common.coordinate;


/**
 * @author Luke Lambert
 *
 */
public class CrsCoordinate extends Coordinate<Double>
{
    /**
     * @param y y value in units of the Coordinate Reference System
     * @param x x value in units of the Coordinate Reference System
     * @param crsAuthority the Coordinate Reference System authority name (typically "EPSG") of the coordinate
     * @param crsIdentifier the version number of the authority of the coordinate
     */
    public CrsCoordinate(final double y, final double x, final String crsAuthority, final int crsIdentifier)
    {
        this(y, x, new CoordinateReferenceSystem(crsAuthority, crsIdentifier));
    }

    /**
     * @param y y value in units of the Coordinate Reference System
     * @param x x value in units of the Coordinate Reference System
     * @param coordinateReferenceSystem the Coordinate Reference System of the coordinate
     */
    public CrsCoordinate(final double y, final double x, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        this(new Coordinate<>(y, x), coordinateReferenceSystem);
    }

    /**
     * @param coordinate the coordinate in units of the Coordinate Reference System
     * @param coordinateReferenceSystem the Coordinate Reference System of the coordinate
     */
    public CrsCoordinate(final Coordinate<Double> coordinate, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        super(coordinate);

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

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != this.getClass())
        {
            return false;
        }

        final CrsCoordinate other = (CrsCoordinate)object;

        return super.equals(other) && this.coordinateReferenceSystem.equals(other.coordinateReferenceSystem);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ this.coordinateReferenceSystem.hashCode();
    }

    private final CoordinateReferenceSystem coordinateReferenceSystem;
}
