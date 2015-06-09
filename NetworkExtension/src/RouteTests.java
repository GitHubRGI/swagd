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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
 * @author Luke Lambert
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "unused"})
public class RouteTests
{
    private static final File geoPackageFile1 = new File("contour.1.gpkg");
    private static final File geoPackageFile2 = new File("usma_pandolf.gpkg");
    private static final File geoPackageFile3 = new File("mwtc_pandolf.gpkg");

    public static void main(final String[] args)
    {
        final int nodes1 = 47181;
        final int nodes2 = 105247;
        final int nodes3 = 169027;
        runRoute2(geoPackageFile3, 100, nodes3);
    }

      private static void runRoute()
    {
        try(final GeoPackage gpkg = new GeoPackage(geoPackageFile1, OpenMode.Open))
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


                final List<Integer> path = RoutingAlgorithms.astar(networkExtension,
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

                    final HashMap<Long, Double> heuristicCache = new HashMap<>();
                    final HashMap<Integer, Double> distanceCache = new HashMap<>();
                    final List<Integer> path = RoutingAlgorithms.astar(networkExtension,
                                                                       network,
                                                                       startNode,
                                                                       endNode,
                                                                       (ThrowingFunction<Edge, Double>)(edge) -> {
                                                                                                                     final int key = edge.getIdentifier();
                                                                                                                     if(distanceCache.containsKey(key))
                                                                                                                     {
                                                                                                                         return distanceCache.get(key);
                                                                                                                     }
                                                                                                                     final Double cost = networkExtension.getEdgeAttribute(edge, distanceAttribute);
                                                                                                                     distanceCache.put(key, cost);
                                                                                                                     return cost;
                                                                                                                 },
                                                                       (startIdentifier, endIdentifier) -> { try
                                                                                                                {
                                                                                                                 final long key = ((startIdentifier + endIdentifier)*(startIdentifier + endIdentifier + 1)/2) + endIdentifier;
                                                                                                                 if(heuristicCache.containsKey(key))
                                                                                                                 {
                                                                                                                     return heuristicCache.get(key);
                                                                                                                 }
                                                                                                                 final List<Object> startCoordinate = networkExtension.getNodeAttributes(startIdentifier, nodeLongitudeAttibute, nodeLatitudeAttibute);
                                                                                                                 final List<Object> endCoordinate   = networkExtension.getNodeAttributes(endIdentifier,   nodeLongitudeAttibute, nodeLatitudeAttibute);

                                                                                                                 final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                                                                                 final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                                                                                 final double distance = Math.sqrt(latitude*latitude + longitude*longitude);
                                                                                                                 heuristicCache.put(key, distance);
                                                                                                                 return distance;
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
}
