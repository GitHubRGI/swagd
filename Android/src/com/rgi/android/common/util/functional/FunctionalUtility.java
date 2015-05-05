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
    /**
     * Returns whether any of the elements of the given {@Link Collection} match the provided {@Link Predicate}
     * May not evaluate the predicate on all elements if not necessary for determining the result.  If the given
     * collection is empty the {@code false} is returned and the predicate is not evaluated.
     * Both the Collection and Predicate must be of the same type parameter.
     *
     * @param collection
     *          The {@Link Collection} of elements to apply the given {@Link Predicate} condition to
     * @param predicate
     *          Applies the operation to each element in the given {@Link Collection} to evaluate the given element
     * @return
     *         True if finds one element that satisfies the predicate; False if none of the elements in the collection satisfies the predicate
     */
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

    /**
     * Returns an {@Link ArrayList} of the input type parameter consisting of the elements of this {@Link Collection} that match
     * the given {@Link Predicate} which has the same type parameter as the given Collection
     *
     * @param collection
     *           The {@Link Collection} of elements that will be filtered by the given {@Link Predicate}
     * @param predicate
     *           The {@Link Predicate} that determines if the element is added to the returned {@Link ArrayList} of the input type parameter
     * @return
     *           The {@Link ArrayList} of the input type parameter consisting of the elements of this {@Link Collection} that match the given {@Link Predicate}
     */
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

    /**
     * Returns an {@Link ArrayList} of the output type parameter consisting of the results
     * of applying the Mapper function to the elements in the given {@Link Collection}
     *
     * @param collection
     *      The {@Link Collection} of elements that the given mapper will be applied
     * @param mapper
     *      Contains the apply function that will map each element in the given {@Link Collection} of the input type to an output of a different type
     * @return
     *      Returns an {@Link ArrayList} of the output type consisting of the elements mapped based on the elements in the given {@Link Collection}
     */
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

    /**
     * @param collection
     * @param mapper
     * @param predicate
     * @return
     */
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

    /**
     * @param collection
     * @param predicate
     * @param mapper
     * @return
     */
    public static <I, O> ArrayList<O> filterMap(final Collection<I> collection,
                                                final Predicate<I>  predicate,
                                                final Mapper<I, O>  mapper)
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

        return FunctionalUtility.map(FunctionalUtility.filter(collection, predicate), mapper);
    }
}
