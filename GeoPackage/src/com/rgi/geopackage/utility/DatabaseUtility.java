/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.geopackage.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.rgi.common.util.jdbc.JdbcUtility;

/**
 * @author Luke Lambert
 *
 */
public class DatabaseUtility
{
    /**
     * @param sqliteFile
     *            the GeoPackag file
     * @return the String representation of the sqliteVersion
     * @throws IOException
     *             throws when the FileDoes not exist or unable to seek in the
     *             File to read the SQLite Version
     */
    public static DatabaseVersion getSqliteVersion(final File sqliteFile) throws IOException
    {
        if(sqliteFile == null)
        {
            throw new IllegalArgumentException("File cannot be null");
        }

        if(!sqliteFile.exists())
        {
            throw new FileNotFoundException("File does not exist: " + sqliteFile.getPath());
        }

        if(sqliteFile.length() < 100)
        {
            throw new IllegalArgumentException("File must be at least 100 bytes to be an SQLite file.");
        }

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(sqliteFile, "r"))
        {
            // https://www.sqlite.org/fileformat2.html
            // Bytes 96 -> 100 are an int representing the sqlite version
            randomAccessFile.seek(96);
            final int version = randomAccessFile.readInt();

            // Major/minor/revision, https://www.sqlite.org/fileformat2.html
            final int major    = version/1000000;
            final int minor    = (version - (major*1000000))/1000;
            final int revision = version - ((major*1000000) + (minor*1000));

            return new DatabaseVersion(major, minor, revision);
        }
    }

    /**
     * @param connection
     *            the connection to the database
     * @param name
     *            the name of the table
     * @return true if the table or view exists in the database; otherwise
     *         returns false
     * @throws SQLException
     *             throws if unable to connect to the database or other various
     *             SQLExceptions
     */
    public static boolean tableOrViewExists(final Connection connection, final String name) throws SQLException
    {
        DatabaseUtility.verify(connection);

        if(name == null || name.isEmpty())
        {
        	throw new IllegalArgumentException("Table/view name cannot be null or empty");
        }

        return JdbcUtility.selectOne(connection,
                                     "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;",
                                     preparedStatement -> preparedStatement.setString(1, name),
                                     resultSet -> resultSet.getInt(1)) > 0;
    }

    /**
     * @param connection
     *            the connection to the database
     * @param names
     *            the names of the tables
     * @return true if All the tables or views exists in the database; otherwise
     *         returns false
     * @throws SQLException
     *             throws if unable to connect to the database or other various
     *             SQLExceptions
     */
    public static boolean tablesOrViewsExists(final Connection connection, final String... names) throws SQLException
    {
        DatabaseUtility.verify(connection);

        final Set<String> uniqueNames = new HashSet<>(Arrays.asList(names));

        return JdbcUtility.selectOne(connection,
                                     String.format("SELECT COUNT(*) AS count FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name IN (%s);",
                                                   String.join(", ", Collections.nCopies(uniqueNames.size(), "?"))),
                                     preparedStatement -> { int index = 1;

                                                            for(final String name : uniqueNames)
                                                            {
                                                                preparedStatement.setString(index++, name);
                                                            }
                                                          },
                                     resultSet -> resultSet.getInt("count")) == uniqueNames.size();
    }

    /**
     * @param connection
     *            connection to the database
     * @param applicationId
     *            the int Application ID to be set
     * @throws SQLException
     *             throws if various SQLExceptions occur
     */
    public static void setApplicationId(final Connection connection, final int applicationId) throws SQLException
    {
        DatabaseUtility.verify(connection);

        JdbcUtility.update(connection, String.format("PRAGMA application_id = %d;", applicationId));
    }

    /**
     * @param connection
     *            connection to the database
     * @return the application Id of the database
     * @throws SQLException
     *             throws if various SQLExceptions occur
     */
    public static int getApplicationId(final Connection connection) throws SQLException
    {
        DatabaseUtility.verify(connection);

        return JdbcUtility.selectOne(connection,
                                     "PRAGMA application_id;",
                                     null,
                                     resultSet -> resultSet.getInt("application_id"));
    }

    /**
     * @param connection
     *            connection to the database
     * @param state
     *            true or false whether you want foreign_keys to be set on or
     *            off
     * @throws SQLException
     *             throws if various SQLExceptions occur
     */
    public static void setPragmaForeignKeys(final Connection connection, final boolean state) throws SQLException
    {
        DatabaseUtility.verify(connection);

        JdbcUtility.update(connection, String.format("PRAGMA foreign_keys = %d;", (state ? 1 : 0)));
    }

    /**
     * @param connection
     *               connection to the database
     * @throws SQLException
     *                 throws if various SQLExceptions occur
     */
    public static void setPragmaJournalModeMemory(final Connection connection) throws SQLException
    {
        DatabaseUtility.verify(connection);

        JdbcUtility.update(connection, "PRAGMA journal_mode = MEMORY;");
    }

    /**
     * @param connection
     *               connection to the database
     * @throws SQLException
     *                 throws if various SQLExceptions occur
     */
    public static void setPragmaSynchronousOff(final Connection connection) throws SQLException
    {
        DatabaseUtility.verify(connection);

        JdbcUtility.update(connection, "PRAGMA synchronous = OFF;");
    }

    /**
     * Get the smallest value for a table and column <i>that does not yet exist
     * </i>
     *
     * @param connection
     *             connection to the database
     * @param tableName
     *             table name
     * @param columnName
     *             column name
     * @return the smallest value for a table and column that does not yet exist
     * @throws SQLException
     *             if there's a database error
     */
    @SuppressWarnings("unchecked")
    public static <T> T nextValue(final Connection connection, final String tableName, final String columnName) throws SQLException
    {
        final String smallestNonexistentValue = String.format("SELECT (table1.%1$s + 1) " +
                                                              "FROM %2$s AS table1 LEFT JOIN %2$s table2 on table2.%1$s = (table1.%1$s + 1) " +
                                                              "WHERE table2.%1$s IS NULL " +
                                                              "ORDER BY table1.%1$s " +
                                                              "LIMIT 1",
                                                              columnName,
                                                              tableName);
        return JdbcUtility.selectOne(connection,
                                     smallestNonexistentValue,
                                     null,
                                     resultSet -> (T)resultSet.getObject(1));
    }

    private static void verify(final Connection connection) throws SQLException
    {
        if(connection == null || connection.isClosed())
        {
            throw new IllegalArgumentException("The connection cannot be null or closed.");
        }
    }
}
