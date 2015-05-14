package com.rgi.common.util.functional;

/**
 * @author Luke Lambert
 *
 * @param <I> the type of the input to the operation
 * @param <O> the type of the output to the operation
 */
public interface Function<I, O>
{
    /**
     * Returns output type that maps from the input type given
     *
     * @param input
     *      the type of input to the operation
     * @return
     *      the type of output to the operation based on this input
     */
    public O apply(final I input);
}
