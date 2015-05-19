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
package com.rgi.android.geopackage.extensions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
import com.rgi.android.common.util.functional.jdbc.JdbcUtility;
import com.rgi.android.common.util.functional.jdbc.ResultSetFunction;
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
public class ExtensionsVerifier extends Verifier
{
    private class ExtensionData
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

    private boolean hasGpkgExtensionsTable;

    // TODO reconsider this mapping.  it should at least be String, ExtensionData, but the column name is repeated as the key...
    private Map<ExtensionData, String> gpkgExtensionsDataAndColumnName;

    /**
     * Constructor
     *
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @param sqliteConnection
     *             A connection handle to the database
     * @throws SQLException
     *             if test initialization fails to get information from the
     *             database
     */
    public ExtensionsVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        try
        {
            this.hasGpkgExtensionsTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageExtensions.ExtensionsTableName);
        }
        catch(final SQLException ex)
        {
            this.hasGpkgExtensionsTable = false;
        }

        if(this.hasGpkgExtensionsTable)
        {

            final String query = String.format("SELECT table_name, column_name, extension_name FROM %s;", GeoPackageExtensions.ExtensionsTableName);

            final Statement statement = this.getSqliteConnection().createStatement();

            try
            {
                final ResultSet tableNameColumnNameRS = statement.executeQuery(query);

                try
                {
                    this.gpkgExtensionsDataAndColumnName = new HashMap<ExtensionData, String>();

                    while(tableNameColumnNameRS.next())
                    {
                        final ExtensionData extensionData = new ExtensionData(tableNameColumnNameRS.getString("table_name"),
                                                                              tableNameColumnNameRS.getString("column_name"),
                                                                              tableNameColumnNameRS.getString("extension_name"));

                        this.gpkgExtensionsDataAndColumnName.put(extensionData, extensionData.columnName);
                    }
                }
                finally
                {
                    tableNameColumnNameRS.close();
                }
            }
            catch(final SQLException ex)
            {
                this.gpkgExtensionsDataAndColumnName = Collections.emptyMap();
            }
            finally
            {
                statement.close();
            }
        }
    }

    /**
     * Requirement 78
     *
     * <blockquote> A GeoPackage MAY contain a table or update table view named
     * gpkg_extensions. If present this table SHALL be defined per clause
     * 2.5.2.1.1 <a
     * href="http://www.geopackage.org/spec/#extensions_table_definition">Table
     * Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_extensions_cols">GeoPackage
     * Extensions Table or View Definition (Table or View Name:
     * gpkg_extensions)</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_extensions_sql">
     * gpkg_extensions Table Definition SQL</a>.
     * </blockquote>
     *
     * @throws SQLException  throws when various SQLExceptions occur
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 78",
                 text      = "A GeoPackage MAY contain a table or updateable view named gpkg_extensions."
                             + " If present this table SHALL be defined per clause 2.5.2.1.1 Table Definition, "
                             + "GeoPackage Extensions Table or View Definition (Table or View Name: gpkg_extensions) "
                             + "and gpkg_extensions Table Definition SQL. ")
    public void Requirement78() throws AssertionError, SQLException
    {
        if(this.hasGpkgExtensionsTable)
        {
            this.verifyTable(ExtensionsVerifier.ExtensionsTableDefinition);
        }
    }


    /**
     * Requirement 79
     *
     * <blockquote>
     * Every extension of a GeoPackage SHALL be registered in a corresponding
     * row in the gpkg_extensions table. The absence of a gpkg_extensions table
     * or the absence of rows in gpkg_extnsions table SHALL both indicate the
     * absence of extensions to a GeoPackage.
     * </blockquote>
     */
    @Requirement(reference = "Requirement 79",
                 text      = "Every extension of a GeoPackage SHALL be registered in a corresponding row "
                              + "in the gpkg_extensions table. The absence of a gpkg_extensions table or "
                              + "the absence of rows in gpkg_extnsions table SHALL both indicate the absence "
                              + "of extensions to a GeoPackage.")
    public void Requirement79()
    {
        // TODO implement this requirement
        // Check if it has geometry_columns table
        // if it does check geometry_type_name,
        // if in Annex E
        // if it is not in the extensions table under extension_name = gpkg_geo_<geometry_type_name>, throw assertion Error
        // else not in annex e
        // extension name does not begin with gpkg and extension name ends with geom<geometry_type_name>
        // check master table for rtree% table
        // check if extension name has gpkg_rtree_index fail if doesn't
        // check master table for fgti_%
        // fail if extension_name != gpkg_srs_id_trigger

        // use Severity.Warning
    }

    /**
     * Requirement 80
     *
     * <blockquote> Values of the <code>gpkg_extensions</code> <code>table_name
     * </code> column SHALL reference values in the <code>gpkg_contents</code>
     * <code>table_name</code> column or be NULL. They SHALL NOT be NULL for
     * rows where the <code>column_name</code> value is not NULL.
     * </blockquote>
     *
     * @throws SQLException throws when various SQLExceptions occur
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 80",
                 text    = "Every extension of a GeoPackage SHALL be registered in a corresponding row "
                            + "in the gpkg_extensions table. The absence of a gpkg_extensions table or "
                            + "the absence of rows in gpkg_extnsions table SHALL both indicate the absence "
                            + "of extensions to a GeoPackage.")
    public void Requirement80() throws SQLException, AssertionError
    {
        if(this.hasGpkgExtensionsTable)
        {
            for(final ExtensionData extensionData : this.gpkgExtensionsDataAndColumnName.keySet())
            {
                final String columnName = this.gpkgExtensionsDataAndColumnName.get(extensionData);

                final boolean validEntry = extensionData.tableName == null ? columnName == null : true; // If table name is null then so must column name

                Assert.assertTrue("The value in table_name can only be null if column_name is also null.",
                                  validEntry,
                                  Severity.Warning);
            }
            // Check that the table_name in GeoPackage Extensions references a table in sqlite master
            final String query = String.format("SELECT table_name as extensionsTableName "+
                                               "FROM   %s "+
                                               "WHERE  extensionsTableName NOT IN"+
                                                  "(SELECT tbl_name "+
                                                   "FROM   sqlite_master "+
                                                   "WHERE  tbl_name = extensionsTableName);",
                                               GeoPackageExtensions.ExtensionsTableName);

            final Statement stmt2 = this.getSqliteConnection().createStatement();

            try
            {
                final ResultSet tablesNotInSM = stmt2.executeQuery(query);

                try
                {
                    final List<String> nonExistantExtensionsTable = new ArrayList<String>();

                    while(tablesNotInSM.next())
                    {
                        nonExistantExtensionsTable.add(tablesNotInSM.getString("extensionsTableName"));
                    }

                    Assert.assertTrue(String.format("The following table(s) does not exist in the sqlite master table. Either create table following table(s) or delete this entry in %s:\n%s",
                                                    GeoPackageExtensions.ExtensionsTableName,
                                                    StringUtility.join(", ", nonExistantExtensionsTable)),
                                      nonExistantExtensionsTable.isEmpty(),
                                      Severity.Warning);
                }
                finally
                {
                    tablesNotInSM.close();
                }
            }
            finally
            {
                stmt2.close();
            }
        }
    }

    /**
     * Requirement 81 <blockquote> The
     * <code>column_name</code> column value in a <code>gpkg_extensions</code>
     * row SHALL be the name of a column in the table specified by the
     * <code>table_name</code> column value for that row, or be NULL.
     * </blockquote>
     *
     * @throws SQLException throws when various SQLExceptions occur
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 81",
                 text      = "The column_name column value in a gpkg_extensions row SHALL"
                           + " be the name of a column in the table specified by the "
                           + "table_name column value for that row, or be NULL.")
    public void Requirement81() throws SQLException, AssertionError
    {
        if(this.hasGpkgExtensionsTable && !this.gpkgExtensionsDataAndColumnName.isEmpty())
        {
            for(final ExtensionData extensionData : this.gpkgExtensionsDataAndColumnName.keySet())
            {
                final String columnName = extensionData.columnName;

                if(extensionData.tableName != null && columnName != null)
                {
                    final String query = String.format("PRAGMA table_info(%s);", extensionData.tableName);

                    final PreparedStatement statement = this.getSqliteConnection().prepareStatement(query);

                    try
                    {
                        final ResultSet tableInfo = statement.executeQuery();

                        try
                        {
                            boolean columnExists = false;

                            while(tableInfo.next())
                            {
                                if(tableInfo.getString("name").equals(columnName))
                                {
                                    columnExists = true;
                                }
                            }

                            Assert.assertTrue(String.format("The column %s does not exist in the table %s. Please either add this column to this table or delete the record in %s.",
                                                            columnName,
                                                            extensionData.tableName,
                                                            GeoPackageExtensions.ExtensionsTableName),
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
     * Requirement 82 <blockquote> Each
     * <code>extension_name</code> column value in a
     * <code>gpkg_extensions</code> row SHALL be a unique case sensitive value
     * of the form &lt;author&gt;_&lt;extension_name&gt; where &lt;author&gt;
     * indicates the person or organization that developed and maintains the
     * extension. The valid character set for <author> SHALL be [a-zA-Z0-9]. The
     * valid character set for &lt;extension_name&gt; SHALL be [a-zA-Z0-9_]. An
     * <code>extension_name</code> for the "gpkg" author name SHALL be one of
     * those defined in this encoding standard or in an OGC Best Practices
     * Document that extends it. </blockquote>
     *
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 82",
                 text    = "Each extension_name column value in a gpkg_extensions row SHALL be a "
                           + "unique case sensitive value of the form <author>_<extension_name> "
                           + "where <author> indicates the person or organization that developed "
                           + "and maintains the extension. The valid character set for <author> "
                           + "SHALL be [a-zA-Z0-9]. The valid character set for <extension_name> "
                           + "SHALL be [a-zA-Z0-9_]. An extension_name for the gpkg author name "
                           + "SHALL be one of those defined in this encoding standard or in an OGC "
                           + "Best Practices Document that extends it.")
    public void Requirement82() throws AssertionError
    {
        if(this.hasGpkgExtensionsTable)
        {

            final Collection<String> invalidExtensionNames = FunctionalUtility.mapFilter(this.gpkgExtensionsDataAndColumnName.keySet(),
                                                                                         new Function<ExtensionData, String>()
                                                                                         {
                                                                                             @Override
                                                                                             public String apply(final ExtensionData input)
                                                                                             {
                                                                                                 return input.extensionName;
                                                                                             }
                                                                                         },
                                                                                         new Predicate<String>()
                                                                                         {
                                                                                             @Override
                                                                                             public boolean apply(final String name)
                                                                                             {
                                                                                                 if(name == null)
                                                                                                 {
                                                                                                     return true;
                                                                                                 }

                                                                                                 final String author[] = name.split("_", 2);

                                                                                                 return author.length != 2 ||
                                                                                                        (author[0].matches("gpkg") && !isRegisteredExtension(name)) ||
                                                                                                        !author[0].matches("[a-zA-Z0-9]+") ||
                                                                                                        !author[1].matches("[a-zA-Z0-9_]+");
                                                                                             }
                                                                                         });


            Assert.assertTrue(String.format("The following extension_name(s) are invalid: \n%s",
                                            StringUtility.join(", ",
                                                               FunctionalUtility.map(invalidExtensionNames,
                                                                                     new Function<String, String>()
                                                                                     {
                                                                                        @Override
                                                                                        public String apply(final String input)
                                                                                        {
                                                                                            return input.isEmpty() ? "<empty string>" : input;
                                                                                        }
                                                                                     }))),
                               invalidExtensionNames.isEmpty(),
                               Severity.Warning);
        }
    }

    /**
     * Requirement 83
     *
     * <blockquote>
     * The definition column value in a <code>gpkg_extensions</code> row SHALL contain or
     * reference the text that results from documenting an extension by filling
     * out the GeoPackage Extension Template in <a
     * href="http://www.geopackage.org/spec/#extension_template"> GeoPackage
     * Extension Template (Normative)</a>.
     * </blockquote>
     *
     * @throws SQLException throws when various SQLExceptions occur
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 83",
                 text    = "The definition column value in a gpkg_extensions row SHALL "
                           + "contain or reference the text that results from documenting "
                           + "an extension by filling out the GeoPackage Extension Template "
                           + "in GeoPackage Extension Template (Normative).")
    public void Requirement83() throws SQLException, AssertionError
    {
        if(this.hasGpkgExtensionsTable)
        {
            final String query = String.format("SELECT table_name "
                                             + "FROM %s "
                                             + "WHERE definition NOT LIKE '%s' "
                                             + "AND   definition NOT LIKE '%s' "
                                             + "AND   definition NOT LIKE '%s' "
                                             + "AND   definition NOT LIKE '%s';",
                                             GeoPackageExtensions.ExtensionsTableName,
                                             "Annex%",
                                             "http%",
                                             "mailto%",
                                             "Extension Title%");

            final Statement statement = this.getSqliteConnection().createStatement();

            try
            {
                final ResultSet invalidDefinitionValues = statement.executeQuery(query);

                try
                {
                    final Collection<String> invalidDefinitions = JdbcUtility.map(invalidDefinitionValues,
                                                                                  new ResultSetFunction<String>()
                                                                                  {
                                                                                      @Override
                                                                                      public String apply(final ResultSet resultSet) throws SQLException
                                                                                      {
                                                                                          return resultSet.getString("table_name");
                                                                                      }
                                                                                  });

                     Assert.assertTrue(String.format("The following table_name values in %s table have invalid values for the definition column: %s.",
                                                     GeoPackageExtensions.ExtensionsTableName,
                                                     StringUtility.join(", ", invalidDefinitions)),
                                  invalidDefinitions.isEmpty(),
                                  Severity.Warning);
                }
                finally
                {
                    invalidDefinitionValues.close();
                }
            }
            finally
            {
                statement.close();
            }
        }
    }

    /**
     * Requirement 84
     *
     * <blockquote>
     * The scope column value in a <code>gpkg_extensions</code> row SHALL be
     * lowercase "read-write" for an extension that affects both readers and
     * writers, or "write-only" for an extension that affects only writers.
     * </blockquote>
     * @throws SQLException throws when various SQLExceptions occur
     * @throws AssertionError throws when the GeoPackage Fails to meet this requirement
     */
    @Requirement(reference = "Requirement 84",
                 text      = "The scope column value in a gpkg_extensions row SHALL be lowercase "
                           + "\"read-write\" for an extension that affects both readers and writers, "
                           + "or \"write-only\" for an extension that affects only writers. ")
    public void Requirement84() throws SQLException, AssertionError
    {
        if(this.hasGpkgExtensionsTable)
        {
            final String query = String.format("SELECT scope FROM %s WHERE scope != 'read-write' AND scope != 'write-only'",
                                               GeoPackageExtensions.ExtensionsTableName);

            final Statement statement = this.getSqliteConnection().createStatement();

            try
            {
                final ResultSet invalidScopeValues = statement.executeQuery(query);

                try
                {
                    final List<String> invalidScopes = JdbcUtility.map(invalidScopeValues,
                                                                       new ResultSetFunction<String>()
                                                                       {
                                                                           @Override
                                                                           public String apply(final ResultSet resultSet) throws SQLException
                                                                           {
                                                                               return resultSet.getString("scope");
                                                                           }
                                                                       });

                    Assert.assertTrue(String.format("There is(are) value(s) in the column scope in %s table that is not 'read-write' or 'write-only' in all lowercase letters. The following values are incorrect: %s",
                                                    GeoPackageExtensions.ExtensionsTableName,
                                                    StringUtility.join(", ", invalidScopes)),
                                      invalidScopes.isEmpty(),
                                      Severity.Warning);
                }
                finally
                {
                    invalidScopeValues.close();
                }
            }
            finally
            {
                statement.close();
            }
        }
    }

    private static boolean isRegisteredExtension(final String extensionName)
    {
        return RegisteredExtensions.contains(extensionName);
    }

    private static final TableDefinition ExtensionsTableDefinition;
    private static final List<String>    RegisteredExtensions;

    static
    {
        final Map<String, ColumnDefinition> extensionsTableColumns = new HashMap<String, ColumnDefinition>();

        extensionsTableColumns.put("table_name",      new ColumnDefinition("TEXT", false, false, false, null));
        extensionsTableColumns.put("column_name",     new ColumnDefinition("TEXT", false, false, false, null));
        extensionsTableColumns.put("extension_name",  new ColumnDefinition("TEXT", true,  false, false, null));
        extensionsTableColumns.put("definition",      new ColumnDefinition("TEXT", true,  false, false, null));
        extensionsTableColumns.put("scope",           new ColumnDefinition("TEXT", true,  false, false, null));

        ExtensionsTableDefinition = new TableDefinition(GeoPackageExtensions.ExtensionsTableName,
                                                        extensionsTableColumns,
                                                        Collections.<ForeignKeyDefinition>emptySet(),
                                                        new HashSet<UniqueDefinition>(Arrays.asList(new UniqueDefinition("table_name", "column_name", "extension_name"))));

        RegisteredExtensions = Arrays.asList("gpkg_zoom_other","gpkg_webp", "gpkg_geometry_columns", "gpkg_rtree_index","gpkg_geometry_type_trigger", "gpkg_srs_id_trigger");
    }
}