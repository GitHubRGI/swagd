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

package com.rgi.common.util;

import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * @author Luke Lambert
 *
 */
public class MimeTypeUtility
{
    /**
     * @param types the mimeTypes wanting to add to the set
     * @return a set of MimeType objects
     */
    public static Set<MimeType> createMimeTypeSet(final String... types)
    {
        Set<MimeType> imageFormats = new HashSet<>();

        for(final String type : types)
        {
            try
            {
                imageFormats.add(new MimeType(type));
            }
            catch(final MimeTypeParseException ex)
            {
                ex.printStackTrace();   // This method was specifically created to avoid checked exceptions
            }
        }
        return imageFormats;
    }
    
    /**
     * @param supportedMimeTypes the supported MimeTypes that are allowed
     * @param mimeType the mimeType tested
     * @return true if the mimeType is supported in the Set; otherwise false;
     */
    public static boolean contains(Set<MimeType> supportedMimeTypes, MimeType mimeType)
    {
        return supportedMimeTypes.stream().anyMatch(allowedImageOutputFormat-> allowedImageOutputFormat.match(mimeType));
    }
}
