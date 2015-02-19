/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.common.util.jdbc;

import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.junit.Test;

import com.mockrunner.mock.jdbc.MockResultSet;

@SuppressWarnings("javadoc")
public class ResultSetStreamTest {

    @Test
    public void ResultSetStreamGetStream() throws SQLException
    {
        try(MockResultSet resultSet = new MockResultSet("myMock"))
        {
            resultSet.addColumn("columnA", new Integer[] { 1 });
            Stream<ResultSet> resultSetStream = null;
            resultSetStream = ResultSetStream.getStream(resultSet);
            assertTrue(resultSetStream != null);
        }
    }
}
