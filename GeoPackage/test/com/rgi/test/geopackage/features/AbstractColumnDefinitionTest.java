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

package com.rgi.test.geopackage.features;

import com.rgi.geopackage.features.AbstractColumnDefinition;
import com.rgi.geopackage.features.ColumnDefault;
import com.rgi.geopackage.features.ColumnFlag;
import org.junit.Test;

import java.util.EnumSet;

/**
 * @author Luke Lambert
 */
public class AbstractColumnDefinitionTest
{
    /**
     * Used to test protected AbstractColumnDefinition methods
     */
    private static final class ConcreteColumnDefinition extends AbstractColumnDefinition
    {
        private ConcreteColumnDefinition(final String              name,
                                         final String              type,
                                         final EnumSet<ColumnFlag> flags,
                                         final String              checkExpression,
                                         final ColumnDefault       defaultValue,
                                         final String              comment)
        {
            super(name, type, flags, checkExpression, defaultValue, comment);
        }
    }

    /**
     * AbstractColumnDefinition's constructor should fail on a null name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullName()
    {
        new ConcreteColumnDefinition(null,
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail on an empty name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyName()
    {
        new ConcreteColumnDefinition("",
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail on an invalid column name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorBadName()
    {
        new ConcreteColumnDefinition("'\";DROP all_tables;",
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should pass on a valid column name
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructorValidName()
    {
        new ConcreteColumnDefinition("a1",
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail on a null type
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullType()
    {
        new ConcreteColumnDefinition("a1",
                                     null,
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail on an empty type
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyType()
    {
        new ConcreteColumnDefinition("a1",
                                     "",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail on a null defaultValue (different from ColumnDefault.Null)
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullDefaultValue()
    {
        new ConcreteColumnDefinition("a1",
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail when the not null flag is specified, but an appropriate default value is not supplied
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNotNullDefaultValueColumnDefaultNull()
    {
        new ConcreteColumnDefinition("a1",
                                     "INTEGER",
                                     EnumSet.of(ColumnFlag.NotNull),
                                     "column",
                                     ColumnDefault.Null,
                                     "");
    }

    /**
     * AbstractColumnDefinition's constructor should fail when the not null flag is specified, but an appropriate default value is not supplied
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNotNullDefaultValueColumnDefaultNone()
    {
        new ConcreteColumnDefinition("a1",
                                     "INTEGER",
                                     EnumSet.of(ColumnFlag.NotNull),
                                     "column",
                                     ColumnDefault.None,
                                     "");
    }
}
