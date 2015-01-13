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

package com.rgi.geopackage.core;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.verification.FailedRequirement;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageCore
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param dataModel
     *             Controls how a GeoPackage's tables are created
     */
    public GeoPackageCore(final Connection databaseConnection)
    {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @return the Core GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<FailedRequirement> getFailedRequirements(final File file)
    {
        return new CoreVerifier(file, this.databaseConnection).getFailedRequirements();
    }

    /**
     * Count the number of entries in a user content table
     *
     * @param content
     *             Specifies the content table whose rows will be counted
     * @return Number of rows in the table referenced by the content parameter
     * @throws SQLException
     */
    public long getRowCount(final Content content) throws SQLException
    {
        if(content == null)
        {
            throw new IllegalArgumentException("Content may not be null.");
        }

        final String rowCountSql = String.format("SELECT COUNT(*) FROM %s;", content.getTableName());

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(rowCountSql);
            final ResultSet         tileResult        = preparedStatement.executeQuery();)
        {
            return tileResult.getLong(1);
        }
    }

    /**
     * Create the default tables
     *
     * @throws SQLException
     */
    public void createDefaultTables() throws SQLException
    {
        try
        {
            // Create the spatial ref system table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getSpatialReferenceSystemCreationSql());
            }

            // Add the default entries to the spatial ref system table
            // See: http://www.geopackage.org/spec/#spatial_ref_sys -> 1.1.2.1.2. Table Data Values, Requirement 11
            this.addSpatialReferenceSystemNoCommit("World Geodetic System (WGS) 1984",
                                                      4326,
                                                      "EPSG",
                                                      4326,
                                                      "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]", // http://spatialreference.org/ref/epsg/wgs-84/ogcwkt/
                                                      "World Geodetic System 1984");

            this.addSpatialReferenceSystemNoCommit("Undefined Cartesian Coordinate Reference System",
                                                      -1,
                                                      "NONE",
                                                      -1,
                                                      "undefined",
                                                      "undefined Cartesian coordinate reference system");

            this.addSpatialReferenceSystemNoCommit("Undefined Geographic Coordinate Reference System",
                                                      0,
                                                      "NONE",
                                                      0,
                                                      "undefined",
                                                      "undefined Geographic coordinate reference system");

            // Create the package contents table or view
            try(Statement statement = this.databaseConnection.createStatement())
            {
                // http://www.geopackage.org/spec/#gpkg_contents_sql
                // http://www.geopackage.org/spec/#_contents
                statement.executeUpdate(this.getContentsCreationSql());
            }

            this.databaseConnection.commit();
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Adds a spatial reference system (SRS) to the gpkg_spatial_ref_sys table.
     *
     * @param name
     *             Human readable name of this spatial reference system
     * @param identifier
     *             Unique identifier for each Spatial Reference System within a GeoPackage
     * @param organization
     *             Case-insensitive name of the defining organization e.g. EPSG or epsg
     * @param organizationSrsId
     *             Numeric ID of the spatial reference system assigned by the organization
     * @param definition
     *             Well-known Text (WKT) representation of the spatial reference system
     * @param description
     *             Human readable description of this spatial reference system
     * @throws SQLException
     */
    public SpatialReferenceSystem addSpatialReferenceSystem(final String name,
                                                            final int    identifier,
                                                            final String organization,
                                                            final int    organizationSrsId,
                                                            final String definition,
                                                            final String description) throws SQLException
    {
        try
        {
            final SpatialReferenceSystem spatialReferenceSystem = this.addSpatialReferenceSystemNoCommit(name,
                                                                                                         identifier,
                                                                                                         organization,
                                                                                                         organizationSrsId,
                                                                                                         definition,
                                                                                                         description);

            this.databaseConnection.commit();

            return spatialReferenceSystem;
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Returns a unique spatial reference system (SRS) based on an
     * organization, and its organization-assigned numeric identifier.
     *
     * @param organization
     *             Name of the defining organization
     * @param organizationSrsId
     *             Numeric identifier of the Spatial Reference System assigned by the organization
     * @return Returns the unique spatial reference system (SRS), or null
     * @throws SQLException
     */
    public SpatialReferenceSystem getSpatialReferenceSystem(final String organization, final int organizationSrsId) throws SQLException
    {
        final String srsQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE organization = ? COLLATE NOCASE AND organization_coordsys_id = ?;",
                                                 "srs_name",
                                                 "srs_id",
                                                 "organization",
                                                 "organization_coordsys_id",
                                                 "definition",
                                                 "description",
                                                 GeoPackageCore.SpatialRefSysTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(srsQuerySql))
        {
            preparedStatement.setString(1, organization);
            preparedStatement.setInt   (2, organizationSrsId);

            try(ResultSet srsResult = preparedStatement.executeQuery())
            {
                if(srsResult.isBeforeFirst())
                {
                    return new SpatialReferenceSystem(srsResult.getString(1),
                                                      srsResult.getInt   (2),
                                                      srsResult.getString(3),
                                                      srsResult.getInt   (4),
                                                      srsResult.getString(5),
                                                      srsResult.getString(6));
                }
            }
        }

        return null;
    }

    /**
     * Returns a unique spatial reference system (SRS) based on its
     * unique identifier for each spatial reference system within a GeoPackage
     *
     * @param identifier
     *             Unique identifier for each Spatial Reference System within a GeoPackage
     * @return Returns the unique spatial reference system (SRS), or null
     * @throws SQLException
     */
    public SpatialReferenceSystem getSpatialReferenceSystem(final int identifier) throws SQLException
    {
        final String srsQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE srs_id = ?;",
                                                 "srs_name",
                                                 "srs_id",
                                                 "organization",
                                                 "organization_coordsys_id",
                                                 "definition",
                                                 "description",
                                                 GeoPackageCore.SpatialRefSysTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(srsQuerySql))
        {
            preparedStatement.setInt(1, identifier);

            try(ResultSet srsResult = preparedStatement.executeQuery())
            {
                if(srsResult.isBeforeFirst())
                {
                    return new SpatialReferenceSystem(srsResult.getString(1),
                                                      srsResult.getInt   (2),
                                                      srsResult.getString(3),
                                                      srsResult.getInt   (4),
                                                      srsResult.getString(5),
                                                      srsResult.getString(6));
                }
            }
        }

        return null;
    }

    /**
     * Add a reference to a tile or feature set to content table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @param tableName
     *             The name of the tiles, feature, or extension specific content table
     * @param dataType
     *             Type of data stored in the table: "features" per clause Features, "tiles" per clause Tiles, or an implementer-defined value for other data tables per clause in an Extended GeoPackage.
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     * @throws SQLException
     */
    public Content addContent(final String                 tableName,
                              final String                 dataType,
                              final String                 identifier,
                              final String                 description,
                              final BoundingBox            boundingBox,
                              final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Tile set name may not be null");
        }

        if(!tableName.matches("^[_a-zA-Z]\\w*"))
        {
            throw new IllegalArgumentException("The tile set's name must begin with a letter (A..Z, a..z) or an underscore (_) and may only be followed by letters, underscores, or numbers");
        }

        if(tableName.startsWith("gpkg_"))
        {
            throw new IllegalArgumentException("The tile set's name may not start with the reserved prefix 'gpkg_'");
        }

        if(dataType == null || dataType.isEmpty())
        {
            throw new IllegalArgumentException("Data type cannot be null, or empty.");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be mull.");
        }

        final Content existingContent = this.getContent(tableName);

        if(existingContent != null)
        {
            if(!existingContent.equals(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem))
            {
                throw new IllegalArgumentException("A content with this table name or identifier already exists but with different properties");
            }

            return existingContent;
        }

        final String insertContent = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                                   GeoPackageCore.ContentsTableName,
                                                   "table_name",
                                                   "data_type",
                                                   "identifier",
                                                   "description",
                                                   "min_x",
                                                   "min_y",
                                                   "max_x",
                                                   "max_y",
                                                   "srs_id");

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertContent))
        {
            final Integer srsId = spatialReferenceSystem == null ? null
                                                                 : spatialReferenceSystem.getIdentifier();

            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, dataType);
            preparedStatement.setString(3, identifier);
            preparedStatement.setString(4, description);
            preparedStatement.setObject(5, boundingBox.getMinX(), Types.DOUBLE); // Using setObject because spec allows the bounding box values to be null
            preparedStatement.setObject(6, boundingBox.getMinY(), Types.DOUBLE);
            preparedStatement.setObject(7, boundingBox.getMaxX(), Types.DOUBLE);
            preparedStatement.setObject(8, boundingBox.getMaxY(), Types.DOUBLE);
            preparedStatement.setObject(9, srsId, Types.INTEGER);                // Using setObject because the spec allows the srs id be null

            preparedStatement.executeUpdate();
        }

        return this.getContent(tableName);
    }

    /**
     * Request all of a specific type of conent from the {@value #ContentsTableName} table that matches a specific spatial reference system
     *
     * @param dataType
     *            Type of content being requested e.g. "tiles", "features" or another value representing an extended GeoPackage's content
     * @param contentFactory
     *            Mechanism used to create a type that corresponds to the dataType
     * @param matchingSpatialReferenceSystem
     *            Results must reference this spatial reference system.  Results are unfiltered if this parameter is null
     * @return Returns a Collection {@link Content}s of the type indicated by the {@link ContentFactory}
     * @throws SQLException
     */
    public <T extends Content> Collection<T> getContent(final String                 dataType,
                                                        final ContentFactory<T>      contentFactory,
                                                        final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        if(dataType == null || dataType.isEmpty())
        {
            throw new IllegalArgumentException("Data type may not be null or empty");
        }

        if(contentFactory == null)
        {
            throw new IllegalArgumentException("Content factory may not be null");
        }

        final ArrayList<T> content = new ArrayList<>();

        final String query = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE data_type = ?%s;",
                                           "table_name",
                                           "data_type",
                                           "identifier",
                                           "description",
                                           "strftime('%Y-%m-%dT%H:%M:%fZ', last_change)",
                                           "min_x",
                                           "min_y",
                                           "max_x",
                                           "max_y",
                                           "srs_id",
                                           GeoPackageCore.ContentsTableName,
                                           matchingSpatialReferenceSystem != null ? " AND srs_id = ?"
                                                                                  : "");

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(query))
        {
            preparedStatement.setString(1, dataType);

            if(matchingSpatialReferenceSystem != null)
            {
                preparedStatement.setInt(2, matchingSpatialReferenceSystem.getIdentifier());
            }

            try(ResultSet results = preparedStatement.executeQuery())
            {
                while(results.next())
                {
                    final Date lastChange = null;
                    try
                    {
                        GeoPackageCore.DateFormat.parse(results.getString(5));
                    }
                    catch(final ParseException ex)
                    {
                        // This should never be reached
                        // do nothing, lastChange is already null which is what we'll pass along.
                        ex.printStackTrace();
                    }

                    final Integer srsId = (Integer)results.getObject(10);

                    final SpatialReferenceSystem spatialReferenceSystem = (srsId != null) ? this.getSpatialReferenceSystem(srsId)
                                                                                          : null;

                    content.add(contentFactory.create(results.getString(1),                          // table name
                                                      results.getString(2),                          // data type
                                                      results.getString(3),                          // identifier
                                                      results.getString(4),                          // description
                                                      lastChange,                                    // last change
                                                      new BoundingBox((Double)results.getObject(7),  // min y        // Unfortunately as of Xerial's SQLite JDBC implementation 3.8.7 getObject(int columnIndex, Class<T> type) is unimplemented, so a cast is required
                                                                      (Double)results.getObject(6),  // min x
                                                                      (Double)results.getObject(9),  // max y
                                                                      (Double)results.getObject(8)), // max x
                                                      spatialReferenceSystem));                      // srs id
                }
            }
        }

        return content;
    }



    /**
     * Gets a specific entry in the contents table based on the name of the table the entry corresponds to
     *
     * @param tableName
     *             Table name to search for
     * @param contentFactory
     *             Mechanism used to create the correct subtype of Content
     * @return Returns a {@link Content} of the type indicated by the {@link ContentFactory}
     * @throws SQLException
     */
    public <T extends Content> T getContent(final String tableName, final ContentFactory<T> contentFactory) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Table name may not be null or empty");
        }

        if(contentFactory == null)
        {
            throw new IllegalArgumentException("Content factory may not be null");
        }

        final String contentQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s WHERE table_name = ?;",
                                                     "data_type",
                                                     "identifier",
                                                     "description",
                                                     "strftime('%Y-%m-%dT%H:%M:%fZ', last_change)",
                                                     "min_x",
                                                     "min_y",
                                                     "max_x",
                                                     "max_y",
                                                     "srs_id",
                                                     GeoPackageCore.ContentsTableName);

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(contentQuerySql))
        {
            preparedStatement.setString(1, tableName);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    Date lastChange = null;
                    try
                    {
                        lastChange = GeoPackageCore.DateFormat.parse(result.getString(4));
                    }
                    catch(final ParseException ex)
                    {
                        // This should never be reached
                        // do nothing, lastChange is already null which is what we'll pass along.
                        ex.printStackTrace();
                    }

                    final Integer srsId = (Integer)result.getObject(9);

                    final SpatialReferenceSystem spatialReferenceSystem = (srsId != null) ? this.getSpatialReferenceSystem(srsId)
                                                                                          : null;

                    return contentFactory.create(tableName,                                    // table name
                                                 result.getString(1),                          // data type
                                                 result.getString(2),                          // identifier
                                                 result.getString(3),                          // description
                                                 lastChange,                                   // last change
                                                 new BoundingBox((Double)result.getObject(6),  // min y        // Unfortunately as of Xerial's SQLite JDBC implementation 3.8.7 getObject(int columnIndex, Class<T> type) is unimplemented, so a cast is required
                                                                 (Double)result.getObject(5),  // min x
                                                                 (Double)result.getObject(8),  // max y
                                                                 (Double)result.getObject(7)), // max x
                                                 spatialReferenceSystem);                      // srs id
                }

                return null;
            }
        }
    }

    /**
     * Adds a spatial reference system (SRS) to the gpkg_spatial_ref_sys table.
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @param name
     *             Human readable name of this spatial reference system
     * @param identifier
     *             Unique identifier for each Spatial Reference System within a GeoPackage
     * @param organization
     *             Case-insensitive name of the defining organization e.g. EPSG or epsg
     * @param organizationSrsId
     *             Numeric ID of the spatial reference system assigned by the organization
     * @param definition
     *             Well-known Text (WKT) representation of the spatial reference system
     * @param description
     *             Human readable description of this spatial reference system
     * @throws SQLException
     */
    private SpatialReferenceSystem addSpatialReferenceSystemNoCommit(final String name,
                                                                     final int    identifier,
                                                                     final String organization,
                                                                     final int    organizationSrsId,
                                                                     final String definition,
                                                                     final String description) throws SQLException
    {
        if(name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("Name may not be null or empty");
        }

        if(organization == null || organization.isEmpty())
        {
            throw new IllegalArgumentException("Organization may not be null or empty");
        }

        if(definition == null || definition.isEmpty())
        {
            throw new IllegalArgumentException("Definition may not be null or empty");
        }

        // TODO: It'd be nice to do an additional check to see if 'definition' was a conformant WKT SRS

        final SpatialReferenceSystem existingSrs = this.getSpatialReferenceSystem(identifier);

        if(existingSrs != null)
        {
            if(existingSrs.equals(name,
                                  identifier,
                                  organization,
                                  organizationSrsId,
                                  definition))
            {
                return existingSrs;
            }

            throw new IllegalArgumentException("A spatial reference system already exists with this identifier, but has different values for its other fields");
        }

        final String insertSpatialRef = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                                                      GeoPackageCore.SpatialRefSysTableName,
                                                      "srs_name",
                                                      "srs_id",
                                                      "organization",
                                                      "organization_coordsys_id",
                                                      "definition",
                                                      "description");

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertSpatialRef))
        {
            preparedStatement.setString(1, name);
            preparedStatement.setInt   (2, identifier);
            preparedStatement.setString(3, organization);
            preparedStatement.setInt   (4, organizationSrsId);
            preparedStatement.setString(5, definition);
            preparedStatement.setString(6, description);

            preparedStatement.executeUpdate();

            return new SpatialReferenceSystem(name,
                                              identifier,
                                              organization,
                                              organizationSrsId,
                                              definition,
                                              description);
        }
    }

    /**
     * Gets a specific entry in the contents table based on the name of the table the entry corresponds to
     *
     * @param tableName
     *             Table name to search for
     * @return Returns a {@link Content}
     * @throws SQLException
     */
    private Content getContent(final String tableName) throws SQLException
    {
        return this.getContent(tableName,
                               (inTableName, inDataType, inIdentifier, inDescription, inLastChange, inBoundingBox, inSpatialReferenceSystem) -> new Content(inTableName, inDataType, inIdentifier, inDescription, inLastChange, inBoundingBox, inSpatialReferenceSystem));
    }

    @SuppressWarnings("static-method")
    protected String getSpatialReferenceSystemCreationSql()
    {
        // http://www.geopackage.org/spec/#_gpkg_spatial_ref_sys
        // http://www.geopackage.org/spec/#spatial_ref_sys
        return "CREATE TABLE " + GeoPackageCore.SpatialRefSysTableName                                                                                    +
               "(srs_name                 TEXT    NOT NULL,             -- Human readable name of this SRS (Spatial Reference System)\n"              +
               " srs_id                   INTEGER NOT NULL PRIMARY KEY, -- Unique identifier for each Spatial Reference System within a GeoPackage\n" +
               " organization             TEXT    NOT NULL,             -- Case-insensitive name of the defining organization e.g. EPSG or epsg\n"    +
               " organization_coordsys_id INTEGER NOT NULL,             -- Numeric ID of the Spatial Reference System assigned by the organization\n" +
               " definition               TEXT    NOT NULL,             -- Well-known Text representation of the Spatial Reference System\n"          +
               " description              TEXT);                        -- Human readable description of this SRS\n";
    }

    @SuppressWarnings("static-method")
    protected String getContentsCreationSql()
    {
       // http://www.geopackage.org/spec/#gpkg_contents_sql
       // http://www.geopackage.org/spec/#_contents
       return "CREATE TABLE " + GeoPackageCore.ContentsTableName + "\n" +
              "(table_name  TEXT     NOT NULL PRIMARY KEY,                                    -- The name of the tiles, or feature table\n"                                  +
              " data_type   TEXT     NOT NULL,                                                -- Type of data stored in the table: \"features\" per clause Features (http://www.geopackage.org/spec/#features), \"tiles\" per clause Tiles (http://www.geopackage.org/spec/#tiles), or an implementer-defined value for other data tables per clause in an Extended GeoPackage\n" +
              " identifier  TEXT     UNIQUE,                                                  -- A human-readable identifier (e.g. short name) for the table_name content\n" +
              " description TEXT     DEFAULT '',                                              -- A human-readable description for the table_name content\n"                  +
              " last_change DATETIME NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')), -- Timestamp value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time\n" +
              " min_x       DOUBLE,                                                           -- Bounding box minimum easting or longitude for all content in table_name\n"  +
              " min_y       DOUBLE,                                                           -- Bounding box minimum northing or latitude for all content in table_name\n"  +
              " max_x       DOUBLE,                                                           -- Bounding box maximum easting or longitude for all content in table_name\n"  +
              " max_y       DOUBLE,                                                           -- Bounding box maximum northing or latitude for all content in table_name\n"  +
              " srs_id      INTEGER,                                                          -- Spatial Reference System ID: gpkg_spatial_ref_sys.srs_id; when data_type is features, SHALL also match gpkg_geometry_columns.srs_id; When data_type is tiles, SHALL also match gpkg_tile_matrix_set.srs.id\n" +
              " CONSTRAINT fk_gc_r_srs_id FOREIGN KEY (srs_id) REFERENCES gpkg_spatial_ref_sys(srs_id));";
    }

    private final Connection databaseConnection;

    public final static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public final static String SpatialRefSysTableName = "gpkg_spatial_ref_sys";
    public final static String ContentsTableName      = "gpkg_contents";
}
