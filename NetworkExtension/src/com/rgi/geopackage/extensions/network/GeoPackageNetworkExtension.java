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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import utility.DatabaseUtility;

import com.rgi.common.BoundingBox;
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
    public GeoPackageNetworkExtension(final Connection databaseConnection, final GeoPackageCore core, final GeoPackageExtensions geoPackageExtenions) throws SQLException
    {
        super(databaseConnection, geoPackageExtenions);

        this.core = core;
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
        return this.core.getContent(networkTableName,
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

        final Network existingContent = this.getNetwork(tableName);

        if(existingContent != null)
        {
            if(existingContent.equals(tableName,
                                      Network.NetworkContentType,
                                      identifier,
                                      description,
                                      boundingBox,
                                      spatialReferenceSystem.getIdentifier()))
            {
                return existingContent;
            }

            throw new IllegalArgumentException("An entry in the content table already exists with this table name, but has different values for its other fields");
        }

        if(DatabaseUtility.tableOrViewExists(this.databaseConnection, tableName))
        {
            throw new IllegalArgumentException("A table already exists with this network set's table name");
        }

        try
        {
            // Create the network set table
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getNetworkCreationSql(tableName));
            }

            // Add tile set to the content table
            this.core.addContent(tableName,
                                 TileSet.TileContentType,
                                 identifier,
                                 description,
                                 boundingBox,
                                 spatialReferenceSystem);


            this.databaseConnection.commit();

            return this.getNetwork(tableName);
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    public NetworkMetadata getNetworkMetadata(final Network network)
    {

    }

    public NetworkMetadata addNetworkMetadata(final Network network)
    {

    }

    public List<NetworkAttributes> getNetworkAttributes(final Network network)
    {

    }

    public List<NetworkAttributes> getNetworkAttributes(final Network network, final AttributedType type)
    {

    }

    @SuppressWarnings("static-method")
    protected String getNetworkCreationSql(final String networkTableName)
    {
        return "CREATE TABLE " + networkTableName + "\n" +
               "(id   INTEGER PRIMARY KEY AUTOINCREMENT, -- Autoincrement primary key\n" +
               " from INTEGER NOT NULL,                  -- starting point of an edge\n" +
               " to   INTEGER NOT NULL,                  -- end of an edge\n"            +
               " UNIQUE (from, to));";
    }

    private final GeoPackageCore core;

    private static final String ExtensionName = "rgi_network";
    private static final String Definition    = null;


}
