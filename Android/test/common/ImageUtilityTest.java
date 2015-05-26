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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import org.junit.Test;

import com.rgi.android.common.util.ImageUtility;

/**
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
public class ImageUtilityTest
{
    @Test
    public void bufferedImageToBytesBackToBufferedImageVerify() throws IOException
    {
        final BufferedImage imageExpected = new BufferedImage(256, 512, BufferedImage.TYPE_BYTE_GRAY);
        final byte[]        returnedBytes = ImageUtility.bufferedImageToBytes(imageExpected, "png");
        final BufferedImage imageReturned = ImageUtility.bytesToBufferedImage(returnedBytes);

        assertTrue("The buffered image created from ImageUtility does not have the expected values.",
                   bufferedImagesEqual(imageExpected, imageReturned));
    }

    @Test
    public void bufferedImageToBytesBackToBufferedImageVerify2() throws IOException, MimeTypeParseException
    {
        final BufferedImage imageExpected = new BufferedImage(256, 512, BufferedImage.TYPE_BYTE_BINARY);
        final byte[]        returnedBytes = ImageUtility.bufferedImageToBytes(imageExpected, ImageIO.getImageWritersByMIMEType(new MimeType("image/jpeg").toString()).next(), null);
        final BufferedImage imageReturned = ImageUtility.bytesToBufferedImage(returnedBytes);

        assertTrue("The buffered image created from ImageUtility does not have the expected values.",
                   bufferedImagesEqual(imageExpected, imageReturned));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException() throws IOException
    {
        ImageUtility.bufferedImageToBytes(null, "png");
        fail("Expected ImageUtility method bufferedImageToBytes(BufferedImage, String) to throw an IllegalArgumentException when passing a null value for buffered image.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2() throws IOException
    {
        ImageUtility.bufferedImageToBytes(new BufferedImage(256, 512, BufferedImage.TYPE_3BYTE_BGR), null);
        fail("Expected ImageUtility method bufferedImageToBytes(BufferedImage, String) to throw an IllegalArgumentException when passing a null value for String.");
    }

    @Test(expected = IOException.class)
    public void iOException() throws IOException
    {
        ImageUtility.bufferedImageToBytes(new BufferedImage(256, 512, BufferedImage.TYPE_3BYTE_BGR), "");
        fail("Expected ImageUtility method bufferedImageToBytes(BufferedImage, String) to throw an IOException when passing an empty string for outputFormat.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3() throws IOException
    {
        ImageUtility.bufferedImageToBytes(null, ImageIO.getImageWritersByFormatName("jpeg").next(), null);
        fail("Expected ImageUtility method bufferedImageToBytes(BufferedImage, ImageWriter, ImageWriteParam) to throw an IllegalArgumentException when passing a null value for BufferedImage.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4() throws IOException
    {
        ImageUtility.bufferedImageToBytes(new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_GRAY), null, null);
        fail("Expected ImageUtility method bufferedImageToBytes(BufferedImage, ImageWriter, ImageWriteParam) to throw an IllegalArgumentException when passing a null value for ImageWriter.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5() throws IOException
    {
        ImageUtility.bytesToBufferedImage(null);
        fail("Expected ImageUtility method bytesToBufferedImage(byte[]) to throw an IllegalArgumentException when passing a null value for Image data.");
    }

    @Test(expected = IOException.class)
    public void iOException2() throws IOException
    {
        ImageUtility.bytesToBufferedImage(new byte[]{1,2,3,4,5});
        fail("Expected ImageUtility method bytesToBufferedImage(byte[]) to throw an IOException when passing a bad image data.");
    }

    /**
     * Tests that graffiti appropriately modifies the
     * given Buffered Image
     */
    @Test
    public void graffitiVerify()
    {
         final BufferedImage imageExpected = new BufferedImage(256, 512, BufferedImage.TYPE_BYTE_GRAY);
         final BufferedImage oldImage = new BufferedImage(256, 512, BufferedImage.TYPE_BYTE_GRAY);

        final int width  = imageExpected.getWidth();
        final int height = imageExpected.getHeight();
        final String text = "Testing!";
        final BufferedImage imageReturned = ImageUtility.graffiti(imageExpected, text);

        final Graphics2D brush = imageExpected.createGraphics();
        brush.drawImage(oldImage, 0, 0, null);
        brush.setColor(Color.red);
        brush.drawLine(      0,        0,  width-1,        0);
        brush.drawLine(width-1,        0,  width-1, height-1);
        brush.drawLine(width-1, height-1,        0, height-1);
        brush.drawLine(      0, height-1,        0,        0);

        brush.setPaint(Color.blue);
        brush.setFont(new Font("Serif", Font.BOLD, 20));

        final FontMetrics fm = brush.getFontMetrics();

        final String[] parts = text.split("\n");
        for(int part = 0; part < parts.length; ++part)
        {
            final int x = 2;
            final int y = fm.getHeight();
            brush.drawString(parts[part], x, y*(part+1));
        }
        brush.dispose();
        assertTrue("The buffered image created from graffiti does not have the expected values.", bufferedImagesEqual(imageExpected, imageReturned));
    }

    private static boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2)
    {
        if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
        {
            return false;
        }

        for(int x = 0; x < img1.getWidth(); x++)
        {
            for(int y = 0; y < img1.getHeight(); y++)
            {
                if(img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
