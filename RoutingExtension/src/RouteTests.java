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

import com.rgi.common.util.functional.ThrowingFunction;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.extensions.routing.RoutingNetworkDescription;
import com.rgi.geopackage.verification.ConformanceException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @author Luke Lambert
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "unused"})
public final class RouteTests
{
    public static final int    seed            = 123456789;
    public static final int    routeIterations = 100;
    public static final double nanoToSecond    = 1.0e9;

    private RouteTests()
    {
    }

    public static void main(final String[] args)
    {
        try(final GeoPackage gpkg = new GeoPackage(new File("routing_networks.gpkg"), GeoPackage.OpenMode.Open))
        {
            final GeoPackageRoutingExtension routingExtension = gpkg.extensions().getExtensionImplementation(GeoPackageRoutingExtension.class);

            routingExtension.getRoutingNetworkDescriptions()
                            .forEach(routingNetwork -> { try
                                                         {
                                                             final double seconds = testNetwork(routingExtension, routingNetwork);

                                                             System.out.format("%s finished %d routes in %f seconds\n",
                                                                               routingNetwork.getNetwork().getTableName(),
                                                                               routeIterations,
                                                                               seconds);
                                                         }
                                                         catch(final SQLException e)
                                                         {
                                                             e.printStackTrace();
                                                         }
                                                       });

        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            ex.printStackTrace();
        }
    }

    private static double testNetwork(final GeoPackageRoutingExtension routingExtension,
                                      final RoutingNetworkDescription  routingNetwork) throws SQLException
    {
        final Random rand = new Random(seed);

        final int maxNodeIdentifier = routingExtension.getNetworkExtension()
                                                      .getNodeCount(routingNetwork.getNetwork());

        final AttributeDescription costAttribute = routingExtension.getNetworkExtension()
                                                                   .getAttributeDescription(routingNetwork.getNetwork(),
                                                                                            "distance",
                                                                                            AttributedType.Edge);

        if(costAttribute == null)
        {
            return 0.0;
        }

        double totalTime = 0;

        for(int x = 0; x < routeIterations; ++x)
        {
            final long startTime = System.nanoTime();

            runRoute(routingExtension,
                     routingNetwork,
                     costAttribute,
                     rand.nextInt(maxNodeIdentifier),
                     rand.nextInt(maxNodeIdentifier));

            totalTime += (System.nanoTime() - startTime);
        }

        return (totalTime / nanoToSecond) / routeIterations;    // Average number of seconds per route call
    }

    private static void runRoute(final GeoPackageRoutingExtension routingExtension,
                                 final RoutingNetworkDescription  routingNetwork,
                                 final List<AttributeDescription> edgeAttributes,
                                 final int                        startNode,
                                 final int                        endNode) throws SQLException
    {
        final GeoPackageNetworkExtension networkExtension = routingExtension.getNetworkExtension();

        final AttributeDescription longitudeAttibute = routingNetwork.getLongitudeDescription();
        final AttributeDescription latitudeAttibute  = routingNetwork.getLatitudeDescription();

        final Map<Integer, Double> heuristicCache = new HashMap<>();
        final Map<Integer, Double> distanceCache  = new HashMap<>();

        routingExtension.aStar(routingNetwork,
                               startNode,
                               endNode,
                               (ThrowingFunction<Edge, Double>)edge -> { final int key = edge.getIdentifier();

                                                                         if(distanceCache.containsKey(key))
                                                                         {
                                                                             return distanceCache.get(key);
                                                                         }
                                                                         final Double cost = networkExtension.getEdgeAttribute(edge, edgeAttribute);
                                                                         distanceCache.put(key, cost);
                                                                         return cost;
                                                                       },
                               (startIdentifier, endIdentifier) -> { try
                                                                     {
                                                                         final int key = ((startIdentifier + endIdentifier)*(startIdentifier + endIdentifier + 1)/2) + endIdentifier;
                                                                         if(heuristicCache.containsKey(key))
                                                                         {
                                                                             return heuristicCache.get(key);
                                                                         }
                                                                         final List<Object> startCoordinate = networkExtension.getNodeAttributes(startIdentifier, longitudeAttibute, latitudeAttibute);
                                                                         final List<Object> endCoordinate   = networkExtension.getNodeAttributes(endIdentifier,   longitudeAttibute, latitudeAttibute);

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
    }

    private static void printPath(final GeoPackageNetworkExtension networkExtension,
                                  final Network                    network,
                                  final List<Integer>              path,
                                  final AttributeDescription       distanceAttribute)
    {
        final double totalWeight = IntStream.range(0, path.size() - 1)
                                            .mapToDouble(index -> { final int firstNode = path.get(index);
                                                                    final int secondNode = path.get(index + 1);

                                                                    try
                                                                    {
                                                                        final Edge edge = networkExtension.getEdge(network, firstNode, secondNode);
                                                                        final double cost = networkExtension.getEdgeAttribute(edge, distanceAttribute);

                                                                        System.out.printf("%f->(%d)-", cost, secondNode);

                                                                        return cost;
                                                                    }
                                                                    catch(final SQLException e)
                                                                    {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                  }).sum();

        System.out.println(String.format("\nTotal distance = %f", totalWeight));
    }
}
