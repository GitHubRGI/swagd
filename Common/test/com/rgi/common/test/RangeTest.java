package com.rgi.common.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.rgi.common.Range;

@SuppressWarnings({"javadoc", "static-method"})
public class RangeTest
{
    Comparator<Double> comparator = new Comparator<Double>()
            {
                @Override
                public int compare(Double o1, Double o2)
                {
                    return o1.compareTo(o2);
                }
            };

    @Test
    public void verifyRange()
    {
        double minimum = 80.0;
        double maximum = 100.0;
        Range<Double> range = new Range<>(minimum, maximum);
        
        assertRangeValues(range, minimum, maximum);
    }
    
    @Test
    public void verifyRange2()
    {
        //Working on test
        List<Double>  listValues = new ArrayList<>(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0));
        Range<Double> range      = new Range<>(listValues, this.comparator);
        assertRangeValues(range, 0.0, 12.0);
    }
    
    @Test
    public void verifyRange3()
    {
        List<Double> listValues =  Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        try(Stream<Double> stream = StreamSupport.stream(listValues.spliterator(), false);)
        {
            Range<Double> range = new Range<>(stream, this.comparator);
            assertRangeValues(range, 0.0, 12.0);
        }
    }
    
    private void assertRangeValues(Range<Double> range, double expectedMinimum, double expectedMaximum)
    {
        assertTrue(String.format("The range did not return the expected values.\nActual: %s.\nExpected: [%s, %s].",
                                 range.toString(), 
                                 expectedMinimum, 
                                 expectedMaximum),
                   range.getMinimum() == expectedMinimum   && 
                   range.getMaximum() == expectedMaximum);
    }
    
}
