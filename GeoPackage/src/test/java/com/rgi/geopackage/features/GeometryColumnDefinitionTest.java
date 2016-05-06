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
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class GeometryColumnDefinitionTest
{
    /**
     * Test the constructor with a null zValueRequirement
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullZValueRequirement()
    {
        new GeometryColumnDefinition("name",
                                     "type",
                                     null,
                                     ValueRequirement.Optional,
                                     "comment");

        fail("Constructor should have thrown on a null z value requirement parameter");
    }

    /**
     * Test the constructor with a null mValueRequirement
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullMValueRequirement()
    {
        new GeometryColumnDefinition("name",
                                     "type",
                                     ValueRequirement.Optional,
                                     null,
                                     "comment");

        fail("Constructor should have thrown on a null m value requirement parameter");
    }

    /**
     * Test getZRequirement()
     */
    @Test
    public void getZRequirement()
    {
        final ValueRequirement zValueRequirement = ValueRequirement.Mandatory;

        final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("name",
                                                                                     "type",
                                                                                     zValueRequirement,
                                                                                     ValueRequirement.Mandatory,
                                                                                     "comment");

        assertEquals("getZRequirement() returned the incorrect value",
                     zValueRequirement,
                     geometryColumn.getZRequirement());
    }

    /**
     * Test getMRequirement()
     */
    @Test
    public void getMRequirement()
    {
        final ValueRequirement mValueRequirement = ValueRequirement.Mandatory;

        final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("name",
                                                                                     "type",
                                                                                     ValueRequirement.Mandatory,
                                                                                     mValueRequirement,
                                                                                     "comment");

        assertEquals("getMRequirement() returned the incorrect value",
                     mValueRequirement,
                     geometryColumn.getMRequirement());
    }

}
