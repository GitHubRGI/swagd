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

import java.nio.file.Path;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
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
            throw new IllegalArgumentException("Location must specify a directory");
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
        final Coordinate<Double> lowerLeft  = this.tileToCrsCoordinate(column, row, zoomLevel, TileOrigin.LowerLeft);
        final Coordinate<Double> upperRight = this.tileToCrsCoordinate(column, row, zoomLevel, TileOrigin.UpperRight);

        return new BoundingBox(lowerLeft.getX(),
                               lowerLeft.getY(),
                               upperRight.getX(),
                               upperRight.getY());
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

    protected static TileOrigin Origin = TileOrigin.LowerLeft;
}
