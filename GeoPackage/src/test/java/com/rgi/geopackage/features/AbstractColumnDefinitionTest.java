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

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    /**
     * AbstractColumnDefinition's constructor should fail when the comment field contains a newline
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorCommentContainsNewline()
    {
        new ConcreteColumnDefinition("a1",
                                     "INTEGER",
                                     EnumSet.noneOf(ColumnFlag.class),
                                     "column",
                                     ColumnDefault.None,
                                     "\n");
    }

    /**
     * getName() should return the name the column definition was constructed with
     */
    @Test
    public void getName()
    {
        final String columnName = "a1";

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition(columnName,
                                                                             "INTEGER",
                                                                             EnumSet.noneOf(ColumnFlag.class),
                                                                             "column",
                                                                             ColumnDefault.None,
                                                                             "");

        assertEquals("getName() failed to return the name the column definition was constructed with",
                     columnName,
                     column.getName());
    }

    /**
     * getType() should return the type the column definition was constructed with
     */
    @Test
    public void getType()
    {
        final String type = "INTEGER";

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             type,
                                                                             EnumSet.noneOf(ColumnFlag.class),
                                                                             "column",
                                                                             ColumnDefault.None,
                                                                             "");

        assertEquals("getType() failed to return the type the column definition was constructed with",
                     type,
                     column.getType());
    }

    /**
     * hasFlag(...) should return true for flags the column definition was constructed with
     */
    @Test
    public void testHasFlagTrue()
    {
        final ColumnFlag flag = ColumnFlag.AutoIncrement;

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             "INTEGER",
                                                                             EnumSet.of(flag),
                                                                             "column",
                                                                             ColumnDefault.None,
                                                                             "");

        assertTrue("hasFlagTrue() failed to return true for a flag the column definition was constructed with",
                   column.hasFlag(flag));
    }

    /**
     * hasFlag(...) should return false for flags the column definition was not constructed with
     */
    @Test
    public void testHasFlagFalse()
    {
        final ColumnFlag flag = ColumnFlag.AutoIncrement;

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             "INTEGER",
                                                                             EnumSet.of(flag),
                                                                             "column",
                                                                             ColumnDefault.None,
                                                                             "");
        assertTrue("hasFlag() failed to return false for flags the name the column definition was not constructed with",
                   !column.hasFlag(ColumnFlag.Unique));
    }

    /**
     * getCheckExpression() should return the check expression the column definition was constructed with
     */
    @Test
    public void getCheckExpression()
    {
        final String checkExpression = "foo";

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             "INTEGER",
                                                                             EnumSet.noneOf(ColumnFlag.class),
                                                                             checkExpression,
                                                                             ColumnDefault.None,
                                                                             "");

        assertEquals("getCheckExpression() failed to return the check expression the column definition was constructed with",
                     checkExpression,
                     column.getCheckExpression());
    }

    /**
     * getDefaultValue() should return the default value the column definition was constructed with
     */
    @Test
    public void getDefaultValue()
    {
        final ColumnDefault columnDefault = ColumnDefault.from("foo");

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             "INTEGER",
                                                                             EnumSet.noneOf(ColumnFlag.class),
                                                                             "column",
                                                                             columnDefault,
                                                                             "");

        assertEquals("getDefaultValue() failed to return the default value the column definition was constructed with",
                     columnDefault,
                     column.getDefaultValue());
    }

    /**
     * getComment() should return the comment the column definition was constructed with
     */
    @Test
    public void getComment()
    {
        final String comment = "comment";

        final ConcreteColumnDefinition column = new ConcreteColumnDefinition("a1",
                                                                             "INTEGER",
                                                                             EnumSet.noneOf(ColumnFlag.class),
                                                                             "column",
                                                                             ColumnDefault.None,
                                                                             comment);

        assertEquals("getComment() failed to return the comment the column definition was constructed with",
                     comment,
                     column.getComment());
    }
}
