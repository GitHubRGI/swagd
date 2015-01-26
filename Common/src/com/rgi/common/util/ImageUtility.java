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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtility
{
    public static byte[] bufferedImageToBytes(final BufferedImage bufferedImage, final String outputFormat) throws IOException
    {
        try(@SuppressWarnings("resource") final ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            if(!ImageIO.write(bufferedImage, outputFormat, outputStream))
            {
                throw new IOException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            return outputStream.toByteArray();
        }
    }

    public static BufferedImage bytesToBufferedImage(final byte[] imageData) throws IOException
    {
        try(ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageData))
        {
            BufferedImage bufferedImage = ImageIO.read(imageInputStream);

            if(bufferedImage == null)
            {
                throw new IOException("Image data is corrupt or in an unknown format");
            }

            return bufferedImage;
        }
    }
}
