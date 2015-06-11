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

package routing;

/**
 * @author Luke Lambert
 *
 */
public class AStarVertex
{
    private AStarVertex previous = null; // Parent node

    private final int nodeIdentifier;

    private double distanceFromStart = Double.MAX_VALUE;
    private double distanceFromEnd   = Double.MAX_VALUE;

    protected AStarVertex(final int nodeIdentifier)
    {
        this.nodeIdentifier = nodeIdentifier;
    }

    protected AStarVertex(final int nodeIdentifier, final double distanceFromStart, final double distanceFromEnd)
    {
        this(nodeIdentifier);
        this.distanceFromStart = distanceFromStart;
        this.distanceFromEnd   = distanceFromEnd;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(obj == this)
        {
            return true;
        }

        if(obj == null || this.getClass() != obj.getClass())
        {
            return false;
        }

        return this.nodeIdentifier == ((AStarVertex)obj).nodeIdentifier; // Is this enough? Node identifiers should be unique right?
    }

    @Override
    public int hashCode()
    {
        return this.nodeIdentifier;
    }

    @Override
    public String toString()
    {
        return String.format("%d (%f, %f, %d)", this.nodeIdentifier, this.distanceFromStart, this.distanceFromEnd, this.previous.nodeIdentifier);
    }

    /**
     * @return the distanceFromStart
     */
    public double getDistanceFromStart()
    {
        return this.distanceFromStart;
    }

    /**
     * @param distanceFromStart the distanceFromStart to set
     */
    public void setDistanceFromStart(final double distanceFromStart)
    {
        if(distanceFromStart < 0.0)
        {
            throw new IllegalArgumentException("Distance from start may not be less than 0");
        }

        this.distanceFromStart = distanceFromStart;
    }

    /**
     * @return the distanceFromEnd
     */
    public double getDistanceFromEnd()
    {
        return this.distanceFromEnd;
    }

    /**
     * @param distanceFromEnd the distanceFromEnd to set
     */
    public void setDistanceFromEnd(final double distanceFromEnd)
    {
        if(distanceFromEnd < 0.0)
        {
            throw new IllegalArgumentException("Distance from end may not be less than 0");
        }

        this.distanceFromEnd = distanceFromEnd;
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