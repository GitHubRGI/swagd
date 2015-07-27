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

    public GeoPackageNetworkExtension getNetworkExtension()
    {
        return this.networkExtension;
    }

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
     * Returns edges that are within a circle
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
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment

//        if(routingNetwork == null)
//        {
//            throw new IllegalArgumentException("Routing network description may not be null");
//        }
//
//        if(visitor == null)
//        {
//            throw new IllegalArgumentException("The visitor callback may not be null");
//        }
//
//        final String networkTableName = routingNetwork.getNetwork().getTableName();
//
//        final String edgeQuery = String.format("SELECT %1$s, %2$s, %3$s " +
//                                               "FROM %4$s "+
//                                               "WHERE  EXISTS "+
//                                                      "(SELECT NULL "+
//                                                      "FROM %5$s "+
//                                                      "WHERE (node_id = %2$s OR node_id = %3$s) "+
//                                                      "AND (((longitude - %6$f)*(longitude-%6$f) + (latitude-%7$f)*(latitude-%7$f)) - (%8$f*%8$f)) <= (length*length))",  // TODO DON'T HARDCODE 'latitude' AND 'longitude' !!!
//                                               "id",             // %1$s
//                                               "from_node",      // %2$s
//                                               "to_node",        // %3$s
//                                               networkTableName, // %4$s
//                                               GeoPackageNetworkExtension.getNodeAttributesTableName(networkTableName), // %5$s
//                                               "node_id",
//                                               "",
//                                               centerX,          // %6$f
//                                               centerY,          // %7$f
//                                               radius);          // %8$s
//
//        JdbcUtility.forEach(this.databaseConnection,
//                            edgeQuery,
//                            null,
//                            resultSet -> visitor.accept(GeoPackageNetworkExtension.createEdge(resultSet.getInt(1),
//                                                                                              resultSet.getInt(2),
//                                                                                              resultSet.getInt(3))));
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
