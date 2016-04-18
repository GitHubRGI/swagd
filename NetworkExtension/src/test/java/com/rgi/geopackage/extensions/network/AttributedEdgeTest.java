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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by steven.lander on 10/29/15.
 */
public class AttributedEdgeTest
{
    @Test
    public void testGetEdgeIdentifier()
    {
        final int edgeIdentifier = 1;
        final List<Object> edgeAttributes = null;
        final AttributedNode fromNode = new AttributedNode(1, Collections.emptyList());
        final AttributedNode toNode = new AttributedNode(1, Collections.emptyList());
        final AttributedEdge attributedEdge = new AttributedEdge(edgeIdentifier, edgeAttributes, fromNode, toNode);
        assertEquals("", 1, attributedEdge.getEdgeIdentifier());
    }

    @Test
    public void testGetEdgeAttributes()
    {
        final List<Object> edgeAttributes = new ArrayList<>();
        edgeAttributes.add("foo");
        final AttributedNode fromNode = new AttributedNode(1, Collections.emptyList());
        final AttributedNode toNode = new AttributedNode(1, Collections.emptyList());
        final int edgeIdentifier = 1;
        final AttributedEdge attributedEdge = new AttributedEdge(edgeIdentifier, edgeAttributes, fromNode, toNode);
        assertEquals("", edgeAttributes, attributedEdge.getEdgeAttributes());
    }

    @Test
    public void testGetFromNode()
    {
        final List<Object> edgeAttributes = Collections.emptyList();
        final List<Object> fromNodeAttributes = new ArrayList<>();
        fromNodeAttributes.add("foo");
        final AttributedNode fromNode = new AttributedNode(1, fromNodeAttributes);
        final AttributedNode toNode = new AttributedNode(1, Collections.emptyList());
        final int edgeIdentifier = 1;
        final AttributedEdge attributedEdge = new AttributedEdge(edgeIdentifier, edgeAttributes, fromNode, toNode);
        assertEquals("", fromNode, attributedEdge.getFromNode());
    }

    @Test
    public void testGetToNode()
    {
        final List<Object> edgeAttributes = Collections.emptyList();
        final List<Object> toNodeAttributes = new ArrayList<>();
        toNodeAttributes.add("foo");
        final AttributedNode fromNode = new AttributedNode(1, Collections.emptyList());
        final AttributedNode toNode = new AttributedNode(1, toNodeAttributes);
        final int edgeIdentifier = 1;
        final AttributedEdge attributedEdge = new AttributedEdge(edgeIdentifier, edgeAttributes, fromNode, toNode);
        assertEquals("", toNode, attributedEdge.getToNode());
    }
}
