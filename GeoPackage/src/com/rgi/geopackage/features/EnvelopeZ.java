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

package com.rgi.geopackage.features;

/**
 * @author Luke Lambert
 */
public final class EnvelopeZ extends Envelope
{
    public EnvelopeZ(final double minimumX,
                     final double maximumX,
                     final double minimumY,
                     final double maximumY,
                     final double minimumZ,
                     final double maximumZ)
    {
        super(minimumX,
              maximumX,
              minimumY,
              maximumY);

        this.minimumZ = minimumZ;
        this.maximumZ = maximumZ;
    }

    @Override
    public double[] toArray()
    {
        return new double[]{ this.getMinimumX(),
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

    public static EnvelopeZ combine(final EnvelopeZ first,
                                    final EnvelopeZ second)
    {
        return new EnvelopeZ(nanMinimum(first.getMinimumX(), second.getMinimumX()),
                             nanMaximum(first.getMaximumX(), second.getMaximumX()),
                             nanMinimum(first.getMinimumY(), second.getMinimumY()),
                             nanMaximum(first.getMaximumY(), second.getMaximumY()),
                             nanMinimum(first.minimumZ,      second.minimumZ),
                             nanMaximum(first.maximumZ,      second.maximumZ));
    }

    public static final EnvelopeZ Empty = new EnvelopeZ(Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN,
                                                        Double.NaN);

    private final double minimumZ;
    private final double maximumZ;
}
