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

package com.rgi.geopackage.metadata;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.activation.MimeType;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.verification.FailedRequirement;

public class GeoPackageMetadata
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     */
    public GeoPackageMetadata(final Connection databaseConnection)
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
        return new MetadataVerifier(this.databaseConnection).getFailedRequirements();
    }

    /**
     * Creates an entry in the GeoPackage metadata table
     *
     * @param scope
     *             Metadata scope
     * @param standardUri
     *             URI reference to the metadata structure definition authority
     * @param mimeType
     *             MIME encoding of metadata
     * @param metadata
     *             Metadata text
     * @return Returns the newly added Metadata object
     * @throws SQLException
     */
    public Metadata addMetadata(final Scope    scope,
                                final URI      standardUri,
                                final MimeType mimeType,
                                final String   metadata) throws SQLException
    {
        if(scope == null)
        {
            throw new IllegalArgumentException("Scope may not be null");
        }

        if(standardUri == null)
        {
            throw new IllegalArgumentException("Standard URI may not be null");
        }

        if(mimeType == null)
        {
            throw new IllegalArgumentException("Mime type may not be null");
        }

        if(metadata == null)
        {
            throw new IllegalArgumentException("Metadata may not be null");
        }

        final Metadata existingMetadata = this.getMetadata(scope,
                                                           standardUri,
                                                           mimeType,
                                                           metadata);

        if(existingMetadata != null)
        {
            return existingMetadata;
        }

        try
        {
            this.createMetadataTableNoCommit();  // Create the metadata table if it doesn't exist

            final String insertMetadataSql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                                                           GeoPackageMetadata.MetadataTableName,
                                                           "md_scope",
                                                           "md_standard_uri",
                                                           "mime_type",
                                                           "metadata");

            try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertMetadataSql))
            {
                preparedStatement.setString(1, scope.getName());
                preparedStatement.setString(2, standardUri.toString());
                preparedStatement.setString(3, mimeType.toString());
                preparedStatement.setString(4, metadata);

                preparedStatement.executeUpdate();
            }

            this.databaseConnection.commit();

            return this.getMetadata(scope,
                                    standardUri,
                                    mimeType,
                                    metadata);
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Gets all entries in the GeoPackage metadata table
     *
     * @return Returns a collection of Metadata objects
     * @throws SQLException
     */
    public Collection<Metadata> getMetadata() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageMetadata.MetadataTableName))
        {
            return Collections.emptyList();
        }

        final String metadataQuerySql = String.format("SELECT %s, %s, %s, %s, %s FROM %s;",
                                                      "id",
                                                      "md_scope",
                                                      "md_standard_uri",
                                                      "mime_type",
                                                      "metadata",
                                                      GeoPackageMetadata.MetadataTableName);


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(metadataQuerySql))
        {
            return ResultSetStream.getStream(preparedStatement.executeQuery())
                                  .map(resultSet -> { try
                                                      {
                                                          return new Metadata(                 resultSet.getInt(1),
                                                                              Scope.fromString(resultSet.getString(2)),
                                                                                       new URI(resultSet.getString(3)),
                                                                                  new MimeType(resultSet.getString(4)),
                                                                                               resultSet.getString(5));
                                                      }
                                                      catch(final Exception ex)
                                                      {
                                                          ex.printStackTrace();
                                                          return null;
                                                      }
                                                    })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());

        }
    }

    /**
     * Creates the GeoPackage metadata table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createMetadataTableNoCommit() throws SQLException
    {
        // Create the tile matrix set table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageMetadata.MetadataTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getMetadataTableCreationSql());
            }
        }
    }

    /**
     * Creates the GeoPackage metadata reference table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createMetadataReferenceTableNoCommit() throws SQLException
    {
        // Create the tile matrix table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageMetadata.MetadataReferenceTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getMetadataReferenceTableCreationSql());
            }
        }
    }

    @SuppressWarnings("static-method")
    protected String getMetadataTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_extensions_cols
        // http://www.geopackage.org/spec/#gpkg_extensions_sql
        return "CREATE TABLE " + GeoPackageMetadata.MetadataTableName + "\n" +
                "(id              INTEGER CONSTRAINT m_pk PRIMARY KEY ASC NOT NULL UNIQUE,             -- Metadata primary key\n"                                                                      +
                " md_scope        TEXT                                    NOT NULL DEFAULT 'dataset',  -- Case sensitive name of the data scope to which this metadata applies; see Metadata Scopes\n" +
                " md_standard_uri TEXT                                    NOT NULL,                    -- URI reference to the metadata structure definition authority\n"                              +
                " mime_type       TEXT                                    NOT NULL DEFAULT 'text/xml', -- MIME encoding of metadata\n"                                                                 +
                " metadata        TEXT                                    NOT NULL                     -- metadata\n"                                                                                  +
                ");";
    }

    @SuppressWarnings("static-method")
    protected String getMetadataReferenceTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_extensions_cols
        // http://www.geopackage.org/spec/#gpkg_extensions_sql
        return "CREATE TABLE " + GeoPackageMetadata.MetadataReferenceTableName + "\n" +
                "(reference_scope TEXT     NOT NULL,                                                -- Lowercase metadata reference scope; one of 'geopackage', 'table','column', 'row', 'row/col'\n" +
                " table_name      TEXT,                                                             -- Name of the table to which this metadata reference applies, or NULL for reference_scope of 'geopackage'\n" +
                " column_name     TEXT,                                                             -- Name of the column to which this metadata reference applies; NULL for reference_scope of 'geopackage','table' or 'row', or the name of a column in the table_name table for reference_scope of 'column' or 'row/col'\n" +
                " row_id_value    INTEGER,                                                          -- NULL for reference_scope of 'geopackage', 'table' or 'column', or the rowed of a row record in the table_name table for reference_scope of 'row' or 'row/col'\n" +
                " timestamp       DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), -- timestamp value in ISO 8601 format as defined by the strftime function '%Y-%m-%dT%H:%M:%fZ' format string applied to the current time\n" +
                " md_file_id      INTEGER  NOT NULL,                                                -- gpkg_metadata table id column value for the metadata to which this gpkg_metadata_reference applies\n" +
                " md_parent_id    INTEGER,                                                          -- gpkg_metadata table id column value for the hierarchical parent gpkg_metadata for the gpkg_metadata to which this gpkg_metadata_reference applies, or NULL if md_file_id forms the root of a metadata hierarchy\n" +
                " CONSTRAINT crmr_mfi_fk FOREIGN KEY (md_file_id) REFERENCES gpkg_metadata(id),\n" +
                " CONSTRAINT crmr_mpi_fk FOREIGN KEY (md_parent_id) REFERENCES gpkg_metadata(id));";
    }

    /**
     * @param scope
     *             Metadata scope
     * @param standardUri
     *             URI reference to the metadata structure definition authority
     * @param mimeType
     *             MIME encoding of metadata
     * @param metadata
     *             Metadata text
     * @return Returns an object representing an entry in the GeoPackage metadata table, or null if no entry matches the supplied criteria
     * @throws SQLException
     */
    private Metadata getMetadata(final Scope    scope,
                                 final URI      standardUri,
                                 final MimeType mimeType,
                                 final String   metadata) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageMetadata.MetadataTableName))
        {
            return null;
        }

        final String metadataQuerySql = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? LIMIT 1;",
                                                      "id",
                                                      GeoPackageMetadata.MetadataTableName,
                                                      "md_scope",
                                                      "md_standard_uri",
                                                      "mime_type",
                                                      "metadata");


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(metadataQuerySql))
        {
            preparedStatement.setString(1, scope.getName());
            preparedStatement.setString(2, standardUri.toString());
            preparedStatement.setString(3, mimeType.toString());
            preparedStatement.setString(4, metadata);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    return new Metadata(result.getInt(1), // identifier
                                        scope,
                                        standardUri,
                                        mimeType,
                                        metadata);
                }

                return null;
            }
        }
    }

    private final Connection databaseConnection;

    public final static String MetadataTableName          = "gpkg_metadata";
    public final static String MetadataReferenceTableName = "gpkg_metadata_reference";
}
