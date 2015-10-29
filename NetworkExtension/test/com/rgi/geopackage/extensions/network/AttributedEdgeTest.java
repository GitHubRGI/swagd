package com.rgi.geopackage.extensions.network;

import org.junit.Test;

import java.util.ArrayList;
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
        final List<Object> edgeAttributes = Collections.emptyList();
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
}
