package com.rgi.common;

/**
 * @author Luke Lambert
 *
 */

public class Dimensions
{
    /**
     * @param height the height
     * @param width the width
     */
    public Dimensions(final double height, final double width)
    {
        this.height = height;
        this.width = width;
    }

    /**
     * @return the height
     */
    public double getHeight()
    {
        return this.height;
    }

    /**
     * @return the width
     */
    public double getWidth()
    {
        return this.width;
    }

    private final double height;
    private final double width;
}
