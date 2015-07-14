package utility.tests;

import com.rgi.common.BoundingBox;
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Luke D. Lambert
 * @author Jenifer Cochran
 * @author Mary Carome
 *
 *
 */
public class GdalUtilityTest
{
    File tiffFile;
    BufferedImage tiffImage;
    Dataset testDatasetNoAlpha;

    public class ImageDataProperties
    {
        Dataset           dataset;
        File              imageFile;
        SpatialReference  srs;
        boolean           hasAlpha;
        CrsProfile        crsProfile;
        BoundingBox       boundingBox;
    }

    ImageDataProperties dataset1 = new ImageDataProperties();
    ImageDataProperties dataset2 = new ImageDataProperties();
    List<ImageDataProperties> imageList = Arrays.asList(this.dataset1, this.dataset2);

    @Before
    public void setUp()
    {
        osr.UseExceptions();
        // Register GDAL for use
        gdal.AllRegister();
        // URL dir_url = ;
        this.initializeDataset(this.dataset1, "testRasterCompressed.tif", false, new EllipsoidalMercatorCrsProfile(), new BoundingBox(-15049605.452, 8551661.071, -15048423.068, 8552583.832));//Retrieved bounding box from cmdline gdalinfo <filename?
        this.initializeDataset(this.dataset2, "testRasterv2-3857WithAlpha.tif", true, new SphericalMercatorCrsProfile(), new BoundingBox(-15042794.840, 8589662.396, -15042426.875, 8590031.386));
    }

    private void initializeDataset(final ImageDataProperties datasetProperties,final String fileName, final boolean hasAlpha, final CrsProfile profile, final BoundingBox bounds)
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
        final File test = new File("test.tiff");
        try
        {
            GdalUtility.open(test);
            fail("Expected GdalUtility method open to throw an Exception when the dataset cannot be opened.");
        }
        finally
        {
            test.delete();
        }
    }

    /**
     * Tests open(File)
     */
    @Test
    public void verifyOpen()
    {
        for(final ImageDataProperties image: this.imageList)
        {
            final Dataset datasetReturned = GdalUtility.open(image.imageFile);
            assertTrue(this.datasetsEqual(image.dataset, datasetReturned));
        }
    }

    /**
     * Tests open(File, CoordinateReferenceSystem)
     */
    @Test
    public void verifyOpen2()
    {
        for (final ImageDataProperties image : this.imageList) {
            final Dataset datasetReturned = GdalUtility.open(image.imageFile, image.crsProfile.getCoordinateReferenceSystem());
            assertTrue(this.datasetsEqual(image.dataset, datasetReturned));
        }
    }

    /**
     * Tests getName(SpatialReference)
     */
    @Test
    public void verifyGetName()
    {
        for(final ImageDataProperties imageData: this.imageList)
        {
            final String srsNameReturned = GdalUtility.getName(imageData.srs);
            assertEquals(imageData.srs.GetAttrValue("PROJCS"), srsNameReturned);
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
        fail("Expected GdalUtility method hasAlpha(Dataset) did not throw an IllegalArgumentException when given a null dataset.");
    }
    /**
     * Tests hasAlpha(Dataset)
     */
    @Test
    public void verifyHasAlphaBand()
    {
        for(final ImageDataProperties imageData: this.imageList)
        {
            final boolean hasAlpha = GdalUtility.hasAlpha(imageData.dataset);
            assertTrue(String.format("The method datasetHasAlpha returned %s when expected %s.", hasAlpha, imageData.hasAlpha),hasAlpha == imageData.hasAlpha);
        }
    }

    private boolean datasetsEqual(final Dataset expected, final Dataset returned)
    {
        return expected.getRasterXSize() == returned.getRasterXSize() &&
               expected.getRasterYSize() == returned.getRasterYSize() &&
              //  Arrays.equals(expected.GetGeoTransform(), returned.GetGeoTransform()) &&
                expected.GetRasterCount() == returned.getRasterCount();
    }

}
