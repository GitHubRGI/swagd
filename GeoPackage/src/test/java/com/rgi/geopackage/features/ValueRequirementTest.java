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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class ValueRequirementTest
{
    /**
     * Test basic values
     */
    @Test
    public void getValue()
    {
        assertEquals("Incorrect enum value",
                     0,
                     ValueRequirement.Prohibited.getValue());

        assertEquals("Incorrect enum value",
                     1,
                     ValueRequirement.Mandatory.getValue());

        assertEquals("Incorrect enum value",
                     2,
                     ValueRequirement.Optional.getValue());
    }

    /**
     * Test fromInt()
     */
    @Test
    public void fromInt()
    {
        assertSame("fromInt() returned the wrong value",
                   ValueRequirement.Prohibited,
                   ValueRequirement.fromInt(0));

        assertSame("fromInt() returned the wrong value",
                   ValueRequirement.Mandatory,
                   ValueRequirement.fromInt(1));

        assertSame("fromInt() returned the wrong value",
                   ValueRequirement.Optional,
                   ValueRequirement.fromInt(2));
    }

    /**
     * Test fromInt() with a bad value
     */
    @Test
    public void fromIntBadValue()
    {
        try
        {
            ValueRequirement.fromInt(-1);
            fail("fromInt() should fail for values outside the range [0, 2]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            ValueRequirement.fromInt(3);
            fail("fromInt() should fail for values outside the range [0, 2]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            ValueRequirement.fromInt(Integer.MAX_VALUE);
            fail("fromInt() should fail for values outside the range [0, 2]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            ValueRequirement.fromInt(Integer.MIN_VALUE);
            fail("fromInt() should fail for values outside the range [0, 2]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }
    }
}
