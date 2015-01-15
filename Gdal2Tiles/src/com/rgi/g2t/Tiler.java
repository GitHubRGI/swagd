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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.rgi.common.task.AbstractTask;
import com.rgi.common.task.MonitorableTask;
import com.rgi.common.task.Settings;
import com.rgi.common.task.TaskFactory;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.task.Settings.Profile;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.tile.store.TileStore;
import com.rgi.common.tile.store.TileStoreException;

public class Tiler extends AbstractTask implements MonitorableTask, TaskMonitor {
  ExecutorService executor = Executors.newSingleThreadExecutor();
  private int jobTotal = 0;
  private int jobCount = 0;
  private int completed = 0;

  public Tiler(TaskFactory factory) {
    super(factory);
  }

  private Set<TaskMonitor> monitors = new HashSet<TaskMonitor>();

  @Override
  public void addMonitor(TaskMonitor monitor) {
    monitors.add(monitor);
  }

  @Override
  public void requestCancel() {
    executor.shutdownNow();
    try {
      executor.awaitTermination(60, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      fireCancelled();
    }
  }

  private void fireProgressUpdate() {
    for (TaskMonitor monitor : monitors) {
      monitor.setProgress(completed);
    }
  }

  private void fireCancelled() {
    for (TaskMonitor monitor : monitors) {
      monitor.cancelled();
    }
  }

  private void fireError(Exception e) {
    for (TaskMonitor monitor : monitors) {
      monitor.setError(e);
    }
  }

  private void fireFinished() {
    for (TaskMonitor monitor : monitors) {
      monitor.finished();
    }
  }

  @Override
  public void execute(Settings opts) {
    Profile profile = Settings.Profile.valueOf(opts.get(Setting.TileProfile));
    // split the job up into individual files, process those files one at a time
    File[] files = opts.getFiles(Setting.FileSelection);
    for (File file : files) {

      TileStore tileStore = null;
      try {
    	  String imageFormat = Settings.Type.valueOf(opts.get(Setting.TileType)).name();
    	  String outputFolder = opts.get(Setting.TileFolder);
        tileStore = new SimpleFileStore(file.getName(), profile.getID(), outputFolder, imageFormat);
      } catch (TileStoreException tse) {
        System.err.println("Unable to create tile store for input file "+file.getName());
        continue;
      }

      Thread jobWaiter = new Thread(new JobWaiter(this.executor.submit(createTileJob(file, tileStore, opts, this))));
      jobWaiter.setDaemon(true);
      jobWaiter.start();
    }
  }

  private Runnable createTileJob(File file, TileStore tileStore, Settings opts, TaskMonitor monitor) {
    return new TileJob(file, tileStore, opts, monitor);
    //    return new FakeTileJob(file, opts, monitor);
  }

  private class JobWaiter implements Runnable {
    private Future<?> job;

    public JobWaiter(Future<?> job) {
      ++jobTotal;
      this.job = job;
    }

    @Override
    public void run() {
      try {
        this.job.get();
      } catch (InterruptedException ie) {
        // unlikely, but we still need to handle it
        System.err.println("Tiling job was interrupted.");
        ie.printStackTrace();
        fireError(ie);
      } catch (ExecutionException ee) {
        System.err.println("Tiling job failed with exception: "+ee.getMessage());
        ee.printStackTrace();
        fireError(ee);
      } catch (CancellationException ce) {
        System.err.println("Tiling job was cancelled.");
        ce.printStackTrace();
        fireError(ce);
      }
    }
  }

  @Override
  public void setMaximum(int max) {
    // updates the progress bar to exit indeterminate mode
    for (TaskMonitor monitor : monitors) {
      monitor.setMaximum(100);
    }
  }

  @Override
  public void setProgress(int value) {

    System.out.println("progress updated: "+value);
    // when called by a tilejob, reports a number from 0-100.
    double perJob = 100.0 / jobTotal;
    completed = (int)((jobCount * perJob) + ((value / 100.0) * perJob));
    fireProgressUpdate();
  }

  @Override
  public void cancelled() {
    // not used
  }

  @Override
  public void finished() {
    ++jobCount;
    if (jobCount == jobTotal) {
      fireFinished();
    } else {
      setProgress(0);
    }
  }

  @Override
  public void setError(Exception e) {
    // this shouldn't be used
  }
}
