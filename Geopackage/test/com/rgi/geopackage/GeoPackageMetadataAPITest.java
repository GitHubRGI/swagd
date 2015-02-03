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
package com.rgi.geopackage;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Random;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.junit.Test;

import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.metadata.Metadata;
import com.rgi.geopackage.metadata.MetadataReference;
import com.rgi.geopackage.metadata.ReferenceScope;
import com.rgi.geopackage.metadata.Scope;
import com.rgi.geopackage.verification.ConformanceException;

public class GeoPackageMetadataAPITest
{
    
    private final Random randomGenerator = new Random();
    
    /**
     * Tests if GeoPackageMetadata can add metadata
     * and verify it returns the expected values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test
    public void addMetadataVerify() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Scope    scope       = Scope.Dataset;
            URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            MimeType mimeType    = new MimeType("text/xml");
            String   metadata     = "";
            
            Metadata metadataReturned =gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            
            assertTrue("Metadata returned does not have the expected values.",
                       metadataReturned.getScope()                 .equalsIgnoreCase(scope.toString())  &&
                       metadataReturned.getStandardUri().toString().equalsIgnoreCase(standardUri.toString()) &&
                       metadataReturned.getMimeType()   .toString().equalsIgnoreCase(mimeType.toString())       &&
                       metadataReturned.getMetadata()   .toString().equalsIgnoreCase(metadata));
            
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if an Illegal ARgument exception is thrown
     * when adding metadata with scope as a null parameter
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(8);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            MimeType mimeType    = new MimeType("text/xml");
            String   metadata     = "";
            
            gpkg.metadata().addMetadata(null, standardUri, mimeType, metadata);
            
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for scope when using the method addMetadata");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if an Illegal ARgument exception is thrown
     * when adding metadata with uri as a null parameter
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException2() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Scope    scope       = Scope.Dataset;
            MimeType mimeType    = new MimeType("text/xml");
            String   metadata     = "";
            
            gpkg.metadata().addMetadata(scope, null, mimeType, metadata);
            
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for uri when using the method addMetadata");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    /**
     * Tests if an Illegal ARgument exception is thrown
     * when adding metadata with mimetype as a null parameter
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException3() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Scope    scope       = Scope.Dataset;
            URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            String   metadata     = "";
            
            gpkg.metadata().addMetadata(scope, standardUri, null, metadata);
            
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for mimetype when using the method addMetadata");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if an Illegal ARgument exception is thrown
     * when adding metadata with metadata as a null parameter
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException4() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            Scope    scope       = Scope.Dataset;
            URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            MimeType mimeType    = new MimeType("text/xml");
            
            gpkg.metadata().addMetadata(scope, standardUri, mimeType, null);
            
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for metadata when using the method addMetadata");
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    /**
     * Tests if GeoPackage Metadata can add
     * a MetadataReference object to the geopackage
     * database properly and return the correct values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test
    public void addMetadataReference() throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException, URISyntaxException, MimeTypeParseException
    {
        File testFile = this.getRandomFile(12);
        
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            ReferenceScope referenceScope   = ReferenceScope.GeoPackage;
            String         tableName        = null;
            String         columnName       = null;
            Integer        rowIdentifier    = null;
            Scope          scope            = Scope.Attribute; 
            URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            MimeType       mimeType         = new MimeType("text/xml");
            String         metadata         = "";
            Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            Metadata       parentIdentifier = null;
            
            MetadataReference metadataReferenceReturned = gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);

            assertTrue("metadata reference returned is not what was expected",
                       metadataReferenceReturned.getReferenceScope().equalsIgnoreCase(referenceScope.getText()) &&
                       metadataReferenceReturned.getTableName() == null &&
                       metadataReferenceReturned.getColumnName() == null &&
                       metadataReferenceReturned.getRowIdentifier() == null &&
                       metadataReferenceReturned.getParentIdentifier() == null);
        }
        finally
        {
            deleteFile(testFile);
        }
    }
    
    private static void deleteFile(File testFile)
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
