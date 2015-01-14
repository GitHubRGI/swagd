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
     * @param constraintName
     *             Case sensitive name of constraint
     * @param constraintType
     *             Lowercase type name of constraint: "range", "enum", or "glob"
     * @param value
     *             Specified case sensitive value for enum or glob or <code>null</code> for range constraintType
     * @param minimum
     *             Minimum value for "range" or <code>null</code> for "enum" or "glob" constraintType
     * @param minimumIsInclusive
     *             <code>false</code> if minimum value is exclusive, or <code>true</code> if minimum value is inclusive
     * @param maximum
     *             Maximum value for "range" or <code>null</code> for "enum" or "glob" constraintType
     * @param maximumIsInclusive
     *             <code>false</code> if maximum value is exclusive, or <code>true</code> if maximum value is inclusive
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
