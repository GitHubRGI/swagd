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

package com.rgi.test.geopackage;

import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.utility.DatabaseVersion;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("JavaDoc")
public class DatabaseUtilityTest
{
    @BeforeClass
    public static void setUp() throws ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver
    }

    /**
     * Tests if the DatabaseUtility will return the expected application Id.
     */
    @Test
    public void getApplicationID() throws SQLException, IOException, ClassNotFoundException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();
        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final int appId = DatabaseUtility.getApplicationId(con);
            assertEquals("DatabaseUtility did not return the expected application Id.", 0, appId);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the application Id can be set correctly through the
     * DatabaseUtility
     */
    @Test
    public void setApplicationID() throws SQLException, IOException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();
        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            DatabaseUtility.setApplicationId(con, 12345);
            assertEquals("DatabaseUtility did not return the expected application Id.", 12345, DatabaseUtility.getApplicationId(con));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Verifies if the Database BoundsUtility setPragmaForeignKeys can set it to off.
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, false);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";

            try(final Statement stmt     = con.createStatement();
                final ResultSet fkPragma = stmt.executeQuery(query))
            {
                final int off = fkPragma.getInt("foreign_keys");
                assertEquals("Database BoundsUtility set pragma foreign keys didn't set the foreign_keys to off when given the parameter false.", 0, off);
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Verifies if the Database BoundsUtility setPragmaForeignKeys can set it to on.
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys2() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, true);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";

            try(final Statement stmt     = con.createStatement();
                ResultSet fkPragma = stmt.executeQuery(query))
            {
                final int on = fkPragma.getInt("foreign_keys");
                assertEquals("Database BoundsUtility set pragma foreign keys didn't set the foreign_keys to on when given the parameter true.", 1, on);
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    @Test
    public void databaseUtilitySetPragmaSynchronousOff() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();
        try(Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            DatabaseUtility.setPragmaSynchronousOff(con);
            try(final Statement stmt = con.createStatement())
            {
                final String query = "PRAGMA synchronous;";
                try(ResultSet sPragma = stmt.executeQuery(query))
                {
                    final int sync = sPragma.getInt("synchronous");
                    assertEquals("DatabaseUtility did not set PRAGMA synchronous to off.", 0, sync);
                }
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does not exists with the tableOrViewExists method.
     */
    @Test
    public void databaseUtilityTableorViewExists() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final boolean tableNotFound = !DatabaseUtility.tableOrViewExists(con, "non_existant_table");
            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", tableNotFound);
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tableOrViewExists method.
     */
    @Test
    public void databaseUtilityTableorViewExists2() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final String tableName = "gpkg_tile_matrix";
            DatabaseUtilityTest.addTable(con, tableName);
            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tableOrViewExists(con, tableName));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tableOrViewExists method.
     */
    @Test(expected= IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists3() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            DatabaseUtility.tableOrViewExists(con, null);
            fail("DatabaseUtility should have thrown an IllegalArgumentException when tablename was null or empty");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an
     * IllegalArgumentException when given a null connection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists4() throws SQLException
    {
        DatabaseUtility.tableOrViewExists(null, null);
        fail("DatabaseUtility should have thrown an IllegalArgumentException when connection is null.");
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an
     * IllegalArgumentException when given a closed connection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists5() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            con.close();
            DatabaseUtility.tableOrViewExists(con, null);
            fail("Database BoundsUtility should have thrown an IllegalArgumentException when given a closed connection.");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tablesOrViewsExists method.
     */
    @Test
    public void databaseUtilityTablesorViewsExists() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final String tableName = "gpkg_tile_matrix";
            DatabaseUtilityTest.addTable(con, tableName);
            final String[] tables = {tableName, "non_existant_table"};

            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", !DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tablesOrViewsExists method.
     */
    @Test
    public void databaseUtilityTablesorViewsExists2() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final String tableName = "gpkg_tile_matrix";
            DatabaseUtilityTest.addTable(con, tableName);
            final String[] tables = {tableName, tableName};
            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tablesOrViewsExists method.
     */
    @Test
    public void databaseUtilityTablesorViewsExists3() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            final String tableName1 = "gpkg_tile_matrix";

            DatabaseUtilityTest.addTable(con, tableName1);
            final String tableName2 = "gpkg_contents";
            DatabaseUtilityTest.addTable(con, tableName2);
            final String[] tables = {tableName1, tableName2};

            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an exception when
     * receiving a file that is less than 100 bytes.
     */
    @Test(expected= IllegalArgumentException.class)
    public void getSqliteVersion() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            DatabaseUtility.getSqliteVersion(testFile);
            fail("Expected an IllegalArgumentException from DatabaseUtility when gave an empty file to getSqliteVersion");
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility gets correct sqlite version of a
     * file.
     */
    @Test
    public void getSqliteVersion2() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();
        testFile.createNewFile();

        try(final Connection con = DatabaseUtilityTest.getConnection(testFile))
        {
            DatabaseUtilityTest.addTable(con, "foo");
            final DatabaseVersion foundSqliteVersion = DatabaseUtility.getSqliteVersion(testFile);
            assertEquals(String.format("The SQLite Version was different from expected. Expected: %s.x, Actual: %s",
                                       sqliteMajorVersion,
                                       foundSqliteVersion),
                         sqliteMajorVersion,
                         foundSqliteVersion.getMajor());
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an exception when
     * receiving a file that is null.
     */
    @Test(expected= IllegalArgumentException.class)
    public void getSqliteVersion3() throws IOException
    {
        DatabaseUtility.getSqliteVersion(null);
        fail("Expected an IllegalArgumentException from DatabaseUtility when gave file that was null to getSqliteVersion");
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an exception when
     * receiving a file that is null.
     */
    @Test(expected= FileNotFoundException.class)
    public void getSqliteVersion4() throws IOException
    {
        DatabaseUtility.getSqliteVersion(TestUtility.getRandomFile());
        fail("Expected an IllegalArgumentException from DatabaseUtility when gave file that does not exist to getSqliteVersion");
    }

    private static Connection getConnection(final File file) throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
    }

    @SuppressWarnings("JDBCExecuteWithNonConstantString")
    private static void addTable(final Connection con, final String tableName) throws SQLException
    {
        try(final Statement statement = con.createStatement())
        {
            statement.executeUpdate("CREATE TABLE " + tableName + " (foo INTEGER);");
        }
    }

    private static final int sqliteMajorVersion = 3;
}
