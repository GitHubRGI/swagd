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

import com.rgi.geopackage.features.Coordinate;
import com.rgi.geopackage.features.Envelope;
import com.rgi.geopackage.features.EnvelopeContentsIndicator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Luke Lambert
 */
public class LinearString
{
    public LinearString(final Coordinate... coordinates)
    {
        this(Arrays.asList(coordinates));
    }

    public LinearString(final Collection<Coordinate> coordinates)
    {
        if(coordinates == null)
        {
            throw new IllegalArgumentException("Coordinate collection may not be null");
        }

        if(coordinates.isEmpty())
        {
            this.hasZ = false;
            this.hasM = false;
        }
        else
        {
            for(final Coordinate coordinate : coordinates)
            {
                if(coordinate == null)
                {
                    throw new IllegalArgumentException("Linear string may not contain null coordinates");
                }
            }

            if(coordinates.stream()
                          .anyMatch(Objects::isNull))
            {
                throw new IllegalArgumentException("Linear string may not contain null coordinates");
            }

            final Coordinate firstCoordinate = coordinates.iterator().next();

            this.hasZ = firstCoordinate.hasZ();
            this.hasM = firstCoordinate.hasM();

            if(coordinates.stream()
                          .anyMatch(coordinate -> coordinate.hasZ() != this.hasZ ||
                                                  coordinate.hasM() != this.hasM))
            {
                throw new IllegalArgumentException("Each coordinate of a linear string must agree on the presence of Z and M");
            }
        }

        this.coordinates = new ArrayList<>(coordinates);
    }

    public List<Coordinate> getCoordinates()
    {
        return Collections.unmodifiableList(this.coordinates);
    }

    public boolean hasZ()
    {
        return this.hasZ;
    }

    public boolean hasM()
    {
        return this.hasM;
    }

    public boolean isEmpty()
    {
        return this.coordinates.isEmpty();
    }

    public Envelope createEnvelope()
    {
        if(this.isEmpty())
        {
            return Envelope.Empty;
        }

        final EnvelopeContentsIndicator envelopeContentsIndicator = this.getEnvelopeContentsIndicator();

        final double[] array = new double[envelopeContentsIndicator.getArraySize()];
        Arrays.fill(array, Double.NaN);

        this.coordinates.forEach(coordinate -> envelopeContentsIndicator.getComparer().accept(coordinate, array));

        return new Envelope(envelopeContentsIndicator, array);
    }

    private EnvelopeContentsIndicator getEnvelopeContentsIndicator()
    {
        if(this.hasZ() && this.hasM())
        {
            return EnvelopeContentsIndicator.Xyzm;
        }

        if(this.hasZ())
        {
            return EnvelopeContentsIndicator.Xyz;
        }

        if(this.hasM())
        {
            return EnvelopeContentsIndicator.Xym;
        }

        return EnvelopeContentsIndicator.Xy;
    }

    public static LinearString readWellKnownBinary(ByteBuffer byteBuffer)
    {

    }

    private final List<Coordinate> coordinates;
    private final boolean          hasZ;
    private final boolean          hasM;


}
