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

package com.rgi.geopackage.features.geometry.m;

import com.rgi.geopackage.features.ByteOutputStream;
import com.rgi.geopackage.features.geometry.xy.Envelope;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public class WkbGeometryMTest
{
    /**
     * Test hasZ()
     */
    @Test
    public void testHasZ()
    {
        assertFalse("hasZ should have returned false",
                    new MyWkbGeometryM().hasZ());
    }

    /**
     * Test hasM()
     */
    @Test
    public void testHasM()
    {
        assertTrue("hasZ should have returned true",
                   new MyWkbGeometryM().hasM());
    }

    private static class MyWkbGeometryM extends WkbGeometryM
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
        public boolean isEmpty()
        {
            return false;
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

        @Override
        public EnvelopeM createEnvelopeM()
        {
            return null;
        }
    }
}
