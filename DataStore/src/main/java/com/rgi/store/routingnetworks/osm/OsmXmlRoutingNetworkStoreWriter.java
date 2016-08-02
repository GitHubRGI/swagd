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

package com.rgi.store.routingnetworks.osm;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public class OsmXmlRoutingNetworkStoreWriter implements RoutingNetworkStoreWriter
{
    public OsmXmlRoutingNetworkStoreWriter(final File                      osmXmlFile,
                                           final BoundingBox               bounds,
                                           final String                    description,
                                           final CoordinateReferenceSystem coordinateReferenceSystem)   // TODO move to write()?
    {
        this.osmXmlFile                = osmXmlFile;
        this.bounds                    = bounds;
        this.description               = description;
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    // TODO use an XML library to do the writing, rather than hand-rolling strings
    @Override
    public void write(final List<Node>               nodes,
                      final List<Edge>               edges,
                      final List<Pair<String, Type>> nodeAttributeDescriptions,
                      final List<Pair<String, Type>> edgeAttributeDescriptions) throws RoutingNetworkStoreException
    {
        try(final Writer writer = Files.newBufferedWriter(this.osmXmlFile.toPath(),
                                                          Charset.forName("UTF-8")))
        {
            writeOsmXmlHeader(writer);

            writeNote(writer, this.description);
            writeBounds(writer, this.bounds);

            writer.append('\n');

            final List<String> nodeAttributeNames = nodeAttributeDescriptions.stream()
                                                                             .map(Pair::getLeft)
                                                                             .collect(Collectors.toList());

            for(final Node node : nodes)
            {
                writeNode(writer,
                          node,
                          nodeAttributeNames);
            }

            writer.append('\n');

            final List<String> edgeAttributeNames = edgeAttributeDescriptions.stream()
                                                                             .map(Pair::getLeft)
                                                                             .collect(Collectors.toList());

            for(final Edge edge : edges)
            {
                writeWay(writer,
                         edge,
                         edgeAttributeNames);
            }

            writeOsmXmlFooter(writer);

            writer.flush();
        }
        catch(final Throwable th)
        {
            throw new RoutingNetworkStoreException(th);
        }
    }

    private static void writeOsmXmlHeader(final Writer writer) throws IOException
    {
        writer.write(String.format("<?xml version=\"%s\" encoding=\"%s\"?>\n",
                                   XML_VERSION,
                                   ENCODING.name()));

        writer.write(String.format("<osm version=\"%s\" generator=\"%s\">\n",
                                   OSM_VERSION,
                                   GENERATOR));
    }

    private static void writeNote(final Writer writer,
                                  final String description) throws IOException
    {
        writer.write(String.format("  <note>%s</note>\n",
                                   description));
    }

    private static void writeBounds(final Writer      writer,
                                    final BoundingBox bounds) throws IOException
    {
        // TODO convert these values to EPSG:4326!
        writer.write(String.format("  <bounds minlon=\"%s\"" +
                                            " minlat=\"%s\"" +
                                            " maxlon=\"%s\"" +
                                            " maxlat=\"%s\"/>",
                                   bounds.getMinimumX(),
                                   bounds.getMinimumY(),
                                   bounds.getMaximumX(),
                                   bounds.getMaximumY()));
    }

    private static void writeNode(final Writer       writer,
                                  final Node         node,
                                  final List<String> nodeAttributeNames) throws IOException
    {
        // TODO convert these values to EPSG:4326!
        writer.write(String.format("  <node id=\"%d\"" +
                                          " lon=\"%s\"" +
                                          " lat=\"%s\"",
                                   node.getIdentifier(),
                                   node.getLongitude(),
                                   node.getLatitude()));

        if(node.getElevation() != null)
        {
            writer.write(String.format(" ele=\"%s\"",
                                       node.getElevation()));
        }

        final List<Object> attributes = node.getAttributes();

        // TODO should it be an error if these values don't match? attribute names and values should be 1:1
        final int attributeCount = Math.min(attributes.size(),
                                            nodeAttributeNames.size());



        for(int x = 0; x < attributeCount; ++x)
        {
            writer.write(String.format(" %s=\"%s\"",
                                       nodeAttributeNames.get(x),
                                       attributes.get(x)));
        }

        writer.write("/>\n");
    }

    private static void writeWay(final Writer       writer,
                                 final Edge         edge,
                                 final List<String> edgeAttributeNames) throws IOException
    {
        writer.write(String.format("  <way id=\"%d\">\n",
                                   edge.getIdentifier()));

        writer.write(String.format("    <nd ref=\"%d\"/>\n",
                                   edge.getFrom()));

        writer.write(String.format("    <nd ref=\"%d\"/>\n",
                                   edge.getTo()));

        final List<Object> attributes = edge.getAttributes();

        // TODO should it be an error if these values don't match? attribute names and values should be 1:1
        final int attributeCount = Math.min(attributes.size(),
                                            edgeAttributeNames.size());

        for(int x = 0; x < attributeCount; ++x)
        {
            writer.write(String.format("    <tag k=\"%s\" v=\"%s\"/>\n",
                                       edgeAttributeNames.get(x),
                                       attributes.get(x)));
        }

        writer.write("  </way>\n");
    }

    private static void writeOsmXmlFooter(final Writer writer) throws IOException
    {
        writer.write("</osm>");
    }

    private static double distance(final double fromLatitude, final double fromLongitude, final double toLatitude, final double toLongitude)
    {
        final double dLat = Math.toRadians(toLatitude - fromLatitude);
        final double dLon = Math.toRadians(toLongitude - fromLongitude);
        final double fromLatitudeRadians = Math.toRadians(fromLatitude);
        final double toLatitudeRadians = Math.toRadians(toLatitude);
        final double a = haversin(dLat) + StrictMath.cos(fromLatitudeRadians) * StrictMath.cos(toLatitudeRadians) * haversin(dLon);
        final double c = 2.0D * StrictMath.atan2(Math.sqrt(a), Math.sqrt(1.0D - a));
        return 6372.8D * c;
    }

    private static double haversin(final double value)
    {
        return StrictMath.pow(StrictMath.sin(value / 2.0D), 2.0D);
    }

    private final File                      osmXmlFile;
    private final String                    description;
    private final BoundingBox               bounds;
    private final CoordinateReferenceSystem coordinateReferenceSystem;

    private static final String  XML_VERSION = "1.0";
    private static final String  OSM_VERSION = "0.6";
    private static final String  GENERATOR   = "SWAGD OsmXmlRoutingNetworkStoreWriter";
    private static final Charset ENCODING    = Charset.forName("UTF-8");
}
