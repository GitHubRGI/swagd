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

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * A handle to access tile data.  Implementations are strongly encouraged to
 * lazy-load or calculate its properties, and then cache the result.
 *
 * @author Luke Lambert
 *
 */
public interface TileHandle
{
    /**
     * @return Returns the tile's zoom level
     */
    public int getZoomLevel();

    /**
     * @return Returns the column (x) of the tile. Column values are relative
     *             to the tile scheme to which this tile belongs
     */
    public int getColumn();

    /**
     * @return Returns the row (y) of the tile. Row values are relative
     *             to the tile scheme to which this tile belongs
     */
    public int getRow();

    /**
     * @return Returns the maximum number of columns and rows at this tile's
     *         zoom level
     * @throws TileStoreException
     *             Occurs when the requested zoom level doesn't exist for the
     *             tile handle's containing tile set
     */
    public TileMatrixDimensions getMatrix() throws TileStoreException;

    /**
     * @return Returns the real world coordinate of this tile's origin in the
     *         unit of its enclosing tile set
     * @throws TileStoreException
     *             Occurs when there's an error in converting a tile coordinate
     *             to one of the associated coordinate reference system
     */
    public CrsCoordinate getCrsCoordinate() throws TileStoreException;

    /**
     * @param corner
     *             Selects the corner of the tile to represent as the CRS coordinate
     * @return Returns the real world coordinate of this tile based on the
     *         corner parameter in the unit of its enclosing tile set
     * @throws TileStoreException
     *             Occurs when there's an error in converting a tile coordinate
     *             to one of the associated coordinate reference system
     */
    public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException;

    /**
     * @return Returns the bounding box of a tile in real world CRS units
     * @throws TileStoreException
     *             A TileStoreException is thrown when unable to get the tile's
     *             row, column, or matrix that is needed to calculate the
     *             Bounds
     */
    public BoundingBox getBounds() throws TileStoreException;

    /**
     * @return Returns the tile's image data
     * @throws TileStoreException
     *             A TileStoreException occurs if unable to retrieve the
     *             specified tile
     */
    public BufferedImage getImage() throws TileStoreException;
}
