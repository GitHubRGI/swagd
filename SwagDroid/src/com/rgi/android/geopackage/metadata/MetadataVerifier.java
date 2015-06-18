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

package com.rgi.android.geopackage.metadata;

import static com.rgi.android.geopackage.verification.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.rgi.android.geopackage.verification.VerificationLevel;
import com.rgi.android.geopackage.verification.Verifier;

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
    
    // Requirements are updated
    /**
     * Requirement 69
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
     * @throws SQLException throws if the method verifyTable throws
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 69",
                 text      = "A GeoPackage MAY contain a table named gpkg_metadata."
                             + " If present it SHALL be defined per clause 2.4.2.1.1 "
                             + "Table Definition, Metadata Table Definition and gpkg_metadata "
                             + "Table Definition SQL. ")
    public void Requirement69() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            this.verifyTable(MetadataTableDefinition);
        }
    }

    /**
     * Requirement 70
     *
     * <blockquote>
     * Each <code>md_scope</code> column value in a <code>gpkg_metadata</code>
     * table or updateable view SHALL be one of the name column values from <a
     * href="http://www.geopackage.org/spec/#metadata_scopes">Metadata Scopes
     * </a>.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 70",
                 text      = "Each md_scope column value in a gpkg_metadata table or "
                             + "updateable view SHALL be one of the name column values from Metadata Scopes.")
    public void Requirement70() throws AssertionError
    {
        if(this.hasMetadataTable)
        {
            final Collection<String> invalidScopeValues = FunctionalUtility.mapFilter(this.metadataValues,
                                                                                      new Function<Metadata, String>()
                                                                                      {
                                                                                         @Override
                                                                                         public String apply(final Metadata input)
                                                                                         {
                                                                                             return input.md_scope;
                                                                                         }
                                                                                      },
                                                                                      new Predicate<String>()
                                                                                      {
                                                                                         @Override
                                                                                         public boolean apply(final String t)
                                                                                         {
                                                                                             return !MetadataVerifier.validMdScope(t);
                                                                                         }
                                                                                      });

            Assert.assertTrue(String.format("The following md_scope(s) are invalid values in the %s table: %s",
                                            GeoPackageMetadata.MetadataTableName,
                                            StringUtility.join(", ", invalidScopeValues)),
                              invalidScopeValues.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 71
     *
     * <blockquote>
     * A GeoPackage that contains a <code>gpkg_metadata</code> table SHALL
     * contain a <code>gpkg_metadata_reference</code> table per clause
     * 2.4.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#metadata_reference_table_table_definition"
     * >Table Definition</a>, <a href=
     * "http://www.geopackage.org/spec/#gpkg_metadata_reference_cols">Metadata
     * Reference Table Definition (Table Name: gpkg_metadata_reference)</a> and
     * <a href="http://www.geopackage.org/spec/#gpkg_metadata_reference_sql">
     * gpkg_metadata_reference Table Definition SQL</a>.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if the method verifyTable throws
     */
    @Requirement(reference = "Requirement 71",
                 text      = "A GeoPackage that contains a gpkg_metadata table SHALL contain a "
                             + "gpkg_metadata_reference table per clause 2.4.3.1.1 Table Definition, "
                             + "Metadata Reference Table Definition (Table Name: gpkg_metadata_reference) "
                             + "and gpkg_metadata_reference Table Definition SQL.")
    public void Requirement71() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            Assert.assertTrue(String.format("This contains a %1$s table but not a %2$s table.  "
                                            + "Either drop the %1$s table or add a %2$s table",
                                            GeoPackageMetadata.MetadataTableName,
                                            GeoPackageMetadata.MetadataReferenceTableName),
                              this.hasMetadataReferenceTable,
                              Severity.Error);

            this.verifyTable(MetadataVerifier.MetadataReferenceTableDefinition);
        }
    }

    /**
     * Requirement 72
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table reference scope column
     * value SHALL be one of 'geopackage', 'table', 'column', 'row', 'row/col'
     * in lowercase.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 72",
                 text      = "Every gpkg_metadata_reference table reference scope column value SHALL be one of 'geopackage', 'table', 'column', 'row', 'row/col' in lowercase. ")
    public void Requirement72() throws AssertionError
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<String> invalidReferenceScopeValues = FunctionalUtility.mapFilter(this.metadataReferenceValues,
                                                                                         new Function<MetadataReference, String>()
                                                                                         {
                                                                                            @Override
                                                                                            public String apply(final MetadataReference input)
                                                                                            {
                                                                                                return input.reference_scope;
                                                                                            }
                                                                                         },
                                                                                         new Predicate<String>()
                                                                                         {
                                                                                            @Override
                                                                                            public boolean apply(final String t)
                                                                                            {
                                                                                                return !MetadataVerifier.validReferenceScope(t);
                                                                                            }
                                                                                         });

            Assert.assertTrue(String.format("The following reference_scope value(s) are invalid from the %s table: %s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            StringUtility.join(", ", invalidReferenceScopeValues)),
                              invalidReferenceScopeValues.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 73
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row with a <code>
     * reference_scope</code> column value of 'geopackage' SHALL have a <code>
     * table_name</code> column value that is NULL. Every other <code>
     * gpkg_metadata_reference</code> table row SHALL have a <code>table_name
     * </code> column value that references a value in the <code>gpkg_contents
     * </code> <code>table_name</code> column.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement(reference = "Requirement 73",
                 text      = "Every gpkg_metadata_reference table row with a reference_scope column "
                           + "value of 'geopackage' SHALL have a table_name column value that is NULL. "
                           + "Every other gpkg_metadata_reference table row SHALL have a table_name column"
                           + " value that references a value in the gpkg_contents table_name column. ")
    public void Requirement73() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final Collection<String> invalidGeoPackageScopeColumns = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                                                 new Predicate<MetadataVerifier.MetadataReference>()
                                                                                                 {
                                                                                                    @Override
                                                                                                    public boolean apply(final MetadataReference t)
                                                                                                    {
                                                                                                        return t.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                               t.column_name != null;
                                                                                                    }
                                                                                                 },
                                                                                                 new Function<MetadataVerifier.MetadataReference, String>()
                                                                                                 {
                                                                                                    @Override
                                                                                                    public String apply(final MetadataReference input)
                                                                                                    {
                                                                                                        return input.column_name;
                                                                                                    }
                                                                                                 });

            Assert.assertTrue(String.format("The following column_name value(s) from %s table are invalid. They have a reference_scope = 'geopackage' and a non-null value in column_name: %s.",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            StringUtility.join(", ", invalidGeoPackageScopeColumns)),
                              invalidGeoPackageScopeColumns.isEmpty(),
                              Severity.Warning);

            // Get table_name values from the gpkg_contents table
            final String query = String.format("SELECT table_name FROM %s;", GeoPackageCore.ContentsTableName);

            final PreparedStatement stmt = this.getSqliteConnection().prepareStatement(query);

            try
            {
                final ResultSet contentsTableNamesRS = stmt.executeQuery();

                try
                {
                    final List<String> contentsTableNames = JdbcUtility.map(contentsTableNamesRS,
                                                                            new ResultSetFunction<String>()
                                                                            {
                                                                                @Override
                                                                                public String apply(final ResultSet resultSet) throws SQLException
                                                                                {
                                                                                    return resultSet.getString("table_name");
                                                                                }
                                                                            });

                    // Check other records that do not have 'geopackage' as a value
                    final List<MetadataReference> invalidTableNameValues = FunctionalUtility.filter(this.metadataReferenceValues,
                                                                                                    new Predicate<MetadataReference>()
                                                                                                    {
                                                                                                        @Override
                                                                                                        public boolean apply(final MetadataReference t)
                                                                                                        {
                                                                                                            return !t.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                                   !contentsTableNames.contains(t);
                                                                                                        }
                                                                                                    });

                    Assert.assertTrue(String.format("The following table_name value(s) in %s table are invalid. The table_name value(s) must reference the table_name(s) in the %s table.\n%s",
                                                    GeoPackageMetadata.MetadataReferenceTableName,
                                                    GeoPackageCore.ContentsTableName,
                                                    StringUtility.join(", ",
                                                                       FunctionalUtility.map(invalidTableNameValues,
                                                                                             new Function<MetadataReference, String>()
                                                                                             {
                                                                                                @Override
                                                                                                public String apply(final MetadataReference input)
                                                                                                {
                                                                                                    return String.format("reference_scope: %s, invalid table_name: %s.",
                                                                                                                         input.reference_scope,
                                                                                                                         input.table_name);
                                                                                                }

                                                                                             }))),
                                      invalidTableNameValues.isEmpty(),
                                      Severity.Warning);
                }
                finally
                {
                    contentsTableNamesRS.close();
                }
            }
            finally
            {
                stmt.close();
            }
        }
    }

    /**
     * Requirement 74
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row with a <code>
     * reference_scope</code> column value of 'geopackage','table' or 'row'
     * SHALL have a <code>column_name</code> column value that is NULL. Every
     * other <code>gpkg_metadata_reference</code> table row SHALL have a <code>
     * column_name</code> column value that contains the name of a column in
     * the SQLite table or view identified by the <code>table_name</code>
     * column value.
     * <blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     * @throws SQLException throws if various SQLExceptions occur
     */
    @Requirement(reference = "Requirement 74",
                 text    = "Every gpkg_metadata_reference table row with a reference_scope column "
                           + "value of 'geopackage','table' or 'row' SHALL have a column_name column"
                           + " value that is NULL. Every other gpkg_metadata_reference table row SHALL"
                           + " have a column_name column value that contains the name of a column in the "
                           + "SQLite table or view identified by the table_name column value. ")
    public void Requirement74() throws AssertionError, SQLException
    {
        if(this.hasMetadataReferenceTable)
        {
            final List<String> invalidColumnNameValues = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                                     new Predicate<MetadataReference>()
                                                                                     {
                                                                                        @Override
                                                                                        public boolean apply(final MetadataReference columnValue)
                                                                                        {
                                                                                            return (columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                    columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                    columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Row.toString())) &&
                                                                                                   columnValue.column_name != null;
                                                                                        }
                                                                                     },
                                                                                     new Function<MetadataReference, String>()
                                                                                     {
                                                                                        @Override
                                                                                        public String apply(final MetadataReference input)
                                                                                        {
                                                                                            return String.format("reference_scope: %s, invalid column_name: %s.",
                                                                                                                 input.reference_scope,
                                                                                                                 input.column_name);
                                                                                        }
                                                                                     });


            Assert.assertTrue(String.format("The following column_name values from %s table are invalid. They contain a reference_scope of either 'geopackage', 'table' or 'row' and need to have a column_value of NULL.\n%s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            StringUtility.join("\n", invalidColumnNameValues)),
                              invalidColumnNameValues.isEmpty(),
                              Severity.Warning);

            final List<MetadataReference> otherReferenceScopeValues = FunctionalUtility.filter(this.metadataReferenceValues,
                                                                                               new Predicate<MetadataReference>()
                                                                                               {
                                                                                                  @Override
                                                                                                  public boolean apply(final MetadataReference columnValue)
                                                                                                  {
                                                                                                      return !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                             !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                             !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Row.toString());
                                                                                                  }
                                                                                               });

            for(final MetadataReference value : otherReferenceScopeValues)
            {
                if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), value.table_name))
                {
                    final String query = "PRAGMA table_info('?');";

                    final PreparedStatement statement = this.getSqliteConnection().prepareStatement(query);

                    try
                    {
                        statement.setString(1, value.table_name);

                        final ResultSet tableInfo = statement.executeQuery(query);

                        try
                        {
                            final boolean columnExists = JdbcUtility.anyMatch(tableInfo,
                                                                              new ResultSetPredicate()
                                                                              {
                                                                                  @Override
                                                                                  public boolean apply(final ResultSet resultSet) throws SQLException
                                                                                  {
                                                                                      return resultSet.getString("name").equals(value.column_name);
                                                                                  }
                                                                              });

                            Assert.assertTrue(String.format("The column_name %s referenced in the %s table doesn't exist in the table %s.",
                                                            value.column_name,
                                                            GeoPackageMetadata.MetadataReferenceTableName,
                                                            value.table_name),
                                              columnExists,
                                              Severity.Warning);
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
            }
        }
    }

    /**
     * Requirement 75
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row with a <code>
     * reference_scope</code> column value of 'geopackage', 'table' or 'column'
     * SHALL have a <code>row_id_value</code> column value that is NULL. Every
     * other <code>gpkg_metadata_reference</code> table row SHALL have a <code>
     * row_id_value</code> column value that contains the ROWID of a row in the
     * SQLite table or view identified by the <code>table_name</code> column
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
            final List<String> invalidColumnValues = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                                 new Predicate<MetadataReference>()
                                                                                 {
                                                                                    @Override
                                                                                    public boolean apply(final MetadataReference columnValue)
                                                                                    {
                                                                                        return (columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) ||
                                                                                                columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      ||
                                                                                                columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Column.toString())) &&
                                                                                               columnValue.row_id_value != null;
                                                                                    }
                                                                                 },
                                                                                 new Function<MetadataReference, String>()
                                                                                 {
                                                                                     @Override
                                                                                     public String apply(final MetadataReference columnValue)
                                                                                     {
                                                                                         return String.format("reference_scope: %s, invalid row_id_value: %d.",
                                                                                                              columnValue.reference_scope,
                                                                                                              columnValue.row_id_value);
                                                                                     }
                                                                                 });

            Assert.assertTrue(String.format("The following row_id_value(s) has(have) a reference_scope value of 'geopackage', 'table' or 'column and do not have a value of NULL in row_id_value.\n %s",
                                            StringUtility.join("\n", invalidColumnValues)),
                              invalidColumnValues.isEmpty(),
                              Severity.Warning);

            final List<MetadataReference> invalidColumnNameValues = FunctionalUtility.filter(this.metadataReferenceValues,
                                                                                             new Predicate<MetadataReference>()
                                                                                             {
                                                                                                @Override
                                                                                                public boolean apply(final MetadataReference columnValue)
                                                                                                {
                                                                                                    return !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.GeoPackage.toString()) &&
                                                                                                           !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Table.toString())      &&
                                                                                                           !columnValue.reference_scope.equalsIgnoreCase(ReferenceScope.Column.toString());
                                                                                                }
                                                                                             });

            for(final MetadataReference value: invalidColumnNameValues)
            {
                final String query = String.format("SELECT COUNT(1) FROM %s WHERE ROWID = ?;",  // TODO make sure COUNT(1) works the way I think it does...
                                                   value.table_name);

                final PreparedStatement statement = this.getSqliteConnection().prepareStatement(query);

                try
                {
                    statement.setInt(1, value.row_id_value);

                    final ResultSet matchingRowIdRS = statement.executeQuery();

                    try
                    {
                        Assert.assertTrue(String.format("The row_id_value %d in the %s table does not reference a row id in the table %s.",
                                                        value.row_id_value,
                                                        GeoPackageMetadata.MetadataReferenceTableName,
                                                        value.table_name),
                                          matchingRowIdRS.next(),
                                          Severity.Warning);
                    }
                    finally
                    {
                        matchingRowIdRS.close();
                    }
                }
                finally
                {
                    statement.close();
                }
            }
        }
    }

    /**
     * Requirement 76
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row timestamp column
     * value SHALL be in ISO 8601 format containing a complete date plus UTC
     * hours, minutes, seconds and a decimal fraction of a second, with a 'Z'
     * ('zulu') suffix indicating UTC.
     * </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage fails to meet this requirement
     */
    @Requirement(reference = "Requirement 76",
                 text      = "Every gpkg_metadata_reference table row timestamp column value "
                             + "SHALL be in ISO 8601 [29] format containing a complete date plus "
                             + "UTC hours, minutes, seconds and a decimal fraction of a second, with "
                             + "a 'Z' ('zulu') suffix indicating UTC.")
    public void Requirement76() throws AssertionError
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
                                           GeoPackageMetadata.MetadataReferenceTableName),
                             Severity.Warning);
                    }
                }
            }
        }
    }

    /**
     * Requirement 77
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row <code>md_file_id
     * </code> column value SHALL be an id column value from the <code>
     * gpkg_metadata</code> table.
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
            final List<String> invalidIds = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                        new Predicate<MetadataReference>()
                                                                        {
                                                                            @Override
                                                                            public boolean apply(final MetadataReference metadataReference)
                                                                            {
                                                                                return FunctionalUtility.anyMatch(MetadataVerifier.this.metadataValues,
                                                                                                                  new Predicate<Metadata>()
                                                                                                                  {
                                                                                                                      @Override
                                                                                                                      public boolean apply(final Metadata metadata)
                                                                                                                      {
                                                                                                                          return metadataReference.md_file_id.equals(metadata.id);
                                                                                                                      }
                                                                                                                  });
                                                                            }
                                                                        },
                                                                        new Function<MetadataReference, String>()
                                                                        {
                                                                            @Override
                                                                            public String apply(final MetadataReference metadataReference)
                                                                            {
                                                                                return String.format("invalid md_file_id: %s, md_parent_id: %d, reference_scope: %s.",
                                                                                                     metadataReference.md_file_id,
                                                                                                     metadataReference.md_parent_id,
                                                                                                     metadataReference.reference_scope);
                                                                            }
                                                                        });


            Assert.assertTrue(String.format("The following md_file_id(s) from %s table do not reference an id column value from the %s table.\n%s",
                                            GeoPackageMetadata.MetadataReferenceTableName,
                                            GeoPackageMetadata.MetadataTableName,
                                            StringUtility.join("\n", invalidIds)),
                              invalidIds.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * Requirement 78
     *
     * <blockquote>
     * Every <code>gpkg_metadata_reference</code> table row <code>md_parent_id
     * </code> column value that is NOT NULL SHALL be an id column value from
     * the <code>gpkg_metadata</code> table that is not equal to the <code>
     * md_file_id</code> column value for that row.
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
            final Function<MetadataReference, String> metadataReferenceToMessage = new Function<MetadataReference, String>()
                                                                                   {
                                                                                       @Override
                                                                                       public String apply(final MetadataReference metadataReference)
                                                                                       {
                                                                                           return String.format("md_parent_id: %d, md_file_id: %d.",
                                                                                                                metadataReference.md_parent_id,
                                                                                                                metadataReference.md_file_id);
                                                                                       }
                                                                                   };

            final List<String> invalidParentIdsBcFileIds = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                                       new Predicate<MetadataReference>()
                                                                                       {
                                                                                           @Override
                                                                                           public boolean apply(final MetadataReference metadataReference)
                                                                                           {
                                                                                               return metadataReference.md_file_id != null &&
                                                                                                      metadataReference.md_file_id.equals(metadataReference.md_parent_id);
                                                                                           }
                                                                                       },
                                                                                       metadataReferenceToMessage);

            Assert.assertTrue(String.format("The following md_parent_id(s) are invalid because they cannot be equivalent to their correspoding md_file_id.\n%s",    // TODO state which table this affects
                                            StringUtility.join("\n", invalidParentIdsBcFileIds)),
                              invalidParentIdsBcFileIds.isEmpty(),
                              Severity.Warning);

            final Collection<Integer> metadataIds = FunctionalUtility.map(this.metadataValues,
                                                                          new Function<Metadata, Integer>()
                                                                          {
                                                                              @Override
                                                                              public Integer apply(final Metadata metadata)
                                                                              {
                                                                                  return metadata.id;
                                                                              }
                                                                          });

            final List<String> invalidParentIds = FunctionalUtility.filterMap(this.metadataReferenceValues,
                                                                              new Predicate<MetadataReference>()
                                                                              {
                                                                                  @Override
                                                                                  public boolean apply(final MetadataReference metadataReference)
                                                                                  {
                                                                                      return metadataReference.md_file_id != null &&
                                                                                             metadataIds.contains(metadataReference.md_file_id);
                                                                                  }
                                                                              },
                                                                              metadataReferenceToMessage);

            Assert.assertTrue(String.format("The following md_parent_id value(s) are invalid because they do not equal id column value from the %s table. \n%s",
                                            GeoPackageMetadata.MetadataTableName,
                                            StringUtility.join("\n", invalidParentIds)),
                              invalidParentIds.isEmpty(),
                              Severity.Warning);
        }
    }

    /**
     * This will provide a List of MetadataReferences records in the current
     * GeoPackage from the gpkg_metadata_references table
     *
     * @return a list of MetadataReference's records
     * @throws SQLException
     */
    private List<MetadataReference> getMetadataReferenceValues() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataReferenceTableName))
        {
            return Collections.emptyList();
        }

        final String query = String.format("SELECT reference_scope, table_name, column_name, row_id_value, timestamp, md_file_id, md_parent_id FROM %s;",
                                           GeoPackageMetadata.MetadataReferenceTableName);

        final Statement statement = this.getSqliteConnection().createStatement();

        try
        {
            final ResultSet metadataReferenceValuesRS = statement.executeQuery(query);

            try
            {
                return JdbcUtility.map(metadataReferenceValuesRS,
                                       new ResultSetFunction<MetadataReference>()
                                       {
                                           @Override
                                           public MetadataReference apply(final ResultSet resultSet) throws SQLException
                                           {
                                               final MetadataReference metadataReference = new MetadataReference();

                                               metadataReference.reference_scope = resultSet.getString("reference_scope");
                                               metadataReference.table_name      = resultSet.getString("table_name");
                                               metadataReference.column_name     = resultSet.getString("column_name");
                                               metadataReference.timestamp       = resultSet.getString("timestamp");
                                               metadataReference.md_file_id      = resultSet.getInt   ("md_file_id");    // Cannot be null
                                               metadataReference.row_id_value    = resultSet.getInt   ("row_id_value");  // getInt() returns 0 if the value in the database was null

                                               if(resultSet.wasNull())  // Check for that null
                                               {
                                                   metadataReference.row_id_value = null;
                                               }

                                               metadataReference.md_parent_id = resultSet.getInt   ("md_parent_id"); // Can be null

                                               if(resultSet.wasNull())  // Check for that null
                                               {
                                                   metadataReference.md_parent_id = null;
                                               }

                                               return metadataReference;
                                           }
                                       });
            }
            finally
            {
                metadataReferenceValuesRS.close();
            }
        }
        finally
        {
            statement.close();
        }
    }
    /**
     * This will provide a List of Metadata records in the current GeoPackage
     * from the gpkg_metadata table
     *
     * @return a list of Metadata's records
     * @throws SQLException
     */
    private List<Metadata> getMetadataValues() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataTableName))
        {
            return Collections.emptyList();
        }

        final String query = String.format("SELECT md_scope, id FROM %s;", GeoPackageMetadata.MetadataTableName);

        final Statement statement = this.getSqliteConnection().createStatement();

        try
        {
            final ResultSet metadataValuesRS = statement.executeQuery(query);

            try
            {
                 return JdbcUtility.map(metadataValuesRS,
                                        new ResultSetFunction<Metadata>()
                                        {
                                            @Override
                                            public Metadata apply(final ResultSet resultSet) throws SQLException
                                            {
                                                final Metadata metadata = new Metadata();
                                                metadata.id = resultSet.getInt("id");
                                                metadata.md_scope = resultSet.getString("md_scope");

                                                return metadata;
                                            }
                                        });
            }
            finally
            {
                metadataValuesRS.close();
            }
        }
        finally
        {
            statement.close();
        }
    }

    /**
     * Verifies that the reference_scope value given is a valid value (one of
     * the predefined scope values)
     *
     * @param referenceScope
     * @return true if the scope is allowed
     */
    private static boolean validReferenceScope(final String referenceScope)
    {
        return FunctionalUtility.anyMatch(Arrays.asList(ReferenceScope.values()),
                                          new Predicate<ReferenceScope>()
                                          {
                                              @Override
                                              public boolean apply(final ReferenceScope scope)
                                              {
                                                  return scope.toString().equalsIgnoreCase(referenceScope);
                                              }
                                          });
    }

    /**
     * Verifies that the metadata scope value given is a valid value (one of
     * the predefined scope values)
     *
     * @param mdScope
     * @return true if the scope is allowed
     */
    private static boolean validMdScope(final String mdScope)
    {
        return FunctionalUtility.anyMatch(Arrays.asList(Scope.values()),
                                          new Predicate<Scope>()
                                          {
                                              @Override
                                              public boolean apply(final Scope scope)
                                              {
                                                  return scope.toString().equalsIgnoreCase(mdScope);
                                              }
                                          });
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
        final Map<String, ColumnDefinition> metadataTableColumns = new HashMap<String, ColumnDefinition>();

        metadataTableColumns.put("id",               new ColumnDefinition("INTEGER", true,  true, false, null));
        metadataTableColumns.put("md_scope",         new ColumnDefinition("TEXT",    true, false, false, "'\\s*dataset\\s*'||\"\\s*dataset\\s*\""));
        metadataTableColumns.put("md_standard_uri",  new ColumnDefinition("TEXT",    true, false, false, null));
        metadataTableColumns.put("mime_type",        new ColumnDefinition("TEXT",    true, false, false, "['\"]\\s*text[/\\\\]xml\\s*['\"]"));
        metadataTableColumns.put("metadata",         new ColumnDefinition("TEXT",    true, false, false, "\\s*''\\s*|\\s*\"\"\\s*"));

        MetadataTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataTableName,
                                                      metadataTableColumns);

        final Map<String, ColumnDefinition> metadataReferenceTableColumns = new HashMap<String, ColumnDefinition>();

        metadataReferenceTableColumns.put("reference_scope",  new ColumnDefinition("TEXT",     true,  false, false, null));
        metadataReferenceTableColumns.put("table_name",       new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("column_name",      new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("row_id_value",     new ColumnDefinition("INTEGER",  false, false, false, null));
        metadataReferenceTableColumns.put("timestamp",        new ColumnDefinition("DATETIME", true,  false, false, "\\s*strftime\\s*\\(\\s*['\"]%Y-%m-%dT%H:%M:%fZ['\"]\\s*,\\s*['\"]now['\"]\\s*\\)\\s*"));
        metadataReferenceTableColumns.put("md_file_id",       new ColumnDefinition("INTEGER",  true,  false, false, null));
        metadataReferenceTableColumns.put("md_parent_id",     new ColumnDefinition("INTEGER",  false, false, false, null));

        MetadataReferenceTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataReferenceTableName,
                                                               metadataReferenceTableColumns,
                                                               new HashSet<ForeignKeyDefinition>(Arrays.asList(new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_parent_id", "id"),
                                                                                                               new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_file_id", "id"))));
    }

}
