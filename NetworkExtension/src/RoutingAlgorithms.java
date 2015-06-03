import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;

/**
 * Implementation of two shortest path/routing algorithms:
 * Dijkstra's and A*
 *
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 */
public class RoutingAlgorithms
{
	/**
	 * This algorithm will find the shortest path from the starting
	 * node to the ending node
	 *
	 * @param networkExtension
	 *            GeoPackageNetworkExtension containing the network
	 * @param network
	 *            network contain the start and end vertices
	 * @param start
	 *            starting vertex
	 * @param end
	 *            ending vertex
	 * @param cost
	 *            cost function for each edge in the path
	 * @param heuristics
	 *            heuristic function for two nodes in the network
	 * @throws SQLException
	 */
    public static List<Integer> astar(final GeoPackageNetworkExtension networkExtension, final Network network, final Integer start, final Integer end, final Function<Edge, Double> cost, final BiFunction<Integer, Integer, Double> heuristics) throws SQLException
    {
        // changed comparator -> change back to distance from end
        final PriorityQueue<VertexAstar> openList = new PriorityQueue<>((o1, o2) -> Double.compare((o1.distanceFromEnd + o1.distanceFromStart), (o2.distanceFromEnd + o2.distanceFromStart)));
        final HashSet<VertexAstar> closedList = new HashSet<>();

        final Map<Integer, VertexAstar> nodeMap = new HashMap<>();

        // initialize starting Vertex
        final VertexAstar startVertex = new VertexAstar(start);
        startVertex.distanceFromEnd = heuristics.apply(start, end);
        openList.add(startVertex);
        nodeMap.put(start, startVertex);

        while (!openList.isEmpty())
        {
            final VertexAstar currentVertex = openList.poll();// get the Vertex closest to the end

            // if current vertex is the target then we are done
            if (currentVertex.nodeIdentifier == end)
            {
                return getAstarPath(end, nodeMap);
            }

            closedList.add(currentVertex); // put it in "done" pile

            // for each reachable Vertex
            for (final Edge edge : networkExtension.getExits(network, currentVertex.nodeIdentifier))
            {
                final int adjacentNode = edge.getTo();

                VertexAstar reachableVertex = nodeMap.get(adjacentNode);

                if (reachableVertex == null)
                {
                    reachableVertex = new VertexAstar(adjacentNode);
                    reachableVertex.distanceFromEnd = Double.MAX_VALUE;
                    reachableVertex.distanceFromStart = Double.MAX_VALUE;
                    nodeMap.put(adjacentNode, reachableVertex);
                }

                // calculate a tentative distance (see if this is better than what you already may have)
                final double distanceFromStart = currentVertex.distanceFromStart + cost.apply(edge);
                final double distanceFromEnd = heuristics.apply(reachableVertex.nodeIdentifier, end);
                // if the closed list already searched this vertex, skip it
                if (closedList.contains(reachableVertex))
                {
                    continue;
                }
                if (!openList.contains(reachableVertex))// if we dont have it, add it
                {
                    reachableVertex.distanceFromStart = distanceFromStart;
                    reachableVertex.distanceFromEnd = distanceFromEnd;
                    reachableVertex.previous = currentVertex;
                    openList.add(reachableVertex);
                }
                // if this is better then update the values and parent Vertex (previous)
                else if (distanceFromStart < reachableVertex.distanceFromStart)
                {
                    reachableVertex.distanceFromStart = distanceFromStart;
                    reachableVertex.distanceFromEnd = distanceFromEnd;
                    reachableVertex.previous = currentVertex;
                    openList.remove(reachableVertex);
                    openList.add(reachableVertex);
                }
            }
        }
        throw new RuntimeException("Didnt find correct path");
        // return getAstarPath(end, nodeMap);//it shouldnt reach here...throw exception?
    }

    /**
     * Returns a path created by Astar
     * @param end
     * @param nodeMap
     * @return
     */
    private static List<Integer> getAstarPath(final Integer end, final Map<Integer, VertexAstar> nodeMap)
    {
        final LinkedList<Integer> path = new LinkedList<>();

        for (VertexAstar backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.previous)
        {
            path.addLast(backTrackVertex.nodeIdentifier);
        }

        return path;
    }

    /**
     * Implementation of vertices used to find
     * a path in the A* algorithm
     */
    private static class VertexAstar
    {
        public VertexAstar previous; // Parent node

