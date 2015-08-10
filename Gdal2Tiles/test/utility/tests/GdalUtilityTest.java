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
package utility.tests;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.Range;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.store.tiles.TileStoreException;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.GCP;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.Before;
import org.junit.Test;
import utility.GdalUtility;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import static org.junit.Assert.*;

/**
 * @author Luke D. Lambert
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 *
 */
@SuppressWarnings({"MagicNumber", "JUnitTestMethodWithNoAssertions"})
public class GdalUtilityTest
{
    @SuppressWarnings({"PublicInnerClass", "PackageVisibleField"})
    public static class ImageDataProperties
    {
        Dataset           dataset;
        File              imageFile;
        SpatialReference  srs;
        boolean           hasAlpha;
        CrsProfile        crsProfile;
        BoundingBox       boundingBox;
    }

    private final GdalUtilityTest.ImageDataProperties dataset1 = new GdalUtilityTest.ImageDataProperties();
    private final GdalUtilityTest.ImageDataProperties dataset2 = new  GdalUtilityTest.ImageDataProperties();
    private final List<GdalUtilityTest.ImageDataProperties> imageList = Arrays.asList(this.dataset1, this.dataset2);

    @Before
    public void setUp()
    {
        osr.UseExceptions();
        // Register GDAL for use
        gdal.AllRegister();
        // URL dir_url = ;
        initializeDataset(this.dataset1, "testRasterCompressed.tif", false, new EllipsoidalMercatorCrsProfile(), new BoundingBox(-15049605.452, 8551661.071, -15048423.068, 8552583.832));//Retrieved bounding box from cmdline gdalinfo <filename?
        initializeDataset(this.dataset2, "testRasterv2-3857WithAlpha.tif", true, new SphericalMercatorCrsProfile(), new BoundingBox(-15042794.840, 8589662.396, -15042426.875, 8590031.386));
    }

