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

package com.rgi.geopackage.verification;

/**
 * @author Luke Lambert
 *
 */
public class ColumnDefinition
{
    /**
     * @param sqlType
     *             String representing the SQL type of the column, e.g.: TEXT, INTEGER, and so on
     * @param notNull
     *             Indicates that this column may not contain null values
     * @param primaryKey
     *             Indicates that this column is the table's primary key (implicitly unique)
     * @param unique
     *             Indicates that values for this column are unique. If primaryKey is true, this value is ignored
     * @param defaultValue
     *             String representation for the default value of this column
     */
    public ColumnDefinition(final String sqlType, final boolean notNull, final boolean primaryKey, final boolean unique, final String defaultValue)
    {
        if(sqlType == null)
        {
            throw new IllegalArgumentException("SQL type may not be null");
        }

        this.sqlType      = sqlType;
        this.notNull      = notNull;
        this.defaultValue = defaultValue;   // TODO convert "foo" to 'foo'
        this.primaryKey   = primaryKey;
        this.unique       = primaryKey || unique;
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || !(object instanceof ColumnDefinition))
        {
            return false;
        }
        else if(this == object)
        {
            return true;
        }

        final ColumnDefinition other = (ColumnDefinition)object;

        return this.sqlType.equals(other.sqlType)    &&
               this.notNull      == other.notNull    &&
               this.primaryKey   == other.primaryKey &&
               this.unique       == other.unique     &&
               (this.defaultValue == null ? other.defaultValue == null : this.defaultValue.matches(other.defaultValue));
    }

    @Override
    public int hashCode()
    {
        return this.sqlType.hashCode()   ^
               (this.notNull    ? 1 : 0) ^
               (this.primaryKey ? 1 : 0) ^
               (this.unique     ? 1 : 0) ^
               (this.defaultValue == null ? 0 : this.defaultValue.hashCode());
    }

    @Override
    public String toString()
    {
        return String.format("Type: %s, not null: %s, default value: %s, primary key: %s, unique: %s",
                             this.sqlType,
                             this.notNull,
                             this.defaultValue,
                             this.primaryKey,
                             this.unique);
    }

    private final String  sqlType;
    private final boolean notNull;
    private final boolean primaryKey;
    private final boolean unique;
    private final String  defaultValue;
}
