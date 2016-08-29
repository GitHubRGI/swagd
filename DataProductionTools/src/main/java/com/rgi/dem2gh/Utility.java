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

import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;
import utility.GdalError;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luke Lambert
 */
public class Utility
{
    public static DataSource createDataSource(final List<Node>       nodes,
                                              final List<Edge>       edges,
                                              final SpatialReference sourceSpatialReference)
    {
       final DataSource dataSource = ogr.GetDriverByName("Memory")
                                         .CreateDataSource("vector data");

        try
        {
            final Layer edgeLayer = createEdgeLayer(dataSource,
                                                    nodes,
                                                    edges,
                                                    sourceSpatialReference);
        }
        catch(final Throwable th)
        {
            dataSource.delete();
            throw th;
        }

        return dataSource;
    }

    private static Layer createEdgeLayer(final DataSource       dataSource,
                                         final Collection<Node> nodes,
                                         final Iterable<Edge>   edges,
                                         final SpatialReference spatialReference)
    {
         final Map<Integer, Node> nodeMap = new HashMap<>(nodes.size());

        for(final Node node : nodes)
        {
            nodeMap.put(node.getIdentifier(), node);
        }

        final Layer edgeLayer = dataSource.CreateLayer("edges",
                                                       spatialReference,
                                                       ogrConstants.wkbLineString);

        try
        {
            for(final Edge edge : edges)
            {
                final Feature edgeFeature = new Feature(edgeLayer.GetLayerDefn());

                final Geometry line = new Geometry(ogrConstants.wkbLineString);

                final Node node0 = nodeMap.get(edge.getFrom());
                final Node node1 = nodeMap.get(edge.getTo());

                line.AddPoint(node0.getX(), node0.getY());
                line.AddPoint(node1.getX(), node1.getY());

                if(edgeFeature.SetGeometry(line) != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }

                if(edgeLayer.CreateFeature(edgeFeature) != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }
            }
        }
        catch(final Throwable th)
        {
            edgeLayer.delete();
            throw th;
        }

        return edgeLayer;
    }
}
