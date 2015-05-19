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

package com.rgi.store.tiles;

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
public interface TileStoreWriter extends AutoCloseable
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
