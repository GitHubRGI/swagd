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

package com.rgi.suite.tilestoreadapter;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;

/**
 * Returned from TileStoreUtility when no other TileStoreReaderAdapter can be
 * found for an input file/folder.
 *
 * @author Luke Lambert
 *
 */
public class UnknownTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    /**
     * Constructor
     *
     * @param file
     *             File or folder that contains one or more tile sets
     * @param allowMultipleReaders
     *             Flag that indicates whether or not we should return more
     *             than one tile store reader if it contains one
     */
    public UnknownTileStoreReaderAdapter(final File file, final boolean allowMultipleReaders)
    {
        super(file, allowMultipleReaders);
    }

    @Override
    public boolean needsInput()
    {
        return true;
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return Arrays.asList(Arrays.asList(new JLabel(""), new JLabel("File not a recognized type of tile store")));
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        return null;
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return Collections.emptyList();
    }
}
