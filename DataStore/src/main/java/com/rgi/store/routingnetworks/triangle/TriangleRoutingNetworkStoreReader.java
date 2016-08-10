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

package com.rgi.store.routingnetworks.triangle;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import com.rgi.store.routingnetworks.osm.NodeDimensionality;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>A reader for ".node" and ".edge" files that are the outputs of
 * <a href="https://www.cs.cmu.edu/~quake/triangle.html">Triangle</a>, a
 * two-dimensional mesh generator and Delaunay triangulator. At the time of
 * writing, it's not certain if the Triangle-associated file formats originated
 * there, or if they're shared with systems.</p>
 *
 * <p>The Triangle program outputs the following file types, depending on what
 * <a href="https://www.cs.cmu.edu/~quake/triangle.switch.html">input
 * parameters</a> it was executed with:</p>
 *
 * <ul>
 *     <li>
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.node.html">.node</a>
 *         - Describes the nodes of a mesh
 *     </li>
 *     <li>
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.ele.html">.ele</a> -
 *         Describes the properties of <b>ele</b>ments (triangles) in a mesh
 *     </li>
 *     <li>
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.poly.html">.poly</a>
 *         - Represents a
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.defs.html#pslg">PSLG
 *        (Planar Straight Line Graph)</a></li> in addition to information
 *        about holes and concavities, as well as regional attributes and
 *        constraints on the areas of triangles
 *     </li>
 *     <li>
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.area.html" >.area
 *         </a> - Associates each triangle with a maximum area that is used for
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.refine.html">mesh
 *         refinement</a>
 *     </li>
 *     <li>
 *         <a href="https://www.cs.cmu.edu/~quake/triangle.edge.html" >.edge
 *         </a> - Describes the edges of a mesh
 *     </li>
 *     <li><a href="https://www.cs.cmu.edu/~quake/triangle.neigh.html">.neigh
 *         </a> - Associates each element (triangle) with its <b>neigh</b>bors
 *     </li>
 * </ul>
 *
 * <p>
 *     <b>For the purposes of network generation, only the .node and .edge
 *     files are used.</b> The documentation for these two file formats will
 *     be reproduced below in case the original documentation becomes
 *     inaccessible:
 * </p>
 *
 * <blockquote cite="https://www.cs.cmu.edu/~quake/triangle.node.html">
 *     <h2>.node files</h2>
 *     <ul>
 *         <li>
 *             First line:  &lt;# of vertices&gt; &lt;dimension (must be 2)&gt;
 *             &lt;# of attributes&gt; &lt;# of boundary markers (0 or 1)&gt;
 *         </li>
 *         <li>
 *             Remaining lines:  &lt;vertex #&gt; &lt;x&gt; &lt;y&gt;
 *             [attributes] [boundary marker]
 *         </li>
 *     </ul>
 *     <p>
 *         Blank lines and comments prefixed by `#' may be placed anywhere.
 *         Vertices must be numbered consecutively, starting from one or zero.
 *     </p>
 *     <p>
 *         The attributes, which are typically floating-point values of
 *         physical quantities (such as mass or conductivity) associated with
 *         the nodes of a finite element mesh, are copied unchanged to the
 *         output mesh.  If <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.q.html">-q</a>, <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.a.html">-a</a>, -u, or -s is
 *         selected, each new <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.defs.html#steiner">Steiner
 *         point</a> added to the mesh will have quantities assigned to it by
 *         linear interpolation.
 *     </p>
 *     <p>
 *         If the fourth entry of the first line is `1', the last column of the
 *         remainder of the file is assumed to contain boundary markers. <a
 *         href="https://www.cs.cmu.edu/~quake/triangle.markers.html">Boundary
 *         markers</a> are used to identify boundary vertices and vertices
 *         resting on <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.defs.html#pslg">PSLG</a> <a
 *         href="https://www.cs.cmu.edu/~quake/triangle.defs.html#segment">
 *         segments</a>. The .node files produced by Triangle contain boundary
 *         markers in the last column unless they are suppressed by the -B switch.
 *     </p>
 * </blockquote>
 *
 * <p>
 *     <b>Note:</b> There is no description of the node attributes. This
 *     information will either be user-supplied or automatically generated.
 * </p>
 *
 * <blockquote cite="https://www.cs.cmu.edu/~quake/triangle.edge.html">
 *     <h2>.edge files</h2>
 *     <ul>
 *         <li>
 *             First line:  &lt;# of edges&gt; &lt;# of boundary markers (0 or
 *             1)&gt;
 *         </li>
 *         <li>
 *             Following lines:  &lt;edge #&gt; &lt;endpoint&gt; &lt;endpoint
 *             &gt; [boundary marker]
 *         </li>
 *     </ul>
 *     <p>
 *         Blank lines and comments prefixed by `#' may be placed anywhere.
 *         Edges are numbered consecutively, starting from one or zero.
 *         Endpoints are indices into the corresponding <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.node.html">.node</a> file.
 *     </p>
 *     <p>
 *         Triangle can produce .edge files (use the -e switch), but cannot
 *         read them. The optional column of <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.markers.html"> boundary
 *         markers</a> is suppressed by the -B switch.
 *     </p>
 *     <p>
 *         In <a href=
 *         "https://www.cs.cmu.edu/~quake/triangle.defs.html#voronoi">Voronoi
 *         diagrams</a>, one also finds a special kind of edge that is an
 *         infinite ray with only one endpoint.  For these edges, a different
 *         format is used:
 *     </p>
 *     <ul>
 *         <li>
 *             &lt;edge #&gt; &lt;endpoint&gt; -1 &lt;direction x&gt; &lt;direction y&gt;
 *         </li>
 *     </ul>
 *
 *     <p>
 *         The `direction' is a floating-point vector that indicates the
 *         direction of the infinite ray.
 *     </p>
 * </blockquote>
 *
 * <p>
 *     <b>Note:</b> There is no concept of node attributes.
 * </p>
 *
 * @author Luke Lambert
 */
