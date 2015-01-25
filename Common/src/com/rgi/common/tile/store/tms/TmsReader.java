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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.AbsoluteTileCoordinate;
import com.rgi.common.coordinates.Coordinate;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Luke Lambert
 *
 */
public class TmsReader extends TmsTileStore implements TileStoreReader
{
    /**
     * Constructor
     *
     * @param profile
     *            The tile profile this tile store is using.
     * @param location
     *            The location of this tile store on-disk.
     */
    public TmsReader(final TileProfile profile, final Path location)
    {
        super(profile, location);

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
    public BufferedImage getTile(int row, int column, int zoomLevel) throws TileStoreException
    {
        final Optional<File> tileFile = this.getTiles(row, column, zoomLevel).findFirst();    // TODO prioritize list based on file type suitability (prefer transparency, etc)

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

        final AbsoluteTileCoordinate tmsCoordiante = this.profile.crsToAbsoluteTileCoordinate(coordinate, zoomLevel, TmsTileStore.Origin);

        return this.getTile(tmsCoordiante.getRow(), tmsCoordiante.getColumn(), zoomLevel);
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
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.profile.getCoordinateReferenceSystem();
    }

    private class Range
    {
        public Range(final int minimum, final int maximum)
        {
            this.minimum = minimum;
            this.maximum = maximum;
        }

        public int minimum;
        public int maximum;
    }

    private void calculateBounds() throws TileStoreException
    {
        final int minimumZoom = this.getTmsRange(this.location.toFile()).minimum; // Get the minimum zoom level

        final Path pathToMinimumZoom = tmsPath(this.location, minimumZoom);

        final Range xRange = this.getTmsRange(pathToMinimumZoom.toFile());
        final Range yRange = this.getTmsRange(tmsPath(pathToMinimumZoom, xRange.maximum).toFile());

        // TODO attention: Lander, this logic needs to be double checked
        final Coordinate<Double> lowerLeftCorner  = this.profile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(yRange.minimum,     xRange.minimum,     minimumZoom, TmsTileStore.Origin));
        final Coordinate<Double> upperRightCorner = this.profile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(yRange.maximum + 1, xRange.maximum + 1, minimumZoom, TmsTileStore.Origin));

        this.bounds = new BoundingBox(lowerLeftCorner.getY(),
                                      lowerLeftCorner.getX(),
                                      upperRightCorner.getY(),
                                      upperRightCorner.getX());
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
                                                   return Integer.parseInt(withoutExtension(file));
                                               }
                                               catch(final NumberFormatException ex)
                                               {
                                                   return null;
                                               }
                                             })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
    }

    /**
     * Gets the integer representation of the file of a certain type (lowest or
     * highest).
     *
     * @param directory
     *            The directory that contains files with integer names.
     * @param type
     *            The file name to retrieve, either highest integer value or
     *            lowest integer value (parsed from a string).
     * @return The integer value of the lowest or highest file name.
     * @throws TileStoreException
     *             If a file name cannot be parsed to an integer, a
     *             TileStoreException is thrown.
     */
    private Range getTmsRange(final File directory) throws TileStoreException
    {
        final Range minmax = Stream.of(directory.listFiles())
                                   .collect(() -> new Range(Integer.MAX_VALUE, Integer.MIN_VALUE),
                                            (range, file) -> { try
                                                               {
                                                                   final int value = Integer.parseInt(withoutExtension(file));

                                                                   if(value < range.minimum)
                                                                   {
                                                                       range.minimum = value;
                                                                   }
                                                                   if(value > range.maximum)
                                                                   {
                                                                       range.maximum = value;
                                                                   }
                                                               }
                                                               catch(final NumberFormatException ex)
                                                               {
                                                                   // do nothing
                                                               }
                                                             },
                                             (range1, range2) -> { range1.minimum = Math.min(range1.minimum, range2.minimum);
                                                                   range1.maximum = Math.max(range1.maximum, range2.maximum);
                                                                 });
        if(minmax.minimum == Integer.MAX_VALUE)
        {
            throw new TileStoreException(String.format("Directory %s contains no TMS entites",
                                                       directory.getName()));
        }

        return minmax;
    }

    private Stream<File> getTiles(int row, int column, int zoomLevel)
    {
        final File[] files = tmsPath(this.location,
                                     zoomLevel,
                                     column).toFile()
                                            .listFiles(); // All of the files in directory zoomLevel/x/

        return files == null ? Stream.empty()
                             : Stream.of(files)
                                     .filter(file -> file.isFile() &&
                                                     withoutExtension(file).equals(String.valueOf(row)) &&
                                                     fileIsImage(file));
    }

    private static boolean fileIsImage(final File file)
    {
        try
        {
            final String mimeType = Files.probeContentType(file.toPath());
            return mimeType != null && mimeType.startsWith("image/");
        }
        catch(final IOException ex)
        {
            return false;
        }
    }

    private static String withoutExtension(final File file)
    {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    private Set<Integer> zoomLevels = null;
    private BoundingBox  bounds     = null;
    private long         tileCount  = -1;
    private long         storeSize  = -1;
}