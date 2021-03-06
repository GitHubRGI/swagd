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

package com.rgi.geopackage.features.geometry.z;

import com.rgi.geopackage.features.EnvelopeContentsIndicator;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Luke Lambert
 */
public final class EnvelopeZTest
{
    /**
     * Test the constructor
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor()
    {
        new EnvelopeZ(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }


    /**
     * Test toArray()
     */
    @Test
    public void toArray()
    {
        //noinspection ZeroLengthArrayAllocation
        assertArrayEquals("toArray returned an incorrect value for an empty envelope",
                          new double[] {},
                          new EnvelopeZ(Double.NaN,
                                        Double.NaN,
                                        Double.NaN,
                                        Double.NaN,
                                        Double.NaN,
                                        Double.NaN).toArray(),
                          0.0);

        final double minX = 0.0;
        final double minY = 1.0;
        final double minZ = 2.0;
        final double maxX = 4.0;
        final double maxY = 5.0;
        final double maxZ = 6.0;

        final double[] array = { minX, maxX, minY, maxY, minZ, maxZ };

        assertArrayEquals("toArray returned an incorrect value for a non-empty envelope",
                          array,
                          new EnvelopeZ(minX,
                                         minY,
                                         minZ,
                                         maxX,
                                         maxY,
                                         maxZ).toArray(),
                          0.0);
    }

    /**
     * Test accessors
     */
    @Test
    public void accessors()
    {
        final double minX = 0.0;
        final double minY = 1.0;
        final double minZ = 2.0;
        final double maxX = 4.0;
        final double maxY = 5.0;
        final double maxZ = 6.0;

        final EnvelopeZ envelope = new EnvelopeZ(minX,
                                                 minY,
                                                 minZ,
                                                 maxX,
                                                 maxY,
                                                 maxZ);

        assertEquals("getMinimumZ returned the wrong value",
                     minZ,
                     envelope.getMinimumZ(),
                     0.0);

        assertEquals("getMaximumZ returned the wrong value",
                     maxZ,
                     envelope.getMaximumZ(),
                     0.0);
    }

    /**
     * Test dimensionality
     */
    @Test
    public void dimensionality()
    {
        assertTrue("EnvelopeZ should support Z values",
                   new EnvelopeZ(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).hasZ());

        assertFalse("EnvelopeZ should not support M values",
                    new EnvelopeZ(0.0, 0.0, 0.0, 0.0, 0.0, 0.0).hasM());
    }

    /**
     * Test getContentsIndicator()
     */
    @Test
    public void getContentsIndicator()
    {
        assertSame("getContentsIndicator returned the wrong value",
                   EnvelopeContentsIndicator.NoEnvelope,
                   new EnvelopeZ(Double.NaN,
                                 Double.NaN,
                                 Double.NaN,
                                 Double.NaN,
                                 Double.NaN,
                                 Double.NaN).getContentsIndicator());

        assertSame("getContentsIndicator returned the wrong value",
                   EnvelopeContentsIndicator.Xyz,
                   new EnvelopeZ(0.0,
                                 1.0,
                                 2.0,
                                 3.0,
                                 4.0,
                                 5.0).getContentsIndicator());
    }

    /**
     * Test combine()
     */
    @Test
    public void combine()
    {
        final EnvelopeZ envelope1 = new EnvelopeZ(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        final EnvelopeZ envelope2 = new EnvelopeZ(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);

        final EnvelopeZ combined = EnvelopeZ.combine(envelope1, envelope2);

        assertEquals("combine() picked the wrong minimum x value",
                     Math.min(envelope1.getMinimumX(),
                              envelope2.getMinimumX()),
                     combined.getMinimumX(),
                     0.0);

        assertEquals("combine() picked the wrong minimum y value",
                     Math.min(envelope1.getMinimumY(),
                              envelope2.getMinimumY()),
                     combined.getMinimumY(),
                     0.0);

        assertEquals("combine() picked the wrong minimum z value",
                     Math.min(envelope1.getMinimumZ(),
                              envelope2.getMinimumZ()),
                     combined.getMinimumZ(),
                     0.0);

        assertEquals("combine() picked the wrong maximum x value",
                     Math.max(envelope1.getMaximumX(),
                              envelope2.getMaximumX()),
                     combined.getMaximumX(),
                     0.0);

        assertEquals("combine() picked the wrong maximum y value",
                     Math.max(envelope1.getMaximumY(),
                              envelope2.getMaximumY()),
                     combined.getMaximumY(),
                     0.0);

        assertEquals("combine() picked the wrong maximum z value",
                     Math.max(envelope1.getMaximumZ(),
                              envelope2.getMaximumZ()),
                     combined.getMaximumZ(),
                     0.0);
    }
}
