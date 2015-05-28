package com.rgi.common.util.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Luke Lambert
 * @param <T> Type to be tested
 *
 */
public interface JdbcPredicate<T>
{
    /**
     * Returns true if the given object satisfies the predicate, false otherwise
     *
     * @param t
     *             Object to be tested
     *
     * @return Returns true if the given {@link ResultSet} satisfies the predicate, false otherwise
     *
     * @throws SQLException
     *             Throws if an SQLException occurs
     */
    public boolean test(final T t) throws SQLException;
}

