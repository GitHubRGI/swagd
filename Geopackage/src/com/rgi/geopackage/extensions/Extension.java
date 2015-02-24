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
 * GeoPackage Extension
 *
 * <blockquote cite="http://www.geopackage.org/spec/#extensions_table_definition" type="cite">
 * The gpkg_extensions table or updateable view in a GeoPackage is used to
 * indicate that a particular extension applies to a GeoPackage, a table in a
 * GeoPackage or a column of a table in a GeoPackage. An application that
 * access a GeoPackage can query the gpkg_extensions table instead of the
 * contents of all the user data tables to determine if it has the required
 * capabilities to read or write to tables with extensions, and to "fail fast"
 * and return an error message if it does not.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#extensions_table_definition">OGC® GeoPackage Encoding Standard - 2.5.2.1.1. Table Definition</a>
 * @see <a href="http://www.geopackage.org/spec/#gpkg_extensions_cols">OGC® GeoPackage Encoding Standard - Table 17. GeoPackage Extensions Table or View Definition (Table or View Name: gpkg_extensions)</a>
 *
 * @author Luke Lambert
 *
 */
public class Extension
{
    /**
     * An object representation of an entry in the GeoPackage extensions table
     *
     * @param tableName
     *             Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL
     * @param columnName
     *             Name of the column that requires the extension. When NULL, the extension is required for the entire table
     * @param extensionName
     *             The case sensitive name of the extension that is required, in the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> is [a-zA-Z0-9]. The valid character set for <extension_name> is [a-zA-Z0-9_]
     * @param definition
     *             Definition of the extension in the form specfied by the template in <a href="http://www.geopackage.org/spec/#extension_template">GeoPackage Extension Template (Normative)</a> or reference thereto.
     * @param scope
     *             Indicates scope of extension effects on readers / writers: "read-write" or "write-only" in lowercase.
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

        if(extensionName == null || extensionName.isEmpty())
        {
            throw new IllegalArgumentException("Extension name may not be null or empty");
        }

        if(!extensionName.matches(Extension.ExtensionNameRegularExpression))
        {
            throw new IllegalArgumentException("Extension name must be a value of the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> SHALL be [a-zA-Z0-9]. The valid character set for <extension_name> SHALL be [a-zA-Z0-9_]");   // Requirement 82
        }

        if(definition == null || definition.isEmpty())
        {
            throw new IllegalArgumentException("Definition may not be null or empty");
        }

        if(Scope.fromText(scope) == null)
        {
            throw new IllegalArgumentException("Bad value for scope");
        }

        this.tableName     = tableName;
        this.columnName    = columnName;
        this.extensionName = extensionName;
        this.definition    = definition;
        this.scope         = scope;
    }

    /**
     * @param inTableName the tableName corresponding to other Extension
     * @param inColumnName the columnName corresponding to other Extension
     * @param inExtensionName the extension Name corresponding to other Extension
     * @param inDefinition the definition corresponding to other Extension
     * @param inScope the Scope corresponding to other Extension
     * @return true if this.Extension equals the parameters
     */
    public boolean equals(final String inTableName,
                          final String inColumnName,
                          final String inExtensionName,
                          final String inDefinition,
                          final Scope  inScope)
    {
        return Extension.equals(this.tableName,     inTableName)     &&
               Extension.equals(this.columnName,    inColumnName)    &&
               Extension.equals(this.extensionName, inExtensionName) &&
               Extension.equals(this.definition,    inDefinition)    &&
               Extension.equals(this.scope,         inScope.toString());
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
    public String getScope()
    {
        return this.scope;
    }

    private static <T> boolean equals(final T lhs, final T rhs)
    {
        return lhs == null ? rhs == null
                           : lhs.equals(rhs);
    }

    private final String tableName;
    private final String columnName;
    private final String extensionName;
    private final String definition;
    private final String scope;

    /**
     * The regular Expression of allowed extension names according to the OGC
     * specification (http://www.geopackage.org/spec/#_extension_mechanism)
     * under Requirement 82
     */
    public static final String ExtensionNameRegularExpression = "[a-zA-Z0-9]+_[a-zA-Z0-9_]+";
}
