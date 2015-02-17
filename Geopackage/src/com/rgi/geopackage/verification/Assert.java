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

package com.rgi.geopackage.verification;

import java.util.Arrays;

/**
 *
 * @author Luke Lambert
 *
 */
public class Assert
{
    /**
     * @param message The error message that is displayed when the arrays do not equal
     * @param expecteds The expected values of the array
     * @param actuals the actual values of the array
     * @throws AssertionError throws when the arrays are not equal
     */
    public static void assertArrayEquals(final String message, final byte[] expecteds, final byte[] actuals) throws AssertionError
    {
        if(Arrays.equals(expecteds, actuals) == false)
        {
            throw new AssertionError(message);
        }
    }

    /**
     * @param condition a relational expression that returns true or false
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final boolean condition) throws AssertionError
    {
        Assert.assertTrue(null, condition);
    }

    /**
     * @param message the message that is displayed when the Assertion Error is thrown
     * @param condition the relational expression that returns true or false
     * @throws AssertionError throws when the condition is false
     */
    static public void assertTrue(final String message, final boolean condition) throws AssertionError
    {
        if(condition == false)
        {
            Assert.fail(message);
        }
    }

    /**
     * @param message the message that is displayed when the Assertion Error is thrown
     * @throws AssertionError always is thrown
     */
    static public void fail(final String message) throws AssertionError
    {
        if(message == null)
        {
            throw new AssertionError();
        }

        throw new AssertionError(message);
    }
}
