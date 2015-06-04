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

package com.rgi.android.common.util.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.rgi.android.common.util.functional.Predicate;

/**
 * @author Luke Lambert
 *
 */
public class JdbcUtility
{
    public static void executeUpdate(final Connection databaseConnection, final String sql) throws SQLException
    {
        final Statement statement = databaseConnection.createStatement();

        try
        {
            statement.executeUpdate(sql);
        }
        finally
        {
            statement.close();
        }
    }

    public static <T> T getOne(final Connection                databaseConnection,
                               final String                    sql,
                               final PreparedStatementConsumer parameterSetter,
                               final ResultSetFunction<T>      resultMapper) throws SQLException
    {
        final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);

        try
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            final ResultSet resultSet = preparedStatement.executeQuery();

            try
            {
                if(resultSet.isBeforeFirst())
                {
                    return resultMapper.apply(resultSet);
                }

                return null;
            }
            finally
            {
                resultSet.close();
            }
        }
        finally
        {
            preparedStatement.close();
        }
    }

    public static <T> List<T> get(final Connection                databaseConnection,
                                  final String                    sql,
                                  final PreparedStatementConsumer parameterSetter,
                                  final ResultSetFunction<T>      resultMapper) throws SQLException
    {
        final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);

        try
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            final ResultSet resultSet = preparedStatement.executeQuery();

            try
            {
                final ArrayList<T> results = new ArrayList<T>();

                while(resultSet.next())
                {
                    results.add(resultMapper.apply(resultSet));
                }

                return results;
            }
            finally
            {
                resultSet.close();
            }
        }
        finally
        {
            preparedStatement.close();
        }
    }

    public static void forEach(final Connection                databaseConnection,
                               final String                    sql,
                               final PreparedStatementConsumer parameterSetter,
                               final ResultSetConsumer         resultConsumer) throws SQLException
    {
        // TODO NEEDS MORE NULLCHECKS

        final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);

        try
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            final ResultSet resultSet = preparedStatement.executeQuery();

            try
            {
                while(resultSet.next())
                {
                    resultConsumer.accept(resultSet);
                }
            }
            finally
            {
                resultSet.close();
            }
        }
        finally
        {
            preparedStatement.close();
        }
    }

    public static <T> T add(final Connection                databaseConnection,
                            final String                    sql,
                            final PreparedStatementConsumer parameterSetter,
                            final ResultSetFunction<T>      keysMapper) throws SQLException
    {
        final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        T returnValue = null;

        try
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            preparedStatement.executeUpdate();

            final ResultSet resultKeys = preparedStatement.getGeneratedKeys();

            try
            {
                returnValue = keysMapper.apply(resultKeys);
            }
            finally
            {
                resultKeys.close();
            }
        }
        catch(final SQLException ex)
        {
            databaseConnection.rollback();
            throw ex;
        }
        finally
        {
            preparedStatement.close();
        }

        databaseConnection.commit();

        return returnValue;
    }

    public static <T> void add(final Connection                     databaseConnection,
                               final String                         sql,
                               final Iterable<T>                    values,
                               final PreparedStatementBiConsumer<T> parameterSetter) throws SQLException
    {
        final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);

        try
        {
            for(final T value : values)
            {
                if(parameterSetter != null)
                {
                    parameterSetter.accept(preparedStatement, value);
                }

                preparedStatement.executeUpdate();
            }
        }
        catch(final SQLException ex)
        {
            databaseConnection.rollback();
            throw ex;
        }
        finally
        {
            preparedStatement.close();
        }

        databaseConnection.commit();
    }

    /**
     * Returns {@link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@link ResultSetPredicate} on the given {@link ResultSet}
     *
     * @param resultSet
     *      The result set containing the elements
     * @param resultSetPredicate
     *      Evaluates each element in the given {@link ResultSet} to see if it satisfies the predicate
     * @return
     *      an ArrayList of the type of input consisting of the elements that satisfy the resultSetPredicate
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static boolean anyMatch(final ResultSet resultSet, final ResultSetPredicate resultSetPredicate) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or closed");
        }

        if(resultSetPredicate == null)
        {
            throw new IllegalArgumentException("Predicate may not be null");
        }

        while(resultSet.next())
        {
            if(resultSetPredicate.apply(resultSet))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@link ArrayList} of the type of the input consisting of the
     * results of applying the operations in {@link ResultSetFunction} on the
     * given {@link ResultSet}
     *
     * @param resultSet
     *      The result set consisting of the elements
     * @param resultSetFunction
     *      Maps the given {@link ResultSet} elements to another type
     * @return
     *      An {@link ArrayList} of the type of the input that are the results
     *      of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> ArrayList<T> map(final ResultSet resultSet, final ResultSetFunction<T> resultSetFunction) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or closed");
        }

        if(resultSetFunction == null)
        {
            throw new IllegalArgumentException("Result set function may not be null");
        }

        final ArrayList<T> results = new ArrayList<T>();

        while(resultSet.next())
        {
            results.add(resultSetFunction.apply(resultSet));
        }

        return results;
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
     * @return
     *      An {@link ArrayList} of the type of the input that are the results
     *      of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *             if mapping function throws
     */
    public static <T> ArrayList<T> mapFilter(final ResultSet            resultSet,
                                             final ResultSetFunction<T> function,
                                             final Predicate<T>         predicate) throws SQLException
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

        final ArrayList<T> results = new ArrayList<T>();

        while(resultSet.next())
        {
            final T mappedValue = function.apply(resultSet);

            if(predicate.apply(mappedValue))
            {
                results.add(mappedValue);
            }
        }

        return results;
    }
}
