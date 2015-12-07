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

import java.util.function.BinaryOperator;

/**
 * @author Luke Lambert
 */
public class Envelope
{
    public Envelope(final EnvelopeContentsIndicator contentsIndicator, final double[] array)
    {
        if(array == null)
        {
            throw new IllegalArgumentException("Array may not be null");
        }

        if(contentsIndicator == null)
        {
            throw new IllegalArgumentException("Contents indicator may not be null");
        }

        if(array.length != contentsIndicator.getArraySize())
        {
            throw new IllegalArgumentException(String.format("Envelop containing parameters %s must correspond to an array with %d elements",
                                                             contentsIndicator.toString().toUpperCase(),
                                                             contentsIndicator.getArraySize()));
        }

        this.array             = array.clone();
        this.contentsIndicator = contentsIndicator;
    }

    public EnvelopeContentsIndicator getContentsIndicator()
    {
        return this.contentsIndicator;
    }

    public double[] getArray()
    {
        return this.array.clone();
    }

    public static final BinaryOperator<Envelope> combine = (final Envelope first, final Envelope second) ->
                                                           {
                                                               if(first.contentsIndicator != second.contentsIndicator)
                                                               {
                                                                   throw new IllegalArgumentException("Envelopes must have the same contents indicator");
                                                               }

                                                               final int arrayLength = first.array.length;

                                                               final double[] newArray = new double[arrayLength];

                                                               for(int index = 0; index < arrayLength; index += 2)
                                                               {
                                                                   newArray[index    ] = nanMinimum(first.array[index    ], second.array[index    ]);
                                                                   newArray[index + 1] = nanMaximum(first.array[index + 1], second.array[index + 1]);
                                                               }

                                                               return new Envelope(first.contentsIndicator, newArray);
                                                           };


    @SuppressWarnings("StaticVariableOfConcreteClass")
    public static final Envelope Empty = new Envelope(EnvelopeContentsIndicator.NoEnvelope, new double[]{});

    private static double nanMinimum(final double first, final double second)
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

    private static double nanMaximum(final double first, final double second)
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

    private final double[]                  array;
    private final EnvelopeContentsIndicator contentsIndicator;
}
