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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.util.CmdArgs;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.dem2gh.ConsoleProgressCallback;
import com.rgi.dem2gh.TagElevationProvider;
import com.rgi.routingnetworks.image.ImageRoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.Utility;
import com.rgi.store.routingnetworks.osm.OsmXmlRoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.triangle.TriangleRoutingNetworkStoreReader;
import org.junit.Test;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Luke.Lambert
 */
@SuppressWarnings("JavaDoc")
public class TriangleRoutingNetworkStoreReaderTest
{
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testTriangleToOsmAndGraphhopper() throws RoutingNetworkStoreException, IOException
    {
        final String datasetName = "mwtc.1";

        final Path baseFolder = Paths.get("C:/Users/corp/Desktop/sample data/networks/triangle/");

        final Path datasetFolder = baseFolder.resolve(datasetName);

        final File nodesFile = datasetFolder.resolve(datasetName + ".node").toFile();
        final File edgesFile = datasetFolder.resolve(datasetName + ".edge").toFile();

        final String baseOutputFileName = "MWTC_renner";

        final RoutingNetworkStoreReader triangleNetworkStoreReader = new TriangleRoutingNetworkStoreReader(nodesFile,
                                                                                                           edgesFile,
                                                                                                           0,
                                                                                                           null,
                                                                                                           new CoordinateReferenceSystem("EPSG", 4326));

        final RoutingNetworkStoreReader networkStoreReader = addHighwayTags(triangleNetworkStoreReader);

        final File osmXmlOutputFile = writeOsmNetwork(baseOutputFileName, networkStoreReader);

        writeGraphHopperBinaryNetwork(baseOutputFileName, osmXmlOutputFile);

        final double scale = 1.0;

        final int imageWidth  = (int)(12762 * scale);
        final int imageHeight = (int)(15897 * scale);

        writeImageNetwork(baseOutputFileName,
                          triangleNetworkStoreReader,
                          imageWidth,
                          imageHeight);

    }

    private static RoutingNetworkStoreReader addHighwayTags(final RoutingNetworkStoreReader inputRoutingNetworkStoreReader) throws RoutingNetworkStoreException
    {
        final List<Pair<String, Type>> edgeAttributeDescriptions = new ArrayList(inputRoutingNetworkStoreReader.getEdgeAttributeDescriptions());
        edgeAttributeDescriptions.add(Pair.of("highway", String.class));

        return Utility.transform(node -> node,
                                 edge -> { final List<Object> attributes = new ArrayList(edge.getAttributes());
                                           attributes.add("footway");
                                           return new Edge(edge.getIdentifier(),
                                                           edge.getFrom(),
                                                           edge.getTo(),
                                                           edge.getEdgeDirectionality(),
                                                           attributes);
                                         },
                                 inputRoutingNetworkStoreReader.getNodeAttributeDescriptions(),
                                 edgeAttributeDescriptions,
                                 inputRoutingNetworkStoreReader.getNodes(),
                                 inputRoutingNetworkStoreReader.getEdges(),
                                 inputRoutingNetworkStoreReader.getCoordinateReferenceSystem(),
                                 inputRoutingNetworkStoreReader.getDescription(),
                                 inputRoutingNetworkStoreReader.getNodeDimensionality());
    }

    private static void writeImageNetwork(final String                    baseOutputFileName,
                                          final RoutingNetworkStoreReader networkReader,
                                          final int                       imageWidth,
                                          final int                       imageHeight) throws RoutingNetworkStoreException
    {
        final long startTime = System.currentTimeMillis();

        final File rasterizedNetworkFile = new File(baseOutputFileName + ".network.tif");

        //noinspection NumericCastThatLosesPrecision
        new ImageRoutingNetworkStoreWriter(rasterizedNetworkFile,
                                           imageWidth,
                                           imageHeight,
                                           new Color(255, 255, 255,   0),   // Transparent
                                           Color.BLACK,
                                           networkReader.getBounds(),
                                           new ConsoleProgressCallback()).write(networkReader.getNodes(),
                                                                                networkReader.getEdges(),
                                                                                networkReader.getNodeDimensionality(),
                                                                                networkReader.getNodeAttributeDescriptions(),
                                                                                networkReader.getEdgeAttributeDescriptions(),
                                                                                networkReader.getCoordinateReferenceSystem());
    }

