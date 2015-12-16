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

import com.rgi.geopackage.features.Envelope;
import com.rgi.geopackage.features.GeometryType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A restricted form of CurvePolygon where each ring is defined as a simple,
 * closed LineString.
 *
 * @see "http://www.geopackage.org/spec/#sfsql_intro"
 *
 * @author Luke Lambert
 *
 */
public class Polygon extends CurvePolygon
{
    public Polygon(final LinearString    exteriorRing,
                   final LinearString... interiorRings)
    {
        this(exteriorRing,
             Arrays.asList(interiorRings));
    }

    public Polygon(final LinearString             exteriorRing,
                   final Collection<LinearString> interiorRings)
    {
        if(exteriorRing == null)
        {
            throw new IllegalArgumentException("The exterior ring may not be null");
        }

        this.interiorRings = interiorRings == null ? Collections.emptyList()
                                                   : new ArrayList<>(interiorRings);

        if(this.interiorRings
               .stream()
               .anyMatch(interiorRing -> interiorRing.hasZ() != exteriorRing.hasZ() ||
                                         interiorRing.hasM() != exteriorRing.hasM()))
        {
            throw new IllegalArgumentException("The dimensions (has z, has m) of each interior ring must agree with the polygon's exterior ring");
        }

        this.exteriorRing = exteriorRing;
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public long getTypeCode()
    {
        return GeometryType.Polygon.getCode();
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public String getGeometryTypeName()
    {
        return GeometryType.Polygon.toString();
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public boolean hasZ()
    {
        return this.exteriorRing.hasZ();
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public boolean hasM()
    {
        return this.exteriorRing.hasM();
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public boolean isEmpty()
    {
        return this.exteriorRing.isEmpty();
    }

    @Override
    @SuppressWarnings("RefusedBequest")
    public void writeWellKnownBinary(final ByteArrayOutputStream byteArrayOutputStream) throws IOException
    {
        throw new UnsupportedOperationException("pending implementaiton");
    }

    @Override
    public Envelope createEnvelope()
    {
        return this.exteriorRing.createEnvelope();
    }

    public LinearString getExteriorRing()
    {
        return this.exteriorRing;
    }

    public List<LinearString> getInteriorRings()
    {
        return Collections.unmodifiableList(this.interiorRings);
    }

    public static Polygon readWellKnownBinary(final ByteBuffer byteBuffer)
    {

    }

    private final LinearString       exteriorRing;
    private final List<LinearString> interiorRings;
}
