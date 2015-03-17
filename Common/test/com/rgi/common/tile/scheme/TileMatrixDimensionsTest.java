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
package com.rgi.common.tile.scheme;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
public class TileMatrixDimensionsTest
{
    
    @Test
    public void verifyTileMatrixDimensions()
    {
        int height = 12;
        int width  = 77;
        TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height);
        assertTrue(String.format("TileMatrixDimensions did not return the expected values.\nActual: (%d, %d).\nReturned: (%d, %d).", 
                                  dimensions.getWidth(), 
                                  dimensions.getHeight(), 
                                  width, 
                                  height),
                   dimensions.getHeight() == height && 
                   dimensions.getWidth()  == width);
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new TileMatrixDimensions(0, 10);
        fail("Expected an IllegalArgumentException to be thrown when width is less than or equal to 0.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new TileMatrixDimensions(10, 0);
        fail("Expected an IllegalArgumentException to be thrown when height is less than or equal to 0.");
    }
    
    @Test
    public void containsTrue()
    {
        int height = 12;
        int width  = 77;
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height);
        
        assertContains(dimensions, width - 1, height - 1, true); //test top 
         
        assertContains(dimensions, 0, 0, true);  //bottom 
        
        assertContains(dimensions, width/2, height/2, true); //middle
    }
    
    @Test
    public void containsFalse()
    {
        int height = 12;
        int width  = 77;
        
        TileMatrixDimensions dimensions = new TileMatrixDimensions(width, height); 
        
        assertContains(dimensions, -1, 0, false);  //test left
        
        assertContains(dimensions, 0, -1, false);  //test down
        
        assertContains(dimensions, width - 1, height, false); //test up
        
        assertContains(dimensions, width, height - 1, false); //test right
    }
    
    private void assertContains(TileMatrixDimensions dimensions, int column, int row, boolean expectedOutcome)
    {
        assertTrue(String.format("Expected the method contains to return true for the following values.\nDimensions: (%d, %d)\nTile Coordinate(%d, %d).", 
                                  dimensions.getWidth(), 
                                  dimensions.getHeight(), 
                                  column,
                                  row),
                  dimensions.contains(column, row) == expectedOutcome);
    }
    

}
