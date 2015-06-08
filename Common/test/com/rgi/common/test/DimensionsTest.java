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

package com.rgi.common.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rgi.common.Dimensions;

<<<<<<< HEAD
@SuppressWarnings({"javadoc", "static-method"})
public class DimensionsTest
{
=======
public class DimensionsTest {
>>>>>>> origin/master

    /**
     * Tests the Dimensions constructor creates a Dimensions object with the
     * given width and height
     */
    @Test
<<<<<<< HEAD
    public void dimensionsConstructorTest()
    {
        final Dimensions<Integer> dim = new Dimensions<>(10, 5);
=======
    @SuppressWarnings({ "unused", "static-method" })
    public void dimensionsConstructorTest()
    {
        final Dimensions<Integer> dim = new Dimensions<Integer>(10, 5);
>>>>>>> origin/master
        assertTrue(dim.getClass().equals(Dimensions.class));
        assertTrue(dim.getWidth() == 10);
        assertTrue(dim.getHeight() == 5);
    }

    /**
     * Tests that getWidth returns the correct width for the Dimensions object
     */
    @Test
<<<<<<< HEAD
    public void getWidthTest()
    {
        final Dimensions<Integer> dim = new Dimensions<>(20, 5);
=======
    @SuppressWarnings({ "unused", "static-method" })
    public void getWidthTest()
    {
        final Dimensions<Integer> dim = new Dimensions<Integer>(20, 5);
>>>>>>> origin/master
        assertTrue(dim.getWidth() == 20);
    }

    /**
     * Tests that getHeight returns the correct height for the Dimensions object
     */
    @Test
<<<<<<< HEAD
    public void getHeightTest()
    {
        final Dimensions<Integer> dim = new Dimensions<>(20, 55);
=======
    @SuppressWarnings({ "unused", "static-method" })
    public void getHeightTest()
    {
        final Dimensions<Integer> dim = new Dimensions<Integer>(20, 55);
>>>>>>> origin/master
        assertTrue(dim.getHeight() == 55);
    }
}
