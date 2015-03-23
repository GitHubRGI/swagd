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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.function.Function;

/**
 * {@link Iterator} implementation for a {@link ResultSet}
 * <br>
 * Based on the example found
 * <a href="https://stackoverflow.com/a/1870090/16434">here</a>
 *
 * @author Luke Lambert
 *
 * @param <T>
 */
class ResultSetIterator<T> implements Iterator<T>
{
    /**
     * Constructor
     *
     * @param resultSet
     *             Result set to be iterated over
     * @param mappingFunction
     *             Function to map a result set instance to a derived value
     */
    protected ResultSetIterator(final ResultSet resultSet, final Function<ResultSet, T> mappingFunction)
    {
        if(resultSet == null)
        {
            throw new IllegalArgumentException("ResultSet may not be null");
        }

        if(mappingFunction == null)
        {
            throw new IllegalArgumentException("Mapping function may not be null");
        }

        this.resultSet       = resultSet;
        this.mappingFunction = mappingFunction;
    }

    @Override
    public boolean hasNext()
    {
        try
        {
            if(this.resultSet.isClosed())
            {
                return false;
            }
        }
        catch(final SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        if(!this.didNext)
        {
            try
            {
                this.hasNext = this.resultSet.next();
                this.didNext = true;
            }
            catch(final SQLException ex)
            {
                this.hasNext = false;
                throw new RuntimeException(ex);
            }

        }
        return this.hasNext;
    }

    @Override
    public T next()
    {
        if(!this.didNext)
        {
            try
            {
                this.resultSet.next();
            }
            catch(final SQLException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        this.didNext = false;
        return this.mappingFunction.apply(this.resultSet);
    }

    private final ResultSet              resultSet;
    private final Function<ResultSet, T> mappingFunction;

    private boolean hasNext = false;
    private boolean didNext = false;
}