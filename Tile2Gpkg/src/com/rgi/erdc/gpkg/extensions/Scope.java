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

package com.rgi.erdc.gpkg.extensions;

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

            default: throw new IllegalArgumentException("Text must either be \"read-write\" or \"write-only\".");
        }
    }

    private final String text;
}