public class TriangleRoutingNetworkStoreReader implements RoutingNetworkStoreReader
{
    /**
     * Constructor
     *
     * @param nodeFile
     *             The ".node" file, containing the triangle node data
     * @param edgeFile
     *             The ".edge" file, containing the triangle edge data
     * @param elevationAttributeIndex
     *             The index (0-based) of the elevation attribute. A negative
     *             value indicates this set of nodes is two dimensional.
     * @param nodeAttributeDescriptions
     *             Name/type pairs that describe the attributes. This
     *             collection should not contain a description for the
     *             elevation attribute specified by the
     *             elevationAttributeIndex. Description order corresponds to
     *             order the attributes appear in the nodes file, omitting the
     *             elevation attribute.
     * @param coordinateReferenceSystem
     *             Coordinate reference system of the data
     * @throws IOException
     *             Throws if there is an issue with file reading
     * @throws RoutingNetworkStoreException
     *             Throws if there is problem parsing the .node and .edge files
     */
    public TriangleRoutingNetworkStoreReader(final File                      nodeFile,
                                             final File                      edgeFile,
                                             final int                       elevationAttributeIndex,
                                             final List<Pair<String, Type>>  nodeAttributeDescriptions,
                                             final CoordinateReferenceSystem coordinateReferenceSystem) throws IOException, RoutingNetworkStoreException
    {
        this.nodeFile                  = nodeFile;
        this.edgeFile                  = edgeFile;
        this.elevationAttributeIndex   = elevationAttributeIndex;
        this.nodeFileHeader            = NodeFileHeader.from(nodeFile);
        this.edgeFileHeader            = EdgeFileHeader.from(edgeFile);
        this.nodeAttributeDescriptions = new ArrayList<>(nodeAttributeDescriptions);
        this.coordinateReferenceSystem = coordinateReferenceSystem;

        final int nonElevationAttributes = this.nodeFileHeader.getAttributeCount() - (elevationAttributeIndex > 0 ? 1 : 0);

        if(this.nodeAttributeDescriptions.size() != nonElevationAttributes)
        {
            throw new IllegalArgumentException(String.format("Expected %d node attribute description, but got %d",
                                                             nonElevationAttributes,
                                                             this.nodeAttributeDescriptions.size()));
        }

        this.nodes = this.parseNodes();

        this.bounds = calculateBounds(this.nodes);

        this.edges = this.parseEdges();
    }

