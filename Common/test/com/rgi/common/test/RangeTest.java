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
    Comparator<Number> numberComparartor = new Comparator<Number>()
            {
                @Override
                public int compare(Number o1, Number o2)
                {
                    Double value1 = o1.doubleValue();
                    Double value2 = o2.doubleValue();
                    return value1.compareTo(value2);
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
        List<Double>  listValues = new ArrayList<>(Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0));
        Range<Double> range      = new Range<>(listValues, this.numberComparartor);
        assertRangeValues(range, 0.0, 12.0);
    }
    
    @Test
    public void verifyRange3()
    {
        List<Double> listValues =  Arrays.asList(2.0, 0.0, 1.0, 5.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0);
        try(Stream<Double> stream = StreamSupport.stream(listValues.spliterator(), false);)
        {
            Range<Double> range = new Range<>(stream, this.numberComparartor);
            assertRangeValues(range, 0.0, 12.0);
        }
    }
    
    @Test
    public void verifyRange4()
    {
        List<Double>  listValues = new ArrayList<>(Arrays.asList(11.0, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5));
        Function<Double, Double> function = new Function<Double, Double>(){

            @Override
            public Double apply(Double t)
            {
                return t*-1.0;
            }};
            
        double expectedMin = -12.5;
        double expectedMax = 1.15;
        
        Range<Double> range = new Range<>(listValues, function, this.numberComparartor);
        
        assertRangeValues(range, expectedMin, expectedMax);
    }
    
    @Test
    public void verifyRange5()
    {
        List<Number>  listValues = new ArrayList<>(Arrays.asList(11, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5, 7.82912381));
        Function<Number, Integer> function = new Function<Number, Integer>(){

            @Override
            public Integer apply(Number t)
            {
                return (int) Math.floor(t.doubleValue());
            }};
            
        int expectedMin = -2;
        int expectedMax = 12;
        
        Range<Number> range = new Range<>(listValues, function, this.numberComparartor);
        
        assertRangeValues(expectedMin, expectedMax, range);
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException()
    {
        List<Number>  listValues = new ArrayList<>(Arrays.asList(11.0, 12.5, -1.0, -1.15, -1.0, -1.15, 2.0, 5.0, 10.0, 12.5, 12.5));
        Function<Number, Integer> function = new Function<Number, Integer>(){

            @Override
            public Integer apply(Number t)
            {
                return (int) Math.floor(t.doubleValue());
            }};
            
        new Range<>(null, function, this.numberComparartor);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is null");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException2()
    {
        List<Number>  listValues = new ArrayList<>();
        Function<Number, Integer> function = new Function<Number, Integer>(){

            @Override
            public Integer apply(Number t)
            {
                return (int) Math.floor(t.doubleValue());
            }};
            
        new Range<>(listValues, function, this.numberComparartor);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException3()
    {
        List<Number>  listValues = new ArrayList<>(Arrays.asList(10.0, 100, -12));
        Function<Number, Integer> function = null;
            
        new Range<>(listValues, function, this.numberComparartor);
        fail("Expected Range to throw an IllegalArgumentException when the Function is null");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException4()
    {
        List<Number>  listValues = new ArrayList<>(Arrays.asList(10.0, 100, -12));
        Function<Number, Integer> function = new Function<Number, Integer>(){
            @Override
            public Integer apply(Number t)
            {
                return (int) Math.floor(t.doubleValue());
            }};
            
        new Range<>(listValues, function, null);
        fail("Expected Range to throw an IllegalArgumentException when the Comparator is null");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException5()
    {
        List<Number>  listValues = new ArrayList<>();
        new Range<>(listValues, this.numberComparartor);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
    }
    
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentException6()
    {
        List<Number>  listValues = new ArrayList<>(Arrays.asList(12, 2.74, 8.0));
        new Range<>(listValues, null);
        fail("Expected Range to throw an IllegalArgumentException when the Iterable is empty");
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
    
    private void assertRangeValues(Number expectedMinimum, Number expectedMaximum, Range<Number> range)
    {
        assertTrue(String.format("The range did not return the expected values.\nActual: %s.\nExpected: [%s, %s].",
                                 range.toString(), 
                                 expectedMinimum, 
                                 expectedMaximum),
                   range.getMinimum() == expectedMinimum   && 
                   range.getMaximum() == expectedMaximum);
    }
    
}
