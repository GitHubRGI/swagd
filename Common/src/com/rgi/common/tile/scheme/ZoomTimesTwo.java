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


/**
 * @author Luke Lambert
 *
 */
public class ZoomTimesTwo implements TileScheme
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
     */
    public ZoomTimesTwo(final int minimumZoomLevel,
                        final int maximumZoomLevel,
                        final int baseZoomLevelTileMatrixHeight,
                        final int baseZoomLevelTileMatrixWidth)
    {
        if(minimumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Minimum zoom level must be at least 0");
        }

        if(maximumZoomLevel < 0)
        {
            throw new IllegalArgumentException("Maximum zoom level must be at least 0");
        }

        if(baseZoomLevelTileMatrixHeight < 1)
        {
            throw new IllegalArgumentException("The initial height must be greater than 0");
        }

        if(baseZoomLevelTileMatrixWidth < 1)
        {
            throw new IllegalArgumentException("The initial width must be greater than 0");
        }

        if(minimumZoomLevel > maximumZoomLevel)
        {
            throw new IllegalArgumentException("Minimum zoom level must be less than or equal to the maximum");
        }

        if(Integer.MAX_VALUE < baseZoomLevelTileMatrixHeight * Math.pow(2.0, maximumZoomLevel) - 1)
        {
            throw new IllegalArgumentException("This combination of initial height and maximum zoom level will cause an integer overflow for tile numbering");
        }

        if(Integer.MAX_VALUE < baseZoomLevelTileMatrixWidth * Math.pow(2.0, maximumZoomLevel) - 1)
        {
            throw new IllegalArgumentException("This combination of initial width and maximum zoom level will cause an integer overflow for tile numbering");
        }

        this.minimumZoomLevel = minimumZoomLevel;
        this.maximumZoomLevel = maximumZoomLevel;

        this.zoomLevelDimensions = new TileMatrixDimensions[maximumZoomLevel - minimumZoomLevel + 1];

        for(int zoomLevel = minimumZoomLevel; zoomLevel <= maximumZoomLevel; ++zoomLevel)
        {
            final int zoom = zoomLevel - minimumZoomLevel;
            final int twoToTheZoomPower = (int)Math.pow(2.0, zoom);
            this.zoomLevelDimensions[zoom] = new TileMatrixDimensions(baseZoomLevelTileMatrixHeight * twoToTheZoomPower,
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

    protected final int minimumZoomLevel;
    protected final int maximumZoomLevel;
}
