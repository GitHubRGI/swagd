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

package com.rgi.android.geopackage.extensions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Mary Carome
 *
 */
<<<<<<< HEAD
@SuppressWarnings({ "static-method" })
=======
>>>>>>> origin/master
public class ScopeTest
{

    /**
     * Tests that fromText throws an IllegalArgumentException when passed null
     * instead of a string
     */
    @Test(expected = IllegalArgumentException.class)
    public void fromTextIllegalArgumentException1()
    {
        final String name = null;
        Scope.fromText(name);
        fail("Expected Scope method fromText to throw an IllegalArgumentException when passed null instead of a valid String.");
    }

    /**
     * Tests that fromText throws an IllegalArgumentException when passed an
     * invalid string
     */
    @Test(expected = IllegalArgumentException.class)
    public void fromTextIllegalArgumentException2()
    {
        final String name = "not a valid String";
        Scope.fromText(name);
        fail("Expected Scope method fromText to throw an IllegalArgumentException when passed an nonvalid string.");
    }

    /**
     * Tests that fromText returns correct values when passed valid strings
     */
    @Test
    public void fromtText()
    {
        final String read = "read-write";
        final String write = "write-only";

<<<<<<< HEAD
        assertTrue(String.format("Expected Scope method fromText to return %s, but %s was returned", Scope.ReadWrite, Scope.fromText(read)), Scope.fromText(read) == Scope.ReadWrite);
        assertTrue(String.format("Expected Scope method fromText to return %s, but %s was returned", Scope.WriteOnly, Scope.fromText(write)), Scope.fromText(write) == Scope.WriteOnly);
=======
        assertTrue(
                String.format(
                        "Expected Scope method fromText to return %s, but %s was returned",
                        Scope.ReadWrite, Scope.fromText(read)),
                Scope.fromText(read) == Scope.ReadWrite);
        assertTrue(
                String.format(
                        "Expected Scope method fromText to return %s, but %s was returned",
                        Scope.WriteOnly, Scope.fromText(write)),
                Scope.fromText(write) == Scope.WriteOnly);
>>>>>>> origin/master
    }

    /**
     * Tests the toString method
     */
    @Test
    public void testToString()
    {
        final Scope read = Scope.ReadWrite;
        final Scope write = Scope.WriteOnly;
<<<<<<< HEAD
        assertTrue(String.format("Expected toString to return ''read-write' but returned '%s'", read.toString()), read.toString().equals("read-write"));
        assertTrue(String.format("Expected toString to return ''write-onlu' but returned '%s'", write.toString()), write.toString().equals("write-only"));
=======
        assertTrue(String.format(
                "Expected toString to return ''read-write' but returned '%s'",
                read.toString()), read.toString().equals("read-write"));
        assertTrue(String.format(
                "Expected toString to return ''write-onlu' but returned '%s'",
                write.toString()), write.toString().equals("write-only"));
>>>>>>> origin/master
    }
}
