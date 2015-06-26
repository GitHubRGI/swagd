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
public abstract class AbstractColumnDefinition
{
    /**
     * @param name
     * @param type
     * @param nullable
     * @param unique
     * @param defaultValue
     * @param comment
     */
    public AbstractColumnDefinition(final String  name,
                                    final String  type,
                                    final boolean nullable,
                                    final boolean unique,
                                    final String  comment)
    {
        this.name     = name;
        this.type     = type;
        this.nullable = nullable;
        this.unique   = unique;
        this.comment  = comment;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @return the nullable
     */
    public boolean isNullable()
    {
        return this.nullable;
    }

    /**
     * @return the unique
     */
    public boolean isUnique()
    {
        return this.unique;
    }

    /**
     * @return the comment
     */
    public String getComment()
    {
        return this.comment;
    }

    private final String  name;
    private final String  type;
    private final boolean nullable;
    private final boolean unique;
    private final String  comment;
}
