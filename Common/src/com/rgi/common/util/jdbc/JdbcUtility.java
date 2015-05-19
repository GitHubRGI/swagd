package com.rgi.common.util.jdbc;

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
     *  Returns {@link ArrayList} of the type of the input consisting of the results of applying the
     *  operations in {@link ResultSetFunction} on the given {@link ResultSet}
     *
     * @param resultSet
     *      The result set consisting of the elements
     * @param resultSetFunction
     *      Maps the given {@link ResultSet} elements to another type
     * @return
     *      An {@link ArrayList} of the type of the input that are the results of the mapping the elements in the given {@link ResultSet}
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public static <T> ArrayList<T> map(final ResultSet            resultSet,
                                       final ResultSetFunction<T> resultSetFunction) throws SQLException
    {
        if(resultSet == null || resultSet.isClosed())
        {
            throw new IllegalArgumentException("Result set may not be null or close");
        }

        if(resultSetFunction == null)
        {
            throw new IllegalArgumentException("Result set function may not be null");
        }

        final ArrayList<T> results = new ArrayList<>();

        while(resultSet.next())
        {
            results.add(resultSetFunction.apply(resultSet));
        }

        return results;
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
            throw new IllegalArgumentException("Result set may not be null or close");
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
