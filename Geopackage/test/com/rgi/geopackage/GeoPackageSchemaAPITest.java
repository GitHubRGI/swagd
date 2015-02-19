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

package com.rgi.geopackage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.schema.DataColumn;
import com.rgi.geopackage.schema.DataColumnConstraint;
import com.rgi.geopackage.schema.Type;
import com.rgi.geopackage.verification.ConformanceException;
@SuppressWarnings({"javadoc"})
public class GeoPackageSchemaAPITest
{
    private final Random randomGenerator = new Random();

    /**
     * Tests if GeoPackage Schema will return
     * the expected values for the DataColumn
     * entry added to the GeoPackage
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test
    public void addDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(5);
        final String   columnName     = "columnName";
        final String   tableName      = "tableName";

        try(GeoPackage gpkg = createGeoPackage(tableName, columnName, testFile))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));


            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            final DataColumn dataColumnReturned = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);
            gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);//bug in getting data column

            assertTrue("GeoPackage Schema did not return the DataColumn expected.(using addDataColumn)",
                       dataColumnReturned.getColumnName()    .equals(columnName)             &&
                       dataColumnReturned.getName()          .equals(name)                   &&
                       dataColumnReturned.getTitle()         .equals(title)                  &&
                       dataColumnReturned.getDescription()   .equals(description)            &&
                       dataColumnReturned.getMimeType()      .equals(mimeType.toString())    &&
                       dataColumnReturned.getConstraintName().equals(constraintName));


        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with a null value for tableName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String   columnName     = "columnName";
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            gpkg.schema().addDataColumn(null, columnName, name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and tableName is null.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with a null value for columnName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            gpkg.schema().addDataColumn(table, null, name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and columnName is null.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if getDataColumn method in
     * geopackage schema returns
     * the expected values.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test
    public void getDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = createGeoPackage("tableName", "columnName", testFile))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));

            final String   columnName     = "columnName";
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            final DataColumn columnExpected = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);

            final DataColumn columnReturned = gpkg.schema().getDataColumn(table, columnName);
            final DataColumn shouldBeNull   = gpkg.schema().getDataColumn(table, "ColumnThatdoesn'tExist");
            assertTrue("The GeoPackage schema did not return the expected values when using the getDataColumn method",
                       dataColumnsEqual(columnExpected, columnReturned) &&
                       shouldBeNull == null);

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if geopackage schema
     * will throw an IllegalArgumentException when
     * trying to get a data column entry with tablename
     * that has a value of null.
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(14);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           gpkg.schema().getDataColumn(null,"columnName");
           fail("Expected GeoPackage Schema to throw an IllegalArgumentException when trying to get a data column and table is null.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema will throw an Illegal
     * argumentException when trying to get a data Column
     * with a null value for column
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, null);
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema will throw an Illegal
     * argumentException when trying to get a data Column
     * with an empty string for column
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, "");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if geopackage schema returns the
     * data column entries expected
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test
    public void getDataColumnCollection() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(10);

        final String tableName = "tableName";
        final String columnName = "columnName";

        try(GeoPackage gpkg = createGeoPackage(tableName, columnName, testFile))
        {
            final Collection<DataColumn> shouldBeEmpty = gpkg.schema().getDataColumns();

            assertTrue("Returned a non empty collection when there were no DataColumn entries in the geopackage with getDataColumn method.",
                       shouldBeEmpty.isEmpty());

            final Content table = gpkg.core().addContent(tableName,
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,90.0,180.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));

            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            final DataColumn column = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);

            final String   columnName2     = "last_column";
            final String   name2           = "name2";
            final String   title2          = "title2";
            final String   description2    = "description2";
            final MimeType mimeType2       = new MimeType("image/jpeg");
            final String   constraintName2 = "constraint_name2";

            final DataColumn column2 = gpkg.schema().addDataColumn(table, columnName2, name2, title2, description2, mimeType2, constraintName2);

            final List<DataColumn> columnsExpected = Arrays.asList(column, column2);

            final Collection<DataColumn> columnsReturned = gpkg.schema().getDataColumns();

            assertTrue("GeoPackage Schema did not returned the expected DataColumn entries from the geopackage when using getDataColumn() method",
                       columnsReturned.size() == 2 &&
                       columnsReturned.stream().allMatch(returned-> columnsExpected.stream().anyMatch(expected -> dataColumnsEqual(expected, returned))));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if geopackage addDataColumnConstraint
     * returns the expected values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void addDataColumnConstraint() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(11);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String  constraintName      = "constraint_name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            final DataColumnConstraint constraint = gpkg.schema().addDataColumnConstraint(constraintName,
                                                                                    constraintType,
                                                                                    value,
                                                                                    minimum,
                                                                                    minimumIsInclusive,
                                                                                    maximum,
                                                                                    maximumIsInclusive,
                                                                                    description);
            gpkg.schema().addDataColumnConstraint(constraintName,
                                                  constraintType,
                                                  value,
                                                  minimum,
                                                  minimumIsInclusive,
                                                  maximum,
                                                  maximumIsInclusive,
                                                  description);

            assertTrue("The Returned datacolumnconstraint using addDataColumnConstraint does not return the expected values",
                       constraint.getConstraintName().equals(constraintName)            &&
                       constraint.getConstraintType().equals(constraintType.toString()) &&
                       constraint.getValue() == null                                    &&
                       constraint.getMinimum().equals(minimum)                          &&
                       constraint.getMinimumIsInclusive().equals(minimumIsInclusive)    &&
                       constraint.getMaximum().equals(maximum)                          &&
                       constraint.getMaximumIsInclusive().equals(maximumIsInclusive));
        }
        finally
        {
            deleteFile(testFile);
        }
    }


    /**
     * Tests if Geopackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with a
     * null value for constraintName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(13);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = null;
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for constraint name");

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    @Test
    public void getDataColumnConstraint() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(11);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String  constraintName      = "constraint_name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = false;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = true;
            final String  description         = "description";

            final DataColumnConstraint constraintExpected = gpkg.schema().addDataColumnConstraint(constraintName,
                                                                                            constraintType,
                                                                                            value,
                                                                                            minimum,
                                                                                            minimumIsInclusive,
                                                                                            maximum,
                                                                                            maximumIsInclusive,
                                                                                            description);

            final DataColumnConstraint constraintReturned = gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            assertTrue("The data Column Constraint returned isn't the same as expected.",
                       dataColumnConstraintsEqual(constraintExpected, constraintReturned));

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    private static GeoPackage createGeoPackage(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.close();
            createTable(tableName, columnName, testFile);

            return new GeoPackage(testFile, false, OpenMode.Open);
        }
    }

    private static void createTable(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, SQLException
    {
        final String createTable = String.format("CREATE TABLE %s ( %s TEXT," +
                                                             "other_column INTEGER NOT NULL," +
                                                             "more_columns INTEGER NOT NULL," +
                                                             "last_Column TEXT NOT NULL)",
                                            tableName,
                                            columnName);
        try(Connection con = getConnection(testFile);
            Statement stmt = con.createStatement();)
        {
            stmt.execute(createTable);
        }
    }

    private static Connection getConnection(final File testFile) throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");   // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + testFile.getPath()); // Initialize the database connection
    }

    private static boolean dataColumnConstraintsEqual(final DataColumnConstraint expected, final DataColumnConstraint returned)
    {
        return isEqual(expected.getConstraintName(),     returned.getConstraintName())    &&
               isEqual(expected.getConstraintType(),     returned.getConstraintType())    &&
               isEqual(expected.getValue(),              returned.getValue())             &&
               isEqual(expected.getMinimum(),            returned.getMinimum())           &&
               isEqual(expected.getMaximumIsInclusive(), returned.getMaximumIsInclusive())&&
               isEqual(expected.getMaximum(),            returned.getMaximum())           &&
               isEqual(expected.getMaximumIsInclusive(), returned.getMaximumIsInclusive())&&
               isEqual(expected.getDescription(),        returned.getDescription());
    }

    private static boolean dataColumnsEqual(final DataColumn expected, final DataColumn returned)
    {
        return isEqual(expected.getColumnName(),     returned.getColumnName())  &&
               isEqual(expected.getTableName(),      returned.getTableName())   &&
               isEqual(expected.getName(),           returned.getName())        &&
               isEqual(expected.getTitle(),          returned.getTitle())       &&
               isEqual(expected.getDescription(),    returned.getDescription()) &&
               isEqual(expected.getMimeType(),       returned.getMimeType())    &&
               isEqual(expected.getConstraintName(), returned.getConstraintName());
    }


    private static <T> boolean isEqual(final T first, final T second)
    {
        return first == null ? second == null
                             : first.equals(second);
    }
    private static void deleteFile(final File testFile)
    {
        if (testFile.exists())
        {
            if (!testFile.delete())
            {
                throw new RuntimeException(String.format(
                        "Unable to delete testFile. testFile: %s", testFile));
            }
        }
    }
    private String getRanString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(this.getRanString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }

}
