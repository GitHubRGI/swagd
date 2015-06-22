package com.rgi.android.common.util.functional;

/**
 * @author Luke.Lambert
 *
 * @param <T> Type to operate on
 */
public interface BinaryOperator<T>
{
    /**
     * Applies this function to the given arguments.
     *
     * @param lhs
     *             the first function argument
     * @param rhs
     *             the second function argument
     * @return the function result
     */
    public T apply(final T lhs, final T rhs);
}
