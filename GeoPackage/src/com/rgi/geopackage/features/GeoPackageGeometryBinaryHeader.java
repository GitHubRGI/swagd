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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GeoPackageGeometryBinaryHeader
{
    public GeoPackageGeometryBinaryHeader(final byte                         version,
                                          final GeoPackageGeometryBinaryType binaryType,
                                          final boolean                      empty,
                                          final ByteOrder                    byteOrder,
                                          final int                          spatialReferenceSystemIdentifier,
                                          final double[]                     envelope)
    {
        if(binaryType == null)
        {
            throw new IllegalArgumentException("Binary type may not be null");
        }

        if(byteOrder == null)
        {
            throw new IllegalArgumentException("Byte order may not be null");
        }

        this.version                          = version;
        this.binaryType                       = binaryType;
        this.empty                            = empty;
        this.byteOrder                        = byteOrder;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
        this.envelope                         = envelope;

        final int isEmptyMask          = empty ? (1 << 4) : 0;
        final int envelopeContentsMask = (byte)0b00001110 & (envelope.length << 1);

        this.flags = 0                            |
                     this.binaryType.getBitMask() |
                     isEmptyMask                  |
                     envelopeContentsMask         |
                     (byteOrder == ByteOrder.BIG_ENDIAN ? 0 : 1);
    }

    public void writeBytes(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        // http://www.geopackage.org/spec/#gpb_spec

        // "magic"
        byteArrayOutputStream.write(ByteBuffer.wrap(this.magic)
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

        // envelope
        final int envelopeLength = this.envelope.length;

        for(int envelopeIndex = 0; envelopeIndex < envelopeLength; ++envelopeIndex)
        {
            byteArrayOutputStream.write(ByteBuffer.allocate(8)
                                                  .putDouble(this.envelope[envelopeIndex])
                                                  .order(this.byteOrder)
                                                  .array());
        }
    }

    private final static byte[] magic = {'G', 'P'};

    private final byte                         version; // This is an *unsigned* value, regardless of Java's interpretation
    private final GeoPackageGeometryBinaryType binaryType;
    private final boolean                      empty;
    private final ByteOrder                    byteOrder;
    private final int                          spatialReferenceSystemIdentifier;
    private final double[]                     envelope;
    private final int                          flags;
}
