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
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.routingnetworks.dem.DemRoutingNetworkStoreReader;
import com.rgi.routingnetworks.image.ImageRoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.osm.OsmXmlRoutingNetworkStoreWriter;
import org.kohsuke.args4j.CmdLineParser;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Luke Lambert
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "MagicNumber"})
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
            final long startTime = System.currentTimeMillis();

            final DemRoutingNetworkStoreReader demNetworkReader = createDemRoutingNetworkStoreReader(options);

            final File osmXmlOutputFile = writeOsmNetwork(baseOutputFileName, demNetworkReader);

            writeGraphHopperBinaryNetwork(baseOutputFileName, osmXmlOutputFile);

            if(options.getOutputRasterizedNetwork())
            {
                writeImageNetwork(options, baseOutputFileName, demNetworkReader);
            }

            System.out.format("Total process finished in %s seconds\n",
                              elapsedTime(System.currentTimeMillis() - startTime));

        }
        catch(final Throwable th)
        {
            System.err.println(th.getMessage());
        }
    }

    private static void writeImageNetwork(final CommandLineOptions           options,
                                          final String                       baseOutputFileName,
                                          final DemRoutingNetworkStoreReader demNetworkReader) throws RoutingNetworkStoreException
    {
        final long startTime = System.currentTimeMillis();

        final File rasterizedNetworkFile = new File(baseOutputFileName + ".network.tif");

        System.out.format("Writing rasterized network to %s...",
                          rasterizedNetworkFile.getName());

        //noinspection NumericCastThatLosesPrecision
        new ImageRoutingNetworkStoreWriter(rasterizedNetworkFile,
                                           (int)(demNetworkReader.getRasterWidth() * options.getOutputRasterScale()),
                                           (int)(demNetworkReader.getRasterHeight()* options.getOutputRasterScale()),
                                           new Color(255, 255, 255,   0),   // Transparent
                                           Color.BLACK,
                                           demNetworkReader.getBounds(),
                                           new ConsoleProgressCallback()).write(demNetworkReader.getNodes(),
                                                                                demNetworkReader.getEdges(),
                                                                                demNetworkReader.getNodeDimensionality(),
                                                                                demNetworkReader.getNodeAttributeDescriptions(),
                                                                                demNetworkReader.getEdgeAttributeDescriptions(),
                                                                                demNetworkReader.getCoordinateReferenceSystem());

        System.out.format(" ...finished! (%s)\n",
                          elapsedTime(System.currentTimeMillis() - startTime));
    }

    private static void writeGraphHopperBinaryNetwork(final String baseOutputFileName,
                                                      final File   osmXmlFile) throws IOException, InterruptedException
    {
        final long startTime = System.currentTimeMillis();

        final String graphHopperOutputDirectoryName = baseOutputFileName + "-gh";

        final String[] inputs = { "graph.flag_encoders=foot",
                                  "graph.elevation.dataaccess=RAM_STORE",
                                  "prepare.ch.weightings=no",
                                  "graph.dataaccess=RAM_STORE",
                                  "graph.location=" + graphHopperOutputDirectoryName, // where to store the results
                                  "osmreader.osm=" + osmXmlFile     // input osm
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

        System.out.format(" ...finished! (%s)\n",
                          elapsedTime(System.currentTimeMillis() - startTime));
    }

    private static void zipDirectory(final File directory,
                                     final int  compressionLevel) throws IOException
    {
        final String graphHopperZipFilename = directory.getName() + ".zip";

        try(final FileOutputStream fileOutputStream = new FileOutputStream(graphHopperZipFilename))
        {
            try(final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream))
            {
                try(final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream))
                {
                    zipOutputStream.setLevel(compressionLevel);

                    final byte[] buffer = new byte[1024];

                    final String[] directoryList = directory.list();
                    if(directoryList != null)
                    {
                        for(final String subFilename : directoryList)
                        {
                            final String entryFilename = directory.getName() + File.separator + subFilename;

                            zipOutputStream.putNextEntry(new ZipEntry(entryFilename));

                            try(final FileInputStream fileInputStream = new FileInputStream(entryFilename))
                            {
                                int readLength;
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

    private static File writeOsmNetwork(final String                       baseOutputFileName,
                                        final DemRoutingNetworkStoreReader demNetworkReader) throws RoutingNetworkStoreException
    {
        final long startTime = System.currentTimeMillis();

        final File osmXmlOutputFile = new File(baseOutputFileName + ".osm.xml");

        System.out.format("Writing OSM XML network to %s...",
                          osmXmlOutputFile.getName());

        final RoutingNetworkStoreWriter networkWriter = new OsmXmlRoutingNetworkStoreWriter(osmXmlOutputFile,
                                                                                            demNetworkReader.getBounds(),
                                                                                            demNetworkReader.getDescription());

        networkWriter.write(demNetworkReader.getNodes(),
                            demNetworkReader.getEdges(),
                            demNetworkReader.getNodeDimensionality(),
                            demNetworkReader.getNodeAttributeDescriptions(),
                            demNetworkReader.getEdgeAttributeDescriptions(),
                            demNetworkReader.getCoordinateReferenceSystem());

        System.out.format(" ...finished! (%s)\n",
                          elapsedTime(System.currentTimeMillis() - startTime));

        return osmXmlOutputFile;
    }

    private static DemRoutingNetworkStoreReader createDemRoutingNetworkStoreReader(final CommandLineOptions options) throws RoutingNetworkStoreException
    {
        final long startTime = System.currentTimeMillis();
        System.out.format("Reading the elevation data, and creating the network from %s... \n",
                          options.getInputFile().getName());

        final DemRoutingNetworkStoreReader demNetworkReader = new DemRoutingNetworkStoreReader(options.getInputFile(),
                                                                                               options.getRasterBand(),
                                                                                               options.getContourElevationInterval(),
                                                                                               options.getNoDataValue(),
                                                                                               options.getCoordinatePrecision(),
                                                                                               options.getSimplificationTolerance(),
                                                                                               options.getTriangulationTolerance(),
                                                                                               new CoordinateReferenceSystem("EPSG", 4326),
                                                                                               new ConsoleProgressCallback());

        System.out.format("\n...finished! (%s)\n",
                          elapsedTime(System.currentTimeMillis() - startTime));

        return demNetworkReader;
    }

    static String elapsedTime(final long milliseconds)
    {
        Duration duration = Duration.ofMillis(milliseconds);

        final StringBuilder timeString = new StringBuilder();

        if(duration.toDays() > 0)
        {
            timeString.append(duration.toDays()).append("d ");
        }

        duration = duration.minusDays(duration.toDays());

        if(duration.toHours() > 0)
        {
            timeString.append(duration.toHours()).append("h ");
        }

        duration = duration.minusHours(duration.toHours());

        if(duration.toMinutes() > 0)
        {
            timeString.append(duration.toMinutes()).append("m ");
        }

        duration = duration.minusMinutes(duration.toMinutes());

        final double seconds = duration.toNanos() / 1.0e9d;

        final DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        timeString.append(df.format(seconds))
                  .append('s');


        return timeString.toString();
    }
}
