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

package com.rgi.geopackage.extensions.routing;

/**
 * @author Luke Lambert
 *
 */
public class AStarVertex
{
    private AStarVertex previous; // Parent node

    private final int nodeIdentifier;

    private double costFromStart = Double.MAX_VALUE;
    private double costToEnd     = Double.MAX_VALUE;

    protected AStarVertex(final int nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
    }

    protected AStarVertex(final int nodeIdentifier, final double costFromStart, final double costToEnd)
    {
        this(nodeIdentifier);

        this.costFromStart = costFromStart;
        this.costToEnd     = costToEnd;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj == this ||
               !(obj == null || this.getClass() != obj.getClass()) && this.nodeIdentifier == ((AStarVertex)obj).nodeIdentifier;

    }

    @Override
    public int hashCode()
    {
        return this.nodeIdentifier;
    }

    @Override
    public String toString()
    {
        return String.format("%d (%f, %f, %d)",
                             this.nodeIdentifier,
                             this.costFromStart,
                             this.costToEnd,
                             this.previous.nodeIdentifier);
    }

    /**
     * @return the costFromStart
     */
    public double getCostFromStart()
    {
        return this.costFromStart;
    }

    /**
     * @param costFromStart the costFromStart to set
     */
    public void setCostFromStart(final double costFromStart)
    {
        if(costFromStart < 0.0)
        {
            throw new IllegalArgumentException("Distance from start may not be less than 0");
        }

        this.costFromStart = costFromStart;
    }

    /**
     * @return the costToEnd
     */
    public double getCostToEnd()
    {
        return this.costToEnd;
    }

    /**
     * @param costToEnd the costToEnd to set
     */
    public void setCostToEnd(final double costToEnd)
    {
        if(costToEnd < 0.0)
        {
            throw new IllegalArgumentException("Distance from end may not be less than 0");
        }

        this.costToEnd = costToEnd;
    }

    /**
     * @return the previous
     */
    public AStarVertex getPrevious()
    {
        return this.previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(final AStarVertex previous)
    {
        this.previous = previous;
    }

    /**
     * @return the nodeIdentifier
     */
    public int getNodeIdentifier()
    {
        return this.nodeIdentifier;
    }
}
