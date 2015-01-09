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

package com.rgi.erdc.gpkg.extensions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.rgi.erdc.gpkg.DatabaseUtility;
import com.rgi.erdc.gpkg.verification.FailedRequirement;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageExtensions
{
    public GeoPackageExtensions(final Connection databaseConnection)
    {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @return The extension GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements()
    {
        return new ExtensionsVerifier(this.databaseConnection).getFailedRequirements();
    }

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

    public Extension getExtension(final String tableName, final String columnName, final String extensionName) throws SQLException
    {
        final String extensionQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ? AND column_name = ? AND extension_name = ?;",
                                                       "table_name",
                                                       "column_name",
                                                       "extension_name",
                                                       "definition",
                                                       "scope",
                                                       GeoPackageExtensions.ExtensionsTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(extensionQuerySql))
        {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(1, columnName);
            preparedStatement.setString(1, extensionName);

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

//    public Collection<Extension> getExtensions() throws SQLException
//    {
//        final String extensionQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s",
//                                                       "table_name",
//                                                       "column_name",
//                                                       "extension_name",
//                                                       "definition",
//                                                       "scope",
//                                                       GeoPackageExtensions.ExtensionsTableName);
//
//        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(extensionQuerySql))
//        {
//            try(ResultSet results = preparedStatement.executeQuery())
//            {
//                ResultSetStream.getStream(results)
//                               .map(resultSet -> )
//            }
//        }
//    }

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

    private final Connection databaseConnection;

    public final static String ExtensionsTableName = "gpkg_extensions";
}
