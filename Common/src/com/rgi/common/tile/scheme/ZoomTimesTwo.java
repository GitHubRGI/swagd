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
public class ZoomTimesTwo extends TileScheme
{
    /**
     * Constructor
     *
     * @param minimumZoomLevel
     *             Lowest valid level of zoom for this tile scheme.  Must be 0 or greater, and less than or equal to maximumZoomLevel.
     * @param maximumZoomLevel
     *             Highest valid level of zoom for this tile scheme  Must be 0 or greater, and greater than or equal to minimumZoomLevel.
     * @param baseZoomLevelTileMatrixHeight
     *             The number of tiles along the y axis for the lowest zoom level
     * @param baseZoomLevelTileMatrixWidth
     *             The number of tiles along the x axis for the lowest zoom level
     * @param origin
     *             Specifies where tile (0, 0) is in the tile matrix
     */
    public ZoomTimesTwo(final int        minimumZoomLevel,
                        final int        maximumZoomLevel,
                        final int        baseZoomLevelTileMatrixHeight,
                        final int        baseZoomLevelTileMatrixWidth,
                        final TileOrigin origin)
    {
        super(minimumZoomLevel,
              maximumZoomLevel,
              baseZoomLevelTileMatrixHeight,
              baseZoomLevelTileMatrixWidth,
              origin);

        this.zoomLevelDimensions = new TileMatrixDimensions[maximumZoomLevel - minimumZoomLevel + 1];

        for(int zoomLevel = minimumZoomLevel; zoomLevel < maximumZoomLevel; ++zoomLevel)
        {
            final int twoToTheZoomPower = (int)Math.pow(2.0, zoomLevel);
            this.zoomLevelDimensions[zoomLevel - minimumZoomLevel] = new TileMatrixDimensions(baseZoomLevelTileMatrixHeight * twoToTheZoomPower,
                                                                                              baseZoomLevelTileMatrixWidth  * twoToTheZoomPower);
        }
    }

    @Override
    public TileMatrixDimensions dimensions(final int zoomLevel)
    {
        if(zoomLevel < this.minimumZoomLevel || zoomLevel > this.maximumZoomLevel)
        {
            throw new IllegalArgumentException(String.format("Zoom level must be in the range [%d, %d]",
                                               this.minimumZoomLevel,
                                               this.maximumZoomLevel));
        }

        return this.zoomLevelDimensions[zoomLevel - this.minimumZoomLevel];
    }

    private final TileMatrixDimensions[] zoomLevelDimensions;
}
