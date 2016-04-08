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

/**
 * @author Luke Lambert
 *
 */
public class GeometryColumn
{
    /**
     * @param tableName
     *            Name of the associated table
     * @param columnName
     *            Name of a column in the feature table that is a Geometry
     *            Column
     * @param geometryType
     *            Type from <a href=
     *            "http://www.geopackage.org/spec/#geometry_types_core">
     *            Geometry Type Codes (Core)</a> or <a href=
     *            "http://www.geopackage.org/spec/#geometry_types_extension">
     *            Geometry Type Codes (Extension)</a> in <a href=
     *            "http://www.geopackage.org/spec/#geometry_types">Geometry
     *            Types (Normative)</a>
     * @param spatialReferenceSystemIdentifier
     * @param zRequirement
     *            Restrictions on the presence of a 'z' value
     * @param mRequirement
     *            Restrictions on the presence of an 'm' value
     */
    protected GeometryColumn(final String           tableName,
                             final String           columnName,
                             final String           geometryType,
                             final int              spatialReferenceSystemIdentifier,
                             final ValueRequirement zRequirement,
                             final ValueRequirement mRequirement)
    {
        this.tableName                        = tableName;
        this.columnName                       = columnName;
        this.geometryType                     = geometryType;
        this.spatialReferenceSystemIdentifier = spatialReferenceSystemIdentifier;
        this.zRequirement                     = zRequirement;
        this.mRequirement                     = mRequirement;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return the columnName
     */
    public String getColumnName()
    {
        return this.columnName;
    }

    /**
     * @return the geometryType
     */
    public String getGeometryType()
    {
        return this.geometryType;
    }

    /**
     * @return the spatialReferenceSystemIdentifier
     */
    public int getSpatialReferenceSystemIdentifier()
    {
        return this.spatialReferenceSystemIdentifier;
    }

    /**
     * @return the zRequirement
     */
    public ValueRequirement getZRequirement()
    {
        return this.zRequirement;
    }

    /**
     * @return the mRequirement
     */
    public ValueRequirement getMRequirement()
    {
        return this.mRequirement;
    }

    private final String           tableName;
    private final String           columnName;
    private final String           geometryType;
    private final int              spatialReferenceSystemIdentifier;
    private final ValueRequirement zRequirement;
    private final ValueRequirement mRequirement;
}
