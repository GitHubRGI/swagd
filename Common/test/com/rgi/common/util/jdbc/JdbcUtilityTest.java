package com.rgi.common.util.jdbc;

import com.mockrunner.mock.jdbc.MockConnection;
import com.rgi.common.Pair;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.sql.*;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;


/**
 *
 * Created by justin.rhee on 10/29/2015.
 */
public class JdbcUtilityTest {

    private static final String TEST_TABLE_NAME = "tiles";

    private final Random randomGenerator = new Random();


    //This portion tests the first SelectOne function block
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullConectionTest() throws SQLException {
        final boolean result = JdbcUtility.selectOne(null,
                "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;",
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("selectOne should have thrown an IllegalArgumentException for a null Connection.");
    }


    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullStringTest() throws Exception {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();

        final Connection con = new MockConnection();
        final boolean result = JdbcUtility.selectOne(con,
                null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("selectOne should have thrown an IllegalArgumentException for a null or empty String.");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullResultSetFunctionTest() throws Exception {
        final File testFile = this.getRandomFile(2);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.selectOne(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                null);
        fail("selectOne should have thrown an IllegalArgumentException for a null resultMapper.");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = AssertionError.class)
    public void selectOneTryStatementTest() throws Exception {
        final File testFile = this.getRandomFile(2);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        final String str = "This is not a sql command";

        JdbcUtility.selectOne(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1));

        fail("selectOne should have a sql command in the String str");
    }







    //This portion tests the first update function block
    @Test(expected = IllegalArgumentException.class)
    public void update1NullDatabaseConnectionTest() throws Exception
    {
        final File testFile = this.getRandomFile(2);
        testFile.createNewFile();

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        JdbcUtility.update(null, str);
        fail("update should have thrown an IllegalArgumentException for a null connection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update1NullStringTest() throws Exception
    {
        final File testFile = this.getRandomFile(5);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        JdbcUtility.update(con, null);
        fail("update should have thrown an IllegalArgumentException for a null string");
    }



    @Test(expected = AssertionError.class)
    public void update1PreparedStatementCatchTest() throws Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        final Connection con = new MockConnection();

        final String str = "Hello! what is up my mans.";

        JdbcUtility.update(con, str);
        fail("update should return a sql string");
    }


//    //TODO find a way to test once the method runs through completely
//    @Test(expected = AssertionError.class)
//    public void update1tryExecuteTest() throws Exception
//    {
//        final File testFile = this.getRandomFile(7);
//        testFile.createNewFile();
//        final Connection con = new MockConnection();
//
//        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";
//
//
//        JdbcUtility.update(con, str);
//        assertTrue("you done fucked up", );
//    }
//







    //this portion tests the second update function block
    @Test(expected = IllegalArgumentException.class)
    public void update2NullDatabaseConnectTest() throws Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.update(null, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("update should have thrown an IllegalArgumentException for a null Connection");
    }


    @Test(expected = IllegalArgumentException.class)
    public void update2NullStringTest() throws Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        final boolean result = JdbcUtility.update(con, null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("update should have thrown an IllegalArgumentException for a null Connection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update2NullKeyMapperTest() throws Exception
    {
        final File testFile = this.getRandomFile(8);
        testFile.createNewFile();

        final Connection con = new MockConnection();
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.update(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                null);
        fail("update should have thrown an illegalArgumentException for a null resultMapper");
    }




    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(FileSystems.getDefault().getPath(this.getRandomString(length)) + ".gpkg");
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

}
























