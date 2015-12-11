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

import com.rgi.geopackage.features.geometry.Geometry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Java implementation of the <a
 * href="http://www.geopackage.org/spec/#gpb_spec">GeoPackage Binary
 * Header</a>
 *
 * @author Luke Lambert
 */
public class BinaryHeader
{
    /**
     *
     * @param bytes
     */
    protected BinaryHeader(final byte[] bytes)
    {
        if(bytes == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        if(bytes.length < 8)
        {
            throw new IllegalArgumentException("Byte buffer must be at least 8 bytes to contain a valid GeoPackage geometry binary header");
        }

        if(bytes[0] != magic[0] ||
           bytes[1] != magic[1])
        {
            throw new IllegalArgumentException("The first two bytes of a GeoPackage geometry binary header must be 'G', 'P'");
        }

        this.version = bytes[2];
        this.flags   = bytes[3];

        // read flags
        this.binaryType = BinaryType.type(bytes[3]);
        this.empty = ((bytes[3] << 4) & 1) == 1;

        this.byteOrder  = ((this.flags & 1) == 0) ? ByteOrder.BIG_ENDIAN
                                                  : ByteOrder.LITTLE_ENDIAN;

        final ByteBuffer srsIdByteBuffer = ByteBuffer.wrap(bytes, 4, 4); // Bytes 5->9 are int32 srs_id
        srsIdByteBuffer.order(this.byteOrder);
        this.spatialReferenceSystemIdentifier = srsIdByteBuffer.getInt();

        final EnvelopeContentsIndicator envelopeContentsIndicator = EnvelopeContentsIndicator.fromCode((this.flags & 0b00001110) >> 1);

        this.byteSize = 2 +  // 2 bytes for the 'magic' header
                        1 +  // 1 byte for version
                        1 +  // 1 byte for flags
                        4 +  // 4 bytes (int32) for the srs id
                        (8 * envelopeContentsIndicator.getArraySize());   // 8 bytes per double, array size number of doubles

        if(bytes.length < this.byteSize)
        {
            throw new IllegalArgumentException("Byte array length is shorter than the envelope size would indicate");
        }

        this.envelope = new Envelope(envelopeContentsIndicator,
                                     getHeaderEnvelopeDoubles(bytes,
                                                              this.byteOrder,
                                                              envelopeContentsIndicator.getArraySize()));

    }

    protected BinaryHeader(final byte       version,
                           final BinaryType binaryType,
                           final Contents   contents,
                           final ByteOrder  byteOrder,
                           final int        spatialReferenceSystemIdentifier,
                           final Envelope   envelope)
    {
        if(binaryType == null)
        {
            throw new IllegalArgumentException("Binary type may not be null");
        }

        if(contents == null)
        {
            throw new IllegalArgumentException("Contents enumeration may not be null");
        }

        if(byteOrder == null)
        {
            throw new IllegalArgumentException("Byte order may not be null");
        }

        if(envelope == null)
        {
            throw new IllegalArgumentException("Envelope may not be null. Use Envelope.Empty to represent an empty envelope.");
        }

        this.version                          = version;
        this.binaryType                       = binaryType;
        this.empty                            = contents == Contents.Empty;
        this.byteOrder                        = byteOrder;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
        this.envelope                         = envelope;

        final int isEmptyMask          = this.empty ? (1 << 4) : 0; // TODO make this part of the Contents enum like BinaryType?
        final int envelopeContentsMask = (byte)(envelope.getContentsIndicator().getCode() << 1);

        this.flags = // 0                         |
                     this.binaryType.getBitMask() |
                     isEmptyMask                  |
                     envelopeContentsMask         |
                     (byteOrder.equals(ByteOrder.BIG_ENDIAN) ? 0 : 1);

        this.byteSize = 2 +  // 2 bytes for the 'magic' header
                        1 +  // 1 byte for version
                        1 +  // 1 byte for flags
                        4 +  // 4 bytes (int32) for the srs id
                        (8 * this.envelope.getContentsIndicator().getArraySize());   // 8 bytes per double, array size number of doubles
    }

    public int getSpatialReferenceSystemIdentifier()
    {
        return this.spatialReferenceSystemIdentifier;
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    public int getByteSize()
    {
        return this.byteSize;
    }

    public void writeBytes(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        // http://www.geopackage.org/spec/#gpb_spec

        // "magic"
        byteArrayOutputStream.write(ByteBuffer.wrap(BinaryHeader.magic)
                                              .order(this.byteOrder)
                                              .array());

        // version
        byteArrayOutputStream.write(this.version);  // https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html#write-int-
                                                    // "Writes the specified byte to this output stream. The general contract for write is that one byte is written to the output stream. The byte to be written is the eight low-order bits of the argument b. The 24 high-order bits of b are ignored."
                                                    // TODO I /believe/ this correctly also handles the signed int -> unsigned byte version, but it's worth double checking

        // flags
        byteArrayOutputStream.write(this.flags);    // This is also a single byte write

        // spatial reference system id, signed int
        byteArrayOutputStream.write(ByteBuffer.allocate(4)
                                              .putInt(this.spatialReferenceSystemIdentifier)
                                              .order(this.byteOrder)
                                              .array());

        for(final double envelopeBound : this.envelope.getArray())
        {
            byteArrayOutputStream.write(ByteBuffer.allocate(8)
                                                  .putDouble(envelopeBound)
                                                  .order(this.byteOrder)
                                                  .array());
        }
    }

    public static void writeBytes(final ByteArrayOutputStream byteArrayOutputStream,
                                  final Geometry              geometry,
                                  final int                   spatialReferenceSystemIdentifier) throws IOException
    {
        new BinaryHeader(defaultVersion,
                         BinaryType.fromGeometryTypeName(geometry.getGeometryTypeName()),
                         geometry.getContents(),
                         defaultByteOrder,
                         spatialReferenceSystemIdentifier,
                         geometry.createEnvelope()).writeBytes(byteArrayOutputStream);

    }

    private static double[] getHeaderEnvelopeDoubles(final byte[]    header,
                                                     final ByteOrder byteOrder,
                                                     final int       numberOfDoubles)
    {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(header,
                                                      8,                   // Envelope starts after the first 8 bytes
                                                      8*numberOfDoubles);  // 8 bytes per double
        byteBuffer.order(byteOrder);

        final double[] envelope = new double[numberOfDoubles];

        for(int x = 0; x < numberOfDoubles; ++x)
        {
            envelope[x] = byteBuffer.getDouble();
        }

        return envelope;
    }

    private static final byte      defaultVersion = (byte)0;                // Confusingly, 0 = "version 1", see: http://www.geopackage.org/spec/#gpb_spec
    private static final ByteOrder defaultByteOrder = ByteOrder.BIG_ENDIAN; // Java default (?), also the network byte order

    private static final byte[] magic = {(byte)71,// 'G'
                                         (byte)80 // 'P'
                                        };

    private final byte       version; // This is an *unsigned* value, regardless of Java's interpretation
    private final BinaryType binaryType;
    private final boolean    empty;
    private final ByteOrder  byteOrder;
    private final int        spatialReferenceSystemIdentifier;
    private final Envelope   envelope;
    private final int        flags;
    private final int        byteSize;
}
