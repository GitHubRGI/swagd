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
 * Coordinate within a specific coordinate reference system
 *
 * @author Luke Lambert
 *
 */
public class CrsCoordinate extends Coordinate<Double>
{
    /**
     * Constructor
     *
     * @param x
     *             Horizontal portion of the coordinate
     * @param y
     *             Vertical portion of the coordinate
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG"). This value is converted to
     *             upper case.
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     */
    public CrsCoordinate(final double x, final double y, final String authority, final int identifier)
    {
        this(x, y, new CoordinateReferenceSystem(authority, identifier));
    }

    /**
     * Constructor
     *
     * @param x
     *             Horizontal portion of the coordinate
     * @param y
     *             Vertical portion of the coordinate
     * @param coordinateReferenceSystem
     *             A coordinate reference system object
     */
    public CrsCoordinate(final double x, final double y, final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        this(new Coordinate<>(x, y), coordinateReferenceSystem);
    }

    /**
     * Constructor
     *
     * @param coordinate
     *             A coordinate in the units of the coordinate reference system
     * @param coordinateReferenceSystem
     *             A coordinate reference system object
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

    /**
     * @return Returns the coordinate reference system
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    private final CoordinateReferenceSystem coordinateReferenceSystem;
}
