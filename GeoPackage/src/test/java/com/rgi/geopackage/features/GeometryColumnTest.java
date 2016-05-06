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

/**
 * @author Luke Lambert
 */
public class GeometryColumnTest
{
    /**
     * Test getTableName()
     */
    @Test
    public void getTableName()
    {
        final String tableName = "table";

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 "POINT",
                                                                 4326,
                                                                 ValueRequirement.Mandatory,
                                                                 ValueRequirement.Mandatory);

        assertEquals("getTableName() returned the incorrect value",
                     tableName,
                     geometryColumn.getTableName());
    }

    /**
     * Test getColumnName()
     */
    @Test
    public void getColumnName()
    {
        final String columnName = "column";

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 "POINT",
                                                                 4326,
                                                                 ValueRequirement.Mandatory,
                                                                 ValueRequirement.Mandatory);

        assertEquals("getColumnName() returned the incorrect value",
                     columnName,
                     geometryColumn.getColumnName());
    }

    /**
     * Test getGeometryType()
     */
    @Test
    public void getGeometryType()
    {
        final String geometryType = "POINT";

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 geometryType,
                                                                 4326,
                                                                 ValueRequirement.Mandatory,
                                                                 ValueRequirement.Mandatory);

        assertEquals("getGeometryType() returned the incorrect value",
                     geometryType,
                     geometryColumn.getGeometryType());
    }

    /**
     * Test getSpatialReferenceSystemIdentifier()
     */
    @Test
    public void getSpatialReferenceSystemIdentifier()
    {
        final int srsId = 4326;

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 "POINT",
                                                                 srsId,
                                                                 ValueRequirement.Mandatory,
                                                                 ValueRequirement.Mandatory);

        assertEquals("getSpatialReferenceSystemIdentifier() returned the incorrect value",
                     srsId,
                     geometryColumn.getSpatialReferenceSystemIdentifier());
    }

    /**
     * Test getZRequirement()
     */
    @Test
    public void getZRequirement()
    {
        final ValueRequirement zValueRequirement = ValueRequirement.Mandatory;

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 "POINT",
                                                                 4326,
                                                                 zValueRequirement,
                                                                 ValueRequirement.Mandatory);

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

        final GeometryColumn geometryColumn = new GeometryColumn("table",
                                                                 "column",
                                                                 "POINT",
                                                                 4326,
                                                                 ValueRequirement.Mandatory,
                                                                 mValueRequirement);

        assertEquals("getMRequirement() returned the incorrect value",
                     mValueRequirement,
                     geometryColumn.getMRequirement());
    }
}
