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
import com.graphhopper.util.CmdArgs;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Luke Lambert
 */
public class GraphHopperTest
{
    @Test
    public void testGraphHopperMounted() throws IOException
    {
//        //final String inputFilename = "C:/Users/corp/Desktop/sample data/networks/osm/kansascity.osm.pbf";
//        final String inputFilename = "C:/Users/corp/Desktop/sample data/networks/osm/ouagadougou_full.osm";
//
//        final String baseOutputFileName = inputFilename.substring(0, inputFilename.lastIndexOf('.'));
//
//        writeGraphHopperBinaryNetwork(baseOutputFileName,
//                                      new File(inputFilename));
    }

    private static void writeGraphHopperBinaryNetwork(final String baseOutputFileName,
                                                      final File   osmXmlFile) throws IOException
    {
        final long startTime = System.currentTimeMillis();

        final String graphHopperOutputDirectoryName = baseOutputFileName + "-gh";

        final String[] inputs = { "graph.flag_encoders=car",
                                  "prepare.ch.weightings=fastest,shortest",
                                  "routing.ch.disabling_allowed=true",
                                  "graph.dataaccess=RAM_STORE",
                                  "graph.location=" + graphHopperOutputDirectoryName, // where to store the results
                                  "osmreader.osm=" + osmXmlFile     // input osm
                                };

        final GraphHopper graphHopper = new GraphHopper().init(CmdArgs.read(inputs));

        try
        {
            graphHopper.importOrLoad(); // Creates binary output

            final File graphHopperOutputDirectory = new File(graphHopperOutputDirectoryName);

            try
            {
                // Create Zip from binary folder output
                zipDirectory(graphHopperOutputDirectory, 9);
            }
            finally
            {
                // Delete the temporary folder
                if(graphHopperOutputDirectory.exists())
                {
                    recursivelyDeleteDirectory(graphHopperOutputDirectory);
                }
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
                            zipOutputStream.putNextEntry(new ZipEntry(directory.getName() + '/' + subFilename));

                            try(final FileInputStream fileInputStream = new FileInputStream(directory.getAbsolutePath() + '/' + subFilename))
                            {
                                int readLength;
                                //noinspection NestedAssignment
                                while((readLength = fileInputStream.read(buffer)) > 0)
                                {
                                    zipOutputStream.write(buffer, 0, readLength);
                                }
                            }
                            finally
                            {
                                zipOutputStream.closeEntry();
                            }
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
}
