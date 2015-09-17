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

package com.rgi.geopackage.extensions.routing.router.astar;

import com.rgi.geopackage.extensions.network.AttributedNode;

import java.util.Collections;
import java.util.List;

/**
 * @author Luke Lambert
 *
 */
class Vertex
{
    Vertex(final AttributedNode node)
    {
        this.node = node;
    }

    Vertex(final AttributedNode node,
           final double costFromStart,
           final double estimatedCostToEnd)
    {
        this(node);

        this.costFromStart      = costFromStart;
        this.estimatedCostToEnd = estimatedCostToEnd;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return obj == this ||
               !(obj == null || this.getClass() != obj.getClass()) && this.node == ((Vertex)obj).node;

    }

    @Override
    public int hashCode()
    {
        return this.node.getIdentifier();
    }

    /**
     * @return the node
     */
    AttributedNode getNode()
    {
        return this.node;
    }

    /**
     * @return the costFromStart
     */
    double getCostFromStart()
    {
        return this.costFromStart;
    }

    /**
     * @return the estimatedCostToEnd
     */
    double getEstimatedCostToEnd()
    {
        return this.estimatedCostToEnd;
    }

    /**
     * @return the previous
     */
    Vertex getPrevious()
    {
        return this.previous;
    }

    double getEdgeCost()
    {
        return this.edgeCost;
    }

    int getEdgeIdentifier()
    {
        return this.edgeIdentifier;
    }

    List<Object> getEdgeAttributes()
    {
        return Collections.unmodifiableList(this.edgeAttributes);
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    void update(final double       costFromStart,
                final double       estimatedCostToEnd,
                final Vertex       previous,
                final int          edgeIdentifier,
                final List<Object> edgeAttributes,
                final double       edgeCost)
    {
        if(costFromStart < 0.0)
        {
            throw new IllegalArgumentException("Distance from start may not be less than 0");
        }

        if(estimatedCostToEnd < 0.0)
        {
            throw new IllegalArgumentException("Distance from end may not be less than 0");
        }

        this.costFromStart      = costFromStart;
        this.estimatedCostToEnd = estimatedCostToEnd;
        this.previous           = previous;
        this.edgeIdentifier     = edgeIdentifier;
        this.edgeAttributes     = edgeAttributes;
        this.edgeCost           = edgeCost;
    }

    private final AttributedNode node;

    private double       costFromStart      = Double.MAX_VALUE;
    private double       estimatedCostToEnd = Double.MAX_VALUE;
    private Vertex       previous;                              // Parent node
    private int          edgeIdentifier;                        // Unique identifier of the edge taken to get to this node
    private List<Object> edgeAttributes;                        // Attributes of edge with the edge being parent to this one
    private double       edgeCost;                              // Calculated cost of parent node to this one
}
