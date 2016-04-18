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

/**
 *
 * @author Mary Carome
 *
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ThrowingFunctionTest
{
    @Test(expected = RuntimeException.class)
    public void testApplyThrowsException()
    {
        final ThrowingFunction<String, Integer> tf = t -> { if(t == null || t.length() == 0)
                                                            {
                                                                throw new IllegalArgumentException();
                                                            }
                                                            return t.length();
                                                          };
        tf.apply("");
        fail("Expexted ThrowingFunction method apply to throw a RuntimeException error.");
    }

    @Test
    public void testApply()
    {
        final ThrowingFunction<String, Integer> tf = t -> { if(t == null || t.length() == 0)
                                                            {
                                                                throw new IllegalArgumentException();
                                                            }
                                                            return t.length();
                                                          };
        final String test = "foo";
        assertTrue(tf.apply(test) == test.length());
    }
}
