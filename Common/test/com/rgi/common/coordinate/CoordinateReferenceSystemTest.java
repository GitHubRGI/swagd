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
public class CoordinateReferenceSystemTest
{
   
    /**
     * Tests if CoordinateReferenceSystem throws an IllegalArgumentException
     * when a parameter is null or empty
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        new CoordinateReferenceSystem(null, 123);
        fail("Expected CoordinateReferenceSystem to throw an IllegalArgumentException when given a null or empty paramter");
    }
    
    /**
     * Tests if CoordinateReferenceSystem throws an IllegalArgumentException
     * when a parameter is null or empty
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        new CoordinateReferenceSystem("", 123);
        fail("Expected CoordinateReferenceSystem to throw an IllegalArgumentException when given a null or empty paramter");
    }
    
    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest1()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), crs1.getIdentifier());
        assertEquals(String.format("The equals method returned false when it should have returned true. CrsCompared: %s, %s.", 
                                   crs1.toString(), crs2.toString()),
                     crs1, crs2);
    }
    
    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest2()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("Different Authority", crs1.getIdentifier());
        assertTrue(String.format("The equals method returned true when it should have returned false. CrsCompared: %s, %s.", 
                                   crs1.toString(), crs2.toString()),
                     !crs1.equals(crs2));
    }
    
    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest3()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), 888);
        assertTrue(String.format("The equals method returned true when it should have returned false. CrsCompared: %s, %s.", 
                                   crs1.toString(), crs2.toString()),
                     !crs1.equals(crs2));
    }
    
    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest4()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        assertTrue("The equals method returned true when it should have returned false.",
                   !crs1.equals(null));
    }
    
    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest5()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        Double differentObject = new Double(291.2);
        assertTrue("The equals method returned true when it should have returned false.",
                   !crs1.equals(differentObject));
    }
    /**
     * Tests if the hashCode function returns the values expected
     */
    @Test
    public void hashCodeTest()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), crs1.getIdentifier());
        assertEquals(String.format("The hashcode method returned different values when it should have returned the same hashCode. Crs's hashCodes Compared: %d, %d.", 
                                   crs1.hashCode(), crs2.hashCode()),
                     crs1.hashCode(), crs2.hashCode());
    }
    
    /**
     * Tests if the hashCode function returns the values expected
     */
    @Test
    public void hashCodeTest2()
    {
        CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("different authority", crs1.getIdentifier());
        assertTrue(String.format("The hashcode method returned same value when it should have returned different hashCodes. Crs's hashCodes Compared: %d, %d.", 
                                   crs1.hashCode(), crs2.hashCode()),
                     crs1.hashCode() != crs2.hashCode());
    }
}
