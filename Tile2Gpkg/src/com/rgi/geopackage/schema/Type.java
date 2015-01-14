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
    Range,
    Enum,
    Glob;

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
