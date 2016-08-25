/*
 * The MIT License (MIT)
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

package com.rgi.dem2gh;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import org.gdal.ogr.DataSource;
import org.gdal.osr.SpatialReference;
import utility.GdalUtility;

import java.io.File;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class RoutingNetworkTerrainFactors
{
    public static void calculateAndWriteTerrainFactors(final File                      terrainFactors,
                                                       final File                      output,
                                                       final List<Node>                nodes,
                                                       final List<Edge>                edges,
                                                       final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        final SpatialReference sourceSpatialReference = GdalUtility.createSpatialReference(coordinateReferenceSystem);

        final DataSource dataSource = Utility.createDataSource(nodes,
                                                               edges,
                                                               sourceSpatialReference);
        try
        {

        }
        finally
        {
            dataSource.delete();    // Also destroys edgeLayer
        }
    }
}
