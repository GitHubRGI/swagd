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

package com.rgi.common.tile.store.tms;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.FileUtility;

/**
 * <a href="http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification">TMS</a>
 * implementation of {@link TileStoreReader}
 *
 * @author Luke Lambert
 *
 */
public class TmsReader extends TmsTileStore implements TileStoreReader
{
    /**
     * Constructor
     *
     * @param coordinateReferenceSystem
     *             The coordinate reference system of this tile store. TMS's
     *             lack of metadata means the coordinate reference system
     *             cannot be inferred.
     * @param location
     *             The location of this tile store on-disk
     */
    public TmsReader(final CoordinateReferenceSystem coordinateReferenceSystem, final Path location)
    {
        // TODO look for tilemapresource.xml for metadata
        super(coordinateReferenceSystem, location);

        if(!location.toFile().canRead())
        {
            throw new IllegalArgumentException("Specified location cannot be read from");
        }
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        if(this.bounds == null)
        {
            this.calculateBounds();
        }

        return this.bounds;
    }

    @Override
    public long countTiles()
    {
        if(this.tileCount == -1)
        {
            this.tileCount = this.countFiles(this.location.toFile());
        }

        return this.tileCount;
    }

    @Override
    public long getByteSize() throws TileStoreException
    {
        if(this.storeSize == -1)
        {
            try
            {
                this.storeSize = this.calculateStoreSize();
            }
            catch(final IOException ex)
            {
                throw new TileStoreException("An error occurred while calculating the size of the tile store.\n" + ex.getMessage());
            }
        }

        return this.storeSize;
    }

    @Override
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
    {
        final Optional<File> tileFile = this.getTiles(column, row, zoomLevel).findFirst();    // TODO prioritize list based on file type suitability (prefer transparency, etc)

        if(tileFile.isPresent())
        {
            try
            {
                return ImageIO.read(tileFile.get());
            }
            catch(final IOException ex)
            {
                throw new TileStoreException(ex);
            }
        }

        return null;
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

        final Coordinate<Integer> tmsCoordiante = this.profile.crsToTileCoordinate(coordinate,
                                                                                   this.profile.getBounds(),    // TMS uses absolute tiling, which covers the whole globe
                                                                                   this.tileScheme.dimensions(zoomLevel),
                                                                                   TmsTileStore.Origin);

        return this.getTile(tmsCoordiante.getX(),
                            tmsCoordiante.getY(),
                            zoomLevel);
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        if(this.zoomLevels == null)
        {
            this.calculateZoomLevels();
        }

        return this.zoomLevels;
    }

    @Override
    public Stream<TileHandle> stream()
    {
        return this.stream(this.location);
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel)
    {
        return this.stream(tmsPath(this.location, zoomLevel));
    }

    @Override
    public String getImageType()
    {
        try
        {
            return Files.walk(TmsReader.this.location)
                        .map(path -> { final File file = path.toFile();

                                       final String absolutePath = file.getAbsolutePath();

                                       if(TmsFilePattern.matcher(absolutePath).matches())
                                       {
                                           try
                                           {
                                               final MimeType mimeType = new MimeType(Files.probeContentType(path));

                                               if(mimeType.getPrimaryType().toLowerCase().equals("image"))
                                               {
                                                  return mimeType.getSubType();
                                              }
                                           }
                                           catch(final MimeTypeParseException | IOException ex)
                                           {
                                               // Do nothing. Fall through and return null
                                           }
                                       }
                                       return null;
                                     })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
        }
        catch(final IOException ex)
        {
            // Do nothing and fall through to return null
        }

        return null;
    }

    @Override
    public Dimensions<Integer> getImageDimensions()
    {
        try
        {
            return Files.walk(TmsReader.this.location)
                        .map(path -> { final File file = path.toFile();

                                       final String absolutePath = file.getAbsolutePath();

                                       final Matcher tmsFileMatch = TmsFilePattern.matcher(absolutePath);

                                       if(tmsFileMatch.matches())
                                       {
                                           try
                                           {
                                               final MimeType mimeType = new MimeType(Files.probeContentType(path));

                                               if(mimeType.getPrimaryType().toLowerCase().equals("image"))
                                               {
                                                   final int zoomLevel = Integer.parseInt(tmsFileMatch.group(1));
                                                   final int column    = Integer.parseInt(tmsFileMatch.group(2));
                                                   final int row       = Integer.parseInt(tmsFileMatch.group(3));

                                                   final BufferedImage image = TmsReader.this.getTile(column, row, zoomLevel);

                                                   if(image != null)
                                                   {
                                                       return new Dimensions<>(image.getWidth(), image.getHeight());
                                                   }
                                              }
                                           }
                                           catch(final MimeTypeParseException | IOException | TileStoreException ex)
                                           {
                                               // Do nothing. Fall through and return null
                                           }
                                       }
                                       return null;
                                     })
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
        }
        catch(final IOException ex)
        {
            // Do nothing and fall through to return null
        }

        return null;
    }

