package com.rgi.common.util.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Luke Lambert
 *
 */
public class JdbcUtility
{
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
}
