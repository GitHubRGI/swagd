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

package com.rgi.geopackage.core;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * 'Core' subsystem of the {@link GeoPackage} implementation
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageCore
{
    /**
     * The Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     */
    public final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * The name of the GeoPackage Spatial Reference System Table "gpkg_spatial_ref_sys"
     */
    public final static String SpatialRefSysTableName = "gpkg_spatial_ref_sys";

    /**
     * The name of the GeoPackage Contents Table "gpkg_contents"
     */
    public final static String ContentsTableName = "gpkg_contents";


    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @throws SQLException throws if an SQLException occurs
     */
    public GeoPackageCore(final Connection databaseConnection) throws SQLException
    {
        this(databaseConnection, false);
    }

    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param createDefaults
     *             If true, GeoPackageCore will create the default tables and entries required by all standard
     * @throws SQLException throws if an SQLException occurs
     */
    public GeoPackageCore(final Connection databaseConnection, final boolean createDefaults) throws SQLException
    {
        this.databaseConnection = databaseConnection;

        if(createDefaults)
        {
            this.createDefaultTables();
        }
    }

    /**
     * Requirements this GeoPackage failed to meet
     *
     * @param file
     *             the GeoPackage database file
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return The Core GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<VerificationIssue> getVerificationIssues(final File file, final VerificationLevel verificationLevel)
    {
        return new CoreVerifier(file, this.databaseConnection, verificationLevel).getVerificationIssues();
    }

    /**
     * Count the number of entries in a user content table
     *
     * @param content
     *             Specifies the content table whose rows will be counted
     * @return Number of rows in the table referenced by the content parameter
     * @throws SQLException throws if an SQLException occurs
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
     * Adds a spatial reference system (SRS) to the gpkg_spatial_ref_sys table.
     *
     * @param name
     *             Human readable name of this spatial reference system
     * @param organization
     *             Case-insensitive name of the defining organization e.g. EPSG or epsg
     * @param organizationSrsId
     *             Numeric ID of the spatial reference system assigned by the organization
     * @param definition
     *             Well-known Text (WKT) representation of the spatial reference system
     * @param description
     *             Human readable description of this spatial reference system
     * @return  a Spatial Reference System with the following parameters
     * @throws SQLException throws if an SQLException occurs
     */
    public SpatialReferenceSystem addSpatialReferenceSystem(final String name,
                                                            final String organization,
                                                            final int    organizationSrsId,
                                                            final String definition,
                                                            final String description) throws SQLException
    {
        try
        {
            final SpatialReferenceSystem existingSrs = this.getSpatialReferenceSystem(organization, organizationSrsId);

            if(existingSrs != null)
            {
                if(existingSrs.equals(name,
                                      organization,
                                      organizationSrsId,
                                      definition))
                {
                    return existingSrs;
                }

                throw new IllegalArgumentException("A spatial reference system already exists with this organization and organization-assigned numeric identifier, but has different values for its other fields");
            }

            final Integer identifier = DatabaseUtility.nextValue(this.databaseConnection,
                                                                 GeoPackageCore.SpatialRefSysTableName,
                                                                 GeoPackageCore.SpatialRefSystemSrsIdColumnName);
            if(identifier == null)
            {
                throw new RuntimeException("There are no more available integer values to represent the column \"identifier\".");
            }

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
     * Returns a unique spatial reference system (SRS) based on an organization,
     * and its organization-assigned numeric identifier.
     *
     * @param organization
     *            Name of the defining organization
     * @param organizationSrsId
     *            Numeric identifier of the Spatial Reference System assigned by
     *            the organization
     * @return Returns the unique spatial reference system (SRS), or null
     * @throws SQLException
     *             throws if an SQLException occurs or thrown by automatic
     *             close() invocation on preparedStatement
     */
    public SpatialReferenceSystem getSpatialReferenceSystem(final String organization, final int organizationSrsId) throws SQLException
    {
        if(organization == null || organization.isEmpty())
        {
            throw new IllegalArgumentException("Organization may not be null or empty");
        }

        final String srsQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE organization COLLATE NOCASE IN (?) AND organization_coordsys_id = ?;",
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
     * Returns a unique spatial reference system (SRS) based on its unique
     * identifier for each spatial reference system within a GeoPackage
     *
     * @param identifier
     *            Unique identifier for each spatial reference system within a
     *            GeoPackage
     * @return Returns the unique spatial reference system (SRS), or null
     * @throws SQLException
     *             thrown by automatic close() invocation on preparedStatement
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
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     * @return  a Content Object with the following parameter values
     * @throws SQLException throws if an SQLException occurs
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

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
           throw new IllegalArgumentException("Content entry references a table that does not exist");
        }

        if(dataType == null || dataType.isEmpty())
        {
            throw new IllegalArgumentException("Data type cannot be null, or empty.");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be null.");
        }

        final Content existingContent = this.getContent(tableName);

        if(existingContent != null)
        {
            if(!existingContent.equals(tableName, dataType, identifier, description, boundingBox, spatialReferenceSystem.getIdentifier()))
            {
                throw new IllegalArgumentException("A content entry with this table name or identifier already exists but with different properties");
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
     * Request all of a specific type of content from the {@value #ContentsTableName} table that matches a specific spatial reference system
     *
     * @param dataType
     *            Type of content being requested e.g. "tiles", "features" or another value representing an extended GeoPackage's content
     * @param contentFactory
     *            Mechanism used to create a type that corresponds to the dataType
     * @param spatialReferenceSystem
     *            Results must reference this spatial reference system.  Results are unfiltered if this parameter is null
     * @return Returns a Collection {@link Content}s of the type indicated by the {@link ContentFactory}
     * @throws SQLException  SQLException thrown by automatic close() invocation on preparedStatement or if other various SQLExceptions occur
     */
    public <T extends Content> Collection<T> getContent(final String                 dataType,
                                                        final ContentFactory<T>      contentFactory,
                                                        final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        if(dataType == null || dataType.isEmpty())
        {
            throw new IllegalArgumentException("Data type may not be null or empty");
        }

        if(contentFactory == null)
        {
            throw new IllegalArgumentException("Content factory may not be null");
        }


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
                                           spatialReferenceSystem != null ? " AND srs_id = ?"
                                                                                  : "");

        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(query))
        {
            preparedStatement.setString(1, dataType);

            if(spatialReferenceSystem != null)
            {
                preparedStatement.setInt(2, spatialReferenceSystem.getIdentifier());
            }

            try(ResultSet         results         = preparedStatement.executeQuery();
                Stream<ResultSet> resultSetStream = ResultSetStream.getStream(results))
            {
                return resultSetStream.map(result -> { try
                                                       {
                                                           return contentFactory.create(result.getString(1),                          // table name
                                                                                        result.getString(2),                          // data type
                                                                                        result.getString(3),                          // identifier
                                                                                        result.getString(4),                          // description
                                                                                        result.getString(5),                          // last change
                                                                                        new BoundingBox((Double)result.getObject(6),  // min x        // Unfortunately as of Xerial's SQLite JDBC implementation 3.8.7 getObject(int columnIndex, Class<T> type) is unimplemented, so a cast is required
                                                                                                        (Double)result.getObject(7),  // min y
                                                                                                        (Double)result.getObject(8),  // max x
                                                                                                        (Double)result.getObject(9)), // max y
                                                                                        (Integer)result.getObject(10));               // srs id
                                                       }
                                                       catch(final SQLException ex)
                                                       {
                                                           return null;
                                                       }
                                                      })
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toCollection(ArrayList::new));
            }
        }
    }

    /**
     * Gets a specific entry in the contents table based on the name of the table the entry corresponds to
     *
     * @param tableName
     *             Table name to search for
     * @param contentFactory
     *             Mechanism used to create the correct subtype of Content
     * @return Returns a {@link Content} of the type indicated by the {@link ContentFactory}
     * @throws SQLException throws if an SQLException occurs
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
                    return contentFactory.create(tableName,                                    // table name
                                                 result.getString(1),                          // data type
                                                 result.getString(2),                          // identifier
                                                 result.getString(3),                          // description
                                                 result.getString(4),                          // last change
                                                 new BoundingBox((Double)result.getObject(5),  // min x        // Unfortunately as of Xerial's SQLite JDBC implementation 3.8.7 getObject(int columnIndex, Class<T> type) is unimplemented, so a cast is required
                                                                 (Double)result.getObject(6),  // min y
                                                                 (Double)result.getObject(7),  // max x
                                                                 (Double)result.getObject(8)), // max y
                                                 (Integer)result.getObject(9));                // srs id
                }

                return null;
            }
        }
    }

    /**
     * Adds a spatial reference system (SRS) to the gpkg_spatial_ref_sys table. <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @param name
     *            Human readable name of this spatial reference system
     * @param identifier
     *            Unique identifier for each Spatial Reference System within a
     *            GeoPackage
     * @param organization
     *            Case-insensitive name of the defining organization e.g. EPSG
     *            or epsg
     * @param organizationSrsId
     *            Numeric ID of the spatial reference system assigned by the
     *            organization
     * @param definition
     *            Well-known Text (WKT) representation of the spatial reference
     *            system
     * @param description
     *            Human readable description of this spatial reference system
     * @throws SQLException
     *             thrown by automatic close() invocation on preparedStatement
     *             to add the Spatial Reference System Object to the database or
     *             if the method {@link #getSpatialReferenceSystem(int)
     *             getSpatailReferenceSystem} throws
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
     * Create the default tables, and default SRS entries
     *
     * @throws SQLException
     */
    protected void createDefaultTables() throws SQLException
    {
        try
        {
            // Create the spatial reference system table
            if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageCore.SpatialRefSysTableName))
            {
                try(Statement statement = this.databaseConnection.createStatement())
                {
                    statement.executeUpdate(this.getSpatialReferenceSystemCreationSql());
                }
            }

            // Add the default entries to the spatial reference system table
            // See: http://www.geopackage.org/spec/#spatial_ref_sys -> 1.1.2.1.2. Table Data Values, Requirement 11
            this.addSpatialReferenceSystemNoCommit("World Geodetic System (WGS) 1984",
                                                   1,
                                                   "EPSG",
                                                   4326,
                                                   "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]", // http://spatialreference.org/ref/epsg/wgs-84/ogcwkt/
                                                   "World Geodetic System 1984");

            this.addSpatialReferenceSystemNoCommit("Undefined Cartesian Coordinate Reference System",
                                                   -1,
                                                   "NONE",
                                                   -1,
                                                   "undefined",
                                                   "Undefined Cartesian coordinate reference system");

            this.addSpatialReferenceSystemNoCommit("Undefined Geographic Coordinate Reference System",
                                                   0,
                                                   "NONE",
                                                   0,
                                                   "undefined",
                                                   "Undefined geographic coordinate reference system");

            // Create the package contents table or view
            if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageCore.ContentsTableName))
            {
                try(Statement statement = this.databaseConnection.createStatement())
                {
                    // http://www.geopackage.org/spec/#gpkg_contents_sql
                    // http://www.geopackage.org/spec/#_contents
                    statement.executeUpdate(this.getContentsCreationSql());
                }
            }

            this.databaseConnection.commit();
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    @SuppressWarnings("static-method")
    protected String getSpatialReferenceSystemCreationSql()
    {
        // http://www.geopackage.org/spec/#_gpkg_spatial_ref_sys
        // http://www.geopackage.org/spec/#spatial_ref_sys
        return "CREATE TABLE " + GeoPackageCore.SpatialRefSysTableName + "\n" +
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

    private final Connection databaseConnection;

    private static final String SpatialRefSystemSrsIdColumnName = "srs_id";
}
