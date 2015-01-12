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

import static com.rgi.geopackage.verification.Assert.assertArrayEquals;
import static com.rgi.geopackage.verification.Assert.assertTrue;
import static com.rgi.geopackage.verification.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.Verifier;

/**
 * @author Luke Lambert
 * @author Jenifer Cochran
 */
public class CoreVerifier extends Verifier
{
    /**
     * Constructor
     *
     * @param file File handle to the SQLite database
     * @param sqliteConnection JDBC connection to the SQLite database
     */
    public CoreVerifier(final File file, final Connection sqliteConnection)
    {
        super(sqliteConnection);
        if(file == null)
        {
            throw new IllegalArgumentException("File cannot be null");
        }

        this.file = file;
    }

    /**
     * Requirement 1
     * <blockquote>
     * A GeoPackage SHALL be a <a href="http://www.sqlite.org/">SQLite</a>
     * database file using <a href="http://sqlite.org/fileformat2.html">version
     * 3 of the SQLite file format</a>. The first 16 bytes of a GeoPackage
     * SHALL contain "SQLite format 3" in ASCII
     * </blockquote>
     *
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws IOException
     * @throws AssertionError
     */
    @Requirement(number = 1,
                 text = "A GeoPackage SHALL be a SQLite database file using version 3 of the SQLite file format. The first 16 bytes of a GeoPackage SHALL contain \"SQLite format 3\" in ASCII.",
                 severity = Severity.Warning)
    public void Requirement1() throws IOException, AssertionError
    {
        final byte[] header = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);    // The GeoPackage spec says it's StandardCharsets.US_ASCII, but the SQLite spec (https://www.sqlite.org/fileformat.html - 1.2.1 Magic Header String) says it's UTF8, i.e, StandardCharsets.UTF_8

        final byte[] data = new byte[header.length];

