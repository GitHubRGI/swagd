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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

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
     * Constructor
     *
     * @param bytes
     *             Bytes that should comprise the GeoPackage Binary Header
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
        this.contents = (bytes[3] & Contents.Empty.getBitMask()) > 0 ? Contents.Empty : Contents.NotEmpty;  // TODO add a 'from bitmask' method in Contents?

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

    /**
     * Constructor
     *
     * @param version
     *             8-bit unsigned integer, 0 = version 1
     * @param binaryType
     *             Standard or Extended
     * @param contents
     *             Whether or not the geometry is "empty"
     * @param byteOrder
     *             Order of the header's bytes
     * @param spatialReferenceSystemIdentifier
     *             Spatial reference system identifier for the geometry. Should
     *             match the identifier in the contents table.
     * @param envelopeContentsIndicator
     *             Indicator of the envelope's contents (empty, not empty, and
     *             dimensionality)
     * @param envelope
     *             Geometry envelope. Size depends on the envelope contents
     *             indicator. Elements are listed min, max, and xyzm order.
     */
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
        this.contents                         = contents;
        this.byteOrder                        = byteOrder;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
        this.envelopeContentsIndicator        = envelopeContentsIndicator;
        this.envelope                         = envelope.clone();

        @SuppressWarnings("NumericCastThatLosesPrecision")
        final int envelopeContentsMask = (byte)(this.envelopeContentsIndicator.getCode() << 1);

        //noinspection NumericCastThatLosesPrecision
        this.flags = (byte)(this.binaryType.getBitMask() |
                            this.contents.getBitMask()   |
                            envelopeContentsMask         |
                            (byteOrder.equals(ByteOrder.BIG_ENDIAN) ? 0 : 1));

        this.byteSize = 2 +  // 2 bytes for the 'magic' header
                        1 +  // 1 byte for version
                        1 +  // 1 byte for flags
                        4 +  // 4 bytes (int32) for the srs id
                        (8 * this.envelopeContentsIndicator.getArraySize());   // 8 bytes per double, array size number of doubles
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(this == obj)
        {
            return true;
        }

        if(obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        final BinaryHeader other = (BinaryHeader)obj;

        return this.version                          == other.version                          &&
               this.spatialReferenceSystemIdentifier == other.spatialReferenceSystemIdentifier &&
               this.flags                            == other.flags                            &&
               this.byteSize                         == other.byteSize                         &&
               this.binaryType                       == other.binaryType                       &&
               this.contents                         == other.contents                         &&
               this.byteOrder                   .equals(other.byteOrder)                       &&
               this.envelopeContentsIndicator        == other.envelopeContentsIndicator        &&
               Arrays.equals(this.getEnvelope(), other.getEnvelope());

    }

    @Override
    public int hashCode()
    {
        int result = (int) this.version;
        result = 31 * result + this.binaryType.hashCode();
        result = 31 * result + this.contents.hashCode();
        result = 31 * result + this.byteOrder.hashCode();
        result = 31 * result + this.spatialReferenceSystemIdentifier;
        result = 31 * result + this.envelopeContentsIndicator.hashCode();
        result = 31 * result + Arrays.hashCode(this.getEnvelope());
        result = 31 * result + (int)this.flags;
        result = 31 * result + this.byteSize;
        return result;
    }

    public byte getVersion()
    {
        return this.version;
    }

    public BinaryType getBinaryType()
    {
        return this.binaryType;
    }

    public Contents getContents()
    {
        return this.contents;
    }

    public ByteOrder getByteOrder()
    {
        return this.byteOrder;
    }

    public int getSpatialReferenceSystemIdentifier()
    {
        return this.spatialReferenceSystemIdentifier;
    }

    public EnvelopeContentsIndicator getEnvelopeContentsIndicator()
    {
        return this.envelopeContentsIndicator;
    }

    public double[] getEnvelope()
    {
        return this.envelope.clone();
    }

    public byte getFlags()
    {
        return this.flags;
    }

    public int getByteSize()
    {
        return this.byteSize;
    }

    /**
     * Writes the binary header to the supplied byte buffer
     *
     * @param byteOutputStream
     *             Destination for the bytes of the header
     */
    public void writeBytes(final ByteOutputStream byteOutputStream)
    {
        if(byteOutputStream == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null or read only");
        }

        // http://www.geopackage.org/spec/#gpb_spec

        byteOutputStream.setByteOrder(this.byteOrder);

        byteOutputStream.write(BinaryHeader.magic);
        byteOutputStream.write(this.version);
        byteOutputStream.write(this.flags);
        byteOutputStream.write(this.spatialReferenceSystemIdentifier);

        for(final double envelopeBound : this.envelope)
        {
            byteOutputStream.write(envelopeBound);
        }
    }

    /**
     * Constructs a header from the supplied arguments, and writes it to a
     * byte buffer. Currently there's no way to skip writing the envelope.
     *
     * @param byteOutputStream
     *             Destination for the bytes of the header
     * @param geometry
     *             Used to determine the envelope, contents (empty or not), and
     *             the geometry's binary type
     * @param spatialReferenceSystemIdentifier
     *             Spatial reference system identifier for the geometry
     */
    public static void writeBytes(final ByteOutputStream byteOutputStream,
                                  final Geometry         geometry,
                                  final int              spatialReferenceSystemIdentifier)
    {
        if(geometry == null)
        {
            throw new IllegalArgumentException("Geometry may not be null");
        }

        final Envelope envelope = geometry.createEnvelope();

        new BinaryHeader(defaultVersion,
                         BinaryType.fromGeometryTypeName(geometry.getGeometryTypeName()),
                         geometry.getContents(),
                         defaultByteOrder,
                         spatialReferenceSystemIdentifier,
                         envelope.getContentsIndicator(),
                         envelope.toArray()).writeBytes(byteOutputStream);

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
    private final Contents                  contents;
    private final ByteOrder                 byteOrder;  // NOTE: this applies *only* to the bytes of the GeoPackage binary header. The byte order of the well known binary that follows the header is specified by the first byte of its contents: 0 - big endian, 1 - little endian
    private final int                       spatialReferenceSystemIdentifier;
    private final EnvelopeContentsIndicator envelopeContentsIndicator;
    private final double[]                  envelope;
    private final byte                      flags;
    private final int                       byteSize;
}
