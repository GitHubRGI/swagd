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

import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.GeoPackageCore;
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
     */
    @Requirement(reference = "Requirement 18",
                 text      = "The gpkg_contents table SHALL contain a row with a lowercase data_type column value of \"features\" for each vector features user data table or view.")
    public void requirement18()
    {
        // This is hard-to-impossible to actually test. For features, a tile
        // pyramid user data table has a singular table definition. This is
        // important because to test this requirement, each table in the
        // database is checked against that table definition to see if it looks
        // like a a tile pyramid user data table. That list is then checked
        // against what's listed in the contents table to see if there's an
        // entry for each matching table. In the case of vector features user
        // data tables, there is no singular definition, just a requirement for
        // a column with column type INTEGER and PRIMARY KEY AUTOINCREMENT, and
        // a column with a geometry type. It would be restrictive to assume
        // that every table that has both such columns is a vector feature data
        // table and therefore must be listed as an entry contents table as
        // such.
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
     * @throws AssertionError
     *             throws if the GeoPackage fails to meet the requirement
     * @throws SQLException
     *             if there is a database error
     */
    @Requirement(reference = "Requirement 19",
                 text      = "A GeoPackage SHALL store feature table geometries with or without optional elevation (Z) and/or measure (M) values in SQL BLOBs using the Standard GeoPackageBinary format specified in table GeoPackage SQL Geometry Binary Format and clause BLOB Format.")
    public void requirement() throws AssertionError, SQLException
    {
        assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full.name(),
                   this.verificationLevel == VerificationLevel.Full,
                   Severity.Skipped);

        final Map<String, List<Integer>> tablesWithBadFeatures = new HashMap<>();

        for(final String tableName : this.contentsFeatureTableNames)
        {
            final String primaryKeyColumnName = this.getPrimaryKeyColumnName(tableName);
            final String geometryColumnName   = this.getGeometryColumnName  (tableName);

            if(primaryKeyColumnName != null && geometryColumnName != null)
            {
                final String featureQuery = String.format("SELECT %s, %s FROM %s",
                                                          primaryKeyColumnName,
                                                          geometryColumnName,
                                                          tableName);

                final List<Integer> badFeatures = JdbcUtility.filterSelect(this.getSqliteConnection(),
                                                                           featureQuery,
                                                                           null,
                                                                           resultSet -> !this.canParseGeometry(resultSet.getBytes(geometryColumnName)),
                                                                           resultSet -> resultSet.getInt(primaryKeyColumnName));

                if(!badFeatures.isEmpty())
                {
                    tablesWithBadFeatures.put(tableName, badFeatures);
                }
            }
        }

        assertTrue(String.format("The following feature table(s) have invalid geometries:\n%s",
                                 tablesWithBadFeatures.entrySet()
                                                      .stream()
                                                      .map(entrySet -> String.format("%s: %s",
                                                                                     entrySet.getKey(),
                                                                                     entrySet.getValue()
                                                                                             .stream()
                                                                                             .map(Object::toString)
                                                                                             .collect(Collectors.joining(", "))))
                                                      .collect(Collectors.joining("\n"))),
                   tablesWithBadFeatures.isEmpty(),
                   Severity.Warning);
    }

    /**
     * Requirement XX
     *
     * <blockquote>
     *
     * </blockquote>
     *
     */
    @Requirement(reference = "Requirement XX",
                 text      = "")
    public void requirementXX()
    {

    }

    private String getGeometryColumnName(final String tableName) throws SQLException
    {
        return JdbcUtility.selectOne(this.getSqliteConnection(),
                                     String.format("SELECT %s FROM %s WHERE %s = ?",
                                                   "column_name",
                                                   GeoPackageFeatures.GeometryColumnsTableName,
                                                   "table_name"),
                                     preparedStatement -> preparedStatement.setString(1, tableName),
                                     resultSet -> resultSet.getString("column_name"));
    }

    private String getPrimaryKeyColumnName(final String tableName) throws SQLException
    {
        try(final Statement statement = this.getSqliteConnection().createStatement())
        {
            try(final ResultSet tableInfo = statement.executeQuery(String.format("PRAGMA table_info(%s)", tableName)))
            {
                while(tableInfo.next())
                {
                    final String name = tableInfo.getString("name");

                    if(tableInfo.getBoolean("pk"))
                    {
                        return name;
                    }
                }
            }
        }

        return null;
    }

    private boolean canParseGeometry(final byte[] geoPackageBinaryBlob)
    {
        try
        {
            this.parseGeometry(geoPackageBinaryBlob);
            return true;
        }
        catch(final Throwable ignored)
        {
            return false;
        }
    }

    private void parseGeometry(final byte[] geoPackageBinaryBlob) throws WellKnownBinaryFormatException
    {
        final BinaryHeader binaryHeader = new BinaryHeader(geoPackageBinaryBlob);   // This will throw if the array length is too short to contain a header (or if it's not long enough to contain the envelope type specified)

        if(binaryHeader.getBinaryType() == BinaryType.Standard)
        {
            final int headerByteLength = binaryHeader.getByteSize();

            this.wellKnownBinaryFactory
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
