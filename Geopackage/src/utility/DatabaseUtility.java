/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Luke Lambert
 *
 */
public class DatabaseUtility
{
    public static String getSqliteVersion(final File sqliteFile) throws IOException
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

            return String.format("%d.%d.%d",
                                 major,
                                 minor,
                                 revision);
        }
    }

    public static boolean tableOrViewExists(final Connection connection, final String name) throws SQLException
    {
        DatabaseUtility.verify(connection);

        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;"))
        {
            preparedStatement.setString(1, name);

            return preparedStatement.executeQuery().getInt(1) > 0;
        }
    }

    public static boolean tablesOrViewsExists(final Connection connection, final String... names) throws SQLException
    {
        DatabaseUtility.verify(connection);

        final Set<String> uniqueNames = new HashSet<>(Arrays.asList(names));

        try(PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT COUNT(*) AS count FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name IN (%s);",
                                                                                            String.join(", ", Collections.nCopies(uniqueNames.size(), "?")))))
        {
            int index = 1;
            for(final String name : uniqueNames)
            {
                preparedStatement.setString(index++, name);
            }

            return preparedStatement.executeQuery().getInt("count") == uniqueNames.size();
        }
    }

    public static void setApplicationId(final Connection connection, final int applicationId) throws SQLException
    {
        DatabaseUtility.verify(connection);

        try(Statement statement = connection.createStatement())
        {
            statement.execute(String.format("PRAGMA application_id = %d;",
                                            applicationId));
        }
    }

    public static int getApplicationId(final Connection connection) throws SQLException
    {
        DatabaseUtility.verify(connection);

        try(Statement statement = connection.createStatement())
        {
            final String sql = "PRAGMA application_id;";
            try(ResultSet rs = statement.executeQuery(sql))
            {
                return rs.getInt("application_id");
            }
        }
    }

    public static void setPragmaForeignKeys(final Connection connection, final boolean state) throws SQLException
    {
        DatabaseUtility.verify(connection);

        try(Statement statement = connection.createStatement())
        {
            statement.execute(String.format("PRAGMA foreign_keys = %d;",
                                            (state ? 1 : 0)));
        }
    }

    private static void verify(final Connection connection) throws SQLException
    {
        if(connection == null)
        {
            throw new IllegalArgumentException("The connection cannot be null.");
        }

        if(connection.isClosed())
        {
            throw new IllegalArgumentException("The connection cannot be closed.");
        }
    }
}
