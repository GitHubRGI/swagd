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

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.coordinate.referencesystem.profile.GlobalGeodeticCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
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
    @SuppressWarnings("unused")
    private final CrsProfile crsProfile;
    private final GlobalGeodeticCrsProfile globalGeodeticCrs = new GlobalGeodeticCrsProfile();
    private final CoordinateReferenceSystem globalGeodetic   = this.globalGeodeticCrs.getCoordinateReferenceSystem();
    private final static TileOrigin Origin     = TileOrigin.UpperLeft;           // Tile Origin for JMapViewer
    private final static TileScheme TileScheme = new ZoomTimesTwo(0, 31, 1, 1);  // Tile scheme for JMapViewer: http://wiki.openstreetmap.org/wiki/Slippy_Map
    /**
     * @param tileStore The tile store that will be viewed.
     * @throws TileStoreException Thrown when the tile store is not supported or is invalid.
     */
    public TileStoreTileSource(final TileStoreReader tileStore) throws TileStoreException
    {
        this.tileStore = tileStore;

        this.minimumZoomLevel = tileStore.getZoomLevels().stream().min(Integer::compare).orElse(-1);
        this.maximumZoomLevel = tileStore.getZoomLevels().stream().max(Integer::compare).orElse(-1);
        
        this.crsProfile = CrsProfileFactory.create(this.tileStore.getCoordinateReferenceSystem());
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
        double distance = Math.sqrt((lo1-lo2)*(lo1-lo2) + (la1-la2)*(la1-la2));
        return distance;
    }
    //this is pixel
    @Override
    public int LonToX(double arg0, int arg1)
    {
        // TODO Auto-generated method stub
        return 0;
    }
//this is pixel
    @Override
    public int LatToY(double aLat, final int aZoomlevel)
    {
        //TODO
        return 0;
    }
//this is pixel
    @Override
    public double XToLon(final int aX, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    
//this is pixel
    @Override
    public double YToLat(int aY, final int aZoomlevel)
    {
        // TODO Auto-generated method stub
        return 0;
    }
   

    @Override
    public double lonToTileX(final double lon, final int zoom)
    {
        CrsCoordinate crsCoordinate = new CrsCoordinate(lon, 0, this.globalGeodetic);
        int tileX = this.globalGeodeticCrs.crsToTileCoordinate(crsCoordinate, this.globalGeodeticCrs.getBounds(), TileStoreTileSource.TileScheme.dimensions(zoom) , TileStoreTileSource.Origin).getX();
        return tileX;
    }

    @Override
    public double latToTileY(final double lat, final int zoom)
    {
        CrsCoordinate crsCoordinate = new CrsCoordinate(lat, 0, this.globalGeodetic);
        int tileY = this.globalGeodeticCrs.crsToTileCoordinate(crsCoordinate, this.globalGeodeticCrs.getBounds(), TileStoreTileSource.TileScheme.dimensions(zoom) , TileStoreTileSource.Origin).getY();
        return tileY;
    }

    @Override
    public double tileXToLon(final int x, final int zoom)
    {
        
        double longitude = this.globalGeodeticCrs.tileToCrsCoordinate(x, 0, this.globalGeodeticCrs.getBounds(), TileStoreTileSource.TileScheme.dimensions(zoom) , TileStoreTileSource.Origin).getX();
        
        return longitude;
    }

    @Override
    public double tileYToLat(final int y, final int zoom)
    {
        // TODO Auto-generated method stub
        double latitude = this.globalGeodeticCrs.tileToCrsCoordinate(0, y, this.globalGeodeticCrs.getBounds(), TileStoreTileSource.TileScheme.dimensions(zoom) , TileStoreTileSource.Origin).getY();
        return latitude;
    }

    @Override
    public String getId()
    {
        return this.tileStore.getName();
    }
    
}
