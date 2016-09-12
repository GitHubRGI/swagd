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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public final class Utility
{
    private Utility()
    {

    }

    public static RoutingNetworkStoreReader transform(final Function<Node, Node>      nodeTransform,
                                                      final Function<Edge, Edge>      edgeTransform,
                                                      final List<Pair<String, Type>>  nodeAttributeDescriptions,
                                                      final List<Pair<String, Type>>  edgeAttributeDescriptions,
                                                      final List<Node>                nodes,
                                                      final List<Edge>                edges,
                                                      final CoordinateReferenceSystem coordinateReferenceSystem,
                                                      final String                    description,
                                                      final NodeDimensionality        nodeDimensionality)
    {
        final List<Node> transformedNodes = nodes.stream()
                                                 .map(nodeTransform)
                                                 .collect(Collectors.toList());

        final List<Edge> transformedEdges = edges.stream()
                                                 .map(edgeTransform)
                                                 .collect(Collectors.toList());



        final BoundingBox bounds = calculateBounds(transformedNodes);

        return new RoutingNetworkStoreReader()
               {
                   @Override
                   public List<Pair<String, Type>> getNodeAttributeDescriptions()
                   {
                       return nodeAttributeDescriptions;
                   }

                   @Override
                   public List<Pair<String, Type>> getEdgeAttributeDescriptions()
                   {
                       return edgeAttributeDescriptions;
                   }

                   @Override
                   public List<Node> getNodes()
                   {
                       return transformedNodes;
                   }

                   @Override
                   public List<Edge> getEdges()
                   {
                       return transformedEdges;
                   }

                   @Override
                   public CoordinateReferenceSystem getCoordinateReferenceSystem()
                   {
                       return coordinateReferenceSystem;
                   }

                   @Override
                   public BoundingBox getBounds()
                   {
                       return bounds;
                   }

                   @Override
                   public String getDescription()
                   {
                       return description;
                   }

                   @Override
                   public NodeDimensionality getNodeDimensionality()
                   {
                       return nodeDimensionality;
                   }
               };
    }

    public static BoundingBox calculateBounds(final Iterable<Node> nodes)
    {
        final double[] bbox = { Double.NaN, // x min
                                Double.NaN, // y min
                                Double.NaN, // x max
                                Double.NaN  // y max
                              };

        nodes.forEach(node -> { final double x = node.getX();
                                final double y = node.getY();

                                if(Double.isNaN(bbox[0]) || x < bbox[0])
                                {
                                    bbox[0] = x;
                                }

                                if(Double.isNaN(bbox[2]) || x > bbox[2])
                                {
                                    bbox[2] = x;
                                }

                                if(Double.isNaN(bbox[1]) || y < bbox[1])
                                {
                                    bbox[1] = y;
                                }

                                if(Double.isNaN(bbox[3]) || y > bbox[3])
                                {
                                    bbox[3] = y;
                                }
                              });

        return new BoundingBox(bbox[0],
                               bbox[1],
                               bbox[2],
                               bbox[3]);
    }

//    private static Map<Integer, Node> deconflict(final List<Node> nodes)
//    {
//        final Map<String, Node>  nodeHashes = new HashMap<>(nodes.size());
//        final Map<Integer, Node> nodeMap    = new HashMap<>(nodes.size());
//
//        for(final Node node : nodes)
//        {
//            final String key = Double.toString(node.getX()) + '_' +
//                               Double.toString(node.getY()) + '_' +
//                               (node.getElevation() != null ? Double.toString(node.getElevation()) : "");  // TODO this could be smarter...
//
//            if(!nodeHashes.containsKey(key))
//            {
//                nodeHashes.put(key, node);
//                nodeMap.put(node.getIdentifier(), node);
//            }
//        }
//
//        return nodeMap;
//    }
//
//    private static List<Edge> deconflict(final List<Edge>         edges,
//                                         final Map<Integer, Node> nodeMap)
//    {
//        final List<Edge> deconflictedEdges = new ArrayList<>(edges.size());
//
//        final Set<String> edgeHashes = new Set<>();
//    }
}



