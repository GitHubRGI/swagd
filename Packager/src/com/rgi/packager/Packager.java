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
package com.rgi.packager;

import com.rgi.common.TaskMonitor;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * Package tiles from a tile store into a GeoPackage or append to an existing GeoPackage.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class Packager
{
    private final TaskMonitor     taskMonitor;
    private final TileStoreReader tileStoreReader;
    private final TileStoreWriter tileStoreWriter;

    /**
     * Constructor
     *
     * @param tileStoreReader
     *             Input tile store
     * @param tileStoreWriter
     *             Destination tile store
     */
    public Packager(final TaskMonitor     taskMonitor,
                    final TileStoreReader tileStoreReader,
                    final TileStoreWriter tileStoreWriter)
    {
        this.taskMonitor     = taskMonitor;

        this.tileStoreReader = tileStoreReader;
        this.tileStoreWriter = tileStoreWriter;
    }

    /**
     * Starts the packaging job
     */
    public boolean execute()
    {
        int tileCount = 0;

        try
        {
            this.taskMonitor.setMaximum((int)this.tileStoreReader.countTiles());

            for(final TileHandle tileHandle : (Iterable<TileHandle>)this.tileStoreReader.stream()::iterator)
            {
                try
                {
                    this.tileStoreWriter.addTile(tileHandle.getCrsCoordinate(this.tileStoreWriter.getTileOrigin()),
                                                 tileHandle.getZoomLevel(),
                                                 tileHandle.getImage());

                    this.taskMonitor.setProgress(++tileCount);
                }
                catch(final TileStoreException | IllegalArgumentException ex)
                {
                    // TODO: report this somewhere else?
                    System.err.printf("Tile z: %d, x: %d, y: %d failed to get copied into the package: %s\n",
                                      tileHandle.getZoomLevel(),
                                      tileHandle.getColumn(),
                                      tileHandle.getRow(),
                                      ex.getMessage());
                }
            }
        }
        catch(final TileStoreException ex)
        {
            this.taskMonitor.setError(ex);
            return false;
        }

        this.taskMonitor.finished();
        return true;
    }


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
//
//    private void fireCancelled()
//    {
//        for(final TaskMonitor monitor : this.monitors)
//        {
//            monitor.cancelled();
//        }
//    }
//
//    private class JobWaiter implements Runnable
//    {
//        private final Future<?> job;
//
//        public JobWaiter(final Future<?> job)
//        {
//            ++Packager.this.jobTotal;
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
//                System.err.println("Packaging job was interrupted.");
//                ie.printStackTrace();
//                Packager.this.fireError(ie);
//            }
//            catch(final ExecutionException ee)
//            {
//                System.err.println("Packaging job failed with exception: " + ee.getMessage());
//                ee.printStackTrace();
//                Packager.this.fireError(ee);
//            }
//            catch(final CancellationException ce)
//            {
//                System.err.println("Packaging job was cancelled.");
//                ce.printStackTrace();
//                Packager.this.fireError(ce);
//            }
//        }
//    }
}
