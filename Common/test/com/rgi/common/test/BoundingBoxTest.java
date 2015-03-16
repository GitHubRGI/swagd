package com.rgi.common.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;

@SuppressWarnings({"javadoc", "static-method"})
public class BoundingBoxTest
{

    @Test
    public void verifyValues()
    {
        double minX = 0;
        double minY = 1;
        double maxX = 2;
        double maxY = 3;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        assertTrue(printErrorMessage(minX, minY, maxX, maxY, bBox), 
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
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double width  = maxX - minX;
        double height = maxY - minY;
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue(String.format("The bounding box did not return expected height. Expected: %f Actual: %f", height, bBox.getHeight()),
                   height == bBox.getHeight());
        
        assertTrue(String.format("The bounding box did not return expected height. Expected: %f Actual: %f", width, bBox.getWidth()),
                   width == bBox.getWidth());
    }
    
    @Test
    public void verifyCenter()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double centerX = (maxX + minX)/2.0;
        double centerY = (maxY + minY)/2.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox.getCenter(), centerX, centerY, "getCenter()"),
                   bBox.getCenter().getX() == centerX && 
                   bBox.getCenter().getY() == centerY);
        
    }
    
    @Test
    public void verifyMinAndMax()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox.getMin(), minX, minY, "getMin()"),
                   bBox.getMin().getX() == minX && 
                   bBox.getMin().getY() == minY);
        
        assertTrue(printErrorMessage(bBox.getMax(), maxX, maxY, "getMax()"),
                   bBox.getMax().getX() == maxX && 
                   bBox.getMax().getY() == maxY);
    }
    
    @Test
    public void verifyTopLeftRightAndBottomLeftRight()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox.getTopLeft(), minX, maxY, "getTopLeft()"), 
                   bBox.getTopLeft().getX() == minX && 
                   bBox.getTopLeft().getY() == maxY);
        
        assertTrue(printErrorMessage(bBox.getTopRight(), maxX, maxY, "getTopRight()"), 
                bBox.getTopRight().getX() == maxX && 
                bBox.getTopRight().getY() == maxY);
        
        assertTrue(printErrorMessage(bBox.getBottomRight(), maxX, minY, "getBottomRight()"), 
                bBox.getBottomRight().getX() == maxX && 
                bBox.getBottomRight().getY() == minY);
        
        assertTrue(printErrorMessage(bBox.getBottomLeft(), minX, minY, "getBottomLeft()"), 
                bBox.getBottomLeft().getX() == minX && 
                bBox.getBottomLeft().getY() == minY);
    }
    
    @Test
    public void verifyEqualsAndHashCode()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        BoundingBox bBox2 = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox, bBox2, "true"), bBox.equals(bBox2) == true);
        assertTrue("The method hashCode did not return equal values for the same bounding box.\nBoundinBox: %d\nBoundingBox other: %d", 
                   bBox.hashCode() == bBox2.hashCode());
    }
    
    @Test 
    public void verifyEqualsAndHashCode2()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double differentMinX = 0.25;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        BoundingBox bBox2 = new BoundingBox(differentMinX, minY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d", 
                   bBox.hashCode() != bBox2.hashCode());
    }
    
    @Test 
    public void verifyEqualsAndHashCode3()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double differentMinY = -2345.678;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        BoundingBox bBox2 = new BoundingBox(minX, differentMinY, maxX, maxY);
        
        assertTrue(printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d", 
                   bBox.hashCode() != bBox2.hashCode());
    }
    
    @Test 
    public void verifyEqualsAndHashCode4()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double differentMaxX = 983212.231;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        BoundingBox bBox2 = new BoundingBox(minX, minY, differentMaxX, maxY);
        
        assertTrue(printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d", 
                   bBox.hashCode() != bBox2.hashCode());
    }
    
    @Test 
    public void verifyEqualsAndHashCode5()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        double differentMaxY = 0.25;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        BoundingBox bBox2 = new BoundingBox(minX, minY, maxX, differentMaxY);
        
        assertTrue(printErrorMessage(bBox, bBox2, "false"), bBox.equals(bBox2) == false);
        assertTrue("The method hashCode did not return different values for the different bounding boxex.\nBoundinBox: %d\nBoundingBox other: %d", 
                   bBox.hashCode() != bBox2.hashCode());
    }
    
    @Test
    public void verifyEqualsDifferentObject()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(minX, minY);
        
        assertTrue("The method equals() did not return false when the parameter is of a different object.", bBox.equals(coordinate) == false);
    }
    
    @Test
    public void verifyContains()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(minX, minY);
        
        assertTrue(printErrorMessage("contains()", "true", coordinate, bBox), bBox.contains(coordinate) == true);
    }
    
    @Test
    public void verifyContains2()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(maxX, maxY);
        
        assertTrue(printErrorMessage("contains()", "true", coordinate, bBox), bBox.contains(coordinate) == true);
    }
    
    @Test
    public void verifyContains3()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(minX - 1, minY);
        
        assertTrue(printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }
    
    @Test
    public void verifyContains4()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(minX, minY - 1);
        
        assertTrue(printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }
    
    @Test
    public void verifyContains5()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(maxX + 1, minY);
        
        assertTrue(printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }
    
    @Test
    public void verifyContains6()
    {
        double minX = -180.0;
        double minY = -90.0;
        double maxX = 180.0;
        double maxY = 90.0;
        
        BoundingBox bBox = new BoundingBox(minX, minY, maxX, maxY);
        
        Coordinate<Double> coordinate = new Coordinate<>(maxX, maxY + 1);
        
        assertTrue(printErrorMessage("contains()", "false", coordinate, bBox), bBox.contains(coordinate) == false);
    }
    
    @Test
    public void verifyEqualsNull()
    {
        double minX = 0.5;
        double minY = -1234.5678;
        double maxX = 2468.6;
        double maxY = 8765.4321;
        
        BoundingBox bBox  = new BoundingBox(minX, minY, maxX, maxY);
        
        assertTrue("The method equals() did not return false when the parameter was null.", bBox.equals(null) == false);
    }
    
    private static boolean isEqual(BoundingBox bBox1, double minX, double minY, double maxX, double maxY)
    {
        return bBox1.getMinX() == minX &&
               bBox1.getMinY() == minY &&
               bBox1.getMaxX() == maxX &&
               bBox1.getMaxY() == maxY;
    }
    
    private String printErrorMessage(String methodName, String expectedValue, Coordinate<Double> coordinate, BoundingBox bBox)
    {
        return String.format("Expected the method %s to return %s.\nCoordinate: %s.\nBoundingBox: %s.", methodName, expectedValue, coordinate.toString(), bBox.toString());
    }
    
    private String printErrorMessage(double minXExpected, double minYExpected, double maxXExpected, double maxYExpected, BoundingBox bBoxActual)
    {
        return String.format("The boundingBox did not return the expected values.\nActual: %s.\nExpected: (%f, %f, %f, %f).", 
                bBoxActual.toString(), 
                minXExpected,         minYExpected,         maxXExpected,         maxYExpected);
    }
    
    private String printErrorMessage(Coordinate<Double> coordinate, double expectedX, double expectedY, String methodName)
    {
       return String.format("The %s method in BoundingBox did not return the expected values. Expected: (%f, %f). Actual: %s", 
                            methodName,
                            expectedX, 
                            expectedY, 
                            coordinate.toString());
    }
    
    private String printErrorMessage(BoundingBox bBoxActual,BoundingBox bBoxExpected, String valueExpected)
    {
        return String.format("The boundingBox method equals did not return the expected value of %s.\nBoundingBox: %s.\nBoundingBox other: %s.", 
                             valueExpected,
                             bBoxActual.toString(), 
                             bBoxExpected.toString());
    }
}
