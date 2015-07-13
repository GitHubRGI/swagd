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

package com.rgi.g2t.tests;

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;

import org.gdal.gdal.Band;
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.junit.Test;

import utility.GdalUtility;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.store.tiles.TileHandle;
import com.rgi.store.tiles.TileStoreException;

/**
 *
 * @author Mary Carome
 *
 */
public class RawImageTileReaderTest
{
    // Tiff used for testing
    final static File rawData = new File("test.tif");

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException1() throws TileStoreException
    {
        final Dataset dataset = GdalUtility.open(rawData);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);
        try (final RawImageTileReader reader = new RawImageTileReader(null, dataset, tileDimensions, null, null))
        {
            // An exception should be thrown
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException2() throws TileStoreException
    {
        final Dataset dataset = GdalUtility.open(rawData);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);

        try (final RawImageTileReader reader = new RawImageTileReader(new File("S"), dataset, tileDimensions, null, null))
        {
            // An exception should be thrown
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException3() throws TileStoreException
    {
        final Color color = Color.BLUE;
        try (final RawImageTileReader reader = new RawImageTileReader(rawData, null, color))
        {
            // An exception should be thrown
        }
    }

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException4() throws TileStoreException
    {
        gdal.AllRegister();
        final Dataset dataset = gdal.GetDriverByName("MEM").Create("test", 12, 23, 0);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);

        try (final RawImageTileReader reader = new RawImageTileReader(rawData, dataset, tileDimensions, null, null))
        {
            // An exception should be thrown
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException5() throws TileStoreException
    {
        gdal.AllRegister();
        final Dataset dataset = gdal.GetDriverByName("MEM").Create("test", 12, 23, 1);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);

        try (final RawImageTileReader reader = new RawImageTileReader(rawData, dataset, tileDimensions, null, null))
        {
            // An exception should be thrown
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException6() throws TileStoreException
    {
        gdal.AllRegister();
        final Dataset dataset = gdal.GetDriverByName("MEM").Create("test", 12, 23, 1);

        final Band rasterBand = dataset.GetRasterBand(1);
        rasterBand.SetRasterColorTable(new ColorTable(1));

        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);

        try (final RawImageTileReader reader = new RawImageTileReader(rawData, dataset, tileDimensions, null, null))
        {
            // An exception should be thrown
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests constructor properly sets up the RawImageTileReader
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testConstructor() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize , Color.BLACK))
        {
            assertTrue("RawImageTileReader constructor did not properly set up the RawImageTileReader",
                       reader.getName().equals("test") &&
                       reader.getImageType().equals("tiff"));
        }
    }

    /**
     * Tests the getBoundsMethod
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetBounds() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create Bounding Box of the Data
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final Coordinate<Integer> minTile = tileRanges.get(minimumZoom).getMinimum();

        final BoundingBox box = profile.getTileBounds(minTile.getX(),
                                                      minTile.getY(),
                                                      profile.getBounds(),
                                                      tileScheme.dimensions(minimumZoom),
                                                      TileOrigin.LowerLeft);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize , Color.BLACK))
        {
            assertTrue("RawImageTileReader method getBounds did not return the correct BoundingBox",
                       reader.getBounds().equals(box));
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Test count tiles
     */
    @SuppressWarnings("static-method")
    @Test
    public void testCountTiles() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Determine tileCount
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                    GdalUtility.getBounds(data),
                                                                    profile.getBounds(),
                                                                    profile,
                                                                    TileOrigin.LowerLeft);

        final int minZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final int maxZoom = GdalUtility.getMaximalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);

