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

package com.rgi.erdc.gpkg;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class CoreTest
{
    public static final String filePath = "test.gpkg";

    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }

    @BeforeClass
    public static void setUp() throws Exception
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver

        try(GeoPackage gpkg = new GeoPackage(new File(filePath)))
        {
            // let the geopackage close
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        final File file = new File(filePath);
        if(file.exists())
        {
            if(!file.delete())
            {
                throw new RuntimeException("Unable to delete old gpkg");
            }
        }
    }

    /**
     * Verify that the GeoPackage is an SQLite version_3 database
     */
    @Test
    public void Req1()
    {

        try
        {
            byte[] data     = Files.readAllBytes(Paths.get(filePath));
            data            = Arrays.copyOfRange(data, 0, 16);
            final byte[] sentinal = "SQLite format 3\0".getBytes("US-ASCII");

            assertArrayEquals(sentinal, data);
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the SQLite database header application id field indicates GeoPackage version 1.0
     */
    @Test
    public void Req2()
    {
        try
        {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            data = Arrays.copyOfRange(data, 68, 72); // Bytes 68-72 describe the
            // application id
            // http://www.sqlite.org/fileformat2.html
            // http://www.geopackage.org/spec/#_sqlite_container
            // A GeoPackage SHALL contain 0x47503130 ("GP10" in ASCII) in the
            // application id field of the SQLite database header to indicate a
            // GeoPackage version 1.0 file.
            // The bytes 'G', 'P', '1', '0' are equivalent to 0x47503130
            final int expectedAppId = 0x47503130;
            final int applicationId = ByteBuffer.wrap(data).asIntBuffer().get();
            assertTrue(String.format("Bad Application ID: 0x%08x Expected: 0x%08x", applicationId, expectedAppId), applicationId == expectedAppId);
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the GeoPackage extension is ."gpkg"
     */
    @Test
    public void Req3()
    {
        try
        {
            final String extension         = filePath.substring(filePath.lastIndexOf(".") + 1, filePath.length());
            final String expectedExtension = "gpkg";

            assertTrue(String.format("Not a GeoPackage File: %s \nExpected a file with the extension: %s", filePath, expectedExtension), extension.equals(expectedExtension));
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the Geopackage only contains specified contents
     */
    @Test
    public void Req4()
    {
        //this requirement is tested through other test cases
        //The tables we test are:
        //gpkg_contents            per test Req13 in CoreTest
        //gpkg_spatial_ref_sys     per test Req10 in CoreTest
        //gpkg_tile_matrix         per test Req41 in OptionClassTiles
        //gpkg_tile_matrix_set     per test Req37 in OptionsClassTiles
        //Pyramid User Data Tables per test Req33 in OptionsClassTiles
    }


    /**
     * Verify that the data types of GeoPackage Contents columns include only the types
     * specified by table column data types
     */
    @Test
    public void Req5()
    {
        final String query = "SELECT table_name FROM gpkg_contents;";

        try (Connection con      = getConnection();
             Statement stmt      = con.createStatement();
             ResultSet tableName = stmt.executeQuery(query);)
        {

            while (tableName.next())
            {
                final String table_name = tableName.getString("table_name");

                try (Statement stmt2           = con.createStatement();
                     ResultSet pragmaTableinfo = stmt2.executeQuery(String.format("PRAGMA table_info(%s);", table_name));)
                {
                    while (pragmaTableinfo.next())
                    {
                        final String pragma           = pragmaTableinfo.getString("type");
                        final boolean correctDataType = checkDataType(pragma, table_name);

                        assertTrue(correctDataType);
                    }
                }
            }
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     * Verify that the GeoPackage passes the SQLite integrity check.
     */
    @Test
    public void Req6()
    {
        final String query = "PRAGMA integrity_check;";

        try (Connection con           = getConnection();
             Statement stmt           = con.createStatement();
             ResultSet integrityCheck = stmt.executeQuery(query);)
        {
            integrityCheck.next();
            final String integrity_check = integrityCheck.getString("integrity_check");
            assertTrue("PRAGMA integrity_check failed.", integrity_check.equals("ok"));
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the GeoPackage passes the SQLite foreign_key_check.
     */
    @Test
    public void Req7()
    {
        final String query = "PRAGMA foreign_key_check;";

        try (Connection con         = getConnection();
             Statement stmt         = con.createStatement();
             ResultSet foreignCheck = stmt.executeQuery(query);)
        {
            final boolean badfk = foreignCheck.next();
            assertTrue("PRAGMA foreign_check failed.", badfk != true);
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Test that the GeoPackage SQLite Extension provides the SQLite SQL API
     * interface.
     */
    @Test
    public void Req8()
    {
        final String query = "SELECT * FROM sqlite_master;";

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();)
        {
            stmt.executeQuery(query);
            // if the statement can execute it has implemented the SQLite SQL
            // API
            // interface
            assertTrue(true);
        }
        catch (final SQLException e)
        {
        	e.printStackTrace();
            fail("GeoPackage needs to provide the SQLite SQL API interface.");
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     * Verify that a GeoPackage SQLite Extension has the Every GeoPackage SQLite
     * Configuration compile and run time options.
     * this does not check to see if Foreign key check was enabled at the
     * start of the program.  Other tests will test if there are infractions
     * on the foreign keys
     */
    @Test
    public void Req9()
    {
        final String query2 = "SELECT sqlite_compileoption_used('SQLITE_OMIT_*')";

        try (Connection con       = getConnection();
             Statement stmt       = con.createStatement();
             ResultSet omitUsed = stmt.executeQuery(query2);)
        {
            assertTrue("For a GeoPackage you are not allowed to use any omit options during compile time.  Please remove any SQLITE_OMIT options used.", 1 != omitUsed.getInt("sqlite_compileoption_used('SQLITE_OMIT_*')"));
        }
        catch (final SQLException e)
        {
            fail(e.getMessage());
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     * Verify that the gpkg_spatial_ref_sys table exists and has the correct
     * definition.
     */
    @Test
    public void Req10()
    {
        final String query = "SELECT sql FROM sqlite_master WHERE type = 'table' AND tbl_name = 'gpkg_spatial_ref_sys';";

        try (Connection con        = getConnection();
             Statement  stmt       = con.createStatement();
             ResultSet  gpkg_srs   = stmt.executeQuery(query);

             Statement  stmt2      = con.createStatement();
             ResultSet  table_info = stmt2.executeQuery("PRAGMA table_info(gpkg_spatial_ref_sys);");)
        {
            while (gpkg_srs.next())
            {
                final String sql = gpkg_srs.getString("sql");
                assertTrue("The sql field must not be empty. Must include the gpkg_spatial_ref_sys Table SQL Definition.", sql != null);
            }

            final Map<String, Boolean> columns = new HashMap<>();

            while (table_info.next())
            {
                final String namecolumn    = table_info.getString("name");
                final String typecolumn    = table_info.getString("type");
                final String notnullcolumn = table_info.getString("notnull");
                final String pk            = table_info.getString("pk");

                final boolean colValid = checkSrsTable(namecolumn, typecolumn, notnullcolumn, pk);
                assertTrue(colValid);
                columns.put(namecolumn, colValid);
            }

            // Make sure the required fields exist in the table
            final String[] requiredCols = new String[]
            { "srs_name", "srs_id", "organization", "organization_coordsys_id", "definition", "description" };

            for (final String requiredCol : requiredCols)
            {
                assertTrue(String.format("The column: %s in table gpkg_spatial_ref_sys is not valid", requiredCol), columns.containsKey(requiredCol));
            }

        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the spatial_ref_sys table contains the required default
     * contents.
     */
    @Test
    public void Req11()
    {
        final String query  = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id = -1 AND organization = 'NONE' AND organization_coordsys_id = -1 AND definition = 'undefined';";
        final String query2 = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id = 0 AND organization = 'NONE' AND organization_coordsys_id = 0 AND definition = 'undefined';";
        final String query3 = "SELECT srs_id FROM gpkg_spatial_ref_sys WHERE srs_id = 4326 AND LOWER(organization) = 'epsg' AND organization_coordsys_id = 4326;";

        try (Connection con             = getConnection();
             Statement stmt             = con.createStatement();
             ResultSet srsdefaultvalues = stmt.executeQuery(query);

             Statement stmt2 = con.createStatement();
             ResultSet srsdefaultvalue2 = stmt2.executeQuery(query2);

             Statement stmt3 = con.createStatement();
             ResultSet srsdefaultvalue3 = stmt3.executeQuery(query3);)
        {
            // make sure the result sets are not empty
            assertTrue(String.format("The gpkg_spatial_ref_sys does not the default values needed for this query: %s", query), srsdefaultvalues.next());

            assertTrue(String.format("The gpkg_spatial_ref_sys does not the default values needed for this query: %s", query2), srsdefaultvalue2.next());

            assertTrue(String.format("The gpkg_spatial_ref_sys does not the default values needed for this query: %s", query3), srsdefaultvalue3.next());
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     * Verify that the spatial_ref_sys table contains rows to define all srs_id
     * values used by features and tiles in a GeoPackage.
     */
    @Test
    public void Req12()
    {
        final String query = "SELECT DISTINCT gc.srs_id AS gc_srid, srs.srs_name, srs.srs_id, srs.organization, srs.organization_coordsys_id," + " srs.definition FROM gpkg_contents AS gc LEFT OUTER JOIN gpkg_spatial_ref_sys AS srs ON srs.srs_id = gc.srs_id;";

        try (Connection con       = getConnection();
             Statement stmt       = con.createStatement();
             ResultSet srsdefined = stmt.executeQuery(query);)
        {

            while (srsdefined.next())
            {
                final String srsgc  = srsdefined.getString("gc_srid");
                final String srssrs = srsdefined.getString("srs_id");

                if (srsgc == null || srssrs == null)
                {
                    fail(String.format("Not all srs_id's being used in a GeoPackage are defined. Contents srs_id: %s Spatial Reference System srs_id: %s", srsgc, srssrs));
                }
            }

        }
        catch (final SQLException ex)
        {
            fail(ex.getMessage());
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify that the gpkg_contents table exists and has the correct
     * definition.
     */
    @Test
    public void Req13()
    {
        final String query = "SELECT sql FROM sqlite_master WHERE type = 'table' AND tbl_name = 'gpkg_contents';";

        try (Connection con           = getConnection();
             Statement  stmt          = con.createStatement();
             ResultSet  gpkg_contents = stmt.executeQuery(query);

             Statement  stmt2        = con.createStatement();
             ResultSet  uniqueinfo   = stmt2.executeQuery("PRAGMA index_list(gpkg_contents);");

             Statement  stmt3        = con.createStatement();
             ResultSet  fkinfo       = stmt3.executeQuery("PRAGMA foreign_key_list(gpkg_contents);");

             Statement  stmt4        = con.createStatement();
             ResultSet  table_info   = stmt4.executeQuery("PRAGMA table_info(gpkg_contents);");)
        {

            final String sql = gpkg_contents.getString("sql");
            assertTrue("The sql field must not be empty. Must include the gpkg_spatial_ref_sys Table SQL Definition.", sql != null);

            boolean uniqueidentifier = false;

            while (uniqueinfo.next() && uniqueidentifier != true)
            {
                final String name = uniqueinfo.getString("name");

                try (Statement stmt2name = con.createStatement();
                     ResultSet unique    = stmt2name.executeQuery(String.format("PRAGMA index_info(%s);", name));)
                {

                    final String identifier = unique.getString("name");

                    if (identifier.equals("identifier"))
                    {
                        uniqueidentifier = true;
                    }

                }
                catch (final Exception e)
                {
                    fail(e.getMessage());
                }
            }
            assertTrue("The column named identifier must be set to unique", uniqueidentifier);
            // check to see if the correct foreign key constraints are placed

            boolean fksrsid = false;
            while (fkinfo.next() && fksrsid != true)
            {
                final String table = fkinfo.getString("table");
                final String from  = fkinfo.getString("from");
                final String to    = fkinfo.getString("to");

                if (table.equals("gpkg_spatial_ref_sys") && from.equals("srs_id") && to.equals("srs_id"))
                {
                    fksrsid = true;
                }
            }
            assertTrue("The column srs_id in gpkg_contents must have a foreign key constraint referencing the gpkg_spatial_ref_sys srs_id", fksrsid);

            // get table info
            // check the data type, null value, default values, and primary key
            // for
            // each column in gpkg_contents
            final Map<String, Boolean> columns = new HashMap<>();
            while (table_info.next())
            {
                final String namecolumn    = table_info.getString("name");
                final String typecolumn    = table_info.getString("type");
                final String notnullcolumn = table_info.getString("notnull");
                final String dflt_value    = table_info.getString("dflt_value");
                final String pk            = table_info.getString("pk");

                final boolean colValid = checkContentsTable(namecolumn, typecolumn, notnullcolumn, dflt_value, pk);
                assertTrue(String.format("The column: %s is defined incorrectly by the GeoPackage requirements for the table: " + "gpkg_contents", namecolumn), colValid);
                columns.put(namecolumn, colValid);

            }
            // Make sure the required fields exist in the table
            final String[] requiredCols = new String[]
            { "table_name", "data_type", "identifier", "last_change", "min_x", "min_y", "max_x", "max_y", "srs_id" };

            for (final String requiredCol : requiredCols)
            {
                assertTrue(String.format("The required column: %s is not in the gpkg_contents table.", requiredCol), columns.containsKey(requiredCol));
            }
        }
        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }


    /**
     * Verify that the table_name column values in the gpkg_contents table are
     * valid.
     */
    @Test
    public void Req14()
    {
        final String query = "SELECT DISTINCT gc.table_name AS gc_table, sm.tbl_name FROM gpkg_contents AS gc LEFT OUTER JOIN sqlite_master AS sm ON gc.table_name = sm.tbl_name;";

        try (Connection con         = getConnection();
             Statement  stmt        = con.createStatement();
             ResultSet  gctablename = stmt.executeQuery(query);)
        {
            // check runtime options (foreign keys)
            while (gctablename.next())
            {
                final String gctable  = gctablename.getString("gc_table");
                final String tbl_name = gctablename.getString("tbl_name");

                assertTrue(String.format("The table_name value in gpkg_contents table is invalid for the table: %s", tbl_name), gctable != null);
            }
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     *Verify that the gpkg_contents table has the correct foreign key constraints
     */
    @Test
    public void Req14ForeignKeyConstraints()
    {
        final String query                   = "PRAGMA foreign_key_list(gpkg_contents);";
        try(Connection  con            = getConnection();
            Statement   stmt           = con.createStatement();
            ResultSet   gpkgContentsFK = stmt.executeQuery(query))
        {
            if(!gpkgContentsFK.next())
            {
                fail("Tile Matrix Table does not have a Foreign Key constraint enabled on the column table_name to referenced the column table_name in gpkg_contents.");
            }

            final String refTable = gpkgContentsFK.getString("table");
            final String from     = gpkgContentsFK.getString("from");
            final String to       = gpkgContentsFK.getString("to");

            final boolean goodFKConstraint = (refTable.equals("gpkg_spatial_ref_sys") && from.equals("srs_id") && to.equals("srs_id"));

            assertTrue("The gpkg_contents Table does not have a Foreign Key constraint enabled on the column srs_id to referenced the column srs_id in gpkg_spatial_ref_sys.", goodFKConstraint);
        }
        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }

    }


    /**
     * Verify that the gpkg_contents table last_change column values are in ISO
     * 8601 format containing a complete date plus UTC hours, minutes,
     * seconds and a decimal fraction of a second, with a "Z" ("zulu") suffix
     * indicating UTC.
     */
    @Test
    public void Req15()
    {
        final String query = "SELECT last_change FROM gpkg_contents;";

        try (Connection con        = getConnection();
             Statement  stmt       = con.createStatement();
             ResultSet  lastchange = stmt.executeQuery(query);)
        {
            // check format of last_change column
            while (lastchange.next())
            {
                final String           data       = lastchange.getString("last_change");
                final String           formatdate = data;
                final SimpleDateFormat formatter  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");
                try
                {
                    formatter.parse(formatdate);
                }
                catch (final ParseException ex)
                {
                    final SimpleDateFormat formatter2  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                    try
                    {
                        formatter2.parse(data);
                    }
                    catch (final Exception e)
                    {
                        fail("A field in the last_change column in gpkg_contents table was not in the correct format. " + e.getMessage());
                    }
                }
            }
        }
        catch (final SQLException ex)
        {
            fail(ex.getMessage());
        }
    }


    /**
     * Verify that the gpkg_contents table srs_id column values reference
     * gpkg_spatial_ref_sys srs_id column values.
     */
    @Test
    public void Req16()
    {
        final String query = "PRAGMA foreign_key_check('gpkg_contents');";
        try (Connection con        = getConnection();
             Statement  stmt       = con.createStatement();
             ResultSet  foreignKey = stmt.executeQuery(query);)
        {
            // check runtime options (foreign keys)
                assertTrue("There are violations on the foreign keys in the table gpkg_contents",!foreignKey.next());
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }



    private static boolean checkContentsTable(final String name, final String type, final String notnull, final String dflt_value, final String pk)
    {
        switch (name)
        {
            case "table_name":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("1"));
            case "data_type":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("0"));
            case "identifier":
                return (type.equals("TEXT") && notnull.equals("0") && pk.equals("0"));
            case "description":
                return (type.equals("TEXT") && notnull.equals("0") && (dflt_value.equals("''") || dflt_value.equals("\"\"")) && pk.equals("0"));
            case "last_change":
                return (type.equals("DATETIME") && notnull.equals("1") && pk.equals("0") && dflt_value.equals("strftime('%Y-%m-%dT%H:%M:%fZ','now')"));
            case "min_x":
            case "min_y":
            case "max_x":
            case "max_y":
                return (type.equals("DOUBLE") && notnull.equals("0") && pk.equals("0"));
            case "srs_id":
                return (type.equals("INTEGER") && notnull.equals("0") && pk.equals("0"));
            default:
                return true;
        }
    }

    public static boolean checkSrsTable(final String name, final String type, final String notnull, final String pk)
    {
        switch (name)
        {
            case "srs_name":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("0"));
            case "srs_id":
                return (type.equals("INTEGER") && notnull.equals("1") && pk.equals("1"));
            case "organization":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("0"));
            case "organization_coordsys_id":
                return (type.equals("INTEGER") && notnull.equals("1") && pk.equals("0"));
            case "definition":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("0"));
            case "description":
                return (type.equals("TEXT") && notnull.equals("0") && pk.equals("0"));
            default:
                return true;
        }
    }

    // checks if the string contains only correct column data types specified
    // by:
    // http://www.geopackage.org/spec/#table_column_data_types
    public static boolean checkDataType(final String pragma, final String table_name)
    {
        switch (pragma)
        {
            case "BOOLEAN":            case "TINYINT":            case "SMALLINT":            case "MEDIUMINT":
            case "INT":                case "FLOAT":              case "DOUBLE":              case "REAL":
            case "TEXT":               case "BLOB":               case "DATE":                case "DATETIME":
            case "GEOMETRY":           case "POINT":              case "LINESTRING":          case "POLYGON":
            case "MULTIPOINT":         case "MULTILINESTRING":   case "MULTIPOLYGON":        case "GEOMETRYCOLLECTION":
            case "CIRCULARSTRING":     case "COMPOUNDCURVE":      case "CURVEPOLYGON":        case "SURFACE":
            case "INTEGER":
                return true;
            default:
                if(pragma.matches("TEXT\\([0-9]+\\)") || pragma.matches("BLOB\\([0-9]+\\)"))
                {
                    return true;
                }
                fail("Invalid data type: " + pragma + " in table " + table_name);
                return false;
        }
    }

}