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

package com.rgi.geopackage.schema;

/**
 * GeoPackage Schema Data Column
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_schema_introduction" type="cite">
 * The schema option provides a means to describe the columns of tables in a
 * GeoPackage with more detail than can be captured by the SQL table definition
 * directly. The information provided by this option can be used by
 * applications to, for instance, present data contained in a GeoPackage in a
 * more user-friendly fashion or implement data validation logic.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#_schema">OGC® GeoPackage Encoding Standard - 2.3.2. Data Columns</a>
 * @see <a href="http://www.geopackage.org/spec/#gpkg_data_columns_cols">OGC® GeoPackage Encoding Standard - Table 11. Data Columns Table or View Definition</a>
 *
 * @author LukeLambert
 *
 */
public class DataColumn
{
    /**
     * Constructor
     *
     * @param tableName
     *             Name of the tiles or feature table
     * @param columnName
     *             Name of the table column
     * @param name
     *             A human-readable identifier (e.g. short name) for the columnName content
     * @param title
     *             A human-readable formal title for the columnName content
     * @param description
     *             A human-readable description for the tableName content
     * @param mimeType
     *            <a href="http://www.iana.org/assignments/media-types/index.html">MIME</a> type of columnName if BLOB type, or NULL for other types
     * @param constraintName
     *            Case sensitive column value constraint name specified by reference to gpkg_data_column_constraints.constraint name
     */
    protected DataColumn(final String tableName,
                         final String columnName,
                         final String name,
                         final String title,
                         final String description,
                         final String mimeType,
                         final String constraintName)
    {
        this.tableName      = tableName;
        this.columnName     = columnName;
        this.name           = name;
        this.title          = title;
        this.description    = description;
        this.mimeType       = mimeType;
        this.constraintName = constraintName;
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
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return this.mimeType;
    }

    /**
     * @return the constraintName
     */
    public String getConstraintName()
    {
        return this.constraintName;
    }

    private final String tableName;
    private final String columnName;
    private final String name;
    private final String title;
    private final String description;
    private final String mimeType;
    private final String constraintName;
}
