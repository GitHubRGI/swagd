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

import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.osm.OsmXmlRoutingNetworkStoreWriter;
import org.junit.Test;

import java.io.File;

/**
 * @author Luke.Lambert
 */
public class DemRoutingNetworkStoreReaderTest
{
    @Test
    public void testConstructor() throws RoutingNetworkStoreException
    {
        final RoutingNetworkStoreReader networkReader = new DemRoutingNetworkStoreReader(new File("C:/Users/corp/Desktop/dataFromMatt/wgs84Dems/dem_40cm_a2_westpoint_maincampus_tile2.tif"),
                                                                                         1,
                                                                                         5.0,
                                                                                         null,
                                                                                         20.0,
                                                                                         0.0);

        final RoutingNetworkStoreWriter networkWriter = new OsmXmlRoutingNetworkStoreWriter(new File("westpoint_maincampus_tile2.osm.xml"),
                                                                                            networkReader.getBounds(),
                                                                                            networkReader.getDescription(),
                                                                                            networkReader.getCoordinateReferenceSystem());

        networkWriter.write(networkReader.getNodes(),
                            networkReader.getEdges(),
                            networkReader.getNodeAttributeDescriptions(),
                            networkReader.getEdgeAttributeDescriptions());


    }
}
