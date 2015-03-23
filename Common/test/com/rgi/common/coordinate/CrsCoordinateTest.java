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
public class CrsCoordinateTest
{
    /**
     * Tests if CrsCoordinate will throw an illegal argument exception when the parameters are null
     * or empty
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException1()
    {
        new CrsCoordinate(5.8, 7.6, null);
        fail("Expected CrsCoordinate to throw an IllegalArgumentException when passing a paramter that is null.");
    }

    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crsCoord.getCoordinateReferenceSystem());
        assertEquals("Expected the equals method to return true but instead returned false.", crsCoord, crsCoord2);
    }

    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest2()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        assertTrue("Expected the equals method to return false but instead returned true.", !crsCoord.equals(null));
    }

    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest3()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final Boolean differentObject = new Boolean(true);
        assertTrue("Expected the equals method to return false but instead returned true.", !crsCoord.equals(differentObject));
    }

    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest4()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crs.getAuthority(), 899);
        assertTrue("Expected the equals method to return true but instead returned false.", !crsCoord.equals(crsCoord2));
    }

    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest5()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final double differentY = -1923.282;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final CrsCoordinate crsCoord2 = new CrsCoordinate(x,differentY, crs);
        assertTrue("Expected the equals method to return true but instead returned false.", !crsCoord.equals(crsCoord2));
    }

    /**
     * Test if the hashCodes are unique or the same when expected
     */
    @Test
    public void hashCodeTest()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crsCoord.getCoordinateReferenceSystem());
        assertEquals("Expected the hashcodes to be the same value instead they returned different values.", crsCoord.hashCode(), crsCoord2.hashCode());
    }

    /**
     * Test if the hashCodes are unique or the same when expected
     */
    @Test
    public void hashCodeTest2()
    {
        final double x = 7281.291;
        final double y = 89120.212;
        final double differentY = -1923.282;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        final CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        final CrsCoordinate crsCoord2 = new CrsCoordinate(x,differentY, crs);
        assertTrue("Expected the equals method to return true but instead returned false.", crsCoord.hashCode() != crsCoord2.hashCode());
    }
}
