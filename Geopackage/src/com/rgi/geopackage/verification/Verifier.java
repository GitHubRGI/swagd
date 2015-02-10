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

package com.rgi.geopackage.verification;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rgi.common.util.jdbc.ResultSetStream;

/**
 * @author Luke Lambert
 * @author Jenifer Cochran
 */
public class Verifier
{
    /**
     * Constructor
     *
     * @param file File handle to the SQLite database
     * @param sqliteConnection JDBC connection to the SQLite database
     */
    public Verifier(final Connection sqliteConnection)
    {
        if(sqliteConnection == null)
        {
            throw new IllegalArgumentException("SQLite connection cannot be null");
        }

        this.sqliteConnection = sqliteConnection;
    }

    /**
     * Checks a GeoPackage (via it's {@link java.sql.Connection}) for violoations of the requirements outlined in the <a href="http://www.geopackage.org/spec/">standard</a>.
     *
     * @return Returns the definition for all failed requirements
     */
    public Collection<FailedRequirement> getFailedRequirements()
    {
        return this.getRequirements()
                   .map(requirementTestMethod -> { try
                                                   {
                                                       requirementTestMethod.invoke(this);
                                                       return null;
                                                   }
                                                   catch(final InvocationTargetException ex)
                                                   {
                                                       final Requirement requirement = requirementTestMethod.getAnnotation(Requirement.class);
                                                       // The ruling on the field,  right now,  is that everything will be wrapped in a failed requirement,  even if it's an issue in the test code.
                                                       //if(ex.getCause() instanceof AssertionError)
                                                       //{
                                                           return new FailedRequirement(ex.getCause().getMessage(),  requirement);
                                                       //}

                                                       //throw new RuntimeException(String.format("Unexpected exception thrown when testing requirement %d for GeoPackage verification: %s",
                                                       //                                         requirement.number(),
                                                       //                                         ex.getCause().getMessage()));
                                                   }
                                                   catch(final IllegalAccessException ex)
                                                   {
                                                       // TODO
                                                       ex.printStackTrace();
                                                       return null;
                                                   }
                                                 })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toCollection(ArrayList::new));
    }

    protected static boolean checkDataType(final String dataType)
    {
        return Verifier.AllowedSqlTypes.contains(dataType)   ||
               dataType.matches("TEXT\\([0-9]+\\)") ||
               dataType.matches("BLOB\\([0-9]+\\)");
    }

    /**
     * @return Returns a stream of methods that are annotated with @Requirement
     */
    protected Stream<Method> getRequirements()
    {
        return Stream.of(this.getClass().getDeclaredMethods())
                     .filter(method -> method.isAnnotationPresent(Requirement.class))
                     .sorted((method1, method2) -> Integer.compare(method1.getAnnotation(Requirement.class).number(),
                                                                   method2.getAnnotation(Requirement.class).number()));
    }

    protected void verifyTable(final TableDefinition table) throws AssertionError, SQLException
    {
        this.verifyTableDefinition(table.getName());

        final Set<UniqueDefinition> uniques = this.getUniques(table.getName());

        this.verifyColumns(table.getName(),
                           table.getColumns(),
                           uniques);

        this.verifyForeignKeys(table.getName(), table.getForeignKeys());

        Verifier.verifyGroupUniques(table.getName(),
                                    table.getGroupUniques(),
                                    uniques);
    }

    /**
     * @param table
     * @throws SQLException
     * @throws AssertionError
     */
    protected void verifyTableDefinition(final String tableName) throws SQLException, AssertionError
    {
        try(final PreparedStatement statement = this.sqliteConnection.prepareStatement("SELECT sql FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND tbl_name = ?;"))
        {
            statement.setString(1, tableName);
            try(ResultSet gpkgContents = statement.executeQuery())
            {
                final String sql = gpkgContents.getString("sql");
                Assert.assertTrue(String.format("The sql field must include the %s Table SQL Definition.",
                                         tableName),
                           sql != null);
            }
        }
    }

    /**
     * @param table
     * @throws SQLException
     * @throws AssertionError
     */
    protected void verifyColumns(final String tableName, final Map<String, ColumnDefinition> requiredColumns, final Set<UniqueDefinition> uniques) throws SQLException, AssertionError
    {
        try(final Statement statement = this.sqliteConnection.createStatement();
            final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s);", tableName)))
        {
            final Map<String, ColumnDefinition> columns = ResultSetStream.getStream(tableInfo)
                                                                         .map(resultSet -> { try
                                                                                             {
                                                                                                 final String columnName = resultSet.getString("name");
                                                                                                 return new AbstractMap.SimpleImmutableEntry<>(columnName,
                                                                                                                                               new ColumnDefinition(tableInfo.getString ("type"),
                                                                                                                                                                    tableInfo.getBoolean("notnull"),
                                                                                                                                                                    tableInfo.getBoolean("pk"),
                                                                                                                                                                    uniques.stream().anyMatch(unique -> unique.equals(columnName)),
                                                                                                                                                                    tableInfo.getString ("dflt_value")));   // Manipulate values so that they're "normalized" sql expressions, e.g. "" -> '', strftime ( '%Y-%m-%dT%H:%M:%fZ' , 'now' ) -> strftime('%Y-%m-%dT%H:%M:%fZ','now')
                                                                                             }
                                                                                             catch(final SQLException ex)
                                                                                             {
                                                                                                 ex.printStackTrace();
                                                                                                 return null;
                                                                                             }
                                                                                           })
                                                                         .filter(Objects::nonNull)
                                                                         .collect(Collectors.toMap(entry -> entry.getKey(),
                                                                                                   entry -> entry.getValue()));
            // Make sure the required fields exist in the table
            for(final Entry<String, ColumnDefinition> column : requiredColumns.entrySet())
            {
                final ColumnDefinition columnDefinition = columns.get(column.getKey());
                Assert.assertTrue(String.format("Required column: %s.%s is missing", tableName, column.getKey()),
                                  columnDefinition != null);

                if(columnDefinition != null)
                {
                    Assert.assertTrue(String.format("Required column %s is defined as:\n%s\nbut should be:\n%s",
                                             column.getKey(),
                                             columnDefinition.toString(),
                                             column.getValue().toString()),
                               columnDefinition.equals(column.getValue()));
                }
            }
        }
    }

    protected void verifyForeignKeys(final String tableName, final Set<ForeignKeyDefinition> requiredForeignKeys) throws AssertionError, SQLException
    {
        try(Statement statement = this.sqliteConnection.createStatement())
        {
            try(ResultSet fkInfo = statement.executeQuery(String.format("PRAGMA foreign_key_list(%s);", tableName)))
            {
                final Set<ForeignKeyDefinition> foreignKeys = ResultSetStream.getStream(fkInfo)
                                                                             .map(resultSet -> { try
                                                                                                 {
                                                                                                     return new ForeignKeyDefinition(resultSet.getString("table"),
                                                                                                                                     resultSet.getString("from"),
                                                                                                                                     resultSet.getString("to"));
                                                                                                 }
                                                                                                 catch(final SQLException ex)
                                                                                                 {
                                                                                                     ex.printStackTrace();
                                                                                                     return null;
                                                                                                 }
                                                                                               })
                                                                             .filter(Objects::nonNull)
                                                                             .collect(Collectors.toSet());

                // check to see if the correct foreign key constraints are placed
                for(final ForeignKeyDefinition foreignKey : requiredForeignKeys)
                {
                    Assert.assertTrue(String.format("The table %s is missing the foreign key constraint: %1$s.%s => %s.%s",
                                             tableName,
                                             foreignKey.getFromColumnName(),
                                             foreignKey.getReferenceTableName(),
                                             foreignKey.getToColumnName()),
                              foreignKeys.contains(foreignKey));
                }
            }
            catch(final SQLException ex)
            {
                // If a table has no foreign keys, executing the query
                // PRAGMA foreign_key_list(<table_name>) will throw an
                // exception complaining that result set is empty.
                // The issue has been posted about it here:
                // https://bitbucket.org/xerial/sqlite-jdbc/issue/162/
                // If the result set is empty (no foreign keys), there's no
                // work to be done.  Unfortunately .executeQuery() may throw an
                // SQLException for other reasons that may require some
                // attention.
            }
        }
    }

    protected static void verifyGroupUniques(final String tableName, final Set<UniqueDefinition> requiredGroupUniques, final Set<UniqueDefinition> uniques) throws AssertionError
    {
        for(final UniqueDefinition groupUnique : requiredGroupUniques)
        {
            Assert.assertTrue(String.format("The table %s is missing the column group unique constraint: (%s)",
                                     tableName,
                                     String.join(", ", groupUnique.getColumnNames())),
                       uniques.contains(groupUnique));
        }
    }

    protected Set<UniqueDefinition> getUniques(final String tableName) throws SQLException
    {
        try(final Statement statement = this.sqliteConnection.createStatement();
            final ResultSet indices   = statement.executeQuery(String.format("PRAGMA index_list(%s);", tableName)))
        {
            return ResultSetStream.getStream(indices)
                                  .map(resultSet -> { try
                                                      {
                                                          final String indexName = resultSet.getString("name");
                                                          try(Statement nameStatement = this.sqliteConnection.createStatement();
                                                              ResultSet namesSet      = nameStatement.executeQuery(String.format("PRAGMA index_info(%s);", indexName));)
                                                          {
                                                              return new UniqueDefinition(ResultSetStream.getStream(namesSet)
                                                                                                         .map(names -> { try
                                                                                                                         {
                                                                                                                             return names.getString("name");
                                                                                                                         }
                                                                                                                         catch(final Exception ex)
                                                                                                                         {
                                                                                                                             ex.printStackTrace();
                                                                                                                             return null;
                                                                                                                         }
                                                                                                                        })
                                                                                                         .filter(Objects::nonNull)
                                                                                                         .collect(Collectors.toList()));
                                                          }
                                                      }
                                                      catch(final Exception ex)
                                                      {
                                                          ex.printStackTrace();
                                                          return null;
                                                      }
                                                     })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toSet());

        }
    }

    /**
     * @return the sqliteConnection
     */
    protected Connection getSqliteConnection()
    {
        return this.sqliteConnection;
    }

    /**
     * @return the AllowedSqlTypes
     */
    protected static List<String> getAllowedSqlTypes()
    {
        return Verifier.AllowedSqlTypes;
    }

    private final Connection sqliteConnection;

    private static final List<String> AllowedSqlTypes = Arrays.asList("BOOLEAN",        "TINYINT",         "SMALLINT",     "MEDIUMINT",
                                                                      "INT",            "FLOAT",           "DOUBLE",       "REAL",
                                                                      "TEXT",           "BLOB",            "DATE",         "DATETIME",
                                                                      "GEOMETRY",       "POINT",           "LINESTRING",   "POLYGON",
                                                                      "MULTIPOINT",     "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION",
                                                                      "INTEGER");
}
