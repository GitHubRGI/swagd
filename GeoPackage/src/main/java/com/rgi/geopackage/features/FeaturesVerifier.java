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

import com.rgi.common.Pair;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rgi.geopackage.verification.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class FeaturesVerifier extends Verifier
{
    /**
     * Constructor
     *
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @param sqliteConnection
     *             A connection handle to the database
     * @throws SQLException
     *             If there is a database error
     */
    public FeaturesVerifier(final Connection sqliteConnection, final VerificationLevel verificationLevel) throws SQLException
    {
        super(sqliteConnection, verificationLevel);

        this.contentsFeatureTableNames = JdbcUtility.select(this.getSqliteConnection(),
                                                            String.format("SELECT table_name FROM %s WHERE data_type = 'features';", GeoPackageCore.ContentsTableName),
                                                            null,
                                                            resultSet -> resultSet.getString("table_name"));
    }

    /**
     * Requirement 18
     *
     * <blockquote>
     * The {@code gpkg_contents} table SHALL contain a row with a lowercase
     * {@code data_type} column value of "features" for each vector features
     * user data table or view.
     * </blockquote>
     *
     * Verify that the gpkg_contents table_name value table exists, and is
     * apparently a feature table for every row with a data_type column value
     * of "features".
     * (<a href="http://www.geopackage.org/spec/_data_20">ref</a>)
     *
     * This test is only an attempt to satisfy the requirement. It's based on
     * the logic found in the Annex A: Conformance / Abstract Test Suite, but
     * that logic is deficient in the following ways:
     *
     * <ol>
     *     <li>This test only checks to see if the features tables listed in
     *     the contents table are valid. It does not check to see if each table
     *     that is could be a feature table is listed.
     *         <li>It's not possible to verify that a table that meets the
     *         vector features user data table criteria necessarily is one. The
     *         criteria is too broad.
     *         </li>
     *     </li>
     *     <li>This test does not verify that a vector table has exactly one
     *     geometry column.
     *     </li>
     * </ol>
     *
     * @throws AssertionError
     *             throws if the GeoPackage fails to meet the requirement
     */
    @Requirement(reference = "Requirement 18",
                 text      = "The gpkg_contents table SHALL contain a row with a lowercase data_type column value of \"features\" for each vector features user data table or view.")
    public void requirement18() throws AssertionError
    {
        final List<String> missingFeatureTables = this.contentsFeatureTableNames
                                                      .stream()
                                                      .filter(tableName -> !this.hasPrimaryKey(tableName))
                                                      .collect(Collectors.toList());

        assertTrue(String.format("The following feature table entries in the gpkg_contents table are missing or are missing a primary key column: %s",
                                 String.join(", ", missingFeatureTables)),
                   missingFeatureTables.isEmpty(),
                   Severity.Warning);
    }

    /**
     * Requirement 19
     *
     * <blockquote>
     * A GeoPackage SHALL store feature table geometries with or without
     * optional elevation (Z) and/or measure (M) values in SQL BLOBs using the
     * Standard GeoPackageBinary format specified in table <a
     * href="http://www.geopackage.org/spec/#gpb_spec">GeoPackage SQL Geometry
     * Binary Format</a> and clause <a
     * href="http://www.geopackage.org/spec/#gpb_data_blob_format">BLOB Format
     * </a>.
     * </blockquote>
     *
     * Verify that geometries stored in feature table geometry columns are
     * encoded in the StandardGeoPackageBinary format.
     * (<a href="http://www.geopackage.org/spec/#_data_21">ref</a>)
     *
     * @throws AssertionError
     *             throws if the GeoPackage fails to meet the requirement
     * @throws SQLException
     *             if there is a database error
     */
    @Requirement(reference = "Requirement 19",
                 text      = "A GeoPackage SHALL store feature table geometries with or without optional elevation (Z) and/or measure (M) values in SQL BLOBs using the Standard GeoPackageBinary format specified in table GeoPackage SQL Geometry Binary Format and clause BLOB Format.")
    public void requirement19() throws AssertionError, SQLException
    {
        assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full.name(),
                   this.verificationLevel == VerificationLevel.Full,
                   Severity.Skipped);

        final List<Pair<String, String>> featureTables = JdbcUtility.select(this.getSqliteConnection(),
                                                                            "SELECT table_name, column_name  FROM gpkg_geometry_columns WHERE table_name IN (SELECT table_name FROM gpkg_contents WHERE data_type = 'features');",
                                                                            null,
                                                                            resultSet -> Pair.of(resultSet.getString("table_name"),
                                                                                                 resultSet.getString("column_name")));

        final Map<String, List<Integer>> tablesWithBadGeometries = new HashMap<>();

        for(final Pair<String, String> tableColumnPair : featureTables)
        {
            final String tableName            = tableColumnPair.getLeft();
            final String geometryColumnName   = tableColumnPair.getRight();
            final String primaryKeyColumnName = this.getPrimaryKeyColumnName(tableName);

            if(primaryKeyColumnName != null)
            {
                final String featureQuery = String.format("SELECT %s, %s FROM %s",
                                                          primaryKeyColumnName,
                                                          geometryColumnName,
                                                          tableName);

                final List<Integer> badGeometries = JdbcUtility.filterSelect(this.getSqliteConnection(),
                                                                             featureQuery,
                                                                             null,
                                                                             resultSet -> !canParseStandardGeoPackageBinaryFormat(resultSet.getBytes(geometryColumnName)),    // The restrictions here match: http://www.geopackage.org/spec/#_data_21
                                                                             resultSet -> resultSet.getInt(primaryKeyColumnName));

                if(!badGeometries.isEmpty())
                {
                    tablesWithBadGeometries.put(tableName, badGeometries);
                }
            }
        }

        assertTrue(String.format("The following feature table(s) have geometries not correctly encoded in the StandardGeoPackageBinary format:\n%s",
                                 tablesWithBadGeometries.entrySet()
                                                        .stream()
                                                        .map(entrySet -> String.format("%s: %s",
                                                                                       entrySet.getKey(),
                                                                                       entrySet.getValue()
                                                                                               .stream()
                                                                                               .map(Object::toString)
                                                                                               .collect(Collectors.joining(", "))))
                                                        .collect(Collectors.joining("\n"))),
                   tablesWithBadGeometries.isEmpty(),
                   Severity.Warning);
    }

    /**
     * Requirement 20
     *
     * <blockquote>
     * A GeoPackage SHALL store feature table geometries with the basic simple
     * feature geometry types (Geometry, Point, LineString, Polygon,
     * MultiPoint, MultiLineString, MultiPolygon, GeomCollection) in <a
     * href="http://www.geopackage.org/spec/#geometry_types">Geometry Types
     * (Normative)</a> <a
     * href="http://www.geopackage.org/spec/#geometry_types_core">Geometry Type
     * Codes (Core)</a> in the GeoPackageBinary geometry encoding format.
     * </blockquote>
     *
     * Verify that existing basic simple feature geometries are stored in valid
     * GeoPackageBinary format encodings.
     *
     * Verify that all basic simple feature geometry types and options are
     * stored in valid GeoPackageBinary format encodings.
     * (<a href="http://www.geopackage.org/spec/#_data_22">ref</a>)
     *
     * @throws AssertionError
     *             throws if the GeoPackage fails to meet the requirement
     * @throws SQLException
     *             if there is a database error
     */
    @Requirement(reference = "Requirement 20",
                 text      = "A GeoPackage SHALL store feature table geometries with the basic simple feature geometry types (Geometry, Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeomCollection) in Geometry Types (Normative) Geometry Type Codes (Core) in the GeoPackageBinary geometry encoding format.")
    public void requirement20() throws AssertionError, SQLException
    {
        assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full.name(),
                   this.verificationLevel == VerificationLevel.Full,
                   Severity.Skipped);

        final List<Pair<String, String>> featureTables = JdbcUtility.select(this.getSqliteConnection(),
                                                                            "SELECT table_name, column_name  FROM gpkg_geometry_columns WHERE table_name IN (SELECT table_name FROM gpkg_contents WHERE data_type = 'features');",
                                                                            null,
                                                                            resultSet -> Pair.of(resultSet.getString("table_name"),
                                                                                                 resultSet.getString("column_name")));

        final Map<String, List<Integer>> tablesWithBadGeometries = new HashMap<>();

        for(final Pair<String, String> tableColumnPair : featureTables)
        {
            final String tableName            = tableColumnPair.getLeft();
            final String geometryColumnName   = tableColumnPair.getRight();
            final String primaryKeyColumnName = this.getPrimaryKeyColumnName(tableName);

            if(primaryKeyColumnName != null)
            {
                final String featureQuery = String.format("SELECT %s, %s FROM %s",
                                                          primaryKeyColumnName,
                                                          geometryColumnName,
                                                          tableName);

                final List<Integer> badGeometries = JdbcUtility.filterSelect(this.getSqliteConnection(),
                                                                             featureQuery,
                                                                             null,
                                                                             resultSet -> !this.isValidGeometry(resultSet.getBytes(geometryColumnName)),    // The restrictions here match: http://www.geopackage.org/spec/#_data_22
                                                                             resultSet -> resultSet.getInt(primaryKeyColumnName));

                if(!badGeometries.isEmpty())
                {
                    tablesWithBadGeometries.put(tableName, badGeometries);
                }
            }
        }

        assertTrue(String.format("The following feature table(s) have geometries not correctly encoded in the StandardGeoPackageBinary format:\n%s",
                                 tablesWithBadGeometries.entrySet()
                                                        .stream()
                                                        .map(entrySet -> String.format("%s: %s",
                                                                                       entrySet.getKey(),
                                                                                       entrySet.getValue()
                                                                                               .stream()
                                                                                               .map(Object::toString)
                                                                                               .collect(Collectors.joining(", "))))
                                                        .collect(Collectors.joining("\n"))),
                   tablesWithBadGeometries.isEmpty(),
                   Severity.Warning);
    }

    // template for remaining requirement tests
