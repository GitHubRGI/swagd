package utility.tests;

import static org.junit.Assert.assertTrue;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
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
        gdal.Error(gdalconst.CE_Warning, errorCode, message);
        final GdalError error = new GdalError();

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
    @SuppressWarnings("static-method")
    @Test
    public void testToString()
    {
        final String message = "testing";
        gdal.Error(gdalconst.CE_None, gdalconst.CPLE_AppDefined, message);

        final GdalError error = new GdalError();
        final String expected = "<None:Application Defined> testing";

        assertTrue("GdalError toString method did not return the correct String",
                   error.toString().equals(expected));
    }

    /**
     * Tests GdalError lastError
     */
    @SuppressWarnings("static-method")
    @Test
    public void testlastError()
    {
        final String message = "testing";
        gdal.Error(gdalconst.CE_None, gdalconst.CPLE_AppDefined, message);

        final String expected = "<None:Application Defined> testing";
        final String returned = GdalError.lastError();

        assertTrue("GdalError toString method did not return the correct String",
                   returned.equals(expected));
    }
    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString1()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_AppDefined);
        final String expected = "Application Defined";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }


    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString2()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_AssertionFailed);
        final String expected = "Assertion Failed";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString3()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_FileIO);
        final String expected = "File IO";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString4()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_IllegalArg);
        final String expected = "Illegal Argument";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString5()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_None);
        final String expected = "None";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString6()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_NotSupported);
        final String expected = "Not Supported";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString7()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_NoWriteAccess);
        final String expected = "No Write Access";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString8()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_OpenFailed);
        final String expected = "Open Failed";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString9()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_OutOfMemory);
        final String expected = "Out Of Memory";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                  returned,
                                  expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorNumberToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorNumberToString10()
    {
        final String returned = GdalError.gdalErrorNumberToString(gdalconst.CPLE_UserInterrupt);
        final String expected = "User Interrupt";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
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

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString1()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Debug);
        final String expected = "Debug";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString2()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Failure);
        final String expected = "Failure";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString3()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Fatal);
        final String expected = "Fatal";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString4()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_None);
        final String expected = "None";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }

    /**
     * Tests gdalErrorTypeToString
     */
    @SuppressWarnings("static-method")
    @Test
    public void testGdalErrorTypeToString5()
    {
        final String returned = GdalError.gdalErrorTypeToString(gdalconst.CE_Warning);
        final String expected = "Warning";

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
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

        assertTrue(String.format("gdalErrorNumberToString returned %s, but %s was expected.",
                                 returned,
                                 expected),
                   returned.equals(expected));
    }
}
