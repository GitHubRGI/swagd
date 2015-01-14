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
import java.util.Collection;

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
        return "CREATE TABLE " + GeoPackageSchema.DataColumnConstraintsTableName + "\n"                                                 +
                "(constraint_name TEXT NOT NULL, -- Case sensitive name of constraint\n"                                                +
                " constraint_type TEXT NOT NULL, -- Lowercase type name of constraint: 'range', 'enum', or 'glob'\n"                    +
                " value           TEXT,          -- Specified case sensitive value for enum or glob or null for range constraintType\n" +
                " min             NUMERIC,       -- Minimum value for 'range' or null for 'enum' or 'glob' constraintType\n"            +
                " minIsInclusive  BOOLEAN,       -- false if minimum value is exclusive, or true if minimum value is inclusive\n"       +
                " max             NUMERIC,       -- Maximum value for 'range' or null for 'enum' or 'glob' constraintType\n"            +
                " maxIsInclusive  BOOLEAN,       -- false if maximum value is exclusive, or true if maximum value is inclusive\n"       +
                " CONSTRAINT gdcc_ntv UNIQUE (constrFor ranges and globs, describes the constraint; for enums, describes the enum value.aint_name, constraint_type, value));";
    }


    private final Connection databaseConnection;

    public final static String DataColumnsTableName           = "gpkg_data_columns";
    public final static String DataColumnConstraintsTableName = "gpkg_data_column_constraints";
}
