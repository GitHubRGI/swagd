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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Luke Lambert
 */
@SuppressWarnings("JavaDoc")
public class BinaryHeaderTest
{
    /**
     * Constructor should fail on a null byte array
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullByteArray()
    {
        new BinaryHeader(null);
    }

    /**
     * Constructor should fail on a byte array that's too short
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorArrayTooShort()
    {
        final byte[] bytes = {(byte)71, (byte)80};

        new BinaryHeader(bytes);
    }

    /**
     * Constructor should fail on a byte array that doesn't begin with the bytes 'G' and 'P'
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorArrayDoesntStartWithMagicBytes()
    {
        final byte[] bytes = {(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0};

        new BinaryHeader(bytes);
    }

    /**
     * Constructor should fail when the header specifies more bytes exist in
     * the envelope than there are left in the array
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorArrayTooShortForEnvelopeIndicator()
    {
        final byte[] bytes = {(byte)71, (byte)80, (byte)0, (byte)0b00001000, (byte)0, (byte)0, (byte)0, (byte)0};

        new BinaryHeader(bytes);
    }

    /**
     * Tests that the bytes going in to the constructor are being correctly
     * parsed
     */
    @Test
    public void constructor()
    {
        final EnvelopeContentsIndicator envelopeContentsIndicator = EnvelopeContentsIndicator.Xyzm;

        final int byteSize = 2 +  // 2 bytes for the 'magic' header
                             1 +  // 1 byte for version
                             1 +  // 1 byte for flags
                             4 +  // 4 bytes (int32) for the srs id
                             (8 * envelopeContentsIndicator.getArraySize());   // 8 bytes per double, array size number of doubles

        final ByteBuffer byteBuffer = ByteBuffer.allocate(byteSize);

        // magic header
        byteBuffer.put((byte)71); // 'G'
        byteBuffer.put((byte)80); // 'P'

        final byte version = (byte)0; // Version "1"

        byteBuffer.put(version);

        final BinaryType binaryType = BinaryType.Extended;
        final Contents   contents   = Contents.Empty;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        final byte flags = (byte)(// skip the first two reserved bits
                                  binaryType.getBitMask()                  |
                                  contents.getBitMask()                    | // 1 = empty geometry
                                  envelopeContentsIndicator.getCode() << 1 | // 4 component envelope
                                  (byte)(Objects.equals(byteBuffer.order(), ByteOrder.BIG_ENDIAN) ? 0b00000000 : 0b00000001));

        byteBuffer.put(flags); // extended empty geometry with a 4 component envelope

        final int srsId = 4326;
        byteBuffer.putInt(srsId); // srs id

        final double[] envelope = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN}; // min x, max x, min y, max y, min z, max z, min m, max m

        byteBuffer.putDouble(envelope[0]); // min x
        byteBuffer.putDouble(envelope[1]); // max x
        byteBuffer.putDouble(envelope[2]); // min y
        byteBuffer.putDouble(envelope[3]); // max y
        byteBuffer.putDouble(envelope[4]); // min z
        byteBuffer.putDouble(envelope[5]); // max z
        byteBuffer.putDouble(envelope[6]); // min m
        byteBuffer.putDouble(envelope[7]); // max m

        final BinaryHeader binaryHeader = new BinaryHeader(byteBuffer.array());

        assertEquals("Version incorrectly set by the constructor",
                     version,
                     binaryHeader.getVersion());

        assertEquals("Binary type incorrectly set by the constructor",
                     binaryType,
                     binaryHeader.getBinaryType());

        assertEquals("Contents incorrectly set by the constructor",
                     contents,
                     binaryHeader.getContents());

        assertSame("Byte order incorrectly set by the constructor",
                   byteBuffer.order(),
                   binaryHeader.getByteOrder());

        assertEquals("Spatial reference system identifier incorrectly set by the constructor",
                     srsId,
                     binaryHeader.getSpatialReferenceSystemIdentifier());

        assertSame("Envelope contents indicator incorrectly set by the constructor",
                   envelopeContentsIndicator,
                   binaryHeader.getEnvelopeContentsIndicator());

        assertArrayEquals("Envelope array incorrectly set by the constructor",
                          envelope,
                          binaryHeader.getEnvelope(),
                          0.0);

        assertEquals("Flags incorrectly set by the constructor",
                     flags,
                     binaryHeader.getFlags());

