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
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.verification.ConformanceException;

public class GeoPackageExtensionsAPITest
{

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();

    @Test
    public void hasExtension() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
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
            GeoPackageExtensionsAPITest.deleteFile(testFile);
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

            //TODO bug in extensions equals method.  line 104 using .equals for scope and .equals is not overwritten
        }
        finally
        {
            GeoPackageExtensionsAPITest.deleteFile(testFile);
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
            GeoPackageExtensionsAPITest.deleteFile(testFile);
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
            GeoPackageExtensionsAPITest.deleteFile(testFile);
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
            GeoPackageExtensionsAPITest.deleteFile(testFile);
        }
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
