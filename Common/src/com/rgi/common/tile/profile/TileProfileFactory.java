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

package com.rgi.common.tile.profile;

import com.rgi.common.CoordinateReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class TileProfileFactory
{
    public static TileProfile create(final String crsAuthority,
                                     final int    crsIdentifier)
    {
        return TileProfileFactory.create(new CoordinateReferenceSystem(crsAuthority, crsIdentifier));
    }

    public static TileProfile create(final CoordinateReferenceSystem coordinateReferenceSystem)
    {
        // TODO: This switch statement is a stop-gap.  Ideally a TileProfile
        // can be created in a general way via the details of the WTK/proj4
        // string.
        switch(coordinateReferenceSystem.toString())
        {
            case "EPSG:900913":
            case "EPSG:3785":   // deprecated by the EPSG
            case "EPSG:3857":
            case "OSGEO:41001": return new SphericalMercatorTileProfile();


            case "EPSG:3395": return new EllipsoidalMercatorTileProfile(); // AKA Global Mercator

            case "EPSG:4326": return new GlobalGeodeticTileProfile();

            //case "EPSG:9804": // Mercator (variant A)
            //case "EPSG:9805": // Mercator (variant B)
            //                  return new ScaledWorldMercatorTileProfile();

            default: throw new RuntimeException(String.format("Unsupported spatial reference system: %s", coordinateReferenceSystem));
        }
    }
}
