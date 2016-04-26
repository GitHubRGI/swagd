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

package com.rgi.common.util.jdbc;

import com.mockrunner.mock.jdbc.MockConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.rgi.common.TestUtility.getConnection;
import static com.rgi.common.TestUtility.getRandomFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Justin Rhee
 * @author Luke Lambert
 */
@SuppressWarnings("JavaDoc")
public class JdbcUtilityTest
{
    private File gpkgFile;

    @Before
    public void setUp() throws IOException, URISyntaxException, ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC");
        this.gpkgFile = new File(ClassLoader.getSystemResource("testNetwork.gpkg").toURI());
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the databaseConnection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullConectionTest() throws SQLException
    {
        JdbcUtility.selectOne(null,
                              "SELECT COUNT(*) FROM sqlite_master",
                              null,
                              resultSet -> resultSet.getInt(1));

        fail("selectOne should have thrown an IllegalArgumentException for a null Connection.");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the String str is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.selectOne(connection,
                                  null,
                                  null,
                                  resultSet -> resultSet.getInt(1));
        }

        fail("selectOne should have thrown an IllegalArgumentException for a null or empty String.");
    }

    /**
     * This is a hyper-specific test for build machines that have spaces in their path
     */
    @Test
    public void openDBConnectionWithSpaces() throws IOException, URISyntaxException, SQLException
    {
        final File gpkgFile = new File(ClassLoader.getSystemResource("space test/testNetwork.gpkg").toURI());

        try(final Connection connection = getConnection(gpkgFile))
        {
            assertFalse("DB connection should not be closed.", connection.isClosed());
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown when the resultMapper is
     * null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectOneNullResultSetFunctionTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.selectOne(connection,
                                  "SELECT COUNT(*) FROM sqlite_master",
                                  null,
                                  null);
        }

        fail("selectOne should have thrown an IllegalArgumentException for a null resultMapper.");
    }

    /**
     * Tests to only run if the string is
     * a sql statement
     */
    @Test(expected = AssertionError.class)
    public void selectOneTryStatementTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.selectOne(connection,
                                  "This is not a sql command",
                                  null,
                                  resultSet -> 1);
        }

        fail("selectOne should have a sql command in the String str");
    }

    /**
     * Tests if a connection is made and a value is returned
     */
    @Test
    public void selectOneTryStatementPassTest() throws SQLException
    {
        try(final Connection connection = getConnection(this.gpkgFile))
        {
            final Integer result = JdbcUtility.selectOne(connection,
                                                         "SELECT COUNT(*) FROM sqlite_master;",
                                                         null,
                                                         resultSet -> resultSet.getInt(1));

            assertNotNull("Result returned null when it should have returned a value", result);
        }
    }

    /**
     * Tests if a MockConnection will return a null value
     */
    @Test
    public void selectOneTryStatementNullTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            final String result = JdbcUtility.selectOne(connection,
                                                        "SELECT COUNT(*) FROM sqlite_master;",
                                                        null,
                                                        resultSet -> resultSet.getString(1));

            assertNull("Result should return null", result);
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the Connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectNullConnectionTest() throws SQLException
    {
        JdbcUtility.select(null,
                           "SELECT COUNT(*) FROM sqlite_master;",
                           null,
                           resultSet -> resultSet.getInt(1));

        fail("select should have thrown an IllegalArgumentException for a null Connection.");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the query string is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectNullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.select(connection,
                               null,
                               null,
                               resultSet -> 1);
        }

        fail("select should have thrown an IllegalArgumentException for a null or empty String.");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the resultSetFunction is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void selectNullResultSetFunctionTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.select(connection,
                               "SELECT COUNT(*) FROM sqlite_master;",
                               null,
                               null);
        }

        fail("select should have thrown an IllegalArgumentException for a null resultMapper.");
    }

    /**
     * Tests if select will return a list with the size of 1
     * when connected to the gpkgFile
     */
    @Test
    public void selectTryStatementPassTest() throws SQLException
    {
        try(final Connection connection = getConnection(this.gpkgFile))
        {
            final List<Integer> result = JdbcUtility.select(connection,
                                                            "SELECT COUNT(*) FROM sqlite_master;",
                                                            null,
                                                            resultSet -> resultSet.getInt(1));

            assertEquals("result should return a size of 1", 1, result.size());
        }
    }

    /**
     * Tests if select will return an empty list if
     * connected to a MockConnection
     */
    @Test
    public void selectTryStatementNullTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            final List<Integer> result = JdbcUtility.select(connection,
                                                            "SELECT COUNT(*) FROM sqlite_master",
                                                            null,
                                                            resultSet -> 1);

            assertTrue("result should return a size of 0", result.isEmpty());
        }
    }

    /**
     * tests if an IllegalArgumentException is thrown
     * when the connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void forEachNullConnectionTest() throws SQLException
    {
        JdbcUtility.forEach(null,
                            "SELECT * FROM sqlite_master;",
                            null,
                            resultSet -> {});

        fail("select should have thrown an IllegalArgumentException for a null Connection.");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the String is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void forEachNullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.forEach(connection,
                                null,
                                null,
                                resultSet -> {});
        }

        fail("forEach should have thrown an IllegalArgumentException for a null String.");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when ResultSetConsumer is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void forEachNullResultSetConsumerFunctionTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.forEach(connection,
                                "SELECT * FROM sqlite_master;",
                                null,
                                null);
        }

        fail("forEach should have thrown an IllegalArgumentException for a null resultMapper.");
    }

    /**
     * Tests when the forEach function passes and should
     * return a non-empty list
     */
    @Test
    public void forEachTryStatementPassTest() throws SQLException
    {
        final Collection<Integer> collection = new ArrayList<>();

        try(final Connection connection = getConnection(this.gpkgFile))
        {
            JdbcUtility.forEach(connection,
                                "SELECT COUNT(*) FROM sqlite_master;",
                                null,
                                resultSet -> collection.add(resultSet.getInt(1)));
        }

        assertTrue("Result List should have items in it after running the forEach method", !collection.isEmpty());
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the Connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update1NullDatabaseConnectionTest() throws SQLException
    {
        JdbcUtility.update(null,
                           "SELECT COUNT(*) FROM sqlite_master;");

        fail("update should have thrown an IllegalArgumentException for a null connection");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the String is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update1NullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection, null);
        }

        fail("update should have thrown an IllegalArgumentException for a null string");
    }

    /**
     * Runs the function with a String that is not a sql statement
     * and should return an Assertion error
     */
    @Test(expected = AssertionError.class)
    public void update1PreparedStatementCatchTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            final String str = "not valid sql";
            JdbcUtility.update(connection, str);
        }

        fail("update should fail on invalid SQL");
    }

    // TODO this test made no sense in its original form, and it still doesn't really. It's unclear what's being tested. -- LLambert
