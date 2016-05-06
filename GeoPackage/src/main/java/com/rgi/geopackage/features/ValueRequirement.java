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

/**
 * Description for Geometry Column 'z' and 'm' value requirements
 *
 * @author Luke Lambert
 */
public enum ValueRequirement
{
    /**
     * Value is prohibited
     */
    Prohibited(0),

    /**
     * Value is required
     */
    Mandatory (1),

    /**
     * Value is optional
     */
    Optional  (2);

    ValueRequirement(final int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return this.value;
    }

    /**
     * Converts integer into a ValueRequirement enum value
     *
     * @param value
     *             Value to map
     * @return The appropriate corresponding enum value
     */
    public static ValueRequirement fromInt(final int value)
    {
        //noinspection SwitchStatement
        switch(value)
        {
            case 0: return Prohibited;
            case 1: return Mandatory;
            case 2: return Optional;

            default: throw new IllegalArgumentException("Value requirement must be 0, 1, or 2");
        }
    }

    private final int value;
}