        final int tileCount  =   IntStream.rangeClosed(minZoom, maxZoom)
                                          .map(zoomLevel -> {
                                                                final Range<Coordinate<Integer>> range = tileRanges.get(zoomLevel);

                                                                return (range.getMaximum().getX() - range.getMinimum().getX() + 1) *
                                                                       (range.getMinimum().getY() - range.getMaximum().getY() + 1);
                                           })
                                          .sum();

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader did not return the correct number of tiles for the test image",
                       reader.countTiles() == tileCount);
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Tests getByteSize
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetByteSize() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader method getByteSize did not return the correct size.",
                       reader.getByteSize() == rawData.length());
        }
    }


    /**
     * Tests that getTile(int, int, int) throws an Exception
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetTile1()
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            reader.getTile(0, 0, 0);
        }
        catch(final Exception e)
        {
            assertTrue("Expected RawImageTileReader method getTile(int, int, int) to throw an OperationNotSupportedException.",
                       e.getClass().equals(TileStoreException.class) &&
                       e.getCause().getClass().equals(OperationNotSupportedException.class));
        }
    }

    /**
     * Tests that getTile(int, int, int) throws an Exception
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetTile2()
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        final CrsCoordinate coordinate = new CrsCoordinate(0,0,"test", 0);
        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            reader.getTile(coordinate, 0);
        }
        catch(final Exception e)
        {
            assertTrue("Expected RawImageTileReader method getTile(CrsCoordinate, int) to throw an OperationNotSupportedException.",
                       e.getClass().equals(TileStoreException.class) &&
                       e.getCause().getClass().equals(OperationNotSupportedException.class));
        }
    }

    /**
     * Test getZoomLevels correctly returns the number of zoom levels
     *
     * @throws TileStoreException
     * @throws DataFormatException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetZoomLevels() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        //Determine zoom levels
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final int maxZoom = GdalUtility.getMaximalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);

        final Set<Integer> zooms = IntStream.rangeClosed(minZoom, maxZoom).boxed().collect(Collectors.toSet());

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader method getZoomLevels did not return the correct set of zoom levels",
                       reader.getZoomLevels().equals(zooms));
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Tests that stream returns a stream with the
     * correct number of TileHandles
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testStream1() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
         try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader method stream() did not return the correct stream",
                       reader.stream().count() == reader.countTiles());
        }
    }

    /**
     * Tests that stream returns a stream with the
     * correct number of TileHandles
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testStream2() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            final AtomicLong count = new AtomicLong(0);
            reader.getZoomLevels().stream().forEach(zoom -> count.addAndGet(reader.stream(zoom).count()));
            assertTrue("RawImageTileReader method stream() did not return the correct stream",
                       reader.stream().count() == reader.countTiles());
        }
    }

    /**
     * Tests getCoordinateReferenceSystem
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetCoordinateReferenceSystem() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("WGS 84 / World Mercator", "EPSG", 3395);
        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader method getCoordinateReferenceSystem did not return the correct CoordinateReferenceSystem",
                       reader.getCoordinateReferenceSystem().equals(crs));
        }
    }

    /**
     * Tests getImageDimensions
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetImageDimensions() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 512);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader method getImageDimensions did not return the correct Dimensions",
                       reader.getImageDimensions().getWidth() == 256 &&
                       reader.getImageDimensions().getHeight() == 512);
        }
    }

    /**
     * Tests getTileScheme
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testTileScheme() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 512);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader getTileScheme did not return the correct TileScheme",
                       reader.getTileScheme().getZoomLevels().equals(new ZoomTimesTwo(0,31,1,1).getZoomLevels()));
        }
    }

    /**
     * Tests getTileOrigin
     *
     * @throws TileStoreException
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetTileOrigin() throws TileStoreException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 512);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            assertTrue("RawImageTileReader getCoordinateReferenceSystem did not return the correct CoordinateReferenceSystem",
                       reader.getTileOrigin().equals(TileOrigin.LowerLeft));
        }
    }

    /**
     * Tests RawImageTileHandle getMatrix
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetMatrix()throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create expected TileMatrixDimensions
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);

        final TileMatrixDimensions expectedDimensions = tileScheme.dimensions(minimumZoom);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertTrue("RawImageTileHandle methods getMatrix did not return the correct TileMatrixDimensions.",
                       handle.getMatrix().getHeight() == expectedDimensions.getHeight() &&
                       handle.getMatrix().getWidth() == expectedDimensions.getWidth());
        }
    }

    /**
     * Tests RawImageTileHandle getCrsCoordinate
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetCrsCoordinate1() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create ExpectedCrsCoordinate
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final Coordinate<Integer> minTile = tileRanges.get(minimumZoom).getMinimum();

        final CrsCoordinate coordinate = profile.tileToCrsCoordinate(minTile.getX() + TileOrigin.LowerLeft.getHorizontal(),
                                                                     minTile.getY() + TileOrigin.LowerLeft.getVertical(),
                                                                     profile.getBounds(),
                                                                     tileScheme.dimensions(minimumZoom),
                                                                     TileOrigin.LowerLeft);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertTrue("RawImageTileHandle methods getCrsCoordinate() did not return the correct CrsCoordinate.",
                       handle.getCrsCoordinate().equals(coordinate));
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Tests RawImageTileHandle getCrsCoordinate(TileOrigin)
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetCrsCoordinate2() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create ExpectedCrsCoordinate
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final Coordinate<Integer> minTile = tileRanges.get(minimumZoom).getMinimum();

        final CrsCoordinate coordinate = profile.tileToCrsCoordinate(minTile.getX() + TileOrigin.UpperRight.getHorizontal(),
                                                                     minTile.getY() + TileOrigin.UpperRight.getVertical(),
                                                                     profile.getBounds(),
                                                                     tileScheme.dimensions(minimumZoom),
                                                                     TileOrigin.LowerLeft);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertTrue("RawImageTileHandle methods getCrsCoordinate(TileOrigin) did not return the correct CrsCoordinate.",
                       handle.getCrsCoordinate(TileOrigin.UpperRight).equals(coordinate));
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Tests RawImageTileHandle getBounds
     */
    @SuppressWarnings("static-method")
    @Test
    public void testTileGetBounds()throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create expected TileMatrixDimensions
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);
        final Coordinate<Integer> minTile = tileRanges.get(minimumZoom).getMinimum();

        final TileMatrixDimensions dimensions = tileScheme.dimensions(minimumZoom);

        final BoundingBox expectedBox = profile.getTileBounds(minTile.getX(),
                                                              minTile.getY(),
                                                              profile.getBounds(),
                                                              dimensions,
                                                              TileOrigin.LowerLeft);

        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertTrue("RawImageTileHandle method getBounds did not return the correct tile bounds.",
                       handle.getBounds().equals(expectedBox));
        }
    }

    /**
     * Tests RawImageTileHandle getImage when
     * the image needs to be read from the raw data
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetImage1() throws TileStoreException, DataFormatException, IOException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("WGS84 / Web Mercator", "EPSG", 3857);

        //Create expected TileMatrixDimensions
        final Dataset data = GdalUtility.open(rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);


        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize, Color.BLACK, crs))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();
            final BufferedImage image = ImageIO.read(new File("base1.png"));

            assertTrue("RawImageTileHandle method getImage did not read the image correctly.",
                       bufferedImagesEqual(image, handle.getImage()));
        }
    }

    /**
     * Compares two BufferedImages and determines if they are equal
     *
     * @param img1 the first buffered image
     * @param img2 the second buffered image
     * @return true if the two BufferedImages are equal
     */
    private static boolean bufferedImagesEqual(final BufferedImage img1, final BufferedImage img2)
    {
        if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
        {
            return false;
        }

        for(int x = 0; x < img1.getWidth(); x++)
        {
            for(int y = 0; y < img1.getHeight(); y++)
            {
                if(img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
