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

package com.rgi.erdc.coordinates;

import com.rgi.erdc.ZoomLevel;
import com.rgi.erdc.tile.TileOrigin;

/**
 * A cartesian coordinate for a tile.  Absolute tile coordinates follow a TMS-
 * like convention.  Zoom level 0 contains a single tile.  Each zoom level
 * covers the entire global extent, and tiles on both axes are numbered 0 to (2^zoomlevel)-1
 * i.e. each tile is bisected horizontally and vertically to create the tiles at
 * the next zoom level.  The tile origin controls which tile in the resulting
 * matrix is (0, 0).
 *
 * @author Luke Lambert
 *
 */
public class AbsoluteTileCoordinate extends Coordinate<Integer>
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
     * @param origin
     *             The the corner of the tile that represents the coordinate
     */
    public AbsoluteTileCoordinate(final Integer row, final Integer column, final int zoomLevel, final TileOrigin origin)
    {
        super(row, column);

        ZoomLevel.verify(zoomLevel);

        if(origin == null)
        {
            throw new IllegalArgumentException("Origin cannot be null");
        }

        this.zoomLevel = zoomLevel;
        this.origin    = origin;
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

    /**
     * @return the origin
     */
    public TileOrigin getOrigin()
    {
        return this.origin;
    }

    /**
     * Transform a tile coordinate between origins
     *
     * @param to The desired tile origin.  If this parameter matches the current origin, no transformation takes place.
     * @return Returns a copy of the coordinate with the row and column values transformed to the new tile origin
     */
    public AbsoluteTileCoordinate transform(final TileOrigin to)
    {
      int size = (int)Math.pow(2, zoomLevel);
      int row = origin.getDeltaY() * to.getDeltaY() < 0 ? size - 1 - getY() : getY();
      int column = origin.getDeltaX() * to.getDeltaX() < 0 ? size - 1 - getX() : getX();
      return new AbsoluteTileCoordinate(row, column, zoomLevel, to);
    }

    private final int        zoomLevel;
    private final TileOrigin origin;
}
