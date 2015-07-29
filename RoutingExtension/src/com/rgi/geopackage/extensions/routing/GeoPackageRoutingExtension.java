package com.rgi.geopackage.extensions.routing;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.utility.DatabaseUtility;

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
        return ExtensionName;
    }

    @Override
    public String getDefinition()
    {
        return ExtensionDefinition;
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

        final String routingNetworkDescriptionQuery = String.format("SELECT %s, %s, %s FROM %s WHERE %s = ?;",
                                                                    "longitude_attribute",
                                                                    "latitude_attribute",
                                                                    "distance_attribute",
                                                                    RoutingNetworkDescriptionsTableName,
                                                                    "table_name");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     routingNetworkDescriptionQuery,
                                     preparedStatement -> preparedStatement.setString(1, networkTableName),
                                     resultSet -> { final Network network = this.networkExtension.getNetwork(networkTableName);

                                                    final AttributeDescription longitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(1), AttributedType.Node);
                                                    final AttributeDescription  latitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(2), AttributedType.Node);
                                                    final AttributeDescription  distanceDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(3), AttributedType.Edge);

                                                    return new RoutingNetworkDescription(network,
                                                                                         longitudeDescription,
                                                                                         latitudeDescription,
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

        final String routingNetworkDescriptionQuery = String.format("SELECT %s, %s, %s, %s FROM %s;",
                                                                    "table_name",
                                                                    "longitude_attribute",
                                                                    "latitude_attribute",
                                                                    "distance_attribute",
                                                                    RoutingNetworkDescriptionsTableName);

        return JdbcUtility.select(this.databaseConnection,
                                  routingNetworkDescriptionQuery,
                                  null,
                                  resultSet -> { final Network network = this.networkExtension.getNetwork(resultSet.getString(1));

                                                 final AttributeDescription longitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(2), AttributedType.Node);
                                                 final AttributeDescription  latitudeDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(3), AttributedType.Node);
                                                 final AttributeDescription  distanceDescription = this.networkExtension.getAttributeDescription(network, resultSet.getString(4), AttributedType.Edge);

                                                 return new RoutingNetworkDescription(network,
                                                                                      longitudeDescription,
                                                                                      latitudeDescription,
                                                                                      distanceDescription);
                                               });
    }

    public RoutingNetworkDescription addRoutingNetworkDescription(final Network              network,
                                                                  final AttributeDescription longitudeDescription,
                                                                  final AttributeDescription latitudeDescription,
                                                                  final AttributeDescription distanceDescription) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(longitudeDescription == null)
        {
            throw new IllegalArgumentException("Longitude description may not be null");
        }

        if(latitudeDescription == null)
        {
            throw new IllegalArgumentException("Latitude description may not be null");
        }

        if(distanceDescription == null)
        {
            throw new IllegalArgumentException("Distance description may not be null");
        }

        try
        {
            if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, RoutingNetworkDescriptionsTableName))
            {
                JdbcUtility.update(this.databaseConnection, this.getRoutingNetworkDescriptionCreationSql());
            }


            JdbcUtility.update(this.databaseConnection,
                               String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
                                             GeoPackageRoutingExtension.RoutingNetworkDescriptionsTableName,
                                             "table_name",
                                             "longitude_attribute",
                                             "latitude_attribute",
                                             "distance_attribute"),
                               preparedStatement -> { preparedStatement.setString(1, network.getTableName());
                                                      preparedStatement.setString(2, longitudeDescription.getName());
                                                      preparedStatement.setString(3, latitudeDescription. getName());
                                                      preparedStatement.setString(4, distanceDescription. getName());
                                                    });

            this.databaseConnection.commit();

            final RoutingNetworkDescription routingNetwork = new RoutingNetworkDescription(network,
                                                                                           longitudeDescription,
                                                                                           latitudeDescription,
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
     * @param visitor
     *             Callback applied to each edge
     * @throws SQLException
     *             if there is a database error
     */
    public void visitEdgesCloseToCircle(final RoutingNetworkDescription routingNetwork,
                                        final double                    centerX,
                                        final double                    centerY,
                                        final double                    radius,
                                        final Consumer<Edge>            visitor) throws SQLException
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
                            resultSet -> visitor.accept(GeoPackageNetworkExtension.createEdge(resultSet.getInt(1),
                                                                                              resultSet.getInt(2),
                                                                                              resultSet.getInt(3))));
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
                                   final Consumer<Edge>            visitor) throws SQLException
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

        final String edgeQuery = String.format("SELECT id, from_node, to_node\n" +
                                               // Compute "param"
                                               "FROM (SELECT id, from_node, to_node, x1, y1, x2, y2, c, d,\n" +
                                                            // "switch" to determine the value of "param"
                                                            "CASE WHEN x1 = x2 AND y1 = y2\n" +              // points 1 and 2 are the same, so the distance/length between them is 0. don't divide by 0.
                                                                 "THEN -1\n" +
                                                                 "ELSE (a * c + b * d) / (c * c + d * d)\n" + // dot / length squared
                                                                 "END AS param\n" +
                                                     // Compute a, b, c, d and pass along other properties
                                                     "FROM (SELECT id, from_node, to_node, x1, y1, x2, y2,\n" +
                                                                  "(%1$f - x1) AS a,\n" +
                                                                  "(%2$f - y1) AS b,\n" +
                                                                  "(  x2 - x1) AS c,\n" +
                                                                  "(  y2 - y1) AS d\n" +
                                                           // Select the edge (id, and from/to nodes) as well as the x/y of the from/to nodes
                                                           "FROM (SELECT id, from_node, to_node,\n" +
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
                            resultSet -> visitor.accept(GeoPackageNetworkExtension.createEdge(resultSet.getInt(1),
                                                                                              resultSet.getInt(2),
                                                                                              resultSet.getInt(3))));
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
    public List<Integer> getNodesInBoundingBox(final RoutingNetworkDescription routingNetwork,
                                               final double                    minimumX,
                                               final double                    minimumY,
                                               final double                    maximumX,
                                               final double                    maximumY) throws SQLException
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

    @SuppressWarnings("static-method")
    protected String getRoutingNetworkDescriptionCreationSql()
    {
        return "CREATE TABLE " + RoutingNetworkDescriptionsTableName + "\n" +
               "(table_name          TEXT PRIMARY KEY NOT NULL, -- Name of network table\n"                 +
               " longitude_attribute TEXT NOT NULL,             -- Name of horizontal (x) node attribute\n" +
               " latitude_attribute  TEXT NOT NULL,             -- Name of vertical (y) node attribute\n"   +
               " distance_attribute  TEXT NOT NULL,             -- Name of distance edge attribute\n"       +
               " CONSTRAINT fk_rntn_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    /**
     * Name of the singular table describing routing network tables
     */
    public static final String RoutingNetworkDescriptionsTableName = "routing_networks";

    protected final GeoPackageNetworkExtension networkExtension;

    private static final String ExtensionName       = "rgi_routing";
    private static final String ExtensionDefinition = "definition"; // TODO
}