//    /**
//     * Requirement XX
//     *
//     * <blockquote>
//     *
//     * </blockquote>
//     *
//     *
//     * (<a href="http://www.geopackage.org/spec/#XX">ref</a>)
//     *
//     * @throws AssertionError
//     *             throws if the GeoPackage fails to meet the requirement
//     */
//    @Requirement(reference = "Requirement XX",
//                 text      = "")
//    public void requirementXX() throws AssertionError
//    {
//
//    }

    /**
     * The restrictions here match: http://www.geopackage.org/spec/#_data_21
     */
    private static boolean canParseStandardGeoPackageBinaryFormat(final byte[] geoPackageBinaryBlob)
    {
        try
        {
            final BinaryHeader binaryHeader = new BinaryHeader(geoPackageBinaryBlob);

            return binaryHeader.getVersion()    == 0                   &&
                   binaryHeader.getBinaryType() == BinaryType.Standard &&
                   !(binaryHeader.getContents()                  == Contents.Empty                       &&
                     binaryHeader.getEnvelopeContentsIndicator() != EnvelopeContentsIndicator.NoEnvelope &&
                     !Arrays.stream(binaryHeader.getEnvelopeArray()).allMatch(Double::isNaN));
        }
        catch(final Throwable ignored)
        {
            return false;
        }
    }

    private boolean hasPrimaryKey(final String tableName)
    {
        try
        {
            return this.getPrimaryKeyColumnName(tableName) != null;
        }
        catch(final SQLException ignored)
        {

        }

        return false;
    }

    private String getPrimaryKeyColumnName(final String tableName) throws SQLException
    {
        try(final Statement statement = this.getSqliteConnection().createStatement())
        {
            //noinspection JDBCExecuteWithNonConstantString
            try(final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s)", tableName)))
            {
                while(tableInfo.next())
                {
                    if(tableInfo.getBoolean("pk")            &&
                       tableInfo.getString("type")
                                .equalsIgnoreCase("INTEGER") &&
                       tableInfo.getBoolean("notnull"))
                    {
                        return tableInfo.getString("name");
                    }
                }
            }
        }

        return null;
    }

    private boolean isValidGeometry(final byte[] geoPackageBinaryBlob)
    {
        try
        {
            final BinaryHeader binaryHeader = new BinaryHeader(geoPackageBinaryBlob);   // This will throw if the array length is too short to contain a header (or if it's not long enough to contain the envelope type specified)

            final Geometry geometry = this.createGeometry(binaryHeader, geoPackageBinaryBlob);   // correctly encoded per ISO 13249-3 clause 5.1.46

            return binaryHeader.getEnvelopeContentsIndicator() != EnvelopeContentsIndicator.NoEnvelope ||
                   binaryHeader.getEnvelope().equals(geometry.createEnvelope());
        }
        catch(final Throwable ignored)
        {
            return false;
        }
    }

    private Geometry createGeometry(final BinaryHeader binaryHeader,
                                    final byte[] geoPackageBinaryBlob) throws WellKnownBinaryFormatException
    {
        if(binaryHeader.getBinaryType() == BinaryType.Standard)
        {
            final int headerByteLength = binaryHeader.getByteSize();

            return this.wellKnownBinaryFactory
                       .createGeometry(ByteBuffer.wrap(geoPackageBinaryBlob,
                                                       headerByteLength,
                                                       geoPackageBinaryBlob.length - headerByteLength)
                                                 .asReadOnlyBuffer());  // Minor insurance that geometry extension implementations can't change the buffer
        }

        // else, this is an extended binary type. The next 4 bytes are the "extension_code"
        // http://www.geopackage.org/spec/#_requirement-70
        // "... This extension_code SHOULD identify the implementer of the
        // extension and/or the particular geometry type extension, and SHOULD
        // be unique."

        // TODO: read the 4 byte extension code, and look it up in a mapping of known extensions codes/parsers (to be registered by extension implementors)

        throw new WellKnownBinaryFormatException("Extensions of GeoPackageBinary geometry encoding are not currently supported");
    }

    private final Collection<String>     contentsFeatureTableNames;
    private final WellKnownBinaryFactory wellKnownBinaryFactory = new WellKnownBinaryFactory();
}
