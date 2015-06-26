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

package com.rgi.geopackage.features;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.rgi.common.BoundingBox;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.ContentFactory;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageFeatures
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param core
     *             Access to GeoPackage's "core" methods
     */
    public GeoPackageFeatures(final Connection databaseConnection, final GeoPackageCore core)
    {
        this.databaseConnection = databaseConnection;
        this.core               = core;
    }

    /**
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return the Feature GeoPackage requirements this GeoPackage fails to conform to
     */
    public Collection<VerificationIssue> getVerificationIssues(final VerificationLevel verificationLevel)
    {
        return new FeaturesVerifier(this.databaseConnection, verificationLevel).getVerificationIssues();
    }

    /**
     * Creates a user defined features table, and adds a corresponding entry to
     * the content table
     *
     * @param tableName
     *            The name of the features table. The table name must begin
     *            with a letter (A..Z, a..z) or an underscore (_) and may only
     *            be followed by letters, underscores, or numbers, and may not
     *            begin with the prefix "gpkg_"
     * @param identifier
     *            A human-readable identifier (e.g. short name) for the
     *            tableName content
     * @param description
     *            A human-readable description for the tableName content
     * @param boundingBox
     *            Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *            Spatial Reference System (SRS)
     * @param geometryColumn
     * @param columnDefinitions
     * @return Returns a newly created user defined features table
     * @throws SQLException
     *             throws if the method {@link #getFeatureSet(String)
     *             getFeatureSet} or the method {@link
     *             DatabaseUtility#tableOrViewExists(Connection, String)
     *             tableOrViewExists} or if the database cannot roll back the
     *             changes after a different exception throws will throw an
     *             SQLException
     */
    public FeatureSet addFeatureSet(final String                   tableName,
                                    final String                   identifier,
                                    final String                   description,
                                    final BoundingBox              boundingBox,
                                    final SpatialReferenceSystem   spatialReferenceSystem,
                                    final GeometryColumnDefinition geometryColumn,
                                    final ColumnDefinition...      columnDefinitions) throws SQLException
    {
        GeoPackageCore.validateNewContentTableName(tableName);

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be mull.");
        }

        if(spatialReferenceSystem == null)
        {
            throw new IllegalArgumentException("Spatial reference system may not be null");
        }

        if(geometryColumn == null)
        {
            throw new IllegalArgumentException("Geometry column definition name may not be null");
        }

        if(columnDefinitions == null || Arrays.asList(columnDefinitions).stream().anyMatch(definition -> definition == null))
        {
            throw new IllegalArgumentException("Column definitions may not be null");
        }

        final FeatureSet existingContent = this.getFeatureSet(tableName);

        if(existingContent != null)
        {
            if(existingContent.equals(tableName,
                                      FeatureSet.FeatureContentType,
                                      identifier,
                                      description,
                                      boundingBox,
                                      spatialReferenceSystem.getIdentifier()))
            {
                return existingContent;
            }

            throw new IllegalArgumentException("An entry in the content table already exists with this table name, but has different values for its other fields");
        }

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
            throw new IllegalArgumentException("A table already exists with this feature set's table name");
        }

        try
        {
            // Create the feature set table
            this.addFeatureTableNoCommit(tableName,
                                         geometryColumn,
                                         columnDefinitions);

            // Add feature set to the content table
            this.core.addContent(tableName,
                                 FeatureSet.FeatureContentType,
                                 identifier,
                                 description,
                                 boundingBox,
                                 spatialReferenceSystem);

            this.addGeometryColumnNoCommit(tableName,
                                           geometryColumn,
                                           spatialReferenceSystem.getIdentifier());

            this.databaseConnection.commit();

            return this.getFeatureSet(tableName);
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Gets a feature set object based on its table name
     *
     * @param featureSetTableName
     *            Name of a feature set table
     * @return Returns a {@link FeatureSet} or null if there isn't with the
     *            supplied table name
     * @throws SQLException
     *             throws if the method
     *             {@link GeoPackageCore#getContent(String, ContentFactory, SpatialReferenceSystem)}
     *             throws an SQLException
     */
    public FeatureSet getFeatureSet(final String featureSetTableName) throws SQLException
    {
        return this.core.getContent(featureSetTableName,
                                    (tableName,
                                     dataType,
                                     identifier,
                                     description,
                                     lastChange,
                                     boundingBox,
                                     spatialReferenceSystem) -> new FeatureSet(tableName,
                                                                               identifier,
                                                                               description,
                                                                               lastChange,
                                                                               boundingBox,
                                                                               spatialReferenceSystem));
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "features"
     * data_type
     *
     * @return Returns a collection of {@link FeatureSet}s
     * @throws SQLException
     *             throws if the method
     *             {@link #getFeatureSets(SpatialReferenceSystem) getFeatureSets}
     *             throws
     */
    public Collection<FeatureSet> getFeatureSets() throws SQLException
    {
        return this.getFeatureSets(null);
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "features"
     * data_type that also match the supplied spatial reference system
     *
     * @param matchingSpatialReferenceSystem
     *            Spatial reference system that returned {@link FeatureSet}s
     *            much refer to
     * @return Returns a collection of {@link FeatureSet}s
     * @throws SQLException
     *             if there's an SQL error
     */
    public Collection<FeatureSet> getFeatureSets(final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        return this.core.getContent(FeatureSet.FeatureContentType,
                                    (tableName, dataType, identifier, description, lastChange, boundingBox, spatialReferenceSystem) -> new FeatureSet(tableName, identifier, description, lastChange, boundingBox, spatialReferenceSystem),
                                    matchingSpatialReferenceSystem);
    }

    public Feature getFeature(final FeatureSet featureSet,
                              final int        featureId)
    {

    }

    public List<Feature> getFeatures(final FeatureSet featureSet)
    {

    }

    public void visitFeatures(final FeatureSet featureSet)
    {

    }

    public Feature addFeature(final List<String> columns,
                              final List<Object> values)
    {

    }

    public void addFeatures(final List<String>       columns,
                            final List<List<Object>> values)
    {

    }

    /**
     * Creates the Geometry Column table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @throws SQLException
     */
    protected void createGeometryColumnTableNoCommit() throws SQLException
    {
        // Create the geometry column table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageFeatures.GeometryColumnsTableName))
        {
            JdbcUtility.update(this.databaseConnection, this.getGeometryColumnsCreationSql());
        }
    }

    /**
     * Creates an entry in the gpkg_geometry_columns table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or roll back as a single transaction.
     *
     * @param tableName
     *            The name of the features table

     * @param spatialReferenceSystemidentifier
     *            Spatial Reference System (SRS)
     * @throws SQLException
     */
    protected void addGeometryColumnNoCommit(final String                   tableName,
                                             final GeometryColumnDefinition geometryColumn,
                                             final int                      spatialReferenceSystemIdentifier) throws SQLException
    {
        this.createGeometryColumnTableNoCommit(); // Create the feature metadata table

        final String insertTileMatrix = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                                                      GeoPackageFeatures.GeometryColumnsTableName,
                                                      "table_name",
                                                      "column_name",
                                                      "geometry_type_name",
                                                      "srs_id",
                                                      "z",
                                                      "m");

        JdbcUtility.update(this.databaseConnection,
                           insertTileMatrix,
                           preparedStatement -> { preparedStatement.setString(1, tableName);
                                                  preparedStatement.setString(2, geometryColumn.getName());
                                                  preparedStatement.setString(3, geometryColumn.getType());
                                                  preparedStatement.setInt   (4, spatialReferenceSystemIdentifier);
                                                  preparedStatement.setInt   (5, geometryColumn.getZRequirement().getValue());
                                                  preparedStatement.setInt   (6, geometryColumn.getMRequirement().getValue());
                                                });
    }

    private void addFeatureTableNoCommit(final String                   featureTableName,
                                         final GeometryColumnDefinition geometryColumn,
                                         final ColumnDefinition...      columnDefinitions) throws SQLException
    {
        // http://www.geopackage.org/spec/#feature_user_tables
        // http://www.geopackage.org/spec/#example_feature_table_sql

        final List<AbstractColumnDefinition> columns = new LinkedList<>(Arrays.asList(columnDefinitions));

        columns.add(0, geometryColumn);

        final String userColumns = String.join("\n",
                                               columns.stream()
                                                      .map(column -> String.format("%s %s %s %s -- %s",
                                                                                   column.getName(),
                                                                                   column.getType(),
                                                                                   column.isNullable() ? "" : "NOT NULL",
                                                                                   column.isUnique()   ? "UNIQUE" : "",
                                                                                   column.getComment()))
                                                      .collect(Collectors.toList()));


        final String createTableSql = String.format("CREATE TABLE %s\n(id INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n%s);",
                                                    featureTableName,
                                                    userColumns);

        JdbcUtility.update(this.databaseConnection, createTableSql);
    }

    @SuppressWarnings("static-method")
    protected String getGeometryColumnsCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_geometry_columns_cols
        // http://www.geopackage.org/spec/#gpkg_geometry_columns_sql
        return "CREATE TABLE " + GeoPackageFeatures.GeometryColumnsTableName  + "\n" +
               "(table_name         TEXT    NOT NULL, -- Name of the table containing the geometry column\n"                                                      +
               " column_name        TEXT    NOT NULL, -- Name of a column in the feature table that is a Geometry Column\n"                                       +
               " geometry_type_name TEXT    NOT NULL, -- Name from Geometry Type Codes (Core) or Geometry Type Codes (Extension) in Geometry Types (Normative)\n" +
               " srs_id             INTEGER NOT NULL, -- Spatial Reference System ID: gpkg_spatial_ref_sys.srs_id\n"                                              +
               " z                  TINYINT NOT NULL, -- 0: z values prohibited; 1: z values mandatory; 2: z values optional\n"                                   +
               " m                  TINYINT NOT NULL, -- 0: m values prohibited; 1: m values mandatory; 2: m values optional\n"                                   +
               " CONSTRAINT pk_geom_cols     PRIMARY KEY (table_name, column_name),"                                                                              +
               " CONSTRAINT uk_gc_table_name UNIQUE      (table_name),"                                                                                           +
               " CONSTRAINT fk_gc_tn         FOREIGN KEY (table_name) REFERENCES gpkg_contents        (table_name),"                                              +
               " CONSTRAINT fk_gc_srs        FOREIGN KEY (srs_id)     REFERENCES gpkg_spatial_ref_sys (srs_id));";
    }

    /**
     * Gets all entries in the GeoPackage's contents table with the "features" data_type that also match the supplied spatial reference system
     * @param core the GeoPackage core object
     *
     * @param matchingSpatialReferenceSystem Spatial reference system that returned {@link FeatureSet}s much refer to
     * @return Returns a collection of {@link FeatureSet}s
     * @throws SQLException throws if an SQLException occurs
     */
    public static Collection<FeatureSet> getFeatureSets(final GeoPackageCore core, final SpatialReferenceSystem matchingSpatialReferenceSystem) throws SQLException
    {
        return core.getContent(FeatureSet.FeatureContentType,
                               (tableName, dataType, identifier, description, lastChange, boundingBox, spatialReferenceSystem) -> new FeatureSet(tableName, identifier, description, lastChange, boundingBox, spatialReferenceSystem),
                               matchingSpatialReferenceSystem);
    }

    public final static String GeometryColumnsTableName = "gpkg_geometry_columns";

    private final Connection     databaseConnection;
    private final GeoPackageCore core;
}
