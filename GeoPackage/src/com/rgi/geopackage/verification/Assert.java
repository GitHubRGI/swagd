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
     * @param message
     *             The error message that is displayed when the arrays do not equal
     * @param expected
     *             The expected values of the array
     * @param actual
     *             The actual values of the array
     * @param severity
     *             Indication of the level of problem encountered when testing
     *             the conformity of a file to the
     *             <a href="http://www.geopackage.org/spec/">GeoPackage
     *             Standards</a> specification
     * @throws AssertionError throws when the arrays are not equal
     */
    public static void assertArrayEquals(final String   message,
                                         final byte[]   expected,
                                         final byte[]   actual,
                                         final Severity severity) throws AssertionError
    {
        if(Arrays.equals(expected, actual) == false)
        {
            throw new AssertionError(message, severity);
        }
    }

    /**
     * @param condition
     *             A relational expression that returns true or false
     * @param severity
     *             Indication of the level of problem encountered when testing
     *             the conformity of a file to the
     *             <a href="http://www.geopackage.org/spec/">GeoPackage
     *             Standards</a> specification
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final boolean condition, final Severity severity) throws AssertionError
    {
        Assert.assertTrue(null, condition, severity);
    }

    /**
     * @param message
     *             The message that is displayed when the Assertion Error is thrown
     * @param condition
     *             The relational expression that returns true or false
     * @param severity
     *             Indication of the level of problem encountered when testing
     *             the conformity of a file to the
     *             <a href="http://www.geopackage.org/spec/">GeoPackage
     *             Standards</a> specification
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final String   message,
                                  final boolean  condition,
                                  final Severity severity) throws AssertionError
    {
        if(condition == false)
        {
            Assert.fail(message, severity);
        }
    }

    /**
     * @param message
     *             The message that is displayed when the Assertion Error is thrown
     * @param severity
     *             Indication of the level of problem encountered when testing
     *             the conformity of a file to the
     *             <a href="http://www.geopackage.org/spec/">GeoPackage
     *             Standards</a> specification
     * @throws AssertionError always is thrown
     */
    static public void fail(final String message, final Severity severity) throws AssertionError
    {
        if(message == null)
        {
            throw new AssertionError((String)null, severity);
        }

        throw new AssertionError(message, severity);
    }
}
