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
package com.rgi.common.util.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.junit.Test;

import com.mockrunner.mock.jdbc.MockResultSet;

@SuppressWarnings({ "static-method", "javadoc" })
public class ResultSetIteratorTest<T> 
{

    @Test(expected = IllegalArgumentException.class)
    public void ResultSetIteratorConstructorNullResult()
    {
        final Function<ResultSet, T> mappingFunction = null;
        @SuppressWarnings("unused")
        final ResultSetIterator<T> rsi = new ResultSetIterator<>(null, mappingFunction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ResultSetIteratorConstructorNullFunction() throws SQLException
    {
        try(MockResultSet rs = new MockResultSet("myMock"))
        {
            rs.addColumn("columnA", new Integer[] { 1 });
            @SuppressWarnings("unused")
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(rs, null);
        }
    }

    @Test
    public void ResultSetInteratorConstructor() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            @SuppressWarnings("unused")
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertFalse(resultSet.isClosed());
        }
    }

    @Test
    public void ResultSetHasNext() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertTrue(rsi.hasNext());
        }
    }

    @Test
    public void ResultSetDoesNotHaveNext() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertFalse(rsi.hasNext());
        }
    }

    @Test
    public void ResultSetDoesNotHaveNextClosed() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            resultSet.addColumn("columnA", new Integer[] { 1 });
            resultSet.close();
            assertFalse(rsi.hasNext());
        }
    }

    @Test 
    public void ResultSetDoesNotHaveNextLastItem() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            final ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            rsi.next();
            assertFalse(rsi.hasNext());
        }
    }
}
