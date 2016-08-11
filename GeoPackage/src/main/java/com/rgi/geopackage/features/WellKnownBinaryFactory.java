/*
 * The MIT License (MIT)
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
import com.rgi.geopackage.features.geometry.GeometryFactory;
import com.rgi.geopackage.features.geometry.m.WkbGeometryCollectionM;
import com.rgi.geopackage.features.geometry.m.WkbGeometryM;
import com.rgi.geopackage.features.geometry.m.WkbLineStringM;
import com.rgi.geopackage.features.geometry.m.WkbMultiLineStringM;
import com.rgi.geopackage.features.geometry.m.WkbMultiPointM;
import com.rgi.geopackage.features.geometry.m.WkbMultiPolygonM;
import com.rgi.geopackage.features.geometry.m.WkbPointM;
import com.rgi.geopackage.features.geometry.m.WkbPolygonM;
import com.rgi.geopackage.features.geometry.xy.WkbGeometryCollection;
import com.rgi.geopackage.features.geometry.xy.WkbLineString;
import com.rgi.geopackage.features.geometry.xy.WkbMultiLineString;
import com.rgi.geopackage.features.geometry.xy.WkbMultiPoint;
import com.rgi.geopackage.features.geometry.xy.WkbMultiPolygon;
import com.rgi.geopackage.features.geometry.xy.WkbPoint;
import com.rgi.geopackage.features.geometry.xy.WkbPolygon;
import com.rgi.geopackage.features.geometry.z.WkbGeometryCollectionZ;
import com.rgi.geopackage.features.geometry.z.WkbGeometryZ;
import com.rgi.geopackage.features.geometry.z.WkbLineStringZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiLineStringZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiPointZ;
import com.rgi.geopackage.features.geometry.z.WkbMultiPolygonZ;
import com.rgi.geopackage.features.geometry.z.WkbPointZ;
import com.rgi.geopackage.features.geometry.z.WkbPolygonZ;
import com.rgi.geopackage.features.geometry.zm.WkbGeometryCollectionZM;
import com.rgi.geopackage.features.geometry.zm.WkbGeometryZM;
import com.rgi.geopackage.features.geometry.zm.WkbLineStringZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiLineStringZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiPointZM;
import com.rgi.geopackage.features.geometry.zm.WkbMultiPolygonZM;
import com.rgi.geopackage.features.geometry.zm.WkbPointZM;
import com.rgi.geopackage.features.geometry.zm.WkbPolygonZM;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public class WellKnownBinaryFactory
{
    public WellKnownBinaryFactory()
    {
        this.geometryFactories.put(GeometryType.Geometry          .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 0)"); } );      // type 0 XY
        this.geometryFactories.put(GeometryType.Point             .getCode(), WkbPoint          ::readWellKnownBinary); // type 1 XY
        this.geometryFactories.put(GeometryType.LineString        .getCode(), WkbLineString     ::readWellKnownBinary); // type 2 XY
        this.geometryFactories.put(GeometryType.Polygon           .getCode(), WkbPolygon        ::readWellKnownBinary); // type 3 XY
        this.geometryFactories.put(GeometryType.MultiPoint        .getCode(), WkbMultiPoint     ::readWellKnownBinary); // type 4 XY
        this.geometryFactories.put(GeometryType.MultiLineString   .getCode(), WkbMultiLineString::readWellKnownBinary); // type 5 XY
        this.geometryFactories.put(GeometryType.MultiPolygon      .getCode(), WkbMultiPolygon   ::readWellKnownBinary); // type 6 XY
        this.geometryFactories.put(GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollection.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XY

        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Geometry          .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 1000)"); } );   // type 0 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Point             .getCode(), WkbPointZ          ::readWellKnownBinary);  // type 1 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.LineString        .getCode(), WkbLineStringZ     ::readWellKnownBinary);  // type 2 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.Polygon           .getCode(), WkbPolygonZ        ::readWellKnownBinary);  // type 3 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiPoint        .getCode(), WkbMultiPointZ     ::readWellKnownBinary);  // type 4 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiLineString   .getCode(), WkbMultiLineStringZ::readWellKnownBinary);  // type 5 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon      .getCode(), WkbMultiPolygonZ   ::readWellKnownBinary);  // type 6 XYZ
        this.geometryFactories.put(WkbGeometryZ.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionZ.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYZ

        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Geometry          .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 2000)"); } );   // type 0 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Point             .getCode(), WkbPointM          ::readWellKnownBinary);  // type 1 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.LineString        .getCode(), WkbLineStringM     ::readWellKnownBinary);  // type 2 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.Polygon           .getCode(), WkbPolygonM        ::readWellKnownBinary);  // type 3 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiPoint        .getCode(), WkbMultiPointM     ::readWellKnownBinary);  // type 4 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiLineString   .getCode(), WkbMultiLineStringM::readWellKnownBinary);  // type 5 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon      .getCode(), WkbMultiPolygonM   ::readWellKnownBinary);  // type 6 XYM
        this.geometryFactories.put(WkbGeometryM.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionM.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYM

        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Geometry          .getCode(), bytes -> { throw new WellKnownBinaryFormatException("Cannot instantiate abstract 'Geometry' type (geometry type code 3000)"); } );   // type 0 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Point             .getCode(), WkbPointZM          ::readWellKnownBinary);  // type 1 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.LineString        .getCode(), WkbLineStringZM     ::readWellKnownBinary);  // type 2 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.Polygon           .getCode(), WkbPolygonZM        ::readWellKnownBinary);  // type 3 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiPoint        .getCode(), WkbMultiPointZM     ::readWellKnownBinary);  // type 4 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiLineString   .getCode(), WkbMultiLineStringZM::readWellKnownBinary);  // type 5 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.MultiPolygon      .getCode(), WkbMultiPolygonZM   ::readWellKnownBinary);  // type 6 XYZM
        this.geometryFactories.put(WkbGeometryZM.GeometryTypeDimensionalityBase + GeometryType.GeometryCollection.getCode(), (byteBuffer) -> WkbGeometryCollectionZM.readWellKnownBinary(this::createGeometry, byteBuffer));  // type 7 XYZM
    }

    /**
     * Associate a geometry factory with a specific geometry type code.
     *
     * @param geometryTypeCode
     *             Code representation of the geometry type. Must be in the
     *             range 0 and 2^32 - 1 (range of a 32 bit unsigned integer)
     *             inclusive
     * @param geometryFactory
     *             Callback that creates a geometry that corresponds to the
     *             geometry type code
     */
    public void registerGeometryFactory(final long            geometryTypeCode,
                                        final GeometryFactory geometryFactory)
    {
        if(geometryTypeCode < 0 || geometryTypeCode > maxUnsignedIntValue)
        {
            throw new IllegalArgumentException("Type code must be between 0 and 2^32 - 1 (range of a 32 bit unsigned integer)");
        }

        if(geometryFactory == null)
        {
            throw new IllegalArgumentException("Geometry factory may not be null");
        }

//        if(this.geometryFactories.containsKey(geometryTypeCode))    // TODO do we really want to prohibit this?
//        {
//            throw new IllegalArgumentException("A geometry factory already exists for this geometry type code");
//        }

        this.geometryFactories.put(geometryTypeCode, geometryFactory);
    }

    public Geometry createGeometry(final ByteBuffer wkbByteBuffer) throws WellKnownBinaryFormatException
    {
        try
        {
            if(wkbByteBuffer.limit()-wkbByteBuffer.position() < 5)
            {
                throw new WellKnownBinaryFormatException("Well known binary buffer must contain at least 5 bytes - the first being the byte order indicator, followed by a 4 byte unsigned integer describing the geometry type.");
            }

            // Save the buffer position (.mark()) before we read the well known
            // binary header (this is *not* the GeoPackage binary header). The
            // well known binary header will be re-read by the parsers stored
            // in the geometry factory. This is so the parsers can stand alone,
            // not relying the ByteBuffer to be positioned after the well known
            // binary header.
            wkbByteBuffer.mark();

            final ByteOrder byteOrder = wkbByteBuffer.get() == 0 ? ByteOrder.BIG_ENDIAN
                                                                 : ByteOrder.LITTLE_ENDIAN;

            wkbByteBuffer.order(byteOrder);

            // Read 4 bytes as an /unsigned/ int
            final long geometryType = Integer.toUnsignedLong(wkbByteBuffer.getInt());

            if(!this.geometryFactories.containsKey(geometryType))
            {
                throw new RuntimeException(String.format("Unrecognized geometry type code %d. Recognized geometry types are: %s. Additional types will require a GeoPackage extention to interact with.",
                                                         geometryType,
                                                         this.geometryFactories
                                                             .keySet()
                                                             .stream()
                                                             .map(Object::toString)
                                                             .collect(Collectors.joining(", "))));
            }

            wkbByteBuffer.reset(); // This will reset the position to before the well known binary header.

            return this.geometryFactories
                       .get(geometryType)
                       .create(wkbByteBuffer);
        }
        catch(final BufferUnderflowException bufferUnderflowException)
        {
            throw new WellKnownBinaryFormatException(bufferUnderflowException);
        }
    }

    private final Map<Long, GeometryFactory> geometryFactories = new HashMap<>();

    private static final long maxUnsignedIntValue = 4294967295L; // 2^31 - 1
}
