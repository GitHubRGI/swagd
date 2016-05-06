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

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class FeatureSetTest
{
    /**
     * Test constructor for failure on null primary key column name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullName()
    {
        new FeatureSet("table",
                       "id",
                       "description",
                       "date",
                       0.0,
                       0.0,
                       0.0,
                       0.0,
                       4326,
                       null,
                       "geometry",
                       null);

        fail("Constructor should fail on null primary key column name");
    }

    /**
     * Test constructor for failure on empty primary key column name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyName()
    {
        new FeatureSet("table",
                       "id",
                       "description",
                       "date",
                       0.0,
                       0.0,
                       0.0,
                       0.0,
                       4326,
                       "",
                       "geometry",
                       null);

        fail("Constructor should fail on empty primary key column name");
    }

    /**
     * Test constructor for failure on null geometry column name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullType()
    {
        new FeatureSet("table",
                       "id",
                       "description",
                       "date",
                       0.0,
                       0.0,
                       0.0,
                       0.0,
                       4326,
                       "primarykey",
                       null,
                       null);


        fail("Constructor should fail on null geometry column name");
    }

    /**
     * Test constructor for failure on empty geometry column name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyType()
    {
        new FeatureSet("table",
                       "id",
                       "description",
                       "date",
                       0.0,
                       0.0,
                       0.0,
                       0.0,
                       4326,
                       "primarykey",
                       "",
                       null);

        fail("Constructor should fail on empty geometry column name");
    }

    /**
     * Test getPrimaryKeyColumnName
     */
    @Test
    public void getPrimaryKeyColumnName()
    {
        final String primaryKeyColumnName = "primarykey";

        final FeatureSet featureSet = new FeatureSet("table",
                                                     "id",
                                                     "description",
                                                     "date",
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     4326,
                                                     primaryKeyColumnName,
                                                     "geometry",
                                                     null);

        assertEquals("getPrimaryKeyColumnName returned the incorrect value",
                     primaryKeyColumnName,
                     featureSet.getPrimaryKeyColumnName());
    }

    /**
     * Test getGeometryColumnName
     */
    @Test
    public void getGeometryColumnName()
    {
        final String geometryColumnName = "geometry";

        final FeatureSet featureSet = new FeatureSet("table",
                                                     "id",
                                                     "description",
                                                     "date",
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     4326,
                                                     "primarykey",
                                                     geometryColumnName,
                                                     null);

        assertEquals("getGeometryColumnName returned the incorrect value",
                     geometryColumnName,
                     featureSet.getGeometryColumnName());
    }

    /**
     * Test getAttributeColumnNames
     */
    @Test
    public void getAttributeColumnNames()
    {
        final Collection<String> attributeColumnNames = Arrays.asList("foo", "bar", "baz");

        final FeatureSet featureSet = new FeatureSet("table",
                                                     "id",
                                                     "description",
                                                     "date",
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     0.0,
                                                     4326,
                                                     "primarykey",
                                                     "geometry",
                                                     attributeColumnNames);

        final Collection<String> returnedColumnNames = featureSet.getAttributeColumnNames();

        assertTrue("getAttributeColumnNames returned the incorrect value",
                   attributeColumnNames.containsAll(returnedColumnNames) &&
                   returnedColumnNames.containsAll(attributeColumnNames));
    }
}
