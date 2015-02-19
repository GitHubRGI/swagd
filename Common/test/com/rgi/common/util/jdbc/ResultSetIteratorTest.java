package com.rgi.common.util.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.junit.Test;

import com.mockrunner.mock.jdbc.MockResultSet;

@SuppressWarnings({"static-method", "javadoc"})
public class ResultSetIteratorTest<T>
{

    @Test(expected = IllegalArgumentException.class)
    public void ResultSetIteratorConstructorNullResult()
    {
        Function<ResultSet, T> mappingFunction = null;
        @SuppressWarnings("unused")
        ResultSetIterator<T> rsi = new ResultSetIterator<>(null, mappingFunction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ResultSetIteratorConstructorNullFunction() throws SQLException
    {
        try(MockResultSet rs = new MockResultSet("myMock"))
        {
            rs.addColumn("columnA", new Integer[] { 1 });
            @SuppressWarnings("unused")
            ResultSetIterator<?> rsi = new ResultSetIterator<>(rs, null);
        }
    }

    @Test
    public void ResultSetInteratorConstructor() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            @SuppressWarnings("unused")
            ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertFalse(resultSet.isClosed());
        }
    }

    @Test
    public void ResultSetHasNext() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertTrue(rsi.hasNext());
        }
    }

    @Test
    public void ResultSetDoesNotHaveNext() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            assertFalse(rsi.hasNext());
        }
    }

    @Test
    public void ResultSetDoesNotHaveNextClosed() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
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
            ResultSetIterator<?> rsi = new ResultSetIterator<>(resultSet, rs -> rs);
            rsi.next();
            assertFalse(rsi.hasNext());
        }
    }
}
