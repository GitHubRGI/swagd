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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.rgi.common.task.TaskMonitor;

public class FakeTileJob implements Runnable {
  private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  private TaskMonitor monitor;
  double tileTotal = 0;
  int tileCount = 0;

  List<Future<?>> tasks = new ArrayList<>();

  public FakeTileJob(/*File file, Settings settings, */TaskMonitor monitor) {
    this.monitor = monitor;
  }

  @Override
  public void run() {
    Random  r = new Random();
    int zoomLevels = r.nextInt(8) + 4;
    System.out.println("faking "+zoomLevels+" zoom levels");
    for (int i = 0; i < zoomLevels; ++i) {
      this.tasks.add(this.executor.submit(new FakeScaleWorker(i)));
    }

    while (!this.tasks.isEmpty()) {
      try {
        Future<?> task = this.tasks.remove(0);
        if (task != null) {
          task.get();
        }
        this.monitor.setProgress((int)(100 * (++this.tileCount) / this.tileTotal));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    this.monitor.finished();
  }

  private class FakeScaleWorker implements Runnable {
    private int zoomLevel;

    public FakeScaleWorker(int zoomLevel) {
      this.zoomLevel = zoomLevel;
      ++FakeTileJob.this.tileTotal;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ie) {
        // do nothing
      }
      FakeTileJob.this.monitor.setMaximum(100);
      int numTiles = (int)Math.pow(2, this.zoomLevel);
      for (int j = 0; j < numTiles; ++j) {
        FakeTileJob.this.tasks.add(FakeTileJob.this.executor.submit(new FakeTileWorker()));
      }
    }
  }

  private class FakeTileWorker implements Runnable {
    public FakeTileWorker() {
      ++FakeTileJob.this.tileTotal;
    }
    @Override
    public void run() {
      try {
        Thread.sleep(200);
      } catch (InterruptedException ie) {
        // do nothing;
      }
    }
  }
}
