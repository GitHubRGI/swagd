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
package com.rgi.common;

import com.rgi.common.coordinate.Coordinate;

/**
 * An immutable object representing the minimum and maximum x and y bounds of
 * an area.
 *
 * @author Luke Lambert
 */
public class BoundingBox
{
    /**
     * Constructor
     *
     * @param minimumX
     *             Minimum X
     * @param minimumY
     *             Minimum Y
     * @param maximumX
     *             Maximum X
     * @param maximumY
     *             Maximum Y
     */
    public BoundingBox(final double minimumX,
                       final double minimumY,
                       final double maximumX,
                       final double maximumY)
    {
        if(minimumX > maximumX)
        {
            throw new IllegalArgumentException("Min x cannot be greater than max x");
        }

        if(minimumY > maximumY)
        {
            throw new IllegalArgumentException("Min y cannot be greater than max y");
        }

        this.minimumX = minimumX;
        this.minimumY = minimumY;
        this.maximumX = maximumX;
        this.maximumY = maximumY;
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s, %s, %s)",
                             this.minimumX,
                             this.minimumY,
                             this.maximumX,
                             this.maximumY);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(!(obj instanceof BoundingBox))
        {
            return false;
        }

        final BoundingBox other = (BoundingBox)obj;

        //noinspection FloatingPointEquality
        return this.minimumX == other.minimumX &&
               this.minimumY == other.minimumY &&
               this.maximumX == other.maximumX &&
               this.maximumY == other.maximumY;
    }

    @Override
    public int hashCode()
    {
        return Double.valueOf(this.minimumX).hashCode() ^
               Double.valueOf(this.minimumY).hashCode() ^
               Double.valueOf(this.maximumX).hashCode() ^
               Double.valueOf(this.maximumY).hashCode();
    }

    /**
     * @return the height of the bounding box
     */
    public double getHeight()
    {
        return this.maximumY - this.minimumY;
    }

    /**
     * @return the width of the bounding box
     */
    public double getWidth()
    {
        return this.maximumX - this.minimumX;
    }

    /**
     * @return the center coordinate of the bounding box
     */
    public Coordinate<Double> getCenter()
    {
        //noinspection MagicNumber
        return new Coordinate<>((this.maximumX + this.minimumX) / 2.0,
                                (this.maximumY + this.minimumY) / 2.0);
    }

    /**
     * @return the minimumY
     */
    public double getMinimumY()
    {
        return this.minimumY;
    }

    /**
     * @return the minimumX
     */
    public double getMinimumX()
    {
        return this.minimumX;
    }

    /**
     * @return the maximumY
     */
    public double getMaximumY()
    {
        return this.maximumY;
    }

    /**
     * @return the maximumX
     */
    public double getMaximumX()
    {
        return this.maximumX;
    }

    /**
     * @return the minimum x and y as a coordinate
     */
    public Coordinate<Double> getMin()
    {
        return new Coordinate<>(this.minimumX, this.minimumY);
    }

    /**
     * @return the maximum x and y as a coordinate
     */
    public Coordinate<Double> getMax()
    {
        return new Coordinate<>(this.maximumX, this.maximumY);
    }

    /**
     * @return the coordinate of the top left corner of the bounding box
     */
    public Coordinate<Double> getTopLeft()
    {
        return new Coordinate<>(this.minimumX, this.maximumY);
    }

    /**
     * @return the coordinate of the top right corner of the bounding box
     */
    public Coordinate<Double> getTopRight()
    {
        return this.getMax();
    }

    /**
     * @return the coordinate of the bottom left corner of the bounding box
     */
    public Coordinate<Double> getBottomLeft()
    {
        return this.getMin();
    }

    /**
     * @return the coordinate of the bottom right corner of the bounding box
     */
    public Coordinate<Double> getBottomRight()
    {
        return new Coordinate<>(this.maximumX, this.minimumY);
    }

    /**
     * Tests if a point is on the boundary of, or wholly within the bounding box. <br>
     * <br>
     * This method uses Java's default comparison operators ('<=' and '>=') and
     * therefore may be intolerant to the issues of rounding and other vagaries
     * of storing irrational numbers on a finite floating point machine. For
     * more information on why this might matter please see <a href=
     * "http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/"
     * > this article</a>.
     *
     * @param point
     *             A Point
     * @return Returns true if the point is on the boundary of, or wholly
     *             within the bounding box and otherwise false
     */
    public boolean contains(final Coordinate<Double> point)
    {
        return point.getY() >= this.minimumY &&
               point.getY() <= this.maximumY &&
               point.getX() >= this.minimumX &&
               point.getX() <= this.maximumX;
    }

    private final double minimumX;
    private final double minimumY;
    private final double maximumX;
    private final double maximumY;
}
