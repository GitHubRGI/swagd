package com.rgi.common.util.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.rgi.common.util.*;

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
		Throwable test = new Throwable("Tests error", null);
		assertTrue(ThrowableUtility.getRoot(test).equals(test));
	}
	
	/**
	 * Tests that getRoot correctly returns the
	 * cause of the Throwable object when it is not null
	 */
	@Test
	public void testGetRoot(){
		Throwable cause = new IllegalArgumentException();
		Throwable test = new Throwable("Tests error", cause);
		assertEquals(ThrowableUtility.getRoot(test), cause);
	}
}
