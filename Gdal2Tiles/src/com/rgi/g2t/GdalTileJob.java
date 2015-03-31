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
package com.rgi.g2t;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.DataFormatException;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.rules.TemporaryFolder;

import utility.GdalUtility;
import utility.GdalUtility.GdalRasterParameters;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.TaskMonitor;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;

/**
 * @author Steven D. Lander
 *
 */
public class GdalTileJob implements Runnable {

    private final TileStoreWriter     writer;
    private final CrsProfile          crsProfile;
    private final File                file;
    private final Path                outputFolder;
    private final Dimensions<Integer> tileDimensions;
    private final Color               noDataColor;
    private final TaskMonitor         monitor;

    private final int             tileSize            = 256;
    private final String          resamplingAlgorithm = "average";
    private final int             gdalGeoQuerySize    = 4 * this.tileSize;
    private final TemporaryFolder tempFolder          = new TemporaryFolder();

    /**
     * @param file
     * @param writer
     * @param tileDimensions
     * @param noDataColor
     * @param monitor
     */
    public GdalTileJob(final File file,
                       final TileStoreWriter writer,
                       final Dimensions<Integer> tileDimensions,
                       final Color noDataColor,
                       final TaskMonitor monitor)
    {
        this.file           = file;
        this.writer         = writer;
        this.tileDimensions = tileDimensions;
        this.monitor        = monitor;
        this.noDataColor    = noDataColor;
        this.crsProfile     = CrsProfileFactory.create(writer.getCoordinateReferenceSystem());
        this.outputFolder   = Paths.get("/data/tiles/swagd");
    }

    @Override
    public void run() {
        try
        {
            final Dataset inputDataset = this.openInput();
            final Dataset outputDataset = this.openOutput(inputDataset);
            final BoundingBox outputBounds = GdalUtility.getBoundsForDataset(outputDataset);
            final List<Range<Coordinate<Integer>>> ranges = GdalUtility.calculateTileRangesForAllZooms(outputBounds,
            																						   this.crsProfile,
            																						   this.writer.getTileScheme(),
            																						   this.writer.getTileOrigin());
            // Generate base tiles
            try
            {
            	final int maxZoom = GdalUtility.maximalZoomForDataset(outputDataset,
            														  ranges,
            														  this.writer.getTileOrigin(),
            														  this.writer.getTileScheme(),
            														  this.tileDimensions);
                this.generateBaseTiles(outputDataset, ranges.get(maxZoom), maxZoom);
                System.out.println("Base tiles finished generating.");
            }
            //catch(TilingException | TileStoreException  ex1)
            catch (Exception ex1)
            {
                ex1.printStackTrace();
            }
            final int minZoom = GdalUtility.minimalZoomForDataset(outputDataset,
																  ranges,
																  this.writer.getTileOrigin(),
																  this.writer.getTileScheme(),
																  this.tileDimensions);
			this.generateOverviewTiles();
        }
        catch(final TilingException | DataFormatException ex1)
        {
            // TODO: handle tiling failure
            ex1.printStackTrace();
        }
    }

    private Dataset openInput() throws TilingException
    {
        osr.UseExceptions();
        // Register gdal for use
        gdal.AllRegister();
        // TODO: Check memory driver in case gdal is configured incorrectly?
        final Dataset dataset = gdal.Open(this.file.toPath().toString(), gdalconstConstants.GA_ReadOnly);
        // TODO: What happens if it cannot open this?
        if (dataset.GetRasterBand(1).GetColorTable() != null)
        {
            // TODO: make a temp vrt with gdal_translate to expand this to RGB/RGBA
            System.out.println("expand this raster to RGB/RGBA");
        }
        if (dataset.GetRasterCount() == 0)
        {
            throw new TilingException("Input file has no raster band.");
        }
        final SpatialReference inputSrs = this.openInputSrs(dataset);
        // We cannot tile an image with no geo referencing information
        if (!GdalUtility.datasetHasGeoReference(dataset))
        {
            throw new TilingException("Input raster image has no georeference.");
        }

        return dataset;
    }

