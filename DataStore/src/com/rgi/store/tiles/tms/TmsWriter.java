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

package com.rgi.store.tiles.tms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.activation.MimeType;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.util.MimeTypeUtility;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreWriter;

/**
 * <a href="http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification">TMS</a>
 * implementation of {@link TileStoreWriter}
 *
 * @author Luke Lambert
 *
 */
public class TmsWriter extends TmsTileStore implements TileStoreWriter
{
    /**
     * Constructor
     *
     * @param coordinateReferenceSystem
     *             The coordinate reference system of this tile store
     * @param location
     *             The location of this tile store on-disk
     * @param imageOutputFormat
     *             Image format for used for output
     */
    public TmsWriter(final CoordinateReferenceSystem coordinateReferenceSystem,
                     final Path                      location,
                     final MimeType                  imageOutputFormat)
    {
        this(coordinateReferenceSystem,
             location,
             imageOutputFormat,
             null);
    }

    /**
     * Constructor
     *
     * @param coordinateReferenceSystem
     *             The coordinate reference system of this tile store
     * @param location
     *             The location of this tile store on-disk
     * @param imageOutputFormat
     *             Image format for used for output
     * @param imageWriteOptions
     *             Controls details of the image writing process.  If null, a default ImageWriteParam used instead
     */
    public TmsWriter(final CoordinateReferenceSystem coordinateReferenceSystem,
                     final Path                      location,
                     final MimeType                  imageOutputFormat,
                     final ImageWriteParam           imageWriteOptions)
    {
        super(coordinateReferenceSystem, location);

        if(!location.toFile().canWrite())
        {
            throw new IllegalArgumentException("Specified location cannot be written to");
        }

        if(imageOutputFormat == null)
        {
            throw new IllegalArgumentException("Image output format may not be null");
        }

        this.imageOutputFormat = imageOutputFormat;

        try
        {
            this.imageWriter = ImageIO.getImageWritersByMIMEType(imageOutputFormat.toString()).next();
        }
        catch(final NoSuchElementException ex)
        {
            throw new IllegalArgumentException(String.format("Mime type '%s' is not a supported for image writing by your Java environment", imageOutputFormat.toString()));
        }

        this.imageWriteOptions = imageWriteOptions;
    }

    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel)
    {
        return this.profile.crsToTileCoordinate(coordinate,
                                                this.profile.getBounds(),    // TMS uses absolute tiling, which covers the whole globe
                                                this.tileScheme.dimensions(zoomLevel),
                                                TmsTileStore.Origin);

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

        final Coordinate<Integer> tmsCoordinate = this.crsToTileCoordinate(coordinate, zoomLevel);

        this.addTile(tmsCoordinate.getX(),
                     tmsCoordinate.getY(),
                     zoomLevel,
                     image);
    }

    @Override
    public void addTile(final int column, final int row, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        final Path tilePath = tmsPath(this.location,
                                      zoomLevel,
                                      column).resolve(String.format("%d.%s",
                                                                    row,
                                                                    this.imageOutputFormat.getSubType().toLowerCase()));
        try
        {
            final Path parentPath = tilePath.getParent();

            if(parentPath == null)
            {
                throw new IllegalArgumentException(String.format("A parent directory does not exist for the tile z: %d, x: %d, y: %d.", zoomLevel, column, row));
            }

            // Image will not write unless the directories exist leading to it.
            if(!parentPath.toFile().exists())
            {
                final boolean directoryFound = (new File(parentPath.toString())).mkdirs();

                if(!directoryFound)
                {
                    throw new TileStoreException(String.format("Image directory does not exist. Invalid directory: %s", parentPath.toString()));
                }
            }

            try(final ImageOutputStream fileOutputStream = ImageIO.createImageOutputStream(tilePath.toFile()))
            {
                this.imageWriter.setOutput(fileOutputStream);

                try
                {
                    this.imageWriter.write(null, new IIOImage(image, null, null), this.imageWriteOptions);
                }
                catch(final IOException ex)
                {
                    if(this.imageWriteOptions == null || !this.imageWriteOptions.canWriteCompressed())
                    {
                        throw ex;   // If this isn't an issue caused by compression options being set, rethrow the exception
                    }

                    this.imageWriter.write(null, new IIOImage(image, null, null), null);
                }

                fileOutputStream.flush();
            }
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public Set<MimeType> getSupportedImageFormats()
    {
        return TmsWriter.SupportedImageFormats;
    }

    private final MimeType        imageOutputFormat;
    private final ImageWriter     imageWriter;
    private final ImageWriteParam imageWriteOptions;

    /**
     * Image MimeTypes supported by the TMS tile store
     */
    public static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet(ImageIO.getReaderMIMETypes());
}
