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

package com.rgi.geopackage.utility;

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
    /**
     * @param connection
     *            a handle to the database connection
     * @param tableName
     *            The name of the table pulling information from
     * @param selectColumns
     *            the names of the columns in the table that need to be selected
     * @param where
     *            the names of the columns and the values that they need to
     *            equal
     * @throws SQLException
     *             throws if various SQLExceptions occur
     */
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
        for(final Entry<String, Object> whereClause : where)
        {
            final Object value = whereClause.getValue();
            if(value != null)
            {
                this.preparedStatement.setObject(parameterIndex++, whereClause.getValue());
            }
        }
    }

    /**
     * @return a ResultSet object that contains the data produced by the query;
     *         never null
     *
     * @throws SQLException
     *             throws if various SQLExceptions occur
     */
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
