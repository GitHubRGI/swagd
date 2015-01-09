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

package com.rgi.geopackage.verification;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 *
 */
public class TableDefinition
{
    /**
     * TODO
     *
     * @param name
     * @param columns
     */
    public TableDefinition(final String                        name,
                           final Map<String, ColumnDefinition> columns)
    {
        this(name, columns, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * TODO
     *
     * @param name
     * @param columns
     * @param foreignKeys
     */
    public TableDefinition(final String                        name,
                           final Map<String, ColumnDefinition> columns,
                           final Set<ForeignKeyDefinition>     foreignKeys)
    {
        this(name, columns, foreignKeys, Collections.emptySet());
    }


    /**
     * TODO
     *
     * @param name
     * @param columns
     * @param foreignKeys
     * @param groupUniques
     */
    public TableDefinition(final String                        name,
                           final Map<String, ColumnDefinition> columns,
                           final Set<ForeignKeyDefinition>     foreignKeys,
                           final Set<UniqueDefinition>         groupUniques)
    {
        if(name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("Table name may not be null or empty");
        }

        if(columns == null)
        {
            throw new IllegalArgumentException("Columns name may not be null");
        }

        if(foreignKeys == null)
        {
            throw new IllegalArgumentException("Foreign key collection may not be null");
        }

        if(groupUniques == null)
        {
            throw new IllegalArgumentException("Group uniques collection may not be null");
        }

        final Set<String> columnNames = columns.keySet();

        final Set<String> badForeignKeyFromColumns = foreignKeys.stream()
                                                                .map(foreignKey -> foreignKey.getFromColumnName())
                                                                .filter(foreignKeyFromColumnName -> !columnNames.contains(foreignKeyFromColumnName))
                                                                .collect(Collectors.toSet());

        if(badForeignKeyFromColumns.size() > 0)
        {
            throw new IllegalArgumentException(String.format("Foreign key definitions reference a the following 'from' columns that do not exist in this table: %s",
                                                             String.join(", ", badForeignKeyFromColumns)));
        }

        final Set<String> groupUniqueColumns = groupUniques.stream()
                                                            .collect(HashSet<String>::new,
                                                                     (set,  groupUnique) -> set.addAll(groupUnique.getColumnNames()),
                                                                     (set1, set2)        -> set1.addAll(set2));

        final Set<String> badGroupUniqueColumns = groupUniqueColumns.stream()
                                                            .filter(columnName -> !columnNames.contains(columnName))
                                                            .collect(Collectors.toSet());


        if(badGroupUniqueColumns.size() > 0)
        {
            throw new IllegalArgumentException(String.format("Group unique definitions reference the following columns that do not exist in this table: %s",
                                                             String.join(", ", badGroupUniqueColumns)));
        }

        this.name         = name;
        this.columns      = columns;
        this.foreignKeys  = foreignKeys;
        this.groupUniques = groupUniques;
    }

    /**
     * @return the table name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the column definitions
     */
    public Map<String, ColumnDefinition> getColumns()
    {
        return this.columns;
    }

    /**
     * @return the foreign key definitions
     */
    public Set<ForeignKeyDefinition> getForeignKeys()
    {
        return this.foreignKeys;
    }

    /**
     * @return the groupUniques
     */
    protected Set<UniqueDefinition> getGroupUniques()
    {
        return this.groupUniques;
    }

    private final String                        name;
    private final Map<String, ColumnDefinition> columns;
    private final Set<ForeignKeyDefinition>     foreignKeys;
    private final Set<UniqueDefinition>         groupUniques;
}
