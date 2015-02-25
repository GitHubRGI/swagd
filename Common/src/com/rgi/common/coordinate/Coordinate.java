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

package com.rgi.common.coordinate;

/**
 * A simple coordinate
 *
 * @author Luke Lambert
 *
 * @param <T> Extends {@link Number}
 */
public class Coordinate<T extends Number>
{
    /**
     * Constructor
     *
     * @param x
     *             Horizontal portion of the coordinate
     * @param y
     *             Vertical portion of the coordinate
     */
    public Coordinate(final T x, final T y)
    {
        if(y == null)
        {
            throw new IllegalArgumentException("Y may not be null");
        }

        if(x == null)
        {
            throw new IllegalArgumentException("X may not be null");
        }

        this.y = y;
        this.x = x;
    }

    /**
     * Constructor
     *
     * @param coordinate
     *           Coordinate to clone
     */
    public Coordinate(final Coordinate<T> coordinate)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        this.y = coordinate.getY();
        this.x = coordinate.getX();
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s)",
                             this.x,
                             this.y);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || object.getClass() != this.getClass())
        {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Coordinate<T> other = (Coordinate<T>)object;

        return this.y.equals(other.y) &&
               this.x.equals(other.x);
    }

    @Override
    public int hashCode()
    {
        return this.y.hashCode() ^ this.x.hashCode();
    }

    /**
     * @return Returns the horizontal portion of the coordinate
     */
    public T getX()
    {
        return this.x;
    }

    /**
     * @return Returns the vertical portion of the coordinate
     */
    public T getY()
    {
        return this.y;
    }

    private final T y;
    private final T x;
}
