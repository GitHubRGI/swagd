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

package com.rgi.geopackage.features.geometry.z;

import com.rgi.geopackage.features.ByteOutputStream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Luke Lambert
 */
public class LinearRingZ
{
    public LinearRingZ(final CoordinateZ... coordinates)
    {
        this(Arrays.asList(coordinates));
    }

    public LinearRingZ(final Collection<CoordinateZ> coordinates)
    {
        if(coordinates == null)
        {
            throw new IllegalArgumentException("Coordinate collection may not be null");
        }

        if(coordinates.stream().anyMatch(Objects::isNull))
        {
            throw new IllegalArgumentException("Linear string may not contain null coordinates");
        }

        this.coordinates = new ArrayList<>(coordinates);
    }

    public List<CoordinateZ> getCoordinates()
    {
        return Collections.unmodifiableList(this.coordinates);
    }

    public boolean isEmpty()
    {
        return this.coordinates.isEmpty();
    }

    public EnvelopeZ createEnvelope()
    {
        return this.coordinates.isEmpty() ? EnvelopeZ.Empty
                                          : this.coordinates
                                                .stream()
                                                .map(CoordinateZ::createEnvelope)
                                                .reduce(EnvelopeZ::combine)
                                                .get();
    }

    /**
     * Assumes the ByteOutputStream's byte order has been properly set
     *
     * @param byteOutputStream
     */
    public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
    {
        if(byteOutputStream == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        byteOutputStream.write(this.coordinates.size());

        this.coordinates.forEach(coordinate -> coordinate.writeWellKnownBinary(byteOutputStream));
    }

    /**
     * Assumes the bytebuffer's byte order has been properly set
     *
     * @param byteBuffer
     * @return
     */
    public static LinearRingZ readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        if(byteBuffer == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        final long pointCount = Integer.toUnsignedLong(byteBuffer.getInt());

        final Collection<CoordinateZ> coordinates = new LinkedList<>();

        for(long pointIndex = 0; pointIndex < pointCount; ++pointIndex)
        {
            coordinates.add(new CoordinateZ(byteBuffer.getDouble(),
                                            byteBuffer.getDouble(),
                                            byteBuffer.getDouble()));
        }

        return new LinearRingZ(coordinates);
    }

    private final List<CoordinateZ> coordinates;
}
