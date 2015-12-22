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
     * @param minimumX
     *             Bounding box minimum easting or longitude for all content
     * @param maximumX
     *             Bounding box minimum northing or latitude for all content
     * @param minimumY
     *             Bounding box maximum easting or longitude for all content
     * @param maximumY
     *             Bounding box maximum northing or latitude for all content
     * @param spatialReferenceSystemIdentifier
     *             Spatial Reference System (SRS)
     */
    protected Content(final String  tableName,
                      final String  dataType,
                      final String  identifier,
                      final String  description,
                      final String  lastChange,
                      final Double  minimumX,
                      final Double  maximumX,
                      final Double  minimumY,
                      final Double  maximumY,
                      final Integer spatialReferenceSystemIdentifier)
    {
        this.tableName                        = tableName;
        this.dataType                         = dataType;
        this.identifier                       = identifier;
        this.description                      = description;
        this.lastChange                       = lastChange;
        this.minimumX                         = minimumX;
        this.maximumX                         = maximumX;
        this.minimumY                         = minimumY;
        this.maximumY                         = maximumY;
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

    public Double getMinimumX()
    {
        return this.minimumX;
    }

    public Double getMaximumX()
    {
        return this.maximumX;
    }

    public Double getMinimumY()
    {
        return this.minimumY;
    }

    public Double getMaximumY()
    {
        return this.maximumY;
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
     * @param inMinimumX
     *             Bounding box minimum easting or longitude for all content
     * @param inMaximumX
     *             Bounding box minimum northing or latitude for all content
     * @param inMinimumY
     *             Bounding box maximum easting or longitude for all content
     * @param inMaximumY
     *             Bounding box maximum northing or latitude for all content
     * @param inSpatialReferenceSystemIdentifier the spatial reference system identifier
     * @return returns true if the Content fields match the table name, data type, identifier, description, bounding box, and the spatial reference system identifier; otherwise returns false;
     */
    public boolean equals(final String  inTableName,
                          final String  inDataType,
                          final String  inIdentifier,
                          final String  inDescription,
                          final Double  inMinimumX,
                          final Double  inMaximumX,
                          final Double  inMinimumY,
                          final Double  inMaximumY,
                          final Integer inSpatialReferenceSystemIdentifier)
    {
        return this.tableName  .equals(inTableName)    &&
               this.dataType   .equals(inDataType)     &&
               equals(this.minimumX,    inMinimumX)    &&
               equals(this.maximumX,    inMaximumX)    &&
               equals(this.minimumY,    inMinimumY)    &&
               equals(this.maximumY,    inMaximumY)    &&
               equals(this.identifier,  inIdentifier)  &&
               equals(this.description, inDescription) &&
               equals(this.spatialReferenceSystemIdentifier, inSpatialReferenceSystemIdentifier);
    }

    private static <T> boolean equals(final T first, final T second)
    {
        return first == null ? second == null
                             : first.equals(second);
    }

    private final String  tableName;
    private final String  dataType;
    private final String  identifier;
    private final String  description;
    private final String  lastChange;
    private final Double  minimumX;
    private final Double  maximumX;
    private final Double  minimumY;
    private final Double  maximumY;
    private final Integer spatialReferenceSystemIdentifier;
}
