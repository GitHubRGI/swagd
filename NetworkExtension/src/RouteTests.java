import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;

/**
 *
 * @author Mary Carome
 *
 */
public class RouteTests
{
    private static final File geoPackageFile = new File("test.gpkg");
    private static final File geoPackageFile2 = new File("test2.gpkg");
    private static final File geoPackageFile3 = new File("test3.gpkg");

    public static void main(final String[] args)
    {
        runRoute2(geoPackageFile2, 100, 105247);
    }

    private static void runRoute()
    {
        try(final GeoPackage gpkg = new GeoPackage(geoPackageFile, OpenMode.Open))
        {
            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network network = networkExtension.getNetwork("mynetwork");

            final AttributeDescription distanceAttribute = networkExtension.getAttributeDescription(network,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            final AttributeDescription nodeLatitudeAttibute = networkExtension.getAttributeDescription(network,
                                                                                                       "latitude",
                                                                                                       AttributedType.Node);

            final AttributeDescription nodeLongitudeAttibute = networkExtension.getAttributeDescription(network,
                                                                                                        "longitude",
                                                                                                        AttributedType.Node);

            final int startNode = 9036;
            final int endNode   = 37236;

            if(networkExtension.getEntries(network, endNode).size() > 0)
            {
                final long startTime = System.nanoTime();

//                final Map<Integer, Double> costCache = new HashMap<>();

//                final List<Integer> path = dijkstra(networkExtension,
//                                                    network,
//                                                    startNode,
//                                                    endNode,
//                                                    (ThrowingFunction<Edge, Double>)(edge) -> { //if(costCache.containsKey(edge.getIdentifier()))
//                                                                                                //{
//                                                                                                //    return costCache.get(edge.getIdentifier());
//                                                                                                //}
//
//                                                                                                final double cost = networkExtension.getAttribute(edge, distanceAttribute);
//
//                                                                                                //costCache.put(edge.getIdentifier(), cost);
//
//                                                                                                return cost;
//                                                                                              });

                final List<Integer> path = astar(networkExtension,
                                                 network,
                                                 startNode,
                                                 endNode,
                                                 (ThrowingFunction<Edge, Double>)(edge) -> networkExtension.getEdgeAttribute(edge, distanceAttribute),
                                                 (startIdentifier, endIdentifier) -> { try
                                                                                       {
                                                                                           final List<List<Object>> values = networkExtension.getNodeAttributes(Arrays.asList(startIdentifier, endIdentifier),
                                                                                                                                                                nodeLongitudeAttibute,
                                                                                                                                                                nodeLatitudeAttibute);

                                                                                           final List<Object> startCoordinate = values.get(0);
                                                                                           final List<Object> endCoordinate   = values.get(1);

                                                                                           final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                                                           final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                                                           return Math.sqrt(latitude*latitude + longitude*longitude);
                                                                                       }
                                                                                       catch(final SQLException ex)
                                                                                       {
                                                                                           throw new RuntimeException(ex);
                                                                                       }
                                                                                     });

                path.forEach(node -> System.out.print(node + ", "));

                printPath(networkExtension, network, path, distanceAttribute);

                System.out.println(String.format("\nAstar took %.2f seconds to calculate.", (System.nanoTime() - startTime)/1.0e9));
            }
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    private static void printPath(final GeoPackageNetworkExtension networkExtension,
                                  final Network                    network,
                                  final List<Integer>              path,
                                  final AttributeDescription       distanceAttribute) throws SQLException
    {
        double totalWeight = 0.0;

        for(int nodeIndex = 0; nodeIndex < path.size()-1; ++nodeIndex)
        {
            final int firstNode  = path.get(nodeIndex);
            final int secondNode = path.get(nodeIndex+1);

            final Edge edge = networkExtension.getEdge(network, firstNode, secondNode);

            final double cost = networkExtension.getEdgeAttribute(edge, distanceAttribute);

            System.out.printf("%f->(%d)-", cost, secondNode);

            totalWeight += cost;
        }

        System.out.println(String.format("\nTotal distance = %f", totalWeight));
    }

    private static void runRoute2(final File geoPackage, final int routes, final int numNodes)
    {
        final Random rand = new Random(123456789);
        try(final GeoPackage gpkg = new GeoPackage(geoPackage, OpenMode.Open))
        {
            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network network = networkExtension.getNetwork("mynetwork");

            final AttributeDescription nodeLatitudeAttibute = networkExtension.getAttributeDescription(network, "latitude", AttributedType.Node);

            final AttributeDescription nodeLongitudeAttibute = networkExtension.getAttributeDescription(network,"longitude", AttributedType.Node);

            final AttributeDescription distanceAttribute = networkExtension.getAttributeDescription(network, "length", AttributedType.Edge);

            final int[] start = rand.ints(routes, 0, numNodes).toArray();
            final int[] end = rand.ints(routes, 0, numNodes).toArray();
            int startNode;
            int endNode;
            double sum = 0;

            for(int i = 0; i < routes; i++)
            {
                startNode = start[i];
                endNode = end[i];
                if(networkExtension.getEntries(network, endNode).size() > 0)
                {
                    final long startTime = System.nanoTime();

                    final Map<Integer, Double> costCache = new HashMap<>();

                    final List<Integer> path = astar(networkExtension,
                                                     network,
                                                     startNode,
                                                     endNode,
                                                     (ThrowingFunction<Edge, Double>)(edge) -> networkExtension.getEdgeAttribute(edge, distanceAttribute),
                                                     (startIdentifier, endIdentifier) -> { try
                                                                                           {
                                                                                                final List<Object> startCoordinate = networkExtension.getNodeAttributes(startIdentifier, nodeLongitudeAttibute, nodeLatitudeAttibute);
                                                                                                final List<Object> endCoordinate   = networkExtension.getNodeAttributes(endIdentifier,   nodeLongitudeAttibute, nodeLatitudeAttibute);

                                                                                                final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                                                                final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                                                                return Math.sqrt(latitude*latitude + longitude*longitude);
                                                                                           }
                                                                                           catch(final SQLException ex)
                                                                                           {
                                                                                               throw new RuntimeException(ex);
                                                                                           }
                                                                                         });

                    path.forEach(node -> System.out.print(node + ", "));

                    System.out.println(String.format("\nAstar took %.2f seconds to calculate.", (System.nanoTime() - startTime)/1.0e9));
                    sum += (System.nanoTime() - startTime)/1.0e9;
                }
            }

            System.out.println(String.format("TO calculat %s routes, astar took %.2f seconds to calculate", routes, sum));
            System.out.println(String.format("Astar took an average of %.2f seconds to calculate", sum/routes));
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            ex.printStackTrace();
        }
    }

    private static class VertexAstar
    {
        public VertexAstar previous;     // Parent node

        public final int nodeIdentifier;

        public double distanceFromStart = Double.MAX_VALUE;
        public double distanceFromEnd   = Double.MAX_VALUE;

        public VertexAstar(final int nodeIdentifier)
        {
            this.nodeIdentifier    = nodeIdentifier;
            this.previous          = null;
            this.distanceFromStart = 0.0;
            this.distanceFromEnd   = 0.0;
        }

        public VertexAstar(final int nodeIdentifier, final VertexAstar previous)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = previous;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if(obj == this)
            {
                return true;
            }

            if(obj == null || this.getClass() != obj.getClass())
            {
                return false;
            }

            return this.nodeIdentifier == ((VertexAstar)obj).nodeIdentifier; // Is this enough? Node identifiers should be unique, right?
        }

        @Override
        public int hashCode()
        {
            return this.nodeIdentifier;
        }

        @Override
        public String toString()
        {
            return String.format("%d (%f, %f, %d)",
                                  this.nodeIdentifier,
                                  this.distanceFromStart,
                                  this.distanceFromEnd,
                                  this.previous.nodeIdentifier);
            }
        }

    private static class Vertex
    {
        public Vertex previous;     // Parent node

        public final int nodeIdentifier;

        public double minimumCost = Double.MAX_VALUE;

        public Vertex(final int nodeIdentifier)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = null;
            this.minimumCost    = 0.0;
        }

        public Vertex(final int nodeIdentifier, final Vertex previous)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = previous;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if(obj == this)
            {
                return true;
            }

            if(obj == null || this.getClass() != obj.getClass())
            {
               return false;
            }

            return this.nodeIdentifier == ((Vertex)obj).nodeIdentifier; // Is this enough? Node identifiers should be unique, right?
        }

        @Override
        public int hashCode()
        {
            return this.nodeIdentifier;
        }

        @Override
        public String toString()
        {
            return String.format("%d (%f, %d)",
                                 this.nodeIdentifier,
                                 this.minimumCost,
                                 this.previous.nodeIdentifier);
        }
    }

