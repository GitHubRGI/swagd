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

package com.rgi.common.util;

import java.util.function.Supplier;


/**
 * Wrapper for {@link Supplier} which allows the throwing of an exception
 *
 * @param <T> The type of results supplied by this supplier
 *
 * @author Luke Lambert
 *
 * @see <a
 *      href="https://stackoverflow.com/a/27252163/16434">https://stackoverflow.com/a/27252163/16434</a>
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T>
{
    @Override
    public default T get()
    {
        try
        {
            return getThrows();
        }
        catch(final Throwable th)
        {
            throw new RuntimeException(th);
        }
    }

    /**
     * Applies this function to the given argument
     *
     * @return the function result
     *
     * @throws Throwable
     *             when the underlying throws
     */
    public T getThrows() throws Throwable;
}
