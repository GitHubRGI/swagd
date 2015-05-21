package utility;

import static org.junit.Assert.*;

import java.io.File;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Test;

public class GdalErrorTest {

	@Test
	public void GdalErrorMessage(){
			File testFile = new File(ClassLoader.getSystemResource("test.tif").getFile());
			Dataset dset   = gdal.Open(testFile.getAbsolutePath());
			if(dset == null){
				assertTrue("Gdal error did not return the expected message.",gdal.GetLastErrorMsg().equals(new GdalError().getMessage())); 
			}
			else{
				fail("Expected gdal to have an error");
			}
				


	}
}
