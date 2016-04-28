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
// @formatter: off
package utility;

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
import com.rgi.g2t.RawImageTileReader;
import com.rgi.g2t.TilingException;
import com.rgi.store.tiles.TileStoreException;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.GCP;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import static org.junit.Assert.*;

/**
 * @author Luke D. Lambert
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 *
 */
@SuppressWarnings("MagicNumber")
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
        Double[]          noDataValue;
    }

    private final GdalUtilityTest.ImageDataProperties dataset1 = new GdalUtilityTest.ImageDataProperties();
    private final GdalUtilityTest.ImageDataProperties dataset2 = new  GdalUtilityTest.ImageDataProperties();
    private final List<GdalUtilityTest.ImageDataProperties> imageList = Arrays.asList(this.dataset1, this.dataset2);
    private final Double[] noDataValues = {0.0, 0.0, 0.0, 0.0};


    @Before
    public void setUp() throws URISyntaxException
    {
        osr.UseExceptions();
        // Register GDAL for use
        gdal.AllRegister();
        // URL dir_url = ;
        initializeDataset(this.dataset1, "testRasterCompressed.tif", false, new EllipsoidalMercatorCrsProfile(), new BoundingBox(-15049605.452, 8551661.071, -15048423.068, 8552583.832), new Double[0]);//Retrieved bounding box from cmdline gdalinfo <filename?
        initializeDataset(this.dataset2, "testRasterv2-3857WithAlpha.tif", true, new SphericalMercatorCrsProfile(), new BoundingBox(-15042794.840, 8589662.396, -15042426.875, 8590031.386), this.noDataValues);
    }

    private static void initializeDataset(final GdalUtilityTest.ImageDataProperties datasetProperties,final String fileName, final boolean hasAlpha, final CrsProfile profile, final BoundingBox bounds, final Double[] noData) throws URISyntaxException
    {
        // In order to provide GdalUtility.open() a good File object, the File object must be made in this manner
        // You CANNOT simply make a new File object using the ClassLoader, because the File object will have encoding
        // that prohibits gdal.Open() from working correctly when spaces are part of the file path
        datasetProperties.imageFile   = TestUtility.loadFileFromDisk(fileName);
        datasetProperties.dataset     = gdal.Open(datasetProperties.imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);
        datasetProperties.srs         = new SpatialReference(datasetProperties.dataset.GetProjection());
        datasetProperties.crsProfile  = profile;
        datasetProperties.boundingBox = bounds;
        datasetProperties.hasAlpha    = hasAlpha;
        datasetProperties.noDataValue = noData;
    }

    /**
     * Tests open throws a RuntimeException when
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
    public void verifyOpen1()
    {
        for(final GdalUtilityTest.ImageDataProperties image: this.imageList)
        {
            final Dataset datasetReturned = GdalUtility.open(image.imageFile);
            assertTrue("GdalUtility method open(File) did not open and return the data file correctly.",
                       this.areDatasetsEqual(image.dataset, datasetReturned));
        }
    }

    //TODO: this test is currently failing! Possibly due to small differences in wkt
    // failing because the two geotransform arrays are different
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
     * Tests open(File, CoordinateReferenceSystem)
     */
    @Test
    public void verifyOpen3()
    {
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", 4326);
        for(final GdalUtilityTest.ImageDataProperties image: this.imageList)
        {
            final Dataset datasetReturned = GdalUtility.open(image.imageFile, crs);
            assertTrue("GdalUtility method open(File) did not open and return the data file correctly.",
                       GdalUtility.doesDataSetMatchCRS(datasetReturned, crs));

            datasetReturned.delete();
        }
    }

    /**
     * Tests doesDatasetMatchCrs returns true
     * when the dataset matches the CoordinateReferenceSystem
     */
    @Test
    public void verifyDoesDataSetMatchCRS1()
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            assertTrue("GdalUtility method doesDataSetMatchCRS did not returned false when true was expected.",
                       GdalUtility.doesDataSetMatchCRS(imageData.dataset, imageData.crsProfile.getCoordinateReferenceSystem()));
        }
    }

    /**
     * Tests doesDatasetMatchCrs returns false when
     * the dataset does not match the CoordinateReferenceSystem
     */
    @Test
    public void verifyDoesDataSetMatchCRS2()
    {
        final CoordinateReferenceSystem crs = new CoordinateReferenceSystem("EPSG", 4326);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            assertFalse("GdalUtility method doesDataSetMatchCRS did not returned false when true was expected.",
                        GdalUtility.doesDataSetMatchCRS(imageData.dataset, crs));
        }
    }

    /**
     * Tests convert(Dataset) throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyConvertException1()
    {
        final Dataset dataset = null;
        GdalUtility.convert(dataset);
        fail("Expected GdalUtility method convert(Dataset) to throw an IllegalArgumentException when the dataset is null");
    }

    /**
     * Tests convert(Dataset) throws a
     *  RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void verifyConvertException2()
    {
        final Dataset dataset = gdal.GetDriverByName("MEM").Create("prueba", 100, 100, 0);
        GdalUtility.convert(dataset);
        fail("Expected GdalUtility method convert(Dataset) to throw a RuntimeException when the dataset has no bands");
    }

    /**
     * Tests convert(Dataset)
     */
    @Test
    public void verifyConvert1() throws IOException
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Dataset dataset = imageData.dataset;
            final BufferedImage bufferedImage = GdalUtility.convert(dataset);
            assertTrue("GdalUtility method convert(Dataset) did not return a valid BufferedImage.",
                       bufferedImage != null &&
                       bufferedImage.isAlphaPremultiplied() == imageData.hasAlpha &&
                       bufferedImage.getWidth() == dataset.getRasterXSize() &&
                       bufferedImage.getHeight() == dataset.getRasterYSize());
        }
    }

    /**
     * Tests convert(Dataset)
     */
    @Test
    public void verifyConvert2() throws IOException
    {
        final Dataset dataset = gdal.GetDriverByName("MEM").Create("prueba", 100, 100, 1);
        final BufferedImage bufferedImage = GdalUtility.convert(dataset);
        assertTrue("GdalUtility method convert(Dataset) did not return a valid BufferedImage.",
                   bufferedImage != null &&
                   !bufferedImage.isAlphaPremultiplied());
    }

    /**
     * Tests getSpatialReference(Dataset) throws an
     * IllegalArgumentException when the Dataset is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetSpatialReferenceFromDatasetException()
    {
        final Dataset data = null;
        GdalUtility.getSpatialReference(data);
        fail("Expected GdalUtility method getSpatialReference(Dataset) to throw an IllegalArgumentException");
    }

    /**
     * Tests getSpatialReference(Dataset)
     */
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void verifyGetSpatialReferenceFromDataset1()
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
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void verifyGetSpatialReferenceFromDataset2() throws URISyntaxException {
        final GCP[] testData = new GCP[1];
        testData[0] = new GCP(0, 0, 0, 0);

        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.toString(), gdalconstConstants.GA_Update);
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
    public void verifyGetSpatialReferenceFromFileException1()
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
    public void verifyGetSpatialReferenceFromFileException2()
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
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void verifyGetSpatialReferenceFromFile()
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
    public void verifyGetSpatialReferenceFromCrsException()
    {
        final CoordinateReferenceSystem crs = null;
        GdalUtility.getSpatialReference(crs);
        fail("Expected GdalUtility method getSpatialReference(CoordinateReferenceSystem) to throw an IllegalArgumentException");
    }

    /**
     * Tests getSpatialReference(CoordinateReferenceSystem)
     */
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void verifyGetSpatialReferencFromCrs()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CoordinateReferenceSystem crs         = imageData.crsProfile.getCoordinateReferenceSystem();
            final SpatialReference srsReturned = GdalUtility.getSpatialReference(crs);
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
        final CrsProfile profile = null;
        GdalUtility.getSpatialReference(profile);
        fail("Expected GdalUtility method getSpatialReference(CrsProfile) to throw an IllegalArgumentException.");
    }

    /**
     * Tests getSpatialReference(CrsProfile)
     */
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void verifyGetSpatialReferencFromCrsProfile()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final CrsProfile profile = imageData.crsProfile;
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
     * Tests hasGeoReference(Dataset) with
     * dataset that has no georeference
     */
    @Test
    public void verifyDatasetHasGeoReference2() throws URISyntaxException
    {
        final File testFile = TestUtility.loadFileFromDisk("NonGeo.tif");
        final Dataset rawData = gdal.Open(testFile.toString());

        try
        {
            assertFalse("Detected that image has a GeoReference",
                        GdalUtility.hasGeoReference(rawData));
        }
        finally
        {
            rawData.delete();
        }
    }

    /**
     * Tests hasGeoReference(Dataset)
     */
    @Test
    public void verifyDatasetHasGeoReference3() throws URISyntaxException
    {
        final File testFile = TestUtility.loadFileFromDisk("NonGeo.tif");
        final Dataset rawData = gdal.Open(testFile.toString(), gdalconstConstants.GA_Update);

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
    public void verifyGetBoundsException2() throws DataFormatException, URISyntaxException
    {
        final File rawData = TestUtility.loadFileFromDisk("NonGeo.tif");
        final double[] argins = { 0.0, 1.0, 3.0, 0.0, 0.0, 1.0 };

        final Dataset testData = gdal.Open(rawData.toString(), gdalconstConstants.GA_Update );

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
    public void verifyGetBoundsException3() throws DataFormatException, URISyntaxException
    {
        final File rawData = TestUtility.loadFileFromDisk("NonGeo.tif");
        final double[] argins = { 0.0, 1.0, 0.0, 0.0, 5.0, 1.0 };

        final Dataset testData = gdal.Open(rawData.toString(), gdalconstConstants.GA_Update );

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
     * Test getBounds(dataset)
     */
    @Test
    public void verifyGetBoundsForDataset() throws DataFormatException
    {
        final BoundingBox boundingBoxReturned = GdalUtility.getBounds(this.dataset1.dataset);
        assertTrue(String.format("BoundingBoxes aren't equal.\nExpected: %s\nActual: %s",
                                 this.dataset1.boundingBox.toString(),
                                 boundingBoxReturned.toString()),
                   this.areBoxesEqual(this.dataset1.boundingBox, boundingBoxReturned));
    }

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
     * Tests getCoordinateReferenceSystem(SpatialReference)
     */
    @Test
    public void verifyGetCoordinateReferenceSystem1()
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
        fail("Expected GdalUtility method getName(SpatialReference) to throw an IllegalArgumentException when given a null SpatialReferenceSystem.");
    }

    /**
     * Tests getName(SpatialReference)
     */
    @Test
    public void verifyGetName1()
    {
        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final String srsNameReturned = GdalUtility.getName(imageData.srs);
            assertEquals(String.format("GdalUtility method getName(SpatialReference) returned %s when expected %s",
                                       srsNameReturned,
                                       imageData.srs.GetAttrValue("PROJCS")),
                         imageData.srs.GetAttrValue("PROJCS"),
                         srsNameReturned);
        }
    }

    /**
     * Tests getName(SpatialReference)
     * returns null when the SpatialReference is empty
     */
    @Test
    public void verifyGetName2()
    {
        final SpatialReference srs = new SpatialReference();

        assertNull("Expected GdalUtility method getName(SpatialReference) to return null.",
                   GdalUtility.getName(srs));
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

            assertTrue("GdalUtility method getCrsProfile(Dataset) did not return the correct CrsProfile",
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
        final BoundingBox tileMatrixBounds = new BoundingBox(0, 0, 0, 0);
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
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the CrsProfile is null.");
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
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the TileScheme is null.");
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
        fail("Expected GdalUtility method calculateTileRanges to throw an IllegalArgumentException when the TileOrigin is null.");
    }

    /**
     * Tests calculateTileRanges
     */
    @Test
    public void verifyCalculateTileRanges() throws DataFormatException
    {
        final GdalUtilityTest.ImageDataProperties imageData = this.imageList.get(0);

        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final BoundingBox datasetBounds = imageData.boundingBox;
        final BoundingBox tileMatrixBounds = imageData.crsProfile.getBounds();
        final CrsProfile crsProfile =  imageData.crsProfile;
        final TileOrigin origin = TileOrigin.LowerLeft;

        final Map<Integer, Range<Coordinate<Integer>>> map = GdalUtility.calculateTileRanges(tileScheme, datasetBounds, tileMatrixBounds, crsProfile, origin);

        final int minZoom = GdalUtility.getMinimalZoom( map);
        final Range<Coordinate<Integer>> range = map.get(minZoom);
        final Coordinate<Integer> left = range.getMinimum();
        final Coordinate<Integer> right = range.getMaximum();

        assertTrue("GdalUtility method calculateTileRanges did not return the correct map of zoom levels to tileCoordinates",
                   map.size() == 32 &&
                   Math.abs(right.getX() - left.getX()) < 2 &&
                   Math.abs(right.getY() - left.getY()) < 2);
    }

    /**
     * Tests getMinimalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException1()
    {
        GdalUtility.getMinimalZoom(null);
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException when the TileRange is null.");
    }

    /**
     * Tests getMinimalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomException2()
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        GdalUtility.getMinimalZoom(tileRanges );
        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException when the TileRange is empty.");
    }

    /**
     * Tests getMinimalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMinimalZoomNoSingleTile()
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer>                      topLeft    = new Coordinate<>(0, 2);
        final Coordinate<Integer>                      lowerRight = new Coordinate<>(2, 0);
        final Range<Coordinate<Integer>>               range      = new Range<>(topLeft, lowerRight);
        tileRanges.put(0,range);
        GdalUtility.getMinimalZoom(tileRanges);

        fail("Expected GdalUtility method getMinimalZoom to throw an IllegalArgumentException when the Tile Range at level 0 is more than 1 tile.");
    }

    /**
     * Tests getMinimalZoom
     */
    @Test
    public void verifyGetMinimalZoom1() throws TileStoreException
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

            final int min = GdalUtility.getMinimalZoom(tileRanges);

            try (final RawImageTileReader reader = new RawImageTileReader(imageData.imageFile, imageData.dataset, tileSize, null, null))
            {
                final long tiles = reader.stream(min).count();
                assertTrue("GdalUtility method getMinimalZoom did not return a valid minimal zoom.",
                           tiles == 1 || tiles == 2);
            }
        }
    }

    /**
     * Tests getMinimalZoom
     */
    @Test
    public void verifyGetMinimalZoom2() throws TileStoreException
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> tileSize = new Dimensions<>(512, 256);

        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList) {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            final int min = GdalUtility.getMinimalZoom( tileRanges);

            try (final RawImageTileReader reader = new RawImageTileReader(imageData.imageFile, imageData.dataset, tileSize, null, null)) {
                final long tiles = reader.stream(min).count();
                assertTrue("GdalUtility method getMinimalZoom did not return a valid minimal zoom.",
                           tiles == 1 || tiles == 2);
            }
        }
    }

    /**
     * Tests getMaximalZoom throws an
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
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests getMaximalZoom throws an
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
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException when the Map of tile ranges is null.");

        dataset.delete();
    }

    /**
     * Tests getMaximalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException3() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException when the Map of tile ranges is empty.");

        dataset.delete();
    }

    /**
     * Tests getMaximalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException4() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final TileOrigin tileOrigin = null;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException when the TileOrigin is null.");

        dataset.delete();
    }

    /**
     * Tests getMaximalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException5() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException when the TileScheme is null.");

        dataset.delete();
    }

    /**
     * Tests getMinimalZoom(Dataset) throws an
     * IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyGetMaximalZoomException6() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> dimensions = null;

        GdalUtility.getMaximalZoom(dataset, tileRanges, tileOrigin, tileScheme, dimensions);
        fail("Expected GdalUtility method getMaximalZoom to throw an IllegalArgumentException.");

        dataset.delete();
    }

    /**
     * Tests getMaximalZoom throws an
     * IllegalArgumentException
     */
    @Test (expected = TileStoreException.class)
    public void verifyGetMaximalZoomException7() throws TileStoreException
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final Dimensions<Integer> tileSize = new Dimensions<>(0, 0);

        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            GdalUtility.getMaximalZoom(imageData.dataset, tileRanges, tileOrigin, tileScheme, tileSize);

            fail("Expected GdalUtility method getMaximalZoom to throw a TileStoreException.");
        }
    }

    /**
     * Tests getMaximalZoom throws an
     * IllegalArgumentException
     */
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

            final int max = GdalUtility.getMaximalZoom(imageData.dataset, tileRanges, tileOrigin, tileScheme, tileSize);

            assertTrue("GdalUtility method getMaximalZoom did not return a valid maximal zoom.",
                       max >= 0 &&
                       max < 32);
        }
    }

    /**
     * Tests getZoomLevels throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetZoomLevelsException1() throws TileStoreException
    {
        final Dataset dataset = null;
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.getZoomLevels(dataset, tileOrigin, tileSize);
        fail("Expected GdalUtility method getZoomLevels to throw an IllegalArgumentException.");
    }

    /**
     * Tests getZoomLevels throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetZoomLevelsException2() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final TileOrigin tileOrigin = null;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.getZoomLevels(dataset, tileOrigin, tileSize);
        fail("Expected GdalUtility method getZoomLevels to throw an IllegalArgumentException.");
        dataset.delete();
    }

    /**
     * Tests getZoomLevels throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetZoomLevelsException3() throws TileStoreException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = null;

        GdalUtility.getZoomLevels(dataset, tileOrigin, tileSize);
        fail("Expected GdalUtility method getZoomLevels to throw an IllegalArgumentException.");
        dataset.delete();
    }

    /**
     * Tests getZoomLevels
     */
    @Test(expected = TileStoreException.class)
    public void verifyGetZoomLevelsException4() throws TileStoreException
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(0, 0);

        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            GdalUtility.getZoomLevels(imageData.dataset, tileOrigin, tileSize);
            fail("Expected GdalUtility method getZoomLevels to throw a TileStoreException.");
        }
    }

    /**
     * Tests getZoomLevels
     */
    @Test
    public void verifyGetZoomLevels() throws TileStoreException
    {
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        for(final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Set<Integer> zooms = GdalUtility.getZoomLevels(imageData.dataset, tileOrigin, tileSize);

            assertTrue("GdalUtility method getZoomLevels did not return a valid set of zoom levels",
                       zooms.parallelStream().noneMatch(zoom -> zoom < 0 || zoom > 32));
        }
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException1() throws TileStoreException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = null;
        final Dataset dataset = null;
        final CrsProfile crsProfile = new SphericalMercatorCrsProfile();
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when Map is null.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException2() throws TileStoreException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Dataset dataset = null;
        final CrsProfile crsProfile = new SphericalMercatorCrsProfile();
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the Map is empty.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException3() throws TileStoreException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final Dataset dataset = null;
        final CrsProfile crsProfile = new SphericalMercatorCrsProfile();
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the dataset is null.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException4() throws TileStoreException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final CrsProfile crsProfile = null;
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the CrsProfile is null.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException5() throws TileStoreException, URISyntaxException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0, 0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final CrsProfile crsProfile = new EllipsoidalMercatorCrsProfile();
        final TileScheme tileScheme = null;
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the TileScheme is null.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException6() throws TileStoreException, URISyntaxException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final CrsProfile crsProfile = new EllipsoidalMercatorCrsProfile();
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = null;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the TileOrigin is null.");
    }

    /**
     * Tests zoomLevelForPixelSize throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyZoomLevelForPixelSizeException7() throws TileStoreException, URISyntaxException
    {
        final Map<Integer, Range<Coordinate<Integer>>> tileRanges = new HashMap<>(100);
        final Coordinate<Integer> coordinate = new Coordinate<>(0,0);
        tileRanges.put(0,new Range<>(coordinate, coordinate));

        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final CrsProfile crsProfile = new EllipsoidalMercatorCrsProfile();
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = null;

        GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, dataset, crsProfile, tileScheme, tileOrigin, tileSize);
        fail("Expected GdalUtility method zoomLevelForPixelSize to throw an IllegalArgumentException when the TileSize is null.");
    }


    /**
     * Tests verifyZoomLevelForPixelSize throws
     * a TileStoreException when cannot find a zoomLevel
     */
    @Test(expected = TileStoreException.class)
    public void verifyZoomLevelForPixelSizeException8() throws TileStoreException
    {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(0, 0);
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, imageData.dataset, imageData.crsProfile, tileScheme, tileOrigin, tileSize);

            fail("Expected GdalUtility method zoomeLevelForPixelSize to throw a TileStoreException.");
        }
    }

    /**
     * Tests verifyZoomLevelForPixelSize returns a
     * valid zoom level
     */
    @Test
    public void verifyZoomLevelForPixelSize1() throws TileStoreException
    {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            final int zoom = GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, imageData.dataset, imageData.crsProfile, tileScheme, tileOrigin, tileSize);

            final Set<Integer> zoomLevels = GdalUtility.getZoomLevels(imageData.dataset, tileOrigin, tileSize);

            assertTrue("GdalUtility method zoomLevelForPIxelSize did not return a valid zoom level.",
                       zoom >= 0 &&
                       zoom < 32 &&
                       zoomLevels.contains(zoom));
        }
    }

    /**
     * Tests verifyZoomLevelForPixelSize properly returns
     * the pixel size
     */
    @Test
    public void verifyZoomLevelForPixelSize2() throws TileStoreException {
        final TileScheme tileScheme = new ZoomTimesTwo(0, 31, 1, 1);
        final TileOrigin tileOrigin = TileOrigin.LowerLeft;
        final Dimensions<Integer> tileSize = new Dimensions<>(256, 512);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final Map<Integer, Range<Coordinate<Integer>>> tileRanges = GdalUtility.calculateTileRanges(tileScheme,
                                                                                                        imageData.boundingBox,
                                                                                                        imageData.crsProfile.getBounds(),
                                                                                                        imageData.crsProfile,
                                                                                                        tileOrigin);

            final int zoom = GdalUtility.zoomLevelForPixelSize(1.0, tileRanges, imageData.dataset, imageData.crsProfile, tileScheme, tileOrigin, tileSize);

            final Set<Integer> zoomLevels = GdalUtility.getZoomLevels(imageData.dataset, tileOrigin, tileSize);

            assertTrue("GdalUtility method zoomLevelForPIxelSize did not return a valid zoom level.",
                       zoom >= 0 &&
                       zoom < 32 &&
                       zoomLevels.contains(zoom));
        }
    }

        /**
         * Tests warpDatasetToSrs throws
         * an IllegalArgumentException
         */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWarpDatasetToSrsException1()
    {
        final Dataset dataset = null;
        final SpatialReference fromSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3395));
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));

        GdalUtility.warpDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method warpDatasetToSrs to throw an IllegalArgumentException when the dataset is null.");
    }

    /**
     * Tests warpDatasetToSrs throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWarpDatasetToSrsException2() throws URISyntaxException
    {
        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final SpatialReference fromSrs = null;
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));

        GdalUtility.warpDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method warpDatasetToSrs to throw an IllegalArgumentException when the from SpatialReference is null.");
        dataset.delete();
    }


    /**
     * Tests warpDatasetToSrs throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWarpDatasetToSrsException3() throws URISyntaxException
    {
        final File imageFile = TestUtility.loadFileFromDisk("testRasterCompressed.tif");
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final SpatialReference fromSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));
        final SpatialReference toSrs = null;

        GdalUtility.warpDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method warpDatasetToSrs to throw an IllegalArgumentException when the To SpatialReference is null.");
        dataset.delete();
    }

    /**
     * Tests verifyWarpDatasetToSrs correctly
     * warps datasets to the given SpatialReference
     */
    @Test
    public void verifyWarpDatasetToSrs1()
    {
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3395));
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Dataset returned = GdalUtility.warpDatasetToSrs(imageData.dataset, imageData.srs,toSrs);
            assertTrue("GdalUtility method warpDatasetToSrs did not correctly warp the dataset.",
                       toSrs.GetAttrValue("PROJCS").equals(GdalUtility.getName(GdalUtility.getSpatialReference(returned))) &&
                       returned.getRasterCount() == imageData.dataset.getRasterCount());
            returned.delete();
        }
    }

    /**
     * Tests verifyWarpDatasetToSrs correctly
     * throws a GdalError when the warping fails
     */
    @Test(expected = RuntimeException.class)
    public void verifyWarpDatasetToSrs2()
    {
        final File testFile = new File("NonGeo.tif");
        final Dataset dataset = gdal.Open(testFile.getPath());

        final SpatialReference fromSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3395));
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));

        try
        {
            GdalUtility.warpDatasetToSrs(dataset, fromSrs, toSrs);
            fail("Expected GdalUtility method warpDatasetToSrs to throw a RuntimeException.");
        }
        finally
        {
            dataset.delete();
        }
    }

    /**
     * Tests reprojectDatasetToSrs throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyReprojectDatasetToSrsException1() throws IOException, TilingException
    {
        final Dataset dataset = null;
        final SpatialReference fromSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3395));
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));

        GdalUtility.reprojectDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method reprojectDatasetToSrs to throw an IllegalArgumentException when the dataset is null.");
    }

    /**
     * Tests reprojectDatasetToSrs throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyReprojectDatasetToSrsException2() throws IOException, TilingException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final SpatialReference fromSrs = null;
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));

        GdalUtility.reprojectDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method reprojectDatasetToSrs to throw an IllegalArgumentException when the From SpatialReference is null.");
        dataset.delete();
    }

    /**
     * Tests reprojectDatasetToSrs throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyReprojectDatasetToSrsException3() throws IOException, TilingException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final SpatialReference fromSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3857));
        final SpatialReference toSrs = null;

        GdalUtility.reprojectDatasetToSrs(dataset, fromSrs, toSrs);
        fail("Expected GdalUtility method reprojectDatasetToSrs to throw an IllegalArgumentException when to To SpatialReference is null.");
        dataset.delete();
    }

    /**
     * Tests reprojectDatasetToSrs correctly
     * reprojects the given dataset
     */
    @Test
    public void verifyReprojectDatasetToSrs() throws IOException, TilingException
    {
        final SpatialReference toSrs = GdalUtility.getSpatialReference(new CoordinateReferenceSystem("EPSG", 3395));
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Dataset returned = GdalUtility.reprojectDatasetToSrs(imageData.dataset, imageData.srs, toSrs);

            assertTrue("GdalUtility method reprojectDatasetToSrs did not correctly warp the dataset.",
                       toSrs.GetAttrValue("PROJCS").equals(GdalUtility.getName(GdalUtility.getSpatialReference(returned))) &&
                       returned.getRasterCount() == imageData.dataset.getRasterCount());
            returned.delete();
        }
    }

    /**
     * Tests that scaleQueryToTileSize(Dataset, Dimensions)
     * throws an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyScaleQueryToTileSizeException1() throws TilingException
    {
        final Dataset dataset = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(1024, 1024);

        GdalUtility.scaleQueryToTileSize(dataset, dimensions);
        fail("Expected GdalUtility method scaleQueryToTileSize(Dataset, Dimensions) to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**s
     * Tests that scaleQueryToTileSize(Dataset, Dimensions)
     * throws an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyScaleQueryToTileSizeException2() throws TilingException
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Dimensions<Integer> dimensions = null;

        GdalUtility.scaleQueryToTileSize(dataset, dimensions);
        fail("Expected GdalUtility method scaleQueryToTileSize(Dataset, Dimensions) to throw an IllegalArgumentException when the Dimensions are null.");
        dataset.delete();
    }

    /**
     * Tests scaleQueryToSize(Dataset, Dimensions)
     */
    @Test
    public void verifyScaleQueryToTileSize1() throws TilingException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 234);
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Dataset returned = GdalUtility.scaleQueryToTileSize(imageData.dataset, dimensions);

            assertTrue("GdalUtility method scaleQueryToTileSize(Dataset, Dimensions) did not correctly downsize the dataset",
                       returned.getRasterXSize() == 256 &&
                       returned.getRasterYSize() == 234 &&
                       returned.getRasterCount() == imageData.dataset.getRasterCount());
            returned.delete();
        }
    }

    /**
     * Tests getNoDataValues(Dataset)
     * throws an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetNoDataValuesException()
    {
        final Dataset dataset = null;

        GdalUtility.getNoDataValues(dataset);
        fail("Expected GdalUtility method getNoDataValues(Dataset) to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Test getNoDataValues(Dataset) correctly returns
     * the noDataValues (if any)
     */
    @Test
    public void verifyGetNoDataValues()
    {
        for (final GdalUtilityTest.ImageDataProperties imageData: this.imageList)
        {
            final Double[] noData = GdalUtility.getNoDataValues(imageData.dataset);
            assertTrue("GdalUtility method getNoDataValues(Dataset) returned no data values for a dataset that does not have NODATA",
                        Arrays.equals(noData, imageData.noDataValue));

        }
    }

    /**
     * Tests getRasterBandCount(Dataset, Band) throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetRasterBandCountException1() throws TileStoreException
    {
        final Dataset dataset= null;
        final Band alphaBand = this.dataset1.dataset.GetRasterBand(0);

        GdalUtility.getRasterBandCount(dataset, alphaBand);
        fail("Expected GdalUtility method getRasterBandCount(Dataset, Band) to throw an IllegalArgumentException when the Dataset is null");
    }

    /**
     * Tests getRasterBandCount(Dataset, Band) throws an
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetRasterBandCountException2() throws TileStoreException
    {
        final Dataset dataset= this.dataset1.dataset;
        final Band alphaBand = null;

        GdalUtility.getRasterBandCount(dataset, alphaBand);
        fail("Expected GdalUtility method getRasterBandCount(Dataset, Band) to throw an IllegalArgumentException when the Band is null");
    }

    /**
     * Tests getRasterBandCount(Dataset, Band) correctly
     * returns the rasterBand count
     */
    @Test
    public void verifyGetRasterBandCount1() throws TileStoreException
    {
        final Dataset dataset = this.dataset2.dataset;
        final int index = GdalUtility.getAlphaBandIndex(dataset);
        final Band alphaBand = dataset.GetRasterBand(index);
        final int returnedCount = GdalUtility.getRasterBandCount(dataset, alphaBand);
        final int expectedCount = 3;

        assertEquals("GdalUtility method getRasterBandCount(Dataset, Band) did not return the correct number of bands",
                     expectedCount,
                     returnedCount);
    }

    /**
     * Tests getAlphaBandIndex(Dataset) throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetAlphaBandIndexException1() throws TileStoreException
    {
        GdalUtility.getAlphaBandIndex(null);
        fail("Expected GdalUtility method getAlphaBandIndex(Dataset) to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests getAlphaBandIndex(Dataset) throws
     * a TileStoreException
     */
    @Test(expected = TileStoreException.class)
    public void verifyGetAlphaBandIndexException2() throws TileStoreException
    {
        GdalUtility.getAlphaBandIndex(this.dataset1.dataset);
        fail("Expected GdalUtility method getAlphaBandIndex(Dataset) to throw a TileStoreException when the Dataset has no AlphaBand.");
    }

    /**
     * Tests getAlphaBandIndex
     */
    @Test
    public void verifyGetAlphaBandIndex1() throws TileStoreException
    {
        final Dataset dataset = this.dataset2.dataset;

        final int returned = GdalUtility.getAlphaBandIndex(dataset);
        final int expected = 4; // determined using gdalinfo cmdline tool

        assertEquals("GdalUtility method getAlphaBandIndex did not return the correct number of bands.",
                     expected,
                     returned);
    }

    /**
     * Tests correctNoDataSimple(Dataset)
     * throws an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyCorrectNoDataSimpleException()
    {
        final Dataset dataset = null;
        GdalUtility.correctNoDataSimple(dataset);
        fail("Expected GdalUtility method correctNoDataSimple(Dataset) to throw an IllegalArgumentException when the Dataset is null.");
    }

// TODO: fix correctNoDataSimple in GdalUtility
//    /**
//     * Tests correctNoDataSimple with
//     * a dataset that has no alpha band
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void verifyCorrectNoDataSimple1()
//    {
//        final Dataset dataset = this.dataset1.dataset; //Dataset with no alpha band
//        final Dataset virtualDataset = gdal.AutoCreateWarpedVRT(dataset);
//
//        final Dataset returned = GdalUtility.correctNoDataSimple(virtualDataset);
//
//        assertTrue("GdalUtility method correctNoDataSimple(Dataset) did not return the correct dataset.",
//                   Arrays.equals(GdalUtility.getNoDataValues(dataset), new Double[0]));
//    }

    /**
     * Tests correctNoDataSimple with
     * a dataset that has no alpha band
     */
    @Test
    public void verifyCorrectNoDataSimple2()
    {
        final Dataset dataset = this.dataset2.dataset; //Dataset with alpha band

        final Dataset returned = GdalUtility.correctNoDataSimple(dataset);

        assertTrue("GdalUtility method correctNoDataSimple(Dataset) did not return the correct dataset.",
                   this.areDatasetsEqual(dataset, returned) &&
                   Arrays.equals(GdalUtility.getNoDataValues(dataset), this.dataset2.noDataValue));
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
    public void verifyHasAlpha()
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
     * Tests getGdalRasterParameters throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetGdalRasterParametersException1()
    {
        final double[] geoTransform = new double[0];
        final BoundingBox boundingBox = new BoundingBox(0,0,0,0);
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 10);
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.getGdalRasterParameters(geoTransform, boundingBox, dimensions, dataset);
        fail("Expected GdalUtility method getGdalRasterParameters to throw an IllegalArgumentException when the array has length 0.");
    }

    /**
     * Tests getGdalRasterParameters throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetGdalRasterParametersException2()
    {
        final double[] geoTransform = new double[12];
        final BoundingBox boundingBox = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 10);
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.getGdalRasterParameters(geoTransform, boundingBox, dimensions, dataset);
        fail("Expected GdalUtility method getGdalRasterParameters to throw an IllegalArgumentException when the BoundingBox is null.");
    }

    /**
     * Tests getGdalRasterParameters throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetGdalRasterParametersException3()
    {
        final double[] geoTransform = new double[12];
        final BoundingBox boundingBox = new BoundingBox(0, 0, 0, 0);
        final Dimensions<Integer> dimensions = null;
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.getGdalRasterParameters(geoTransform, boundingBox, dimensions, dataset);
        fail("Expected GdalUtility method getGdalRasterParameters to throw an IllegalArgumentException when the Dimensions are null.");
    }

    /**
     * Tests getGdalRasterParameters throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGetGdalRasterParametersException4()
    {
        final double[] geoTransform = new double[12];
        final BoundingBox boundingBox =  new BoundingBox(0, 0, 0, 0);
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 10);
        final Dataset dataset = null;

        GdalUtility.getGdalRasterParameters(geoTransform, boundingBox, dimensions, dataset);
        fail("Expected GdalUtility method getGdalRasterParameters to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests that getGdalRasterParameters
     */
    @Test
    public void verifyGetGdalRasterParameters()
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final double[] geoTransform = imageData.dataset.GetGeoTransform();
            final BoundingBox boundingBox = imageData.boundingBox;

            final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(geoTransform,
                                                                                                boundingBox,
                                                                                                dimensions,
                                                                                                imageData.dataset);
            /* Created Expected GdalRasterParameters */
            final int readX = (int)((boundingBox.getMinimumX() - geoTransform[0]) / geoTransform[1] + 0.001);
            final int readY = (int)((boundingBox.getMaximumY() - geoTransform[3]) / geoTransform[5] + 0.001);

            final int readXSize = (int)(boundingBox.getWidth()  /  geoTransform[1] + 0.5);
            final int readYSize = (int)(boundingBox.getHeight() / -geoTransform[5] + 0.5);

            final GdalUtility.GdalRasterParameters expected = new GdalUtility.GdalRasterParameters(readX,
                                                                                                   readY,
                                                                                                   readXSize,
                                                                                                   readYSize,
                                                                                                   dimensions,
                                                                                                   imageData.dataset);

            assertTrue("GdalUtility method getGdalRasterParameters did not return the GdalRasterParametesr object correctly.",
                       expected.getReadX() == params.getReadX() &&
                       expected.getReadY() == params.getReadY() &&
                       expected.getReadXSize() == params.getReadXSize() &&
                       expected.getReadYSize() == params.getReadYSize() &&
                       expected.getQueryXSize() == params.getQueryXSize() &&
                       expected.getQueryYSize() == params.getQueryYSize() &&
                       expected.getWriteX() == params.getWriteX() &&
                       expected.getWriteY() == params.getWriteY() &&
                       expected.getWriteXSize() == params.getWriteXSize() &&
                       expected.getWriteYSize() == params.getWriteYSize());
        }
    }

    /**
     * Tests readRaster(GdalRasterParams, Dataset)
     * throws an IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyReadRasterException1() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = null;
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.readRaster(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRaster(GdalRasterParameters, Dataset) to throw an IllegalArgumentException when the GdalRasterParameters are null.");
    }

    /**
     * Tests readRaster(GdalRasterParams, Dataset)
     * throws an IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyReadRasterException2() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0,
                                                                                                           0,
                                                                                                           0,
                                                                                                           0,
                                                                                                           new Dimensions<>(0, 0),
                                                                                                           this.dataset1.dataset);
        final Dataset dataset = null;

        GdalUtility.readRaster(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRaster(GdalRasterParameters, Dataset) to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests readRaster(GdalRasterParams, Dataset)
     * throws an IOException
     */
    @Test (expected = IOException.class)
    public void verifyReadRasterException3() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0, 0, -100, 100, new Dimensions<>(0, 0), this.dataset1.dataset);
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.readRaster(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRaster(GdalRasterParameters, Dataset) to throw an IOException.");
    }

    /**
     * Tests readRaster(GdalRasterParams, Dataset)
     * throws an IOException
     */
    @Test (expected = IOException.class)
    public void verifyReadRasterException4() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0, 0, 0, 0, new Dimensions<>(-10, -12), this.dataset1.dataset);
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.readRaster(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRaster(GdalRasterParameters, Dataset) to throw an IOException.");
    }

    /**
     * Tests readRaster(GdalRasterParams, Dataset)
     */
    @Test
    public void verifyReadRaster1() throws IOException, TilingException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(imageData.dataset.GetGeoTransform(),
                                                                                                imageData.boundingBox,
                                                                                                dimensions,
                                                                                                imageData.dataset);

            final byte[] bytes = GdalUtility.readRaster(params, imageData.dataset);

            assertTrue("GdalUtility method readRaster(GdalRasterParameters, Dataset) did not return a valid byte array.",
                       bytes.length > 0);
        }
    }

    /**
     * Tests readRasterDirect(GdalRasterParams, Dataset)
     * throws an IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyReadRasterDirectException1() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = null;
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.readRasterDirect(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRasterDirect(GdalRasterParameters, Dataset) to throw an IllegalArgumentException.");
    }

    /**
     * Tests readRasterDirect(GdalRasterParams, Dataset)
     * throws an IllegalArgumentException
     */
    @Test (expected = IllegalArgumentException.class)
    public void verifyReadRasterDirectException2() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0, 0, 0, 0, new Dimensions<>(0, 0), this.dataset1.dataset);
        final Dataset dataset = null;

        GdalUtility.readRasterDirect(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRasterDirect(GdalRasterParameters, Dataset) to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests readRasterDirect(GdalRasterParams, Dataset)
     * throws a TilingException
     */
    @Test (expected = TilingException.class)
    public void verifyReadRasterDirectException3() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0, 0, -100, 100, new Dimensions<>(0, 0), this.dataset1.dataset);
        final Dataset dataset = this.dataset1.dataset;

        GdalUtility.readRasterDirect(gdalRasterParameters, dataset);
        fail("Expected GdalUtility method readRasterDirect(GdalRasterParameters, Dataset) to throw an IllegalArgumentException.");
    }

    /**
     * Tests readRasterDirect(GdalRasterParams, Dataset)
     */
    @Test
    public void verifyReadRasterDirect1() throws IOException, TilingException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(imageData.dataset.GetGeoTransform(),
                                                                                                imageData.boundingBox,
                                                                                                dimensions,
                                                                                                imageData.dataset);

            final ByteBuffer buffer = GdalUtility.readRasterDirect(params, imageData.dataset);

            final byte[] bytes  = GdalUtility.readRaster(params, imageData.dataset);
            final ByteBuffer expected = ByteBuffer.wrap(bytes);

            assertTrue("GdalUtility method readRasterDirect(GdalRasterParameters, Dataset) did not return the correct ByteBuffer.",
                       buffer != null &&
                       buffer.equals(expected));
        }
    }

    /**
     * Tests writeRaster throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWriteRasterException1() throws TilingException
    {
        final GdalUtility.GdalRasterParameters params = null;
        final byte[] imageData = new byte[10];
        final int bandCount = 3;

        GdalUtility.writeRaster(params, imageData, bandCount);
        fail("Expected GdalUtility method writeRaster to throw an IllegalArgumentException when the GdalRasterParameters are null.");
    }

    /**
     * Tests writeRaster throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWriteRasterException2() throws TilingException
    {
        final Dataset dataset = this.dataset1.dataset;
        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(0, 0, 0, 0, new Dimensions<>(0,0), dataset);
        final byte[] imageData = new byte[0];
        final int bandCount = 3;

        GdalUtility.writeRaster(params, imageData, bandCount);
        fail("Expected GdalUtility method writeRaster to throw an IllegalArgumentException when the byte array is null.");
    }

    /**
     * Tests writeRaster
     * throws a TilingException
     */
    @Test (expected = TilingException.class)
    public void verifyWriteRasterException3() throws IOException, TilingException
    {
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(0,
                                                                                                           0,
                                                                                                           100,
                                                                                                           100,
                                                                                                           new Dimensions<>(256, 256),
                                                                                                           this.dataset1.dataset);
        final byte[] imageData = new byte[10];
        final int bandCount = 3;

        GdalUtility.writeRaster(gdalRasterParameters, imageData, bandCount);
        fail("Expected GdalUtility method writeRaster to throw a TilingException when it fails to write the tile data to an output Dataset.");
    }

    /**
     * Tests writeRaster
     */
    @Test
    public void verifyWriteRaster() throws IOException, TilingException, DataFormatException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(imageData.dataset.GetGeoTransform(),
                                                                                                imageData.boundingBox,
                                                                                                dimensions,
                                                                                                imageData.dataset);

            final byte[] readData = GdalUtility.readRaster(params, imageData.dataset);
            final byte[] writeData = new byte[readData.length];

            final Dataset dataset = GdalUtility.writeRaster(params, writeData, imageData.dataset.getRasterCount());
            dataset.SetGeoTransform(imageData.dataset.GetGeoTransform());

            final GdalUtility.GdalRasterParameters writeParams = GdalUtility.getGdalRasterParameters(dataset.GetGeoTransform(),
                                                                                                     GdalUtility.getBounds(dataset),
                                                                                                     dimensions,
                                                                                                     dataset);

            assertTrue("GdalUtility method writeRaster did not correctly write to and return a dataset",
                       Arrays.equals(writeData, GdalUtility.readRaster(writeParams, dataset)));
        }
    }

    /**
     * Tests writeRasterDirect throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWriteRasterDirectException1() throws TilingException
    {
        final GdalUtility.GdalRasterParameters params = null;
        final ByteBuffer imageData = ByteBuffer.wrap(new byte[3]);
        final int bandCount = 3;

        GdalUtility.writeRasterDirect(params, imageData, bandCount);
        fail("Expected GdalUtility method writeRasterDirect to throw an IllegalArgumentException when the GdalRasterParameters are null.");
    }

    /**
     * Tests writeRasterDirect throws
     * an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyWriteRasterDirectException2() throws TilingException
    {
        final Dataset dataset = this.dataset1.dataset;
        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(0, 0, 0, 0, new Dimensions<>(0, 0), dataset);
        final ByteBuffer imageData = null;
        final int bandCount = 3;

        GdalUtility.writeRasterDirect(params, imageData, bandCount);
        fail("Expected GdalUtility method writeRasterDirect to throw an IllegalArgumentException when the ByteBuffer is null.");
    }

    /**
     * Tests writeRasterDirect
     * throws a TilingException
     */
    @Test (expected = TilingException.class)
    public void verifyWriteRasterDirectException3() throws IOException, TilingException
    {
        final Dataset dataset = this.dataset1.dataset;
        final GdalUtility.GdalRasterParameters gdalRasterParameters = new GdalUtility.GdalRasterParameters(-10, -10, 100, 100, new Dimensions<>(10, 10), dataset);
        final ByteBuffer imageData = ByteBuffer.allocateDirect(10);
        final int bandCount = 3;

        GdalUtility.writeRasterDirect(gdalRasterParameters, imageData, bandCount);
        fail("Expected GdalUtility method writeRasterDirect to throw a TilingException when it fails to write the ByteBuffer data to a Dataset.");
    }

    /**
     * Tests writeRasterDirect
     */
    @Test
    public void verifyWriteRasterDirect() throws IOException, TilingException, DataFormatException
    {
        final Dimensions<Integer> dimensions = new Dimensions<>(256, 256);
        for (final GdalUtilityTest.ImageDataProperties imageData : this.imageList)
        {
            final GdalUtility.GdalRasterParameters params = GdalUtility.getGdalRasterParameters(imageData.dataset.GetGeoTransform(),
                                                                                                imageData.boundingBox,
                                                                                                dimensions,
                                                                                                imageData.dataset);

            final ByteBuffer readData = GdalUtility.readRasterDirect(params, imageData.dataset);
            final ByteBuffer writeData = ByteBuffer.allocateDirect(readData.capacity());


            final Dataset dataset = GdalUtility.writeRasterDirect(params, writeData, imageData.dataset.getRasterCount());
            dataset.SetGeoTransform(imageData.dataset.GetGeoTransform());

            final GdalUtility.GdalRasterParameters writeParams = GdalUtility.getGdalRasterParameters(dataset.GetGeoTransform(),
                                                                                                     GdalUtility.getBounds(dataset),
                                                                                                     dimensions,
                                                                                                     dataset);

            assertEquals("GdalUtility method writeRasterDirect did not correctly write to and return a dataset",
                         writeData,
                         GdalUtility.readRasterDirect(writeParams, dataset));
        }
    }

    /**
     * Tests the GdalRasterParameters constructor
     * throws an IllehalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGdalRasterParametersException1()
    {
        final File imageFile = new File(ClassLoader.getSystemResource("testRasterCompressed.tif").getFile());
        final Dataset dataset = gdal.Open(imageFile.getAbsolutePath(), gdalconstConstants.GA_ReadOnly);

        final Dimensions<Integer> dimensions = null;

        new GdalUtility.GdalRasterParameters(0, 0, 0, 0, dimensions, dataset);
        fail("Expected GdalRasterParameters constructor to throw an IllegalArgumentException when the Dimensions are null.");
        dataset.delete();
    }

    /**
     * Tests the GdalRasterParameters constructor
     * throws an IllehalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void verifyGdalRasterParametersException2()
    {
        final Dataset dataset = null;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 10);

        new GdalUtility.GdalRasterParameters(0, 1, 2, 3, dimensions, dataset);
        fail("Expected GdalRasterParameters constructor to throw an IllegalArgumentException when the Dataset is null.");
    }

    /**
     * Tests the GdalRasterParameters construcotr
     * correctly create a GdalRasterParameters object
     */
    @Test
    public void verifyGdalRasterParameters1()
    {
        final Dataset dataset = this.dataset2.dataset;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 12);

        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(0, 1, 2, 3, dimensions, dataset);
        final int writeX = 0;
        final int writeY = 0;
        final int queryXSize = 40;
        final int queryYSize = 48;
        final int writeXSize = 40;
        final int writeYSize = 48;

        assertTrue("GdalRasterParameters did not correctly create a GdalRasterParameter object.",
                   params.getReadX() == 0 &&
                   params.getReadY() == 1 &&
                   params.getReadXSize() == 2 &&
                   params.getReadYSize() == 3 &&
                   params.getWriteX() == writeX &&
                   params.getWriteY() == writeY &&
                   params.getQueryXSize() == queryXSize &&
                   params.getQueryYSize() == queryYSize &&
                   params.getWriteXSize() == writeXSize &&
                   params.getWriteYSize() == writeYSize);
    }

    /**
     * Tests the GdalRasterParameters construcotr
     * correctly create a GdalRasterParameters object
     */
    @Test
    public void verifyGdalRasterParameters2()
    {
        final Dataset dataset = this.dataset2.dataset;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 12);

        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(-12, 1, 2, 3, dimensions, dataset);

        int readXSize = 2;
        int writeXSize = 40;

        /* Adjust x values */
        final int writeX = (int) (writeXSize * ((float) 12 / readXSize));
        writeXSize -= writeX;
        readXSize -= (int)(readXSize * (float)12 / readXSize);

        final int writeYSize = 48;
        final int queryYSize = 48;
        final int queryXSize = 40;
        final int writeY = 0;
        final int readYSize = 3;
        final int readY = 1;
        final int readX = 0;

        assertTrue("GdalRasterParameters did not correctly create a GdalRasterParameter object.",
                   params.getReadX() == readX &&
                   params.getReadY() == readY &&
                   params.getReadXSize() == readXSize &&
                   params.getReadYSize() == readYSize &&
                   params.getWriteX() == writeX &&
                   params.getWriteY() == writeY &&
                   params.getQueryXSize() == queryXSize &&
                   params.getQueryYSize() == queryYSize &&
                   params.getWriteXSize() == writeXSize &&
                   params.getWriteYSize() == writeYSize);
    }

    /**
     * Tests the GdalRasterParameters construcotr
     * correctly create a GdalRasterParameters object
     */
    @Test
    public void verifyGdalRasterParameters3()
    {
        final Dataset dataset = this.dataset2.dataset;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 12);

        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(1, -12, 2, 3, dimensions, dataset);

        int readYSize = 3;
        int writeYSize = 48;

        /* Adjust y values */
        final int writeY = (int) (writeYSize * ((float) 12 / readYSize));
        writeYSize -= writeY;
        readYSize -= (int)(readYSize * (float)12 / readYSize);

        final int writeXSize = 40;
        final int queryYSize = 48;
        final int queryXSize = 40;
        final int writeX = 0;
        final int readXSize = 2;
        final int readX = 1;
        final int readY = 0;

        assertTrue("GdalRasterParameters did not correctly create a GdalRasterParameter object.",
                   params.getReadX() == readX &&
                   params.getReadY() == readY &&
                   params.getReadXSize() == readXSize &&
                   params.getReadYSize() == readYSize &&
                   params.getWriteX() == writeX &&
                   params.getWriteY() == writeY &&
                   params.getQueryXSize() == queryXSize &&
                   params.getQueryYSize() == queryYSize &&
                   params.getWriteXSize() == writeXSize &&
                   params.getWriteYSize() == writeYSize);
    }

    /**
     * Tests the GdalRasterParameters construcotr
     * correctly create a GdalRasterParameters object
     */
    @Test
    public void verifyGdalRasterParameters4()
    {
        final Dataset dataset = this.dataset2.dataset;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 12);

        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(500, 400, 300, 3, dimensions, dataset);

        final int readX = 500;
        int writeXSize = 40;
        int readXSize = 300;

        /* adjust x values */
        writeXSize *= (float) (dataset.GetRasterXSize() - readX) / readXSize;
        readXSize = dataset.GetRasterXSize() - readX;

        final int readY = 400;
        final int readYSize = 3;
        final int writeX = 0;
        final int writeY = 0;
        final int queryXSize = 40;
        final int queryYSize = 48;
        final int writeYSize = 48;

        assertTrue("GdalRasterParameters did not correctly create a GdalRasterParameter object.",
                   params.getReadX() == readX &&
                   params.getReadY() == readY &&
                   params.getReadXSize() == readXSize&&
                   params.getReadYSize() == readYSize &&
                   params.getWriteX() == writeX &&
                   params.getWriteY() == writeY &&
                   params.getQueryXSize() == queryXSize &&
                   params.getQueryYSize() == queryYSize &&
                   params.getWriteXSize() == writeXSize &&
                   params.getWriteYSize() == writeYSize);
    }

    /**
     * Tests the GdalRasterParameters construcotr
     * correctly create a GdalRasterParameters object
     */
    @Test
    public void verifyGdalRasterParameters5()
    {
        final Dataset dataset = this.dataset2.dataset;
        final Dimensions<Integer> dimensions = new Dimensions<>(10, 12);

        final GdalUtility.GdalRasterParameters params = new GdalUtility.GdalRasterParameters(500, 400, 2, 500, dimensions, dataset);

        final int readY = 400;
        int writeYSize = 48;
        int readYSize = 500;

        /* adjust x values */
        writeYSize *= (float) (dataset.GetRasterXSize() - readY) / readYSize;
        readYSize = dataset.GetRasterYSize() - readY;

        final int readX = 500;
        final int readXSize = 2;
        final int writeX = 0;
        final int writeY = 0;
        final int queryXSize = 40;
        final int queryYSize = 48;
        final int writeXSize = 40;

        assertTrue("GdalRasterParameters did not correctly create a GdalRasterParameter object.",
                   params.getReadX() == readX &&
                   params.getReadY() == readY &&
                   params.getReadXSize() == readXSize&&
                   params.getReadYSize() == readYSize &&
                   params.getWriteX() == writeX &&
                   params.getWriteY() == writeY &&
                   params.getQueryXSize() == queryXSize &&
                   params.getQueryYSize() == queryYSize &&
                   params.getWriteXSize() == writeXSize &&
                   params.getWriteYSize() == writeYSize);
    }

    /* Private helper methods */
    @SuppressWarnings("MethodMayBeStatic")
    private void assertSRS(final SpatialReference expectedSrs, final SpatialReference srsReturned)
    {
        assertTrue("The getDatasetSpatialReference method did not return the expected SpatialReference object.",
                   expectedSrs.IsSame(srsReturned) == 1 &&
                   expectedSrs.IsSameGeogCS(srsReturned) == 1); /*&&
                expectedSrs.IsSameVertCS(srsReturned) == 1);*/ // 5 tests that use this method give SpatialReferences that do not have VertCS's
    }

    @SuppressWarnings("MethodMayBeStatic")
    private boolean areDatasetsEqual(final Dataset expected, final Dataset returned)
    {
        return expected.getRasterXSize() == returned.getRasterXSize() &&
               expected.getRasterYSize() == returned.getRasterYSize() &&
               Arrays.equals(expected.GetGeoTransform(), returned.GetGeoTransform()) &&
               expected.GetRasterCount() == returned.getRasterCount();
    }

    @SuppressWarnings("MethodMayBeStatic")
    private boolean areBoxesEqual(final BoundingBox b1, final BoundingBox b2)
    {
        final double epsilon = 0.001;
        return (b1.getMaximumX() == b2.getMaximumX() || StrictMath.abs(b1.getMaximumX() - b2.getMaximumX()) < epsilon) &&
               (b1.getMaximumY() == b2.getMaximumY() || StrictMath.abs(b1.getMaximumY() - b2.getMaximumY()) < epsilon) &&
               (b1.getMinimumX() == b2.getMinimumX() || StrictMath.abs(b1.getMinimumX() - b2.getMinimumX()) < epsilon) &&
               (b1.getMinimumY() == b2.getMinimumY() || StrictMath.abs(b1.getMinimumY() - b2.getMinimumY()) < epsilon);
    }

}
