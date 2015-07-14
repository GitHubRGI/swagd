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
import java.util.function.Supplier;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.gdal.osr.SpatialReference;

import utility.GdalUtility;

import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

/**
 * Reader adapter for a raw image
 *
 * @author Luke Lambert
 *
 */
public class RawImageTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    private final Supplier<Dimensions<Integer>> tileDimensionsSupplier;
    private final boolean forceInput;

    private final JComboBox<CoordinateReferenceSystem> referenceSystems = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                           .stream()
                                                                                                           .sorted()
                                                                                                           .toArray(CoordinateReferenceSystem[]::new));

    private final JLabel nativeReferenceSystem = new JLabel();

    private final Collection<Collection<JComponent>> readerParameterControls = Arrays.asList(Arrays.asList(new JLabel("Native reference system:"),  this.nativeReferenceSystem),
                                                                                             Arrays.asList(new JLabel("Output reference system::"), this.referenceSystems));

    /**
     * Constructor
     *
     * @param file
     *             Image file to treat like a tile store
     * @param tileDimensionsSupplier
     *             Callback to supply a width and height for tile size
     * @param forceInput
     *             If true, {@link #needsInput} will always return true. This
     *             forces the selection of a coordinate reference system.
     */
    public RawImageTileStoreReaderAdapter(final File                          file,
                                          final Supplier<Dimensions<Integer>> tileDimensionsSupplier,
                                          final boolean                       forceInput)
    {
        super(file, false);

        this.tileDimensionsSupplier = tileDimensionsSupplier;
        this.forceInput             = forceInput;

        final SpatialReference          srs = GdalUtility.getSpatialReference(file);
        final CoordinateReferenceSystem crs = GdalUtility.getCoordinateReferenceSystem(srs);

        String crsName;

        if(crs != null)
        {
            crsName = crs.toString();
        }
        else
        {
            crsName = GdalUtility.getName(srs);
            if(crsName == null)
            {
                crsName = "<none specified>";
            }
        }

        this.nativeReferenceSystem.setText(crsName);
        this.referenceSystems.setSelectedItem(crs);
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return this.readerParameterControls;
    }

    @Override
    public boolean needsInput()
    {
        return this.forceInput || this.referenceSystems.getSelectedItem() != null;
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        return new RawImageTileReader(this.file,
                                      this.tileDimensionsSupplier.get(),
                                      null,                                 // TODO dynamic UI for getting no-data color
                                      (CoordinateReferenceSystem)this.referenceSystems.getSelectedItem());
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return Arrays.asList(this.getTileStoreReader());
    }
}
