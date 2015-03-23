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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.rgi.common.Range;

@SuppressWarnings({"javadoc", "static-method"})
public class RangeTest
{
    Comparator<Number> numberComparator = (o1, o2) ->
    {
        final Double value1 = o1.doubleValue();
        final Double value2 = o2.doubleValue();
        return value1.compareTo(value2);
    };

    @Test
    public void verifyRange()
    {
        final double minimum = 80.0;
        final double maximum = 100.0;
        final Range<Double> range = new Range<>(minimum, maximum);

        this.assertRangeValues(range, minimum, maximum);
    }

    @Test
    public void verifyRange2()
    {
        final List<Double>  listValues = new ArrayList<>(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0));
        final Range<Double> range      = new Range<>(listValues, this.numberComparator);
        this.assertRangeValues(range, 0.0, 12.0);
    }

    @Test
    public void verifyRange3()
    {
        final List<Double> listValues =  Arrays.asList(2.0, 0.0, 1.0, 5.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        try(Stream<Double> stream = StreamSupport.stream(listValues.spliterator(), false);)
        {
            final Range<Double> range = new Range<>(stream, this.numberComparator);
            this.assertRangeValues(range, 0.0, 12.0);
        }
    }

    @Test
    public void verifyRange4()
    {
        final List<Double>  listValues = new ArrayList<>(Arrays.asList(11.0, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5));
        final Function<Double, Double> function = t -> t*-1.0;

        final double expectedMin = -12.5;
        final double expectedMax = 1.15;

        final Range<Double> range = new Range<>(listValues, function, this.numberComparator);

        this.assertRangeValues(range, expectedMin, expectedMax);
    }

    @Test
    public void verifyRange5()
    {
        final List<Number>  listValues = new ArrayList<>(Arrays.asList(11, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5, 7.82912381));
        final Function<Number, Integer> function = t -> (int) Math.floor(t.doubleValue());

        final int expectedMin = -2;
        final int expectedMax = 12;

        final Range<Number> range = new Range<>(listValues, function, this.numberComparator);

        this.assertRangeValues(expectedMin, expectedMax, range);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        final List<Number>  listValues = new ArrayList<>(Arrays.asList(11.0, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5));
        final Function<Number, Integer> function = t -> (int) Math.floor(t.doubleValue());

        new Range<>(null, function, this.numberComparator);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is null");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        final List<Number>  listValues = new ArrayList<>();
        final Function<Number, Integer> function = t -> (int) Math.floor(t.doubleValue());

        new Range<>(listValues, function, this.numberComparator);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        final List<Number>  listValues = new ArrayList<>(Arrays.asList(10.0, 100, -12));
        final Function<Number, Integer> function = null;

        new Range<>(listValues, function, this.numberComparator);
        fail("Expected Range to throw an IllegalArgumentException when the Function is null");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        final List<Number>  listValues = new ArrayList<>(Arrays.asList(10.0, 100, -12));
        final Function<Number, Integer> function = t -> (int) Math.floor(t.doubleValue());

        new Range<>(listValues, function, null);
        fail("Expected Range to throw an IllegalArgumentException when the Comparator is null");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        final List<Number>  listValues = new ArrayList<>();
        new Range<>(listValues, this.numberComparator);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException6()
    {
        final List<Number>  listValues = new ArrayList<>(Arrays.asList(12, 2.74, 8.0));
        new Range<>(listValues, null);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException7()
    {
        new Range<>((Iterable<Number>)null, this.numberComparator);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }

    private void assertRangeValues(final Range<Double> range, final double expectedMinimum, final double expectedMaximum)
    {
        assertTrue(String.format("The range did not return the expected values.\nActual: %s.\nExpected: [%s, %s].",
                                 range.toString(),
                                 expectedMinimum,
                                 expectedMaximum),
                   range.getMinimum() == expectedMinimum   &&
                   range.getMaximum() == expectedMaximum);
    }

    private void assertRangeValues(final Number expectedMinimum, final Number expectedMaximum, final Range<Number> range)
    {
        assertTrue(String.format("The range did not return the expected values.\nActual: %s.\nExpected: [%s, %s].",
                                 range.toString(),
                                 expectedMinimum,
                                 expectedMaximum),
                   range.getMinimum() == expectedMinimum   &&
                   range.getMaximum() == expectedMaximum);
    }

}
