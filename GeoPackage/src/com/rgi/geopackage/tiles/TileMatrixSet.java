/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.geopackage.tiles;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.core.SpatialReferenceSystem;

/**
 * @author Luke Lambert
 *
 */
public class TileMatrixSet
{
    /**
     * Constructor
     *
     * @param tableName
     *            the name of the Tiles Table that this TileMatrixSet
     *            corresponds to
     * @param spatialReferenceSystem
     *            the Spatial Reference System the tiles are projected to
     * @param boundingBox
     *            the minimum bounding box of the tile data
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
