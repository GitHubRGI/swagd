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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Based on idea found here: https://stackoverflow.com/a/1870090/16434
 *
 * @author Luke Lambert
 *
 * @param <T>
 */
class ResultSetIterator<T> implements Iterator<T>
{
    private final ResultSet              resultSet;
    private final Function<ResultSet, T> mappingFunction;

    private boolean hasNext = false;
    private boolean didNext = false;

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
}