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

package com.rgi.routingnetworks.dem;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.EdgeDirecctionality;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.NodeDimensionality;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.ProgressCallback;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import utility.GdalError;
import utility.GdalUtility;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author Luke Lambert
 */
public class DemRoutingNetworkStoreReader implements RoutingNetworkStoreReader
{
    /**
     * Constructor
     *
     * @param file                            File containing the digital elevation model dataset
     * @param rasterBand                      Band of the raster to treat as elevation data
     * @param contourElevationInterval        Contour Elevation interval (elevation values will be multiples of the interval)
     * @param noDataValue                     Value that indicates that a pixel contains no elevation data, and is to be ignored (nullable)
     * @param coordinatePrecision             Number of decimal places to round the coordinates. A negative value will cause no rounding to occur
     * @param simplificationTolerance         Tolerance used to simplify the contour rings that are used in the triangulation of the data
     * @param triangulationTolerance          Snaps points that are within a tolerance's distance from one another (we THINK)
     * @param targetCoordinateReferenceSystem Coordinate system for all output
     * @throws RoutingNetworkStoreException thrown if the resulting network would contain invalid data
     */
    public DemRoutingNetworkStoreReader(final File                      file,
                                        final int                       rasterBand,
                                        final double                    contourElevationInterval,
                                        final Double                    noDataValue,
                                        final int                       coordinatePrecision,
                                        final double                    simplificationTolerance,
                                        final double                    triangulationTolerance,
                                        final CoordinateReferenceSystem targetCoordinateReferenceSystem) throws RoutingNetworkStoreException
    {
        this(file,
             rasterBand,
             contourElevationInterval,
             noDataValue,
             coordinatePrecision,
             simplificationTolerance,
             triangulationTolerance,
             targetCoordinateReferenceSystem,
             null);
    }

