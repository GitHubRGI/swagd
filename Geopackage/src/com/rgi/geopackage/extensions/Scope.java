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
 * GeoPackage Extension Scopes
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_requirement-84" type="cite">
 * The scope column value in a gpkg_extensions row SHALL be lowercase
 * "read-write" for an extension that affects both readers and writers, or
 * "write-only" for an extension that affects only writers.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#gpkg_extensions_cols">OGC® GeoPackage Encoding Standard - Table 17. GeoPackage Extensions Table or View Definition (Table or View Name: gpkg_extensions)</a>
 * @see <a href="http://www.geopackage.org/spec/#_requirement-84">OGC® GeoPackage Encoding Standard - Requirement 84</a>
 *
 * @author Luke Lambert
 *
 */
public enum Scope
{
    ReadWrite("read-write"),
    WriteOnly("write-only");

    Scope(final String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return this.text;
    }

    public static Scope fromText(final String text)
    {
        switch(text)
        {
            case "read-write": return ReadWrite;
            case "write-only": return WriteOnly;

            default: throw new IllegalArgumentException("Scope must either be \"read-write\" or \"write-only\".");
        }
    }

    private final String text;
}
