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

import com.rgi.erdc.BoundingBox;
import com.rgi.erdc.gpkg.core.SpatialReferenceSystem;

public class TileMatrixSet
{
    /**
     * Constructor
     *
     * @param tableName
     * @param spatialReferenceSystem
     * @param boundingBox
     */
    protected TileMatrixSet(final String tableName, final SpatialReferenceSystem spatialReferenceSystem, final BoundingBox boundingBox)
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Table name cannot null or empty");
        }

        if(spatialReferenceSystem == null)
        {
            throw new IllegalArgumentException("Spatial reference system cannot be null");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be null");
        }

        if(boundingBox.containsNull())
        {
            throw new IllegalArgumentException("No component of the bounding box may be null");
        }

        this.tableName              = tableName;
        this.spatialReferenceSystem = spatialReferenceSystem;
        this.boundingBox            = boundingBox;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return the spatialReferenceSystem
     */
    public SpatialReferenceSystem getSpatialReferenceSystem()
    {
        return this.spatialReferenceSystem;
    }

    /**
     * @return the boundingBox
     */
    public BoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    private final String                 tableName;
    private final SpatialReferenceSystem spatialReferenceSystem;
    private final BoundingBox            boundingBox;

}
