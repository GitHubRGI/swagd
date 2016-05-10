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

import com.rgi.geopackage.features.EnvelopeContentsIndicator;
import com.rgi.geopackage.features.geometry.xy.Envelope;

/**
 * @author Luke Lambert
 */
public final class EnvelopeZM extends Envelope
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
     * @param minimumM
     *             Minimum (inclusive) m value
     * @param maximumX
     *             Maximum (inclusive) x value
     * @param maximumY
     *             Maximum (inclusive) y value
     * @param maximumZ
     *             Maximum (inclusive) z value
     * @param maximumM
     *             Maximum (inclusive) m value
     */
    public EnvelopeZM(final double minimumX,
                      final double minimumY,
                      final double minimumZ,
                      final double minimumM,
                      final double maximumX,
                      final double maximumY,
                      final double maximumZ,
                      final double maximumM)
    {
        super(minimumX,
              minimumY,
              maximumX,
              maximumY);

        this.minimumZ = minimumZ;
        this.minimumM = minimumM;
        this.maximumZ = maximumZ;
        this.maximumM = maximumM;
    }

    /**
     * This is in the order:
     * minimum x, maximum x, minimum y, maximum y, minimum z, maximum z, minimum m, maximum m
     * @see <a href="http://www.geopackage.org/spec/#flags_layout">GeoPackage spec, Table 6. bit layout of GeoPackageBinary flags byte</a>
     *
     * @return An array of doubles in the order: minimum x, maximum x, minimum y, maximum y, minimum z, maximum z, minimum m, maximum m
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
                                              this.maximumZ,
                                              this.minimumM,
                                              this.maximumM
                                            };
    }

    public double getMinimumZ()
    {
        return this.minimumZ;
    }

    public double getMinimumM()
    {
        return this.minimumM;
    }

    public double getMaximumZ()
    {
        return this.maximumZ;
    }

    public double getMaximumM()
    {
        return this.maximumM;
    }

    @Override
    public boolean hasZ()
    {
        return true;
    }

    @Override
    public boolean hasM()
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty()             &&
               Double.isNaN(this.minimumZ) &&
               Double.isNaN(this.minimumM) &&
               Double.isNaN(this.maximumZ) &&
               Double.isNaN(this.maximumM);
    }

    @Override
    public EnvelopeContentsIndicator getContentsIndicator()
    {
        return this.isEmpty() ? EnvelopeContentsIndicator.NoEnvelope
                              : EnvelopeContentsIndicator.Xyzm;
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
    public static EnvelopeZM combine(final EnvelopeZM first,
                                     final EnvelopeZM second)
    {
        return new EnvelopeZM(nanMinimum(first.getMinimumX(), second.getMinimumX()),
                              nanMinimum(first.getMinimumY(), second.getMinimumY()),
                              nanMinimum(first.minimumZ,      second.minimumZ),
                              nanMinimum(first.minimumM,      second.minimumM),
                              nanMaximum(first.getMaximumX(), second.getMaximumX()),
                              nanMaximum(first.getMaximumY(), second.getMaximumY()),
                              nanMaximum(first.maximumZ,      second.maximumZ),
                              nanMaximum(first.maximumM,      second.maximumM));
    }

    /**
     * Empty envelope
     */
    public static final EnvelopeZM Empty = new EnvelopeZM(Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN,
                                                          Double.NaN);

    private final double minimumZ;
    private final double minimumM;
    private final double maximumZ;
    private final double maximumM;

    private static final double[] EMPTY_ARRAY = {};
}
