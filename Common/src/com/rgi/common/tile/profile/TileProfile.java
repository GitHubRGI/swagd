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
import com.rgi.common.coordinates.CrsTileCoordinate;
import com.rgi.common.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 */
public abstract class TileProfile
{
    /**
     * Constructor
     *
     * @param accuracyDegree Acceptable degree of accuracy (decimal places) for the coordinate values of this profile
     */
    protected TileProfile(final int accuracyDegree)
    {
        if(accuracyDegree < 0)
        {
            throw new IllegalArgumentException("Accuracy degree must be greater than 0");
        }

        this.accuracyDegree = accuracyDegree;
    }

    /**
     * Returns the bounds of the world in CRS units.
     * @return the bounds of the world in crs units
     */
    public abstract BoundingBox getBounds();
    
    /**
     * Determines what tile the coordinate lies in
     *
     * @param coordinate Coordinate in the same unit as this tile profile
     * @param zoomLevel Zoom level value for input coordinate
     * @return Returns the tile that the coordinate coresponds to
     */
    public abstract AbsoluteTileCoordinate crsToAbsoluteTileCoordinate(final Coordinate<Double> coordinate, final int zoomLevel, final TileOrigin origin);

    /**
     * Determines the profile unit coordinate for the specified tile
     *
     * @param absoluteTileCoordinate
     *             Tile coordinate (tile y, x, zoom level)
     * @return Returns the coordinate that the tile coresponds to
     */
    public abstract CrsTileCoordinate absoluteToCrsCoordinate(final AbsoluteTileCoordinate absoluteTileCoordinate);

    public abstract Dimension2D getTileDimensions(final int zoomLevel);

    public abstract CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * @param ordinate Scalar double
     * @return Returns a String representation of the input ordinate truncated to the accuracy degree (number of decimals) for this profile
     */
    public String truncate(final double ordinate)
    {
        final double n = Math.pow(10, this.accuracyDegree);
        return String.format("%." + this.accuracyDegree + "d",
                             ((int)(ordinate*n))/n);
    }

    /**
     * Acceptable degree of accuracy (decimal places) for the coordinate values of this profile
     */
    private final int accuracyDegree;
}
