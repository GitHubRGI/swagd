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

import java.sql.ResultSet;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A {@link Stream} wrapper for a {@link ResultSet}
 *
 * @author Luke Lambert
 *
 */
public class ResultSetStream
{
    /**
     * Creates a stream from a {@link ResultSet}
     *
     * @param resultSet
     *             A {@link ResultSet}
     * @return A {@link Stream} of {@link ResultSet}s
     */
    public static Stream<ResultSet> getStream(final ResultSet resultSet)
    {
        return ResultSetStream.getStream(resultSet, rs -> rs);
    }

    /**
     * @param resultSet
     *             A {@link ResultSet}
     * @param mappingFunction
     *             A mapping function that transforms a result set instance to
     *             another derived value
     * @return A {@link Stream} of values derived from a {@link ResultSet}
     */
    public static <T> Stream<T> getStream(final ResultSet resultSet, final Function<ResultSet, T> mappingFunction)
    {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ResultSetIterator<>(resultSet, mappingFunction),
                                                                        0), // No particular spliterator characteristics can be relied on (http://docs.oracle.com/javase/8/docs/api/java/util/Spliterator.html#characteristics--)
                                    false);
    }
}
