/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.suite;

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
