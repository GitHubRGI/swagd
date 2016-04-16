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

package com.rgi.suite.uielements;

import javax.swing.AbstractSpinnerModel;

/**
 * Extension to {@link AbstractSpinnerModel} that displays values that go up or
 * down by successive or previous powers of two
 *
 * @author Luke Lambert
 *
 */
public class PowerOfTwoSpinnerModel extends AbstractSpinnerModel
{
    private static final long serialVersionUID = 4397576810228153980L;

    private final int binaryLogOfMinimum;
    private final int binaryLogOfMaximum;

    private int binaryLogOfValue;

    /**
     * Constructor
     *
     * @param initial
     *             Initial spinner value. Rounded to the nearest power of two.
     * @param minimum
     *             Minimum spinner value. Rounded to the nearest power of two.
     * @param maximum
     *             Maximum spinner value. Rounded to the nearest power of two.
     */
    public PowerOfTwoSpinnerModel(final int initial, final int minimum, final int maximum)
    {
        this.binaryLogOfMinimum = PowerOfTwoSpinnerModel.binaryLog(minimum);
        this.binaryLogOfMaximum = PowerOfTwoSpinnerModel.binaryLog(maximum);

        this.setValue(initial);
    }

    @Override
    public Object getValue()
    {
        return (int)Math.pow(2, this.binaryLogOfValue);
    }

    @Override
    public void setValue(final Object value)
    {
        this.binaryLogOfValue = PowerOfTwoSpinnerModel.binaryLog((int)value);
        this.fireStateChanged();
    }

    @Override
    public Object getNextValue()
    {
        return this.binaryLogOfValue >= this.binaryLogOfMaximum ? null
                                                                : (int)Math.pow(2, this.binaryLogOfValue+1);
    }

    @Override
    public Object getPreviousValue()
    {
        return this.binaryLogOfValue <= this.binaryLogOfMinimum ? null
                                                                : (int)Math.pow(2, this.binaryLogOfValue-1);
    }

    private static int binaryLog(final int val)
    {
        return (int)(Math.log(val) / Math.log(2));
    }
}
