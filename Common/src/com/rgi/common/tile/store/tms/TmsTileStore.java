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

package com.rgi.common.tile.store.tms;

import java.nio.file.Path;

import com.rgi.common.coordinates.referencesystem.profile.CrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;

/**
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
abstract class TmsTileStore
{
    /**
     * Constructor
     *
     * @param profile
     *            The tile profile this tile store is using.
     * @param location
     *            The location of this tile store on-disk.
     */
    protected TmsTileStore(final CrsProfile profile, final Path location)
    {
        if(profile == null)
        {
            throw new IllegalArgumentException("Tile profile cannot be null");
        }

        if(location == null)
        {
            throw new IllegalArgumentException("Tile could not be retreived from this store");
        }

        if(!location.toFile().isDirectory())
        {
            throw new IllegalArgumentException("Location must specify a directory");
        }

        this.profile  = profile;
        this.location = location;

        this.tileScheme = new ZoomTimesTwo(0, 31, 1, 1, TmsTileStore.Origin);
    }

    protected static Path tmsPath(final Path path, final int... tmsSubDirectories)
    {
        // TODO use Stream.collect ?
        Path newPath = path.normalize();
        for(final int tmsSubdirectory : tmsSubDirectories)
        {
            newPath = newPath.resolve(String.valueOf(tmsSubdirectory));
        }
        return newPath;
    }

    protected final CrsProfile profile;
    protected final Path       location;
    protected final TileScheme tileScheme;

    protected static TileOrigin Origin = TileOrigin.LowerLeft;
}
