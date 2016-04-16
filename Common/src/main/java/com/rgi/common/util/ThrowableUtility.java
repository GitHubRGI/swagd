package com.rgi.common.util;

/**
 * Utilities for {@link Throwable} objects
 *
 * @author Luke Lambert
 *
 */
public class ThrowableUtility
{
    /**
     * Calls {@link Throwable#getCause()} recursively and to return the root
     * cause of a {@link Throwable}, or null if the input was null
     *
     * @param th
     *             {@link Throwable} object
     * @return the root cause of a {@link Throwable}, or null if the input was
     *             null
     */
    public static Throwable getRoot(final Throwable th)
    {
        Throwable root = th;

        while(root != null && root.getCause() != null)
        {
            root = root.getCause();
        }

        return root;
    }
}
