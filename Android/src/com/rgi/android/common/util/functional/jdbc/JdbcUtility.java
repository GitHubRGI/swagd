package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JdbcUtility
{
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

    public static <T> ArrayList<T> map(final ResultSet resultSet, final ResultSetMapper<T> resultSetMapper) throws SQLException
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
