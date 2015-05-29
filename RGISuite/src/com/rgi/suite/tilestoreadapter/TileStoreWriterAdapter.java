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

import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.TileStoreWriter;
import com.rgi.suite.Settings;

/**
 * Abstract base class for UI adapters for tile store writers
 *
 * @author Luke Lambert
 *
 */
public abstract class TileStoreWriterAdapter
{
    /**
     * Constructor
     *
     * @param settings
     *             Handle to the application's settings object
     */
    public TileStoreWriterAdapter(final Settings settings)
    {
        this.settings = settings;
    }

    /**
     * Use an input file to hint at possible values for the UI elements
     *
     * @param inputFile
     *             Input file
     * @throws TileStoreException
     *             if there's a problem with the tile store
     */
    public abstract void hint(final File inputFile) throws TileStoreException;

    /**
     * Provides UI elements to use as input to construct a tile store writer
     *
     * @return Returns a matrix of UI elements to build an input form that will
     *             provide the inputs to build a corresponding tile store
     *             writer
     */
    public abstract Collection<Collection<JComponent>> getWriterParameterControls();

    /**
     * Constructs a tile store writer based on the values of the UI elements
     *
     * @param tileStoreReader
     *             Tile store reader that may be required to know how to build
     *             a tile scheme for our new tile store writer
     * @return A {@link TileStoreWriter}
     * @throws TileStoreException
     *             if construction of the tile store reader fails
     */
    public abstract TileStoreWriter getTileStoreWriter(final TileStoreReader tileStoreReader) throws TileStoreException;

    /**
     * In the case of a failed tile store operation (e.g. packaging, or tiling)
     * call this method to clean up the process started by calling {@link
     * #getTileStoreWriter(TileStoreReader)}
     *
     * @throws TileStoreException
     *             if tile store removal fails
     */
    public abstract void removeStore() throws TileStoreException;

    protected final Settings settings;
}
