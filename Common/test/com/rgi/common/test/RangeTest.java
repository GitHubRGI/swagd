package com.rgi.common.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.rgi.common.Range;

@SuppressWarnings({"javadoc", "static-method"})
public class RangeTest
{

    @Test
    public void verifyRange()
    {
        double minimum = 80.0;
        double maximum = 100.0;
        Range<Double> range = new Range<>(minimum, maximum);
        
        assertTrue(String.format("Range did not return expected values.\nActual: %s.\nExpected: [%f, %f].",
                                 range.toString(), 
                                 minimum, 
                                 maximum),
                   range.getMaximum() == maximum && 
                   range.getMinimum() == minimum);
    }
    
    @Test
    public void verifyRange2()
    {
        //Working on test
        //ArrayList<Number> listValues = new ArrayList<>( Arrays.asList(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0));
        //Iterator<Number> iterator = listValues.iterator();
        
    }
}
