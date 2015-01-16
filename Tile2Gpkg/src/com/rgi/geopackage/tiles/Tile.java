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

/**
 * An object representation of an entry in a tile pyramid user data table
 * @author Luke Lambert
 *
 */
public class Tile
{
    /**
     * Constructor
     *
     * @param identifier
     *             Autoincrement primary key
     * @param zoomLevel
     *             zoom level
     * @param row
     *             Y component of the tile's cartesian coordinate
     * @param column
     *             X component of the tile's cartesian coordinate
     * @param imageData
     *             Bytes of an image file
     */
    protected Tile(final int    identifier,
                   final int    zoomLevel,
                   final int    row,
                   final int    column,
                   final byte[] imageData)
    {
        if(zoomLevel < 0)
        {
            throw new IllegalArgumentException("Zoom level must be 0 or greater");
        }

        if(row < 0)
        {
            throw new IllegalArgumentException("Row must be 0 or greater");
        }

        if(column < 0)
        {
            throw new IllegalArgumentException("Column must be 0 or greater");
        }

        if(imageData == null)
        {
            throw new IllegalArgumentException("Data cannot be null");
        }

        this.identifier = identifier;
        this.zoomLevel  = zoomLevel;
        this.row        = row;
        this.column     = column;
        this.imageData  = imageData;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the zoomLevel
     */
    public int getZoomLevel()
    {
        return this.zoomLevel;
    }

    /**
     * @return the row
     */
    public int getRow()
    {
        return this.row;
    }

    /**
     * @return the column
     */
    public int getColumn()
    {
        return this.column;
    }

    /**
     * @return the imageData
     */
    public byte[] getImageData()
    {
        return this.imageData;
    }

    private final int    identifier;
    private final int    zoomLevel;
    private final int    row;
    private final int    column;
    private final byte[] imageData;
}
