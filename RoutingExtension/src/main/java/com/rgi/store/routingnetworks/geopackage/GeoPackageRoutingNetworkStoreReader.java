/*
 * The MIT License (MIT)
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

package com.rgi.store.routingnetworks.geopackage;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.verification.VerificationLevel;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public class GeoPackageRoutingNetworkStoreReader implements RoutingNetworkStoreReader, AutoCloseable
{
    public GeoPackageRoutingNetworkStoreReader(final File   geoPackageFile,
                                               final String networkTableName) throws RoutingNetworkStoreException
    {
        this(geoPackageFile,
             networkTableName,
             VerificationLevel.Fast);
    }

    public GeoPackageRoutingNetworkStoreReader(final File              geoPackageFile,
                                               final String            networkTableName,
                                               final VerificationLevel verificationLevel) throws RoutingNetworkStoreException
    {
        if(geoPackageFile == null)
        {
            throw new IllegalArgumentException("GeoPackage file may not be null");
        }

        if(networkTableName == null)
        {
            throw new IllegalArgumentException("Tile set may not be null or empty");
        }

        try
        {
            this.geoPackage = new GeoPackage(geoPackageFile,
                                             verificationLevel,
                                             GeoPackage.OpenMode.Open);
        }
        catch(final Exception ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }

        try
        {
            this.routingExtension = this.geoPackage
                                        .extensions()
                                        .getExtensionImplementation(GeoPackageRoutingExtension.class);

            this.networkExtension = this.routingExtension.getNetworkExtension();

            this.network = this.networkExtension.getNetwork(networkTableName);
        }
        catch(final Exception ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }
    }

    @Override
    public List<Pair<String, Type>> getNodeAttributeDescriptions() throws RoutingNetworkStoreException
    {
        return this.getAtrributeDescriptions(AttributedType.Node);
    }

    @Override
    public List<Pair<String, Type>> getEdgeAttributeDescriptions() throws RoutingNetworkStoreException
    {
        return this.getAtrributeDescriptions(AttributedType.Edge);
    }

    @Override
    public List<Node> getNodes()
    {
        return null;//this.networkExtension
    }

    @Override
    public List<Edge> getEdges()
    {
        return null;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return null;
    }

    @Override
    public BoundingBox getBounds()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public boolean isBidirectional()
    {
        return false;
    }

    @Override
    public void close() throws SQLException
    {
        this.geoPackage.close();
    }

    private List<Pair<String, Type>> getAtrributeDescriptions(final AttributedType attributedType) throws RoutingNetworkStoreException
    {
        try
        {
            final List<AttributeDescription> attributeDescriptions = this.networkExtension
                                                                         .getAttributeDescriptions(this.network,
                                                                                                   attributedType);

            return attributeDescriptions.stream()
                                        .map(attributeDescription -> Pair.of(attributeDescription.getName(),
                                                                             convertType(attributeDescription.getDataType())))
                                        .collect(Collectors.toList());
        }
        catch(final SQLException ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }
    }

    private static Type convertType(final DataType dataType)
    {
        switch(dataType)
        {
            case Blob:    return byte[].class;
            case Integer: return int.class;
            case Real:    return double.class;
            case Text:    return String.class;
        }
    }

    private final GeoPackage geoPackage;
    private final Network    network;

    final GeoPackageRoutingExtension routingExtension;
    final GeoPackageNetworkExtension networkExtension;
}
