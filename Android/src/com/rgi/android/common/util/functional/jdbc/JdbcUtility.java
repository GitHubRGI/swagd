package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;

/**
 * @author Luke Lambert
 *
 */
public class JdbcUtility
{
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
            throw new IllegalArgumentException("Result set may not be null or close");
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
     *  Returns {@link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@link ResultSetFunction} on the given {@link ResultSet}
     *
     * @param resultSet
     *      The result set consisting of the elements
     * @param resultSetFunction
     *      Maps the given {@link ResultSet} elements and outputs to the same type of input based on the apply function
     * @return
     *      An {@link ArrayList} of the type of the input that are the results of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> ArrayList<T> map(final ResultSet resultSet, final ResultSetFunction<T> resultSetFunction) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or close");
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

    public static <T> ArrayList<T> mapFilter(final ResultSet               resultSet,
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

        return FunctionalUtility.filter(JdbcUtility.map(resultSet, function), predicate);
    }
}
