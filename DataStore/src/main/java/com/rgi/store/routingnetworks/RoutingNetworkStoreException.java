/*
 * The MIT License (MIT)
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

package com.rgi.store.routingnetworks;

/**
 * Exception specific to tile store readers and writers
 *
 * @author Steven D. Lander
 *
 */
@SuppressWarnings("serial")
public class RoutingNetworkStoreException extends Exception
{
    /**
     * Constructor
     *
     * @param message
     *             The exception message as a String when a TileStoreException is thrown
     */
    public RoutingNetworkStoreException(final String message)
    {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause
     *             The Throwable object that indicates the cause of the TileStoreException
     */
    public RoutingNetworkStoreException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor
     *
     * @param message
     *             The Exception message as a String when a TileStoreException is thrown
     * @param cause
     *             The Throwable object that indicates the cause of the TileStoreException
     */
    public RoutingNetworkStoreException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
