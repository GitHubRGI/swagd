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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by steven.lander on 10/28/15.
 */
public class PairTest
{
    @Test
    public void testNullPair()
    {
        final Pair<String, String> stringPair1 = new Pair<>(null, null);
        final Pair<String, String> stringPair2 = new Pair<>(null, null);
        assertEquals("Null pairs should evaluate true.", stringPair1, stringPair2);
    }

    @Test
    public void testGetLeft()
    {
        final Pair<Integer, Integer> integerPair = new Pair<>(1, 2);
        assertEquals("Get left should return left value.", 1, (int)integerPair.getLeft());
    }

    @Test
    public void testGetRight()
    {
        final Pair<Integer, Integer> integerPair = new Pair<>(1, 2);
        assertEquals("Get left should return left value.", 2, (int)integerPair.getRight());
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEqualsBranchOne()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        assertTrue("Pairs should be equal.", stringIntegerPair.equals(stringIntegerPair));
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void testEqualsBranchTwo()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        final Integer notEqualObject = 1;
        assertFalse("Pairs should be equal.", stringIntegerPair.equals(notEqualObject));
    }

    @SuppressWarnings("ObjectEqualsNull")
    @Test
    public void testEqualsBranchThree()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        assertFalse("Pairs should be equal.", stringIntegerPair.equals(null));
    }

    @Test
    public void testEqualsBranchFour()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("foo", 2);
        assertFalse("Pairs should be equal.", stringIntegerPair1.equals(stringIntegerPair2));
    }

    @Test
    public void testEqualsBranchFive()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("bar", 1);
        assertFalse("Pairs should be equal.", stringIntegerPair1.equals(stringIntegerPair2));
    }

    @Test
    public void testHashcode()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("foo", 1);
        assertEquals("Hashcodes should be equal.", stringIntegerPair1.hashCode(), stringIntegerPair2.hashCode());
    }
}
