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
package common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.android.common.BoundingBox;
import com.rgi.android.common.coordinate.Coordinate;

@SuppressWarnings({"javadoc", "static-method"})
public class BoundingBoxTest
{

    @Test
    public void verifyValues()
    {
        final double minX = 0;
        final double minY = 1;
        final double maxX = 2;
        final double maxY = 3;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        assertTrue(this.printErrorMessage(minX, minY, maxX, maxY, bBox),
                   isEqual(bBox, minX, minY, maxX, maxY));
    }

    @SuppressWarnings("unused")
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new BoundingBox(1000, 1, 1, 1);//minX > maxX
        fail("Expected BoundingBox to throw an IllegalArgumentException when minX is larger than maxX.");
    }

    @SuppressWarnings("unused")
    @Test (expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new BoundingBox(1, 10000, 1, 1);//minY > maxY
        fail("Expected BoundingBox to throw an IllegalArgumentException when minY is larger than maxY.");
    }

    @Test
    public void verifyHeightandWidth()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double width  = maxX - minX;
        final double height = maxY - minY;
        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue(String.format("The bounding box did not return expected height. Expected: %f Actual: %f", height, bBox.getHeight()),
                   height == bBox.getHeight());

        assertTrue(String.format("The bounding box did not return expected height. Expected: %f Actual: %f", width, bBox.getWidth()),
                   width == bBox.getWidth());
    }

    @Test
    public void verifyCenter()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double centerX = (maxX + minX)/2.0;
        final double centerY = (maxY + minY)/2.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox.getCenter(), centerX, centerY, "getCenter()"),
                   bBox.getCenter().getX() == centerX &&
                   bBox.getCenter().getY() == centerY);

    }

    @Test
    public void verifyMinAndMax()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox.getMin(), minX, minY, "getMin()"),
                   bBox.getMin().getX() == minX &&
                   bBox.getMin().getY() == minY);

        assertTrue(this.printErrorMessage(bBox.getMax(), maxX, maxY, "getMax()"),
                   bBox.getMax().getX() == maxX &&
                   bBox.getMax().getY() == maxY);
    }

    @Test
    public void verifyTopLeftRightAndBottomLeftRight()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox.getTopLeft(), minX, maxY, "getTopLeft()"),
                   bBox.getTopLeft().getX() == minX &&
                   bBox.getTopLeft().getY() == maxY);

        assertTrue(this.printErrorMessage(bBox.getTopRight(), maxX, maxY, "getTopRight()"),
                bBox.getTopRight().getX() == maxX &&
                bBox.getTopRight().getY() == maxY);

        assertTrue(this.printErrorMessage(bBox.getBottomRight(), maxX, minY, "getBottomRight()"),
                bBox.getBottomRight().getX() == maxX &&
                bBox.getBottomRight().getY() == minY);

        assertTrue(this.printErrorMessage(bBox.getBottomLeft(), minX, minY, "getBottomLeft()"),
                bBox.getBottomLeft().getX() == minX &&
                bBox.getBottomLeft().getY() == minY);
    }

    @Test
    public void verifyEqualsAndHashCode()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        final BoundingBox bBox2 = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox, bBox2, "true"), bBox.equals(bBox2) == true);
        assertTrue("The method hashCode did not return equal values for the same bounding box.\nBoundinBox: %d\nBoundingBox other: %d",
                   bBox.hashCode() == bBox2.hashCode());
    }

    @Test
    public void verifyEqualsAndHashCode2()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double differentMinX = 0.25;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        final BoundingBox bBox2 = new BoundingBox(differentMinX, minY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d",
                   bBox.hashCode() != bBox2.hashCode());
    }

    @Test
    public void verifyEqualsAndHashCode3()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double differentMinY = -2345.678;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        final BoundingBox bBox2 = new BoundingBox(minX, differentMinY, maxX, maxY);

        assertTrue(this.printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d",
                   bBox.hashCode() != bBox2.hashCode());
    }

    @Test
    public void verifyEqualsAndHashCode4()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double differentMaxX = 983212.231;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        final BoundingBox bBox2 = new BoundingBox(minX, minY, differentMaxX, maxY);

        assertTrue(this.printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d",
                   bBox.hashCode() != bBox2.hashCode());
    }

    @Test
    public void verifyEqualsAndHashCode5()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final double differentMaxY = 0.25;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        final BoundingBox bBox2 = new BoundingBox(minX, minY, maxX, differentMaxY);

        assertTrue(this.printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d",
                   bBox.hashCode() != bBox2.hashCode());
    }

    @Test
    public void verifyEqualsDifferentObject()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(minX, minY);

        assertTrue("The method equals() did not return false when the parameter is of a different object.", bBox.equals(coordinate) == false);
    }

    @Test
    public void verifyContains()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(minX, minY);

        assertTrue(this.printErrorMessage("contains()", "true", coordinate, bBox), bBox.contains(coordinate) == true);
    }

    @Test
    public void verifyContains2()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(maxX, maxY);

        assertTrue(this.printErrorMessage("contains()", "true", coordinate, bBox), bBox.contains(coordinate) == true);
    }

    @Test
    public void verifyContains3()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(minX - 1, minY);

        assertTrue(this.printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }

    @Test
    public void verifyContains4()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(minX, minY - 1);

        assertTrue(this.printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }

    @Test
    public void verifyContains5()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(maxX + 1, minY);

        assertTrue(this.printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }

    @Test
    public void verifyContains6()
    {
        final double minX = -180.0;
        final double minY = -90.0;
        final double maxX = 180.0;
        final double maxY = 90.0;

        final BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);

        final Coordinate<Double> coordinate = new Coordinate<Double>(maxX, maxY + 1);

        assertTrue(this.printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }

    @Test
    public void verifyEqualsNull()
    {
        final double minX = 0.5;
        final double minY = -1234.5678;
        final double maxX = 2468.6;
        final double maxY = 8765.4321;

        final BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);

        assertTrue("The method equals() did not return false when the parameter was null.", bBox.equals(null) == false);
    }

    private static boolean isEqual(final BoundingBox bBox1, final double minX, final double minY, final double maxX, final double maxY)
    {
        return bBox1.getMinX() == minX &&
               bBox1.getMinY() == minY &&
               bBox1.getMaxX() == maxX &&
               bBox1.getMaxY() == maxY;
    }

    private String printErrorMessage(final String methodName, final String expectedValue, final Coordinate<Double> coordinate, final BoundingBox bBox)
    {
        return String.format("Expected the method %s to return %s.\nCoordinate: %s.\nBoundingBox: %s.", methodName, expectedValue, coordinate.toString(), bBox.toString());
    }

    private String printErrorMessage(final double minXExpected, final double minYExpected, final double maxXExpected, final double maxYExpected, final BoundingBox bBoxActual)
    {
        return String.format("The boundingBox did not return the expected values.\nActual: %s.\nExpected: (%f, %f, %f, %f).",
                bBoxActual.toString(),
                minXExpected,         minYExpected,         maxXExpected,         maxYExpected);
    }

    private String printErrorMessage(final Coordinate<Double> coordinate, final double expectedX, final double expectedY, final String methodName)
    {
       return String.format("The %s method in BoundingBox did not return the expected values. Expected: (%f, %f). Actual: %s",
                            methodName,
                            expectedX,
                            expectedY,
                            coordinate.toString());
    }

    private String printErrorMessage(final BoundingBox bBoxActual,final BoundingBox bBoxExpected, final String valueExpected)
    {
        return String.format("The boundingBox method equals did not return the expected value of %s.\nBoundingBox: %s.\nBoundingBox other: %s.",
                             valueExpected,
                             bBoxActual.toString(),
                             bBoxExpected.toString());
    }
}
