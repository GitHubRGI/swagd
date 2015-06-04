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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Luke Lambert
 *
 */
public class JdbcUtility
{
    public static void executeUpdate(final Connection databaseConnection, final String sql) throws SQLException
    {
        try(Statement statement = databaseConnection.createStatement())
        {
            statement.executeUpdate(sql);
        }
    }

    public static <T> T getOne(final Connection                databaseConnection,
                               final String                    sql,
                               final PreparedStatementConsumer parameterSetter,
                               final ResultSetFunction<T>      resultMapper) throws SQLException
    {
        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                if(resultSet.isBeforeFirst())
                {
                    return resultMapper.apply(resultSet);
                }

                return null;
            }
        }
    }

    /**
     * Returns {@link ArrayList} of the type of the input consisting of the
     * results of applying the operations in {@link ResultSetFunction} on the
     * given {@link ResultSet}
     *
     * @param resultSet
     *             The result set consisting of the elements
     * @param resultSetFunction
     *             Maps the given {@link ResultSet} elements to another type
     * @return An {@link ArrayList} of the type of the input that are the
     *             results of the mapping the elements in the given {@link
     *             ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> ArrayList<T> map(final ResultSet            resultSet,
                                       final ResultSetFunction<T> resultSetFunction) throws SQLException
    {
        return JdbcUtility.map(resultSet,
                               resultSetFunction,
                               ArrayList<T>::new);
    }

    /**
     * Returns {@link ArrayList} of the type of the input consisting of the
     * results of applying the operations in {@link ResultSetFunction} on the
     * given {@link ResultSet}
     *
     * @param resultSet
     *             The result set consisting of the elements
     * @param resultSetFunction
     *             Maps the given {@link ResultSet} elements to another type
     * @param collectionFactory
     *             Supplier which returns a new, empty Collection of the
     *             appropriate type
     * @return An {@link ArrayList} of the type of the input that are the
     *             results of the mapping the elements in the given {@link
     *             ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T, C extends Collection<T>> C map(final ResultSet            resultSet,
                                                     final ResultSetFunction<T> resultSetFunction,
                                                     final Supplier<C>          collectionFactory) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or closed");
        }

        if(resultSetFunction == null)
        {
            throw new IllegalArgumentException("Result set function may not be null");
        }

        if(collectionFactory == null)
        {
            throw new IllegalArgumentException("Collection factory may not be null");
        }

        final C collection = collectionFactory.get();

        while(resultSet.next())
        {
            collection.add(resultSetFunction.apply(resultSet));
        }

        return collection;
    }

    /**
     * Returns {@link ArrayList} of the type of the input consisting of the
     * results of applying the operations in {@link ResultSetFunction} on the
     * given {@link ResultSet}, and then filtering those results based on the
     * given {@link Predicate}
     *
     * @param resultSet
     *             The result set consisting of the elements
     * @param function
     *             Maps the given {@link ResultSet} elements to another type
     * @param predicate
     *             Filter mechanism for the mapped data
     * @param collectionFactory
     *             Supplier which returns a new, empty Collection of the
     *             appropriate type
     * @return
     *      An {@link ArrayList} of the type of the input that are the results
     *      of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *             if mapping function throws
     */
    public static <T, C extends Collection<T>> C mapFilter(final ResultSet            resultSet,
                                                           final ResultSetFunction<T> function,
                                                           final JdbcPredicate<T>     predicate,
                                                           final Supplier<C>          collectionFactory) throws SQLException
    {
        if(resultSet == null)
        {
            throw new IllegalArgumentException("Result set may not be null");
        }

        if(function == null)
        {
            throw new IllegalArgumentException("function may not be null");
        }

        if(predicate == null)
        {
            throw new IllegalArgumentException("Predicate may not be null");
        }

        final C collection = collectionFactory.get();

        while(resultSet.next())
        {
            final T mappedValue = function.apply(resultSet);

            if(predicate.test(mappedValue))
            {
                collection.add(mappedValue);
            }
        }

        return collection;
    }

    /**
     *  Returns {@link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@link ResultSetFunction} on the given {@link ResultSet}
     *
     * @param resultSet
     *      The result set consisting of the elements
     * @param resultSetFunction
     *      Maps the given {@link ResultSet} element to another type
     * @return
     *      An {@link ArrayList} of the type of the input that are the results of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> T mapOne(final ResultSet resultSet, final ResultSetFunction<T> resultSetFunction) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or closed");
        }

        if(resultSetFunction == null)
        {
            throw new IllegalArgumentException("Result set function may not be null");
        }

        if(resultSet.isBeforeFirst())
        {
            return resultSetFunction.apply(resultSet);
        }

        return null;
    }

    /**
     * Create list of {@link Object}s by repeatedly calling
     * {@link ResultSet#getObject(int)}
     *
     * @param result
     *             Result set to query
     * @param startColumnIndex
     *             Column index to begin with.  Must be less than or equal to
     *             <tt>endColumnIndex</tt>
     * @param endColumnIndex
     *             Column index to end with (inclusive). Must be greater than
     *             or equal to <tt>startColumnIndex</tt>
     * @return List of {@link Object}s with size <tt>endColumnIndex - startColumnIndex + 1</tt>
     * @throws SQLException
     *             if there is a database error
     */
    public static List<Object> getObjects(final ResultSet result, final int startColumnIndex, final int endColumnIndex) throws SQLException
    {
        if(result == null || result.isClosed())
        {
            throw new IllegalArgumentException("Result may not be null or closed");
        }

        if(endColumnIndex < startColumnIndex)
        {
            throw new IllegalArgumentException("End column index must be greater than start column index");
        }

        final List<Object> objects = new ArrayList<>(endColumnIndex - startColumnIndex + 1);

        for(int columnIndex = startColumnIndex; columnIndex <= endColumnIndex; ++columnIndex)
        {
            objects.add(result.getObject(columnIndex));
        }

        return objects;
    }
}
