package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper<T>
{
    public T apply(final ResultSet resultSet) throws SQLException;
}
