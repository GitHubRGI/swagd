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

package com.rgi.geopackage.test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class OptionsClassTiles
{
    public static final String filePath = "test.gpkg"; // jenTest.gpkg

    public static Connection getConnection() throws SQLException
    {
       return DriverManager.getConnection("jdbc:sqlite:" + filePath);
    }

    @BeforeClass
    public static void setUp() throws Exception
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver

        try (GeoPackage gpkg = new GeoPackage(new File(filePath)))
        {
            //add this so the default tables are inside for a tiles table.
            gpkg.tiles()
                .addTileSet("pyramid_table",
                            "identifier",
                            "description",
                            new BoundingBox(0.0,0.0,0.0,0.0),
                            gpkg.core().getSpatialReferenceSystem(4326));
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
     * Verify that the gpkg_contents table_name value table exists and is
     * apparently a tiles table for every row with a data_type column value of
     * tiles
     */
    @Test
    public void Req33()
    {

            final String query1 = "SELECT tbl_name FROM sqlite_master WHERE tbl_name NOT LIKE 'gpkg_%' AND type = 'table';";
            final String query2 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
            try(Connection con                   = getConnection();
                Statement stmt1                  = con.createStatement();
                ResultSet possiblePyramidTables  = stmt1.executeQuery(query1);

                Statement stmt2                  = con.createStatement();
                ResultSet contentsPyramidTables  = stmt2.executeQuery(query2))

                {
                final Map<String, Boolean> tableNamesContents = new HashMap<>();
                while(contentsPyramidTables.next())
                {
                    //fill the map with the pyramid table names of gpkg_contents
                    final String tableNameInContents = contentsPyramidTables.getString("table_name");
                    tableNamesContents.put(tableNameInContents, true);
                }
                while (possiblePyramidTables.next())
                {
                    //get the names that pass as a pyramid user data table from sqlite master and check that the table is referenced in
                    //the gpkg_contents table
                    final String tablenameSM = possiblePyramidTables.getString("tbl_name");
                    final boolean isPyramidTable = this.checkPyramidTableJustByName(tablenameSM);
                    if(isPyramidTable)
                    {
                        assertTrue((String.format("The Tile Pyramid User Data table that is not refrenced in gpkg_contents table is: %s.  "
                                + "This table needs to be referenced in the gpkg_contents table.", tablenameSM)),  tableNamesContents.containsKey(tablenameSM));
                    }
                }
             }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }

    }


    /**
     * Verify that by default zoom level pixel sizes for tile matrix user data
     * tables vary by factors of 2 between adjacent zoom levels in the tile
     * matrix metadata table.
     */
    @Test
    public void Req34()
    {

        final String query1 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles' ;";

        try (Connection con             = getConnection();
             Statement stmt             = con.createStatement();
             ResultSet pyramidtablename = stmt.executeQuery(query1))
        {
            while (pyramidtablename.next())
            {
                final String pyramidtable = pyramidtablename.getString("table_name");
                final String query2       = String.format("SELECT table_name, " +
                                                           "zoom_level, " +
                                                           "pixel_x_size, " +
                                                           "pixel_y_size " +
                                                    "FROM gpkg_tile_matrix " +
                                                    "WHERE table_name = '%s' ORDER BY zoom_level ASC;", pyramidtable);

                try (Statement stmt2     = con.createStatement();
                     ResultSet pixelinfo = stmt2.executeQuery(query2))
                {
                    final DecimalFormat df = new DecimalFormat("###.##");

                    Double pixelx2    = null;
                    Double pixely2    = null;
                    int zoomlevelnext = -1000;

                    while (pixelinfo.next())
                    {
                        final Double pixelx        = pixelinfo.getDouble("pixel_x_size");
                        final Double pixely        = pixelinfo.getDouble("pixel_y_size");
                        final int zoomlevelcurrent = pixelinfo.getInt("zoom_level");



                        if (pixelx2 != null && pixely2 != null && zoomlevelcurrent == zoomlevelnext + 1)
                        {
                            assertTrue(String.format("Pixel sizes for tile matrix user data tables do not vary by factors of 2" +
                                                     " between adjacent zoom levels in the tile matrix metadata table: %s, %s",
                                                     pixelx.toString(), pixely.toString()),
                                                     (Double.valueOf(df.format((pixelx2 / 2)))).equals(Double.valueOf(df.format((pixelx))))
                                                  && (Double.valueOf(df.format((pixely2 / 2)))).equals(Double.valueOf(df.format((pixely)))));

                            pixelx2 = pixelx;
                            pixely2 = pixely;
                        }
                        else if (pixelinfo.next())
                        {
                            pixelx2       =  pixelinfo.getDouble("pixel_x_size");
                            pixely2       =  pixelinfo.getDouble("pixel_y_size");
                            zoomlevelnext =  pixelinfo.getInt("zoom_level");

                            if (zoomlevelcurrent + 1 == zoomlevelnext)
                            {

                                assertTrue(String.format("Pixel sizes for tile matrix user data tables do not vary by factors of 2 " +
                                                         "between adjacent zoom levels in the tile matrix metadata table: %s, %s.  For zoom levels %d and %d",
                                                         pixelx2.toString(), pixely2.toString(), zoomlevelcurrent, zoomlevelnext),
                                                         (Double.valueOf(df.format((pixelx / 2)))).equals(Double.valueOf(df.format((pixelx2))))
                                                      && (Double.valueOf(df.format((pixely / 2)))).equals(Double.valueOf(df.format(pixely2))));
                            }
                        }
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
     * Req 35: Verify that a tile matrix user data table that contains tile data
     * that is not MIME type image/jpeg by default contains tile data in MIME
     * type image/png. Req 36: Verify that a tile matrix user data table that
     * contains tile data that is not MIME type image/png by default contains
     * tile data in MIME type image/jpeg.
     */
    @Test
    public void Req35AND36()
    {

        final String query1 = "SELECT table_name " + "FROM gpkg_contents " + "WHERE data_type = 'tiles' ;";

        try (Connection con = getConnection(); Statement stmt = con.createStatement(); ResultSet pyramidtablename = stmt.executeQuery(query1))
        {

            while (pyramidtablename.next())
            {

                final String pyramidtable = pyramidtablename.getString("table_name");
                final String selectTileDataQuery = String.format("SELECT tile_data FROM %s;", pyramidtable);

                try (Statement stmt2 = con.createStatement(); ResultSet tiledata = stmt2.executeQuery(selectTileDataQuery))
                {

                    while (tiledata.next())
                    {
                        final byte[] blob = tiledata.getBytes("tile_data");

                        try (MemoryCacheImageInputStream gpkgImage = new MemoryCacheImageInputStream(new ByteArrayInputStream(blob)))
                        {
                            final Iterator<ImageReader> iter = ImageIO.getImageReaders(gpkgImage);

                            while (iter.hasNext())
                            {
                                final ImageReader ir = iter.next();
                                final String imageType = ir.getFormatName();
                                assertTrue(String.format("The Tile matrix user data table that contains tile data that is not MIME type image/jpeg by default " +
                                                "does not contain tile data in MIME type image/png. Bad Image Type Detected: %s", imageType), imageType.toLowerCase().equals("png") || imageType.toLowerCase().equals("jpeg"));
                            }
                        }

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
     * Verify that the gpkg_tile_matrix_set table exists and has the correct
     * definition.
     */
    @Test
    public void Req37()
    {


        final String query1 = "SELECT sql FROM sqlite_master WHERE type = 'table' AND tbl_name = 'gpkg_tile_matrix_set';";

        try(Connection con             = getConnection();
            Statement stmt             = con.createStatement();
            ResultSet gpkg_tms         = stmt.executeQuery(query1);

            Statement stmt2            = con.createStatement();
            ResultSet table_info       = stmt2.executeQuery("PRAGMA table_info(gpkg_tile_matrix_set);");)
        {

            while (gpkg_tms.next())
            {
                final String sql = gpkg_tms.getString("sql");
                assertTrue("The sql field must not be empty. "+
                           "Must include the gpkg_tile_matrix_set Table SQL Definition.", sql != null);
            }


            final Map<String, Boolean> columns = new HashMap<>();
            while (table_info.next())
            {
                final String namecolumn    = table_info.getString("name");
                final String typecolumn    = table_info.getString("type");
                final String notnullcolumn = table_info.getString("notnull");
                final String pk            = table_info.getString("pk");

                final boolean colValid     = checktmsTable(namecolumn, typecolumn, notnullcolumn, pk);
                assertTrue(colValid);

                columns.put(namecolumn, colValid);
            }

            // Make sure the required fields exist in the table
            final String[] requiredCols = new String[]
                    { "table_name", "srs_id",   "min_x",   "max_x",   "min_y",  "max_y" };

                for (final String requiredCol : requiredCols)
                {
                    assertTrue(String.format("The column: %s  in table gpkg_spatial_ref_sys is not valid", requiredCol), columns.containsKey(requiredCol));
                }
        }

        catch (final Exception e)
        {
            fail(e.getMessage());
        }

    }

    /**
     * Verify that the gpkg_tile_matrix_set has the correct foreign key constraints
     */
    @Test
    public void req37ForeignKeyConstraint()
    {
        final String query             = "PRAGMA foreign_key_list(gpkg_tile_matrix_set);";
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
            if(refTable.equals("gpkg_spatial_ref_sys"))
            {
            final boolean goodFKConstraint = ( from.equals("srs_id") && to.equals("srs_id"));
            assertTrue("The gpkg_tile_matrix_set Table does not have a Foreign Key constraint enabled on the column srs_id to referenced the column srs_id in gpkg_spatial_ref_sys.", goodFKConstraint);
            }
            else
            {
                final boolean goodFKConstraint2 = (refTable.equals("gpkg_contents") && from.equals("table_name") && to.equals("table_name"));
                assertTrue("The gpkg_tile_matrix_set Table does not have a Foreign Key constraint enabled on the column table_name to referenced the column table_name in gpkg_contents Table.", goodFKConstraint2);
            }
        }
        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify that values of the gpkg_tile_matrix_set table_name column
     * reference values in the gpkg_contents table_name column for rows with a
     * data type of tiles.
     */
    @Test
    public void Req38()
    {

        final String query1 = "SELECT table_name FROM gpkg_tile_matrix_set;";
        final String query2 = String.format("SELECT table_name FROM gpkg_tile_matrix_set AS tms WHERE  table_name NOT IN  " +
                                           "(SELECT table_name FROM gpkg_contents        AS gc  WHERE  tms.table_name = gc.table_name " +
                                                                                               "AND  gc.data_type = 'tiles' )");

            try (Connection con             = getConnection();
                 Statement stmt1            = con.createStatement();
                 ResultSet pyramidTableName = stmt1.executeQuery(query1))
            {
                if (pyramidTableName.next())
                {
                    try (Statement stmt2              = con.createStatement();
                         ResultSet unreferencedtables = stmt2.executeQuery(query2))
                    {

                        if (unreferencedtables.next())
                        {
                            fail(String.format("There are Pyramid user data tables in gpkg_tile_matrix_set table_name field such that the table_name does not " +
                                               "reference values in the gpkg_contents table_name column for rows with a data type of �tiles�.  " +
                                               "Unreferenced table: %s", unreferencedtables.getString("table_name")));
                        }
                        else
                        {
                            assertTrue(true);
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
     * Verify that the gpkg_tile_matrix_set table contains a row record for each
     * tile pyramid user data table .(this test assumes test req33 has passed, otherwise this test is invalid)
     * this testwill pass sometimes if it is actually a fail bc it assumes req 33 passed (but if req 33 passes and then this will
     * be clear if it fails or not) if req33 passed then all the Pyramid user data tables are being referenced
     * in the gpkg_contents table ergo we can check if gpkg_tile_matrix_set references all pyramid user data tables
     * by using the names of the pyramid user data tables in the gpkg_contents table
     */
    @Test
    public void Req39()
    {

        final String query1 = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles'";

        try (Connection con             = getConnection();
             Statement stmt             = con.createStatement();
             ResultSet pyramidtablename = stmt.executeQuery(query1))
        {

            while (pyramidtablename.next())
            {

                final String pyramidname = pyramidtablename.getString("table_name");
                final String query2      = String.format("SELECT table_name " +
                                                   "FROM gpkg_tile_matrix_set " +
                                                   "WHERE table_name ='%s';", pyramidname);

                try (Statement stmt2           = con.createStatement();
                     ResultSet referencedtable = stmt2.executeQuery(query2))
                {

                    if (!referencedtable.next())
                    {
                        fail(String.format("The gpkg_tile_matrix_set table does not contains a row record " +
                                           "for the tile pyramid user data table: %s", pyramidname));
                    }
                    else
                    {
                        assertTrue(true);
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
     * Verify that the gpkg_tile_matrix_set table srs_id column values reference
     * gpkg_spatial_ref_sys srs_id column values.
     */
    @Test
    public void Req40()
    {

        final String query1 = "SELECT srs_id from gpkg_tile_matrix_set AS tms " +
                               "WHERE srs_id NOT IN" +
                                             "(SELECT srs_id " +
                                              "FROM gpkg_spatial_ref_sys);";

        try (Connection con            = getConnection();
             Statement stmt            = con.createStatement();
             ResultSet unreferencedsrs = stmt.executeQuery(query1))
        {

            if (unreferencedsrs.next())
            {
                fail(String.format("The gpkg_tile_matrix_set table contains a reference to an srs_id that is not defined in the gpkg_spatial_ref_sys Table. " +
                                   "Unreferenced srs_id: %s", unreferencedsrs.getInt("srs_id")));
            }
            else
            {
                assertTrue(true);
            }
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify that the gpkg_tile_matrix table exists and has the correct
     * definition.
     */
    @Test
    public void Req41()
    {


        final String query1 = "SELECT sql " +
                        "FROM sqlite_master " +
                        "WHERE type = 'table' AND tbl_name = 'gpkg_tile_matrix';";

        try(Connection con       = getConnection();
            Statement stmt       = con.createStatement();
            ResultSet gpkg_tm    = stmt.executeQuery(query1);

            Statement stmt2      = con.createStatement();
            ResultSet table_info = stmt2.executeQuery("PRAGMA table_info(gpkg_tile_matrix);"))

        {

            while (gpkg_tm.next())
            {
                final String sql = gpkg_tm.getString("sql");
                assertTrue("The sql field must not be empty.  " +
                           "Must include the gpkg_tile_matrix Table SQL Definition.",
                           sql != null);
            }

            final Map<String, Boolean> columns = new HashMap<>();

            while (table_info.next())
            {
                final String namecolumn    = table_info.getString("name");
                final String typecolumn    = table_info.getString("type");
                final String notnullcolumn = table_info.getString("notnull");
                final String pk            = table_info.getString("pk");

                final boolean colValid = checktmTable(namecolumn, typecolumn, notnullcolumn, pk);

                assertTrue(colValid);

                columns.put(namecolumn, colValid);
            }

            // Make sure the required fields exist in the table
            final String[] requiredCols = new String[] { "table_name",  "zoom_level", "matrix_width",  "matrix_height",
                                                   "tile_width",  "tile_height", "pixel_x_size", "pixel_y_size" };

            for (final String requiredCol : requiredCols)
            {
                assertTrue(String.format("The column: %s  in table gpkg_tile_matrix is not valid", requiredCol), columns.containsKey(requiredCol));
            }
        }

        catch (final Exception e)
        {
            fail(e.getMessage());
        }
    }

    /**
     * Verifies that foreign key constraints are enforced in gpkg_tile_matrix table
     * according to the contents of Annex C Table 29
     */
    @Test
    public void Req41ForeignKeyTest()
    {
        final String query                = "PRAGMA foreign_key_list(gpkg_tile_matrix);";
        try(Connection con          = getConnection();
            Statement stmt          = con.createStatement();
            ResultSet gpkg_tm_fk    = stmt.executeQuery(query))
        {
            if(!gpkg_tm_fk.next())
            {
                fail("Tile Matrix Table does not have a Foreign Key constraint enabled on the column table_name to referenced the column table_name in gpkg_contents.");
            }

            final String refTable = gpkg_tm_fk.getString("table");
            final String from     = gpkg_tm_fk.getString("from");
            final String to       = gpkg_tm_fk.getString("to");

            final boolean goodFKConstraint = (refTable.equals("gpkg_contents") && from.equals("table_name") && to.equals("table_name"));

            assertTrue("Tile Matrix Table does not have a Foreign Key constraint enabled on the column table_name to referenced the column table_name in gpkg_contents.", goodFKConstraint);
        }
        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify that values of the gpkg_tile_matrix table_name column reference
     * values in the gpkg_contents table_name column for rows with a data type
     * of tiles.(this test assumes test req33 has passed, otherwise this test is could pass even if it shouldn't.
     *   but if test req 33 passes this will fail if it should or pass if it should)
     */
    @Test
    public void Req42()
    {

        final String query1 =              "SELECT table_name FROM gpkg_tile_matrix;";

        final String query2 = String.format("SELECT table_name FROM gpkg_tile_matrix AS tm " +
                                      "WHERE table_name NOT IN" +
                                                               "(SELECT table_name  " +
                                                               "FROM gpkg_contents AS gc " +
                                                               "WHERE tm.table_name = gc.table_name AND gc.data_type = 'tiles');");

        try(Connection con               = getConnection();
            Statement stmt               = con.createStatement();
            ResultSet pyramidtablename   = stmt.executeQuery(query1);

            Statement stmt2              = con.createStatement();
            ResultSet unreferencedtables = stmt2.executeQuery(query2);)
        {

            if (pyramidtablename.next())
            {

                if (unreferencedtables.next())
                {
                    fail(String.format("There are Pyramid user data tables in gpkg_tile_matrix table_name field such that the table_name does not" +
                                       " reference values in the gpkg_contents table_name column for rows with a data type of �tiles�." +
                                       " Unreferenced table: %s", unreferencedtables.getString("table_name")));
                }
                else
                {
                    assertTrue(true);
                }
            }
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify that the gpkg_tile_matrix table contains a row record for each
     * zoom level that contains one or more tiles in each tile pyramid user data
     * table.
     */
    @Test
    public void Req43()
    {

        final String query1 = "SELECT table_name FROM gpkg_contents WHERE data_type = \"tiles\";";

        try (Connection con             = getConnection();
             Statement stmt             = con.createStatement();
             ResultSet pyramidtablename = stmt.executeQuery(query1);)
        {

            while (pyramidtablename.next())
            {
                final String pyramidname = pyramidtablename.getString("table_name");
                final String query2      = String.format("SELECT DISTINCT gtmm.zoom_level AS gtmm_zoom, udt.zoom_level AS udtt_zoom " +
                                                   "FROM gpkg_tile_matrix AS gtmm LEFT OUTER JOIN %s AS udt " +
                                                   "ON udt.zoom_level = gtmm.zoom_level AND gtmm.table_name = '%s';", pyramidname, pyramidname);

                try (Statement stmt2                     = con.createStatement();
                     ResultSet gm_and_pyramid_zoomlevels = stmt2.executeQuery(query2))
                {
                    while (gm_and_pyramid_zoomlevels.next())
                    {
                        int gmzoom = -1;
                            gmzoom = gm_and_pyramid_zoomlevels.getInt("gtmm_zoom");
                        final int pyzoom = gm_and_pyramid_zoomlevels.getInt("udtt_zoom");

                            if (gmzoom == -1)
                            {
                                fail(String.format("The gpkg_tile_matrix table does not contains a row record for the zoom level: %d " +
                                                   "that contains one or more tiles the tile pyramid user data table: %s", pyzoom, pyramidname));
                            }
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
     * Verify that zoom level column values in the gpkg_tile_matrix table are
     * not negative.
     */
    @Test
    public void Req44()
    {
        final String query1 = "SELECT zoom_level FROM gpkg_tile_matrix;";

        try (Connection con      = getConnection();
             Statement stmt      = con.createStatement();
             ResultSet zoomlevel = stmt.executeQuery(query1))
        {

            if (zoomlevel.next())
            {

                final String query2 = String.format("SELECT min(zoom_level) FROM gpkg_tile_matrix;");

                try (Statement stmt2   = con.createStatement();
                     ResultSet minzoom = stmt2.executeQuery(query2);)
                {

                    final int testzoom = minzoom.getInt("min(zoom_level)");
                    if (testzoom < 0)
                    {
                        fail(String.format("The zoom_level in gpkg_tile_matrix must be greater than 0. Bad zoom_level: %d", testzoom));
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
     * Verify that the matrix_width values in the gpkg_tile_matrix table are valid.
     */
    @Test
    public void Req45()
    {


        final String query1 = "SELECT matrix_width FROM gpkg_tile_matrix;";

        try (Connection con        = getConnection();
             Statement stmt        = con.createStatement();
             ResultSet matrixWidth = stmt.executeQuery(query1))
        {

            if (matrixWidth.next())
            {

                final String query2 = "SELECT min(matrix_width) FROM gpkg_tile_matrix;";

                try (Statement stmt2          = con.createStatement();
                     ResultSet minMatrixWidth = stmt2.executeQuery(query2);)
                {

                    final int testMinMatrixWidth = minMatrixWidth.getInt("min(matrix_width)");
                    if (testMinMatrixWidth < 1)
                    {
                        fail(String.format("The matrix_width in gpkg_tile_matrix must be greater than 1. Bad matrix_width: %d", testMinMatrixWidth));
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
     * Verify that the matrix_height values in the gpkg_tile_matrix table are valid.
     */
    @Test
    public void Req46()
    {

        final String query1 = "SELECT matrix_height FROM gpkg_tile_matrix;";

        try (Connection con          = getConnection();
             Statement  stmt         = con.createStatement();
             ResultSet  matrixHeight = stmt.executeQuery(query1))
        {

            if (matrixHeight.next())
            {

                final String query2 = "SELECT min(matrix_height) FROM gpkg_tile_matrix;";

                try (Statement stmt2           = con.createStatement();
                     ResultSet minMatrixHeight = stmt2.executeQuery(query2);)
                {

                    final int testMinMatrixHeight = minMatrixHeight.getInt("min(matrix_height)");
                    if (testMinMatrixHeight < 1)
                    {
                        fail(String.format("The matrix_height in gpkg_tile_matrix must be greater than 1. Bad matrix_height: %d", testMinMatrixHeight));
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
     * Verify that the tile_width values in the gpkg_tile_matrix table are valid.
     */
    @Test
    public void Req47()
    {
        final String query1 = "SELECT tile_width FROM gpkg_tile_matrix;";

        try (Connection con          = getConnection();
             Statement  stmt         = con.createStatement();
             ResultSet  tileWidth    = stmt.executeQuery(query1))
        {

            if (tileWidth.next())
            {

                final String query2 = "SELECT min(tile_width) FROM gpkg_tile_matrix;";

                try (Statement stmt2        = con.createStatement();
                     ResultSet minTileWidth = stmt2.executeQuery(query2);)
                {

                    final int testMinTileWidth = minTileWidth.getInt("min(tile_width)");
                    if (testMinTileWidth < 1)
                    {
                        fail(String.format("The tile_width in gpkg_tile_matrix must be greater than 1. Bad tile_width: %d", testMinTileWidth));
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
     *Verify that the tile_height values in the gpkg_tile_matrix table are valid.
     */
    @Test
    public void Req48()
    {
        final String query1 = "SELECT tile_height FROM gpkg_tile_matrix;";

        try (Connection con          = getConnection();
             Statement  stmt         = con.createStatement();
             ResultSet  tileHeight   = stmt.executeQuery(query1))
        {

            if (tileHeight.next())
            {

                final String query2 = "SELECT min(tile_height) FROM gpkg_tile_matrix;";

                try (Statement stmt2         = con.createStatement();
                     ResultSet minTileHeight = stmt2.executeQuery(query2);)
                {

                    final int testMinTileHeight = minTileHeight.getInt("min(tile_height)");
                    if (testMinTileHeight < 1)
                    {
                        fail(String.format("The tile_height in gpkg_tile_matrix must be greater than 1. Bad tile_height: %d", testMinTileHeight));
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
     * Verify that the pixel_x_size values in the gpkg_tile_matrix table are valid
     */
    @Test
    public void Req49()
    {
        final String query1 = "SELECT pixel_x_size FROM gpkg_tile_matrix;";

        try (Connection con          = getConnection();
             Statement  stmt         = con.createStatement();
             ResultSet  pixelXSize   = stmt.executeQuery(query1))
        {

            if (pixelXSize.next())
            {

                final String query2 = "SELECT min(pixel_x_size) FROM gpkg_tile_matrix;";

                try (Statement stmt2         = con.createStatement();
                     ResultSet minPixelXSize = stmt2.executeQuery(query2);)
                {

                    final int testMinPixelXSize = minPixelXSize.getInt("min(pixel_x_size)");
                    if (testMinPixelXSize < 0)
                    {
                        fail(String.format("The pixel_x_size in gpkg_tile_matrix must be greater than 0. Bad pixel_x_size: %d", testMinPixelXSize));
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
     * Verify that the pixel_y_size values in the gpkg_tile_matrix table are valid.
     */
    @Test
    public void Req50()
    {
        final String query1 = "SELECT pixel_y_size FROM gpkg_tile_matrix;";

        try (Connection con          = getConnection();
             Statement  stmt         = con.createStatement();
             ResultSet  pixelYSize   = stmt.executeQuery(query1))
        {

            if (pixelYSize.next())
            {

                final String query2 = "SELECT min(pixel_y_size) FROM gpkg_tile_matrix;";

                try (Statement stmt2         = con.createStatement();
                     ResultSet minPixelYSize = stmt2.executeQuery(query2);)
                {

                    final int testMinPixelYSize = minPixelYSize.getInt("min(pixel_y_size)");
                    if (testMinPixelYSize < 0)
                    {
                        fail(String.format("The pixel_y_size in gpkg_tile_matrix must be greater than 0. Bad pixel_y_size: %d", testMinPixelYSize));
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
     *Verify that the pixel_x_size and pixel_y_size column values for zoom level column values in a gpkg_tile_matrix table
     *sorted in ascending order are sorted in descending order, showing that lower zoom levels are zoomed out.
     */
    @Test
    public void Req51()
    {
        final String query1 = "SELECT table_name FROM gpkg_contents WHERE data_type = \"tiles\";";

        try (Connection con             = getConnection();
             Statement stmt             = con.createStatement();
             ResultSet pyramidTableName = stmt.executeQuery(query1))
                {

                    while (pyramidTableName.next())
                    {
                        final String pyramidTable = pyramidTableName.getString("table_name");
                        final String query2       = String.format("SELECT zoom_level, pixel_x_size, pixel_y_size from gpkg_tile_matrix WHERE table_name = '%s' ORDER BY zoom_level ASC;", pyramidTable);
                        Double pixelX2      = null;
                        Double pixelY2      = null;

                        try (Statement stmt2        = con.createStatement();
                             ResultSet zoomPixxPixy = stmt2.executeQuery(query2))
                                {
                                   while(zoomPixxPixy.next())
                                       {
                                           // Double zoomLevel = zoomPixxPixy.getDouble("zoom_level");
                                            final Double pixelX = zoomPixxPixy.getDouble("pixel_x_size");
                                            final Double pixelY = zoomPixxPixy.getDouble("pixel_y_size");

                                            if (pixelX2 != null && pixelY2 != null)
                                            {
                                                assertTrue(String.format("Pixel sizes for tile matrix user data tables do not increase while " +
                                                                         "the zoom level decrease. bad pixel_x_size, pixel_y_size: %s, %s",
                                                                         pixelX.toString(), pixelY.toString()), pixelX2 > pixelX && pixelY2 > pixelY);

                                                pixelX2 = pixelX;
                                                pixelY2 = pixelY;
                                            }
                                            else if (zoomPixxPixy.next())
                                            {
                                                pixelX2  = zoomPixxPixy.getDouble("pixel_x_size");
                                                pixelY2  = zoomPixxPixy.getDouble("pixel_y_size");

                                                    assertTrue(String.format("Zoom level pixel sizes for tile matrix user data tables do not vary by factors of 2 " +
                                                                             "between adjacent zoom levels in the tile matrix metadata table: %s, %s",
                                                                             pixelX2.toString(), pixelY2.toString()), pixelX > pixelX2 && pixelY > pixelY2);
                                            }
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
     * Verify that each pyramid user data table contains a record in the gpkg_tile_matrix_set.
     */
    @Test
    public void Req52()
    {
        final String query = "SELECT DISTINCT table_name FROM gpkg_contents WHERE data_type = 'tiles' AND table_name NOT IN(SELECT DISTINCT table_name FROM gpkg_tile_matrix_set);";

        try(Connection     con                           = getConnection();
            Statement      stmt                          = con.createStatement();
            ResultSet      unreferencedPyramidTableInTMS = stmt.executeQuery(query))
             {
               if(unreferencedPyramidTableInTMS.next())
            {
                fail(String.format("There are Pyramid User Data Tables that do not contain a record in the gpkg_tile_matrix_set."
                                   + " Unreferenced Pyramid table: %s", unreferencedPyramidTableInTMS.getString("table_name")));
            }
             }

        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }

    }

    /**
     * Make sure every Pyramid User Data Table has the correct table definition
     */
    @Test
    public void Req52PyramidCheck()
    {
        final String query = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";

        try(Connection con              = getConnection();
            Statement stmt              = con.createStatement();
            ResultSet sampleTilePyramid = stmt.executeQuery(query))
        {

                while (sampleTilePyramid.next())
                {
                    final String tablename = sampleTilePyramid.getString("table_name");
                    assertTrue(String.format("The Pyramid User data table: %s is not valid.  Check the SQL definition for the Pyramid User Data Table.", tablename),this.checkPyramidTableJustByName(tablename));

                }
        }
        catch (final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Verify for each field in the zoom_level column for the Tile Pyramid User Data table,
     * the zoom level cannot be bigger than the maximum zoom level in the Tile Matrix Table
     *  or less than the minimum zoom level in the Tile Matrix Table
     */
    @Test
    public void Req53()
    {
        final String query = "SELECT DISTINCT table_name FROM gpkg_tile_matrix;";

        try(Connection     con                   = getConnection();
            Statement      stmt                  = con.createStatement();
            ResultSet      pyramidTableNameInTM  = stmt.executeQuery(query))
        {
            while(pyramidTableNameInTM.next())
            {
                final String           pyramidName = pyramidTableNameInTM.getString("table_name");
                final String           query2      = String.format("SELECT zoom_level FROM %s;", pyramidName);

                try(Statement    stmt2       = con.createStatement();
                    ResultSet    pyramidZoom = stmt2.executeQuery(query2))
                {
                    if(pyramidZoom.next())
                    {
                        final String        query3     = String.format("SELECT MIN(zoom_level) AS min_gtm_zoom, MAX(zoom_level) "
                                                               + "AS max_gtm_zoom FROM gpkg_tile_matrix WHERE table_name = '%s'", pyramidName);

                        try(Statement stmt3      = con.createStatement();
                            ResultSet minMaxZoom = stmt3.executeQuery(query3)   )
                        {
                           final int      minZoom      = minMaxZoom.getInt("min_gtm_zoom");
                           final int      maxZoom      = minMaxZoom.getInt("max_gtm_zoom");
                           final String   query4       = String.format("SELECT id FROM %s WHERE zoom_level < %d OR zoom_level > %d", pyramidName, minZoom, maxZoom);

                           try(Statement stmt4        = con.createStatement();
                               ResultSet invalidZooms = stmt4.executeQuery(query4))
                               {
                               if(invalidZooms.next())
                            {
                                fail(String.format("There are zoom_levels in the Pyramid User Data Table: %s  such that the zoom level is bigger than the maximum zoom level: %d or smaller than the minimum zoom_level: %d"
                                                   + " that was determined by the gpkg_tile_matrix Table.  InValid zoom_level id: %d", pyramidName, maxZoom, minZoom, invalidZooms.getInt("id")));
                            }
                               }
                        }
                    }

                }
            }
        }
        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }

    }

    /**
     * Verify : In the Tile Pyramid User table each field in the tile_column column must
     * be greater than or equal to zero and no higher than the matrix's width (from the Tile Matrix Table) minus 1.
     * This is only when both the Tile Matrix Table zoom level field is equal to Tile Pyramid User Data Table zoom level field.
     */
    @Test
   public void Req54()
   {
      final String         query            = "SELECT DISTINCT table_name FROM gpkg_tile_matrix;";
      try(Connection con              = getConnection();
          Statement  stmt             = con.createStatement();
          ResultSet  pyramidTableName = stmt.executeQuery(query))
      {
          while(pyramidTableName.next())
          {
              final String pyramidName = pyramidTableName.getString("table_name");
              //this query will only pull the incorrect values for the pyramid user data table's column width, the value
              //of the tile_column value for the pyramid user data table SHOULD be null otherwise those fields are in violation
              //of the range
              final String query2      =  String.format("SELECT DISTINCT gtmm.zoom_level   AS gtmm_zoom, "
                                                            +     "gtmm.matrix_width AS gtmm_width,"
                                                            +     "udt.zoom_level    AS udt_zoom, "
                                                            +     "udt.tile_column   AS udt_column"
                                                            + " FROM gpkg_tile_matrix AS gtmm "
                                                + "LEFT OUTER JOIN %s AS udt ON"
                                                            +     " udt.zoom_level  = gtmm.zoom_level AND"
                                                            +     " gtmm.table_name = '%s'            AND"
                                                            +     " (udt_column < 0 OR udt_column > (gtmm_width - 1));",pyramidName, pyramidName);
             try(Statement stmt2             = con.createStatement();
                 ResultSet  incorrectColumns = stmt2.executeQuery(query2))
             {
                 while(incorrectColumns.next())
                 {
                     final int pyramidTileColumn = incorrectColumns.getInt("udt_column");
                     //if it doesn't equal 0 (meaning a null value) then that tile_column from the pyramid user data table is invalid
                     //any other value is beyond the range
                     if(pyramidTileColumn != 0)
                    {
                        fail(String.format("The Pyramid User Data table tile_column value must be greater than zero and less than or equal to the Tile Matrix's table's width minus 1,"
                                 + " when the zoom_level in the Tile Matrix Table equals the zoom_level in the Pyramid User Data Table. Invalid tile_column: %d  Pyramid Table: %s", pyramidTileColumn, pyramidName));
                    }
                 }

             }

          }
      }
      catch(final Exception ex)
      {
          fail(ex.getMessage());
      }
   }

    /**
     *Verify : For each field in the Tile Pyramid User table tile_row column, the tile_row must be
     *greater than or equal to zero and no bigger than the matrix's height (from the Tile Matrix Table) minus 1.
     *This is only when both the Tile Matrix Table zoom level field is equal to the Tile pyramid User Data Table zoom level field.
     */
    @Test
    public void Req55()
    {
        final String         query            = "SELECT DISTINCT table_name FROM gpkg_tile_matrix;";
        try(Connection con              = getConnection();
            Statement  stmt             = con.createStatement();
            ResultSet  pyramidTableName = stmt.executeQuery(query))
        {
            while(pyramidTableName.next())
            {
                final String pyramidName = pyramidTableName.getString("table_name");
                //this query will only pull the incorrect values for the pyramid user data table's column height, the value
                //of the tile_row value for the pyramid user data table SHOULD be null otherwise those fields are in violation
                //of the range
                final String query2 = String.format("SELECT DISTINCT gtmm.zoom_level AS gtmm_zoom, gtmm.matrix_height AS gtmm_height," +
                                           "udt.zoom_level AS udt_zoom, udt.tile_row AS udt_row FROM gpkg_tile_matrix AS gtmm "+
                                            "LEFT OUTER JOIN %s AS udt ON "+
                                            "udt.zoom_level = gtmm.zoom_level AND gtmm.table_name = '%s' AND (udt_row < 0 OR udt_row > (gtmm_height- 1));", pyramidName, pyramidName);

                try(Statement  stmt2            = con.createStatement();
                    ResultSet  incorrectTileRow = stmt2.executeQuery(query2))
                    {
                        while(incorrectTileRow.next())
                        {
                            final int pyramidTileRow = incorrectTileRow.getInt("udt_row");
                            //if it doesn't equal 0 (meaning a null value) then that tile_row from the pyramid user data table is invalid
                            //any other value is beyond the range
                            if(pyramidTileRow != 0)
                            {
                                fail(String.format("The Pyramid User Data table tile_row value must be greater than zero and less than or equal to the Tile Matrix's table's height minus 1,"
                                        + " only when the zoom_level in the Tile Matrix Table equals the zoom_level in the Pyramid User Data Table. Invalid Row: %d  Pyramid Table: %s", pyramidTileRow, pyramidName));
                            }
                        }

                    }

            }
        }
        catch(final Exception ex)
        {
            fail(ex.getMessage());
        }
    }

    /**
     * Checks to see if the width has exactly the correct number of tiles for that particular zoom levels
     * @throws SQLException
     */
    @Test
    public void matrixWidthCorrect() throws SQLException
    {
        final String sql = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
        try (Connection con = getConnection(); Statement pyramid = con.createStatement(); ResultSet pyramidTableName = pyramid.executeQuery(sql))
        {
            // for every pyramid user data table
            while (pyramidTableName.next())
            {
                // for each matrix_width
                final String query = "SELECT matrix_width FROM gpkg_tile_matrix;";
                try (Statement stmt = con.createStatement(); ResultSet matrixWidth = stmt.executeQuery(query);)
                {
                    while (matrixWidth.next())
                    {
                        final int matrixwidth = matrixWidth.getInt("matrix_width");
                        // find zoom level with this matrix width
                        final String zoomsql = String.format("SELECT DISTINCT (zoom_level) FROM gpkg_tile_matrix WHERE matrix_width = %d", matrixwidth);
                        try (Statement zoomstmt = con.createStatement(); ResultSet currentzoom = zoomstmt.executeQuery(zoomsql))
                        {
                            final int zoomlevel = currentzoom.getInt("zoom_level");
                            // count all the tiles with the current zoom level
                            // under this current matrix width
                            //Select count(tile_row) FROM (Select DISTINCT tile_row From tiles Where zoom_level =17)
                            final String query2 = String.format("SELECT COUNT (tile_row) FROM (SELECT DISTINCT tile_row FROM %s WHERE zoom_level = %d);", pyramidTableName.getString("table_name"), zoomlevel);

                            try (Statement stmt2 = con.createStatement(); ResultSet countRow = stmt.executeQuery(query2))
                            {
                                final int rowCount = countRow.getInt("COUNT (tile_row)");
                                // make sure the count and the matrix width are
                                // equal
                                assertTrue(String.format("Invalid number of tiles at current zoom level: %d. Matrix width %d  row Count: %d", zoomlevel, matrixwidth, rowCount), matrixwidth == rowCount);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Checks to see if the height has exactly the correct number of tiles for that particular zoom levels
     * @throws SQLException
     */
    @Test
    public void matrixHeightCorrect() throws SQLException
    {
        final String sql = "SELECT table_name FROM gpkg_contents WHERE data_type = 'tiles';";
        try (Connection con = getConnection(); Statement pyramid = con.createStatement(); ResultSet pyramidTableName = pyramid.executeQuery(sql))
        {
            // for every pyramid user data table
            while (pyramidTableName.next())
            {
                // for each matrix_width
                final String query = "SELECT matrix_height FROM gpkg_tile_matrix;";
                try (Statement stmt = con.createStatement(); ResultSet matrixHeightData = stmt.executeQuery(query);)
                {
                    while (matrixHeightData.next())
                    {
                        final int matrixHeight = matrixHeightData.getInt("matrix_height");
                        // find zoom level with this matrix width
                        final String zoomsql = String.format("SELECT DISTINCT (zoom_level) FROM gpkg_tile_matrix WHERE matrix_height = %d", matrixHeight);
                        try (Statement zoomstmt = con.createStatement(); ResultSet currentzoom = zoomstmt.executeQuery(zoomsql))
                        {
                            final int zoomlevel = currentzoom.getInt("zoom_level");
                            // count all the tiles with the current zoom level
                            // under this current matrix width
                            //Select count(tile_row) FROM (Select DISTINCT tile_row From tiles Where zoom_level =17)
                            final String query2 = String.format("SELECT COUNT (tile_column) FROM (SELECT DISTINCT tile_column FROM %s WHERE zoom_level = %d);",
                                                            pyramidTableName.getString("table_name"), zoomlevel);

                            try (Statement stmt2 = con.createStatement();
                                 ResultSet countColumn = stmt.executeQuery(query2))
                            {
                                final int columnCount = countColumn.getInt("COUNT (tile_column)");
                                // make sure the count and the matrix width are
                                // equal
                                assertTrue(String.format("Invalid number of tiles at current zoom level: %d. Matrix height: %d  column Count: %d",
                                                            zoomlevel, matrixHeight, columnCount), matrixHeight == columnCount);
                            }
                        }

                    }
                }
            }
        }
    }

    private static boolean checktmTable(final String name, final String type, final String notnull, final String pk)
    {

        switch (name)
        {
            case "table_name":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("1"));

            case "zoom_level":
                return (type.equals("INTEGER") && notnull.equals("1") && pk.equals("2"));

            case "matrix_width" :
            case "matrix_height":
            case "tile_width"   :
            case "tile_height"  :
                return (type.equals("INTEGER") && notnull.equals("1") && pk.equals("0"));

            case "pixel_x_size":
            case "pixel_y_size":
                return (type.equals("DOUBLE") && notnull.equals("1") && pk.equals("0"));

            default:
                return true;
        }

    }

    private static boolean checktmsTable(final String name, final String type, final String notnull, final String pk)
    {

        switch (name)
        {
            case "table_name":
                return (type.equals("TEXT") && notnull.equals("1") && pk.equals("1"));

            case "srs_id":

                return (type.equals("INTEGER") && notnull.equals("1") && pk.equals("0"));
            case "min_x":
            case "min_y":
            case "max_x":
            case "max_y":
                return (type.equals("DOUBLE") && notnull.equals("1") && pk.equals("0"));

            default:
                return true;
        }
    }

    private static boolean checkPyramidTableColumnsValidity(final String name, final String type, final String notNull, final String pk)
    {
        switch (name)
        {
            case "id":
                return type.equals("INTEGER") && notNull.equals("0") && pk.equals("1");

            case "zoom_level" :
            case "tile_column":
            case "tile_row"   :
                return type.equals("INTEGER") && notNull.equals("1") && pk.equals("0");

            case "tile_data":
                return type.equals("BLOB") && notNull.equals("1") && pk.equals("0");

            default:
                return true;
        }
    }
   private boolean  checkPyramidTableJustByName(final String tablename)
   {

           final Map<String, Boolean> columns = new HashMap<>();

           try (   Connection con      = getConnection();
                   Statement stmt4     = con.createStatement();
                   ResultSet tableInfo = stmt4.executeQuery(String.format("PRAGMA table_info(%s);", tablename)))
              {

                  while (tableInfo.next())
                  {
                      final String nameColumn    = tableInfo.getString("name");
                      final String typeColumn    = tableInfo.getString("type");
                      final String notNullColumn = tableInfo.getString("notnull");
                      final String pk            = tableInfo.getString("pk");

                      final Boolean colValid = checkPyramidTableColumnsValidity(nameColumn, typeColumn, notNullColumn, pk);
                      if (!colValid)
                    {
                        return false;
                    }
                      columns.put(nameColumn, colValid);
                  }
              }
           catch(final Exception ex)
           {
               fail(ex.getMessage());
           }

          if (columns.size() != 0)
          {

              final String[] requiredCols = new String[]
              { "zoom_level", "tile_column", "tile_row", "tile_data" };

              for (final String requiredCol : requiredCols)
              {
                  if(!columns.containsKey(requiredCol))
                {
                    return false;
                }

              }
          }
          else
          {
              return false;
          }

       return true;
    }
}
