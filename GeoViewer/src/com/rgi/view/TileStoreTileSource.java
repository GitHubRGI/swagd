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

import java.awt.Image;
import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * Connect jmapviewer code with SWAGD code for viewing tile stores in a map viewer.
 *
 * @author Steven D. Lander
 */
public class TileStoreTileSource implements TileSource
{
    private final TileStoreReader tileStore;

    private final int minimumZoomLevel;
    private final int maximumZoomLevel;

    /**
     * @param tileStore The tile store that will be viewed.
     * @throws TileStoreException Thrown when the tile store is not supported or is invalid.
     */
    public TileStoreTileSource(final TileStoreReader tileStore) throws TileStoreException
    {
        this.tileStore = tileStore;

        this.minimumZoomLevel = tileStore.getZoomLevels().stream().min(Integer::compare).orElse(-1);
        this.maximumZoomLevel = tileStore.getZoomLevels().stream().max(Integer::compare).orElse(-1);
    }

    @Override
    public boolean requiresAttribution()
    {
        return false;
    }

    @Override
    public String getAttributionText(final int zoom, final Coordinate topLeft, final Coordinate botRight)
    {
        return null;
    }

    @Override
    public String getAttributionLinkURL()
    {
        return null;
    }

    @Override
    public Image getAttributionImage()
    {
        return null;
    }

    @Override
    public String getAttributionImageURL()
    {
        return null;
    }

    @Override
    public String getTermsOfUseText()
    {
        return null;
    }

    @Override
    public String getTermsOfUseURL()
    {
        return null;
    }

    @Override
    public int getMaxZoom()
    {
        return this.maximumZoomLevel;
    }

    @Override
    public int getMinZoom()
    {
        return this.minimumZoomLevel;
    }

    @Override
    public TileUpdate getTileUpdate()
    {
        return TileUpdate.None; // The "server" does not support any of the other update mechanisms
    }

    @Override
    public String getName()
    {
        return this.tileStore.getName();
    }

    @Override
    public String getTileUrl(final int zoom, final int tilex, final int tiley) throws IOException
    {
        return null;
    }

    @Override
    public String getTileType()
    {
        try
        {
            return this.tileStore.getImageType();
        }
        catch(final TileStoreException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public int getTileSize()
    {
        try
        {
            return this.tileStore.getImageDimensions().getWidth();
        }
        catch(final TileStoreException ex)
        {
            ex.printStackTrace();
            return 0;
        }
    }

    @Override
    public double getDistance(final double la1, final double lo1, final double la2, final double lo2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int LonToX(final double aLongitude, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int LatToY(final double aLat, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double XToLon(final int aX, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double YToLat(final int aY, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double lonToTileX(final double lon, final int zoom)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double latToTileY(final double lat, final int zoom)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double tileXToLon(final int x, final int zoom)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double tileYToLat(final int y, final int zoom)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getId()
    {
        return this.tileStore.getName();
    }
}
