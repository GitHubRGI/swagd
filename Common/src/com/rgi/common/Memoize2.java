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

import com.rgi.common.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Functional memoization strategy, based on <a
 * href="https://stackoverflow.com/a/3624099/16434">this example</a>.
 *
 * @param <P1> Type of first parameter
 * @param <P2> Type of second parameter
 * @param <R> Type of object returned
 *
 * @author Luke Lambert
 */
public class Memoize2<P1, P2, R>
{
    public Memoize2(final BiFunction<P1, P2, R> evaluator)
    {
        if(evaluator == null)
        {
            throw new IllegalArgumentException("Evaluator may not be null");
        }

        this.evaluator = evaluator;
    }

    public R get(final P1 parameter1, final P2 parameter2)
    {
        final Pair<P1, P2> pair = Pair.of(parameter1, parameter2);

        if(this.values.containsKey(pair))
        {
            return this.values.get(pair);
        }
        else
        {
            final R value = this.evaluator.apply(parameter1, parameter2);
            this.values.put(pair, value);
            return value;
        }
    }

    private final Map<Pair<P1, P2>, R> values = new HashMap<Pair<P1, P2>, R>();   // TODO maybe use a hash as a key
    private final BiFunction<P1, P2, R> evaluator;
}
