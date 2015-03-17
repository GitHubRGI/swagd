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

package com.rgi.suite.tilestoreadapter;

import java.io.File;
import java.util.Collection;

import javax.swing.JComponent;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;

/**
 * @author Luke Lambert
 *
 */
public abstract class TileStoreReaderAdapter
{
    public TileStoreReaderAdapter(final File file)
    {
        if(file == null)
        {
            throw new IllegalArgumentException("File may not be null");
        }

        this.file = file;
    }

    public abstract Collection<Collection<JComponent>> getReaderParameterControls();

    public abstract TileStoreReader getTileStoreReader() throws TileStoreException;

    protected final File file;
}
