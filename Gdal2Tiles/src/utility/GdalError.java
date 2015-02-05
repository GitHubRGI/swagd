/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package utility;

import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

public class GdalError
{
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
