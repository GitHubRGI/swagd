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

import javax.activation.MimeType;

import store.GeoPackageWriter;

import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.task.AbstractTask;
import com.rgi.common.task.MonitorableTask;
import com.rgi.common.task.Settings;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.task.TaskFactory;
import com.rgi.common.task.TaskMonitor;
import com.rgi.common.tile.store.tms.TmsReader;

public class Packager extends AbstractTask implements MonitorableTask {
	public Packager(final TaskFactory factory) {
		super(factory);
	}

	@Override
	public void execute(final Settings opts) {
		final File[] files = opts.getFiles(Setting.FileSelection);
		final File gpkgFile = new File("foo.gpkg");
		if (files.length == 1) {
			try
			{
				final SphericalMercatorCrsProfile smcp = new SphericalMercatorCrsProfile();
				final TmsReader reader = new TmsReader(smcp, files[0].toPath());

				try(final GeoPackageWriter gpkgWriter = new GeoPackageWriter(gpkgFile,
                                                                             smcp.getCoordinateReferenceSystem(),
                                                                             "footiles",
                                                                             "1",
                                                                             "test tiles",
                                                                             reader.getBounds(),
                                                                             reader.getTimeScheme(),
                                                                             new MimeType("image/png"),
                                                                             null))
				{
				    reader.stream().forEach(tileHandle -> {
                        try
                        {
                            final CrsCoordinate coordinate = smcp.tileToCrsCoordinate(tileHandle.getRow(),
                                                                                      tileHandle.getColumn(),
                                                                                      smcp.getBounds(),
                                                                                      tileHandle.getMatrix(),
                                                                                      reader.getTileOrigin());

                            gpkgWriter.addTile(coordinate, tileHandle.getZoomLevel(), tileHandle.getImage());

                        }
                        catch(final Exception e)
                        {
                            e.printStackTrace();
                        }
                    });
				    System.out.println("Done.");
				}
			} catch (final Exception e)
			{
				e.printStackTrace();
			}
		} else {
			// i dunno
		}
	}

	@Override
	public void addMonitor(final TaskMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestCancel() {
		// TODO Auto-generated method stub

	}
}
