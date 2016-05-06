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

import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.features.geometry.xy.WkbPoint;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class FeatureTest
{
    /**
     * Test that the constructor throws for a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
     public void nullGeometry()
    {
        new Feature(0,
                    null,
                    null);

        fail("Feature's constructor should fail on a null geometry");
    }

    /**
     * Test that the constructor sets an empty map when passed a null attribute map
     */
    @Test
    public void nullAttributes()
    {
        final Feature feature = new Feature(0,
                                            new WkbPoint(0.0, 0.0),
                                            null);

        assertEquals("Feature's constructor should set an empty map on a null attributes",
                     0,
                     feature.getAttributes().size());
    }

    /**
     * Test getIdentifier
     */
    @Test
    public void getIdentifier()
    {
        final int id = 0;

        final Feature feature = new Feature(id,
                                            new WkbPoint(0.0, 0.0),
                                            null);

        assertEquals("getIdentifier returned the wrong value",
                     id,
                     feature.getIdentifier());
    }

    /**
     * Test getGeometry
     */
    @Test
    public void getGeometry()
    {
        final Geometry geometry = new WkbPoint(0.0, 0.0);

        final Feature feature = new Feature(0,
                                            geometry,
                                            null);

        assertEquals("getGeometry returned the wrong value",
                     geometry,
                     feature.getGeometry());
    }

    /**
     * Test getAttributes
     */
    @Test
    public void getAttributes()
    {
        final Map<String, Object> attributes = new HashMap<>();

        attributes.put("a", 1);
        attributes.put("b", 2.0);
        attributes.put("c", "3");

        final Feature feature = new Feature(0,
                                            new WkbPoint(0.0, 0.0),
                                            attributes);

        assertEquals("getGeometry returned the wrong value",
                     attributes,
                     feature.getAttributes());
    }
}
