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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class GeoPackageNetworkExtension2 extends ExtensionImplementation
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
    public GeoPackageNetworkExtension2(final Connection           databaseConnection,
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
        if(networkTableName == null)
        {
            throw new IllegalArgumentException("Network may not be null");
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
                try(Statement statement = this.databaseConnection.createStatement())
                {
                    statement.executeUpdate(this.getAttributeDescriptionCreationSql());
                }
            }

            // Create the network table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getNetworkCreationSql(tableName));
            }

            // Create the network's attributes table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getNodeAttributeTableCreationSql(nodeAttributesTableName));
            }

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
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Get an edge based on its identifier
     *
     * @param network
     *             Network table reference
     * @param edgeIdentifier
     *             An edge's unique identifier
     * @return an {@link Edge} entry in the supplied {@link Network}, or null
     *             if no matching edge exists
     * @throws SQLException
     *             if there is a database error
     */
    public Edge getEdge(final Network network, final int edgeIdentifier) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String edgeQuery = String.format("SELECT %s, %s FROM %s WHERE %s = ? LIMIT 1;",
                                               "from_node",
                                               "to_node",
                                               network.getTableName(),
                                               "id");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(edgeQuery))
        {
            preparedStatement.setInt(1, edgeIdentifier);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.mapOne(resultSet,
                                          results -> new Edge(edgeIdentifier,        // identifier
                                                              results.getInt(1),   // attribute name
                                                              results.getInt(2))); // attributed type
            }

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
     * @return an an {@link Edge} entry in the supplied {@link Network}, or null
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

        final String edgeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1;",
                                               "id",
                                               network.getTableName(),
                                               "from_node",
                                               "to_node");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(edgeQuery))
        {
            preparedStatement.setInt(1, from);
            preparedStatement.setInt(1, to);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.mapOne(resultSet,
                                          results -> new Edge(results.getInt(1), // identifier
                                                              from,              // attribute name
                                                              to));              // attributed type
            }
        }
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
    public List<Integer> getEntries(final Network network, final int node) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        final String edgeQuery = String.format("SELECT %s FROM %s WHERE %s = ?;",
                                               "from_node",
                                               network.getTableName(),
                                               "to_node");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(edgeQuery))
        {
            preparedStatement.setInt(1, node);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.map(resultSet, results -> results.getInt(1));
            }
        }
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

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(edgeQuery))
        {
            preparedStatement.setInt(1, node);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.map(resultSet,
                                       results -> new Edge(results.getInt(1),
                                                           node,
                                                           results.getInt(2)));
            }
        }
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

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s FROM %s;",
                                                               "id",
                                                               "from_node",
                                                               "to_node",
                                                               network.getTableName());

        try(final Statement statement = this.databaseConnection.createStatement())
        {
            try(final ResultSet resultSet = statement.executeQuery(attributeDescriptionQuery))
            {
                while(resultSet.next())
                {
                    consumer.accept(new Edge(resultSet.getInt(1),
                                             resultSet.getInt(2),
                                             resultSet.getInt(3)));
                }
            }
        }
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

        int identifier = -1;

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS))
        {
            preparedStatement.setInt(1, from);
            preparedStatement.setInt(2, to);

            preparedStatement.executeUpdate();

            identifier = preparedStatement.getGeneratedKeys().getInt(1);
        }

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

        final String insert = String.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
                                            network.getTableName(),
                                            "from_node",
                                            "to_node");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
        {
            for(final Pair<Integer, Integer> edge : edges)
            {
                preparedStatement.setInt(1, edge.getLeft());
                preparedStatement.setInt(2, edge.getRight());

                preparedStatement.executeUpdate();
            }
        }

        this.databaseConnection.commit();
    }

    /**
     * Adds edges to a {@link Network} along with each edge's attributes
     *
     * @param network
     *             Network table reference
     * @param attributeDescriptions
     *             Collection of {@link AttributeDescription}s
     * @param attributedEdges
     *             Collection of edge/attribute pairs, where the edges are each a pair of nodes
     * @throws SQLException
     *             if there is a database error
     */
    public void addAttributedEdges(final Network network,
                                   final List<AttributeDescription> attributeDescriptions,
                                   final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(attributeDescriptions == null || attributeDescriptions.isEmpty())
        {
            throw new IllegalArgumentException("Attribute description collection may not be null or empty");
        }

        if(attributedEdges == null || attributedEdges.isEmpty())
        {
            throw new IllegalArgumentException("Attributed edges collection may not be null or empty");
        }

        final String insert = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, %s)",
                                            network.getTableName(),
                                            String.join(", ", getColumnNames(attributeDescriptions)),
                                            "from_node",
                                            "to_node",
                                            String.join(", ", Collections.nCopies(attributeDescriptions.size(), "?")));

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
        {
            for(final Pair<Pair<Integer, Integer>, List<Object>> attributedEdge : attributedEdges)
            {
                final Pair<Integer, Integer> edge   = attributedEdge.getLeft();
                final List<Object>           values = attributedEdge.getRight();

                int parameterIndex = 1;

                preparedStatement.setInt(parameterIndex++, edge.getLeft());
                preparedStatement.setInt(parameterIndex++, edge.getRight());

                for(final Object value : values)
                {
                    preparedStatement.setObject(parameterIndex++, value);
                }

                preparedStatement.executeUpdate();
            }
        }

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
    public List<AttributeDescription> getAttributeDescriptions(final Network network, final AttributedType attributedType) throws SQLException
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = ? AND %s = ?;",
                                                               "name",
                                                               "units",
                                                               "data_type",
                                                               "description",
                                                               AttributeDescriptionTableName,
                                                               "table_name",
                                                               "attributed_type");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeDescriptionQuery))
        {
            preparedStatement.setString(1, network.getTableName());
            preparedStatement.setString(2, attributedType.toString());

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.map(preparedStatement.executeQuery(),
                                       results -> new AttributeDescription(network.getTableName(),                 // network table name
                                                                           results.getString(1),                   // attribute name
                                                                           results.getString(2),                   // attribute units
                                                                           DataType.valueOf(results.getString(3)), // attribute data type
                                                                           results.getString(4),                   // attribute description
                                                                           attributedType));                       // attributed type
            }


        }
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

        if(attributedType == null)
        {
            throw new IllegalArgumentException("Attributed type may not be null");
        }

        if(name == null)
        {
            throw new IllegalArgumentException("Attribute name may not be null");
        }

        final String attributeDescriptionQuery = String.format("SELECT %s, %s, %s FROM %s WHERE %s = ? AND %s = ? AND %s = ? LIMIT 1;",
                                                               "units",
                                                               "data_type",
                                                               "description",
                                                               AttributeDescriptionTableName,
                                                               "table_name",
                                                               "attributed_type",
                                                               "name");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeDescriptionQuery))
        {
            preparedStatement.setString(1, network.getTableName());
            preparedStatement.setString(2, attributedType.toString());
            preparedStatement.setString(3, name);

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.mapOne(resultSet,
                                          results -> new AttributeDescription(network.getTableName(),                 // network table name
                                                                              name,                                   // attribute name
                                                                              results.getString(1),                   // attribute units
                                                                              DataType.valueOf(results.getString(2)), // attribute data type
                                                                              results.getString(3),                   // attribute description
                                                                              attributedType));                       // attributed type
            }


        }
    }

    /**
     * Adds an attribute description to a {@link Network}
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
     * @param defaultValue
     *             Default value for the new attribute column
     * @return handle to the added {@link AttributeDescription}
     * @throws SQLException
     *             if there is a database error
     */
    public AttributeDescription addAttributeDescription(final Network        network,
                                                        final String         name,
                                                        final String         units,
                                                        final DataType       dataType,
                                                        final String         description,
                                                        final AttributedType attributedType,
                                                        final Object         defaultValue) throws SQLException
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

        final String insert = String.format("ALTER TABLE %s ADD COLUMN %s DEFAULT ?;",
                                            "table_name",
                                            "name");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
        {
            preparedStatement.setObject(1, defaultValue);

            preparedStatement.executeUpdate();
        }

        this.databaseConnection.commit();

        return new AttributeDescription(network.getTableName(),
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
     * @return the attribute's value stored for the node or edge
     * @throws SQLException
     *             if there is a database error
     */
    public <T> T getAttribute(final Edge edge, final AttributeDescription attributeDescription) throws SQLException
    {
        if(attributeDescription == null)
        {
            throw new IllegalArgumentException("Attribute description may not be null");
        }

        final String attributeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1;",
                                                    attributeDescription.getName(),
                                                    attributeDescription.getNetworkTableName(),
                                                    "id");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeQuery))
        {
            preparedStatement.setInt(1, edge.getIdentifier());

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                return JdbcUtility.mapOne(resultSet,
                                          results -> { if(!results.isBeforeFirst())
                                                       {
                                                           throw new IllegalArgumentException("Edge and attribute description do not belong to the same network");
                                                       }

                                                       @SuppressWarnings("unchecked")
                                                       final T value = (T)results.getObject(1); // This may throw a ClassCastException if the stored type cannot be converted to the requested type

                                                       if(!attributeDescription.dataTypeAgrees(value))
                                                       {
                                                           throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");   // Throw if the requested type doesn't match the
                                                       }

                                                       return value;
                                                     });
            }
        }
    }

    /**
     * Get multiple attribute values from an edge
     *
     * @param edge
     *             Reference to an edge in a network table
     * @param attributeDescriptions
     *             Collection of which attributes should be retrieved
     * @return the attributes' respective values stored for the node or edge
     * @throws SQLException
     *             if there is a database error
     */
    public List<Object> getAttributes(final Edge                    edge,
                                      final AttributeDescription... attributeDescriptions) throws SQLException
    {
        if(attributeDescriptions == null || attributeDescriptions.length == 0)
        {
            throw new IllegalArgumentException("Attribute descriptions may not be null or empty");
        }

        final String networkTableName = attributeDescriptions[0].getNetworkTableName();

        final String attributeQuery = String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1;",
                                                    String.join(", ", getColumnNames(Arrays.asList(attributeDescriptions))),
                                                    networkTableName,
                                                    "id");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeQuery))
        {
            preparedStatement.setInt(0, edge.getIdentifier());

            try(final ResultSet resultSet = preparedStatement.executeQuery())
            {
                if(resultSet.isBeforeFirst())
                {
                    throw new IllegalArgumentException("Edge does not belong to the network table specified by the supplied attributes");
                }

                final List<Object> values = new ArrayList<>(attributeDescriptions.length);

                for(int attributeIndex = 1; attributeIndex <= attributeDescriptions.length; ++attributeIndex)
                {
                    values.add(resultSet.getObject(attributeIndex));
                }

                return values;
            }
        }
    }

    /**
     * Adds attributes for a given node or edge
     *
     * @param network
     *             Network table reference
     * @param nodeIdentifier
     *             Unique identifier to either a node or edge
     * @param attributeDescriptions
     *             Collection of which attributes should be set
     * @param values
     *             Values of the attributes in corresponding order to the given
     *             attribute descriptions
     * @throws SQLException
     *             if there is a database error
     */
    public void addAttributes(final Network                    network,
                              final int                        nodeIdentifier,
                              final List<AttributeDescription> attributeDescriptions,
                              final List<Object>               values) throws SQLException
    {
        if(attributeDescriptions == null)
        {
            throw new IllegalArgumentException("Attribute descriptions list may not be null");
        }

        if(values == null)
        {
            throw new IllegalArgumentException("Values list may not be null");
        }

        if(values.size() != attributeDescriptions.size())
        {
            throw new IllegalArgumentException("The size of the attribute description list must match the size of the values list");
        }

        if(!attributeDescriptions.isEmpty())
        {
            this.addAttributesNoCommit(network,
                                       nodeIdentifier,
                                       attributeDescriptions,
                                       values);

            this.databaseConnection.commit();
        }
    }

    /**
     * Adds attributes to a collection of either edges or nodes
     *
     * @param network
     *             Network table reference
     * @param attributeDescriptions
     *             Collection of which attributes should be set
     * @param attributedIdentifierValuePairs
     *             Collection of pairs, attributed object (node or edge identifier) and an attribute collection
     * @throws SQLException
     *             if there is a database error
     */
    public void addAttributes(final Network                               network,
                              final List<AttributeDescription>            attributeDescriptions,
                              final Iterable<Pair<Integer, List<Object>>> attributedIdentifierValuePairs) throws SQLException
    {
        if(attributeDescriptions == null)
        {
            throw new IllegalArgumentException("Attribute descriptions list may not be null");
        }

        if(attributedIdentifierValuePairs == null)
        {
            throw new IllegalArgumentException("Attributed identifer value pairs iterator may not be null");
        }

        if(!attributeDescriptions.isEmpty())
        {
            final AttributedType attributedType = attributeDescriptions.get(0).getAttributedType();

            if(!attributeDescriptions.stream().allMatch(description -> description.getNetworkTableName().equals(network.getTableName())))
            {
                throw new IllegalArgumentException("Attribute descriptions must all refer to the network parameter");
            }

            if(!attributeDescriptions.stream().allMatch(description -> description.getAttributedType().equals(attributedType)))
            {
                throw new IllegalArgumentException("Attribute descriptions must all refer to either nodes or edges");
            }

            final String insert = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                                                getNodeAttributesTableName(network.getTableName()),
                                                "attribute_description_id",
                                                "attributed_id",
                                                "value");

            try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
            {
                for(final Pair<Integer, List<Object>> attributedIdentifierValuePair : attributedIdentifierValuePairs)
                {
                    final int          attributedIdentifier = attributedIdentifierValuePair.getLeft();
                    final List<Object> values               = attributedIdentifierValuePair.getRight();

                    if(values.size() != attributeDescriptions.size())
                    {
                        throw new IllegalArgumentException("The size of the attribute description list must match the size of the values list");
                    }

                    final int size = attributeDescriptions.size(); // Same as attributeDescriptions.size()

                    for(int valueIndex = 0; valueIndex < size; ++valueIndex)
                    {
                        final AttributeDescription attributeDescription = attributeDescriptions.get(valueIndex);
                        final Object               value                = values               .get(valueIndex);

                        if(!attributeDescription.dataTypeAgrees(value))
                        {
                            throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");
                        }

                        preparedStatement.setInt   (1, attributeDescription.getIdentifier());
                        preparedStatement.setInt   (2, attributedIdentifier);
                        preparedStatement.setObject(3, value);

                        preparedStatement.executeUpdate();
                    }
                }
            }

            this.databaseConnection.commit();
        }
    }

    private static Iterable<String> getColumnNames(final List<AttributeDescription> attributeDescriptions)
    {
        if(attributeDescriptions == null)
        {
            throw new IllegalArgumentException("Attribute description collection may not be null");
        }

        if(attributeDescriptions.size() == 0)
        {
            return Collections.emptyList();
        }

        final String fistNetworkTableName = attributeDescriptions.get(0).getNetworkTableName();

        if(attributeDescriptions.stream()
                                .skip(1)
                                .allMatch(description -> description.getNetworkTableName()
                                                                    .equals(fistNetworkTableName)))
        {
            throw new IllegalArgumentException("Attribute descriptions must all refer to the same network table");
        }

        return attributeDescriptions.stream()
                                    .map(description -> description.getName())
                                    .collect(Collectors.toList());
    }

    private void addAttributesNoCommit(final Network                    network,
                                       final int                        attributedIdentifier,
                                       final List<AttributeDescription> attributeDescriptions,
                                       final List<Object>               values) throws SQLException
    {
        if(attributeDescriptions == null)
        {
            throw new IllegalArgumentException("Attribute descriptions list may not be null");
        }

        if(values == null)
        {
            throw new IllegalArgumentException("Values list may not be null");
        }

        if(values.size() != attributeDescriptions.size())
        {
            throw new IllegalArgumentException("The size of the attribute description list must match the size of the values list");
        }

        final String insert = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                                            getNodeAttributesTableName(network),
                                            "attribute_description_id",
                                            "attributed_id",
                                            "value");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
        {
            final int size = values.size(); // Same as attributeDescriptions.size()

            for(int valueIndex = 0; valueIndex < size; ++valueIndex)
            {
                final AttributeDescription attributeDescription = attributeDescriptions.get(valueIndex);
                final Object               value                = values               .get(valueIndex);

                if(!attributeDescription.getNetworkTableName().equals(network.getTableName()))
                {
                    throw new IllegalArgumentException(String.format("Attribute description %s belongs to table '%s', not '%s'",
                                                                     attributeDescription.toString(),
                                                                     attributeDescription.getNetworkTableName(),
                                                                     network.getTableName()));
                }

                if(!attributeDescription.dataTypeAgrees(value))
                {
                    throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");
                }

                preparedStatement.setInt   (1, attributeDescription.getIdentifier());
                preparedStatement.setInt   (2, attributedIdentifier);
                preparedStatement.setObject(3, value);

                preparedStatement.executeUpdate();
            }
        }
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
               " UNIQUE (table_name, name, attributed_type),"                                        +
               " CONSTRAINT fk_natd_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getNodeAttributeTableCreationSql(final String nodeAttributeTableName)
    {
        return "CREATE TABLE " + nodeAttributeTableName + "\n"   +
               "(node_id INTEGER PRIMARY KEY, -- Node identifier\n" + ");";
    }

    private static final String ExtensionName             = "rgi_network";
    private static final String ExtensionDefinition       = "definition"; // TODO
    private static final String NodeAttributesTableSuffix = "_node_attributes";

    /**
     * Name of the singular table describing attributes for network tables
     */
    public static final String AttributeDescriptionTableName = "network_attribute_description";
}
