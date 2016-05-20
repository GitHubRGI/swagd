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

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.Contents;
import com.rgi.geopackage.features.geometry.xy.Envelope;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * @author Luke Lambert
 */
@SuppressWarnings("JavaDoc")
public class GeometryTest
{
    /**
     * Tests that an empty geometry returns Contents.Empty
     */
    @Test
    public void getContents()
    {
        final Geometry geometry = new MyGeometry();

        assertEquals("Empty geometry must report Contents.Empty for getContents",
                     Contents.Empty,
                     geometry.getContents());
    }

    /**
     * Test readWellKnownBinaryHeader() when the {@link ByteBuffer} contains an unexpected type code
     */
    @Test(expected = IllegalArgumentException.class)
    public void readEwellKnownBinaryHeaderWithUnexpectedTypeCode()
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(5);

        byteBuffer.put((byte)0);  // Use big endian
        byteBuffer.putInt(1);     // Geometry type 1

        byteBuffer.position(0);

        MyGeometry.readHeader(byteBuffer, 2L);
    }

    private static class MyGeometry extends Geometry
    {
        @Override
        public boolean equals(final Object obj)
        {
            return false;
        }

        @Override
        public int hashCode()
        {
            return 0;
        }

        @Override
        public long getTypeCode()
        {
            return 0;
        }

        @Override
        public String getGeometryTypeName()
        {
            return null;
        }

        @Override
        public boolean hasZ()
        {
            return false;
        }

        @Override
        public boolean hasM()
        {
            return false;
        }

        @Override
        public boolean isEmpty()
        {
            return true;
        }

        @Override
        public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
        {

        }

        @Override
        public Envelope createEnvelope()
        {
            return null;
        }

        public static void readHeader(final ByteBuffer byteBuffer, final long typeCode)
        {
            readWellKnownBinaryHeader(byteBuffer, typeCode);
        }
    }
}
