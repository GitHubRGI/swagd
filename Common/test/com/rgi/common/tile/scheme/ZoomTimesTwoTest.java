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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
public class ZoomTimesTwoTest
{
    
    @Test
    public void verifyZoomTimesTwoDimensions()
    {
        int minZoom = 4;
        int maxZoom = 9;
        
        int minZoomWidth  = 2;
        int minZoomHeight = 3;
        
        ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, minZoomWidth, minZoomHeight);
        
        Map<Integer, TileMatrixDimensions> expectedDimensions = new HashMap<>();
        expectedDimensions.put(minZoom,     new TileMatrixDimensions(minZoomWidth,    minZoomHeight));
        expectedDimensions.put(minZoom + 1, new TileMatrixDimensions(minZoomWidth*2,  minZoomHeight*2));
        expectedDimensions.put(minZoom + 2, new TileMatrixDimensions(minZoomWidth*4,  minZoomHeight*4));
        expectedDimensions.put(minZoom + 3, new TileMatrixDimensions(minZoomWidth*8,  minZoomHeight*8));
        expectedDimensions.put(minZoom + 4, new TileMatrixDimensions(minZoomWidth*16, minZoomHeight*16));
        expectedDimensions.put(minZoom + 5, new TileMatrixDimensions(minZoomWidth*32, minZoomHeight*32));
        
        for(Integer zoom: expectedDimensions.keySet())
        {
            TileMatrixDimensions returnedDimensions = tileScheme.dimensions(zoom);
            assertDimensions(expectedDimensions.get(zoom), returnedDimensions);
        }
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new ZoomTimesTwo(-1, 0 , 1, 1); //negative min zoom level
        fail("Expected an IllegalArgumentException when the minimum zoom level was less than 0.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new ZoomTimesTwo(0, -1, 1, 1); //negative max zoom level
        fail("Expected an IllegalArgumentException when the maximum zoom level was less than 0.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        new ZoomTimesTwo(2, 3, 1, 0); //matrix height < 1
        fail("Expected an IllegalArgumentException when the matrix height was less than 1.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        new ZoomTimesTwo(2, 3, 0, 1); //matrix width < 1
        fail("Expected an IllegalArgumentException when the matrix width was less than 1.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        new ZoomTimesTwo(10, 2, 1, 1); //min zoom > max zoom
        fail("Expected an IllegalArgumentException when the minimum zoom is greater than maximum zoom.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException6()
    {
        new ZoomTimesTwo(1, 21, 1028, 100); // (2^21 - 1)* 1028 > Integer.MaxValue -> matrix width and zoom causes overflow
        fail("Expected an IllegalArgumentException when the combination of initial width and maximum zoom level will cause an integer overflow for tile numbering.");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException7()
    {
        new ZoomTimesTwo(1, 21, 100, 1028); // (2^21 - 1)* 1028 > Integer.MaxValue -> matrix height and zoom causes overflow
        fail("Expected an IllegalArgumentException when the combination of initial height and maximum zoom level will cause an integer overflow for tile numbering.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException8()
    {
        int minZoom = 0;
        int maxZoom = 12;
        
        ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, 2, 3);
        tileScheme.dimensions(minZoom - 1);
        
        fail("Expected an IllegalArgumentException when requesting for a dimensions less than the minimum zoom level.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException9()
    {
        int minZoom = 0;
        int maxZoom = 12;
        
        ZoomTimesTwo tileScheme = new ZoomTimesTwo(minZoom, maxZoom, 2, 3);
        tileScheme.dimensions(maxZoom + 1);
        
        fail("Expected an IllegalArgumentException when requesting for a dimensions greater than the maximum zoom level.");
    }
    
    private static void assertDimensions(TileMatrixDimensions expectedTileMatrixDimensions, TileMatrixDimensions returnedDimensions)
    {
        assertTrue(String.format("TileMatrixDimensions returned from the method dimensions() were not the expected values.\nActual: (%d, %d).\nReturned: (%d, %d).", 
                                 returnedDimensions.getWidth(), 
                                 returnedDimensions.getHeight(), 
                                 expectedTileMatrixDimensions.getWidth(), 
                                 expectedTileMatrixDimensions.getHeight()),
                  expectedTileMatrixDimensions.getWidth()  == returnedDimensions.getWidth() &&
                  expectedTileMatrixDimensions.getHeight() == returnedDimensions.getHeight());
    }
}
