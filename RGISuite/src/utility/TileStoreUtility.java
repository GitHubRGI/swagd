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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import store.GeoPackageReader;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.tms.TmsReader;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.ConformanceException;

public class TileStoreUtility
{
    public static Collection<TileStoreReader> getStores(final File file, final CoordinateReferenceSystem inputCoordinateReferenceSystem) throws FileNotFoundException, TileStoreException
    {
        if(file == null)
        {
            throw new IllegalArgumentException("File may not be null");
        }

        if(!file.exists())
        {
            throw new FileNotFoundException();
        }

        if(file.isDirectory()) // TODO: do we need to do some verification that this folder structure is actually TMS?
        {
            return Arrays.asList(new TmsReader(inputCoordinateReferenceSystem, file.toPath()));
        }

        if(file.getName().toLowerCase().endsWith(".gpkg"))
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
                                                return null;
                                            }
                                          })
                          .filter(Objects::nonNull)
                          .collect(Collectors.toCollection(ArrayList<TileStoreReader>::new));
            }
            catch(final ClassNotFoundException | SQLException | ConformanceException | IOException ex)
            {
                throw new TileStoreException(ex);
            }
        }

        throw new TileStoreException("Unrecognized file store type");
    }
}
