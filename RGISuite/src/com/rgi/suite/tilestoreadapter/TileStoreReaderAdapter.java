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
import java.util.Collection;

import javax.swing.JComponent;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * Abstract base class for UI adapters for tile store readers
 *
 * @author Luke Lambert
 *
 */
public abstract class TileStoreReaderAdapter
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
    public TileStoreReaderAdapter(final File file, final boolean allowMultipleReaders)
    {
        if(file == null)
        {
            throw new IllegalArgumentException("File may not be null");
        }

        this.file                 = file;
        this.allowMultipleReaders = allowMultipleReaders;
    }

    /**
     * @return the file
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * Provides UI elements to use as input to construct a tile store reader
     *
     * @return Returns a matrix of UI elements to build an input form that will
     *             provide the inputs to build a corresponding tile store
     *             reader
     */
    public abstract Collection<Collection<JComponent>> getReaderParameterControls();

    /**
     * In some scenarios (e.g. viewing) we want to be able to use input
     * defaults to construct a tile store reader without using a UI
     *
     * @return <code>true</code> if the corresponding tile store reader has
     *             mandatory inputs that cannot be automatically inferred
     */
    public abstract boolean needsInput();

    /**
     * Constructs a tile store reader based on the values of the UI elements
     *
     * @return A {@link TileStoreReader}
     * @throws TileStoreException
     *             if construction of the tile store reader fails
     */
    public abstract TileStoreReader getTileStoreReader() throws TileStoreException;

    /**
     * Some file types can contain more than one tile set. This methods allows
     * for the construction of a tile store reader for each tile set.
     *
     * @return A collection of {@link TileStoreReader}s
     * @throws TileStoreException
     *             if construction of a tile store reader fails
     */
    public abstract Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException;

    protected final File    file;
    protected final boolean allowMultipleReaders;
}
