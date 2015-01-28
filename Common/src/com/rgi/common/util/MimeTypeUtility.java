package com.rgi.common.util;

import java.util.HashSet;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MimeTypeUtility
{
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
}
