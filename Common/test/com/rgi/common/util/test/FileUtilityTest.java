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
package com.rgi.common.util.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.rgi.common.util.FileUtility;

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
        File   file                     = new File("directory/nameOfFile.txt");
        String fileNameWithoutExtension = FileUtility.nameWithoutExtension(file);
        assertStringsEqual(fileNameWithoutExtension, "nameOfFile");
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
        File fileExists = new File("TestFileName"); 
        try
        {
            fileExists.createNewFile();
            
            String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            String expectedFileName  = "TestFileName (1)";//expect append to only one time
            
            assertStringsEqual(expectedFileName, returnedFileName);
        } catch (IOException e)
        {
            deleteFile(fileExists);
            e.printStackTrace();
        }
        finally
        {
            deleteFile(fileExists);
        }
    }
    
    @Test
    public void appendForUniqueVerify2()
    {
        File fileExists  = new File("TestFileName");
        File fileExists2 = new File("TestFileName (1)");
        try
        {
            //create two files
            fileExists.createNewFile();
            fileExists2.createNewFile();
            //get unique name
            String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            //expected name
            String expectedFileName  = "TestFileName (2)"; //expect append two times
            //verify equals
            assertStringsEqual(expectedFileName, returnedFileName);
        } 
        catch (IOException e)
        {
            deleteFile(fileExists);
            deleteFile(fileExists2);
            e.printStackTrace();
        }
        finally
        {
            deleteFile(fileExists);
            deleteFile(fileExists2);
        }
    }

    @Test
    public void appendForUniqueVerify3()
    {
        File fileExists = new File("FileName.something.extension.gpkg");//test with multiple "."'s
        
        try
        {
            fileExists.createNewFile();
            
            String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            String expectedFileName = "FileName.something.extension (1).gpkg";
            
            assertStringsEqual(expectedFileName, returnedFileName);
        }
        catch (IOException e)
        {
            deleteFile(fileExists);
            e.printStackTrace();
        }
        finally
        {
            deleteFile(fileExists);
        }
    }
    
    @Test
    public void appendForUniqueVerify4()
    {
        File fileExists = new File(".gpkg"); //test if . is at the beginning
        
        try
        {
            fileExists.createNewFile();
            
            String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            String expectedFileName = "(1).gpkg";
            
            assertStringsEqual(expectedFileName, returnedFileName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }
        finally
        {
            deleteFile(fileExists);
        }
    }
    
    @Test
    public void appendForUniqueVerify5()
    {
        File fileDoesNotExist = new File("returnTheSame.FileName"); //test if it will return the same name without alteration
            
        String returnedFileName = FileUtility.appendForUnique(fileDoesNotExist.getName());
        String expectedFileName = fileDoesNotExist.getName();
        
        assertStringsEqual(expectedFileName, returnedFileName);
        
    }
    
    @Test
    public void illegalArgumentException2()
    {
        FileUtility.appendForUnique(null);
        fail("Expected FileUtility method appendForUnique(String) to throw an IllegalArgumentException when passing a null value for String.");
    }
    
    private void assertStringsEqual(String expected, String returned)
    {
        assertTrue(String.format("The appendForUnique did not return the expected value.\nExpected: %s.  Returned: %s",
                                 expected, 
                                 returned),
                   expected.equals(returned));
    }
    
    private void deleteFile(File fileExists)
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
