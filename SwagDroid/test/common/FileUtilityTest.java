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
package common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.rgi.android.common.util.FileUtility;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
public class FileUtilityTest
{

    @Test
    public void nameWithoutExtensionVerify()
    {
        final File   file                     = new File("directory/nameOfFile.txt");
        final String fileNameWithoutExtension = FileUtility.nameWithoutExtension(file);
        this.assertStringsEqual(fileNameWithoutExtension, "nameOfFile");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        FileUtility.nameWithoutExtension(null);
        fail("Expected FileUtility method nameWithoutExtension(File file) to "
             + "throw an IllegalArgumentException when passed a null value for File.");
    }

    @Test
    public void appendForUniqueVerify()
    {
        final File fileExists = new File("TestFileName");
        try
        {
            fileExists.createNewFile();

            final String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            final String expectedFileName  = "TestFileName (1)";//expect append to only one time

            this.assertStringsEqual(expectedFileName, returnedFileName);
        } catch (final IOException e)
        {
            this.deleteFile(fileExists);
            e.printStackTrace();
        }
        finally
        {
            this.deleteFile(fileExists);
        }
    }

    @Test
    public void appendForUniqueVerify2()
    {
        final File fileExists  = new File("TestFileName");
        final File fileExists2 = new File("TestFileName (1)");
        try
        {
            //create two files
            fileExists.createNewFile();
            fileExists2.createNewFile();
            //get unique name
            final String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            //expected name
            final String expectedFileName  = "TestFileName (2)"; //expect append two times
            //verify equals
            this.assertStringsEqual(expectedFileName, returnedFileName);
        }
        catch (final IOException e)
        {
            this.deleteFile(fileExists);
            this.deleteFile(fileExists2);
            e.printStackTrace();
        }
        finally
        {
            this.deleteFile(fileExists);
            this.deleteFile(fileExists2);
        }
    }

    @Test
    public void appendForUniqueVerify3()
    {
        final File fileExists = new File("FileName.something.extension.gpkg");//test with multiple "."'s

        try
        {
            fileExists.createNewFile();

            final String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            final String expectedFileName = "FileName.something.extension (1).gpkg";

            this.assertStringsEqual(expectedFileName, returnedFileName);
        }
        catch (final IOException e)
        {
            this.deleteFile(fileExists);
            e.printStackTrace();
        }
        finally
        {
            this.deleteFile(fileExists);
        }
    }

    @Test
    public void appendForUniqueVerify4()
    {
        final File fileExists = new File(".gpkg"); //test if . is at the beginning

        try
        {
            fileExists.createNewFile();

            final String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            final String expectedFileName = "(1).gpkg";

            this.assertStringsEqual(expectedFileName, returnedFileName);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            this.deleteFile(fileExists);
        }
    }

    @Test
    public void appendForUniqueVerify5()
    {
        final File fileDoesNotExist = new File("returnTheSame.FileName"); //test if it will return the same name without alteration

        final String returnedFileName = FileUtility.appendForUnique(fileDoesNotExist.getName());
        final String expectedFileName = fileDoesNotExist.getName();

        this.assertStringsEqual(expectedFileName, returnedFileName);

    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        FileUtility.appendForUnique(null);
        fail("Expected FileUtility method appendForUnique(String) to throw an IllegalArgumentException when passing a null value for String.");
    }

    private void assertStringsEqual(final String expected, final String returned)
    {
        assertTrue(String.format("The appendForUnique did not return the expected value.\nExpected: %s.  Returned: %s",
                                 expected,
                                 returned),
                   expected.equals(returned));
    }

    private void deleteFile(final File fileExists)
    {
        if(fileExists.exists())
        {
            if(!fileExists.delete())
            {
                throw new RuntimeException("file was unable to be deleted");
            }
        }
    }
}
