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

import java.sql.ResultSet;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Luke Lambert
 *
 */
public class ResultSetStream
{
    public static Stream<ResultSet> getStream(final ResultSet resultSet)
    {
        return ResultSetStream.getStream(resultSet, rs -> rs);
    }

    public static <T> Stream<T> getStream(final ResultSet resultSet, final Function<ResultSet, T> mappingFunction)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ResultSetIterator<>(resultSet, mappingFunction),
                                                                        0), // No particular spliterator characteristics can be relied on (http://docs.oracle.com/javase/8/docs/api/java/util/Spliterator.html#characteristics--)
                            false);
    }
}
