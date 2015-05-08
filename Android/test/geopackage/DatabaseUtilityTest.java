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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.swing.filechooser.FileSystemView;

import org.junit.Test;

import com.rgi.android.geopackage.utility.DatabaseUtility;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class DatabaseUtilityTest
{
    private final Random randomGenerator = new Random();

    /**
     * Tests if the DatabaseUtility will return the expected application Id.
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws Exception
     *             can throw an SecurityException when accessing the file and
     *             other various Exceptions
     */
    @Test
    public void getApplicationID() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        Connection con = this.getConnection(testFile.getAbsolutePath());
        try
        {
            final int appId = DatabaseUtility.getApplicationId(con);
            assertTrue("DatabaseUtility did not return the expected application Id.",appId == 0);
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
        }
    }



    /**
     * Tests if the application Id can be set correctly through the
     * DatabaseUtility
     *
     * @throws SQLException
     *             throws if an SQLException occurs
     * @throws Exception
     *             throws if cannot access file
     */
    @Test
    public void setApplicationID() throws SQLException, Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        Connection con = this.getConnection(testFile.getAbsolutePath());
        try
        {
            DatabaseUtility.setApplicationId(con, 12345);
            assertTrue("DatabaseUtility did not return the expected application Id.", DatabaseUtility.getApplicationId(con) == 12345);
        }
        finally
        {
            this.deleteFile(testFile);
        }
    }

    /**
     * Verifies if the Database BoundsUtility setPragmaForeinKeys can set it to off.
     *
     * @throws Exception throws when an Exception occurs
     */
    @Test
    public void databaseUtilitySetPragmaForiegnKeys() throws Exception
    {
        final File testFile = this.getRandomFile(5);
        testFile.createNewFile();
        Connection con = this.getConnection(testFile.getAbsolutePath());

        try
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, false);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";
            Statement stmt     = con.createStatement();

            try
            {
                ResultSet fkPragma = stmt.executeQuery(query);

                try
                {
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
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(5);
        testFile.createNewFile();
        Connection con = this.getConnection(testFile.getAbsolutePath());

        try
        {
            //set it false using database utility
            DatabaseUtility.setPragmaForeignKeys(con, true);
            //pragma the database
            final String query = "PRAGMA foreign_keys;";
            Statement stmt     = con.createStatement();

            try
            {
                ResultSet fkPragma = stmt.executeQuery(query);

                try
                {
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
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(12);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            final boolean tableFound = DatabaseUtility.tableOrViewExists(con, "non_existant_table");
            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", !tableFound);
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        final String tableName = "gpkg_tile_matrix";
        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            this.addTable(con, tableName);
            assertTrue("The Database BoundsUtility method table or view exists method returned false when it should have returned true.", DatabaseUtility.tableOrViewExists(con, tableName));
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
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
    public void databaseUtilityTableorViewExists3() throws Exception
    {
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            final boolean tableFound = DatabaseUtility.tableOrViewExists(con, null);
            assertTrue("The Database BoundsUtility method table or view exists method returned true when it should have returned false.", !tableFound);
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            con.close();
            DatabaseUtility.tableOrViewExists(con, null);
            fail("Database BoundsUtility should have thrown an IllegalArgumentException when given a closed connection.");
        }
        finally
        {
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

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
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

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
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

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
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            DatabaseUtility.getSqliteVersion(testFile);
            fail("Expected an IllegalArgumentException from DatabaseUtility when gave an empty file to getSqliteVersion");
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
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
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();
        Connection con  = this.getConnection(testFile.getAbsolutePath());

        try
        {
            this.addTable(con, "foo");
            final String sqliteVersion =  DatabaseUtility.getSqliteVersion(testFile);
            assertTrue(String.format("The SQLite Version was different from expected. Expected: %s, Actual: %s",
                                     geopackageSqliteVersion, sqliteVersion),
                       geopackageSqliteVersion.equals(sqliteVersion));
        }
        finally
        {
            con.close();
            this.deleteFile(testFile);
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
            String filename = FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() + "/" +  this.getRandomString(length) + ".gpkg";
            testFile = new File(filename);
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

    private void deleteFile(final File testFile)
    {
        if(testFile.exists())
        {
            testFile.delete();
        }
    }

    private void addTable(final Connection con, final String tableName) throws Exception
    {
        Statement statement = con.createStatement();
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
    private final static String geopackageSqliteVersion = "3.8.7";

}
