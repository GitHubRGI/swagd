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

package com.rgi.common.util;

import java.io.File;

/**
 * A place to put File utility functions
 *
 * @author Luke Lambert
 *
 */
public class FileUtility
{
    /**
     * @param file
     *             File handle
     * @return The name of the file without its extension
     */
    public static String nameWithoutExtension(final File file)
    {
        if(file == null)
        {
            throw new IllegalArgumentException("File may not be null");
        }

        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    /**
     * Appends a number to a filename if the original name already exists.
     *
     * @param fileName
     *             Desired file name
     * @return A file name with a number appended before the extension if the
     *             original was already a file.
     */
    public static String appendForUnique(final String fileName)
    {
        String newFileName = fileName;

        for(int x = 0; new File(newFileName).exists(); ++x)
        {
            newFileName = fileName.replaceFirst("[.][^.]+$", String.format(" (%d).", x));
        }

        return newFileName;
    }
}
