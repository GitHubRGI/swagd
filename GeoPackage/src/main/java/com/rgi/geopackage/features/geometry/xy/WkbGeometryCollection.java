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

package com.rgi.geopackage.features.geometry.xy;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.GeometryType;
import com.rgi.geopackage.features.WellKnownBinaryFormatException;
import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.features.geometry.GeometryFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of zero or more Geometry instances.
 * <br>
 * <br>
 * GeometryCollection is a generic term for the ST_GeomCollection type defined
 * in <a href="http://www.geopackage.org/spec/#12">ISO/IEC 13249-3:2011</a>,
 * which uses it for the definition of Well Known Text (WKT) and Well Known
 * Binary (WKB) encodings. The SQL type name GEOMCOLLECTION defined in <a
 * href="http://www.geopackage.org/spec/#10">OGC 06-104r4</a> and used in
 * <a href="spatial_ref_sys_data_table_definition">GeoPackage Specification
 * Clause 1.1.2.1.1</a> and <a href=
 * "http://www.geopackage.org/spec/#geometry_types">Annex E of the GeoPackage
 * Specification</a> refers to the SQL BLOB encoding of a GeometryCollection.
 *
 * @see "http://www.geopackage.org/spec/#_footnote_7"
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class WkbGeometryCollection<T extends WkbGeometry> extends WkbGeometry
{
    @SafeVarargs
    public WkbGeometryCollection(final T... geometries)
    {
        this(Arrays.asList(geometries));
    }

    public WkbGeometryCollection(final Collection<T> geometries)
    {
        if(geometries == null)
        {
            throw new IllegalArgumentException("Geometry collection may not be null");
        }

        if(geometries.stream().anyMatch(Objects::isNull))
        {
            throw new IllegalArgumentException("Geometry collection may not contain null geometries");
        }

        this.geometries = new ArrayList<>(geometries);
    }

    @Override
    public boolean equals(final Object o)
    {
        if(this == o)
        {
            return true;
        }

        if(o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        return this.geometries.equals((WkbGeometryCollection<?>)o);
    }

    @Override
    public int hashCode()
    {
        return this.geometries.hashCode();
    }

    @Override
    public long getTypeCode()
    {
        return GeometryType.GeometryCollection.getCode();
    }

    @Override
    public String getGeometryTypeName()
    {
        return GeometryType.GeometryCollection.toString();
    }

    @Override
    public boolean isEmpty()
    {
        return this.geometries.isEmpty();
    }

    @Override
    public Envelope createEnvelope()
    {
        return this.geometries.isEmpty() ? Envelope.Empty
                                         : this.geometries
                                               .stream()
                                               .map(WkbGeometry::createEnvelope)
                                               .reduce(Envelope::combine)
                                               .get();

    }

    public List<T> getGeometries()
    {
        return Collections.unmodifiableList(this.geometries);
    }

    public static WkbGeometryCollection<WkbGeometry> readWellKnownBinary(final GeometryFactory geometryFactory,
                                                                         final ByteBuffer      byteBuffer) throws WellKnownBinaryFormatException
    {
        readWellKnownBinaryHeader(byteBuffer, GeometryType.GeometryCollection.getCode());

        final long geometryCount = Integer.toUnsignedLong(byteBuffer.getInt());

        final Collection<WkbGeometry> geometries = new LinkedList<>();

        for(long geometryIndex = 0; geometryIndex < geometryCount; ++geometryIndex)
        {
            final Geometry geometry = geometryFactory.create(byteBuffer);

            if(geometry instanceof WkbGeometry)
            {
                //noinspection CastToConcreteClass
                geometries.add((WkbGeometry)geometry);
            }
            else
            {
                throw new WellKnownBinaryFormatException(String.format("Geometry at index %d is not in dimensionality agreement with its parent geometry collection",
                                                                       geometryIndex));
            }
        }

        return new WkbGeometryCollection<>(geometries);
    }

    @Override
    public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
    {
        this.writeWellKnownBinaryHeader(byteOutputStream); // Checks byteOutputStream for null

        final List<T> geometries = this.getGeometries();

        byteOutputStream.write(geometries.size());

        geometries.forEach(wkbGeometry -> wkbGeometry.writeWellKnownBinary(byteOutputStream));
    }

    private final List<T> geometries;
}
