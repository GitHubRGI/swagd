package com.rgi.erdc;

/**
 * @author Luke Lambert
 *
 */
public class Dimension2D
{
    /**
     * @param height
     * @param width
     */
    public Dimension2D(final double height, final double width)
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
