///* The MIT License (MIT)
// *
// * Copyright (c) 2015 Reinventing Geospatial, Inc.
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//
//import com.rgi.geopackage.extensions.network.Edge;
//import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
//import com.rgi.geopackage.extensions.network.Network;
//import com.rgi.geopackage.extensions.routing.AStarVertex;
//
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
///**
// * Implementation of two shortest path/routing algorithms:
// * Dijkstra's and A*
// *
// * @author Jenifer Cochran
// * @author Mary Carome
// * @author Luke Lambert
// *
// */
//public class RoutingAlgorithms
//{
//    /**
//     * This algorithm will find the shortest path from the starting
//     * node to the ending node
//     *
//     * @param networkExtension
//     *            GeoPackageNetworkExtension containing the network
//     * @param network
//     *            network contain the start and end node
//     * @param start
//     *            starting node
//     * @param end
//     *            ending node
//     * @param edgeCostEvaluator
//     *            cost function for each edge in the path
//     * @param heuristic
//     *            heuristic function for two nodes in the network
//     * @return Optimal path from the start node to the end node
//     * @throws SQLException
//     *             if there is a database error
//     */
//    public static List<Integer> aStar(final GeoPackageNetworkExtension           networkExtension,
//                                      final Network                              network,
//                                      final int                                  start,
//                                      final int                                  end,
//                                      final Function<Edge, Double>               edgeCostEvaluator,
//                                      final BiFunction<Integer, Integer, Double> heuristic) throws SQLException
//    {
//        if(networkExtension == null)
//        {
//            throw new IllegalArgumentException("Network extension may not be null");
//        }
//
//        if(network == null)
//        {
//            throw new IllegalArgumentException("Network may not be null");
//        }
//
//        if(edgeCostEvaluator == null)
//        {
//            throw new IllegalArgumentException("Edge cost function may not be null");
//        }
//
//        if(heuristic == null)
//        {
//            throw new IllegalArgumentException("Heuristic function may not be null");
//        }
//
//        // changed comparator -> change back to distance from end
//        final PriorityQueue<AStarVertex> openList = new PriorityQueue<>((o1, o2) -> Double.compare((o1.getCostToEnd() + o1.getCostFromStart()),
//                                                                                                   (o2.getCostToEnd() + o2.getCostFromStart())));
//        final HashSet<Integer> closedList = new HashSet<>();
//
//        final Map<Integer, AStarVertex> nodeMap = new HashMap<>();
//
//        // Starting Vertex
//        final AStarVertex startVertex = new AStarVertex(start,
//                                                        0,
//                                                        heuristic.apply(start, end));
//
//        openList.add(startVertex);
//        nodeMap.put(start, startVertex);
//
//        while(!openList.isEmpty())
//        {
//            final AStarVertex currentVertex = openList.poll(); // Get the Vertex closest to the end
//
//            // If current vertex is the target then we are done
//            if(currentVertex.getNodeIdentifier() == end)
//            {
//                return getAStarPath(end, nodeMap);
//            }
//
//            closedList.add(currentVertex.getNodeIdentifier()); // Put it in "done" pile
//
//            // For each reachable Vertex
//            for(final Edge edge : networkExtension.getExits(network, currentVertex.getNodeIdentifier()))
//            {
//                final int adjacentNode = edge.getTo();
//
//                AStarVertex reachableVertex = nodeMap.get(adjacentNode);
//
//                if(reachableVertex == null)
//                {
//                    reachableVertex = new AStarVertex(adjacentNode);
//                    nodeMap.put(adjacentNode, reachableVertex);
//                }
//
//                // If the closed list already searched this vertex, skip it
//                if(!closedList.contains(reachableVertex.getNodeIdentifier()))
//                {
//                    final double edgeCost = edgeCostEvaluator.apply(edge);
//
//                    if(edgeCost <= 0.0)    // Are positive values that are extremely close to 0 going to be a problem?
//                    {
//                        throw new RuntimeException("The A* algorithm is only valid for edge costs greater than 0");
//                    }
//
//                    final double distanceFromStart = currentVertex.getCostFromStart() + edgeCost;
//
//                    if(!openList.contains(reachableVertex))         // If we don't have it, add it
//                    {
//                        final double distanceFromEnd = heuristic.apply(reachableVertex.getNodeIdentifier(), end);
//
//                        reachableVertex.setCostFromStart(distanceFromStart);
//                        reachableVertex.setCostToEnd  (distanceFromEnd);
//                        reachableVertex.setPrevious         (currentVertex);
//
//                        openList.add(reachableVertex);
//                    }
//                    else if(distanceFromStart < reachableVertex.getCostFromStart()) // If this is better then update the values and parent Vertex (previous)
//                    {
//                        final double distanceFromEnd = heuristic.apply(reachableVertex.getNodeIdentifier(), end);
//
//                        reachableVertex.setCostFromStart(distanceFromStart);
//                        reachableVertex.setCostToEnd  (distanceFromEnd);
//                        reachableVertex.setPrevious         (currentVertex);
//
//                        openList.remove(reachableVertex);   // Re-add to trigger the reprioritization of this vertex
//                        openList.add(reachableVertex);
//                    }
//                }
//            }
//        }
//
//        throw new RuntimeException("Didnt find correct path"); // TODO, clean this up
//    }
//
//    private static List<Integer> getAStarPath(final Integer end, final Map<Integer, AStarVertex> nodeMap)
//    {
//        final LinkedList<Integer> path = new LinkedList<>();
//
//        for(AStarVertex backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.getPrevious())
//        {
//            path.addLast(backTrackVertex.getNodeIdentifier());
//        }
//
//        return path;
//    }
//
//    /**
//     * Based on http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm This
//     * algorithm will find the shortest path from the starting node to the
//     * ending node.
//     *
//     * @param networkExtension
//     *             GeoPackageNetworkExtension containing the network
//     * @param network
//     *             network contain the start and end vertices
//     * @param start
//     *             starting node
//     * @param end
//     *             ending node
//     * @param edgeCostEvaluator
//     *             cost function for each edge in the path
//     * @return Optimal path from the start node to the end node
//     * @throws SQLException
//     *             if there is a database error
//     */
//    public static List<Integer> dijkstra(final GeoPackageNetworkExtension networkExtension,
//                                         final Network                    network,
//                                         final int                        start,
//                                         final int                        end,
//                                         final Function<Edge, Double>     edgeCostEvaluator) throws SQLException
//    {
//        if(networkExtension == null)
//        {
//            throw new IllegalArgumentException("Network extension may not be null");
//        }
//
//        if(network == null)
//        {
//            throw new IllegalArgumentException("Network may not be null");
//        }
//
//        if(edgeCostEvaluator == null)
//        {
//            throw new IllegalArgumentException("Edge cost function may not be null");
//        }
//
//        final Map<Integer, Vertex> nodeMap = new HashMap<>();
//
//        final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>((o1, o2) -> Double.compare(o1.minimumCost, o2.minimumCost));
//
//        final Vertex startVertex = new Vertex(start);
//
//        vertexQueue.add(startVertex);
//
//        while(!vertexQueue.isEmpty())
//        {
//            final Vertex currentVertex = vertexQueue.poll(); // Gets the vertex with min cost/distance
//            networkExtension.getExits(network, currentVertex.nodeIdentifier).forEach(edge ->
//            {
//                final int adjacentNode = edge.getTo();
//
//                Vertex reachableVertex = nodeMap.get(adjacentNode);
//
//                if(reachableVertex == null)
//                {
//                    reachableVertex = new Vertex(adjacentNode, currentVertex);
//                    nodeMap.put(adjacentNode, reachableVertex);
//                }
//
//                final double cost = currentVertex.minimumCost + edgeCostEvaluator.apply(edge); // Get the distance between nodes
//
//                if(cost < reachableVertex.minimumCost) // If the cost is unset or less, change the cost
//                {
//                    vertexQueue.remove(reachableVertex);      // Remove it from the queue if it is there
//                    reachableVertex.minimumCost = cost;       // Adjust to new values
//                    reachableVertex.previous = currentVertex; // Adjust the path
//                    vertexQueue.add(reachableVertex);         // Add it to the queue in the correct order
//                }
//            });
//        }
//
//        final LinkedList<Integer> path = new LinkedList<>();
//
//        for(Vertex backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.previous)
//        {
//            path.addLast(backTrackVertex.nodeIdentifier);
//        }
//
//        return path;
//    }
//
//    /**
//     * Implementation of vertices used to find
//     * a path in Dijkstra's Algorithm
//     */
//    private static class Vertex
//    {
//        public Vertex previous; // Parent node
//
//        public final int nodeIdentifier;
//
//        public double minimumCost = Double.MAX_VALUE;
//
//        public Vertex(final int nodeIdentifier)
//        {
//            this.nodeIdentifier = nodeIdentifier;
//            this.previous = null;
//            this.minimumCost = 0.0;
//        }
//
//        public Vertex(final int nodeIdentifier, final Vertex previous)
//        {
//            this.nodeIdentifier = nodeIdentifier;
//            this.previous = previous;
//        }
//
//        @Override
//        public boolean equals(final Object obj)
//        {
//            if(obj == this)
//            {
//                return true;
//            }
//
//            if(obj == null || this.getClass() != obj.getClass())
//            {
//                return false;
//            }
//
//            return this.nodeIdentifier == ((Vertex) obj).nodeIdentifier; // Is this enough? Node identifiers should be unique, right?
//        }
//
//        @Override
//        public int hashCode()
//        {
//            return this.nodeIdentifier;
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format("%d (%f, %d)", this.nodeIdentifier, this.minimumCost, this.previous.nodeIdentifier);
//        }
//    }
//}
