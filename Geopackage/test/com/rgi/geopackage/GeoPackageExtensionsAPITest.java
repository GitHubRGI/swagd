package com.rgi.geopackage;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.Random;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class GeoPackageExtensionsAPITest 
{
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();
    
   
    
    
    
    
    
    
    private String getRanString(final int length)
    {
        final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(this.randomGenerator.nextInt(characters.length()));
        }
        return new String(text);
    }

    private File getRandomFile(final int length)
    {
        File testFile;

        do
        {
            testFile = new File(String.format(FileSystems.getDefault().getPath(this.getRanString(length)).toString() + ".gpkg"));
        }
        while (testFile.exists());

        return testFile;
    }

}
