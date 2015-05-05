package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Luke Lambert
 *
 */
public interface ResultSetPredicate
{
    /**
     * Returns true if the given {@Link ResultSet} satisfies the predicate, false otherwise
     *
     * @param resultSet
     *      The result set containing the elements
     * @return
     *      Returns true if the given {@Link ResultSet} satisfies the predicate, false otherwise
     *
     * @throws SQLException
     *      Throws if an SQLException occurs
     */
    public boolean apply(final ResultSet resultSet) throws SQLException;
}
