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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import utility.TestUtility;

import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.GeoPackage.OpenMode;
import com.rgi.android.geopackage.extensions.Extension;
import com.rgi.android.geopackage.extensions.GeoPackageExtensions;
import com.rgi.android.geopackage.extensions.Scope;
import com.rgi.android.geopackage.verification.ConformanceException;
import com.rgi.android.geopackage.verification.VerificationLevel;

@SuppressWarnings({"static-method", "javadoc"})
public class GeoPackageExtensionsAPITest
{
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
    public void getExtensionWithNullParameters() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);

        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String extensionName = "something_extension";

            final Extension expectedExtension = gpkg.extensions().addExtension(null, null, extensionName, "definition", Scope.ReadWrite); //this works fine

            final Extension returnedExtension = gpkg.extensions().getExtension(null, null, extensionName); //this does not



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
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getExtension2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String tableName = "TableName";
            final String columnName = "columnName";
            final String extensionName = "extension_Name";

            final Extension expectedExtension = gpkg.extensions().addExtension(tableName, columnName, extensionName, "definition", Scope.ReadWrite);
            final Extension returnedExtension = gpkg.extensions().getExtension(tableName, columnName, extensionName);

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
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getExtensionUsingExtensionGetters() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String extensionName = "something_extension";

            final Extension expectedExtension = gpkg.extensions().addExtension(null, null, extensionName, "definition", Scope.ReadWrite); //this works fine

            final Extension returnedExtension = gpkg.extensions().getExtension(null, null, extensionName); //this does not



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
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getExtension() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String tableName = "TableName";
            final String columnName = "columnName";
            final String extensionName = "extension_Name";

            final Extension expectedExtension = gpkg.extensions().addExtension(tableName, columnName, extensionName, "definition", Scope.ReadWrite);
            final Extension returnedExtension = gpkg.extensions().getExtension(tableName, columnName, extensionName);

            assertTrue(String.format("Did not return the expected Extension.\nExpected: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s.\nActual: table_name: %s, column_name: %s, extension_name: %s, definition: %s, Scope: %s. ",
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
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension(null, "ColumnNameShouldBeNull", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a null value for tableName and not columnName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension("", "columnName", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a empty string for tableName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(13);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension(null, "columnName", "extension_Name", "definition", Scope.ReadWrite);
            fail("Expected GeoPackageExtensions to throw an IllegalArgumentException when trying to add an extension with a empty string for tableName.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalARgumentException4() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(14);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension("", null, "extension_Name", "definition", Scope.WriteOnly);
            fail("Expected GeoPackageExtensions to throw an Illegal argument exception when column name is null and table name is an empty string.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException5() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(17);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
           gpkg.extensions().addExtension("TableName", "" , "extension_Name", "definition", Scope.ReadWrite);
           fail("Expected GeoPackageExtensions to throw an Illegal argument exception when column name and table name are empty strings.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException6() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension("tableName", "columnName", null, "definition", Scope.ReadWrite);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add an extension with a null value for extension name");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage will throw an IllegalArgumentException when trying to
     * add an extension with an emptry string for extension name.
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addExtensionIllegalArgumentException7() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension(null, null, "", "Definition", Scope.ReadWrite);
            fail("Expected GeoPackage to throw an IllegalArgumentException when trying to add an extension with an emptry string for extension name.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionIllegalArgumentException8() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(9);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            gpkg.extensions().addExtension("tablename", "columnName", "illegalExtensionName", "definition", Scope.WriteOnly);
            fail("Expected GeoPackage to throw an IllegalArgumentException when adding an extension that has an invalid extension name.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionWithoutNullParameters() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(12);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String tableName ="tablename";
            final String columnName = "columnName";
            final String extensionName ="extension_Name";
            final String definition = "definition";
            final Scope scope = Scope.ReadWrite;

            final Extension extensionReturned = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            assertTrue("The GeoPackage did not return the expected extension.",
                       extensionReturned.getTableName()       .equals(tableName)      &&
                       extensionReturned.getColumnName()      .equals(columnName)     &&
                       extensionReturned.getExtensionName()   .equals(extensionName)  &&
                       extensionReturned.getDefinition()      .equals(definition)     &&
                       extensionReturned.getScope().toString().equals(scope.toString()));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
        final File testFile = TestUtility.getRandomFile(8);
        testFile.createNewFile();
        //Create Extensions table
        GeoPackageExtensionsAPITest.createExtensionsTable(testFile);
        final GeoPackage gpkg = new GeoPackage(testFile, VerificationLevel.None, OpenMode.OpenOrCreate);

        try
        {
            final String tableName ="tablename";
            final String columnName = "columnName";
            final String extensionName ="extension_Name";
            final String definition = "definition";
            final Scope scope = Scope.ReadWrite;

            final Extension extensionReturned = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            assertTrue("The GeoPackage did not return the expected extension.",
                       extensionReturned.getTableName()       .equals(tableName)      &&
                       extensionReturned.getColumnName()      .equals(columnName)     &&
                       extensionReturned.getExtensionName()   .equals(extensionName)  &&
                       extensionReturned.getDefinition()      .equals(definition)     &&
                       extensionReturned.getScope().toString().equals(scope.toString()));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExistingExtension() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {

            final String tableName = "tableName";
            final String columnName = "columnname";
            final String extensionName = "Extension_name";
            final String definition = "definition";
            final Scope scope = Scope.ReadWrite;

            final Extension firstTime = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);
            final Extension secondTime =  gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            assertTrue("When Trying to add the same extension twice, it did not return the values expected",
                       firstTime.equals(secondTime.getTableName(),
                                        secondTime.getColumnName(),
                                        secondTime.getExtensionName(),
                                        secondTime.getDefinition(),
                                        Scope.fromText(secondTime.getScope())));

        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void addExtensionWithSameUniqueValuesButDifferentOtherValues() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final String tableName = null;
            final String columnName = null;
            final String extensionName = "Extension_name";
            final String definition = "definition";
            final Scope scope = Scope.ReadWrite;
            gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            final String differentDefinition = "different definition";
            final Scope differentScope = Scope.WriteOnly;

            gpkg.extensions().addExtension(tableName, columnName, extensionName, differentDefinition, differentScope);
            fail("Expected GeoPackage Extensions to throw an IllegalArgumentException when trying to add two extensions "
                    + "with the same tableName columnName and extensionName but different definition and scope values.");
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void hasExtension() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(14);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
            final boolean hasExtensionShouldBeFalse = gpkg.extensions().hasExtension("extension_Name");

            gpkg.extensions().addExtension("tableName", "columnName", "extension_Name", "definition", Scope.ReadWrite);

            final boolean hasExstensionShouldBeTrue = gpkg.extensions().hasExtension("extension_Name");

            assertTrue(String.format("The hasExtension did not return the expected values. When it should be true returned: %s, when should be false returned: %s",
                                     hasExstensionShouldBeTrue,
                                     hasExtensionShouldBeFalse),
                       hasExtensionShouldBeFalse == false  && hasExstensionShouldBeTrue == true);
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getExtensions() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(7);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
           final Extension extension1 = gpkg.extensions().addExtension(null,          null, "extension_name1", "definition", Scope.ReadWrite);
           final Extension extension2 = gpkg.extensions().addExtension("table_name",  null, "extension_Name2", "definition", Scope.WriteOnly);
           final Extension extension3 = gpkg.extensions().addExtension("table_name2", null, "extension_Name3", "definition", Scope.ReadWrite);

           final List<Extension> extensionsExpected = new ArrayList<Extension>(Arrays.asList(extension1, extension2, extension3));

           final Collection<Extension> extensionsReturned = gpkg.extensions().getExtensions();

           for(final Extension extensionReturned: extensionsReturned)
           {
               final boolean extensionMatches = FunctionalUtility.anyMatch(extensionsExpected,
                                                                     new Predicate<Extension>()
                                                                     {
                                                                          @Override
                                                                          public boolean apply(final Extension extensionExpected)
                                                                          {
                                                                              return extensionReturned.equals(extensionExpected.getTableName(),
                                                                                                              extensionExpected.getColumnName(),
                                                                                                              extensionExpected.getExtensionName(),
                                                                                                              extensionExpected.getDefinition(),
                                                                                                              Scope.fromText(extensionExpected.getScope()));
                                                                          }
                                                                     });

               assertTrue("The method getExtensions did not return the extensions expected.", extensionMatches && extensionsReturned.size() == 3);
           }
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void getExtensionsWithNoExtensions() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(8);
        final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

        try
        {
           final Collection<Extension> shouldBeNull = gpkg.extensions().getExtensions();
           assertTrue("Expected GeoPackage Extensions to return an empty collection when there are no extensions "
                       + "or extensions table in this geopackage when using the method getExtensions.",
                      shouldBeNull.isEmpty());
        }
        finally
        {
            gpkg.close();
            TestUtility.deleteFile(testFile);
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
    public void testEqualsExtension() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
       final File testFile = TestUtility.getRandomFile(9);
       final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

       try
       {
            final String tableName     = "table_name";
            final String columnName    = null;
            final String extensionName = "extension_name";
            final String definition    = "definition";
            final Scope  scope         = Scope.ReadWrite;

            final Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            final String columnName2    = "column_name";

            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName2, extensionName, definition, scope));

       }
       finally
       {
           gpkg.close();
           TestUtility.deleteFile(testFile);
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
    public void testEqualsExtension2() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
       final File testFile = TestUtility.getRandomFile(9);
       final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

       try
       {
            final String tableName     = "table_name";
            final String columnName    = null;
            final String extensionName = "extension_name";
            final String definition    = "definition";
            final Scope  scope         = Scope.ReadWrite;

            final Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            final Scope  scope2         = Scope.WriteOnly;

            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName, extensionName, definition, scope2));

       }
       finally
       {
           gpkg.close();
           TestUtility.deleteFile(testFile);
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
    public void testEqualsExtension3() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
       final File testFile = TestUtility.getRandomFile(9);
       final GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create);

       try
       {
            final String tableName     = "table_name";
            final String columnName    = "column_name";
            final String extensionName = "extension_name";
            final String definition    = "definition";
            final Scope  scope         = Scope.ReadWrite;

            final Extension extension  = gpkg.extensions().addExtension(tableName, columnName, extensionName, definition, scope);

            final String extensionName2 = "Different_extension_name";

            assertTrue("Expected equals method in Extension would return false when it returned true (when two extensions were not equal)",
                       !extension.equals(tableName, columnName, extensionName2, definition, scope));

       }
       finally
       {
           gpkg.close();
           TestUtility.deleteFile(testFile);
       }
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopeFromTextIllegalArgumentException()
    {
        Scope.fromText("doesn't match anything");
        fail("Expected GeoPackage Extensions Scope class to throw an IllegalArgumentException since the text does not match either of the cases.");
    }

    private static void createExtensionsTable(final File testFile) throws SQLException, ClassNotFoundException
    {
        final String sql = "CREATE TABLE " + GeoPackageExtensions.ExtensionsTableName +
                     "(table_name     TEXT,          -- Name of the table that requires the extension. When NULL, the extension is required for the entire GeoPackage. SHALL NOT be NULL when the column_name is not NULL.\n" +
                     " column_name    TEXT,          -- Name of the column that requires the extension. When NULL, the extension is required for the entire table.\n"                                                         +
                     " extension_name TEXT NOT NULL, -- The case sensitive name of the extension that is required, in the form <author>_<extension_name>.\n"                                                                  +
                     " definition     TEXT NOT NULL, -- Definition of the extension in the form specfied by the template in GeoPackage Extension Template (Normative) or reference thereto.\n"                                +
                     " scope          TEXT NOT NULL, -- Indicates scope of extension effects on readers / writers: read-write or write-only in lowercase.\n"                                                                  +
                     " CONSTRAINT ge_tce UNIQUE (table_name, column_name, extension_name))";

        final Connection con = TestUtility.getConnection(testFile);

        try
        {
            final Statement stmt = con.createStatement();

            try
            {
                stmt.executeUpdate(sql);
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




}
