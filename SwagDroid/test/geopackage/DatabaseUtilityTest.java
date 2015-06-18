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

package geopackage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import utility.TestUtility;

import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.utility.DatabaseVersion;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
@RunWith(RobolectricTestRunner.class)
public class DatabaseUtilityTest
{
    /**
     * Tests if the DatabaseUtility will return the expected application Id.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws Exception
     *             can throw an SecurityException when accessing the file and
     *             other various Exceptions
     */
    // TODO The current version of SQLite being used doesn't support app id
//    @Test
//    public void getApplicationID() throws SQLException, Exception
//    {
//        final File testFile = TestUtility.getRandomFile(4);
//        testFile.createNewFile();
//        final Connection con = TestUtility.getConnection(testFile);
//        try
//        {
//            final int appId = DatabaseUtility.getApplicationId(con);
//            assertTrue("DatabaseUtility did not return the expected application Id.", appId == 0);
//        }
//        finally
//        {
//            con.close();
//            TestUtility.deleteFile(testFile);
//        }
//    }

    /**
     * Tests if the application Id can be set correctly through the
     * DatabaseUtility
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws Exception
     *             throws if cannot access file
     */
    // TODO The current version of SQLite being used doesn't support app id
//    @Test
//    public void setApplicationID() throws SQLException, Exception
//    {
//        final File testFile = TestUtility.getRandomFile(4);
//        testFile.createNewFile();
//        final Connection con = TestUtility.getConnection(testFile);
//        try
//        {
//            DatabaseUtility.setApplicationId(con, 12345);
//            assertTrue("DatabaseUtility did not return the expected application Id.", DatabaseUtility.getApplicationId(con) == 12345);
//        }
//        finally
//        {
//            con.close();
//            TestUtility.deleteFile(testFile);
//        }
//    }

    /**
     * Verifies if the Database BoundsUtility setPragmaForeinKeys can set it to off.
     *
     * @throws Exception throws when an Exception occurs
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        testFile.createNewFile();
        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, false);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";
            final Statement stmt     = con.createStatement();

            try
            {
                final ResultSet fkPragma = stmt.executeQuery(query);

                try
                {
                    fkPragma.first();
                    final int off = fkPragma.getInt("foreign_keys");
                    assertTrue("Database BoundsUtility set pragma foreign keys didn't set the foreign_keys to off when given the parameter false.", off == 0);

                }
                finally
                {
                    fkPragma.close();
                }

            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Verifies if the Database BoundsUtility setPragmaForeinKeys can set it to on.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(5);
        testFile.createNewFile();
        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, true);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";
            final Statement stmt     = con.createStatement();

            try
            {
                final ResultSet fkPragma = stmt.executeQuery(query);

                try
                {
                    fkPragma.first();
                    final int on = fkPragma.getInt("foreign_keys");
                    assertTrue("Database BoundsUtility set pragma foreign keys didn't set the foreign_keys to on when given the parameter true.", on == 1);
                }
                finally
                {
                    fkPragma.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does not exists with the tableOrViewExists method.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilityTableorViewExists() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(12);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            final boolean tableFound = DatabaseUtility.tableOrViewExists(con, "non_existant_table");
            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", !tableFound);
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tableOrViewExists method.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilityTableorViewExists2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();

        final String tableName = "gpkg_tile_matrix";
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            this.addTable(con, tableName);
            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tableOrViewExists(con, tableName));
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tableOrViewExists method.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists3() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            DatabaseUtility.tableOrViewExists(con, null);
            fail("DatabaseUtility should have thrown an IllegalArgumentException when table name is null or empty.");
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an
     * IllegalArgumentException when given a null connection.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists4() throws Exception
    {
            DatabaseUtility.tableOrViewExists(null, null);
            fail("DatabaseUtility should have thrown an IllegalArgumentException when connection is null.");
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an
     * IllegalArgumentException when given a closed connection.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists5() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();

        final Connection con  = TestUtility.getConnection(testFile);

        try
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
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilityTablesorViewsExists() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            final String tableName = "gpkg_tile_matrix";
            this.addTable(con, tableName);
            final String[] tables = {tableName, "non_existant_table"};

            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", !DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tablesOrViewsExists method.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilityTablesorViewsExists2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            final String tableName = "gpkg_tile_matrix";
            this.addTable(con, tableName);
            final String[] tables = {tableName, tableName};
            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would accurately detect if a table
     * does exists with the tablesOrViewsExists method.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void databaseUtilityTablesorViewsExists3() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            final String tableName1 = "gpkg_tile_matrix";
            final String tableName2 = "gpkg_contents";

            this.addTable(con, tableName1);
            this.addTable(con, tableName2);
            final String[] tables = {tableName1, tableName2};

            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an exception when
     * receiving a file that is less than 100 bytes.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test(expected= IllegalArgumentException.class)
    public void getSqliteVersion() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            DatabaseUtility.getSqliteVersion(testFile);
            fail("Expected an IllegalArgumentException from DatabaseUtility when gave an empty file to getSqliteVersion");
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility gets correct sqlite version of a
     * file.
     *
     * @throws Exception
     *             throws when an Exception occurs
     */
    @Test
    public void getSqliteVersion2() throws Exception
    {
        final File testFile = TestUtility.getRandomFile(3);
        testFile.createNewFile();
        final Connection con  = TestUtility.getConnection(testFile);

        try
        {
            this.addTable(con, "foo");
            final DatabaseVersion sqliteVersion =  DatabaseUtility.getSqliteVersion(testFile);
            assertTrue(String.format("The SQLite Version was different from expected. Expected: %d.x, Actual: %s",
                                     geoPackageSqliteMajorVersion,
                                     sqliteVersion.toString()),
                       sqliteVersion.getMajor() == geoPackageSqliteMajorVersion);
        }
        finally
        {
            con.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Checks to see if the Database BoundsUtility would throw an exception when
     * receiving a file that is null.
     *
     * @throws IOException
     *             throws when DatabaseUtility cannot read sqliteVersion from a
     *             file
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
     *
     * @throws IOException
     *             throws when DatabaseUtility cannot read sqliteVersion from a
     *             file
     */
    @Test(expected= FileNotFoundException.class)
    public void getSqliteVersion4() throws IOException
    {
        DatabaseUtility.getSqliteVersion(TestUtility.getRandomFile(4));
        fail("Expected an IllegalArgumentException from DatabaseUtility when gave file that does not exist to getSqliteVersion");
    }

    private void addTable(final Connection con, final String tableName) throws Exception
    {
        final Statement statement = con.createStatement();
        try
        {
            statement.executeUpdate("CREATE TABLE " + tableName + " (foo INTEGER);");
        }
        finally
        {
            statement.close();
        }
    }

    /**
     * The Sqlite version required for a GeoPackage shall contain SQLite 3
     * format
     */
    private final static int geoPackageSqliteMajorVersion = 3;

}
