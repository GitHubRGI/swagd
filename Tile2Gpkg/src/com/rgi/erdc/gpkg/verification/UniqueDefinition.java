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

package com.rgi.erdc.gpkg.verification;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UniqueDefinition
{
    public UniqueDefinition(final String... columnNames)
    {
        this(Arrays.asList(columnNames));
    }

    public UniqueDefinition(final Collection<String> columnNames)
    {
        this.columnNames = new HashSet<>(columnNames);
    }

    /**
     * @return the columnNames
     */
    public Set<String> getColumnNames()
    {
        return this.columnNames;
    }

    public boolean equals(final String columnName)
    {
        return this.columnNames.size() == 1 &&
               this.columnNames.contains(columnName);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || !(object instanceof UniqueDefinition))
        {
            return false;
        }
        else if(this == object)
        {
            return true;
        }

        final UniqueDefinition other = (UniqueDefinition)object;

        return  this.columnNames.containsAll(other.columnNames) &&
               other.columnNames.containsAll( this.columnNames);
    }

    @Override
    public int hashCode()
    {
        return this.columnNames.hashCode();
    }

    private final Set<String> columnNames;
}
