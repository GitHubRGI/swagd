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

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({ "javadoc", "unused" })
public final class GenerateRoutingNetworks
{
    private static final Pattern SpacePattern  = Pattern.compile("\\s+");

    private GenerateRoutingNetworks()
    {
        // hide the constructor
    }

    public static void main(final String[] args)
    {
        final File geoPackageFile = new File("routing_networks.gpkg");

        if(geoPackageFile.exists())
        {
            geoPackageFile.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(geoPackageFile, VerificationLevel.None, GeoPackage.OpenMode.Create))
        {

            final GeoPackageRoutingExtension routingExtension = gpkg.extensions()
                                                                    .getExtensionImplementation(GeoPackageRoutingExtension.class);

            final GeoPackageNetworkExtension networkExtension = routingExtension.getNetworkExtension();

            loadContourDataset(gpkg,
                               networkExtension,
                               routingExtension);

            loadSqliteDataset("usma_pandolf",
                              gpkg,
                              networkExtension,
                              routingExtension);

            loadSqliteDataset("mwtc_pandolf",
                              gpkg,
                              networkExtension,
                              routingExtension);

        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void loadSqliteDataset(final String                     name,
                                          final GeoPackage                 geoPackage,
                                          final GeoPackageNetworkExtension networkExtension,
                                          final GeoPackageRoutingExtension routingExtension) throws IOException, ClassNotFoundException, SQLException
    {
        Class.forName("org.sqlite.JDBC");

        final File databaseFile = new File("data/" + name + ".sqlite");

        try(final Connection db = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath()))
        {
            final File nodesFile = new File("data/" + name + "_node_geometries.txt");

            final Network network = networkExtension.addNetwork(name,
                                                                  name,
                                                                  name + " dataset provided by Matt Renner of AGC",
                                                                  getBoundingBox(nodesFile, 2, 3),
                                                                  geoPackage.core().getSpatialReferenceSystem(-1));

            final AttributeDescription slopeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                 "slope",
                                                                                                 "meters",
                                                                                                 DataType.Real,
                                                                                                 "slope",
                                                                                                 AttributedType.Edge);

            final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(network,
                                                                                                    "distance",
                                                                                                    "meters",
                                                                                                    DataType.Real,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            final AttributeDescription pandolfCostAttribute = networkExtension.addAttributeDescription(network,
                                                                                                       "cost_pandolf",
                                                                                                       "kcal",
                                                                                                       DataType.Real,
                                                                                                       "caloric cost walking",
                                                                                                       AttributedType.Edge);

            final AttributeDescription elevationAttribute = networkExtension.addAttributeDescription(network,
                                                                                                     "elevation",
                                                                                                     "meters",
                                                                                                     DataType.Real,
                                                                                                     "elevation",
                                                                                                     AttributedType.Node);

            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                   "longitude",
                                                                                                   "degrees",
                                                                                                    DataType.Real,
                                                                                                    "longitude",
                                                                                                    AttributedType.Node);

            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                    "latitude",
                                                                                                    "degrees",
                                                                                                    DataType.Real,
                                                                                                    "latitude",
                                                                                                    AttributedType.Node);
            // Add the attributed edges
            final String query = String.format("Select %s, %s, %s, %s, %s FROM %s",
                                               "from_node",
                                               "to_node",
                                               "slope",
                                               "length",
                                               "cost_pandolf",
                                               "edges");

            try(final PreparedStatement stmt = db.prepareStatement(query))
            {
                try(ResultSet results = stmt.executeQuery())
                {
                    loadAttributedEdges(networkExtension,
                                        results,
                                        network,
                                        slopeAttribute,
                                        distanceAttribute,
                                        pandolfCostAttribute);
                }
            }

            // Add attributed nodes
            loadNodeAttributes(networkExtension,
                               nodesFile,
                               network,
                               elevationAttribute,
                               longitudeAttribute,
                               latitudeAttribute);

            routingExtension.addRoutingNetworkDescription(network,
                                                          longitudeAttribute,
                                                          latitudeAttribute,
                                                          distanceAttribute);

        }
    }

    private static void loadContourDataset(final GeoPackage                 geoPackage,
                                           final GeoPackageNetworkExtension networkExtension,
                                           final GeoPackageRoutingExtension routingExtension) throws SQLException, IOException
    {
        final File nodeFile = new File("data/contour.1/contour.1.node");

        final Network network = networkExtension.addNetwork("contour_1",
                                                            "contour_1",
                                                            "contour.1 dataset provided by Matt Renner of AGC",
                                                            getBoundingBox(nodeFile, 1, 2),
                                                            geoPackage.core().getSpatialReferenceSystem(-1));

        final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                 "longitude",
                                                                                                 "degrees",
                                                                                                 DataType.Real,
                                                                                                 "longitude",
                                                                                                 AttributedType.Node);

        final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(network,
                                                                                                "latitude",
                                                                                                "degrees",
                                                                                                DataType.Real,
                                                                                                "latitude",
                                                                                                AttributedType.Node);

        final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(network,
                                                                                                "distance",
                                                                                                "degrees",
                                                                                                DataType.Real,
                                                                                                "distance",
                                                                                                AttributedType.Edge);



        loadNodeAttributes(networkExtension,
                           nodeFile,
                           network,
                           longitudeAttribute,
                           latitudeAttribute);

        final File edgeFile = new File("data/contour.1/contour.1.edge");

        loadEdges(networkExtension,
                  edgeFile,
                  network);

        calculateDistanceCost(networkExtension,
                              network,
                              distanceAttribute,
                              longitudeAttribute,
                              latitudeAttribute);

        routingExtension.addRoutingNetworkDescription(network,
                                                      longitudeAttribute,
                                                      latitudeAttribute,
                                                      distanceAttribute);
    }

    private static void calculateDistanceCost(final GeoPackageNetworkExtension networkExtension,
                                              final Network                    network,
                                              final AttributeDescription       distanceDescription,
                                              final AttributeDescription       longitudeDescription,
                                              final AttributeDescription       latitudeDescription) throws SQLException
    {
        networkExtension.visitEdges(network, edge -> { try
                                                       {
                                                           final List<List<Object>> values = networkExtension.getNodeAttributes(Arrays.asList(edge.getFrom(), edge.getTo()), longitudeDescription, latitudeDescription);

                                                           final List<Object> startCoordinate = values.get(0);
                                                           final List<Object> endCoordinate = values.get(1);

                                                           final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                           final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                           final double distance = Math.sqrt(latitude * latitude + longitude * longitude);

                                                           networkExtension.updateEdgeAttributes(edge, Arrays.asList(distance), distanceDescription);
                                                       }
                                                       catch(final Exception ex)
                                                       {
                                                           ex.printStackTrace();
                                                       }
                                                     });
    }

    private static BoundingBox getBoundingBox(final File triangleFormatNodes,
                                              final int  longitudeIndex,
                                              final int  latitudeIndex) throws IOException
    {
        final double[] bbox = { Double.MAX_VALUE, // x min
                                Double.MAX_VALUE, // y min
                                Double.MIN_VALUE, // x max
                                Double.MIN_VALUE  // y max
                              };

        Files.lines(triangleFormatNodes.toPath())
             .skip(1L) // the first line is a header
             .filter(line -> !line.startsWith("#"))
             .forEach(line -> { final String[] pieces = SpacePattern.split(line.trim());

                                double x = Double.valueOf(pieces[longitudeIndex]);
                                double y = Double.valueOf(pieces[latitudeIndex]);

                                if(x < bbox[0])
                                {
                                    bbox[0] = x;
                                }

                                if(x > bbox[2])
                                {
                                    bbox[2] = x;
                                }

                                if(y < bbox[1])
                                {
                                    bbox[1] = y;
                                }

                                if(y > bbox[3])
                                {
                                    bbox[3] = y;
                                }
                              });

        return new BoundingBox(bbox[0],
                               bbox[1],
                               bbox[2],
                               bbox[3]);
    }

    /**
     * Puts a file in the Triangle utility node format
     * (https://www.cs.cmu.edu/~quake/triangle.node.html) into a network
     */
    private static void loadNodeAttributes(final GeoPackageNetworkExtension networkExtension,
                                           final File                       triangleFormatNodes,
                                           final Network                    network,
                                           final AttributeDescription...    attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = SpacePattern.split(line.trim());

                                                                                   return new Pair<>(Integer.valueOf(pieces[0]),                // vertex # (node id)
                                                                                                     Arrays.asList(pieces)
                                                                                                           .stream()
                                                                                                           .skip(1L)    // Skip the first element, the vertex number/node id
                                                                                                           .limit(attributeDescriptions.length)
                                                                                                           .map(Double::valueOf)
                                                                                                           .collect(Collectors.toList()));
                                                                                 };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(triangleFormatNodes.toPath())
                                                                                       .skip(1L) // the first line is a header
                                                                                       .filter(line -> !line.startsWith("#"))
                                                                                       .map(lineToPair))
        {
            networkExtension.addNodes(pairs::iterator,
                                      attributeDescriptions);
        }
    }

    private static void loadEdges(final GeoPackageNetworkExtension networkExtension,
                                  final File                       triangleFormatEdges,
                                  final Network                    network) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, Integer>> lineToPair = line -> { final String[] pieces = SpacePattern.split(line.trim());

                                                                              // Integer.valueOf(pieces[0]),                 // edge # (edge id), unused, we use our own id, but it should be the same in most cases

                                                                              return new Pair<>(Integer.valueOf(pieces[1]),  // from node
                                                                                                Integer.valueOf(pieces[2])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1L) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }

        // Now add the links in reverse (i.e., we've added one direction, A->B, now add B->A, since the original data had no directionality
        final Function<String, Pair<Integer, Integer>> lineToPair2 = line -> { final String[] pieces = SpacePattern.split(
                line.trim());

                                                                               return new Pair<>(Integer.valueOf(pieces[2]),  // from node
                                                                                                 Integer.valueOf(pieces[1])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1L) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair2))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }
    }

    private static void loadAttributedEdges(final GeoPackageNetworkExtension networkExtension,
                                            final ResultSet                  resultSet,
                                            final Network                    network,
                                            final AttributeDescription...    attributeDescriptions) throws SQLException
    {
        final List<Pair<Pair<Integer, Integer>, List<Object>>> edges = new ArrayList<>();

        while(resultSet.next())
        {
            edges.add(Pair.of(Pair.of(resultSet.getInt(1),  // from node
                                      resultSet.getInt(2)), // to node
                              Arrays.asList(resultSet.getDouble(3),        // slope
                                            resultSet.getDouble(4),        // distance
                                            resultSet.getDouble(5))));     // pandolf cost
        }

        networkExtension.addAttributedEdges(edges, attributeDescriptions);
    }
}