    /**
     * Constructor
     *
     * @param file                            File containing the digital elevation model dataset
     * @param rasterBand                      Band of the raster to treat as elevation data
     * @param contourElevationInterval        Contour Elevation interval (elevation values will be multiples of the interval)
     * @param noDataValue                     Value that indicates that a pixel contains no elevation data, and is to be ignored (nullable)
     * @param coordinatePrecision             Number of decimal places to round the coordinates. A negative value will cause no rounding to occur
     * @param simplificationTolerance         Tolerance used to simplify the contour rings that are used in the triangulation of the data
     * @param triangulationTolerance          Snaps points that are within a tolerance's distance from one another (we THINK)
     * @param targetCoordinateReferenceSystem Coordinate system for all output
     * @param progressCallback                Callback to observe process progress. Ignored if null.
     * @throws RoutingNetworkStoreException thrown if the resulting network would contain invalid data
     */
    public DemRoutingNetworkStoreReader(final File                      file,
                                        final int                       rasterBand,
                                        final double                    contourElevationInterval,
                                        final Double                    noDataValue,
                                        final int                       coordinatePrecision,
                                        final double                    simplificationTolerance,
                                        final double                    triangulationTolerance,
                                        final CoordinateReferenceSystem targetCoordinateReferenceSystem,
                                        final ProgressCallback          progressCallback) throws RoutingNetworkStoreException
    {

        this.coordinateReferenceSystem = targetCoordinateReferenceSystem;

        final SpatialReference targetSpatialReference;

        try
        {
            targetSpatialReference = GdalUtility.createSpatialReference(targetCoordinateReferenceSystem);
        }
        catch(final RuntimeException ex)
        {
            throw new RoutingNetworkStoreException(ex);
        }

        final Dataset dataset = GdalUtility.open(file);

        try
        {
            this.rasterWidth  = dataset.getRasterXSize();
            this.rasterHeight = dataset.getRasterYSize();

            final SpatialReference sourceSpatialReference = new SpatialReference(dataset.GetProjection());

            final DataSource dataSource = ogr.GetDriverByName("Memory") // Make constant
                                             .CreateDataSource("data source");

            final Geometry pointCollection = new Geometry(ogrConstants.wkbMultiPoint);

            try
            {
                final Layer outputLayer = dataSource.CreateLayer("contours",
                                                                 sourceSpatialReference);

                // http://www.gdal.org/gdal__alg_8h.html#aceaf98ad40f159cbfb626988c054c085
                final int gdalError = gdal.ContourGenerate(dataset.GetRasterBand(rasterBand),         // Band             srcBand                  - The band to read raster data from. The whole band will be processed
                                                           contourElevationInterval,                  // double           contourElevationInterval - The elevation interval between contours generated
                                                           0,                                         // double           contourBase              - The "base" relative to which contour intervals are applied. This is normally zero, but could be different. To generate 10m contours at 5, 15, 25, ... the ContourBase would be 5
                                                           null,                                      // double[]         fixedLevels              - The list of fixed contour levels at which contours should be generated. It will contain FixedLevelCount entries, and may be NULL
                                                           (noDataValue == null) ? 0   : 1,           // int              useNoData                - If TRUE the noDataValue will be used
                                                           (noDataValue == null) ? 0.0 : noDataValue, // double           noDataValue              - The value to use as a "no data" value. That is, a pixel value which should be ignored in generating contours as if the value of the pixel were not known
                                                           outputLayer,                               // Layer            dstLayer                 - The layer to which new contour vectors will be written. Each contour will have a LINESTRING geometry attached to it
                                                           -1,                                        // int              idField                  - If not -1 this will be used as a field index to indicate where a unique id should be written for each feature (contour) written
                                                           -1,                                        // int              elevField                - If not -1 this will be used as a field index to indicate where the elevation value of the contour should be written
                                                           progressCallback);                         // ProgressCallback callback                 - A ProgressCallback that may be used to report progress to the user, or to interrupt the algorithm. May be NULL if not required

                if(gdalError != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }

                // Render Contours (for debug purposes)
                renderContours(file.getName() + ".contours.tif",
                               this.rasterWidth,
                               this.rasterHeight,
                               GdalUtility.getBounds(dataset),
                               sourceSpatialReference,
                               outputLayer,
                               new Color(255, 255, 255, 0),
                               Color.BLACK);

                final CoordinateTransformation coordinateTransformation = CoordinateTransformation.CreateCoordinateTransformation(sourceSpatialReference,
                                                                                                                                  targetSpatialReference);

                for(Feature feature = outputLayer.GetNextFeature(); feature != null; feature = outputLayer.GetNextFeature())
                {
                    final Geometry originalGeometry = feature.GetGeometryRef();

                    // http://gdal.org/java/org/gdal/ogr/Geometry.html#SimplifyPreserveTopology(double) ->
                    // This function is built on the GEOS library, check it for the definition of the geometry operation. If OGR is built without the GEOS library, this function will always fail, issuing a CPLE_NotSupported error.
                    // http://geos.refractions.net/ro/doxygen_docs/html/classgeos_1_1simplify_1_1TopologyPreservingSimplifier.html ->
                    // All vertices in the simplified geometry will be within this distance of the original geometry. The tolerance value must be non-negative. A tolerance value of zero is effectively a no-op.
                    final Geometry simplifiedGeometry = originalGeometry.SimplifyPreserveTopology(simplificationTolerance); // https://gis.stackexchange.com/questions/102254/ogr-simplifypreservetopology-does-not-keep-the-topology
                                                                                                                            // Topology preserving means in practice that parts of the multilinestring meet after simplification, polygons
                                                                                                                            // do not have self-intersections, inner rings in polygons stay inside outer rings, etc. Especially for polygon
                                                                                                                            // layers this method does not prevent gaps, overlaps, and slivers from appearing, even though this is the
                                                                                                                            // general belief. I would say that the method has a misleading name which makes users to believe that it saves
                                                                                                                            // the topology for the whole layer. However, the name and behaviour is the same in PostGIS and in JTS
                                                                                                                            // http://www.tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/simplify/TopologyPreservingSimplifier.html

                    final int pointCount = simplifiedGeometry.GetPointCount();

                    for(int x = 0; x < pointCount; ++x)
                    {
                        final double[] point = simplifiedGeometry.GetPoint(x);

                        pointCollection.AddGeometry(warp(point[0],
                                                         point[1],
                                                         point[2],
                                                         coordinateTransformation,
                                                         coordinatePrecision));
                    }
                }
            }
            finally
            {
                dataSource.delete();    // Also destroys outputLayer
            }

            // http://www.gdal.org/classOGRGeometry.html#ab7d3c3e5b033ca6bbb470016e7661da7
            final Geometry triangulation = pointCollection.DelaunayTriangulation(triangulationTolerance, // double tolerance - optional snapping tolerance to use for improved robustness
                                                                                 1);                     // int    onlyEdges - if TRUE (non-zero), will return a MULTILINESTRING, otherwise it will return a GEOMETRYCOLLECTION containing triangular POLYGONs
            final double[] envelope = new double[4]; // minX, maxX, minY, maxY

            triangulation.GetEnvelope(envelope);

            this.bounds = new BoundingBox(envelope[0],
                                          envelope[2],
                                          envelope[1],
                                          envelope[3]);

            final int lineStringCount = triangulation.GetGeometryCount();

            for(int x = 0; x < lineStringCount; ++x)
            {
                final Geometry edge = triangulation.GetGeometryRef(x);    // Aline string that represents an edge in the Delaunay triangulation

                final Node node0 = this.getNode(edge.GetPoint(0));
                final Node node1 = this.getNode(edge.GetPoint(1));

                this.edges.add(new Edge(this.edges.size(),
                                        node0.getIdentifier(),
                                        node1.getIdentifier(),
                                        EdgeDirecctionality.TWO_WAY,
                                        Collections.emptyList()));
            }

            this.description = String.format("Elevation model routing network generated from source data %s, band %d. Contains %d nodes and %d edges. Created with parameters, contour interval: %s, pixel no data value: %s, contour simplification tolerance: %s, triangulation tolerance: %s.",
                                             file.getName(),
                                             rasterBand,
                                             this.nodes.size(),
                                             this.edges.size(),
                                             contourElevationInterval,
                                             noDataValue,
                                             simplificationTolerance,
                                             triangulationTolerance);
        }
        catch(final Throwable th)
        {
            throw new RoutingNetworkStoreException(th);
        }
        finally
        {
            dataset.delete();
        }
    }

