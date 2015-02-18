/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.common.util;

import java.util.Comparator;
import java.util.function.Function;

/**
 * @author Luke Lambert
 *
 */
public class Range <T>
{
    /**
     * @param minimum
     * @param maximum
     */
    public Range(final T minimum, final T maximum)
    {
        this.minimum = minimum;
        this.maximum = maximum;
    }

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

    public Range(final Iterable<T> iterable,
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

        if(comparator == null)
        {
            throw new IllegalArgumentException("Comparator may not be null");
        }

        T min = null;
        T max = null;

        for(final T value : iterable)
        {
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
