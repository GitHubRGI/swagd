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
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * Class responsible for loading TMS tiles to a JMapViewer.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class TileStoreLoader implements TileLoader
{
    private final TileLoaderListener listener;
    //private TileSource                 tileSource;
    private final TileStoreReader    tileStore;
    private final TileProfile        tileProfile;

    private final int minimumZoomLevel;
    private final int maximumZoomLevel;

    private static final BufferedImage TransparentTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

    public TileStoreLoader(final TileStoreReader tileStore, final TileLoaderListener listener) throws TileStoreException
    {
        this.tileStore   = tileStore;
        this.tileProfile = TileProfileFactory.create(tileStore.getCoordinateReferenceSystem());
        this.listener    = listener;
        //this.tileSource  = new TileSourceShell(tileStore);

        this.minimumZoomLevel = tileStore.getZoomLevels().stream().min(Integer::compare).orElse(-1);
        this.maximumZoomLevel = tileStore.getZoomLevels().stream().max(Integer::compare).orElse(-1);
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
                    tile.loaded  = false;
                    tile.error   = false;
                    tile.loading = true;

                    // TODO do we need to move the .setImage call into the synchronized block?
                }

                try
                {
                    BufferedImage image = null;

                    if(tile.getZoom() <= TileStoreLoader.this.maximumZoomLevel &&
                       tile.getZoom() >= TileStoreLoader.this.minimumZoomLevel)
                    {
                        final CrsCoordinate crsCoordinate = TileStoreLoader.this.toCrsCoordinate(tile);

                        image = TileStoreLoader.this.tileStore.getTile(crsCoordinate, tile.getZoom());
                    }

                    if(image != null)
                    {
                        tile.setImage(image);
                    }
                    else
                    {
                        tile.setError("No tile available at this location.");
                        tile.setImage(TransparentTile);
                    }

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

    private CrsCoordinate toCrsCoordinate(final Tile tile)
    {
        return this.tileProfile
                   .absoluteToCrsCoordinate(new AbsoluteTileCoordinate(tile.getYtile(),
                                                                       tile.getXtile(),
                                                                       tile.getZoom(),
                                                                       origin));
    }

    public final static TileOrigin origin = TileOrigin.UpperLeft; // Tile origin for JMapViewer

}
