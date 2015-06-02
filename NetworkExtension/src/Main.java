import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import java.util.stream.Stream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.util.functional.ThrowingFunction;
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

@SuppressWarnings({ "javadoc", "unused" })
public class Main
{
    private static final File geoPackageFile = new File("test.gpkg");
    private static final File nodeFile       = new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.node");
    private static final File edgeFile       = new File("C:/Users/corp/Desktop/sample data/networks/triangle/contour.1/contour.1.edge");
    private static final File dataFile       = new File("F:/Routing Test Data/mwtc_pandolf.sqlite");
    private static final File nodes          = new File("F:/Routing Test Data/MWTC_Nodes.txt"); // nodes file for usma_pandolf.gpkg
    //private static final File nodes          = new File("F:/Routing Test Data/MWTC_Nodes.txt");
    private static final File geoPackageFile2 = new File("test2.gpkg");
    private static final File geoPackageFile3 = new File("test3.gpkg");

    public static void main(final String[] args)
    {
        //runRoute2(1000);
        runRoute2(geoPackageFile2, 100);

        //createGpkg();

        //runRoute();
    }


    private static void runRoute2(final File geoPackage, final int routes)
    {
        final Random rand = new Random(123456789);
        try(final GeoPackage gpkg = new GeoPackage(geoPackage, OpenMode.Open))
        {
            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network network = networkExtension.getNetwork("mynetwork");

            final AttributeDescription nodeLatitudeAttibute = networkExtension.getAttributeDescription(network, "latitude", AttributedType.Node);

            final AttributeDescription nodeLongitudeAttibute = networkExtension.getAttributeDescription(network,"longitude", AttributedType.Node);

            final AttributeDescription distanceAttribute = networkExtension.getAttributeDescription(network, "length", AttributedType.Edge);

            final int[] start = rand.ints(routes, 0, 105248).toArray();
            final int[] end = rand.ints(routes, 0, 105248).toArray();
            int startNode;
			int endNode;
            double sum = 0;

            for (int i = 0; i < routes; i++)
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
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
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

                final Map<Integer, Double> costCache = new HashMap<>();

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

    @SuppressWarnings("hiding")
	private static void createGpkg2(final File geoPackageFile, final File dataFile, final File nodes) throws SQLException, ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC");   // Register the driver

        try(final Connection db = DriverManager.getConnection("jdbc:sqlite:" + dataFile.getPath()))// Initialize the database connection
        {
            if(geoPackageFile.exists())
            {
                geoPackageFile.delete();
            }
            try (final GeoPackage gpkg = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Create))
            {
            	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

                final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                      "Super Important Routing Stuff",
                                                                      "routing stuff. super important",
                                                                      new BoundingBox(0, 0, 0, 0),
                                                                      gpkg.core().getSpatialReferenceSystem(-1));

                final AttributeDescription slopeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "slope",
                                                                                                     "meters(?)",
                                                                                                     DataType.Real,
                                                                                                     "slope",
                                                                                                     AttributedType.Edge);

                final AttributeDescription lengthAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                      "length",
                                                                                                      "meters(?)",
                                                                                                      DataType.Real,
                                                                                                      "length",
                                                                                                      AttributedType.Edge);

