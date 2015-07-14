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
package com.rgi.suite.tilestoreadapter;

/**
 * Exception indicating that a {@link TileStoreReaderAdapter} or a {@link
 * TileStoreWriterAdapter} has been initialized with a file that does not map
 * that tile store format
 *
 * @author Luke Lambert
 *
 */
public class AdapterMismatchException extends Exception
{
    private static final long serialVersionUID = 8066480795328932111L;


    /**
     * Constructor
     *
     * @param message
     *             The detail message. The detail message is saved for later
     *             retrieval by the {@link #getMessage()} method.
     */
    public AdapterMismatchException(final String message)
    {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause
     *             the cause (which is saved for later retrieval by the {@link
     *             #getCause()} method).  (A <tt>null</tt> value is permitted,
     *             and indicates that the cause is nonexistent or unknown.)
     */
    public AdapterMismatchException(final Exception cause)
    {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message
     *             the detail message (which is saved for later retrieval by
     *             the {@link #getMessage()} method).
     * @param cause
     *             the cause (which is saved for later retrieval by the {@link
     *             #getCause()} method).  (A <tt>null</tt> value is permitted,
     *             and indicates that the cause is nonexistent or unknown.)
     */
    public AdapterMismatchException(final String message, final Exception cause)
    {
        super(message, cause);
    }
}
