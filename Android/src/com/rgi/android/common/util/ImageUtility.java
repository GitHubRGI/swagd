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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * Utility methods to convert back and forth between <code>byte[]</code>s and
 * {@link BufferedImage}s
 *
 * @author Luke Lambert
 *
 */
public class ImageUtility
{
    /**
     * Converts a {@link BufferedImage} into bytes using a specific image
     * writer, and image write parameters
     *
     * @param bufferedImage
     *             The {@link BufferedImage} to be converted to bytes
     * @param imageWriter
     *             Writer responsible for the conversion
     * @param imageWriteParameter
     *             Controls image writing parameters, e.g. transparency, compression, etc
     * @return The image as an array of bytes
     * @throws IOException
     *             Throws if image writing fails
     */
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

        final ByteArrayOutputStream        outputStream     = new ByteArrayOutputStream();
        final MemoryCacheImageOutputStream memoryCacheImage = new MemoryCacheImageOutputStream(outputStream);

        try
        {
            imageWriter.setOutput(memoryCacheImage);

            imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParameter);

            outputStream.flush();

            return outputStream.toByteArray();
        }
        finally
        {
            outputStream.close();
            memoryCacheImage.close();
        }
    }

    /**
     * Converts a {@link BufferedImage} into bytes using an image writer that
     * corresponds to the specified output format
     *
     * @param bufferedImage
     *             The {@link BufferedImage} to be converted to bytes
     * @param outputFormat
     *             A string containing the informal name of the format.  See {@link ImageIO#write(RenderedImage, String, OutputStream)}
     * @return The image as an array of bytes
     * @throws IOException
     *             Throws if image writing fails
     */
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

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try
        {
            if(!ImageIO.write(bufferedImage, outputFormat, outputStream))
            {
                throw new IOException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            return outputStream.toByteArray();
        }
        finally
        {
            outputStream.close();
        }
    }

    /**
     * Converts an image as an array of bytes to a {@link BufferedImage}
     *
     * @param imageData
     *             The image as an array of bytes
     * @return A {@link BufferedImage}
     * @throws IOException If an error occurs in reading the image
     */
    public static BufferedImage bytesToBufferedImage(final byte[] imageData) throws IOException
    {
        if(imageData == null)
        {
            throw new IllegalArgumentException("Image data may not be null");
        }

        final ByteArrayInputStream imageInputStream = new ByteArrayInputStream(imageData);

        try
        {
            final BufferedImage bufferedImage = ImageIO.read(imageInputStream);

            if(bufferedImage == null)
            {
                throw new IOException("Image data is corrupt or in an unknown format");
            }

            return bufferedImage;
        }
        finally
        {
            imageInputStream.close();
        }
    }

    /**
     * Writes text on a {@link BufferedImage} with a red border around the image
     *
     * @param oldImage
     *             The image that is to be written on
     * @param text
     *             The words that are to be written on the image
     * @return A {@link BufferedImage} with text written on the image
     */
    public static BufferedImage graffiti(final BufferedImage oldImage, final String text)
    {
        final int width  = oldImage.getWidth();
        final int height = oldImage.getHeight();

        final BufferedImage newImage = new BufferedImage(width,
                                                         height,
                                                         oldImage.getType());

        final Graphics2D brush = newImage.createGraphics();
        brush.drawImage(oldImage, 0, 0, null);

        brush.setColor(Color.red);
        brush.drawLine(      0,        0,  width-1,        0);
        brush.drawLine(width-1,        0,  width-1, height-1);
        brush.drawLine(width-1, height-1,        0, height-1);
        brush.drawLine(      0, height-1,        0,        0);

        brush.setPaint(Color.blue);
        brush.setFont(new Font("Serif", Font.BOLD, 20));

        //brush.clearRect(0, 0, width, height);

        final FontMetrics fm = brush.getFontMetrics();

        final String[] parts = text.split("\n");
        for(int part = 0; part < parts.length; ++part)
        {
            final int x = 2;//bufferedImage.getWidth() - fm.stringWidth(text) - 5;
            final int y = fm.getHeight();
            brush.drawString(parts[part], x, y*(part+1));
        }
        brush.dispose();

        return newImage;
    }

}
