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

package com.rgi.common.tile.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

@SuppressWarnings({"javadoc","static-method"})
/**
 * 
 * @author Jenifer Cochran
 *
 */
public class TileOriginTest
{
    List<TileOrigin> tileOrigins = new ArrayList<>(Arrays.asList(TileOrigin.LowerLeft, TileOrigin.UpperLeft, TileOrigin.LowerRight, TileOrigin.UpperRight));
    
    @Test
    public void transformSameOriginToSameOrigin()
    {
        Coordinate<Integer>  tileCoordinateExpected = new Coordinate<>(2, 6);
        TileMatrixDimensions matrixDimensions       = new TileMatrixDimensions(85, 97);
        
        for(TileOrigin origin: this.tileOrigins)
        {
            Coordinate<Integer>  tileCoordinateReturned = origin.transform(origin, 
                                                                           tileCoordinateExpected.getX(), 
                                                                           tileCoordinateExpected.getY(), 
                                                                           matrixDimensions);
            //There should be no changes in the tile coordinate
            assertTileCoordinates(tileCoordinateExpected, tileCoordinateReturned);
        }
    }
    
    @Test
    public void transformOriginToOrigin()
    {
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(6, 5);
        
        Map<TileOrigin, Coordinate<Integer>> expectedCoordinates = new HashMap<>();
        
        expectedCoordinates.put(TileOrigin.UpperLeft,  new Coordinate<>(0, 1));
        expectedCoordinates.put(TileOrigin.UpperRight, new Coordinate<>(5, 1));
        expectedCoordinates.put(TileOrigin.LowerLeft,  new Coordinate<>(0, 3));
        expectedCoordinates.put(TileOrigin.LowerRight, new Coordinate<>(5, 3));
        
        for(TileOrigin startOrigin: expectedCoordinates.keySet())
        {
            Coordinate<Integer> initialCoordinate = expectedCoordinates.get(startOrigin);
            
            for(TileOrigin toOrigin : expectedCoordinates.keySet())
            {
                Coordinate<Integer>  expectedCoordinate = expectedCoordinates.get(toOrigin);
                
                Coordinate<Integer>  returnedCoordinate = startOrigin.transform(toOrigin,
                                                                                initialCoordinate.getX(), 
                                                                                initialCoordinate.getY(), 
                                                                                matrixDimensions);
                
                assertTileCoordinates(expectedCoordinate, returnedCoordinate);
            }
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        TileMatrixDimensions matrixDimensions = new TileMatrixDimensions(6, 5);
        TileOrigin.LowerLeft.transform(null, 0, 0, matrixDimensions);
        fail("Expected TileOrigin to throw an IllegalArgumentException when passing a null toOrigin to the method transform().");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        TileOrigin.LowerLeft.transform(TileOrigin.LowerRight, 0, 0, null);
        fail("Expected TileOrigin to throw an IllegalArgumentException when passing a null TileMatrixDimensions to the method transform().");
    }
    
    private static void assertTileCoordinates(Coordinate<Integer> tileCoordinateExpected, Coordinate<Integer> tileCoordinateReturned)
    {
        assertTrue(String.format("The Coordinate did not return the expected values.\nActual: %s\nExpected: %s", 
                                 tileCoordinateReturned.toString(), 
                                 tileCoordinateExpected.toString()), 
                  tileCoordinateExpected.equals(tileCoordinateReturned));
    }
}
