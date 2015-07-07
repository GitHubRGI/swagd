package com.rgi.g2t.tests;

import com.rgi.common.Dimensions;
import com.rgi.g2t.RawImageTileReader;
import com.rgi.store.tiles.TileStoreException;
import org.gdal.gdal.*;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import utility.GdalUtility;

import java.awt.*;
import java.io.File;

public class RawImageTileReaderTest
{
    /**
     * Tests RawImageTileReader constructor
     */
    @SuppressWarnings("static-method")
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentException1() throws TileStoreException
    {
        final Dataset dataset = GdalUtility.open(new File("test.tif"));
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
        final Dataset dataset = GdalUtility.open(new File("test.tif"));
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
        final File rawData = new File("test.tif");
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
        final File rawData = new File("test.tif");
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
        final File rawData = new File("test.tif");
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
        final File rawData = new File("test.tif");

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

    @Test
    public void testConstructor() throws TileStoreException
    {
        final File rawData = new File("test.tif");
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        try(final RawImageTileReader reader = new RawImageTileReader(rawData, tileSize , Color.BLACK))
        {
            assertTrue("RawImageTileReader constructor did not properly set up the RawImageTileReader",
                       reader.getName().equals("test") &&
                       reader.getImageType().equals("tiff") &&
                       reader.getImageDimensions().equals(tileSize) &&
                       reader.getByteSize() == rawData.length());
        }
    }
}
