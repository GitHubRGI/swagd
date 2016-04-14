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
     * @param minX
     *            Minimum X
     * @param minY
     *            Minimum Y
     * @param maxX
     *            Maximum X
     * @param maxY
     *            Maximum Y
     */
    public BoundingBox(final double minX,
                       final double minY,
                       final double maxX,
                       final double maxY)
    {
        if(minX > maxX)
        {
            throw new IllegalArgumentException("Min x cannot be greater than max x");
        }

        if(minY > maxY)
        {
            throw new IllegalArgumentException("Min y cannot be greater than max y");
        }

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s, %s, %s)",
                             this.minX,
                             this.minY,
                             this.maxX,
                             this.maxY);
    }

    @Override
    public boolean equals(final Object object)
    {
        if(object == null || !(object instanceof BoundingBox))
        {
            return false;
        }

        final BoundingBox other = (BoundingBox)object;

        return this.minY == other.minY &&
               this.minX == other.minX &&
               this.maxY == other.maxY &&
               this.maxX == other.maxX;
    }

    @Override
    public int hashCode()
    {
        return Double.valueOf(this.minY).hashCode() ^
               Double.valueOf(this.minX).hashCode() ^
               Double.valueOf(this.maxY).hashCode() ^
               Double.valueOf(this.maxX).hashCode();
    }

    /**
     * @return the height of the bounding box
     */
    public double getHeight()
    {
        return this.getMaxY() - this.getMinY();
    }

    /**
     * @return the width of the bounding box
     */
    public double getWidth()
    {
        return this.getMaxX() - this.getMinX();
    }

    /**
     * @return the center coordinate of the bounding box
     */
    public Coordinate<Double> getCenter()
    {
        return new Coordinate<>((this.getMaxX() + this.getMinX()) / 2.0,
                                (this.getMaxY() + this.getMinY()) / 2.0);
    }

    /**
     * @return the minY
     */
    public double getMinY()
    {
        return this.minY;
    }

    /**
     * @return the minX
     */
    public double getMinX()
    {
        return this.minX;
    }

    /**
     * @return the maxY
     */
    public double getMaxY()
    {
        return this.maxY;
    }

    /**
     * @return the maxX
     */
    public double getMaxX()
    {
        return this.maxX;
    }

    /**
     * @return the minimum x and y as a coordinate
     */
    public Coordinate<Double> getMin()
    {
        return new Coordinate<>(this.minX, this.minY);
    }

    /**
     * @return the maximum x and y as a coordinate
     */
    public Coordinate<Double> getMax()
    {
        return new Coordinate<>(this.maxX, this.maxY);
    }

    /**
     * @return the coordinate of the top left corner of the bounding box
     */
    public Coordinate<Double> getTopLeft()
    {
        return new Coordinate<>(this.minX, this.maxY);
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
        return new Coordinate<>(this.maxX, this.minY);
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
        return point.getY() >= this.minY &&
               point.getY() <= this.maxY &&
               point.getX() >= this.minX &&
               point.getX() <= this.maxX;
    }

    final private double minY;
    final private double minX;
    final private double maxY;
    final private double maxX;
}
