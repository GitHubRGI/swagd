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

import java.util.EnumSet;

/**
 * @author Luke Lambert
 *
 */
public class ColumnDefinition extends AbstractColumnDefinition
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
     *             Column default value
     * @param comment
     *             Comment to be added to the table definition. Ignored if
     *             null.
     */
    public ColumnDefinition(final String              name,
                            final String              type,
                            final EnumSet<ColumnFlag> flags,
                            final String              checkExpression,
                            final ColumnDefault       defaultValue,
                            final String              comment)
    {
        super(name, type, flags, checkExpression, defaultValue, comment);

        // TODO explicitly disallow primary key/auto increment here since those belong solely to PrimaryKeyColumnDefinition?
    }
}
