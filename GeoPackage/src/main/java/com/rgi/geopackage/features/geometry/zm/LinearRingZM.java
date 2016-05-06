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

package com.rgi.geopackage.features.geometry.zm;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.geometry.m.CoordinateM;

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
public class LinearRingZM
{
    public LinearRingZM(final CoordinateZM... coordinates)
    {
        this(Arrays.asList(coordinates));
    }

    public LinearRingZM(final Collection<CoordinateZM> coordinates)
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

        return this.coordinates.equals((LinearRingZM)o);
    }

    @Override
    public int hashCode()
    {
        return this.coordinates.hashCode();
    }

    public List<CoordinateZM> getCoordinates()
    {
        return Collections.unmodifiableList(this.coordinates);
    }

    public boolean isEmpty()
    {
        return this.coordinates.isEmpty();
    }

    public EnvelopeZM createEnvelope()
    {
        return this.coordinates.isEmpty() ? EnvelopeZM.Empty
                                          : this.coordinates
                                                .stream()
                                                .map(CoordinateZM::createEnvelope)
                                                .reduce(EnvelopeZM::combine)
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
    public static LinearRingZM readWellKnownBinary(final ByteBuffer byteBuffer)
    {
        if(byteBuffer == null)
        {
            throw new IllegalArgumentException("Byte buffer may not be null");
        }

        final long pointCount = Integer.toUnsignedLong(byteBuffer.getInt());

        final Collection<CoordinateZM> coordinates = new LinkedList<>();

        for(long pointIndex = 0; pointIndex < pointCount; ++pointIndex)
        {
            coordinates.add(new CoordinateZM(byteBuffer.getDouble(),
                                             byteBuffer.getDouble(),
                                             byteBuffer.getDouble(),
                                             byteBuffer.getDouble()));
        }

        return new LinearRingZM(coordinates);
    }

    private final List<CoordinateZM> coordinates;
}
