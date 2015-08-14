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

import com.rgi.geopackage.extensions.network.Node;

import java.util.Collections;
import java.util.List;

/**
 * @author Luke Lambert
 *
 */
public class AStarVertex
{
    protected AStarVertex(final Node node)
    {
        this.node = node;
    }

    protected AStarVertex(final Node   node,
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
               !(obj == null || this.getClass() != obj.getClass()) && this.node == ((AStarVertex)obj).node;

    }

    @Override
    public int hashCode()
    {
        return this.node.getIdentifier();
    }

    /**
     * @return the costFromStart
     */
    public double getCostFromStart()
    {
        return this.costFromStart;
    }

    /**
     * @return the estimatedCostToEnd
     */
    public double getEstimatedCostToEnd()
    {
        return this.estimatedCostToEnd;
    }

    /**
     * @return the previous
     */
    public AStarVertex getPrevious()
    {
        return this.previous;
    }

    public double getEdgeCost()
    {
        return this.edgeCost;
    }

    public List<Object> getEdgeAttributes()
    {
        return Collections.unmodifiableList(this.edgeAttributes);
    }

    public void update(final double       costFromStart,
                       final double       estimatedCostToEnd,
                       final AStarVertex  previous,
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
        this.edgeAttributes     = edgeAttributes;
        this.edgeCost           = edgeCost;
    }

    /**
     * @return the node
     */
    public Node getNode()
    {
        return this.node;
    }

    private final Node node;

    private double       costFromStart      = Double.MAX_VALUE;
    private double       estimatedCostToEnd = Double.MAX_VALUE;
    private AStarVertex  previous;       // Parent node
    private List<Object> edgeAttributes; // Attributes of edge with the edge being parent to this one
    private double       edgeCost;       // Aalculated cost of parent node to this one
}
