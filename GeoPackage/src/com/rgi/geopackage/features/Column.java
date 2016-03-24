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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Luke Lambert
 *
 * TODO collapse this into the verifier's ColumnDefinition
 */
public class Column
{
    /**
     * Constructor
     *
     * @param name
     *             Column name
     * @param type
     *             Column type
     * @param checkExpression
     *             SQLite "check" expression. Ignored if null.  Each time a new
     *             row is inserted into the table or an existing row is
     *             updated, the expression associated with each CHECK
     *             constraint is evaluated and cast to a NUMERIC value in the
     *             same way as a CAST expression. If the result is zero
     *             (integer value 0 or real value 0.0), then a constraint
     *             violation has occurred. If the CHECK expression evaluates
     *             to NULL, or any other non-zero value, it is not a constraint
     *             violation. The expression of a CHECK constraint may not
     *             contain a subquery.
     * @param flags
     *             Column constraint flags
     * @param defaultValue
     *             Column default value. Ignored if null.
     */
    protected Column(final String              name,
                     final String              type,
                     final EnumSet<ColumnFlag> flags,
                     final String              checkExpression,
                     final String              defaultValue)
    {
        if(name == null)
        {
            throw new IllegalArgumentException("");
        }

        if(type == null)
        {
            throw new IllegalArgumentException("");
        }

        this.flags = flags == null ? EnumSet.noneOf(ColumnFlag.class)
                                   : flags;

        this.name            = name;
        this.type            = type;
        this.checkExpression = checkExpression == null ? "" : checkExpression;
        this.defaultValue    = defaultValue;
    }

    public String getName()
    {
        return this.name;
    }

    public String getType()
    {
        return this.type;
    }

    public Set<ColumnFlag> getFlags()
    {
        return Collections.unmodifiableSet(this.flags);
    }

    public String getCheckExpression()
    {
        return this.checkExpression;
    }

    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    private final String              name;
    private final String              type;
    private final EnumSet<ColumnFlag> flags;
    private final String              checkExpression;  // TODO don't keep this if it can't be queried for
    private final String              defaultValue;
}
