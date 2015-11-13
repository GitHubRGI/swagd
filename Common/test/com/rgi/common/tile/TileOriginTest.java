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

package com.rgi.common.tile;

import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

/**
 * Created by matthew.moran on 11/4/15.
 */
public class TileOriginTest
{

    @Test
    public void testTileOriginTransformSameOrigin()
    {
        final TileOrigin           origin     = TileOrigin.LowerLeft;
        final TileOrigin toOrigin = TileOrigin.LowerLeft;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(1, 1);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, 1, 1, dimensions);
        assertTrue(String.format("Transformation to the original origin should not move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()) &&
                    Objects.equals(result.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformLowerLeftUpperLeftBothWays()
    {
        final TileOrigin           origin     = TileOrigin.LowerLeft;
        final TileOrigin    toOrigin = TileOrigin.UpperLeft;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(0, 0);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the UpperLeft from LowerLeft should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()) &&
                    Objects.equals(result.getY(), startPoint.getY() + 1)));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the LowerLeft from UpperLeft should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformLowerLeftUpperRightBothWays()
    {
        final TileOrigin           origin     = TileOrigin.LowerLeft;
        final TileOrigin    toOrigin = TileOrigin.UpperRight;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(0, 0);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the UpperRight from LowerLeft should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()+1) &&
                    Objects.equals(result.getY(), startPoint.getY()+1)));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the LowerLeft from UpperRight should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformLowerLeftLowerRightBothWays()
    {
        final TileOrigin           origin     = TileOrigin.LowerLeft;
        final TileOrigin    toOrigin = TileOrigin.LowerRight;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(0, 0);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the LowerRight from LowerLeft should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()+1) &&
                    Objects.equals(result.getY(), startPoint.getY())));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the LowerLeft from LowerRight should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformUpperLeftUpperRightBothWays()
    {
        final TileOrigin           origin     = TileOrigin.UpperLeft;
        final TileOrigin    toOrigin = TileOrigin.UpperRight;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(0, 1);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the UpperRight from UpperLeft should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()+1) &&
                    Objects.equals(result.getY(), startPoint.getY())));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the UpperLeft from UpperRight should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformUpperLeftLowerRightBothWays()
    {
        final TileOrigin           origin     = TileOrigin.UpperLeft;
        final TileOrigin    toOrigin = TileOrigin.LowerRight;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(0, 1);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the LowerRight from UpperLeft should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()+1) &&
                    Objects.equals(result.getY(), startPoint.getY()-1)));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the UpperLeft from LowerRight should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }

    @Test
    public void testTransformUpperRightLowerRightBothWays()
    {
        final TileOrigin           origin     = TileOrigin.UpperRight;
        final TileOrigin    toOrigin = TileOrigin.LowerRight;
        final TileMatrixDimensions dimensions = new TileMatrixDimensions(2, 2);
        final Coordinate<Integer>  startPoint = new Coordinate<>(1, 1);
        final Coordinate<Integer>  result     = origin.transform(toOrigin, startPoint.getX(), startPoint.getY(), dimensions);
        //to upper left
        assertTrue(String.format("Transformation to the LowerRight from UpperRight should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", startPoint.getX(), startPoint.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(result.getX(), startPoint.getX()) &&
                    Objects.equals(result.getY(), startPoint.getY() -1 )));
        //back to lower left
        final Coordinate<Integer>  resultreversed    = toOrigin.transform(origin, result.getX(), result.getY(), dimensions);
        assertTrue(String.format("Transformation to the UpperRight from LowerRight should should move a coordinate \n, " +
                                 "original coordinate: (%d,%d) result: (%d,%d)", resultreversed.getX(), resultreversed.getY(),
                                 result.getX(), result.getY()),
                   (Objects.equals(resultreversed.getX(), startPoint.getX()) &&
                    Objects.equals(resultreversed.getY(), startPoint.getY())));
    }
}
