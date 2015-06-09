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
import java.util.stream.Stream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

@SuppressWarnings({ "javadoc", "unused" })
public class GenerateGeoPackages
{
    //Files for geoPackage1
    private static final File geoPackageFile  = new File("contour.1.gpkg");
    private static final File nodeFile        = new File("data/contour.1/contour.1.node");
    private static final File edgeFile        = new File("data/contour.1/contour.1.edge");

    //Files for geoPackage2
    private static final File geoPackageFile2 = new File("usma_pandolf.gpkg");
    private static final File dataFile2       = new File("data/usma_pandolf.sqlite");
    private static final File nodes2          = new File("data/usma_pandolf_node_geometries.txt");

    //Files for geoPackage3
    private static final File geoPackageFile3 = new File("mwtc_pandolf.gpkg");
    private static final File dataFile3       = new File("data/mwtc_pandolf.sqlite");
    private static final File nodes3          = new File("data/mwtc_pandolf_node_geometries.txt");

    public static void main(final String[] args)
    {
        createGpkg();
        createGpkg2(geoPackageFile2, dataFile2, nodes2);
        createGpkg2(geoPackageFile3, dataFile3, nodes3);
    }


    @SuppressWarnings("hiding")
    private static void createGpkg2(final File geoPackageFile, final File dataFile, final File nodes)
    {
        try
        {
            Class.forName("org.sqlite.JDBC"); //Register the driver
        }
        catch (final ClassNotFoundException e2)
        {
            e2.printStackTrace();
        }

        try(final Connection db = DriverManager.getConnection("jdbc:sqlite:" + dataFile.getPath()))// Initialize the database connection
        {
            if(geoPackageFile.exists())
            {
                geoPackageFile.delete();
            }

            try (final GeoPackage gpkg = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Create))
            {
                final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

                final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                      "Super Important Routing Stuff",
                                                                      "routing stuff. super important",
                                                                      new BoundingBox(0, 0, 0, 0),
                                                                      gpkg.core().getSpatialReferenceSystem(-1));

                final AttributeDescription slopeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "slope",
                                                                                                     "meters(?)",
                                                                                                     DataType.Real,
                                                                                                     "slope",
                                                                                                     AttributedType.Edge);