    public static List<Integer> astar(final GeoPackageNetworkExtension           networkExtension,
                                      final Network                              network,
                                      final Integer                              start,
                                      final Integer                              end,
                                      final Function<Edge, Double>               cost,
                                      final BiFunction<Integer, Integer, Double> heuristics) throws SQLException
    {
        //changed comparator -> change back to distance from end
        final PriorityQueue<VertexAstar> openList   = new PriorityQueue<>((o1, o2) -> Double.compare((o1.distanceFromEnd + o1.distanceFromStart), (o2.distanceFromEnd + o2.distanceFromStart)));
        final HashSet<VertexAstar>       closedList = new HashSet<>();

        final Map<Integer, VertexAstar> nodeMap = new HashMap<>();

        //initialize starting Vertex
        final VertexAstar startVertex = new VertexAstar(start);
        startVertex.distanceFromEnd = heuristics.apply(start, end);
        openList.add(startVertex);
        nodeMap.put(start, startVertex);

        while(!openList.isEmpty())
        {
            final VertexAstar currentVertex = openList.poll();//get the Vertex closest to the end

            //if current vertex is the target then we are done
            if(currentVertex.nodeIdentifier == end)
            {
                return getAstarPath( end, nodeMap);
            }

            closedList.add(currentVertex); //put it in "done" pile

            //for each reachable Vertex
            for(final Edge edge: networkExtension.getExits(network, currentVertex.nodeIdentifier))
            {
                final int adjacentNode = edge.getTo();

                VertexAstar reachableVertex = nodeMap.get(adjacentNode);

                if(reachableVertex == null)
                {
                    reachableVertex = new VertexAstar(adjacentNode);
                    reachableVertex.distanceFromEnd = Double.MAX_VALUE;
                    reachableVertex.distanceFromStart = Double.MAX_VALUE;
                    nodeMap.put(adjacentNode, reachableVertex);
                }

                //calculate a tentative distance (see if this is better than what you already may have)
                final double distanceFromStart = currentVertex.distanceFromStart + cost.apply(edge);
                final double distanceFromEnd   = heuristics.apply(reachableVertex.nodeIdentifier, end);
                //if the closed list already searched this vertex, skip it
                if(closedList.contains(reachableVertex))
                {
                    continue;
                }
                if(!openList.contains(reachableVertex))//if we dont have it, add it
                {
                    reachableVertex.distanceFromStart = distanceFromStart;
                    reachableVertex.distanceFromEnd   = distanceFromEnd;
                    reachableVertex.previous = currentVertex;
                    openList.add(reachableVertex);
                }
                //if this is better then update the values and parent Vertex (previous)
                else if(distanceFromStart < reachableVertex.distanceFromStart)
                {
                    reachableVertex.distanceFromStart = distanceFromStart;
                    reachableVertex.distanceFromEnd   = distanceFromEnd;
                    reachableVertex.previous          = currentVertex;
                    openList.remove(reachableVertex);
                    openList.add(reachableVertex);
                }
            }
        }
        throw new RuntimeException("Didnt find correct path");
        //return getAstarPath(end, nodeMap);//it shouldnt reach here...throw exception?
    }

