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
import java.util.NoSuchElementException;
import java.util.Set;

import javax.activation.MimeType;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.MimeTypeUtility;

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
     * @param imageOutputFormat
     *             Image format for used for output
     */
    public TmsWriter(final CrsProfile profile,
                     final Path       location,
                     final MimeType   imageOutputFormat)
    {
        this(profile, location, imageOutputFormat, null);
    }

    /**
     * Constructor
     *
     * @param profile
     *            The tile profile this tile store is using.
     * @param location
     *            The location of this tile store on-disk.
     * @param imageOutputFormat
     *             Image format for used for output
     * @param imageWriteOptions
     *             Controls details of the image writing process.  If null, a default ImageWriteParam used instead
     */
    public TmsWriter(final CrsProfile      profile,
                     final Path            location,
                     final MimeType        imageOutputFormat,
                     final ImageWriteParam imageWriteOptions)
    {
        super(profile, location);

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

        this.imageWriteOptions = imageWriteOptions != null ? imageWriteOptions
                                                           : this.imageWriter.getDefaultWriteParam();
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

        final Coordinate<Integer> tmsCoordiante = this.profile.crsToTileCoordinate(coordinate,
                                                                                   this.profile.getBounds(),    // TMS uses absolute tiling, which covers the whole globe
                                                                                   this.tileScheme.dimensions(zoomLevel),
                                                                                   TmsTileStore.Origin);
        this.addTile(tmsCoordiante.getY(),
                     tmsCoordiante.getX(),
                     zoomLevel,
                     image);
    }

    @Override
    public void addTile(final int row, final int column, final int zoomLevel, final BufferedImage image) throws TileStoreException
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
            // Image will not write unless the directories exist leading to it.
            if(!tilePath.getParent().toFile().exists())
            {
                final boolean directoryFound = (new File(tilePath.getParent().toString())).mkdirs();

                if(!directoryFound)
                {
                    throw new TileStoreException(String.format("Image directory does not exist. Invalid directory: %s", tilePath.getParent().toString()));
                }
            }

            try(final ImageOutputStream fileOutputStream = ImageIO.createImageOutputStream(tilePath.toFile()))
            {
                this.imageWriter.setOutput(fileOutputStream);
                this.imageWriter.write(null, new IIOImage(image, null, null), this.imageWriteOptions);
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

    private static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet(ImageIO.getReaderMIMETypes());
}
