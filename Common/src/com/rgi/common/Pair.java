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

/**
 * @author Luke Lambert
 *
 * @param <L> "Left" member of the pair
 * @param <R> "Right" member of the pair
 */
public class Pair<L, R>
{
    /**
     * Constructor
     *
     * @param left
     *             "Left" member of the pair
     * @param right
     *             "Right" member of the pair
     */
    public Pair(final L left, final R right)
    {
        this.left  = left;
        this.right = right;
    }

    /**
     * @param left
     *             "Left" member of the pair
     * @param right
     *             "Right" member of the pair
     * @return a Pair constructed with the left and right parameters
     */
    public static <L, R> Pair<L, R> of(final L left, final R right)
    {
        return new Pair<>(left, right);
    }

    /**
     * @return the left
     */
    public L getLeft()
    {
        return this.left;
    }

    /**
     * @return the right
     */
    public R getRight()
    {
        return this.right;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        final Pair<?, ?> pair = (Pair<?, ?>)obj;

        if(this.left != null ? !this.left.equals(pair.left) : pair.left != null)
        {
            return false;
        }

        return this.right != null ? this.right.equals(pair.right) : pair.right == null;
    }

    @Override
    public int hashCode()
    {
        int result = this.left != null ? this.left.hashCode() : 0;
        result = 31 * result + (this.right != null ? this.right.hashCode() : 0);
        return result;
    }

    private final L left;
    private final R right;
}
