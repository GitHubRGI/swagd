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

package com.rgi.erdc.gpkg;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class DatabaseUtilityTest
{
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();

    /**
     * Tests if the DatabaseUtility will return the expected application Id.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void getApplicationID() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        try(Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            final int appId = DatabaseUtility.getApplicationId(con);
            assertTrue("DatabaseUtility did not return the expected application Id.",appId == 0);
        }
        finally
        {
            if(testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    @Test
    public void setApplicationID() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        try(Connection con = this.getConnection(testFile.getAbsolutePath()))
        {
            DatabaseUtility.setApplicationId(con, 12345);
            assertTrue("DatabaseUtility did not return the expected application Id.", DatabaseUtility.getApplicationId(con) == 12345);
        }
        finally
        {
            if(testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Verifies if the Database Utility setPragmaForeinKeys can set it to off.
     *
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(5);
             testFile.createNewFile();

        try(Connection con = this.getConnection(testFile.getAbsolutePath());)
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, false);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";

            try(Statement stmt     = con.createStatement();
                 ResultSet fkPragma = stmt.executeQuery(query);)
            {
                final int off = fkPragma.getInt("foreign_keys");
                assertTrue("Database Utility set pragma foreign keys didn't set the foreign_keys to off when given the parameter false.", off == 0);
            }
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Verifies if the Database Utility setPragmaForeinKeys can set it to on.
     *
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys2() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(5);
             testFile.createNewFile();

        try(Connection con = this.getConnection(testFile.getAbsolutePath());)
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, true);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";

            try(Statement stmt     = con.createStatement();
                ResultSet fkPragma = stmt.executeQuery(query);)
            {
                final int on = fkPragma.getInt("foreign_keys");
                assertTrue("Database Utility set pragma foreign keys didn't set the foreign_keys to on when given the parameter true.", on == 1);
            }
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does not exists with the tableOrViewExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTableorViewExists() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(12);
        testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            final boolean tableFound = DatabaseUtility.tableOrViewExists(con, "non_existant_table");
            assertTrue("The Database Utility method table or view exists method returned true when it should have returned false.", !tableFound);
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does exists with the tableOrViewExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTableorViewExists2() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        final String tableName = "gpkg_tile_matrix";

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            this.addTable(con, tableName);
            assertTrue("The Database Utility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tableOrViewExists(con, tableName));
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does exists with the tableOrViewExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTableorViewExists3() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
             testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath());
            )
        {
            final boolean tableFound = DatabaseUtility.tableOrViewExists(con, null);
            assertTrue("The Database Utility method table or view exists method returned true when it should have returned false.", !tableFound);
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would throw an IllegalArgumentException when given a null connection.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists4() throws SQLException, Exception
    {
            DatabaseUtility.tableOrViewExists(null, null);
            fail("DatabaseUtility should have thrown an IllegalArgumentException when connection is null.");
    }

    /**
     * Checks to see if the Database Utility would throw an IllegalArgumentException when given a closed connection.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void databaseUtilityTableorViewExists5() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
             testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath());
            )
        {
            con.close();
            DatabaseUtility.tableOrViewExists(con, null);
            fail("Database Utility should have thrown an IllegalArgumentException when given a closed connection.");
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does exists with the tablesOrViewsExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTablesorViewsExists() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
             testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            final String tableName = "gpkg_tile_matrix";
            this.addTable(con, tableName);
            final String[] tables = {tableName, "non_existant_table"};

            assertTrue("The Database Utility method table or view exists method returned true when it should have returned false.", !DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does exists with the tablesOrViewsExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTablesorViewsExists2() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
             testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            final String tableName = "gpkg_tile_matrix";
            this.addTable(con, tableName);
            final String[] tables = {tableName, tableName};
            assertTrue("The Database Utility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would accurately detect if a table does exists with the tablesOrViewsExists method.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void databaseUtilityTablesorViewsExists3() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
             testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            final String tableName1 = "gpkg_tile_matrix";
            final String tableName2 = "gpkg_contents";

            this.addTable(con, tableName1);
            this.addTable(con, tableName2);
            final String[] tables = {tableName1, tableName2};

            assertTrue("The Database Utility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tablesOrViewsExists(con, tables));
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would throw an exception when receiving a file that is less than 100 bytes.
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected= IllegalArgumentException.class)
    public void getSqliteVersion() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath());
            )
        {
            DatabaseUtility.getSqliteVersion(testFile);
            fail("Expected an IllegalArgumentException from DatabaseUtility when gave an empty file to getSqliteVersion");
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility gets correct sqlite version of a file.
     * @throws SQLException
     * @throws Exception
     */
    @Test
    public void getSqliteVersion2() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        try(Connection con  = this.getConnection(testFile.getAbsolutePath()))
        {
            this.addTable(con, "foo");
            final String sqliteVersion =  DatabaseUtility.getSqliteVersion(testFile);
            assertTrue(String.format("The SQLite Version was different from expected. Expected: %s, Actual: %s",
                                     geopackageSqliteVersion, sqliteVersion),
                       geopackageSqliteVersion.equals(sqliteVersion));
        }
        finally
        {
            if (testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Checks to see if the Database Utility would throw an exception when receiving a file that is null.
     * @throws IOException
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected= IllegalArgumentException.class)
    public void getSqliteVersion3() throws IOException
    {
        DatabaseUtility.getSqliteVersion(null);
        fail("Expected an IllegalArgumentException from DatabaseUtility when gave file that was null to getSqliteVersion");
    }

    /**
     * Checks to see if the Database Utility would throw an exception when receiving a file that is null.
     * @throws IOException
     * @throws SQLException
     * @throws Exception
     */
    @Test(expected= FileNotFoundException.class)
    public void getSqliteVersion4() throws IOException
    {
        DatabaseUtility.getSqliteVersion(this.getRandomFile(4));
        fail("Expected an IllegalArgumentException from DatabaseUtility when gave file that does not exist to getSqliteVersion");
    }

    private Connection getConnection(final String filePath) throws Exception
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }
    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(this.getRandomString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }
    private String getRandomString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private void addTable(final Connection con, final String tableName) throws Exception
    {
        try(Statement statement = con.createStatement())
        {
            statement.executeUpdate("CREATE TABLE " + tableName + " (foo INTEGER);");
        }
    }

    /**
     * The Sqlite version required for a GeoPackage shall contain SQLite 3
     * format
     */
    private final static String geopackageSqliteVersion = "3.8.7";

}
