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

package com.rgi.routingnetworks.image;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.store.routingnetworks.Edge;
import com.rgi.store.routingnetworks.Node;
import com.rgi.store.routingnetworks.NodeDimensionality;
import com.rgi.store.routingnetworks.RoutingNetworkStoreException;
import com.rgi.store.routingnetworks.RoutingNetworkStoreWriter;
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
import org.gdal.osr.SpatialReference;
import utility.GdalError;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Rasterizes routing network information to a GeoTiff
 *
 * @author Luke Lambert
 */
public class ImageRoutingNetworkStoreWriter implements RoutingNetworkStoreWriter
{
    /**
     * Constructor
     *
     * @param imageFile   Destination file
     * @param imageWidth  Image width, in pixels
     * @param imageHeight Image height, in pixels
     * @param background  Image background color. RGBA, with each component having a valid range of 0 to 255
     * @param foreground  Edge color. RGBA, with each component having a valid range of 0 to 255
     * @param bounds      Geographic bounds of the routing network
     */
    public ImageRoutingNetworkStoreWriter(final File        imageFile,
                                          final int         imageWidth,
                                          final int         imageHeight,
                                          final Color       background,
                                          final Color       foreground,
                                          final BoundingBox bounds)
    {
        this(imageFile,
             imageWidth,
             imageHeight,
             background,
             foreground,
             bounds,
             null);
    }

    /**
     * Constructor
     *
     * @param imageFile   Destination file
     * @param imageWidth  Image width, in pixels
     * @param imageHeight Image height, in pixels
     * @param background  Image background color. RGBA, with each component having a valid range of 0 to 255
     * @param foreground  Edge color. RGBA, with each component having a valid range of 0 to 255
     * @param bounds      Geographic bounds of the routing network
     */
    public ImageRoutingNetworkStoreWriter(final File             imageFile,
                                          final int              imageWidth,
                                          final int              imageHeight,
                                          final Color            background,
                                          final Color            foreground,
                                          final BoundingBox      bounds,
                                          final ProgressCallback progressCallback)
    {
        this.imageFile        = imageFile;
        this.imageWidth       = imageWidth;
        this.imageHeight      = imageHeight;
        this.background       = background;
        this.foreground       = foreground;
        this.bounds           = bounds;
        this.progressCallback = progressCallback;
    }

    @Override
    public void write(final List<Node>                nodes,
                      final List<Edge>                edges,
                      final NodeDimensionality        nodeDimensionality,        // not used
                      final List<Pair<String, Type>>  nodeAttributeDescriptions, // not used
                      final List<Pair<String, Type>>  edgeAttributeDescriptions, // not used
                      final CoordinateReferenceSystem coordinateReferenceSystem) throws RoutingNetworkStoreException
    {
        if(!coordinateReferenceSystem.getAuthority().equalsIgnoreCase("EPSG"))
        {
            throw new RuntimeException("Currently only EPSG codes can be used in coordinate reference systems");
        }

        final SpatialReference sourceSpatialReference = new SpatialReference();
        sourceSpatialReference.ImportFromEPSG(coordinateReferenceSystem.getIdentifier());

        final Dataset rasterDataset = this.createRaster(sourceSpatialReference);

        try
        {
            final DataSource dataSource = ogr.GetDriverByName("Memory")
                                             .CreateDataSource("vector data");

            try
            {
                final Layer edgeLayer = createEdgeLayer(dataSource,
                                                        nodes,
                                                        edges,
                                                        sourceSpatialReference);

                final int rasterizeError = gdal.RasterizeLayer(rasterDataset,
                                                               new int[]{1, 2, 3, 4},
                                                               edgeLayer,
                                                               new double[]{ this.foreground.getRed(),
                                                                             this.foreground.getGreen(),
                                                                             this.foreground.getBlue(),
                                                                             this.foreground.getAlpha()
                                                                           },
                                                               null,                    // "options" vector. valid choices are described here: http://gdal.org/gdal__alg_8h.html#adfe5e5d287d6c184aab03acbfa567cb1
                                                               this.progressCallback);

                if(rasterizeError != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }
            }
            finally
            {
                dataSource.delete();    // Also destroys edgeLayer
            }
        }
        finally
        {
            rasterDataset.delete();
        }
    }

    private Dataset createRaster(final SpatialReference spatialReference)
    {
        final Vector imageCreationOptions = new Vector(1);

        imageCreationOptions.add("COMPRESS=LZW");

        final Dataset rasterDataset = gdal.GetDriverByName("GTiff")
                                          .Create(this.imageFile.getAbsolutePath(),
                                                  this.imageWidth,
                                                  this.imageHeight,
                                                  4,    // RGBA
                                                  gdalconstConstants.GDT_Byte,
                                                  imageCreationOptions);

        try
        {
            rasterDataset.GetRasterBand(1).Fill(this.background.getRed());
            rasterDataset.GetRasterBand(2).Fill(this.background.getGreen());
            rasterDataset.GetRasterBand(3).Fill(this.background.getBlue());
            rasterDataset.GetRasterBand(4).Fill(this.background.getAlpha());

            rasterDataset.SetGeoTransform(new double[]{ this.bounds.getTopLeft().getX(),                    // top left x
                                                        this.bounds.getWidth() / (double)this.imageWidth,   // w-e pixel resolution
                                                        0,                                                  // rotation, 0 if image is "north up"
                                                        this.bounds.getTopLeft().getY(),                    // top left y
                                                        0,                                                  // rotation, 0 if image is "north up"
                                                        -this.bounds.getHeight() / (double)this.imageHeight // n-s pixel resolution (negative value!)
                                                      });

            rasterDataset.SetProjection(spatialReference.ExportToWkt());
        }
        catch(final Throwable th)
        {
            rasterDataset.delete();
            throw th;
        }

        return rasterDataset;
    }

    private static Layer createEdgeLayer(final DataSource       dataSource,
                                         final Collection<Node> nodes,
                                         final Iterable<Edge>   edges,
                                         final SpatialReference spatialReference)
    {
         final Map<Integer, Node> nodeMap = new HashMap<>(nodes.size());

        for(final Node node : nodes)
        {
            nodeMap.put(node.getIdentifier(), node);
        }

        final Layer edgeLayer = dataSource.CreateLayer("edges",
                                                       spatialReference,
                                                       ogrConstants.wkbLineString);

        try
        {
            for(final Edge edge : edges)
            {
                final Feature edgeFeature = new Feature(edgeLayer.GetLayerDefn());

                final Geometry line = new Geometry(ogrConstants.wkbLineString);

                final Node node0 = nodeMap.get(edge.getFrom());
                final Node node1 = nodeMap.get(edge.getTo());

                line.AddPoint(node0.getX(), node0.getY());
                line.AddPoint(node1.getX(), node1.getY());

                if(edgeFeature.SetGeometry(line) != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }

                if(edgeLayer.CreateFeature(edgeFeature) != gdalconstConstants.CE_None)
                {
                    throw new RuntimeException(new GdalError().getMessage());
                }
            }
        }
        catch(final Throwable th)
        {
            edgeLayer.delete();
            throw th;
        }

        return edgeLayer;
    }

    private final File             imageFile;
    private final int              imageWidth;
    private final int              imageHeight;
    private final Color            background;
    private final Color            foreground;
    private final BoundingBox      bounds;
    private final ProgressCallback progressCallback;
}
