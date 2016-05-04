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

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.features.geometry.GeometryFactory;
import com.rgi.geopackage.features.geometry.m.WkbGeometryCollectionM;
import com.rgi.geopackage.features.geometry.m.WkbGeometryM;
import com.rgi.geopackage.features.geometry.m.WkbLineStringM;
import com.rgi.geopackage.features.geometry.m.WkbMultiLineStringM;
import com.rgi.geopackage.features.geometry.m.WkbMultiPointM;
import com.rgi.geopackage.features.geometry.m.WkbMultiPolygonM;
import com.rgi.geopackage.features.geometry.m.WkbPointM;
import com.rgi.geopackage.features.geometry.m.WkbPolygonM;
import com.rgi.geopackage.features.geometry.xy.WkbGeometryCollection;
import com.rgi.geopackage.features.geometry.xy.WkbLineString;
import com.rgi.geopackage.features.geometry.xy.WkbMultiLineString;
import com.rgi.geopackage.features.geometry.xy.WkbMultiPoint;
import com.rgi.geopackage.features.geometry.xy.WkbMultiPolygon;
import com.rgi.geopackage.features.geometry.xy.WkbPoint;
import com.rgi.geopackage.features.geometry.xy.WkbPolygon;
import com.rgi.geopackage.features.geometry.z.WkbGeometryCollectionZ;
import com.rgi.geopackage.features.geometry.z.WkbGeometryZ;
import com.rgi.geopackage.features.geometry.z.WkbLineStringZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiLineStringZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiPointZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiPolygonZ;
import com.rgi.geopackage.features.geometry.z.WkbPointZ;
import com.rgi.geopackage.features.geometry.z.WkbPolygonZ;
import com.rgi.geopackage.features.geometry.zm.WkbGeometryCollectionZM;
import com.rgi.geopackage.features.geometry.zm.WkbGeometryZM;
import com.rgi.geopackage.features.geometry.zm.WkbLineStringZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiLineStringZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiPointZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiPolygonZM;
import com.rgi.geopackage.features.geometry.zm.WkbPointZM;
import com.rgi.geopackage.features.geometry.zm.WkbPolygonZM;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        this.geometryFactories.put(GeometryType.Geometry          .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 0)"); } );      // type 0 XY
        this.geometryFactories.put(GeometryType.Point             .getCode(), WkbPoint          ::readWellKnownBinary); // type 1 XY
        this.geometryFactories.put(GeometryType.LineString        .getCode(), WkbLineString     ::readWellKnownBinary); // type 2 XY
        this.geometryFactories.put(GeometryType.Polygon           .getCode(), WkbPolygon        ::readWellKnownBinary); // type 3 XY
        this.geometryFactories.put(GeometryType.MultiPoint        .getCode(), WkbMultiPoint     ::readWellKnownBinary); // type 4 XY
        this.geometryFactories.put(GeometryType.MultiLineString   .getCode(), WkbMultiLineString::readWellKnownBinary); // type 5 XY
        this.geometryFactories.put(GeometryType.MultiPolygon      .getCode(), WkbMultiPolygon   ::readWellKnownBinary); // type 6 XY
        this.geometryFactories.put(GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollection.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XY

        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Geometry           .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 1000)"); } );   // type 0 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Point              .getCode(), WkbPointZ          ::readWellKnownBinary);  // type 1 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.LineString         .getCode(), WkbLineStringZ     ::readWellKnownBinary);  // type 2 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Polygon            .getCode(), WkbPolygonZ        ::readWellKnownBinary);  // type 3 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiPoint         .getCode(), WkbMultiPointZ     ::readWellKnownBinary);  // type 4 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiLineString    .getCode(), WkbMultiLineStringZ::readWellKnownBinary);  // type 5 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon       .getCode(), WkbMultiPolygonZ   ::readWellKnownBinary);  // type 6 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionZ.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYZ

        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Geometry       .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 2000)"); } );   // type 0 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Point          .getCode(), WkbPointM          ::readWellKnownBinary);  // type 1 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.LineString     .getCode(), WkbLineStringM     ::readWellKnownBinary);  // type 2 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Polygon        .getCode(), WkbPolygonM        ::readWellKnownBinary);  // type 3 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiPoint     .getCode(), WkbMultiPointM     ::readWellKnownBinary);  // type 4 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiLineString.getCode(), WkbMultiLineStringM::readWellKnownBinary);  // type 5 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon   .getCode(), WkbMultiPolygonM   ::readWellKnownBinary);  // type 6 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionM.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYM

        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Geometry       .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 3000)"); } );   // type 0 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Point          .getCode(), WkbPointZM          ::readWellKnownBinary);  // type 1 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.LineString     .getCode(), WkbLineStringZM     ::readWellKnownBinary);  // type 2 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Polygon        .getCode(), WkbPolygonZM        ::readWellKnownBinary);  // type 3 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiPoint     .getCode(), WkbMultiPointZM     ::readWellKnownBinary);  // type 4 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiLineString.getCode(), WkbMultiLineStringZM::readWellKnownBinary);  // type 5 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon   .getCode(), WkbMultiPolygonZM   ::readWellKnownBinary);  // type 6 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionZM.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYZM
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
     *             The name of the features table. The table name must begin
     *             with a letter (A..Z, a..z) or an underscore (_) and may only
     *             be followed by letters, underscores, or numbers, and may not
     *             begin with the prefix "gpkg_"
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the
     *             tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     * @param primaryKeyColumnName
     *             Column name for the primary key. The column name must begin
     *             with a letter (A..Z, a..z) or an underscore (_) and may
     *             only be followed by letters, underscores, or numbers
     * @param geometryColumn
     *             Geometry column definition
     * @param columnDefinitions
     *             Definitions of non-geometry columns
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
                                    final String                   primaryKeyColumnName,
                                    final GeometryColumnDefinition geometryColumn,
                                    final ColumnDefinition...      columnDefinitions) throws SQLException
    {
        return this.addFeatureSet(tableName,
                                  identifier,
                                  description,
                                  boundingBox,
                                  spatialReferenceSystem,
                                  primaryKeyColumnName,
                                  geometryColumn,
                                  Arrays.asList(columnDefinitions));
    }

    /**
     * Creates a user defined features table, and adds a corresponding entry to
     * the content table
     *
     * @param tableName
     *             The name of the features table. The table name must begin
     *             with a letter (A..Z, a..z) or an underscore (_) and may only
     *             be followed by letters, underscores, or numbers, and may not
     *             begin with the prefix "gpkg_"
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the
     *             tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     * @param primaryKeyColumnName
     *             Column name for the primary key. The column name must begin
     *             with a letter (A..Z, a..z) or an underscore (_) and may
     *             only be followed by letters, underscores, or numbers
     * @param geometryColumn
     *             Geometry column definition
     * @param columnDefinitions
     *             Definitions of non-geometry columns
     * @return Returns a newly created user defined features table
     * @throws SQLException
     *             throws if the method {@link #getFeatureSet(String)
     *             getFeatureSet} or the method {@link
     *             DatabaseUtility#tableOrViewExists(Connection, String)
     *             tableOrViewExists} or if the database cannot roll back the
     *             changes after a different exception throws will throw an
     *             SQLException
     */
    public FeatureSet addFeatureSet(final String                       tableName,
                                    final String                       identifier,
                                    final String                       description,
                                    final BoundingBox                  boundingBox,
                                    final SpatialReferenceSystem       spatialReferenceSystem,
                                    final String                       primaryKeyColumnName,
                                    final GeometryColumnDefinition     geometryColumn,
                                    final Collection<ColumnDefinition> columnDefinitions) throws SQLException
    {
        DatabaseUtility.validateTableName(tableName);

        if(geometryColumn == null)
        {
            throw new IllegalArgumentException("Geometry column definition name may not be null");
        }

        if(columnDefinitions == null || columnDefinitions.contains(null))
        {
            throw new IllegalArgumentException("Column definitions may not be null");
        }

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
            throw new IllegalArgumentException("A table already exists with this feature set's table name");
        }

        try
        {
            // Create the feature set table
            this.addFeatureTableNoCommit(tableName,
                                         primaryKeyColumnName,
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

            return this.getFeatureSet(tableName);   // TODO this is a lazy way of doing things (carried on from the tiles implementation). There should be a method in core to query for the only information that can't already be obtained in this function - the last_change
        }
        catch(final Throwable th)
        {
            this.databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Gets the geometry column definition for a specified {@link FeatureSet}
     *
     * @param featureSet
     *             Target feature set
     * @return a {@link GeometryColumn}
     * @throws SQLException
     *             When there is a database error
     */
    public GeometryColumn getGeometryColumn(final FeatureSet featureSet) throws SQLException
    {
        if(featureSet == null)
        {
            throw new IllegalArgumentException("Feature set may not be null");
        }

        final String geometryColumnQuery = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ?",
                                                         "column_name",
                                                         "geometry_type_name",
                                                         "srs_id",
                                                         "z",
                                                         "m",
                                                         GeoPackageFeatures.GeometryColumnsTableName,
                                                         "table_name");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     geometryColumnQuery,
                                     preparedStatement -> preparedStatement.setString(1, featureSet.getTableName()),
                                     resultSet -> new GeometryColumn(featureSet.getTableName(),
                                                                     resultSet.getString(1),                          // geometry column name
                                                                     resultSet.getString(2),                          // geometry column type
                                                                     resultSet.getInt   (3),                          // geometry column srs id
                                                                     ValueRequirement.fromInt(resultSet.getInt(4)),   // z value requirement
                                                                     ValueRequirement.fromInt(resultSet.getInt(5)))); // m value requirement
    }

    /**
     * Gets the non-identifier (primary key) and non-geometry columns
     *
     * @param featureSet
     *             Handle to a feature table
     * @return a collection of {@link Column}s that describe the attributes the feature set contains
     * @throws SQLException
     *             If there is a database error
     */
    public List<Column> getAttributeColumns(final FeatureSet featureSet) throws SQLException
    {
        if(featureSet == null)
        {
            throw new IllegalArgumentException("Feature set may not be null");
        }

        try(final Statement statement = GeoPackageFeatures.this.databaseConnection.createStatement())
        {
            //noinspection JDBCExecuteWithNonConstantString
            try(final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s)", featureSet.getTableName())))
            {
                final List<Column> columns = new ArrayList<>();

                while(tableInfo.next())
                {
                    final String name = tableInfo.getString("name");

                    if(!tableInfo.getBoolean("pk") &&                               // We don't want the primary key column
                       !name.equalsIgnoreCase(featureSet.getGeometryColumnName()))  // We also don't want the geometry column
                    {
                            final String type         = tableInfo.getString("type");
                            final String defaultValue = tableInfo.getString("dflt_value");

                            final EnumSet<ColumnFlag> flags = EnumSet.noneOf(ColumnFlag.class);

                            if(tableInfo.getBoolean("notnull"))
                            {
                                flags.add(ColumnFlag.NotNull);
                            }

                            // TODO there are other ColumnFlags that need to be checked here: AutoIncrement, Unique
                            // TODO neither those flags aren't directly available in table_info

                            columns.add(new Column(name,
                                                   type,
                                                   flags,
                                                   null,
                                                   defaultValue));
                    }
                }

                return columns;
            }
        }
    }

    /**
     * Gets a feature set object based on its table name
     *
     * @param featureSetTableName
     *             Name of a feature set table
     * @return Returns a {@link FeatureSet} or null if there isn't with the
     *             supplied table name
     * @throws SQLException
     *             If there is a database error
     */
    @SuppressWarnings("JDBCExecuteWithNonConstantString")
    public FeatureSet getFeatureSet(final String featureSetTableName) throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageFeatures.GeometryColumnsTableName))
        {
            return null;
        }

        final String geometryColumnQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                                                         "column_name",
                                                         GeoPackageFeatures.GeometryColumnsTableName,
                                                         "table_name");

        final String geometryColumnName = JdbcUtility.selectOne(this.databaseConnection,
                                                                geometryColumnQuery,
                                                                preparedStatement -> preparedStatement.setString(1, featureSetTableName),
                                                                resultSet -> resultSet.getString(1)); // geometry column name

        if(geometryColumnName == null) // If the table exists, but isn't an entry in the geometry column table...
        {
            return null;
        }

        try(final Statement statement = GeoPackageFeatures.this.databaseConnection.createStatement())
        {
            try(final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s)", featureSetTableName)))
            {
                String primaryKeyColumnName = null;

                final Collection<String> attributeColumnNames = new ArrayList<>();

                while(tableInfo.next())
                {
                    final String name = tableInfo.getString("name");

                    if(!name.equalsIgnoreCase(geometryColumnName))
                    {
                        if(tableInfo.getBoolean("pk"))
                        {
                            primaryKeyColumnName = name;
                        }
                        else // non-primary key columns (there can only be one primary key column according to the spec)
                        {
                            attributeColumnNames.add(name);
                        }
                    }
                }

                final String finalPrimaryKeyColumnName = primaryKeyColumnName;

                return this.core.getContent(featureSetTableName,
                                            (tableName,
                                             dataType,
                                             identifier,
                                             description,
                                             lastChange,
                                             minimumX,
                                             minimumY,
                                             maximumX,
                                             maximumY,
                                             spatialReferenceSystem) -> new FeatureSet(tableName,
                                                                                       identifier,
                                                                                       description,
                                                                                       lastChange,
                                                                                       minimumX,
                                                                                       minimumY,
                                                                                       maximumX,
                                                                                       maximumY,
                                                                                       spatialReferenceSystem,
                                                                                       finalPrimaryKeyColumnName,
                                                                                       geometryColumnName,
                                                                                       attributeColumnNames));
            }
        }
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
        return this.core
                   .getContentTableNames(FeatureSet.FeatureContentType,
                                         matchingSpatialReferenceSystem)
                   .stream()
                   .map((ThrowingFunction<String, FeatureSet>)(this::getFeatureSet))
                   .collect(Collectors.toList());
    }

    /**
     * Returns a list of all features that correspond to the given feature set.
     * If a large set of features is anticipated use {@link #visitFeatures} to
     * avoid memory issues
     *
     * @param featureSet
     *             Handle to a feature table
     * @return List of features
     * @throws SQLException
     *             if there is a database error
     * @throws WellKnownBinaryFormatException
     *             Handle to a feature table
     */
    public List<Feature> getFeatures(final FeatureSet featureSet) throws SQLException, WellKnownBinaryFormatException
    {
        if(featureSet == null)
        {
            throw new IllegalArgumentException("Feature set may not be null");
        }

        final String featureQuery = String.format("SELECT %s, %s%s FROM %s",
                                                  featureSet.getPrimaryKeyColumnName(),
                                                  featureSet.getGeometryColumnName(),
                                                  featureSet.getAttributeColumnNames().isEmpty() ? ""
                                                                                                 : ", " + String.join(", ", featureSet.getAttributeColumnNames()),
                                                  featureSet.getTableName());

        try(final Statement statement = this.databaseConnection.createStatement())
        {
            //noinspection JDBCExecuteWithNonConstantString
            try(final ResultSet resultSet = statement.executeQuery(featureQuery))
            {
                final List<Feature> results = new ArrayList<>();

                while(resultSet.next())
                {
                    final Map<String, Object> attributes = new HashMap<>();

                    for(final String columnName : featureSet.getAttributeColumnNames())
                    {
                        attributes.put(columnName, resultSet.getObject(columnName));
                    }

                    results.add(new Feature(resultSet.getInt(featureSet.getPrimaryKeyColumnName()),
                                            this.createGeometry(resultSet.getBytes(featureSet.getGeometryColumnName())),
                                            attributes));
                }

                return results;
            }
        }
    }

    /**
     * Gets a {@link Feature} given a geometry column and feature identifier
     *
     * @param featureSet
     *             Feature set containing the requested feature
     * @param featureIdentifier
     *             Identifier for a feature
     * @return a {@link Feature}
     * @throws SQLException
     *             if there is a database error
     * @throws WellKnownBinaryFormatException
     *             if any of the features contain malformed Well Known Binary data
     */
    public Feature getFeature(final FeatureSet featureSet,
                              final int        featureIdentifier) throws SQLException, WellKnownBinaryFormatException
    {
        if(featureSet == null)
        {
            throw new IllegalArgumentException("Feature set may not be null");
        }

        final String featureQuery = String.format("SELECT %s%s FROM %s WHERE %s = ?",
                                                  featureSet.getGeometryColumnName(),
                                                  featureSet.getAttributeColumnNames().isEmpty() ? ""
                                                                                                 : ", " + String.join(", ", featureSet.getAttributeColumnNames()),
                                                  featureSet.getTableName(),
                                                  featureSet.getPrimaryKeyColumnName());

        final Pair<byte[], Map<String, Object>> feature = JdbcUtility.selectOne(this.databaseConnection,
                                                                                featureQuery,
                                                                                preparedStatement -> preparedStatement.setInt(1, featureIdentifier),
                                                                                resultSet -> { final Map<String, Object> attributes = new HashMap<>();

                                                                                               for(final String columnName : featureSet.getAttributeColumnNames())
                                                                                               {
                                                                                                   attributes.put(columnName, resultSet.getObject(columnName));
                                                                                               }

                                                                                               return Pair.of(resultSet.getBytes(featureSet.getGeometryColumnName()),
                                                                                                              attributes);
                                                                                             });
        if(feature == null)
        {
            return null;
        }

        return new Feature(featureIdentifier,
                           this.createGeometry(feature.getLeft()),
                           feature.getRight());
    }

    /**
     * Applies a consumer to every feature in a feature set
     *
     * @param featureSet
     *             Handle to a feature table
     * @param featureConsumer
     *             Callback that operates on a single feature
     * @throws SQLException
     *             if there is a database error
     * @throws WellKnownBinaryFormatException
     *             Handle to a feature table
     */
    public void visitFeatures(final FeatureSet        featureSet,
                              final Consumer<Feature> featureConsumer) throws SQLException, WellKnownBinaryFormatException
    {
        if(featureSet == null)
        {
            throw new IllegalArgumentException("Geometry column may not be null");
        }

        if(featureConsumer == null)
        {
            throw new IllegalArgumentException("Feature consumer may not be null");
        }

        final String featureQuery = String.format("SELECT %s, %s%s FROM %s",
                                                  featureSet.getPrimaryKeyColumnName(),
                                                  featureSet.getGeometryColumnName(),
                                                  featureSet.getAttributeColumnNames().isEmpty() ? ""
                                                                                                 : ", " + String.join(", ", featureSet.getAttributeColumnNames()),
                                                  featureSet.getTableName());

        try(final Statement statement = this.databaseConnection.createStatement())
        {
            //noinspection JDBCExecuteWithNonConstantString
            try(final ResultSet resultSet = statement.executeQuery(featureQuery))
            {
                while(resultSet.next())
                {
                    final Map<String, Object> attributes = new HashMap<>();

                    for(final String columnName : featureSet.getAttributeColumnNames())
                    {
                        attributes.put(columnName, resultSet.getObject(columnName));
                    }

                    featureConsumer.accept(new Feature(resultSet.getInt(featureSet.getPrimaryKeyColumnName()),
                                                       this.createGeometry(resultSet.getBytes(featureSet.getGeometryColumnName())),
                                                       attributes));
                }
            }
        }
    }

    /**
     * Adds a feature to a feature set
     *
     * @param geometryColumn
     *             Geometry column of a feature set
     * @param geometry
     *             Geometry of a feature
     * @param attributeColumnNames
     *             List of attribute column names, specified in the same order as the supplied values
     * @param attributeValues
     *             List of attribute values, specified in the same order as the supplied column names
     * @return a handle to the newly created {@link Feature} object
     * @throws SQLException
     *             if there is a database error
     */
    public Feature addFeature(final GeometryColumn geometryColumn,
                              final Geometry       geometry,
                              final List<String>   attributeColumnNames,
                              final List<Object>   attributeValues) throws SQLException
    {
        if(geometryColumn == null)
        {
            throw new IllegalArgumentException("Geometry column may not be null");
        }

        if(geometry == null)
        {
            throw new IllegalArgumentException("Geometry may not be null");
        }

        if(attributeColumnNames == null)
        {
            throw new IllegalArgumentException("Attribute column names may not be null");
        }

        if(attributeValues == null)
        {
            throw new IllegalArgumentException("Attribute values may not be null");
        }

        if(attributeColumnNames.size() != attributeValues.size())
        {
            throw new IllegalArgumentException("The number of attribute column names must match the number of attribute values");
        }

        if(!geometryColumn.getGeometryType()
                          .toUpperCase()
                          .equals(geometry.getGeometryTypeName()))
        {
            throw new IllegalArgumentException("Geometry column may only contain geometries of type " + geometryColumn.getGeometryType().toUpperCase());
        }

        verifyValueRequirements(geometryColumn, geometry);

        final List<String> columnNames = new LinkedList<>(attributeColumnNames);

        columnNames.add(0, geometryColumn.getColumnName());

        final String insertFeatureSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                                      geometryColumn.getTableName(),
                                                      String.join(", ", columnNames),
                                                      String.join(", ", Collections.nCopies(columnNames.size(), "?")));

        final int identifier = JdbcUtility.update(this.databaseConnection,
                                                  insertFeatureSql,
                                                  preparedStatement -> { int parameterIndex = 1;

                                                                         final byte[] bytes = createBlob(geometry, geometryColumn.getSpatialReferenceSystemIdentifier());
                                                                         preparedStatement.setBytes(parameterIndex++, bytes);

                                                                         columnNames.remove(0);    // Skip the geometry column

                                                                         for(final Object attributeValue : attributeValues)
                                                                         {
                                                                             preparedStatement.setObject(parameterIndex++, attributeValue);
                                                                         }
                                                                        },
                                                  resultSet -> resultSet.getInt(1));    // New feature identifier

        this.databaseConnection.commit();

        final Map<String, Object> attributes = new HashMap<>(attributeColumnNames.size());

        for(int x = 0; x < attributeColumnNames.size(); ++x)
        {
            attributes.put(attributeColumnNames.get(x),
                           attributeValues     .get(x));
        }

        return new Feature(identifier,
                           geometry,
                           attributes);
    }

    /**
     * Add multiple features to a feature set
     *
     * @param geometryColumn
     *             Geometry column of the target feature set
     * @param attributeColumnNames
     *             A list of columns for which the attribute values are being provided
     * @param features
     *             A collection of geometry/attribute collection pairs. The
     *             attribute collection must have the same number and order for
     *             attributes as specified by the attributeColumns parameter.
     * @throws SQLException
     *             if there is a database error
     */
    public void addFeatures(final GeometryColumn                         geometryColumn,
                            final List<String>                           attributeColumnNames,
                            final Iterable<Pair<Geometry, List<Object>>> features) throws SQLException
    {
        if(geometryColumn == null)
        {
            throw new IllegalArgumentException("Geometry column may not be null");
        }

        if(attributeColumnNames == null)
        {
            throw new IllegalArgumentException("Columns may not be null");
        }

        if(features == null)
        {
            throw new IllegalArgumentException("Values may not be null");
        }

        features.forEach(feature -> { if(feature == null)
                                      {
                                          throw new IllegalArgumentException("Features collection may not contain null features");
                                      }

                                      final Geometry geometry = feature.getLeft();

                                      if(geometry == null)
                                      {
                                          throw new IllegalArgumentException("Features collection may not contain null geometries");
                                      }

                                      final List<Object> attributes = feature.getRight();

                                      if(attributes == null)
                                      {
                                          throw new IllegalArgumentException("Feature collection may not have a null set of attributes");
                                      }

                                      if(attributes.size() != attributeColumnNames.size())
                                      {
                                          throw new IllegalArgumentException("Feature attribute collections must match the size of the attribute column name collection");
                                      }

                                      if(!geometryColumn.getGeometryType()
                                                        .toUpperCase()
                                                        .equals(geometry.getGeometryTypeName()))
                                      {
                                          throw new IllegalArgumentException("Geometry column may only contain geometries of type " + geometryColumn.getGeometryType().toUpperCase());
                                      }



                                      verifyValueRequirements(geometryColumn, geometry);
                                    });

        final List<String> columnNames = new LinkedList<>(attributeColumnNames);

        columnNames.add(0, geometryColumn.getColumnName());

        final int columnCount = columnNames.size();

        final String insertFeatureSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                                      geometryColumn.getTableName(),
                                                      String.join(", ", columnNames),
                                                      String.join(", ", Collections.nCopies(columnNames.size(), "?")));

        JdbcUtility.update(this.databaseConnection,
                           insertFeatureSql,
                           features,
                           (preparedStatement, feature) -> { final Geometry     geometry   = feature.getLeft();
                                                             final List<Object> attributes = feature.getRight();

                                                             preparedStatement.setBytes(1, createBlob(geometry, geometryColumn.getSpatialReferenceSystemIdentifier()));

                                                             for(int parameterIndex = 2; parameterIndex <= columnCount; ++parameterIndex)
                                                             {
                                                                 preparedStatement.setObject(parameterIndex, attributes.get(parameterIndex-2));
                                                             }
                                                           });

        this.databaseConnection.commit();
    }

    /**
     * Associate a geometry factory with a specific geometry type code.
     *
     * @param geometryTypeCode
     *             Code representation of the geometry type. Must be in the
     *             range 0 and 2^32 - 1 (range of a 32 bit unsigned integer)
     * @param geometryFactory
     *             Callback that creates a geometry that corresponds to the
     *             geometry type code
     */
    public void registerGeometryFactory(final long            geometryTypeCode,
                                        final GeometryFactory geometryFactory)
    {
        if(geometryTypeCode < 0 || geometryTypeCode > maxUnsignedIntValue)
        {
            throw new IllegalArgumentException("Type code must be between 0 and 2^32 - 1 (range of a 32 bit unsigned integer)");
        }

        if(geometryFactory == null)
        {
            throw new IllegalArgumentException("Geometry factory may not be null");
        }

//        if(this.geometryFactories.containsKey(geometryTypeCode))    // TODO do we really want to prohibit this?
//        {
//            throw new IllegalArgumentException("A geometry factory already exists for this geometry type code");
//        }

        this.geometryFactories.put(geometryTypeCode, geometryFactory);
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
     *             if there is a database error
     */
    protected void createGeometryColumnTableNoCommit() throws SQLException
    {
        // Create the geometry column table or view
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageFeatures.GeometryColumnsTableName))
        {
            JdbcUtility.update(this.databaseConnection, getGeometryColumnsCreationSql());
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
     * @param geometryColumn
     *            Definition of a geometry column to be added
     * @param spatialReferenceSystemIdentifier
     *            Spatial Reference System (SRS)
     * @throws SQLException
     *             if there is a database error
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

    protected static String getGeometryColumnsCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_geometry_columns_cols
        // http://www.geopackage.org/spec/#gpkg_geometry_columns_sql
        return "CREATE TABLE " + GeoPackageFeatures.GeometryColumnsTableName  + '\n' +
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

    private Geometry createGeometry(final byte[] geoPackageBinaryBlob) throws WellKnownBinaryFormatException
    {
        final BinaryHeader binaryHeader = new BinaryHeader(geoPackageBinaryBlob);   // This will throw if the array length is too short to contain a header (or if it's not long enough to contain the envelope type specified)

        if(binaryHeader.getBinaryType() == BinaryType.Standard)
        {
            final int headerByteLength = binaryHeader.getByteSize();

            return this.createGeometry(ByteBuffer.wrap(geoPackageBinaryBlob,
                                                       headerByteLength,
                                                       geoPackageBinaryBlob.length - headerByteLength)
                                                 .asReadOnlyBuffer());  // Minor insurance that geometry extension implementations can't change the buffer
        }

        // else, this is an extended binary type. The next 4 bytes are the "extension_code"
        // http://www.geopackage.org/spec/#_requirement-70
        // "... This extension_code SHOULD identify the implementer of the
        // extension and/or the particular geometry type extension, and SHOULD
        // be unique."

        // TODO: read the 4 byte extension code, and look it up in a mapping of known extensions codes/parsers (to be registered by extension implementers)

        throw new WellKnownBinaryFormatException("Extensions of GeoPackageBinary geometry encoding are not currently supported");
    }

    private Geometry createGeometry(final ByteBuffer wkbByteBuffer) throws WellKnownBinaryFormatException
    {
        try
        {
            if(wkbByteBuffer == null)
            {
                throw new IllegalArgumentException("Well known binary byte buffer may not be null");
            }

            if(wkbByteBuffer.limit() < 5)
            {
                throw new WellKnownBinaryFormatException("Well known binary buffer must contain at least 5 bytes - the first being the byte order indicator, followed by a 4 byte unsigned integer describing the geometry type.");
            }

            // Save the buffer position (.mark()) before we read the well known
            // binary header (this is *not* the GeoPackage binary header). The
            // well known binary header will be re-read by the parsers stored
            // in the geometry factory. This is so the parsers can stand alone,
            // not relying the ByteBuffer to be positioned after the well known
            // binary header.
            wkbByteBuffer.mark();

            final ByteOrder byteOrder = wkbByteBuffer.get() == 0 ? ByteOrder.BIG_ENDIAN
                                                                 : ByteOrder.LITTLE_ENDIAN;

            wkbByteBuffer.order(byteOrder);

            // Read 4 bytes as an /unsigned/ int
            final long geometryType = Integer.toUnsignedLong(wkbByteBuffer.getInt());

            if(!this.geometryFactories.containsKey(geometryType))
            {
                throw new RuntimeException(String.format("Unrecognized geometry type code %d. Recognized geometry types are: %s. Additional types will require a GeoPackage extention to interact with.",
                                                         geometryType,
                                                         this.geometryFactories
                                                             .keySet()
                                                             .stream()
                                                             .map(Object::toString)
                                                             .collect(Collectors.joining(", "))));
            }

            wkbByteBuffer.reset(); // This will reset the position to before the well known binary header.

            return this.geometryFactories
                       .get(geometryType)
                       .create(wkbByteBuffer);
        }
        catch(final BufferUnderflowException bufferUnderflowException)
        {
            throw new WellKnownBinaryFormatException(bufferUnderflowException);
        }

    }

    private static byte[] createBlob(final Geometry geometry, final int spatialReferenceSystemIdentifier)
    {
        final ByteOutputStream byteOutputStream = new ByteOutputStream();

        // TODO HEADER USES OPTIONS:
        // FORCE ENVELOPE
        // FORCE ENDIANNESS

        BinaryHeader.writeBytes(byteOutputStream,
                                geometry,
                                spatialReferenceSystemIdentifier);

        byteOutputStream.setByteOrder(ByteOrder.BIG_ENDIAN); // TODO make this an option (?)

        geometry.writeWellKnownBinary(byteOutputStream);

        return byteOutputStream.array();
    }

    private static void verifyValueRequirements(final GeometryColumn geometryColumn, final Geometry geometry)
    {
        final ValueRequirement zRequirement = geometryColumn.getZRequirement();
        final ValueRequirement mRequirement = geometryColumn.getMRequirement();

        if(geometry == null)
        {
            throw new IllegalArgumentException("Geometry may not be null");
        }

        final boolean hasZ = geometry.hasZ();
        final boolean hasM = geometry.hasM();

        if((zRequirement == ValueRequirement.Prohibited &&  hasZ) ||
           (zRequirement == ValueRequirement.Mandatory  && !hasZ) ||
           (mRequirement == ValueRequirement.Prohibited &&  hasM) ||
           (mRequirement == ValueRequirement.Mandatory  && !hasM))
        {
            throw new IllegalArgumentException(String.format("Geometry is incompatible with the requirements Z %s, M %s",
                                                             zRequirement.toString().toLowerCase(),
                                                             mRequirement.toString().toLowerCase()));
        }
    }

    private void addFeatureTableNoCommit(final String                       featureTableName,
                                         final String                       primaryKeyColumnName,
                                         final GeometryColumnDefinition     geometryColumn,
                                         final Collection<ColumnDefinition> columnDefinitions) throws SQLException
    {
        // http://www.geopackage.org/spec/#feature_user_tables
        // http://www.geopackage.org/spec/#example_feature_table_sql

        final List<AbstractColumnDefinition> columns = new LinkedList<>(columnDefinitions);

        columns.add(0, new PrimaryKeyColumnDefinition(primaryKeyColumnName));

        columns.add(1, geometryColumn);

        // TODO Move the table-building functionality to the table definition class?
        final StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE ");
        createTableSql.append(featureTableName);
        createTableSql.append("\n(");

        for(int columnIndex = 0; columnIndex < columns.size(); ++columnIndex)
        {
            final AbstractColumnDefinition column = columns.get(columnIndex);

            final String comment      = column.getComment();
            final String defaultValue = column.getDefaultValue().equals(ColumnDefault.None) ? "" : " DEFAULT " + column.getDefaultValue().sqlLiteral();

            createTableSql.append(System.lineSeparator());

            createTableSql.append(column.getName());  // Verified by AbstractColumnDefinition to not contain SQL injection
            createTableSql.append(' ');
            createTableSql.append(column.getType());  // Verified by AbstractColumnDefinition to not contain SQL injection
            createTableSql.append(column.hasFlag(ColumnFlag.PrimaryKey)    ? " PRIMARY KEY"   : "");
            createTableSql.append(column.hasFlag(ColumnFlag.AutoIncrement) ? " AUTOINCREMENT" : "");
            createTableSql.append(column.hasFlag(ColumnFlag.NotNull)       ? " NOT NULL"      : "");
            createTableSql.append(column.hasFlag(ColumnFlag.Unique)        ? " UNIQUE"        : "");
            createTableSql.append(defaultValue);
            createTableSql.append(columnIndex == columns.size()-1          ? ""               : ",");
            createTableSql.append(comment     == null                      ? ""               : " -- " + comment);  // Verified by AbstractColumnDefinition to not contain newlines, which I think is the only way injection could work here
        }

        createTableSql.append("\n);");

        JdbcUtility.update(this.databaseConnection, createTableSql.toString());
    }

    /**
     * Standard table name of the GeoPackage Geometry Columns table
     */
    public static final String GeometryColumnsTableName = "gpkg_geometry_columns";

    private final Connection     databaseConnection;
    private final GeoPackageCore core;

    private final Map<Long, GeometryFactory> geometryFactories = new HashMap<>();

    private static final long maxUnsignedIntValue = 4294967295L; // 2^31 - 1
}
