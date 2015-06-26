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

package com.rgi.android.geopackage.verification;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.rgi.android.common.util.StringUtility;
import com.rgi.android.common.util.functional.Function;
import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.common.util.jdbc.JdbcUtility;
import com.rgi.android.common.util.jdbc.ResultSetFunction;

/**
 * @author Luke Lambert
 * @author Jenifer Cochran
 */
public class Verifier
{
    /**
     * Constructor
     *
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @param sqliteConnection JDBC connection to the SQLite database
     *
     */
    public Verifier(final Connection sqliteConnection, final VerificationLevel verificationLevel)
    {
        if(sqliteConnection == null)
        {
            throw new IllegalArgumentException("SQLite connection cannot be null");
        }

        this.sqliteConnection  = sqliteConnection;
        this.verificationLevel = verificationLevel;
    }

    /**
     * Checks a GeoPackage (via it's {@link java.sql.Connection}) for violations of the requirements outlined in the <a href="http://www.geopackage.org/spec/">standard</a>.
     *
     * @return Returns the definition for all failed requirements
     */
    public Collection<VerificationIssue> getVerificationIssues()
    {
        return FunctionalUtility.mapFilter(this.getRequirements(),
                                           new Function<Method, VerificationIssue>()
                                           {
                                               @Override
                                              public VerificationIssue apply(final Method requirementTestMethod)
                                              {
                                                  try
                                                  {
                                                      requirementTestMethod.invoke(Verifier.this);
                                                      return null;
                                                  }
                                                  catch(final InvocationTargetException ex)
                                                  {
                                                      final Requirement requirement = requirementTestMethod.getAnnotation(Requirement.class);

                                                      final Throwable cause = ex.getCause();

                                                      if(cause != null && cause instanceof AssertionError)
                                                      {
                                                          final AssertionError assertionError = (AssertionError)cause;

                                                          return assertionError.getSeverity() == Severity.Skipped ? null
                                                                                                                  : new VerificationIssue(assertionError.getMessage(),
                                                                                                                                          requirement,
                                                                                                                                          assertionError.getSeverity());
                                                      }

                                                      return new VerificationIssue(String.format("Unexpected exception thrown when testing requirement %s for GeoPackage verification: %s",
                                                                                                 requirement.reference(),
                                                                                                 ex.getMessage()),
                                                                                   requirement);
                                                  }
                                                  catch(final IllegalAccessException ex)
                                                  {
                                                      // TODO
                                                      ex.printStackTrace();
                                                      return null;
                                                  }
                                              }
                                           },
                                           new Predicate<VerificationIssue>()
                                           {
                                               @Override
                                               public boolean apply(final VerificationIssue verification)
                                               {
                                                   return verification != null;
                                               }
                                           });
    }

    /**
     * @param dataType
     *             Data type type string
     * @return Returns true if dataType is one of the known SQL types or
     * matches one of the formatted TEXT or BLOB types
     */
    protected static boolean checkDataType(final String dataType)
    {
        return Verifier.AllowedSqlTypes.contains(dataType) ||
               dataType.matches("TEXT\\([0-9]+\\)")        ||
               dataType.matches("BLOB\\([0-9]+\\)");
    }

    /**
     * @return Returns a stream of methods that are annotated with @Requirement
     */
    protected Collection<Method> getRequirements()
    {
        final List<Method> requirements = new ArrayList<Method>();

        for(final Method method : this.getClass().getDeclaredMethods())
        {
            if(method.isAnnotationPresent(Requirement.class))
            {
                requirements.add(method);
            }
        }

        Collections.sort(requirements,
                         new Comparator<Method>()
                         {
                            @Override
                            public int compare(final Method o1, final Method o2)
                            {
                                return o1.getAnnotation(Requirement.class)
                                         .reference()
                                         .compareTo(o2.getAnnotation(Requirement.class)
                                                      .reference());
                            }
                         });

        return requirements;
    }

