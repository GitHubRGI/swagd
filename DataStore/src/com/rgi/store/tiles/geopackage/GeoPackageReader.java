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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.GeoPackageTiles.TileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileMatrixSet;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageReader implements TileStoreReader
{
    /**
     * @param geoPackageFile
     *            Handle to a new or existing GeoPackage file
     * @param tileSetTableName
     *            Name for the new tile set's table in the GeoPackage database
     * @throws TileStoreException
     *             if there's an error in constructing the underlying tile store implementation
     */
    public GeoPackageReader(final File geoPackageFile, final String tileSetTableName) throws TileStoreException
    {
        this(geoPackageFile, tileSetTableName, VerificationLevel.Fast);
    }

    /**
     * @param geoPackageFile
     *            Handle to a new or existing GeoPackage file
     * @param tileSetTableName
     *            Name for the new tile set's table in the GeoPackage database
     * @param verificationLevel
     *             Controls the level of verification testing performed on this
     *             GeoPackage.  If verificationLevel is not None
     *             {@link GeoPackage#verify()} is called automatically and will throw if
     *             there are any conformance violations with the severity
     *             {@link com.rgi.geopackage.verification.Severity#Error}.  Throwing from this method means
     *             that it won't be possible to instantiate a GeoPackage object
     *             based on an SQLite "GeoPackage" file with severe errors.
     * @throws TileStoreException
     *             if there's an error in constructing the underlying tile store implementation
     */
    public GeoPackageReader(final File geoPackageFile, final String tileSetTableName, final VerificationLevel verificationLevel) throws TileStoreException
    {
        if(geoPackageFile == null)
        {
            throw new IllegalArgumentException("GeoPackage file may not be null");
        }

        if(tileSetTableName == null)
        {
            throw new IllegalArgumentException("Tile set may not be null or empty");
        }

        try
        {
            this.geoPackage = new GeoPackage(geoPackageFile, verificationLevel, GeoPackage.OpenMode.Open);
        }
        catch(final Exception ex)
        {
            throw new TileStoreException(ex);
        }

        try
        {
            this.tileSet = this.geoPackage.tiles().getTileSet(tileSetTableName);

            if(this.tileSet == null)
            {
                throw new IllegalArgumentException("Table name does not specify a valid GeoPackage tile set");
            }

            final SpatialReferenceSystem srs = this.geoPackage.core().getSpatialReferenceSystem(this.tileSet.getSpatialReferenceSystemIdentifier());

            if(srs == null)
            {
                throw new IllegalArgumentException("SRS may not be null");
            }

            this.crsProfile = CrsProfileFactory.create(srs.getOrganization(), srs.getOrganizationSrsId());

            this.zoomLevels = this.geoPackage.tiles().getTileZoomLevels(this.tileSet);

            this.tileMatrixSet = this.geoPackage.tiles().getTileMatrixSet(this.tileSet);

            this.tileMatrices = this.geoPackage.tiles()
                                                .getTileMatrices(this.tileSet)
                                                .stream()
                                                .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                          tileMatrix -> tileMatrix));

            this.tileScheme = new TileScheme()
                              {
                                  @Override
                                  public TileMatrixDimensions dimensions(final int zoomLevel)
                                  {
                                      if(GeoPackageReader.this.tileMatrices.containsKey(zoomLevel))
                                      {
                                          final TileMatrix tileMatrix = GeoPackageReader.this.tileMatrices.get(zoomLevel);
                                          return new TileMatrixDimensions(tileMatrix.getMatrixWidth(), tileMatrix.getMatrixHeight());
                                      }

                                      throw new IllegalArgumentException(String.format("Zoom level must be in the range %s",
                                                                                       new Range<>(GeoPackageReader.this.tileMatrices.keySet(), Integer::compare)));
                                  }

                                  @Override
                                  public Collection<Integer> getZoomLevels()
                                  {
                                      try
                                      {
                                          return GeoPackageReader.this.geoPackage.tiles().getTileZoomLevels(GeoPackageReader.this.tileSet);
                                      }
                                      catch(final SQLException ex)
                                      {
                                          throw new RuntimeException(ex);
                                      }
                                  }
                              };
        }
        catch(final Exception ex)
        {
            try
            {
                this.close();
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
    public BoundingBox getBounds() throws TileStoreException
    {
        return this.tileMatrixSet.getBoundingBox();
    }

    @Override
    public long countTiles() throws TileStoreException
    {
        // TODO lazy precalculation ?
        try
        {
            return this.geoPackage.core().getRowCount(this.tileSet);
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public long getByteSize() throws TileStoreException
    {
        // TODO lazy precalculation ?
        return this.geoPackage.getFile().getTotalSpace();
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        try
        {
            return getImage(this.geoPackage
                                .tiles()
                                .getTile(this.tileSet,
                                         column,
                                         row,
                                         zoomLevel));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        try
        {
            return getImage(this.geoPackage
                                .tiles()
                                .getTile(this.tileSet,
                                         coordinate,
                                         this.crsProfile.getPrecision(),
                                         zoomLevel));
        }
        catch(final IllegalArgumentException ex)
        {
            return null;//this is to catch an IAE if the crsCoordinate
                        //Requested is outside the bounds of the GeoPackage
                        //Tiles BoundingBox
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.crsProfile.getCoordinateReferenceSystem();
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        return this.zoomLevels;
    }

    @Override
    public Stream<TileHandle> stream() throws TileStoreException
    {
        try
        {
            return this.geoPackage
                       .tiles()
                       .getTiles(this.tileSet)
                       .map(tileCoordinate -> this.getTileHandle(tileCoordinate.getZoomLevel(),
                                                                 tileCoordinate.getColumn(),
                                                                 tileCoordinate.getRow()));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel) throws TileStoreException
    {
        try
        {
            return this.geoPackage
                       .tiles()
                       .getTiles(this.tileSet, zoomLevel)
                       .map(tileCoordinate -> this.getTileHandle(zoomLevel,
                                                                 tileCoordinate.getX(),
                                                                 tileCoordinate.getY()));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public String getImageType() throws TileStoreException
    {
        try
        {
            final TileCoordinate coordinate = this.geoPackage
                                                  .tiles()
                                                  .getTiles(this.tileSet)
                                                  .findFirst()
                                                  .orElse(null);

            if(coordinate != null)
            {
                final Tile tile = this.geoPackage
                                      .tiles()
                                      .getTile(this.tileSet,
                                               coordinate.getColumn(),
                                               coordinate.getRow(),
                                               coordinate.getZoomLevel());
                if(tile != null)
                {
                    final byte[] imageData = tile.getImageData();

                    try(final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData))
                    {
                        try(final ImageInputStream imageInputStream = ImageIO.createImageInputStream(byteArrayInputStream))
                        {
                            final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

                            while(imageReaders.hasNext())
                            {
                                final ImageReader imageReader = imageReaders.next();

                                final String[] names = imageReader.getOriginatingProvider().getFormatNames();
                                if(names != null && names.length > 0)
                                {
                                    return names[0];
                                }
                            }
                        }
                    }
                }
            }

            return null;
        }
        catch(final IOException | SQLException ex)
        {
            throw new TileStoreException(ex);
        }


    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException
    {
        final TileHandle tile = this.stream().findFirst().orElse(null);

        if(tile != null)
        {
            final BufferedImage image = tile.getImage();
            return new Dimensions<>(image.getWidth(), image.getHeight());
        }

        return null;
    }

    @Override
    public String getName()
    {
        return String.format("%s-%s",
                             this.geoPackage.getFile().getName(),
                             this.tileSet.getIdentifier());
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

    private static BufferedImage getImage(final Tile tile) throws TileStoreException
    {
        if(tile == null)
        {
            return null;
        }

        try
        {
            return ImageUtility.bytesToBufferedImage(tile.getImageData());
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    private TileHandle getTileHandle(final int zoomLevel, final int column, final int row)
    {
        final TileMatrix           tileMatrix = GeoPackageReader.this.tileMatrices.get(zoomLevel);
        final TileMatrixDimensions matrix     = new TileMatrixDimensions(tileMatrix.getMatrixWidth(), tileMatrix.getMatrixHeight());

        return new TileHandle()
                   {
                        @Override
                        public int getZoomLevel()
                        {
                            return zoomLevel;
                        }

                        @Override
                        public int getColumn()
                        {
                            return column;
                        }

                        @Override
                        public int getRow()
                        {
                            return row;
                        }

                        @Override
                        public TileMatrixDimensions getMatrix() throws TileStoreException
                        {
                            return matrix;
                        }

                        @Override
                        public CrsCoordinate getCrsCoordinate() throws TileStoreException
                        {
                            return GeoPackageReader.this
                                                   .crsProfile
                                                   .tileToCrsCoordinate(column,
                                                                        row,
                                                                        GeoPackageReader.this.getBounds(),
                                                                        matrix,
                                                                        GeoPackageTiles.Origin);
                        }

                        @Override
                        public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException
                        {
                            return GeoPackageReader.this
                                                   .crsProfile
                                                   .tileToCrsCoordinate(column + corner.getHorizontal(),     // same as: column - (GeoPackageTiles.Origin.getVertical() - corner.getHorizontal()) because GeoPackageTiles.Origin.getVertical() is always 0
                                                                        row    + (1 - corner.getVertical()),
                                                                        GeoPackageReader.this.getBounds(),
                                                                        matrix,
                                                                        GeoPackageTiles.Origin);
                        }

                        @Override
                        public BoundingBox getBounds() throws TileStoreException
                        {
                            final Coordinate<Double> upperLeft  = this.getCrsCoordinate(TileOrigin.UpperLeft);
                            final Coordinate<Double> lowerRight = this.getCrsCoordinate(TileOrigin.LowerRight);

                            return new BoundingBox(upperLeft.getX(),
                                                   lowerRight.getY(),
                                                   lowerRight.getX(),
                                                   upperLeft.getY());
                        }

                        @Override
                        public BufferedImage getImage() throws TileStoreException
                        {
                            return GeoPackageReader.this.getTile(column, row, zoomLevel);
                        }
                   };
    }

    private final GeoPackage               geoPackage;
    private final TileSet                  tileSet;
    private final CrsProfile               crsProfile;
    private final TileScheme               tileScheme;
    private final Set<Integer>             zoomLevels;
    private final Map<Integer, TileMatrix> tileMatrices;
    private final TileMatrixSet            tileMatrixSet;

}
