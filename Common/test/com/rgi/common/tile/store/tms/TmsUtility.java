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
