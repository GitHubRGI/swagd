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

package com.rgi.common.coordinate.referencesystem.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * @author Luke Lambert
 *
 */
public interface CrsProfile
{
    /**
     * Returns the bounds of the world in the units of the coordinate reference system.
     * @return the bounds of the world in the units of the coordinate reference system
     */
    public BoundingBox getBounds();

    /**
     * Determines what tile the coordinate lies in
     *
     * @param coordinate
     *             Coordinate in the same unit as this tile profile
     * @param dimensions
     *             Height and width of the tile matrix
     * @return Returns the tile that the coordinate corresponds to
     */
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate        coordinate,
                                                   final TileMatrixDimensions dimensions,
                                                   final TileOrigin           tileOrigin);

    /**
     * Determines the profile unit coordinate for the specified tile
     *
     * @param row
     *             Vertical portion of the tile's coordinate
     * @param column
     *             Horizontal portion of the tile's coordinate
     * @param dimensions
     *             Height and width of the tile matrix
     * @param tileOrigin
     *             Specifies where tile (0, 0) is in the tile matrix
     * @return Returns the coordinate that the tile corresponds to
     */
    public CrsCoordinate tileToCrsCoordinate(final int                  row,
                                             final int                  column,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin);

    /**
     * Calculates the dimensions of a tile in CRS units
     *
     * @param dimensions
     *             Height and width of the tile matrix
     * @return Returns the dimensions of a single tile in CRS units
     */
    public Dimensions getTileDimensions(final TileMatrixDimensions dimensions);

    /**
     * @return Returns the coordinate reference system implemented by this CrsProfile
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Transform a coordinate from the CRS of the CrsProfile implementation to Global Geodetic (EPSG 4326)
     *
     * This is *temporary* because we don't have a good coordinate transformation mechanism
     *
     * @param coordinate
     * @return
     */
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate);
}