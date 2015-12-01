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
import java.util.Arrays;
import java.util.List;

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
    protected Geometry(final BinaryHeader header)
    {
        if(header == null)
        {
            throw new IllegalArgumentException("Binary header may not be null");
        }

        this.header = header;
    }

    public abstract int     getTypeCode();
    public abstract String  getGeometryTypeName();
    public abstract boolean hasZ();
    public abstract boolean hasM();

    public int getSpatialReferenceIdentifier()
    {
        return this.header.getSpatialReferenceSystemIdentifier();
    }

    public boolean isEmpty()
    {
        return this.header.isEmpty();
    }

    public static Geometry fromBytes(final byte[] bytes) throws IOException
    {
        if(bytes == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
//
//        byteBuffer.

        return null;
    }

    public byte[] getStandardBinary() throws IOException
    {
        try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            this.header.writeBytes(byteArrayOutputStream);
            this.writeWkbGeometry(byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
    }

    // TODO
//    public byte[] getStandardBinary(OUTPUT FLAGS) throws IOException
//    {
//        try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
//        {
//            HEADER USES OPTIONS:
//            FORCE ENVELOPE
//            FORCE ENDIANNESS
//
//            this.header.writeBytes(byteArrayOutputStream);
//            this.writeWkbGeometry(byteArrayOutputStream);
//
//            return byteArrayOutputStream.toByteArray();
//        }
//    }

    public abstract void writeWkbGeometry(final ByteArrayOutputStream byteArrayOutputStream) throws IOException;

    @SuppressWarnings("FinalMethod")
    public final boolean isStandard()
    {
        return standardGeometryTypes.contains(this.getGeometryTypeName().toUpperCase());
    }

    protected final BinaryHeader header;

    // Standard geometry types
    // http://www.geopackage.org/spec/#geometry_types
    private static final List<String> standardGeometryTypes = Arrays.asList("GEOMETRY",
                                                                            "POINT",
                                                                            "LINESTRING",
                                                                            "POLYGON",
                                                                            "MULTIPOINT",
                                                                            "MULTILINESTRING",
                                                                            "MULTIPOLYGON",
                                                                            "GEOMETRYCOLLECTION");
}
