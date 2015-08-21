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

package com.rgi.geopackage.extensions.routing;

import com.rgi.common.Memoize2;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.implementation.ExtensionImplementation;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.EdgeEvaluationParameters;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.extensions.network.Node;
import com.rgi.geopackage.utility.DatabaseUtility;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of the RGI Routing GeoPackage extension
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageRoutingExtension extends ExtensionImplementation
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     * @param geoPackageCore
     *             'Core' subsystem of the {@link GeoPackage} implementation
     * @param geoPackageExtensions
     *             'Extensions' subsystem of the {@link GeoPackage} implementation
     * @throws SQLException
     *             if getting the corresponding {@link Extension} from the
     *             {@link GeoPackage} fails
     * @throws BadImplementationException
     *             if the Class type parameter doesn't match the requirements
     *             needed to create the requested extension.  See {@link
     *             BadImplementationException#getCause()} for more details
     */
    public GeoPackageRoutingExtension(final Connection           databaseConnection,
                                      final GeoPackageCore       geoPackageCore,
                                      final GeoPackageExtensions geoPackageExtensions) throws SQLException, BadImplementationException
    {
        super(databaseConnection, geoPackageCore, geoPackageExtensions);

        this.networkExtension = this.geoPackageExtensions.getExtensionImplementation(GeoPackageNetworkExtension.class);
    }


    @Override
    public String getTableName()
    {
        return null;
    }

    @Override
    public String getColumnName()
    {
        return null;
    }

    @Override
    public String getExtensionName()
    {
        return "rgi_routing";
    }

    @Override
    public String getDefinition()
    {
        return "definition"; // TODO
    }

    @Override
    public Scope getScope()
    {
        return Scope.ReadWrite;
    }

    /**
     * @return the {@link GeoPackageNetworkExtension} object used by the
     *             routing extension
     */
    public GeoPackageNetworkExtension getNetworkExtension()
    {
        return this.networkExtension;
    }

    /**
     * Gets a specific routing network by table name, or null if no routing
     * network exists with that table name.
     *
     * @param networkTableName
     *             Name of the routing network table
     * @return handle to the routing network
     * @throws SQLException
     *             if there is a database error
     */
    public RoutingNetworkDescription getRoutingNetworkDescription(final String networkTableName) throws SQLException
    {
        if(networkTableName == null)
        {
            throw new IllegalArgumentException("Network table name may not be null");
        }

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, RoutingNetworkDescriptionsTableName))
        {
            return null;
        }

        final String routingNetworkDescriptionQuery = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = ?;",
                                                                    "longitude_attribute",
                                                                    "latitude_attribute",
                                                                    "elevation_attribute",
                                                                    "distance_attribute",
                                                                    RoutingNetworkDescriptionsTableName,
                                                                    "table_name");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     routingNetworkDescriptionQuery,
                                     preparedStatement -> preparedStatement.setString(1, networkTableName),
                                     resultSet -> { final Network network = this.networkExtension.getNetwork(networkTableName);

                                                    final AttributeDescription longitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(1), AttributedType.Node);
                                                    final AttributeDescription latitudeDescription  = this.networkExtension.getAttributeDescription(network, resultSet.getString(2), AttributedType.Node);
                                                    final AttributeDescription elevationDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(3), AttributedType.Node);
                                                    final AttributeDescription distanceDescription  = this.networkExtension.getAttributeDescription(network, resultSet.getString(4), AttributedType.Edge);

                                                    return new RoutingNetworkDescription(network,
                                                                                         longitudeDescription,
                                                                                         latitudeDescription,
                                                                                         elevationDescription,
                                                                                         distanceDescription);
                                                  });
    }

    /**
     * Gets the routing networks from a GeoPackage
     *
     * @return Collection of the routing descriptions of networks contained in
     *             the GeoPackage
     * @throws SQLException
     *             if there is a database error
     */
    public List<RoutingNetworkDescription> getRoutingNetworkDescriptions() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, RoutingNetworkDescriptionsTableName))
        {
            return Collections.emptyList();
        }

        final String routingNetworkDescriptionQuery = String.format("SELECT %s, %s, %s, %s, %s FROM %s;",
                                                                    "table_name",
                                                                    "longitude_attribute",
                                                                    "latitude_attribute",
                                                                    "elevation_attribute",
                                                                    "distance_attribute",
                                                                    RoutingNetworkDescriptionsTableName);

        return JdbcUtility.select(this.databaseConnection,
                                  routingNetworkDescriptionQuery,
                                  null,
                                  resultSet -> { final Network network = this.networkExtension.getNetwork(resultSet.getString(1));

                                                 final AttributeDescription longitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(2), AttributedType.Node);
                                                 final AttributeDescription  latitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(3), AttributedType.Node);
                                                 final AttributeDescription elevationDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(4), AttributedType.Node);
                                                 final AttributeDescription  distanceDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(5), AttributedType.Edge);

                                                 return new RoutingNetworkDescription(network,
                                                                                      longitudeDescription,
                                                                                      latitudeDescription,
                                                                                      elevationDescription,
                                                                                      distanceDescription);
                                               });
    }

    /**
     * Associates a routing description with a network
     *
     * @param network
     *             Routing network being searched for the closest node
     * @param longitudeDescription
     *             Routing network attribute description for the horizontal
     *             portion of a node's coordinate
     * @param latitudeDescription
     *             Routing network attribute description for the vertical
     *             portion of a node's coordinate
     * @param elevationDescription
     *             Routing network attribute description for the elevation
     *             portion of a node's coordinate. This value may be null if
     *             the network is only in two dimensions
     * @param distanceDescription
     *             Routing network attribute description for the distance
     *             between an edge's two nodes
     * @return A handle to the newly created {@link RoutingNetworkDescription}
     * @throws SQLException
     *             if there is a database error
     */
    public RoutingNetworkDescription addRoutingNetworkDescription(final Network              network,
                                                                  final AttributeDescription longitudeDescription,
                                                                  final AttributeDescription latitudeDescription,
                                                                  final AttributeDescription elevationDescription,
                                                                  final AttributeDescription distanceDescription) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(longitudeDescription == null                                    ||
           longitudeDescription.getAttributedType() != AttributedType.Node ||
           !longitudeDescription.getNetworkTableName().equals(network.getTableName()))
        {
            throw new IllegalArgumentException("Longitude description may not be null, it must refer to a node, and must refer to the supplied network");
        }

        if(latitudeDescription == null                                    ||
           latitudeDescription.getAttributedType() != AttributedType.Node ||
           !latitudeDescription.getNetworkTableName().equals(network.getTableName()))
        {
            throw new IllegalArgumentException("Latitude description may not be null, it must refer to a node, and must refer to the supplied network");
        }

        if(elevationDescription != null &&
           (elevationDescription.getAttributedType() != AttributedType.Node ||
            !elevationDescription.getNetworkTableName().equals(network.getTableName())))
        {
            throw new IllegalArgumentException("If the elevation description is not null, it must refer to a node, and must refer to the supplied network");
        }

        if(distanceDescription == null                                    ||
           distanceDescription.getAttributedType() != AttributedType.Edge ||
           !distanceDescription.getNetworkTableName().equals(network.getTableName()))
        {
            throw new IllegalArgumentException("Distance description may not be null, it must refer to an edge, and must refer to the supplied network");
        }

        try
        {
            if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, RoutingNetworkDescriptionsTableName))
            {
                JdbcUtility.update(this.databaseConnection, GeoPackageRoutingExtension.getRoutingNetworkDescriptionCreationSql());
            }


            JdbcUtility.update(this.databaseConnection,
                               String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                             GeoPackageRoutingExtension.RoutingNetworkDescriptionsTableName,
                                             "table_name",
                                             "longitude_attribute",
                                             "latitude_attribute",
                                             "elevation_attribute",
                                             "distance_attribute"),
                               preparedStatement -> { final String elevationDescriptionName = elevationDescription == null ? null : elevationDescription.getName();

                                                      preparedStatement.setString(1, network.getTableName());
                                                      preparedStatement.setString(2, longitudeDescription.getName());
                                                      preparedStatement.setString(3, latitudeDescription. getName());
                                                      preparedStatement.setString(4, elevationDescriptionName);
                                                      preparedStatement.setString(5, distanceDescription. getName());
                                                    });

            this.databaseConnection.commit();

            final RoutingNetworkDescription routingNetwork = new RoutingNetworkDescription(network,
                                                                                           longitudeDescription,
                                                                                           latitudeDescription,
                                                                                           elevationDescription,
                                                                                           distanceDescription);

            this.addExtensionEntry();

            return routingNetwork;
        }
        catch(final Throwable th)
        {
            this.databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Returns the node identifier of the closest node to a point
     *
     * @param routingNetwork
     *             Routing network being searched for the closest node
     * @param longitude
     *             Horizontal component of a coordinate
     * @param latitude
     *             Vertical component of a coordinate
     * @return Node identifier of the closest node to a point
     * @throws SQLException
     *             if there is a database error
     */
    public Integer getClosestNode(final RoutingNetworkDescription routingNetwork,
                                  final double                    longitude,
                                  final double                    latitude) throws SQLException
    {
        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Routing network description may not be null");
        }

        final String distanceQuery = String.format("SELECT %s, MIN(((%2$s - %3$f) * (%2$s - %3$f)) + ((%4$s - %5$s) * (%4$s - %5$s))) as distSqrd FROM %6$s;",
                                                   "node_id",
                                                   routingNetwork.getLongitudeDescription().getName(),
                                                   longitude,
                                                   routingNetwork.getLatitudeDescription().getName(),
                                                   latitude,
                                                   GeoPackageNetworkExtension.getNodeAttributesTableName(routingNetwork.getNetwork().getTableName()));

        return JdbcUtility.selectOne(this.databaseConnection,
                                     distanceQuery,
                                     null,
                                     resultSet -> resultSet.getInt(1));
    }

    /**
     * Applies a callback to edges that are "close to" a specified radial area
     *
     * @param routingNetwork
     *             Routing network being searched for the closest node
     * @param centerX
     *             x coordinate for the center of the circle bounds
     * @param centerY
     *             y coordinate for the center of the circle bounds
     * @param radius
     *             the radius for the circle bounds
     * @param nodeAttributes
     *            Attributes of each network node to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link Node#getAttributes()} as an array of {@link Object}s
     *            in the <i>in the order in which the {@link
     *            AttributeDescription}s are specified</i>.
     * @param edgeAttributes
     *            Attributes of each network edge to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link EdgeEvaluationParameters#getEdgeAttributes()} as an
     *            array of {@link Object}s in the <i>in the order in which the
     *            {@link AttributeDescription}s are specified</i>.
     * @param visitor
     *             Callback applied to each edge
     * @throws SQLException
     *             if there is a database error
     */
    public void visitEdgesCloseToCircle(final RoutingNetworkDescription          routingNetwork,
                                        final double                             centerX,
                                        final double                             centerY,
                                        final double                             radius,
                                        final Collection<AttributeDescription>   nodeAttributes,
                                        final Collection<AttributeDescription>   edgeAttributes,
                                        final Consumer<EdgeEvaluationParameters> visitor) throws SQLException
    {
        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Routing network description may not be null");
        }

        if(visitor == null)
        {
            throw new IllegalArgumentException("The visitor callback may not be null");
        }

        final String networkTableName               = routingNetwork.getNetwork().getTableName();
        final String networkNodeAttributesTableName = GeoPackageNetworkExtension.getNodeAttributesTableName(networkTableName);
        final String longitudeName                  = routingNetwork.getLongitudeDescription().getName();
        final String latitudeName                   = routingNetwork.getLatitudeDescription() .getName();
        final String distanceName                   = routingNetwork.getDistanceDescription() .getName();

        final String edgeQuery = String.format("SELECT id, from_node, to_node " +
                                               "FROM %1$s "+
                                               "WHERE  EXISTS "+
                                                      "(SELECT NULL "+
                                                      "FROM %2$s "+
                                                      "WHERE (node_id = from_node OR node_id = to_node) "+
                                                      "AND (((%3$s - %5$f) * (%3$s - %5$f) + (%4$s - %6$f) * (%4$s - %6$f)) - %7$f) <= (%8$s*%8$s))",
                                               networkTableName,               // %1$s
                                               networkNodeAttributesTableName, // %2$s
                                               longitudeName,                  // %3$s
                                               latitudeName,                   // %4$s
                                               centerX,                        // %5$f
                                               centerY,                        // %6$f
                                               radius*radius,                  // %7$f
                                               distanceName);                  // %8$s

        JdbcUtility.forEach(this.databaseConnection,
                            edgeQuery,
                            null,
                            resultSet -> visitor.accept(this.networkExtension.getEdgeEvaluationParameters(resultSet.getInt(1),
                                                                                                          resultSet.getInt(2),
                                                                                                          resultSet.getInt(3),
                                                                                                          nodeAttributes,
                                                                                                          edgeAttributes)));
    }

    /**
     * Applies a callback to edges that intersect with a specified radial area
     *
     * @param routingNetwork
     *             Routing network being searched for the closest node
     * @param centerX
     *             x coordinate for the center of the circle bounds
     * @param centerY
     *             y coordinate for the center of the circle bounds
     * @param radius
     *             the radius for the circle bounds
     * @param visitor
     *             Callback applied to each edge
     * @throws SQLException
     *             if there is a database error
     */
    public void visitEdgesInCircle(final RoutingNetworkDescription routingNetwork,
                                   final double                    centerX,
                                   final double                    centerY,
                                   final double                    radius,
                                   final Consumer<Integer>         visitor) throws SQLException
    {
        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Routing network description may not be null");
        }

        if(visitor == null)
        {
            throw new IllegalArgumentException("Visitor callback may not be null");
        }

        if(radius < 0.0)
        {
            throw new IllegalArgumentException("Radius may not be less than 0");
        }

        if(radius > Math.sqrt(Double.MAX_VALUE))
        {
            throw new IllegalArgumentException("Radius exceeds the square root of the maximum size of a double which will cause a numeric overflow");
        }

        final String networkTableName               = routingNetwork.getNetwork().getTableName();
        final String networkNodeAttributesTableName = GeoPackageNetworkExtension.getNodeAttributesTableName(networkTableName);
        final String longitudeName                  = routingNetwork.getLongitudeDescription().getName();
        final String latitudeName                   = routingNetwork.getLatitudeDescription() .getName();

        // The following SQL query is asking which edges intersect (partially
        // or completely) with a circle. It does this by finding the shortest
        // distance between the circle's center and each edge. If that distance
        // is less than or equal to the radius of the circle, the two objects
        // intersect. We operate on square distances because SQLite doesn't
        // have a square root operation. The algorithm is based on the
        // pseudocode found here: https://stackoverflow.com/a/6853926/16434
        //
        // function pDistance(x, y, x1, y1, x2, y2)
        // {
        //     var A = x - x1;
        //     var B = y - y1;
        //     var C = x2 - x1;
        //     var D = y2 - y1;
        //
        //     var dot = A * C + B * D;
        //     var len_sq = C * C + D * D;
        //     var param = -1;
        //     if(len_sq != 0) //in case of 0 length line
        //         param = dot / len_sq;
        //
        //     var xx, yy;
        //
        //     if(param < 0)
        //     {
        //       xx = x1;
        //       yy = y1;
        //     }
        //     else if (param > 1)
        //     {
        //       xx = x2;
        //       yy = y2;
        //     }
        //     else
        //     {
        //       xx = x1 + param * C;
        //       yy = y1 + param * D;
        //     }
        //
        //     var dx = x - xx;
        //     var dy = y - yy;
        //     return Math.sqrt(dx * dx + dy * dy);
        // }

        // I apologize for how disgusting the whole thing ends up looking.

        final String edgeQuery = String.format("SELECT id\n" +
                                               // Compute "param"
                                               "FROM (SELECT id, x1, y1, x2, y2, c, d,\n" +
                                                            // "switch" to determine the value of "param"
                                                            "CASE WHEN x1 = x2 AND y1 = y2\n" +              // points 1 and 2 are the same, so the distance/length between them is 0. don't divide by 0.
                                                                 "THEN -1\n" +
                                                                 "ELSE (a * c + b * d) / (c * c + d * d)\n" + // dot / length squared
                                                                 "END AS param\n" +
                                                     // Compute a, b, c, d and pass along other properties
                                                     "FROM (SELECT id, x1, y1, x2, y2,\n" +
                                                                  "(%1$f - x1) AS a,\n" +
                                                                  "(%2$f - y1) AS b,\n" +
                                                                  "(  x2 - x1) AS c,\n" +
                                                                  "(  y2 - y1) AS d\n" +
                                                           // Select the edge (id, and from/to nodes) as well as the x/y of the from/to nodes
                                                           "FROM (SELECT id\n" +
                                                                        "a1.%3$s AS x1,\n" +
                                                                        "a1.%4$s AS y1,\n" +
                                                                        "a2.%3$s AS x2,\n" +
                                                                        "a2.%4$s AS y2\n" +
                                                                 "FROM %5$s,\n" +
                                                                      "%6$s AS a1,\n" +
                                                                      "%6$s AS a2\n" +
                                                                 "WHERE a1.node_id = %5$s.from_node AND\n" +
                                                                       "a2.node_id = %5$s.to_node)))\n" +
                                               // dx = x - xx
                                               // dy = y - yy
                                               // distance squared = dx*dx + dy*dy
                                               "WHERE (param < 0 AND (%1$f - x1) * (%1$f - x1) + (%2$f - y1) * (%2$f - y1) <= %7$f) OR\n" +                                      // xx, yy = x1, y1
                                                     "(param > 1 AND (%1$f - x2) * (%1$f - x2) + (%2$f - y2) * (%2$f - y2) <= %7$f) OR\n" +                                      // xx, yy = x2, y2
                                                     "((%1$f - (x1 + param * c)) * (%1$f - (x1 + param * c)) + (%2$f - (y1 + param * d)) * (%2$f - (y1 + param * d)) <= %7$f);", // xx, yy = (x1 + param * c), (y1 + param * d)
                                               centerX,                         // %1$f
                                               centerY,                         // %2$f
                                               longitudeName,                   // %3$s
                                               latitudeName,                    // %4$s
                                               networkTableName,                // %5$s
                                               networkNodeAttributesTableName,  // %6$s
                                               radius*radius);                  // %7$f

        JdbcUtility.forEach(this.databaseConnection,
                            edgeQuery,
                            null,
                            resultSet -> visitor.accept(resultSet.getInt(1)));
    }

    /**
     * Returns a list of node identifier that lie in a rectangle boundary given
     *
     *
     * @param routingNetwork
     *             Routing network being searched for the closest node
     * @param minimumX
     *             minimum x value in rectangle
     * @param minimumY
     *             minimum y value in rectangle
     * @param maximumX
     *             maximum x value in rectangle
     * @param maximumY
     *             maximum y value in rectangle
     * @return a list of node identifier in the contained in the rectangle region
     * @throws SQLException
     *             if there is a database error
     */
    public List<Integer> getNodesInBoundingBox(final RoutingNetworkDescription        routingNetwork,
                                               final double                           minimumX,
                                               final double                           minimumY,
                                               final double                           maximumX,
                                               final double                           maximumY) throws SQLException
    {
        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Routing network description may not be null");
        }

        final String nodeQuery = String.format("SELECT %1$s, %2$s "+
                                               "FROM %3$s "+
                                               "WHERE %1$s <= %6$s AND %1$s >= %4$s AND %2$s <= %7$s AND %2$s >= %5$s",
                                               routingNetwork.getLongitudeDescription().getName(),
                                               routingNetwork. getLatitudeDescription().getName(),
                                               GeoPackageNetworkExtension.getNodeAttributesTableName(routingNetwork.getNetwork().getTableName()),
                                               minimumX,
                                               minimumY,
                                               maximumX,
                                               maximumY);

        return JdbcUtility.select(this.databaseConnection,
                                  nodeQuery,
                                  null,
                                  resultSet -> resultSet.getInt(1));
    }


    /**
     * This algorithm will find the shortest path from the starting
     * node to the ending node
     *
     * @param routingNetwork
     *            Network on which to route between a start and end node
     * @param startNodeIdentifier
     *            Starting node
     * @param endNodeIdentifier
     *            Ending node
     * @param nodeAttributes
     *            Attributes of each network node to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link Node#getAttributes()} as an array of {@link Object}s
     *            in the <i>in the order in which the {@link
     *            AttributeDescription}s are specified</i>.
     * @param edgeAttributes
     *            Attributes of each network edge to query for. These
     *            attributes will be passed to the edge cost evaluator via
     *            {@link EdgeEvaluationParameters#getEdgeAttributes()} as an
     *            array of {@link Object}s in the <i>in the order in which the
     *            {@link AttributeDescription}s are specified</i>.
     * @param edgeCostEvaluator
     *            Cost function for each edge in the path
     * @param heuristic
     *            Cost heuristic function to be applied between a intermediate
     *            and end node to determine the search order of A*
     * @param restrictedNodeIdentifiers
     *            Collection of nodes to not consider in routing
     * @param restrictedEdgeIdentifiers
     *            Collection of edges to not consider in routing
     * @return Optimal path from the start node to the end node
     * @throws SQLException
     *             if there is a database error
     */
    public Route aStar(final RoutingNetworkDescription                  routingNetwork,
                       final int                                        startNodeIdentifier,
                       final int                                        endNodeIdentifier,
                       final Collection<AttributeDescription>           nodeAttributes,
                       final Collection<AttributeDescription>           edgeAttributes,
                       final Function<EdgeEvaluationParameters, Double> edgeCostEvaluator,
                       final BiFunction<Node, Node, Double>             heuristic,
                       final Collection<Integer>                        restrictedNodeIdentifiers,
                       final Collection<Integer>                        restrictedEdgeIdentifiers) throws SQLException
    {
        if(routingNetwork == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(edgeCostEvaluator == null)
        {
            throw new IllegalArgumentException("Edge cost function may not be null");
        }

        if(heuristic == null)
        {
            throw new IllegalArgumentException("Heuristic function may not be null");
        }

        final Collection<Integer> ignoredEdges = new HashSet<>((restrictedEdgeIdentifiers == null) ?  Collections.emptySet() : restrictedEdgeIdentifiers);

        final Memoize2<Node, Node, Double> cachedHeuristic = new Memoize2<>(heuristic);

        // changed comparator -> change back to distance from end
        final PriorityQueue<AStarVertex> openList = new PriorityQueue<>((vertex1, vertex2) -> Double.compare((vertex1.getEstimatedCostToEnd() + vertex1.getCostFromStart()),
                                                                                                             (vertex2.getEstimatedCostToEnd() + vertex2.getCostFromStart())));

        final Collection<Integer> closedList = new HashSet<>((restrictedNodeIdentifiers == null) ? Collections.emptySet() : restrictedNodeIdentifiers);

        final Map<Integer, AStarVertex> nodeMap = new HashMap<>();

        final Node startNode = this.networkExtension.getNode(startNodeIdentifier, nodeAttributes);
        final Node endNode   = this.networkExtension.getNode(endNodeIdentifier,   nodeAttributes);

        // Starting Vertex
        final AStarVertex startVertex = new AStarVertex(startNode,
                                                        0.0,
                                                        cachedHeuristic.get(startNode, endNode));

        openList.add(startVertex);
        nodeMap.put(startNodeIdentifier, startVertex);

        while(!openList.isEmpty())
        {
            final AStarVertex currentVertex = openList.poll(); // Get the Vertex closest to the end

            // If current vertex is the target then we are done
            if(currentVertex.getNode().getIdentifier() == endNodeIdentifier)
            {
                return getAStarPath(endNodeIdentifier, nodeMap);
            }

            closedList.add(currentVertex.getNode().getIdentifier()); // Put it in "done" pile

            for(final Edge exit : this.networkExtension.getExits(routingNetwork.getNetwork(), currentVertex.getNode().getIdentifier())) // For each node adjacent to the current node
            {
                // Ignore restricted edges
                if(!ignoredEdges.contains(exit.getIdentifier()))
                {
                    final int adjacentNodeIdentifier = exit.getTo();

                    AStarVertex reachableVertex = nodeMap.get(adjacentNodeIdentifier);

                    if(reachableVertex == null)
                    {
                        reachableVertex = new AStarVertex(this.networkExtension.getNode(adjacentNodeIdentifier, nodeAttributes));
                        nodeMap.put(adjacentNodeIdentifier, reachableVertex);
                    }

                    // If the closed list already searched this vertex, skip it
                    if(!closedList.contains(reachableVertex.getNode().getIdentifier()))
                    {
                        final List<Object> edgeAttributeValues = edgeAttributes.isEmpty() ? Collections.emptyList()
                                                                                          : this.networkExtension.getEdgeAttributes(currentVertex.getNode().getIdentifier(),
                                                                                                                                    reachableVertex.getNode().getIdentifier(),
                                                                                                                                    edgeAttributes);

                        final double edgeCost = edgeCostEvaluator.apply(new EdgeEvaluationParameters(exit.getIdentifier(),
                                                                                                     edgeAttributeValues,
                                                                                                     currentVertex.getNode(),
                                                                                                     reachableVertex.getNode()));

                        if(edgeCost <= 0.0)    // Are positive values that are extremely close to 0 going to be a problem?
                        {
                            throw new RuntimeException("The A* algorithm is only valid for edge costs greater than 0");
                        }

                        final double costFromStart = currentVertex.getCostFromStart() + edgeCost;

                        final boolean isShorterPath = costFromStart < reachableVertex.getCostFromStart();

                        if(!openList.contains(reachableVertex) || isShorterPath)
                        {
                            reachableVertex.update(costFromStart,
                                                   cachedHeuristic.get(reachableVertex.getNode(), endNode), // Estimated cost to the end node
                                                   currentVertex,
                                                   edgeAttributeValues,
                                                   edgeCost);

                            if(isShorterPath)
                            {
                                openList.remove(reachableVertex);   // Re-add to trigger the reprioritization of this vertex
                            }

                            openList.add(reachableVertex);
                        }
                    }
                }
            }
        }

        return null;    // No path between the start and end nodes
    }

    private static Route getAStarPath(final Integer end, final Map<Integer, AStarVertex> nodeMap)
    {
        final LinkedList<List<Object>> nodesAttributes = new LinkedList<>();
        final LinkedList<List<Object>> edgesAttributes = new LinkedList<>();
        final LinkedList<Double>       edgeCost        = new LinkedList<>();

        for(AStarVertex vertex = nodeMap.get(end); vertex != null; vertex = vertex.getPrevious())
        {
            nodesAttributes.addLast(vertex.getNode().getAttributes());

            if(vertex.getPrevious() != null)
            {
                edgesAttributes.addFirst(vertex.getEdgeAttributes());
                edgeCost       .addFirst(vertex.getEdgeCost());
            }
        }

        return new Route(nodesAttributes,
                         edgesAttributes,
                         edgeCost);
    }

    private static String getRoutingNetworkDescriptionCreationSql()
    {
        return "CREATE TABLE " + RoutingNetworkDescriptionsTableName + '\n' +
               "(table_name          TEXT PRIMARY KEY NOT NULL, -- Name of network table\n"                 +
               " longitude_attribute TEXT NOT NULL,             -- Name of horizontal (x) node attribute\n" +
               " latitude_attribute  TEXT NOT NULL,             -- Name of vertical (y) node attribute\n"   +
               " elevation_attribute TEXT DEFAULT NULL,         -- Name of elevation (z) node attribute\n"   +
               " distance_attribute  TEXT NOT NULL,             -- Name of distance edge attribute\n"       +
               " CONSTRAINT fk_rntn_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    /**
     * Name of the singular table describing routing network tables
     */
    public static final String RoutingNetworkDescriptionsTableName = "routing_networks";

    private final GeoPackageNetworkExtension networkExtension;
}