    private Dataset openOutput(final Dataset inputDataset) throws TilingException
    {
        final Dataset outputDataset;
        // Get the output SRS
        try
        {
        	final SpatialReference inputSrs = GdalUtility.getDatasetSpatialReference(inputDataset);
        	final SpatialReference outputSrs = GdalUtility.getSpatialReferenceFromCrs(this.crsProfile.getCoordinateReferenceSystem());
        	// If input srs and output srs are not the same, reproject by making a VRT
        	if (inputSrs.ExportToProj4() != outputSrs.ExportToProj4() || inputDataset.GetGCPCount() == 0)
        	{
        	    // Create a warped VRT
        	    outputDataset = GdalUtility.warpDatasetToSrs(inputDataset, outputSrs);
        	}
        	else
        	{
        	    // The input and output projections are the same, no reprojection needed
        	    outputDataset = inputDataset;
        	}
        	//return this.correctNoData(outputDataset, this.getNoDataValues(inputDataset));
        }
        catch (DataFormatException tse)
        {
        	throw new TilingException(tse);
        }
        return GdalUtility.correctNoDataSimple(outputDataset);
    }

    private SpatialReference openInputSrs(final Dataset dataset) throws TilingException
    {
        final SpatialReference srs = new SpatialReference();
        // Get the well-known-text of this dataset
        String wkt = dataset.GetProjection();
        if (wkt.isEmpty() && dataset.GetGCPCount() != 0)
        {
            // If the wkt is empty and there are GCPs...
            wkt = dataset.GetGCPProjection();
        }
        if (!wkt.isEmpty())
        {
            // Initialize the srs from the non-empt wkt string
            srs.ImportFromWkt(wkt);
            return srs;
        }
        throw new TilingException("Cannot get source file spatial reference system.");
    }

    private void generateBaseTiles(final Dataset dataset, final Range<Coordinate<Integer>> baseZoomRange, final int baseZoom) throws TilingException
    {
        // Create a tile folder name
        final Coordinate<Integer> topLeftCoordinate = baseZoomRange.getMinimum();
        final Coordinate<Integer> bottomRightCoordinate = baseZoomRange.getMaximum();
        // Set x/y min/max values
        final int tileMinX = topLeftCoordinate.getX();
        final int tileMaxX = bottomRightCoordinate.getX();
        final int tileMinY = bottomRightCoordinate.getY();
        final int tileMaxY = topLeftCoordinate.getY();
        final TileMatrixDimensions zoomDimensions = this.writer.getTileScheme().dimensions(baseZoom);
        // Calculate a total tile count: (BR.X - TL.X) * (TL.Y - BR.Y)
        //final int totalXTiles = 1 + Math.abs((tileMaxX - tileMinX));
        //final int totalYTiles = 1 + Math.abs((tileMaxY - tileMinY));
        //final int totalTileCount = (totalXTiles * totalYTiles);
        // Create a for loop for tile y, counting down
        for (int tileY = tileMaxY; tileY >= tileMinY; tileY--)
        {
            // Create a for loop for tile x, counting up
            // This makes queries start at top-left of the data
            for (int tileX = tileMinX; tileX <= tileMaxX; tileX++)
            {
                // Resolve a tile path and name
                final CrsCoordinate tileTopLeftCorner = this.crsProfile.tileToCrsCoordinate(tileX, tileY + 1, this.crsProfile.getBounds(), zoomDimensions, this.writer.getTileOrigin());
                final CrsCoordinate tileBottomRightCorner = this.crsProfile.tileToCrsCoordinate(tileX + 1, tileY, this.crsProfile.getBounds(), zoomDimensions, this.writer.getTileOrigin());
                final BoundingBox tileBBox = new BoundingBox(tileTopLeftCorner.getX(), tileBottomRightCorner.getY(), tileBottomRightCorner.getX(), tileTopLeftCorner.getY());
                final GdalRasterParameters params = GdalUtility.getGdalRasterParameters(dataset.GetGeoTransform(), tileBBox, this.tileDimensions, dataset);
                // Read dat raster using a Byte array type (GDT_Byte)
                final byte[] imageData = GdalUtility.readRaster(params, dataset);
                // TODO: logic goes here in the case that the querysize == tile size (gdalconstConstants.GRA_NearestNeighbour) (write directly)
                // Time to start writing the tile
                final Dataset querySizeImageCanvas = GdalUtility.writeRaster(params, imageData, dataset.GetRasterCount());
                
                // Scale each band of tileDataInMemory down to the tile size (down from the query size)
                final Dataset tileDataInMemory = GdalUtility.scaleQueryToTileSize(querySizeImageCanvas, this.tileDimensions);
                try
                {
                	this.writer.addTile(tileX, tileY, baseZoom, GdalUtility.convert(tileDataInMemory));
                }
                catch (TileStoreException tse)
                {
                	throw new TilingException(tse);
                }
            }
        }
    }
    
    private void generateOverviewTiles()
    {
        // TODO:
    }
}