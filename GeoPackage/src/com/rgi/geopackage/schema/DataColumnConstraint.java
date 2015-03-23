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

package com.rgi.geopackage.schema;

/**
 * GeoPackage Schema Data Column Constraint
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_data_column_constraints" type="cite">
 * The <code>gpkg_data_column_constraints</code> table contains data to specify
 * restrictions on basic data type column values. The constraint_name column is
 * referenced by the <code>constraint_name</code> column in the
 * <code>gpkg_data_columns</code> table.
 * </blockquote>
 *
 * @see <a href="http://www.geopackage.org/spec/#_data_column_constraints">OGC® GeoPackage Encoding Standard - 2.3.3. Data Column Constraints</a>
 * @see <a href="http://www.geopackage.org/spec/#gpkg_data_column_constraints_cols">OGC® GeoPackage Encoding Standard - Table 12. Data Column Constraints Table or View Definition</a>
 *
 * @author Luke Lambert
 *
 */
public class DataColumnConstraint
{
    /**
     * Constructor
     *
     * @param constraintName
     *             Case sensitive name of constraint
     * @param constraintType
     *             Lowercase type name of constraint: "range", "enum", or "glob"
     * @param value
     *             Specified case sensitive value for enum or glob or <code>null</code> for range constraintType
     * @param minimum
     *             Minimum value for "range" or <code>null</code> for "enum" or "glob" constraintType
     * @param minimumIsInclusive
     *             <code>false</code> if minimum value is exclusive, <code>true</code> if minimum value is inclusive, or <code>null</code> for "enum" or "glob" constraintType
     * @param maximum
     *             Maximum value for "range" or <code>null</code> for "enum" or "glob" constraintType
     * @param maximumIsInclusive
     *             <code>false</code> if maximum value is exclusive, <code>true</code> if maximum value is inclusive, or <code>null</code> for "enum" or "glob" constraintType
     * @param description
     *             For ranges and globs, describes the constraint; for enums, describes the enum value.
     */
    public DataColumnConstraint(final String  constraintName,
                                final String  constraintType,
                                final String  value,
                                final Number  minimum,
                                final Boolean minimumIsInclusive,
                                final Number  maximum,
                                final Boolean maximumIsInclusive,
                                final String  description)
    {
        this.constraintName     = constraintName;
        this.constraintType     = constraintType;
        this.value              = value;
        this.minimum            = minimum;
        this.minimumIsInclusive = minimumIsInclusive;
        this.maximum            = maximum;
        this.maximumIsInclusive = maximumIsInclusive;
        this.description        = description;
    }

    /**
     * @return the constraintName
     */
    public String getConstraintName()
    {
        return this.constraintName;
    }

    /**
     * @return the constraintType
     */
    public String getConstraintType()
    {
        return this.constraintType;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * @return the minimum
     */
    public Number getMinimum()
    {
        return this.minimum;
    }

    /**
     * @return the minimumIsInclusive
     */
    public Boolean getMinimumIsInclusive()
    {
        return this.minimumIsInclusive;
    }

    /**
     * @return the maximum
     */
    public Number getMaximum()
    {
        return this.maximum;
    }

    /**
     * @return the maximumIsInclusive
     */
    public Boolean getMaximumIsInclusive()
    {
        return this.maximumIsInclusive;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    private final String  constraintName;
    private final String  constraintType;
    private final String  value;
    private final Number  minimum;
    private final Boolean minimumIsInclusive;
    private final Number  maximum;
    private final Boolean maximumIsInclusive;
    private final String  description;
}
