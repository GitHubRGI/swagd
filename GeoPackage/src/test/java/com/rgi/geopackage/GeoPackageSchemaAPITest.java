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

package com.rgi.geopackage;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.schema.DataColumn;
import com.rgi.geopackage.schema.DataColumnConstraint;
import com.rgi.geopackage.schema.Type;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jenifer Cochran
 * @author Luke Lambert
 */
@SuppressWarnings({"javadoc", "ConstantConditions"})
public class GeoPackageSchemaAPITest
{
    @BeforeClass
    public static void setUp() throws ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver
    }

    /**
     * Tests if GeoPackage Schema will return
     * the expected values for the DataColumn
     * entry added to the GeoPackage
     */
    @Test
    public void addDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final String columnName = "columnName";
        final String tableName  = "tableName";

        try(final GeoPackage gpkg = createGeoPackage(tableName, columnName))
        {
            final Content table = gpkg.core()
                                      .addContent("tableName",
                                                  "tiles",
                                                  "identifier",
                                                  "description",
                                                  new BoundingBox(0.0,0.0,180.0,90.0),
                                                  gpkg.core().getSpatialReferenceSystem("EPSG", 4326));


            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            final DataColumn dataColumnReturned = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);
            gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);    // bug in getting data column

            assertTrue("GeoPackage Schema did not return the DataColumn expected.(using addDataColumn)",
                       dataColumnReturned.getColumnName()    .equals(columnName)             &&
                       dataColumnReturned.getName()          .equals(name)                   &&
                       dataColumnReturned.getTitle()         .equals(title)                  &&
                       dataColumnReturned.getDescription()   .equals(description)            &&
                       dataColumnReturned.getMimeType()      .equals(mimeType.toString())    &&
                       dataColumnReturned.getConstraintName().equals(constraintName));
        }
    }

    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with a null value for tableName
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
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
    }

    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with a null value for columnName
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            gpkg.schema().addDataColumn(table, null, name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and columnName is null.");
        }
    }


    /**
     * Tests if GeoPackage Schema throws
     * an IllegalArgumentException when adding
     * a data column with an empty string for columnName
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));
            final String   name           = "name";
            final String   title          = "title";
            final String   description    = "description";
            final MimeType mimeType       = new MimeType("image/png");
            final String   constraintName = "constraint_name";

            gpkg.schema().addDataColumn(table, "", name, title, description, mimeType, constraintName);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add a datacolumn and columnName is null.");
        }
    }

    /**
     * Tests if getDataColumn method in
     * GeoPackage schema returns
     * the expected values.
     */
    @Test
    public void getDataColumn() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        try(final GeoPackage gpkg = createGeoPackage("tableName", "columnName"))
        {
            final Content table = gpkg.core().addContent("tableName",
                                                         "tiles",
                                                         "identifier",
                                                         "description",
                                                         new BoundingBox(0.0,0.0,180.0,90.0),
                                                         gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

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
                       areDateColumnsEqual(columnExpected, columnReturned) &&
                       shouldBeNull == null);
        }
    }

    /**
     * Tests if GeoPackage schema
     * will throw an IllegalArgumentException when
     * trying to get a data column entry with table name
     * that has a value of null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           gpkg.schema().getDataColumn(null, "columnName");
           fail("Expected GeoPackage Schema to throw an IllegalArgumentException when trying to get a data column and table is null.");
        }
    }

    /**
     * Tests if GeoPackage Schema will throw an Illegal
     * argumentException when trying to get a data Column
     * with a null value for column
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table = gpkg.tiles().addTileSet("tableName",
                                                          "identifier",
                                                          "description",
                                                          new BoundingBox(0.0,0.0,180.0,90.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, null);
        }
    }

    /**
     * Tests if GeoPackage Schema will throw an Illegal
     * argumentException when trying to get a data Column
     * with an empty string for column
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Content table =  gpkg.tiles().addTileSet("tableName",
                                                           "identifier",
                                                           "description",
                                                           new BoundingBox(0.0,0.0,180.0,90.0),
                                                           gpkg.core().getSpatialReferenceSystem(4326));

            gpkg.schema().getDataColumn(table, "");
        }
    }

    /**
     * Tests if GeoPackage schema returns the
     * data column entries expected
     */
    @Test
    public void getDataColumnCollection() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final String tableName = "tableName";
        final String columnName = "columnName";

        try(final GeoPackage gpkg = createGeoPackage(tableName, columnName))
        {
            final Collection<DataColumn> shouldBeEmpty = gpkg.schema().getDataColumns();

            assertTrue("Returned a non empty collection when there were no DataColumn entries in the geopackage with getDataColumn method.",
                       shouldBeEmpty.isEmpty());

            final Content table = gpkg.core().addContent(tableName,
                                                   "tiles",
                                                   "identifier",
                                                   "description",
                                                   new BoundingBox(0.0,0.0,180.0,90.0),
                                                   gpkg.core().getSpatialReferenceSystem("EPSG", 4326));

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
                       columnsReturned.stream().allMatch(returned-> columnsExpected.stream().anyMatch(expected -> areDateColumnsEqual(expected, returned))));
        }
    }

    /**
     * Tests if GeoPackage addDataColumnConstraint
     * returns the expected values
     */
    @Test
    public void addDataColumnConstraint() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
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
                       constraint.getConstraintName()    .equals(constraintName)            &&
                       constraint.getConstraintType()    .equals(constraintType.toString()) &&
                       Objects.equals(constraint.getValue(), value)                         &&
                       constraint.getMinimum()           .equals(minimum)                   &&
                       constraint.getMinimumIsInclusive().equals(minimumIsInclusive)        &&
                       constraint.getMaximum()           .equals(maximum)                   &&
                       constraint.getMaximumIsInclusive().equals(maximumIsInclusive));
        }
    }

    /**
     * Tests if GeoPackage addDataColumnConstraint
     */
    @Test
    public void addDataColumnConstraint2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
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
                        Objects.equals(constraint.getMinimum(),            minimum) &&
                        Objects.equals(constraint.getMinimumIsInclusive(), minimumIsInclusive) &&
                        Objects.equals(constraint.getMaximum(),            maximum) &&
                        Objects.equals(constraint.getMaximumIsInclusive(), maximumIsInclusive));
        }
    }


    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with a
     * null value for constraintName
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = null;    // illegal!
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
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with a
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "";    // illegal!
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
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = null;    // illegal!
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for constraint type");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException4() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = "illegal";    // illegal when type is range
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non-null value for value when the constraint type is range.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException5() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = null;    // illegal when type is range
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for minimum when the constraintType is Range.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException6() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = null;    // illegal when type is range
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for maximum when the constraintType is Range.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException7() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = -1;    // illegal if minimum is < maximum
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a minimum that is < maximum.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException8() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = null;    // illegal when type is range
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = false;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a a null value for minimumIsInclusive when constraint type is range.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException9() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Range;
            final String  value               = null;
            final Number  minimum             = 0;
            final Boolean minimumIsInclusive  = true;
            final Number  maximum             = 20;
            final Boolean maximumIsInclusive  = null;    // illegal when type is range
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a a null value for maximumIsInclusive when constraint type is range.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException10() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = 0;    // illegal when type is Enum or Glob
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimum when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException11() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Glob;
            final String  value               = "value";
            final Number  minimum             = 0;    // illegal when type is Enum or Glob
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimum when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException12() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = true;    // illegal when type is Enum or Glob
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for minimumIsInclusive when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException13() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = 10;    // illegal when type is Enum or Glob
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for maximum when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException14() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = "value";
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = true;    // illegal when type is Enum or Glob
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a non null value for maximumIsInclusive when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException15() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Enum;
            final String  value               = null;    // illegal when type is Enum or Glob
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for value when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if GeoPackage schema will
     * throw an IllegalArgumentException when
     * trying to add a dataColumnConstraint with
     * invalid parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void addDataColumnConstraintIllegalArgumentException16() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();
        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final String  constraintName      = "Constraint name";
            final Type    constraintType      = Type.Glob;
            final String  value               = null;    // illegal when type is Enum or Glob
            final Number  minimum             = null;
            final Boolean minimumIsInclusive  = null;
            final Number  maximum             = null;
            final Boolean maximumIsInclusive  = null;
            final String  description         = "description";

            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for value when constraint type is enum or glob.");
        }
    }

    /**
     * Tests if the data Column constraint contains the values expected
     */
    @Test
    public void getDataColumnConstraint() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
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
                       areDataColumnConstraintsEqual(constraintExpected, constraintReturned));
        }
    }

    /**
     * Tests if the data Column constraint contains the values expected
     */
    @Test
    public void getDataColumnConstraint2() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
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

            assertNull("The data Column Constraint returned a non null value when the data column constraint searched for did not exist in the geopackage.",
                       constraintReturned);
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String  constraintName      = null;    // illegal!!
            final Type    constraintType      = Type.Range;
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in a null paramter for constraintName.");
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException2() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String  constraintName      = "";    // illegal!!
            final Type    constraintType      = Type.Range;
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in an empty string for constraintName.");
        }
    }

    /**
     * Tests if GeoPackage Schema throws an IllegalArgumentException
     * when using the method getDataColumnConstraint with illegal values
     * for the parameters
     */
    @Test(expected = IllegalArgumentException.class)
    public void getDataColumnContraintIllegalArgumentException3() throws SQLException, ClassNotFoundException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final String  constraintName      = "constraintName";
            final Type    constraintType      = null;    // Illegal!!!
            final String  value               = null;

            gpkg.schema().getDataColumnConstraint(constraintName, constraintType, value);

            fail("Expected GeoPackageSchema to throw an IllegalArgumentException when passing in an empty string for constraintName.");
        }
    }

    /**
     * Tests if getDataColumnConstraints() returns the
     * expected values
     */
    @Test
    public void getDataColumnConstraints() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {
           final Collection<DataColumnConstraint> shouldBeEmptyList = gpkg.schema().getDataColumnConstraints();
           assertTrue("Expected GeoPackageSchema to return an empty list when there are no data column constraints in the GeoPackage.", shouldBeEmptyList.isEmpty());
        }
    }

    /**
     * Tests if getDataColumnConstraints() returns the
     * expected values
     */
    @Test
    public void getDataColumnConstraints2() throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile))
        {

           final Number  minimumNull            = null;
           final Boolean minimumIsInclusiveNull = null;
           final Number  maximumNull            = null;
           final Boolean maximumIsInclusiveNull = null;
           final String  descriptionNull        = null;
           final String  valueNull              = null;

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
           assertTrue("Expected GeoPackageSchema to return an empty list when there are no data column constraints in the GeoPackage.",
                      returnedCollection.stream()
                                        .allMatch(returned -> expectedCollection.stream()
                                                                                .anyMatch(expected -> areDataColumnConstraintsEqual(expected, returned))));
        }
    }

    private static GeoPackage createGeoPackage(final String tableName, final String columnName) throws ClassNotFoundException, SQLException, ConformanceException, IOException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.close();
            createTable(tableName, columnName, testFile);

            return new GeoPackage(testFile, VerificationLevel.None, OpenMode.Open);
        }
    }

    @SuppressWarnings("JDBCExecuteWithNonConstantString")
    private static void createTable(final String tableName, final String columnName, final File testFile) throws SQLException
    {
        final String createTable = String.format("CREATE TABLE %s ( %s TEXT," +
                                                             "other_column INTEGER NOT NULL," +
                                                             "more_columns INTEGER NOT NULL," +
                                                             "last_Column TEXT NOT NULL)",
                                            tableName,
                                            columnName);
        try(final Connection con = TestUtility.getConnection(testFile))
        {
            try(final Statement stmt = con.createStatement())
            {
                stmt.execute(createTable);
            }
        }
    }

    private static boolean areDataColumnConstraintsEqual(final DataColumnConstraint expected, final DataColumnConstraint returned)
    {
        return Objects.equals(expected.getConstraintName(),     returned.getConstraintName())    &&
               Objects.equals(expected.getConstraintType(),     returned.getConstraintType())    &&
               Objects.equals(expected.getValue(),              returned.getValue())             &&
               Objects.equals(expected.getMinimum(),            returned.getMinimum())           &&
               Objects.equals(expected.getMaximum(),            returned.getMaximum())           &&
               Objects.equals(expected.getMaximumIsInclusive(), returned.getMaximumIsInclusive())&&
               Objects.equals(expected.getDescription(),        returned.getDescription());
    }

    private static boolean areDateColumnsEqual(final DataColumn expected, final DataColumn returned)
    {
        return Objects.equals(expected.getColumnName(),     returned.getColumnName())  &&
               Objects.equals(expected.getTableName(),      returned.getTableName())   &&
               Objects.equals(expected.getName(),           returned.getName())        &&
               Objects.equals(expected.getTitle(),          returned.getTitle())       &&
               Objects.equals(expected.getDescription(),    returned.getDescription()) &&
               Objects.equals(expected.getMimeType(),       returned.getMimeType())    &&
               Objects.equals(expected.getConstraintName(), returned.getConstraintName());
    }
}
