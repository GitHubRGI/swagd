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

package com.rgi.android.common.util.functional;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Luke Lambert
 *
 */
public class FunctionalUtility
{
    public static <T> boolean anyMatch(final Collection<T> collection, final Predicate<T> predicate)
    {
        if(collection == null)
        {
            throw new IllegalArgumentException("Collection may not be null");
        }

        if(predicate == null)
        {
            throw new IllegalArgumentException("Predicate may not be null");
        }

        for(final T t : collection)
        {
            if(predicate.apply(t))
            {
                return true;
            }
        }

        return false;
    }

    public static <T> ArrayList<T> filter(final Collection<T> collection, final Predicate<T> predicate)
    {
        if(collection == null)
        {
            throw new IllegalArgumentException("Collection may not be null");
        }

        if(predicate == null)
        {
            throw new IllegalArgumentException("Predicate may not be null");
        }

        final ArrayList<T> newCollection = new ArrayList<T>();

        for(final T t : collection)
        {
            if(predicate.apply(t))
            {
                newCollection.add(t);
            }
        }

        return newCollection;
    }

    public static <I, O> ArrayList<O> map(final Collection<I> collection, final Mapper<I, O> mapper)
    {
        if(collection == null)
        {
            throw new IllegalArgumentException("Collection may not be null");
        }

        if(mapper == null)
        {
            throw new IllegalArgumentException("Mapper may not be null");
        }

        final ArrayList<O> newCollection = new ArrayList<O>();

        for(final I i : collection)
        {
            newCollection.add(mapper.apply(i));
        }

        return newCollection;
    }

    public static <I, O> ArrayList<O> mapFilter(final Collection<I> collection,
                                                final Mapper<I, O>  mapper,
                                                final Predicate<O>  predicate)
    {
        if(collection == null)
        {
            throw new IllegalArgumentException("Collection may not be null");
        }

        if(mapper == null)
        {
            throw new IllegalArgumentException("Mapper may not be null");
        }

        if(predicate == null)
        {
            throw new IllegalArgumentException("Predicate may not be null");
        }

        return FunctionalUtility.filter(FunctionalUtility.map(collection, mapper), predicate);
    }
}
