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
package com.rgi.geopackage.verification;

import java.util.Arrays;

/**
 *
 * @author Luke Lambert
 *
 */
public class Assert
{
    /**
     * @param message The error message that is displayed when the arrays do not equal
     * @param expecteds The expected values of the array
     * @param actuals the actual values of the array
     * @throws AssertionError throws when the arrays are not equal
     */
    public static void assertArrayEquals(final String message, final byte[] expecteds, final byte[] actuals) throws AssertionError
    {
        if(Arrays.equals(expecteds, actuals) == false)
        {
            throw new AssertionError(message);
        }
    }

    /**
     * @param condition a relational expression that returns true or false
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final boolean condition) throws AssertionError
    {
        Assert.assertTrue(null, condition);
    }

    /**
     * @param message the message that is displayed when the Assertion Error is thrown
     * @param condition the relational expression that returns true or false
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final String message, final boolean condition) throws AssertionError
    {
        if(condition == false)
        {
            Assert.fail(message);
        }
    }

    /**
     * @param message the message that is displayed when the Assertion Error is thrown
     * @throws AssertionError always is thrown
     */
    static public void fail(final String message) throws AssertionError
    {
        if(message == null)
        {
            throw new AssertionError();
        }

        throw new AssertionError(message);
    }
}
