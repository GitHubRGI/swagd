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
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.utility.DatabaseUtility;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
import com.rgi.store.routingnetworks.osm.NodeDimensionality;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public class GeoPackageRoutingNetworkStoreWriter implements RoutingNetworkStoreWriter
{
    public GeoPackageRoutingNetworkStoreWriter(final File        geoPackageFile,
                                               final String      networkTableName,
                                               final String      networkIdentifier,
                                               final String      description,
                                               final String      coordinateUnits, // TODO it'd sure be nice to be able to pull the units from the CRS that gets passed in to write()
                                               final String      elevationUnits,  // TODO it'd sure be nice to be able to pull the units from the CRS that gets passed in to write()
                                               final BoundingBox networkBounds) throws RoutingNetworkStoreException
    {
        if(geoPackageFile == null)
        {
            throw new IllegalArgumentException("GeoPackageFile cannot be null.");
        }

        DatabaseUtility.validateTableName(networkTableName);

        final boolean parentCreated;

        if(geoPackageFile.getParentFile() != null && !geoPackageFile.getParentFile().isDirectory())
        {
            if(!geoPackageFile.getParentFile().mkdirs())
            {
                throw new RuntimeException("Unable to create file: " + geoPackageFile.getPath());
            }

            parentCreated = true;
        }
        else
        {
            parentCreated = false;
        }

        try
        {
            this.geoPackage = new GeoPackage(geoPackageFile, GeoPackage.OpenMode.OpenOrCreate);
        }
        catch(final ClassNotFoundException | ConformanceException | IOException | SQLException ex)
        {
            if(parentCreated)
            {
                geoPackageFile.getParentFile()
                              .delete();    // TODO check for delete failure?
            }

            throw new RoutingNetworkStoreException(ex);
        }

        this.networkTableName  = networkTableName;
        this.networkIdentifier = networkIdentifier;
        this.description       = description;
        this.coordinateUnits   = coordinateUnits;
        this.elevationUnits    = elevationUnits;
        this.networkBounds     = networkBounds;

    }

    @Override
    public void write(final List<Node>                nodes,
                      final List<Edge>                edges,
                      final NodeDimensionality        nodeDimensionality,
                      final List<Pair<String, Type>>  nodeAttributeDescriptions,
                      final List<Pair<String, Type>>  edgeAttributeDescriptions,
                      final CoordinateReferenceSystem coordinateReferenceSystem) throws RoutingNetworkStoreException
    {
        try
        {
            final GeoPackageRoutingExtension routingExtension = this.geoPackage
                                                                    .extensions()
                                                                    .getExtensionImplementation(GeoPackageRoutingExtension.class);

            final GeoPackageNetworkExtension networkExtension = routingExtension.getNetworkExtension();

            final SpatialReferenceSystem spatialReferenceSystem = this.geoPackage
                                                                      .core()
                                                                      .getSpatialReferenceSystem(coordinateReferenceSystem.getAuthority(),
                                                                                                 coordinateReferenceSystem.getIdentifier());

            final Network network = networkExtension.addNetwork(this.networkTableName,
                                                                this.networkIdentifier,
                                                                this.description,
                                                                this.networkBounds,
                                                                spatialReferenceSystem);

            final List<AttributeDescription> defaultSpatialAttributeDescriptions = this.addDefaultSpatialAttributeDescriptions(networkExtension,
                                                                                                                               nodeDimensionality,
                                                                                                                               network);

            routingExtension.addRoutingNetworkDescription(network,
                                                          defaultSpatialAttributeDescriptions.get(0),   // TODO it's a little janky to rely on index ordering here...
                                                          defaultSpatialAttributeDescriptions.get(1),
                                                          defaultSpatialAttributeDescriptions.get(2));

            addNodes(nodes,
                     nodeDimensionality,
                     nodeAttributeDescriptions,
                     networkExtension,
                     network,
                     defaultSpatialAttributeDescriptions);

            addEdges(edges,
                     edgeAttributeDescriptions,
                     networkExtension,
                     network);
        }
        catch(final BadImplementationException | SQLException ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }
    }

    private static void addEdges(final Collection<Edge>             edges,
                                 final Iterable<Pair<String, Type>> edgeAttributeDescriptions,
                                 final GeoPackageNetworkExtension   networkExtension,
                                 final Network                      network) throws SQLException
    {
        final List<AttributeDescription> gpkgEdgeAttributeDescriptions = addAttributes(networkExtension,
                                                                                       network,
                                                                                       edgeAttributeDescriptions,
                                                                                       AttributedType.Edge);

        addEdges(networkExtension,
                 edges,
                 gpkgEdgeAttributeDescriptions);
    }

    private static void addNodes(final Collection<Node>             nodes,
                                 final NodeDimensionality           nodeDimensionality,
                                 final Iterable<Pair<String, Type>> nodeAttributeDescriptions,
                                 final GeoPackageNetworkExtension   networkExtension,
                                 final Network                      network,
                                 final List<AttributeDescription>   defaultSpatialAttributeDescriptions) throws SQLException
    {
        final List<AttributeDescription> gpkgNodeAttributeDescriptions = new LinkedList<>();

        gpkgNodeAttributeDescriptions.add(defaultSpatialAttributeDescriptions.get(0)); // Longitude
        gpkgNodeAttributeDescriptions.add(defaultSpatialAttributeDescriptions.get(1)); // Latitude

        final AttributeDescription elevationAttributeDescription = defaultSpatialAttributeDescriptions.get(2);

        if(elevationAttributeDescription != null)
        {
            gpkgNodeAttributeDescriptions.add(elevationAttributeDescription);
        }

        final List<AttributeDescription> gpkgOtherEdgeAttributeDescriptions = addAttributes(networkExtension,
                                                                                            network,
                                                                                            nodeAttributeDescriptions,
                                                                                            AttributedType.Node);

        gpkgNodeAttributeDescriptions.addAll(gpkgOtherEdgeAttributeDescriptions);

        addNodes(networkExtension,
                 nodes,
                 nodeDimensionality,
                 gpkgNodeAttributeDescriptions);
    }


    private List<AttributeDescription> addDefaultSpatialAttributeDescriptions(final GeoPackageNetworkExtension networkExtension,
                                                                              final NodeDimensionality         nodeDimensionality,
                                                                              final Network                    network) throws SQLException
    {
        final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                 "longitude",
                                                                                                 this.coordinateUnits,
                                                                                                 DataType.Real,
                                                                                                 "longitude",
                                                                                                 AttributedType.Node);

        final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                "latitude",
                                                                                                this.coordinateUnits,
                                                                                                DataType.Real,
                                                                                                "latitude",
                                                                                                AttributedType.Node);

        final AttributeDescription elevationAttribute = nodeDimensionality == NodeDimensionality.NoElevation
                                                                            ? null
                                                                            : networkExtension.addAttributeDescription(network,
                                                                                                 "elevation",
                                                                                                 this.elevationUnits,
                                                                                                 DataType.Real,
                                                                                                 "elevation",
                                                                                                 AttributedType.Node);

        return Arrays.asList(longitudeAttribute,
                             latitudeAttribute,
                             elevationAttribute);
    }

    private static List<AttributeDescription> addAttributes(final GeoPackageNetworkExtension         networkExtension,
                                                                  final Network                      network,
                                                                  final Iterable<Pair<String, Type>> nodeAttributeDescriptions,
                                                                  final AttributedType               attributedType) throws SQLException
    {
        final List<AttributeDescription> attributeDescriptions = new LinkedList<>();

        for(final Pair<String, Type> nodeAttributeDescription : nodeAttributeDescriptions)
        {
            attributeDescriptions.add(networkExtension.addAttributeDescription(network,
                                                                               nodeAttributeDescription.getLeft(),
                                                                               "",
                                                                               fromType(nodeAttributeDescription.getRight()),
                                                                               "",
                                                                               attributedType));
        }

        return attributeDescriptions;
    }

    private static void addNodes(final GeoPackageNetworkExtension networkExtension,
                                 final Collection<Node>           nodes,
                                 final NodeDimensionality         nodeDimensionality,
                                 final List<AttributeDescription> gpkgNodeAttributeDescriptions) throws SQLException
    {
        networkExtension.addNodes(nodes.stream()
                                       .map(node -> Pair.of(node.getIdentifier(),
                                                            extractAttributes(node,
                                                                              nodeDimensionality)))
                                       .collect(Collectors.toList()),
                                  gpkgNodeAttributeDescriptions);
    }

    private static List<Object> extractAttributes(final Node               node,
                                                  final NodeDimensionality nodeDimensionality)
    {
        final List<Object> nonSpatialAttributes = node.getAttributes();

        final List<Object> attributes = new ArrayList<>((nodeDimensionality == NodeDimensionality.NoElevation ? 2 : 3) + nonSpatialAttributes.size());

        attributes.add(node.getX());
        attributes.add(node.getY());

        if(nodeDimensionality != NodeDimensionality.NoElevation)
        {
            attributes.add(node.getElevation());
        }

        attributes.addAll(nonSpatialAttributes);

        return attributes;
    }

    private static void addEdges(final GeoPackageNetworkExtension networkExtension,
                                 final Collection<Edge>           edges,
                                 final List<AttributeDescription> gpkgEdgeAttributeDescriptions) throws SQLException
    {
        networkExtension.addAttributedEdges(edges.stream()
                                                 .map(edge -> Pair.of(Pair.of(edge.getTo(),
                                                                              edge.getFrom()),
                                                                      edge.getAttributes()))
                                                 .collect(Collectors.toList()),
                                            gpkgEdgeAttributeDescriptions);
    }

    private static DataType fromType(final Type type)
    {
        if(type.equals(byte[].class) || type.equals(Byte[].class))
        {
            return DataType.Blob;
        }
        else if(type.equals(int.class) || type.equals(Integer.class))
        {
            return DataType.Integer;
        }
        else if(type.equals(float.class)  ||
                type.equals(Float.class)  ||
                type.equals(double.class) ||
                type.equals(Double.class))
        {
            return DataType.Real;
        }
        else    // Treat all other types as String
        {
            return DataType.Text;
        }
    }

    private final GeoPackage  geoPackage;
    private final String      networkTableName;
    private final String      networkIdentifier;
    private final String      description;
    private final String      coordinateUnits;
    private final String      elevationUnits;
    private final BoundingBox networkBounds;
}
