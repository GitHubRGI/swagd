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

package common.coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.rgi.android.common.coordinate.Coordinate;
import com.rgi.android.common.coordinate.CrsCoordinate;

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
        new Coordinate<Integer>(null, 5);
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
        new Coordinate<Double>(5.89, null);
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
        new Coordinate<Number>(null);
        fail("Expected Coordinate to throw an IllegalArgumentException when a null paramter is passed in.");
    }

    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordianteEquals()
    {
        final Double x = new Double(123.321);
        final Double y = new Double(5.123);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Integer> coordinate2 = new Coordinate<Integer>(7,8);
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(coordinate2));
    }

    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals2()
    {
        final Long x = new Long(134);
        final Long y = new Long(-123);
        final Coordinate<Long> coordinate = new Coordinate<Long>(x,y);
        final Integer notCoordinateObject = 10;
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(notCoordinateObject));
    }


    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals3()
    {
        final Integer x = new Integer(134);
        final Integer y = new Integer(-123);
        final Coordinate<Integer> coordinate = new Coordinate<Integer>(x,y);
        assertTrue("The equals method in coordinate returned true when it should have returned false.",
                   !coordinate.equals(null));
    }

    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordianteEquals4()
    {
        final Integer xInt = new Integer(123);
        final Integer yInt = new Integer(5);
        final Double xDoub = new Double(xInt);
        final Double yDoub = new Double(yInt);
        final Coordinate<Double> coordinate = new Coordinate<Double>(xDoub,yDoub);
        final Coordinate<Integer> coordinate2 = new Coordinate<Integer>(xInt,yInt);
        assertTrue("The equals method in coordinate returned false when it should have returned true.",
                   !coordinate.equals(coordinate2));//shouldn't equal because they are different types
    }

    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals5()
    {
        final Double x = new Double(1245.2132123);
        final Double y = new Double(-1231412.123);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Double> coordinate2 = new Coordinate<Double>(x,y);
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
        final Double x = new Double(1245.2132123);
        final Double y = new Double(-1231412.123);
        final Double differentY = new Double(999.222);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Double> coordinate2 = new Coordinate<Double>(x,differentY);
        assertTrue("The equals method in coordinate returned false when it should have returned true.",
                   !coordinate.equals(coordinate2));
    }

    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void coordinateEquals7()
    {
        final Double x = new Double(1245.2132123);
        final Double y = new Double(-1231412.123);
        final Double differentx = new Double(999.222);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Double> coordinate2 = new Coordinate<Double>(differentx,y);
        assertTrue("The equals method in coordinate returned false when it should have returned true.",
                   !coordinate.equals(coordinate2));
    }
    /**
     * Tests if the .equals function in Coordinate returns the expected values
     */
    @Test
    public void crsCoordinateAndCoordinateEquals()
    {
        final CrsCoordinate crsCoord = new CrsCoordinate(4323.3, 32123.12, "EPSG", 3395);
        final Coordinate<Double> otherCoordinate = new Coordinate<Double>(crsCoord);
        assertTrue("The equals method in coordinate returned false when it should have returned true.",
                   !crsCoord.equals(otherCoordinate));//shouldn't equal because they are different types
    }


    /**
     * Tests if the .hashCode function in Coordinate returns the expected values
     */
    @Test
    public void hashCodeTest()
    {
        final CrsCoordinate crsCoord = new CrsCoordinate(4323.3, 32123.12, "EPSG", 3395);
        final Coordinate<Double> otherCoordinate = new Coordinate<Double>(crsCoord);
        assertTrue("The equals method in coordinate returned false when it should have returned true.",
                   crsCoord.hashCode() != otherCoordinate.hashCode());//shouldn't equal because they are different types
    }


    /**
     * Tests if the .hashCode function in Coordinate returns the expected values
     */
    @Test
    public void hashCodeTest2()
    {
        final Double x = new Double(1245.2132123);
        final Double y = new Double(-1231412.123);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Double> coordinate2 = new Coordinate<Double>(x,y);
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
        final Double x = new Double(1245.2132123);
        final Double y = new Double(-1231412.123);
        final Double differentx = new Double(90291.212);
        final Coordinate<Double> coordinate = new Coordinate<Double>(x,y);
        final Coordinate<Double> coordinate2 = new Coordinate<Double>(differentx,y);
        assertTrue(String.format("The equals method in coordinate returned false when it should have returned true. %s, %s",
                                 coordinate.toString(),
                                 coordinate2.toString()),
                   coordinate.hashCode() != coordinate2.hashCode());
    }

}
