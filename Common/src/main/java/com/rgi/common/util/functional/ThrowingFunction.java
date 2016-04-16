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

package com.rgi.common.util.functional;

import java.util.function.Function;

/**
 * Wrapper for {@link Function} which allows the throwing of an exception
 *
 * @param <T> The type of the input to the function
 * @param <R> The type of the result of the function
 *
 * @author Luke Lambert
 *
 * @see <a
 *      href="https://stackoverflow.com/a/27252163/16434">https://stackoverflow.com/a/27252163/16434</a>
 */
@FunctionalInterface
public interface ThrowingFunction<T, R> extends Function<T, R>
{
    @Override
    public default R apply(final T t)
    {
        try
        {
            return applyThrows(t);
        }
        catch(final Throwable th)
        {
            throw new RuntimeException(th);
        }
    }

    /**
     * Applies this function to the given argument
     *
     * @param t the function argument
     *
     * @return the function result
     *
     * @throws Throwable
     *             when the underlying apply throws
     */
    public R applyThrows(T t) throws Throwable;
}
