package com.rgi.common;

import com.rgi.common.coordinates.Coordinate;

/**
 * An immutable object representing the min and max x and y bounds of an area.
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
    public BoundingBox(final Double minY,
                       final Double minX,
                       final Double maxY,
                       final Double maxX)
    {
        if(minY != null && maxY != null && minY.doubleValue() > maxY.doubleValue())
        {
            throw new IllegalArgumentException("Min y cannot be greater than max y");
        }

        if(minX != null && maxX != null && minX.doubleValue() > maxX.doubleValue())
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

        return BoundingBox.equals(this.minY, other.minY) &&
               BoundingBox.equals(this.minX, other.minX) &&
               BoundingBox.equals(this.maxY, other.maxY) &&
               BoundingBox.equals(this.maxX, other.maxX);
    }

    public boolean containsNull()
    {
        return this.getMinY() == null ||
               this.getMinX() == null ||
               this.getMaxY() == null ||
               this.getMaxX() == null;
    }

    private static boolean equals(final Double first, final Double second)
    {
        return first == null ? second == null
                             : first.equals(second);
    }

    @Override
    public int hashCode()
    {
        return (this.minY == null ? 0 : this.minY.hashCode()) ^
               (this.minX == null ? 0 : this.minX.hashCode()) ^
               (this.maxY == null ? 0 : this.maxY.hashCode()) ^
               (this.maxX == null ? 0 : this.maxX.hashCode());
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
    public Double getMinY()
    {
        return this.minY;
    }

    /**
     * @return the minX
     */
    public Double getMinX()
    {
        return this.minX;
    }

    /**
     * @return the maxY
     */
    public Double getMaxY()
    {
        return this.maxY;
    }

    /**
     * @return the maxX
     */
    public Double getMaxX()
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

    final private Double minY;
    final private Double minX;
    final private Double maxY;
    final private Double maxX;
}
