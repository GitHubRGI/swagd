package com.rgi.erdc;

import com.rgi.erdc.coordinates.Coordinate;

/**
 * An immutable object representing the min and max x and y bounds of an area.
 *
 * @param <T> Extends {@link java.lang.Number}
 * @author Luke Lambert
 */
public class BoundingBox<T extends Number>
{
  /**
   * Constructor
   *
   * @param minY Minimum Y
   * @param minX Minimum X
   * @param maxY Maximum Y
   * @param maxX Maximum X
   */
  public BoundingBox(final T minY,
                     final T minX,
                     final T maxY,
                     final T maxX)
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

    final BoundingBox<T> other = (BoundingBox<T>)object;

    return equals(this.minY, other.minY) &&
           equals(this.minX, other.minX) &&
           equals(this.maxY, other.maxY) &&
           equals(this.maxX, other.maxX);
  }

  public boolean containsNull()
  {
    return this.getMinY() == null ||
           this.getMinX() == null ||
           this.getMaxY() == null ||
           this.getMaxX() == null;
  }

  private boolean equals(final T first, final T second)
  {
    return first == null ? second == null
        : first.equals(second);
  }

  @Override
  public int hashCode()
  {
    return this.minY.hashCode() ^
           this.minX.hashCode() ^
           this.maxY.hashCode() ^
           this.maxX.hashCode();
  }

  public double getHeight()
  {
    return this.getMaxY().doubleValue() - this.getMinY().doubleValue();
  }

  public double getWidth()
  {
    return this.getMaxX().doubleValue() - this.getMinX().doubleValue();
  }

  /**
   * @return the minY
   */
  public T getMinY()
  {
    return this.minY;
  }

  /**
   * @return the minX
   */
  public T getMinX()
  {
    return this.minX;
  }

  /**
   * @return the maxY
   */
  public T getMaxY()
  {
    return this.maxY;
  }

  /**
   * @return the maxX
   */
  public T getMaxX()
  {
    return this.maxX;
  }

  /**
   * @return the minimum x and y as a coordinate
   */
  public Coordinate<T> getMin()
  {
    return new Coordinate<T>(this.minY, this.minX);
  }

  /**
   * @return the maximum x and y as a coordinate
   */
  public Coordinate<T> getMax()
  {
    return new Coordinate<T>(this.maxY, this.maxX);
  }


  /**
   * Tests if a point is on the boundary of, wholly within the bounding box.
   * <br>
   * <br>
   * This method uses Java's default comparison operators ('<=' and '>=') and
   * therefore may be intolerant to the issues of rounding and other vagaries
   * of storing irrational numbers on a finite floating point machine.  For
   * more information on why this might matter please see
   * <a href="http://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/">
   * this article</a>.
   *
   * @param point
   *             A Point
   * @return Returns true if the point is on the boundary of, wholly within the bounding box and otherwise false
   */
  public boolean contains(final Coordinate<T> point)
  {
    return point.getY().doubleValue() >= this.minY.doubleValue() &&
           point.getY().doubleValue() <= this.maxY.doubleValue() &&
           point.getX().doubleValue() >= this.minX.doubleValue() &&
           point.getX().doubleValue() <= this.maxX.doubleValue();
  }

  final private T minY;
  final private T minX;
  final private T maxY;
  final private T maxX;
}