    private static Geometry warp(final double                   x,
                                 final double                   y,
                                 final double                   z,
                                 final CoordinateTransformation coordinateTransformation,
                                 final int                      coordinatePrecision)
    {
        final Geometry pointGeometry = new Geometry(ogrConstants.wkbPoint);

        final double[] point = coordinateTransformation.TransformPoint(x, y, z);

        pointGeometry.AddPoint(round(point[0], coordinatePrecision),
                               round(point[1], coordinatePrecision),
                               round(point[2], coordinatePrecision));

        return pointGeometry;
    }

    private static void renderContours(final String           contoursFilePath,
                                       final int              imageWidth,
                                       final int              imageHeight,
                                       final BoundingBox      bounds,
                                       final SpatialReference spatialReference,
                                       final Layer            layer,
                                       final Color            background,
                                       final Color            foreground)
    {
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final Vector<String> imageCreationOptions = new Vector<>(1);

        imageCreationOptions.add("COMPRESS=LZW");

        final Dataset rasterDataset = gdal.GetDriverByName("GTiff")
                                          .Create(contoursFilePath,
                                                  imageWidth,
                                                  imageHeight,
                                                  4,    // RGBA
                                                  gdalconstConstants.GDT_Byte,
                                                  imageCreationOptions);

        try
        {
            rasterDataset.GetRasterBand(1).Fill(background.getRed());
            rasterDataset.GetRasterBand(2).Fill(background.getGreen());
            rasterDataset.GetRasterBand(3).Fill(background.getBlue());
            rasterDataset.GetRasterBand(4).Fill(background.getAlpha());

            rasterDataset.SetGeoTransform(new double[]{ bounds.getTopLeft().getX(),               // top left x
                                                        bounds.getWidth() / (double)imageWidth,   // w-e pixel resolution
                                                        0,                                        // rotation, 0 if image is "north up"
                                                        bounds.getTopLeft().getY(),               // top left y
                                                        0,                                        // rotation, 0 if image is "north up"
                                                        -bounds.getHeight() / (double)imageHeight // n-s pixel resolution (negative value!)
                                                      });

            rasterDataset.SetProjection(spatialReference.ExportToWkt());


            final int rasterizeError = gdal.RasterizeLayer(rasterDataset,
                                                           new int[]{1, 2, 3, 4},
                                                           layer,
                                                           new double[]{ foreground.getRed(),
                                                                         foreground.getGreen(),
                                                                         foreground.getBlue(),
                                                                         foreground.getAlpha()
                                                                       },
                                                           null,                    // "options" vector. valid choices are described here: http://gdal.org/gdal__alg_8h.html#adfe5e5d287d6c184aab03acbfa567cb1
                                                           null);

            if(rasterizeError != gdalconstConstants.CE_None)
            {
                throw new RuntimeException(new GdalError().getMessage());
            }
        }
        finally
        {
            rasterDataset.delete();
        }
    }

