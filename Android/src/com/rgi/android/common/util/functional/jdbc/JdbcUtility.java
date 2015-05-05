package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Luke Lambert
 *
 */
public class JdbcUtility
{
    /**
     * Returns {@Link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@Link ResultSetPredicate} on the given {@Link ResultSet}
     *
     * @param resultSet
     *      The result set containing the elements
     * @param resultSetPredicate
     *      Evaluates each element in the given {@Link ResultSet} to see if it satisfies the predicate
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
     *  Returns {@Link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@Link ResultSetMapper} on the given {@Link ResultSet}
     *
     * @param resultSet
     *      The result set consisting of the elements
     * @param resultSetMapper
     *      Maps the given {@Link ResultSet} elements and outputs to the same type of input based on the apply function
     * @return
     *      An {@Link ArrayList} of the type of the input that are the results of the mapping the elements in the given {@Link ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> ArrayList<T> map(final ResultSet resultSet, final ResultSetFunction<T> resultSetMapper) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or close");
        }

        if(resultSetMapper == null)
        {
            throw new IllegalArgumentException("Result set mapper may not be null");
        }

        final ArrayList<T> results = new ArrayList<T>();

        while(resultSet.next())
        {
            results.add(resultSetMapper.apply(resultSet));
        }

        return results;
    }
}
