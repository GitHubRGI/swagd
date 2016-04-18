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

import com.rgi.common.Memoize2;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedEdge;
import com.rgi.geopackage.extensions.network.AttributedNode;
import com.rgi.geopackage.extensions.network.NodeExitGetter;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.extensions.routing.Route;
import com.rgi.geopackage.extensions.routing.RoutingNetworkDescription;
import com.rgi.geopackage.extensions.routing.router.Router;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Luke Lambert
 */
public class AStar extends Router implements AutoCloseable
{
    /**
     * Constructor
     *
     * @param routingExtension
     *            Handle to a GeoPackage's routing extension
     * @param routingNetwork
     *            Network on which to route between a start and end node
     * @param nodeAttributeDescriptions
     *            Attributes of each network node to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link AttributedNode#getAttributes()} as an array of {@link Object}s
     *            in the <i>in the order in which the {@link
     *            AttributeDescription}s are specified</i>.
     * @param edgeAttributeDescriptions
     *            Attributes of each network edge to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link AttributedEdge#getEdgeAttributes()} as an
     *            array of {@link Object}s in the <i>in the order in which the
     *            {@link AttributeDescription}s are specified</i>.
     * @param edgeCostEvaluator
     *            Cost function for each edge in the path
     * @param heuristic
     *            Cost heuristic function to be applied between a intermediate
     *            and end node to determine the search order of A*
     * @throws SQLException
     *            if there is a database error
     */
    public AStar(final GeoPackageRoutingExtension                         routingExtension,
                 final RoutingNetworkDescription                          routingNetwork,
                 final Collection<AttributeDescription>                   nodeAttributeDescriptions,
                 final Collection<AttributeDescription>                   edgeAttributeDescriptions,
                 final Function<AttributedEdge, Double>                   edgeCostEvaluator,
                 final BiFunction<AttributedNode, AttributedNode, Double> heuristic) throws SQLException
    {
        this(routingExtension,
             routingNetwork,
             nodeAttributeDescriptions,
             edgeAttributeDescriptions,
             edgeCostEvaluator,
             heuristic,
             null,  // Creates empty list
             null); // Creates empty list
    }

    /**
     * Constructor
     *
     * @param routingExtension
     *            Handle to a GeoPackage's routing extension
     * @param routingNetwork
     *            Network on which to route between a start and end node
     * @param nodeAttributeDescriptions
     *            Attributes of each network node to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link AttributedNode#getAttributes()} as an array of {@link Object}s
     *            in the <i>in the order in which the {@link
     *            AttributeDescription}s are specified</i>.
     * @param edgeAttributeDescriptions
     *            Attributes of each network edge to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link AttributedEdge#getEdgeAttributes()} as an
     *            array of {@link Object}s in the <i>in the order in which the
     *            {@link AttributeDescription}s are specified</i>.
     * @param edgeCostEvaluator
     *            Cost function for each edge in the path
     * @param heuristic
     *            Cost heuristic function to be applied between a intermediate
     *            and end node to determine the search order of A*
     * @param restrictedNodeIdentifiers
     *            Collection of nodes to not consider in routing
     * @param restrictedEdgeIdentifiers
     *            Collection of edges to not consider in routing
     * @throws SQLException
     *            if there is a database error
     */
    public AStar(final GeoPackageRoutingExtension                         routingExtension,
                 final RoutingNetworkDescription                          routingNetwork,
                 final Collection<AttributeDescription> nodeAttributeDescriptions,
                 final Collection<AttributeDescription> edgeAttributeDescriptions,
                 final Function<AttributedEdge, Double>                   edgeCostEvaluator,
                 final BiFunction<AttributedNode, AttributedNode, Double> heuristic,
                 final Collection<Integer> restrictedNodeIdentifiers,
                 final Collection<Integer> restrictedEdgeIdentifiers) throws SQLException
    {
        super(routingExtension,
              routingNetwork,
              nodeAttributeDescriptions,
              edgeAttributeDescriptions,
              edgeCostEvaluator,
              restrictedNodeIdentifiers,
              restrictedEdgeIdentifiers);

        if(heuristic == null)
        {
            throw new IllegalArgumentException("Heuristic function may not be null");
        }

        this.cachedHeuristic = new Memoize2<>(heuristic);

        this.edgeGetter = this.networkExtension.getNodeExitGetter(routingNetwork.getNetwork(),
                                                                  this.nodeAttributeDescriptions,
                                                                  this.edgeAttributeDescriptions);
    }

    @Override
    public void close() throws SQLException
    {
        this.edgeGetter.close();
    }

