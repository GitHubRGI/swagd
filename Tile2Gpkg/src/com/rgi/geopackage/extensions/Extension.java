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

package com.rgi.geopackage.extensions;

/**
 * @author Luke Lambert
 *
 */
public class Extension
{
    /**
     * An object representation of an entry in the GeoPackage extensions table
     *
     * @param tableName
     * @param columnName
     * @param extensionName
     * @param definition
     * @param scope
     */
    protected Extension(final String tableName,
                        final String columnName,
                        final String extensionName,
                        final String definition,
                        final String scope)
    {
        if(columnName != null && tableName == null)
        {
            throw new IllegalArgumentException("Table name may not be null if column name is not null"); // Requirement 80
        }

        if(tableName != null && tableName.isEmpty())
        {
            throw new IllegalArgumentException("If table name is not null, it may not be empty");
        }

        if(columnName != null && columnName.isEmpty())
        {
            throw new IllegalArgumentException("If column name is not null, it may not be empty");
        }

        if(extensionName == null || !extensionName.isEmpty())
        {
            throw new IllegalArgumentException("Extension name may not be null or empty");
        }

        if(!extensionName.matches(Extension.ExtensionNameRegularExpression))
        {
            throw new IllegalArgumentException("Extension name must be a value of the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> SHALL be [a-zA-Z0-9]. The valid character set for <extension_name> SHALL be [a-zA-Z0-9_]");   // Requirement 82
        }

        this.tableName     = tableName;
        this.columnName    = columnName;
        this.extensionName = extensionName;
        this.definition    = definition;
        this.scope         = Scope.fromText(scope);
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return the columnName
     */
    public String getColumnName()
    {
        return this.columnName;
    }

    /**
     * @return the extensionName
     */
    public String getExtensionName()
    {
        return this.extensionName;
    }

    /**
     * @return the definition
     */
    public String getDefinition()
    {
        return this.definition;
    }

    /**
     * @return the scope
     */
    public Scope getScope()
    {
        return this.scope;
    }

    private final String tableName;
    private final String columnName;
    private final String extensionName;
    private final String definition;
    private final Scope  scope;

    public static final String ExtensionNameRegularExpression = "[a-zA-Z0-9]+_[a-zA-Z0-9_]+";
}
