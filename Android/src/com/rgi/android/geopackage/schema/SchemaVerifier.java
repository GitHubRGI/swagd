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

package com.rgi.android.geopackage.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.rgi.android.common.util.StringUtility;
import com.rgi.android.common.util.functional.Function;
import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.common.util.jdbc.JdbcUtility;
import com.rgi.android.common.util.jdbc.ResultSetFunction;
import com.rgi.android.common.util.jdbc.ResultSetPredicate;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.verification.Assert;
import com.rgi.android.geopackage.verification.AssertionError;
import com.rgi.android.geopackage.verification.ColumnDefinition;
import com.rgi.android.geopackage.verification.ForeignKeyDefinition;
import com.rgi.android.geopackage.verification.Requirement;
import com.rgi.android.geopackage.verification.Severity;
import com.rgi.android.geopackage.verification.TableDefinition;
import com.rgi.android.geopackage.verification.UniqueDefinition;
import com.rgi.android.geopackage.verification.VerificationLevel;
import com.rgi.android.geopackage.verification.Verifier;

/**
 *
 * @author Jenifer Cochran
 *
 */
public class SchemaVerifier extends Verifier
{
    private class DataColumns
    {
        private String tableName;
        private String columnName;
        private String constraintName;
    }

    private class DataColumnConstraints
    {
        String  constraintName;
        String  constraintType;
        String  value;
        Double  min;
        Boolean minIsInclusive;
        Double  max;
        Boolean maxIsInclusive;

        public String invalidMinMaxWithRangeType()
        {
           return String.format(Locale.getDefault(),
                                "constraint_name: %10s, constraint_type: %5s, invalid min: %.3f, invalid max: %.3f.",
                                this.constraintName,
                                this.constraintType,
                                this.min,
                                this.max);
        }
    }

    private final boolean                     hasDataColumnsTable;
    private final boolean                     hasDataColumnsConstraintsTable;
    private final List<DataColumns>           dataColumnsValues;
    private final List<DataColumnConstraints> dataColumnConstraintsValues;