    /**
     * @param table
     *             Table definition to
     * @throws AssertionError
     * @throws SQLException
     */
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
        final PreparedStatement statement = this.sqliteConnection.prepareStatement("SELECT sql FROM sqlite_master WHERE (type = 'table' OR type = 'view') AND tbl_name = ?;");

        try
        {
            statement.setString(1, tableName);

            final ResultSet gpkgContents = statement.executeQuery();

            try
            {
                final String sql = gpkgContents.getString("sql");
                Assert.assertTrue(String.format("The `sql` field must include the %s table SQL Definition.",
                                                tableName),
                                  sql != null,
                                  Severity.Error);
            }
            finally
            {
                gpkgContents.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    /**
     * @param table
     * @throws SQLException
     * @throws AssertionError
     */
    protected void verifyColumns(final String tableName, final Map<String, ColumnDefinition> requiredColumns, final Set<UniqueDefinition> uniques) throws SQLException, AssertionError
    {
        final Statement statement = this.sqliteConnection.createStatement();

        try
        {
            final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s);",
                                                                             tableName));

            try
            {
                final TreeMap<String, ColumnDefinition> columns = new TreeMap<String, ColumnDefinition>(String.CASE_INSENSITIVE_ORDER);

                while(tableInfo.next())
                {
                    try
                    {
                        final String columnName = tableInfo.getString("name");
                        columns.put(columnName,
                                    new ColumnDefinition(tableInfo.getString ("type"),
                                                         tableInfo.getBoolean("notnull"),
                                                         tableInfo.getBoolean("pk"),
                                                         FunctionalUtility.anyMatch(uniques,
                                                                                    new Predicate<UniqueDefinition>()
                                                                                    {
                                                                                        @Override
                                                                                        public boolean apply(final UniqueDefinition t)
                                                                                        {
                                                                                            return t.equals(columnName);
                                                                                        }
                                                                                    }),
                                                         tableInfo.getString ("dflt_value")));   // TODO manipulate values so that they're "normalized" sql expressions, e.g. "" -> '', strftime ( '%Y-%m-%dT%H:%M:%fZ' , 'now' ) -> strftime('%Y-%m-%dT%H:%M:%fZ','now')
                    }
                    catch(final SQLException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                // Make sure the required fields exist in the table
                for(final Entry<String, ColumnDefinition> column : requiredColumns.entrySet())
                {
                    Assert.assertTrue(String.format("Required column: %s.%s is missing", tableName, column.getKey()),
                                      columns.containsKey(column.getKey()),
                                      Severity.Error);

                    final ColumnDefinition columnDefinition = columns.get(column.getKey());

                    if(columnDefinition != null)
                    {
                        Assert.assertTrue(String.format("Required column %s is defined as:\n%s\nbut should be:\n%s",
                                                        column.getKey(),
                                                        columnDefinition.toString(),
                                                        column.getValue().toString()),
                                          columnDefinition.equals(column.getValue()),
                                          Severity.Error);
                    }
                }
            }
            finally
            {
                tableInfo.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    protected void verifyForeignKeys(final String tableName, final Set<ForeignKeyDefinition> requiredForeignKeys) throws AssertionError, SQLException
    {
        final Statement statement = this.sqliteConnection.createStatement();

        try
        {
            final ResultSet fkInfo = statement.executeQuery(String.format("PRAGMA foreign_key_list(%s);", tableName));

            try
            {
                final List<ForeignKeyDefinition> foundForeignKeys = JdbcUtility.map(fkInfo,
                                                                                    new ResultSetFunction<ForeignKeyDefinition>()
                                                                                    {
                                                                                        @Override
                                                                                        public ForeignKeyDefinition apply(final ResultSet resultSet) throws SQLException
                                                                                        {
                                                                                            return new ForeignKeyDefinition(resultSet.getString("table"),
                                                                                                                            resultSet.getString("from"),
                                                                                                                            resultSet.getString("to"));
                                                                                        }
                                                                                    });

                final Collection<ForeignKeyDefinition> missingKeys = new HashSet<ForeignKeyDefinition>(requiredForeignKeys);
                missingKeys.removeAll(foundForeignKeys);

                final Collection<ForeignKeyDefinition> extraneousKeys = new HashSet<ForeignKeyDefinition>(foundForeignKeys);
                extraneousKeys.removeAll(requiredForeignKeys);

                final StringBuilder error = new StringBuilder();

                if(!missingKeys.isEmpty())
                {
                    error.append(String.format("The table %s is missing the foreign key constraint(s): \n", tableName));
                    for(final ForeignKeyDefinition key : missingKeys)
                    {
                        error.append(String.format("%s.%s -> %s.%s\n",
                                                   tableName,
                                                   key.getFromColumnName(),
                                                   key.getReferenceTableName(),
                                                   key.getToColumnName()));
                    }
                }

                if(!extraneousKeys.isEmpty())
                {
                    error.append(String.format("The table %s has extraneous foreign key constraint(s): \n", tableName));
                    for(final ForeignKeyDefinition key : extraneousKeys)
                    {
                        error.append(String.format("%s.%s -> %s.%s\n",
                                                   tableName,
                                                   key.getFromColumnName(),
                                                   key.getReferenceTableName(),
                                                   key.getToColumnName()));
                    }
                }

                Assert.assertTrue(error.toString(),
                                  error.length() == 0,
                                  Severity.Error);
            }
            finally
            {
                fkInfo.close();
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
        finally
        {
            statement.close();
        }
    }

    protected static void verifyGroupUniques(final String tableName, final Set<UniqueDefinition> requiredGroupUniques, final Set<UniqueDefinition> uniques) throws AssertionError
    {
        for(final UniqueDefinition groupUnique : requiredGroupUniques)
        {
            Assert.assertTrue(String.format("The table %s is missing the column group unique constraint: (%s)",
                                            tableName,
                                            StringUtility.join(", ", groupUnique.getColumnNames())),
                              uniques.contains(groupUnique),
                              Severity.Error);
        }
    }

    protected Set<UniqueDefinition> getUniques(final String tableName) throws SQLException
    {
        final Statement statement = this.sqliteConnection.createStatement();

        try
        {
            final ResultSet indices = statement.executeQuery(String.format("PRAGMA index_list(%s);", tableName));

            try
            {
                final Set<UniqueDefinition> uniqueDefinitions = new HashSet<UniqueDefinition>();

                while(indices.next())
                {
                    try
                    {
                        final String indexName = indices.getString("name");

                        final Statement nameStatement = this.sqliteConnection.createStatement();

                        try
                        {
                            final ResultSet namesSet = nameStatement.executeQuery(String.format("PRAGMA index_info(%s);", indexName));

                            try
                            {
                                final List<String> columnNames = new ArrayList<String>();

                                while(namesSet.next())
                                {
                                    try
                                    {
                                        columnNames.add(namesSet.getString("name"));
                                    }
                                    catch(final Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                }

                                uniqueDefinitions.add(new UniqueDefinition(columnNames));
                            }
                            finally
                            {
                                namesSet.close();
                            }
                        }
                        finally
                        {
                            nameStatement.close();
                        }
                    }
                    catch(final Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }

                return uniqueDefinitions;
            }
            finally
            {
                indices.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    /**
     * @return The SQLite connection
     */
    protected Connection getSqliteConnection()
    {
        return this.sqliteConnection;
    }

    /**
     * @return The list of allowed SQL types
     */
    protected static List<String> getAllowedSqlTypes()
    {
        return Verifier.AllowedSqlTypes;
    }

    private final Connection sqliteConnection;

    protected final VerificationLevel verificationLevel;

    private static final List<String> AllowedSqlTypes = Arrays.asList("BOOLEAN",        "TINYINT",         "SMALLINT",     "MEDIUMINT",
                                                                      "INT",            "FLOAT",           "DOUBLE",       "REAL",
                                                                      "TEXT",           "BLOB",            "DATE",         "DATETIME",
                                                                      "GEOMETRY",       "POINT",           "LINESTRING",   "POLYGON",
                                                                      "MULTIPOINT",     "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION",
                                                                      "INTEGER");
}
