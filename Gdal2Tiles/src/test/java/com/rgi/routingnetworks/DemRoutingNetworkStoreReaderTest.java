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
        final File inputFile  = new File("C:/Users/corp/Desktop/mwtc_srtm_90m.tif");
        final File outputFile = new File(inputFile.getName().substring(0, inputFile.getName().indexOf('.')) + ".osm.xml");

        final RoutingNetworkStoreReader networkReader = new DemRoutingNetworkStoreReader(inputFile,
                                                                                         1,
                                                                                         1.0,
                                                                                         null,
                                                                                         7,
                                                                                         20.0,
                                                                                         0.00001);  // I /think/ this is about a meter's worth of distance at the equator

        final RoutingNetworkStoreWriter networkWriter = new OsmXmlRoutingNetworkStoreWriter(outputFile,
                                                                                            networkReader.getBounds(),
                                                                                            networkReader.getDescription(),
                                                                                            networkReader.getCoordinateReferenceSystem());

        networkWriter.write(networkReader.getNodes(),
                            networkReader.getEdges(),
                            networkReader.getNodeAttributeDescriptions(),
                            networkReader.getEdgeAttributeDescriptions());


    }
}
