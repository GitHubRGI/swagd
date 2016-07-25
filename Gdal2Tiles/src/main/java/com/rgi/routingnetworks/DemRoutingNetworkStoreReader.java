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

package com.rgi.routingnetworks;

import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import org.gdal.gdal.Dataset;
import utility.GdalUtility;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class DemRoutingNetworkStoreReader implements RoutingNetworkStoreReader
{
    public DemRoutingNetworkStoreReader(final File file)
    {
        final Dataset dataset = GdalUtility.open(file);

        // We cannot tile an image with no geo referencing information
        if(!GdalUtility.hasGeoReference(dataset))
        {
            throw new IllegalArgumentException("Input raster image has no georeference.");
        }

        this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystem(GdalUtility.getSpatialReference(dataset));

        if(this.coordinateReferenceSystem  == null)
        {
            throw new IllegalArgumentException("Image file is not in a recognized coordinate reference system");
        }

        

    }

    @Override
    public List<Pair<String, Type>> getNodeAttributes()
    {
        return null;
    }

    @Override
    public List<Pair<String, Type>> getEdgeAttributes()
    {
        return null;
    }

    @Override
    public List<Node> getNodes()
    {
        return null;
    }

    @Override
    public List<Edge> getEdges()
    {
        return null;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    private final CoordinateReferenceSystem coordinateReferenceSystem;
}
