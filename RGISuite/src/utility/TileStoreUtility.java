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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.suite.tilestoreadapter.AdapterMismatchException;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.UnknownTileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreReaderAdapter;

/**
 * Common tile store utilities
 *
 * @author Luke Lambert
 *
 */
public class TileStoreUtility
{
    /**
     * @param files
     *             Files containing one or more tile stores
     * @return A {@link Collection} of {@link TileStoreReader}s.
     */
    public static Collection<TileStoreReaderAdapter> getTileStoreReaderAdapters(final boolean allowMultipleReaders, final File... files)
    {
        return Stream.of(files)
                     .map(file -> getTileStoreReaderAdapter(allowMultipleReaders, file))
                     .collect(Collectors.toList());
    }

    public static TileStoreReaderAdapter getTileStoreReaderAdapter(final boolean allowMultipleReaders, final File file)
    {
        for(final Class<? extends TileStoreReaderAdapter> readerClass : KnownTileStoreReaderAdapters)
        {
            try
            {
                return readerClass.getConstructor(File.class, boolean.class).newInstance(file, allowMultipleReaders);
            }
            catch(final InvocationTargetException ex)
            {
                if(ex.getTargetException() instanceof AdapterMismatchException)
                {
                    continue;
                }
            }
            catch(final NoSuchMethodException | SecurityException | IllegalAccessException | InstantiationException ex)
            {
                ex.printStackTrace();
            }
        }

        return new UnknownTileStoreReaderAdapter(file, allowMultipleReaders);
    }

    private final static Collection<Class<? extends TileStoreReaderAdapter>> KnownTileStoreReaderAdapters = Arrays.asList(TmsTileStoreReaderAdapter.class, GeoPackageTileStoreReaderAdapter.class);
}
