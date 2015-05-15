import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

public class Main
{
    public static void main(final String[] args)
    {
        final File file = new File("test.gpkg");

//        if(file.exists())
//        {
//            file.delete();
//        }

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.OpenOrCreate))
        {

            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

//            final Network myNetwork = networkExtension.addNetwork("mynetwork",
//                                                                  "Super Important Routing Stuff",
//                                                                  "routing stuff. super important",
//                                                                  new BoundingBox(0, 0, 0, 0),
//                                                                  gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
//                                                                                                     "longitude",
//                                                                                                     DataType.Real,
//                                                                                                     "longitude",
//                                                                                                     AttributedType.Node);
//
//            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
//                                                                                                    "latitude",
//                                                                                                    DataType.Real,
//                                                                                                    "latitude",
//                                                                                                    AttributedType.Node);
//
//            final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(myNetwork,
//                                                                                                    "distance",
//                                                                                                    DataType.Real,
//                                                                                                    "distance",
//                                                                                                    AttributedType.Edge);
//
//            loadNodeAttributes(networkExtension,
//                               new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.node"),
//                               myNetwork,
//                               Arrays.asList(longitudeAttribute, latitudeAttribute));
//
//            loadEdges(networkExtension,
//                      new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.edge"),
//                      myNetwork);
//
//            calculateDistanceCost(networkExtension,
//                                  myNetwork,
//                                  distanceAttribute,
//                                  longitudeAttribute,
//                                  latitudeAttribute);

            final Network myNetwork = networkExtension.getNetwork("mynetwork");


        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    private class Vertex<T> // T = cost type
    {
        public Vertex<T> previous;     // Parent node

        public final int nodeIdentifier;

        public T minimumCost   = null; // the minimum cost of this vertex

        public Vertex(final int nodeIdentifier)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = this;
        }

        public Vertex(final int nodeIdentifier, final Vertex<T> previous, final T minimumCost)
        {
            this.nodeIdentifier = nodeIdentifier;
            this.previous       = previous;
            this.minimumCost    = minimumCost;
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

            return this.nodeIdentifier  == ((Vertex<?>)obj).nodeIdentifier; // Is this enough? Node identifiers should be unique, right?
        }
    }


    /**
     * Based on http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
     * This algorithm will find the shortest path from the starting node to the ending node.
     *
     * @param start starting vertex
     * @param end   ending vertex
     * @param nodeCostEvaluator
     * @throws SQLException
     */
    public <T extends Comparable<T>> void dijkstra(final GeoPackageNetworkExtension      networkExtension,
                                                   final Network                         network,
                                                   final Integer                         start,
                                                   final Integer                         end,
                                                   final BiFunction<Integer, Integer, T> edgeCostEvaluator) throws SQLException
    {
        final int count = 0;
        //this.clearVertices();

        //final Map<Integer, T> minCost = new HashMap<>();

//        if((this.checkVertexInGraph(start) && this.checkVertexInGraph(end)) != true)
//        {
//            return;
//        }

         final PriorityQueue<Vertex<T>> vertexQueue = new PriorityQueue<>((o1, o2) -> (o1 == null) ? ((o2 == null) ? 0 : -1)
                                                                                                   : o1.minimumCost.compareTo(o2.minimumCost)); // what if o2 is null here?
         final Vertex<T> startVertex = new Vertex<>(start);

         vertexQueue.add(startVertex);

         while(!vertexQueue.isEmpty())
         {
             final Vertex<T> currentVertex = vertexQueue.poll(); // Gets the vertex with min cost/distance

             if(currentVertex.nodeIdentifier == end) // if this is the target node stop the search
             {
                 System.out.printf("\n\nNumber of nodes visited for dijkstra with target: %d", count);
                 return;
             }


             final List<Vertex<T>> adjacentVertices = networkExtension.getExits(network, currentVertex.nodeIdentifier)
                                                                      .stream()
                                                                      .map(nodeIdentifier -> new Vertex<>(nodeIdentifier,
                                                                                                          currentVertex,
                                                                                                          edgeCostEvaluator.apply(currentVertex.nodeIdentifier,
                                                                                                                                  nodeIdentifier)))
                                                                      .collect(Collectors.toList());

             //visit every edge exiting current Vertex
             this.visitReachableVertices(currentVertex,
                                         adjacentVertices,
                                         vertexQueue);
         }
    }

    /**
     * Visits the neighboring nodes and updates the cost if the new cost is less
     *
     * @param currentVertex
     * @param adjacentVertices
     * @param queue
     */
    public <T> void visitReachableVertices(final Vertex<T>                currentVertex,
                                           final Iterable<Vertex<T>>      adjacentVertices,
                                           final PriorityQueue<Vertex<T>> queue)
     {
         for(final Vertex<T> reachableNode : adjacentVertices)
         {
              if(cost < reachableNode.minDistance)      // If the cost is less, change the weight
              {
                  queue.remove(reachableNode);            // Remove it from the queue if it is there
                  reachableNode.minDistance = cost;       // Adjust to new values
                  reachableNode.previous = currentVertex; // Adjust the path
                  queue.add(reachableNode);               // Add it to the queue in the correct order
              }

              this.count++;
         }
     }

