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
//@formatter:off
package com.rgi.g2t;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
@SuppressWarnings("MagicNumber")
public class RawImageTileReaderTest
{
    // Tiff used for testing
    private final File rawData = new File("test.tif");

    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException1() throws TileStoreException
    {
        final Dataset dataset = GdalUtility.open(this.rawData);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);
        try (final RawImageTileReader ignored = new RawImageTileReader(null, dataset, tileDimensions, null, null))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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
        final Dataset dataset = GdalUtility.open(this.rawData);
        final Dimensions<Integer> tileDimensions = new Dimensions<>(256, 256);

        try (final RawImageTileReader ignored = new RawImageTileReader(new File("S"), dataset, tileDimensions, null, null))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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
        try (final RawImageTileReader ignored = new RawImageTileReader(this.rawData, null, color))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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

        try (final RawImageTileReader ignored = new RawImageTileReader(this.rawData, dataset, tileDimensions, null, null))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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

        try (final RawImageTileReader ignored = new RawImageTileReader(this.rawData, dataset, tileDimensions, null, null))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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

        try (final RawImageTileReader ignored = new RawImageTileReader(this.rawData, dataset, tileDimensions, null, null))
        {
            fail("Expected RawImageTileReader to throw an IllegalArgumentException.");
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
        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize , Color.BLACK))
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
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize , Color.BLACK))
        {
            assertEquals("RawImageTileReader method getBounds did not return the correct BoundingBox",
                         box,
                         reader.getBounds());
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
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader did not return the correct number of tiles for the test image",
                         tileCount,
                         reader.countTiles());
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader method getByteSize did not return the correct size.",
                         reader.getByteSize(),
                         this.rawData.length());
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            reader.getTile(0, 0, 0);
        }
        catch(final TileStoreException exp)
        {
            assertTrue("Expected RawImageTileReader method getTile(int, int, int) to throw an OperationNotSupportedException.",
                       exp.getClass().equals(TileStoreException.class) &&
                       exp.getCause().getClass().equals(OperationNotSupportedException.class));
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
        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            reader.getTile(coordinate, 0);
        }
        catch(final TileStoreException exp)
        {
            assertTrue("Expected RawImageTileReader method getTile(CrsCoordinate, int) to throw an OperationNotSupportedException.",
                       exp.getClass().equals(TileStoreException.class) &&
                       exp.getCause().getClass().equals(OperationNotSupportedException.class));
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
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader method getZoomLevels did not return the correct set of zoom levels",
                         zooms,
                         reader.getZoomLevels());
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
         try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader method stream() did not return the correct stream",
                         reader.stream().count(),
                         reader.countTiles());
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
        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            final AtomicLong count = new AtomicLong(0);
            reader.getZoomLevels().stream().forEach(zoom -> {
                try
                {
                    count.addAndGet(reader.stream(zoom).count());
                }
                catch(TileStoreException e)
                {
                    throw new RuntimeException(e);
                }
            });
            assertEquals("RawImageTileReader method stream() did not return the correct stream",
                         reader.stream().count(),
                         reader.countTiles());
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
        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader method getCoordinateReferenceSystem did not return the correct CoordinateReferenceSystem",
                         crs,
                         reader.getCoordinateReferenceSystem());
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader getTileScheme did not return the correct TileScheme",
                         reader.getTileScheme().getZoomLevels(),
                         new ZoomTimesTwo(0,31,1,1).getZoomLevels());
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            assertEquals("RawImageTileReader getCoordinateReferenceSystem did not return the correct CoordinateReferenceSystem",
                         TileOrigin.LowerLeft,
                         reader.getTileOrigin());
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
        final Dataset data = GdalUtility.open(this.rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int minimumZoom = GdalUtility.getMinimalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);

        final TileMatrixDimensions expectedDimensions = tileScheme.dimensions(minimumZoom);

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
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
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertEquals("RawImageTileHandle methods getCrsCoordinate() did not return the correct CrsCoordinate.",
                         coordinate,
                         handle.getCrsCoordinate());
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
    public void testTileGetCrsCoordinate() throws TileStoreException, DataFormatException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        //Create ExpectedCrsCoordinate
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertEquals("RawImageTileHandle methods getCrsCoordinate(TileOrigin) did not return the correct CrsCoordinate.",
                         coordinate,
                         handle.getCrsCoordinate(TileOrigin.UpperRight));
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
        final Dataset data = GdalUtility.open(this.rawData);
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK))
        {
            final TileHandle handle = reader.stream(minimumZoom).findFirst().get();

            assertEquals("RawImageTileHandle method getBounds did not return the correct tile bounds.",
                         expectedBox,
                         handle.getBounds());
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

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK, null))
        {
            final TileHandle handle = reader.stream()
                                            .filter(tile -> tile.getColumn() == 32627 && tile.getRow() == 224798)
                                            .findFirst()
                                            .get();

            final BufferedImage image = ImageIO.read(new File("224798.png"));

            assertTrue("RawImageTileHandle method getImage did not return the correct image.",
                       bufferedImagesEqual(image, handle.getImage()));
        }
    }

    /**
     * Tests RawImageTileHandle getImage when
     * the image needs to be read from cached tiles
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGetImage2() throws TileStoreException, DataFormatException, IOException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        /* Get the maximum zoom level */
        final Dataset data = GdalUtility.open(this.rawData);
        final TileScheme tileScheme = new ZoomTimesTwo(0,31,1,1);
        final CrsProfile profile = CrsProfileFactory.create(new CoordinateReferenceSystem("EPSG", 3395));

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                    GdalUtility.getBounds(data),
                                                                                                    profile.getBounds(),
                                                                                                    profile,
                                                                                                    TileOrigin.LowerLeft);

        final int maxZoom = GdalUtility.getMaximalZoom(data, tileRanges, TileOrigin.LowerLeft, tileScheme, tileSize);


        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK, null))
        {
           /* Cache all tile images for the base zoom level */
           reader.stream()
                 .filter(tile -> tile.getZoomLevel() == maxZoom)
                 .forEach(tile->{ try
                                  {
                                     tile.getImage();
                                  }
                                  catch (final TileStoreException exp)
                                  {
                                      throw new RuntimeException(exp);
                                  }});

           final TileHandle handle = reader.stream()
                                           .filter(tile -> tile.getColumn() == 16313 && tile.getRow() == 112398)
                                           .findAny()
                                           .get();

           final BufferedImage image = ImageIO.read(new File("112398.png"));

           assertTrue("RawImageTileHandle method getImage did not return the correct image.",
                      bufferedImagesEqual(image, handle.getImage()));
        }
        finally
        {
            data.delete();
        }
    }

    /**
     * Tests RawImageTileHandle toString method
     */
    @SuppressWarnings("static-method")
    @Test
    public void testToString() throws TileStoreException, DataFormatException, IOException
    {
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        try(final RawImageTileReader reader = new RawImageTileReader(this.rawData, tileSize, Color.BLACK, null))
        {
            final TileHandle handle = reader.stream()
                                            .filter(tile -> tile.getColumn() == 32627 && tile.getRow() == 224798 && tile.getZoomLevel() == 18)
                                            .findFirst()
                                            .get();

            final String expected = "18/32627/224798";
            final String returned = handle.toString();

            assertEquals(String.format("RawImageTileHandle method toString did not return the correct String, %s was returned, but %s was expected.",
                                       returned,
                                       expected),
                         expected,
                         returned);
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

        for(int xCoord = 0; xCoord < img1.getWidth(); xCoord++)
        {
            for(int yCoord = 0; yCoord < img1.getHeight(); yCoord++)
            {
                if(img1.getRGB(xCoord, yCoord) != img2.getRGB(xCoord, yCoord))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
