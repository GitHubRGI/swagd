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

package com.rgi.view;

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

import com.rgi.common.tile.profile.SphericalMercatorTileProfile;
import com.rgi.common.tile.store.TileStore;
import com.rgi.common.tile.store.TmsTileStore;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.GeoPackageTileStore;
import com.rgi.geopackage.tiles.TileSet;

public class MapViewWindow extends JFrame implements JMapViewerEventListener
{

    private static final long serialVersionUID = 1337L;
    private JMapViewerTree    treeMap;
    private TileLoader        loader;
    private final File        location;

    public MapViewWindow(final File location)
    {
        super("Tile Viewer");
        this.location = location;
        this.initialize();
    }

    public MapViewWindow(final String title, final File location)
    {
        super(title);
        this.location = location;
        this.initialize();
    }

    @Override
    public void processCommand(final JMVCommandEvent command)
    {
        // TODO:
        // This fires whenever the map is moved or zoomed in. Use this to call
        // methods that update relevant zoom-dependent information like pixel
        // resolution or current zoom level.
    }

    private void initialize()
    {
        this.treeMap = new JMapViewerTree("Visualized tile set");
        this.map().addJMVListener(this);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        final JPanel panel = new JPanel();
        this.add(panel, BorderLayout.NORTH);

        TileStore tileStore = null;
        if(this.location.isDirectory())
        {
            // TMS or WMTS based directory
            // create a TMS tile store
            tileStore = new TmsTileStore(new SphericalMercatorTileProfile(), this.location.toPath());
        }
        else if(this.location.getName().toLowerCase().endsWith(".gpkg")) // File
                                                                         // based
                                                                         // tile
                                                                         // store
                                                                         // like
                                                                         // GPKG
        {
            try
            {
                final GeoPackage gpkg = new GeoPackage(this.location, OpenMode.Open);
                final Collection<TileSet> tileSets = gpkg.tiles().getTileSets();
                if(tileSets.size() > 0)
                {
                    final TileSet set = tileSets.iterator().next();
                    // final SpatialReferenceSystem srs =
                    // set.getSpatialReferenceSystem();
                    tileStore = new GeoPackageTileStore(gpkg, set);
                }
            }
            catch(final Exception e)
            {
                //
                e.printStackTrace();
            }
        }
        else
        {
            throw new NullPointerException("Tile store unable to be generated.");
        }

        this.loader = new TileStoreLoader(tileStore, this.map());
        this.map().setTileLoader(this.loader);

        try
        {
            // TODO
            // final Coordinate<Double> topLeft =
            // tileStore.calculateBounds().getMin();

            // Coordinate 4326geographicCoordinate = transform topLeft FROM
            // (tileStore.getSrsAuthority, tileStore.getSrsAuthority) TO
            // ("EPSG", 4326) TODO is jmapviewer really in 4326?

            // this.map()
            // .setDisplayPosition(new Coordinate(geoCoord.getLatitude(),
            // geoCoord.getLongitude()),
            // Collections.min(tileStore.getZoomLevels()));
            System.err.println("The tile store data isn't going to be zoomed to -> the coordinate conversion stuff is still being worked out");
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
        this.add(this.treeMap, BorderLayout.CENTER);
    }

    private JMapViewer map()
    {
        return this.treeMap.getViewer();
    }
}
