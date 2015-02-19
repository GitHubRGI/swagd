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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import store.GeoPackageWriter;

import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.Utility;
import com.rgi.common.task.AbstractTask;
import com.rgi.common.task.MonitorableTask;
import com.rgi.common.task.Settings;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.task.TaskFactory;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.tile.store.tms.TmsReader;
import com.rgi.common.util.Range;
import com.rgi.geopackage.verification.ConformanceException;

/**
 * Package tiles from a tile store into a GeoPackage or append to an existing GeoPackage.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class Packager extends AbstractTask implements MonitorableTask, TaskMonitor
{
    private final ExecutorService executor  = Executors.newSingleThreadExecutor();
    private int             jobTotal  = 0;
    private int             jobCount  = 0;
    private int             completed = 0;
    private AutoCloseable   resource;

	/**
	 * @param factory
	 */
    public Packager(final TaskFactory factory)
    {
        super(factory);
    }

    private final Set<TaskMonitor> monitors = new HashSet<>();


	@Override
	public void execute(final Settings opts)
	{
		// TODO: Create new geopackage or append to existing one
		// Get file/directory from settings
		final File[] files = opts.getFiles(Setting.FileSelection);
		// Create a new geopackage file
		final File gpkgFile = new File(opts.get(Setting.OutputFileName));

		if(gpkgFile.exists())
		{
		    if(!gpkgFile.delete())
		    {
		        this.fireError(new Exception("Unable to overwrite existing geopackage file: " + gpkgFile.getAbsolutePath()));
		    }
		}

		if(files.length == 1)
		{
			try
			{
				CrsProfile crsProfile = new SphericalMercatorCrsProfile(); // TODO: this should be the default.
				switch (opts.get(Setting.CrsProfile))
				{
					case "SphericalMercator":
					case "WebMercator":
						crsProfile = new SphericalMercatorCrsProfile();
						break;
					case "WorldMercator":
						crsProfile = new EllipsoidalMercatorCrsProfile();
						break;
					case "ScaledWorldMercator":
					case "Geodetic":
					case "Raster":
					default:
						// TODO: need to clean up this mess
						this.fireError(new Exception("Unsupported output profile or no profile supplied."));
				}
                // Figure out what the file selection is and create a reader
				final TmsReader tileStoreReader = new TmsReader(crsProfile, files[0].toPath());

				final Set<Integer> zoomLevels = tileStoreReader.getZoomLevels();

				if(zoomLevels.size() == 0)
				{
				    System.err.println("Input tile store contains no zoom levels");
				    return;
				}

				final Range<Integer> zoomLevelRange = new Range<>(zoomLevels, Integer::compare);

				final List<TileHandle> tiles = tileStoreReader.stream(zoomLevelRange.getMinimum()).collect(Collectors.toList());

				final Range<Integer> columnRange = new Range<>(tiles, tile -> tile.getColumn(), Integer::compare);
				final Range<Integer>    rowRange = new Range<>(tiles, tile -> tile.getRow(),    Integer::compare);

				final int minZoomLevelMatrixHeight =    rowRange.getMaximum() -    rowRange.getMinimum() + 1;
				final int minZoomLevelMatrixWidth  = columnRange.getMaximum() - columnRange.getMinimum() + 1;

                // Create a new geopackage writer with things like table name and description
				final GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkgFile,
                                                                         crsProfile.getCoordinateReferenceSystem(),
                                                                         //"footiles",
                                                                         opts.get(Setting.TileSetName),
                                                                         //"1",
                                                                         opts.get(Setting.TileSetName),
                                                                         //"test tiles",
                                                                         opts.get(Setting.TileSetDescription),
                                                                         tileStoreReader.getBounds(),
                                                                         new ZoomTimesTwo(zoomLevelRange.getMinimum(),
                                                                                          zoomLevelRange.getMaximum(),
                                                                                          minZoomLevelMatrixHeight,
                                                                                          minZoomLevelMatrixWidth),
                                                                         new MimeType("image/png"),
                                                                         null);

				this.resource = gpkgWriter;

                // Create a new PackageJob task
				final Thread jobWaiter = new Thread(new JobWaiter(this.executor.submit(this.createPackageJob(tileStoreReader, gpkgWriter))));
				jobWaiter.setDaemon(true);
				jobWaiter.start();
			}
            catch(ClassNotFoundException | IOException | SQLException | ConformanceException | TileStoreException | MimeTypeParseException ex)
			{
				this.fireError(ex);
			}
		} else
		{
			this.fireError(new Exception("More than one file was specified for packaging"));
		}
	}

	private Runnable createPackageJob(final TileStoreReader tileStoreReader,
			                          final TileStoreWriter tileStoreWriter)
	{
		return () -> { int tileCount = 0;

		               try
                       {
                           for(final TileHandle tileHandle : (Iterable<TileHandle>)tileStoreReader.stream()::iterator)
                           {
                               try
                               {
                                   if(!tileStoreReader.getBounds().contains(tileHandle.getCrsCoordinate()))
                                   {
                                       System.out.println("wtf");
                                   }

                                   final CrsCoordinate crsCoordinate = new CrsCoordinate(Utility.boundsCorner(tileHandle.getBounds(), tileStoreWriter.getTileOrigin()), tileStoreReader.getCoordinateReferenceSystem());

                                   tileStoreWriter.addTile(crsCoordinate,
                                                           tileHandle.getZoomLevel(),
                                                           tileHandle.getImage());
                                   ++tileCount;
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
                           // TODO Auto-generated catch block
                           ex.printStackTrace();
                       }

		               try
                       {
                           System.out.printf("Packaged %d of %d tiles.\n", tileCount, tileStoreReader.countTiles());
                       }
                       catch(final TileStoreException ex)
                       {
                           System.out.printf("Copied %d tiles.\n", tileCount);
                       }
		               finally
		               {
		                   this.fireFinished();
		               }
		             };
	}

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
            this.close();
        }
        catch(final InterruptedException ie)
        {
            this.fireCancelled();
        }
	}

	private void close()
	{
	    if(this.resource != null)
	    {
	        try
            {
                this.resource.close();
                this.resource = null;
            }
            catch(final Exception ex)
            {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
	    }
	}

	private void fireCancelled()
	{
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.cancelled();
        }

        this.close();
	}

    private void fireProgressUpdate()
    {
        for(final TaskMonitor monitor : this.monitors)
        {
            monitor.setProgress(this.completed);
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

        this.close();
    }

	private class JobWaiter implements Runnable
	{
		private final Future<?> job;

        public JobWaiter(final Future<?> job)
        {
            ++Packager.this.jobTotal;
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
                System.err.println("Packaging job was interrupted.");
                ie.printStackTrace();
                Packager.this.fireError(ie);
            }
            catch(final ExecutionException ee)
            {
                System.err.println("Packaging job failed with exception: " + ee.getMessage());
                ee.printStackTrace();
                Packager.this.fireError(ee);
            }
            catch(final CancellationException ce)
            {
                System.err.println("Packaging job was cancelled.");
                ce.printStackTrace();
                Packager.this.fireError(ce);
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

        this.close();
    }

    @Override
    public void setError(final Exception e)
    {
        // this shouldn't be used
    }
}
