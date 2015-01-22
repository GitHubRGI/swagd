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

package com.rgi.g2t;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
/**
 * @author Duff Means
 * @author Luke Lambert
 *
 */
public class SimpleFileStoreWriter extends SimpleFileStore implements TileStoreWriter
{
    public SimpleFileStoreWriter(String name, TileProfile tileProfile, TileOrigin tileOrigin, String location, String imageFormat) throws TileStoreException
    {
        super(name, tileProfile, tileOrigin, location, imageFormat);

        if(!this.rootFolder.exists())
        {
            if(!this.rootFolder.mkdirs())
            {
                throw new TileStoreException("Unable to create folders");
            }
        }
    }

    @Override
    public void addTile(int row, int column, int zoomLevel, BufferedImage image) throws TileStoreException
    {
        final File zoomFolder = new File(this.rootFolder, ""+zoomLevel);
        if(!zoomFolder.exists())
        {
            if(!zoomFolder.mkdirs())
            {
                throw new TileStoreException("Unable to create folders");
            }
        }

        final File xFolder = new File(zoomFolder, "" + column);
        if(!xFolder.exists())
        {
            if(!xFolder.mkdirs())
            {
                throw new TileStoreException("Unable to create folders");
            }
        }
        final File yFile = new File(xFolder, row + "." + this.imageFormat);
        try
        {
            ImageIO.write(image, this.imageFormat, yFile);
        }
        catch(final IOException ioe)
        {
            throw new TileStoreException("Unable to write tile to file", ioe);
        }
    }

    @Override
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.tileProfile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final AbsoluteTileCoordinate absTileCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate, zoomLevel, this.tileOrigin);

        this.addTile(absTileCoordinate.getRow(),
                     absTileCoordinate.getColumn(),
                     zoomLevel,
                     image);
    }
}
