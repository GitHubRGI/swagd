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
package com.rgi.common.util.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.util.BoundsUtility;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
public class BoundsUtilityTest
{
    private final BoundingBox bounds = new BoundingBox(-180.0, -90.0, 180.0, 90.0);
    
    @Test
    public void verifyContainsTrue()
    {
        Map<TileOrigin, Coordinate<Double>> cornerCoordinates = new HashMap<>();
        cornerCoordinates.put(TileOrigin.LowerLeft,  this.bounds.getBottomLeft());
        cornerCoordinates.put(TileOrigin.UpperLeft,  this.bounds.getTopLeft());
        cornerCoordinates.put(TileOrigin.LowerRight, this.bounds.getBottomRight());
        cornerCoordinates.put(TileOrigin.UpperRight, this.bounds.getTopRight());
        
        for(TileOrigin origin: cornerCoordinates.keySet())
        {
            assertContains(cornerCoordinates.get(origin), origin, true);
        }
        
    }
    
    @Test
    public void verifyContainsFalse()
    {
        Map<Coordinate<Double>, TileOrigin> farEdgeCoordinates = new HashMap<>();
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaxX(), 0.0),                   TileOrigin.LowerLeft);//on right edge
        farEdgeCoordinates.put(new Coordinate<>(0.0,                   this.bounds.getMaxY()), TileOrigin.LowerLeft);//on top edge
        
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaxX(), 1.0),                   TileOrigin.UpperLeft);//on right edge
        farEdgeCoordinates.put(new Coordinate<>(1.0,                   this.bounds.getMinY()), TileOrigin.UpperLeft); //on bottom edge
        
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMinX(), 2.0),                   TileOrigin.LowerRight);//on left edge
        farEdgeCoordinates.put(new Coordinate<>(2.0,                   this.bounds.getMaxY()), TileOrigin.LowerRight);//on top edge
        
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMinX(), 3.0),                   TileOrigin.UpperRight);//on left edge
        farEdgeCoordinates.put(new Coordinate<>(3.0,                   this.bounds.getMinY()), TileOrigin.UpperRight);//on bottom edge
        
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaxX() + 1, this.bounds.getMaxY() + 1), TileOrigin.LowerLeft);//just completely outside the bounds
        
        for(Coordinate<Double> expectedCoordinate: farEdgeCoordinates.keySet())
        {
            assertContains(expectedCoordinate, farEdgeCoordinates.get(expectedCoordinate), false);
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        BoundsUtility.contains(null, this.bounds.getBottomLeft(), TileOrigin.LowerLeft);
        fail("Expected BoundsUtility method contains to throw an IllegalArgumentException when given a null value for BoundingBox.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        BoundsUtility.contains(this.bounds, null, TileOrigin.LowerLeft);
        fail("Expected BoundsUtility method contains to throw an IllegalArgumentException when given a null value for Coordinate.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        BoundsUtility.contains(this.bounds, this.bounds.getBottomLeft(), null);
        fail("Expected BoundsUtility method contains to throw an IllegalArgumentException when given a null value for BoundingBox.");
    }
    
    @Test
    public void boundsCornerVerify()
    {
        Map<TileOrigin, Coordinate<Double>> expectedCorners = new HashMap<>();
        
        expectedCorners.put(TileOrigin.LowerLeft,  this.bounds.getBottomLeft());
        expectedCorners.put(TileOrigin.LowerRight, this.bounds.getBottomRight());
        expectedCorners.put(TileOrigin.UpperLeft,  this.bounds.getTopLeft());
        expectedCorners.put(TileOrigin.UpperRight, this.bounds.getTopRight());
        
        for(TileOrigin origin: expectedCorners.keySet())
        {
            assertBoundsCorner(origin, expectedCorners.get(origin));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        BoundsUtility.boundsCorner(null, TileOrigin.LowerLeft);
        fail("Expected BoundsUtility method boundsCorner to throw when given a null value for BoundingBox.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        BoundsUtility.boundsCorner(this.bounds, null);
        fail("Expected BoundsUtility method boundsCorner to throw when given a null value for TileOrigin.");
    }
    
    private void assertBoundsCorner(TileOrigin origin, Coordinate<Double> expectedCoordinate)
    {
        Coordinate<Double> coordinateReturned = BoundsUtility.boundsCorner(this.bounds, origin);
        assertTrue(String.format("BoundsUtility boundsCorner method returned a different value than expected.\nActual: %s\nExpected: %s",
                                 coordinateReturned.toString(),
                                 expectedCoordinate.toString()), 
                   coordinateReturned.equals(expectedCoordinate));
    }

    private void assertContains(Coordinate<Double> expectedCoordinate, TileOrigin origin, boolean outcome)
    {
        assertTrue(String.format("Expected boundsUtility method contains to return true for the following coordinate.\nCoordinate: %s, Bounds: %s Origin: %s.",
                                 expectedCoordinate.toString(), 
                                 this.bounds.toString(),
                                 origin.toString()),
                   BoundsUtility.contains(this.bounds,expectedCoordinate, origin) == outcome);
    }
}
