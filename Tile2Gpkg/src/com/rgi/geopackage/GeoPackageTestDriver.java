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

package com.rgi.geopackage;

import java.io.File;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinates.CrsTileCoordinate;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;

/**
 * A simple driver to test changes of the GeoPackage API implementation.  Not intended for release or production use.
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageTestDriver
{
    public static void main(final String[] args)
    {
        final File file = new File("test.gpkg");
        if(file.exists())
        {
            if(!file.delete())
            {
                throw new RuntimeException("Unable to delete old gpkg");
            }
        }

        try
        {
            try(GeoPackage gpkg = new GeoPackage(file))
            {
                final TileSet tileSet = gpkg.tiles().addTileSet("foo",
                                                          "foo",
                                                          "foo",
                                                          new BoundingBox(-90.0, -180.0, 90.0, 180.0),
                                                          gpkg.core().getSpatialReferenceSystem(4326));

                final TileMatrix tileMatrix = gpkg.tiles().addTileMatrix(tileSet,
                                                                   0,
                                                                   1,
                                                                   1,
                                                                   1,
                                                                   1,
                                                                   360.0,
                                                                   180.0);

                gpkg.tiles().addTile(tileSet,
                                     tileMatrix,
                                     new CrsTileCoordinate(-90.0, -180.0, 0, "EPSG", 4326),
                                     new byte[] {});
            }
        }
        catch(final Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
