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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
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
import com.rgi.geopackage.schema.Type;
import com.rgi.geopackage.verification.ConformanceException;

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
    public void addDataColumn() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(5);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Content table = gpkg.core().addContent("tableName", 
                                                   "tiles", 
                                                   "identifier", 
                                                   "description", 
                                                   new BoundingBox(0.0,0.0,90.0,180.0), 
                                                   gpkg.core().getSpatialReferenceSystem(4326));
            
            String   columnName     = "columnName";
            String   name           = "name";
            String   title          = "title";
            String   description    = "description";
            MimeType mimeType       = new MimeType("image/png");
            String   constraintName = "constraint_name";
            
            DataColumn dataColumnReturned = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);
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
    public void addDataColumnIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            
            String   columnName     = "columnName";
            String   name           = "name";
            String   title          = "title";
            String   description    = "description";
            MimeType mimeType       = new MimeType("image/png");
            String   constraintName = "constraint_name";
            
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
    public void addDataColumnIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Content table = gpkg.core().addContent("tableName", 
                                                   "tiles", 
                                                   "identifier", 
                                                   "description", 
                                                   new BoundingBox(0.0,0.0,90.0,180.0), 
                                                   gpkg.core().getSpatialReferenceSystem(4326));
            String   name           = "name";
            String   title          = "title";
            String   description    = "description";
            MimeType mimeType       = new MimeType("image/png");
            String   constraintName = "constraint_name";
            
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
    public void getDataColumn() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Content table = gpkg.core().addContent("tableName", 
                                                   "tiles", 
                                                   "identifier", 
                                                   "description", 
                                                   new BoundingBox(0.0,0.0,90.0,180.0), 
                                                   gpkg.core().getSpatialReferenceSystem(4326));
            
            String   columnName     = "columnName";
            String   name           = "name";
            String   title          = "title";
            String   description    = "description";
            MimeType mimeType       = new MimeType("image/png");
            String   constraintName = "constraint_name";
            
            DataColumn columnExpected = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);
            
            DataColumn columnReturned = gpkg.schema().getDataColumn(table, columnName);
            DataColumn shouldBeNull   = gpkg.schema().getDataColumn(table, "ColumnThatdoesn'tExist");
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
    public void getDataColumnIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(14);
        
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
    public void getDataColumnIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Content table = gpkg.core().addContent("tableName", 
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
    public void getDataColumnIllegalArgumentException3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Content table = gpkg.core().addContent("tableName", 
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
    public void getDataColumnCollection() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(10);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Collection<DataColumn> shouldBeEmpty = gpkg.schema().getDataColumn();
            
            assertTrue("Returned a non empty collection when there were no DataColumn entries in the geopackage with getDataColumn method.",
                       shouldBeEmpty.isEmpty());
            
            Content table = gpkg.core().addContent("tableName", 
                                                   "tiles", 
                                                   "identifier", 
                                                   "description", 
                                                   new BoundingBox(0.0,0.0,90.0,180.0), 
                                                   gpkg.core().getSpatialReferenceSystem(4326));
            
            String   columnName     = "columnName";
            String   name           = "name";
            String   title          = "title";
            String   description    = "description";
            MimeType mimeType       = new MimeType("image/png");
            String   constraintName = "constraint_name";
            
            DataColumn column = gpkg.schema().addDataColumn(table, columnName, name, title, description, mimeType, constraintName);
            
            String   columnName2     = "columnName2";
            String   name2           = "name2";
            String   title2          = "title2";
            String   description2    = "description2";
            MimeType mimeType2       = new MimeType("image/jpeg");
            String   constraintName2 = "constraint_name2";
            
            DataColumn column2 = gpkg.schema().addDataColumn(table, columnName2, name2, title2, description2, mimeType2, constraintName2);
            
            List<DataColumn> columnsExpected = Arrays.asList(column, column2);
            
            Collection<DataColumn> columnsReturned = gpkg.schema().getDataColumn();
            
            assertTrue("GeoPackage Schema did not returned the expected DataColumn entries from the geopackage when using getDataColumn() method",
                       columnsReturned.size() == 2 &&
                       columnsReturned.stream().allMatch(returned-> columnsExpected.stream().anyMatch(expected -> dataColumnsEqual(expected, returned))));
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
//    /**
//     * Tests if geopackage addDataColumnConstraint
//     * returns the expected values
//     * @throws FileAlreadyExistsException
//     * @throws ClassNotFoundException
//     * @throws FileNotFoundException
//     * @throws SQLException
//     * @throws ConformanceException
//     */
//    @Test
//    public void addDataColumnConstraint() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
//    {
//        File testFile = this.getRandomFile(11);
//        
//        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
//        {
//            
//            String  constraintName      = "constraint_name";
//            Type    constraintType      = Type.Range;
//            String  value               = null;
//            Number  minimum             = 0;
//            Boolean minimumIsInclusive  = true;
//            Number  maximum             = 20;
//            Boolean maximumIsInclusive  = false;
//            String  description         = "description";
//            
//            DataColumnConstraint constraint = gpkg.schema().addDataColumnConstraint(constraintName, 
//                                                                                    constraintType, 
//                                                                                    value, 
//                                                                                    minimum, 
//                                                                                    minimumIsInclusive, 
//                                                                                    maximum, 
//                                                                                    maximumIsInclusive, 
//                                                                                    description);
//            gpkg.schema().addDataColumnConstraint(constraintName, 
//                                                  constraintType, 
//                                                  value, 
//                                                  minimum, 
//                                                  minimumIsInclusive, 
//                                                  maximum, 
//                                                  maximumIsInclusive, 
//                                                  description);
//            
//            assertTrue("The Returned datacolumnconstraint using addDataColumnConstraint does not return the expected values",
//                       constraint.getConstraintName().equals(constraintName)            &&
//                       constraint.getConstraintType().equals(constraintType.toString()) &&
//                       constraint.getValue() == null                                    &&
//                       constraint.getMinimum().equals(minimum)                          &&
//                       constraint.getMinimumIsInclusive().equals(minimumIsInclusive)    &&
//                       constraint.getMaximum().equals(maximum)                          &&
//                       constraint.getMaximumIsInclusive().equals(maximumIsInclusive));
//        }
//        finally
//        {
//            deleteFile(testFile);
//        }
//    }//TODO bug in get DataColumnConstraints
    
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
    public void addDataColumnConstraintIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(13);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String  constraintName      = null;
            Type    constraintType      = Type.Range;
            String  value               = null;
            Number  minimum             = 0;
            Boolean minimumIsInclusive  = true;
            Number  maximum             = 20;
            Boolean maximumIsInclusive  = false;
            String  description         = "description";
            
            gpkg.schema().addDataColumnConstraint(constraintName, constraintType, value, minimum, minimumIsInclusive, maximum, maximumIsInclusive, description);
            fail("GeoPackage Schema did not throw an IllegalArgumentException when trying to add a datacolumn constraint with a null value for constraint name");
            
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
//    private boolean dataColumnConstraintsEqual(DataColumnConstraint expected, DataColumnConstraint returned) //TODO can't use until bug is fixed
//    {
//        return expected.getConstraintName()    .equals(returned.getConstraintName())    &&
//               expected.getConstraintType()    .equals(returned.getConstraintType())    &&
//               expected.getValue()             .equals(returned.getValue())             &&
//               expected.getMinimum()           .equals(returned.getMinimum())           &&
//               expected.getMaximumIsInclusive().equals(returned.getMaximumIsInclusive())&&
//               expected.getMaximum()           .equals(returned.getMaximum())           &&
//               expected.getMaximumIsInclusive().equals(returned.getMaximumIsInclusive())&&
//               expected.getDescription()       .equals(returned.getDescription());
//    }

    private boolean dataColumnsEqual(DataColumn expected, DataColumn returned)
    {
        return expected.getColumnName()    .equals(returned.getColumnName())  &&
               expected.getTableName()     .equals(returned.getTableName())   &&
               expected.getName()          .equals(returned.getName())        &&
               expected.getTitle()         .equals(returned.getTitle())       &&
               expected.getDescription()   .equals(returned.getDescription()) &&
               expected.getMimeType()      .equals(returned.getMimeType())    &&
               expected.getConstraintName().equals(returned.getConstraintName());
    }
    private static void deleteFile(File testFile)
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
