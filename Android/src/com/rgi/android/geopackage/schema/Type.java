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

import java.util.stream.Stream;

/**
 * GeoPackage Column Constraint Types
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_table_data_values_7" type="cite">
 * The lowercase gpkg_data_column_constraints constraint_type column value
 * specifies the type of constraint: "range", "enum", or "glob" (text pattern
 * match).
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#_table_data_values_7">OGC® GeoPackage Encoding Standard - 2.3.3.1.2. Table Data Values</a>
 *
 * @author Luke Lambert
 */

public enum Type
{
    // http://www.geopackage.org/spec/#metadata_scopes
    /**
     * text pattern match "range"
     */
    Range,
    /**
     * text pattern match "enum"
     */
    Enum,
    /**
     * text pattern match "glob"
     */
    Glob;

    /**
     * @param inName String of a Type
     * @return the Type object associated with the parameter
     */
    public static Type fromString(final String inName)
    {
        return Stream.of(Type.values())
                     .filter(scope -> scope.toString().equalsIgnoreCase(inName))
                     .findFirst()
                     .orElse(null);
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }
}
