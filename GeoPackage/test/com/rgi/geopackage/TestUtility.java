package com.rgi.geopackage;

import java.io.File;
import java.io.IOException;

/**
 * @author Luke Lambert
 */
@SuppressWarnings("JavaDoc")
public final class TestUtility
{
    private TestUtility() {}

    public static void deleteFile(final File testFile)
    {
        if(testFile.exists())
        {
            if(!testFile.delete())
            {
                throw new RuntimeException(String.format("Unable to delete test file: %s", testFile));
            }
        }
    }

    public static File getRandomFile() throws IOException
    {
        final File testFile = File.createTempFile("test", ".gpkg");
        testFile.delete();
        return testFile;
    }
}
