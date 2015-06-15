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
package com.rgi.android.common.util;

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
     * @param filename
     *             Desired file name
     * @return A file name with a number appended before the extension if the
     *             original was already a file.
     */
    public static String appendForUnique(final String filename)
    {
        if(filename == null)
        {
            throw new IllegalArgumentException("Filename may not be null");
        }

        String newFileName = filename;

        for(int x = 1; new File(newFileName).exists(); ++x)
        {
            final int index = filename.lastIndexOf(".");

            if(index == -1)
            {
                newFileName = String.format("%s (%d)", filename, x);
            }
            else if(index == 0)
            {
                newFileName = String.format("(%d)%s", x, filename);
            }
            else
            {
                newFileName = String.format("%s (%d)%s",
                                            filename.substring(0, index),
                                            x,
                                            filename.substring(index));
            }
        }

        return newFileName;
    }
}
