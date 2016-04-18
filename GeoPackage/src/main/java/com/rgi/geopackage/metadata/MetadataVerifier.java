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

import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rgi.geopackage.verification.Assert.assertTrue;

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

        this.metadataValues = this.hasMetadataTable ? JdbcUtility.select(this.getSqliteConnection(),
                                                                         String.format("SELECT md_scope, id FROM %s;", GeoPackageMetadata.MetadataTableName),
                                                                         null,
                                                                         resultSet -> new MetadataVerifier.MetadataEntry(resultSet.getInt("id"),
                                                                                                                         resultSet.getString("md_scope")))
                                                    : Collections.emptyList();

        final String metadataReferencesQuery = String.format("SELECT reference_scope, table_name, column_name, row_id_value, timestamp, md_file_id, md_parent_id FROM %s;", GeoPackageMetadata.MetadataReferenceTableName);

        this.metadataReferenceValues = this.hasMetadataReferenceTable ? JdbcUtility.select(this.getSqliteConnection(),
                                                                                           metadataReferencesQuery,
                                                                                           null,
                                                                                           resultSet -> new MetadataVerifier.MetadataReference(resultSet.getString("reference_scope"),
                                                                                                                                               resultSet.getString("table_name"),
                                                                                                                                               resultSet.getString("column_name"),
                                                                                                                                               resultSet.getString("timestamp"),
                                                                                                                                               resultSet.getInt   ("md_file_id"),      // Cannot be null
                                                                                                                                               nullSafeGet(resultSet, "row_id_value"), // getInt() returns 0 if the value in the database was null
                                                                                                                                               nullSafeGet(resultSet, "md_parent_id")))
                                                                      : Collections.emptyList();
    }

    /**
     * Requirement 93
     *
     * <blockquote>
     * A GeoPackage MAY contain a table named gpkg_metadata. If present it
     * SHALL be defined per clause 2.4.2.1.1 <a href=
     * "http://www.geopackage.org/spec/#metadata_table_table_definition">Table
     * Definition</a>, <a href=
     * "http://www.geopackage.org/spec/#gpkg_metadata_cols">Metadata Table
     * Definition</a> and <a href=
     * "http://www.geopackage.org/spec/#gpkg_metadata_sql">gpkg_metadata Table
     * Definition SQL</a>.
     * </blockquote>
     *
     * @throws SQLException
     *             if the method verifyTable throws
     * @throws AssertionError
     *             when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 93",
                 text      = "A GeoPackage MAY contain a table named gpkg_metadata. If present it SHALL be defined per clause 2.4.2.1.1 Table Definition, Metadata Table Definition and gpkg_metadata Table Definition SQL.")
    public void Requirement93() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            final Map<String, ColumnDefinition> metadataTableColumns = new HashMap<>();

            metadataTableColumns.put("id",              new ColumnDefinition("INTEGER", true, true,  false, null));
            metadataTableColumns.put("md_scope",        new ColumnDefinition("TEXT",    true, false, false, "'dataset'"));
            metadataTableColumns.put("md_standard_uri", new ColumnDefinition("TEXT",    true, false, false, null));
            metadataTableColumns.put("mime_type",       new ColumnDefinition("TEXT",    true, false, false, "'text/xml'"));
            metadataTableColumns.put("metadata",        new ColumnDefinition("TEXT",    true, false, false, "''"));

            this.verifyTable(GeoPackageMetadata.MetadataTableName,
                             metadataTableColumns,
                             Collections.emptySet(),
                             Collections.emptyList());
        }
    }

    /**
     * Requirement 94
     *
     * <blockquote>
     * Each {@code md_scope} column value in a {@code gpkg_metadata} table or
     * updateable view SHALL be one of the name column values from <a href=
     * "http://www.geopackage.org/spec/#metadata_scopes">Metadata Scopes</a>.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 94",
                 text      = "Each md_scope column value in a gpkg_metadata table or updateable view SHALL be one of the name column values from Metadata Scopes.")
    public void Requirement94() throws AssertionError
    {
        if(this.hasMetadataTable)
        {
            final List<String> invalidScopeValues = this.metadataValues.stream()
                                                        .filter(metadata -> !MetadataVerifier.isValidMdScope(metadata.mdScope))
                                                        .map(metadata -> metadata.mdScope)
                                                        .collect(Collectors.toList());

            assertTrue(String.format("The following md_scope(s) are invalid values in the %s table: %s",
                                     GeoPackageMetadata.MetadataTableName,
                                     String.join(", ", invalidScopeValues)),
                      invalidScopeValues.isEmpty(),
                      Severity.Warning);
        }
    }

    /**
     * Requirement 95
     *
     * <blockquote>
     * A GeoPackage that contains a {@code gpkg_metadata} table SHALL contain a
     * {@code gpkg_metadata_reference} table per clause 2.4.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#metadata_reference_table_table_definition"
     * >Table Definition</a>, <a href=
     * "http://www.geopackage.org/spec/#gpkg_metadata_reference_cols">Metadata
     * Reference Table Definition (Table Name: gpkg_metadata_reference)</a> and
     * <a href="http://www.geopackage.org/spec/#gpkg_metadata_reference_sql">
     * gpkg_metadata_reference Table Definition SQL</a>.
     * </blockquote>
     *
     * @throws SQLException
     *             if there is a database error
     * @throws AssertionError
     *            when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 95",
                 text      = "A GeoPackage that contains a gpkg_metadata table SHALL contain a gpkg_metadata_reference table per clause 2.4.3.1.1 Table Definition, Metadata Reference Table Definition (Table Name: gpkg_metadata_reference) and gpkg_metadata_reference Table Definition SQL.")
    public void Requirement95() throws SQLException, AssertionError
    {
        if(this.hasMetadataTable)
        {
            assertTrue(String.format("This contains a %1$s table but not a %2$s table. Either drop the %1$s table or add a %2$s table",
                                     GeoPackageMetadata.MetadataTableName,
                                     GeoPackageMetadata.MetadataReferenceTableName),
                       this.hasMetadataReferenceTable,
                       Severity.Error);

            final Map<String, ColumnDefinition> metadataReferenceTableColumns = new HashMap<>();

            metadataReferenceTableColumns.put("reference_scope", new ColumnDefinition("TEXT",     true,  false, false, null));
            metadataReferenceTableColumns.put("table_name",      new ColumnDefinition("TEXT",     false, false, false, null));
            metadataReferenceTableColumns.put("column_name",     new ColumnDefinition("TEXT",     false, false, false, null));
            metadataReferenceTableColumns.put("row_id_value",    new ColumnDefinition("INTEGER",  false, false, false, null));
            metadataReferenceTableColumns.put("timestamp",       new ColumnDefinition("DATETIME", true,  false, false, "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')"));
            metadataReferenceTableColumns.put("md_file_id",      new ColumnDefinition("INTEGER",  true,  false, false, null));
            metadataReferenceTableColumns.put("md_parent_id",    new ColumnDefinition("INTEGER",  false, false, false, null));

            this.verifyTable(GeoPackageMetadata.MetadataReferenceTableName,
                             metadataReferenceTableColumns,
                             new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_parent_id", "id"),
                                                         new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_file_id",   "id"))),
                             Collections.emptyList());
        }
    }

    /**
     * Requirement 96
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table reference scope column value
     * SHALL be one of 'geopackage', 'table', 'column', 'row', 'row/col' in
     * lowercase.
     * </blockquote>
     *
     * @throws AssertionError
     *             when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 96",
                 text      = "Every gpkg_metadata_reference table reference scope column value SHALL be one of 'geopackage', 'table', 'column', 'row', 'row/col' in lowercase.")
    public void Requirement96() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final Collection<String> invalidScopeValues = this.metadataReferenceValues
                                                              .stream()
                                                              .filter(value -> !MetadataVerifier.isValidReferenceScope(value.referenceScope))
                                                              .map(value -> value.referenceScope)
                                                              .collect(Collectors.toList());

            assertTrue(String.format("The following reference_scope value(s) are invalid from the %s table: %s",
                                     GeoPackageMetadata.MetadataReferenceTableName,
                                     String.join(", ", invalidScopeValues)),
                       invalidScopeValues.isEmpty(),
                       Severity.Warning);
        }
    }

    /**
     * Requirement 97
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row with a {@code
     * reference_scope} column value of 'geopackage' SHALL have a {@code
     * table_name} column value that is NULL. Every other {@code
     * gpkg_metadata_reference} table row SHALL have a {@code table_name}
     * column value that references a value in the {@code gpkg_contents}
     * {@code table_name} column.
     * </blockquote>
     *
     * @throws SQLException
     *             if there is a database error
     * @throws AssertionError
     *             when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 97",
                 text      = "Every gpkg_metadata_reference table row with a reference_scope column value of 'geopackage' SHALL have a table_name column value that is NULL. Every other gpkg_metadata_reference table row SHALL have a table_name column value that references a value in the gpkg_contents table_name column.")
    public void Requirement97() throws SQLException, AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            // Check reference_scope column that has 'geopackage'
            final List<MetadataVerifier.MetadataReference> invalidGeoPackageValue = this.metadataReferenceValues.stream()
                                                                                         .filter(columnValue -> columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()))
                                                                                         .filter(columnValue -> columnValue.columnName != null)
                                                                                         .collect(Collectors.toList());

            assertTrue(String.format("The following column_name value(s) from %s table are invalid. They have a reference_scope = 'geopackage' and a non-null value in column_name: %s.",
                                     GeoPackageMetadata.MetadataReferenceTableName,
                                     invalidGeoPackageValue.stream()
                                                           .map(columnValue -> columnValue.columnName)
                                                           .collect(Collectors.joining(", "))),
                       invalidGeoPackageValue.isEmpty(),
                       Severity.Warning);

            // Get table_name values from the gpkg_contents table
            final String query = String.format("SELECT table_name FROM %s;", GeoPackageCore.ContentsTableName);

            try(PreparedStatement stmt                 = this.getSqliteConnection().prepareStatement(query);
                ResultSet         contentsTableNamesRS = stmt.executeQuery())
            {
                final List<String> contentsTableNames = ResultSetStream.getStream(contentsTableNamesRS)
                                                                       .map(resultSet ->  { try
                                                                                            {
                                                                                                return resultSet.getString("table_name");
                                                                                            }
                                                                                            catch(final SQLException ignored)
                                                                                            {
                                                                                                return null;
                                                                                            }
                                                                                          })
                                                                       .filter(Objects::nonNull)
                                                                       .collect(Collectors.toList());

                //check other records that does not have 'geopackage' as a value
                final List<MetadataVerifier.MetadataReference> invalidTableNameValues = this.metadataReferenceValues.stream()
                                                                                                   .filter(columnValue -> !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()))
                                                                                                   .filter(columnValue -> contentsTableNames.stream().anyMatch(contentsTableName -> !columnValue.tableName.equals(contentsTableName)))
                                                                                                   .collect(Collectors.toList());

                assertTrue(String.format("The following table_name value(s) in %s table are invalid. The table_name value(s) must reference the table_name(s) in %s table.\n%s",
                                         GeoPackageMetadata.MetadataReferenceTableName,
                                         GeoPackageCore.ContentsTableName,
                                         invalidTableNameValues.stream()
                                                               .map(tableName -> String.format("reference_scope: %s, invalid table_name: %s.",
                                                                                               tableName.referenceScope,
                                                                                               tableName.tableName))
                                                               .collect(Collectors.joining("\n"))),
                           invalidTableNameValues.isEmpty(),
                           Severity.Warning);
            }
        }
    }

    /**
     * Requirement 98
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row with a {@code
     * reference_scope} column value of 'geopackage','table' or 'row'
     * SHALL have a {@code column_name} column value that is NULL. Every
     * other {@code gpkg_metadata_reference} table row SHALL have a {@code
     * column_name} column value that contains the name of a column in
     * the SQLite table or view identified by the {@code table_name}
     * column value.
     * <blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement(reference = "Requirement 98",
                 text    = "Every gpkg_metadata_reference table row with a reference_scope column "
                           + "value of 'geopackage','table' or 'row' SHALL have a column_name column"
                           + " value that is NULL. Every other gpkg_metadata_reference table row SHALL"
                           + " have a column_name column value that contains the name of a column in the "
                           + "SQLite table or view identified by the table_name column value. ")
    public void Requirement98() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataVerifier.MetadataReference> invalidColumnNameValues = this.metadataReferenceValues.stream()
                                                                                                .filter(columnValue -> columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                                       columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                                       columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Row.toString()))
                                                                                                .filter(columnValue -> columnValue.columnName != null)
                                                                                                .collect(Collectors.toList());

            assertTrue(String.format("The following column_name values from %s table are invalid. They contain a reference_scope of either 'geopackage', 'table' or 'row' and need to have a column_value of NULL.\n%s",
                                     GeoPackageMetadata.MetadataReferenceTableName,
                                     invalidColumnNameValues.stream()
                                                            .map(value -> String.format("reference_scope: %s, invalid column_name: %s.", value.referenceScope, value.columnName))
                                                            .collect(Collectors.joining("\n"))),
                       invalidColumnNameValues.isEmpty(),
                       Severity.Warning);

            final List<MetadataVerifier.MetadataReference> otherReferenceScopeValues = this.metadataReferenceValues.stream()
                                                                                                  .filter(columnValue -> !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                                         !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                                         !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Row.toString()))
                                                                                                  .collect(Collectors.toList());
            for(final MetadataVerifier.MetadataReference value : otherReferenceScopeValues)
            {
                if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), value.tableName))
                {
                    final String query = "PRAGMA table_info('?');";

                    try(PreparedStatement statement = this.getSqliteConnection().prepareStatement(query))
                    {
                        statement.setString(1, value.tableName);

                        try( ResultSet tableInfo = statement.executeQuery(query))
                        {
                           final boolean columnExists = ResultSetStream.getStream(tableInfo)
                                                                       .anyMatch(resultSet -> {  try
                                                                                                 {
                                                                                                     return resultSet.getString("name").equals(value.columnName);
                                                                                                 }
                                                                                                 catch(final SQLException ignored)
                                                                                                 {
                                                                                                     return false;
                                                                                                 }
                                                                                               });
                           assertTrue(String.format("The column_name %s referenced in the %s table doesn't exist in the table %s.",
                                                    value.columnName,
                                                    GeoPackageMetadata.MetadataReferenceTableName,
                                                    value.tableName),
                                      columnExists,
                                      Severity.Warning);
                        }
                    }
                }
            }
        }
    }

    /**
     * Requirement 75
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row with a {@code
     * reference_scope} column value of 'geopackage', 'table' or 'column'
     * SHALL have a {@code row_id_value} column value that is NULL. Every
     * other {@code gpkg_metadata_reference} table row SHALL have a {@code
     * row_id_value} column value that contains the ROWID of a row in the
     * SQLite table or view identified by the {@code table_name} column
     * value.
     * <blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement(reference = "Requirement 75",
                 text      = "Every gpkg_metadata_reference table row with a reference_scope column value "
                             + "of 'geopackage', 'table' or 'column' SHALL have a row_id_value column value "
                             + "that is NULL. Every other gpkg_metadata_reference table row SHALL have a row_id_value"
                             + " column value that contains the ROWID of a row in the SQLite table or view identified "
                             + "by the table_name column value.")
    public void Requirement75() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataVerifier.MetadataReference> invalidColumnValues = this.metadataReferenceValues.stream()
                                                                                            .filter(columnValue -> columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                                   columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                                   columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Column.toString()))
                                                                                            .filter(columnValue -> columnValue.rowIdValue != null)
                                                                                            .collect(Collectors.toList());
            assertTrue(String.format("The following row_id_value(s) has(have) a reference_scope value of 'geopackage', "
                                         + "'table' or 'column and do not have a value of NULL in row_id_value.\n %s",
                                     invalidColumnValues.stream()
                                                        .map(columnValue -> String.format("reference_scope: %s, invalid row_id_value: %d.", columnValue.referenceScope, columnValue.rowIdValue))
                                                        .collect(Collectors.joining("\n"))),
                       invalidColumnValues.isEmpty(),
                       Severity.Warning);

            final List<MetadataVerifier.MetadataReference> invalidColumnNameValues = this.metadataReferenceValues.stream()
                                                                                         .filter(columnValue -> !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                                !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                                !columnValue.referenceScope.equalsIgnoreCase(ReferenceScope.Column.toString()))
                                                                                         .collect(Collectors.toList());
            for(final MetadataVerifier.MetadataReference value: invalidColumnNameValues)
            {
                final String query = String.format("SELECT COUNT(1) FROM %s WHERE ROWID = ?;",  // TODO make sure COUNT(1) works the way I think it does...
                                     value.tableName);

                try(PreparedStatement statement = this.getSqliteConnection().prepareStatement(query))
                {
                    statement.setString(1, value.tableName);
                    statement.setInt   (2, value.rowIdValue);

                    try(ResultSet matchingRowIdRS = statement.executeQuery())
                    {
                        assertTrue(String.format("The row_id_value %d in the %s table does not reference a row id in the table %s.",
                                                 value.rowIdValue,
                                                 GeoPackageMetadata.MetadataReferenceTableName,
                                                 value.tableName),
                                   matchingRowIdRS.next(),
                                   Severity.Warning);
                    }
                }
            }
        }
    }

    /**
     * Requirement 76
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row timestamp column
     * value SHALL be in ISO 8601 format containing a complete date plus UTC
     * hours, minutes, seconds and a decimal fraction of a second, with a 'Z'
     * ('zulu') suffix indicating UTC.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 76",
                 text      = "Every gpkg_metadata_reference table row timestamp column value "
                             + "SHALL be in ISO 8601 format containing a complete date plus "
                             + "UTC hours, minutes, seconds and a decimal fraction of a second, with "
                             + "a 'Z' ('zulu') suffix indicating UTC.")
    public void Requirement76() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            for(final MetadataVerifier.MetadataReference value : this.metadataReferenceValues)
            {
                assertTrue(String.format("The timestamp %s in the %s table is not in the correct format.",
                                         value.timestamp,
                                         GeoPackageMetadata.MetadataReferenceTableName),
                           isValidDate(value.timestamp),
                           Severity.Warning);
            }
        }
    }

    /**
     * Requirement 77
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row {@code md_file_id
     * } column value SHALL be an id column value from the {@code
     * gpkg_metadata} table.
     * <blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 77",
                 text      = "Every gpkg_metadata_reference table row md_file_id column "
                             + "value SHALL be an id column value from the gpkg_metadata table. ")
    public void Requirement77() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataVerifier.MetadataReference> invalidIds = this.metadataReferenceValues.stream()
                                                                            .filter(metadataReferenceValue -> !(this.metadataValues.stream()
                                                                                                                                   .anyMatch(metadataValue -> metadataReferenceValue.mdFileId == metadataValue.id)))
                                                                            .collect(Collectors.toList());

            assertTrue(String.format("The following md_file_id(s) from %s table do not reference an id column value from the %s table.\n%s",
                                     GeoPackageMetadata.MetadataReferenceTableName,
                                     GeoPackageMetadata.MetadataTableName,
                                     invalidIds.stream()
                                               .map(invalidId -> String.format("invalid md_file_id: %s, md_parent_id: %d, reference_scope: %s.",
                                                                               invalidId.mdFileId,
                                                                               invalidId.mdParentId,
                                                                               invalidId.referenceScope))
                                               .collect(Collectors.joining("\n"))),
                       invalidIds.isEmpty(),
                       Severity.Warning);
        }
    }

    /**
     * Requirement 78
     *
     * <blockquote>
     * Every {@code gpkg_metadata_reference} table row {@code md_parent_id
     * } column value that is NOT NULL SHALL be an id column value from
     * the {@code gpkg_metadata} table that is not equal to the {@code
     * md_file_id} column value for that row.
     * <blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 78",
                 text      = "Every gpkg_metadata_reference table row md_parent_id column value "
                             + "that is NOT NULL SHALL be an id column value from the gpkg_metadata "
                             + "table that is not equal to the md_file_id column value for that row. ")
    public void Requirement78() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<MetadataVerifier.MetadataReference> invalidParentIdsBcFileIds = this.metadataReferenceValues.stream()
                                                                                                  .filter(metadataReferenceValue -> metadataReferenceValue.mdFileId == metadataReferenceValue.mdParentId)
                                                                                                  .collect(Collectors.toList());

            assertTrue(String.format("The following md_parent_id(s) are invalid because they cannot be equivalent to their correspoding md_file_id.\n%s",
                                     invalidParentIdsBcFileIds.stream()
                                                              .map(value -> String.format("Invalid md_parent_id: %d, md_file_id: %d.",
                                                                                          value.mdParentId,
                                                                                          value.mdFileId))
                                                              .collect(Collectors.joining("\n"))),
                       invalidParentIdsBcFileIds.isEmpty(),
                       Severity.Warning);

            final List<MetadataVerifier.MetadataReference> invalidParentIds = this.metadataReferenceValues.stream()
                                                                                         .filter(metadataReferenceValue -> metadataReferenceValue.mdParentId != null)
                                                                                         .filter(metadataReferenceValue -> !(this.metadataValues.stream()
                                                                                                                                                .anyMatch(metadataValue ->  metadataReferenceValue.mdParentId.equals(metadataValue.id))))
                                                                                         .collect(Collectors.toList());

            assertTrue(String.format("The following md_parent_id value(s) are invalid because they do not equal id column value from the %s table. \n%s",
                                     GeoPackageMetadata.MetadataTableName,
                                     invalidParentIds.stream()
                                                     .map(value -> String.format("Invalid md_parent_id: %d,  md_file_id: %d.",
                                                                                 value.mdParentId,
                                                                                 value.mdFileId))
                                                     .collect(Collectors.joining("\n"))),
                       invalidParentIds.isEmpty(),
                       Severity.Warning);
        }
    }

    private static Integer nullSafeGet(final ResultSet resultSet, final String columnLabel) throws SQLException
    {
        final Integer value = resultSet.getInt(columnLabel);

        return resultSet.wasNull() ? null
                                   : value;
    }

    private static boolean isValidReferenceScope(final String referenceScope)
    {
        return Stream.of(ReferenceScope.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(referenceScope));
    }

    private static boolean isValidMdScope(final String mdScope)
    {
        return Stream.of(Scope.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(mdScope));
    }

    private static boolean isValidDate(final String date)
    {
        final SimpleDateFormat dateFormatWithFractionalSeconds    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");
        final SimpleDateFormat dateFormatWithoutFractionalSeconds = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        return isValidDate(dateFormatWithFractionalSeconds,    date) ||
               isValidDate(dateFormatWithoutFractionalSeconds, date);
    }

    private static boolean isValidDate(final SimpleDateFormat dateFormat, final String date)
    {
        try
        {
            dateFormat.parse(date);
            return true;
        }
        catch(final ParseException ignored)
        {
            return false;
        }
    }

    private final boolean                                  hasMetadataTable;
    private final boolean                                  hasMetadataReferenceTable;
    private final List<MetadataVerifier.MetadataEntry>     metadataValues;
    private final List<MetadataVerifier.MetadataReference> metadataReferenceValues;

    // TODO don't use these inner classes
    private static final class MetadataEntry
    {
        private MetadataEntry(final int    id,
                              final String mdScope)
        {
            this.id      = id;
            this.mdScope = mdScope;
        }

        private final int    id;
        private final String mdScope;

    }

    // TODO don't use these inner classes
    private static final class MetadataReference
    {
        private final String  referenceScope;
        private final String  tableName;
        private final String  columnName;
        private final Integer rowIdValue;
        private final String  timestamp;
        private final int     mdFileId;
        private final Integer mdParentId;

        private MetadataReference(final String  referenceScope,
                                  final String  tableName,
                                  final String  columnName,
                                  final String  timestamp,
                                  final int     mdFileId,
                                  final Integer rowIdValue,
                                  final Integer mdParentId)
        {
            this.referenceScope = referenceScope;
            this.tableName      = tableName;
            this.columnName     = columnName;
            this.rowIdValue     = rowIdValue;
            this.timestamp      = timestamp;
            this.mdFileId       = mdFileId;
            this.mdParentId     = mdParentId;
        }
    }
}
