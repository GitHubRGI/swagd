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

package com.rgi.g2t;

import java.awt.Color;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rgi.common.Dimensions;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Duff Means
 * @author Luke Lambert
 *
 */
public class Tiler
{
    ExecutorService executor  = Executors.newSingleThreadExecutor();
    private final int     jobTotal  = 0;
    private final int     jobCount  = 0;
    private final int     completed = 0;

    final private File                file;
    final private TileStoreWriter     tileWriter;
    //final private TileStoreReader     tileReader;
    final private Dimensions<Integer> tileDimensions;
    final private Color               noDataColor;

    /**
     * Constructor
     *
     * @param file
     *             Source image
     * @param tileWriter
     *             Destination tile store
     * @param tileDimensions
     *             Desired tile pixel width and height
     * @param noDataColor
     *             Default tile color
     */
    public Tiler(final File                file,
                 final TileStoreWriter     tileWriter,
                 final Dimensions<Integer> tileDimensions,
                 final Color               noDataColor)
    {
        this.file           = file;
        //this.tileReader     = tileReader;
        this.tileWriter     = tileWriter;
        this.tileDimensions = tileDimensions;
        this.noDataColor    = noDataColor;

        sanityCheckGdalInstallation();
    }

//    private final Set<TaskMonitor> monitors = new HashSet<>();
//
//    @Override
//    public void addMonitor(final TaskMonitor monitor)
//    {
//        this.monitors.add(monitor);
//    }
//
//    @Override
//    public void requestCancel()
//    {
//        this.executor.shutdownNow();
//        try
//        {
//            this.executor.awaitTermination(60, TimeUnit.SECONDS);
//        }
//        catch(final InterruptedException ie)
//        {
//            this.fireCancelled();
//        }
//    }

    /**
     * Creates the tiles
     */
    public void execute()
    {
        //final Thread jobWaiter = new Thread(new JobWaiter(this.executor.submit(new TileJob(this.file,
        //                                                                                   //this.tileReader,
        //                                                                                   this.tileWriter,
        //                                                                                   this.tileDimensions,
        //                                                                                   this.noDataColor,
        //                                                                                   this))));
        //
        //jobWaiter.setDaemon(true);
        //jobWaiter.start();

        // TODO this is *temporarily* synchronous
        final TileJob tileJob = new TileJob(this.file,
                                            //this.tileReader,
                                            this.tileWriter,
                                            this.tileDimensions,
                                            this.noDataColor/*,
                                            this*/);
        tileJob.run();
    }

    private static void sanityCheckGdalInstallation()
    {
        // GDAL_DATA needs to be a valid path
        if(System.getenv("GDAL_DATA") == null)
        {
            throw new RuntimeException("Tiling will not work without GDAL_DATA environment variable.");
        }
        // Get the system path
        //String paths = System.getenv("PATH");
        // Parse the path entries
        // Check each path entry for the required dll's/so's
        // Throw an error if any of the required ones are missing
    }

//    private void fireProgressUpdate()
//    {
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.setProgress(this.completed);
//        }
//    }
//
//    private void fireCancelled()
//    {
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.cancelled();
//        }
//    }
//
//    @SuppressWarnings("unused")
//    private void fireError(final Exception e)
//    {
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.setError(e);
//        }
//    }
//
//    private void fireFinished()
//    {
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.finished();
//        }
//    }

//    private class JobWaiter implements Runnable
//    {
//        private final Future<?> job;
//
//        public JobWaiter(final Future<?> job)
//        {
//            ++Tiler.this.jobTotal;
//            this.job = job;
//        }
//
//        @Override
//        public void run()
//        {
//            try
//            {
//                this.job.get();
//            }
//            catch(final InterruptedException ie)
//            {
//                // unlikely, but we still need to handle it
//                System.err.println("Tiling job was interrupted.");
//                ie.printStackTrace();
//                Tiler.this.fireError(ie);
//            }
//            catch(final ExecutionException ee)
//            {
//                System.err.println("Tiling job failed with exception: " + ee.getMessage());
//                ee.printStackTrace();
//                Tiler.this.fireError(ee);
//            }
//            catch(final CancellationException ce)
//            {
//                System.err.println("Tiling job was cancelled.");
//                ce.printStackTrace();
//                Tiler.this.fireError(ce);
//            }
//        }
//    }

//    @Override
//    public void setMaximum(final int max)
//    {
//        // updates the progress bar to exit indeterminate mode
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.setMaximum(100);
//        }
//    }
//
//    @Override
//    public void setProgress(final int value)
//    {
//
//        System.out.println("progress updated: " + value);
//        // when called by a tilejob, reports a number from 0-100.
//        final double perJob = 100.0 / this.jobTotal;
//        this.completed = (int)((this.jobCount * perJob) + ((value / 100.0) * perJob));
//        this.fireProgressUpdate();
//    }
//
//    @Override
//    public void cancelled()
//    {
//        // not used
//    }
//
//    @Override
//    public void finished()
//    {
//        ++this.jobCount;
//        if(this.jobCount == this.jobTotal)
//        {
//            this.fireFinished();
//        }
//        else
//        {
//            this.setProgress(0);
//        }
//    }
//
//    @Override
//    public void setError(final Exception e)
//    {
//        // this shouldn't be used
//    }
}
