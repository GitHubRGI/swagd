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

package com.rgi.geopackage.core;

import com.rgi.common.BoundingBox;

/**
 * @author Luke Lambert
 *
 */
public class Content
{
    /**
     * Constructor
     *
     * @param tableName
     *             The name of the tiles, feature, or extension specific content table
     * @param dataType
     *             Type of data stored in the table: "features" per clause Features, "tiles" per clause Tiles, or an implementer-defined value for other data tables per clause in an Extended GeoPackage.
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystemIdentifier
     *             Spatial Reference System (SRS)
     */
    protected Content(final String      tableName,
                      final String      dataType,
                      final String      identifier,
                      final String      description,
                      final String      lastChange,
                      final BoundingBox boundingBox,
                      final Integer     spatialReferenceSystemIdentifier)
    {
        this.tableName                        = tableName;
        this.dataType                         = dataType;
        this.identifier                       = identifier;
        this.description                      = description;
        this.lastChange                       = lastChange;
        this.boundingBox                      = boundingBox;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
    }

    /**
     * @return The name of the tiles, feature, or extension specific content table
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return Type of data stored in the table: "features" per clause Features, "tiles" per clause Tiles, or an implementer-defined value for other data tables per clause in an Extended GeoPackage.
     */
    public String getDataType()
    {
        return this.dataType;
    }

    /**
     * @return A human-readable identifier (e.g. short name) for the tableName content
     */
    public String getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return A human-readable description for the tableName content
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     */
    public String getLastChange()
    {
        return this.lastChange;
    }

    /**
     * @return Bounding box for all content in tableName
     */
    public BoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    /**
     * @return Spatial Reference System (SRS)
     */
    public Integer getSpatialReferenceSystemIdentifier()
    {
        return this.spatialReferenceSystemIdentifier;
    }

    /**
     * @param inTableName table name
     * @param inDataType data type
     * @param inIdentifier the identifier
     * @param inDescription the description
     * @param inBoundingBox the bounding box
     * @param inSpatialReferenceSystemIdentifier the spatial reference system identifier
     * @return returns true if the Content fields match the table name, data type, identifier, description, bounding box, and the spatial reference system identifier; otherwise returns false;
     */
    public boolean equals(final String      inTableName,
                          final String      inDataType,
                          final String      inIdentifier,
                          final String      inDescription,
                          final BoundingBox inBoundingBox,
                          final Integer     inSpatialReferenceSystemIdentifier)
    {
        return        this.tableName  .equals(inTableName)   &&
                      this.dataType   .equals(inDataType)    &&
                      this.boundingBox.equals(inBoundingBox) &&
               equals(this.identifier, inIdentifier)         &&
               equals(this.description,inDescription)        &&
               equals(this.spatialReferenceSystemIdentifier, inSpatialReferenceSystemIdentifier);
    }

    private static <T> boolean equals(final T first, final T second)
    {
        return first == null ? second == null
                             : first.equals(second);
    }

    private final String      tableName;
    private final String      dataType;
    private final String      identifier;
    private final String      description;
    private final String      lastChange;
    private final BoundingBox boundingBox;
    private final Integer     spatialReferenceSystemIdentifier;
}
