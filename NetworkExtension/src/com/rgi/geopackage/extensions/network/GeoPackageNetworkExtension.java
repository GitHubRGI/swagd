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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map.Entry;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.extensions.GeoPackageExtensions;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.extensions.implementation.ExtensionImplementation;
import com.rgi.geopackage.extensions.implementation.ImplementsExtension;
import com.rgi.geopackage.tiles.TileSet;

/**
 * Implementation of the RGI Network GeoPackage extension
 *
 * @author Luke Lambert
 *
 */
@ImplementsExtension(name = "rgi_network")
public class GeoPackageNetworkExtension extends ExtensionImplementation
{
    public GeoPackageNetworkExtension(final Connection           databaseConnection,
                                      final GeoPackageCore       geoPackageCore,
                                      final GeoPackageExtensions geoPackageExtentions) throws SQLException
    {
        super(databaseConnection, geoPackageCore, geoPackageExtentions);
    }

    @Override
    public String getTableName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getColumnName()
    {
        // TODO Auto-generated method stub
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
        return Definition;
    }

    @Override
    public Scope getScope()
    {
        return Scope.ReadWrite;
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

        final String networkAttributesTableName = tableName + NetworkAttributeTableSuffix;

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, networkAttributesTableName))
        {
            throw new IllegalArgumentException("A table already exists with this network attribute's table name");
        }

        try
        {
            // Create the network table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getNetworkCreationSql(tableName));
            }

            // Create the network attributes table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getNetworkAttributeCreationSql(networkAttributesTableName));
            }

            // Add tile set to the content table
            this.geoPackageCore.addContent(tableName,
                                           TileSet.TileContentType,
                                           identifier,
                                           description,
                                           boundingBox,
                                           spatialReferenceSystem);

            this.databaseConnection.commit();

            final Network network = this.getNetwork(tableName);

            this.lazyAddExtensionEntry();

            return network;
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

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
                                                               "id",
                                                               "name",
                                                               "data_type",
                                                               "description",
                                                               AttributeDescriptionTableName,
                                                               "table_name",
                                                               "attributed_type");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeDescriptionQuery))
        {
            preparedStatement.setString(1, network.getTableName());
            preparedStatement.setString(2, attributedType.toString());

            return JdbcUtility.map(preparedStatement.executeQuery(),
                                   resultSet -> new AttributeDescription(resultSet.getInt(1),                      // identifier
                                                                         network.getTableName(),                   // network table name
                                                                         resultSet.getString(2),                   // attribute name
                                                                         DataType.valueOf(resultSet.getString(3)), // attribute data type
                                                                         resultSet.getString(4),                   // attribute description
                                                                         attributedType));                         // attributed type
        }
    }

    public AttributeDescription getAttributeDescription(final Network network, final String name, final AttributedType attributedType) throws SQLException
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
                                                               "id",
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
            preparedStatement.setString(2, name);

            return JdbcUtility.mapOne(preparedStatement.executeQuery(),
                                      resultSet -> new AttributeDescription(resultSet.getInt(1),                      // identifier
                                                                            network.getTableName(),                   // network table name
                                                                            resultSet.getString(2),                   // attribute name
                                                                            DataType.valueOf(resultSet.getString(3)), // attribute data type
                                                                            resultSet.getString(4),                   // attribute description
                                                                            attributedType));                         // attributed type
        }
    }

    public AttributeDescription addAttributeDescription(final Network        network,
                                                        final String         name,
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

        final String insert = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)",
                                            AttributeDescriptionTableName,
                                            "table_name",
                                            "name",
                                            "data_type",
                                            "description",
                                            "attributed_type");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
        {
            preparedStatement.setString(1, network.getTableName());
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, dataType.toString());
            preparedStatement.setString(4, description);
            preparedStatement.setString(5, attributedType.toString());

            preparedStatement.executeUpdate();
        }

        this.databaseConnection.commit();

        return this.getAttributeDescription(network, name, attributedType);
    }


    public <T> T getAttribute(final int attributedIdentifier, final AttributeDescription attributeDescription) throws SQLException
    {
        if(attributeDescription == null)
        {
            throw new IllegalArgumentException("Attribute description may not be null");
        }

        final String attributeQuery = String.format("SELECT %s FROM %s%s WHERE %s = ? AND %s = ? LIMIT 1;",
                                                    "value",
                                                    attributeDescription.getNetworkTableName(),
                                                    NetworkAttributeTableSuffix,
                                                    "attribute_description_id",
                                                    "attributed_id");

        try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(attributeQuery))
        {
            preparedStatement.setInt(1, attributeDescription.getIdentifier());
            preparedStatement.setInt(2, attributedIdentifier);

            return JdbcUtility.mapOne(preparedStatement.executeQuery(),
                                      resultSet -> { @SuppressWarnings("unchecked")
                                                     final T value = (T)resultSet.getObject(1); // This may throw a ClassCastException if the stored type cannot be converted to the requested type

                                                     if(!attributeDescription.dataTypeAgrees(value))
                                                     {
                                                         throw new IllegalArgumentException("Value does not match the data type specified by the attribute description");   // Throw if the requested type doesn't match the
                                                     }

                                                     return value;
                                                   });
        }
    }

    public void addAttributes(final int attributedIdentifier, final List<AttributeDescription> attributeDescriptions, final List<Object> values) throws SQLException
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
            final String networkTableName = attributeDescriptions.get(0).getNetworkTableName();

            final String insert = String.format("INSERT INTO %s%s (%s, %s, %s) VALUES (?, ?, ?)",
                                                networkTableName,
                                                NetworkAttributeTableSuffix,
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

            this.databaseConnection.commit();
        }
    }

    public void addAttributes(final List<AttributeDescription> attributeDescriptions, final Iterable<Entry<Integer, List<Object>>> attributedIdentifierValuePairs) throws SQLException
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
            final String networkTableName = attributeDescriptions.get(0).getNetworkTableName();

            final String insert = String.format("INSERT INTO %s%s (%s, %s, %s) VALUES (?, ?, ?)",
                                                networkTableName,
                                                NetworkAttributeTableSuffix,
                                                "attribute_description_id",
                                                "attributed_id",
                                                "value");

            try(final PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insert))
            {
                for(final Entry<Integer, List<Object>> attributedIdentifierValuePair : attributedIdentifierValuePairs)
                {
                    final int          attributedIdentifier = attributedIdentifierValuePair.getKey();
                    final List<Object> values               = attributedIdentifierValuePair.getValue();

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

    @SuppressWarnings("static-method")
    protected String getNetworkCreationSql(final String networkTableName)
    {
        return "CREATE TABLE " + networkTableName + "\n" +
               "(id   INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n" +
               " from INTEGER NOT NULL,                  -- Starting point of an edge\n" +
               " to   INTEGER NOT NULL,                  -- End of an edge\n"            +
               " UNIQUE (from, to));";
    }

    @SuppressWarnings("static-method")
    protected String getAttributeDescriptionCreationSql()
    {
        return "CREATE TABLE " + AttributeDescriptionTableName + "\n" +
               "(id              INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n"            +
               " table_name      TEXT NOT NULL,                     -- Name of network table\n"                +
               " name            TEXT NOT NULL,                     -- Name of attribute\n"                    +
               " data_type       TEXT NOT NULL,                     -- Data type of attribute\n"               +
               " description     TEXT NOT NULL,                     -- Attribute description\n"               +
               " attributed_type TEXT NOT NULL,                     -- Target attribute type (edge or node)\n" +
               " UNIQUE (table_name, name, attributed_type),"                                        +
               " CONSTRAINT fk_natd_table_name FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getNetworkAttributeCreationSql(final String networkAttributeTableName)
    {
        return "CREATE TABLE " + networkAttributeTableName + "\n" +
               "(attribute_description_id INTEGER NOT NULL, -- Identifier of the attribute description\n" +
               " attributed_id            INTEGER NOT NULL, -- Target (edge or node) identifier\n"        +
               " value                    BLOB,             -- Value of the attribute\n"                  +
               " UNIQUE (attribute_description_id, attributed_id),"                                       +
               " CONSTRAINT fk_na_table_name FOREIGN KEY (attribute_description_id) REFERENCES " + AttributeDescriptionTableName + "(id));";
    }

    private static final String ExtensionName               = "rgi_network";
    private static final String Definition                  = null;
    private static final String NetworkAttributeTableSuffix = "_attributes";

    public static final String AttributeDescriptionTableName = "network_attribute_description";
}
