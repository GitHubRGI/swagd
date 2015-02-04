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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.verification.ConformanceException;

public class GeoPackageExtensionsAPITest
{

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();


    /**
     * Tests if GeoPackage Extensions can
     * retrive an extension that has a null value
     * for tablename and columnname
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtensionWithNullParameters() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String extensionName = "something_extension";

            Extension expectedExtension = gpkg.extensions().addExtension(null, null, extensionName, "definition", Scope.ReadWrite); //this works fine

            Extension returnedExtension = gpkg.extensions().getExtension(null, null, extensionName); //this does not



            assertTrue(String.format("The GeoPackageExtensions did not return the extension expected. Expected: %s.\nActual: %s.",
                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s",
                                                    expectedExtension.getTableName(),
                                                    expectedExtension.getColumnName(),
                                                    expectedExtension.getExtensionName(),
                                                    expectedExtension.getDefinition(),
                                                    expectedExtension.getScope().toString()),
                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s",
                                                    returnedExtension.getTableName(),
                                                    returnedExtension.getColumnName(),
                                                    returnedExtension.getExtensionName(),
                                                    returnedExtension.getDefinition(),
                                                    expectedExtension.getScope().toString())),
                       returnedExtension.equals(expectedExtension.getTableName(),
                                                expectedExtension.getTableName(),
                                                expectedExtension.getExtensionName(),
                                                expectedExtension.getDefinition(),
                                                Scope.ReadWrite));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if gpkgExtensions returns the values expected
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtension2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String tableName = "TableName";
            String columnName = "columnName";
            String extensionName = "extension_Name";

            Extension expectedExtension = gpkg.extensions().addExtension(tableName, columnName, extensionName, "definition", Scope.ReadWrite);
            Extension returnedExtension = gpkg.extensions().getExtension(tableName, columnName, extensionName);

            assertTrue(String.format("Did not return the expected Extension.\nExpected: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s."
                            + " \nActual: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s. ",
                                      tableName,
                                      columnName,
                                      extensionName,
                                      expectedExtension.getDefinition(),
                                      expectedExtension.getScope(),
                                      returnedExtension.getTableName(),
                                      returnedExtension.getColumnName(),
                                      returnedExtension.getExtensionName(),
                                      returnedExtension.getDefinition(),
                                      returnedExtension.getScope()),
                            returnedExtension.equals(tableName,
                                                     columnName,
                                                     extensionName,
                                                     expectedExtension.getDefinition(),
                                                     Scope.ReadWrite));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Extensions returned
     * the expected extension and uses the getters
     * from Extensions class to verify the values
     * were inputted into the class correctly
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtensionUsingExtensionGetters() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String extensionName = "something_extension";

            Extension expectedExtension = gpkg.extensions().addExtension(null, null, extensionName, "definition", Scope.ReadWrite); //this works fine

            Extension returnedExtension = gpkg.extensions().getExtension(null, null, extensionName); //this does not



            assertTrue(String.format("The GeoPackageExtensions did not return the extension expected. Expected: %s.\nActual: %s.",
                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s",
                                                    expectedExtension.getTableName(),
                                                    expectedExtension.getColumnName(),
                                                    expectedExtension.getExtensionName(),
                                                    expectedExtension.getDefinition(),
                                                    expectedExtension.getScope().toString()),
                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s",
                                                    returnedExtension.getTableName(),
                                                    returnedExtension.getColumnName(),
                                                    returnedExtension.getExtensionName(),
                                                    returnedExtension.getDefinition(),
                                                    expectedExtension.getScope().toString())),
                       returnedExtension.equals(expectedExtension.getTableName(),
                                                expectedExtension.getTableName(),
                                                expectedExtension.getExtensionName(),
                                                expectedExtension.getDefinition(),
                                                Scope.ReadWrite));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if gpkgExtensions returns the values expected
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtension() throws FileAlreadyExistsException,
            ClassNotFoundException, FileNotFoundException, SQLException,
            ConformanceException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String tableName = "TableName";
            String columnName = "columnName";
            String extensionName = "extension_Name";

            Extension expectedExtension = gpkg.extensions().addExtension(tableName, columnName, extensionName, "definition", Scope.ReadWrite);
            Extension returnedExtension = gpkg.extensions().getExtension(tableName, columnName, extensionName);

            assertTrue(String.format("Did not return the expected Extension.\nExpected: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s."
                            + " \nActual: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s. ",
                                      tableName,
                                      columnName,
                                      extensionName,
                                      expectedExtension.getDefinition(),
                                      expectedExtension.getScope(),
                                      returnedExtension.getTableName(),
                                      returnedExtension.getColumnName(),
                                      returnedExtension.getExtensionName(),
                                      returnedExtension.getDefinition(),
                                      returnedExtension.getScope()),
                            returnedExtension.equals(tableName,
                                                     columnName,
                                                     extensionName,
                                                     expectedExtension.getDefinition(),
                                                     Scope.ReadWrite));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage Extensions will throw
     * and IllegalArgumentException when giving a null
     * value for tableName and not to ColumnName (if table
     * name is null, then so must columnName)
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(13);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension(null, "ColumnNameShouldBeNull", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a null value for tableName and not columnName.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage Extensions will throw
     * and IllegalArgumentException when giving an
     * empty string for tableName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(13);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension("", "columnName", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a empty string for tableName.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the GeoPackage Extensions will throw
     * and IllegalArgumentException when giving an
     * empty string for tableName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(13);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension(null, "columnName", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a empty string for tableName.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if GeoPackage Extensions will throw
     * and IllegalArgumentException when adding an
     * Extension that has an empty string for
     * tableName
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalARgumentException4() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(14);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension("", null, "extension_Name", "definition", Scope.WriteOnly);
            fail("Expected GeoPackageExtensions to throw an Illegal argument exception when column name is null and table name is an empty string.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Extensions will throw an
     * IllegalArgumentException when columnName is empty
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException5() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(17);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           gpkg.extensions().addExtension("TableName", "" , "extension_Name", "definition", Scope.ReadWrite);
           fail("Expected GeoPackageExtensions to throw an Illegal argument exception when column name and table name are empty strings.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when trying to add an extension with a null value
     * for extension name
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException6() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension("tableName", "columnName", null, "definition", Scope.ReadWrite);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add an extension with a null value for extension name");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if Geopackage will throw an IllegalArgumentException when trying to
     * add an extension with an emptry string for extension name.
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException7() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension(null, null, "", "Definition", Scope.ReadWrite);
            fail("Expected Geopackage to throw an IllegalArgumentException when trying to add an extension with an emptry string for extension name.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage will throw an IllegalArgumentException when adding an
     * extension that has an invalid extension name.
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException8() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(9);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            gpkg.extensions().addExtension("tablename", "columnName", "illegalExtensionName", "definition", Scope.WriteOnly);
            fail("Expected GeoPackage to throw an IllegalArgumentException when adding an extension that has an invalid extension name.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if the getters from Extension
     * will get the right values when adding an
     * extension and then checking its value
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void addExtensionWithoutNullParameters() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(12);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String tableName ="tablename";
            String columnName = "columnName";
            String extensionName ="extension_Name";
            String definition = "definition";
            Scope scope = Scope.ReadWrite;

            Extension extensionReturned = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            assertTrue("The GeoPackage did not return the expected extension.",
                       extensionReturned.getTableName()       .equals(tableName)      &&
                       extensionReturned.getColumnName()      .equals(columnName)     &&
                       extensionReturned.getExtensionName()   .equals(extensionName)  &&
                       extensionReturned.getDefinition()      .equals(definition)     &&
                       extensionReturned.getScope().toString().equals(scope.toString()));

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if the getters from Extension
     * will get the right values when adding an
     * extension when the file already contains
     * the extensions table
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void addExtensionWhenExtensionsTableExists() throws IOException, ClassNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        testFile.createNewFile();
        //Create Extensions table
        GeoPackageExtensionsAPITest.createExtensionsTable(testFile);
        try(GeoPackage gpkg = new GeoPackage(testFile, false, OpenMode.OpenOrCreate))
        {
            String tableName ="tablename";
            String columnName = "columnName";
            String extensionName ="extension_Name";
            String definition = "definition";
            Scope scope = Scope.ReadWrite;

            Extension extensionReturned = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            assertTrue("The GeoPackage did not return the expected extension.",
                       extensionReturned.getTableName()       .equals(tableName)      &&
                       extensionReturned.getColumnName()      .equals(columnName)     &&
                       extensionReturned.getExtensionName()   .equals(extensionName)  &&
                       extensionReturned.getDefinition()      .equals(definition)     &&
                       extensionReturned.getScope().toString().equals(scope.toString()));

        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests that it will not add the same extension
     * twice to the geopackage
     * and return the values expected
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void addExistingExtension() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           
            String tableName = "tableName";
            String columnName = "columnname";
            String extensionName = "Extension_name";
            String definition = "definition";
            Scope scope = Scope.ReadWrite;
            
            Extension firstTime = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            Extension secondTime =  gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            
            assertTrue("When Trying to add the same extension twice, it did not return the values expected",
                       firstTime.equals(secondTime.getTableName(), 
                                        secondTime.getColumnName(), 
                                        secondTime.getExtensionName(), 
                                        secondTime.getDefinition(), 
                                        Scope.fromText(secondTime.getScope())));
            
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if GeoPackage Extensions will throw an IllegalArgumentException
     * when trying to add two extensions with the same tableName columnName and
     * extensionName but different definition and scope values
     * 
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionWithSameUniqueValuesButDifferentOtherValues() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            String tableName = null;
            String columnName = null;
            String extensionName = "Extension_name";
            String definition = "definition";
            Scope scope = Scope.ReadWrite;
            gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            
            String differentDefinition = "different definition";
            Scope differentScope = Scope.WriteOnly;
            
            gpkg.extensions().addExtension(tableName, columnName, extensionName, differentDefinition, differentScope);
            fail("Expected GeoPackage Extensions to throw an IllegalArgumentException when trying to add two extensions "
                    + "with the same tableName columnName and extensionName but different definition and scope values.");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if a GeoPackage method hasExtension
     * returns the proper values.
     * 
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void hasExtension() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(14);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            boolean hasExtensionShouldBeFalse = gpkg.extensions().hasExtension("extension_Name");
            
            gpkg.extensions().addExtension("tableName", "columnName", "extension_Name", "definition", Scope.ReadWrite);
            
            boolean hasExstensionShouldBeTrue = gpkg.extensions().hasExtension("extension_Name");
            
            assertTrue(String.format("The hasExtension did not return the expected values. When it should be true returned: %s, when should be false returned: %s", 
                                     hasExstensionShouldBeTrue, 
                                     hasExtensionShouldBeFalse),
                       hasExtensionShouldBeFalse == false  && hasExstensionShouldBeTrue == true);
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if the getExtensions method
     * returns all the extensions expected
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtensions() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           Extension extension1 = gpkg.extensions().addExtension(null,          null, "extension_name1", "definition", Scope.ReadWrite);
           Extension extension2 = gpkg.extensions().addExtension("table_name",  null, "extension_Name2", "definition", Scope.WriteOnly);
           Extension extension3 = gpkg.extensions().addExtension("table_name2", null, "extension_Name3", "definition", Scope.ReadWrite);
           
           List<Extension> extensionsExpected = new ArrayList<Extension>(Arrays.asList(extension1, extension2, extension3));
     
           Collection<Extension> extensionsReturned = gpkg.extensions().getExtensions();
           
           assertTrue("The method getExtensions did not return the extensions expected.",
                       extensionsReturned.stream()
                                         .allMatch(extensionReturned ->
                                                       extensionsExpected.stream()
                                                                         .anyMatch(extensionExpected -> 
                                                                                         extensionReturned.equals(extensionExpected.getTableName(), 
                                                                                                                  extensionExpected.getColumnName(), 
                                                                                                                  extensionExpected.getExtensionName(), 
                                                                                                                  extensionExpected.getDefinition(),
                                                                                                                  Scope.fromText(extensionExpected.getScope())))) && 
                      extensionsReturned.size() == 3);
                   
        }
        finally
        {
            deleteFile(testFile);
        }
    }
 
    /**
     * Tests if geoPackage Extensions
     * will return null when a geopackage
     * does not have any extensions using
     * the getExtensions method
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void getExtensionsWithNoExtensions() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
           Collection<Extension> shouldBeNull = gpkg.extensions().getExtensions();
           assertTrue("Expected GeoPackage Extensions to return an empty collection when there are no extensions "
                       + "or extensions table in this geopackage when using the method getExtensions.",
                      shouldBeNull.isEmpty());
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if the equals Method in Extension
     * returns the expected values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void testEqualsExtension() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
       File testFile = this.getRandomFile(9);
       
       try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
       {
            String tableName     = "table_name";
            String columnName    = null;
            String extensionName = "extension_name";
            String definition    = "definition";
            Scope  scope         = Scope.ReadWrite;
            
            Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            
            String columnName2    = "column_name";
            
            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName2, extensionName, definition, scope));
            
       }
       finally
       {
           deleteFile(testFile);
       }
    }
    
    /**
     * Tests if the equals Method in Extension
     * returns the expected values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void testEqualsExtension2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
       File testFile = this.getRandomFile(9);
       
       try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
       {
            String tableName     = "table_name";
            String columnName    = null;
            String extensionName = "extension_name";
            String definition    = "definition";
            Scope  scope         = Scope.ReadWrite;
            
            Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            
            Scope  scope2         = Scope.WriteOnly;
            
            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName, extensionName, definition, scope2));
            
       }
       finally
       {
           deleteFile(testFile);
       }
    }
    
    /**
     * Tests if the equals Method in Extension
     * returns the expected values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test
    public void testEqualsExtension3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
       File testFile = this.getRandomFile(9);
       
       try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
       {
            String tableName     = "table_name";
            String columnName    = "column_name";
            String extensionName = "extension_name";
            String definition    = "definition";
            Scope  scope         = Scope.ReadWrite;
            
            Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            
            String extensionName2 = "Different_extension_name";
            
            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName, extensionName2, definition, scope));
            
       }
       finally
       {
           deleteFile(testFile);
       }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void scopeFromTextIllegalArgumentException()
    {
        Scope.fromText("doesn't match anything");
        fail("Expected GeoPackage Extensions Scope class to throw an IllegalArgumentException since the text does not match either of the cases.");
    }

    private static void createExtensionsTable(File testFile) throws SQLException, ClassNotFoundException
    {
        String sql = "CREATE TABLE " + GeoPackageExtensions.ExtensionsTableName +
                     "(table_name     TEXT,          -- Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL.\n" +
                     " column_name    TEXT,          -- Name of the column that requires the extension. When NULL, the extension is required for the entire table.\n"                                                         +
                     " extension_name TEXT NOT NULL, -- The case sensitive name of the extension that is required, in the form <author>_<extension_name>.\n"                                                                  +
                     " definition     TEXT NOT NULL, -- Definition of the extension in the form specfied by the template in GeoPackage Extension Template (Normative) or reference thereto.\n"                                +
                     " scope          TEXT NOT NULL, -- Indicates scope of extension effects on readers / writers: read-write or write-only in lowercase.\n"                                                                  +
                     " CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))";
        try(Connection con = GeoPackageExtensionsAPITest.getConnection(testFile);
            Statement stmt = con.createStatement();)
        {
            stmt.executeUpdate(sql);
        }
    }
    

    private static Connection getConnection(File testFile) throws ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");   // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + testFile.getPath()); // Initialize the database connection
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