//    /**
//     * runs the update function with a string
//     * that is a sql statement
//     */
//    @Test
//    public void update1tryStatementTest() throws SQLException
//    {
//        final Collection<Integer> collection = new ArrayList<>();
//
//        final Connection connection = getConnection(this.gpkgFile);
//        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ?;";
//        JdbcUtility.update(connection, str);
//        connection.close();
//
//        assertFalse("Result List should have items in it after running the forEach method", !collection.isEmpty());
//    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the Connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update2NullConnectionTest() throws SQLException
    {
        JdbcUtility.update(null,
                           "SELECT COUNT(*) FROM sqlite_master;",
                           null);

        fail("update should have thrown an IllegalArgumentException for a null connection");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the String is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update2NullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection,
                               null,
                               null);
        }

        fail("update should have thrown an IllegalArgumentException for a null String");
    }

    /**
     * Tests if the function properly updates the File
     * and sets the preparedStatementConsumer values
     */
    @Test
    public void update2TryStatementPassTest() throws SQLException, IOException, URISyntaxException
    {
        final File file = getRandomFile();
        final File original = new File(ClassLoader.getSystemResource("testNetwork_orig.gpkg").toURI());

        Files.copy(original.toPath(), file.toPath(), REPLACE_EXISTING);

        try(final Connection connection = getConnection(file))
        {
            final String insert = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                                                "alaska2",
                                                "from_node",
                                                "to_node");
            JdbcUtility.update(connection,
                               insert,
                               preparedStatement -> { preparedStatement.setInt(1, 1);
                                                      preparedStatement.setInt(2, 2);
                                                    });

            // TODO this test doesn't absolutely nothing relevant. We should start with an empty file, update it, and re-read that update
            assertNotNull("runs the function properly", file);
        }
    }

    /**
     * This tests if an IllegalArgumentException is thrown
     * if the connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update3NullConnectionTest() throws SQLException
    {
        JdbcUtility.update(null,
                           "SELECT COUNT(*) FROM sqlite_master;",
                           null,
                           resultSet -> resultSet.getInt(1));

        fail("update should have thrown an IllegalArgumentException for a null Connection");
    }

    /**
     * This tests if an IllegalArgumentException is thrown
     * if the String is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update3NullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection,
                               null,
                               null,
                               resultSet -> resultSet.getInt(1));
        }

        fail("update should have thrown an IllegalArgumentException for a null Connection");
    }

    /**
     * This tests if an IllegalArgumentException is thrown
     * if ResultSetFunction is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update3NullKeyMapperTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection,
                               "SELECT COUNT(*) FROM sqlite_master;",
                               preparedStatement -> preparedStatement.setString(1, "tiles"),
                               null);
        }

        fail("update should have thrown an illegalArgumentException for a null resultMapper");
    }

    /**
     * Tests if the function returns null when the connection
     * is connected to a MockConnection
     */
    @Test(expected = SQLException.class)
    public void update3TryStatementNullTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            final String result = JdbcUtility.update(connection,
                                                     "SELECT COUNT(*) FROM sqlite_master;",
                                                     preparedStatement -> preparedStatement.setString(1, "tiles"),
                                                     resultSet -> resultSet.getString(1));

            assertNull("Result should return null", result);
        }
    }

    /**
     * Tests if the function runs with the proper connection
     * and the preparedStatement updates the database
     */
    @Test
    public void update3TryStatementPassTest() throws SQLException, IOException, URISyntaxException
    {
        final File file = getRandomFile();
        final File original = new File(ClassLoader.getSystemResource("testNetwork_orig.gpkg").toURI());

        Files.copy(original.toPath(), file.toPath(), REPLACE_EXISTING);

        try(final Connection connection = getConnection(file))
        {
            final String insert = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                                                "alaska2",
                                                "from_node",
                                                "to_node");

            final int identifier = JdbcUtility.update(connection,
                                                      insert,
                                                      preparedStatement -> { preparedStatement.setInt(1, 1);
                                                                             preparedStatement.setInt(2, 2);
                                                                           },
                                                      resultSet -> resultSet.getInt(1));

            assertTrue("Result should be a non negative, non-zero integer", identifier > 0);
        }
    }

    //this portion tests the fourth update function block
    /**
     * Tests if an IllegalArgumentException is thrown
     * when the connection is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update4NullConnectionTest() throws SQLException
    {
        JdbcUtility.update(null,
                           "SELECT COUNT(*) FROM sqlite_master;",
                           Collections.emptyList(),
                           (preparedStatement, args) -> { });

        fail("update should have thrown an illegalArgumentException for a null connection");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the String is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update4NullStringTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection,
                               null,
                               Collections.emptyList(),
                               (preparedStatement, args) -> { });
        }

        fail("update should have thrown an illegalArgumentException for a null connection");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the Iterable is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void update4NullIterableTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            JdbcUtility.update(connection,
                               "SELECT COUNT(*) FROM sqlite_master;",
                               null,
                               (preparedStatement, args) -> { });
        }

        fail("update should have thrown an illegalArgumentException for a null Iterable");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the resultSet is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void map2ResultSetNullTest() throws SQLException
    {
        JdbcUtility.map(null,
                        resultSet -> "",
                        HashSet<String>::new);

        fail("map should have thrown an IllegalArgumentException for a null resultSet");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the ResultSetFunction is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void map2ResultSetFunctionNullTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(final ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    JdbcUtility.map(resultSet,
                                    null,
                                    HashSet<String>::new);
                }
            }

            fail("map should have thrown an IllegalArgumentException for a null resultSetFunction");
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the CollectionFactory is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void map2NullCollectionFactoryTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(final ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    JdbcUtility.map(results,
                                    resultSet -> "",
                                    null);
                }
            }
        }

        fail("map should have thrown an IllegalArgumentException for a null collectionFactory");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when the ResultSet is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterNullResultSetTest() throws SQLException
    {
        JdbcUtility.mapFilter(null,
                             resultSet -> "",
                             pyramidName -> true,
                             HashSet<String>::new);

        fail("mapFilter should have thrown an IllegalArgumentException for a null resultSet");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when ResultSetFunction is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterResultSetTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(final ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    JdbcUtility.mapFilter(results,
                                          null,
                                          pyramidName -> true,
                                          HashSet<String>::new);
                }
            }
        }

        fail("mapFilter should have thrown an IllegalArgumentException for a null funciton");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when JdbcPredicate is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterNullpredicateTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    JdbcUtility.mapFilter(results,
                                          resultSet -> "",
                                          null,
                                          HashSet<String>::new);
                }
            }
        }

        fail("mapFilter should have thrown an IllegalArgumentException for a null predicate");
    }

    /**
     * Tests if an the function returns a collection after
     * executing with a proper string
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterFunctionTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(final ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    final Collection<String> collection = JdbcUtility.mapFilter(results,
                                                                                resultSet -> "",
                                                                                pyramidName -> true,
                                                                                HashSet<String>::new);

                    assertNotNull("mapFilter should have a set", collection);
                }
            }
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when ResultSet is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void getObjectsNullResultSetTest() throws SQLException
    {
        JdbcUtility.getObjects(null, 1, 3);
        fail("getObjects should have thrown an IllegalArgumentException for a null ResultSet");
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when startColumnIndex is greater than EndColumnIndex
     */
    @Test(expected = IllegalArgumentException.class)
    public void getObjectsEndLessThanStartTest() throws SQLException
    {
        try(final Connection connection = new MockConnection())
        {
            try(final Statement statement = connection.createStatement())
            {
                try(final ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master;"))
                {
                    JdbcUtility.getObjects(resultSet, 3, 1);
                }
            }
        }

        fail("getObjects should have thrown an IllegalArgumentException where endColumn is greater than startIndex");
    }
}