    private static void writeGraphHopperBinaryNetwork(final String baseOutputFileName,
                                                      final File   osmXmlFile) throws IOException
    {
        final long startTime = System.currentTimeMillis();

        final String graphHopperOutputDirectoryName = baseOutputFileName + "-gh";

        final String[] inputs = { "graph.flag_encoders=foot",
                                  "graph.elevation.dataaccess=RAM_STORE",
                                  "prepare.ch.weightings=no",
                                  "graph.dataaccess=RAM_STORE",
                                  "graph.location=" + graphHopperOutputDirectoryName, // where to store the results
                                  "osmreader.osm=" + osmXmlFile                       // input osm
                                };

        final GraphHopper graphHopper = new GraphHopper().init(CmdArgs.read(inputs));

        try
        {
            final ElevationProvider tagElevationProvider = new TagElevationProvider();

            tagElevationProvider.setBaseURL(osmXmlFile.getPath());

            graphHopper.setElevation(true);
            graphHopper.setElevationProvider(tagElevationProvider);

            graphHopper.importOrLoad(); // Creates binary output

            final File graphHopperOutputDirectory = new File(graphHopperOutputDirectoryName);

            // Create Zip from binary folder output
            zipDirectory(graphHopperOutputDirectory, 9);

            // Delete the temporary folder
            if(graphHopperOutputDirectory.exists())
            {
                recursivelyDeleteDirectory(graphHopperOutputDirectory);
            }
        }
        finally
        {
            graphHopper.close();
        }
    }

    private static void zipDirectory(final File directory,
                                     final int  compressionLevel) throws IOException
    {
        final String graphHopperZipFilename = directory.getName() + ".zip";

        try(final FileOutputStream fileOutputStream = new FileOutputStream(graphHopperZipFilename))
        {
            try(final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream))
            {
                try(final ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream))
                {
                    zipOutputStream.setLevel(compressionLevel);

                    final String[] directoryList = directory.list();
                    if(directoryList != null)
                    {
                        @SuppressWarnings("CheckForOutOfMemoryOnLargeArrayAllocation")
                        final byte[] buffer = new byte[1024];

                        for(final String subFilename : directoryList)
                        {
                            final String entryFilename = directory.getName() + '/' + subFilename;

                            zipOutputStream.putNextEntry(new ZipEntry(entryFilename));

                            try(final FileInputStream fileInputStream = new FileInputStream(entryFilename))
                            {
                                int readLength;
                                //noinspection NestedAssignment
                                while((readLength = fileInputStream.read(buffer)) > 0)
                                {
                                    zipOutputStream.write(buffer, 0, readLength);
                                }
                            }

                            zipOutputStream.closeEntry();
                        }
                    }
                }
            }
        }
    }

    private static void recursivelyDeleteDirectory(final File graphHopperOutputDirectory) throws IOException
    {
        // Example courtesy of: https://stackoverflow.com/a/27917071/16434
        Files.walkFileTree(graphHopperOutputDirectory.toPath(),
                           new SimpleFileVisitor<Path>()
                           {
                               @Override
                               public FileVisitResult visitFile(final Path                file,
                                                                final BasicFileAttributes attrs) throws IOException
                               {
                                   Files.delete(file);
                                   return FileVisitResult.CONTINUE;
                               }

                               @Override
                               public FileVisitResult postVisitDirectory(final Path        dir,
                                                                         final IOException exc) throws IOException
                               {
                                   Files.delete(dir);
                                   return FileVisitResult.CONTINUE;
                               }
                           });
    }

    private static File writeOsmNetwork(final String                    baseOutputFileName,
                                        final RoutingNetworkStoreReader networkReader) throws RoutingNetworkStoreException
    {
        final File osmXmlOutputFile = new File(baseOutputFileName + ".osm.xml");

        final RoutingNetworkStoreWriter networkWriter = new OsmXmlRoutingNetworkStoreWriter(osmXmlOutputFile,
                                                                                            networkReader.getBounds(),
                                                                                            networkReader.getDescription());

        networkWriter.write(networkReader.getNodes(),
                            networkReader.getEdges(),
                            networkReader.getNodeDimensionality(),
                            networkReader.getNodeAttributeDescriptions(),
                            networkReader.getEdgeAttributeDescriptions(),
                            networkReader.getCoordinateReferenceSystem());

        return osmXmlOutputFile;
    }
}


