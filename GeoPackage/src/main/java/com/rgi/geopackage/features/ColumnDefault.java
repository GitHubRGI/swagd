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

import java.math.BigInteger;

/**
 * Provides a way to correctly format column default values
 *
 * @author Luke Lambert
 */
public interface ColumnDefault
{
    String sqlLiteral();

    /**
     * Creates a constant expression.
     *
     * <blockquote>
     * <p>An expression is considered constant if it does contains no sub-queries,
     * column or table references, <a href=
     * "https://www.sqlite.org/lang_expr.html#varparam">bound parameters</a>,
     * or string literals enclosed in double-quotes instead of single-quotes.</p>
     * <footer>&ndash; <a href="https://www.sqlite.org/lang_createtable.html">
     * CREATE TABLE</a> - Column Definitions</footer>
     * </blockquote>
     *
     * @param expression
     *             An SQLite constant expression
     * @return A ColumnDefault for an expression
     */
    @SuppressWarnings("ReturnOfInnerClass") // Shouldn't matter from a static method - anonymous inner class has no access/reference to the parent interface
    static ColumnDefault expression(final String expression)
    {
        return () -> String.format("(%s)", expression);
    }

    /**
     * Creates a numeric literal
     *
     * @param value
     *             Any double value
     * @return A ColumnDefault for a numeric value
     */
    @SuppressWarnings("ReturnOfInnerClass") // Shouldn't matter from a static method - anonymous inner class has no access/reference to the parent interface
    static ColumnDefault from(final int value)
    {
        return () -> Integer.toString(value);
    }

    /**
     * Creates a numeric literal
     *
     * @param value
     *             Any double value
     * @return A ColumnDefault for a numeric value
     */
    @SuppressWarnings("ReturnOfInnerClass") // Shouldn't matter from a static method - anonymous inner class has no access/reference to the parent interface
    static ColumnDefault from(final double value)
    {
        return () -> Double.toString(value);
    }

    /**
     * Creates a string literal of the form '<string>'. Single quotes in the
     * string value will be escaped with an additional single quote e.g. '
     * becomes ''
     *
     * @param value
     *             Any string
     * @return A ColumnDefault for a string
     */
    @SuppressWarnings("ReturnOfInnerClass") // Shouldn't matter from a static method - anonymous inner class has no access/reference to the parent interface
    static ColumnDefault from(final String value)
    {
        return () -> {
            // IntelliJ wanted to replace this statement with something I wasn't sure was functionally equivalent
            //noinspection DynamicRegexReplaceableByCompiledPattern
            return String.format("'%s'", value.replace("'", "''"));  // Escape single quotes with an additional single quote
        };
    }

    /**
     * Creates a blob literal in the form X'<hex digits>'
     *
     * @param value
     *             Array of bytes representing a binary blob
     * @return A ColumnDefault for a binary blob
     */
    @SuppressWarnings("ReturnOfInnerClass") // Shouldn't matter from a static method - anonymous inner class has no access/reference to the parent interface
    static ColumnDefault from(final byte[] value)
    {
        return () -> {
            // This solution was found here:
            // https://stackoverflow.com/a/943963/16434
            final BigInteger bigInt = new BigInteger(1, value);
            return String.format("X'%0" + (value.length << 1) + "X'", bigInt); // SQLite blob literals are of the form X'<hex digits>'. See "Literal Values (Constants)" at https://www.sqlite.org/lang_expr.html
        };
    }

    /**
     * No default value, so none will be specified. By default, the token NULL
     * is used by SQLite
     */
    @SuppressWarnings("ConstantDeclaredInInterface")
    ColumnDefault None = () -> "";

    /**
     * Specifies the token NULL as the default value (which is what's used if
     * no value is specified)
     */
    @SuppressWarnings("ConstantDeclaredInInterface")
    ColumnDefault Null = () -> "NULL";

    /**
     * Specifies the token CURRENT_TIME. The time is current UTC time in the
     * form "HH:MM:SS". See the heading "Column Definitions" <a href=
     * "https://www.sqlite.org/lang_createtable.html">here</a>.
     */
    @SuppressWarnings("ConstantDeclaredInInterface")
    ColumnDefault CurrentTime = () -> "CURRENT_TIME";
    /**
     * Specifies the token CURRENT_DATE. The date is the current UTC date in
     * the form "YYYY-MM-DD". See the heading "Column Definitions"
     * <a href="https://www.sqlite.org/lang_createtable.html">here</a>.
     */
    @SuppressWarnings("ConstantDeclaredInInterface")
    ColumnDefault CurrentDate = () -> "CURRENT_DATE";
    /**
     * Specifies the token CURRENT_TIMESTAMP. The date is the current UTC datetime
     * in the form "YYYY-MM-DD HH:MM:SS". See the heading "Column
     * Definitions" <a href="https://www.sqlite.org/lang_createtable.html">here
     * </a>.
     */
    @SuppressWarnings("ConstantDeclaredInInterface")
    ColumnDefault CurrentTimestamp = () -> "CURRENT_TIMESTAMP";
}
