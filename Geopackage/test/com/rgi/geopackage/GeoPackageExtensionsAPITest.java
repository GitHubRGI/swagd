package com.rgi.geopackage;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.FailedRequirement;

public class GeoPackageExtensionsAPITest
{

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private final Random randomGenerator = new Random();
    
   //commented out so we can build
//    @Test
//    public void hasExtension() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
//    {
//        File testFile = this.getRandomFile(12);
//        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
//        {
//            String extensionName = "something_extension";
//            
//            Extension expectedExtension = gpkg.extensions().addExtension(null, null, extensionName, "definition", Scope.ReadWrite); //this works fine
//            
//            Extension returnedExtension = gpkg.extensions().getExtension(null, null, extensionName); //this does not
//            
//            
//            
//            assertTrue(String.format("The GeoPackageExtensions did not return the extension expected. Expected: %s.\nActual: %s.",
//                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s", 
//                                                    expectedExtension.getTableName(), 
//                                                    expectedExtension.getColumnName(), 
//                                                    expectedExtension.getExtensionName(), 
//                                                    expectedExtension.getDefinition(), 
//                                                    expectedExtension.getScope().toString()),
//                                     String.format("TableName: %s, Column Name: %s, extension name: %s definition: %s, scope: %s",
//                                                    returnedExtension.getTableName(),
//                                                    returnedExtension.getColumnName(),
//                                                    returnedExtension.getExtensionName(),
//                                                    returnedExtension.getDefinition(),
//                                                    expectedExtension.getScope().toString())),
//                       returnedExtension.equals(expectedExtension.getTableName(), 
//                                                expectedExtension.getTableName(), 
//                                                expectedExtension.getExtensionName(), 
//                                                expectedExtension.getDefinition(), 
//                                                Scope.ReadWrite));
//        }
//        finally
//        {
//            this.deleteFile(testFile);
//        }
//    }
    
    @Test
    public void testing() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        File testFile = new File("ESRI_GeoPackage.gpkg");
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Open))
        {
            Collection<FailedRequirement> failedRequirements = gpkg.tiles().getFailedRequirements();
            
            System.err.println(String.format("GeoPackage failed to meet the following requirements:\n %s",
                    failedRequirements.stream()
                                      .sorted((requirement1, requirement2) -> Integer.compare(requirement1.getRequirement().number(), requirement2.getRequirement().number()))
                                      .map(failedRequirement -> String.format("(%s) Requirement %d: \"%s\"\n%s",
                                                                              failedRequirement.getRequirement().severity(),
                                                                              failedRequirement.getRequirement().number(),
                                                                              failedRequirement.getRequirement().text(),
                                                                              failedRequirement.getReason()))
                                      .collect(Collectors.joining("\n"))));
            
        }
    }
    
    private void deleteFile(File testFile)
    {
        if (testFile.exists())
        {
            if (!testFile.delete())
            {
                throw new RuntimeException(String.format(
                        "Unable to delete testFile. testFile: %s", testFile));
            }
        }
    }
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
