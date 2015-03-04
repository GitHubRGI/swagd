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

import javax.activation.MimeType;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;

/**
 * Interface for writing tiles to a store
 *
 * @author Luke Lambert
 *
 */
public interface TileStoreWriter
{
    /**
     * Converts a geographic coordinate, in units of the tile store's
     * coordinate reference system, to a tile coordinate relative to this tile
     * store's tile scheme.
     *
     * @param coordinate
     *             The geographic coordinate that corresponds to the tile
     * @param zoomLevel
     *             The zoom level of the tile
     * @return Returns a tile {@link Coordinate} that is relative to this tile
     *             store's tile scheme
     * @throws TileStoreException
     *             Wraps errors thrown by the tile store writer implementation
     */
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException;


    /**
     * Converts a tile coordinate to a geographic coordinate, in the coordinate
     * reference system of this tile store.  The <code>corner</code> parameter
     * controls which corner point will represent the tile.
     *
     * @param column
     *             The 'x' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param row
     *             The 'y' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param zoomLevel
     *            The zoom level of the tile
     * @param corner
     *             Selects the corner of the tile to represent as the CRS
     *             coordinate
     * @return A {@link CrsCoordinate} that represents one of the corners of
     *             the specified tile
     * @throws TileStoreException
     *             Wraps errors thrown by the tile store writer implementation
     */
    public CrsCoordinate tileToCrsCoordinate(final int column, final int row, final int zoomLevel, final TileOrigin corner) throws TileStoreException;

    /**
     * Gets the geographic bounds of a tile, in units of the tile store's
     * coordinate reference system.
     *
     * @param column
     *             The 'x' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param row
     *             The 'y' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param zoomLevel
     *            The zoom level of the tile
     * @return A {@link BoundingBox} that represents the geographic bounds of a
     *            tile
     * @throws TileStoreException
     *             Wraps errors thrown by the tile store writer implementation
     */
    public BoundingBox getTileBoundingBox(final int column, final int row, final int zoomLevel) throws TileStoreException;

    /**
     * Insert a tile into this tile store via at a row and column that
     * corresponds to a geographic coordinate
     *
     * @param coordinate
     *             The geographic coordinate that corresponds to the tile
     * @param zoomLevel
     *             The zoom level of the tile
     * @param image
     *             The {@link BufferedImage} containing the tile data
     * @throws TileStoreException
     *             Wraps errors thrown by the tile store writer implementation
     */
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException;

    /**
     * Insert a tile into this tile store via at a column and row that corresponds to a geographic coordinate
     *
     * @param column
     *             The 'x' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param row
     *             The 'y' portion of the coordinate. This value is relative to
     *             this tile store's tile scheme
     * @param zoomLevel
     *            The zoom level of the tile
     * @param image
     *             The {@link BufferedImage} containing the tile data
     * @throws TileStoreException
     *             Wraps errors thrown by the tile store writer implementation
     */
    public void addTile(final int column, final int row, final int zoomLevel, final BufferedImage image) throws TileStoreException;


    /**
     * Reports the image formats that are valid for this type of tile store writer
     *
     * @return A set of {@link MimeType}s that this type of tile store writer supports
     */
    public Set<MimeType> getSupportedImageFormats();

    /**
     * @return returns the tile store's coordinate reference system
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * @return Tile numbering scheme used by this tile store
     */
    public TileScheme getTileScheme();

    /**
     * @return Returns the tile origin
     */
    public TileOrigin getTileOrigin();
}