    /**
     * This algorithm will find the route from the start node to the end node
     *
     * @param startNodeIdentifier
     *            Starting node
     * @param endNodeIdentifier
     *            Ending node
     * @return Optimal path from the start node to the end node
     * @throws SQLException
     *             if there is a database error
     */
    @Override
    public Route route(final int startNodeIdentifier,
                       final int endNodeIdentifier) throws SQLException
    {
        final PriorityQueue<Vertex> openList   = new PriorityQueue<>(10, AStar.vertexComparator);
        final Collection<Integer>   closedList = new HashSet<>((this.restrictedNodeIdentifiers == null) ? Collections.<Integer>emptySet() : this.restrictedNodeIdentifiers);
        final Map<Integer, Vertex>  nodeMap    = new HashMap<>();

        final AttributedNode startNode = this.networkExtension.getAttributedNode(startNodeIdentifier, this.nodeAttributeDescriptions);
        final AttributedNode endNode   = this.networkExtension.getAttributedNode(endNodeIdentifier,   this.nodeAttributeDescriptions);

        // Starting Vertex
        final Vertex startVertex = new Vertex(startNode,
                                              0.0,
                                              this.cachedHeuristic.get(startNode, endNode));

        nodeMap.put(startNodeIdentifier, startVertex);

        for(Vertex currentVertex = startVertex; currentVertex != null; currentVertex = openList.poll())
        {
            // If current vertex is the target then we are done
            if(currentVertex.getNode().getIdentifier() == endNodeIdentifier)
            {
                return getAStarPath(endNodeIdentifier, nodeMap);
            }

            closedList.add(currentVertex.getNode().getIdentifier()); // Put it in "done" pile

            for(final AttributedEdge exit : this.edgeGetter.getExits(currentVertex.getNode().getIdentifier())) // For each node adjacent to the current node
            {
                // Ignore restricted edges
                if(!this.restrictedEdgeIdentifiers.contains(exit.getEdgeIdentifier()))
                {
                    Vertex reachableVertex = nodeMap.get(exit.getToNode().getIdentifier());

                    if(reachableVertex == null)
                    {
                        reachableVertex = new Vertex(exit.getToNode());
                        nodeMap.put(exit.getToNode().getIdentifier(), reachableVertex);
                    }

                    // If the closed list already searched this vertex, skip it
                    if(!closedList.contains(exit.getToNode().getIdentifier()))
                    {
                        final double edgeCost = this.edgeCostEvaluator.apply(exit);

                        if(edgeCost <= 0.0)    // Are positive values that are extremely close to 0 going to be a problem?
                        {
                            throw new RuntimeException("The A* algorithm is only valid for edge costs greater than 0");
                        }

                        final double costFromStart = currentVertex.getCostFromStart() + edgeCost;

                        final boolean isShorterPath = costFromStart < reachableVertex.getCostFromStart();

                        if(!openList.contains(reachableVertex) || isShorterPath)
                        {
                            final double estimatedCostFromEnd = exit.getToNode().getIdentifier() == endNode.getIdentifier() ? 0.0
                                                                                                                            : this.cachedHeuristic.get(reachableVertex.getNode(), endNode);

                            reachableVertex.update(costFromStart,
                                                   estimatedCostFromEnd,
                                                   currentVertex,
                                                   exit.getEdgeIdentifier(),
                                                   exit.getEdgeAttributes(),
                                                   edgeCost);

                            if(isShorterPath)
                            {
                                openList.remove(reachableVertex);   // Re-add to trigger the reprioritization of this vertex
                            }

                            openList.add(reachableVertex);
                        }
                    }
                }
            }
        }

        return null;    // No path between the start and end nodes
    }

    private static Route getAStarPath(final Integer end, final Map<Integer, Vertex> nodeMap)
    {
        final LinkedList<List<Object>> nodesAttributes = new LinkedList<List<Object>>();
        final LinkedList<List<Object>> edgesAttributes = new LinkedList<List<Object>>();
        final LinkedList<Integer>      edgeIdentifiers = new LinkedList<Integer>();
        final LinkedList<Double>       edgeCosts       = new LinkedList<Double>();

        for(Vertex vertex = nodeMap.get(end); vertex != null; vertex = vertex.getPrevious())
        {
            nodesAttributes.addFirst(vertex.getNode().getAttributes());

            if(vertex.getPrevious() != null)
            {
                edgesAttributes.addFirst(vertex.getEdgeAttributes());
                edgeIdentifiers.addFirst(vertex.getEdgeIdentifier());
                edgeCosts      .addFirst(vertex.getEdgeCost());
            }
        }

        return new Route(nodesAttributes,
                         edgesAttributes,
                         edgeIdentifiers,
                         edgeCosts);
    }

    private final Memoize2<AttributedNode, AttributedNode, Double> cachedHeuristic;
    private final NodeExitGetter                                   edgeGetter;

    private static final Comparator<Vertex> vertexComparator = (vertex1, vertex2) -> Double.compare((vertex1.getEstimatedCostToEnd() + vertex1.getCostFromStart()),
                                                                                                    (vertex2.getEstimatedCostToEnd() + vertex2.getCostFromStart()));
}
