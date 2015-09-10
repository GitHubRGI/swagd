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

package com.rgi.store.tiles.geopackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.util.ImageUtility;
import com.rgi.common.util.MimeTypeUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreWriter;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageWriter implements TileStoreWriter
{
    /**
     * @param geoPackageFile
     *            Handle to a new or existing GeoPackage file
     * @param coordinateReferenceSystem
     *            Coordinate reference system
     * @param tileSetTableName
     *            Name for the new tile set's table in the GeoPackage database
     * @param tileSetIdentifier
     *            A human-readable identifier (e.g. short name) for the tile set
     * @param tileSetDescription
     *            A human-readable description of the tile set
     * @param tileSetBounds
     *            Minimum bounds of the tile set, in spatial reference system
     *            units
     * @param tileScheme
     *            Contains the mechanism to calculate the relationship between
     *            the tile matrix dimensions at valid zoom levels
     * @param imageOutputFormat
     *            Image format for used for output
     * @param imageWriteOptions
     *            Controls details of the image writing process. If null, a
     *            default ImageWriteParam used instead
     * @throws TileStoreException
     *             if there's an error in constructing the underlying tile store implementation
     *
     */
    public GeoPackageWriter(final File                      geoPackageFile,
                            final CoordinateReferenceSystem coordinateReferenceSystem,
                            final String                    tileSetTableName,
                            final String                    tileSetIdentifier,
                            final String                    tileSetDescription,
                            final BoundingBox               tileSetBounds,
                            final TileScheme                tileScheme,
                            final MimeType                  imageOutputFormat,
                            final ImageWriteParam           imageWriteOptions) throws TileStoreException
    {
        if(geoPackageFile == null)
        {
            throw new IllegalArgumentException("GeoPackageFile cannot be null.");
        }

        if(coordinateReferenceSystem == null)
        {
            throw new IllegalArgumentException("Coordinate reference system cannot be null");
        }

        if(imageOutputFormat == null)
        {
            throw new IllegalArgumentException("Image output format may not be null");
        }

        if(!MimeTypeUtility.contains(GeoPackageWriter.SupportedImageFormats, imageOutputFormat))
        {
            throw new IllegalArgumentException(String.format("Image output type '%s' is inappropriate for this tile store. Valid formats are: %s",
                                                             imageOutputFormat.toString(),
                                                             GeoPackageWriter.SupportedImageFormats
                                                                             .stream()
                                                                             .map(mimeType -> mimeType.toString())
                                                                             .collect(Collectors.joining(", ", "'", "'"))));
        }

        if(geoPackageFile.getParentFile() != null && !geoPackageFile.getParentFile().isDirectory())
        {
            if(!geoPackageFile.getParentFile().mkdirs())
            {
                throw new RuntimeException("Unable to create file: " + geoPackageFile.getAbsolutePath());
            }
        }

        try
        {
            this.imageWriter = ImageIO.getImageWritersByMIMEType(imageOutputFormat.toString()).next();
        }
        catch(final NoSuchElementException ex)
        {
            throw new IllegalArgumentException(String.format("Mime type '%s' is not a supported for image writing by your Java environment", imageOutputFormat.toString()));
        }

        try
        {
            this.geoPackage = new GeoPackage(geoPackageFile, OpenMode.OpenOrCreate);
        }
        catch(final ClassNotFoundException | ConformanceException | IOException | SQLException ex)
        {
            throw new TileStoreException(ex);
        }

        try
        {
            if(this.geoPackage.tiles().getTileSet(tileSetTableName) != null)
            {
                throw new IllegalArgumentException("Tile set table name must be unique in this GeoPackage");
            }

            this.crsProfile = CrsProfileFactory.create(coordinateReferenceSystem);

            final SpatialReferenceSystem spatialReferenceSystem = this.geoPackage.core()
                                                                                 .addSpatialReferenceSystem(this.crsProfile.getName(),
                                                                                                            this.crsProfile.getCoordinateReferenceSystem().getAuthority(),
                                                                                                            this.crsProfile.getCoordinateReferenceSystem().getIdentifier(),
                                                                                                            this.crsProfile.getWellKnownText(),
                                                                                                            this.crsProfile.getDescription());
            this.tileSet = this.geoPackage.tiles()
                                          .addTileSet(tileSetTableName,
                                                      tileSetIdentifier,
                                                      tileSetDescription,
                                                      tileSetBounds,
                                                      spatialReferenceSystem);

            this.imageWriteOptions = imageWriteOptions; // May be null

            this.tileScheme = tileScheme;
        }
        catch(final Exception ex)
        {
            try
            {
                this.geoPackage.close();
            }
            catch(final SQLException ex1)
            {
                ex1.printStackTrace();
            }

            throw new TileStoreException(ex);
        }
    }

    @Override
    public void close() throws SQLException
    {
        this.geoPackage.close();
    }

    @Override
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        try
        {
            return this.geoPackage
                       .tiles()
                       .crsToTileCoordinate(this.tileSet,
                                            coordinate,
                                            this.crsProfile.getPrecision(),
                                            zoomLevel);
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public CrsCoordinate tileToCrsCoordinate(final int column, final int row, final int zoomLevel, final TileOrigin corner) throws TileStoreException
    {
        if(corner == null)
        {
            throw new IllegalArgumentException("Corner may not be null");
        }

        try
        {
            final TileMatrixDimensions dimensions     = this.tileScheme.dimensions(zoomLevel);
            final Coordinate<Integer>  tileCoordinate = corner.transform(GeoPackageTiles.Origin, column, row, dimensions);
            return this.geoPackage
                       .tiles()
                       .tileToCrsCoordinate(this.tileSet,
                                            tileCoordinate.getX() + corner.getHorizontal(),
                                            tileCoordinate.getY() + GeoPackageTiles.Origin.getVertical() - corner.getVertical(),
                                            zoomLevel);
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public BoundingBox getTileBoundingBox(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        final Coordinate<Double> lowerLeft  = this.tileToCrsCoordinate(column, row, zoomLevel, TileOrigin.LowerLeft);
        final Coordinate<Double> upperRight = this.tileToCrsCoordinate(column, row, zoomLevel, TileOrigin.UpperRight);

        return new BoundingBox(lowerLeft.getX(),
                               lowerLeft.getY(),
                               upperRight.getX(),
                               upperRight.getY());
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

        try
        {
            this.geoPackage
                .tiles()
                .addTile(this.tileSet,
                         this.getTileMatrix(zoomLevel, image.getWidth(), image.getHeight()),
                         coordinate,
                         this.crsProfile.getPrecision(),
                         ImageUtility.bufferedImageToBytes(image, this.imageWriter, this.imageWriteOptions));
        }
        catch(final SQLException | IOException ex)
        {
           throw new TileStoreException(ex);
        }
    }

    @Override
    public void addTile(final int column, final int row, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        try
        {
            this.geoPackage
                .tiles()
                .addTile(this.tileSet,
                         this.getTileMatrix(zoomLevel, image.getWidth(), image.getHeight()),
                         column,
                         row,
                         ImageUtility.bufferedImageToBytes(image, this.imageWriter, this.imageWriteOptions));
        }
        catch(final SQLException | IOException ex)
        {
           throw new TileStoreException(ex);
        }
    }

    @Override
    public Set<MimeType> getSupportedImageFormats()
    {
        return GeoPackageWriter.SupportedImageFormats;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.crsProfile.getCoordinateReferenceSystem();
    }

    @Override
    public TileScheme getTileScheme()
    {
        return this.tileScheme;
    }

    @Override
    public TileOrigin getTileOrigin()
    {
        return GeoPackageTiles.Origin;
    }

    private TileMatrix getTileMatrix(final int zoomLevel, final int imageWidth, final int imageHeight) throws SQLException
    {
        TileMatrix tileMatrix = null;

        if(!this.tileMatrices.containsKey(zoomLevel))
        {
            tileMatrix = this.addTileMatrix(zoomLevel, imageHeight, imageWidth);
            this.tileMatrices.put(zoomLevel, tileMatrix);
        }
        else
        {
            tileMatrix = this.tileMatrices.get(zoomLevel);
        }

        return tileMatrix;
    }

    private TileMatrix addTileMatrix(final int zoomLevel, final int tilePixelHeight, final int tilePixelWidth) throws SQLException
    {
        final TileMatrixDimensions tileMatrixDimensions = this.tileScheme.dimensions(zoomLevel);

        final BoundingBox tileSetBounds = this.tileSet.getBoundingBox();

        return this.geoPackage.tiles()
                              .addTileMatrix(this.tileSet,
                                             zoomLevel,
                                             tileMatrixDimensions.getWidth(),
                                             tileMatrixDimensions.getHeight(),
                                             tilePixelWidth,
                                             tilePixelHeight,
                                             tileSetBounds.getWidth()  / tileMatrixDimensions.getWidth()  / tilePixelWidth,
                                             tileSetBounds.getHeight() / tileMatrixDimensions.getHeight() / tilePixelHeight);
    }

    private final GeoPackage      geoPackage;
    private final TileSet         tileSet;
    private final CrsProfile      crsProfile;
    private final ImageWriter     imageWriter;
    private final ImageWriteParam imageWriteOptions;
    private final TileScheme      tileScheme;

    private final Map<Integer, TileMatrix> tileMatrices = new HashMap<>();

    /**
     * Image formats supported by an unextended GeoPackage
     */
    public static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet("image/jpeg", "image/png");
}
