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

import com.rgi.geopackage.features.geometry.xy.Envelope;
import com.rgi.geopackage.features.EnvelopeContentsIndicator;

/**
 * @author Luke Lambert
 */
public final class EnvelopeZ extends Envelope
{
    /**
     * Constructor
     *
     * @param minimumX
     *             Minimum (inclusive) x value
     * @param minimumY
     *             Minimum (inclusive) y value
     * @param minimumZ
     *             Minimum (inclusive) z value
     * @param maximumX
     *             Maximum (inclusive) x value
     * @param maximumY
     *             Maximum (inclusive) y value
     * @param maximumZ
     *             Maximum (inclusive) z value
     */
    public EnvelopeZ(final double minimumX,
                     final double minimumY,
                     final double minimumZ,
                     final double maximumX,
                     final double maximumY,
                     final double maximumZ)
    {
        super(minimumX,
              minimumY,
              maximumX,
              maximumY);

        this.minimumZ = minimumZ;
        this.maximumZ = maximumZ;
    }

    /**
     * This is in the order:
     * minimum x, maximum x, minimum y, maximum y, minimum z, maximum z
     * @see <a href="http://www.geopackage.org/spec/#flags_layout">GeoPackage spec, Table 6. bit layout of GeoPackageBinary flags byte</a>
     *
     * @return An array of doubles in the order: minimum x, maximum x, minimum y, maximum y, minimum z, maximum z
     */
    @Override
    public double[] toArray()
    {
        return this.isEmpty() ? EMPTY_ARRAY
                              : new double[]{ this.getMinimumX(),
                                              this.getMaximumX(),
                                              this.getMinimumY(),
                                              this.getMaximumY(),
                                              this.minimumZ,
                                              this.maximumZ
                                            };
    }

    public double getMinimumZ()
    {
        return this.minimumZ;
    }

    public double getMaximumZ()
    {
        return this.maximumZ;
    }

    @Override
    public boolean hasZ()
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty()             &&
               Double.isNaN(this.minimumZ) &&
               Double.isNaN(this.maximumZ);
    }

    @Override
    public EnvelopeContentsIndicator getContentsIndicator()
    {
        return this.isEmpty() ? EnvelopeContentsIndicator.NoEnvelope
                              : EnvelopeContentsIndicator.Xyz;
    }

    /**
     * Combines two envelopes
     *
     * @param first
     *             The first envelope
     * @param second
     *             The second envelope
     * @return an envelope that minimally includes the entirety of the
     *             input envelopes
     */
    public static EnvelopeZ combine(final EnvelopeZ first,
                                    final EnvelopeZ second)
    {
        return new EnvelopeZ(nanMinimum(first.getMinimumX(), second.getMinimumX()),
                             nanMinimum(first.getMinimumY(), second.getMinimumY()),
                             nanMinimum(first.minimumZ,      second.minimumZ),
                             nanMaximum(first.getMaximumX(), second.getMaximumX()),
                             nanMaximum(first.getMaximumY(), second.getMaximumY()),
                             nanMaximum(first.maximumZ,      second.maximumZ));
    }

    /**
     * Empty envelope
     */
    public static final EnvelopeZ Empty = new EnvelopeZ(Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN);

    private final double minimumZ;
    private final double maximumZ;

    private static final double[] EMPTY_ARRAY = {};
}