    private void calculateBounds() throws TileStoreException
    {
        final int minimumZoom = TmsReader.getTmsRange(this.location.toFile()).getMinimum();

        final Path pathToMinimumZoom = tmsPath(this.location, minimumZoom);

        final Range<Integer> xRange = TmsReader.getTmsRange(pathToMinimumZoom.toFile());
        final Range<Integer> yRange = TmsReader.getTmsRange(tmsPath(pathToMinimumZoom, xRange.getMaximum()).toFile());

        final TileMatrixDimensions dimensions = this.tileScheme.dimensions(minimumZoom);

        final Coordinate<Integer> transformedMinTileCoordinate = TmsTileStore.Origin.transform(TileOrigin.LowerLeft,  xRange.getMinimum(), yRange.getMinimum(), dimensions);
        final Coordinate<Integer> transformedMaxTileCoordinate = TmsTileStore.Origin.transform(TileOrigin.UpperRight, xRange.getMaximum(), yRange.getMaximum(), dimensions);

        final Coordinate<Double> lowerLeftCorner  = this.profile.tileToCrsCoordinate(transformedMinTileCoordinate.getX(), transformedMinTileCoordinate.getY(), this.profile.getBounds(), dimensions, TileOrigin.LowerLeft);    // TMS uses absolute tiling, which covers the whole globe
        final Coordinate<Double> upperRightCorner = this.profile.tileToCrsCoordinate(transformedMaxTileCoordinate.getX(), transformedMaxTileCoordinate.getY(), this.profile.getBounds(), dimensions, TileOrigin.UpperRight);   // TMS uses absolute tiling, which covers the whole globe

        this.bounds = new BoundingBox(lowerLeftCorner.getX(),
                                      lowerLeftCorner.getY(),
                                      upperRightCorner.getX(),
                                      upperRightCorner.getY());
    }

    /**
     * Counts the number of files of a certain type in an input folder.
     *
     * @param directory
     *            The folder in which the files should be counted in.
     * @return The number of files of a certain type found in the input folder.
     */
    private long countFiles(final File directory)
    {
        if(directory == null || !directory.canRead() || !directory.isDirectory())
        {
            return 0;
        }
        // Count files that have an allowed file extension
        final long fileCount = Arrays.stream(directory.listFiles())
                                     .filter(file -> file.isFile() &&   // Not a directory
                                                     fileIsImage(file)) // Allowed file extension
                                     .count();

        return Arrays.stream(directory.listFiles())
                     .filter(file -> file.isDirectory())
                     .map(subFolder -> this.countFiles(subFolder))
                     .reduce(fileCount, (a, b) -> a + b);   // Sum up all of the values
    }

    private long calculateStoreSize() throws IOException
    {
        final AtomicLong size = new AtomicLong(0);

        Files.walkFileTree(this.location,
                           new SimpleFileVisitor<Path>()
                           {
                               @Override
                               public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException
                               {
                                   if(attrs.isRegularFile())
                                   {
                                       size.addAndGet(attrs.size());
                                   }
                                   return FileVisitResult.CONTINUE;
                               }
                           });

        return size.get();
    }

    private void calculateZoomLevels()
    {
        this.zoomLevels = Stream.of(this.location.toFile().listFiles())
                                .filter(file -> file.isDirectory())
                                .map(file -> { try
                                               {
                                                   return Integer.parseInt(FileUtility.nameWithoutExtension(file));
                                               }
                                               catch(final NumberFormatException ex)
                                               {
                                                   return null;
                                               }
                                             })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
    }

