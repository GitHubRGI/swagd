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

package com.rgi.geopackage.tiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.UniqueDefinition;

/**
 * @author Jenifer Cochran
 *
 */
public class TilePyramidUserDataTableDefinition extends TableDefinition
{
    private final static Map<String, ColumnDefinition> Columns;
    private final static Set<ForeignKeyDefinition>     ForeignKeys;
    private final static Set<UniqueDefinition>         UniqueColumnGroups;

    static
    {
        Columns = new HashMap<>();

        Columns.put("id",           new ColumnDefinition("INTEGER", false, true,  true, null));
        Columns.put("zoom_level",   new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_column",  new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_row",     new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_data",    new ColumnDefinition("BLOB",    true, false, false, null));

        ForeignKeys = Collections.emptySet();
        UniqueColumnGroups =  new HashSet<>(Arrays.asList(new UniqueDefinition("zoom_level", "tile_column", "tile_row")));
    }

    /**
     * @param name table name of the Tiles Table
     */
    public TilePyramidUserDataTableDefinition(final String name)
    {
        super(name,
              TilePyramidUserDataTableDefinition.Columns,
              TilePyramidUserDataTableDefinition.ForeignKeys,
              TilePyramidUserDataTableDefinition.UniqueColumnGroups);
    }

}
