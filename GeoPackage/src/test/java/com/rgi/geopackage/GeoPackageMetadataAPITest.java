/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.rgi.geopackage;

import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.metadata.Metadata;
import com.rgi.geopackage.metadata.MetadataReference;
import com.rgi.geopackage.metadata.ReferenceScope;
import com.rgi.geopackage.metadata.Scope;
import com.rgi.geopackage.verification.ConformanceException;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jenifer Cochran
 * @author Luke Lambert
 *
 */
@SuppressWarnings("javadoc")
public class GeoPackageMetadataAPITest
{
    @BeforeClass
    public static void setUp() throws ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver
    }

    /**
     * Tests if GeoPackageMetadata can add metadata
     * and verify it returns the expected values
     */
    @Test
    public void addMetadataVerify() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

            final Metadata metadataReturned =gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);
            gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);

            assertTrue("Metadata returned does not have the expected values.",
                       metadataReturned.getScope()      .equalsIgnoreCase(scope.toString())       &&
                       metadataReturned.getStandardUri().equalsIgnoreCase(standardUri.toString()) &&
                       metadataReturned.getMimeType()   .equalsIgnoreCase(mimeType.toString())    &&
                       metadataReturned.getMetadata()   .equalsIgnoreCase(metadata));
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when adding metadata with scope as a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

            gpkg.metadata().addMetadata(null, standardUri, mimeType, metadata);

            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for scope when using the method addMetadata");
        }
    }
    /**
     * Tests if an IllegalArgumentException is thrown
     * when adding metadata with uri as a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata     = "";

            gpkg.metadata().addMetadata(scope, null, mimeType, metadata);

            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for uri when using the method addMetadata");
        }
    }
    /**
     * Tests if an IllegalArgumentException is thrown
     * when adding metadata with mime type as a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException3() throws ClassNotFoundException, IOException, SQLException, ConformanceException, URISyntaxException
    {
        final File testFile = TestUtility.getRandomFile();
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final String   metadata     = "";

            gpkg.metadata().addMetadata(scope, standardUri, null, metadata);

            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for mimetype when using the method addMetadata");
        }
    }

    /**
     * Tests if an IllegalArgumentException is thrown
     * when adding metadata with metadata as a null parameter
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataIllegalArgumentException4() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();
        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Scope    scope       = Scope.Dataset;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");

            gpkg.metadata().addMetadata(scope, standardUri, mimeType, null);

            fail("Expected GeoPackage Metadata to throw an IllegalArgumentException when passing a null value for metadata when using the method addMetadata");
        }
    }

    /**
     * Tests if GeoPackage Metadata can add
     * a MetadataReference object to the GeoPackage
     * database properly and return the correct values
     */
    @Test
    public void addMetadataReference() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }


    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing non-null value for tableName (When referenceScope is GeoPackage)
     * as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException2() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a non-null value for columnName (when reference scope is Row) for
     * scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException3() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException4() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException5() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing an empty string for columnName(when reference scope is Column) for
     * scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException6() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage Metadata will throw an IllegalArgumentException when
     * passing a null value for rowIdentifier (when reference scope is RowCol) for
     * scope as a parameter in addMetadataReference()
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void addMetadataReferenceIllegalArgumentException7() throws ClassNotFoundException, IOException, SQLException, ConformanceException
    {
        final File testFile = TestUtility.getRandomFile();

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
    }

    /**
     * Tests if GeoPackage metadata returned
     * the all the metadata entries in a GeoPackage
     * with all the correct values
     */
    @Test
    public void getMetadata() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

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
                       metadataReturned.stream().allMatch(returned -> metadataExpected.stream().anyMatch(expected -> GeoPackageMetadataAPITest.areMetadatasEqual(expected, returned))));
        }
    }

    /**
     * Tests if GeoPackage get metadata method with a parameter
     * of identifier gives back the expected metadata entry from
     * the GeoPackage
     */
    @Test
    public void getMetadataWithIdentifier() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();
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
                       GeoPackageMetadataAPITest.areMetadatasEqual(metadataExpected, metadataReturned));
        }
    }

    /**
     * Tests if getMetadata with identifier
     * will return null if the metadata entry
     * does not exist (case with a metadata
     * table in GeoPackage)
     */
    @Test
    public void getMetadataWithIdentifier2() throws ClassNotFoundException, IOException, SQLException, ConformanceException, MimeTypeParseException, URISyntaxException
    {
        final File testFile = TestUtility.getRandomFile();

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {

            final Scope    scope       = Scope.Attribute;
            final URI      standardUri = new URI("http://www.geopackage.org/spec/#metadata_scopes");
            final MimeType mimeType    = new MimeType("text/xml");
            final String   metadata    = "";

            gpkg.metadata().addMetadata(scope, standardUri, mimeType, metadata);//this will create metadata tables

            assertNull("The geoPackage returned a non null metadata object when trying to retrieve a metadata object that does not exist",
                       gpkg.metadata().getMetadata(999999));
        }
    }

    /**
     * GeoPackage returns the correct values for the metadata
     * references entries in the GeoPackage
     *
     */
    @Test
    public void getMetadatReferences() throws ClassNotFoundException, ConformanceException, IOException, SQLException, URISyntaxException, MimeTypeParseException
    {
        final File testFile = TestUtility.getRandomFile();

        try(GeoPackage gpkg = new GeoPackage(testFile, OpenMode.Create))
        {
            final Collection<MetadataReference> shouldBeEmpty = gpkg.metadata().getMetadataReferences();
            assertTrue("GeoPackage returned an non empty collection when there are no entries in the metadata table.",
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

            assertTrue("GeoPackage did not return all or the expected entries in the metadataReference table",
                       returnedReferences.size() == 2 &&
                       returnedReferences.stream().allMatch(returned -> expectedReferences.stream().anyMatch(expected -> GeoPackageMetadataAPITest.areMetadataReferencesEqual(expected, returned))));
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
        assertSame("The Scope method from string did not return the expected scope", Scope.Undefined, scope);
    }

    /**
     * tests if scope's code and description
     * is as expected
     */
    @Test
    public void scopeGetCodeAndDescription()
    {
        final Scope scope = Scope.CollectionHardware;
        assertEquals("The scope did not return the expected code", "003", scope.getCode());
        assertTrue("The scope did not return the expected description", scope.getDescription().equalsIgnoreCase("Information applies to the collection hardware class"));
    }

    /**
     * Tests all cases of fromText()
     * method for reference scope
     */
    @Test
    public void referenceScopeFromText()
    {
        final ReferenceScope reference = ReferenceScope.fromText("GeOpAcKaGe");
        assertSame("Reference scope method from text did not return geopackage as expected", ReferenceScope.GeoPackage, reference);

        final ReferenceScope referencetable = ReferenceScope.fromText("tAbLe");
        assertSame("Reference scope method from text did not return geopackage as expected", ReferenceScope.Table, referencetable);

        final ReferenceScope referenceColumn = ReferenceScope.fromText("CoLuMn");
        assertSame("Reference scope method from text did not return geopackage as expected", ReferenceScope.Column, referenceColumn);

        final ReferenceScope referenceRow = ReferenceScope.fromText("RoW");
        assertSame("Reference scope method from text did not return geopackage as expected", ReferenceScope.Row, referenceRow);

        final ReferenceScope referenceRowCol = ReferenceScope.fromText("RoW/CoL");
        assertSame("Reference scope method from text did not return geopackage as expected", ReferenceScope.RowCol, referenceRowCol);
    }

    /**
     * Tests if ReferenceScope will throw
     * an IllegalArgumentException if the
     * text in fromText doesn't match
     * any of the allowed cases
     */
    @Test(expected = IllegalArgumentException.class)
    public void referenceScopeFromTextIllegalArgumentException()
    {
        ReferenceScope.fromText("doesn't match anything");
        fail("Expected GeoPackage Metadata to throw an IllegalArguementException when it doesn't match any of the cases allowed");
    }

    private static boolean areMetadataReferencesEqual(final MetadataReference expected, final MetadataReference returned)
    {
        return Objects.equals(expected.getReferenceScope(),   returned.getReferenceScope()) &&
               Objects.equals(expected.getTableName(),        returned.getTableName())      &&
               Objects.equals(expected.getColumnName(),       returned.getColumnName())     &&
               Objects.equals(returned.getRowIdentifier(),    expected.getRowIdentifier()) &&
               Objects.equals(returned.getParentIdentifier(), expected.getParentIdentifier());
    }

    private static boolean areMetadatasEqual(final Metadata expected, final Metadata returned)
    {
        return expected.getScope()      .equalsIgnoreCase(returned.getScope())       &&
               expected.getStandardUri().equalsIgnoreCase(returned.getStandardUri()) &&
               expected.getMetadata()   .equalsIgnoreCase(returned.getMetadata())    &&
               expected.getMimeType()   .equalsIgnoreCase(returned.getMimeType());
    }
}