        public final int nodeIdentifier;

        public double distanceFromStart = Double.MAX_VALUE;
        public double distanceFromEnd = Double.MAX_VALUE;

        public VertexAstar(final int nodeIdentifier)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous = null;
            this.distanceFromStart = 0.0;
            this.distanceFromEnd = 0.0;
        }

        @SuppressWarnings("unused")
		public VertexAstar(final int nodeIdentifier, final VertexAstar previous)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous = previous;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == this)
            {
                return true;
            }

            if (obj == null || this.getClass() != obj.getClass())
            {
                return false;
            }

            return this.nodeIdentifier == ((VertexAstar) obj).nodeIdentifier; // Is
                                                                                // this
                                                                                // enough?
                                                                                // Node
                                                                                // identifiers
                                                                                // should
                                                                                // be
                                                                                // unique,
                                                                                // right?
        }

        @Override
        public int hashCode()
        {
            return this.nodeIdentifier;
        }

        @Override
        public String toString()
        {
            return String.format("%d (%f, %f, %d)", this.nodeIdentifier, this.distanceFromStart, this.distanceFromEnd, this.previous.nodeIdentifier);
        }
    }

    /**
     * Based on http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm This
     * algorithm will find the shortest path from the starting node to the
     * ending node.
     *
     * @param start
     *            starting vertex
     * @param end
     *            ending vertex
     * @param edgeCostEvaluator
     * @throws SQLException
     */
    public static List<Integer> dijkstra(final GeoPackageNetworkExtension networkExtension, final Network network, final Integer start, final Integer end, final Function<Edge, Double> edgeEvaluator) throws SQLException
    {
        final Map<Integer, Vertex> nodeMap = new HashMap<>();

        final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>((o1, o2) -> Double.compare(o1.minimumCost, o2.minimumCost));

        final Vertex startVertex = new Vertex(start);

        vertexQueue.add(startVertex);

        while (!vertexQueue.isEmpty())
        {
            final Vertex currentVertex = vertexQueue.poll(); // Gets the vertex with min cost/distance
            networkExtension.getExits(network, currentVertex.nodeIdentifier).forEach(edge ->
            {
                final int adjacentNode = edge.getTo();

                Vertex reachableVertex = nodeMap.get(adjacentNode);

                if (reachableVertex == null)
                {
                    reachableVertex = new Vertex(adjacentNode, currentVertex);
                    nodeMap.put(adjacentNode, reachableVertex);
                }

                final double cost = currentVertex.minimumCost + edgeEvaluator.apply(edge); // Get the distance between nodes

                if (cost < reachableVertex.minimumCost) // If the cost is unset or less, change the cost
                {
                    vertexQueue.remove(reachableVertex);      // Remove it from the queue if it is there
                    reachableVertex.minimumCost = cost;       // Adjust to new values
                    reachableVertex.previous = currentVertex; // Adjust the path
                    vertexQueue.add(reachableVertex);         // Add it to the queue in the correct order
                }
            });
        }

        final LinkedList<Integer> path = new LinkedList<>();

        for (Vertex backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.previous)
        {
            path.addLast(backTrackVertex.nodeIdentifier);
        }

        return path;
    }

    /**
     * Implementation of vertices used to find
     * a path in Dijkstra's Algorithm
     */
    private static class Vertex
    {
        public Vertex previous; // Parent node

        public final int nodeIdentifier;

        public double minimumCost = Double.MAX_VALUE;

        public Vertex(final int nodeIdentifier)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous = null;
            this.minimumCost = 0.0;
        }

        public Vertex(final int nodeIdentifier, final Vertex previous)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous = previous;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == this)
            {
                return true;
            }

            if (obj == null || this.getClass() != obj.getClass())
            {
                return false;
            }

            return this.nodeIdentifier == ((Vertex) obj).nodeIdentifier; // Is
                                                                            // this
                                                                            // enough?
                                                                            // Node
                                                                            // identifiers
                                                                            // should
                                                                            // be
                                                                            // unique,
                                                                            // right?
        }

        @Override
        public int hashCode()
        {
            return this.nodeIdentifier;
        }

        @Override
        public String toString()
        {
            return String.format("%d (%f, %d)", this.nodeIdentifier, this.minimumCost, this.previous.nodeIdentifier);
        }
    }
}