    private static void initializeDataset(final GdalUtilityTest.ImageDataProperties datasetProperties,final String fileName, final boolean hasAlpha, final CrsProfile profile, final BoundingBox bounds)
    {
        datasetProperties.imageFile   = new File(ClassLoader.getSystemResource(fileName).getFile());
        datasetProperties.dataset     = gdal.Open(datasetProperties.imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
        datasetProperties.srs         = new SpatialReference(datasetProperties.dataset.GetProjection());
        datasetProperties.crsProfile  = profile;
        datasetProperties.boundingBox = bounds;
        datasetProperties.hasAlpha    = hasAlpha;
    }

    /**
     * Tests open throws an Exception when
     * it fails to open the dataset
     */
    @Test(expected = RuntimeException.class)
    public void verifyOpenException1()
    {
        final File testFile = new File("test.tiff");
        try
        {
            GdalUtility.open(testFile);
            fail("Expected GdalUtility method open to throw an Exception when the dataset cannot be opened.");
        }
        finally
        {
            if(testFile.exists())
            {
                testFile.delete();
            }
        }
    }

    /**
     * Tests open(File)
     */
    @Test
    public void verifyOpen()
    {
        for(final GdalUtilityTest.ImageDataProperties image: this.imageList)
        {
            final Dataset datasetReturned = GdalUtility.open(image.imageFile);
            assertTrue("GdalUtility method open(File) did not open and return the data file correctly.",
                       this.areDatasetsEqual(image.dataset, datasetReturned));
        }
    }
//TODO: this test is currently failing! Come back once easier methods have been tested
//    /**
//     * Tests open(File, CoordinateReferenceSystem)
//     */
//    @Test
//    public void verifyOpen2()
//    {
//        for (final GdalUtilityTest.ImageDataProperties image : this.imageList) {
//            final Dataset datasetReturned = GdalUtility.open(image.imageFile, image.crsProfile.getCoordinateReferenceSystem());
//            assertTrue("GdalUtility method open(File, CoordinateReferenceSystem did not return the dataset correctly",
//                       this.areDatasetsEqual(image.dataset, datasetReturned));
//        }
//    }

    /**
     * Tests doesDatasetMatchCrs
     */
    //TODO: better way to compare crs's?? or change somewhere else
    @Test
    public void verifyDoesDataSetMatchCRS1()
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            assertTrue("GdalUtility method doesDataSetMatchCRS did not returned false when true was expected.",
                       GdalUtility.doesDataSetMatchCRS(imageData.dataset, GdalUtility.getCoordinateReferenceSystem(imageData.srs)));
        }
    }

    /**
     * Tests convert(Dataset) throws an
     *  IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyConvertException1()
    {
        final Dataset dataset = null;
        GdalUtility.convert(dataset);
        fail("Expected GdalUtility method convert(Dataset) to throw an IllegalArgumentException when the dataset is null");
    }

    /**
     * Tests convert(Dataset)
     */
    @Test
    public void verifyConvert1() throws IOException
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            // TODO: find way to read Tiff as buffered image
        }
    }

    /**
     * Tests hasAlpha(Dataset) throws an Exception
     * when the Dataset is null
     */

    @Test(expected = IllegalArgumentException.class)
    public void verifyHasAlphaException()
    {
        GdalUtility.hasAlpha(null);
        fail("GdalUtility method hasAlpha(Dataset) did not throw an IllegalArgumentException when given a null dataset.");
    }

    /**
     * Tests hasAlpha(Dataset)
     */
    @Test
    public void verifyHasAlphaBand()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final boolean hasAlpha = GdalUtility.hasAlpha(imageData.dataset);
            assertEquals(String.format("GdalUtility method hasAlpha(Dataset) returned %s when expected %s.",
                                       hasAlpha,
                                       imageData.hasAlpha),
                         hasAlpha,
                         imageData.hasAlpha);
        }
    }

    /**
     * Tests getSpatialReference(Dataset) throws an
     * IllegalArgumentException when the Dataset is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetDatasetSpatialReferenceFromDatasetException()
    {
        final Dataset data = null;
        GdalUtility.getSpatialReference(data);
        fail("Expected GdalUtility method getSpatialReference(Dataset) to throw an IllegalArgumentException");
    }

    /**
     * Tests getSpatialReference(Dataset)
     */
    @Test
    public void verifyGetDatasetSpatialReferenceFromDataset1()
    {
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final SpatialReference srsReturned = GdalUtility.getSpatialReference(imageData.dataset);
            this.assertSRS(imageData.srs, srsReturned);
        }
    }

    /**
     * Tests getSpatialReference(Dataset) when
     * the dataset projection is empty
     */
    @Test
    public void verifyGetDatasetSpatialReferenceFromDataset2() {
        final GCP[] testData = new GCP[1];
        testData[0] = new GCP(0, 0, 0, 0);

        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_Update);
        final String proj = dataset.GetProjection();

        try
        {
            dataset.SetGCPs(testData, proj);
            final SpatialReference srsReturned = GdalUtility.getSpatialReference(dataset);
            this.assertSRS(new SpatialReference(proj), srsReturned);
        }
        finally
        {
            dataset.SetProjection(proj);
            dataset.delete();
        }
    }
    /**
     * Tests getSpatialReference(File) throws an
     * IllegalArgumentException when the File is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetDatasetSpatialReferenceFromFileException1()
    {
        final File file = null;
        GdalUtility.getSpatialReference(file);
        fail("Expected GdalUtility method getSpatialReference(File) to throw an IllegalArgumentException");
    }

    /**
     * Tests getSpatialReference(File) throws an
     * IllegalArgumentException when the File is not readable
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetDatasetSpatialReferenceFromFileException2()
    {
        final File file = new File("test.txt");
        file.setReadable(false);

        try
        {
            GdalUtility.getSpatialReference(file);
            fail("Expected GdalUtility method getSpatialReference(File) to throw an IllegalArgumentException");
        }
        finally
        {
            if(file.exists())
            {
                file.delete();
            }
        }
    }

    /**
     * Tests getSpatialReference(File)
     */
    @Test
    public void verifyGetDatasetSpatailReferenceFromFile()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final SpatialReference srsReturned = GdalUtility.getSpatialReference(imageData.imageFile);
            this.assertSRS(imageData.srs, srsReturned);
        }
    }

    /**
     * Tests getSpatialReference(CoordinateReferenceSystem) throws an
     * IllegalArgumentException when the CoordinateReferenceSystem is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetDatasetSpatialReferenceFromCrsException()
    {
        final CoordinateReferenceSystem crs = null;
        GdalUtility.getSpatialReference(crs);
        fail("Expected GdalUtility method getSpatialReference(CoordinateReferenceSystem) to throw an IllegalArgumentException");
    }

    /**
     * Tests getSpatialReference(CoordinateReferenceSystem)
     */
    @Test
    public void verifyGetSpatialReferencFromCrs()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CoordinateReferenceSystem crs         = imageData.crsProfile.getCoordinateReferenceSystem();
            final SpatialReference          srsReturned = GdalUtility.getSpatialReference(crs);
            this.assertSRS(imageData.srs, srsReturned);
        }
    }

    /**
     * Tests getSpatialReference(CrsProfile) throws an
     * IllegalArgumentException when the CrsProfile is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetSpatialReferenceFromCrsProfileException()
    {
        final CrsProfile       profile     = null;
        GdalUtility.getSpatialReference(profile);
        fail("Expected GdalUtility method getSpatialReference(CrsProfile) to throw an IllegalArgumentException.");
    }

    /**
     * Tests getSpatialReference(CrsProfile)
     */
    @Test
    public void verifyGetSpatialReferencFromCrsProfile()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CrsProfile       profile     = imageData.crsProfile;
            final SpatialReference srsReturned = GdalUtility.getSpatialReference(profile);
            this.assertSRS(imageData.srs, srsReturned);
        }
    }

    /**
     * Tests hasGeoReference(Dataset) throws an
     * IllegalArgumentException when given a null dataset
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyDatasetHasGeoReferenceException()
    {
        final Dataset data = null;
        GdalUtility.hasGeoReference(data);
        fail("Expected GdalUtilty method hasGeoReference(Dataset) to throw an IllegalArgumentException.");
    }

    /**
     * Tests hasGeoReference(Dataset)
     */
    @Test
    public void verifyDatasetHasGeoReference1()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            assertTrue("Did not detect that images have a GeoReference.",
                       GdalUtility.hasGeoReference(imageData.dataset));
        }
    }

    /**
     * Tests hasGeoReference(Dataset)
     */
    @Test
    public void verifyDatasetHasGeoReference2()
    {
        final File testFile = new File("NonGeo.tif");
        final Dataset rawData = gdal.Open(testFile.getPath());

        try
        {
            assertFalse("Detected that image has a GeoReference",
                        GdalUtility.hasGeoReference(rawData));
        }
        finally
        {
            rawData.delete();
            //testData.delete();
        }
    }

    /**
     * Tests hasGeoReference(Dataset)
     */
    @Test
    public void verifyDatasetHasGeoReference3()
    {
        final File testFile = new File("NonGeo.tif");
        final Dataset rawData = gdal.Open(testFile.getPath(), gdalconstConstants.GA_Update);

        final double[] original = rawData.GetGeoTransform();
        final double[] geoTransform = { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
        rawData.SetGeoTransform(geoTransform);

        try
        {
            assertFalse("Detected that image has a GeoReference",
                        GdalUtility.hasGeoReference(rawData));
        }
        finally
        {
            rawData.SetGeoTransform(original);
            rawData.delete();
        }
    }

    /**
     * Tests getBounds(Dataset) throws an
     * IllegalArgumentException when given a null Dataset
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetBoundsException1() throws DataFormatException
    {
        final Dataset data = null;
        GdalUtility.getBounds(data);
        fail("Expected GdalUtility method getBounds(Dataset) to throw an IllegalArgumentException.");
    }

    /**
     * Tests getBounds(Dataset) throws an
     * IllegalArgumentException when the image is tilted
     */
    @Test(expected = DataFormatException.class)
    public void verifyGetBoundsException2() throws DataFormatException
    {
        final File rawData = new File("NonGeo.tif");
        final double[] argins = { 0.0, 1.0, 3.0, 0.0, 0.0, 1.0 };

        final Dataset testData = gdal.Open(rawData.getPath(), gdalconstConstants.GA_Update );

        final double[] original = testData.GetGeoTransform();
        testData.SetGeoTransform(argins);
        try
        {
            GdalUtility.getBounds(testData);
            fail("Expected GdalUtility method getBounds(Dataset) to throw a DataFormatException.");
        }
        finally
        {
            testData.SetGeoTransform(original);
            testData.delete();
        }
    }

    /**
     * Tests getBounds(Dataset) throws an
     * IllegalArgumentException when the image is tilted
     */
    @Test(expected = DataFormatException.class)
    public void verifyGetBoundsException3() throws DataFormatException
    {
        final File rawData = new File("NonGeo.tif");
        final double[] argins = { 0.0, 1.0, 0.0, 0.0, 5.0, 1.0 };

        final Dataset testData = gdal.Open(rawData.getPath(), gdalconstConstants.GA_Update );

        final double[] original = testData.GetGeoTransform();
        testData.SetGeoTransform(argins);
        try
        {
            GdalUtility.getBounds(testData);
            fail("Expected GdalUtility method getBounds(Dataset) to throw a DataFormatException.");
        }
        finally
        {
            testData.SetGeoTransform(original);
            testData.delete();
        }
    }
//    /**
//     * Test getBounds(dataset)
//     */
//    @Test
//    public void verifyGetBoundsForDataset() throws DataFormatException
//    {
//        final BoundingBox boundingBoxReturned = GdalUtility.getBounds(this.dataset1.dataset);
//        assertEquals(String.format("BoundingBoxes aren't equal.\nExpected: %s\nActual: %s",
//                        this.dataset1.boundingBox.toString(),
//                        boundingBoxReturned.toString()),
//                boundingBoxReturned,
//                this.dataset1.boundingBox);//TODO why is there more precision when Utility returns the boundingbox than using cmd line gdalinfo <tif> bounds
//    }

    /**
     * Tests getCoordinateReferenceSystem(SpatialReference)
     * throws and exception when the srs is null
     */
    @Test (expected = IllegalArgumentException.class)
    public void getCoordinateReferenceSystemException()
    {
        final SpatialReference srs = null;
        GdalUtility.getCoordinateReferenceSystem(srs);
        fail("Expected GdalUtility method getCoordinateReferenceSystem(SpatialReference) to throw an IllegalArgumentExeption");
    }

    /**
     * tests getCoordinateReferenceSystem(SpatialReference)
     */
    @Test
    public void verifyGetCoordinateReferenceSystem()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CoordinateReferenceSystem crs = GdalUtility.getCoordinateReferenceSystem(imageData.srs);
            final CoordinateReferenceSystem expected = imageData.crsProfile.getCoordinateReferenceSystem();

            assertEquals("GdalUtility method getCoordinateReferenceSystem did not return the correct CoordinateReferenceSystem",
                         crs,
                         expected);
        }
    }

    /**
     * Tests getName(SpatialReference)
     * throws an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetNameException()
    {
        GdalUtility.getName(null);
        fail("Expected GdalUtility method getName to throw an IllegalArgumentException when given a null SpatialReferenceSystem.");
    }
    /**
     * Tests getName(SpatialReference)
     */
    @Test
    public void verifyGetName()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final String srsNameReturned = GdalUtility.getName(imageData.srs);
            assertEquals(String.format("GdalUtility method getName(Spatial reference returned %s when expected %s",
                            srsNameReturned,
                            imageData.srs.GetAttrValue("PROJCS")),
                    imageData.srs.GetAttrValue("PROJCS"), srsNameReturned);
        }
    }

    /**
     * Tests getCrsProfile(Dataset) throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetCrsProfileException()
    {
        final Dataset dataset = null;
        GdalUtility.getCrsProfile(dataset);
        fail("Expected GdalUtility method getCrsProfile(Dataset) to throw an IllegalArgumentException.");
    }

    /**
     * Tests getCrsProfile(Dataset)
     */
    @Test
    public void verifyGetCrsProfile()
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CrsProfile image = imageData.crsProfile;
            final CrsProfile profile = GdalUtility.getCrsProfile(imageData.dataset);
            assertTrue("blah",
                    image.getDescription().equals(profile.getDescription()) &&
                            image.getName().equals(profile.getName()) &&
                            image.getWellKnownText().equals(profile.getWellKnownText()) &&
                            image.getPrecision() == profile.getPrecision() &&
                            image.getCoordinateReferenceSystem().equals(profile.getCoordinateReferenceSystem()));
        }
    }

    /**
     * Tests calculateTileRanges throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCalculateTileRangesException1()
    {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 5, 16, 16);
        final BoundingBox datasetBounds = null;
        final BoundingBox tileMatrixBounds = new BoundingBox(0,0,0,0);
        final CrsProfile crsProfile =  new SphericalMercatorCrsProfile();
        final TileOrigin origin = TileOrigin.LowerLeft;

        GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the datasetBounds is null.");
    }

    /**
     * Tests calculateTileRanges throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCalculateTileRangesException2()
    {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 5, 16, 16);
        final BoundingBox datasetBounds = new BoundingBox(0,0,0,0);
        final BoundingBox tileMatrixBounds = new BoundingBox(0,0,0,0);
        final CrsProfile crsProfile =  null;
        final TileOrigin origin = TileOrigin.LowerLeft;

        GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the datasetBounds is null.");
    }


    /**
     * Tests calculateTileRanges throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCalculateTileRangesException3()
    {
        final TileScheme tileScheme = null;
        final BoundingBox datasetBounds = new BoundingBox(0,0,0,0);
        final BoundingBox tileMatrixBounds = new BoundingBox(0,0,0,0);
        final CrsProfile crsProfile =  new SphericalMercatorCrsProfile();
        final TileOrigin origin = TileOrigin.LowerLeft;

        GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the datasetBounds is null.");
    }

    /**
     * Tests calculateTileRanges throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCalculateTileRangesException4()
    {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 5, 16, 16);
        final BoundingBox datasetBounds = new BoundingBox(0,0,0,0);
        final BoundingBox tileMatrixBounds = new BoundingBox(0,0,0,0);
        final CrsProfile crsProfile =  new SphericalMercatorCrsProfile();
        final TileOrigin origin = null;

        GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the datasetBounds is null.");
    }

    /**
     * Tests calculateTileRanges throws
     * an IllegalArgumentException
     */
    //TODO: better way to test this method
    @Test
    public void verifyCalculateTileRanges() throws DataFormatException
    {
        final GdalUtilityTest.ImageDataProperties imageData = this.imageList.get(0);

        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final BoundingBox datasetBounds = GdalUtility.getBounds(imageData.dataset);
        final BoundingBox tileMatrixBounds = imageData.crsProfile.getBounds();
        final CrsProfile crsProfile =  imageData.crsProfile;
        final TileOrigin origin = TileOrigin.LowerLeft;

        final Map<Integer, Range<Coordinate<Integer>>> map = GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);
        assertTrue("GdalUtility method calculateTileRanges did not return the correct map of zoom levels to tileCoordinates",
                map.size() == 32);
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException1()
    {
        final Dataset dataset = null;
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException.");
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException2()
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = null;
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException3()
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = null;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException4()
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException5()
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = null;

        GdalUtility.getMinimalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    //TODO: how to determine what zoom level should be returned
    @Test
    public void verifyGetMinimalZoom1()
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            GdalUtility.getMinimalZoom(imageData.dataset, tileRanges, tileOrigin, tileScheme, tileSize);
        }
    }
    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException1() throws TileStoreException
    {
        final Dataset dataset = null;
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException2() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = null;
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException3() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = null;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException4() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException5() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = null;

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }


    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    //TODO: how to determine what zoom level should be returned
    @Test
    public void verifyGetMaximalZoom1() throws TileStoreException
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                    imageData.boundingBox,
                    imageData.crsProfile.getBounds(),
                    imageData.crsProfile,
                    tileOrigin);

            GdalUtility.getMaximalZoom(imageData.dataset, tileRanges, tileOrigin, tileScheme, tileSize);
        }
    }
    /* Private helper methods */
    @SuppressWarnings("MethodMayBeStatic")
    private void assertSRS(final SpatialReference expectedSrs, final SpatialReference srsReturned)
    {
        assertTrue("The getDatasetSpatialReference method did not return the expected SpatialReference object.",
                expectedSrs.IsSame(srsReturned) == 1 &&
                        expectedSrs.IsSameGeogCS(srsReturned) == 1);/* &&
                   expectedSrs.IsSameVertCS(srsReturned) == 1);*/ //TODO: what does this method do??
    }
    @SuppressWarnings("MethodMayBeStatic")
    private boolean areDatasetsEqual(final Dataset expected, final Dataset returned)
    {
        return expected.getRasterXSize() == returned.getRasterXSize() &&
               expected.getRasterYSize() == returned.getRasterYSize() &&
               Arrays.equals(expected.GetGeoTransform(), returned.GetGeoTransform()) &&
               expected.GetRasterCount() == returned.getRasterCount();
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
