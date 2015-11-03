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

import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.common.util.jdbc.SavedParameterizedQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class NodeExitGetter extends SavedParameterizedQuery<List<AttributedEdge>>
{
    /**
     * Constructor
     *
     * @param nodeAttributeDescriptions
     *         Collection of attributes that will be retrieved for the
     *         edge's two endpoints
     * @param edgeAttributeDescriptions
     *         Collection of attributes that will be retrieved for the
     *         edge
     * @throws SQLException
     *         if there is a database error
     */
    protected NodeExitGetter(final Connection databaseConnection,
                             final Network network,
                             final Collection<AttributeDescription> nodeAttributeDescriptions,
                             final Collection<AttributeDescription> edgeAttributeDescriptions) throws SQLException
    {
        super(databaseConnection,
              () -> { final StringBuilder edgeAttributes = new StringBuilder();

                      if(edgeAttributeDescriptions != null)
                      {
                          for(final AttributeDescription attribute : edgeAttributeDescriptions)
                          {
                              edgeAttributes.append(String.format(", edge.%s", attribute.getName()));
                          }
                      }

                      final StringBuilder nodeAttributes = new StringBuilder();

                      if(nodeAttributeDescriptions != null)
                      {
                          for(final AttributeDescription attribute : nodeAttributeDescriptions)
                          {
                              nodeAttributes.append(String.format(", f.%s", attribute.getName()));  // from_node attributes, 'f'
                          }

                          for(final AttributeDescription attribute : nodeAttributeDescriptions)
                          {
                              nodeAttributes.append(String.format(", t.%s", attribute.getName())); // to_node attributes, 't'
                          }
                      }

                      return String.format("SELECT edge.id, edge.to_node%1$s%2$s\n" +
                                                   "FROM %3$s as f,\n" +
                                                   "     %3$s as t,\n" +
                                                   "     (SELECT id, to_node%1$s\n" +
                                                   "      FROM %4$s\n" +
                                                   "      WHERE from_node = ?) as edge\n" +
                                                   "WHERE f.node_id = ? AND t.node_id = to_node;",
                                           edgeAttributes.toString(),                                       // %1$s additional requested edge attributes to query for
                                           nodeAttributes.toString(),                                       // %2$s requested node attributes to query for
                                           GeoPackageNetworkExtension.getNodeAttributesTableName(network),  // %3$s node attribute table name
                                           network.getTableName());                                         // %4$s network table name;
                     });

        final int edgeAttributeCount = edgeAttributeDescriptions == null ? 0 : edgeAttributeDescriptions.size();
        final int nodeAttributeCount = nodeAttributeDescriptions == null ? 0 : nodeAttributeDescriptions.size();

        this.firstEdgeAttributeColumn = 3;  // Edge attributes always start after edge.id and edge.to_node, and column indices are 1-based
        this.lastEdgeAttributeColumn  = this.firstEdgeAttributeColumn + edgeAttributeCount;

        this.firstFromNodeAttributeColumn = edgeAttributeCount == 0 ? this.firstEdgeAttributeColumn : this.lastEdgeAttributeColumn + 1;    // From node attributes follow edge attributes
        this.lastFromNodeAttributeColumn  = this.firstFromNodeAttributeColumn + nodeAttributeCount - 1;

        this.firstToNodeAttributeColumn =  nodeAttributeCount == 0 ? this.firstFromNodeAttributeColumn : this.lastFromNodeAttributeColumn + 1;  // To node attributes follow edge attributes
        this.lastToNodeAttributeColumn  = this.firstToNodeAttributeColumn + nodeAttributeCount - 1;
    }

    /**
     * Returns a collection of edge that represent exits from the supplied node
     *
     * @param fromNodeIdentifier
     *             'From' node identifier
     * @return a collection of edge that represent exits from the supplied node
     * @throws SQLException
     *             if there is a database error
     */
    public List<AttributedEdge> getExits(final int fromNodeIdentifier) throws SQLException
    {
        this.fromNodeIdentifier = fromNodeIdentifier;

        this.getPreparedStatement().setInt(1, fromNodeIdentifier);
        this.getPreparedStatement().setInt(2, fromNodeIdentifier);

        return this.execute();
    }

    @Override
    protected List<AttributedEdge> processResult(final ResultSet resultSet) throws SQLException
    {
        return JdbcUtility.map(resultSet,
                               results -> new AttributedEdge(results.getInt(1),   // edge identifier
                                                             NodeExitGetter.this.getEdgeAttributes(results),
                                                             new AttributedNode(this.fromNodeIdentifier,
                                                                                NodeExitGetter.this.getFromNodeAttributes(results)),
                                                             new AttributedNode(resultSet.getInt(2),
                                                                                NodeExitGetter.this.getToNodeAttributes(results))));
    }

    protected List<Object> getEdgeAttributes(final ResultSet resultSet) throws SQLException
    {
        return this.firstEdgeAttributeColumn < this.lastEdgeAttributeColumn ? JdbcUtility.getObjects(resultSet, this.firstEdgeAttributeColumn, this.lastEdgeAttributeColumn)
                                                                            : null;
    }

    protected List<Object> getFromNodeAttributes(final ResultSet resultSet) throws SQLException
    {
        return this.firstFromNodeAttributeColumn < this.lastFromNodeAttributeColumn ? JdbcUtility.getObjects(resultSet, this.firstFromNodeAttributeColumn, this.lastFromNodeAttributeColumn)
                                                                                    : null;
    }

    protected List<Object> getToNodeAttributes(final ResultSet resultSet) throws SQLException
    {
        return this.firstToNodeAttributeColumn < this.lastToNodeAttributeColumn ? JdbcUtility.getObjects(resultSet, this.firstToNodeAttributeColumn, this.lastToNodeAttributeColumn)
                                                                                : null;
    }

    private final int firstEdgeAttributeColumn;
    private final int lastEdgeAttributeColumn;
    private final int firstFromNodeAttributeColumn;
    private final int lastFromNodeAttributeColumn;
    private final int firstToNodeAttributeColumn;
    private final int lastToNodeAttributeColumn;

    private int fromNodeIdentifier;
}
