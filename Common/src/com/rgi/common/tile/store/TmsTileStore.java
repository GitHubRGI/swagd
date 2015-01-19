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

package com.rgi.common.tile.store;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
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
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public class TmsTileStore implements TileStore
{
    /**
     * Constructor
     *
     * @param profile
     *            The tile profile this tile store is using.
     * @param origin
     *            The TileOrigin of this tile store is using.
     * @param location
     *            The location of this tile store on-disk.
     */
    public TmsTileStore(final TileProfile profile, final Path location)
    {
        if(profile == null)
        {
            throw new IllegalArgumentException("Tile profile cannot be null");
        }

        if(location == null)
        {
            throw new IllegalArgumentException("Tile could not be retreived from this store");
        }

        if(location.toFile().isFile())
        {
            throw new IllegalArgumentException("Location must specify a directory");
        }

        this.profile  = profile;
        this.location = location;
    }

    @Override
    public BoundingBox calculateBounds() throws TileStoreException
    {
        if(!this.location.toFile().exists())
        {
            return new BoundingBox(0.0, 0.0, 0.0, 0.0);
        }

        final int minimumZoom = this.getTmsRange(this.location.toFile()).minimum; // Get the minimum zoom level

        final Path pathToMinimumZoom = tmsPath(this.location, minimumZoom);

        final Range xRange = this.getTmsRange(pathToMinimumZoom.toFile());
        final Range yRange = this.getTmsRange(tmsPath(pathToMinimumZoom, xRange.maximum).toFile());

        // TODO attn: Lander, this logic needs to be double checked
        final Coordinate<Double> lowerLeftCorner  = this.profile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(yRange.minimum,     xRange.minimum,     minimumZoom, TmsTileStore.Origin));
        final Coordinate<Double> upperRightCorner = this.profile.absoluteToCrsCoordinate(new AbsoluteTileCoordinate(yRange.maximum + 1, xRange.maximum + 1, minimumZoom, TmsTileStore.Origin));

        return new BoundingBox(lowerLeftCorner.getY(),
                               lowerLeftCorner.getX(),
                               upperRightCorner.getY(),
                               upperRightCorner.getX());
    }

    @Override
    public long countTiles()
    {
        return this.countFiles(this.location.toFile());
    }

    @Override
    public long calculateSize() throws TileStoreException
    {
        final AtomicLong locationSize = new AtomicLong(0);
        try
        {
            Files.walkFileTree(this.location,
                               new SimpleFileVisitor<Path>()
                               {
                                   @Override
                                   public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException
                                   {
                                       if(attrs.isRegularFile())
                                       {
                                           locationSize.addAndGet(attrs.size());
                                       }
                                       return FileVisitResult.CONTINUE;
                                   }
                               });
            return locationSize.get();
        }
        catch(final IOException ex)
        {
            throw new TileStoreException("An error occurred while calculating the size of the tile store.\n" + ex.getMessage());
        }
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final AbsoluteTileCoordinate tmsCoordiante = this.profile.crsToAbsoluteTileCoordinate(coordinate, zoomLevel, TmsTileStore.Origin);

        final Optional<File> tileFile = this.getTiles(tmsCoordiante).findFirst();    // TODO prioritize list based on file type suitability (prefer transparency, etc)

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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final String outputFormat = "png";  // TODO how do we want to pick this ?

        final AbsoluteTileCoordinate tmsCoordiante = this.profile.crsToAbsoluteTileCoordinate(coordinate,
                                                                                              zoomLevel,
                                                                                              TmsTileStore.Origin);

        final Path tilePath = tmsPath(this.location,
                                      tmsCoordiante.getZoomLevel(),
                                      tmsCoordiante.getColumn()).resolve(String.format("%d.%s",
                                                                                       tmsCoordiante.getRow(),
                                                                                       outputFormat));
        try
        {
        	// Image will not write unless the directories exist leading to it.
        	if (!tilePath.getParent().toFile().exists()) {
        		(new File(tilePath.getParent().toString())).mkdirs();
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

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.profile.getCoordinateReferenceSystem();
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
        if(directory == null || !directory.isDirectory() || !directory.exists())
        {
            throw new IllegalArgumentException("Directory cannot be null, it must specify a directory, and it must exist");
        }

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

    /**
     * Counts the number of files of a certain type in an input folder.
     *
     * @param directory
     *            The folder in which the files should be counted in.
     * @return The number of files of a certain type found in the input folder.
     */
    private long countFiles(final File directory)
    {
        if(directory == null)
        {
            throw new IllegalArgumentException("Directory cannot be null");
        }

        if(!directory.exists())
        {
            return 0;
        }

        if(!directory.isDirectory())
        {
            throw new IllegalArgumentException("File specified instead of directory");
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

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        return !this.location.toFile().exists() ? Collections.emptySet()
                                                : Stream.of(this.location.toFile().listFiles())
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
                                                        .filter(integer -> integer != null)
                                                        .collect(Collectors.toSet());
    }

    private static String withoutExtension(final File file)
    {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    private static Path tmsPath(final Path path, final int... tmsSubDirectories)
    {
        // TODO use Stream.collect ?
        Path newPath = path.normalize();
        for(final int tmsSubdirectory : tmsSubDirectories)
        {
            newPath = newPath.resolve(String.valueOf(tmsSubdirectory));
        }
        return newPath;
    }

    private Stream<File> getTiles(final AbsoluteTileCoordinate coordinate)
    {
        final File[] files = tmsPath(this.location,
                                     coordinate.getZoomLevel(),
                                     coordinate.getX()).toFile()
                                                       .listFiles(); // All of the files in directory zoomLevel/x/

        return files == null ? Stream.empty()
                             : Stream.of(files)
                                     .filter(file -> { try
                                                       {
                                                           return file.isFile() &&
                                                                  withoutExtension(file).equals(String.valueOf(coordinate.getY())) &&
                                                                  fileIsImage(file);
                                                       }
                                                       catch(final Exception ex)
                                                       {
                                                           return false;
                                                       }
                                                     });
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

    private final TileProfile profile;
    private final Path        location;

    private static  TileOrigin Origin = TileOrigin.LowerLeft;
}
