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
import com.rgi.geopackage.features.envelope.Envelope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    public abstract int     getTypeCode();
    public abstract String  getGeometryTypeName();
    public abstract boolean hasZ();
    public abstract boolean hasM();
    public abstract boolean isEmpty();

    public abstract void writeWellKnownBinary(final ByteArrayOutputStream byteArrayOutputStream) throws IOException;

    public abstract Envelope createEnvelope();

    public Contents getContents()
    {
        return this.isEmpty() ? Contents.Empty
                              : Contents.NotEmpty;
    }

//    public byte[] getStandardBinary() throws IOException
//    {
//        try(final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
//        {
//            this.header.writeBytes(byteArrayOutputStream);
//            this.writeWellKnownBinary(byteArrayOutputStream);
//
//            return byteArrayOutputStream.toByteArray();
//        }
//    }

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
//            this.writeWellKnownBinary(byteArrayOutputStream);
//
//            return byteArrayOutputStream.toByteArray();
//        }
//    }



//    @SuppressWarnings("FinalMethod")
//    public final boolean isStandard()
//    {
//        return standardGeometryTypes.contains(this.getGeometryTypeName().toUpperCase());
//    }
//
//    // Standard geometry types
//    // http://www.geopackage.org/spec/#geometry_types
//    private static final List<String> standardGeometryTypes = Arrays.asList("GEOMETRY",
//                                                                            "POINT",
//                                                                            "LINESTRING",
//                                                                            "POLYGON",
//                                                                            "MULTIPOINT",
//                                                                            "MULTILINESTRING",
//                                                                            "MULTIPOLYGON",
//                                                                            "GEOMETRYCOLLECTION");
}
