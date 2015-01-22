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

package com.rgi.common.tile.store.tilecluster;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" i.e. ClusteredTileStream
 *
 */
public class TileClusterWriter extends TileCluster implements TileStoreWriter
{
    public TileClusterWriter(final Path        location,
                             final String      setName,
                             final int         levels,
                             final int         breakPoint,
                             final TileProfile tileProfile)
    {
        super(location, setName, levels, breakPoint, tileProfile);

        if(!location.toFile().canWrite())
        {
            throw new IllegalArgumentException("Specified location cannot be written to");
        }
    }

    @Override
    public void addTile(final int row, final int column, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(row, column, zoomLevel);
        final File           clusterFile    = this.getClusterFile(clusterAddress);

        // If the file doesn't exist, set up an empty cluster file
        if(!clusterFile.exists())
        {
            try
            {
                TileClusterWriter.createNewClusterFile(clusterFile, clusterAddress.endlevel - clusterAddress.startlevel + 1);
            }
            catch(final IOException ex)
            {
                throw new TileStoreException(ex);
            }
        }

        final String outputFormat = "PNG";  // TODO how do we want to pick this ?

        try(final RandomAccessFile      randomAccessFile  = new RandomAccessFile(clusterFile, "rw");
            @SuppressWarnings("resource")
            final ByteArrayOutputStream outputStream      = new ByteArrayOutputStream())
        {
            if(!ImageIO.write(image, outputFormat, outputStream))
            {
                throw new TileStoreException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            final byte[] imageData = outputStream.toByteArray();

            // Write the data at the end of the tile file
            final long tilePosition = randomAccessFile.length();

            randomAccessFile.seek(tilePosition);

            randomAccessFile.writeLong(TileClusterWriter.MagicNumber);
            randomAccessFile.writeLong(TileClusterWriter.MagicNumber);
            randomAccessFile.writeLong(column);
            randomAccessFile.writeLong(row);
            randomAccessFile.writeInt (imageData.length);
            randomAccessFile.write    (imageData);

            final long indexPosition = this.getIndexPosition(row, column, zoomLevel);

            randomAccessFile.seek(indexPosition);

            // write the tile position and size in the index
            randomAccessFile.writeLong(tilePosition);
            randomAccessFile.writeInt(imageData.length);
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.tileProfile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final AbsoluteTileCoordinate clusterCoordinate = this.tileProfile.crsToAbsoluteTileCoordinate(coordinate,
                                                                                                      zoomLevel,
                                                                                                      TileCluster.Origin);
        this.addTile(clusterCoordinate.getRow(),
                     clusterCoordinate.getColumn(),
                     zoomLevel,
                     image);
    }

    /**
     * Create an empty file and fills the index with NoDataLong values
     *
     * @param file
     * @param numberOfzoomLevels
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void createNewClusterFile(final File file, final int numberOfzoomLevels) throws FileNotFoundException, IOException
    {
        try(final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"))
        {
            randomAccessFile.seek(0);

            final long tiles = TileCluster.getCumulativeTileCount(numberOfzoomLevels);

            for(long i = 0; i < tiles; ++i)
            {
                randomAccessFile.writeLong(TileCluster.NoDataLong);  // NoData position of tile
                randomAccessFile.writeInt (TileCluster.NoDataInt);   // NoData size of tile
            }
        }
    }



    private static final long MagicNumber = 0x772211ee;

}
