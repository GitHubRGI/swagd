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

package com.rgi.suite.tilestoreadapter.geopackage;

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

            this.scrollPane = new JScrollPane(this.tileSets);

            //this.scrollPane.setSize(220, 50);

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
