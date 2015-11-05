package com.rgi.common.util.jdbc;

import com.mockrunner.mock.jdbc.MockConnection;
import com.rgi.common.Pair;
import com.sun.org.apache.xalan.internal.utils.XMLSecurityManager;
import com.sun.org.omg.CORBA.AttributeDescription;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.function.BinaryOperator;

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
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.selectOne(null, str,
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







    //this portion tests the select function block
    @Test(expected = IllegalArgumentException.class)
    public void selectNullConnectionTest() throws SQLException {
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.selectOne(null, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("select should have thrown an IllegalArgumentException for a null Connection.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectNullStringTest() throws Exception {
        final File testFile = this.getRandomFile(4);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        final boolean result = JdbcUtility.selectOne(con, null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1)) > 0;
        fail("select should have thrown an IllegalArgumentException for a null or empty String.");
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void selectNullResultSetFunctionTest() throws Exception {
        final File testFile = this.getRandomFile(2);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final boolean result = JdbcUtility.selectOne(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                null);
        fail("selectOne should have thrown an IllegalArgumentException for a null resultMapper.");
    }






    //this portion tests the forEach function block
    @Test(expected = IllegalArgumentException.class)
    public void forEachNullConnectionTest() throws Exception {
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        JdbcUtility.forEach(null, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1));
        fail("select should have thrown an IllegalArgumentException for a null Connection.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void forEachNullStringTest() throws Exception {
        final File testFile = this.getRandomFile(3);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        JdbcUtility.forEach(con, null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1));
        fail("forEach should have thrown an IllegalArgumentException for a null String.");
    }


    @SuppressWarnings("ConstantConditions")
    @Test(expected = IllegalArgumentException.class)
    public void forEachNullResultSetConsumerFunctionTest() throws Exception {
        final File testFile = this.getRandomFile(8);
        testFile.createNewFile();

        final Connection con = new MockConnection();
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        JdbcUtility.forEach(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                null);
        fail("forEach should have thrown an IllegalArgumentException for a null resultMapper.");
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







    //this portion tests the second update funciton block
    @Test(expected = IllegalArgumentException.class)
    public void update2NullConnectionTest() throws Exception
    {
        final File testFile = this.getRandomFile(5);
        testFile.createNewFile();

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        JdbcUtility.update(null, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                        resultSet -> resultSet.getInt(1));
        fail("update should have thrown an IllegalArgumentException for a null connection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update2NullStringTest() throws Exception
    {
        final File testFile = this.getRandomFile(7);
        testFile.createNewFile();

        final Connection con = new MockConnection();

        JdbcUtility.update(con, null,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                resultSet -> resultSet.getInt(1));
        fail("update should have thrown an IllegalArgumentException for a null String");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update2NullResultSetFuncitonTest() throws Exception
    {
        final File testFile = this.getRandomFile(5);
        testFile.createNewFile();

        final Connection con = new MockConnection();
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        JdbcUtility.update(con, str,
                preparedStatement -> preparedStatement.setString(1, "tiles"),
                null);
        fail("update should have thrown an IllegalArgumentException for a null resultSetFunction");
    }









    //this portion tests the third update function block
    @Test(expected = IllegalArgumentException.class)
    public void update3NullConnectionTest() throws Exception
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
    public void update3NullStringTest() throws Exception
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
    public void update3NullKeyMapperTest() throws Exception
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







    //this portion tests the fourth update function block
    @Test(expected = IllegalArgumentException.class)
    public void update4NullConnectionTest() throws SQLException
    {

        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";

        final Iterable<Pair<Integer, Integer>> edges = Arrays.asList(
                new Pair<>(12, 23),
                new Pair<>(12, 42),
                new Pair<>(34, 56));

        JdbcUtility.update(null, str, edges,
                (preparedStatement, edge) -> { preparedStatement.setInt(1, edge.getLeft());
                                               preparedStatement.setInt(2, edge.getRight());
                                             });
        fail("update should have thrown an illegalArgumentException for a null connection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update4NullStringTest() throws SQLException
    {

        final Connection con = new MockConnection();
        final Iterable<Pair<Integer, Integer>> edges = Arrays.asList(
                new Pair<>(12, 23),
                new Pair<>(12, 42),
                new Pair<>(34, 56));

        JdbcUtility.update(con, null, edges,
                (preparedStatement, edge) -> { preparedStatement.setInt(1, edge.getLeft());
                                               preparedStatement.setInt(2, edge.getRight());
                                             });
        fail("update should have thrown an illegalArgumentException for a null connection");
    }

    @Test(expected = IllegalArgumentException.class)
    public void update4NullIterableTest() throws SQLException
    {
        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";
        final Connection con = new MockConnection();

        JdbcUtility.update(con, str, null,
                (preparedStatement, edge) -> { preparedStatement.setInt(1, Integer.parseInt("tiles"));
                                               preparedStatement.setInt(2, Integer.parseInt("tiles"));
                                             });
        fail("update should have thrown an illegalArgumentException for a null Iterable");
    }

    
    
    

//   // this portion tests the accumulate function block
//    @Test(expected = IllegalArgumentException.class)
//    public void accumulateNullConnectionTest() throws SQLException {
//        final String str = "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;";
//
//        final boolean initialValue = true;
//
//
//
//        final boolean result = JdbcUtility.accumulate(null, str, preparedStatement -> preparedStatement.setString(1, "tiles"), initialValue,
//                resultSet -> resultSet.getInt(1), true);
//        fail("selectOne should have thrown an IllegalArgumentException for a null Connection.");
//    }

    //This portion tests the first map function block
    @Test(expected = IllegalArgumentException.class)
    public void map1NullResultSetTest() throws Exception
    {
        final Connection con = new MockConnection();
        final String query = "SELECT table_name, column_name, extension_name FROM %s;";
        try(Statement statement             = con.createStatement();
            ResultSet tableNameColumnNameRS = statement.executeQuery(query))
        {
            JdbcUtility.map(tableNameColumnNameRS,
                    null);

            fail("map should have thrown an IllegalArgumentException for a null resultSet");
        }
    }

    //This portion tests arrayList map function block
    @Test(expected = IllegalArgumentException.class)
    public void map1NullResultSetFunctionTest() throws SQLException
    {
        final Connection con = new MockConnection();
        final String query = "SELECT table_name, column_name, extension_name FROM %s;";
        try(Statement statement             = con.createStatement();
            ResultSet tableNameColumnNameRS = statement.executeQuery(query))
        {
            JdbcUtility.map(null,
                    resultSet -> new JdbcUtilityTest.ExtensionData(tableNameColumnNameRS.getString("table_name"),
                            tableNameColumnNameRS.getString("column_name"),
                            tableNameColumnNameRS.getString("extension_name")));

            fail("map should have thrown an IllegalArgumentException for a null resultSetFunction");
        }
    }


    //This portion tests the Collection map function block
    @Test(expected = IllegalArgumentException.class)
    public void map2ResultSetNullTest() throws SQLException
    {
        final String str = "SELECT table_name FROM %s WHERE data_type = 'tiles';";

        final Connection con = new MockConnection();

        JdbcUtility.map(null,
                resultSet -> resultSet.getString("table_name"),
                HashSet<String>::new);
        fail("map should have thrown an IllegalArgumentException for a null resultSet");
    }

    @Test(expected = IllegalArgumentException.class)
    public void map2ResultSetFunctionNullTest() throws SQLException
    {
        final String str = "SELECT table_name FROM %s WHERE data_type = 'tiles';";

        final Connection con = new MockConnection();

        try(final Statement createStmt2           = con.createStatement();
            final ResultSet contentsPyramidTables = createStmt2.executeQuery(str))
        {
            JdbcUtility.map(contentsPyramidTables,
                    null,
                    HashSet<String>::new);
            fail("map should have thrown an IllegalArgumentException for a null resultSetFunction");

        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void map2NullCollectionFactoryTest() throws SQLException
    {
        final String str = "SELECT table_name FROM %s WHERE data_type = 'tiles';";

        final Connection con = new MockConnection();

        try(final Statement createStmt2           = con.createStatement();
            final ResultSet contentsPyramidTables = createStmt2.executeQuery(str))
        {
            JdbcUtility.map(contentsPyramidTables,
                    resultSet -> resultSet.getString("table_name"),
                    null);
            fail("map should have thrown an IllegalArgumentException for a null collectionFactory");

        }
    }


    //This portion tests the mapFilter function block
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterNullResultSetTest() throws SQLException
    {
        final Connection con = new MockConnection();

        JdbcUtility.mapFilter(null,
                resultSet -> resultSet.getString("table_name"),
                pyramidName -> JdbcUtilityTest.tableOrViewExists(con, pyramidName),
                HashSet<String>::new);

        fail("mapFilter should have thrown an IllegalArgumentException for a null resultSet");

    }

    @Test(expected = IllegalArgumentException.class)
    public void mapFilterNullFunctionTest() throws SQLException
    {
        final String str = "SELECT DISTINCT table_name FROM %s;";
        final Connection con = new MockConnection();

        try(Statement createStmt3             = con.createStatement();
            ResultSet tileMatrixPyramidTables = createStmt3.executeQuery(str))
        {
            JdbcUtility.mapFilter(tileMatrixPyramidTables,
                    null,
                    pyramidName -> JdbcUtilityTest.tableOrViewExists(con, pyramidName),
                    HashSet<String>::new);

            fail("mapFIlter should have thrown an IllegalArgumentException for a null funciton");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapFilterNullpredicateTest() throws SQLException
    {
        final String str = "SELECT DISTINCT table_name FROM %s;";
        final Connection con = new MockConnection();

        try(Statement createStmt3             = con.createStatement();
            ResultSet tileMatrixPyramidTables = createStmt3.executeQuery(str))
        {
            JdbcUtility.mapFilter(tileMatrixPyramidTables,
                    resultSet -> resultSet.getString("table_name"),
                    null,
                    HashSet<String>::new);

            fail("mapFIlter should have thrown an IllegalArgumentException for a null predicate");
        }
    }

    //@TODO find out a way to test getObjects
////    this portion tests the getObjects function block
//    @Test(expected = IllegalArgumentException.class)
//    public void getObjectsNullResultSetTest() throws SQLException
//    {
//        final Connection con = new MockConnection();
//
//            JdbcUtility.getObjects(null, 1, this.attributeDescriptions.size());
//            fail("getObjects should have thrown an IllegalArgumentException for a null ResultSet");
//    }


    private static final class ExtensionData
    {
        private ExtensionData(final String tableName,
                              final String columnName,
                              final String extensionName)
        {
            this.tableName     = tableName;
            this.columnName    = columnName;
            this.extensionName = extensionName;
        }

        private final String tableName;
        private final String columnName;
        private final String extensionName;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean tableOrViewExists(final Connection connection, final String name) throws SQLException
    {
        final Connection con = new MockConnection();
                JdbcUtilityTest.verify(connection);

        if(name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("Table/view name cannot be null or empty");
        }

        return JdbcUtility.selectOne(connection,
                "SELECT COUNT(*) FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND name = ? LIMIT 1;",
                preparedStatement -> preparedStatement.setString(1, name),
                resultSet -> resultSet.getInt(1)) > 0;
    }

    private static void verify(final Connection connection) throws SQLException
    {
        if(connection == null || connection.isClosed())
        {
            throw new IllegalArgumentException("The connection cannot be null or closed.");
        }
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























