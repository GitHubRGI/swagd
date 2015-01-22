package com.rgi.common.util.jdbc;

import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.stream.Stream;

import org.junit.Test;

import com.mockrunner.mock.jdbc.MockResultSet;

public class ResultSetStreamTest {

	@Test
	public void ResultSetStreamGetStream() {
		MockResultSet resultSet = new MockResultSet("myMock");
		resultSet.addColumn("columnA", new Integer[]{1});
		Stream<ResultSet> resultSetStream = null;
		resultSetStream = ResultSetStream.getStream(resultSet);
		assertTrue(resultSetStream instanceof Stream<?>);
	}
}
