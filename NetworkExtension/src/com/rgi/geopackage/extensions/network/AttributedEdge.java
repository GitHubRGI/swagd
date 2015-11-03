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

package com.rgi.geopackage.extensions.network;

import java.util.Collections;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class AttributedEdge
{
    /**
     * Constructor
     *
     * @param edgeIdentifier
     *             Unique edge identifier
     * @param edgeAttributes
     *             All or a subset of this edge's attributes
     * @param fromNode
     *             Unique 'from' node identifier, and attributes
     * @param toNode
     *             Unique 'to' node identifier, and attributes
     */
    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")    // Suppress this warning for routing performance reasons. *Many* of these objects are created while routing so constantly recopying the arrays seems like a bad idea.
    public AttributedEdge(final int edgeIdentifier,
                          final List<Object> edgeAttributes,
                          final AttributedNode fromNode,
                          final AttributedNode toNode)
    {
        this.edgeIdentifier = edgeIdentifier;
        this.edgeAttributes = edgeAttributes == null ? Collections.emptyList() : edgeAttributes;
        this.fromNode       = fromNode;
        this.toNode         = toNode;
    }

    public int getEdgeIdentifier()
    {
        return this.edgeIdentifier;
    }

    public List<Object> getEdgeAttributes()
    {
        return Collections.unmodifiableList(this.edgeAttributes);
    }

    public AttributedNode getFromNode()
    {
        return this.fromNode;
    }

    public AttributedNode getToNode()
    {
        return this.toNode;
    }

    private final int            edgeIdentifier;
    private final List<Object>   edgeAttributes;
    private final AttributedNode fromNode;
    private final AttributedNode toNode;
}
