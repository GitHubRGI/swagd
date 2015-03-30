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

package com.rgi.suite.tilestoreadapter.geopackage;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import store.GeoPackageReader;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.suite.tilestoreadapter.AdapterMismatchException;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

/**
 * {@link TileStoreReaderAdapter} for the GeoPackage tile store format
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    private class TileSetAdapter
    {
        public TileSetAdapter(final TileSet tileSet)
        {
            if(tileSet == null)
            {
                throw new IllegalArgumentException("Tile set may not be null");
            }

            this.tileSet = tileSet;
        }

        @Override
        public String toString()
        {
            return this.tileSet.getIdentifier();
        }

        /**
         * @return the tile set
         */
        public TileSet getTileSet()
        {
            return this.tileSet;
        }

        final private TileSet tileSet;
    }

    private final JList<TileSetAdapter> tileSets;
    private final JScrollPane           scrollPane;
    private final JLabel selectCount = new JLabel();

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
    public GeoPackageTileStoreReaderAdapter(final File file, final boolean allowMultipleReaders) throws AdapterMismatchException
    {
        super(file, allowMultipleReaders);

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.Fast, OpenMode.Open))
        {
            this.tileSets = new JList<>(gpkg.tiles()
                                            .getTileSets()
                                            .stream()
                                            .map(tileSet -> new TileSetAdapter(tileSet))
                                            .toArray(TileSetAdapter[]::new));

            this.tileSets.addListSelectionListener(e -> this.selectCount.setText(String.format("%d/%d selected",
                                                                                               this.tileSets.getSelectedValuesList().size(),
                                                                                               this.tileSets.getModel().getSize())));

            final int scrollLines = Math.min(4, this.tileSets.getModel().getSize());

            this.scrollPane = new JScrollPane(this.tileSets);

            this.scrollPane.setPreferredSize(new Dimension(220, scrollLines * 15));

            if(allowMultipleReaders)
            {
                this.tileSets.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                this.tileSets.setSelectionInterval(0, this.tileSets.getModel().getSize()-1);
            }
            else
            {
                this.tileSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                if(this.tileSets.getModel().getSize() > 0)
                {
                    this.tileSets.setSelectedIndex(0);
                }
            }
        }
        catch(ClassNotFoundException | SQLException | ConformanceException | IOException ex)
        {
            throw new AdapterMismatchException(ex);
        }
    }

    @Override
    public boolean needsInput()
    {
        return this.tileSets.getModel().getSize() > 1;
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        final Collection<JComponent> components = this.allowMultipleReaders ? Arrays.asList(new JLabel("Tile set(s):"), this.scrollPane, this.selectCount)
                                                                            : Arrays.asList(new JLabel("Tile set:"),    this.scrollPane);
        return Arrays.asList(components);
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        final TileSetAdapter selected = this.tileSets.getSelectedValue();

        if(selected == null)
        {
            throw new TileStoreException("No tile store selected");
        }

        return new GeoPackageReader(this.file,
                                    selected.getTileSet().getTableName(),
                                    VerificationLevel.None);    // Verification has already taken place in the constructor (with VerificationLevel.Fast)
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return this.tileSets
                   .getSelectedValuesList()
                   .stream()
                   .map(tileSetAdapter -> { try
                                            {
                                                return new GeoPackageReader(this.file,
                                                                            tileSetAdapter.getTileSet().getTableName(),
                                                                            VerificationLevel.None);    // Verification has already taken place in the constructor (with VerificationLevel.Fast)
                                            }
                                            catch(final TileStoreException ex)
                                            {
                                                ex.printStackTrace();   // TODO what should we really do here ?
                                                return null;
                                            }
                                           })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());

    }
}
