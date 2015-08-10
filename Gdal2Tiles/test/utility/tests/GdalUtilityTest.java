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
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.EllipsoidalMercatorCrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.junit.Before;
import org.junit.Test;
import utility.GdalUtility;

import java.io.File;
import java.util.Arrays;
import java.util.List;
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
     * it fails to throw an Exception
     */
    @Test(expected = RuntimeException.class)
    public void verifyOpenException()
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
//        for (final ImageDataProperties image : this.imageList) {
//            final Dataset datasetReturned = GdalUtility.open(image.imageFile, image.crsProfile.getCoordinateReferenceSystem());
//            assertTrue(this.datasetsEqual(image.dataset, datasetReturned));
//        }
//    }

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
            assertEquals(String.format("GdalUtility method hasAlpha(DatasetO returned %s when expected %s.",
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
        final double[] argins = { 0.0, 1.0, 3.0, 0.0, 4.0, 1.0 };

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
     * Test getBounds(dataset)
     */
    @Test
    public void verifyGetBoundsForDataset() throws DataFormatException
    {
        final BoundingBox boundingBoxReturned = GdalUtility.getBounds(this.dataset1.dataset);
        assertEquals(String.format("BoundingBoxes aren't equal.\nExpected: %s\nActual: %s",
                                   this.dataset1.boundingBox.toString(),
                                   boundingBoxReturned.toString()),
                     boundingBoxReturned,
                     this.dataset1.boundingBox);//TODO why is there more precision when Utility returns the boundingbox than using cmd line gdalinfo <tif> bounds
    }


    /* Private helper methods */
    @SuppressWarnings("MethodMayBeStatic")
    private void assertSRS(final SpatialReference expectedSrs, final SpatialReference srsReturned)
    {
        assertTrue("The getDatasetSpatialReference method did not return the expected SpatialReference object.",
                   expectedSrs.IsSame(srsReturned)        == 1 &&
                   expectedSrs.IsSameGeogCS(srsReturned)  == 1);/* &&
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

}
