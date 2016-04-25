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

package com.rgi.geopackage.features;

import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class ByteOutputStreamTest
{
    /**
     * Empty constructor passes
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        //noinspection EmptyTryBlock
        try(final ByteOutputStream ignored = new ByteOutputStream())
        {

        }
    }

    /**
     * Constructor with a negative capacity should fail
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorWithNegativeCapacity()
    {
        //noinspection EmptyTryBlock
        try(final ByteOutputStream ignored = new ByteOutputStream(-1))
        {

        }
    }

    /**
     * .array() should return the correct value
     */
    @Test
    public void array()
    {
        try(final ByteOutputStream byteOutputStream1 = new ByteOutputStream(1))
        {
            final byte byte1 = (byte)42;

            byteOutputStream1.write(byte1);

            assertArrayEquals("Array returned an incorrect value",
                              new byte[] { byte1 },
                              byteOutputStream1.array());
        }

        try(final ByteOutputStream byteOutputStream2 = new ByteOutputStream())
        {
            final byte[] emptyArray = {};

            assertArrayEquals("Array returned an incorrect value",
                              emptyArray,
                              byteOutputStream2.array());
        }
    }

    /**
     * Write a byte
     */
    @Test
    public void writeByte()
    {
        try(final ByteOutputStream byteOutputStream1 = new ByteOutputStream(1))
        {
            final byte byte1 = (byte)42;

            byteOutputStream1.write(byte1);

            assertArrayEquals(".write(byte) failed",
                              new byte[] { byte1 },
                              byteOutputStream1.array());
        }
    }

    /**
     * Write a short
     */
    @Test
    public void writeShort()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final short s = (short)7;

            final byte[] array = { (byte)(s >> 8),
                                   (byte)(s     )
                                 };

            byteOutputStream.write(s);

            assertArrayEquals(".write(short) (big endian) failed",
                              array,
                              byteOutputStream.array());
        }

        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final short s = (short)7;

            final byte[] array = { (byte)(s     ),
                                   (byte)(s >> 8)
                                 };

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            byteOutputStream.write(s);

            assertArrayEquals(".write(short) (little endian) failed",
                              array,
                              byteOutputStream.array());
        }
    }

    /**
     * Write a int
     */
    @Test
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public void writeInt()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final int i = 0x12345678;

            final byte[] array = { (byte)(i >> 24),
                                   (byte)(i >> 16),
                                   (byte)(i >>  8),
                                   (byte)(i      )
                                 };

            byteOutputStream.write(i);

            assertArrayEquals(".write(int) (big endian) failed",
                              array,
                              byteOutputStream.array());
        }

        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final int i = 0x12345678;

            final byte[] array = { (byte)(i      ),
                                   (byte)(i >>  8),
                                   (byte)(i >> 16),
                                   (byte)(i >> 24)
                                 };

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            byteOutputStream.write(i);

            assertArrayEquals(".write(int) (little endian) failed",
                              array,
                              byteOutputStream.array());
        }
    }

    /**
     * Write a float
     */
    @Test
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public void writeFloat()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final float f = 0x12345678;

            final int i = Float.floatToRawIntBits(f);

            final byte[] array = { (byte)(i >> 24),
                                   (byte)(i >> 16),
                                   (byte)(i >>  8),
                                   (byte)(i      )
                                 };

            byteOutputStream.write(f);

            assertArrayEquals(".write(float) (big endian) failed",
                              array,
                              byteOutputStream.array());
        }

        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final float f = 0x12345678;

            final int i = Float.floatToRawIntBits(f);

            final byte[] array = { (byte)(i      ),
                                   (byte)(i >>  8),
                                   (byte)(i >> 16),
                                   (byte)(i >> 24)
                                 };

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            byteOutputStream.write(f);

            assertArrayEquals(".write(float) (little endian) failed",
                              array,
                              byteOutputStream.array());
        }
    }

    /**
     * Write a long
     */
    @Test
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public void writeLong()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final long l = Long.MAX_VALUE;

            final byte[] array = { (byte)(l >> 56),
                                   (byte)(l >> 48),
                                   (byte)(l >> 40),
                                   (byte)(l >> 32),
                                   (byte)(l >> 24),
                                   (byte)(l >> 16),
                                   (byte)(l >>  8),
                                   (byte)(l      )
                                 };

            byteOutputStream.write(l);

            assertArrayEquals(".write(long) (big endian) failed",
                              array,
                              byteOutputStream.array());
        }

        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final long l = Long.MAX_VALUE;

            final byte[] array = { (byte)(l      ),
                                   (byte)(l >>  8),
                                   (byte)(l >> 16),
                                   (byte)(l >> 24),
                                   (byte)(l >> 32),
                                   (byte)(l >> 40),
                                   (byte)(l >> 48),
                                   (byte)(l >> 56)
                                 };

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            byteOutputStream.write(l);

            assertArrayEquals(".write(float) (little endian) failed",
                              array,
                              byteOutputStream.array());
        }
    }

    /**
     * Write a double
     */
    @Test
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public void writeDouble()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final double d = Long.MAX_VALUE;

            final long l = Double.doubleToRawLongBits(d);

            final byte[] array = { (byte)(l >> 56),
                                   (byte)(l >> 48),
                                   (byte)(l >> 40),
                                   (byte)(l >> 32),
                                   (byte)(l >> 24),
                                   (byte)(l >> 16),
                                   (byte)(l >>  8),
                                   (byte)(l      )
                                 };

            byteOutputStream.write(d);

            assertArrayEquals(".write(double) (big endian) failed",
                              array,
                              byteOutputStream.array());
        }

        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            final double d = Long.MAX_VALUE;

            final long l = Double.doubleToRawLongBits(d);

            final byte[] array = { (byte)(l      ),
                                   (byte)(l >>  8),
                                   (byte)(l >> 16),
                                   (byte)(l >> 24),
                                   (byte)(l >> 32),
                                   (byte)(l >> 40),
                                   (byte)(l >> 48),
                                   (byte)(l >> 56)
                                 };

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            byteOutputStream.write(d);

            assertArrayEquals(".write(double) (little endian) failed",
                              array,
                              byteOutputStream.array());
        }
    }

    /**
     * .write(byte[]) should fail on a null byte array
     */
    @Test(expected = IllegalArgumentException.class)
    public void byteArrayBad()
    {
        try(final ByteOutputStream byteOutputStream1 = new ByteOutputStream(1))
        {
            byteOutputStream1.write(null);

            fail("Write byte array should fail on null byte array");
        }
    }

    /**
     * Write an array of bytes
     */
    @Test
    public void byteArrayGood()
    {
        try(final ByteOutputStream byteOutputStream1 = new ByteOutputStream(1))
        {
            final byte[] bytes = { (byte)0, (byte)1, (byte)2, (byte)3 };

            byteOutputStream1.write(bytes);

            assertArrayEquals("Write byte array wrote a wrong value",
                              bytes,
                              byteOutputStream1.array());
        }
    }

    /**
     * Get byte order
     */
    @Test
    public void getByteOrder()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            assertSame("getByteOrder failed to return the correct value",
                       ByteOrder.BIG_ENDIAN,
                       byteOutputStream.getByteOrder());

            byteOutputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            assertSame("getByteOrder failed to return the correct value",
                       ByteOrder.LITTLE_ENDIAN,
                       byteOutputStream.getByteOrder());
        }
    }

    /**
     * Set byte order fails on null
     */
    @Test(expected = IllegalArgumentException.class)
    public void setByteOrder()
    {
        try(final ByteOutputStream byteOutputStream = new ByteOutputStream(1))
        {
            byteOutputStream.setByteOrder(null);

            fail("setByteOrder failed to throw on a null ByteOrder");
        }
    }
}
