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

package utility;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import store.GeoPackageReader;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.tms.TmsReader;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.ConformanceException;

/**
 * Common tile store utilities
 *
 * @author Luke Lambert
 *
 */
public class TileStoreUtility
{
    /**
     * Describes the traits of a tile store without having to construct one.
     *
     * @author Luke Lambert
     *
     */
    public static class TileStoreTraits
    {
        /**
         * Constructor
         *
         * @param knowsCrs
         *         Indicates if the referenced {@link TileStoreReader} contains
         *         metadata specifying its coordinate reference system.
         */
        public TileStoreTraits(final boolean knowsCrs)
        {
            this.knowsCrs = knowsCrs;
        }

        /**
         * @return the knowsCrs
         */
        public boolean knowsCrs()
        {
            return this.knowsCrs;
        }

        private final boolean knowsCrs;
    }

    /**
     * Describes the traits {@link File} represents a tile store
     *
     * @param file
     *             File or directory that may represents a tile store
     * @return Returns a {@link TileStoreTraits} object, or null if the file
     *             isn't a recognized tile store
     */
    public static TileStoreTraits getTraits(final File file)
    {
        if(isTms(file))
        {
            return TmsTraits;
        }
        else if(isGeoPackage(file))
        {
            return GeoPackageTraits;
        }

        return null; // file is not a recognized tile store
    }

    /**
     * @param files
     *             Files containing one or more tile stores
     * @param coordinateReferenceSystem
     *             Hint to tile stores that don't contain metadata indicating
     *             what coordinate reference system they're in.  Ignored if the
     *             underlying store knows its own coordinate reference system.
     * @return A {@link Collection} of {@link TileStoreReader}s.
     */
    public static Collection<TileStoreReader> getStores(final CoordinateReferenceSystem coordinateReferenceSystem, final File... files)
    {
        final List<TileStoreReader> readers = new LinkedList<>();

        for(final File file : files)
        {
            readers.addAll(getStores(coordinateReferenceSystem, file));
        }

        return readers;
    }

    @SuppressWarnings("resource")
    private static Collection<TileStoreReader> getStores(final CoordinateReferenceSystem coordinateReferenceSystem, final File file)
    {
        if(file != null && file.canRead())
        {
            if(isTms(file)) // TODO: do we need to do some verification that this folder structure is actually TMS?
            {
                return Arrays.asList(new TmsReader(coordinateReferenceSystem, file.toPath()));
            }

            if(isGeoPackage(file))
            {
                try(final GeoPackage gpkg = new GeoPackage(file, OpenMode.Open))
                {
                   return gpkg.tiles()
                              .getTileSets()
                              .stream()
                              .map(tileSet -> { try
                                                {
                                                    return new GeoPackageReader(file, tileSet.getTableName());
                                                }
                                                catch(final ClassNotFoundException | SQLException | ConformanceException | IOException ex)
                                                {
                                                    ex.printStackTrace();
                                                    return null;
                                                }
                                              })
                              .filter(Objects::nonNull)
                              .collect(Collectors.toCollection(ArrayList<TileStoreReader>::new));
                }
                catch(final ClassNotFoundException | SQLException | ConformanceException | IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return Collections.emptyList();
    }

    private static boolean isTms(final File file)
    {
        return file.isDirectory(); // TODO: do we need to do some verification that this folder structure is actually TMS?
    }

    private static boolean isGeoPackage(final File file)
    {
        return file.getName().toLowerCase().endsWith(".gpkg"); // TODO: should this operate on something other than file extension?
    }

    private static final TileStoreTraits TmsTraits        = new TileStoreTraits(false);
    private static final TileStoreTraits GeoPackageTraits = new TileStoreTraits(true);
}
