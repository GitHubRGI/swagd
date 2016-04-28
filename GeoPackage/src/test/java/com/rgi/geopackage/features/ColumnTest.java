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
import static org.junit.Assert.fail;

/**
 * @author Luke Lambert
 */
public class ColumnTest
{
    /**
     * Test constructor for failure on null name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullName()
    {
        new Column(null,
                   "type",
                   EnumSet.noneOf(ColumnFlag.class),
                   "",
                   "");

        fail("Constructor should fail on null name");
    }

    /**
     * Test constructor for failure on empty name
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyName()
    {
        new Column("",
                   "type",
                   EnumSet.noneOf(ColumnFlag.class),
                   "",
                   "");

        fail("Constructor should fail on empty name");
    }

    /**
     * Test constructor for failure on null type
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNullType()
    {
        new Column("name",
                   null,
                   EnumSet.noneOf(ColumnFlag.class),
                   "",
                   "");

        fail("Constructor should fail on null type");
    }

    /**
     * Test constructor for failure on empty type
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyType()
    {
        new Column("name",
                   "",
                   EnumSet.noneOf(ColumnFlag.class),
                   "",
                   "");

        fail("Constructor should fail on empty type");
    }

    /**
     * Test constructor for correct EnumSet for ColumnFlag on null parameter
     */
    @Test
    public void constructorEnumSetForColumnFlag()
    {
        final Column column = new Column("name",
                                         "int",
                                         null,
                                         "",
                                         "");

        assertEquals("A null enum set for column flags should result in a non-null empty EnumSet",
                     EnumSet.noneOf(ColumnFlag.class),
                     column.getFlags());
    }

    /**
     * Test getName
     */
    @Test
    public void getName()
    {
        final String name = "name";

        final Column column = new Column(name,
                                         "int",
                                         null,
                                         "",
                                         "");

        assertEquals("getName() returned an incorrect value",
                     name,
                     column.getName());
    }

    /**
     * Test getType
     */
    @Test
    public void getType()
    {
        final String type = "INTEGER";

        final Column column = new Column("name",
                                         type,
                                         null,
                                         "",
                                         "");

        assertEquals("getType() returned an incorrect value",
                     type,
                     column.getType());
    }

    /**
     * Test getFlags
     */
    @Test
    public void getFlags()
    {
        final EnumSet<ColumnFlag> flags = EnumSet.allOf(ColumnFlag.class);

        final Column column = new Column("name",
                                         "INTEGER",
                                         flags,
                                         "",
                                         "");

        assertEquals("getFlags() returned an incorrect value",
                     flags,
                     column.getFlags());
    }

    /**
     * Test getCheckExpression
     */
    @Test
    public void getCheckExpresion()
    {
        final String expression = "expression";

        final Column column = new Column("name",
                                         "INTEGER",
                                         null,
                                         expression,
                                         "");

        assertEquals("getCheckExpression() returned an incorrect value",
                     expression,
                     column.getCheckExpression());
    }

    /**
     * Test getDefaultValue
     */
    @Test
    public void getDefaultValue()
    {
        final String value = "value";

        final Column column = new Column("name",
                                         "INTEGER",
                                         null,
                                         "",
                                         value);

        assertEquals("getDefaultValue() returned an incorrect value",
                     value,
                     column.getDefaultValue());
    }
}
