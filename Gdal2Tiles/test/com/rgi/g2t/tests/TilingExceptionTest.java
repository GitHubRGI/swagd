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

package com.rgi.g2t.tests;

import static org.junit.Assert.assertTrue;

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
        final Exception e =  new TilingException();

        assertTrue("TilingException constructor did not make an exception of the correct type.",
                   e.getClass().equals(TilingException.class));
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
        final TilingException e = new TilingException(message, cause);

        assertTrue("TilingException constructor did not correctly set the message for the exception.",
                e.getMessage().equals(message));

        assertTrue("TilingException constructor did not correctly set the cause for the exception.",
                   e.getCause().equals(cause));
    }

    /**
     * Tests message constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testMessageConstructor()
    {
        final Exception e = new TilingException("This is a test");

        assertTrue("TilingException constructor did not correctly set the message for the exception.",
                   e.getMessage().equals("This is a test"));
    }

    /**
     * Tests Throwable constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCauseConstructor()
    {
        final Throwable cause = new Throwable("Testing!!");
        final Exception e = new TilingException(cause);

        assertTrue("TilingException constructor did not correct setly the cause for the exception.",
                    e.getCause().equals(cause));
    }


}
