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
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Luke Lambert
 *
 */
public final class JdbcUtility
{
    /**
     * Disabled constructor
     */
    private JdbcUtility()
    {

    }

    /**
     * Selects and returns one result.  Null is returned if the query returns
     * no result.
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of a {@link
     *             PreparedStatement}. Ignored if null.
     * @param resultMapper
     *             Callback that accepts a {@link ResultSet} to create another
     *             object
     * @return Instance of T that corresponds to the singular result of the
     *             query. Null is returned if the query returned no results.
     * @throws SQLException
     *             if there is a database error
     */
    public static <T> T selectOne(final Connection                databaseConnection,
                                  final String                    sql,
                                  final PreparedStatementConsumer parameterSetter,
                                  final ResultSetFunction<T>      resultMapper) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(resultMapper == null)
        {
            throw new IllegalArgumentException("Mapping callback for the result set may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                if(resultSet.next())
                {
                    return resultMapper.apply(resultSet);
                }

                return null;
            }
        }
    }

    /**
     * Returns an instance of T per result of the query. If the query produces
     * no results, an empty collection is returned.
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of a {@link
     *             PreparedStatement}. Ignored if null.
     * @param resultMapper
     *             Callback that accepts a {@link ResultSet} to create another
     *             object
     * @return Instance of T per result of the query. If the query produces
     *             no results, an empty collection is returned.
     * @throws SQLException
     *             if there is a database error
     */
    public static <T> List<T> select(final Connection                databaseConnection,
                                     final String                    sql,
                                     final PreparedStatementConsumer parameterSetter,
                                     final ResultSetFunction<T>      resultMapper) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(resultMapper == null)
        {
            throw new IllegalArgumentException("Mapping callback for the result set may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                final List<T> results = new ArrayList<>();

                while(resultSet.next())
                {
                    results.add(resultMapper.apply(resultSet));
                }

                return results;
            }
        }
    }

    /**
     * Applies an operation on every result of a query
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of a {@link
     *             PreparedStatement}. Ignored if null.
     * @param resultConsumer
     *             Callback that is called for every result of a query
     * @throws SQLException
     *             if there is a database error
     */
    public static void forEach(final Connection                databaseConnection,
                               final String                    sql,
                               final PreparedStatementConsumer parameterSetter,
                               final ResultSetConsumer         resultConsumer) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(resultConsumer == null)
        {
            throw new IllegalArgumentException("Consumer callback for the result set may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                while(resultSet.next())
                {
                    resultConsumer.accept(resultSet);
                }
            }
        }
    }

    /**
     * Applies a database update
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @throws SQLException
     *             if there is a database error
     */
    public static void update(final Connection databaseConnection, final String sql) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        try(final Statement statement = databaseConnection.createStatement())
        {
            statement.executeUpdate(sql);
        }
        catch(final Throwable th)
        {
            databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Applies a database update
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of a {@link
     *             PreparedStatement}. Ignored if null.
     * @throws SQLException
     *             if there is a database error
     */
    public static void update(final Connection                databaseConnection,
                              final String                    sql,
                              final PreparedStatementConsumer parameterSetter) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            preparedStatement.executeUpdate();
        }
        catch(final Throwable th)
        {
            databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Applies a database update, and returns an object that represents the
     * keys that were automatically generated.  See {@link PreparedStatement#
     * executeUpdate(String sql, int autoGeneratedKeys)} for more detail on
     * keys produced by updates.
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of a {@link
     *             PreparedStatement}. Ignored if null.
     * @param keysMapper
     *             Callback that maps the {@link ResultSet} returned by {@link
     *             PreparedStatement#getGeneratedKeys()} to an object that
     *             represents the key(s)
     * @return Object that represents the auto-generated key(s)
     * @throws SQLException
     *             if there is a database error
     */
    public static <T> T update(final Connection                databaseConnection,
                               final String                    sql,
                               final PreparedStatementConsumer parameterSetter,
                               final ResultSetFunction<T>      keysMapper) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(keysMapper == null)
        {
            throw new IllegalArgumentException("Key mapping callback may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            preparedStatement.executeUpdate();

            try(final ResultSet resultKeys = preparedStatement.getGeneratedKeys())
            {
                return keysMapper.apply(resultKeys);
            }
        }
        catch(final Throwable th)
        {
            databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Applies database updates.  {@link PreparedStatement#executeUpdate()} is
     * called for each value supplied.
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param values
     *             Objects used to set the parameters to successive calls to
     *             {@link PreparedStatement#executeUpdate()}
     * @param parameterSetter
     *             Callback that sets parameters of the {@link PreparedStatement}
     * @throws SQLException
     *             if there is a database error
     */
    public static <T> void update(final Connection                     databaseConnection,
                                  final String                         sql,
                                  final Iterable<T>                    values,
                                  final PreparedStatementBiConsumer<T> parameterSetter) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(values == null)
        {
            throw new IllegalArgumentException("Collection of values may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
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
        catch(final Throwable th)
        {
            databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Accumulates results from a query.
     *
     * @param databaseConnection
     *             Connection to the database
     * @param sql
     *             SQL query
     * @param parameterSetter
     *             Callback that sets parameters of the {@link PreparedStatement}
     * @param initialValue
     *             A starting value for the accumulation
     * @param resultFunction
     *             Maps a result set to an instance of T
     * @param combiner
     *             Combines two instances of T
     * @return the accumulation of all results
     * @throws SQLException
     *             if there is a database error
     */
    public static <T> T accumulate(final Connection                databaseConnection,
                                   final String                    sql,
                                   final PreparedStatementConsumer parameterSetter,
                                   final T                         initialValue,
                                   final ResultSetFunction<T>      resultFunction,
                                   final BinaryOperator<T>         combiner) throws SQLException
    {
        if(databaseConnection == null)
        {
            throw new IllegalArgumentException("Database connection may not be null");
        }

        if(sql == null || sql.isEmpty())
        {
            throw new IllegalArgumentException("Query statement may not be null or empty");
        }

        if(resultFunction == null)
        {
            throw new IllegalArgumentException("Result function may not be null");
        }

        if(combiner == null)
        {
            throw new IllegalArgumentException("Combiner may not be null");
        }

        try(final PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql))
        {
            if(parameterSetter != null)
            {
                parameterSetter.accept(preparedStatement);
            }

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                T value = initialValue;

                while(resultSet.next())
                {
                    final T currentValue = resultFunction.apply(resultSet);

                    value = combiner.apply(value, currentValue);
                }

                return value;
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
