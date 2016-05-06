package utility;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Luke Lambert
 *
 */
public final class TestUtility
{
    private TestUtility()
    {

    }

    public static File getRandomFile() throws IOException
    {
        final File testFile = File.createTempFile("test", ".gpkg");
        testFile.delete();
        testFile.deleteOnExit();
        return testFile;
    }

    public static Connection getConnection(final File testFile) throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite:" + testFile.toURI());
    }

    /**
     * Takes the {@link String) name of a file in the test resources and returns a valid {@link File} object.
     *
     * @param fileName {@link String} file name that must reside in the test resources folder
     *
     * @return A {@link File} object that can correctly work on multiple platforms
     *
     * @throws URISyntaxException If an error occurs while getting the resource
     */
    public static File loadFileFromDisk(final String fileName)
    {
        try
        {
            // The File object must be made in this manner in order to interoperate on multiple OS platforms
            // You CANNOT simply make a new File object using the ClassLoader, because the File object will have encoding
            // that carries over when that file path is converted to a String
            return Paths.get(ClassLoader.getSystemResource(fileName).toURI()).toFile();
        }
        catch(final URISyntaxException exception)
        {
            throw new RuntimeException(exception);
        }
    }
}
