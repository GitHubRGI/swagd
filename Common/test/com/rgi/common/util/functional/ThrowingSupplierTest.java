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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ThrowingSupplierTest
{

    @SuppressWarnings("static-method")
	@Test(expected = RuntimeException.class)
    public void testGetThrowsException()
    {
        final ThrowingSupplier<String> ts = () ->
        {
            throw new NullPointerException();
        };
        ts.get();
        fail("Expected ThrowingSupplier method throws to throw at RuntimeException");
    }

    @SuppressWarnings("static-method")
	@Test
    public void testGet()
    {
        final ThrowingSupplier<String> ts = () -> "Hello, world!";
        final String result = "Hello, world!";
        assertTrue("Expected ThrowingSupplier method throws to return the string 'Hello, world.'", result.equals(ts.get()));
    }
}
