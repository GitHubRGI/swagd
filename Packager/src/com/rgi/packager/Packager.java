/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
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
