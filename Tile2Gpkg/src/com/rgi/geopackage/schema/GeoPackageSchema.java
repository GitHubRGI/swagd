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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.activation.MimeType;

import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.metadata.GeoPackageMetadata;
import com.rgi.geopackage.verification.FailedRequirement;

public class GeoPackageSchema
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     */
    public GeoPackageSchema(final Connection databaseConnection)
    {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Metadata requirements this GeoPackage failed to meet
     *
     * @return The metadata GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements()
    {
        return new SchemaVerifier(this.databaseConnection).getFailedRequirements();
    }

    /**
     * Add an entry to the geopackage_data_columns table
     *
     * @param table
     *             Content table
     * @param columnName
     *             Name of the table column
     * @param name
     *             A human-readable identifier (e.g. short name) for the columnName content
     * @param title
     *             A human-readable formal title for the columnName content
     * @param description
     *             A human-readable description for the tableName content
     * @param mimeType
     *            <a href="http://www.iana.org/assignments/media-types/index.html">MIME</a> type of columnName if BLOB type, or NULL for other types
     * @param constraintName
     *            Case sensitive column value constraint name specified by reference to gpkg_data_column_constraints.constraint name
     * @return Returns the newly added {@link DataColumn} object
     * @throws SQLException
     */
    public DataColumn addDataColumn(final Content  table,
                                    final String   columnName,
                                    final String   name,
                                    final String   title,
                                    final String   description,
                                    final MimeType mimeType,
                                    final String   constraintName) throws SQLException
    {
        if(table == null)
        {
            throw new IllegalArgumentException("Table may not be null");
        }

        if(columnName == null)
        {
            throw new IllegalArgumentException("Column name may not be null");
        }

        // TODO check to make sure the column belongs to the table

        final DataColumn existingDataColumn = this.getDataColumn(table, columnName);

        if(existingDataColumn != null)
        {
            return existingDataColumn;
        }

        try
        {
            this.createDataColumnsTableNoCommit();  // Create the data column table if it doesn't exist

            final String insertDataColumnSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                                             GeoPackageMetadata.MetadataReferenceTableName,
                                                             "table_name",
                                                             "column_name",
                                                             "name",
                                                             "title",
                                                             "description",
                                                             "mime_type",
                                                             "constraint_name");

            try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertDataColumnSql))
            {
                preparedStatement.setString(1, table.getTableName());
                preparedStatement.setString(2, columnName);
                preparedStatement.setString(3, name);
                preparedStatement.setString(4, title);
                preparedStatement.setString(5, description);
                preparedStatement.setObject(6, mimeType.toString());
                preparedStatement.setObject(6, constraintName);

                preparedStatement.executeUpdate();
            }

            final DataColumn dataColumn = new DataColumn(table.getTableName(),
                                                         columnName,
                                                         name,
                                                         title,
                                                         description,
                                                         mimeType.toString(),
                                                         constraintName);
            this.databaseConnection.commit();

            return dataColumn;
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }

    }

//    public DataColumnConstraint addDataColumnConstraint()
//    {
//
//    }

    /**
     * Gets an entry in the GeoPackage Data Column table that matches the supplied criteria
     *
     * @param table
     *             Content table
     * @param columnName
     *             Name of the table column
     * @return Returns the a {@link DataColumn} that matches the supplied criteria, or null if there isn't a match
     * @throws SQLException
     */
    public DataColumn getDataColumn(final Content table, final String columnName) throws SQLException
    {
        if(table == null)
        {
            throw new IllegalArgumentException("Content table may not be null");
        }

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            return null;
        }

        final String dataColumnQuerySql = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1;",
                                                        "name",
                                                        "title",
                                                        "description",
                                                        "mime_type",
                                                        "constraint_name",
                                                        GeoPackageSchema.DataColumnsTableName,
                                                        "table_name",
                                                        "column_name");


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(dataColumnQuerySql))
        {
            preparedStatement.setString(1, table.getTableName());
            preparedStatement.setString(1, columnName);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    return new DataColumn(table.getTableName(),
                                          columnName,
                                          result.getString(1),  // name
                                          result.getString(2),  // title
                                          result.getString(3),  // description
                                          result.getString(4),  // mime type
                                          result.getString(5)); // constraint name
                }
            }

            return null;
        }
    }

    /**
     * Creates the GeoPackage data columns table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createDataColumnsTableNoCommit() throws SQLException
    {
        // Create the tile matrix set table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getDataColumnsTableCreationSql());
            }
        }
    }

    /**
     * Creates the GeoPackage data column constraints table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createDataColumnConstraintsTableNoCommit() throws SQLException
    {
        // Create the tile matrix table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getDataColumnConstraintsTableCreationSql());
            }
        }
    }

    @SuppressWarnings("static-method")
    protected String getDataColumnsTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_data_columns_cols
        // http://www.geopackage.org/spec/#gpkg_data_columns_sql
        return "CREATE TABLE " + GeoPackageSchema.DataColumnsTableName + "\n"                                                                                             +
                "(table_name      TEXT NOT NULL, -- Name of the tiles or feature table\n"                                                                                 +
                " column_name     TEXT NOT NULL, -- Name of the table column\n"                                                                                           +
                " name            TEXT,          -- A human-readable identifier (e.g. short name) for the columnName content\n"                                           +
                " title           TEXT,          -- A human-readable formal title for the columnName content\n"                                                           +
                " description     TEXT,          -- A human-readable description for the tableName content\n"                                                             +
                " mime_type       TEXT,          -- MIME type of columnName if BLOB type, or NULL for other types\n"                                                      +
                " constraint_name TEXT,          -- Case sensitive column value constraint name specified by reference to gpkg_data_column_constraints.constraint name\n" +
                " CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name),\n"                                                                                             +
                " CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getDataColumnConstraintsTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_data_column_constraints_cols
        // http://www.geopackage.org/spec/#gpkg_data_column_constraints_sql
        return "CREATE TABLE " + GeoPackageSchema.DataColumnConstraintsTableName + "\n"                                                     +
                "(constraint_name TEXT NOT NULL, -- Case sensitive name of constraint\n"                                                    +
                " constraint_type TEXT NOT NULL, -- Lowercase type name of constraint: 'range', 'enum', or 'glob'\n"                        +
                " value           TEXT,          -- Specified case sensitive value for enum or glob or null for range constraintType\n"     +
                " min             NUMERIC,       -- Minimum value for 'range' or null for 'enum' or 'glob' constraintType\n"                +
                " minIsInclusive  BOOLEAN,       -- false if minimum value is exclusive, or true if minimum value is inclusive\n"           +
                " max             NUMERIC,       -- Maximum value for 'range' or null for 'enum' or 'glob' constraintType\n"                +
                " maxIsInclusive  BOOLEAN,       -- false if maximum value is exclusive, or true if maximum value is inclusive\n"           +
                " description     TEXT,          -- For ranges and globs, describes the constraint; for enums, describes the enum value.\n" +
                " CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value));";
    }


    private final Connection databaseConnection;

    public final static String DataColumnsTableName           = "gpkg_data_columns";
    public final static String DataColumnConstraintsTableName = "gpkg_data_column_constraints";
}
