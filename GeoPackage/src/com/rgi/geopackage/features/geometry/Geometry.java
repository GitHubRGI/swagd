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

package com.rgi.geopackage.features.geometry;

import com.rgi.geopackage.features.Contents;
import com.rgi.geopackage.features.WellKnownBinaryFormatException;
import com.rgi.geopackage.features.geometry.xy.Envelope;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The root of the geometry type hierarchy.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public abstract class Geometry
{
    /**
     * This represents an *unsigned* 32bit value
     *
     * @return
     */
    public abstract long    getTypeCode();
    public abstract String  getGeometryTypeName();
    public abstract boolean hasZ();
    public abstract boolean hasM();
    public abstract boolean isEmpty();

    public abstract void writeWellKnownBinary(final ByteBuffer byteBuffer);

    public abstract Envelope createEnvelope();

    public Contents getContents()
    {
        return this.isEmpty() ? Contents.Empty
                              : Contents.NotEmpty;
    }

    protected static void readWellKnownBinaryHeader(final ByteBuffer byteBuffer, final long typeCode)
    {
        if(byteBuffer == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        final ByteOrder byteOrder = byteBuffer.get() == 0 ? ByteOrder.BIG_ENDIAN
                                                          : ByteOrder.LITTLE_ENDIAN;

        byteBuffer.order(byteOrder);

        final long geometryType = readGeometryType(byteBuffer);

        if(geometryType != typeCode)
        {
            throw new IllegalArgumentException(String.format("Unexpected geometry type %d. Expected %d",
                                                             geometryType,
                                                             typeCode));
        }
    }

    protected void writeWellKnownBinaryHeader(final ByteBuffer byteBuffer)
    {
        if(byteBuffer == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        final byte byteOrder = (byte)(byteBuffer.order().equals(ByteOrder.BIG_ENDIAN) ? 0 : 1);

        byteBuffer.put(byteOrder);
        byteBuffer.putInt((int)this.getTypeCode()); // This long -> int cast should be safe. The long value is used to represent an unsigned value
    }

    protected static long readGeometryType(final ByteBuffer byteBuffer)
    {
        // Read 4 bytes as an /unsigned/ int
        return Integer.toUnsignedLong(byteBuffer.getInt());
    }
}
