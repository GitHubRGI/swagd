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

package com.rgi.erdc.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.TileStoreLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

import com.rgi.erdc.gpkg.GeoPackage;
import com.rgi.erdc.gpkg.GeoPackage.OpenMode;
import com.rgi.erdc.gpkg.GeoPackageTileStore;
import com.rgi.erdc.gpkg.core.SpatialReferenceSystem;
import com.rgi.erdc.gpkg.tiles.TileSet;
import com.rgi.erdc.tile.profile.SphericalMercatorTileProfile;
import com.rgi.erdc.tile.store.TileStore;
import com.rgi.erdc.tile.store.TmsTileStore;

public class MapViewWindow extends JFrame implements JMapViewerEventListener {

	private static final long serialVersionUID = 1337L;
	private JMapViewerTree treeMap;
	private TileLoader loader;
	private File location;

	public MapViewWindow(File location) {
		super("ERDC-GRL Tile Viewer");
		this.location = location;
		this.initialize();
	}

	public MapViewWindow(String title, File location) {
		super(title);
		this.location = location;
		this.initialize();
	}

	@Override
	public void processCommand(JMVCommandEvent command) {
		// TODO:
		// This fires whenever the map is moved or zoomed in. Use this to call
		// methods that update relevant zoom-dependent information like pixel
		// resolution or current zoom level.
	}

	private void initialize() {
		this.treeMap = new JMapViewerTree("Visualized tile set");
		this.map().addJMVListener(this);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		JPanel panel = new JPanel();
		this.add(panel, BorderLayout.NORTH);

		TileStore tileStore = null;
		if (this.location.isDirectory()) {
			// TMS or WMTS based directory
			// create a TMS tile store
			tileStore = new TmsTileStore(new SphericalMercatorTileProfile(), this.location.toPath());
		} else {
			// File based tile store like GPKG
			if (this.location.getName().toLowerCase().endsWith(".gpkg")) {
				try {
					GeoPackage gpkg = new GeoPackage(this.location, OpenMode.Open);
					Collection<TileSet> tileSets = gpkg.tiles().getTileSets();
					if (tileSets.size() > 0) {
						TileSet set = tileSets.iterator().next();
						SpatialReferenceSystem srs = set.getSpatialReferenceSystem();
						tileStore = new GeoPackageTileStore(gpkg, set);
					}
				} catch (Exception e) {
					//
					e.printStackTrace();
				}
			}
		}

		if (tileStore == null) {
			throw new NullPointerException("Tile store unable to be generated.");
		}

		this.loader = new TileStoreLoader(tileStore, this.map());
		this.map().setTileLoader(this.loader);

        try
        {
            // TODO
            //BoundingBox bounds = tileStore.calculateBounds();

            //com.rgi.erdc.coordinates.Coordinate<Double> topLeft = new com.rgi.erdc.coordinates.Coordinate<>(bounds.getMaxY(), bounds.getMinX());

            // Coordinate 4326geographicCoordinate = transform topLeft FROM (tileStore.getSrsAuthority, tileStore.getSrsAuthority) TO ("EPSG", 4326) TODO is jmapviewer really in 4326?

            //this.map()
            //    .setDisplayPosition(new Coordinate(geoCoord.getLatitude(),
            //                                       geoCoord.getLongitude()),
            //                        Collections.min(tileStore.getZoomLevels()));
            System.err.println("The tile store data isn't going to be zoomed to -> the coordinate conversion stuff is still being worked out");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
		this.add(this.treeMap, BorderLayout.CENTER);
	}

	private JMapViewer map() {
		return this.treeMap.getViewer();
	}
}
