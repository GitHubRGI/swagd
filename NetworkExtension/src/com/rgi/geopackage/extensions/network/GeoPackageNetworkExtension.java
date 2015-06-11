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

package com.rgi.geopackage.extensions.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.extensions.implementation.ExtensionImplementation;
import com.rgi.geopackage.extensions.implementation.ImplementsExtension;

/**
 * Implementation of the RGI Network GeoPackage extension
 *
 * @author Luke Lambert
 *
 */
@ImplementsExtension(name = "rgi_network")
public class GeoPackageNetworkExtension extends ExtensionImplementation
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
    public GeoPackageNetworkExtension(final Connection           databaseConnection,
                                      final GeoPackageCore       geoPackageCore,
                                      final GeoPackageExtensions geoPackageExtensions) throws SQLException
    {
        super(databaseConnection, geoPackageCore, geoPackageExtensions);
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
     * Gets the number of edges in the given network
     *
     * @param network
     *             Network table reference
     * @return the number of edges in the network
     * @throws SQLException
     *             if there is a database error
     *
     */
    public int getEdgeCount(final Network network) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("The given network is null");
        }

        final String query = String.format("SELECT COUNT(*) FROM %s;",
                                           network.getTableName());

        return JdbcUtility.selectOne(this.databaseConnection,
                                     query,
                                     null,
                                     results -> results.getInt(1));
    }

    /**
     * Gets the number of nodes in the given Network
     *
     * @param network
     *             Network table reference
     * @return the number of nodes in the network
     * @throws SQLException
     *             if there is a database error
     */
    public int getNodeCount(final Network network) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("The given network is null");
        }

        final String query = String.format("Select COUNT(*) from(select from_node from %s UNION select to_node from %s);",
                                           network.getTableName(),
                                           network.getTableName());

        return JdbcUtility.selectOne(this.databaseConnection,
                                     query,
                                     null,
                                     results -> results.getInt(1));
    }

    /**
     * @param network
     *             Network table reference
     * @return the name of the unique corresponding network attribute table
     */
    public static String getNodeAttributesTableName(final Network network)
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        return network.getTableName() + NodeAttributesTableSuffix;
    }

    /**
     * @param networkTableName
     *             Network table name
     * @return the name of the unique corresponding network attribute table
     */
    public static String getNodeAttributesTableName(final String networkTableName)
    {
        if(networkTableName == null || networkTableName.isEmpty())
        {
            throw new IllegalArgumentException("Network may not be null or empty");
        }

        return networkTableName + NodeAttributesTableSuffix;
    }

    /**
     * Gets a network object based on its table name
     *
     * @param networkTableName
     *             Name of a network set table
     * @return Returns a {@link Network} or null if there isn't with the
     *             supplied table name
     * @throws SQLException
     *             throws if the method
     *             {@link GeoPackageCore#getContent(String, com.rgi.geopackage.core.ContentFactory, SpatialReferenceSystem)}
     *             throws an SQLException
     */
    public Network getNetwork(final String networkTableName) throws SQLException
    {
        return this.geoPackageCore.getContent(networkTableName,
                                              (tableName,
                                               dataType,
                                               identifier,
                                               description,
                                               lastChange,
                                               boundingBox,
                                               spatialReferenceSystem) -> new Network(tableName,
                                                                                      identifier,
                                                                                      description,
                                                                                      lastChange,
                                                                                      boundingBox,
                                                                                      spatialReferenceSystem));
    }

    /**
     * Creates a user defined network table, and adds a corresponding entry to the
     * content table
     *
     * @param tableName
     *            The name of the network table. The table name must begin with a
     *            letter (A..Z, a..z) or an underscore (_) and may only be
     *            followed by letters, underscores, or numbers, and may not
     *            begin with the prefix "gpkg_"
     * @param identifier
     *            A human-readable identifier (e.g. short name) for the
     *            tableName content
     * @param description
     *            A human-readable description for the tableName content
     * @param boundingBox
     *            Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *            Spatial Reference System (SRS)
     * @return Returns a newly created user defined network table
     * @throws SQLException
     *             throws if the method {@link #getNetwork(String) getNetwork}
     *             or the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)
     *             tableOrViewExists} or if the database cannot roll back the
     *             changes after a different exception throws will throw an SQLException
     *
     */
    public Network addNetwork(final String                 tableName,
                              final String                 identifier,
                              final String                 description,
                              final BoundingBox            boundingBox,
                              final SpatialReferenceSystem spatialReferenceSystem) throws SQLException
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Network set name may not be null");
        }

        if(!tableName.matches("^[_a-zA-Z]\\w*"))
        {
            throw new IllegalArgumentException("The network set's table name must begin with a letter (A..Z, a..z) or an underscore (_) and may only be followed by letters, underscores, or numbers");
        }

        if(tableName.startsWith("gpkg_"))
        {
            throw new IllegalArgumentException("The network set's name may not start with the reserved prefix 'gpkg_'");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be mull.");
        }

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
            throw new IllegalArgumentException("A table already exists with this network's table name");
        }

        final String nodeAttributesTableName = getNodeAttributesTableName(tableName);

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, nodeAttributesTableName))
        {
            throw new IllegalArgumentException("A table already exists with this node attribute's table name");
        }

        try
        {
            if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, AttributeDescriptionTableName))
            {
                JdbcUtility.update(this.databaseConnection, this.getAttributeDescriptionCreationSql());
            }

            // Create the network table
            JdbcUtility.update(this.databaseConnection, this.getNetworkCreationSql(tableName));

            // Create the network's attributes table
            JdbcUtility.update(this.databaseConnection, this.getNodeAttributeTableCreationSql(nodeAttributesTableName));

            // Add the network to the content table
            this.geoPackageCore.addContent(tableName,
                                           Network.NetworkContentType,
                                           identifier,
                                           description,
                                           boundingBox,
                                           spatialReferenceSystem);

            this.databaseConnection.commit();

            final Network network = this.getNetwork(tableName);

            this.addExtensionEntry();

            return network;
        }
        catch(final Throwable th)
        {
            this.databaseConnection.rollback();
            throw th;
        }
    }

    /**
     * Get an edge based on its identifier
     *
     * @param network
     *             Network table reference
     * @param from
     *             The 'from' node of the edge
     * @param to
     *             The 'to' node of the edge
     * @return an {@link Edge} entry in the supplied {@link Network}, or null
     *             if no matching edge exists
     * @throws SQLException
     *             if there is a database error
     */
    public Edge getEdge(final Network network, final int from, final int to) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String edgeQuery = String.format("Select %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1;",
                                               "id",
                                               network.getTableName(),
                                               "from_node",
                                               "to_node");

        final Edge edge = JdbcUtility.selectOne(this.databaseConnection,
                                          edgeQuery,
                                          preparedStatement -> { preparedStatement.setInt(1, from);
                                                                preparedStatement.setInt(2, to);
                                                               },
                                          results -> new Edge(results.getInt(1), // identifier
                                                              from,              // attribute name
                                                              to));              // attributed type

        if(edge == null)
        {
            throw new IllegalArgumentException("The given edge is not in the given network");
        }

        return edge;
    }

    /**
     * Get the 'from' nodes that share an edge with a node
     *
     * @param network
     *             Network table reference
     * @param node
     *             'to' node identifier
     * @return a {@link List} of node identifiers
     * @throws SQLException
     *             if there is a database error
     */
    public List<Edge> getEntries(final Network network, final int node) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String edgeQuery = String.format("SELECT %s, %s FROM %s WHERE %s = ?;",
                                               "id",
                                               "from_node",
                                               network.getTableName(),
                                               "to_node");

        return JdbcUtility.select(this.databaseConnection,
                                  edgeQuery,
                                  preparedStatement -> preparedStatement.setInt(1, node),
                                  resultSet -> new Edge(resultSet.getInt(1),
                                                        resultSet.getInt(2),
                                                        node));
    }

    /**
     * Get the 'to' nodes that share an edge with a node
     *
     * @param network
     *             Network table reference
     * @param node
     *             'from' node identifier
     * @return a {@link List} of node identifiers
     * @throws SQLException
     *             if there is a database error
     */
    public List<Edge> getExits(final Network network, final int node) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String edgeQuery = String.format("SELECT %s, %s FROM %s WHERE %s = ?;",
                                               "id",
                                               "to_node",
                                               network.getTableName(),
                                               "from_node");

        return JdbcUtility.select(this.databaseConnection,
                                  edgeQuery,
                                  preparedStatement -> preparedStatement.setInt(1, node),
                                  resultSet -> new Edge(resultSet.getInt(1),
                                                        node,
                                                        resultSet.getInt(2)));
    }

    /**
     * Iterate through the edges of a {@link Network}, applying a supplied
     * operation
     *
     * @param network
     *             Network table reference
     * @param consumer
     *             Callback applied to each edge
     * @throws SQLException
     *             if there is a database error
     */
    public void visitEdges(final Network network, final Consumer<Edge> consumer) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(consumer == null)
        {
            throw new IllegalArgumentException("Consumer callback may not be null");
        }

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s FROM %s;",
                                                               "id",
                                                               "from_node",
                                                               "to_node",
                                                               network.getTableName());

        JdbcUtility.forEach(this.databaseConnection,
                            attributeDescriptionQuery,
                            null,
                            resultSet -> consumer.accept(new Edge(resultSet.getInt(1),
                                                                  resultSet.getInt(2),
                                                                  resultSet.getInt(3))));
    }

    /**
     * Adds an edge to a {@link Network}
     *
     * @param network
     *             Network table reference
     * @param from
     *             'from' node
     * @param to
     *             'to' node
     * @return an {@link Edge} reference to the added edge
     * @throws SQLException
     *             if there is a database error
     */
    public Edge addEdge(final Network network, final int from, final int to) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String insert = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                                            network.getTableName(),
                                            "from_node",
                                            "to_node");

        final int identifier = JdbcUtility.update(this.databaseConnection,
                                                  insert,
                                                  preparedStatement -> { preparedStatement.setInt(1, from);
                                                                         preparedStatement.setInt(2, to);
                                                                       },
                                                  resultSet -> resultSet.getInt(1));

        this.databaseConnection.commit();

        return new Edge(identifier, from, to);
    }

    /**
     * Adds edges to a {@link Network}
     *
     * @param network
     *             Network table reference
     * @param edges
     *             Collection of from/to node pairs
     * @throws SQLException
     *             if there is a database error
     */
    public void addEdges(final Network network, final Iterable<Pair<Integer, Integer>> edges) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(edges == null)
        {
            throw new IllegalArgumentException("Edge collection may not be null");
        }

        final String insert = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                                            network.getTableName(),
                                            "from_node",
                                            "to_node");

        JdbcUtility.update(this.databaseConnection,
                           insert,
                           edges,
                           (preparedStatement, edge) -> { preparedStatement.setInt(1, edge.getLeft());
                                                          preparedStatement.setInt(2, edge.getRight());
                                                        });

        this.databaseConnection.commit();
    }

    /**
     * Adds edges to a {@link Network} along with each edge's attributes
     * @param attributedEdges
     *             Collection of edge/attribute pairs, where the edges are each a pair of nodes
     * @param attributeDescriptions
     *             Collection of {@link AttributeDescription}s
     *
     * @throws SQLException
     *             if there is a database error
     */
    public void addAttributedEdges(final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges,
                                   final AttributeDescription...                          attributeDescriptions) throws SQLException
    {
        if(attributedEdges == null || attributedEdges.isEmpty())
        {
            throw new IllegalArgumentException("Attributed edges collection may not be null or empty");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Edge, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        final String insert = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, %s)",
                                            networkTableName,
                                            "from_node",
                                            "to_node",
                                            String.join(", ", columnNames),
                                            String.join(", ", Collections.nCopies(attributeDescriptions.length, "?")));

        JdbcUtility.update(this.databaseConnection,
                           insert,
                           attributedEdges,
                           (preparedStatement, attributedEdge) -> { final Pair<Integer, Integer> edge   = attributedEdge.getLeft();
                                                                    final List<Object>           values = attributedEdge.getRight();

                                                                    if(values.size() != attributeDescriptions.length)
                                                                    {
                                                                        throw new IllegalArgumentException(String.format("Edge (%d -> %d) has %d values; expected %d",
                                                                                                                         edge.getLeft(),
                                                                                                                         edge.getRight(),
                                                                                                                         values.size(),
                                                                                                                         attributeDescriptions.length));
                                                                    }

                                                                    int parameterIndex = 1;

                                                                    preparedStatement.setInt(parameterIndex++, edge.getLeft());
                                                                    preparedStatement.setInt(parameterIndex++, edge.getRight());

                                                                    for(final Object value : values)
                                                                    {
                                                                        preparedStatement.setObject(parameterIndex++, value);
                                                                    }
                                                                  });

        this.databaseConnection.commit();
    }

    /**
     * Adds edges to a {@link Network} along with each edge's attributes
     *
     * @param edge
     *             Edge reference
     * @param values
     *             Values for each of the supplied attribute descriptions
     * @param attributeDescriptions
     *             Specification of which attributes will be set
     * @throws SQLException
     *             if there is a database error
     */
    public void updateEdgeAttributes(final Edge                    edge,
                                     final List<Object>            values,
                                     final AttributeDescription... attributeDescriptions) throws SQLException
    {
        if(edge == null)
        {
            throw new IllegalArgumentException("Edge may not be null");
        }

        if(values == null)
        {
            throw new IllegalArgumentException("Values collection may not be null");
        }

        if(values.size() != attributeDescriptions.length)
        {
            throw new IllegalArgumentException("Values collection and AttributeDescription list must have the same length");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Edge, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        final String update = String.format("UPDATE %s SET %s WHERE %s = ?",
                                            networkTableName,
                                            String.join(", ",
                                                        columnNames.stream()
                                                                   .map(name -> name + " = ?")
                                                                   .collect(Collectors.toList())),
                                            "id");

        JdbcUtility.update(this.databaseConnection,
                           update,
                           preparedStatement -> { int parameterIndex = 1;

                                                  for(final Object value : values)
                                                  {
                                                      preparedStatement.setObject(parameterIndex++, value);
                                                  }

                                                  preparedStatement.setInt(parameterIndex, edge.getIdentifier());
                                                });

        this.databaseConnection.commit();
    }


    /**
     * Get's a list of a {@link Network}'s {@link AttributeDescription}s for
     * either its nodes or edges
     *
     * @param network
     *             Network table reference
     * @param attributedType
     *             Indicates whether you want the {@link AttributeDescription}s for an node or a edge
     * @return a collection of {@link AttributeDescription}s
     * @throws SQLException
     *             if there is a database error
     */
    public List<AttributeDescription> getAttributeDescriptions(final Network        network,
                                                               final AttributedType attributedType) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ? AND %s = ?;",
                                                               "id",
                                                               "name",
                                                               "units",
                                                               "data_type",
                                                               "description",
                                                               AttributeDescriptionTableName,
                                                               "table_name",
                                                               "attributed_type");

        return JdbcUtility.select(this.databaseConnection,
                                  attributeDescriptionQuery,
                                  preparedStatement -> { preparedStatement.setString(1, network.getTableName());
                                                         preparedStatement.setString(2, attributedType.toString());
                                                       },
                                  resultSet -> new AttributeDescription(resultSet.getInt(1),                      // attribute unique identifier
                                                                          network.getTableName(),                   // network table name
                                                                          resultSet.getString(2),                   // attribute name
                                                                          resultSet.getString(3),                   // attribute units
                                                                          DataType.valueOf(resultSet.getString(4)), // attribute data type
                                                                          resultSet.getString(5),                   // attribute description
                                                                          attributedType));                          // attributed type
    }

    /**
     * Get's a {@link Network}'s named {@link AttributeDescription}
     *
     * @param network
     *             Network table reference
     * @param name
     *             Name of the attribute
     * @param attributedType
     *             Indicates whether you want the {@link AttributeDescription}s for an node or a edge
     * @return an {@link AttributeDescription}, or null if none match the
     *             supplied criteria
     * @throws SQLException
     *             if there is a database error
     */
    public AttributeDescription getAttributeDescription(final Network        network,
                                                        final String         name,
                                                        final AttributedType attributedType) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(name == null)
        {
            throw new IllegalArgumentException("Attribute name may not be null");
        }

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = ? AND %s = ? AND %s = ? LIMIT 1;",
                                                               "id",
                                                               "units",
                                                               "data_type",
                                                               "description",
                                                               AttributeDescriptionTableName,
                                                               "table_name",
                                                               "attributed_type",
                                                               "name");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     attributeDescriptionQuery,
                                     preparedStatement -> { preparedStatement.setString(1, network.getTableName());
                                                            preparedStatement.setString(2, attributedType.toString());
                                                            preparedStatement.setString(3, name);
                                                          },
                                     resultSet -> new AttributeDescription(resultSet.getInt(1),                      // attribute unique identifier
                                                                           network.getTableName(),                   // network table name
                                                                           name,                                     // attribute name
                                                                           resultSet.getString(2),                   // attribute units
                                                                           DataType.valueOf(resultSet.getString(3)), // attribute data type
                                                                           resultSet.getString(4),                   // attribute description
                                                                           attributedType));                         // attributed type
    }

    /**
     * Adds an attribute description to a {@link Network} for a node or edge
     *
     * @param network
     *             Network table reference
     * @param name
     *             Name of the attribute. The combination of network, name, and
     *             attributed type are unique in a {@link GeoPackage}.
     * @param units
     *             Description of the attribute's value unit
     * @param dataType
     *             Database storage type for the attribute's value
     * @param description
     *             Human readable description of the attribute
     * @param attributedType
     *             Indication of whether this is a description for nodes or
     *             edges. The combination of network, name, and attributed
     *             type are unique in a {@link GeoPackage}.
     * @return handle to the added {@link AttributeDescription}
     * @throws SQLException
     *             if there is a database error
     */
    public AttributeDescription addAttributeDescription(final Network        network,
                                                        final String         name,
                                                        final String         units,
                                                        final DataType       dataType,
                                                        final String         description,
                                                        final AttributedType attributedType) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("Name may not be null or empty");
        }

        if(units == null || units.isEmpty())
        {
            throw new IllegalArgumentException("Units may not be null or empty");
        }

        if(dataType == null)
        {
            throw new IllegalArgumentException("Data type may not be null");
        }

        if(description == null || description.isEmpty())
        {
            throw new IllegalArgumentException("Description may not be null or empty");
        }

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        final String tableName = attributedType == AttributedType.Edge ? network.getTableName()
                                                                       : getNodeAttributesTableName(network);

        final String insert = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                                            AttributeDescriptionTableName,
                                            "table_name",
                                            "name",
                                            "units",
                                            "data_type",
                                            "description",
                                            "attributed_type");

        final int attributeDescriptionIdentifier = JdbcUtility.update(this.databaseConnection,
                                                                      insert,
                                                                      preparedStatement -> { preparedStatement.setString(1, network.getTableName());
                                                                                             preparedStatement.setString(2, name);
                                                                                             preparedStatement.setString(3, units);
                                                                                             preparedStatement.setString(4, dataType.toString());
                                                                                             preparedStatement.setString(5, description);
                                                                                             preparedStatement.setString(6, attributedType.toString());
                                                                                           },
                                                                      keySet -> keySet.getInt(1));

        final String alter = String.format("ALTER TABLE %s ADD COLUMN %s %s DEFAULT NULL;",
                                           tableName,
                                           name,
                                           dataType.toString().toUpperCase(Locale.getDefault()));

        JdbcUtility.update(this.databaseConnection, alter);

        this.databaseConnection.commit();

        return new AttributeDescription(attributeDescriptionIdentifier,
                                        network.getTableName(),
                                        name,
                                        units,
                                        dataType,
                                        description,
                                        attributedType);
    }

    /**
     * Gets an attribute value
     *
     * @param edge
     *             Reference to an edge in a network table
     * @param attributeDescription
     *             Handle to which attribute should be retrieved
     * @return the attribute's value stored for edge or null if the edge does
     *             not have that attribute
     * @throws SQLException
     *             if there is a database error
     */
    public <T> T getEdgeAttribute(final Edge                 edge,
                                  final AttributeDescription attributeDescription) throws SQLException
    {
        if(edge == null)
        {
            throw new IllegalArgumentException("Edge may not be null");
        }

        if(attributeDescription == null)
        {
            throw new IllegalArgumentException("Attribute description may not be null");
        }

        if(attributeDescription.getAttributedType() == AttributedType.Node)
        {
            throw new IllegalArgumentException("Attribute description must be for an edge");
        }

        final String attributeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1;",
                                                    attributeDescription.getName(),
                                                    attributeDescription.getNetworkTableName(),
                                                    "id");

        return JdbcUtility.selectOne(this.databaseConnection,
                                     attributeQuery,
                                     preparedStatement -> preparedStatement.setInt(1, edge.getIdentifier()),
                                     resultSet -> { if(resultSet.getObject(1) == null)
                                                    {
                                                        return null;
                                                    }

                                                    @SuppressWarnings("unchecked")
                                                    final T value = (T)resultSet.getObject(1); // This may throw a ClassCastException if the stored type cannot be converted to the requested type

                                                    if(!attributeDescription.dataTypeAgrees(value))
                                                    {
                                                        throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");   // Throw if the requested type doesn't match the
                                                    }

                                                    return value;
                                                 });
    }

    /**
     * Get multiple attribute values from an edge
     *
     * @param edge
     *             Reference to an edge in a network table
     * @param attributeDescriptions
     *             Collection of which attributes should be retrieved
     * @return the edge's attribute values in the same order as the specified attribute descriptions
     * @throws SQLException
     *             if there is a database error
     */
    public List<Object> getEdgeAttributes(final Edge                    edge,
                                          final AttributeDescription... attributeDescriptions) throws SQLException
    {
        if(edge == null)
        {
            throw new IllegalArgumentException("Edge may not be null");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Edge, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        final String attributeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1",
                                                    String.join(", ", columnNames),
                                                    networkTableName,
                                                    "id");

        final List<Object> attributes =  JdbcUtility.selectOne(this.databaseConnection,
                                                               attributeQuery,
                                                               preparedStatement -> preparedStatement.setInt(1, edge.getIdentifier()),
                                                               resultSet -> JdbcUtility.getObjects(resultSet, 1, attributeDescriptions.length));
        if(attributes == null)
        {
            throw new IllegalArgumentException("The given edge is not in the table");
        }

        return attributes;
    }

    /**
     * Get multiple attribute values from an edge
     *
     * @param nodeIdentifier
     *             Unique node identifier
     * @param attributeDescriptions
     *             Collection of which attributes should be retrieved
     * @return the node's attribute values in the same order as the specified attribute descriptions
     * @throws SQLException
     *             if there is a database error
     */
    public List<Object> getNodeAttributes(final int                     nodeIdentifier,
                                          final AttributeDescription... attributeDescriptions) throws SQLException
    {
        return this.getNodeAttributes(Arrays.asList(nodeIdentifier), attributeDescriptions).get(0);
    }

    /**
     * Get multiple attribute values from multiple nodes
     *
     * @param nodeIdentifiers
     *             Collection of unique node identifier
     * @param attributeDescriptions
     *             Collection of which attributes should be retrieved
     * @return the node's attribute values in the same order as the specified attribute descriptions
     * @throws SQLException
     *             if there is a database error
     */
    public List<List<Object>> getNodeAttributes(final List<Integer>           nodeIdentifiers,
                                                final AttributeDescription... attributeDescriptions) throws SQLException
    {
        if(nodeIdentifiers == null || nodeIdentifiers.isEmpty())
        {
            throw new IllegalArgumentException("NodeIdentifiers list may not be null or empty");
        }

        if(attributeDescriptions == null || attributeDescriptions.length == 0)
        {
            throw new IllegalArgumentException("Attribute descriptions may not be null or empty");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Node, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        final String attributeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1;",
                                                    String.join(", ", columnNames),
                                                    getNodeAttributesTableName(networkTableName),
                                                    "node_id");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeQuery))
        {
            final List<List<Object>> valueCollections = new ArrayList<>(nodeIdentifiers.size());

            for(final int nodeIdentifier : nodeIdentifiers)
            {
                preparedStatement.setInt(1, nodeIdentifier);

                try(final ResultSet resultSet = preparedStatement.executeQuery())
                {
                    if(!resultSet.isBeforeFirst())
                    {
                        throw new IllegalArgumentException("Node does not belong to the network table specified by the supplied attributes");
                    }

                    valueCollections.add(JdbcUtility.getObjects(resultSet, 1, attributeDescriptions.length));
                }
            }

            return valueCollections;
        }
    }

    /**
     * Adds attributes to a node
     *
     * @param nodeIdentifier
     *             Unique node identifier
     * @param attributeDescriptions
     *             Collection of which attributes should be set
     * @param values
     *             Values of the attributes in corresponding order to the given
     *             attribute descriptions
     * @throws SQLException
     *             if there is a database error
     */
    public void addNodeAttributes(final int                     nodeIdentifier,
                                  final List<Object>            values,
                                  final AttributeDescription... attributeDescriptions) throws SQLException
    {
        if(values == null)
        {
            throw new IllegalArgumentException("Values list may not be null");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Node, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        if(values.size() != attributeDescriptions.length)
        {
            throw new IllegalArgumentException("The size of the attribute description list must match the size of the values list");
        }

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        final String update = String.format("UPDATE %s SET %s WHERE %s = ?",
                                            getNodeAttributesTableName(networkTableName),
                                            String.join(", ", columnNames.stream().map(name -> name + " = ?").collect(Collectors.toList())),
                                            "node_id");

        JdbcUtility.update(this.databaseConnection,
                           update,
                           preparedStatement -> { final int size = values.size(); // Same as attributeDescriptions.size()

                                                  for(int valueIndex = 0; valueIndex < size; ++valueIndex)
                                                  {
                                                      final AttributeDescription attributeDescription = attributeDescriptions[valueIndex];
                                                      final Object               value                = values.get(valueIndex);

                                                      if(!attributeDescription.dataTypeAgrees(value))
                                                      {
                                                          throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");
                                                      }

                                                      preparedStatement.setObject(valueIndex+1, value);
                                                  }

                                                  preparedStatement.setInt(size+1, nodeIdentifier);
                                                });

        this.databaseConnection.commit();
    }

    /**
     * Adds a collection of nodes with their attributes
     *
     * @param nodeAttributePairs
     *             Collection of node-attribute pairs
     * @param attributeDescriptions
     *             Collection of which attributes should be set
     * @throws SQLException
     *             if there is a database error
     */
    public void addNodes(final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs,
                         final AttributeDescription...               attributeDescriptions) throws SQLException
    {
        if(attributeDescriptions == null)
        {
            throw new IllegalArgumentException("Attribute descriptions list may not be null");
        }

        if(nodeAttributePairs == null)
        {
            throw new IllegalArgumentException("Collection of node-attribute may not be null");
        }

        final Pair<String, List<String>> schema = getSchema(AttributedType.Node, attributeDescriptions); // Checks attribute description collection for null/empty/all referencing the same network table, and attributed type

        final String       networkTableName = schema.getLeft();
        final List<String> columnNames      = schema.getRight();

        columnNames.add(0, "node_id");

        final String insert = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                            getNodeAttributesTableName(networkTableName),
                                            String.join(", ", columnNames),
                                            String.join(", ", Collections.nCopies(columnNames.size(), "?")));

        JdbcUtility.update(this.databaseConnection,
                           insert,
                           nodeAttributePairs,
                           (preparedStatement, nodeAttributePair) -> { final int          nodeIdentifier = nodeAttributePair.getLeft();
                                                                       final List<Object> values         = nodeAttributePair.getRight();

                                                                       if(values == null)
                                                                       {
                                                                           throw new IllegalArgumentException("Values list may not be null");
                                                                       }

                                                                       if(values.size() != columnNames.size()-1) // We subtract 1 because "node_id" was added above...
                                                                       {
                                                                           throw new IllegalArgumentException("The size of the column name list must match the size of the values list");
                                                                       }

                                                                       int argumentIndex = 1;

                                                                       preparedStatement.setInt(argumentIndex++, nodeIdentifier);

                                                                       for(final Object value : values)
                                                                       {
                                                                           preparedStatement.setObject(argumentIndex++, value);
                                                                       }
                                                                      });

        this.databaseConnection.commit();
    }

    private static Pair<String, List<String>> getSchema(final AttributedType          attributedType,
                                                        final AttributeDescription... attributeDescriptions)
    {
        if(attributeDescriptions == null || attributeDescriptions.length == 0)
        {
            throw new IllegalArgumentException("Collection of attribute descriptions may not be null or empty");
        }

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        final String firstNetworkTableName = attributeDescriptions[0].getNetworkTableName();

        return Pair.of(firstNetworkTableName,
                       Arrays.asList(attributeDescriptions)
                             .stream()
                             .map(description -> { if(!description.getNetworkTableName().equals(firstNetworkTableName))
                                                   {
                                                       throw new IllegalArgumentException("Attribute descriptions must all refer to the same network table");
                                                   }

                                                   if(!description.getAttributedType().equals(attributedType))
                                                   {
                                                       throw new IllegalArgumentException("Attribute descriptions must all refer exclusively to nodes or exclusively to edges");
                                                   }

                                                   return description.getName();
                                                 })
                             .collect(Collectors.toList()));
    }

    @SuppressWarnings("static-method")
    protected String getNetworkCreationSql(final String networkTableName)
    {
        return "CREATE TABLE " + networkTableName + "\n" +
               "(id        INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n" +
               " from_node INTEGER NOT NULL,                  -- Starting point of an edge\n" +
               " to_node   INTEGER NOT NULL,                  -- End of an edge\n"            +
               " UNIQUE (from_node, to_node));";
    }

    @SuppressWarnings("static-method")
    protected String getAttributeDescriptionCreationSql()
    {
        return "CREATE TABLE " + AttributeDescriptionTableName + "\n" +
               "(id              INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n"            +
               " table_name      TEXT NOT NULL,                     -- Name of network table\n"                +
               " name            TEXT NOT NULL,                     -- Name of attribute\n"                    +
               " units           TEXT NOT NULL,                     -- Attribute value's units\n"              +
               " data_type       TEXT NOT NULL,                     -- Data type of attribute\n"               +
               " description     TEXT NOT NULL,                     -- Attribute description\n"                +
               " attributed_type TEXT NOT NULL,                     -- Target attribute type (edge or node)\n" +
               " UNIQUE (table_name, name, attributed_type),"                                                  +
               " CONSTRAINT fk_natd_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getNodeAttributeTableCreationSql(final String nodeAttributeTableName)
    {
        return "CREATE TABLE " + nodeAttributeTableName + "\n"      +
               "(node_id INTEGER PRIMARY KEY, -- Node identifier\n" +
               " UNIQUE (node_id));";   // An index wasn't being automatically created for "node_id"
    }

    private static final String ExtensionName             = "rgi_network";
    private static final String ExtensionDefinition       = "definition"; // TODO
    private static final String NodeAttributesTableSuffix = "_node_attributes";

    /**
     * Name of the singular table describing attributes for network tables
     */
    public static final String AttributeDescriptionTableName = "network_attribute_description";
}
