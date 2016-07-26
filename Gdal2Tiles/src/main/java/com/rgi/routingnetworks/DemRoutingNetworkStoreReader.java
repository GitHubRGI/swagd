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

package com.rgi.routingnetworks;

import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.RoutingNetworkStoreReader;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import utility.GdalError;
import utility.GdalUtility;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class DemRoutingNetworkStoreReader implements RoutingNetworkStoreReader, AutoCloseable
{
    public DemRoutingNetworkStoreReader(final File   file,
                                        final int    rasterBand,
                                        final double contourInterval,
                                        final Double noDataValue,
                                        final double simplificationTolerance)
    {
        final Dataset dataset = GdalUtility.open(file);

        // We cannot tile an image with no geo referencing information
        if(!GdalUtility.hasGeoReference(dataset))
        {
            throw new IllegalArgumentException("Input raster image has no georeference.");
        }

        this.coordinateReferenceSystem = GdalUtility.getCoordinateReferenceSystem(GdalUtility.getSpatialReference(dataset));

        if(this.coordinateReferenceSystem  == null)
        {
            throw new IllegalArgumentException("Image file is not in a recognized coordinate reference system");
        }

        this.dataSource = ogr.GetDriverByName("Memory").CreateDataSource("foo"); // todo change name

        final Layer outputLayer = this.dataSource.CreateLayer("bar"); // todo change name

        // http://www.gdal.org/gdal__alg_8h.html#aceaf98ad40f159cbfb626988c054c085
        final int gdalError = gdal.ContourGenerate(dataset.GetRasterBand(rasterBand),         // Band             srcBand         - The band to read raster data from. The whole band will be processed
                                                   contourInterval,                           // double           contourInterval - The elevation interval between contours generated
                                                   0,                                         // double           contourBase     - The "base" relative to which contour intervals are applied. This is normally zero, but could be different. To generate 10m contours at 5, 15, 25, ... the ContourBase would be 5
                                                   null,                                      // double[]         fixedLevels     - The list of fixed contour levels at which contours should be generated. It will contain FixedLevelCount entries, and may be NULL
                                                   (noDataValue == null) ? 0   : 1,           // int              useNoData       - If TRUE the noDataValue will be used
                                                   (noDataValue == null) ? 0.0 : noDataValue, // double           noDataValue     - The value to use as a "nodata" value. That is, a pixel value which should be ignored in generating contours as if the value of the pixel were not known
                                                   outputLayer,                               // Layer            dstLayer        - The layer to which new contour vectors will be written. Each contour will have a LINESTRING geometry attached to it
                                                   -1,                                        // int              idField         - If not -1 this will be used as a field index to indicate where a unique id should be written for each feature (contour) written
                                                   -1,                                        // int              elevField       - If not -1 this will be used as a field index to indicate where the elevation value of the contour should be written
                                                   null);                                     // ProgressCallback callback        - A ProgressCallback that may be used to report progress to the user, or to interrupt the algorithm. May be NULL if not required

        if(gdalError != gdalconstConstants.CE_None)
        {
            throw new RuntimeException(new GdalError().getMessage());
        }

        for(Feature feature = outputLayer.GetNextFeature(); feature != null; feature = outputLayer.GetNextFeature())
        {
            // http://gdal.org/java/org/gdal/ogr/Geometry.html#SimplifyPreserveTopology(double) ->
            // This function is built on the GEOS library, check it for the definition of the geometry operation. If OGR is built without the GEOS library, this function will always fail, issuing a CPLE_NotSupported error.
            // http://geos.refractions.net/ro/doxygen_docs/html/classgeos_1_1simplify_1_1TopologyPreservingSimplifier.html ->
            // All vertices in the simplified geometry will be within this distance of the original geometry. The tolerance value must be non-negative. A tolerance value of zero is effectively a no-op.
            feature.SetGeometry(feature.GetGeometryRef()
                                       .SimplifyPreserveTopology(simplificationTolerance)); // https://gis.stackexchange.com/questions/102254/ogr-simplifypreservetopology-does-not-keep-the-topology
                                                                                            // Topology preserving means in practice that parts of the multilinestring meet after simplification, polygons
                                                                                            // do not have self-intersections, inner rings in polygons stay inside outer rings, etc. Especially for polygon
                                                                                            // layers this method does not prevent gaps, overlaps, and slivers from appearing, even though this is the
                                                                                            // general belief. I would say that the method has a misleading name which makes users to believe that it saves
                                                                                            // the topology for the whole layer. However, the name and behaviour is the same in PostGIS and in JTS
                                                                                            // http://www.tsusiatsoftware.net/jts/javadoc/com/vividsolutions/jts/simplify/TopologyPreservingSimplifier.html

            //feature.GetGeometryRef().DelaunayTriangulation()
        }
    }

    @Override
    public List<Pair<String, Type>> getNodeAttributes()
    {
        return null;
    }

    @Override
    public List<Pair<String, Type>> getEdgeAttributes()
    {
        return null;
    }

    @Override
    public List<Node> getNodes()
    {
        return null;
    }

    @Override
    public List<Edge> getEdges()
    {
        return null;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
    }

    private final CoordinateReferenceSystem coordinateReferenceSystem;

    @Override
    public void close()
    {
        this.dataSource.delete();
    }

    private final DataSource dataSource;

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