        try(FileInputStream fileInputStream = new FileInputStream(this.file))
        {
            assertTrue("The header information of the file does not contain enough bytes to include necessary information", fileInputStream.read(data, 0, header.length) == header.length);
            assertArrayEquals("The database file is not using a version 3 of the SQLite format.  Or does not include the SQLite version in the file header.", header, data);
        }
    }

    /**
     * Requirement 2
     * <blockquote>
     * A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII)
     * in the application id field of the SQLite database header
     * to indicate a GeoPackage version 1.0 file.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws IOException
     */
    @Requirement(number = 2,
                 text = "A GeoPackage SHALL contain 0x47503130 ('GP10' in ASCII) in the application id field of the SQLite database header to indicate a GeoPackage version 1.0 file.",
                 severity = Severity.Warning)
    public void Requirement2() throws AssertionError
    {
        final int  sizeOfInt = 4;
        final long applicationIdByteOffset = 68;

        final byte[] data = new byte[sizeOfInt];    // 4 bytes in an int

        // application id
        // http://www.sqlite.org/fileformat2.html
        // http://www.geopackage.org/spec/#_sqlite_container
        // A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
        // application id field of the SQLite database header to indicate a
        // GeoPackage version 1.0 file.
        // The bytes 'G', 'P', '1', '0' are equivalent to 0x47503130
        final int expectedAppId = 0x47503130;

        try(RandomAccessFile randomAccessFile = new RandomAccessFile(this.file, "r"))
        {
            randomAccessFile.seek(applicationIdByteOffset);
            assertTrue("The file does not have enough bytes to include a header for the GeoPackage to check the valid information.",
                       randomAccessFile.read(data, 0, sizeOfInt) == sizeOfInt);
        }
        catch(final Exception ex)
        {
            throw new AssertionError(ex);
        }

        final int applicationId = ByteBuffer.wrap(data).asIntBuffer().get();

        assertTrue(String.format("Bad Application ID: 0x%08x Expected: 0x%08x", applicationId, expectedAppId),
                   applicationId == expectedAppId);
    }

    /**
     * Requirement 3
     * <blockquote>
     * A GeoPackage SHALL have the file extension name ".gpkg".
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws AssertionError
     */
    @Requirement(number = 3,
                 text = "A GeoPackage SHALL have the file extension name '.gpkg'",
                 severity = Severity.Warning)
    public void Requirement3() throws AssertionError
    {
        final String fileName          = this.file.getName();
        final String extension         = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        final String expectedExtension = "gpkg";

        assertTrue(String.format("Not a GeoPackage File: %s \nExpected a file with the extension: %s", fileName, expectedExtension),
                   extension.equals(expectedExtension));
    }

    /**
     * Requirment 4
     * <blockquote>
     * A GeoPackage SHALL only contain data elements, SQL constructs and GeoPackage extensions with the "gpkg" author name specified in this encoding standard.
     * </blockquote>
     *
     * @return Returns true, this test is checked by other tests in GeoPackage CoreVerifier
     */
    @Requirement(number = 4,
                 text = "A GeoPackage SHALL only contain data elements, SQL constructs and GeoPackage extensions with the \"gpkg\" author name specified in this encoding standard.",
                 severity = Severity.Warning)
    public static void Requirement4()
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
     * <blockquote>
     * The columns of tables in a GeoPackage SHALL only be declared using one of the data types specified in table <a href="http://www.geopackage.org/spec/#table_column_data_types">GeoPackage Data Types</a>.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 5,
                 text = "The columns of tables in a GeoPackage SHALL only be declared using one of the data types specified in table GeoPackage Data Types.",
                 severity = Severity.Error)
    public void Requirement5() throws SQLException, AssertionError
    {
        final String query = "SELECT table_name FROM gpkg_contents;";

        try(Statement stmt      = this.getSqliteConnection().createStatement();
            ResultSet tableName = stmt.executeQuery(query);)
        {
            while(tableName.next())
            {
                final String table_name = tableName.getString("table_name");

                try(Statement stmt2           = this.getSqliteConnection().createStatement();
                    ResultSet pragmaTableinfo = stmt2.executeQuery(String.format("PRAGMA table_info(%s);", table_name));)
                {
                    while(pragmaTableinfo.next())
                    {
                        final String dataType         = pragmaTableinfo.getString("type");
                        final boolean correctDataType = Verifier.checkDataType(dataType);

                        assertTrue(String.format("Incorrect data type encountered: %s  From table: %s", dataType, table_name), correctDataType);
                    }
                }
            }
        }
    }

    /**
     * Requirement 6
     * <blockquote>
     * The SQLite PRAGMA integrity_check SQL command SHALL return "ok" for a GeoPackage file.
     * </blockquote>
     *
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 6,
                 text = "The SQLite PRAGMA integrity_check SQL command SHALL return \"ok\" for a GeoPackage file.",
                 severity = Severity.Error)
    public void Requirement6() throws SQLException, AssertionError
    {
        final String query = "PRAGMA integrity_check;";

        try(Statement stmt           = this.getSqliteConnection().createStatement();
            ResultSet integrityCheck = stmt.executeQuery(query);)
        {
            integrityCheck.next();
            final String integrity_check = integrityCheck.getString("integrity_check");
            assertTrue("PRAGMA integrity_check failed.", integrity_check.equals("ok"));
        }
    }

    /**
     * Requirement 7
     * <blockquote>
     * The SQLite PRAGMA foreign_key_check SQL with no parameter value SHALL return an empty result set indicating no invalid foreign key values for a GeoPackage file.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 7,
                 text = "The SQLite PRAGMA foreign_key_check SQL with no parameter value SHALL return an empty result set indicating no invalid foreign key values for a GeoPackage file.",
                 severity = Severity.Error)
    public void Requirement7() throws SQLException, AssertionError
    {
        final String query = "PRAGMA foreign_key_check;";

        try(Statement stmt         = this.getSqliteConnection().createStatement();
            ResultSet foreignCheck = stmt.executeQuery(query);)
        {
            final boolean badfk = foreignCheck.next();
            assertTrue("PRAGMA foreign_key_check failed.", badfk != true);
        }
    }

    /**
     * Requirement 8
     * <blockquote>
     * A GeoPackage SQLite Configuration SHALL provide SQL access to GeoPackage contents via software APIs.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 8,
                 text = " A GeoPackage SQLite Configuration SHALL provide SQL access to GeoPackage contents via software APIs.",
                 severity = Severity.Error)
    public void Requirement8() throws AssertionError
    {
        final String query = "SELECT * FROM sqlite_master;";

        try(Statement stmt = this.getSqliteConnection().createStatement();)
        {
            stmt.executeQuery(query);

            assertTrue(true);  // If the statement can execute it has implemented the SQLite SQL API interface
        }
        catch(final SQLException e)
        {
        	e.printStackTrace();
            fail("GeoPackage needs to provide the SQLite SQL API interface.");
        }
    }

    /**
     * Requirement 9
     * <blockquote>
     * Every GeoPackage SQLite Configuration SHALL have the SQLite library compile and
     * run time options specified in table <a href="http://www.geopackage.org/spec/#every_gpkg_sqlite_config_table">
     * Every GeoPackage SQLite Configuration</a>.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 9,
                 text = "Every GeoPackage SQLite Configuration SHALL have the SQLite library compile and run time options specified in table http://www.geopackage.org/spec/#every_gpkg_sqlite_config_table.",
                 severity = Severity.Warning)
    public void Requirement9() throws SQLException, AssertionError
    {
        final String query2 = "SELECT sqlite_compileoption_used('SQLITE_OMIT_*')";

        try(Statement stmt     = this.getSqliteConnection().createStatement();
            ResultSet omitUsed = stmt.executeQuery(query2);)
        {
            assertTrue("For a GeoPackage you are not allowed to use any omit options during compile time."
                    + "  Please remove any SQLITE_OMIT options used.", 1 != omitUsed.getInt("sqlite_compileoption_used('SQLITE_OMIT_*')"));
        }
    }

    /**
     * Requirement 10
     * <blockquote>
     * A GeoPackage SHALL include a <code>gpkg_spatial_ref_sys</code> table per clause 1.1.2.1.1
     * <a href="http://www.geopackage.org/spec/#spatial_ref_sys_data_table_definition">Table Definition</a>,
     * Table <a href="http://www.geopackage.org/spec/#gpkg_spatial_ref_sys_cols">Spatial Ref Sys Table Definition</a> and Table
     * <a href="http://www.geopackage.org/spec/#gpkg_spatial_ref_sys_sql">gpkg_spatial_ref_sys Table Definition SQL</a>.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 10,
                 text = "A GeoPackage SHALL include a gpkg_spatial_ref_sys table per clause 1.1.2.1.1 Table Definition, Table Spatial Ref Sys Table Definition and Table gpkg_spatial_ref_sys Table Definition SQL.",
                 severity = Severity.Error)
    public void Requirement10() throws SQLException, AssertionError
    {
        this.verifyTable(CoreVerifier.SpatialReferenceSystemDefinition);
    }

    /**
     * Requirement 11
     * <blockquote>The <code>gpkg_spatial_ref_sys</code> table in a GeoPackage SHALL contain a record for
     * organization <a href="http://www.epsg.org/Geodetic.html">EPSG</a> or epsg</a>
     * and <code>organization_coordsys_id</code>
     * <a href="http://www.epsg-registry.org/report.htm?type=selection&amp;entity=urn:ogc:def:crs:EPSG::4326&amp;reportDetail=long&amp;title=WGS%2084&amp;style=urn:uuid:report-style:default-with-code&amp;style_name=OGP%20Default%20With%20Code">4326</a>
     * </a><a href="#14"></a> for <a href="http://www.google.com/search?as_q=WGS-84">WGS-84</a> <a href="#15"></a>, a record with an <code>srs_id</code> of -1, an organization of "NONE", an <code>organization_coordsys_id</code>
     * of -1, and definition "undefined" for undefined Cartesian coordinate reference systems, and a record with an <code>srs_id</code> of 0, an organization of "NONE", an <code>organization_coordsys_id</code> of 0,
     * and definition "undefined" for undefined geographic coordinate reference systems.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 11,
                 text = "The gpkg_spatial_ref_sys table in a GeoPackage SHALL contain a record for organization EPSG or epsg" +
                        " and organization_coordsys_id 4326 for WGS-84, a record with an srs_id of -1, an organization of \"NONE\", " +
                        "an organization_coordsys_id of -1, and definition \"undefined\" for undefined Cartesian coordinate reference systems, " +
                        "and a record with an srs_id of 0, an organization of \"NONE\", an organization_coordsys_id of 0, and definition \"undefined\" " +
                        "for undefined geographic coordinate reference systems. ",
                 severity = Severity.Warning)
    public void Requirement11() throws SQLException, AssertionError
    {
        final String query  = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id = -1    AND       organization  = 'NONE' AND organization_coordsys_id = -1    AND definition = 'undefined';";
        final String query2 = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id =  0    AND       organization  = 'NONE' AND organization_coordsys_id =  0    AND definition = 'undefined';";
        final String query3 = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id =  4326 AND LOWER(organization) = 'epsg' AND organization_coordsys_id =  4326;";

        try(Statement stmt             = this.getSqliteConnection().createStatement();
            ResultSet srsdefaultvalues = stmt.executeQuery(query);
            )
            {
                // make sure the result sets are not empty
                assertTrue(String.format("The gpkg_spatial_ref_sys does not contain the default values needed to meet the standard."
                                        + "\n Need to include a record with this information: %s", query),
                                          srsdefaultvalues.next());
            }
        try(Statement stmt2 = this.getSqliteConnection().createStatement();
            ResultSet srsdefaultvalue2 = stmt2.executeQuery(query2))
                {

                 assertTrue(String.format("The gpkg_spatial_ref_sys does not contain the default values needed to meet the standard."
                                    + "\n Need to include a record with this information: %s", query2),
                                     srsdefaultvalue2.next());
                }
        try(Statement stmt3 = this.getSqliteConnection().createStatement();
            ResultSet srsdefaultvalue3 = stmt3.executeQuery(query3))
                {
                    assertTrue(String.format("The gpkg_spatial_ref_sys does not contain the default values needed to meet the standard."
                                    + "\n Need to include a record with this information: %s", query3),
                                     srsdefaultvalue3.next());
                }
    }


    /**
     * Requirement 12
     * <blockquote>
     * The <code>gpkg_spatial_ref_sys</code> table in a GeoPackage SHALL contain records to define all spatial reference systems used by features and tiles in a GeoPackage.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 12,
                 text = "The gpkg_spatial_ref_sys table in a GeoPackage SHALL contain records to define all spatial reference systems used by features and tiles in a GeoPackage.",
                 severity = Severity.Error)
    public void Requirement12() throws SQLException, AssertionError
    {
        final String query = "SELECT DISTINCT gc.srs_id AS gc_srid, srs.srs_name, srs.srs_id, srs.organization, srs.organization_coordsys_id, "
                            + " srs.definition FROM gpkg_contents AS gc LEFT OUTER JOIN gpkg_spatial_ref_sys AS srs ON srs.srs_id = gc.srs_id;";

        try(Statement stmt       = this.getSqliteConnection().createStatement();
            ResultSet srsdefined = stmt.executeQuery(query);)
        {

            while(srsdefined.next())
            {
                final String srsGC  = srsdefined.getString("gc_srid");
                final String srsSRS = srsdefined.getString("srs_id");

                assertTrue(String.format("Not all srs_id's being used in a GeoPackage are defined.\n gpkg_contents srs_id: %s gpkg_spatial_ref_sys srs_id: %s", srsGC, srsSRS), srsGC != null && srsSRS !=null);
            }

        }
    }

    /**
     * Requirement 13
     * <blockquote>
     * A GeoPackage file SHALL include a <code>gpkg_contents</code> table per table
     *  <a href="http://www.geopackage.org/spec/#gpkg_contents_cols">Contents Table or View Definition</a>
     *  and <a href="http://www.geopackage.org/spec/#gpkg_contents_sql">gpkg_contents Table Definition SQL</a>.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number   = 13,
                 text     = "A GeoPackage file SHALL include a gpkg_contents table per table Contents Table or View Definition and gpkg_contents Table Definition SQL.",
                 severity = Severity.Error)
    public void Requirement13() throws SQLException, AssertionError
    {
        this.verifyTable(CoreVerifier.ContentTableDefinition);
    }

    /**
     * Requirement 14
     * <blockquote>
     * The <code>table_name</code> column value in a <code>gpkg_contents</code> table row SHALL contain the name of a SQLite table or view.
     * </blockquote>
     *
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 14,
                 text = "The table_name column value in a <code>gpkg_contents table row SHALL contain the name of a SQLite table or view.",
                 severity = Severity.Warning)
    public void Requirement14() throws SQLException, AssertionError
    {
        final String query = "SELECT DISTINCT gc.table_name AS gc_table, sm.tbl_name FROM gpkg_contents AS gc LEFT OUTER JOIN sqlite_master AS sm ON gc.table_name = sm.tbl_name;";

        try(Statement stmt        = this.getSqliteConnection().createStatement();
            ResultSet gctablename = stmt.executeQuery(query);)
        {
            // check runtime options (foreign keys)
            while(gctablename.next())
            {
                final String gctable  = gctablename.getString("gc_table");
                final String tbl_name = gctablename.getString("tbl_name");
                assertTrue(String.format("The table_name value in gpkg_contents table is invalid for the table: %s", tbl_name), gctable != null);
            }
        }

        //check foreign key constraints
        final String query2 = "PRAGMA foreign_key_list(gpkg_contents);";

        try(Statement stmt           = this.getSqliteConnection().createStatement();
            ResultSet gpkgContentsFK = stmt.executeQuery(query2))
        {
            assertTrue("Tile Matrix Table does not have a Foreign Key constraint enabled on the column table_name to referenced the column table_name in gpkg_contents.", gpkgContentsFK.next());

            final String refTable = gpkgContentsFK.getString("table");
            final String from     = gpkgContentsFK.getString("from");
            final String to       = gpkgContentsFK.getString("to");

            final boolean goodFKConstraint = (refTable.equals("gpkg_spatial_ref_sys") && from.equals("srs_id") && to.equals("srs_id"));
            assertTrue("The gpkg_contents Table does not have a Foreign Key constraint enabled on the column srs_id to referenced the column srs_id in gpkg_spatial_ref_sys.", goodFKConstraint);
        }
    }

    /**
     * Requirement 15
     * <blockquote>
     * Values of the <code>gpkg_contents</code> table <code>last_change</code> column SHALL be in
     * <a href="http://www.iso.org/iso/catalogue_detail?csnumber=40874">ISO 8601 </a></a>
     * format containing a complete date plus UTC hours, minutes, seconds and a decimal fraction of a second,
     * with a ‘Z’ (‘zulu’) suffix indicating UTC.
     * </blockquote>
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 15,
                 text = "Values of the gpkg_contents table last_change column SHALL be in ISO 8601 format containing a complete date plus UTC hours, minutes, seconds and a decimal fraction of a second, with a 'Z' ('zulu') suffix indicating UTC.",
                 severity = Severity.Warning)
    public void Requirement15() throws SQLException, AssertionError
    {
        final String query = "SELECT last_change FROM gpkg_contents;";

        try(Statement stmt       = this.getSqliteConnection().createStatement();
            ResultSet lastchange = stmt.executeQuery(query);)
        {
            // check format of last_change column
            while(lastchange.next())
            {
                final String data       = lastchange.getString("last_change");
                final String formatdate = data;

                try
                {
                    final SimpleDateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");

                    formatter.parse(formatdate);
                }
                catch(final ParseException ex)
                {
                    final SimpleDateFormat formatter2  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    try
                    {
                        formatter2.parse(data);
                    }
                    catch(final Exception e)
                    {
                        fail("A field in the last_change column in gpkg_contents table was not in the correct format. " + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Requirement 16
     * <blockquote>
     * Values of the <code>gpkg_contents</code> table <code>srs_id</code> column SHALL reference values in the <code>gpkg_spatial_ref_sys</code> table <code>srs_id</code> column.
     * </blockquote>
     *
     * @return Returns true if it meets the specified requirement; otherwise false;
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement(number = 16,
                 text = "Values of the gpkg_contents table srs_id column SHALL reference values in the gpkg_spatial_ref_sys table srs_id column.",
                 severity = Severity.Error)
    public void Requirement16() throws SQLException, AssertionError
    {
        final String query = "PRAGMA foreign_key_check('gpkg_contents');";

        try(Statement stmt       = this.getSqliteConnection().createStatement();
            ResultSet foreignKey = stmt.executeQuery(query);)
        {
            // check runtime options (foreign keys)
            assertTrue("There are violations on the foreign keys in the table gpkg_contents", !foreignKey.next());
        }
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
        contentColumns.put("description", new ColumnDefinition("TEXT",     false, false, false, "\\s*''\\s*|\\s*\"\"\\s*"));
        contentColumns.put("last_change", new ColumnDefinition("DATETIME", true,  false, false, "\\s*strftime\\s*\\(\\s*['\"]%Y-%m-%dT%H:%M:%fZ['\"]\\s*,\\s*['\"]now['\"]\\s*\\)\\s*"));
        contentColumns.put("min_x",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("min_y",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("max_x",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("max_y",       new ColumnDefinition("DOUBLE",   false, false, false, null));
        contentColumns.put("srs_id",      new ColumnDefinition("INTEGER",  false, false, false, null));

        ContentTableDefinition = new TableDefinition("gpkg_contents",
                                                     contentColumns,
                                                     new HashSet<>(Arrays.asList(new ForeignKeyDefinition("gpkg_spatial_ref_sys", "srs_id", "srs_id"))));

        final Map<String, ColumnDefinition> spatialReferenceSystemColumns = new HashMap<>();

        spatialReferenceSystemColumns.put("srs_name",                 new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("srs_id",                   new ColumnDefinition("INTEGER", true,  true,  true,  null));
        spatialReferenceSystemColumns.put("organization",             new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("organization_coordsys_id", new ColumnDefinition("INTEGER", true,  false, false, null));
        spatialReferenceSystemColumns.put("definition",               new ColumnDefinition("TEXT",    true,  false, false, null));
        spatialReferenceSystemColumns.put("description",              new ColumnDefinition("TEXT",    false, false, false, null));

        SpatialReferenceSystemDefinition = new TableDefinition("gpkg_spatial_ref_sys",
                                                               spatialReferenceSystemColumns);
    }
}
