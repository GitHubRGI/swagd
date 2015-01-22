package com.rgi.common.util.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.junit.Test;

import com.mockrunner.mock.jdbc.MockResultSet;

public class ResultSetIteratorTest<T> {
	
	@Test(expected = IllegalArgumentException.class)
	public void ResultSetIteratorConstructorNullResult() {
		Function<ResultSet, T> mappingFunction = null;
		ResultSetIterator<T> rsi = new ResultSetIterator<T>(null, mappingFunction);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ResultSetIteratorConstructorNullFunction() {
		MockResultSet rs = new MockResultSet("myMock");
		rs.addColumn("columnA", new Integer[]{1});
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(rs, null);
	}
	
	@Test
	public void ResultSetInteratorConstructor() {
		MockResultSet resultSet = new MockResultSet("myMock");
		resultSet.addColumn("columnA", new Integer[]{1});
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(resultSet, rs -> rs);
		assertFalse(resultSet.isClosed());
	}
	
	@Test
	public void ResultSetHasNext() {
		MockResultSet resultSet = new MockResultSet("myMock");
		resultSet.addColumn("columnA", new Integer[]{1});
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(resultSet, rs -> rs);
		assertTrue(rsi.hasNext());
	}
	
	@Test
	public void ResultSetDoesNotHaveNext() {
		MockResultSet resultSet = new MockResultSet("myMock");
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(resultSet, rs -> rs);
		assertFalse(rsi.hasNext());
	}

	@Test
	public void ResultSetDoesNotHaveNextClosed() throws SQLException {
		MockResultSet resultSet = new MockResultSet("myMock");
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(resultSet, rs -> rs);
		resultSet.addColumn("columnA", new Integer[]{1});
		resultSet.close();
		assertFalse(rsi.hasNext());
	}
	
	@Test
	public void ResultSetDoesNotHaveNextLastItem() {
		MockResultSet resultSet = new MockResultSet("myMock");
		resultSet.addColumn("columnA", new Integer[]{1});
		ResultSetIterator<?> rsi = new ResultSetIterator<Object>(resultSet, rs -> rs);
		rsi.next();
		assertFalse(rsi.hasNext());
	}
}
