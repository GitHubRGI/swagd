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
 * A means of uniquely identifying a coordinate reference system
 *
 * @author Luke Lambert
 *
 */
public class CoordinateReferenceSystem
{
    /**
     * Constructor
     *
     * @param authority
     *             The name of the defining authority of the coordinate
     *             reference system (e.g. "EPSG"). This value is converted to
     *             upper case.
     * @param identifier
     *             The identifier as assigned by the the authority of the
     *             coordinate reference system
     */
    public CoordinateReferenceSystem(final String authority, final int identifier)
    {
        if(authority == null || authority.isEmpty())
        {
            throw new IllegalArgumentException("Authority string may not be null or empty");
        }

        this.authority  = authority.toUpperCase();
        this.identifier = identifier;
    }

    /**
     * @return Returns the defining authority's name
     */
    public String getAuthority()
    {
        return this.authority;
    }

    /**
     * @return Returns the identifier as assigned by the the authority
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public String toString()
    {
        return String.format("%s:%d",
                             this.authority,
                             this.identifier);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != this.getClass())
        {
            return false;
        }

        final CoordinateReferenceSystem other = (CoordinateReferenceSystem)object;

        return this.authority.equals(other.authority) && this.identifier == other.identifier;
    }

    @Override
    public int hashCode()
    {
        return this.authority.hashCode() ^ this.identifier;
    }

    private final String authority;
    private final int    identifier;
}
