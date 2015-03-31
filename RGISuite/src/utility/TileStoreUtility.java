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
     * Creates a {@link TileStoreReaderAdapter} for each file
     *
     * @param allowMultipleReaders
     *             Allows adapters to return multiple {@link TileStoreReader}s
     *             from individual files that may contain more than one tile
     *             set
     * @param files
     *             Files representing tile stores
     * @return A {@link Collection} of {@link TileStoreReaderAdapter}s
     */
    public static Collection<TileStoreReaderAdapter> getTileStoreReaderAdapters(final boolean allowMultipleReaders, final File... files)
    {
        return Stream.of(files)
                     .map(file -> getTileStoreReaderAdapter(allowMultipleReaders, file))
                     .collect(Collectors.toList());
    }

    /**
     * Creates a {@link TileStoreReaderAdapter} for the input file
     *
     * @param allowMultipleReaders
     *             Allows adapters to return multiple {@link TileStoreReader}s
     *             from individual files that may contain more than one tile
     *             set
     * @param file
     *             File representing a tile store
     * @return A {@link TileStoreReaderAdapter}
     */
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
