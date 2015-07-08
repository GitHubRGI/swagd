package utility.tests;

import org.gdal.gdal.Dataset;
import org.junit.Test;
import utility.GdalUtility;

import java.io.File;

public class GdalUtilityTest
{
    final static File rawData = new File("test.tif");
    @Test
    public void testReprojectImage()
    {
        //problem when reprojecting from 3395 to 3857 causes a null pointer exception
        //in reprojectImage
    }

}
