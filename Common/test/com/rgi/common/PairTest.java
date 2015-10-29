package com.rgi.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by steven.lander on 10/28/15.
 */
public class PairTest
{
    @Test
    public void testNullPair()
    {
        final Pair<String, String> stringPair1 = new Pair<>(null, null);
        final Pair<String, String> stringPair2 = new Pair<>(null, null);
        assertEquals("Null pairs should evaluate true.", stringPair1, stringPair2);
    }

    @Test
    public void testGetLeft()
    {
        final Pair<Integer, Integer> integerPair = new Pair<>(1, 2);
        //assertEquals(, integerPair.getLeft(), 1);
        assertEquals("Get left should return left value.", 1, (int)integerPair.getLeft());
    }

    /*@Test
    public void testGetRight()
    {

    }

    @Test
    public void testEquals()
    {

    }

    @Test
    public void testHashcode()
    {

    }*/
}