                final AttributeDescription pandolfCostAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                           "cost_pandolf",
                                                                                                           "unknown",
                                                                                                           DataType.Real,
                                                                                                           "caloric cost walking?",
                                                                                                           AttributedType.Edge);

                final AttributeDescription elevationAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                         "elev",
                                                                                                         "meters(?)",
                                                                                                         DataType.Real,
                                                                                                         "elevation",
                                                                                                         AttributedType.Node);

                final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                       "longitude",
                                                                                                       "degrees",
                                                                                                        DataType.Real,
                                                                                                        "longitude",
                                                                                                        AttributedType.Node);

                final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                        "latitude",
                                                                                                        "degrees",
                                                                                                        DataType.Real,
                                                                                                        "latitude",
                                                                                                        AttributedType.Node);
                // Add the attributed edges
                final String query = String.format("Select %s, %s, %s, %s, %s FROM %s", "from_node", "to_node", "slope", "length", "cost_pandolf", "edges");
                try(final PreparedStatement stmt =  db.prepareStatement(query))
                {
                	try(ResultSet results = stmt.executeQuery())
                	{
                		loadAttributedEdges(networkExtension,
                		                    results,
                		                    myNetwork,
                		                    slopeAttribute,
                		                    lengthAttribute,
                		                    pandolfCostAttribute);
                	}
                }

                //Add attributed nodes
                loadNodeAttributes2(networkExtension,
                                    nodes,
                                    myNetwork,
                                    elevationAttribute,
                                    longitudeAttribute,
                                    latitudeAttribute);

            }
            catch (ConformanceException | IOException | BadImplementationException e)
    		{
    			e.printStackTrace();
    		}
        }
    }

    private static void createGpkg()
    {
        if(geoPackageFile.exists())
        {
            geoPackageFile.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Create))
        {

            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                  "Super Important Routing Stuff",
                                                                  "routing stuff. super important",
                                                                  new BoundingBox(0, 0, 0, 0),
                                                                  gpkg.core().getSpatialReferenceSystem(-1));

            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "longitude",
                                                                                                     "degrees",
                                                                                                     DataType.Real,
                                                                                                     "longitude",
                                                                                                     AttributedType.Node);

            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "latitude",
                                                                                                    "degrees",
                                                                                                    DataType.Real,
                                                                                                    "latitude",
                                                                                                    AttributedType.Node);

            final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "distance",
                                                                                                    "degrees",
                                                                                                    DataType.Real,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            loadNodeAttributes(networkExtension,
                               nodeFile,
                               myNetwork,
                               longitudeAttribute,
                               latitudeAttribute);

            loadEdges(networkExtension,
                      edgeFile,
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

    private static void calculateDistanceCost(final GeoPackageNetworkExtension networkExtension,
                                              final Network                    network,
                                              final AttributeDescription       distanceDescription,
                                              final AttributeDescription       longitudeDescription,
                                              final AttributeDescription       latitudeDescription) throws SQLException
    {
        networkExtension.visitEdges(network,
                                    edge -> { try
                                              {
                                                  final List<List<Object>> values = networkExtension.getNodeAttributes(Arrays.asList(edge.getFrom(), edge.getTo()),
                                                                                                                   longitudeDescription,
                                                                                                                   latitudeDescription);

                                                  final List<Object> startCoordinate = values.get(0);
                                                  final List<Object> endCoordinate   = values.get(1);

                                                  final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                  final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                  final double distance = Math.sqrt(latitude*latitude + longitude*longitude);

                                                  networkExtension.updateEdgeAttributes(edge,
                                                                                    Arrays.asList(distance),
                                                                                    distanceDescription);
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
                                           final File                        triangleFormatNodes,
                                           final Network                     network,
                                           final AttributeDescription...     attributeDescriptions) throws SQLException, IOException
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
            networkExtension.addNodes(pairs::iterator,
                                      attributeDescriptions);
        }
    }

    /**
     * Takes nodes in text file and adds them to
     */
    private static void loadNodeAttributes2(final GeoPackageNetworkExtension networkExtension,
    		                                final File nodes,
                                            final Network network,
                                            final AttributeDescription... attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                                      return new Pair<>(Integer.valueOf(pieces[0]), // vertex # (node id)
                                                                                                        Arrays.asList((Object)Double.valueOf(pieces[1]),   // elevation
                                                                                                                      (Object)Double.valueOf(pieces[2]),  // X (longitude)
                                                                                                                      (Object)Double.valueOf(pieces[3]))); // y (latitude)
       };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(nodes.toPath())
                                                                         .skip(1) // the first line is a header
                                                                         .filter(line -> !line.startsWith("#"))
                                                                         .map(lineToPair))
        {
            networkExtension.addNodes(pairs::iterator,
            		                  attributeDescriptions);
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

    private static void loadAttributedEdges(final GeoPackageNetworkExtension networkExtension,
                                            final ResultSet                  rs,
                                            final Network                    network,
                                            final AttributeDescription...    attributeDescriptions) throws SQLException
    {
        final List<Pair<Pair<Integer, Integer>, List<Object>>> edges = new ArrayList<Pair<Pair<Integer, Integer>, List<Object>>>();

        Pair<Integer, Integer> pair;
        while(rs.next())
        {
            pair = new Pair<Integer, Integer>(rs.getInt(1), rs.getInt(2));       // from node, and to node
            edges.add(new Pair<>(pair, Arrays.asList((Object)rs.getDouble(3),    // slope
                                                     (Object)rs.getDouble(4),    // length
                                                     (Object)rs.getDouble(5)))); // pandolf cost
        }

        networkExtension.addAttributedEdges(edges, attributeDescriptions);
    }

//    private String firstLine(final File file) throws IOException
//    {
//        try(Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.US_ASCII))
//        {
//            return lines.findFirst().orElseThrow(() -> new IOException("Empty file"));
//        }
//    }
}