    private static List<Integer> getAstarPath(final Integer end,
                                              final  Map<Integer, VertexAstar> nodeMap)
    {
        final LinkedList<Integer> path = new LinkedList<>();

        for(VertexAstar backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.previous)
        {
            path.addLast(backTrackVertex.nodeIdentifier);
        }

        return path;
    }

    /**
     * Based on http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
     * This algorithm will find the shortest path from the starting node to the ending node.
     *
     * @param start starting vertex
     * @param end   ending vertex
     * @param edgeCostEvaluator
     * @throws SQLException
     */
    public static List<Integer> dijkstra(final GeoPackageNetworkExtension networkExtension,
                                         final Network                    network,
                                         final Integer                    start,
                                         final Integer                    end,
                                         final Function<Edge, Double>     edgeEvaluator) throws SQLException
    {
        final Map<Integer, Vertex> nodeMap = new HashMap<>();

        final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>((o1, o2) -> Double.compare(o1.minimumCost, o2.minimumCost));

        final Vertex startVertex = new Vertex(start);

        vertexQueue.add(startVertex);

        while(!vertexQueue.isEmpty())
        {
            final Vertex currentVertex = vertexQueue.poll(); // Gets the vertex with min cost/distance

            networkExtension.getExits(network, currentVertex.nodeIdentifier)
                            .forEach(edge -> { final int adjacentNode = edge.getTo();

                                               Vertex reachableVertex = nodeMap.get(adjacentNode);

                                               if(reachableVertex == null)
                                               {
                                                   reachableVertex = new Vertex(adjacentNode, currentVertex);
                                                   nodeMap.put(adjacentNode, reachableVertex);
                                               }

                                               final double cost = currentVertex.minimumCost + edgeEvaluator.apply(edge); // Get the distance between nodes

                                               if(cost < reachableVertex.minimumCost) // If the cost is unset or less, change the cost
                                               {
                                                   vertexQueue.remove(reachableVertex);         // Remove it from the queue if it is there
                                                   reachableVertex.minimumCost = cost;          // Adjust to new values
                                                   reachableVertex.previous    = currentVertex; // Adjust the path
                                                   vertexQueue.add(reachableVertex);            // Add it to the queue in the correct order
                                               }
                                             });
        }

        final LinkedList<Integer> path = new LinkedList<>();

        for(Vertex backTrackVertex = nodeMap.get(end); backTrackVertex != null; backTrackVertex = backTrackVertex.previous)
        {
            path.addLast(backTrackVertex.nodeIdentifier);
        }

        return path;
    }
}
