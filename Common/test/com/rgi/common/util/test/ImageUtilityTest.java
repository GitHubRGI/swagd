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

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import org.junit.Test;

import com.rgi.common.util.ImageUtility;

/**
 * @author Jenifer Cochran
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
