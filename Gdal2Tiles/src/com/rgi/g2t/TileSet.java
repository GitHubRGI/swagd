package com.rgi.g2t;

import com.rgi.common.tile.TileOrigin;

/**
 * An immutable object representing the min and max x and y tile bounds
 *
 * @author Duff Means
 */
public class TileSet {
    private int zoomLevel;
    private TileOrigin origin;

  /**
   * Instantiates an immutable TileSet.
   *
   * @param north the north bound
   * @param west the west bound
   * @param south the south bound
   * @param east the east bound
   */
  public TileSet(final int zoomLevel, TileOrigin origin) {
    this.zoomLevel = zoomLevel;
    this.origin = origin;
  }

  @Override
  public String toString() {
    return String.format("Z: %d North: %d; West: %d; South: %d; East: %d", this.zoomLevel, getNorth(), getWest(), getSouth(), getEast());
  }

  @Override
  public boolean equals(final Object object) {
    if(object == null || object.getClass() != this.getClass()) {
      return false;
    }

    final TileSet other = (TileSet)object;

    return this.zoomLevel == other.zoomLevel &&
           this.origin.equals(other.origin);
  }

  @Override
  public int hashCode()  {
    return getNorth() ^ getWest() ^ getSouth() ^ getEast();
  }

  /**
   * @return the north bound
   */
  public int getNorth() {
    return this.origin == TileOrigin.UpperLeft || this.origin == TileOrigin.UpperRight ? 0 : (int)Math.pow(2, this.zoomLevel);
  }

  /**
   * @return the west bound
   */
  public int getWest() {
      return this.origin == TileOrigin.UpperLeft || this.origin == TileOrigin.LowerLeft ? 0 : (int)Math.pow(2, this.zoomLevel);
  }

  /**
   * @return the south bound
   */
  public int getSouth() {
      return this.origin == TileOrigin.LowerLeft || this.origin == TileOrigin.LowerRight ? 0 : (int)Math.pow(2, this.zoomLevel);
  }

  /**
   * @return the east bound
   */
  public int getEast() {
      return this.origin == TileOrigin.UpperRight || this.origin == TileOrigin.LowerRight ? 0 : (int)Math.pow(2, this.zoomLevel);
  }

  /**
   * @return the difference between the east and west bounds
   */
  public int getWidth() {
    return Math.abs(getEast() - getWest());
  }

  /**
   * @return the difference between the south and north bounds
   */
  public int getHeight() {
    return Math.abs(getSouth() - getNorth());
  }
}
