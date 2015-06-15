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

package geopackage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import utility.TestUtility;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.GeoPackage.OpenMode;
import com.rgi.android.geopackage.core.Content;
import com.rgi.android.geopackage.schema.DataColumn;
import com.rgi.android.geopackage.schema.DataColumnConstraint;
import com.rgi.android.geopackage.schema.Type;
import com.rgi.android.geopackage.verification.ConformanceException;
import com.rgi.android.geopackage.verification.VerificationLevel;

@SuppressWarnings({"static-method", "javadoc"})
public class GeoPackageSchemaAPITest
{
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
    public void addDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(5);
        final String   columnName     = "columnName";
        final String   tableName      = "tableName";
        final GeoPackage gpkg = createGeoPackage(tableName, columnName, testFile);

        try
        {
            final Content table = gpkg.core().addContent("tableName",
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,180.0,90.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));


            final String name           = "name";
            final String title          = "title";
            final String description    = "description";
            final String mimeType       = "image/png";
            final String constraintName = "constraint_name";

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
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addDataColumnIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String columnName     = "columnName";
            final String name           = "name";
            final String title          = "title";
            final String description    = "description";
            final String mimeType       = "image/png";
            final String constraintName = "constraint_name";

            gpkg.schema().addDataColumn(null, columnName, name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and tableName is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addDataColumnIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));
            final String name           = "name";
            final String title          = "title";
            final String description    = "description";
            final String mimeType       = "image/png";
            final String constraintName = "constraint_name";

            gpkg.schema().addDataColumn(table, null, name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and columnName is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with an empty string for columnName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final String   mimeType       = "image/png";
            final String   constraintName = "constraint_name";

            gpkg.schema().addDataColumn(table, "", name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and columnName is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = createGeoPackage("tableName", "columnName", testFile);

        try
        {
            final Content table = gpkg.core().addContent("tableName",
                                                         "tiles",
                                                         "identifier",
                                                         "description",
                                                         new BoundingBox(0.0,0.0,180.0,90.0),
                                                         gpkg.core().getSpatialReferenceSystem(4326));

            final String columnName     = "columnName";
            final String name           = "name";
            final String title          = "title";
            final String description    = "description";
            final String mimeType       = "image/png";
            final String constraintName = "constraint_name";

            final DataColumn columnExpected = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);

            final DataColumn columnReturned = gpkg.schema().getDataColumn(table, columnName);
            final DataColumn shouldBeNull   = gpkg.schema().getDataColumn(table, "ColumnThatdoesn'tExist");
            assertTrue("The GeoPackage schema did not return the expected values when using the getDataColumn method",
                       dataColumnsEqual(columnExpected, columnReturned) &&
                       shouldBeNull == null);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
        final File testFile = TestUtility.getRandomFile(14);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
           gpkg.schema().getDataColumn(null,"columnName");
           fail("Expected GeoPackage Schema to throw an IllegalArgumentException when trying to get a data column and table is null.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, null);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final Content table =  gpkg.tiles().addTileSet("tableName",
                                                           "identifier",
                                                           "description",
                                                           new BoundingBox(0.0,0.0,180.0,90.0),
                                                           gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, "");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getDataColumnCollection() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final String tableName = "tableName";
        final String columnName = "columnName";
        final GeoPackage gpkg = createGeoPackage(tableName, columnName, testFile);

        try
        {
            final Collection<DataColumn> shouldBeEmpty = gpkg.schema().getDataColumns();

            assertTrue("Returned a non empty collection when there were no DataColumn entries in the geopackage with getDataColumn method.",
                       shouldBeEmpty.isEmpty());

            final Content table = gpkg.core().addContent(tableName,
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,180.0,90.0),
                                                   gpkg.core().getSpatialReferenceSystem(4326));

            final String name           = "name";
            final String title          = "title";
            final String description    = "description";
            final String mimeType       = "image/png";
            final String constraintName = "constraint_name";

            final DataColumn column = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);

            final String columnName2     = "last_column";
            final String name2           = "name2";
            final String title2          = "title2";
            final String description2    = "description2";
            final String mimeType2       = "image/jpeg";
            final String constraintName2 = "constraint_name2";

            final DataColumn column2 = gpkg.schema().addDataColumn(table, columnName2, name2, title2, description2, mimeType2, constraintName2);

            final List<DataColumn> columnsExpected = Arrays.asList(column, column2);

            final Collection<DataColumn> columnsReturned = gpkg.schema().getDataColumns();

            for(final DataColumn returned: columnsReturned)
            {
                final boolean columnMatches = FunctionalUtility.anyMatch(columnsExpected,
                                                                   new Predicate<DataColumn>(){
                                                                                                  @Override
                                                                                                public boolean apply(final DataColumn expected)
                                                                                                  {
                                                                                                      return dataColumnsEqual(expected, returned);
                                                                                                  }
                                                                                              });

                assertTrue("GeoPackage Schema did not returned the expected DataColumn entries from the geopackage when using getDataColumn() method",
                           columnsReturned.size() == 2 && columnMatches);
            }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
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
                       constraint.getValue() == value                                    &&
                       constraint.getMinimum().equals(minimum)                          &&
                       constraint.getMinimumIsInclusive().equals(minimumIsInclusive)    &&
                       constraint.getMaximum().equals(maximum)                          &&
                       constraint.getMaximumIsInclusive().equals(maximumIsInclusive));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if geopackage addDataColumnConstraint
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void addDataColumnConstraint2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Glob;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            final DataColumnConstraint constraint = gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            assertTrue("The Returned datacolumnconstraint using addDataColumnConstraint does not return the expected values",
                        constraint.getConstraintName().equals(constraintName)            &&
                        constraint.getConstraintType().equals(constraintType.toString()) &&
                        constraint.getValue().equals(value)                              &&
                        constraint.getMinimum() == (minimum)                             &&
                        constraint.getMinimumIsInclusive() == (minimumIsInclusive)       &&
                        constraint.getMaximum() == (maximum)                             &&
                        constraint.getMaximumIsInclusive() == (maximumIsInclusive));
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }


    /**
     * Tests if GeoPackage schema will
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
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = null;//illegal!
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
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with a
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "";//illegal!
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with an empty string for constraint name");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = null;//illegal!
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for constraint type");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException4() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = "illegal";//illegal when type is range
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non-null value for value when the constraint type is range.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException5() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = null;//illegal when type is range
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for minimum when the constraintType is Range.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException6() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = null;//illegal when type is range
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for maximum when the constraintType is Range.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException7() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = -1;//illegal if minimum is < maximum
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a minimum that is < maximum.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException8() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = null;// illegal when type is range
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a a null value for minimumIsInclusive when constraint type is range.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException9() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = null;// illegal when type is range
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a a null value for maximumIsInclusive when constraint type is range.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException10() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = 0;// illegal when type is Enum or Glob
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimum when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException11() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Glob;
            final String  value               = "value";
            final Number  minimum             = 0;// illegal when type is Enum or Glob
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimum when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException12() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = true;// illegal when type is Enum or Glob
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimumIsInclusive when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException13() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = 10;// illegal when type is Enum or Glob
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for maximum when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException14() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = true;// illegal when type is Enum or Glob
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for maximumIsInclusive when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException15() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = null;// illegal when type is Enum or Glob
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for value when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException16() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Glob;
            final String  value               = null;// illegal when type is Enum or Glob
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for value when constraint type is enum or glob.");

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the data Column constraint contains the values expected
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getDataColumnConstraint() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
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
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if the data Column constraint contains the values expected
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getDataColumnConstraint2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String  constraintName      = "constraint_name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = false;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = true;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName,
                                                  constraintType,
                                                  value,
                                                  minimum,
                                                  minimumIsInclusive,
                                                  maximum,
                                                  maximumIsInclusive,
                                                  description);

            final DataColumnConstraint constraintReturned = gpkg.schema().getDataColumnConstraint("ConstraintThatDoesn'tExist", Type.Range, "Doesn'tExist");

            assertTrue("The data Column Constraint returned a non null value when the data column constraint searched for did not exist in the geopackage.",
                       constraintReturned == null);

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String  constraintName      = null;//illegal!!
            final Type    constraintType      = Type.Range;
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in a null paramter for constraintName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String  constraintName      = "";//illegal!!
            final Type    constraintType      = Type.Range;
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in an empty string for constraintName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(11);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String  constraintName      = "constraintName";
            final Type    constraintType      = null;//Illegal!!!
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in an empty string for constraintName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if getDataColumnConstraints() returns the
     * expected values
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test
    public void getDataColumnConstraints() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
           final Collection<DataColumnConstraint> shouldBeEmptyList = gpkg.schema().getDataColumnConstraints();
           assertTrue("Expected GeoPackageSchema to return an empty list when there are no data column constraints in the GeoPackage.",shouldBeEmptyList.size() == 0);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if getDataColumnConstraints() returns the
     * expected values
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws IOException
     */
    @Test
    public void getDataColumnConstraints2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {

           final Number  minimumNull            = null;
           final Boolean minimumIsInclusiveNull = null;
           final Number  maximumNull            = null;
           final Boolean maximumIsInclusiveNull = null;
           final String  descriptionNull        = null;
           final String  valueNull        = null;

           final DataColumnConstraint  constraint1 = gpkg.schema().addDataColumnConstraint("name1",
                                                                                     Type.Enum,
                                                                                     "value1",
                                                                                     minimumNull,
                                                                                     minimumIsInclusiveNull,
                                                                                     maximumNull,
                                                                                     maximumIsInclusiveNull,
                                                                                     descriptionNull);
           final DataColumnConstraint  constraint2 = gpkg.schema().addDataColumnConstraint("name2",
                                                                                     Type.Glob,
                                                                                     "value2",
                                                                                     minimumNull,
                                                                                     minimumIsInclusiveNull,
                                                                                     maximumNull,
                                                                                     maximumIsInclusiveNull,
                                                                                     descriptionNull);
           final DataColumnConstraint  constraint3 = gpkg.schema().addDataColumnConstraint("name3",
                                                                                     Type.Range,
                                                                                     valueNull,
                                                                                     0,
                                                                                     true,
                                                                                     20,
                                                                                     false,
                                                                                     "description");

           final Collection<DataColumnConstraint> expectedCollection = Arrays.asList(constraint1, constraint2, constraint3);
           final Collection<DataColumnConstraint> returnedCollection = gpkg.schema().getDataColumnConstraints();

           for(final DataColumnConstraint returned : returnedCollection)
           {
               final boolean dataColumnConstraintMatches = FunctionalUtility.anyMatch(expectedCollection,
                                                                                new Predicate<DataColumnConstraint>(){
                                                                                                                         @Override
                                                                                                                        public boolean apply(final DataColumnConstraint expected)
                                                                                                                         {
                                                                                                                             return  dataColumnConstraintsEqual(expected, returned);
                                                                                                                         }
                                                                                                                     });

               assertTrue("Expected GeoPackageSchema to return an empty list when there are no data column constraints in the GeoPackage.", dataColumnConstraintMatches);
           }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    private static GeoPackage createGeoPackage(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        gpkg.close();
        createTable(tableName, columnName, testFile);

        return new GeoPackage(testFile, VerificationLevel.None, OpenMode.Open);
    }

    private static void createTable(final String tableName, final String columnName, final File testFile) throws ClassNotFoundException, SQLException
    {
        final String createTable = String.format("CREATE TABLE %s ( %s TEXT," +
                                                             "other_column INTEGER NOT NULL," +
                                                             "more_columns INTEGER NOT NULL," +
                                                             "last_Column TEXT NOT NULL)",
                                            tableName,
                                            columnName);

        final Connection con = getConnection(testFile);

        try
        {
            final Statement stmt = con.createStatement();
            try
            {
                stmt.execute(createTable);
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            con.close();
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
}