    private static double round(final double real,
                                final int    numberOfDecimalPlaces)
    {
        if(numberOfDecimalPlaces < 0)
        {
            return real;
        }

        return new BigDecimal(real).setScale(numberOfDecimalPlaces,
                                             RoundingMode.FLOOR)
                                   .doubleValue();
    }

    @Override
    public List<Pair<String, Type>> getNodeAttributeDescriptions()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Pair<String, Type>> getEdgeAttributeDescriptions()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Node> getNodes()
    {
        return Collections.unmodifiableList(this.nodes);
    }

    @Override
    public List<Edge> getEdges()
    {
        return Collections.unmodifiableList(this.edges);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
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
        return this.description;
    }

    @Override
    public NodeDimensionality getNodeDimensionality()
    {
        return NodeDimensionality.HAS_ELEVATION;
    }

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public int getRasterWidth()
    {
        return this.rasterWidth;
    }

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public int getRasterHeight()
    {
        return this.rasterHeight;
    }

    private Node getNode(final double[] coordinate)
    {
        final int coordinateHash = Arrays.hashCode(coordinate);

        final double longitude = coordinate[0];
        final double latitude  = coordinate[1];
        final Double elevation = coordinate.length > 2 ? coordinate[2] : null;

        final String key = Double.toString(longitude) + '_' + Double.toString(latitude) + '_' + (elevation != null ? Double.toString(elevation) : "");  // TODO this could be smarter...

        if(this.nodeMap.containsKey(key))
        {
            return this.nodeMap.get(key);
        }

        final Node node = new Node(this.nodes.size(),
                                   longitude,
                                   latitude,
                                   elevation,
                                   Collections.emptyList());

        this.nodes.add(node);
        this.nodeMap.put(key, node);

        return node;
    }

    private final List<Node>                nodes   = new LinkedList<>();
    private final List<Edge>                edges   = new LinkedList<>();
    private final Map<String, Node>         nodeMap = new TreeMap<>();
    private final BoundingBox               bounds;
    private final String                    description;
    private final CoordinateReferenceSystem coordinateReferenceSystem;
    private final int                       rasterWidth;
    private final int                       rasterHeight;

    // TODO when progress is implemented, we'll want to use something like the following:
    // from ogr2ogr.java - https://searchcode.com/codesearch/view/18938479/
//    private static class ScaledProgress extends ProgressCallback
//    {
//        private final double           percentMinimum;
//        private final double           percentMaximum;
//        private final ProgressCallback mainCallback;
//
//        ScaledProgress(final double           percentMinimum,
//                       final double           percentMaximum,
//                       final ProgressCallback mainCallback)
//        {
//            this.percentMinimum = percentMinimum;
//            this.percentMaximum = percentMaximum;
//            this.mainCallback   = mainCallback;
//        }
//
//        @Override
//        public int run(final double percentComplete,
//                       final String message)
//        {
//            return this.mainCallback
//                       .run(this.percentMinimum + percentComplete * (this.percentMaximum - this.percentMinimum),
//                            message);
//        }
//    }
}
