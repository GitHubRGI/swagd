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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageUtility
{
    public static byte[] bufferedImageToBytes(final BufferedImage bufferedImage, final ImageWriter imageWriter, final ImageWriteParam imageWriteParameter) throws IOException
    {
        if(bufferedImage == null)
        {
            throw new IllegalArgumentException("Buffered image may not be null");
        }

        if(imageWriter == null)
        {
            throw new IllegalArgumentException("Image writer may not be null");
        }

        if(imageWriteParameter == null)
        {
            throw new IllegalArgumentException("Image write parameter may not be null");
        }

        try(final ByteArrayOutputStream        outputStream     = new ByteArrayOutputStream();
            final MemoryCacheImageOutputStream memoryCacheImage = new MemoryCacheImageOutputStream(outputStream))
        {
            imageWriter.setOutput(memoryCacheImage);

            imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParameter);

            outputStream.flush();

            return outputStream.toByteArray();
        }
    }

    public static byte[] bufferedImageToBytes(final BufferedImage bufferedImage, final String outputFormat) throws IOException
    {
        if(bufferedImage == null)
        {
            throw new IllegalArgumentException("Buffered image may not be null");
        }

        if(outputFormat == null)
        {
            throw new IllegalArgumentException("Output format may not be null");
        }

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
        if(imageData == null)
        {
            throw new IllegalArgumentException("Output format may not be null");
        }

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
