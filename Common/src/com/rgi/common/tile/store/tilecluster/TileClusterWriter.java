/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.common.tile.store.tilecluster;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.ImageUtility;
import com.rgi.common.util.MimeTypeUtility;

/**
 * @author Luke Lambert
 *
 * TODO This implementation is incomplete, and not ready for use
 *
 * TODO give attribution for this code "Tile-Based Geospatial Information Systems" by John T. Sample and Elias Ioup, Chapter 8 and specifically Listing 8.3 "Tile Clusters implementation" i.e. ClusteredTileStream
 *
 */
@SuppressWarnings("javadoc")
public class TileClusterWriter extends TileCluster implements TileStoreWriter
{
    /**
     * Constructor
     *
     * @param location
     * @param setName
     * @param levels
     * @param breakPoint
     * @param crsProfile
     * @param imageOutputFormat
     * @param imageWriteOptions
     */
    public TileClusterWriter(final Path            location,
                             final String          setName,
                             final int             levels,
                             final int             breakPoint,
                             final CrsProfile      crsProfile,
                             final MimeType        imageOutputFormat,
                             final ImageWriteParam imageWriteOptions)
    {
        super(location, setName, levels, breakPoint, crsProfile);

        if(!location.toFile().canWrite())
        {
            throw new IllegalArgumentException("Specified location cannot be written to");
        }

        if(imageOutputFormat == null)
        {
            throw new IllegalArgumentException("Image output format may not be null");
        }

        try
        {
            this.imageWriter = ImageIO.getImageWritersByMIMEType(imageOutputFormat.toString()).next();
        }
        catch(final NoSuchElementException ex)
        {
            throw new IllegalArgumentException(String.format("Mime type '%s' is not a supported for image writing by your Java environment", imageOutputFormat.toString()));
        }

        this.imageWriteOptions = imageWriteOptions; // May be null
    }

    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel)
    {
        return this.crsProfile.crsToTileCoordinate(coordinate,
                                                   this.getBounds(),    // TODO: Should this be crs bounds?
                                                   this.tileScheme.dimensions(zoomLevel),
                                                   TileCluster.Origin);
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int column, final int row, final int zoomLevel, final TileOrigin corner)
    {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    @Override
    public BoundingBox getTileBoundingBox(final int column, final int row, final int zoomLevel)
    {
        // TODO
        throw new RuntimeException("Not implemented");
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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.crsProfile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final Coordinate<Integer> clusterCoordinate = this.crsToTileCoordinate(coordinate, zoomLevel);

        this.addTile(clusterCoordinate.getX(),
                     clusterCoordinate.getY(),
                     zoomLevel,
                     image);
    }

    @Override
    public void addTile(final int column, final int row, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        final ClusterAddress clusterAddress = this.getClusterAddress(column, row, zoomLevel);
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

        try(final RandomAccessFile randomAccessFile  = new RandomAccessFile(clusterFile, "rw"))
        {
            final byte[] imageData = ImageUtility.bufferedImageToBytes(image, this.imageWriter, this.imageWriteOptions);

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

    @Override
    public Set<MimeType> getSupportedImageFormats()
    {
        return TileClusterWriter.SupportedImageFormats;
    }

    private final ImageWriter     imageWriter;
    private final ImageWriteParam imageWriteOptions;

    private static final long          MagicNumber           = 0x772211ee;
    private static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet(ImageIO.getReaderMIMETypes());
}
