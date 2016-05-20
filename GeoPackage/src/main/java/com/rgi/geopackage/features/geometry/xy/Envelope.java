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

import com.rgi.geopackage.features.EnvelopeContentsIndicator;

/**
 * @author Luke Lambert
 */
public class Envelope
{
    /**
     * Constructor
     *
     * @param minimumX
     *             Minimum (inclusive) x value
     * @param minimumY
     *             Minimum (inclusive) y value
     * @param maximumX
     *             Maximum (inclusive) x value
     * @param maximumY
     *             Maximum (inclusive) y value
     */
    public Envelope(final double minimumX,
                    final double minimumY,
                    final double maximumX,
                    final double maximumY)
    {
        this.minimumX = minimumX;
        this.minimumY = minimumY;
        this.maximumX = maximumX;
        this.maximumY = maximumY;
    }

    /**
     * This is in the order:
     * minimum x, maximum x, minimum y, maximum y
     * @see <a href="http://www.geopackage.org/spec/#flags_layout">GeoPackage spec, Table 6. bit layout of GeoPackageBinary flags byte</a>
     *
     * @return An array of doubles in the order: minimum x, maximum x, minimum y, maximum y
     */
    public double[] toArray()
    {
        return this.isEmpty() ? EMPTY_ARRAY
                              : new double[]{ this.minimumX,
                                              this.maximumX,
                                              this.minimumY,
                                              this.maximumY
                                            };
    }

    public double getMinimumX()
    {
        return this.minimumX;
    }

    public double getMinimumY()
    {
        return this.minimumY;
    }

    public double getMaximumX()
    {
        return this.maximumX;
    }

    public double getMaximumY()
    {
        return this.maximumY;
    }

    /**
     * @return indicates whether or not this Envelope has a Z component
     */
    public boolean hasZ()
    {
        return false;
    }

    /**
     * @return indicates whether or not this Envelope has a M component
     */
    public boolean hasM()
    {
        return false;
    }

    public boolean isEmpty()
    {
        return Double.isNaN(this.minimumX) &&
               Double.isNaN(this.minimumY) &&
               Double.isNaN(this.maximumX) &&
               Double.isNaN(this.maximumY);
    }

    public EnvelopeContentsIndicator getContentsIndicator()
    {
        return this.isEmpty() ? EnvelopeContentsIndicator.NoEnvelope
                              : EnvelopeContentsIndicator.Xy;
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
    public static Envelope combine(final Envelope first,
                                   final Envelope second)
    {
        return new Envelope(nanMinimum(first.minimumX, second.minimumX),
                            nanMinimum(first.minimumY, second.minimumY),
                            nanMaximum(first.maximumX, second.maximumX),
                            nanMaximum(first.maximumY, second.maximumY));
    }

    /**
     * Empty envelope
     */
    public static final Envelope Empty = new Envelope(Double.NaN,
                                                      Double.NaN,
                                                      Double.NaN,
                                                      Double.NaN);

    /**
     * Gets the minimum of two doubles. If both values are NaN, the result is
     * NaN. One component is NaN, the other value is returned as the minimum.
     *
     * @param first
     *             double value
     * @param second
     *             double value
     * @return the minimum of two values
     */
    public static double nanMinimum(final double first, final double second)
    {
        if(Double.isNaN(first) && Double.isNaN(second))
        {
            return Double.NaN;
        }

        if(Double.isNaN(first))
        {
            return second;
        }

        if(Double.isNaN(second))
        {
            return first;
        }

        return Double.min(first, second);
    }

    /**
     * Gets the maximum of two doubles. If both values are NaN, the result is
     * NaN. One component is NaN, the other value is returned as the maximum.
     *
     * @param first
     *             double value
     * @param second
     *             double value
     * @return the maximum of two values
     */
    public static double nanMaximum(final double first, final double second)
    {
        if(Double.isNaN(first) && Double.isNaN(second))
        {
            return Double.NaN;
        }

        if(Double.isNaN(first))
        {
            return second;
        }

        if(Double.isNaN(second))
        {
            return first;
        }

        return Double.max(first, second);
    }

    private final double minimumX;
    private final double minimumY;
    private final double maximumX;
    private final double maximumY;

    private static final double[] EMPTY_ARRAY = {};
}
