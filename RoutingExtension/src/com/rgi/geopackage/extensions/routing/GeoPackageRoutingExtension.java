package com.rgi.geopackage.extensions.routing;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.rgi.common.Pair;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.Edge;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.utility.DatabaseUtility;

/**
 * Implementation of the RGI Network GeoPackage extension
 *
 * @author Luke.Lambert
 *
 */
public class GeoPackageRoutingExtension extends GeoPackageNetworkExtension
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
     */
    public GeoPackageRoutingExtension(final Connection           databaseConnection,
                                      final GeoPackageCore       geoPackageCore,
                                      final GeoPackageExtensions geoPackageExtensions) throws SQLException
    {
        super(databaseConnection, geoPackageCore, geoPackageExtensions);
    }

    public List<RoutingNetworkDescription> getRoutingNetworks() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, RoutingNetworkDescriptionsTableName))
        {
            return Collections.emptyList();
        }

        final String routingNetworkDescriptionQuery = String.format("SELECT %s, %s, %s FROM %s;",
                                                                    "table_name",
                                                                    "longitude_attribute",
                                                                    "latitude_attribute",
                                                                    RoutingNetworkDescriptionsTableName);

        return JdbcUtility.select(this.databaseConnection,
                                  routingNetworkDescriptionQuery,
                                  null,
                                  resultSet -> { final Network network = this.getNetwork(resultSet.getString(1));

                                                 final AttributeDescription longitudeDescription = this.getAttributeDescription(network, resultSet.getString(2), AttributedType.Node);
                                                 final AttributeDescription latitudeDescription =  this.getAttributeDescription(network, resultSet.getString(3), AttributedType.Node);

                                                 return new RoutingNetworkDescription(network,
                                                                                      longitudeDescription,
                                                                                      latitudeDescription);
                                               });
    }

    /**
     * Returns the node identifier of the closest node to a point
     *
     * @param xDescription
     *             Attribute description of the horizontal component of a
     *             coordinate
     * @param x
     *             Horizontal component of a coordinate
     * @param yDescription
     *             Attribute description of the vertical component of a
     *             coordinate
     * @param y
     *             Vertical component of a coordinate
     * @return Node identifier of the closest node to a point
     * @throws SQLException
     *             if there is a database error
     */
    public Integer getClosestNode(final AttributeDescription xDescription,
                                  final double               x,
                                  final AttributeDescription yDescription,
                                  final double               y) throws SQLException
    {
        final Pair<String, List<String>> schema = getSchema(AttributedType.Node, xDescription, yDescription);

        final String distanceQuery = String.format("SELECT %s, MIN(((%2$s - %3$f) * (%2$s - %3$f)) + ((%4$s - %5$s) * (%4$s - %5$s))) as distSqrd FROM %6$s;",
                                                   "node_id",
                                                   xDescription.getName(),
                                                   x,
                                                   yDescription.getName(),
                                                   y,
                                                   getNodeAttributesTableName(schema.getLeft()));

        return JdbcUtility.selectOne(this.databaseConnection,
                                     distanceQuery,
                                     null,
                                     resultSet -> resultSet.getInt(1));
    }

    /**
     * Returns edges that are reachable to the circle bounds
     *
     * @param network
     *            Network table reference
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
    public void visitEdgesInCircle(final Network        network,
                                   final double         centerX,
                                   final double         centerY,
                                   final double         radius,
                                   final Consumer<Edge> visitor) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("The network may not be null");
        }

        if(visitor == null)
        {
            throw new IllegalArgumentException("The visitor callback may not be null");
        }

        final String edgeQuery = String.format("SELECT %1$s, %2$s, %3$s " +
                                               "FROM %4$s "+
                                               "WHERE  EXISTS "+
                                                      "(SELECT NULL "+
                                                      "FROM %5$s "+
                                                      "WHERE (node_id = %2$s OR node_id = %3$s) "+
                                                      "AND (((longitude - %6$f)*(longitude-%6$s) + (latitude-%7$s)*(latitude-%7$s)) - %8$s) <= (length*length))",  // TODO DON'T HARDCODE 'latitude' AND 'longitude' !!!
                                               "id",
                                               "from_node",
                                               "to_node",
                                               network.getTableName(),
                                               getNodeAttributesTableName(network),
                                               centerX,
                                               centerY,
                                               radius);

        JdbcUtility.forEach(this.databaseConnection,
                            edgeQuery,
                            null,
                            resultSet -> visitor.accept(createEdge(resultSet.getInt(1),
                                                                   resultSet.getInt(2),
                                                                   resultSet.getInt(3))));
    }


    /**
     * Returns a list of node identifier that lie in a rectangle boundary given
     *
     * @param minimumX
     *           minimum x value in rectangle
     * @param minimumY
     *           minimum y value in rectangle
     * @param maximumX
     *           maximum x value in rectangle
     * @param maximumY
     *           maximum y value in rectangle
     * @param attributeDescriptionX
     *            Horizontal component of a coordinate
     * @param attributeDescriptionY
     *            Vertical component of a coordinate
     * @return a list of node identifier in the contained in the rectangle region
     * @throws SQLException
     *             if there is a database error
     */
    public List<Integer> getNodesInRectangleRange(final double               minimumX,
                                                  final double               minimumY,
                                                  final double               maximumX,
                                                  final double               maximumY,
                                                  final AttributeDescription attributeDescriptionX,
                                                  final AttributeDescription attributeDescriptionY) throws SQLException
    {
        final Pair<String, List<String>> schema = getSchema(AttributedType.Node, attributeDescriptionX, attributeDescriptionY);

        final String nodeQuery = String.format("SELECT %1$s, %2$s "+
                                               "FROM %3$s "+
                                               "WHERE %1$s <= %6$s AND %1$s >= %4$s AND %2$s <= %7$s AND %2$s >= %5$s",
                                               attributeDescriptionX.getName(),
                                               attributeDescriptionY.getName(),
                                               getNodeAttributesTableName(schema.getLeft()),
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
               "(table_name          TEXT PRIMARY KEY NOT NULL, -- Name of network table\n"   +
               " latitude_attribute  TEXT NOT NULL,             -- Name of attribute\n"       +
               " longitude_attribute TEXT NOT NULL,             -- Attribute value's units\n" +
               " CONSTRAINT fk_rntn_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    /**
     * Name of the singular table describing routing network tables
     */
    public static final String RoutingNetworkDescriptionsTableName = "routing_networks";
}
