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

package com.rgi.geopackage.tiles;

import com.rgi.common.coordinate.Coordinate;

/**
 * Tile coordinate specific to a tile set in a GeoPackage.  Unlike absolute
 * tile coordinates (ala TMS), relative tile coordinates "reset" at every zoom
 * level and are only valid for a specific tile set/zoom level combination.
 *
 * @author Luke Lambert
 *
 */
public class RelativeTileCoordinate extends Coordinate<Integer>
{
    /**
     * Constructor
     *
     * @param row
     *             The 'y' portion of the coordinate
     * @param column
     *             The 'x' portion of the coordinate
     * @param zoomLevel
     *             The zoom level associated with the coordinate
     */
    public RelativeTileCoordinate(final int row, final int column, final int zoomLevel)
    {
        super(row, column);

        if(zoomLevel < 0)
        {
            throw new IllegalArgumentException("Zoom level must be 0 or greater");
        }

        this.zoomLevel = zoomLevel;
    }

    /**
     * @return Returns the row (y) portion of the coordinate
     */
    public Integer getRow()
    {
        return this.getY();
    }

    /**
     * @return Returns the column (x) portion of the coordinate
     */
    public Integer getColumn()
    {
        return this.getX();
    }

    /**
     * @return the zoomLevel
     */
    public int getZoomLevel()
    {
        return this.zoomLevel;
    }

    private final int zoomLevel;
}
