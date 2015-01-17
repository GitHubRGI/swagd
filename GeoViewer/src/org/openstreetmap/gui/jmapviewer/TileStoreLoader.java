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

package org.openstreetmap.gui.jmapviewer;

import java.awt.image.BufferedImage;

import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.profile.TileProfileFactory;
import com.rgi.common.tile.store.TileStore;

/**
 * Class responsible for loading TMS tiles to a JMapViewer.
 *
 * @author Steven D. Lander
 *
 */
public class TileStoreLoader implements TileLoader
{
    private final TileLoaderListener         listener;
    //private TileSource                 tileSource;
    private final TileStore                  tileStore;
    private final TileProfile                tileProfile;
    private static final BufferedImage TRANSPARENT_TILE = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

    public TileStoreLoader(final TileStore tileStore, final TileLoaderListener listener)
    {
        this.tileStore   = tileStore;
        this.tileProfile = TileProfileFactory.create(tileStore.getCoordinateReferenceSystem());
        this.listener    = listener;
        //this.tileSource  = new TileSourceShell(tileStore);
    }

    @Override
    public TileJob createTileLoaderJob(final Tile tile)
    {
        return new TileJob()
        {
            @Override
            public void run()
            {
                synchronized(tile)
                {
                    if((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                    {
                        return;
                    }
                    tile.loaded = false;
                    tile.error = false;
                    tile.loading = true;
                }

                try
                {
                    final int y = tile.getYtile();
                    final int x = tile.getXtile();
                    final int z = tile.getZoom();

                    final CrsCoordinate crsCoordinate = TileStoreLoader.this.tileProfile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(tile.getYtile(), tile.getXtile(), tile.getZoom(), TileOrigin.LowerLeft));

                    final BufferedImage image = TileStoreLoader.this.tileStore.getTile(crsCoordinate, tile.getZoom());

                    if(image != null)
                    {
                        tile.setImage(image);
                    }
                    else
                    {
                        tile.setError("No tile available at this location.");
                        tile.setImage(TRANSPARENT_TILE);
                    }

                    tile.setLoaded(true);
                    TileStoreLoader.this.listener.tileLoadingFinished(tile, true);
                }
                catch(final Exception exception)
                {
                    // Error encountered during tile retrieval
                    tile.setError(exception.getMessage());
                    TileStoreLoader.this.listener.tileLoadingFinished(tile, false);
                }
                finally
                {
                    tile.loading = false;
                    tile.setLoaded(true);
                }
            }

            @Override
            public Tile getTile()
            {
                return tile;
            }
        };
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName();
    }

}
