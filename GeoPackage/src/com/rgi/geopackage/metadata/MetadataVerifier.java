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

package com.rgi.geopackage.metadata;

import static com.rgi.geopackage.verification.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.verification.Assert;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

/**
 * @author Luke Lambert
 * @author Jenifer Cochran
 */
public class MetadataVerifier extends Verifier
{
    /**
     * @param sqliteConnection
     *            the handle to the database connection
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @throws SQLException
     *             throws when the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             or when other SQLExceptions occur
     */
    public MetadataVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        this.hasMetadataTable          = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataTableName);
        this.hasMetadataReferenceTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataReferenceTableName);
        this.metadataValues            = this.getMetadataValues();
        this.metadataReferenceValues   = this.getMetadataReferenceValues();

    }

    /**
     * <div class="title">Requirement 68</div> <blockquote> A GeoPackage MAY
     * contain a table named gpkg_metadata. If present it SHALL be defined per
     * clause 2.4.2.1.1 <a
     * href="http://www.geopackage.org/spec/#metadata_table_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_cols">Metadata Table
     * Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_sql">gpkg_metadata
     * Table Definition SQL</a>. </blockquote> </div>
     * @throws SQLException throws if the method verifyTable throws
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement (number   = 68,
                  text     = "A GeoPackage MAY contain a table named gpkg_metadata."
                              + " If present it SHALL be defined per clause 2.4.2.1.1 "
                              + "Table Definition, Metadata Table Definition and gpkg_metadata "
                              + "Table Definition SQL. ",
                  severity = Severity.Error)
    public void Requirement68() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            this.verifyTable(MetadataTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 69</div> <blockquote> Each
     * <code>md_scope</code> column value in a <code>gpkg_metadata</code> table
     * or updateable view SHALL be one of the name column values from <a
     * href="http://www.geopackage.org/spec/#metadata_scopes">Metadata
     * Scopes</a>. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement (number    = 69,
                  text      = "Each md_scope column value in a gpkg_metadata table or "
                              + "updateable view SHALL be one of the name column values from Metadata Scopes. ",
                  severity  = Severity.Warning)
    public void Requirement69() throws AssertionError
    {
        if(this.hasMetadataTable)
        {
            final List<Metadata> invalidMetadataValues = this.metadataValues.stream()
                                                                            .filter(metadata -> !MetadataVerifier.validMdScope(metadata.md_scope))
                                                                            .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following md_scope(s) are invalid values in the %s table: %s",
                                            GeoPackageMetadata.MetadataTableName,
                                            invalidMetadataValues.stream()
                                                                 .map(value -> value.md_scope)
                                                                 .collect(Collectors.joining(", "))),
                             invalidMetadataValues.isEmpty());
        }
    }

    /**
     * <div class="title">Requirement 70</div> <blockquote> A GeoPackage that
     * contains a <code>gpkg_metadata</code> table SHALL contain a
     * <code>gpkg_metadata_reference</code> table per clause 2.4.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#metadata_reference_table_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_reference_cols"
     * >Metadata Reference Table Definition (Table Name:
     * gpkg_metadata_reference)</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_reference_sql"
     * >gpkg_metadata_reference Table Definition SQL</a>. </blockquote> </div>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if the method verifyTable throws
     */
    @Requirement (number   = 70,
                  text     = "A GeoPackage that contains a gpkg_metadata table SHALL contain a "
                              + "gpkg_metadata_reference table per clause 2.4.3.1.1 Table Definition, "
                              + "Metadata Reference Table Definition (Table Name: gpkg_metadata_reference) "
                              + "and gpkg_metadata_reference Table Definition SQL. ",
                  severity = Severity.Error)
    public void Requirement70() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            Assert.assertTrue(String.format("This contains a %1$s table but not a %2$s table.  "
                                            + "Either drop the %1$s table or add a %2$s table",
                                            GeoPackageMetadata.MetadataTableName,
                                            GeoPackageMetadata.MetadataReferenceTableName),
                              this.hasMetadataReferenceTable);

            this.verifyTable(MetadataVerifier.MetadataReferenceTableDefinition);
        }
    }

    /**
     * <div class="title">Requirement 71</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table reference scope column value
     * SHALL be one of ‘geopackage’, ‘table’, ‘column’, ’row’, ’row/col’ in
     * lowercase. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement (number   = 71,
                  text     = "Every gpkg_metadata_reference table reference scope column "
                              + "value SHALL be one of ‘geopackage’, ‘table’, ‘column’, ’row’, "
                              + "’row/col’ in lowercase. ",
                  severity = Severity.Warning)
    public void Requirement71() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataReference>  invalidMetadataReferenceValues = this.metadataReferenceValues.stream()
                                                                                                        .filter(value -> !MetadataVerifier.validReferenceScope(value.reference_scope))
                                                                                                        .collect(Collectors.toList());
            Assert.assertTrue(String.format("The following reference_scope value(s) are invalid from the %s table: %s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            invalidMetadataReferenceValues.stream()
                                                                           .map(value -> value.reference_scope)
                                                                           .collect(Collectors.joining(", "))),
                              invalidMetadataReferenceValues.isEmpty());
        }
    }

    /**
     * <div class="title">Requirement 72</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row with a
     * <code>reference_scope</code> column value of ‘geopackage’ SHALL have a
     * <code>table_name</code> column value that is NULL. Every other
     * <code>gpkg_metadata_reference</code> table row SHALL have a
     * <code>table_name</code> column value that references a value in the
     * <code>gpkg_contents</code> <code>table_name</code> column. </blockquote>
     * </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement (number   = 72,
                  text     = "Every gpkg_metadata_reference table row with a reference_scope column "
                              + "value of ‘geopackage’ SHALL have a table_name column value that is NULL. "
                              + "Every other gpkg_metadata_reference table row SHALL have a table_name column"
                              + " value that references a value in the gpkg_contents table_name column. ",
                  severity = Severity.Warning)
    public void Requirement72() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            //check reference_scope column that has 'geopackage'
            final List<MetadataReference> invalidGeoPackageValue = this.metadataReferenceValues.stream()
                                                                                         .filter(columnValue -> columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()))
                                                                                         .filter(columnValue -> columnValue.column_name != null)
                                                                                         .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following column_name value(s) from %s table are invalid.  "
                                            + "They have a reference_scope = 'geopackage' and a non-null value in column_name: %s.",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            invalidGeoPackageValue.stream()
                                                                  .map(columnValue -> columnValue.column_name)
                                                                  .collect(Collectors.joining(", "))),
                              invalidGeoPackageValue.isEmpty());

            //get table_name values from the gpkg_contents table
            final String query = String.format("SELECT table_name FROM %s;", GeoPackageCore.ContentsTableName);
            try(PreparedStatement stmt                 = this.getSqliteConnection().prepareStatement(query);
                ResultSet         contentsTableNamesRS = stmt.executeQuery())
            {
              final List<String> contentsTableNames = ResultSetStream.getStream(contentsTableNamesRS)
                                                               .map(resultSet ->  { try
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

              //check other records that does not have 'geopackage' as a value
              final List<MetadataReference> invalidTableNameValues = this.metadataReferenceValues.stream()
                                                                                                 .filter(columnValue -> !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()))
                                                                                                 .filter(columnValue -> contentsTableNames.stream().anyMatch(contentsTableName -> !columnValue.table_name.equals(contentsTableName)))
                                                                                                 .collect(Collectors.toList());
              Assert.assertTrue(String.format("The following table_name value(s) in %s table are invalid. "
                                               + "The table_name value(s) must reference the table_name(s) in %s table. "
                                               + " \n%s",
                                               GeoPackageMetadata.MetadataReferenceTableName,
                                               GeoPackageCore.ContentsTableName,
                                              invalidTableNameValues.stream()
                                                                    .map(tableName -> String.format("reference_scope: %s, invalid table_name: %s.",
                                                                                                    tableName.reference_scope,
                                                                                                    tableName.table_name))
                                                                    .collect(Collectors.joining("\n"))),
                                invalidTableNameValues.isEmpty());

            }
        }
    }

    /**
     * <div class="title">Requirement 73</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row with a
     * <code>reference_scope</code> column value of ‘geopackage’,‘table’ or
     * ‘row’ SHALL have a <code>column_name</code> column value that is NULL.
     * Every other <code>gpkg_metadata_reference</code> table row SHALL have a
     * <code>column_name</code> column value that contains the name of a column
     * in the SQLite table or view identified by the <code>table_name</code>
     * column value. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement (number   = 73,
                  text     = "Every gpkg_metadata_reference table row with a reference_scope column "
                              + "value of ‘geopackage’,‘table’ or ‘row’ SHALL have a column_name column"
                              + " value that is NULL. Every other gpkg_metadata_reference table row SHALL"
                              + " have a column_name column value that contains the name of a column in the "
                              + "SQLite table or view identified by the table_name column value. ",
                  severity = Severity.Warning)
    public void Requirement73() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataReference> invalidColumnNameValues = this.metadataReferenceValues.stream()
                                                                                                .filter(columnValue -> columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                                       columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                                       columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Row.toString()))
                                                                                                .filter(columnValue -> columnValue.column_name != null)
                                                                                                .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following column_name values from %s table are invalid. "
                                                + "They contain a reference_scope of either 'geopackage', 'table' or 'row' and "
                                                + "need to have a column_value of NULL.\n%s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            invalidColumnNameValues.stream()
                                                                   .map(value -> String.format("reference_scope: %s, invalid column_name: %s.", value.reference_scope, value.column_name))
                                                                   .collect(Collectors.joining("\n"))),
                              invalidColumnNameValues.isEmpty());

            final List<MetadataReference> otherReferenceScopeValues = this.metadataReferenceValues.stream()
                                                                                                  .filter(columnValue -> !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                                         !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                                         !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Row.toString()))
                                                                                                  .collect(Collectors.toList());
            for(final MetadataReference value: otherReferenceScopeValues)
            {
                if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), value.table_name))
                {
                    final String query = "PRAGMA table_info('?');";

                    try(PreparedStatement stmt      = this.getSqliteConnection().prepareStatement(query))
                    {
                        stmt.setString(1, value.table_name);

                        try( ResultSet tableInfo = stmt.executeQuery(query))
                        {
                           final boolean columnExists = ResultSetStream.getStream(tableInfo)
                                                                       .anyMatch(resultSet -> {  try
                                                                                                 {
                                                                                                     return resultSet.getString("name").equals(value.column_name);
                                                                                                 }
                                                                                                 catch(final SQLException ex)
                                                                                                 {
                                                                                                     return false;
                                                                                                 }
                                                                                               });
                            Assert.assertTrue(String.format("The column_name %s referenced in the %s table doesn't exist in the table %s.",
                                                            value.column_name,
                                                            GeoPackageMetadata.MetadataReferenceTableName,
                                                            value.table_name),
                                             columnExists);
                        }
                    }
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 74</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row with a
     * <code>reference_scope</code> column value of ‘geopackage’, ‘table’ or
     * ‘column’ SHALL have a <code>row_id_value</code> column value that is
     * NULL. Every other <code>gpkg_metadata_reference</code> table row SHALL
     * have a <code>row_id_value</code> column value that contains the ROWID of
     * a row in the SQLite table or view identified by the
     * <code>table_name</code> column value. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement (number    = 74,
                  text      = "Every gpkg_metadata_reference table row with a reference_scope column value "
                                  + "of ‘geopackage’, ‘table’ or ‘column’ SHALL have a row_id_value column value "
                                  + "that is NULL. Every other gpkg_metadata_reference table row SHALL have a row_id_value"
                                  + " column value that contains the ROWID of a row in the SQLite table or view identified "
                                  + "by the table_name column value. ",
                  severity  = Severity.Warning)
    public void Requirement74() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataReference> invalidColumnValues = this.metadataReferenceValues.stream()
                                                                                            .filter(columnValue -> columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                                   columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                                   columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Column.toString()))
                                                                                            .filter(columnValue -> columnValue.row_id_value != null)
                                                                                            .collect(Collectors.toList());
            Assert.assertTrue(String.format("The following row_id_value(s) has(have) a reference_scope value of 'geopackage', "
                                                + "'table' or 'column and do not have a value of NULL in row_id_value.\n %s",
                                            invalidColumnValues.stream()
                                                               .map(columnValue -> String.format("reference_scope: %s, invalid row_id_value: %d.", columnValue.reference_scope, columnValue.row_id_value))
                                                               .collect(Collectors.joining("\n"))),
                              invalidColumnValues.isEmpty());

            final List<MetadataReference> invalidColumnNameValues = this.metadataReferenceValues.stream()
                                                                                          .filter(columnValue -> !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                                 !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                                 !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Column.toString()))
                                                                                          .collect(Collectors.toList());
            for(final MetadataReference value: invalidColumnNameValues)
            {
                final String query = "SELECT * FROM ? WHERE ROWID = ?;";

                try(PreparedStatement stmt = this.getSqliteConnection().prepareStatement(query))
                {
                    stmt.setString(1, value.table_name);
                    stmt.setInt(2, value.row_id_value);

                    try(ResultSet matchingRowIdRS = stmt.executeQuery())
                    {
                        Assert.assertTrue(String.format("The row_id_value %d in the %s table does not reference a row id in the table %s.",
                                                        value.row_id_value,
                                                        GeoPackageMetadata.MetadataReferenceTableName,
                                                        value.table_name),
                                         matchingRowIdRS.next());
                    }
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 75</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row timestamp column value
     * SHALL be in ISO 8601 format containing a complete
     * date plus UTC hours, minutes, seconds and a decimal fraction of a second,
     * with a ‘Z’ (‘zulu’) suffix indicating UTC.
     * </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement (number   = 75,
                  text     = "Every gpkg_metadata_reference table row timestamp column value "
                              + "SHALL be in ISO 8601 [29] format containing a complete date plus "
                              + "UTC hours, minutes, seconds and a decimal fraction of a second, with "
                              + "a ‘Z’ (‘zulu’) suffix indicating UTC.",
                  severity = Severity.Warning)
    public void Requirement75() throws AssertionError
    {
        if (this.hasMetadataReferenceTable)
        {
            for (final MetadataReference value : this.metadataReferenceValues)
            {
                try
                {
                    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");

                    formatter.parse(value.timestamp);
                }
                catch (final ParseException ex)
                {
                    final SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    try
                    {
                        formatter2.parse(value.timestamp);
                    }
                    catch (final ParseException e)
                    {
                        fail(String.format("The timestamp %s in the %s table is not in the correct format.",
                                           value.timestamp,
                                           GeoPackageMetadata.MetadataReferenceTableName));
                    }
                }
            }
        }
    }

    /**
     * <div class="title">Requirement 76</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row <code>md_file_id</code>
     * column value SHALL be an id column value from the
     * <code>gpkg_metadata</code> table. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement (number   = 76,
                  text     = "Every gpkg_metadata_reference table row md_file_id column "
                               + "value SHALL be an id column value from the gpkg_metadata table. ",
                  severity = Severity.Warning)
    public void Requirement76() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataReference> invalidIds = this.metadataReferenceValues.stream()
                                                                                   .filter(metadataReferenceValue ->
                                                                                                               !(this.metadataValues.stream()
                                                                                                                                    .anyMatch(metadataValue ->
                                                                                                                                                       metadataReferenceValue.md_file_id.equals(metadataValue.id))))
                                                                                   .collect(Collectors.toList());
            Assert.assertTrue(String.format("The following md_file_id(s) from %s table "
                                               + "do not reference an id column value from the %s table.\n%s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            GeoPackageMetadata.MetadataTableName,
                                            invalidIds.stream()
                                                      .map(invalidId -> String.format("invalid md_file_id: %s, md_parent_id: %d, reference_scope: %s.",
                                                                                      invalidId.md_file_id,
                                                                                      invalidId.md_parent_id,
                                                                                      invalidId.reference_scope))
                                                      .collect(Collectors.joining("\n"))),
                              invalidIds.isEmpty());
        }
    }

    /**
     * <div class="title">Requirement 77</div> <blockquote> Every
     * <code>gpkg_metadata_reference</code> table row <code>md_parent_id</code>
     * column value that is NOT NULL SHALL be an id column value from the
     * <code>gpkg_metadata</code> table that is not equal to the
     * <code>md_file_id</code> column value for that row. </blockquote> </div>
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(number  = 77,
                 text    = "Every gpkg_metadata_reference table row md_parent_id column value "
                             + "that is NOT NULL SHALL be an id column value from the gpkg_metadata "
                             + "table that is not equal to the md_file_id column value for that row. ",
                 severity = Severity.Warning)
    public void Requirement77() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataReference> invalidParentIdsBcFileIds = this.metadataReferenceValues.stream()
                                                                                                  .filter(metadataReferenceValue -> metadataReferenceValue.md_file_id.equals(metadataReferenceValue.md_parent_id))
                                                                                                  .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following md_parent_id(s) are invalid because they cannot be equivalent "
                                                + "to their correspoding md_file_id.\n%s",
                                            invalidParentIdsBcFileIds.stream()
                                                                     .map(value -> String.format("Invalid md_parent_id: %d,  md_file_id: %d.", value.md_parent_id, value.md_file_id))
                                                                     .collect(Collectors.joining("\n"))),
                              invalidParentIdsBcFileIds.isEmpty());

            final List<MetadataReference> invalidParentIds = this.metadataReferenceValues.stream()
                                                                                         .filter(metadataReferenceValue -> metadataReferenceValue.md_parent_id != null)
                                                                                         .filter(metadataReferenceValue -> !(this.metadataValues.stream()
                                                                                                                                                .anyMatch(metadataValue ->  metadataReferenceValue.md_parent_id.equals(metadataValue.id))))
                                                                                         .collect(Collectors.toList());

            Assert.assertTrue(String.format("The following md_parent_id value(s) are invalid because they do not equal id column value from the %s table. \n%s",
                                            GeoPackageMetadata.MetadataTableName,
                                            invalidParentIds.stream()
                                                            .map(value -> String.format("Invalid md_parent_id: %d,  md_file_id: %d.", value.md_parent_id, value.md_file_id))
                                                            .collect(Collectors.joining("\n"))),
                              invalidParentIds.isEmpty());

        }
    }

    /**
     * This will provide a List of MetadataReferences
     * records in the current GeoPackage from the
     * gpkg_metadata_references table
     * @return a list of MetadataReference's records
     */
    private List<MetadataReference> getMetadataReferenceValues()
    {

        final String query = String.format("SELECT reference_scope, table_name, column_name, row_id_value, timestamp, md_file_id, md_parent_id FROM %s;", GeoPackageMetadata.MetadataReferenceTableName);

        try(Statement stmt            = this.getSqliteConnection().createStatement();
            ResultSet metadataValueRS = stmt.executeQuery(query))
            {
                return ResultSetStream.getStream(metadataValueRS)
                                      .map(resultSet -> {  try
                                                           {
                                                                final MetadataReference metadataReference = new MetadataReference();

                                                                metadataReference.reference_scope   = resultSet.getString("reference_scope");
                                                                metadataReference.table_name        = resultSet.getString("table_name");
                                                                if(resultSet.wasNull())
                                                                {
                                                                    metadataReference.table_name = null;
                                                                }
                                                                metadataReference.column_name       = resultSet.getString("column_name");
                                                                if(resultSet.wasNull())
                                                                {
                                                                    metadataReference.column_name = null;
                                                                }
                                                                metadataReference.row_id_value      = resultSet.getInt("row_id_value");
                                                                if(resultSet.wasNull())
                                                                {
                                                                    metadataReference.row_id_value = null;
                                                                }
                                                                metadataReference.timestamp         = resultSet.getString("timestamp");
                                                                metadataReference.md_file_id        = resultSet.getInt("md_file_id");
                                                                metadataReference.md_parent_id      = resultSet.getInt("md_parent_id");
                                                                if(resultSet.wasNull())
                                                                {
                                                                    metadataReference.md_parent_id = null;
                                                                }

                                                                return metadataReference;
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
    /**
     * This will provide a List of Metadata
     * records in the current GeoPackage from
     * the gpkg_metadata table
     * @return a list of Metadata's records
     */
    private List<Metadata> getMetadataValues()
    {
        final String query = String.format("SELECT md_scope, id FROM %s;", GeoPackageMetadata.MetadataTableName);

        try(Statement stmt            = this.getSqliteConnection().createStatement();
            ResultSet metadataValueRS = stmt.executeQuery(query))
            {
                return ResultSetStream.getStream(metadataValueRS)
                                      .map(resultSet -> {  try
                                                           {
                                                                final Metadata metadata = new Metadata();
                                                                metadata.id = resultSet.getInt("id");
                                                                metadata.md_scope = resultSet.getString("md_scope");

                                                                return metadata;
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
    /**
     * Verifies that the reference_scope
     * value given is a valid value
     * (one of the predefined scope values)
     * @param referenceScope
     * @return true if the scope is allowed
     */
    private static boolean validReferenceScope(final String referenceScope)
    {
        return Stream.of(ReferenceScope.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(referenceScope));
    }
    /**
     * Verifies that the metadata scope
     * value given is a valid value
     * (one of the predefined scope values)
     * @param mdScope
     * @return true if the scope is allowed
     */
    private static boolean validMdScope(final String mdScope)
    {
        return Stream.of(Scope.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(mdScope));
    }

    private final boolean                 hasMetadataTable;
    private final boolean                 hasMetadataReferenceTable;
    private final List<Metadata>          metadataValues;
    private final List<MetadataReference> metadataReferenceValues;

    private class Metadata
    {
        int    id;
        String md_scope;
    }

    private class MetadataReference
    {
        String  reference_scope;
        String  table_name;
        String  column_name;
        Integer row_id_value;
        String  timestamp;
        Integer md_file_id;
        Integer md_parent_id;
    }

    private static final TableDefinition MetadataTableDefinition;
    private static final TableDefinition MetadataReferenceTableDefinition;
    static
    {
        final Map<String, ColumnDefinition> metadataTableColumns = new HashMap<>();

        metadataTableColumns.put("id",               new ColumnDefinition("INTEGER", true,  true, false, null));
        metadataTableColumns.put("md_scope",         new ColumnDefinition("TEXT",    true, false, false, "'\\s*dataset\\s*'||\"\\s*dataset\\s*\""));
        metadataTableColumns.put("md_standard_uri",  new ColumnDefinition("TEXT",    true, false, false, null));
        metadataTableColumns.put("mime_type",        new ColumnDefinition("TEXT",    true, false, false, "['\"]\\s*text[/\\\\]xml\\s*['\"]"));
        metadataTableColumns.put("metadata",         new ColumnDefinition("TEXT",    true, false, false, "\\s*''\\s*|\\s*\"\"\\s*"));

        MetadataTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataTableName,
                                                      metadataTableColumns);

        final Map<String, ColumnDefinition> metadataReferenceTableColumns = new HashMap<>();

        metadataReferenceTableColumns.put("reference_scope",  new ColumnDefinition("TEXT",     true,  false, false, null));
        metadataReferenceTableColumns.put("table_name",       new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("column_name",      new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("row_id_value",     new ColumnDefinition("INTEGER",  false, false, false, null));
        metadataReferenceTableColumns.put("timestamp",        new ColumnDefinition("DATETIME", true,  false, false, "\\s*strftime\\s*\\(\\s*['\"]%Y-%m-%dT%H:%M:%fZ['\"]\\s*,\\s*['\"]now['\"]\\s*\\)\\s*"));
        metadataReferenceTableColumns.put("md_file_id",       new ColumnDefinition("INTEGER",  true,  false, false, null));
        metadataReferenceTableColumns.put("md_parent_id",     new ColumnDefinition("INTEGER",  false, false, false, null));

        MetadataReferenceTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataReferenceTableName,
                                                               metadataReferenceTableColumns,
                                                               new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_parent_id", "id"),
                                                                                           new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_file_id", "id"))));


    }

}
