import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;


public class Main
{
    private static final File file = new File("test.gpkg");

    public static void main(final String[] args)
    {
        //createGpkg();
        runRoute();
    }

    private static void runRoute()
    {
        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Open))
        {
            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network network = networkExtension.getNetwork("mynetwork");

            final AttributeDescription distanceAttribute = networkExtension.getAttributeDescription(network,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            dijkstra(networkExtension,
                     network,
                     9036,
                     37236,
                     (from, to) -> { try
                                     {
                                         final Edge edge = networkExtension.getEdge(network, from, to);
                                         return networkExtension.getAttribute(edge.getIdentifier(), distanceAttribute);
                                     }
                                     catch(final Exception ex)
                                     {
                                         throw new RuntimeException(ex);
                                     }
                                   }).forEach(node -> System.out.println(node + ","));
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    private static void createGpkg()
    {
        if(file.exists())
        {
            file.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.OpenOrCreate))
        {

            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                  "Super Important Routing Stuff",
                                                                  "routing stuff. super important",
                                                                  new BoundingBox(0, 0, 0, 0),
                                                                  gpkg.core().getSpatialReferenceSystem(-1));

            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "longitude",
                                                                                                     DataType.Real,
                                                                                                     "longitude",
                                                                                                     AttributedType.Node);

            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "latitude",
                                                                                                    DataType.Real,
                                                                                                    "latitude",
                                                                                                    AttributedType.Node);

            final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "distance",
                                                                                                    DataType.Real,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            loadNodeAttributes(networkExtension,
                               new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.node"),
                               myNetwork,
                               Arrays.asList(longitudeAttribute, latitudeAttribute));

            loadEdges(networkExtension,
                      new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.edge"),
                      myNetwork);

            calculateDistanceCost(networkExtension,
                                  myNetwork,
                                  distanceAttribute,
                                  longitudeAttribute,
                                  latitudeAttribute);




        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    private static class Vertex
    {
        public Vertex previous;     // Parent node

        public final int nodeIdentifier;

        public Double minimumCost = null;

        public Vertex(final int nodeIdentifier)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = this;
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
        public String toString()
        {
            return String.format("%d (%f, %d)",
                                 this.nodeIdentifier,
                                 this.minimumCost,
                                 this.previous.nodeIdentifier);
        }
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
    public static List<Integer> dijkstra(final GeoPackageNetworkExtension           networkExtension,
                                         final Network                              network,
                                         final Integer                              start,
                                         final Integer                              end,
                                         final BiFunction<Integer, Integer, Double> edgeEvaluator) throws SQLException
    {
        final Map<Integer, Vertex> nodeMap = new HashMap<>();

//        if(!networkExtension.isNodeReachable(end))
//        {
//              return Collections.emptyList();
//        }

        final PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>((o1, o2) -> Double.compare(o1.minimumCost, o2.minimumCost));
        final Vertex startVertex = new Vertex(start);

        vertexQueue.add(startVertex);

        while(!vertexQueue.isEmpty())
        {
            final Vertex currentVertex = vertexQueue.poll(); // Gets the vertex with min cost/distance

            System.out.format("*** Processing node: %s ***\n", currentVertex);
            System.out.println("    queue count: " + vertexQueue.size());

            if(currentVertex.nodeIdentifier == end) // if this is the target node stop the search
            {
                final LinkedList<Integer> path = new LinkedList<>();
                int backTrackNode = end;

                while(backTrackNode != start)
                {
                    path.addLast(backTrackNode);
                    backTrackNode = nodeMap.get(backTrackNode).previous.nodeIdentifier;
                }

                return path;
            }



            final List<Integer> adjacent = networkExtension.getExits(network, currentVertex.nodeIdentifier);

            System.out.println("    adjacent nodes: " + String.join(", ",
                                           adjacent.stream()
                                                   .map(id -> id.toString())
                                                   .collect(Collectors.toList())));

            adjacent.forEach(nodeIdentifier -> { final Vertex reachableVertex = nodeMap.containsKey(nodeIdentifier) ? nodeMap.get(nodeIdentifier) : new Vertex(nodeIdentifier, currentVertex);

                                                 final double edgeCost = edgeEvaluator.apply(currentVertex.nodeIdentifier, reachableVertex.nodeIdentifier);

                                                 final double cost = currentVertex.minimumCost + edgeEvaluator.apply(currentVertex.nodeIdentifier, reachableVertex.nodeIdentifier); // Get the distance between nodes

                                                 System.out.println("    " + reachableVertex);

                                                 if(reachableVertex.minimumCost == null || cost < reachableVertex.minimumCost) // If the cost is unset or less, change the cost
                                                 {
                                                     System.out.format("    adjusting: %s to (%f, %d)\n",
                                                                       reachableVertex,
                                                                       cost,
                                                                       currentVertex.nodeIdentifier);

                                                     vertexQueue.remove(reachableVertex);         // Remove it from the queue if it is there
                                                     reachableVertex.minimumCost = cost;          // Adjust to new values
                                                     reachableVertex.previous    = currentVertex; // Adjust the path
                                                     vertexQueue.add(reachableVertex);            // Add it to the queue in the correct order
                                                 }
                                               });

            System.out.println("    queue count: " + vertexQueue.size());
        }

        return Collections.emptyList();
    }

    private static void calculateDistanceCost(final GeoPackageNetworkExtension networkExtension,
                                              final Network                    myNetwork,
                                              final AttributeDescription       distanceDescription,
                                              final AttributeDescription       longitudeDescription,
                                              final AttributeDescription       latitudeDescription) throws SQLException
    {
        networkExtension.visitEdges(myNetwork,
                                    edge -> { try
                                              {
                                                  final double fromLongitude = networkExtension.getAttribute(edge.getFrom(), longitudeDescription);
                                                  final double fromLatitude  = networkExtension.getAttribute(edge.getFrom(), latitudeDescription);

                                                  final double toLongitude   = networkExtension.getAttribute(edge.getTo(), longitudeDescription);
                                                  final double toLatitude    = networkExtension.getAttribute(edge.getTo(), latitudeDescription);

                                                  final double longitude = toLongitude - fromLongitude;
                                                  final double latitude  = toLatitude  - fromLatitude;

                                                  final double distance = Math.sqrt((longitude*longitude) + (latitude*latitude));

                                                  networkExtension.addAttributes(edge.getIdentifier(),
                                                                                 Arrays.asList(distanceDescription),
                                                                                 Arrays.asList(distance));
                                              }
                                              catch(final Exception ex)
                                              {
                                                  // TODO Auto-generated catch block
                                                  ex.printStackTrace();
                                              }
                                            });
    }


    /**
     * Puts a file in the Triangle utility node format
     * (https://www.cs.cmu.edu/~quake/triangle.node.html) into a network
     */
    private static void loadNodeAttributes(final GeoPackageNetworkExtension networkExtension,
                                           final File triangleFormatNodes,
                                           final Network network,
                                           final List<AttributeDescription> attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                                   return new Pair<>(Integer.valueOf(pieces[0]),                        // vertex # (node id)
                                                                                                     Arrays.asList((Object)Double.valueOf(pieces[1]),   // x (longitude)
                                                                                                                   (Object)Double.valueOf(pieces[2]))); // y (latitude)
                                                                                 };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(triangleFormatNodes.toPath())
                                                                                       .skip(1) // the first line is a header
                                                                                       .filter(line -> !line.startsWith("#"))
                                                                                       .map(lineToPair))
        {
            networkExtension.addAttributes(attributeDescriptions, pairs::iterator);
        }
    }

    private static void loadEdges(final GeoPackageNetworkExtension networkExtension, final File triangleFormatEdges, final Network network) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, Integer>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                              // Integer.valueOf(pieces[0]),                 // edge # (edge id), unused, we use our own id, but it should be the same in most cases

                                                                              return new Pair<>(Integer.valueOf(pieces[1]),  // from node
                                                                                                Integer.valueOf(pieces[2])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }

        // Now add the links in reverse (i.e., we've added one direction, A->B, now add B->A, since the original data had no directionality
        final Function<String, Pair<Integer, Integer>> lineToPair2 = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                               return new Pair<>(Integer.valueOf(pieces[2]),  // from node
                                                                                                 Integer.valueOf(pieces[1])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair2))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }
    }

//    private String firstLine(final File file) throws IOException
//    {
//        try(Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.US_ASCII))
//        {
//            return lines.findFirst().orElseThrow(() -> new IOException("Empty file"));
//        }
//    }
}
