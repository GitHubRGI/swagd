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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

/**
 * @author Jenifer Cochran
 *
 */
@SuppressWarnings({"static-method", "javadoc"})
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
    public void addMetadataVerify() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

            final Metadata metadataReturned =gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
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
    public void addMetadataIllegalArgumentException() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

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
    public void addMetadataIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

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
    public void addMetadataIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException, URISyntaxException
    {
        final File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final String   metadata     = "";

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
    public void addMetadataIllegalArgumentException4() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(18);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");

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
    public void addMetadataReference() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.GeoPackage;
            final String         tableName        = null;
            final String         columnName       = null;
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);

            final MetadataReference metadataReferenceReturned = gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, null);

            assertTrue("metadata reference returned is not what was expected",
                       metadataReferenceReturned.getReferenceScope().equalsIgnoreCase(referenceScope.getText()) &&
                       metadataReferenceReturned.getTableName()        == null &&
                       metadataReferenceReturned.getColumnName()       == null &&
                       metadataReferenceReturned.getRowIdentifier()    == null &&
                       metadataReferenceReturned.getParentIdentifier() == parentIdentifier.getIdentifier() &&
                       metadataReferenceReturned.getFileIdentifier()   == 1);

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = null;
            final String         tableName        = null;
            final String         columnName       = null;
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for scope as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }


    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing non-null value for tableName (When referenceScope is geopackage)
     * as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException2() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.GeoPackage;
            final String         tableName        = "Should be null";
            final String         columnName       = null;
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a non-null value for tableName "
                    + "(When referenceScope is geopackage) as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a non-null value for columnName (when reference scope is Row) for
     * scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException3() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.Row;
            final String         tableName        = "tablename";
            final String         columnName       = "Should be null";
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a non-null value for columnName"
                    + " (when reference scope is Row) as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException4() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.RowCol;
            final String         tableName        = "tablename";
            final String         columnName       = null;
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for rowIdentifier"
                    + " (when reference scope is Row) as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException5() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.Row;
            final String         tableName        = "";
            final String         columnName       = null;
            final Integer        rowIdentifier    = 1;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing an Empty string for tableName"
                    + "as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing an empty string for columnName(when reference scope is Column) for
     * scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException6() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.Column;
            final String         tableName        = "tablename";
            final String         columnName       = "";
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing an empty string for columnName"
                    + " (when reference scope is Column) as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException7() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = this.getRandomFile(8);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final ReferenceScope referenceScope   = ReferenceScope.RowCol;
            final String         tableName        = "tablename";
            final String         columnName       = "ColumnName";
            final Integer        rowIdentifier    = 1;
            final Metadata       fileIdentifier   = null;
            final Metadata       parentIdentifier = null;

            gpkg.metadata().addMetadataReference(referenceScope, tableName, columnName, rowIdentifier, fileIdentifier, parentIdentifier);
            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for fileidentifier"
                    + " as a parameter in addMetadataReference()");
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if GeoPackage metadata returned
     * the all the metadata entries in a geopackage
     * with all the correct values
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test
    public void getMetadata() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Collection<Metadata> shouldBeEmpty = gpkg.metadata().getMetadata();

            assertTrue("GeoPackage returned a non empty collection when there was no metadata in the geopackage.",
                       shouldBeEmpty.isEmpty());

            final Scope    scope       = Scope.Attribute;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata    = "";

            final Metadata metadata1   = gpkg.metadata().addMetadata(scope,standardUri, mimeType, metadata);

            final Scope    scope2          = Scope.Catalog;
            final URI      standardUri2    = new URI("http://www.geopackage.org/spec");
            final MimeType mimeType2       = new MimeType("image/png");
            final String   strMetadata2    = "secondOne";

            final Metadata metadata2   = gpkg.metadata().addMetadata(scope2, standardUri2, mimeType2, strMetadata2);

            final List<Metadata> metadataExpected = Arrays.asList(metadata1, metadata2);

            final Collection<Metadata> metadataReturned = gpkg.metadata().getMetadata();

            assertTrue("GeoPackage metadata did not return all the expected metadata objects or "
                        + "they were not equivalent to those inputted into the geopackage",
                       metadataReturned.size() == 2 &&
                       metadataReturned.stream().allMatch(returned -> metadataExpected.stream().anyMatch(expected -> this.metadatasEqual(expected, returned))));

        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if goepackage get metdata method with a parameter
     * of identifer gives back the expected metadata entry from
     * the geopackage
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test
    public void getMetadataWithIdentifier() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(6);
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Metadata shouldBeNull = gpkg.metadata().getMetadata(0);


            final Scope    scope       = Scope.Attribute;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata    = "";

            final Metadata metadataExpected = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);

            final Scope    scope2          = Scope.Catalog;
            final URI      standardUri2    = new URI("http://www.geopackage.org/spec");
            final MimeType mimeType2       = new MimeType("image/png");
            final String   strMetadata2    = "secondOne";

            gpkg.metadata().addMetadata(scope2, standardUri2, mimeType2, strMetadata2); //another metadata not expected

            final Metadata metadataReturned = gpkg.metadata().getMetadata(metadataExpected.getIdentifier());

            assertTrue("The geopackage metadata getMetadata(identifier) did not return the expected metadata object",
                       shouldBeNull == null &&
                       this.metadatasEqual(metadataExpected, metadataReturned));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if getMetadata with identifier
     * will return null if the metadata entry
     * does not exist (case with a metadata
     * table in geopackage)
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws MimeTypeParseException
     * @throws URISyntaxException
     */
    @Test
    public void getMetadataWithIdentifier2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException, URISyntaxException
    {
        final File testFile = this.getRandomFile(7);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final Scope    scope       = Scope.Attribute;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata    = "";

            gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);//this will create metadata tables

            assertTrue("The geoPackage returned a non null metadata object when trying to retrieve a metadata object that does not exist",
                       gpkg.metadata().getMetadata(999999) == null);
        }
        finally
        {
            deleteFile(testFile);
        }

    }

    /**
     * GeoPackage returns the correct values for the metadata
     * references entries in the geopcakge
     *
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     * @throws URISyntaxException
     * @throws MimeTypeParseException
     */
    @Test
    public void getMetadatReferences() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = this.getRandomFile(12);

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Collection<MetadataReference> shouldBeEmpty = gpkg.metadata().getMetadataReferences();
            assertTrue("Geopackage returned an non empty collection when there are no entries in the metadata table.",
                       shouldBeEmpty.isEmpty());
            //first reference values
            final ReferenceScope referenceScope   = ReferenceScope.GeoPackage;
            final String         tableName        = null;
            final String         columnName       = null;
            final Integer        rowIdentifier    = null;
            final Scope          scope            = Scope.Attribute;
            final URI            standardUri      = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType       mimeType         = new MimeType("text/xml");
            final String         metadata         = "";
            final Metadata       fileIdentifier   = gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            final Metadata       parentIdentifier =  null;

            final MetadataReference reference1 = gpkg.metadata().addMetadataReference(referenceScope,  tableName,  columnName,  rowIdentifier,  fileIdentifier,  parentIdentifier);
            //second reference values
            final ReferenceScope referenceScope2   = ReferenceScope.Column;
            final String         tableName2        = "tableName";
            final String         columnName2       = "columnName";
            final Integer        rowIdentifier2    =  null;
            final Scope          scope2            = Scope.Catalog;
            final URI            standardUri2      = new URI("http://www.geopackage.org/spec");
            final MimeType       mimeType2         = new MimeType("image/png");
            final String         metadata2         ="SecondMetadata";
            final Metadata       fileIdentifier2   = gpkg.metadata().addMetadata(scope2, standardUri2, mimeType2, metadata2);
            final Metadata       parentIdentifier2 = null;

            final MetadataReference reference2 = gpkg.metadata().addMetadataReference(referenceScope2, tableName2, columnName2, rowIdentifier2, fileIdentifier2, parentIdentifier2 );

            final List<MetadataReference> expectedReferences = Arrays.asList(reference1, reference2);

            final Collection<MetadataReference> returnedReferences = gpkg.metadata().getMetadataReferences();

            assertTrue("Geopackage did not return all or the expected entries in the metadataReference table",
                       returnedReferences.size() == 2 &&
                       returnedReferences.stream().allMatch(returned -> expectedReferences.stream().anyMatch(expected -> this.metadataReferencesEqual(expected, returned))));
        }
        finally
        {
            deleteFile(testFile);
        }
    }

    /**
     * Tests if scope returns
     * the correct value in method
     * from string
     */
    @Test
    public void scopeFromString()
    {
        final Scope scope = Scope.fromString("uNdEfInEd");
        assertTrue("The Scope method from string did not return the expected scope", scope == Scope.Undefined);
    }

    /**
     * tests if scope's code and description
     * is as expected
     */
    @Test
    public void scopeGetCodeAndDescription()
    {
        final Scope scope = Scope.CollectionHardware;
        assertTrue("The scope did not return the expected code",scope.getCode().equals("003"));
        assertTrue("The scope did not return the expected description", scope.getDescription().equalsIgnoreCase("Information applies to the collection hardware class"));
    }

    /**
     * Tests all cases of fromtext
     * method for reference scope
     */
    @Test
    public void referenceScopeFromText()
    {
        final ReferenceScope reference = ReferenceScope.fromText("GeOpAcKaGe");
        assertTrue("Reference scope method from text did not return geopackage as expected",reference == ReferenceScope.GeoPackage);

        final ReferenceScope referencetable = ReferenceScope.fromText("tAbLe");
        assertTrue("Reference scope method from text did not return geopackage as expected",referencetable == ReferenceScope.Table);

        final ReferenceScope referenceColumn = ReferenceScope.fromText("CoLuMn");
        assertTrue("Reference scope method from text did not return geopackage as expected",referenceColumn == ReferenceScope.Column);

        final ReferenceScope referenceRow = ReferenceScope.fromText("RoW");
        assertTrue("Reference scope method from text did not return geopackage as expected",referenceRow == ReferenceScope.Row);

        final ReferenceScope referenceRowCol = ReferenceScope.fromText("RoW/CoL");
        assertTrue("Reference scope method from text did not return geopackage as expected",referenceRowCol == ReferenceScope.RowCol);
    }

    /**
     * Tests if ReferenceScope will throw
     * an Illegal Argument Exception if the
     * text in fromText doesn't match
     * any of the allowed cases
     */
    @Test(expected = IllegalArgumentException.class)
    public void referenceScopeFromTextIllegalArgumentException()
    {
        ReferenceScope.fromText("doesn't match anything");
        fail("Expected GeoPackage Metadata to throw an IllegalArguementException when it doesn't match any of the cases allowed");
    }
