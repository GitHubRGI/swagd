package com.rgi.common;

/**
 * @author Luke Lambert
 *
 * @param <T> extends Number
 */
public class Dimensions <T extends Number>
{
    /**
     * @param height the height
     * @param width the width
     */
    public Dimensions(final T height, final T width)
    {
        this.height = height;
        this.width  = width;
    }

    /**
     * @return the height
     */
    public T getHeight()
    {
        return this.height;
    }

    /**
     * @return the width
     */
    public T getWidth()
    {
        return this.width;
    }

    private final T height;
    private final T width;
}
