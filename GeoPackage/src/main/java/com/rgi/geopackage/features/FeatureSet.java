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

package com.rgi.geopackage.features;

import com.rgi.geopackage.core.Content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Luke Lambert
 *
 */
public class FeatureSet extends Content
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
     * @param minimumX
     *             Bounding box minimum easting or longitude for all content
     * @param minimumY
     *             Bounding box maximum easting or longitude for all content
     * @param maximumX
     *             Bounding box minimum northing or latitude for all content
     * @param maximumY
     *             Bounding box maximum northing or latitude for all content
     * @param spatialReferenceSystemIdentifier
     *             Spatial Reference System (SRS)
     * @param primaryKeyColumnName
     *             Column name of the primary key
     * @param geometryColumnName
     *             Column name of the geometry attribute
     * @param attributeColumnNames
     *             Column names of the other attributes
     */
    protected FeatureSet(final String             tableName,
                         final String             identifier,
                         final String             description,
                         final String             lastChange,
                         final Double             minimumX,
                         final Double             minimumY,
                         final Double             maximumX,
                         final Double             maximumY,
                         final int                spatialReferenceSystemIdentifier,
                         final String             primaryKeyColumnName,
                         final String             geometryColumnName,
                         final Collection<String> attributeColumnNames)
    {
        super(tableName,
              FeatureSet.FeatureContentType,
              identifier,
              description,
              lastChange,
              minimumX,
              minimumY,
              maximumX,
              maximumY,
              spatialReferenceSystemIdentifier);

        if(primaryKeyColumnName == null || primaryKeyColumnName.isEmpty())
        {
            throw new IllegalArgumentException("Primary key column name may not be null or empty");
        }

        if(geometryColumnName == null || geometryColumnName.isEmpty())
        {
            throw new IllegalArgumentException("Geometry key column name may not be null or empty");
        }

        this.primaryKeyColumnName = primaryKeyColumnName;
        this.geometryColumnName   = geometryColumnName;
        this.attributeColumnNames = new ArrayList<>(attributeColumnNames == null ? Collections.emptyList()
                                                                                 : attributeColumnNames);
    }

    public String getPrimaryKeyColumnName()
    {
        return this.primaryKeyColumnName;
    }

    public String getGeometryColumnName()
    {
        return this.geometryColumnName;
    }

    public Collection<String> getAttributeColumnNames()
    {
        return Collections.unmodifiableCollection(this.attributeColumnNames);
    }

    /**
     * The data type "features"
     */
    public static final String FeatureContentType = "features";

    private final String             primaryKeyColumnName;
    private final String             geometryColumnName;
    private final Collection<String> attributeColumnNames;

}