    /**
     * @param sqliteConnection
     *             A handle to the database connection
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @throws SQLException throws if the method {@link DatabaseUtility#tableOrViewExists(Connection, String)} throws
     */
    public SchemaVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        this.hasDataColumnsTable            = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnsTableName);
        this.hasDataColumnsConstraintsTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnConstraintsTableName);

        this.dataColumnsValues           = this.getDataColumnValues();
        this.dataColumnConstraintsValues = this.getDataColumnConstraintsValues();
    }

    /**
     * Requirement 57
     *
     * <blockquote>
     * A GeoPackage MAY contain a table or updateable view named
     * <code>gpkg_data_columns</code>. If present it SHALL be defined per
     * clause 2.3.2.1.1 <a href=
     * "http://www.geopackage.org/spec/#schema_data_columns_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_data_columns_cols">Data
     * Columns Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_data_columns_sql"
     * >gpkg_data_columns Table Definition SQL</a>.
     * </blockquote>
     *
     * @throws SQLException throws if the method verifyTable throws an SQLException
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 57",
                 text      = "A GeoPackage MAY contain a table or updateable view named "
                             + "gpkg_data_columns. If present it SHALL be defined per "
                             + "clause 2.3.2.1.1 Table Definition, Data Columns Table or"
                             + " View Definition and gpkg_data_columns Table Definition SQL. ")
    public void Requirement57() throws AssertionError, SQLException
    {
        if(this.hasDataColumnsTable)
        {
            this.verifyTable(SchemaVerifier.DataColumnsTableDefinition);
        }
    }

    /**
     * Requirement 58
     *
     * <blockquote>
     * Values of the <code>gpkg_data_columns</code> table <code>
     * table_name</code> column value SHALL reference values in the <code>
     * gpkg_contents</code> <code>table_name</code> column.
     * </blockquote>
     *
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 58",
                 text      = "Values of the gpkg_data_columns table table_name column value "
                             + "SHALL reference values in the gpkg_contents table_name column.")
    public void Requirement58() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsTable)
        {
            final String query = String.format("SELECT dc.table_name "
                                             + "FROM %s AS dc "
                                             + "WHERE dc.table_name NOT IN(SELECT gc.table_name "
                                                                        + "FROM %s AS gc);",
                                              GeoPackageSchema.DataColumnsTableName,
                                              GeoPackageCore.ContentsTableName);

            final Statement statement = this.getSqliteConnection().createStatement();

            try
            {
                final ResultSet invalidTableNamesRS = statement.executeQuery(query);

                try
                {
                    final List<String> invalidTableNames = JdbcUtility.map(invalidTableNamesRS,
                                                                           new ResultSetFunction<String>()
                                                                           {
                                                                               @Override
                                                                               public String apply(final ResultSet resultSet) throws SQLException
                                                                               {
                                                                                   return resultSet.getString("table_name");
                                                                               }
                                                                           });

                Assert.assertTrue(String.format("The following table_name(s) is(are) from %s and is(are) not referenced in the %s table_name: %s",
                                                GeoPackageSchema.DataColumnsTableName,
                                                GeoPackageCore.ContentsTableName,
                                                StringUtility.join(", ", invalidTableNames)),
                                  invalidTableNames.isEmpty(),
                                  Severity.Error);
                }
                finally
                {
                    invalidTableNamesRS.close();
                }
            }
            finally
            {
                statement.close();
            }
        }
    }

    /**
     * Requirement 59
     *
     * <blockquote>
     * The <code>column_name</code> column value in a <code>gpkg_data_columns
     * </code> table row SHALL contain the name of a column in the SQLite table
     * or view identified by the <code>table_name</code> column value.
     * </blockquote>
     *
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 59",
                 text      = "The column_name column value in a gpkg_data_columns table row "
                             + "SHALL contain the name of a column in the SQLite table or view "
                             + "identified by the table_name column value. ")
    public void Requirement59() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsTable)
        {
            for(final DataColumns dataColumn : this.dataColumnsValues)
            {
                if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), dataColumn.tableName))
                {
                    final String query = String.format("PRAGMA table_info(%s);", dataColumn.tableName);

                    final PreparedStatement statement = this.getSqliteConnection().prepareStatement(query);

                    try
                    {
                        final ResultSet tableInfoRS = statement.executeQuery();

                        try
                        {
                            final boolean columnExists = JdbcUtility.anyMatch(tableInfoRS,
                                                                              new ResultSetPredicate()
                                                                              {
                                                                                  @Override
                                                                                  public boolean apply(final ResultSet resultSet) throws SQLException
                                                                                  {
                                                                                      return resultSet.getString("name").equals(dataColumn.columnName);
                                                                                  }
                                                                              });

                             Assert.assertTrue(String.format("The column %s does not exist in the table %s.",
                                                             dataColumn.columnName,
                                                             dataColumn.tableName),
                                               columnExists,
                                               Severity.Warning);
                        }
                        finally
                        {
                            tableInfoRS.close();
                        }
                    }
                    finally
                    {
                        statement.close();
                    }
                }
            }
        }
    }

    /**
     * Requirement 60
     *
     * <blockquote>
     * The constraint_name column value in a gpkg_data_columns table MAY be
     * NULL. If it is not NULL, it SHALL contain a case sensitive
     * constraint_name column value from the gpkg_data_column_constraints
     * table.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 60",
                 text      = "The constraint_name column value in a gpkg_data_columns table MAY be NULL. "
                             + "If it is not NULL, it SHALL contain a case sensitive constraint_name "
                             + "column value from the gpkg_data_column_constraints table. ")
    public void Requirement60() throws AssertionError
    {
        if(this.hasDataColumnsTable && this.hasDataColumnsConstraintsTable)
        {
            for(final DataColumnConstraints dataColumnConstraints : this.dataColumnConstraintsValues)
            {
                if(dataColumnConstraints.constraintName != null)
                {
                    final boolean containsConstraint = FunctionalUtility.anyMatch(this.dataColumnsValues,
                                                                                  new Predicate<DataColumns>()
                                                                                  {
                                                                                      @Override
                                                                                      public boolean apply(final DataColumns dataColumn)
                                                                                      {
                                                                                          return dataColumn.constraintName != null &&
                                                                                                 dataColumn.constraintName.equals(dataColumnConstraints.constraintName);
                                                                                      }
                                                                                  });

                   Assert.assertTrue(String.format("The constraint_name %s in %s is not referenced in %s table in the column constraint_name.",
                                                   dataColumnConstraints.constraintName,
                                                   GeoPackageSchema.DataColumnsTableName,
                                                   GeoPackageSchema.DataColumnConstraintsTableName),
                                     containsConstraint,
                                     Severity.Warning);
               }
            }
        }
    }

    /**
     * Requirement 61
     *
     * <blockquote>
     * A GeoPackage MAY contain a table or updateable view named
     * gpkg_data_column_constraints. If present it SHALL be defined per clause
     * 2.3.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#data_column_constraints_table_definition"
     * >Table Definition</a>, <a href=
     * "http://www.geopackage.org/spec/#gpkg_data_column_constraints_cols">Data
     * Column Constraints Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_data_column_constraints_sql"
     * >gpkg_data_columns Table Definition SQL</a>.
     * </blockquote>
     *
     * @throws SQLException throws if the method verifyTable throws
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 61",
                 text      = "A GeoPackage MAY contain a table or updateable view named "
                             + "gpkg_data_column_constraints. If present it SHALL be defined "
                             + "per clause 2.3.3.1.1 Table Definition, Data Column Constraints "
                             + "Table or View Definition and gpkg_data_columns Table Definition SQL. ")
    public void Requirement61() throws AssertionError, SQLException
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            this.verifyTable(SchemaVerifier.DataColumnConstraintsTableDefinition);
        }
    }

    /**
     * Requirement 62
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains data, the lowercase <code>constraint_type</code> column values
     * SHALL be one of "range", "enum", or "glob".
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 62",
                 text      = "The gpkg_data_column_constraints table MAY be empty. "
                             + "If it contains data, the lowercase constraint_type "
                             + "column values SHALL be one of \"range\", \"enum\", or "
                             + "\"glob\". ")
    public void Requirement62() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final Collection<String> invalidConstraintTypes = FunctionalUtility.mapFilter(this.dataColumnConstraintsValues,
                                                                                          new Function<DataColumnConstraints, String>()
                                                                                          {
                                                                                              @Override
                                                                                              public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                                              {
                                                                                                  return dataColumnConstraints.constraintType;
                                                                                              }
                                                                                          },
                                                                                          new Predicate<String>()
                                                                                          {
                                                                                              @Override
                                                                                              public boolean apply(final String constraintType)
                                                                                              {
                                                                                                  return !SchemaVerifier.validConstraintType(constraintType);
                                                                                              }
                                                                                          });

            Assert.assertTrue(String.format("There is(are) value(s) in %s table constraint_type that does not match \"range\" or \"enum\" or \"glob\". The invalid value(s): %s.",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join(", ", invalidConstraintTypes)),
                              invalidConstraintTypes.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 63
     *
     * <blockquote>
     * gpkg_data_column_constraint constraint_name values for rows with
     * constraint_type values of <em>range</em> and <em>glob</em> SHALL be
     * unique.
     * </blockquote>
     *
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 63",
                 text      = "gpkg_data_column_constraint constraint_name values "
                             + "for rows with constraint_type values of range and "
                             + "glob SHALL be unique. ")
    public void Requirement63() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
           final String query = String.format("SELECT DISTINCT constraint_name AS cs FROM %s WHERE constraint_type IN ('range', 'glob');",
                                              GeoPackageSchema.DataColumnConstraintsTableName);

           final Statement statement = this.getSqliteConnection().createStatement();

           try
           {
               final ResultSet constraintNamesWithRangeOrGlobRS = statement.executeQuery(query);

               try
               {
                   final List<String> constraintNamesWithRangeOrGlob = JdbcUtility.map(constraintNamesWithRangeOrGlobRS,
                                                                                       new ResultSetFunction<String>()
                                                                                       {
                                                                                           @Override
                                                                                           public String apply(final ResultSet resultSet) throws SQLException
                                                                                           {
                                                                                               return resultSet.getString("constraint_name");
                                                                                           }
                                                                                       });

                   for(final String constraintName : constraintNamesWithRangeOrGlob)
                   {
                       final String query2 = String.format("SELECT count(*) FROM %s WHERE constraint_name = '?'",
                                                           GeoPackageSchema.DataColumnConstraintsTableName);

                       final PreparedStatement statement2 = this.getSqliteConnection().prepareStatement(query2);

                       try
                       {
                           statement2.setString(1, constraintName);

                           final ResultSet countConstraintNameRS = statement2.executeQuery();

                           try
                           {
                               final int count = countConstraintNameRS.getInt("count(*)");

                               Assert.assertTrue(String.format("There are constraint_name values in %s with a constraint_type of 'glob' or 'range' are not unique. "
                                                                 + "Non-unique constraint_name: %s",
                                                               GeoPackageSchema.DataColumnConstraintsTableName,
                                                               constraintName),
                                                 count <= 1,
                                                 Severity.Warning);
                           }
                           finally
                           {
                               countConstraintNameRS.close();
                           }
                       }
                       finally
                       {
                           statement2.close();
                       }
                   }
               }
               finally
               {
                   constraintNamesWithRangeOrGlobRS.close();
               }
           }
           finally
           {
               statement.close();
           }
        }
    }

    /**
     * Requirement 64
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with constraint_type column values of "range", the
     * <code>value</code> column values for those rows SHALL be NULL.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 64",
                 text      = "The gpkg_data_column_constraints table MAY be empty. "
                             + "If it contains rows with constraint_type column "
                             + "values of \"range\", the value column values for "
                             + "those rows SHALL be NULL. ")
    public void Requirement64() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<String> invalidColumnConstraints = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                                      new Predicate<DataColumnConstraints>()
                                                                                      {
                                                                                          @Override
                                                                                          public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                                          {
                                                                                              return dataColumnConstraints != null &&
                                                                                                     Type.Range.equals(dataColumnConstraints.constraintType);
                                                                                          }
                                                                                      },
                                                                                      new Function<DataColumnConstraints, String>()
                                                                                      {
                                                                                          @Override
                                                                                          public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                                          {
                                                                                              return dataColumnConstraints.value;
                                                                                          }
                                                                                      });

            Assert.assertTrue(String.format("There are records in %s that have a constraint_type of \"range\" but does not have a corresponding null value for the column value. \nInvalid value(s): %s",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join(", ", invalidColumnConstraints)),
                             invalidColumnConstraints.isEmpty(),
                             Severity.Warning);
        }
    }

    /**
     * Requirement 65
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of
     * "range", the <code>min</code> column values for those rows SHALL be NOT
     * NULL and less than the <code>max</code> column value which shall be NOT
     * NULL.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     *
     */
    @Requirement(reference = "Requirement 65",
                 text      = "The gpkg_data_column_constraints table MAY be empty. If it contains rows with "
                             + "constraint_type column values of \"range\", the min column values for those "
                             + "rows SHALL be NOT NULL and less than the max column value which shall be NOT NULL.")
    public void Requirement65() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<String> invalidConstraintValuesWithRange = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                                              new Predicate<DataColumnConstraints>()
                                                                                              {
                                                                                                  @Override
                                                                                                  public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                                                  {
                                                                                                      return Type.Range.equals(dataColumnConstraints.constraintType) &&
                                                                                                             (dataColumnConstraints.min == null ||
                                                                                                              dataColumnConstraints.max == null ||
                                                                                                              dataColumnConstraints.min >= dataColumnConstraints.max);
                                                                                                  }
                                                                                              },
                                                                                              new Function<DataColumnConstraints, String>()
                                                                                              {
                                                                                                  @Override
                                                                                                  public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                                                  {
                                                                                                      return dataColumnConstraints.invalidMinMaxWithRangeType();
                                                                                                  }
                                                                                              });

            Assert.assertTrue(String.format("The following records in %s have invalid values for min, or max or both:\n%s",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join("\n", invalidConstraintValuesWithRange)),
                              invalidConstraintValuesWithRange.isEmpty(),
                              Severity.Warning);

        }
    }

    /**
     * Requirement 66
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of
     * "range", the <code>minIsInclusive</code> and <code>maxIsInclusive
     * </code> column values for those rows SHALL be 0 or 1.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */

    @Requirement(reference = "Requirement 66",
                 text      = "The gpkg_data_column_constraints table MAY be empty. If it contains "
                             + "rows with constraint_type column values of \"range\", the minIsInclusive "
                             + "and maxIsInclusive column values for those rows SHALL be 0 or 1. ")
    public void Requirement66() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final Function<DataColumnConstraints, String> toName = new Function<DataColumnConstraints, String>()
                                                                   {
                                                                       @Override
                                                                       public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                       {
                                                                           return dataColumnConstraints.constraintName;
                                                                       }
                                                                   };

            final List<String> invalidMinIsInclusives = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                                    new Predicate<DataColumnConstraints>()
                                                                                    {
                                                                                        @Override
                                                                                        public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                                        {
                                                                                            return Type.Range.equals    (dataColumnConstraints.constraintType) &&
                                                                                                   !Boolean.TRUE.equals (dataColumnConstraints.minIsInclusive) &&
                                                                                                   !Boolean.FALSE.equals(dataColumnConstraints.minIsInclusive);
                                                                                        }
                                                                                    },
                                                                                    toName);

            Assert.assertTrue(String.format("The following are violations of the minIsInclusive columns in the %s table for which the values are not 0 or 1. %s. \n%s.",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join(", ", invalidMinIsInclusives)),
                              invalidMinIsInclusives.isEmpty(),
                              Severity.Warning);

            final List<String> invalidMaxIsInclusives = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                                    new Predicate<DataColumnConstraints>()
                                                                                    {
                                                                                        @Override
                                                                                        public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                                        {
                                                                                            return Type.Range.equals    (dataColumnConstraints.constraintType) &&
                                                                                                   !Boolean.TRUE.equals (dataColumnConstraints.maxIsInclusive) &&
                                                                                                   !Boolean.FALSE.equals(dataColumnConstraints.maxIsInclusive);
                                                                                        }
                                                                                    },
                                                                                    toName);

            Assert.assertTrue(String.format("The following are violations of the maxIsInclusive columns in the %s table for which the values are not 0 or 1. %s. \n%s.",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join(", ", invalidMaxIsInclusives)),
                              invalidMaxIsInclusives.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 67
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "enum"
     * or "glob", the <code>min</code>, <code>max</code>, <code>minIsInclusive
     * </code> and <code>maxIsInclusive</code> column values for those rows
     * SHALL be NULL.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 67",
                 text      = "The gpkg_data_column_constraints table MAY be empty. If it contains "
                             + "rows with constraint_type column values of \"enum\" or \"glob\", the min,"
                             + " max, minIsInclusive and maxIsInclusive column values for those rows SHALL be NULL.")
    public void Requirement67() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<String> invalidConstraintRecords = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                                      new Predicate<DataColumnConstraints>()
                                                                                      {
                                                                                          @Override
                                                                                          public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                                          {
                                                                                              return (Type.Enum.equals(dataColumnConstraints.constraintType) ||
                                                                                                      Type.Glob.equals(dataColumnConstraints.constraintType)) &&
                                                                                                     !(dataColumnConstraints.min            == null &&
                                                                                                       dataColumnConstraints.max            == null &&
                                                                                                       dataColumnConstraints.minIsInclusive == null &&
                                                                                                       dataColumnConstraints.maxIsInclusive == null);
                                                                                          }
                                                                                      },
                                                                                      new Function<DataColumnConstraints, String>()
                                                                                      {
                                                                                          @Override
                                                                                          public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                                          {
                                                                                              return dataColumnConstraints.constraintName;
                                                                                          }
                                                                                      });

            Assert.assertTrue(String.format("The following constraint_name(s) have a constraint_type of \"enum\" or \"glob\" "
                                            + "and do NOT have null values for min, max, minIsInclusive, and/or maxIsInclusive. "
                                            + "\nInvalid constraint_name(s): %s.",
                                            StringUtility.join(", ", invalidConstraintRecords)),
                              invalidConstraintRecords.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 68
     *
     * <blockquote>
     * The <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "enum"
     * or "glob", the <code>value</code> column SHALL NOT be NULL.
     * </blockquote>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(reference = "Requirement 68",
                 text      = "The gpkg_data_column_constraints table MAY be empty. "
                             + "If it contains rows with constraint_type column values "
                             + "of \"enum\" or \"glob\", the value column SHALL NOT be NULL. ")
    public void Requirement68() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<String> invalidValues = FunctionalUtility.filterMap(this.dataColumnConstraintsValues,
                                                                           new Predicate<DataColumnConstraints>()
                                                                           {
                                                                               @Override
                                                                               public boolean apply(final DataColumnConstraints dataColumnConstraints)
                                                                               {
                                                                                   return (Type.Enum.equals(dataColumnConstraints.constraintType) ||
                                                                                           Type.Glob.equals(dataColumnConstraints.constraintType)) &&
                                                                                          dataColumnConstraints.value == null;
                                                                               }
                                                                           },
                                                                           new Function<DataColumnConstraints, String>()
                                                                           {
                                                                               @Override
                                                                               public String apply(final DataColumnConstraints dataColumnConstraints)
                                                                               {
                                                                                   return dataColumnConstraints.constraintName;
                                                                               }
                                                                           });

            Assert.assertTrue(String.format("The following constraint_name(s) from the %s table have invalid values for the column value. \nInvalid value with constraint_name as: %s.",
                                            GeoPackageSchema.DataColumnConstraintsTableName,
                                            StringUtility.join(", ", invalidValues)),
                             invalidValues.isEmpty(),
                             Severity.Warning);
        }
    }

    private static boolean validConstraintType(final String constraintType)
    {
        for(final Type type : Arrays.asList(Type.values()))
        {
            if(type.toString().equalsIgnoreCase(constraintType))
            {
                return true;
            }
        }

        return false;
    }

    private List<DataColumnConstraints> getDataColumnConstraintsValues() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnConstraintsTableName))
        {
            return Collections.emptyList();
        }

        final String query = String.format("SELECT constraint_name, constraint_type, value, min, minIsInclusive, max, maxIsInclusive FROM %s;",
                                           GeoPackageSchema.DataColumnConstraintsTableName);

        final Statement statement = this.getSqliteConnection().createStatement();

        try
        {
            final ResultSet tableNamesAndColumnsRS = statement.executeQuery(query);

            try
            {
                return JdbcUtility.map(tableNamesAndColumnsRS,
                                       new ResultSetFunction<DataColumnConstraints>()
                                       {
                                           @Override
                                           public DataColumnConstraints apply(final ResultSet resultSet) throws SQLException
                                           {
                                               final DataColumnConstraints dataColumnConstraints    = new DataColumnConstraints();

                                               dataColumnConstraints.constraintName = resultSet.getString("constraint_name");
                                               dataColumnConstraints.constraintType = resultSet.getString("constraint_type");
                                               dataColumnConstraints.value          = resultSet.getString("value");


                                               dataColumnConstraints.min = resultSet.getDouble("min");

                                               if(resultSet.wasNull())
                                               {
                                                   dataColumnConstraints.min = null;
                                               }

                                               dataColumnConstraints.minIsInclusive = resultSet.getBoolean("minIsInclusive");

                                               if(resultSet.wasNull())
                                               {
                                                   dataColumnConstraints.minIsInclusive = null;
                                               }

                                               dataColumnConstraints.max = resultSet.getDouble("max");

                                               if(resultSet.wasNull())
                                               {
                                                   dataColumnConstraints.max = null;
                                               }

                                               dataColumnConstraints.maxIsInclusive = resultSet.getBoolean("maxIsInclusive");

                                               if(resultSet.wasNull())
                                               {
                                                   dataColumnConstraints.maxIsInclusive = null;
                                               }

                                               return dataColumnConstraints;
                                           }
                                       });
            }
            finally
            {
                tableNamesAndColumnsRS.close();
            }


        }
        finally
        {
            statement.close();
        }
    }

    private List<DataColumns> getDataColumnValues() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnsTableName))
        {
            return Collections.emptyList();
        }

        final String query = String.format("SELECT table_name, column_name, constraint_name FROM %s;", GeoPackageSchema.DataColumnsTableName);

        final Statement statement = this.getSqliteConnection().createStatement();

        try
        {
            final ResultSet tableNamesAndColumnsRS = statement.executeQuery(query);

            try
            {
                return JdbcUtility.map(tableNamesAndColumnsRS,
                                       new ResultSetFunction<DataColumns>()
                                       {
                                           @Override
                                           public DataColumns apply(final ResultSet resultSet) throws SQLException
                                           {
                                               final DataColumns dataColumn = new DataColumns();

                                               dataColumn.tableName      = resultSet.getString("table_name");
                                               dataColumn.columnName     = resultSet.getString("column_name");
                                               dataColumn.constraintName = resultSet.getString("constraint_name");

                                               return dataColumn;
                                           }
                                       });
            }
            finally
            {
                tableNamesAndColumnsRS.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    private static final TableDefinition DataColumnsTableDefinition;
    private static final TableDefinition DataColumnConstraintsTableDefinition;

    static
    {
        final Map<String, ColumnDefinition> dataColumnsTableColumns = new HashMap<String, ColumnDefinition>();

        dataColumnsTableColumns.put("table_name",      new ColumnDefinition("TEXT", true,  true,  true,  null));
        dataColumnsTableColumns.put("column_name",     new ColumnDefinition("TEXT", true,  true,  true,  null));
        dataColumnsTableColumns.put("name",            new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("title",           new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("description",     new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("mime_type",       new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("constraint_name", new ColumnDefinition("TEXT", false, false, false, null));

        DataColumnsTableDefinition = new TableDefinition(GeoPackageSchema.DataColumnsTableName,
                                                         dataColumnsTableColumns,
                                                         new HashSet<ForeignKeyDefinition>(Arrays.asList(new ForeignKeyDefinition(GeoPackageCore.ContentsTableName, "table_name", "table_name"))));


        final Map<String, ColumnDefinition> dataColumnConstraintsColumns = new HashMap<String, ColumnDefinition>();

        dataColumnConstraintsColumns.put("constraint_name", new ColumnDefinition("TEXT",    true,  false, false, null));
        dataColumnConstraintsColumns.put("constraint_type", new ColumnDefinition("TEXT",    true,  false, false, null));
        dataColumnConstraintsColumns.put("value",           new ColumnDefinition("TEXT",    false, false, false, null));
        dataColumnConstraintsColumns.put("min",             new ColumnDefinition("NUMERIC", false, false, false, null));
        dataColumnConstraintsColumns.put("minIsInclusive",  new ColumnDefinition("BOOLEAN", false, false, false, null));
        dataColumnConstraintsColumns.put("max",             new ColumnDefinition("NUMERIC", false, false, false, null));
        dataColumnConstraintsColumns.put("maxIsInclusive",  new ColumnDefinition("BOOLEAN", false, false, false, null));
        dataColumnConstraintsColumns.put("description",     new ColumnDefinition("TEXT",    false, false, false, null));

        DataColumnConstraintsTableDefinition = new TableDefinition(GeoPackageSchema.DataColumnConstraintsTableName,
                                                                   dataColumnConstraintsColumns,
                                                                   Collections.<ForeignKeyDefinition>emptySet(),
                                                                   new HashSet<UniqueDefinition>(Arrays.asList(new UniqueDefinition("constraint_name", "constraint_type", "value"))));

    }
}
