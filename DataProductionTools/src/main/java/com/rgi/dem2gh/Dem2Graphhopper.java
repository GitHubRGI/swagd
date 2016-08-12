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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.util.CmdArgs;
import com.rgi.routingnetworks.DemRoutingNetworkStoreReader;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.osm.OsmXmlRoutingNetworkStoreWriter;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;

/**
 * @author Luke Lambert
 */
public final class Dem2Graphhopper
{
    private Dem2Graphhopper()
    {
    }

    public static void main(final String[] args)
    {
        final CommandLineOptions options = new CommandLineOptions();
        final CmdLineParser      parser  = new CmdLineParser(options);

        try
        {
            parser.parseArgument(args);
        }
        catch(final Throwable th)
        {
            System.err.println(th.getMessage());
            parser.printUsage(System.out);
        }

        final String inputFilename = options.getInputFile().getName();

        final String baseOutputFileName = inputFilename.substring(0, inputFilename.indexOf('.'));

        try
        {
            final RoutingNetworkStoreReader networkReader = new DemRoutingNetworkStoreReader(options.getInputFile(),
                                                                                             options.getRasterBand(),
                                                                                             options.getContourElevationInterval(),
                                                                                             options.getNoDataValue(),
                                                                                             options.getCoordinatePrecision(),
                                                                                             options.getSimplificationTolerance(),
                                                                                             options.getTriangulationTolerance());

            final File osmXmloutputFile = new File(baseOutputFileName + ".osm.xml");
            final RoutingNetworkStoreWriter networkWriter = new OsmXmlRoutingNetworkStoreWriter(osmXmloutputFile,
                                                                                                networkReader.getBounds(),
                                                                                                networkReader.getDescription());

            networkWriter.write(networkReader.getNodes(),
                                networkReader.getEdges(),
                                networkReader.getNodeDimensionality(),
                                networkReader.getNodeAttributeDescriptions(),
                                networkReader.getEdgeAttributeDescriptions(),
                                networkReader.getCoordinateReferenceSystem());

            final String graphHopperOutputDirectoryName = baseOutputFileName + "-gh";
            final String[] inputs = { "graph.flag_encoders=foot",
                                      "graph.elevation.dataaccess=RAM_STORE",
                                      "prepare.ch.weightings=no",
                                      "graph.dataaccess=RAM_STORE",
                                      "graph.location=" + graphHopperOutputDirectoryName, // where to store the results
                                      "osmreader.osm=" + osmXmloutputFile     // input osm
                                    };

            final GraphHopper graphHopper = new GraphHopper().init(CmdArgs.read(inputs));

            try
            {
                final ElevationProvider tagElevationProvider = new TagElevationProvider();

                tagElevationProvider.setBaseURL(osmXmloutputFile.getPath());

                graphHopper.setElevation(true);
                graphHopper.setElevationProvider(tagElevationProvider);

                graphHopper.importOrLoad(); // Creates binary output
            }
            finally
            {
                graphHopper.close();
            }
        }
        catch(final Throwable th)
        {
            System.err.println(th.getMessage());
        }
    }
}
