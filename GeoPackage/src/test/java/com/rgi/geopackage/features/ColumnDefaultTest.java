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

package com.rgi.geopackage.features;

import org.junit.Test;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * @author Luke Lambert
 */
public class ColumnDefaultTest
{
    private static final Pattern ESCAPE_SQL_STRING = Pattern.compile("'", Pattern.LITERAL);

    /**
     * Test ColumnDefault value for correct default expression values
     */
    @Test
    public void expressionDefault()
    {
        final String expression = "1";

        final ColumnDefault columnDefault = ColumnDefault.expression(expression);

        assertEquals("Incorrect expression default value",
                     String.format("(%s)", expression),
                     columnDefault.toString());
    }

    /**
     * Test ColumnDefault value for correct default int value
     */
    @Test
    public void intDefault()
    {
        final int value = 1;

        final ColumnDefault columnDefault = ColumnDefault.from(value);

        assertEquals("Incorrect int default value",
                     Integer.toString(value),
                     columnDefault.toString());
    }

    /**
     * Test ColumnDefault value for default double value
     */
    @Test
    public void doubleDefault()
    {
        final double value = 1.0;

        final ColumnDefault columnDefault = ColumnDefault.from(value);

        assertEquals("Incorrect double default value",
                     Double.toString(value),
                     columnDefault.toString());
    }

    /**
     * Test ColumnDefault value for default string value
     */
    @Test
    public void stringDefault()
    {
        final String value = "fo''o";

        final ColumnDefault columnDefault = ColumnDefault.from(value);

        assertEquals("Incorrect string default value",
                     String.format("'%s'", ESCAPE_SQL_STRING.matcher(value).replaceAll(Matcher.quoteReplacement("''"))),
                     columnDefault.toString());

    }

    /**
     * Test ColumnDefault value for default blob value
     */
    @Test
    public void blobDefault()
    {
        final byte[] value = {(byte)0, (byte)0};

        final ColumnDefault columnDefault = ColumnDefault.from(value);

        // This solution was found here:
        // https://stackoverflow.com/a/943963/16434
        final BigInteger bigInt = new BigInteger(1, value);

        assertEquals("Incorrect blob default value",
                     String.format("X'%0" + (value.length << 1) + "X'", bigInt),
                     columnDefault.toString());
    }

    /**
     * Test ColumnDefault value for default empty string ("none") value
     */
    @Test
    public void noneDefault()
    {
        assertEquals("Incorrect None default value",
                     "",
                     ColumnDefault.None.toString());
    }

    /**
     * Test ColumnDefault value for default null value
     */
    @Test
    public void nullDefault()
    {
        assertEquals("Incorrect null default value",
                     "NULL",
                     ColumnDefault.Null.toString());
    }

    /**
     * Test ColumnDefault value for current time default value
     */
    @Test
    public void currentTimeDefault()
    {
        assertEquals("Incorrect  default value",
                     "CURRENT_TIME",
                     ColumnDefault.CurrentTime.toString());
    }

    /**
     * Test ColumnDefault value for current date default value
     */
    @Test
    public void currentDateDefault()
    {
        assertEquals("Incorrect current date default value",
                     "CURRENT_DATE",
                     ColumnDefault.CurrentDate.toString());
    }

    /**
     * Test ColumnDefault value for current timestamp default value
     */
    @Test
    public void currentTimestampDefault()
    {
        assertEquals("Incorrect current timestamp default value",
                     "CURRENT_TIMESTAMP",
                     ColumnDefault.CurrentTimestamp.toString());
    }
}
