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

import java.nio.file.Path;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;

/**
 * Abstract base class for a tile store reader and writer of the <a
 * href="http://wiki.osgeo.org/wiki/Tile_Map_Service_Specification">TMS</a>
 * tiling convention.
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
abstract class TmsTileStore
{
    /**
     * Constructor
     *
     * @param coordinateReferenceSystem
     *             The coordinate reference system of this tile store
     * @param location
     *             The location of this tile store on-disk
     */
    protected TmsTileStore(final CoordinateReferenceSystem coordinateReferenceSystem, final Path location)
    {
        if(coordinateReferenceSystem == null)
        {
            throw new IllegalArgumentException("Coordinate reference may not be null");
        }

        if(location == null)
        {
            throw new IllegalArgumentException("Tile could not be retreived from this store");
        }

        if(!location.toFile().isDirectory())
        {
            if(!location.toFile().mkdirs())
            {
                throw new RuntimeException("Unable to create directory: " + location.toFile().getAbsolutePath());
            }
        }

        this.profile  = CrsProfileFactory.create(coordinateReferenceSystem);
        this.location = location;

        this.tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
    }

    public void close()
    {
        // Nothing to do here.  This method exists for child classes that need to implement AutoClosable
    }

    public String getName()
    {
        return this.location.toFile().getName();
    }


    @SuppressWarnings("static-method") // Function must be seen as an override in child implementations that implement TileStoreReader/TileStoreWriter
    public TileOrigin getTileOrigin()
    {
        return Origin;
    }

    public TileScheme getTileScheme()
    {
        return this.tileScheme;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.profile.getCoordinateReferenceSystem();
    }

    public CrsCoordinate tileToCrsCoordinate(final int column, final int row, final int zoomLevel, final TileOrigin corner)
    {
        if(corner == null)
        {
            throw new IllegalArgumentException("Corner may not be null");
        }

        return this.profile.tileToCrsCoordinate(column + corner.getHorizontal(),
                                                row    + corner.getVertical(),
                                                this.profile.getBounds(),    // TMS uses absolute tiling, which covers the whole globe
                                                this.tileScheme.dimensions(zoomLevel),
                                                TmsTileStore.Origin);
    }

    public BoundingBox getTileBoundingBox(final int column, final int row, final int zoomLevel)
    {
        return this.profile.getTileBounds(column,
                                          row,
                                          this.profile.getBounds(),
                                          this.tileScheme.dimensions(zoomLevel),
                                          TmsTileStore.Origin);
    }

    protected static Path tmsPath(final Path path, final int... tmsSubDirectories)
    {
        // TODO use Stream.collect ?
        Path newPath = path.normalize();
        for(final int tmsSubdirectory : tmsSubDirectories)
        {
            newPath = newPath.resolve(String.valueOf(tmsSubdirectory));
        }
        return newPath;
    }

    protected final CrsProfile profile;
    protected final Path       location;
    protected final TileScheme tileScheme;

    public static final TileOrigin Origin = TileOrigin.LowerLeft;
}
