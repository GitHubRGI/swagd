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

package com.rgi.geopackage.features;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.TestUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Luke Lambert
 */
@SuppressWarnings("JavaDoc")
public class GeoPackageFeaturesTest
{
    @BeforeClass
    public static void setUp() throws ClassNotFoundException
    {
        Class.forName("org.sqlite.JDBC"); // Register the driver
    }

    /**
     * Test a normal construction
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void constructor() throws IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile();

        try(final Connection connection = TestUtility.getConnection(testFile))
        {
            final GeoPackageCore core = new GeoPackageCore(connection);

            new GeoPackageFeatures(connection, core);
        }
    }

    /**
     * Test getVerificationIssues()
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void getVerificationIssues() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().getVerificationIssues(VerificationLevel.Full);
        }
    }

    /**
     * Test addFeatureSet()
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void addFeatureSet() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            final ColumnDefinition attributeDefinition1 = new ColumnDefinition("attribute1",
                                                                               SqlType.TEXT.toString(),
                                                                               null,
                                                                               null,
                                                                               ColumnDefault.None,
                                                                               "comment");

            final ColumnDefinition attributeDefinition2 = new ColumnDefinition("attribute2",
                                                                               SqlType.TEXT.toString(),
                                                                               null,
                                                                               null,
                                                                               ColumnDefault.None,
                                                                               "comment");

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               geometryColumn,
                               attributeDefinition1,
                               attributeDefinition2);
        }
    }

    /**
     * Test addFeatureSet() with a null spatial reference system
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureSetNullSpatialReferenceSystem() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               null,
                               "id",
                               geometryColumn);
        }
    }

    /**
     * Test addFeatureSet() with a null geometry column
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureSetNullGeometryColumn() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               null);
        }
    }

    /**
     * Test addFeatureSet() with a null column definition collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureSetNullColumnDefinition() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               geometryColumn,
                               (Collection<ColumnDefinition>)null);
        }
    }

    /**
     * Test addFeatureSet() with a null column definition in the column definition collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureSetNullMemberColumnDefinition() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            //noinspection CastToConcreteClass
            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               geometryColumn,
                               Arrays.asList((ColumnDefinition)null));
        }
    }

    /**
     * Test addFeatureSet() with a table name that already exists
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureSetDuplicateFeatureSet() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               geometryColumn);

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                               "id",
                               geometryColumn);
        }
    }
}
