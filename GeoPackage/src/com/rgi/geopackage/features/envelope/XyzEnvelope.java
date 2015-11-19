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

package com.rgi.geopackage.features.envelope;

/**
 * @author Luke Lambert
 */
public class XyzEnvelope implements Envelope
{
    public XyzEnvelope(final double[] array)
    {
        if(array == null || array.length != 6)
        {
            throw new IllegalArgumentException("Array may not be null, and must have a length of 6");
        }

        this.array = array;
    }

    @Override
    public Double getMinimumX()
    {
        return this.array[0];
    }

    @Override
    public Double getMaximumX()
    {
        return this.array[1];
    }

    @Override
    public Double getMinimumY()
    {
        return this.array[2];
    }

    @Override
    public Double getMaximumY()
    {
        return this.array[3];
    }

    @Override
    public Double getMinimumZ()
    {
        return this.array[4];
    }

    @Override
    public Double getMaximumZ()
    {
        return this.array[5];
    }

    @Override
    public Double getMinimumM()
    {
        return null;
    }

    @Override
    public Double getMaximumM()
    {
        return null;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public boolean hasX()
    {
        return true;
    }

    @Override
    public boolean hasY()
    {
        return true;
    }

    @Override
    public boolean hasZ()
    {
        return true;
    }

    @Override
    public boolean hasM()
    {
        return false;
    }

    @Override
    public int getContentsIndicatorCode()
    {
        return 2;
    }

    @Override
    public double[] getArray()
    {
        return this.array.clone();
    }

    private final double[] array;
}
