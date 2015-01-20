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

package com.rgi.common.tile.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 */
public interface TileProfile
{
    /**
     * Returns the bounds of the world in CRS units.
     * @return the bounds of the world in crs units
     */
    public BoundingBox getBounds();

    /**
     * Determines what tile the coordinate lies in
     *
     * @param coordinate Coordinate in the same unit as this tile profile
     * @param zoomLevel Zoom level value for input coordinate
     * @return Returns the tile that the coordinate corresponds to
     */
    public AbsoluteTileCoordinate crsToAbsoluteTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel, final TileOrigin origin);

    /**
     * Determines the profile unit coordinate for the specified tile
     *
     * @param absoluteTileCoordinate
     *             Tile coordinate (tile y, x, zoom level)
     * @return Returns the coordinate that the tile corresponds to
     */
    public CrsCoordinate absoluteToCrsCoordinate(final AbsoluteTileCoordinate absoluteTileCoordinate);

    /**
     * TODO
     *
     * @param zoomLevel
     * @return
     */
    public Dimension2D getTileDimensions(final int zoomLevel);

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
