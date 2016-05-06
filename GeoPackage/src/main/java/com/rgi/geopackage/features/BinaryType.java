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

import java.util.Arrays;
import java.util.List;

/**
 * @see "http://www.geopackage.org/spec/#gpb_format"
 *
 * Binary type, the fifth bit of the GeoPackageBinary flags byte.
 *
 * 0 - Standard geometry type
 * 1 - extended geometry type
 *
 * @author LukeLambert
 *
 */
public enum BinaryType
{
    Standard((byte)0b00000000),
    Extended((byte)0b00100000);

    /**
     * @return the bitMask
     */
    public byte getBitMask()
    {
        return this.bitMask;
    }

    public static BinaryType type(final byte flags)
    {
        return ((flags >> 5) & 1) == 0 ? Standard : Extended;   // Check to see if the 5th bit is unset. unset -> Standard, set -> Extended
    }

    public static BinaryType fromGeometryTypeName(final String geometryTypeName)
    {
        return standardGeometryTypes.contains(geometryTypeName.toUpperCase()) ? Standard
                                                                              : Extended;
    }

    BinaryType(final byte bitMask)
    {
        this.bitMask = bitMask;
    }

    private final byte bitMask;

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
