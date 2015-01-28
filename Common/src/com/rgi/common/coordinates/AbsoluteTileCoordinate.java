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

package com.rgi.common.coordinates;

import com.rgi.common.tile.TileOrigin;

/**
 * A Cartesian coordinate for a tile.  Absolute tile coordinates follow a TMS-
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
    public AbsoluteTileCoordinate(final int row, final int column, final int zoomLevel, final TileOrigin origin)
    {
        super(row, column);

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
    public int getRow()
    {
        return this.getY();
    }

    /**
     * @return Returns the column (x) portion of the coordinate
     */
    public int getColumn()
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

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != AbsoluteTileCoordinate.class)
        {
            return false;
        }

        final AbsoluteTileCoordinate other = (AbsoluteTileCoordinate)object;

        return super.equals(other)               &&
               this.zoomLevel == other.zoomLevel &&
               this.origin    == other.origin;
    }

    /**
     * Transform a tile coordinate between origins
     *
     * @param to The desired tile origin.  If this parameter matches the current origin, no transformation takes place.
     * @return Returns a copy of the coordinate with the row and column values transformed to the new tile origin
     */
    public AbsoluteTileCoordinate transform(final TileOrigin to)
    {
      final int size = (int)Math.pow(2, this.zoomLevel);

      final int row    = this.origin.getDeltaY() * to.getDeltaY() < 0 ? size - 1 - this.getY() : this.getY();
      final int column = this.origin.getDeltaX() * to.getDeltaX() < 0 ? size - 1 - this.getX() : this.getX();

      return new AbsoluteTileCoordinate(row, column, this.zoomLevel, to);
    }

    private final int        zoomLevel;
    private final TileOrigin origin;
}
