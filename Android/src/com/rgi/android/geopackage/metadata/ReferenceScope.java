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

package com.rgi.android.geopackage.metadata;

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
