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

import static com.rgi.geopackage.features.EnvelopeContentsIndicator.NoEnvelope;
import static com.rgi.geopackage.features.EnvelopeContentsIndicator.Xy;
import static com.rgi.geopackage.features.EnvelopeContentsIndicator.Xym;
import static com.rgi.geopackage.features.EnvelopeContentsIndicator.Xyz;
import static com.rgi.geopackage.features.EnvelopeContentsIndicator.Xyzm;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class EnvelopeContentsIndicatorTest
{
    /**
     * Test the enum values
     */
    @Test
    public void testValues()
    {
        assertTrue("NoEnvelope has incorrect values", NoEnvelope.getCode() == 0 && NoEnvelope.getArraySize() == 0);
        assertTrue("Xy         has incorrect values", Xy        .getCode() == 1 && Xy        .getArraySize() == 4);
        assertTrue("Xyz        has incorrect values", Xyz       .getCode() == 2 && Xyz       .getArraySize() == 6);
        assertTrue("Xym        has incorrect values", Xym       .getCode() == 3 && Xym       .getArraySize() == 6);
        assertTrue("Xyzm       has incorrect values", Xyzm      .getCode() == 4 && Xyzm      .getArraySize() == 8);
    }

    /**
     * Test fromCode()
     */
    @Test
    public void fromCode()
    {
        assertSame("fromCode() returned an incorrect value", NoEnvelope, EnvelopeContentsIndicator.fromCode(0));
        assertSame("fromCode() returned an incorrect value", Xy,         EnvelopeContentsIndicator.fromCode(1));
        assertSame("fromCode() returned an incorrect value", Xyz,        EnvelopeContentsIndicator.fromCode(2));
        assertSame("fromCode() returned an incorrect value", Xym,        EnvelopeContentsIndicator.fromCode(3));
        assertSame("fromCode() returned an incorrect value", Xyzm,       EnvelopeContentsIndicator.fromCode(4));
    }

    /**
     * Test fromCode() with bad values
     */
    @Test
    public void fromCodeBadValue()
    {
        try
        {
            EnvelopeContentsIndicator.fromCode(-1);
            fail("fromInt() should fail for values outside the range [0, 4]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            EnvelopeContentsIndicator.fromCode(5);
            fail("fromInt() should fail for values outside the range [0, 4]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            EnvelopeContentsIndicator.fromCode(Integer.MAX_VALUE);
            fail("fromInt() should fail for values outside the range [0, 4]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }

        try
        {
            EnvelopeContentsIndicator.fromCode(Integer.MIN_VALUE);
            fail("fromInt() should fail for values outside the range [0, 4]");
        }
        catch(final IllegalArgumentException ignored)
        {
            // ignored
        }
    }
}
