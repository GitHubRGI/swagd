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
package com.rgi.common.tile.store.tms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.rules.TemporaryFolder;

@SuppressWarnings("javadoc")
public class TmsUtility
{
    public static Path createTMSFolderMercator(final TemporaryFolder tempFolder, final int zooms)
    {
        try
        {
            final File tmsFolder = tempFolder.newFolder(getRanString(8));
            for(int i = 0; i < zooms; i++)
            {
                for(int j = 0; j < Math.pow(2, i); j++)
                {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = tempFolder.newFolder(rowPath);
                    for(int k = 0; k < Math.pow(2, i); k++)
                    {
                        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                        final Path thisColumn = thisRow.toPath().resolve(String.valueOf(k) + ".png");
                        ImageIO.write(img, "PNG", thisColumn.toFile());
                    }
                }
            }
            return tmsFolder.toPath();
        }
        catch(final IOException ioException)
        {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    public static Path createTMSFolderGeodetic(final TemporaryFolder tempFolder, final int zooms)
    {
        try
        {
            final File tmsFolder = tempFolder.newFolder(getRanString(8));
            for(int i = 0; i < zooms; i++)
            {
                for(int j = 0; j < Math.pow(2, i); j++)
                {
                    final String[] rowPath = { tmsFolder.getName().toString(), String.valueOf(i), String.valueOf(j) };
                    final File thisRow = tempFolder.newFolder(rowPath);
                    for(int k = 0; k < Math.pow(2, (i - 1)); k++)
                    {
                        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                        final Path thisColumn = thisRow.toPath().resolve(String.valueOf(k) + ".png");
                        ImageIO.write(img, "PNG", thisColumn.toFile());
                    }
                }
            }
            return tmsFolder.toPath();
        }
        catch(final IOException ioException)
        {
            System.err.println("Could not generate TMS directory structure." + "\n" + ioException.getMessage());
            return null;
        }
    }

    private static String getRanString(final int length)
    {
        final Random randomGenerator = new Random();

        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for(int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }
}
