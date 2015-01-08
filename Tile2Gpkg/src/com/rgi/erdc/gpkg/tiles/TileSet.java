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

package com.rgi.erdc.gpkg.tiles;

import java.util.Date;

import com.rgi.erdc.BoundingBox;
import com.rgi.erdc.gpkg.core.Content;
import com.rgi.erdc.gpkg.core.SpatialReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class TileSet extends Content
{
    /**
     * Constructor
     *
     * @param tableName
     *             The name of the tiles, feature, or extension specific content table
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     */
    protected TileSet(final String                 tableName,
                      final String                 identifier,
                      final String                 description,
                      final Date                   lastChange,
                      final BoundingBox            boundingBox,
                      final SpatialReferenceSystem spatialReferenceSystem)
    {
        super(tableName,
              TileSet.TileContentType,
              identifier,
              description,
              lastChange,
              boundingBox,
              spatialReferenceSystem);
    }

    public static final String TileContentType = "tiles";
}
