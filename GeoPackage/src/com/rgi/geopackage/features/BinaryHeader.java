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
import com.rgi.geopackage.features.geometry.xy.Envelope;

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
        this.emptyGeometry = ((bytes[3] << 4) & 1) == 1;

        this.byteOrder  = ((this.flags & 1) == 0) ? ByteOrder.BIG_ENDIAN
                                                  : ByteOrder.LITTLE_ENDIAN;

        final ByteBuffer srsIdByteBuffer = ByteBuffer.wrap(bytes, 4, 4); // Bytes 5->9 are int32 srs_id
        srsIdByteBuffer.order(this.byteOrder);
        this.spatialReferenceSystemIdentifier = srsIdByteBuffer.getInt();

        this.envelopeContentsIndicator = EnvelopeContentsIndicator.fromCode((this.flags & 0b00001110) >> 1);

        this.byteSize = 2 +  // 2 bytes for the 'magic' header
                        1 +  // 1 byte for version
                        1 +  // 1 byte for flags
                        4 +  // 4 bytes (int32) for the srs id
                        (8 * this.envelopeContentsIndicator.getArraySize());   // 8 bytes per double, array size number of doubles

        if(bytes.length < this.byteSize)
        {
            throw new IllegalArgumentException("Byte array length is shorter than the envelope size would indicate");
        }

        this.envelope = getHeaderEnvelopeDoubles(bytes,
                                                 this.byteOrder,
                                                 this.envelopeContentsIndicator.getArraySize());

    }

    protected BinaryHeader(final byte                      version,
                           final BinaryType                binaryType,
                           final Contents                  contents,
                           final ByteOrder                 byteOrder,
                           final int                       spatialReferenceSystemIdentifier,
                           final EnvelopeContentsIndicator envelopeContentsIndicator,
                           final double[]                  envelope)
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

        if(envelopeContentsIndicator == null)
        {
            throw new IllegalArgumentException("Envelope contents indicator may not be null");
        }

        if(envelope == null)
        {
            throw new IllegalArgumentException("Envelope may not be null. Use Envelope.Empty to represent an empty envelope.");
        }

        if(envelope.length != envelopeContentsIndicator.getArraySize())
        {
            throw new IllegalArgumentException("Envelope content indicator does not agree with the length of the envelope array");
        }

        this.version                          = version;
        this.binaryType                       = binaryType;
        this.emptyGeometry                    = contents == Contents.Empty;
        this.byteOrder                        = byteOrder;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
        this.envelopeContentsIndicator        = envelopeContentsIndicator;
        this.envelope                         = envelope;

        final int isEmptyMask          = this.emptyGeometry ? (1 << 4) : 0; // TODO make this part of the Contents enum like BinaryType?
        final int envelopeContentsMask = (byte)(this.envelopeContentsIndicator.getCode() << 1);

        this.flags = // 0                         |
                     this.binaryType.getBitMask() |
                     isEmptyMask                  |
                     envelopeContentsMask         |
                     (byteOrder.equals(ByteOrder.BIG_ENDIAN) ? 0 : 1);

        this.byteSize = 2 +  // 2 bytes for the 'magic' header
                        1 +  // 1 byte for version
                        1 +  // 1 byte for flags
                        4 +  // 4 bytes (int32) for the srs id
                        (8 * this.envelopeContentsIndicator.getArraySize());   // 8 bytes per double, array size number of doubles
    }

    public int getSpatialReferenceSystemIdentifier()
    {
        return this.spatialReferenceSystemIdentifier;
    }

    public boolean isEmptyGeometry()
    {
        return this.emptyGeometry;
    }

    public int getByteSize()
    {
        return this.byteSize;
    }

    public BinaryType getBinaryType()
    {
        return this.binaryType;
    }

    public void writeBytes(final ByteBuffer byteBuffer) throws IOException
    {
        // http://www.geopackage.org/spec/#gpb_spec

        byteBuffer.order(this.byteOrder);

        byteBuffer.put   (BinaryHeader.magic);
        byteBuffer.put   (this.version);
        byteBuffer.put   ((byte)this.flags);
        byteBuffer.putInt(this.spatialReferenceSystemIdentifier);

        for(final double envelopeBound : this.envelope)
        {
            byteBuffer.putDouble(envelopeBound);
        }
    }

    public static void writeBytes(final ByteBuffer byteBuffer,
                                  final Geometry   geometry,
                                  final int        spatialReferenceSystemIdentifier) throws IOException
    {
        final Envelope envelope = geometry.createEnvelope();

        new BinaryHeader(defaultVersion,
                         BinaryType.fromGeometryTypeName(geometry.getGeometryTypeName()),
                         geometry.getContents(),
                         defaultByteOrder,
                         spatialReferenceSystemIdentifier,
                         envelope.getContentsIndicator(),
                         envelope.toArray()).writeBytes(byteBuffer);

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

    private static final byte[] magic = {(byte)71, // 'G'
                                         (byte)80  // 'P'
                                        };

    private final byte                      version; // This is an *unsigned* value, regardless of Java's interpretation
    private final BinaryType                binaryType;
    private final boolean                   emptyGeometry;
    private final ByteOrder                 byteOrder;  // NOTE: this applies *only* to the bytes of the GeoPackage binary header. The byte order of the well known binary that follows the header is specified by the first byte of its contents: 0 - big endian, 1 - little endian
    private final int                       spatialReferenceSystemIdentifier;
    private final EnvelopeContentsIndicator envelopeContentsIndicator;
    private final double[]                  envelope;
    private final int                       flags;
    private final int                       byteSize;
}
