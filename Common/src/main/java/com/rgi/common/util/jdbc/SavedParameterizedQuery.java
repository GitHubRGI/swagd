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

package com.rgi.common.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Encapsulation of a {@link PreparedStatement} so that prepared statements
 * need not be recreated
 *
 * @author Luke Lambert
 * @param <T> Type of the object to be returned by the query
 */
public abstract class SavedParameterizedQuery<T> implements AutoCloseable
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sqlSupplier
     *             Callback that generates the query string
     * @throws SQLException
     *             if there is a database error
     */
    protected SavedParameterizedQuery(final Connection       databaseConnection,
                                      final Supplier<String> sqlSupplier) throws SQLException
    {
        if(databaseConnection == null || databaseConnection.isClosed())
        {
            throw new IllegalArgumentException("Database connection may not be null or closed");
        }

        if(sqlSupplier == null)
        {
            throw new IllegalArgumentException("SQL supplier may not be null");
        }

        this.preparedStatement =  databaseConnection.prepareStatement(sqlSupplier.get());
    }

    @Override
    public void close() throws SQLException
    {
        this.preparedStatement.close();
    }

    protected PreparedStatement getPreparedStatement()
    {
        return this.preparedStatement;
    }

    protected abstract T processResult(final ResultSet resultSet) throws SQLException;

    protected T execute() throws SQLException
    {
        try(final ResultSet resultSet = this.preparedStatement.executeQuery())
        {
            return resultSet.isBeforeFirst() ? this.processResult(resultSet)
                                             : null;
        }
    }

    private final PreparedStatement preparedStatement;
}
