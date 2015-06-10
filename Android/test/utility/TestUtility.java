package utility;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Random;

/**
 * @author Luke Lambert
 *
 */
public class TestUtility
{
    private static final Random randomGenerator = new Random();

    /**
     * Creates a org.sqlite.JDBC connection based on a file handle
     *
     * @param testFile
     *             File handle
     * @return org.sqlite.JDBC connection
     * @throws ClassNotFoundException
     *             if the driver can't be found
     * @throws SQLException
     *             if there is a database error
     */
    public static Connection getConnection(final File testFile) throws ClassNotFoundException, SQLException
    {
        if(testFile == null)
        {
            throw new IllegalArgumentException("Test file may not be null");
        }

        Class.forName("org.sqlite.JDBC");   // Register the driver

        return DriverManager.getConnection("jdbc:sqlite:" + testFile.getPath()); // Initialize the database connection
    }

    /**
     * Gets a file handle to a file with a random (unused) name with the given
     * length
     *
     * @param length
     *             Desired length of file name, not including the ".gpkg"
     *             extension
     * @return File handle
     */
    public static File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            final String filename = String.format("%s/%s.gpkg",
                                                  new File("").getAbsoluteFile(),
                                                  TestUtility.getRandomString(length));
            testFile = new File(filename);
        }
        while(testFile.exists());

        return testFile;
    }

    private static String getRandomString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];

        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(randomGenerator.nextInt(characters.length()));
        }

        return new String(text);
    }

    /**
     * Deletes file, if it exists
     *
     * @param testFile
     *             File
     */
    public static void deleteFile(final File testFile)
    {
        if(testFile != null && testFile.exists())
        {
            if(!testFile.delete())
            {
                throw new RuntimeException(String.format(Locale.getDefault(),
                                                         "Unable to delete testFile. testFile: %s",
                                                         testFile));
            }
        }
    }
}