//    /**
//     *
//     */
//    @Test
//    public void referenceScopeFromTextNullValue()
//    {
//        ReferenceScope scope = ReferenceScope.fromText(null);
//        //TODO check for null in fromtext
//    }

    private boolean metadataReferencesEqual(final MetadataReference expected, final MetadataReference returned)
    {
        return this.isEqual(expected.getReferenceScope(), returned.getReferenceScope()) &&
               this.isEqual(expected.getTableName(),      returned.getTableName())      &&
               this.isEqual(expected.getColumnName(),     returned.getColumnName())     &&
               returned.getRowIdentifier()    == expected.getRowIdentifier()       &&
               returned.getParentIdentifier() == expected.getParentIdentifier();
    }

    private <T> boolean isEqual(final T expected, final T returned)
    {
        return expected == null ? returned == null
                                : expected.equals(returned);
    }

    private boolean metadatasEqual(final Metadata expected, final Metadata returned)
    {
        return expected.getScope()      .equalsIgnoreCase(returned.getScope()) &&
               expected.getStandardUri().equalsIgnoreCase(returned.getStandardUri()) &&
               expected.getMetadata()   .equalsIgnoreCase(returned.getMetadata())      &&
               expected.getMimeType()   .equalsIgnoreCase(returned.getMimeType());
    }

    private static void deleteFile(final File testFile)
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
