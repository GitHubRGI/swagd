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

import java.awt.Image;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

public class TileSourceShell implements TileSource {

    private final TileStoreReader tileStore;
    private Set<Integer> zooms;

    public TileSourceShell(final TileStoreReader tileStore) throws TileStoreException
    {
        this.tileStore = tileStore;
        this.zooms     = tileStore.getZoomLevels();
    }

    @Override
    public boolean requiresAttribution() {
        return false;
    }

    @Override
    public String getAttributionText(final int zoom, final Coordinate topLeft, final Coordinate botRight) {
        return null;
    }

    @Override
    public String getAttributionLinkURL() {
        return null;
    }

    @Override
    public Image getAttributionImage() {
        return null;
    }

    @Override
    public String getAttributionImageURL() {
        return null;
    }

    @Override
    public String getTermsOfUseText() {
        return null;
    }

    @Override
    public String getTermsOfUseURL() {
        return null;
    }

    @Override
    public int getMaxZoom() {
        return Collections.max(this.zooms);
    }

    @Override
    public int getMinZoom() {
        return Collections.min(this.zooms);
    }

    @Override
    public TileUpdate getTileUpdate() {
        return null;
    }

    @Override
    public String getName() {
        return this.tileStore.toString();
    }

    @Override
    public String getTileUrl(final int zoom, final int tilex, final int tiley) throws IOException {
        return null;
    }

    @Override
    public String getTileType() {
        return "png";
    }

    @Override
    public int getTileSize() {
        return 256;
    }

    @Override
    public double getDistance(final double la1, final double lo1, final double la2, final double lo2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int LonToX(final double aLongitude, final int aZoomlevel) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int LatToY(final double aLat, final int aZoomlevel) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double XToLon(final int aX, final int aZoomlevel) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double YToLat(final int aY, final int aZoomlevel) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double lonToTileX(final double lon, final int zoom) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double latToTileY(final double lat, final int zoom) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double tileXToLon(final int x, final int zoom) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double tileYToLat(final int y, final int zoom) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return this.tileStore.toString();
    }

}
