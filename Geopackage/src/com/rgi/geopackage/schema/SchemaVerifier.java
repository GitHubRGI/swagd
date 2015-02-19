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

package com.rgi.geopackage.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utility.DatabaseUtility;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.verification.Assert;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.UniqueDefinition;
import com.rgi.geopackage.verification.Verifier;
/**
 * 
 * @author Jenifer Cochran
 *
 */
public class SchemaVerifier extends Verifier
{
    private class DataColumns
    {
        String tableName;
        String columnName;
        String constraintName;
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
           return String.format("constraint_name: %10s, constraint_type: %5s, invalid min: %.3f, invalid max: %.3f.",
                                 this.constraintName,
                                 this.constraintType,
                                 this.min,
                                 this.max);
        }
    }


    private final boolean hasDataColumnsTable;
    private final boolean hasDataColumnsConstraintsTable;
    private final List<DataColumns> dataColumnsValues;
    private final List<DataColumnConstraints> dataColumnConstraintsValues;
    /**
     * @param sqliteConnection a handle to the database connection
     * @throws SQLException throws if the method {@link DatabaseUtility#tableOrViewExists(Connection, String)} throws
     */
    public SchemaVerifier(final Connection sqliteConnection) throws SQLException
    {
        super(sqliteConnection);

        this.hasDataColumnsTable            = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnsTableName);
        this.hasDataColumnsConstraintsTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageSchema.DataColumnConstraintsTableName);

        this.dataColumnsValues           = this.getDataColumnValues();
        this.dataColumnConstraintsValues = this.getDataColumnConstraintsValues();

    }

    /**
     * <div class="title">Requirement 56</div> <blockquote> A GeoPackage MAY
     * contain a table or updateable view named <code>gpkg_data_columns</code>.
     * If present it SHALL be defined per clause 2.3.2.1.1 <a href=
     * "http://www.geopackage.org/spec/#schema_data_columns_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_data_columns_cols">Data
     * Columns Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_data_columns_sql"
     * >gpkg_data_columns Table Definition SQL</a>. </blockquote> </div>
     * @throws SQLException throws if the method verifyTable throws an SQLException
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 56,
                  text     = "A GeoPackage MAY contain a table or updateable view named "
                              + "gpkg_data_columns. If present it SHALL be defined per "
                              + "clause 2.3.2.1.1 Table Definition, Data Columns Table or"
                              + " View Definition and gpkg_data_columns Table Definition SQL. ",
                  severity = Severity.Error)
    public void Requirement56() throws AssertionError, SQLException
    {
        if(this.hasDataColumnsTable)
        {
            this.verifyTable(SchemaVerifier.DataColumnsTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 57</div> <blockquote> Values of the
     * <code>gpkg_data_columns</code> table <code>table_name</code> column value
     * SHALL reference values in the <code>gpkg_contents</code>
     * <code>table_name</code> column. </blockquote> </div>
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement(number   = 57,
                 text     = "Values of the gpkg_data_columns table table_name column value "
                             + "SHALL reference values in the gpkg_contents table_name column.",
                 severity = Severity.Error)
    public void Requirement57() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsTable)
        {
            final String query = "SELECT dc.table_name "
                            + "FROM gpkg_data_columns AS dc "
                            + "WHERE dc.table_name NOT IN(SELECT gc.table_name "
                                                       + "FROM gpkg_contents AS gc);";

            try(Statement stmt                = this.getSqliteConnection().createStatement();
                ResultSet invalidTableNamesRS = stmt.executeQuery(query))
            {
                final List<String> invalidTableNames = ResultSetStream.getStream(invalidTableNamesRS)
                                                                      .map(resultSet -> { try
                                                                                          {
                                                                                               return resultSet.getString("table_name");
                                                                                          }
                                                                                          catch(final SQLException ex)
                                                                                          {
                                                                                               return null;
                                                                                          }
                                                                                         })
                                                                       .filter(Objects::nonNull)
                                                                       .collect(Collectors.toList());

                Assert.assertTrue(String.format("The following table_name(s) is(are) from gpkg_data_columns and is(are) not referenced in the gpkg_contents table_name: %s",
                                                invalidTableNames.stream()
                                                                 .collect(Collectors.joining(", "))),
                                  invalidTableNames.isEmpty());
            }
        }
    }

    /**
     * <div class="title">Requirement 58</div> <blockquote> The
     * <code>column_name</code> column value in a <code>gpkg_data_columns</code>
     * table row SHALL contain the name of a column in the SQLite table or view
     * identified by the <code>table_name</code> column value. </blockquote>
     * </div>
     *
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 58,
                  text     = "The column_name column value in a gpkg_data_columns table row "
                              + "SHALL contain the name of a column in the SQLite table or view "
                              + "identified by the table_name column value. ",
                  severity = Severity.Warning)
    public void Requirement58() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsTable)
        {
            for(final DataColumns dataColumn: this.dataColumnsValues)
            {
                if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), dataColumn.tableName))
                {
                    final String query = String.format("PRAGMA table_info(%s);", dataColumn.tableName);

                    try(Statement stmt        = this.getSqliteConnection().createStatement();
                        ResultSet tableInfoRS = stmt.executeQuery(query))
                    {
                        final boolean columnExists = ResultSetStream.getStream(tableInfoRS)
                                                                    .anyMatch(resultSet -> { try
                                                                                             {
                                                                                                return resultSet.getString("name").equals(dataColumn.columnName);
                                                                                             }
                                                                                             catch(final SQLException ex)
                                                                                             {
                                                                                                 return false;
                                                                                             }
                                                                                            });
                        Assert.assertTrue(String.format("The column %s does not exist in the table %s.",
                                                        dataColumn.columnName,
                                                        dataColumn.tableName),
                                          columnExists);
                    }
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 59</div> <blockquote> The constraint_name
     * column value in a gpkg_data_columns table MAY be NULL. If it is not NULL,
     * it SHALL contain a case sensitive constraint_name column value from the
     * gpkg_data_column_constraints table. </blockquote> </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number    = 59,
                  text      = "The constraint_name column value in a gpkg_data_columns table MAY be NULL. "
                                  + "If it is not NULL, it SHALL contain a case sensitive constraint_name "
                                  + "column value from the gpkg_data_column_constraints table. ",
                  severity  = Severity.Warning)
    public void Requirement59() throws AssertionError
    {
        if(this.hasDataColumnsTable && this.hasDataColumnsConstraintsTable)
        {
            for(final DataColumnConstraints dataColumnConstraints: this.dataColumnConstraintsValues)
            {
                if(dataColumnConstraints.constraintName != null || !dataColumnConstraints.constraintName.isEmpty())
                {
                    final boolean containsConstraint = this.dataColumnsValues.stream()
                                                                             .filter(dataColumn -> dataColumn.constraintName != null)
                                                                             .anyMatch(dataColumn -> dataColumn.constraintName.equals(dataColumnConstraints.constraintName));

                    Assert.assertTrue(String.format("The constraint_name %s in gpkg_data_columns is not referenced in gpkg_data_constraints table in the column constraint_name.",
                                                    dataColumnConstraints.constraintName),
                                      containsConstraint);
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 60</div> <blockquote> A GeoPackage MAY
     * contain a table or updateable view named gpkg_data_column_constraints. If
     * present it SHALL be defined per clause 2.3.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#data_column_constraints_table_definition"
     * >Table Definition</a>, <a href=
     * "http://www.geopackage.org/spec/#gpkg_data_column_constraints_cols">Data
     * Column Constraints Table or View Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_data_column_constraints_sql"
     * >gpkg_data_columns Table Definition SQL</a>. </blockquote> </div>
     * @throws SQLException throws if the method verifyTable throws
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 60,
                  text     = "A GeoPackage MAY contain a table or updateable view named "
                              + "gpkg_data_column_constraints. If present it SHALL be defined "
                              + "per clause 2.3.3.1.1 Table Definition, Data Column Constraints "
                              + "Table or View Definition and gpkg_data_columns Table Definition SQL. ",
                  severity = Severity.Error)
    public void Requirement60() throws AssertionError, SQLException
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            this.verifyTable(SchemaVerifier.DataColumnConstraintsTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 61</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains data, the lowercase <code>constraint_type</code> column values
     * SHALL be one of "range", "enum", or "glob". </blockquote> </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 61,
                  text     = "The gpkg_data_column_constraints table MAY be empty. "
                              + "If it contains data, the lowercase constraint_type "
                              + "column values SHALL be one of \"range\", \"enum\", or "
                              + "\"glob\". ",
                  severity = Severity.Warning)
    public void Requirement61() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final boolean validConstraintType = this.dataColumnConstraintsValues.stream()
                                                                          .allMatch(dataColumnConstraintValue -> SchemaVerifier.validConstraintType(dataColumnConstraintValue.constraintType));

            Assert.assertTrue(String.format("There is(are) value(s) in gpkg_data_column_constraints table constraint_type that does not match \"range\" or \"enum\" or \"glob\". The invalid value(s): %s.",
                                            this.dataColumnConstraintsValues.stream()
                                                                            .filter(dataColumnConstraintValue -> !SchemaVerifier.validConstraintType(dataColumnConstraintValue.constraintType))
                                                                            .map(value -> value.constraintType).collect(Collectors.joining(", "))),
                              validConstraintType);
        }
    }

    /**
     * <div class="title">Requirement 62</div> <blockquote>
     * gpkg_data_column_constraint constraint_name values for rows with
     * constraint_type values of <em>range</em> and <em>glob</em> SHALL be
     * unique. </blockquote> </div>
     * @throws SQLException throws if various SQLExceptions occur
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 62,
                  text     = "gpkg_data_column_constraint constraint_name values "
                              + "for rows with constraint_type values of range and "
                              + "glob SHALL be unique. ",
                  severity = Severity.Warning)
    public void Requirement62() throws SQLException, AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
           final String query = "SELECT DISTINCT constraint_name AS cs FROM gpkg_data_column_constraints WHERE constraint_type IN ('range', 'glob');";

           try(Statement stmt                             = this.getSqliteConnection().createStatement();
               ResultSet constraintNamesWithRangeOrGlobRS = stmt.executeQuery(query))
           {
               final List<String> constraintNamesWithRangeOrGlob = ResultSetStream.getStream(constraintNamesWithRangeOrGlobRS)
                                                                                  .map(resultSet -> { try
                                                                                                      {
                                                                                                         return resultSet.getString("constraint_name");
                                                                                                      }
                                                                                                      catch(final SQLException ex)
                                                                                                      {
                                                                                                          return null;
                                                                                                      }
                                                                                                    })
                                                                                 .filter(Objects::nonNull)
                                                                                 .collect(Collectors.toList());
               for(final String constraintName: constraintNamesWithRangeOrGlob)
               {
                   final String query2 = String.format("SELECT count(*) FROM gpkg_data_column_constraints WHERE constraint_name = '%s'", constraintName);

                   try(Statement stmt2 = this.getSqliteConnection().createStatement();
                       ResultSet countConstraintNameRS = stmt2.executeQuery(query2))
                   {
                       final int count = countConstraintNameRS.getInt("count(*)");

                       Assert.assertTrue(String.format("There are constraint_name values in gpkg_data_column_constraints with a constraint_type of 'glob' or 'range' are not unique. "
                                                         + "Non-unique constraint_name: %s",
                                                       constraintName),
                                         count <= 1);
                   }
               }
           }
        }
    }

    /**
     * <div class="title">Requirement 63</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with constraint_type column values of "range", the
     * <code>value</code> column values for those rows SHALL be NULL.
     * </blockquote> </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 63,
                  text     = "The gpkg_data_column_constraints table MAY be empty. "
                                  + "If it contains rows with constraint_type column "
                                  + "values of \"range\", the value column values for "
                                  + "those rows SHALL be NULL. ",
                  severity = Severity.Warning)
    public void Requirement63() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<DataColumnConstraints> invalidColumnConstraintRecords = this.dataColumnConstraintsValues.stream()
                                                                                                         .filter(dataColumnConstraint -> Type.Range.equals(dataColumnConstraint.constraintType))
                                                                                                         .filter(dataColumnConstraint -> dataColumnConstraint.value != null)
                                                                                                         .collect(Collectors.toList());

            Assert.assertTrue(String.format("There are records in gpkg_data_column_constraints that have a constraint_type of \"range\" "
                                                + "but does not have a corresponding null value for the column value. \nInvalid value(s): %s",
                                            invalidColumnConstraintRecords.stream().map(columnValue -> columnValue.value).collect(Collectors.joining(", "))),
                             invalidColumnConstraintRecords.isEmpty());
        }
    }

    /**
     * <div class="title">Requirement 64</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "range",
     * the <code>min</code> column values for those rows SHALL be NOT NULL and
     * less than the <code>max</code> column value which shall be NOT NULL.
     * </blockquote> </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     *
     */
    @Requirement (number   = 64,
                  text     = "The gpkg_data_column_constraints table MAY be empty. If it contains rows with "
                                  + "constraint_type column values of \"range\", the min column values for those "
                                  + "rows SHALL be NOT NULL and less than the max column value which shall be NOT NULL.",
                  severity = Severity.Warning)
    public void Requirement64() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            final List<DataColumnConstraints> invalidConstraintValuesWithRange = this.dataColumnConstraintsValues.stream()
                                                                                                           .filter(constraintValue -> Type.Range.equals(constraintValue.constraintType))
                                                                                                           .filter(constraintValue -> constraintValue.min == null ||
                                                                                                                                      constraintValue.max == null ||
                                                                                                                                      constraintValue.min >= constraintValue.max)
                                                                                                           .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following records in gpkg_data_column_constraints have invalid values for min, or max or both:\n%s",
                                            invalidConstraintValuesWithRange.stream()
                                                                            .map(constraintValue -> constraintValue.invalidMinMaxWithRangeType())
                                                                            .collect(Collectors.joining("\n"))),
                              invalidConstraintValuesWithRange.isEmpty());

        }
    }

    /**
     * <div class="title">Requirement 65</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "range",
     * the <code>minIsInclusive</code> and <code>maxIsInclusive</code> column
     * values for those rows SHALL be 0 or 1. </blockquote> </div>
     *
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */

    @Requirement (number   = 65,
                  text     = "The gpkg_data_column_constraints table MAY be empty. If it contains "
                              + "rows with constraint_type column values of \"range\", the minIsInclusive "
                              + "and maxIsInclusive column values for those rows SHALL be 0 or 1. ",
                  severity = Severity.Warning)
    public void Requirement65() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            List<DataColumnConstraints> invalidMinIsInclusiveRecords = this.dataColumnConstraintsValues.stream()
                                                                                                       .filter(columnValue ->  Type.Range.equals(columnValue.constraintType))
                                                                                                       .filter(columnValue -> !Boolean.TRUE.equals(columnValue.minIsInclusive) &&
                                                                                                                              !Boolean.FALSE.equals(columnValue.minIsInclusive))
                                                                                                       .collect(Collectors.toList());

            List<DataColumnConstraints> invalidMaxIsInclusiveRecords = this.dataColumnConstraintsValues.stream()
                                                                                                       .filter(columnValue ->  Type.Range.equals(columnValue.constraintType))
                                                                                                       .filter(columnValue -> !Boolean.TRUE.equals(columnValue.maxIsInclusive) &&
                                                                                                                              !Boolean.FALSE.equals(columnValue.maxIsInclusive))
                                                                                                       .collect(Collectors.toList());
            
            Assert.assertTrue(String.format("The following are violations on either the minIsInclusive or maxIsIclusive columns "
                                            + "in the gpkg_data_column_constraints table for which the values are not 0 or 1. %s. \n%s.",
                                            invalidMinIsInclusiveRecords.stream()
                                                                        .map(record -> String.format("Invalid minIsInclusive for constraint_name: %10s.", record.constraintName))
                                                                        .collect(Collectors.joining(", ")),
                                            invalidMaxIsInclusiveRecords.stream()
                                                                        .map(record -> String.format("Invalid maxIsInclusive for constraint_name: %10s.", record.constraintName))
                                                                        .collect(Collectors.joining(", "))),
                             invalidMinIsInclusiveRecords.isEmpty() && invalidMaxIsInclusiveRecords.isEmpty());

        }
    }

    /**
     * <div class="title">Requirement 66</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "enum"
     * or "glob", the <code>min</code>, <code>max</code>,
     * <code>minIsInclusive</code> and <code>maxIsInclusive</code> column values
     * for those rows SHALL be NULL. </blockquote> </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number   = 66,
                  text     = "The gpkg_data_column_constraints table MAY be empty. If it contains "
                             + "rows with constraint_type column values of \"enum\" or \"glob\", the min,"
                             + " max, minIsInclusive and maxIsInclusive column values for those rows SHALL be NULL.",
                  severity = Severity.Warning)
    public void Requirement66() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            List<DataColumnConstraints> invalidConstraintRecords = this.getDataColumnConstraintsValues().stream()
                                                                                                        .filter(columnValue -> Type.Enum.equals(columnValue.constraintType) ||
                                                                                                                               Type.Glob.equals(columnValue.constraintType))
                                                                                                        .filter(columnValue -> !(columnValue.min == null            &&
                                                                                                                                 columnValue.max == null            &&
                                                                                                                                 columnValue.minIsInclusive == null &&
                                                                                                                                 columnValue.maxIsInclusive == null))
                                                                                                        .collect(Collectors.toList());
            Assert.assertTrue(String.format("The following constraint_name(s) have a constraint_type of \"enum\" or \"glob\" "
                                            + "and do NOT have null values for min, max, minIsInclusive, and/or maxIsInclusive. "
                                            + "\nInvalid constraint_name(s): %s.",
                                            invalidConstraintRecords.stream()
                                                                    .map(columnValue -> columnValue.constraintName)
                                                                    .collect(Collectors.joining(", "))),
                              invalidConstraintRecords.isEmpty());
        }
    }

    /**
     * <div class="title">Requirement 67</div> <blockquote> The
     * <code>gpkg_data_column_constraints</code> table MAY be empty. If it
     * contains rows with <code>constraint_type</code> column values of "enum"
     * or "glob", the <code>value</code> column SHALL NOT be NULL. </blockquote>
     * </div>
     * @throws AssertionError throws if the GeoPackage fails to meet this Requirement
     */
    @Requirement (number = 67,
                  text     = "The gpkg_data_column_constraints table MAY be empty. "
                              + "If it contains rows with constraint_type column values "
                              + "of \"enum\" or \"glob\", the value column SHALL NOT be NULL. ",
                  severity = Severity.Warning)
    public void Requirement67() throws AssertionError
    {
        if(this.hasDataColumnsConstraintsTable)
        {
            List<DataColumnConstraints> invalidValueRecords = this.getDataColumnConstraintsValues().stream()
                                                                                                   .filter(columnValue -> Type.Enum.equals(columnValue.constraintType) ||
                                                                                                                          Type.Glob.equals(columnValue.constraintType))
                                                                                                   .filter(columnValue -> columnValue.value == null)
                                                                                                   .collect(Collectors.toList());
            Assert.assertTrue(String.format("The following constraint_name(s) from the gpkg_data_column_constraints "
                                              + "table have invalid values for the column value. \nInvalid value with "
                                              + "constraint_name as: %s.",
                                            invalidValueRecords.stream()
                                                               .map(columnValue -> columnValue.constraintName)
                                                               .collect(Collectors.joining(", "))),
                             invalidValueRecords.isEmpty());
        }
    }

    private static boolean validConstraintType(final String constraintType)
    {
        return Stream.of(Type.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(constraintType));
    }

    private List<DataColumnConstraints> getDataColumnConstraintsValues()
    {
        final String query = "SELECT constraint_name, constraint_type, value, min, minIsInclusive, max, maxIsInclusive FROM gpkg_data_column_constraints;";

        try(Statement stmt                   = this.getSqliteConnection().createStatement();
            ResultSet tableNamesAndColumnsRS = stmt.executeQuery(query))
        {
            return ResultSetStream.getStream(tableNamesAndColumnsRS)
                                  .map(resultSet -> { try
                                                      {
                                                          final DataColumnConstraints dataColumnConstraints    = new DataColumnConstraints();

                                                          dataColumnConstraints.constraintName = resultSet.getString("constraint_name");
                                                          dataColumnConstraints.constraintType = resultSet.getString("constraint_type");
                                                          dataColumnConstraints.value          = resultSet.getString("value");
                                                          if(resultSet.wasNull())
                                                          {
                                                              dataColumnConstraints.value = null;
                                                          }
                                                          dataColumnConstraints.min            = resultSet.getDouble("min");
                                                          if(resultSet.wasNull())
                                                          {
                                                              dataColumnConstraints.min = null;
                                                          }
                                                          dataColumnConstraints.minIsInclusive = resultSet.getBoolean("minIsInclusive");
                                                          if(resultSet.wasNull())
                                                          {
                                                              dataColumnConstraints.minIsInclusive = null;
                                                          }
                                                          dataColumnConstraints.max            = resultSet.getDouble("max");
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
                                                      catch(final SQLException ex)
                                                      {
                                                          return null;
                                                      }
                                                    })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());
        }
        catch(final SQLException ex)
        {
            return Collections.emptyList();
        }
    }


    private List<DataColumns> getDataColumnValues()
    {
        final String query = "SELECT table_name, column_name, constraint_name FROM gpkg_data_columns;";

        try(Statement stmt                   = this.getSqliteConnection().createStatement();
            ResultSet tableNamesAndColumnsRS = stmt.executeQuery(query))
        {
            return ResultSetStream.getStream(tableNamesAndColumnsRS)
                                  .map(resultSet -> { try
                                                      {
                                                          final DataColumns dataColumn    = new DataColumns();

                                                          dataColumn.tableName      = resultSet.getString("table_name");
                                                          dataColumn.columnName     = resultSet.getString("column_name");
                                                          dataColumn.constraintName = resultSet.getString("constraint_name");

                                                          return dataColumn;
                                                      }
                                                      catch(final SQLException ex)
                                                      {
                                                          return null;
                                                      }
                                                    })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());
        }
        catch(final SQLException ex)
        {
            return Collections.emptyList();
        }
    }

    private static final TableDefinition DataColumnsTableDefinition;
    private static final TableDefinition DataColumnConstraintsTableDefinition;
    static
    {
        final Map<String, ColumnDefinition> dataColumnsTableColumns = new HashMap<>();

        dataColumnsTableColumns.put("table_name",        new ColumnDefinition("TEXT", true,  true, true, null));
        dataColumnsTableColumns.put("column_name",       new ColumnDefinition("TEXT", true,  true, true, null));
        dataColumnsTableColumns.put("name",              new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("title",             new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("description",       new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("mime_type",         new ColumnDefinition("TEXT", false, false, false, null));
        dataColumnsTableColumns.put("constraint_name",   new ColumnDefinition("TEXT", false, false, false, null));

        DataColumnsTableDefinition = new TableDefinition(GeoPackageSchema.DataColumnsTableName,
                                                         dataColumnsTableColumns,
                                                         new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_contents", "table_name", "table_name"))));


        final Map<String, ColumnDefinition> dataColumnConstraintsColumns = new HashMap<>();

        dataColumnConstraintsColumns.put("constraint_name",   new ColumnDefinition("TEXT",    true,  false, false, null));
        dataColumnConstraintsColumns.put("constraint_type",   new ColumnDefinition("TEXT",    true,  false, false, null));
        dataColumnConstraintsColumns.put("value",             new ColumnDefinition("TEXT",    false, false, false, null));
        dataColumnConstraintsColumns.put("min",               new ColumnDefinition("NUMERIC", false, false, false, null));
        dataColumnConstraintsColumns.put("minIsInclusive",    new ColumnDefinition("BOOLEAN", false, false, false, null));
        dataColumnConstraintsColumns.put("max",               new ColumnDefinition("NUMERIC", false, false, false, null));
        dataColumnConstraintsColumns.put("maxIsInclusive",    new ColumnDefinition("BOOLEAN", false, false, false, null));
        dataColumnConstraintsColumns.put("description",       new ColumnDefinition("TEXT",    false, false, false, null));

        DataColumnConstraintsTableDefinition = new TableDefinition(GeoPackageSchema.DataColumnConstraintsTableName,
                                                                   dataColumnConstraintsColumns,
                                                                   Collections.emptySet(),
                                                                   new HashSet<>(Arrays.asList(new UniqueDefinition("constraint_name", "constraint_type", "value"))));

    }



}