    private TileHandle getTileHandle(final Path path)
    {
        final File file = path.toFile();

        final String absolutePath = file.getAbsolutePath();

        final Matcher tmsFileMatch = TmsFilePattern.matcher(absolutePath);

        if(tmsFileMatch.matches())
        {
            try
            {
                final MimeType mimeType = new MimeType(Files.probeContentType(path));

                if(mimeType.getPrimaryType().toLowerCase().equals("image"))
                {
                    final int zoomLevel = Integer.parseInt(tmsFileMatch.group(1));
                    final int column    = Integer.parseInt(tmsFileMatch.group(2));
                    final int row       = Integer.parseInt(tmsFileMatch.group(3));

                    return new TileHandle()
                           {
                               private final TileMatrixDimensions matrix = TmsReader.this.tileScheme.dimensions(zoomLevel);

                               private boolean       gotImage = false;
                               private BufferedImage image;

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
                                   return this.matrix;
                               }

                               @Override
                               public CrsCoordinate getCrsCoordinate() throws TileStoreException
                               {
                                   return TmsReader.this.tileToCrsCoordinate(column,
                                                                             row,
                                                                             zoomLevel,
                                                                             TmsTileStore.Origin);
                               }

                               @Override
                               public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException
                               {
                                   return TmsReader.this.tileToCrsCoordinate(column,
                                                                             row,
                                                                             zoomLevel,
                                                                             corner);
                               }

                               @Override
                               public BoundingBox getBounds() throws TileStoreException
                               {
                                   return TmsReader.this.getTileBoundingBox(column, row, zoomLevel);
                               }

                               @Override
                               public BufferedImage getImage() throws TileStoreException
                               {
                                   if(!this.gotImage)
                                   {
                                       this.image    = TmsReader.this.getTile(column, row, zoomLevel);
                                       this.gotImage = true;
                                   }

                                   return this.image;
                               }
                           };

                }
            }
            catch(final MimeTypeParseException | IOException ex)
            {
                // Do nothing.  Fall through to return null.
            }
        }

        return null;
    }

    /**
     * Gets the integer representation of the file of a certain type (lowest or
     * highest).
     *
     * @param directory
     *            The directory that contains files with integer names
     * @return The minimum and maximum integer value the supplied directory's file names
     * @throws TileStoreException
     *             If a file name cannot be parsed to an integer, a
     *             TileStoreException is thrown.
     */
    private static Range<Integer> getTmsRange(final File directory) throws TileStoreException
    {
        try
        {
            final Iterable<Integer> tmsNames = Stream.of(directory.listFiles())
                                                     .map(file -> { try
                                                                    {
                                                                        return Integer.parseInt(FileUtility.nameWithoutExtension(file));
                                                                    }
                                                                    catch(final NumberFormatException ex)
                                                                    {
                                                                        return null;
                                                                    }
                                                                  })
                                                     .filter(Objects::nonNull)
                                                     .collect(Collectors.toList());

            return new Range<>(tmsNames, Integer::compare);
        }
        catch(final IllegalArgumentException ex)
        {
            throw new TileStoreException(String.format("Directory %s contains no TMS entites",
                                                       directory.getName()));
        }
    }

    private Stream<File> getTiles(final int column, final int row, final int zoomLevel)
    {
        final File[] files = tmsPath(this.location,
                                     zoomLevel,
                                     column).toFile()
                                            .listFiles(); // All of the files in directory zoomLevel/x/

        return files == null ? Stream.empty()
                             : Stream.of(files)
                                     .filter(file -> file.isFile() &&
                                                     FileUtility.nameWithoutExtension(file).equals(String.valueOf(row)) &&
                                                     fileIsImage(file));
    }

    private Stream<TileHandle> stream(final Path startLocation)
    {
        try
        {
            return Files.walk(startLocation)
                        .map(path -> this.getTileHandle(path))
                        .filter(Objects::nonNull);
        }
        catch(final IOException ex)
        {
            // Do nothing and fall through to return an empty stream
        }

        return Stream.empty();
    }

    private static boolean fileIsImage(final File file)
    {
        try
        {
            final String mimeType = Files.probeContentType(file.toPath());
            return mimeType != null && mimeType.toLowerCase().startsWith("image/");
        }
        catch(final IOException ex)
        {
            return false;
        }
    }

    private Set<Integer> zoomLevels = null;
    private BoundingBox  bounds     = null;
    private long         tileCount  = -1;
    private long         storeSize  = -1;

    private static Pattern TmsFilePattern = Pattern.compile(".*(?:\\\\|/)([0-9]+)(?:\\\\|/)([0-9]+)(?:\\\\|/)([0-9]+)\\.[^\\\\/]*$");
}
