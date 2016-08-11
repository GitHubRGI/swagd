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

package com.rgi.geopackage.extensions.routing.router;

import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedEdge;
import com.rgi.geopackage.extensions.network.AttributedNode;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.extensions.routing.Route;
import com.rgi.geopackage.extensions.routing.RoutingNetworkDescription;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/**
 * @author Luke Lambert
 */
public abstract class Router
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
     */
    protected Router(final GeoPackageRoutingExtension       routingExtension,
                     final RoutingNetworkDescription        routingNetwork,
                     final Collection<AttributeDescription> nodeAttributeDescriptions,
                     final Collection<AttributeDescription> edgeAttributeDescriptions,
                     final Function<AttributedEdge, Double> edgeCostEvaluator)
    {
        this(routingExtension,
             routingNetwork,
             nodeAttributeDescriptions,
             edgeAttributeDescriptions,
             edgeCostEvaluator,
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
     * @param restrictedNodeIdentifiers
     *            Collection of nodes to not consider in routing
     * @param restrictedEdgeIdentifiers
     *            Collection of edges to not consider in routing
     */
    protected Router(final GeoPackageRoutingExtension       routingExtension,
                     final RoutingNetworkDescription        routingNetwork,
                     final Collection<AttributeDescription> nodeAttributeDescriptions,
                     final Collection<AttributeDescription> edgeAttributeDescriptions,
                     final Function<AttributedEdge, Double> edgeCostEvaluator,
                     final Collection<Integer>              restrictedNodeIdentifiers,
                     final Collection<Integer>              restrictedEdgeIdentifiers)
    {
        if(routingExtension == null)
        {
            throw new IllegalArgumentException("Routing extension may not be null");
        }

        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(edgeCostEvaluator == null)
        {
            throw new IllegalArgumentException("Edge cost function may not be null");
        }

        this.routingExtension  = routingExtension;
        this.networkExtension  = routingExtension.getNetworkExtension();
        this.routingNetwork    = routingNetwork;
        this.edgeCostEvaluator = edgeCostEvaluator;

        this.nodeAttributeDescriptions = nodeAttributeDescriptions == null ? Collections.emptyList() : new ArrayList<>(nodeAttributeDescriptions);
        this.edgeAttributeDescriptions = edgeAttributeDescriptions == null ? Collections.emptyList() : new ArrayList<>(edgeAttributeDescriptions);
        this.restrictedNodeIdentifiers = restrictedNodeIdentifiers == null ? Collections.emptySet()  : new HashSet<>(restrictedNodeIdentifiers);
        this.restrictedEdgeIdentifiers = restrictedEdgeIdentifiers == null ? Collections.emptySet()  : new HashSet<>(restrictedEdgeIdentifiers);
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
    public abstract Route route(final int startNodeIdentifier,
                                final int endNodeIdentifier) throws SQLException;

    protected final GeoPackageRoutingExtension       routingExtension;
    protected final GeoPackageNetworkExtension       networkExtension;
    protected final RoutingNetworkDescription        routingNetwork;
    protected final List<AttributeDescription>       nodeAttributeDescriptions;
    protected final List<AttributeDescription>       edgeAttributeDescriptions;
    protected final Function<AttributedEdge, Double> edgeCostEvaluator;
    protected final Collection<Integer>              restrictedNodeIdentifiers;
    protected final Collection<Integer>              restrictedEdgeIdentifiers;
}
