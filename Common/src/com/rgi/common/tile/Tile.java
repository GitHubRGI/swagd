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

package com.rgi.common.tile;

import java.awt.image.BufferedImage;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;

/**
 * An image tile stored in a BufferedImage
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class Tile
{
    /**
     * Constructor
     *
     * @param coordinate
     *            The row and column of the tile image.
     * @param bufferedImage
     *            A BufferedImage object with the Image binary data.
     */
    public Tile(final AbsoluteTileCoordinate coordinate,
                final BufferedImage  bufferedImage)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }

        if(bufferedImage == null)
        {
            throw new IllegalArgumentException("Buffered image cannot be null");
        }

        this.coordinate    = coordinate;
        this.bufferedImage = bufferedImage;
    }

    /**
     * Constructor
     *
     * @param zoomLevel
     *            The zoom level of the tile image.
     * @param row
     *            Tile row (y)
     * @param column
     *            Tile column (x)
     * @param origin
     *            The TileOrigin enum value for the tile image.
     * @param bufferedImage
     *            A BufferedImage object with the Image binary data.
     */
    public Tile(final int           row,
                final int           column,
                final int           zoomLevel,
                final TileOrigin    origin,
                final BufferedImage bufferedImage)
    {
        this(new AbsoluteTileCoordinate(row, column, zoomLevel, origin), bufferedImage);
    }

    public int getZoomLevel()
    {
        return this.coordinate.getZoomLevel();
    }

    public AbsoluteTileCoordinate getCoordinate()
    {
        return this.coordinate;
    }

    public int getTileRow()
    {
        return this.coordinate.getRow();
    }

    public int getTileColumn()
    {
        return this.coordinate.getColumn();
    }

    public TileOrigin getTileOrigin()
    {
        return this.coordinate.getOrigin();
    }

    public int getWidth()
    {
        return this.bufferedImage.getWidth();
    }

    public int getHeight()
    {
        return this.bufferedImage.getHeight();
    }

    public BufferedImage getImageContents()
    {
        return this.bufferedImage;
    }

    private final AbsoluteTileCoordinate coordinate;
    private final BufferedImage  bufferedImage;
}
