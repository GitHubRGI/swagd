package com.rgi.packager;

import store.GeoPackageWriter;

import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Steven D. Lander
 *
 */
public class PackageJob implements Runnable {
	
	private final TileStoreReader tileStoreReader;
	private final CrsProfile crsProfile;
	private final GeoPackageWriter gpkgWriter;
	private final TaskMonitor monitor;
	
	/**
	 * @param tileStoreReader
	 * @param crsProfile 
	 * @param gpkgWriter
	 * @param monitor 
	 */
	public PackageJob(TileStoreReader tileStoreReader,
					  CrsProfile crsProfile,
					  GeoPackageWriter gpkgWriter,
					  TaskMonitor monitor)
	{
		this.tileStoreReader = tileStoreReader;
		this.crsProfile = crsProfile;
		this.gpkgWriter = gpkgWriter;
		this.monitor = monitor;
	}

	@Override
	public void run() {
		this.tileStoreReader.stream().forEach(tileHandle -> { try
			{
                gpkgWriter.addTile(tileHandle.getCrsCoordinate(), tileHandle.getZoomLevel(), tileHandle.getImage());
			}
			catch(Exception e)
			{
				// TODO: handle this better
				e.printStackTrace();
			}
		});
	}
}
