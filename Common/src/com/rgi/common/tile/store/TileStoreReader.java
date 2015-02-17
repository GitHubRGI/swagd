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
import java.util.stream.Stream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.scheme.TileScheme;

/**
 * @author Luke Lambert
 *
 */
public interface TileStoreReader
{
    /**
     * Calculate the bounds of this tile store.
     *
     * @return a simple bounding box in the default unit of measure of the tile
     *         profile of this tile store
     * @throws TileStoreException
     *             TileStoreException in the event of a specific error.
     */
    public BoundingBox getBounds() throws TileStoreException;

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
     * Return the byte size of this tile store.
     *
     * @return The approximate size of this tile store in bytes.
     * @throws TileStoreException
     */
    public long getByteSize() throws TileStoreException;

    /**
     * Get a tile at a specified zoom, row (y), and column (x).
     *
     * @param row
     *             The 'y' portion of the coordinate. This value is relative to this tile store's tile scheme.
     * @param column
     *             The 'x' portion of the coordinate. This value is relative to this tile store's tile scheme.
     * @param zoomLevel
     *            The zoom level of the tile.
     * @return A buffered image, or null if the tile store has no tile data for the specified coordinate
     * @throws TileStoreException
     *             A TileStoreException occurs if an error occurs during tile retrieval.
     */
    public BufferedImage getTile(final int row, final int column, final int zoomLevel) throws TileStoreException;

    /**
     * Get a tile at a specified zoom, and geographic coordinate.
     *
     * @param coordinate
     *            Geographic coordinate that corresponds to the requested tile
     * @param zoomLevel
     *            The zoom level of the tile.
     * @return A buffered image, or null if the tile store has no tile data for the specified coordinate
     * @throws TileStoreException
     *             A TileStoreException occurs if an error occurs during tile retrieval.
     */
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException;

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
     * Gets a stream of every tile in the tile store.  Tile stores need not
     * contain the maximum number of tiles (rows * columns, per zoom level) so
     * missing entries will not be represented by this stream.
     *
     * @return Returns a {@link Stream} of {@link TileHandle}s
     */
    public Stream<TileHandle> stream();

    /**
     * @return returns the tile store's coordinate reference system
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * @return returns a human readable identifier for this tile store
     */
    public String getName();

    /**
     * @return Returns the best guess for the image type (MimeType subtype).
     * Tile stores need not necessarily contain a single image type, so the
     * store's implementation will return what it considers the most suitable.
     * This  function may return null if there are no tiles in the store, or if
     * there is an error.
     */
    public String getImageType();

    /**
     * @return Returns the best guess for the pixel dimensions of the tile
     * store's images. Tile stores may contain images of differing sizes, so
     * the store's implementation will return what it considers the most
     * suitable. This function may return null if there are no tiles in the
     * store, or if there is an error.
     */
    public Dimensions getImageDimensions();

    /**
     * @return the Tile Scheme which can calculate the number of tiles at a particular zoom level
     */
    public TileScheme getTileScheme();
}
