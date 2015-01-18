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

package com.rgi.common.tile.store;

import java.awt.image.BufferedImage;
import java.util.Set;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileException;

/**
 * An representation of a container of tiles.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public interface TileStore
{
    /**
     * Calculate the bounds of this tile store.
     *
     * @return a simple bounding box in the default unit of measure of the tile
     *         profile of this tile store
     * @throws A
     *             TileStoreException in the event of a specific error.
     */
    public BoundingBox calculateBounds() throws TileStoreException;

    /**
     * Count the number of tiles in this tile store.
     *
     * @return The number of tiles contained within this tile store.
     * @throws TileStoreException
     *             When an error occurs calculating the bounds of this tile
     *             store, a TileStoreException is thrown.
     */
    public long countTiles() throws TileStoreException;

    /**
     * Calculate the byte size of this tile store.
     *
     * @return The approximate size of this tile store in bytes.
     */
    public long calculateSize() throws TileStoreException;

    /**
     * Get a tile at a specified zoom, row (x), and column (y).
     *
     * @param coordinate
     *            Location that corresponds to the requested tile
     * @param zoomLevel
     *            The zoom level of the tile.
     * @return A buffered image, or null if the tile store has no tile data for the specified coordinate
     * @throws TileStoreException
     *             A TileStoreException occurs if either the tile does not exist
     *             or another error occurs during tile retrieval.
     */
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileException, TileStoreException;

    /**
     * Insert a tile into this tile store via a BufferedImage and tile
     * coordinates.
     *
     * @param image
     *            The BufferedImage stream containing the tile data.
     * @param zoomLevel
     *            The zoom level of the tile.
     * @param coordinate
     *            The row and column cartesian representation of the tile.
     * @throws TileStoreException
     *             A TileStoreException is thrown when an error occurs while
     *             inserting this tile into the tile store.
     */
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileException, TileStoreException;

    /**
     * Ask the tile store for all the zoom levels that it contains.
     *
     * @return A list of integers that represent zoom levels this tile store
     *         contains.
     * @throws TileStoreException
     *             A TileStoreException is thrown when the list of zoom levels
     *             cannot be built.
     */
    public Set<Integer> getZoomLevels() throws TileStoreException;

    /**
     * @return returns the tile store's coordinate reference system
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

}
