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

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.store.TileStore;

/**
 * Class responsible for loading TMS tiles to a JMapViewer.
 * 
 * @author Steven D. Lander
 *
 */
public class TileStoreLoader implements TileLoader {

	protected TileLoaderListener listener;
	protected TileSource tileSource;
	protected TileStore tileStore;
	protected static final BufferedImage TRANSPARENT_TILE = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
	
	public TileStoreLoader(TileStore tileStore, TileLoaderListener listener) {
		this.tileStore = tileStore;
		this.listener = listener;
		this.tileSource = new TileSourceShell(tileStore);
	}

	@Override
	public TileJob createTileLoaderJob(Tile tile) {
		return new TileJob() {
			@Override
			public void run() {
				synchronized (tile) {
					if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
						return;
					tile.loaded = false;
					tile.error = false;
					tile.loading = true;
				}
				try {
					AbsoluteTileCoordinate tileCoord = new AbsoluteTileCoordinate(tile.getYtile(), tile.getXtile(), tile.getZoom(),
							TileOrigin.LowerLeft);
					com.rgi.common.tile.Tile commonTile = TileStoreLoader.this.tileStore.getTile(tileCoord);
					if (commonTile != null) {
						tile.setImage(commonTile.getImageContents());
					} else {
						tile.setError("No tile available at this location.");
						tile.setImage(TRANSPARENT_TILE);
					}
					tile.setLoaded(true);
					TileStoreLoader.this.listener.tileLoadingFinished(tile, true);
				} catch (Exception exception) {
					// Error encountered during tile retrieval
					tile.setError(exception.getMessage());
					TileStoreLoader.this.listener.tileLoadingFinished(tile, false);
				} finally {
					tile.loading = false;
					tile.setLoaded(true);
				}
			}

			@Override
			public Tile getTile() {
				return tile;
			}
		};
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
