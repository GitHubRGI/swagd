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

package com.rgi.g2t;

import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.rgi.common.Dimensions;
import com.rgi.common.task.MonitorableTask;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Luke Lambert
 *
 */
public class Tiler implements MonitorableTask, TaskMonitor
{
    ExecutorService executor  = Executors.newSingleThreadExecutor();
    private int     jobTotal  = 0;
    private int     jobCount  = 0;
    private int     completed = 0;

    final private File                file;
    final private TileStoreWriter     tileWriter;
    //final private TileStoreReader     tileReader;
    final private Dimensions<Integer> tileDimensions;
    final private Color               noDataColor;

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

    private final Set<TaskMonitor> monitors = new HashSet<>();

    @Override
    public void addMonitor(final TaskMonitor monitor)
    {
        this.monitors.add(monitor);
    }

    @Override
    public void requestCancel()
    {
        this.executor.shutdownNow();
        try
        {
            this.executor.awaitTermination(60, TimeUnit.SECONDS);
        }
        catch(final InterruptedException ie)
        {
            this.fireCancelled();
        }
    }

    public void execute()
    {
        final Thread jobWaiter = new Thread(new JobWaiter(this.executor.submit(new TileJob(this.file,
                                                                                           //this.tileReader,
                                                                                           this.tileWriter,
                                                                                           this.tileDimensions,
                                                                                           this.noDataColor,
                                                                                           this))));

        jobWaiter.setDaemon(true);
        jobWaiter.start();
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

    private void fireProgressUpdate()
    {
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.setProgress(this.completed);
        }
    }

    private void fireCancelled()
    {
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.cancelled();
        }
    }

    private void fireError(final Exception e)
    {
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.setError(e);
        }
    }

    private void fireFinished()
    {
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.finished();
        }
    }

    private class JobWaiter implements Runnable
    {
        private final Future<?> job;

        public JobWaiter(final Future<?> job)
        {
            ++Tiler.this.jobTotal;
            this.job = job;
        }

        @Override
        public void run()
        {
            try
            {
                this.job.get();
            }
            catch(final InterruptedException ie)
            {
                // unlikely, but we still need to handle it
                System.err.println("Tiling job was interrupted.");
                ie.printStackTrace();
                Tiler.this.fireError(ie);
            }
            catch(final ExecutionException ee)
            {
                System.err.println("Tiling job failed with exception: " + ee.getMessage());
                ee.printStackTrace();
                Tiler.this.fireError(ee);
            }
            catch(final CancellationException ce)
            {
                System.err.println("Tiling job was cancelled.");
                ce.printStackTrace();
                Tiler.this.fireError(ce);
            }
        }
    }

    @Override
    public void setMaximum(final int max)
    {
        // updates the progress bar to exit indeterminate mode
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.setMaximum(100);
        }
    }

    @Override
    public void setProgress(final int value)
    {

        System.out.println("progress updated: " + value);
        // when called by a tilejob, reports a number from 0-100.
        final double perJob = 100.0 / this.jobTotal;
        this.completed = (int)((this.jobCount * perJob) + ((value / 100.0) * perJob));
        this.fireProgressUpdate();
    }

    @Override
    public void cancelled()
    {
        // not used
    }

    @Override
    public void finished()
    {
        ++this.jobCount;
        if(this.jobCount == this.jobTotal)
        {
            this.fireFinished();
        }
        else
        {
            this.setProgress(0);
        }
    }

    @Override
    public void setError(final Exception e)
    {
        // this shouldn't be used
    }
}
