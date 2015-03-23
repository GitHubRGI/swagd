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

package utility;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

/**
 * Convenience class for getting GDAL error messages
 *
 * @author Luke Lambert
 *
 */
public class GdalError
{
    /**
     * Constructor
     */
    public GdalError()
    {
        this.message = gdal.GetLastErrorMsg();
        this.number  = gdal.GetLastErrorNo();
        this.type    = gdal.GetLastErrorType();
    }

    @Override
    public String toString()
    {
        return String.format("<%s:%s> %s",
                             GdalError.gdalErrorTypeToString(this.type),
                             GdalError.gdalErrorNumberToString(this.number),
                             this.message);
    }

    /**
     * @return The last GDAL error as a string
     */
    public static String lastError()
    {
       return (new GdalError()).toString();
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * @return the number
     */
    public int getNumber()
    {
        return this.number;
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return this.type;
    }

    /**
     * Maps a GDAL error constant to a human readable string
     *
     * @param number
     *             GDAL error constant
     * @return A string containing the human readable equivalent of the error
     *             number
     */
    public static String gdalErrorNumberToString(final int number)
    {
        // Having this as an if-else is ridiculous, but Java says that
        // gdalconstConstants are not "constant expressions" and therefore
        // cannot be used as switch cases.

        if(number == gdalconstConstants.CPLE_AppDefined)
        {
            return "Application Defined";
        }
        else if(number == gdalconstConstants.CPLE_AssertionFailed)
        {
            return "Assertion Failed";
        }
        else if(number == gdalconstConstants.CPLE_FileIO)
        {
            return "File IO";
        }
        else if(number == gdalconstConstants.CPLE_IllegalArg)
        {
            return "Illegal Argument";
        }
        else if(number == gdalconstConstants.CPLE_None)
        {
            return "None";
        }
        else if(number == gdalconstConstants.CPLE_NotSupported)
        {
            return "Not Supported";
        }
        else if(number == gdalconstConstants.CPLE_NoWriteAccess)
        {
            return "No Write Access";
        }
        else if(number == gdalconstConstants.CPLE_OpenFailed)
        {
            return "Open Failed";
        }
        else if(number == gdalconstConstants.CPLE_OutOfMemory)
        {
            return "Out Of Memory";
        }
        else if(number == gdalconstConstants.CPLE_UserInterrupt)
        {
            return "User Interrupt";
        }
        else
        {
            return "Unrecognized GDAL Error Number";
        }
    }

    /**
     * Maps GDAL error types to human readable strings
     *
     * @param type
     *             GDAL error type constant
     * @return A string containing the human readable equivalent of the error
     *             type
     */
    public static String gdalErrorTypeToString(final int type)
    {
        if(type == gdalconstConstants.CE_Debug)
        {
            return "Debug";
        }
        else if(type == gdalconstConstants.CE_Failure)
        {
            return "Failure";
        }
        else if(type == gdalconstConstants.CE_Fatal)
        {
            return "Fatal";
        }
        else if(type == gdalconstConstants.CE_None)
        {
            return "None";
        }
        else if(type == gdalconstConstants.CE_Warning)
        {
            return "Warning";
        }
        else
        {
            return "Unrecognized GDAL Error Type";
        }
    }

    private final String message;
    private final int    number;
    private final int    type;

}
