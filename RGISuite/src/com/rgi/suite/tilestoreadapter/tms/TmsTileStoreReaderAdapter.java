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

package com.rgi.suite.tilestoreadapter.tms;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.store.tiles.tms.TmsReader;
import com.rgi.suite.tilestoreadapter.AdapterMismatchException;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

/**
 * @author Luke Lambert
 *
 */
public class TmsTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    private final JComboBox<CoordinateReferenceSystem> referenceSystems = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                           .stream()
                                                                                                           .sorted()
                                                                                                           .toArray(CoordinateReferenceSystem[]::new));

    /**
     * Constructor
     *
     * @param file
     *             Folder that contains a TMS tile sets
     * @param allowMultipleReaders
     *             Flag that indicates whether or not we should return more
     *             than one tile store reader if it contains one
     * @throws AdapterMismatchException
     *             if the supplied file doesn't contain a TMS tile set
     */
    public TmsTileStoreReaderAdapter(final File file, final boolean allowMultipleReaders) throws AdapterMismatchException
    {
        super(file, allowMultipleReaders);

        this.referenceSystems.setSelectedItem(null);

        if(!file.isDirectory())
        {
            throw new AdapterMismatchException("Input file is not a directory");
        }
    }

    @Override
    public boolean needsInput()
    {
        return true;
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return Arrays.asList(Arrays.asList(new JLabel("Reference system:"),
                                           this.referenceSystems));
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        if(this.referenceSystems.getSelectedItem() == null)
        {
            throw new TileStoreException("Please select a reference system");
        }

        return new TmsReader((CoordinateReferenceSystem)this.referenceSystems.getSelectedItem(), this.file.toPath());
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return Arrays.asList(this.getTileStoreReader());
    }

}
