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

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Interface for all routing network store readers
 *
 * @author Luke Lambert
 */
public interface RoutingNetworkStoreReader
{
    /**
     * @return Name / type pairs describing node attributes
     * @throws RoutingNetworkStoreException If there is an issue generating the
     *                                      descriptions
     */
    List<Pair<String, Type>> getNodeAttributeDescriptions() throws RoutingNetworkStoreException;

    /**
     * @return Name / type pairs describing edge attributes
     * @throws RoutingNetworkStoreException If there is an issue generating the
     *                                      descriptions
     */
    List<Pair<String, Type>> getEdgeAttributeDescriptions() throws RoutingNetworkStoreException;

    /**
     * @return The collection of nodes in the network
     * @throws RoutingNetworkStoreException If there is an issue reading the
     *                                      nodes
     */
    List<Node> getNodes() throws RoutingNetworkStoreException;

    /**
     * @return The collection of edges in the network
     * @throws RoutingNetworkStoreException If there is an issue reading the
     *                                      edges
     */
    List<Edge> getEdges();

    /**
     * @return The coordinate reference system of the data
     * @throws RoutingNetworkStoreException If there is an issue determining
     * the coordinate reference system
     */
    CoordinateReferenceSystem getCoordinateReferenceSystem() throws RoutingNetworkStoreException;

    /**
     * @return Tight fitting bounds of the nodes of the network. If the network
     * contains no nodes, the values will be NaN.
     * @throws RoutingNetworkStoreException
     */
    BoundingBox getBounds() throws RoutingNetworkStoreException;

    /**
     * @return A short, human-readable description of the routing network. It's
     *         recommended that information about the source data, number of
     *         nodes and edges, generation parameters, etc, be included in this
     *         description.
     */
    String getDescription();

    /**
     * @return Returns an indication of whether or not this network contains
     *         elevation data
     * @throws RoutingNetworkStoreException If there is an issue with
     *                                      determining the network's
     *                                      functionality
     */
    NodeDimensionality getNodeDimensionality() throws RoutingNetworkStoreException;
}
