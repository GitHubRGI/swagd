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
        final Map<TileOrigin, Coordinate<Double>> cornerCoordinates = new HashMap<>();

        cornerCoordinates.put(TileOrigin.LowerLeft,  this.bounds.getBottomLeft());
        cornerCoordinates.put(TileOrigin.UpperLeft,  this.bounds.getTopLeft());
        cornerCoordinates.put(TileOrigin.LowerRight, this.bounds.getBottomRight());
        cornerCoordinates.put(TileOrigin.UpperRight, this.bounds.getTopRight());

        for(final TileOrigin origin: cornerCoordinates.keySet())
        {
            this.assertContains(cornerCoordinates.get(origin), origin, true);
        }

    }

    @Test
    public void verifyContainsFalse()
    {
        final Map<Coordinate<Double>, TileOrigin> farEdgeCoordinates = new HashMap<>();
        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaximumX(), 0.0), TileOrigin.LowerLeft);//on right edge
        farEdgeCoordinates.put(new Coordinate<>(0.0,                   this.bounds.getMaximumY()), TileOrigin.LowerLeft);//on top edge

        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaximumX(), 1.0), TileOrigin.UpperLeft);//on right edge
        farEdgeCoordinates.put(new Coordinate<>(1.0,                   this.bounds.getMinimumY()), TileOrigin.UpperLeft); //on bottom edge

        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMinimumX(), 2.0), TileOrigin.LowerRight);//on left edge
        farEdgeCoordinates.put(new Coordinate<>(2.0,                   this.bounds.getMaximumY()), TileOrigin.LowerRight);//on top edge

        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMinimumX(), 3.0), TileOrigin.UpperRight);//on left edge
        farEdgeCoordinates.put(new Coordinate<>(3.0,                   this.bounds.getMinimumY()), TileOrigin.UpperRight);//on bottom edge

        farEdgeCoordinates.put(new Coordinate<>(this.bounds.getMaximumX() + 1, this.bounds.getMaximumY() + 1), TileOrigin.LowerLeft);//just completely outside the bounds

        for(final Coordinate<Double> expectedCoordinate: farEdgeCoordinates.keySet())
        {
            this.assertContains(expectedCoordinate, farEdgeCoordinates.get(expectedCoordinate), false);
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
        final Map<TileOrigin, Coordinate<Double>> expectedCorners = new HashMap<>();

        expectedCorners.put(TileOrigin.LowerLeft,  this.bounds.getBottomLeft());
        expectedCorners.put(TileOrigin.LowerRight, this.bounds.getBottomRight());
        expectedCorners.put(TileOrigin.UpperLeft,  this.bounds.getTopLeft());
        expectedCorners.put(TileOrigin.UpperRight, this.bounds.getTopRight());

        for(final TileOrigin origin: expectedCorners.keySet())
        {
            this.assertBoundsCorner(origin, expectedCorners.get(origin));
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

    private void assertBoundsCorner(final TileOrigin origin, final Coordinate<Double> expectedCoordinate)
    {
        final Coordinate<Double> coordinateReturned = BoundsUtility.boundsCorner(this.bounds, origin);
        assertTrue(String.format("BoundsUtility boundsCorner method returned a different value than expected.\nActual: %s\nExpected: %s",
                                 coordinateReturned.toString(),
                                 expectedCoordinate.toString()),
                   coordinateReturned.equals(expectedCoordinate));
    }

    private void assertContains(final Coordinate<Double> expectedCoordinate, final TileOrigin origin, final boolean outcome)
    {
        assertTrue(String.format("Expected boundsUtility method contains to return true for the following coordinate.\nCoordinate: %s, Bounds: %s Origin: %s.",
                                 expectedCoordinate.toString(),
                                 this.bounds.toString(),
                                 origin.toString()),
                   BoundsUtility.contains(this.bounds,expectedCoordinate, origin) == outcome);
    }
}
