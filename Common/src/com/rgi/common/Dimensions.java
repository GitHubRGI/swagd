package com.rgi.common;

/**
 * @author Luke Lambert
 *
 * @param <T> extends Number
 */
public class Dimensions <T extends Number>
{
    /**
     * @param width the width
     * @param height the height
     */
    public Dimensions(final T width, final T height)
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
