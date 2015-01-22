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

import com.rgi.common.coordinates.CrsCoordinate;

/**
 * @author Luke Lambert
 *
 */
public interface TileStoreWriter
{
    /**
     * Insert a tile into this tile store via at a row and column that corresponds to a geographic coordinate
     *
     * @param coordinate
     *            The geographic coordinate that corresponds to the tile.
     * @param zoomLevel
     *            The zoom level of the tile.
     * @param image
     *            The BufferedImage containing the tile data.
     * @throws TileStoreException
     *             A TileStoreException is thrown when an error occurs while
     *             inserting this tile into the tile store.
     */
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException;

    /**
     * Insert a tile into this tile store via at a row and column that corresponds to a geographic coordinate
     *
     * @param row
     *             The 'y' portion of the coordinate. This value is relative to this tile store's tile scheme.
     * @param column
     *             The 'x' portion of the coordinate. This value is relative to this tile store's tile scheme.
     * @param zoomLevel
     *            The zoom level of the tile.
     * @param image
     *            The BufferedImage containing the tile data.
     * @throws TileStoreException
     *             A TileStoreException is thrown when an error occurs while
     *             inserting this tile into the tile store.
     */
    public void addTile(final int row, final int column, final int zoomLevel, final BufferedImage image) throws TileStoreException;
}
