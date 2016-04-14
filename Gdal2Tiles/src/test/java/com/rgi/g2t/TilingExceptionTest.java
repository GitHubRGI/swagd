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

package com.rgi.g2t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.rgi.g2t.TilingException;

/**
 * Unit tests for Tiling Exceptions
 *
 * @author Mary Carome
 */
@SuppressWarnings("javadoc")
public class TilingExceptionTest
{
    /**
     * Test default constructor for TilingException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testDefaultConstructor()
    {
        final Exception exception =  new TilingException("test");

        assertSame("TilingException constructor did not make an exception of the correct type.",
                   TilingException.class,
                   exception.getClass());
    }

    /**
     * Tests message constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testMessageAndCauseConstructor()
    {
        final Throwable cause = new Throwable("cause");
        final String message = "Message";
        final TilingException exception = new TilingException(message, cause);

        assertEquals("TilingException constructor did not correctly set the message for the exception.",
                     message,
                     exception.getMessage());

        assertEquals("TilingException constructor did not correctly set the cause for the exception.",
                     cause,
                     exception.getCause());
    }

    /**
     * Tests message constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testMessageConstructor()
    {
        final Exception exception = new TilingException("This is a test");

        assertEquals("TilingException constructor did not correctly set the message for the exception.",
                     "This is a test",
                     exception.getMessage());
    }

    /**
     * Tests Throwable constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCauseConstructor()
    {
        final Throwable cause = new Throwable("Testing!!");
        final Exception exception = new TilingException(cause);

        assertEquals("TilingException constructor did not correct setly the cause for the exception.",
                     cause,
                     exception.getCause());
    }
}