                final AttributeDescription lengthAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                      "length",
                                                                                                      "meters(?)",
                                                                                                      DataType.Real,
                                                                                                      "length",
                                                                                                      AttributedType.Edge);

                final AttributeDescription pandolfCostAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                           "cost_pandolf",
                                                                                                           "unknown",
                                                                                                           DataType.Real,
                                                                                                           "caloric cost walking?",
                                                                                                           AttributedType.Edge);

                final AttributeDescription elevationAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                         "elev",
                                                                                                         "meters(?)",
                                                                                                         DataType.Real,
                                                                                                         "elevation",
                                                                                                         AttributedType.Node);

                final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                       "longitude",
                                                                                                       "degrees",
                                                                                                        DataType.Real,
                                                                                                        "longitude",
                                                                                                        AttributedType.Node);

                final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                        "latitude",
                                                                                                        "degrees",
                                                                                                        DataType.Real,
                                                                                                        "latitude",
                                                                                                        AttributedType.Node);
                // Add the attributed edges
                final String query = String.format("Select %s, %s, %s, %s, %s FROM %s", "from_node", "to_node", "slope", "length", "cost_pandolf", "edges");
                try(final PreparedStatement stmt =  db.prepareStatement(query))
                {
                    try(ResultSet results = stmt.executeQuery())
                    {
                        loadAttributedEdges(networkExtension,
                                            results,
                                            myNetwork,
                                            slopeAttribute,
                                            lengthAttribute,
                                            pandolfCostAttribute);
                    }
                }

                //Add attributed nodes
                loadNodeAttributes2(networkExtension,
                                    nodes,
                                    myNetwork,
                                    elevationAttribute,
                                    longitudeAttribute,
                                    latitudeAttribute);

            }
            catch (ConformanceException | IOException | BadImplementationException | ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        catch (final SQLException e1)
        {
            e1.printStackTrace();
        }
    }

    private static void createGpkg()
    {
        if(geoPackageFile.exists())
        {
            geoPackageFile.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Create))
        {

            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                  "Super Important Routing Stuff",
                                                                  "routing stuff. super important",
                                                                  new BoundingBox(0, 0, 0, 0),
                                                                  gpkg.core().getSpatialReferenceSystem(-1));

            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "longitude",
                                                                                                     "degrees",
                                                                                                     DataType.Real,
                                                                                                     "longitude",
                                                                                                     AttributedType.Node);

            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "latitude",
                                                                                                    "degrees",
                                                                                                    DataType.Real,
                                                                                                    "latitude",
                                                                                                    AttributedType.Node);

            final AttributeDescription lengthAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                  "length",
                                                                                                  "degrees",
                                                                                                  DataType.Real,
                                                                                                  "distance",
                                                                                                  AttributedType.Edge);

            loadNodeAttributes(networkExtension,
                               nodeFile,
                               myNetwork,
                               longitudeAttribute,
                               latitudeAttribute);

            loadEdges(networkExtension,
                      edgeFile,
                      myNetwork);

            calculateDistanceCost(networkExtension,
                                  myNetwork,
                                  lengthAttribute,
                                  longitudeAttribute,
                                  latitudeAttribute);
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void calculateDistanceCost(final GeoPackageNetworkExtension networkExtension,
                                              final Network                    network,
                                              final AttributeDescription       distanceDescription,
                                              final AttributeDescription       longitudeDescription,
                                              final AttributeDescription       latitudeDescription) throws SQLException
    {
        networkExtension.visitEdges(network,
                                    edge -> { try
                                              {
                                                  final List<List<Object>> values = networkExtension.getNodeAttributes(Arrays.asList(edge.getFrom(), edge.getTo()),
                                                                                                                       longitudeDescription,
                                                                                                                       latitudeDescription);

                                                  final List<Object> startCoordinate = values.get(0);
                                                  final List<Object> endCoordinate   = values.get(1);

                                                  final double longitude = (Double)endCoordinate.get(0) - (Double)startCoordinate.get(0);
                                                  final double latitude  = (Double)endCoordinate.get(1) - (Double)startCoordinate.get(1);

                                                  final double distance = Math.sqrt(latitude*latitude + longitude*longitude);

                                                  networkExtension.updateEdgeAttributes(edge,
                                                                                        Arrays.asList(distance),
                                                                                        distanceDescription);
                                              }
                                              catch(final Exception ex)
                                              {
                                                  ex.printStackTrace();
                                              }
                                            });
    }

    /**
     * Puts a file in the Triangle utility node format
     * (https://www.cs.cmu.edu/~quake/triangle.node.html) into a network
     */
    private static void loadNodeAttributes(final GeoPackageNetworkExtension networkExtension,
                                           final File                        triangleFormatNodes,
                                           final Network                     network,
                                           final AttributeDescription...     attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                                   return new Pair<>(Integer.valueOf(pieces[0]),                        // vertex # (node id)
                                                                                                     Arrays.asList((Object)Double.valueOf(pieces[1]),   // x (longitude)
                                                                                                                   (Object)Double.valueOf(pieces[2]))); // y (latitude)
                                                                                 };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(triangleFormatNodes.toPath())
                                                                                       .skip(1) // the first line is a header
                                                                                       .filter(line -> !line.startsWith("#"))
                                                                                       .map(lineToPair))
        {
            networkExtension.addNodes(pairs::iterator,
                                      attributeDescriptions);
        }
    }

    /**
     * Takes nodes in text file and adds them to
     */
    private static void loadNodeAttributes2(final GeoPackageNetworkExtension networkExtension,
                                            final File nodes,
                                            final Network network,
                                            final AttributeDescription... attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                                      return new Pair<>(Integer.valueOf(pieces[0]), // vertex # (node id)
                                                                                                        Arrays.asList((Object)Double.valueOf(pieces[1]),   // elevation
                                                                                                                      (Object)Double.valueOf(pieces[2]),   // X (longitude)
                                                                                                                      (Object)Double.valueOf(pieces[3]))); // y (latitude)
       };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(nodes.toPath())
                                                                         .skip(1) // the first line is a header
                                                                         .filter(line -> !line.startsWith("#"))
                                                                         .map(lineToPair))
        {
            networkExtension.addNodes(pairs::iterator,
                                      attributeDescriptions);
        }
    }

    private static void loadEdges(final GeoPackageNetworkExtension networkExtension, final File triangleFormatEdges, final Network network) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, Integer>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                              // Integer.valueOf(pieces[0]),                 // edge # (edge id), unused, we use our own id, but it should be the same in most cases

                                                                              return new Pair<>(Integer.valueOf(pieces[1]),  // from node
                                                                                                Integer.valueOf(pieces[2])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }

        // Now add the links in reverse (i.e., we've added one direction, A->B, now add B->A, since the original data had no directionality
        final Function<String, Pair<Integer, Integer>> lineToPair2 = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                               return new Pair<>(Integer.valueOf(pieces[2]),  // from node
                                                                                                 Integer.valueOf(pieces[1])); // to node
                                                                            };

        try(final Stream<Pair<Integer, Integer>> pairs = Files.lines(triangleFormatEdges.toPath())
                                                                                        .skip(1) // the first line is a header
                                                                                        .filter(line -> !line.startsWith("#"))
                                                                                        .map(lineToPair2))
        {
            networkExtension.addEdges(network, pairs::iterator);
        }
    }

    private static void loadAttributedEdges(final GeoPackageNetworkExtension networkExtension,
                                            final ResultSet                  rs,
                                            final Network                    network,
                                            final AttributeDescription...    attributeDescriptions) throws SQLException
    {
        final List<Pair<Pair<Integer, Integer>, List<Object>>> edges = new ArrayList<Pair<Pair<Integer, Integer>, List<Object>>>();

        Pair<Integer, Integer> pair;
        while(rs.next())
        {
            pair = new Pair<Integer, Integer>(rs.getInt(1), rs.getInt(2));       // from node, and to node
            edges.add(new Pair<>(pair, Arrays.asList((Object)rs.getDouble(3),    // slope
                                                     (Object)rs.getDouble(4),    // length
                                                     (Object)rs.getDouble(5)))); // pandolf cost
        }

        networkExtension.addAttributedEdges(edges, attributeDescriptions);
    }

//    private String firstLine(final File file) throws IOException
//    {
//        try(Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.US_ASCII))
//        {
//            return lines.findFirst().orElseThrow(() -> new IOException("Empty file"));
//        }
//    }
}
