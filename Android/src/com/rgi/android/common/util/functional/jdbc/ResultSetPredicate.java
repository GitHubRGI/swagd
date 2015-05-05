package com.rgi.android.common.util.functional.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetPredicate
{
    public boolean apply(final ResultSet resultSet) throws SQLException;
}
