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

package com.rgi.common;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generic range class.  Minimum and maximum values are inclusive.
 *
 * @author Luke Lambert
 * @param <T> Any reference type
 *
 */
public class Range <T>
{
    /**
     * Constructor
     *
     * @param minimum
     *             Minimum value (inclusive)
     * @param maximum
     *             Maximum value (inclusive)
     */
    public Range(final T minimum, final T maximum)
    {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * This constructor iterates over a container whose generic type is
     * mapped to the T via the mapper function.  The comparison function
     * determines the ordering of the container's objects.
     *
     * @param iterable
     *             Container of objects to be map
     * @param mapper
     *             Maps the container type <I> to the Range type <T>
     * @param comparator
     *             Comparison function
     */
    public <I> Range(final Iterable<I> iterable,
                     final Function<? super I, ? extends T> mapper,
                     final Comparator<? super T> comparator)
    {
        if(iterable == null)
        {
            throw new IllegalArgumentException("Iterable may not be null");
        }

        if(!iterable.iterator().hasNext())
        {
            throw new IllegalArgumentException("Iterable may not be empty");
        }

        if(mapper == null)
        {
            throw new IllegalArgumentException("Mapper may not be null");
        }

        if(comparator == null)
        {
            throw new IllegalArgumentException("Comparator may not be null");
        }

        T min = null;
        T max = null;

        for(final I value : iterable)
        {
            final T mappedValue = mapper.apply(value);

            if(min == null || comparator.compare(mappedValue, min) < 0)
            {
                min = mappedValue;
            }

            if(max == null || comparator.compare(mappedValue, max) > 0)
            {
                max = mappedValue;
            }
        }

        this.minimum = min;
        this.maximum = max;
    }

    /**
     * Constructor
     *
     * This constructor iterates over a container using the supplied comparison
     * function to determine the minimum and maximum values
     *
     * @param iterable
     *             Container of objects
     * @param comparator
     *             Comparison function
     */
    public Range(final Iterable<T> iterable,
                 final Comparator<? super T> comparator)
    {
        if(iterable == null)
        {
            throw new IllegalArgumentException("Iterable may not be null");
        }

        final Iterator<T> iterator = iterable.iterator();

        if(!iterator.hasNext())
        {
            throw new IllegalArgumentException("Iterable may not be empty");
        }

        if(comparator == null)
        {
            throw new IllegalArgumentException("Comparator may not be null");
        }

        T min = null;
        T max = null;

        while(iterator.hasNext())
        {
            final T value = iterator.next();

            if(min == null || comparator.compare(value, min) < 0)
            {
                min = value;
            }

            if(max == null || comparator.compare(value, max) > 0)
            {
                max = value;
            }
        }

        this.minimum = min;
        this.maximum = max;
    }

    /**
     * This constructor iterates over a container using the supplied comparison
     * function to determine the minimum and maximum values
     *
     * @param stream
     *             Stream of objects
     * @param comparator
     *             Comparison function
     */
    public Range(final Stream<T> stream,
                 final Comparator<? super T> comparator)
    {
        this((Iterable<T>)stream::iterator, comparator);
    }

    @Override
    public String toString()
    {
        return String.format("[%s, %s]",
                             this.minimum,
                             this.maximum);
    }

    /**
     * @return the minimum
     */
    public T getMinimum()
    {
        return this.minimum;
    }

    /**
     * @return the maximum
     */
    public T getMaximum()
    {
        return this.maximum;
    }

    private final T minimum;
    private final T maximum;
}
