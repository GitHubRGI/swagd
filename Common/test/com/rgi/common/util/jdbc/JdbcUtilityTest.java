package com.rgi.common.util.jdbc;

import com.mockrunner.mock.jdbc.MockConnection;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;



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


    //TODO fix the tests for preparedStatement section
//    @SuppressWarnings("ConstantConditions")
//    @Test
//    public void selectOnePreparedStatementTest() throws Exception {
//        final File testFile = this.getRandomFile(6);
//        testFile.createNewFile();
//
//        final Connection con = new MockConnection();
//
//        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";
//
//        final boolean result = JdbcUtility.selectOne(con, str,
//                preparedStatement -> preparedStatement.setString(1, "tiles"),
//                resultSet -> resultSet.getInt(1)) > 0;
//
//        fail("selectOne should have thrown an IllegalArgumentException for a null resultMapper.");
//
//    }


















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