    @Override
    public List<Pair<String, Type>> getNodeAttributeDescriptions() throws RoutingNetworkStoreException
    {
        return Collections.unmodifiableList(this.nodeAttributeDescriptions);
    }

    @Override
    public List<Pair<String, Type>> getEdgeAttributeDescriptions() throws RoutingNetworkStoreException
    {
        return Collections.emptyList(); // .edge files contain no attributes
    }

    @Override
    public List<Node> getNodes() throws RoutingNetworkStoreException
    {
        return Collections.unmodifiableList(this.nodes);
    }

    @Override
    public List<Edge> getEdges()
    {
        return Collections.unmodifiableList(this.edges);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() throws RoutingNetworkStoreException
    {
        return this.coordinateReferenceSystem;
    }

    @Override
    public BoundingBox getBounds()
    {
        return this.bounds;
    }

    @Override
    public String getDescription()
    {
        return String.format("Triangle network as described by %s and %s. Contains %d nodes and %d edges.",
                             this.nodeFile.getName(),
                             this.edgeFile.getName(),
                             this.nodes.size(),
                             this.edges.size());
    }

    @Override
    public NodeDimensionality getNodeDimensionality() throws RoutingNetworkStoreException
    {
        return this.elevationAttributeIndex < 0
                                            ? NodeDimensionality.NoElevation
                                            : NodeDimensionality.HasElevation;
    }

    private List<Node> parseNodes() throws RoutingNetworkStoreException
    {
        try
        {
            final List<Node> nodes = Files.lines(this.nodeFile.toPath())
                                          .skip(this.nodeFileHeader.getLineNumber()+1)                    // Skip past the header line
                                          .filter(line -> !TRIANGLE_NO_DATA_LINE.matcher(line).matches()) // Skip empty lines, and comments
                                          .map(line -> this.nodeFileHeader.parse(line, this.elevationAttributeIndex))
                                          .collect(Collectors.toList());

            if(nodes.size() != this.nodeFileHeader.getNodeCount())
            {
                throw new RoutingNetworkStoreException(String.format("Node file header reports a node count of %d, but the file contains %d nodes",
                                                                     this.nodeFileHeader.getNodeCount(),
                                                                     nodes.size()));
            }

            return nodes;
        }
        catch(final RuntimeException | IOException ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }
    }

    private List<Edge> parseEdges() throws RoutingNetworkStoreException
    {
        try
        {
            final List<Edge> edges = Files.lines(this.edgeFile.toPath())
                                          .skip(this.edgeFileHeader.getLineNumber()+1)                    // Skip past the header line
                                          .filter(line -> !TRIANGLE_NO_DATA_LINE.matcher(line).matches()) // Skip empty lines, and comments
                                          .map(this.edgeFileHeader::parse)
                                          .collect(Collectors.toList());

            if(edges.size() != this.edgeFileHeader.getEdgeCount())
            {
                throw new RoutingNetworkStoreException(String.format("Node file header reports an edge count of %d, but the file contains %d edges",
                                                                     this.edgeFileHeader.getEdgeCount(),
                                                                     edges.size()));
            }

            return edges;
        }
        catch(final RuntimeException | IOException ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }
    }

    private static BoundingBox calculateBounds(final Iterable<Node> nodes)
    {
        final double[] bbox = { Double.NaN, // x min
                                Double.NaN, // y min
                                Double.NaN, // x max
                                Double.NaN  // y max
                              };

        nodes.forEach(node -> { final double x = node.getX();
                                final double y = node.getY();

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

    private final File                      nodeFile;
    private final File                      edgeFile;
    private final NodeFileHeader            nodeFileHeader;
    private final EdgeFileHeader            edgeFileHeader;
    private final int                       elevationAttributeIndex;
    private final List<Pair<String, Type>>  nodeAttributeDescriptions;
    private final CoordinateReferenceSystem coordinateReferenceSystem;

    private final List<Node>  nodes;
    private final List<Edge>  edges;
    private final BoundingBox bounds;

    static final Pattern TRIANGLE_NO_DATA_LINE = Pattern.compile("(\\s*(#.*)?)?$");
}
