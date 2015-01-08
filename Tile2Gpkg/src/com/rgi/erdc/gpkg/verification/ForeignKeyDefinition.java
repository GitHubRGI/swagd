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

public class ForeignKeyDefinition
{
    /**
     * @param referenceTableName
     * @param fromColumnName
     * @param toColumnName
     */
    public ForeignKeyDefinition(final String referenceTableName, final String fromColumnName, final String toColumnName)
    {
        if(referenceTableName == null || referenceTableName.isEmpty())
        {
            throw new IllegalArgumentException("Reference table name may not be null or empty");
        }

        if(fromColumnName == null || fromColumnName.isEmpty())
        {
            throw new IllegalArgumentException("From column name table name may not be null or empty");
        }

        if(toColumnName == null || toColumnName.isEmpty())
        {
            throw new IllegalArgumentException("To column name may not be null or empty");
        }

        this.referenceTableName = referenceTableName;
        this.fromColumnName     = fromColumnName;
        this.toColumnName       = toColumnName;
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || !(object instanceof ForeignKeyDefinition))
        {
            return false;
        }

        if(object == this)
        {
            return true;
        }

        final ForeignKeyDefinition other = (ForeignKeyDefinition)object;

        return this.referenceTableName.equals(other.referenceTableName) &&
               this.    fromColumnName.equals(other.    fromColumnName) &&
               this.      toColumnName.equals(other.      toColumnName);

    }

    @Override
    public int hashCode()
    {
        return this.referenceTableName.hashCode() ^
               this.    fromColumnName.hashCode() ^
               this.      toColumnName.hashCode();
    }

    /**
     * @return the referenceTableName
     */
    public String getReferenceTableName()
    {
        return this.referenceTableName;
    }

    /**
     * @return the fromColumnName
     */
    public String getFromColumnName()
    {
        return this.fromColumnName;
    }

    /**
     * @return the toColumnName
     */
    public String getToColumnName()
    {
        return this.toColumnName;
    }

    private final String referenceTableName;
    private final String     fromColumnName;
    private final String       toColumnName;
}
