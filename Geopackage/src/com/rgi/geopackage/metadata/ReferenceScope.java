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

package com.rgi.geopackage.metadata;

/**
 * GeoPackage Metadata Reference Scopes
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_requirement-71" type="cite">
 * Every gpkg_metadata_reference table reference scope column value SHALL be one of 'geopackage', 'table', 'column', 'row', 'row/col' in lowercase.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#gpkg_metadata_reference_cols">OGC® GeoPackage Encoding Standard - Table 16. Metadata Reference Table Definition (Table Name: gpkg_metadata_reference)</a>
 * @see <a href="http://www.geopackage.org/spec/#_requirement-71">OGC® GeoPackage Encoding Standard - Requirement 71</a>
 *
 * @author Luke Lambert
 *
 */
public enum ReferenceScope
{
    /**
     * The Reference Scope is of the entire GeoPackage
     */
    GeoPackage("geopackage"),
    /**
     * The Reference Scope is of a single Table
     */
    Table     ("table"),
    /**
     * The Reference Scope is of a column
     */
    Column    ("column"),
    /**
     * The Reference Scope is of a row
     */
    Row       ("row"),
    /**
     * The Reference Scope is of a row or a column
     */
    RowCol    ("row/col");

    /**
     * @return the text
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * @param text a string value of the valid Reference scopes
     * @return the ReferenceScope object that corresponds to the text
     */
    public static ReferenceScope fromText(final String text)
    {
        if(text == null)
        {
            throw new IllegalArgumentException("Text may not be null");
        }

        switch(text.toLowerCase())
        {
            case "geopackage": return ReferenceScope.GeoPackage;
            case "table":      return ReferenceScope.Table;
            case "column":     return ReferenceScope.Column;
            case "row":        return ReferenceScope.Row;
            case "row/col":    return ReferenceScope.RowCol;

            default: throw new IllegalArgumentException("Text does not match any valid values for GeoPackage reference scopes");
        }
    }

    /**
     * @param scope the ReferenceScope value
     * @return true if the ReferenceScope pertains to the Row; otherwise returns false.
     */
    public static boolean isRowScope(final ReferenceScope scope)
    {
        return scope == ReferenceScope.Row || scope == ReferenceScope.RowCol;
    }

    /**
     * @param scope the ReferenceScope value
     * @return true if the ReferenceScope pertains to the Column; otherwise returns false;
     */
    public static boolean isColumnScope(final ReferenceScope scope)
    {
        return scope == ReferenceScope.Column || scope == ReferenceScope.RowCol;
    }

    ReferenceScope(final String text)
    {
        this.text = text;
    }

    private final String text;
}
