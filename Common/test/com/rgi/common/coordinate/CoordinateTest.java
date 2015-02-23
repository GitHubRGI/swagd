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

package com.rgi.common.coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings("static-method")
public class CoordinateTest
{
    
    /**
     * Tests if Coordinate class will throw an IllegalArgumentException
     * when passed a null value as a parameter
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException1()
    {
        new Coordinate<>(null, 5);
        fail("Expected Coordinate to throw an IllegalArgumentException when a null paramter is passed in.");
    }
    
    /**
     * Tests if Coordinate class will throw an IllegalArgumentException
     * when passed a null value as a parameter
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new Coordinate<>(5.89, null);
        fail("Expected Coordinate to throw an IllegalArgumentException when a null paramter is passed in.");
    }
    
    /**
     * Tests if Coordinate class will throw an IllegalArgumentException
     * when passed a null value as a parameter
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        new Coordinate<>(null);
        fail("Expected Coordinate to throw an IllegalArgumentException when a null paramter is passed in.");
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordianteEquals()
    {
        Double x = new Double(123.321);
        Double y = new Double(5.123);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Integer> coordinate2 = new Coordinate<>(7,8);
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(coordinate2));
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals2()
    {
        Long x = new Long(134);
        Long y = new Long(-123);
        Coordinate<Long> coordinate = new Coordinate<>(x,y);
        Integer notCoordinateObject = 10;
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(notCoordinateObject));
    }
    
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals3()
    {
        Integer x = new Integer(134);
        Integer y = new Integer(-123);
        Coordinate<Integer> coordinate = new Coordinate<>(x,y);
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(null));
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordianteEquals4()
    {
        Integer xInt = new Integer(123);
        Integer yInt = new Integer(5);
        Double xDoub = new Double(xInt);
        Double yDoub = new Double(yInt);
        Coordinate<Double> coordinate = new Coordinate<>(xDoub,yDoub);
        Coordinate<Integer> coordinate2 = new Coordinate<>(xInt,yInt);
        assertTrue("The equals method in coordinate returned false when it should have returned true.", 
                   !coordinate.equals(coordinate2));//shouldn't equal because they are different types
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals5()
    {
        Double x = new Double(1245.2132123);
        Double y = new Double(-1231412.123);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Double> coordinate2 = new Coordinate<>(x,y);
        assertEquals("The equals method in coordinate returned false when it should have returned true.", 
                     coordinate, 
                     coordinate2);
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals6()
    {
        Double x = new Double(1245.2132123);
        Double y = new Double(-1231412.123);
        Double differentY = new Double(999.222);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Double> coordinate2 = new Coordinate<>(x,differentY);
        assertTrue("The equals method in coordinate returned false when it should have returned true.", 
                   !coordinate.equals(coordinate2));
    }
    
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals7()
    {
        Double x = new Double(1245.2132123);
        Double y = new Double(-1231412.123);
        Double differentx = new Double(999.222);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Double> coordinate2 = new Coordinate<>(differentx,y);
        assertTrue("The equals method in coordinate returned false when it should have returned true.", 
                   !coordinate.equals(coordinate2));
    }
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void crsCoordinateAndCoordinateEquals()
    {
        CrsCoordinate crsCoord = new CrsCoordinate(4323.3, 32123.12, "EPSG", 3395);
        Coordinate<Double> otherCoordinate = new Coordinate<>(crsCoord);
        assertTrue("The equals method in coordinate returned false when it should have returned true.", 
                   !crsCoord.equals(otherCoordinate));//shouldn't equal because they are different types
    }
    
    
    /**
     * Tests if the .hashCode function in Coordinate returns the expected values
     */
    @Test
    public void hashCodeTest()
    {
        CrsCoordinate crsCoord = new CrsCoordinate(4323.3, 32123.12, "EPSG", 3395);
        Coordinate<Double> otherCoordinate = new Coordinate<>(crsCoord);
        assertTrue("The equals method in coordinate returned false when it should have returned true.", 
                   crsCoord.hashCode() != otherCoordinate.hashCode());//shouldn't equal because they are different types
    }
    
    
    /**
     * Tests if the .hashCode function in Coordinate returns the expected values
     */
    @Test
    public void hashCodeTest2()
    {
        Double x = new Double(1245.2132123);
        Double y = new Double(-1231412.123);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Double> coordinate2 = new Coordinate<>(x,y);
        assertEquals("The equals method in coordinate returned false when it should have returned true.", 
                     coordinate.hashCode(), 
                     coordinate2.hashCode());
    }
    
    /**
     * Tests if the .hashCode function in Coordinate returns the expected values
     */
    @Test
    public void hashCodeTest3()
    {
        Double x = new Double(1245.2132123);
        Double y = new Double(-1231412.123);
        Double differentx = new Double(90291.212);
        Coordinate<Double> coordinate = new Coordinate<>(x,y);
        Coordinate<Double> coordinate2 = new Coordinate<>(differentx,y);
        assertTrue(String.format("The equals method in coordinate returned false when it should have returned true. %s, %s", 
                                 coordinate.toString(), 
                                 coordinate2.toString()), 
                   coordinate.hashCode() != coordinate2.hashCode());
    }

}
