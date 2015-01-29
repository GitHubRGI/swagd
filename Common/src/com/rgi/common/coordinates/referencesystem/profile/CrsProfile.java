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

package com.rgi.common.coordinates.referencesystem.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.coordinates.CrsCoordinate;
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
     * @param coordinate Coordinate in the same unit as this tile profile
     * @param dimensions
     * @return Returns the tile that the coordinate corresponds to
     */
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate        coordinate,
                                                   final TileMatrixDimensions dimensions,
                                                   final TileOrigin           tileOrigin);

    /**
     * Determines the profile unit coordinate for the specified tile
     *
     * @param row
     * @param column
     * @param dimensions
     * @param tileOrigin
     * @return Returns the coordinate that the tile corresponds to
     */
    public CrsCoordinate tileToCrsCoordinate(final int                  row,
                                             final int                  column,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin);

    /**
     * TODO
     *
     * @param zoomLevel
     * @return
     */
    public Dimensions getTileDimensions(final int zoomLevel);

    /**
     * @return TODO
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * TODO
     *
     * This is *temporary* because we don't have a good coordinate transformation mechanism
     *
     * @param coordinate
     * @return
     */
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate);
}