        assertEquals("Constructor has incorrectly calculated the binary header's total byte size",
                     byteSize,
                     binaryHeader.getByteSize());
    }

    /**
     * Constructor should fail when binary type is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void binaryTypeIsNull()
    {
        new BinaryHeader((byte)1,
                         null,
                         Contents.NotEmpty,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         EnvelopeContentsIndicator.NoEnvelope,
                         emptyEnvelope);
    }

    /**
     * Constructor should fail when contents is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void contentsIsNull()
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         null,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         EnvelopeContentsIndicator.NoEnvelope,
                         emptyEnvelope);
    }

    /**
     * Constructor should fail when byte order is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void byteOrderIsNull()
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         Contents.NotEmpty,
                         null,
                         4326,
                         EnvelopeContentsIndicator.NoEnvelope,
                         emptyEnvelope);
    }

    /**
     * Constructor should fail when envelope contents indicator is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void envelopeContentsIndicatorIsNull()
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         Contents.NotEmpty,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         null,
                         emptyEnvelope);
    }

    /**
     * Constructor should fail when envelope is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void envelopeIsNull()
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         Contents.NotEmpty,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         EnvelopeContentsIndicator.NoEnvelope,
                         null);
    }

    /**
     * Constructor should fail when the envelope size doesn't match the contents indicator
     */
    @Test(expected = IllegalArgumentException.class)
    public void envelopeSizeDoesntMatchContentsIndicator()
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         Contents.NotEmpty,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         EnvelopeContentsIndicator.Xyzm,
                         emptyEnvelope);
    }

    /**
     * Constructor should fail when the envelope size doesn't match the contents indicator
     */
    @Test
    public void goodConstructorCall()
    {
        final byte                      version           = (byte)1;
        final BinaryType                binaryType        = BinaryType.Standard;
        final Contents                  contents          = Contents.NotEmpty;
        final ByteOrder                 byteOrder         = ByteOrder.BIG_ENDIAN;
        final int                       srsId             = 4326;
        final EnvelopeContentsIndicator envelopeIndicator = EnvelopeContentsIndicator.NoEnvelope;

        final BinaryHeader header = new BinaryHeader(version,
                                                     binaryType,
                                                     contents,
                                                     byteOrder,
                                                     srsId,
                                                     envelopeIndicator,
                                                     emptyEnvelope);

        assertEquals("Version incorrectly set by the constructor",
                     version,
                     header.getVersion());

        assertEquals("Binary type incorrectly set by the constructor",
                     binaryType,
                     header.getBinaryType());

        assertEquals("Contents incorrectly set by the constructor",
                     contents,
                     header.getContents());

        assertSame("Byte order incorrectly set by the constructor",
                   byteOrder,
                   header.getByteOrder());

        assertEquals("Spatial reference system identifier incorrectly set by the constructor",
                     srsId,
                     header.getSpatialReferenceSystemIdentifier());

        assertSame("Envelope contents indicator incorrectly set by the constructor",
                   envelopeIndicator,
                   header.getEnvelopeContentsIndicator());
    }

    /**
     * writeBytes() should fail on null ByteBuffer
     */
    @Test(expected = IllegalArgumentException.class)
    public void writeBytesFailsOnNullByteBuffer() throws IOException
    {
        new BinaryHeader((byte)1,
                         BinaryType.Standard,
                         Contents.NotEmpty,
                         ByteOrder.BIG_ENDIAN,
                         4326,
                         EnvelopeContentsIndicator.NoEnvelope,
                         emptyEnvelope).writeBytes(null);
    }

    /**
     * writeBytes() should produce a byte buffer that can be re-read to produce an identical header
     */
    @Test
    public void writeBytesRoundTrip() throws IOException
    {
        final double[] envelope = {0.0, 0.0, 0.0, 0.0};

        final BinaryHeader expected = new BinaryHeader((byte)1,
                                                       BinaryType.Standard,
                                                       Contents.NotEmpty,
                                                       ByteOrder.BIG_ENDIAN,
                                                       4326,
                                                       EnvelopeContentsIndicator.Xy,
                                                       envelope);

        final ByteOutputStream byteOutputStream = new ByteOutputStream(expected.getByteSize());

        expected.writeBytes(byteOutputStream);

        final BinaryHeader actual = new BinaryHeader(byteOutputStream.array());

        assertEquals("writeBytes() did not produce a correct BinaryHeader",
                     expected,
                     actual);
    }

    /**
     * Test that calling .equals on the same object returns true
     */
    @Test
    public void equalSameObject()
    {
        final BinaryHeader header = new BinaryHeader((byte)1,
                                                     BinaryType.Standard,
                                                     Contents.NotEmpty,
                                                     ByteOrder.BIG_ENDIAN,
                                                     4326,
                                                     EnvelopeContentsIndicator.NoEnvelope,
                                                     emptyEnvelope);

        assertEquals("Equals should have returned true for testing with the same object",
                     header,
                     header);
    }

    /**
     * Test that calling .equals on an object of a different type, fails
     */
    @Test
    public void equalDifferentType()
    {
        final BinaryHeader header = new BinaryHeader((byte)1,
                                                     BinaryType.Standard,
                                                     Contents.NotEmpty,
                                                     ByteOrder.BIG_ENDIAN,
                                                     4326,
                                                     EnvelopeContentsIndicator.NoEnvelope,
                                                     emptyEnvelope);

        //noinspection MisorderedAssertEqualsArguments
        assertNotEquals("Equals should have returned false for testing with a BinaryHeader with an object of a different type",
                        header,
                        new Object());
    }

    /**
     * Test that hashCode() for different BinaryHeaders with the same parameters produce identical results
     */
    @Test
    public void testHashCode()
    {
        final BinaryHeader header1 = new BinaryHeader((byte)1,
                                                      BinaryType.Standard,
                                                      Contents.NotEmpty,
                                                      ByteOrder.BIG_ENDIAN,
                                                      4326,
                                                      EnvelopeContentsIndicator.NoEnvelope,
                                                      emptyEnvelope);

        final BinaryHeader header2 = new BinaryHeader((byte)1,
                                                      BinaryType.Standard,
                                                      Contents.NotEmpty,
                                                      ByteOrder.BIG_ENDIAN,
                                                      4326,
                                                      EnvelopeContentsIndicator.NoEnvelope,
                                                      emptyEnvelope);

        assertEquals("Equals should have returned true for testing with the same object",
                     header1.hashCode(),
                     header2.hashCode());
    }

    /**
     * Static writeBytes() should fail on a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void staticWriteBytes() throws IOException
    {
        BinaryHeader.writeBytes(new ByteOutputStream(),
                                null,
                                4326);
    }

    /**
     * Static writeBytes() produce a BinaryHeader (round trip) with BinaryType,
     * Contents, and envelope that corresponds to the input geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void staticWriteBytesRoundTrip() throws IOException
    {
        BinaryHeader.writeBytes(new ByteOutputStream(),
                                null,
                                4326);
    }

    private static final double[] emptyEnvelope = {};
}
