/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.openstreetmap.gui.jmapviewer;

import java.awt.image.BufferedImage;

import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
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
    /**
     * Constructor
     *
     * @param tileStore
     *             Tile store reader
     * @param listener
     *             Callback mechanism to report tile loader status
     * @throws TileStoreException
     *             Thrown when there's an error in the underlying tile store implementation
     */
    public TileStoreLoader(final TileStoreReader tileStore, final TileLoaderListener listener) throws TileStoreException
    {
        this.tileStore  = tileStore;
        this.crsProfile = CrsProfileFactory.create(tileStore.getCoordinateReferenceSystem());
        this.listener   = listener;

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
//
//                System.out.printf("%s %d,%d,%d\n",
//                                  TileStoreLoader.this.tileStore.getName(),
//                                  tile.getZoom(),
//                                  tile.getXtile(),
//                                  tile.getYtile());

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
        com.rgi.common.coordinate.Coordinate<Integer> transformedCoordinate = Origin.transform(this.tileStore.getTileOrigin(), tile.getXtile(), tile.getYtile(), TileScheme.dimensions(tile.getZoom()));
        return this.crsProfile
                   .tileToCrsCoordinate(transformedCoordinate.getX(),
                                        transformedCoordinate.getY(),
                                        this.crsProfile.getBounds(),
                                        TileScheme.dimensions(tile.getZoom()),
                                        this.tileStore.getTileOrigin());
    }

    private final static TileOrigin Origin     = TileOrigin.UpperLeft;           // Tile Origin for JMapViewer
    private final static TileScheme TileScheme = new ZoomTimesTwo(0, 31, 1, 1);  // Tile scheme for JMapViewer: http://wiki.openstreetmap.org/wiki/Slippy_Map

    private final TileLoaderListener listener;
    private final TileStoreReader    tileStore;
    private final CrsProfile         crsProfile;

    private final int minimumZoomLevel;
    private final int maximumZoomLevel;

    private static final BufferedImage TransparentTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
}
