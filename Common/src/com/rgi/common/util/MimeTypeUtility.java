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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * Utility to simplify the creation and interaction with sets of
 * {@link MimeType}s
 *
 * @author Luke Lambert
 *
 */
public class MimeTypeUtility
{
    /**
     * Create a set of {@link MimeType} objects from their corresponding string
     * designations
     *
     * @param types
     *             Mime type strings
     * @return A set of MimeType objects
     */
    public static Set<MimeType> createMimeTypeSet(final String... types)
    {
        final Set<MimeType> imageFormats = new HashSet<>();

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
     * Checks if a specific {@link MimeType} is in a collection of mime types
     *
     * @param mimeTypes
     *             A collection of {@link MimeType}s
     * @param mimeType
     *             The mime type to test
     * @return True if the mime type is in the collection
     */
    public static boolean contains(final Collection<MimeType> mimeTypes, final MimeType mimeType)
    {
        return mimeTypes.stream().anyMatch(allowedImageOutputFormat-> allowedImageOutputFormat.match(mimeType));
    }
}
