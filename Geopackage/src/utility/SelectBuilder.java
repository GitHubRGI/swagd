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

package utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * This class is used to facilitate the creation and execution of
 * {@link PreparedStatement}s with parameters that may be null.  Unfortunately,
 * an SQL WHERE clause must use IS NULL as a null test rather than the equals
 * operator.  This means the the SQL of the prepared statement varies depending
 * on the values being bound to it.
 *
 * @author Luke Lambert
 *
 */
public class SelectBuilder implements AutoCloseable
{
    public SelectBuilder(final Connection                        connection,
                         final String                            tableName,
                         final Collection<String>                selectColumns,
                         final Collection<Entry<String, Object>> where) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Table name may not be null or empty");
        }

        if(selectColumns == null || selectColumns.isEmpty())
        {
            throw new IllegalArgumentException("The selected columns collection may not be null or empty");
        }

        if(selectColumns.stream().anyMatch(columnName -> columnName == null || columnName.isEmpty()))
        {
            throw new IllegalArgumentException("No column name in the selected columns may be null or empty");
        }

        if(where == null || where.isEmpty())
        {
            throw new IllegalArgumentException("The where columns collection may not be null or empty");
        }

        if(where.stream().anyMatch(entry -> entry.getKey() == null || entry.getKey().isEmpty()))
        {
            throw new IllegalArgumentException("No column name in a where clause may be null or empty");
        }

        final String querySql = String.format("SELECT %s FROM %s WHERE %s;",
                                              String.join(", ", selectColumns),
                                              tableName,
                                              where.stream()
                                                   .map(entry -> entry.getKey() + (entry.getValue() == null ? " IS NULL" : " = ?"))
                                                   .collect(Collectors.joining(" AND ")));

        this.preparedStatement = connection.prepareStatement(querySql);

        int parameterIndex = 1;    // 1-indexed
        for(Entry<String, Object> whereClause : where)
        {
            Object value = whereClause.getValue();
            if(value != null)
            {
                this.preparedStatement.setObject(parameterIndex++, whereClause.getValue());
            }
        }
    }

    public ResultSet executeQuery() throws SQLException
    {
        return this.preparedStatement.executeQuery();
    }

    @Override
    public void close() throws SQLException
    {
        this.preparedStatement.close();
    }

    private final PreparedStatement preparedStatement;

}
