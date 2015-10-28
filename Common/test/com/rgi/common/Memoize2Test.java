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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.function.BiFunction;

/**
 * Created by steven.lander on 10/28/15.
 */
public class Memoize2Test
{
    @Test
    public void testMemoizeGetOnce()
    {
        final BiFunction<String, String, Integer> stringAdd = (x, y) -> Integer.valueOf(x) + Integer.valueOf(y);
        final Memoize2<String, String, Integer> stringAddMemoizer = new Memoize2<>(stringAdd);
        final Integer result = stringAddMemoizer.get("1", "1");
        final Integer desiredResult = 2;
        assertEquals("Memoized result should equal 2.", desiredResult, result);
    }

    @Test
    public void testMemoizeGetTwice()
    {
        final BiFunction<String, String, Integer> stringAdd = (x, y) -> Integer.valueOf(x) + Integer.valueOf(y);
        final Memoize2<String, String, Integer> stringAddMemoizer = new Memoize2<>(stringAdd);
        final Integer resultOnce = stringAddMemoizer.get("1", "1");
        final Integer resultTwice = stringAddMemoizer.get("1", "1");
        final Integer desiredResult = 2;
        assertEquals("Memoized result should equal 2.", desiredResult, resultTwice);
    }
}
