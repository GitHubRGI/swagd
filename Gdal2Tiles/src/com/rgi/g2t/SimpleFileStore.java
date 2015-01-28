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

package com.rgi.g2t;

import java.io.File;

import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.profile.TileProfile;

/**
 * @author Duff Means
 * @author Luke Lambert
 *
 */
abstract class SimpleFileStore
{
    protected final File        rootFolder;
    protected final TileProfile tileProfile;
    protected final TileOrigin  tileOrigin;
    protected final String      imageFormat;

    public SimpleFileStore(final String name, final TileProfile tileProfile, final TileOrigin tileOrigin, final String location, final String imageFormat)
    {
        if(!"png".equalsIgnoreCase(imageFormat) && !"jpg".equalsIgnoreCase(imageFormat))
        {
            throw new IllegalArgumentException("Only PNG and JPG formats supported");
        }

        this.imageFormat = imageFormat;
        this.tileProfile = tileProfile;
        this.tileOrigin  = tileOrigin;

        String filePart = name.replaceFirst("[.][^.]+$", "");   // Remove any ".<extension>"

        this.rootFolder = new File(new File(location, Integer.toString(this.tileProfile.getCoordinateReferenceSystem().getIdentifier())), filePart);
    }
}
