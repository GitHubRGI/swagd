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

package com.rgi.android.geopackage.tiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rgi.android.geopackage.verification.ColumnDefinition;
import com.rgi.android.geopackage.verification.ForeignKeyDefinition;
import com.rgi.android.geopackage.verification.TableDefinition;
import com.rgi.android.geopackage.verification.UniqueDefinition;

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
        Columns = new HashMap<String, ColumnDefinition>();

        Columns.put("id",           new ColumnDefinition("INTEGER", false, true,  true, null));
        Columns.put("zoom_level",   new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_column",  new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_row",     new ColumnDefinition("INTEGER", true, false, false, null));
        Columns.put("tile_data",    new ColumnDefinition("BLOB",    true, false, false, null));

        ForeignKeys = Collections.emptySet();
        UniqueColumnGroups =  new HashSet<UniqueDefinition>(Arrays.asList(new UniqueDefinition("zoom_level", "tile_column", "tile_row")));
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
