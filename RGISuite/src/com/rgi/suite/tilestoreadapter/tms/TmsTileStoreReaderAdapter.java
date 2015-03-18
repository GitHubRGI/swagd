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

package com.rgi.suite.tilestoreadapter.tms;

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
import com.rgi.common.tile.store.tms.TmsReader;
import com.rgi.suite.tilestoreadapter.AdapterMismatchException;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

/**
 * @author Luke Lambert
 *
 */
public class TmsTileStoreReaderAdapter extends TileStoreReaderAdapter
{
    private final JComboBox<CoordinateReferenceSystem> crsComboBox = new JComboBox<>(CrsProfileFactory.getSupportedCoordinateReferenceSystems()
                                                                                                      .stream()
                                                                                                      .sorted()
                                                                                                      .toArray(CoordinateReferenceSystem[]::new));

    public TmsTileStoreReaderAdapter(final File file, final boolean allowMultipleReaders) throws AdapterMismatchException
    {
        super(file, allowMultipleReaders);

        if(!file.isDirectory())
        {
            throw new AdapterMismatchException("Input file is not a directory");
        }
    }

    @Override
    public Collection<Collection<JComponent>> getReaderParameterControls()
    {
        return Arrays.asList(Arrays.asList(new JLabel("Reference System:"),
                                           this.crsComboBox));
    }

    @Override
    public TileStoreReader getTileStoreReader()
    {
        return new TmsReader((CoordinateReferenceSystem)this.crsComboBox.getSelectedItem(), this.file.toPath());
    }

    @Override
    public Collection<TileStoreReader> getTileStoreReaders() throws TileStoreException
    {
        return Arrays.asList(this.getTileStoreReader());
    }

}
