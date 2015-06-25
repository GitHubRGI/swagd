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

public class GeoPackageGeometryBinaryHeader
{
    public GeoPackageGeometryBinaryHeader(final byte                           version,
                                          final GeoPackageGeometryBinaryType   binaryType,
                                          final boolean                        isEmpty,
                                          final GeoPackageGeometryBinaryEndian endianness,
                                          final int                            spatialReferenceSystemIdentifier,
                                          final double[]                       envelope)
    {


        final int isEmptyMask = isEmpty ? (1 << 4) : 0;

        final int envelopeContentsMask = (byte)0b00001110 & (envelope.length << 1);

        this.version  = version;
        this.flags    = 0 | binaryType.getBitMask() | isEmptyMask | envelopeContentsMask | endianness.getBitMask();
        this.srsId    = spatialReferenceSystemIdentifier;
        this.envelope = envelope;
    }

    public final static byte[] magic = {'G', 'P'};

    private final byte     version; // This is an *unsigned* value, regardless of Java's interpretation
    private final int      flags;
    private final int      srsId;
    private final double[] envelope;


}
