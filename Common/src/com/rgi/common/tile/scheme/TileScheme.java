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

package com.rgi.common.tile.scheme;

import com.rgi.common.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 */
public abstract class TileScheme
{
    /**
     * TODO
     *
     * @param minimumZoomLevel
     * @param maximumZoomLevel
     * @param initialHeight
     * @param initialWidth
     * @param origin
     */
    public TileScheme(final int        minimumZoomLevel,
                      final int        maximumZoomLevel,
                      final int        initialHeight,
                      final int        initialWidth,
                      final TileOrigin origin)
    {
            if(minimumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Minimum zoom level must be at least 0");
        }

        if(maximumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Maximum zoom level must be at least 0");
        }

        if(initialHeight < 1)
        {
            throw new IllegalArgumentException("The initial height must be greater than 0");
        }

        if(initialWidth < 1)
        {
            throw new IllegalArgumentException("The initial width must be greater than 0");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Tile origin may not be null");
        }

        if(minimumZoomLevel >= maximumZoomLevel)
        {
            throw new IllegalArgumentException("Minimum zoom level must be less than or equal to the maximum");
        }

        if(Integer.MAX_VALUE < initialHeight * Math.pow(2.0, maximumZoomLevel))
        {
            throw new IllegalArgumentException("This combination of initial height and maximum zoom level will cause an integer overflow for tile numbering");
        }

        if(Integer.MAX_VALUE < initialWidth * Math.pow(2.0, maximumZoomLevel))
        {
            throw new IllegalArgumentException("This combination of initial width and maximum zoom level will cause an integer overflow for tile numbering");
        }

        this.origin           = origin;
        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;
    }
    /**
     * TODO
     *
     * @param zoomLevel
     * @return
     */
    public abstract TileMatrixDimensions dimensions(final int zoomLevel);

    /**
     * TODO
     *
     * @return
     */
    public abstract TileOrigin origin();

    protected final TileOrigin origin;
    protected final int        minimumZoomLevel;
    protected final int        maximumZoomLevel;
}
