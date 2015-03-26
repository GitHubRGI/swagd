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

package com.rgi.suite.tilestoreadapter.rawimage;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

public class RawImageTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    private final JComboBox<CoordinateReferenceSystem> referenceSystems = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                           .stream()
                                                                                                           .sorted()
                                                                                                           .toArray(CoordinateReferenceSystem[]::new));

    private final JLabel nativeReferenceSystem = new JLabel();

    private final Collection<Collection<JComponent>> readerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Native reference system:"),  this.nativeReferenceSystem),
                                                                                             Arrays.asList(new JLabel("Output reference system::"), this.referenceSystems));

    public RawImageTileStoreReaderAdapter(final File file, final boolean forceInput)
    {
        super(file, false);

        // TODO
        //CoordinateReferenceSystem crs = GdalUtility.getCRS(file);
        //this.nativeReferenceSystem.setText(crs.toString());
        //this.referenceSystems.setSelectedItem(crs);   // TODO double check that selecting an equivalent object (like this) works, rather than looking up the matching object in the combobox, and then selecting that
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return this.readerParameterControls;
    }

    @Override
    public boolean needsInput()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        // TODO
        //return new RawImageTileStoreReader(this.referenceSystems.getSelectedItem());
        return null;
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return Arrays.asList(this.getTileStoreReader());
    }
}
