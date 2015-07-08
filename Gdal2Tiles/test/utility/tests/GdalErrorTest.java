package utility.tests;

import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.junit.Test;
import utility.GdalError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GdalErrorTest
{
    /**
     * Tests GdalError constructor
     */
    @Test
    public void testGdalErrorConstructor()
    {
        final String message = "testing";
        final int errorCode = 12;
        gdal.Error(gdalconst.CE_Warning, errorCode, message);
        GdalError error = new GdalError();

        assertTrue("GdalError constructor did not correctly set the GdalError message.",
                   error.getMessage().equals(message));

        assertTrue("GdalError constructor did not correctly set the GdalError number",
                   error.getNumber() == errorCode);

        assertTrue("GdalError constructor did not correctly set the GdalError type",
                   error.getType() == gdalconst.CE_Warning);
    }

    /**
     * Tests GdalError toString
     */
    @Test
    public void testToString()
    {
        final String message = "testing";
        gdal.Error(gdalconst.CE_None, gdalconst.CPLE_AppDefined, message);

        GdalError error = new GdalError();
        String expected = "<None:Application Defined> testing";

        assertTrue("GdalError toString method did not return the correct String",
                   error.toString().equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString1()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_AppDefined);
        String expected = "Application Defined";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString2()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_AssertionFailed);
        String expected = "Assertion Failed";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString3()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_FileIO);
        String expected = "File IO";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString4()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_IllegalArg);
        String expected = "Illegal Argument";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString5()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_None);
        String expected = "None";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString6()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_NotSupported);
        String expected = "Not Supported";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString7()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_NoWriteAccess);
        String expected = "No Write Access";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString8()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_OpenFailed);
        String expected = "Open Failed";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString9()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_OutOfMemory);
        String expected = "Out Of Memory";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                  returned,
                                  expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString10()
    {
        String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_UserInterrupt);
        String expected = "User Interrupt";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @Test
    public void testGdalErrorNumberToString11()
    {
        String returned = GdalError.gdalErrorNumberToString(-100);
        String expected = "Unrecognized GDAL Error Number";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString1()
    {
        String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Debug);
        String expected = "Debug";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString2()
    {
        String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Failure);
        String expected = "Failure";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString3()
    {
        String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Fatal);
        String expected = "Fatal";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString4()
    {
        String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_None);
        String expected = "None";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString5()
    {
        String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Warning);
        String expected = "Warning";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @Test
    public void testGdalErrorTypeToString6()
    {
        String returned = GdalError.gdalErrorTypeToString(-100);
        String expected = "Unrecognized GDAL Error Type";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }
}
