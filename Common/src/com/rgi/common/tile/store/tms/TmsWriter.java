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

package com.rgi.common.tile.store.tms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Luke Lambert
 *
 */
public class TmsWriter extends TmsTileStore implements TileStoreWriter
{
    /**
     * Constructor
     *
     * @param profile
     *            The tile profile this tile store is using.
     * @param location
     *            The location of this tile store on-disk.
     */
    public TmsWriter(final TileProfile profile, final Path location)
    {
        super(profile, location);

        if(!location.toFile().canWrite())
        {
            throw new IllegalArgumentException("Specified location cannot be written to");
        }
    }

    @Override
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.profile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final AbsoluteTileCoordinate tmsCoordiante = this.profile.crsToAbsoluteTileCoordinate(coordinate,
                                                                                              zoomLevel,
                                                                                              TmsTileStore.Origin);
        this.addTile(tmsCoordiante.getRow(),
                     tmsCoordiante.getColumn(),
                     zoomLevel,
                     image);
    }

    @Override
    public void addTile(int row, int column, int zoomLevel, BufferedImage image) throws TileStoreException
    {
        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        final String outputFormat = "png";  // TODO how do we want to pick this ?

        final Path tilePath = tmsPath(this.location,
                                      zoomLevel,
                                      column).resolve(String.format("%d.%s",
                                                                    row,
                                                                    outputFormat));
        try
        {
            // Image will not write unless the directories exist leading to it.
            if (!tilePath.getParent().toFile().exists()) {
                boolean directoryFound = (new File(tilePath.getParent().toString())).mkdirs();
                
                if(!directoryFound)
                {
                    throw new TileStoreException(String.format("The directory does not exist leading to the Image. Invalid directory: %s", tilePath.getParent().toString()));
                }
            }

            if(!ImageIO.write(image, outputFormat, tilePath.toFile()))
            {
                throw new TileStoreException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }
}
