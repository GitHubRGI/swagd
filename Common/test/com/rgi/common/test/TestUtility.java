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
package com.rgi.common.test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;

/**
 * @author Jenifer Cochran
 */
public class TestUtility
{
    public static boolean floatsEqual(final double value1, final double value2, final double epsilon)
    {
        return Math.abs(value1 - value2) < epsilon;
    }

    public static boolean coordinatesEqual(final Coordinate<Double> coordinate1, final Coordinate<Double> coordinate2, final double epsilon)
    {
        return floatsEqual(coordinate1.getX(), coordinate2.getX(), epsilon) && floatsEqual(coordinate1.getY(), coordinate2.getY(), epsilon);
    }

    public static boolean boundingBoxesEqual(final BoundingBox bounds1, final BoundingBox bounds2, final double epsilon)
    {
       return coordinatesEqual(bounds1.getBottomLeft(), bounds2.getBottomLeft(), epsilon) && coordinatesEqual(bounds1.getTopRight(), bounds2.getTopRight(), epsilon);
    }
}
