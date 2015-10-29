package com.rgi.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertEquals("Get left should return left value.", 1, (int)integerPair.getLeft());
    }

    @Test
    public void testGetRight()
    {
        final Pair<Integer, Integer> integerPair = new Pair<>(1, 2);
        assertEquals("Get left should return left value.", 2, (int)integerPair.getRight());
    }

    @Test
    public void testEqualsBranchOne()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        assertTrue("Pairs should be equal.", stringIntegerPair.equals(stringIntegerPair));
    }

    @Test
    public void testEqualsBranchTwo()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        final Integer notEqualObject = 1;
        assertFalse("Pairs should be equal.", stringIntegerPair.equals(notEqualObject));
    }

    @Test
    public void testEqualsBranchThree()
    {
        final Pair<String, Integer> stringIntegerPair = new Pair<>("foo", 1);
        assertFalse("Pairs should be equal.", stringIntegerPair.equals(null));
    }

    @Test
    public void testEqualsBranchFour()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("foo", 2);
        assertFalse("Pairs should be equal.", stringIntegerPair1.equals(stringIntegerPair2));
    }

    @Test
    public void testEqualsBranchFive()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("bar", 1);
        assertFalse("Pairs should be equal.", stringIntegerPair1.equals(stringIntegerPair2));
    }

    @Test
    public void testHashcode()
    {
        final Pair<String, Integer> stringIntegerPair1 = new Pair<>("foo", 1);
        final Pair<String, Integer> stringIntegerPair2 = new Pair<>("foo", 1);
        assertEquals("Hashcodes should be equal.", stringIntegerPair1.hashCode(), stringIntegerPair2.hashCode());
    }
}
