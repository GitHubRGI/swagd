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

package com.rgi.erdc.test;

import java.util.Random;

import org.junit.Test;

import com.rgi.erdc.ZoomLevel;

/**
 * Unit tests for ZoomLevel
 * @author Steven D. Lander
 *
 */
public class ZoomLevelTest {
	
	private final Random randomGenerator = new Random();

	@Test(expected = IllegalArgumentException.class)
	public void breakOnBadMinZoom() {
		ZoomLevel.verify(this.randomGenerator.nextInt(33) - 33);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void breakOnBadMaxZoom() {
		ZoomLevel.verify(this.randomGenerator.nextInt(33) + 33);
	}
	
	@Test
	public void verifyCorrectZoom() {
		ZoomLevel.verify(this.randomGenerator.nextInt(32));
	}
}
