package com.rgi.common.util.jdbc;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import java.io.File;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.Assert.fail;

/**
 * Created by justin.rhee on 10/29/2015.
 */
public class JdbcUtilityTest {

    private static final String TEST_TABLE_NAME = "tiles";

    private final Random randomGenerator = new Random();

    private final File testFile = this.getRandomFile(5);


    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullConectionTest() throws SQLException
    {
        final boolean result = JdbcUtility.selectOne(null,
                "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;",
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("selectOne should have thrown an IllegalArgumentException for a null Connection.");
    }



    //TODO find out what to put for database connection
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullStringTest() throws Exception
    {
        final Connection con = getConnection(this.testFile.getAbsolutePath());
        final boolean result = JdbcUtility.selectOne(con,
                null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("selectOne should have thrown an IllegalArgumentException for a null or empty String.");
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

    private static Connection getConnection(final String filePath) throws Exception
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }



}
