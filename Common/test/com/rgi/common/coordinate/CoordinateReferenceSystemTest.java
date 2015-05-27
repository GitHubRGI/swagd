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
     * Tests if CoordinateReferenceSystem throws an IllegalArgumentException
     * when a parameter is null or empty
     */
    @SuppressWarnings("unused")
	@Test (expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        new CoordinateReferenceSystem("", "test", 123);
        fail("Expected CoordinateReferenceSystem to throw an IllegalArgumentException when given a null or empty paramter");
    }

    /**
     * Tests that toString returns the correct string
     */
    @Test
    public void testToString()
    {
        final String name = "bob";
        final String authority = "RGI";
        final int id = 123;
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem(name, authority, id);
        assertTrue("CoordinateReferenceSysemt method toString() did not return expected string",crs.toString().equals(String.format("%s:%d - %s", authority, id, name)));
    }

    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest1()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), crs1.getIdentifier());
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
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("Different Authority", crs1.getIdentifier());
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
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), 888);
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
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        assertTrue("The equals method returned true when it should have returned false.",
                   !crs1.equals(null));
    }

    /**
     * Tests if the .equals method returns the expected values
     */
    @Test
    public void equalsTest5()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final Double differentObject = new Double(291.2);
        assertTrue("The equals method returned true when it should have returned false.",
                   !crs1.equals(differentObject));
    }
    /**
     * Tests if the hashCode function returns the values expected
     */
    @Test
    public void hashCodeTest()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem(crs1.getAuthority(), crs1.getIdentifier());
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
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("different authority", crs1.getIdentifier());
        assertTrue(String.format("The hashcode method returned same value when it should have returned different hashCodes. Crs's hashCodes Compared: %d, %d.",
                                   crs1.hashCode(), crs2.hashCode()),
                     crs1.hashCode() != crs2.hashCode());
    }

    /**
     * Tests compareTo throws a NullPointerException when
     * given an null parameter
     */
    @Test (expected = NullPointerException.class)
    public void testCompareTo1()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = null;
        crs1.compareTo(crs2);
        fail("Expected CoordinateReferenceSystem method compareTo to throw a NullPointerException when given a null parameter");
    }

    /**
     * Tests compareTo when the authorities are different
     */
    @Test
    public void testCompareTo2()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("Different authority", 123);
        assertTrue("Expected CoordinateReferenceSystem method compareTo to return a nonzero integer.", crs1.compareTo(crs2) != 0);
    }

    /**
     * Tests compareTo when the identifiers are different
     */
    @Test
    public void testCompareTo3()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("Authority", 123);
        assertTrue("Expected CoordinateReferenceSystem method compareTo to return a nonzero integer.", crs1.compareTo(crs2) != 0);
    }

    /**
     * Tests compareTo when the CoordinateReferenceSystems are the same
     */
    @Test
    public void testCompareTo4()
    {
        final CoordinateReferenceSystem crs1 = new CoordinateReferenceSystem("Authority", 555);
        final CoordinateReferenceSystem crs2 = new CoordinateReferenceSystem("Authority", 555);
        assertTrue("Expected CoordinateReferenceSystem method comparTo to return a nonzero integer.", crs1.compareTo(crs2) == 0);
    }
}
