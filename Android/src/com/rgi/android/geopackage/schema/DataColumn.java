/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.android.geopackage.schema;

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
 * @author Luke Lambert
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
