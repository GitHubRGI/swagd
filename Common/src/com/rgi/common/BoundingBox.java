package com.rgi.common;

import com.rgi.common.coordinates.Coordinate;

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
     * @param minY
     *            Minimum Y
     * @param minX
     *            Minimum X
     * @param maxY
     *            Maximum Y
     * @param maxX
     *            Maximum X
     */
    public BoundingBox(final double minY,
                       final double minX,
                       final double maxY,
                       final double maxX)
    {
        if(minY > maxY)
        {
            throw new IllegalArgumentException("Min y cannot be greater than max y");
        }

        if(minX > maxX)
        {
            throw new IllegalArgumentException("Min x cannot be greater than max x");
        }

        this.minY = minY;
        this.minX = minX;
        this.maxY = maxY;
        this.maxX = maxX;
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

    public double getHeight()
    {
        return this.getMaxY() - this.getMinY();
    }

    public double getWidth()
    {
        return this.getMaxX() - this.getMinX();
    }

    public Coordinate<Double> getCenter()
    {
        return new Coordinate<>((this.getMaxY() + this.getMinY()) / 2.0,
                                (this.getMaxX() + this.getMinX()) / 2.0);
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
        return new Coordinate<>(this.minY, this.minX);
    }

    /**
     * @return the maximum x and y as a coordinate
     */
    public Coordinate<Double> getMax()
    {
        return new Coordinate<>(this.maxY, this.maxX);
    }

    public Coordinate<Double> getTopLeft()
    {
        return new Coordinate<>(this.maxY, this.minX);
    }

    public Coordinate<Double> getTopRight()
    {
        return this.getMax();
    }

    public Coordinate<Double> getBottomLeft()
    {
        return this.getMin();
    }

    public Coordinate<Double> getBottomRight()
    {
        return new Coordinate<>(this.minY, this.maxX);
    }

    /**
     * Tests if a point is on the boundary of, wholly within the bounding box. <br>
     * <br>
     * This method uses Java's default comparison operators ('<=' and '>=') and
     * therefore may be intolerant to the issues of rounding and other vagaries
     * of storing irrational numbers on a finite floating point machine. For
     * more information on why this might matter please see <a href=
     * "http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/"
     * > this article</a>.
     *
     * @param point
     *            A Point
     * @return Returns true if the point is on the boundary of, wholly within
     *         the bounding box and otherwise false
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
