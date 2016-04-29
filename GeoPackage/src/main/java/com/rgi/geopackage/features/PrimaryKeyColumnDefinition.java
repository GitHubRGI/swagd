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
 */
public class PrimaryKeyColumnDefinition extends AbstractColumnDefinition
{
    /**
     * Constructor
     *
     * @param name
     *             Primary key column name
     */
    protected PrimaryKeyColumnDefinition(final String name)
    {
        this(name, defaultComment);
    }

    /**
     * Constructor
     *
     * @param name
     *             Primary key column name
     * @param comment
     *             Comment to be added to the table definition. Ignored if null.
     */
    protected PrimaryKeyColumnDefinition(final String name,
                                         final String comment)
    {
        super(name,
              type.toString(),
              flags,
              checkExpression,
              defaultValue,
              comment);
    }

    private static final SqlType             type            = SqlType.INTEGER;
    private static final EnumSet<ColumnFlag> flags           = EnumSet.of(ColumnFlag.PrimaryKey, ColumnFlag.AutoIncrement, ColumnFlag.NotNull);   // Specifying unique is unnecessary as primary keys are assumed to be unique
    private static final String              checkExpression = null;
    private static final ColumnDefault       defaultValue    = ColumnDefault.None;
    private static final String              defaultComment  = "Autoincrement primary key";
}
