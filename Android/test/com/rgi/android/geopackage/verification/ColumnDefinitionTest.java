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

package com.rgi.android.geopackage.verification;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Mary Carome
 *
 */
<<<<<<< HEAD
@SuppressWarnings({ "static-method" })
=======
>>>>>>> origin/master
public class ColumnDefinitionTest
{

    /**
     * Tests that the ColumnDefintion constructor throws an
     * IllegalArgumentException when passed a null SQL type
     */
<<<<<<< HEAD
    @Test(expected = IllegalArgumentException.class)
    public void columnDefinitionIllegalArgumentException()
    {
        @SuppressWarnings("unused")
        final ColumnDefinition colDef = new ColumnDefinition(null, true, true, true, "default");
=======
    @Test (expected = IllegalArgumentException.class)
    public void columnDefinitionIllegalArgumentException()
    {
        @SuppressWarnings("unused")
        final
        ColumnDefinition colDef = new ColumnDefinition(null, true, true, true, "default");
>>>>>>> origin/master
        fail("Expected ColumnDefintion to throw an IllegalArgumentException when passed a null SQL type.");
    }

    /**
<<<<<<< HEAD
     * Tests that equals returns false when passed null instead of a valid
     * object
=======
     * Tests that equals returns false when
     * passed null instead of a valid object
>>>>>>> origin/master
     */
    @Test
    public void testEquals1()
    {
        final ColumnDefinition def1 = new ColumnDefinition("Test", true, true, true, "default");
<<<<<<< HEAD
        assertFalse("Expected ColumnDefintion method equals(Object) to return false when passed null instead of an Object.", def1.equals(null));
    }

    /**
     * Tests that equals returns false when passed an Object that is not type
     * ColumnDefinition
=======
        assertFalse("Expected ColumnDefintion method equals(Object) to return false when passed null instead of an Object.",def1.equals(null));
    }

    /**
     * Tests that equals returns false when
     * passed an Object that is not type ColumnDefinition
>>>>>>> origin/master
     */
    @Test
    public void testEquals2()
    {
        final ColumnDefinition def1 = new ColumnDefinition("Test", true, true, true, "default");
        final Object test = new String();
<<<<<<< HEAD
        assertFalse("Expected ColumnDefintion method equals(Object) to return false when passed an Object not of type ColumnDefintion.", def1.equals(test));
    }

    /**
     * Tests that equals returns true when passed itself (Tests that equals is
     * reflexive)
=======
        assertFalse("Expected ColumnDefintion method equals(Object) to return false when passed an Object not of type ColumnDefintion.",def1.equals(test));
    }

    /**
     * Tests that equals returns true when passed itself
     * (Tests that equals is reflexive)
>>>>>>> origin/master
     */
    @Test
    public void testEquals3()
    {
        final ColumnDefinition def1 = new ColumnDefinition("test", true, true, true, "default");
        final Object def2 = def1;
        assertTrue("Expected ColumnDefintion method equals(Object) to return true when passed itself.", def1.equals(def2));
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return true and identical hash codes for two
     * identical ColumnDefinition objects
=======
     * Verifies equals and hashCode return true
     * and identical hash codes for two identical ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash1()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);

        assertTrue("Expected ColumnDefinition method equals(Object) to return true when given two identical ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the same HashCode for identical ColumnDefinitions.", def1.hashCode() == def2.hashCode());
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return false and different hash codes for
     * different ColumnDefinition objects
=======
     * Verifies equals and hashCode return false
     * and different hash codes for different
     * ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash2()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final String sqlType2 = "different type";

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType2, notNull, primaryKey, unique, defaultValue);

        assertFalse("Expected ColumnDefinition method equals(Object) to return false when given two different ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the differnt hash codes for two differnt ColumnDefinitions.", def1.hashCode() != def2.hashCode());
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return false and different hash codes for
     * different ColumnDefinition objects
=======
     * Verifies equals and hashCode return false
     * and different hash codes for different
     * ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash3()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final boolean notNull2 = false;

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType, notNull2, primaryKey, unique, defaultValue);

        assertFalse("Expected ColumnDefinition method equals(Object) to return false when given two different ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the differnt hash codes for two differnt ColumnDefinitions.", def1.hashCode() != def2.hashCode());
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return false and different hash codes for
     * different ColumnDefinition objects
=======
     * Verifies equals and hashCode return false
     * and different hash codes for different
     * ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash4()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final boolean primaryKey2 = true;

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType, notNull, primaryKey2, unique, defaultValue);

        assertFalse("Expected ColumnDefinition method equals(Object) to return false when given two different ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the differnt hash codes for two differnt ColumnDefinitions.", def1.hashCode() != def2.hashCode());
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return false and different hash codes for
     * different ColumnDefinition objects
=======
     * Verifies equals and hashCode return false
     * and different hash codes for different
     * ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash5()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final boolean unique2 = false;

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType, notNull, primaryKey, unique2, defaultValue);

        assertFalse("Expected ColumnDefinition method equals(Object) to return false when given two different ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the differnt hash codes for two differnt ColumnDefinitions.", def1.hashCode() != def2.hashCode());
    }

    /**
<<<<<<< HEAD
     * Verifies equals and hashCode return false and different hash codes for
     * different ColumnDefinition objects
=======
     * Verifies equals and hashCode return false
     * and different hash codes for different
     * ColumnDefinition objects
>>>>>>> origin/master
     */
    @Test
    public void testEqualsAndHash6()
    {
        final String sqlType = "test";
        final boolean notNull = true;
        final boolean primaryKey = false;
        final boolean unique = true;
        final String defaultValue = "default";

        final String defaultValue2 = "different default";

        final ColumnDefinition def1 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue);
        final ColumnDefinition def2 = new ColumnDefinition(sqlType, notNull, primaryKey, unique, defaultValue2);

        // Check for default value is commented out in equals method, so two ColumnDefinitions with different default values will be the same
        //assertFalse("Expected ColumnDefinition method equals(Object) to return false when given two different ColumnDefinitions", def1.equals(def2));
        assertTrue("Expected ColumnDefition method hashCode to return the differnt hash codes for two differnt ColumnDefinitions.", def1.hashCode() != def2.hashCode());
    }
}
