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

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rgi.android.common.util.jdbc.JdbcUtility;
import com.rgi.android.common.util.jdbc.PreparedStatementConsumer;
import com.rgi.android.common.util.jdbc.ResultSetFunction;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.android.geopackage.extensions.implementation.ExtensionImplementation;
import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.utility.SelectBuilder;
import com.rgi.android.geopackage.verification.VerificationIssue;
import com.rgi.android.geopackage.verification.VerificationLevel;

/**
 * 'Extensions' subsystem of the {@link GeoPackage} implementation
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageExtensions
{
    /**
     * The String name "gpkg_extensions" of the database Extensions table
     * containing the extensions of the GeoPackage (http://www.geopackage.org/spec/#_extensions)
     */
    public final static String ExtensionsTableName = "gpkg_extensions";

    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param geoPackageCore
     *             'Core' subsystem of the {@link GeoPackage} implementation
     *
     */
    public GeoPackageExtensions(final Connection databaseConnection,
                                final GeoPackageCore geoPackageCore)
    {
        this.databaseConnection = databaseConnection;
        this.geoPackageCore     = geoPackageCore;
    }

    /**
     * Extension requirements this GeoPackage failed to meet
     *
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return The extension GeoPackage requirements this GeoPackage fails to conform to
     * @throws SQLException
     *             if there is a database error
     */
    public Collection<VerificationIssue> getVerificationIssues(final VerificationLevel verificationLevel) throws SQLException
    {
        return new ExtensionsVerifier(this.databaseConnection, verificationLevel).getVerificationIssues();
    }

    /**
     * Queries the GeoPackage for an specific named extension with the format
     * <author>_<extension_name>
     *
     * @param name
     *            Name of the extension in the form <author>_<extension_name>
     * @return Returns true if the GeoPackage contains the named extension
     * @throws SQLException
     *             throws if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             throws or other various SQLExceptions occur
     */
    public boolean hasExtension(final String name) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return false;
        }

        final String extensionNameQuerySql = String.format("SELECT COUNT(*) FROM %s WHERE extension_name = ? LIMIT 1",
                                                           GeoPackageExtensions.ExtensionsTableName);

        final int count = JdbcUtility.selectOne(this.databaseConnection,
                                                extensionNameQuerySql,
                                                new PreparedStatementConsumer()
                                                {
                                                    @Override
                                                    public void accept(final PreparedStatement preparedStatement) throws SQLException
                                                    {
                                                        preparedStatement.setString(1, name);
                                                    }
                                                },
                                                new ResultSetFunction<Integer>()
                                                {
                                                    @Override
                                                    public Integer apply(final ResultSet resultSet) throws SQLException
                                                    {
                                                        return resultSet.getInt(1);
                                                    }
                                                });
        return count > 0;
    }

    /**
     * Gets an extension represented by a specific table, column, and extension
     * name
     *
     * @param tableName
     *            Name of the table that requires the extension. When NULL, the
     *            extension is required for the entire GeoPackage. SHALL NOT be
     *            NULL when the column_name is not NULL
     * @param columnName
     *            Name of the column that requires the extension. When NULL, the
     *            extension is required for the entire table
     * @param extensionName
     *            The case sensitive name of the extension that is required, in
     *            the form <author>_<extension_name> where <author> indicates
     *            the person or organization that developed and maintains the
     *            extension. The valid character set for <author> is
     *            [a-zA-Z0-9]. The valid character set for <extension_name> is
     *            [a-zA-Z0-9_]
     * @return Returns an instance of {@link Extension} that represents an entry
     *         in the GeoPackage extensions table
     * @throws SQLException
     *             throws if the methods {@link DatabaseUtility#
     *             tableOrViewExists(Connection, String)} or {@link
     *             SelectBuilder#SelectBuilder} throws or other various SQLExceptions occur
     */
    public Extension getExtension(final String tableName, final String columnName, final String extensionName) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return null;
        }

        final String extensionQuery = String.format("SELECT %s, %s FROM %s WHERE %s IS ? AND %s IS ? AND %s IS ? LIMIT 1;",   // 'IS' instead of '=' because the values could be null
                                                    "definition",
                                                    "scope",
                                                    GeoPackageExtensions.ExtensionsTableName,
                                                    "table_name",
                                                    "column_name",
                                                    "extension_name");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     extensionQuery,
                                     new PreparedStatementConsumer()
                                     {
                                         @Override
                                         public void accept(final PreparedStatement preparedStatement) throws SQLException
                                         {
                                             preparedStatement.setString(1, tableName     == null ? "NULL" : tableName);
                                             preparedStatement.setString(2, columnName    == null ? "NULL" : columnName);
                                             preparedStatement.setString(3, extensionName == null ? "NULL" : extensionName);
                                         }
                                     },
                                     new ResultSetFunction<Extension>()
                                     {
                                         @Override
                                         public Extension apply(final ResultSet resultSet) throws SQLException
                                         {
                                             return new Extension(tableName,
                                                                  columnName,
                                                                  extensionName,
                                                                  resultSet.getString(1),
                                                                  resultSet.getString(2));
                                         }
                                     });
    }

    /**
     * Gets the entries of the GeoPackage extension table as a collection of
     * extension objects
     *
     * @return Returns a collection of {@link Extension} objects that represent
     *         all of the entries in the GeoPackage extensions table
     * @throws SQLException
     *             throws if the method {@link DatabaseUtility
     *             #tableOrViewExists(Connection, String)} throws or other
     *             various SQLExceptions occur
     */
    public List<Extension> getExtensions() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return Collections.emptyList();
        }

        final String extensionQuerySql = String.format("SELECT %s, %s, %s, %s, %s FROM %s",
                                                       "table_name",
                                                       "column_name",
                                                       "extension_name",
                                                       "definition",
                                                       "scope",
                                                       GeoPackageExtensions.ExtensionsTableName);

        return JdbcUtility.select(this.databaseConnection,
                                  extensionQuerySql,
                                  null,
                                  new ResultSetFunction<Extension>()
                                  {
                                      @Override
                                      public Extension apply(final ResultSet resultSet) throws SQLException
                                      {
                                          return new Extension(resultSet.getString(1),
                                                               resultSet.getString(2),
                                                               resultSet.getString(3),
                                                               resultSet.getString(4),
                                                               resultSet.getString(5));
                                      }
                                  });
    }

    /**
     * Adds an extension to the GeoPackage extensions table
     *
     * @param tableName
     *            Name of the table that requires the extension. When NULL, the
     *            extension is required for the entire GeoPackage. SHALL NOT be
     *            NULL when the column_name is not NULL
     * @param columnName
     *            Name of the column that requires the extension. When NULL, the
     *            extension is required for the entire table
     * @param extensionName
     *            The case sensitive name of the extension that is required, in
     *            the form <author>_<extension_name> where <author> indicates
     *            the person or organization that developed and maintains the
     *            extension. The valid character set for <author> is
     *            [a-zA-Z0-9]. The valid character set for <extension_name> is
     *            [a-zA-Z0-9_]
     * @param definition
     *            Definition of the extension in the form specfied by the
     *            template in <a
     *            href="http://www.geopackage.org/spec/#extension_template"
     *            >GeoPackage Extension Template (Normative)</a> or reference
     *            thereto.
     * @param scope
     *            Indicates scope of extension effects on readers / writers
     * @return Returns an instance of {@link Extension} that represents the new
     *         extension entry
     * @throws SQLException
     *             throws if the methods
     *             {@link #getExtension(String, String, String)} or
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             throw or if the Extensions Table is unable to be created or the database
     *             is unable to rollback the commit after a different Exception is thrown.
     */
    public Extension addExtension(final String tableName,
                                  final String columnName,
                                  final String extensionName,
                                  final String definition,
                                  final Scope  scope) throws SQLException
    {
        if(columnName != null && tableName == null)
        {
            throw new IllegalArgumentException("Table name may not be null if column name is not null"); // Requirement 80
        }

        if(tableName != null && tableName.isEmpty())
        {
            throw new IllegalArgumentException("If table name is not null, it may not be empty");
        }

        if(columnName != null && columnName.isEmpty())
        {
            throw new IllegalArgumentException("If column name is not null, it may not be empty");
        }

        if(extensionName == null || extensionName.isEmpty())
        {
            throw new IllegalArgumentException("Extension name may not be null or empty");
        }

        if(!extensionName.matches(Extension.ExtensionNameRegularExpression))
        {
            throw new IllegalArgumentException("Extension name must be a value of the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> SHALL be [a-zA-Z0-9]. The valid character set for <extension_name> SHALL be [a-zA-Z0-9_]");   // Requirement 82
        }

        if(scope == null)
        {
            throw new IllegalArgumentException("Scope may not be null");
        }

        final Extension existingExtension = this.getExtension(tableName, columnName, extensionName);

        if(existingExtension != null)
        {
            if(existingExtension.equals(tableName,
                                        columnName,
                                        extensionName,
                                        definition,
                                        scope))
            {
                return existingExtension;
            }

            throw new IllegalArgumentException("An extension already exists with this combination of table, column and extension name, but has different values for its other fields");
        }

        final String insertExtension = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                                     GeoPackageExtensions.ExtensionsTableName,
                                                     "table_name",
                                                     "column_name",
                                                     "extension_name",
                                                     "definition",
                                                     "scope");

        this.createExtensionTableNoCommit(); // Create the extension table

        JdbcUtility.update(this.databaseConnection,
                           insertExtension,
                           new PreparedStatementConsumer()
                           {
                               @Override
                               public void accept(final PreparedStatement preparedStatement) throws SQLException
                               {
                                   preparedStatement.setString(1, tableName);
                                   preparedStatement.setString(2, columnName);
                                   preparedStatement.setString(3, extensionName);
                                   preparedStatement.setString(4, definition);
                                   preparedStatement.setString(5, scope.toString());
                               }
                           });

        this.databaseConnection.commit();

        return new Extension(tableName,
                             columnName,
                             extensionName,
                             definition,
                             scope.toString());
    }

    /**
     * Gets a handle to an {@link ExtensionImplementation} which exposes
     * extension specific functionality
     *
     * @param clazz
     *             {@link Class} representing
     * @return a handle to an implementation of {@link ExtensionImplementation}
     * @throws BadImplementationException
     *             if the Class type parameter doesn't match the requirements
     *             needed to create the requested extension.  See {@link
     *             BadImplementationException#getCause()} for more details
     */
    public <T extends ExtensionImplementation> T getExtensionImplementation(final Class<T> clazz) throws BadImplementationException
    {
        if(clazz == null)
        {
            throw new IllegalArgumentException("Class cannot be null");
        }

        if(this.implementations.containsKey(clazz))
        {
            final ExtensionImplementation implementation = this.implementations.get(clazz);

            return clazz.cast(implementation);
        }

        try
        {
            final Constructor<T> constructor = clazz.getDeclaredConstructor(Connection.class,
                                                                            GeoPackageCore.class,
                                                                            GeoPackageExtensions.class);

            final T implementation = constructor.newInstance(this.databaseConnection, this.geoPackageCore, this);

            this.implementations.put(clazz, implementation);

            return implementation;
        }
        catch(final Exception ex)
        {
            throw new BadImplementationException(String.format("There was an error instantiating an instance of the '%s' GeoPackage extension implementation", clazz.getName()), ex);
        }
    }

    @SuppressWarnings("static-method")
    protected String getExtensionsTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_extensions_cols
        // http://www.geopackage.org/spec/#gpkg_extensions_sql
        return "CREATE TABLE " + GeoPackageExtensions.ExtensionsTableName +
                "(table_name     TEXT,          -- Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL.\n" +
                " column_name    TEXT,          -- Name of the column that requires the extension. When NULL, the extension is required for the entire table.\n"                                                         +
                " extension_name TEXT NOT NULL, -- The case sensitive name of the extension that is required, in the form <author>_<extension_name>.\n"                                                                  +
                " definition     TEXT NOT NULL, -- Definition of the extension in the form specfied by the template in GeoPackage Extension Template (Normative) or reference thereto.\n"                                +
                " scope          TEXT NOT NULL, -- Indicates scope of extension effects on readers / writers: read-write or write-only in lowercase.\n"                                                                  +
                " CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))";
    }

    /**
     * Creates the tables required for storing extensions
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @throws SQLException
     */
    protected void createExtensionTableNoCommit() throws SQLException
    {
        // Create the tile matrix set table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            JdbcUtility.update(this.databaseConnection, this.getExtensionsTableCreationSql());
        }
    }

    private final Connection     databaseConnection;
    private final GeoPackageCore geoPackageCore;

    private final Map<Class<? extends ExtensionImplementation>, ExtensionImplementation> implementations = new HashMap<Class<? extends ExtensionImplementation>, ExtensionImplementation>();
}
