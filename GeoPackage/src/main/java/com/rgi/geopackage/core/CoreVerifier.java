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

import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.geopackage.verification.Verifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.rgi.geopackage.verification.Assert.assertArrayEquals;
import static com.rgi.geopackage.verification.Assert.assertTrue;
import static com.rgi.geopackage.verification.Assert.fail;

/**
 * Verifier for the GeoPackage Core requirements
 *
 * TODO: many of these tests can be rewritten with the JdbcUtilities.select
 * (and similar) functions for conciseness and clarity
 *
 * @author Luke Lambert
 * @author Jenifer Cochran
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
public class CoreVerifier extends Verifier
{
    private boolean hasContentsTable;
    private boolean hasSpatialReferenceSystemTable;

    /**
     * Constructor
     *
     * @param file
     *             File handle to the SQLite database
     * @param sqliteConnection
     *             JDBC connection to the SQLite database
     * @param verificationLevel
     *             Controls the level of verification testing performed
     */
    public CoreVerifier(final File file, final Connection sqliteConnection, final VerificationLevel verificationLevel)
    {
        super(sqliteConnection, verificationLevel);

        if(file == null)
        {
            throw new IllegalArgumentException("File cannot be null");
        }

        this.file = file;

        try
        {
            this.hasContentsTable = this.doesTableExist(GeoPackageCore.ContentsTableName);
        }
        catch(final SQLException ignored)
        {
            this.hasContentsTable = false;
        }

        try
        {
            this.hasSpatialReferenceSystemTable = this.doesTableExist(GeoPackageCore.SpatialRefSysTableName);
        }
        catch(final SQLException ignored)
        {
            this.hasSpatialReferenceSystemTable = false;
        }
    }

    /**
     * Requirement 1
     *
     * <blockquote>
     * A GeoPackage SHALL be a <a href="http://www.sqlite.org/">SQLite</a>
     * database file using <a href="http://sqlite.org/fileformat2.html">version
     * 3 of the SQLite file format</a>. The first 16 bytes of a GeoPackage
     * SHALL contain "SQLite format 3" in ASCII
     * </blockquote>
     *
     * @throws IOException throws if an I/O error occurs when reading the file header
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 1",
                 text      = "A GeoPackage SHALL be a SQLite database file using version 3 of the SQLite file format. The first 16 bytes of a GeoPackage SHALL contain \"SQLite format 3\" in ASCII.")
    public void requirement1() throws IOException, AssertionError
    {
        final byte[] header = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);    // The GeoPackage spec says it's StandardCharsets.US_ASCII, but the SQLite spec (https://www.sqlite.org/fileformat.html - 1.2.1 Magic Header String) says it's UTF8, i.e, StandardCharsets.UTF_8

        final byte[] data = new byte[header.length];

        try(final FileInputStream fileInputStream = new FileInputStream(this.file))
        {
            assertTrue("The header information of the file does not contain enough bytes to include necessary information",
                       fileInputStream.read(data, 0, header.length) == header.length,
                       Severity.Error);

            assertArrayEquals("The database file is not using a version 3 of the SQLite format.  Or does not include the SQLite version in the file header.",
                              header,
                              data,
                              Severity.Warning);
        }
    }

    /**
     * Requirement 2
     *
     * <blockquote>
     * A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII)
     * in the application id field of the SQLite database header
     * to indicate a GeoPackage version 1.0 file.
     * </blockquote>
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 2",
                 text      = "A GeoPackage SHALL contain 0x47503130 ('GP10' in ASCII) in the application id field of the SQLite database header to indicate a GeoPackage version 1.0 file.")
    public void requirement2() throws AssertionError
    {
        final int  sizeOfInt = 4;
        final byte[] data = new byte[sizeOfInt];    // 4 bytes in an int

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "r"))
        {
            final long applicationIdByteOffset = 68;
            randomAccessFile.seek(applicationIdByteOffset);
            assertTrue("The file does not have enough bytes to contain a GeoPackage.",
                       randomAccessFile.read(data, 0, sizeOfInt) == sizeOfInt,
                       Severity.Warning);
        }
        catch(final Exception ex)
        {
            throw new AssertionError(ex, Severity.Error);
        }

        // application id
        // http://www.sqlite.org/fileformat2.html
        // http://www.geopackage.org/spec/#_sqlite_container
        // A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
        // application id field of the SQLite database header to indicate a
        // GeoPackage version 1.0 file.
        // The bytes 'G', 'P', '1', '0' are equivalent to 0x47503130
        final int expectedAppId = 0x47503130;

        final int applicationId = ByteBuffer.wrap(data).asIntBuffer().get();

        assertTrue(String.format("Bad Application ID: 0x%08x Expected: 0x%08x", applicationId, expectedAppId),
                   applicationId == expectedAppId,
                   Severity.Warning);
    }

    /**
     * Requirement 3
     *
     * <blockquote>
     * A GeoPackage SHALL have the file extension name ".gpkg".
     * </blockquote>
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 3",
                 text      = "A GeoPackage SHALL have the file extension name '.gpkg'")
    public void requirement3() throws AssertionError
    {
        assertTrue("Expected a file with the extension: '.gpkg'",
                   this.file.getName().endsWith(".gpkg"),
                   Severity.Warning);
    }

    /**
     * Requirement 4
     *
     * <blockquote>
     * A GeoPackage SHALL only contain data elements, SQL constructs and GeoPackage extensions with the "gpkg" author name specified in this encoding standard.
     * </blockquote>
     */
    @Requirement(reference = "Requirement 4",
                 text      = "A GeoPackage SHALL only contain data elements, SQL constructs and GeoPackage extensions with the \"gpkg\" author name specified in this encoding standard.")
    public static void requirement4()
    {
        // This requirement is tested through other test cases.
        // The tables we test are:
        // gpkg_contents            per test Req13 in CoreVerifier
        // gpkg_spatial_ref_sys     per test Req10 in CoreVerifier
        // gpkg_tile_matrix         per test Req41 in TileVerifier
        // gpkg_tile_matrix_set     per test Req37 in TileVerifier
        // Pyramid User Data Tables per test Req33 in TileVerifier
    }

    /**
     * Requirement 5
     *
     * <blockquote>
     * The columns of tables in a GeoPackage SHALL only be declared using one of the data types specified in table <a href="http://www.geopackage.org/spec/#table_column_data_types">GeoPackage Data Types</a>.
     * </blockquote>
     * @throws SQLException throws if an SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 5",
                 text      = "The columns of tables in a GeoPackage SHALL only be declared using one of the data types specified in table GeoPackage Data Types.")
    public void requirement5() throws SQLException, AssertionError
    {
        if(this.hasContentsTable)
        {
            final String query = String.format("SELECT table_name FROM %s;", GeoPackageCore.ContentsTableName);

            try(final Statement stmt      = this.getSqliteConnection().createStatement();
                final ResultSet resultSet = stmt.executeQuery(query))
            {
                while(resultSet.next())
                {
                    final String tableName = resultSet.getString("table_name");

                    if(DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), tableName))
                    {

                        try(final PreparedStatement stmt2           = this.getSqliteConnection().prepareStatement(String.format("PRAGMA table_info('%s');", tableName));
                            final ResultSet         pragmaTableinfo = stmt2.executeQuery())
                        {
                            while(pragmaTableinfo.next())
                            {
                                final String dataType = pragmaTableinfo.getString("type");
                                final boolean correctDataType = Verifier.checkDataType(dataType);

                                assertTrue(String.format("Incorrect data type encountered: %s  From table: %s",
                                                         dataType,
                                                         tableName),
                                           correctDataType,
                                           Severity.Error);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Requirement 6
     *
     * <blockquote>
     * The SQLite PRAGMA integrity_check SQL command SHALL return "ok" for a GeoPackage file.
     * </blockquote>
     *
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 6",
                 text      = "The SQLite PRAGMA integrity_check SQL command SHALL return \"ok\" for a GeoPackage file.")
    public void requirement6() throws SQLException, AssertionError
    {
        assertTrue("Test skipped when verification level is not set to " + VerificationLevel.Full,
                   this.verificationLevel == VerificationLevel.Full,
                   Severity.Skipped);

        final String query = "PRAGMA integrity_check;";

        try(final Statement stmt           = this.getSqliteConnection().createStatement();
            final ResultSet integrityCheck = stmt.executeQuery(query))
        {
            integrityCheck.next();
            assertTrue("PRAGMA integrity_check failed.",
                       integrityCheck.getString("integrity_check").toLowerCase().equals("ok"),
                       Severity.Error);
        }
    }

    /**
     * Requirement 7
     *
     * <blockquote>
     * The SQLite PRAGMA foreign_key_check SQL with no parameter value SHALL return an empty result set indicating no invalid foreign key values for a GeoPackage file.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 7",
                 text      = "The SQLite PRAGMA foreign_key_check SQL with no parameter value SHALL return an empty result set indicating no invalid foreign key values for a GeoPackage file.")
    public void requirement7() throws SQLException, AssertionError
    {
        final String query = "PRAGMA foreign_key_check;";

        try(final Statement stmt         = this.getSqliteConnection().createStatement();
            final ResultSet foreignCheck = stmt.executeQuery(query))
        {
            assertTrue("PRAGMA foreign_key_check failed.",
                       !foreignCheck.next(),
                       Severity.Error);
        }
    }

    /**
     * Requirement 8
     *
     * <blockquote>
     * A GeoPackage SQLite Configuration SHALL provide SQL access to GeoPackage contents via software APIs.
     * </blockquote>
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 8",
                 text      = " A GeoPackage SQLite Configuration SHALL provide SQL access to GeoPackage contents via software APIs.")
    public void requirement8() throws AssertionError
    {
        try(final Statement stmt   = this.getSqliteConnection().createStatement();
            final ResultSet result = stmt.executeQuery("SELECT * FROM sqlite_master;"))
        {
            // If the statement can execute it has implemented the SQLite SQL API interface
            return;
        }
        catch(final SQLException ignored)
        {
            // fall through to failure
        }

        fail("GeoPackage needs to provide the SQLite SQL API interface.",
             Severity.Error);
    }

    /**
     * Requirement 9
     *
     * <blockquote>
     * Every GeoPackage SQLite Configuration SHALL have the SQLite library compile and
     * run time options specified in table <a href="http://www.geopackage.org/spec/#every_gpkg_sqlite_config_table">
     * Every GeoPackage SQLite Configuration</a>.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 9",
                 text      = "Every GeoPackage SQLite Configuration SHALL have the SQLite library compile and run time options specified in table http://www.geopackage.org/spec/#every_gpkg_sqlite_config_table.")
    public void requirement9() throws SQLException, AssertionError
    {
        try(final Statement stmt     = this.getSqliteConnection().createStatement();
            final ResultSet omitUsed = stmt.executeQuery("SELECT sqlite_compileoption_used('SQLITE_OMIT_*')"))
        {
            final int result = omitUsed.getInt(1);

            assertTrue("SQLite library compilations shall not include any OMIT options.",
                       result == 0,
                       Severity.Warning);
        }
    }

    /**
     * Requirement 10
     *
     * <blockquote>
     * A GeoPackage SHALL include a {@code gpkg_spatial_ref_sys} table per clause 1.1.2.1.1
     * <a href="http://www.geopackage.org/spec/#spatial_ref_sys_data_table_definition">Table Definition</a>,
     * Table <a href="http://www.geopackage.org/spec/#gpkg_spatial_ref_sys_cols">Spatial Ref Sys Table Definition</a> and Table
     * <a href="http://www.geopackage.org/spec/#gpkg_spatial_ref_sys_sql">gpkg_spatial_ref_sys Table Definition SQL</a>.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 10",
                 text      = "A GeoPackage SHALL include a gpkg_spatial_ref_sys table per clause 1.1.2.1.1 Table Definition, Table Spatial Ref Sys Table Definition and Table gpkg_spatial_ref_sys Table Definition SQL.")
    public void requirement10() throws AssertionError, SQLException
    {
        if(this.hasSpatialReferenceSystemTable)
        {
            this.verifyTable(CoreVerifier.SpatialReferenceSystemDefinition);
        }
        else
        {
            throw new AssertionError(String.format("The GeoPackage does not contain a %s table. This is a required table for all GeoPackages.",
                                                   GeoPackageCore.SpatialRefSysTableName),
                                     Severity.Error);
        }
    }

    /**
     * Requirement 11
     *
     * <blockquote>The {@code gpkg_spatial_ref_sys} table in a GeoPackage SHALL contain a record for
     * organization <a href="http://www.epsg.org/Geodetic.html">EPSG</a> or epsg</a>
     * and {@code organization_coordsys_id}
     * <a
     * href="http://www.epsg-registry.org/report.htm?type=selection&amp;entity=urn:ogc:def:crs:EPSG::4326&amp;reportDetail=long&amp;title=WGS%2084&amp;style=urn:uuid:report-style:default-with-code&amp;style_name=OGP%20Default%20With%20Code"
     * >4326</a> for <a href="http://www.google.com/search?as_q=WGS-84">WGS-84
     * </a>, a record with an {@code srs_id} of -1, an organization of
     * "NONE", an {@code organization_coordsys_id} of -1, and definition
     * "undefined" for undefined Cartesian coordinate reference systems, and a
     * record with an {@code srs_id} of 0, an organization of "NONE", an
     * {@code organization_coordsys_id} of 0, and definition "undefined"
     * for undefined geographic coordinate reference systems.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement
     */
    @Requirement(reference = "Requirement 11",
                 text      = "The gpkg_spatial_ref_sys table in a GeoPackage SHALL contain a record for organization EPSG or epsg " +
                             "and organization_coordsys_id 4326 for WGS-84, a record with an srs_id of -1, an organization of \"NONE\", " +
                             "an organization_coordsys_id of -1, and definition \"undefined\" for undefined Cartesian coordinate reference systems, " +
                             "and a record with an srs_id of 0, an organization of \"NONE\", an organization_coordsys_id of 0, and definition \"undefined\" " +
                             "for undefined geographic coordinate reference systems.")
    public void requirement11() throws SQLException, AssertionError
    {
        if(this.hasSpatialReferenceSystemTable)
        {
            final String wgs1984Sql = String.format("SELECT srs_id FROM %s WHERE organization_coordsys_id = 4326 AND (organization = 'EPSG' OR organization = 'epsg');",    // TODO figure out case insensitive comparison
                                                    GeoPackageCore.SpatialRefSysTableName);

            try(final Statement statement       = this.getSqliteConnection().createStatement();
                final ResultSet srsDefaultValue = statement.executeQuery(wgs1984Sql))
            {
                assertTrue(String.format("The %s table shall contain a record for organization \"EPSG\" or \"epsg\" and organization_coordsys_id 4326 for WGS-84",
                                         GeoPackageCore.SpatialRefSysTableName),
                           srsDefaultValue.next(),
                           Severity.Warning);
            }

            final String undefinedCartesianSql = String.format("SELECT srs_id FROM %s WHERE srs_id = -1 AND organization = 'NONE' AND organization_coordsys_id = -1 AND definition = 'undefined';", GeoPackageCore.SpatialRefSysTableName);

            try(final Statement statement       = this.getSqliteConnection().createStatement();
                final ResultSet srsDefaultValue = statement.executeQuery(undefinedCartesianSql))
            {
                assertTrue(String.format("The %s table shall contain a record with an srs_id of -1, an organization of \"NONE\", an organization_coordsys_id of -1, and definition \"undefined\" for undefined Cartesian coordinate reference systems",
                                         GeoPackageCore.SpatialRefSysTableName),
                           srsDefaultValue.next(),
                           Severity.Warning);
            }

            final String undefinedGeographicSql = String.format("SELECT srs_id FROM %s WHERE srs_id = 0 AND organization = 'NONE' AND organization_coordsys_id =  0 AND definition = 'undefined';", GeoPackageCore.SpatialRefSysTableName);

            try(final Statement statement       = this.getSqliteConnection().createStatement();
                final ResultSet srsDefaultValue = statement.executeQuery(undefinedGeographicSql))
            {
                assertTrue(String.format("The %s table shall contain a record with an srs_id of 0, an organization of \"NONE\", an organization_coordsys_id of 0, and definition \"undefined\" for undefined geographic coordinate reference systems.",
                                         GeoPackageCore.SpatialRefSysTableName),
                                         srsDefaultValue.next(),
                           Severity.Warning);
            }
        }
    }

    /**
     * Requirement 12
     *
     * <blockquote>
     * The {@code gpkg_spatial_ref_sys} table in a GeoPackage SHALL contain records to define all spatial reference systems used by features and tiles in a GeoPackage.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 12",
                 text      = "The gpkg_spatial_ref_sys table in a GeoPackage SHALL contain records to define all spatial reference systems used by features and tiles in a GeoPackage.")
    public void requirement12() throws SQLException, AssertionError
    {
        if(this.hasContentsTable && this.hasSpatialReferenceSystemTable)
        {
            final String query = String.format("SELECT DISTINCT srs_id as srsContents "+
                                               "FROM            %s "+
                                               "WHERE           srsContents " +
                                               "NOT IN (SELECT srs_id FROM %s);",
                                               GeoPackageCore.ContentsTableName,
                                               GeoPackageCore.SpatialRefSysTableName);


            final List<Integer> invalidSrsIds = JdbcUtility.select(this.getSqliteConnection(),
                                                                   query,
                                                                   null,
                                                                   resultSet -> resultSet.getInt("srsContents"));


            assertTrue(String.format("Not all srs_id's being used in a GeoPackage are defined. The following srs_id(s) are not in %s: %s",
                                     GeoPackageCore.SpatialRefSysTableName,
                                     invalidSrsIds.stream()
                                                  .map(Object::toString)
                                                  .collect(Collectors.joining(", "))),
                       invalidSrsIds.isEmpty(),
                       Severity.Error);
        }
    }

    /**
     * Requirement 13
     *
     * <blockquote>
     * A GeoPackage file SHALL include a {@code gpkg_contents} table per table
     *  <a href="http://www.geopackage.org/spec/#gpkg_contents_cols">Contents Table or View Definition</a>
     *  and <a href="http://www.geopackage.org/spec/#gpkg_contents_sql">gpkg_contents Table Definition SQL</a>.
     * </blockquote>
     * @throws SQLException throws if SQLException occurs
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 13",
                 text      = "A GeoPackage file SHALL include a gpkg_contents table per table Contents Table or View Definition and gpkg_contents Table Definition SQL.")
    public void requirement13() throws SQLException, AssertionError
    {
        if(this.hasContentsTable)
        {
            this.verifyTable(CoreVerifier.ContentTableDefinition);
        }
        else
        {
            throw new AssertionError(String.format("The GeoPackage does not contain a %s table. This is a required table for all GeoPackages.",
                                                   GeoPackageCore.ContentsTableName),
                                     Severity.Error);
        }
    }

    /**
     * Requirement 14
     *
     * <blockquote>
     * The {@code table_name} column value in a {@code gpkg_contents} table row SHALL contain the name of a SQLite table or view.
     * </blockquote>
     *
     * @throws SQLException throws if an SQLException occurs;
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 14",
                 text      = "The table_name column value in a gpkg_contents table row SHALL contain the name of a SQLite table or view.")
    public void requirement14() throws SQLException, AssertionError
    {
        if(this.hasContentsTable)
        {
            final String query =  String.format("SELECT DISTINCT table_name AS gc_table " +
                                                "FROM            %s " +
                                                "WHERE           gc_table NOT IN "+
                                                                               "(SELECT tbl_name"+
                                                                               " FROM   sqlite_master " +
                                                                                "WHERE  tbl_name = gc_table);",
                                               GeoPackageCore.ContentsTableName);

            try(final Statement stmt        = this.getSqliteConnection().createStatement();
                final ResultSet gcTableName = stmt.executeQuery(query))
            {
                // check runtime options (foreign keys)
                final List<String> invalidContentsTableNames = ResultSetStream.getStream(gcTableName)
                                                                              .map(result -> { try
                                                                                               {
                                                                                                    return result.getString("gc_table");
                                                                                               }
                                                                                               catch(final SQLException ignored)
                                                                                               {
                                                                                                   return null;
                                                                                               }
                                                                                             })
                                                                              .filter(Objects::nonNull)
                                                                              .collect(Collectors.toList());

                assertTrue(String.format("The following table_name(s) in %s are invalid: \n%s.",
                                         GeoPackageCore.ContentsTableName,
                                         invalidContentsTableNames.stream()
                                                                  .map(table_name->String.format("\t%s",table_name))
                                                                  .collect(Collectors.joining("\n"))),
                           invalidContentsTableNames.isEmpty(),
                           Severity.Warning);
            }
        }
    }

    /**
     * Requirement 15
     *
     * <blockquote>
     * Values of the {@code gpkg_contents} table {@code last_change}
     * column SHALL be in <a
     * href="http://www.iso.org/iso/catalogue_detail?csnumber=40874">ISO 8601
     * </a> format containing a complete date plus UTC hours, minutes, seconds
     * and a decimal fraction of a second, with a 'Z' ('zulu') suffix
     * indicating UTC.
     * </blockquote>
     * @throws SQLException throws if an SQLException occurs;
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 15",
                 text      = "Values of the gpkg_contents table last_change column SHALL be in ISO 8601 "
                             + "format containing a complete date plus UTC hours, minutes, seconds and a "
                             + "decimal fraction of a second, with a 'Z' ('zulu') suffix indicating UTC.")
    public void requirement15() throws SQLException, AssertionError
    {
        if(this.hasContentsTable)
        {
            final String query = String.format("SELECT last_change FROM %s;", GeoPackageCore.ContentsTableName);

            try(final Statement stmt       = this.getSqliteConnection().createStatement();
                final ResultSet lastchange = stmt.executeQuery(query))
            {
                // check format of last_change column
                while(lastchange.next())
                {
                    final String data = lastchange.getString("last_change");

                    try
                    {
                        final SimpleDateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");

                        formatter.parse(data);
                    }
                    catch(final ParseException ex)
                    {
                        final SimpleDateFormat formatter2  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                        try
                        {
                            formatter2.parse(data);
                        }
                        catch(final ParseException ignored)
                        {
                            fail(String.format("A field in the last_change column in %s table was not in the correct format. %s",
                                               GeoPackageCore.ContentsTableName,
                                               ex.getMessage()),
                                 Severity.Warning);
                        }
                    }
                }
            }
        }
    }

    /**
     * Requirement 16
     *
     * <blockquote>
     * Values of the {@code gpkg_contents} table {@code srs_id}
     * column SHALL reference values in the {@code gpkg_spatial_ref_sys}
     * table {@code srs_id} column.
     * </blockquote>
     *
     * @throws SQLException throws if an SQLException occurs;
     * @throws AssertionError throws if it fails to meet the specified requirement;
     */
    @Requirement(reference = "Requirement 16",
                 text      = "Values of the gpkg_contents table srs_id column SHALL reference values in the gpkg_spatial_ref_sys table srs_id column.")
    public void requirement16() throws SQLException, AssertionError
    {
        if(this.hasContentsTable)
        {
            final String query = String.format("PRAGMA foreign_key_check('%s');",
                                               GeoPackageCore.ContentsTableName);

            try(final Statement statement  = this.getSqliteConnection().createStatement();
                final ResultSet foreignKey = statement.executeQuery(query))
            {
                // check runtime options (foreign keys)
                assertTrue(String.format("There are violations on the foreign keys in the table %s",
                                         GeoPackageCore.ContentsTableName),
                           !foreignKey.next(),
                           Severity.Error);
            }
        }
    }

    private boolean doesTableExist(final String tableName) throws SQLException
    {
        return DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), tableName);
    }

    private final File file;

    private static final TableDefinition ContentTableDefinition;
    private static final TableDefinition SpatialReferenceSystemDefinition;

    static
    {
        final Map<String, ColumnDefinition> contentColumns = new HashMap<>();

        contentColumns.put("table_name",  new ColumnDefinition("TEXT",     true,  true,  true,  null));
        contentColumns.put("data_type",   new ColumnDefinition("TEXT",     true,  false, false, null));
        contentColumns.put("identifier",  new ColumnDefinition("TEXT",     false, false, true,  null));
        contentColumns.put("description", new ColumnDefinition("TEXT",     false, false, false, "''"));
        contentColumns.put("last_change", new ColumnDefinition("DATETIME", true,  false, false, "strftime('%Y-%m-%dT%H:%M:%fZ', 'now')"));
        contentColumns.put("min_x",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("min_y",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("max_x",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("max_y",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("srs_id",      new ColumnDefinition("INTEGER",  false, false, false, null));

        ContentTableDefinition = new TableDefinition(GeoPackageCore.ContentsTableName,
                                                     contentColumns,
                                                     new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageCore.SpatialRefSysTableName, "srs_id", "srs_id"))));

        final Map<String, ColumnDefinition> spatialReferenceSystemColumns = new HashMap<>();

        spatialReferenceSystemColumns.put("srs_name",                 new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("srs_id",                   new ColumnDefinition("INTEGER", true,  true,  true,  null));
        spatialReferenceSystemColumns.put("organization",             new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("organization_coordsys_id", new ColumnDefinition("INTEGER", true,  false, false, null));
        spatialReferenceSystemColumns.put("definition",               new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("description",              new ColumnDefinition("TEXT",    false, false, false, null));

        SpatialReferenceSystemDefinition = new TableDefinition(GeoPackageCore.SpatialRefSysTableName,
                                                               spatialReferenceSystemColumns);
    }
}
