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

package com.rgi.android.common.coordinate;

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
