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
public class CoordinateReferenceSystem
{
    /**
     * @param authority the Coordinate Reference System authority name (typically "EPSG")
     * @param identifier the version number of the authority
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
     * @return the authority
     */
    public String getAuthority()
    {
        return this.authority;
    }

    /**
     * @return the identifier
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