//    private static void calculateDistanceCost(final GeoPackageNetworkExtension networkExtension,
//                                              final Network                    myNetwork,
//                                              final AttributeDescription       distanceDescription,
//                                              final AttributeDescription       longitudeDescription,
//                                              final AttributeDescription       latitudeDescription) throws SQLException
//    {
//        networkExtension.visitEdges(myNetwork,
//                                    edge -> { try
//                                              {
//                                                  final double fromLongitude = networkExtension.getAttribute(edge.getFrom(), longitudeDescription);
//                                                  final double fromLatitude  = networkExtension.getAttribute(edge.getFrom(), latitudeDescription);
//
//                                                  final double toLongitude   = networkExtension.getAttribute(edge.getFrom(), longitudeDescription);
//                                                  final double toLatitude    = networkExtension.getAttribute(edge.getFrom(), latitudeDescription);
//
//                                                  final double longitude = toLongitude - fromLongitude;
//                                                  final double latitude  = toLatitude  - fromLatitude;
//
//                                                  final double distance = Math.sqrt((longitude*longitude) + (latitude*latitude));
//
//                                                  networkExtension.addAttributes(edge.getIdentifier(),
//                                                                                 Arrays.asList(distanceDescription),
//                                                                                 Arrays.asList(distance));
//
//                                              }
//                                              catch(final Exception ex)
//                                              {
//                                                  // TODO Auto-generated catch block
//                                                  ex.printStackTrace();
//                                              }
//
//                                            });
//    }
//
//
//    /**
//     * Puts a file in the Triangle utility node format
//     * (https://www.cs.cmu.edu/~quake/triangle.node.html) into a network
//     */
//    private static void loadNodeAttributes(final GeoPackageNetworkExtension networkExtension,
//                                           final File triangleFormatNodes,
//                                           final Network network,
//                                           final List<AttributeDescription> attributeDescriptions) throws SQLException, IOException
//    {
//        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");
//
//                                                                                   return new Pair<>(Integer.valueOf(pieces[0]),                        // vertex # (node id)
//                                                                                                     Arrays.asList((Object)Double.valueOf(pieces[1]),   // x (longitude)
//                                                                                                                   (Object)Double.valueOf(pieces[2]))); // y (latitude)
//                                                                                 };
//
//        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(triangleFormatNodes.toPath())
//                                                                                       .skip(1) // the first line is a header
//                                                                                       .filter(line -> !line.startsWith("#"))
//                                                                                       .map(lineToPair))
//        {
//            networkExtension.addAttributes(attributeDescriptions, pairs::iterator);
//        }
//    }
//
//    private static void loadEdges(final GeoPackageNetworkExtension networkExtension, final File triangleFormatEdges, final Network network) throws SQLException, IOException
//    {
//        final Function<String, Pair<Integer, Integer>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");
//
//                                                                              // Integer.valueOf(pieces[0]),                 // edge # (edge id), unused, we use our own id, but it should be the same in most cases
//
//                                                                              return new Pair<>(Integer.valueOf(pieces[1]),  // from node
//                                                                                                Integer.valueOf(pieces[2])); // to node
//                                                                            };
//
//        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
//                                                                                        .skip(1) // the first line is a header
//                                                                                        .filter(line -> !line.startsWith("#"))
//                                                                                        .map(lineToPair))
//        {
//            networkExtension.addEdges(network, pairs::iterator);
//        }
//
//        // Now add the links in reverse (i.e., we've added one direction, A->B, now add B->A, since the original data had no directionality
//        final Function<String, Pair<Integer, Integer>> lineToPair2 = line -> { final String[] pieces = line.trim().split("\\s+");
//
//                                                                               return new Pair<>(Integer.valueOf(pieces[2]),  // from node
//                                                                                                 Integer.valueOf(pieces[1])); // to node
//                                                                            };
//
//        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
//                                                                                        .skip(1) // the first line is a header
//                                                                                        .filter(line -> !line.startsWith("#"))
//                                                                                        .map(lineToPair2))
//        {
//            networkExtension.addEdges(network, pairs::iterator);
//        }
//    }

//    private String firstLine(final File file) throws IOException
//    {
//        try(Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.US_ASCII))
//        {
//            return lines.findFirst().orElseThrow(() -> new IOException("Empty file"));
//        }
//    }
}
