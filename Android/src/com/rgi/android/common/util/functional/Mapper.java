package com.rgi.android.common.util.functional;

public interface Mapper<I, O>
{
    public O apply(final I input);
}
