package utility.tests;

import static org.junit.Assert.assertEquals;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.junit.Test;

import utility.GdalError;

public class GdalErrorTest
{
    /**
     * Tests GdalError constructor
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorConstructor()
    {
        final String message = "testing";
        final int errorCode = 12;
        gdal.Error(gdalconstConstants.CE_Warning, errorCode, message);
        final GdalError error = new GdalError();

        assertEquals("GdalError constructor did not correctly set the GdalError message.",
                message, error.getMessage());

        assertEquals("GdalError constructor did not correctly set the GdalError number",
                     errorCode,
                     error.getNumber());

        assertEquals("GdalError constructor did not correctly set the GdalError type",
                     gdalconstConstants.CE_Warning,
                     error.getType());
    }

    /**
     * Tests GdalError toString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testToString()
    {
        final String message = "testing";
        gdal.Error(gdalconstConstants.CE_None, gdalconstConstants.CPLE_AppDefined, message);

        final GdalError error = new GdalError();
        final String expected = "<None:Application Defined> testing";

        assertEquals("GdalError toString method did not return the correct String",
                     expected,
                     error.toString());
    }

    /**
     * Tests GdalError lastError
     */
    @SuppressWarnings("static-method")
    @Test
    public void testlastError()
    {
        final String message = "testing";
        gdal.Error(gdalconstConstants.CE_None, gdalconstConstants.CPLE_AppDefined, message);

        final String expected = "<None:Application Defined> testing";
        final String returned = GdalError.lastError();

        assertEquals("GdalError toString method did not return the correct String",
                expected,
                returned);
    }
    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString1()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_AppDefined);
        final String expected = "Application Defined";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                        returned,
                        expected),
                expected,
                returned);
    }


    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString2()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_AssertionFailed);
        final String expected = "Assertion Failed";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString3()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_FileIO);
        final String expected = "File IO";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString4()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_IllegalArg);
        final String expected = "Illegal Argument";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString5()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_None);
        final String expected = "None";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString6()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_NotSupported);
        final String expected = "Not Supported";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString7()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_NoWriteAccess);
        final String expected = "No Write Access";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString8()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_OpenFailed);
        final String expected = "Open Failed";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString9()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_OutOfMemory);
        final String expected = "Out Of Memory";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString10()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconstConstants.CPLE_UserInterrupt);
        final String expected = "User Interrupt";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString11()
    {
        final String returned = GdalError.gdalErrorNumberToString(-100);
        final String expected = "Unrecognized GDAL Error Number";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString1()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconstConstants.CE_Debug);
        final String expected = "Debug";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                        returned,
                        expected),
                expected,
                returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString2()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconstConstants.CE_Failure);
        final String expected = "Failure";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString3()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconstConstants.CE_Fatal);
        final String expected = "Fatal";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString4()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconstConstants.CE_None);
        final String expected = "None";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString5()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconstConstants.CE_Warning);
        final String expected = "Warning";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString6()
    {
        final String returned = GdalError.gdalErrorTypeToString(-100);
        final String expected = "Unrecognized GDAL Error Type";

        assertEquals(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                   returned,
                                   expected),
                     expected,
                     returned);
    }
}
