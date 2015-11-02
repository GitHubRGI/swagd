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
    public void updatePreparedStatementCatchTest() throws Exception
    {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();
        final Connection con = new MockConnection();

        final String str = "Hello! what is up my mans.";

        JdbcUtility.update(con, str);
        fail("update should return a sql string");
    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void tryExecuteUpdateTest() throws Exception
//    {
//        final File testFile = this.getRandomFile(7);
//        testFile.createNewFile();
//        final Connection con = new MockConnection();
//
//        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";
//
//        try(final Statement statement = con.createStatement())
//        {
//            statement.executeUpdate(str);
//        }
//
//    }
//

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
























