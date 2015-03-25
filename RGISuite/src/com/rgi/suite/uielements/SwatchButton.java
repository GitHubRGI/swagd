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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;

/**
 * @author Duff Means
 *
 */
public class SwatchButton extends JButton
{
    private static final long serialVersionUID = -4668559044616232318L;

    private Color color = null;

    /**
     * Creates a button with text.
     *
     * @param label
     *             the text of the button
     */
    public SwatchButton(final String label)
    {
        super(label);
    }

    /**
     * @return the color
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(final Color color)
    {
        this.color = color;
    }

    @Override
    public void paintComponent(final Graphics g)
    {
        super.paintComponent(g);
        final Rectangle r = g.getClipBounds();
        g.setColor(Color.white);
        g.fillRect(r.x, r.y, r.width, r.height);
        boolean check = false;
        g.setColor(Color.lightGray);
        for(int x = r.x; x < r.width; x += 5)
        {
            boolean rowcheck = check;
            check = !check;
            for(int y = r.y; y < r.height; y += 5)
            {
                rowcheck = !rowcheck;
                if(rowcheck)
                {
                    g.fillRect(x, y, 5, 5);
                }
            }
        }

        if(this.color != null)
        {
            g.setColor(this.color);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
