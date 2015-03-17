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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

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

    private final JComboBox<TileSetAdapter> tileSetComboBox;

    public GeoPackageTileStoreReaderAdapter(final File file) throws AdapterMismatchException
    {
        super(file);

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.Fast, OpenMode.Open))
        {
            final TileSetAdapter[] tileSets = gpkg.tiles()
                                                  .getTileSets()
                                                  .stream()
                                                  .map(tileSet -> new TileSetAdapter(tileSet))
                                                  .toArray(TileSetAdapter[]::new);

            this.tileSetComboBox = new JComboBox<>(new DefaultComboBoxModel<>(tileSets));
        }
        catch(ClassNotFoundException | SQLException | ConformanceException | IOException ex)
        {
            throw new AdapterMismatchException(ex);
        }
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return Arrays.asList(Arrays.asList(new JLabel("Tile set:"),
                                           this.tileSetComboBox));
    }

    @Override
    public TileStoreReader getTileStoreReader() throws TileStoreException
    {
        return new GeoPackageReader(this.file,
                                    ((TileSetAdapter)this.tileSetComboBox.getSelectedItem()).getTileSet().getTableName(),
                                    VerificationLevel.None);    // Verification has already taken place in the constructor (with VerificationLevel.Fast)
    }
}
