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

package com.rgi.geopackage.extensions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.verification.FailedRequirement;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageExtensions
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     */
    public GeoPackageExtensions(final Connection databaseConnection)
    {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Extension requirements this GeoPackage failed to meet
     *
     * @return The extension GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements()
    {
        return new ExtensionsVerifier(this.databaseConnection).getFailedRequirements();
    }

    /**
     * Queries the GeoPackage for an specific named extension with the format <author>_<extension_name>
     *
     * @param name
     *             Name of the extension in the form <author>_<extension_name>
     * @return Returns true if the GeoPackage contains the named extension
     * @throws SQLException
     */
    public boolean hasExtension(final String name) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return false;
        }

        final String extensionNameQuerySql = String.format("SELECT COUNT(*) FROM %s WHERE extension_name = ?",
                                                           GeoPackageExtensions.ExtensionsTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(extensionNameQuerySql))
        {
            preparedStatement.setString(1, name);

            return preparedStatement.executeQuery().getInt(1) > 0;
        }
    }

    /**
     * Gets an extension represented by a specific table, column, and extension name
     *
     * @param tableName
     *             Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL
     * @param columnName
     *             Name of the column that requires the extension. When NULL, the extension is required for the entire table
     * @param extensionName
     *             The case sensitive name of the extension that is required, in the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> is [a-zA-Z0-9]. The valid character set for <extension_name> is [a-zA-Z0-9_]
     * @return Returns an instance of {@link Extension} that represents an entry in the GeoPackage extensions table
     * @throws SQLException
     */
    public Extension getExtension(final String tableName, final String columnName, final String extensionName) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return null;
        }

        final String extensionQuerySql = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE table_name = ? AND column_name = ? AND extension_name = ?;",
                                                       "table_name",
                                                       "column_name",
                                                       "extension_name",
                                                       "definition",
                                                       "scope",
                                                       GeoPackageExtensions.ExtensionsTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(extensionQuerySql))
        {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            preparedStatement.setString(3, extensionName);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    return new Extension(result.getString(1),
                                         result.getString(2),
                                         result.getString(3),
                                         result.getString(4),
                                         result.getString(5));
                }
            }
        }

        return null;
    }

    /**
     * Gets the entries of the GeoPackage extension table as a collection of extension objects
     *
     * @return Returns a collection of {@link Extension} objects that represent all of the entries in the GeoPackage extensions table
     * @throws SQLException
     */
    public Collection<Extension> getExtensions() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageExtensions.ExtensionsTableName))
        {
            return Collections.emptyList();
        }

        final String extensionQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s",
                                                       "table_name",
                                                       "column_name",
                                                       "extension_name",
                                                       "definition",
                                                       "scope",
                                                       GeoPackageExtensions.ExtensionsTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(extensionQuerySql))
        {
            try(ResultSet results = preparedStatement.executeQuery())
            {
                return ResultSetStream.getStream(results)
                                      .map(result -> { try
                                                       {
                                                           return new Extension(result.getString(1),
                                                                                result.getString(2),
                                                                                result.getString(3),
                                                                                result.getString(4),
                                                                                result.getString(5));
                                                       }
                                                       catch(final SQLException ex)
                                                       {
                                                           return null;
                                                       }
                                                     })
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toCollection(ArrayList<Extension>::new));

            }
        }
    }

    /**
     * Adds an extension to the GeoPackage extensions table
     *
     * @param tableName
     *             Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL
     * @param columnName
     *             Name of the column that requires the extension. When NULL, the extension is required for the entire table
     * @param extensionName
     *             The case sensitive name of the extension that is required, in the form <author>_<extension_name> where <author> indicates the person or organization that developed and maintains the extension. The valid character set for <author> is [a-zA-Z0-9]. The valid character set for <extension_name> is [a-zA-Z0-9_]
     * @param definition
     *             Definition of the extension in the form specfied by the template in <a href="http://www.geopackage.org/spec/#extension_template">GeoPackage Extension Template (Normative)</a> or reference thereto.
     * @param scope
     *             Indicates scope of extension effects on readers / writers
     * @return Returns an instance of {@link Extension} that represents the new extension entry
     * @throws SQLException
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

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertExtension))
        {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            preparedStatement.setString(3, extensionName);
            preparedStatement.setString(4, definition);
            preparedStatement.setString(5, scope.toString());

            preparedStatement.executeUpdate();

            this.databaseConnection.commit();
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }

        return new Extension(tableName,
                             columnName,
                             extensionName,
                             definition,
                             scope.toString());
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
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getExtensionsTableCreationSql());
            }
        }
    }

    private final Connection databaseConnection;

    public final static String ExtensionsTableName = "gpkg_extensions";
}
