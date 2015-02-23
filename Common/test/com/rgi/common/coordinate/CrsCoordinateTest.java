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
        double x = 7281.291;
        double y = 89120.212;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crsCoord.getCoordinateReferenceSystem());
        assertEquals("Expected the equals method to return true but instead returned false.", crsCoord, crsCoord2);
    }
    
    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest2()
    {
        double x = 7281.291;
        double y = 89120.212;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        assertTrue("Expected the equals method to return false but instead returned true.", !crsCoord.equals(null));
    }
    
    /**
     * Tests if the equals method returns the expected values
     */
    @Test
    public void equalsTest3()
    {
        double x = 7281.291;
        double y = 89120.212;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        Boolean differentObject = new Boolean(true);
        assertTrue("Expected the equals method to return false but instead returned true.", !crsCoord.equals(differentObject));
    }
    
    /**
     * Tests if the equals method returns the expected values
     */
    @Test 
    public void equalsTest4()
    {
        double x = 7281.291;
        double y = 89120.212;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crs.getAuthority(), 899);
        assertTrue("Expected the equals method to return true but instead returned false.", !crsCoord.equals(crsCoord2));
    }
    
    /**
     * Tests if the equals method returns the expected values
     */
    @Test 
    public void equalsTest5()
    {
        double x = 7281.291;
        double y = 89120.212;
        double differentY = -1923.282;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        CrsCoordinate crsCoord2 = new CrsCoordinate(x,differentY, crs);
        assertTrue("Expected the equals method to return true but instead returned false.", !crsCoord.equals(crsCoord2));
    }
    
    /**
     * Test if the hashCodes are unique or the same when expected
     */
    @Test
    public void hashCodeTest()
    {
        double x = 7281.291;
        double y = 89120.212;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        CrsCoordinate crsCoord2 = new CrsCoordinate(x,y, crsCoord.getCoordinateReferenceSystem());
        assertEquals("Expected the hashcodes to be the same value instead they returned different values.", crsCoord.hashCode(), crsCoord2.hashCode());
    }
    
    /**
     * Test if the hashCodes are unique or the same when expected
     */
    @Test
    public void hashCodeTest2()
    {
        double x = 7281.291;
        double y = 89120.212;
        double differentY = -1923.282;
        CoordinateReferenceSystem crs = new CoordinateReferenceSystem("authority", 882);
        CrsCoordinate crsCoord = new CrsCoordinate(x,y, crs);
        CrsCoordinate crsCoord2 = new CrsCoordinate(x,differentY, crs);
        assertTrue("Expected the equals method to return true but instead returned false.", crsCoord.hashCode() != crsCoord2.hashCode());
    }
}
