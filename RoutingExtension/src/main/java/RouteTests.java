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

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedNode;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.extensions.routing.RoutingNetworkDescription;
import com.rgi.geopackage.verification.ConformanceException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

/**
 * @author Luke Lambert
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "unused"})
public final class RouteTests
{
    private static final int    seed            = 123456789;
    private static final int    routeIterations = 100;
    private static final double nanoToSecond    = 1.0e9;

    private static final double load = 40.0;
    private static final double weight = 85.0;
    private static final double velocity = 0.7;
    private static final double terrainFactor = 1.0;
    private static final double PECONSTANT1 = 1.5 * RouteTests.weight + 2.0*(RouteTests.weight + RouteTests.load)*(RouteTests.load / RouteTests.weight)*(RouteTests.load / RouteTests.weight) + 1.5* RouteTests.velocity * RouteTests.velocity * RouteTests.terrainFactor * RouteTests.weight + 1.5* RouteTests.velocity * RouteTests.velocity * RouteTests.terrainFactor * RouteTests.load;
    private static final double PECONSTANT2 = 0.35* RouteTests.velocity * RouteTests.terrainFactor;

    private static final double CFConstant1 = (RouteTests.velocity * RouteTests.terrainFactor *(RouteTests.weight + RouteTests.load))/3.5;
    private static final double CFConstant2 = (RouteTests.terrainFactor *(RouteTests.weight + RouteTests.load))/ RouteTests.weight;
    private static final double CFConstant3 = 25.0* RouteTests.terrainFactor * RouteTests.velocity * RouteTests.velocity;

    private RouteTests()
    {
    }

    public static void main(final String[] args)
    {
//        try(final GeoPackage gpkg = new GeoPackage(new File("routing_networks.gpkg"), GeoPackage.OpenMode.Open))
//        {
//            final GeoPackageRoutingExtension routingExtension = gpkg.extensions().getExtensionImplementation(GeoPackageRoutingExtension.class);
//
//            final RoutingNetworkDescription routingNetwork = routingExtension.getRoutingNetworkDescription("contour_1");
//
//            final AttributeDescription longitudeAttribute = routingNetwork.getLongitudeDescription();
//            final AttributeDescription latitudeAttribute  = routingNetwork.getLatitudeDescription();
//            final AttributeDescription elevationAttribute = routingNetwork.getElevationDescription();
//
//            final Route route = routingExtension.aStar(routingNetwork,
//                                   1678,
//                                   36553,
//                                   Arrays.asList(longitudeAttribute,
//                                                 latitudeAttribute,
//                                                elevationAttribute),
//                                   Collections.emptyList(),
//                                   edgeParams -> distance(edgeParams.getFrom(), edgeParams.getTo()),
//                                   RouteTests::distance2,
//                                   null,
//                                   null);
//
//            System.out.println(route.getNodesAttributes().size());
//        }
//        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
//        {
//            ex.printStackTrace();
//        }

        try(final GeoPackage gpkg = new GeoPackage(new File("routing_networks.gpkg"), GeoPackage.OpenMode.Open))
        {
            final GeoPackageRoutingExtension routingExtension = gpkg.extensions().getExtensionImplementation(GeoPackageRoutingExtension.class);

            //testNetworks(routingExtension, routingExtension.getRoutingNetworkDescriptions());
            testNetworks(routingExtension, Collections.singletonList(routingExtension.getRoutingNetworkDescription("mwtc_pandolf")));


        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void testNetworks(final GeoPackageRoutingExtension          routingExtension,
                                     final Iterable<RoutingNetworkDescription> routingNetworks)
    {
        routingNetworks.forEach(routingNetwork -> { try
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

    private static double testNetwork(final GeoPackageRoutingExtension routingExtension,
                                      final RoutingNetworkDescription  routingNetwork) throws SQLException
    {
        final Random rand = new Random(seed);

        final int maxNodeIdentifier = routingExtension.getNetworkExtension()
                                                      .getNodeCount(routingNetwork.getNetwork());

        final AttributeDescription costAttribute = routingExtension.getNetworkExtension()
                                                                   .getAttributeDescription(routingNetwork.getNetwork(),
                                                                                            "cost_pandolf",
                                                                                            AttributedType.Edge);

        if(costAttribute == null)
        {
            return 0.0;
        }

        final AttributeDescription longitudeAttribute = routingNetwork.getLongitudeDescription();
        final AttributeDescription latitudeAttribute  = routingNetwork.getLatitudeDescription();
        final AttributeDescription elevationAttribute = routingNetwork.getElevationDescription();

        final Collection<AttributeDescription> nodeAttributes = Arrays.asList(longitudeAttribute,
                                                                              latitudeAttribute,
                                                                              elevationAttribute);

        double totalTime = 0;

        for(int x = 0; x < routeIterations; ++x)
        {
            final long startTime = System.nanoTime();

            routingExtension.aStar(routingNetwork,
                                   rand.nextInt(maxNodeIdentifier),
                                   rand.nextInt(maxNodeIdentifier),
                                   nodeAttributes,
                                   Collections.emptyList(),
                                   attributedEdge -> RouteTests.getCaloricCost(attributedEdge.getFromNode(), attributedEdge.getToNode()),
                                   RouteTests::getCaloricCost,
                                   null,
                                   null);

            totalTime += (System.nanoTime() - startTime);
        }

        return (totalTime / nanoToSecond) / routeIterations;    // Average number of seconds per route call
    }

    /**
     * Pandolf Caloric equation found at following link
     * https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CB4QFjAAahUKEwiE96OIwbXHAhUGOj4KHa2-BZs&url=http%3A%2F%2Fweb.stanford.edu%2F~clint%2FRun_Walk2004a.rtf&ei=_KbUVYSJOYb0-AGt_ZbYCQ&usg=AFQjCNE3KbcRBmdb04KbkMyU5UnOYh-U0Q&sig2=vg108EAL0net65gcbIsUFw&bvm=bv.99804247,d.cWw
     * http://www.researchgate.net/publication/279517297_Comparative_Analysis_of_Metabolic_Cost_Equations_A_Review
     * W= weight
     * L = load in kilograms
     * G = grade (%)
     * V = velocity (meters/second)
     * T = terrian factor
     *   Terrain factor categories
     *     1.0 = blacktop road or treadmill
     *     1.1 = dirt road
     *     1.2 = light brush
     *     1.5 = heavy brush
     *     1.8 = swampy bog
     *     2.1 = loose sand
     *     2.5 = soft snow (15cm depth)
     *     3.3 = soft snow (25 cm depth)
     *     4.1 = soft snow (35 cm depth)
     *  M = Metabolic rate (in Watts)
     *  CF = correction factor (accounting for metabolic rate for down hill)
     *  PE = pandolf equation
     *
     *  Our Simplified Version of PE
     *
     *  PE = PEConstant1 + PEConstant2(G*L + G*W)
     *
     *  PEConstant1 = 1.5*W + 2.0(W + L)*(1/W)(1/W) + 1.5*V*V*T*W + 1.5*V*V*T*L
     *  PEConstant2 = 0.35*V*T
     *
     *
     *  Derived from:
     *
     *  PE = 1.5*W + 2.0(W + L)*(1/W)(1/W) + T(W + L)*(1.5*V*V + 0.35*V*G)
     *       1.5*W + 2.0(W + L)*(1/W)(1/W) + (T*W + T*L)*(1.5*V*V + 0.35*V*G)
     *       1.5*W + 2.0(W + L)*(1/W)(1/W) + 1.5*V*V*T*W + 1.5*V*V*T*L + 0.35*V*G*T*L + 0.35*V*G*T*W
     *                         PEConstant1                             +     0.35*V*T(G*L + G*W)
     *       PEConstant1 + PEConstant2(G*L + G*W)
     *
     * Our Simplified Version CF
     *
     * CF = G*CFConstant1 - (G + 6)(G + 6)*CFConstant2 + CFContstant3
     *
     * CFConstant1 = (V*T(W+L))/3.5
     * CFConstant2 = (T(W+L))/W
     * CFConstant3 = 25*T*V*V
     *
     * Derived from:
     *
     * CF = T[((G(W + L)*V)/3.5)  +  ((W + L)(G + 6)^2)/W)     + 25*V*V)]
     *     (T*(G(W + L)*V)/3.5)   +  (T*(W + L)(G + 6)^2)/W)   + 25*T*V*V)
     *     G[(V*T(W + L))/3.5]    +  (G + 6)^2[T * (W + L)/W]  + 25*T*V*V
     *     G[CFConstant1]         +   (G + 6)^2[CFConstant2]   + CFConstant3
     *
     *
     * @param fromNode start node
     * @param toNode end node
     * @return caloric cost to traverse between those two nodes
     */
    private static double getCaloricCost(final AttributedNode fromNode, final AttributedNode toNode)
    {
        final double grade = RouteTests.getGrade(fromNode, toNode);
        final double pandolfEquation = RouteTests.PECONSTANT1 + RouteTests.PECONSTANT2 * (grade * RouteTests.load + grade * RouteTests.weight);
        double correctionFactor = 0.0;
        //if downhill
        if(grade < 0.0)
        {
            correctionFactor =grade * RouteTests.CFConstant1 - (grade + 6.0)*(grade + 6.0)* RouteTests.CFConstant2 + RouteTests.CFConstant3;
        }

        final double metabolicRate  = pandolfEquation-correctionFactor;//watts

        return metabolicRate * RouteTests.getDistance(fromNode, toNode)/4184.0/ RouteTests.velocity;
    }

    /**
     * Returns the percentage of grade traversing between two nodes
     * @param fromNode the node traveling from
     * @param toNode the node traveling to
     * @return percentage of grade traversing two nodes
     */
    private static double getGrade(final AttributedNode fromNode, final AttributedNode toNode)
    {
        final double longitude = (Double)toNode.getAttribute(0) - (Double)fromNode.getAttribute(0);
        final double latitude  = (Double)toNode.getAttribute(1) - (Double)fromNode.getAttribute(1);
        final double elevation = (Double)toNode.getAttribute(2) - (Double)fromNode.getAttribute(2);

        return 100.0 * elevation/(Math.sqrt(longitude*longitude + latitude*latitude));
    }

    private static double getDistance(final AttributedNode fromNode, final AttributedNode toNode)
    {
        final double longitude = (Double)toNode.getAttribute(0) - (Double)fromNode.getAttribute(0);
        final double latitude  = (Double)toNode.getAttribute(1) - (Double)fromNode.getAttribute(1);
        final double elevation = (Double)toNode.getAttribute(2) - (Double)fromNode.getAttribute(2);

        return Math.sqrt(latitude * latitude + longitude * longitude + elevation * elevation);
    }
}
