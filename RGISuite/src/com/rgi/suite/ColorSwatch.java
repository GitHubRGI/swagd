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
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ColorSwatch implements Icon {
	private int width = 64;
	private int height = 16;
	private Color color = null;

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return this.color;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		// TODO Auto-generated method stub
		g.setColor(Color.white);
		g.fillRect(0, 0, this.width, this.height);
		boolean check = false;
		g.setColor(Color.lightGray);
		for (int i = 0; i < this.width; i += 5) {
			for (int j = 0; j < this.height; j += 5) {
				check = !check;
				if (check) {
					g.fillRect(i, j, 5, 5);
				}
			}
		}
		g.setColor(this.color);
		g.fillRect(0, 0, this.width, this.height);
	}

	@Override
	public int getIconWidth() {
		return this.width;
	}

	@Override
	public int getIconHeight() {
		return this.height;
	}
}
