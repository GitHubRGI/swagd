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

package com.rgi.android.geopackage.schema;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TypeTest {
    /**
     * Tests that fromString returns null
     * when passed a string that is not one of
     * the Type values
     */
    @Test
    public void fromStringNull(){
        final String name = "not in list";
        assertTrue("Expected Type method fromString to return null.", Type.fromString(name) == null);
    }

    /**
     * Tests that fromString returns
     * Range when passed the String "Range"
     */
    @Test
    public void fromStringRange(){
        final String name = "Range";
        assertTrue("Expected Type method fromString to return Range", Type.fromString(name) == Type.Range);
    }

    /**
     * Tests that fromString returns
     * Enum when passed the String "Enum"
     */
    @Test
    public void fromStringEnum(){
        final String name = "Enum";
        assertTrue("Expected Type method fromString to return Range", Type.fromString(name) == Type.Enum);
    }

    /**
     * Tests that fromString returns
     * Glob when passed the String "Glob"
     */
    @Test
    public void fromStringGlob(){
        final String name = "Glob";
        assertTrue("Expected Type method fromString to return Range", Type.fromString(name) == Type.Glob);
    }
}
