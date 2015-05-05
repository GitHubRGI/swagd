package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Luke Lambert
 *
 * @param <T> the type of the input to the operation
 */
public interface ResultSetMapper<T>
{
    /**
     * The function that performs an operation on the given {@Link ResultSet}
     * and returns the type of the input to the operation after operation is applied
     *
     * @param resultSet
     *      the {@Link ResultSet} to perform the operations on
     * @return
     *      the type of the input to the operation after applying the operation
     * @throws SQLException
     *      throws if an SQLException occurs
     */
    public T apply(final ResultSet resultSet) throws SQLException;
}
