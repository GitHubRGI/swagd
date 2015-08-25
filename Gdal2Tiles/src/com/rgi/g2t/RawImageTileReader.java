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

package com.rgi.g2t;

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
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.util.FileUtility;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import org.gdal.gdal.Dataset;
import utility.GdalUtility;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.naming.OperationNotSupportedException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

/**
 * TileStoreReader implementation for GDAL-readable image files
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class RawImageTileReader implements TileStoreReader
{
    /**
     * Constructor
     *
     * @param rawImage
     *             A raster image
     * @param tileSize
     *             A {@link Dimensions} that describes what an individual tile
     *             looks like
     * @param noDataColor
     *             The {@link Color} of the NODATA fields within the raster image
     * @throws TileStoreException
     *             Thrown when GDAL could not get the correct
     *             {@link CoordinateReferenceSystem} of the input raster OR if
     *             the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader(final File                rawImage,
                              final Dimensions<Integer> tileSize,
                              final Color               noDataColor) throws TileStoreException
    {
        this(rawImage,
             tileSize,
             noDataColor,
             null);
    }

    /**
     * Constructor
     *
     * @param rawImage
     *             A raster image {@link File}
     * @param tileSize
     *             A {@link Dimensions} that describes what an individual tile
     *             looks like
     * @param noDataColor
     *             The {@link Color} of the NODATA fields within the raster image
     * @param coordinateReferenceSystem
     *             The {@link CoordinateReferenceSystem} the tiles should be
     *             output in
     * @throws TileStoreException
     *             Thrown when GDAL could not get the correct
     *             {@link CoordinateReferenceSystem} of the input raster OR if
     *             the raw image could not be loaded as a {@link Dataset}
     */
    public RawImageTileReader(final File                      rawImage,
                              final Dimensions<Integer>       tileSize,
                              final Color                     noDataColor,
                              final CoordinateReferenceSystem coordinateReferenceSystem) throws TileStoreException
    {
        this(rawImage,
             GdalUtility.open(rawImage, coordinateReferenceSystem),
             tileSize,
             noDataColor,
             coordinateReferenceSystem);
    }


    public RawImageTileReader(final File                      rawImage,
                              final Dataset                   dataset,
                              final Dimensions<Integer>       tileSize,
                              final Color                     noDataColor,
                              final CoordinateReferenceSystem coordinateReferenceSystem) throws TileStoreException
    {
        if(rawImage == null || !rawImage.canRead())
        {
            throw new IllegalArgumentException("Raw image may not be null, and must represent a valid file on disk.");
        }

        if(tileSize == null)
        {
            throw new IllegalArgumentException("Tile size may not be null.");
        }

        this.rawImage = rawImage;
        this.tileSize = tileSize;

        // TODO check noDataColor for null when the feature is implemented
        // this.noDataColor = noDataColor;

        this.dataset = dataset;

        try
        {
            if(this.dataset.GetRasterCount() == 0)
            {
                throw new IllegalArgumentException("Input file has no raster bands");
            }

            if(this.dataset.GetRasterBand(1).GetColorTable() != null)
            {
                System.out.println("expand this raster to RGB/RGBA"); // TODO: make a temporary vrt with gdal_translate to expand this to RGB/RGBA
            }

            // We cannot tile an image with no geo referencing information
            if(!GdalUtility.hasGeoReference(this.dataset))
            {
                throw new IllegalArgumentException("Input raster image has no georeference.");
            }

            this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystem(GdalUtility.getSpatialReference(this.dataset));

            if(this.coordinateReferenceSystem == null)
            {
                throw new IllegalArgumentException("Image file is not in a recognized coordinate reference system");
            }

            this.profile = CrsProfileFactory.create(this.coordinateReferenceSystem);

            this.tileScheme = new ZoomTimesTwo(0, MAX_ZOOM_LEVEL, 1, 1);    // Use absolute tile numbering

            final BoundingBox datasetBounds = GdalUtility.getBounds(this.dataset);

            this.tileRanges = GdalUtility.calculateTileRanges(this.tileScheme,
                                                              datasetBounds,
                                                              this.profile.getBounds(),
                                                              this.profile,
                                                              RawImageTileReader.Origin);

            final int minimumZoom = GdalUtility.getMinimalZoom(this.dataset, this.tileRanges, Origin, this.tileScheme, tileSize);
            final int maximumZoom = GdalUtility.getMaximalZoom(this.dataset, this.tileRanges, Origin, this.tileScheme, tileSize);

            // The bounds of the dataset is **almost never** the bounds of the
            // data.  The bounds of the dataset fit inside the bounds of the
            // data because the bounds of the data must align to the tile grid.
            // The minimum zoom level is selected such that the entire dataset
            // fits inside a single tile.  that single tile is the minimum data
            // bounds.

            final Coordinate<Integer> minimumTile = this.tileRanges.get(minimumZoom).getMinimum();  // The minimum and maximum for the range returned from tileRanges.get() should be identical (it's a single tile)

            this.dataBounds = this.getTileBoundingBox(minimumTile.getX(),
                                                      minimumTile.getY(),
                                                      this.tileScheme.dimensions(minimumZoom));

            this.zoomLevels = IntStream.rangeClosed(minimumZoom, maximumZoom)
                                       .boxed()
                                       .collect(Collectors.toSet());

            this.tileCount = IntStream.rangeClosed(minimumZoom, maximumZoom)
                                      .map(zoomLevel -> { final Range<Coordinate<Integer>> range = this.tileRanges.get(zoomLevel);

                                                          return (range.getMaximum().getX() - range.getMinimum().getX() + 1) *
                                                                 (range.getMinimum().getY() - range.getMaximum().getY() + 1);
                                                    })
                                      .sum();
        }
        catch(final DataFormatException dfe)
        {
            this.close();
            throw new TileStoreException(dfe);
        }

        this.cachedTiles = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void close() throws TileStoreException
    {
        // Remove temporary reprojected file
        if(this.dataset != null)
        {
            final Iterable<String> files = this.dataset.GetFileList();
            this.dataset.delete();
            for (final String file : files)
            {
                if(file.startsWith((System.getProperty("java.io.tmpdir"))))
                {
                    try
                    {
                        Files.delete(Paths.get(file));
                    }
                    catch (final IOException e)
                    {
                        throw new TileStoreException(e);
                    }
                }
            }
        }
        // Remove final temporary cached tile(s)
        try
        {
            this.cachedTiles.values().stream().forEach(tile -> {
                                                                   try
                                                                   {
                                                                       Files.delete(tile);
                                                                   }
                                                                   catch (final IOException e)
                                                                   {
                                                                       // Break the stream if a tile cannot
                                                                       // be deleted
                                                                       //noinspection ThrowCaughtLocally
                                                                       throw new RuntimeException(e);
                                                                   }
                                                               });
        }
        catch(final RuntimeException e)
        {
            // Catch the exception when a tile cannot be deleted and throw a new TileStoreException
            throw new TileStoreException(e);
        }
    }

    @Override
    public BoundingBox getBounds()
    {
        return this.dataBounds;
    }

    @Override
    public long countTiles()
    {
        return this.tileCount;
    }

    @Override
    public long getByteSize()
    {
        return this.rawImage.length();
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException(NOT_SUPPORTED_MESSAGE));
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        throw new TileStoreException(new OperationNotSupportedException(NOT_SUPPORTED_MESSAGE));
    }

    @Override
    public Set<Integer> getZoomLevels()
    {
        // removes the possibility of the collection being modified during return
        return Collections.unmodifiableSet(this.zoomLevels);
    }

    @Override
    public Stream<TileHandle> stream() throws TileStoreException
    {
        final List<TileHandle> tileHandles = new ArrayList<>();

        final Range<Integer> zoomRange = new Range<>(this.zoomLevels, Integer::compare);

        // Should always start with the lowest-integer-zoom-level that has only one tile
        final Range<Coordinate<Integer>> zoomInfo = this.tileRanges.get(zoomRange.getMinimum());

        // Get the coordinate information
        final Coordinate<Integer> topLeftCoordinate     = zoomInfo.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();

        // Parse each coordinate into min/max tiles for X/Y
        final int zoomMinXTile =     topLeftCoordinate.getX();
        final int zoomMaxXTile = bottomRightCoordinate.getX();
        final int zoomMinYTile = bottomRightCoordinate.getY();
        final int zoomMaxYTile =     topLeftCoordinate.getY();

        //Make tiles for each tile at the min zoom level
        for(int x = zoomMinXTile; x <= zoomMaxXTile; x++)
        {
            for (int y = zoomMinYTile; y <= zoomMaxYTile; y++)
            {
                this.makeTiles(tileHandles, new RawImageTileReader.RawImageTileHandle(zoomRange.getMinimum(), x, y), zoomRange.getMaximum());
            }
        }

        // Sort the tile handles so that they decrement.  This ensures all base level tiles
        // are generated before the overview tiles
        tileHandles.sort((o1, o2) -> Integer.compare(o2.getZoomLevel(), o1.getZoomLevel()));
        return tileHandles.stream();
    }

    private boolean tileIntersectsData(final TileHandle tile)
    {
        final int zoom   = tile.getZoomLevel();
        final int column = tile.getColumn();
        final int row    = tile.getRow();

        final Range<Coordinate<Integer>> zoomRange = this.tileRanges.get(zoom);

        return column >= zoomRange.getMinimum().getX() &&
               column <= zoomRange.getMaximum().getX() &&
               row    >= zoomRange.getMaximum().getY() &&
               row    <= zoomRange.getMinimum().getY();
    }

    private void makeTiles(final List<TileHandle> tileHandles, final TileHandle tile, final int maxZoom) throws TileStoreException
    {
        if(!this.tileIntersectsData(tile))
        {
            // Do nothing if the tile does not intersect with the data bounding box
            return;
        }
        if(tile.getZoomLevel() == maxZoom)
        {
            // tell the RawImageTileHandle this is a special case: a gdalImage
            tileHandles.add(new RawImageTileReader.RawImageTileHandle(tile.getZoomLevel(), tile.getColumn(), tile.getRow(), true));
        }
        else
        {
            // calculate all the tiles below this current one
            final int zoomBelow       = tile.getZoomLevel() + 1;
            // Shift values instead of multiplying by 2 for possible performance improvement
            final int zoomColumnBelow = tile.getColumn() << 1;
            final int zoomRowBelow    = tile.getRow() << 1;

            // create handles for below tiles
            final TileHandle belowOriginTile = new RawImageTileReader.RawImageTileHandle(zoomBelow,
                                                                         zoomColumnBelow,
                                                                         zoomRowBelow);
            final TileHandle belowColumnShiftedTile = new RawImageTileReader.RawImageTileHandle(zoomBelow,
                                                                         zoomColumnBelow + 1,
                                                                         zoomRowBelow);
            final TileHandle belowRowShiftedTile = new RawImageTileReader.RawImageTileHandle(zoomBelow,
                                                                         zoomColumnBelow,
                                                                         zoomRowBelow + 1);
            final TileHandle belowBothShiftedTile = new RawImageTileReader.RawImageTileHandle(zoomBelow,
                                                                         zoomColumnBelow + 1,
                                                                         zoomRowBelow + 1);

            // recurse
            this.makeTiles(tileHandles, belowOriginTile, maxZoom);
            this.makeTiles(tileHandles, belowColumnShiftedTile, maxZoom);
            this.makeTiles(tileHandles, belowRowShiftedTile, maxZoom);
            this.makeTiles(tileHandles, belowBothShiftedTile, maxZoom);

            // finally, add this current tile
            tileHandles.add(tile);
        }
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel) throws TileStoreException
    {
        final Range<Coordinate<Integer>> zoomInfo = this.tileRanges.get(zoomLevel);

        // Get the coordinate information
        final Coordinate<Integer> topLeftCoordinate     = zoomInfo.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = zoomInfo.getMaximum();

        // Parse each coordinate into min/max tiles for X/Y
        final int zoomMinXTile =     topLeftCoordinate.getX();
        final int zoomMaxXTile = bottomRightCoordinate.getX();
        final int zoomMinYTile = bottomRightCoordinate.getY();
        final int zoomMaxYTile =     topLeftCoordinate.getY();

        // Create a tile handle list that we can append to
        final Collection<TileHandle> tileHandles = new ArrayList<>();

        for(int tileY = zoomMaxYTile; tileY >= zoomMinYTile; --tileY)
        {
            for(int tileX = zoomMinXTile; tileX <= zoomMaxXTile; ++tileX)
            {
                tileHandles.add(new RawImageTileReader.RawImageTileHandle(zoomLevel, tileX, tileY));
            }
        }
        // Return the entire tile handle list as a stream
        return tileHandles.stream();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    @Override
    public String getName()
    {
        return FileUtility.nameWithoutExtension(this.rawImage);
    }

    @Override
    public String getImageType() throws TileStoreException
    {
        try
        {
            final MimeType mimeType = new MimeType(Files.probeContentType(this.rawImage.toPath()));

            if(mimeType.getPrimaryType().toLowerCase().equals("image"))
            {
               return mimeType.getSubType();
            }

            return null;
        }
        catch(final MimeTypeParseException | IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException
    {
        return this.tileSize;
    }

    @Override
    public TileScheme getTileScheme() throws TileStoreException
    {
        return this.tileScheme;
    }

    @Override
    public TileOrigin getTileOrigin()
    {
        return Origin;
    }

    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    private class RawImageTileHandle implements TileHandle
    {
        private final TileMatrixDimensions matrix;

        private boolean gotImage;
        private boolean gdalImage;
        private Path cachedImageLocation;
        private BufferedImage image;

        private final int zoomLevel;
        private final int column;
        private final int row;

        RawImageTileHandle(final int zoom,
                           final int column,
                           final int row) throws TileStoreException
        {
            this.zoomLevel = zoom;
            this.column    = column;
            this.row       = row;
            this.matrix    = RawImageTileReader.this.getTileScheme().dimensions(this.zoomLevel);
        }

        RawImageTileHandle(final int zoom,
                           final int column,
                           final int row,
                           final Path cachedImageLocation) throws TileStoreException
        {
            this.zoomLevel           = zoom;
            this.column              = column;
            this.row                 = row;
            this.matrix              = RawImageTileReader.this.getTileScheme().dimensions(this.zoomLevel);
            this.cachedImageLocation = cachedImageLocation;
        }

        RawImageTileHandle(final int zoom,
                           final int column,
                           final int row,
                           final boolean gdalImage) throws TileStoreException
        {
            this.zoomLevel = zoom;
            this.column    = column;
            this.row       = row;
            this.matrix    = RawImageTileReader.this.getTileScheme().dimensions(this.zoomLevel);
            this.gdalImage = gdalImage;
        }

        @Override
        public int getZoomLevel()
        {
            return this.zoomLevel;
        }

        @Override
        public int getColumn()
        {
            return this.column;
        }

        @Override
        public int getRow()
        {
            return this.row;
        }

        public Path getCachedImagePath()
        {
            return this.cachedImageLocation;
        }

        @Override
        public TileMatrixDimensions getMatrix() throws TileStoreException
        {
            return this.matrix;
        }

        @Override
        public CrsCoordinate getCrsCoordinate() throws TileStoreException
        {
            return RawImageTileReader.this.tileToCrsCoordinate(this.column,
                                                               this.row,
                                                               this.matrix,
                                                               RawImageTileReader.this.getTileOrigin());
        }

        @Override
        public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException
        {
            return RawImageTileReader.this.tileToCrsCoordinate(this.column,
                                                               this.row,
                                                               this.matrix,
                                                               corner);
        }

        @Override
        public BoundingBox getBounds() throws TileStoreException
        {
             return RawImageTileReader.this.getTileBoundingBox(this.column,
                                                               this.row,
                                                               this.matrix);
        }

        @Override
        public BufferedImage getImage() throws TileStoreException
        {
            if(this.gotImage)
            {
                return this.image;
            }

            if(this.gdalImage)
            {
                // Build the parameters for GDAL read raster call
                final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(RawImageTileReader.this.dataset.GetGeoTransform(),
                                                                                                    this.getBounds(),
                                                                                                    RawImageTileReader.this.tileSize,
                                                                                                    RawImageTileReader.this.dataset);
                try
                {
                    // Read image data directly from the raster
                    final byte[] imageData = GdalUtility.readRaster(params,
                                                                    RawImageTileReader.this.dataset);

                    // TODO: logic goes here in the case that the querysize == tile size (gdalconstConstants.GRA_NearestNeighbour) (write directly)
                    final Dataset querySizeImageCanvas = GdalUtility.writeRaster(params,
                                                                                 imageData,
                                                                                 RawImageTileReader.this.dataset.GetRasterCount());

                    try
                    {
                        // Scale each band of tileDataInMemory down to the tile size (down from the query size)
                        final Dataset tileDataInMemory = GdalUtility.scaleQueryToTileSize(querySizeImageCanvas,
                                                                                          RawImageTileReader.this.tileSize);

                        this.image = GdalUtility.convert(tileDataInMemory);

                        // Write this image to disk for later overview generation
                        final Path baseTilePath = this.writeTempTile(this.image);

                        // Add the image path to the reader map
                        RawImageTileReader.this.cachedTiles.put(this.tileKey(this.zoomLevel,
                                                                             this.column,
                                                                             this.row),
                                                                baseTilePath);

                        // Clean up dataset
                        tileDataInMemory.delete();
                        this.gotImage = true;

                        return this.image;
                    }
                    catch(final TilingException | IOException ex)
                    {
                        throw new TileStoreException(ex);
                    }
                    finally
                    {
                        querySizeImageCanvas.delete();
                    }
                }
                catch(final TilingException ex)
                {
                    throw new TileStoreException(ex);
                }
                catch(final IOException ignored)
                {
                    // An IOException is thrown by GdalUtility.readRaster() when the tile boundary
                    // requested lies outside of the databounding box.  This can sometimes occur
                    // when using the lowest-integer-zoom tile boundary as the tile-able area. This
                    // should not happen as the RawImageTileReader.makeTiles() method automatically
                    // checks if a generated tile does NOT intersect with the reported input raster
                    // bounding box.
                    // In this case, if readRaster does indeed throw IOException, just return a
                    // transparent tile.
                    this.gotImage = true;
                    return this.createTransparentImage();
                }
            }

            // Make this tile by getting the tiles below it and scaling them to fit
            return this.generateScaledTileFromChildren();
        }

        private Path writeTempTile(final RenderedImage tileImage) throws IOException
        {
            final Path baseTilePath = File.createTempFile("baseTile" + this.zoomLevel
                                                                     + this.column
                                                                     + this.row,
                                                          ".png")
                                          .toPath();
            try(final ImageOutputStream fileOutputStream = ImageIO.createImageOutputStream(baseTilePath.toFile()))
            {
                ImageIO.write(tileImage, "png", baseTilePath.toFile());
            }
            return baseTilePath;
        }

        private BufferedImage createTransparentImage()
        {
            final int tileWidth = RawImageTileReader.this.tileSize.getWidth();
            final int tileHeight = RawImageTileReader.this.tileSize.getHeight();

            final BufferedImage transparentImage = new BufferedImage(tileWidth,
                                                                     tileHeight,
                                                                     BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = transparentImage.createGraphics();
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, tileWidth, tileHeight);
            graphics.dispose();

            // Return the transparent tile
            return transparentImage;
        }

        private BufferedImage generateScaledTileFromChildren() throws TileStoreException
        {
            final int tileWidth  = RawImageTileReader.this.tileSize.getWidth();
            final int tileHeight = RawImageTileReader.this.tileSize.getHeight();

            // Create the full-sized buffered image from the child tiles
            final BufferedImage fullCanvas = new BufferedImage(tileWidth  * 2,
                                                               tileHeight * 2,
                                                               BufferedImage.TYPE_INT_ARGB);

            // Create the full-sized graphics object
            final Graphics2D fullCanvasGraphics = fullCanvas.createGraphics();

            // Get child handles
            final List<RawImageTileReader.RawImageTileHandle> children = this.generateTileChildren();

            // Get the cached children of this tile
            final List<RawImageTileReader.RawImageTileHandle> transformedChildren = children.stream().map(tileHandle ->
            {
                final Coordinate<Integer> resultCoordinate = RawImageTileReader.Origin.transform(TileOrigin.UpperLeft,
                                                                                                 tileHandle.column,
                                                                                                 tileHandle.row,
                                                                                                 this.matrix);
                try
                {
                    return new RawImageTileHandle(tileHandle.zoomLevel,
                                                  resultCoordinate.getX(),
                                                  resultCoordinate.getY(),
                                                  tileHandle.cachedImageLocation);
                } catch(TileStoreException e)
                {
                    throw new RuntimeException(e);
                }
            })
            .sorted((final TileHandle o1, final TileHandle o2) -> {
                final int columnCompare = Integer.compare(o1.getColumn(), o2.getColumn());
                   final int rowCompare    = Integer.compare(o1.getRow(),    o2.getRow());

                   // column values are the same
                   if(columnCompare == 0)
                   {
                       return rowCompare;
                   }
                   if(rowCompare == 0)
                   {
                       return columnCompare;
                   }

               // TODO: Duplicate tile case?
               return 0;
            })
            .collect(Collectors.toList());

            if(transformedChildren.size() == 4)
            {
                try
                {
                    // Origin tile
                    if(transformedChildren.get(2).cachedImageLocation != null)
                    {
                        final BufferedImage originImage = ImageIO.read(transformedChildren.get(2).cachedImageLocation.toFile());
                        fullCanvasGraphics.drawImage(originImage, null, 0, 0);
                    }

                    // Tile that is Y+1 in relation to the origin
                    if(transformedChildren.get(0).cachedImageLocation != null)
                    {
                        final BufferedImage rowShiftedImage = ImageIO.read(transformedChildren.get(0).cachedImageLocation.toFile());
                        fullCanvasGraphics.drawImage(rowShiftedImage, null, 0, tileHeight);
                    }

                    // Tile that is X+1 in relation to the origin
                    if(transformedChildren.get(3).cachedImageLocation != null)
                    {
                        final BufferedImage columnShiftedImage = ImageIO.read(transformedChildren.get(3).cachedImageLocation.toFile());
                        fullCanvasGraphics.drawImage(columnShiftedImage, null, tileWidth, 0);
                    }

                    // Tile that is both X+1 and Y+1 in relation to the origin
                    if(transformedChildren.get(1).cachedImageLocation != null)
                    {
                        final BufferedImage bothShiftedImage = ImageIO.read(transformedChildren.get(1).cachedImageLocation.toFile());
                        fullCanvasGraphics.drawImage(bothShiftedImage, null, tileWidth, tileHeight);
                    }
                }
                catch(final IOException ex)
                {
                    throw new TileStoreException(ex);
                }
            }
            else
            {
                throw new RuntimeException("Cannot create tile from child tiles.");
            }

            // Write cached tile
            try
            {
                final BufferedImage scaledTile = this.scaleToTileCanvas(fullCanvas);

                RawImageTileReader.this.cachedTiles.put(this.tileKey(this.zoomLevel, this.column, this.row), this.writeTempTile(scaledTile));
                return scaledTile;
            }
            catch(final IOException ex)
            {
                throw new TileStoreException(ex);
            }
            finally
            {
                // Clean-up step
                fullCanvasGraphics.dispose();
                children.stream().filter(Objects::nonNull).forEach(child ->
                {
                    if(child.cachedImageLocation != null)
                    {
                        child.cachedImageLocation.toFile().delete();
                    }
                    RawImageTileReader.this.cachedTiles.remove(this.tileKey(child.zoomLevel, child.column, child.row));
                });
            }
        }

        private BufferedImage scaleToTileCanvas(final BufferedImage fullCanvas)
        {
            final BufferedImage tileCanvas = new BufferedImage(RawImageTileReader.this.tileSize.getWidth(),
                                                               RawImageTileReader.this.tileSize.getHeight(),
                                                               BufferedImage.TYPE_INT_ARGB);

            final AffineTransform affineTransform = new AffineTransform();
            affineTransform.scale(AFFINE_SCALE, AFFINE_SCALE);
            final BufferedImageOp scaleOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);

            return scaleOp.filter(fullCanvas, tileCanvas);
        }

        private String tileKey(final int zoom, final int column, final int row)
        {
            return String.format("%d/%d/%d", zoom, column, row);
        }

        private List<RawImageTileReader.RawImageTileHandle> generateTileChildren() throws TileStoreException
        {
            final int childZoom   = this.zoomLevel + 1;
            final int childColumn = this.column * 2;
            final int childRow    = this.row    * 2;

            final Path origin        = RawImageTileReader.this.cachedTiles.get(this.tileKey(childZoom, childColumn,     childRow));
            final Path columnShifted = RawImageTileReader.this.cachedTiles.get(this.tileKey(childZoom, childColumn + 1, childRow));
            final Path rowShifted    = RawImageTileReader.this.cachedTiles.get(this.tileKey(childZoom, childColumn,     childRow + 1));
            final Path bothShifted   = RawImageTileReader.this.cachedTiles.get(this.tileKey(childZoom, childColumn + 1, childRow + 1));

            final List<RawImageTileReader.RawImageTileHandle> children = new ArrayList<>();

            children.add(new RawImageTileReader.RawImageTileHandle(childZoom, childColumn,     childRow,     origin));
            children.add(new RawImageTileReader.RawImageTileHandle(childZoom, childColumn + 1, childRow,     columnShifted));
            children.add(new RawImageTileReader.RawImageTileHandle(childZoom, childColumn,     childRow + 1, rowShifted));
            children.add(new RawImageTileReader.RawImageTileHandle(childZoom, childColumn + 1, childRow + 1, bothShifted));

            return children;
        }

        @Override
        public String toString()
        {
            return this.tileKey(this.zoomLevel, this.column, this.row);
        }
    }

    private CrsCoordinate tileToCrsCoordinate(final int                  column,
                                                final int                  row,
                                                final TileMatrixDimensions tileMatrixDimensions,
                                                final TileOrigin           corner)
    {
        if(corner == null)
        {
            throw new IllegalArgumentException("Corner may not be null");
        }

        return this.profile.tileToCrsCoordinate(column + corner.getHorizontal(),
                                                row    + corner.getVertical(),
                                                this.profile.getBounds(),    // RawImageTileReader uses absolute tiling, which covers the whole globe
                                                tileMatrixDimensions,
                                                RawImageTileReader.Origin);
    }

    private BoundingBox getTileBoundingBox(final int                  column,
                                             final int                  row,
                                             final TileMatrixDimensions tileMatrixDimensions)
    {
        return this.profile.getTileBounds(column,
                                          row,
                                          this.profile.getBounds(),
                                          tileMatrixDimensions,
                                          RawImageTileReader.Origin);
    }

    private final File                                     rawImage;
    private final CoordinateReferenceSystem                coordinateReferenceSystem;
    private final Dimensions<Integer>                      tileSize;
    //private final Color                                    noDataColor; // TODO implement no-data color handling
    private final Dataset                                  dataset;
    private final BoundingBox                              dataBounds;
    private final Set<Integer>                             zoomLevels;
    private final ZoomTimesTwo                             tileScheme;
    private final CrsProfile                               profile;
    private final int                                      tileCount;
    private final Map<Integer, Range<Coordinate<Integer>>> tileRanges;
    private final Map<String, Path>                        cachedTiles;

    private static final int                               MAX_ZOOM_LEVEL = 31;
    private static final double                            AFFINE_SCALE = 0.5;
    private static final String                            NOT_SUPPORTED_MESSAGE = "Call to unsupported method.";

    private static final TileOrigin Origin = TileOrigin.LowerLeft;
}
