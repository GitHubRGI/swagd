package com.rgi.common.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rgi.common.util.ThrowableUtility;

public class ThrowableUtilityTest {
    /**
     * Tests that getRoot returns null when
     * given a null Throwable object
     */
    @Test
    public void testGetRootNull(){
        assertNull(ThrowableUtility.getRoot(null));
    }

    /**
     * Tests that getRoot returns throwable when
     * given a Throwable object with a null cause
     */
    @Test
    public void testGetRootCause(){
        final Throwable test = new Throwable("Tests error", null);
        assertTrue(ThrowableUtility.getRoot(test).equals(test));
    }

    /**
     * Tests that getRoot correctly returns the
     * cause of the Throwable object when it is not null
     */
    @Test
    public void testGetRoot(){
        final Throwable cause = new IllegalArgumentException();
        final Throwable test = new Throwable("Tests error", cause);
        assertEquals(ThrowableUtility.getRoot(test), cause);
    }
}
