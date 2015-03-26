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
package com.rgi.verifiertool;

import javafx.scene.paint.Color;


/**
 * @author Jenifer Cochran
 *
 */
public enum PassingLevel
{
    /**
     * Set the font to green if GeoPackage passes verifier
     */
    Pass(Color.GREEN, "Passed"),
    /**
     * Set the font to yellow if GeoPackage has warnings but no errors in verifier
     */
    Warning(Color.ORANGE, "Warning"),
    /**
     * Set the font to red if GeoPackage has failing requirements
     */
    Fail(Color.RED, "Failed");
    private Color color;
    private String text;

    PassingLevel(final Color color, final String text)
    {
        this.setColor(color);
        this.setText(text);
    }

    /**
     * @return get text of level (Pass, Failed, Warning)
     */
    public String getText()
    {
        return this.text;
    }

    /**
     * @param text set message of level (Pass, failed, warning)
     */
    public void setText(final String text)
    {
        this.text = text;
    }

    /**
     * @return set color of the passing level (red-> error, orange-> warning, green-> passed)
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * @param color set color of the passing level (red-> error, orange-> warning, green-> passed)
     */
    public void setColor(final Color color)
    {
        this.color = color;
    }
}
