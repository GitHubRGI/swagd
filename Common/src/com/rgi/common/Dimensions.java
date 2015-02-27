package com.rgi.common;

/**
 * Generic dimensions container
 *
 * @author Luke Lambert
 *
 * @param <T> extends Number
 */
public class Dimensions <T extends Number>
{
    /**
     * Constructor
     *
     * @param width
     *             The width
     * @param height
     *             The height
     */
    public Dimensions(final T width, final T height)
    {
        this.height = height;
        this.width  = width;
    }

    /**
     * @return The height
     */
    public T getHeight()
    {
        return this.height;
    }

    /**
     * @return The width
     */
    public T getWidth()
    {
        return this.width;
    }

    private final T height;
    private final T width;
}
