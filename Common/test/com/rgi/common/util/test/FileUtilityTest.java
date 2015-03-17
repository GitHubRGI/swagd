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
        assertTrue(fileNameWithoutExtension.equals("nameOfFile"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        FileUtility.nameWithoutExtension(null);
        fail("Expected FileUtility method nameWithoutExtension(File file) to throw an IllegalArgumentException when passed a null value for File.");
    }
    
    @Test
    public void appendForUniqueVerify()
    {
        File fileExists = new File("TestFileName");
        try
        {
            fileExists.createNewFile();
            String returnedFileName = FileUtility.appendForUnique(fileExists.getName());
            String expetedFileName  = "TestFileName (1)";
            assertTrue(expetedFileName.equals(returnedFileName));
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
