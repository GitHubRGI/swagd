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
public class GeometryColumnDefinition extends AbstractColumnDefinition
{
    /**
     * @param name
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
     * @param zRequirement
     *            Restrictions on the presence of a 'z' value
     * @param mRequirement
     *            Restrictions on the presence of an 'm' value
     * @param comment
     *            Human readable comment appended to the column line that can be viewed in some database editors
     */
    public GeometryColumnDefinition(final String           name,
                                    final String           geometryType,
                                    final ValueRequirement zRequirement,
                                    final ValueRequirement mRequirement,
                                    final String           comment)
    {
        super(name,
              geometryType,
              null,              // no additional flags
              null,              // no checks - values are binaries of the form 'StandardGeoPackageBinary'. Writing a check against them would be really complicated without custom SQLite functions
              ColumnDefault.None,
              comment);

        if(zRequirement == null)
        {
            throw new IllegalArgumentException("Z requirement may not be null");
        }

        if(mRequirement == null)
        {
            throw new IllegalArgumentException("M requirement may not be null");
        }

        this.zRequirement = zRequirement;
        this.mRequirement = mRequirement;
    }

    /**
     * @return the zRequirement
     */
    @SuppressWarnings("SuspiciousGetterSetter") // the inspection complains about ZRequirement vs zRequirement
    public ValueRequirement getZRequirement()
    {
        return this.zRequirement;
    }

    /**
     * @return the mRequirement
     */
    @SuppressWarnings("SuspiciousGetterSetter") // the inspection complains about MRequirement vs mRequirement
    public ValueRequirement getMRequirement()
    {
        return this.mRequirement;
    }

    private final ValueRequirement zRequirement;
    private final ValueRequirement mRequirement;
}
