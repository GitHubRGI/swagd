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
import com.rgi.common.Pair;
import com.rgi.common.util.jdbc.JdbcUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.TestUtility;
import com.rgi.geopackage.core.GeoPackageCore;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.features.geometry.xy.Coordinate;
import com.rgi.geopackage.features.geometry.xy.Envelope;
import com.rgi.geopackage.features.geometry.xy.WkbGeometry;
import com.rgi.geopackage.features.geometry.xy.WkbGeometryCollection;
import com.rgi.geopackage.features.geometry.xy.WkbLineString;
import com.rgi.geopackage.features.geometry.xy.WkbMultiPoint;
import com.rgi.geopackage.features.geometry.xy.WkbPoint;
import com.rgi.geopackage.features.geometry.zm.WkbPointZM;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

    /**
     * Test getGeometryColumn() with a null FeatureSet
     */
    @Test(expected = IllegalArgumentException.class)
    public void getGeometryColumnNullFeatureSet() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().getGeometryColumn(null);
        }
    }

    /**
     * Test getGeometryColumn()
     */
    @Test
    public void getGeometryColumn() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final String                 tableName             = "mytable";
            final String                 columnName             = "geometry";
            final String                 geometryType           = GeometryType.Point.toString();
            final SpatialReferenceSystem spatialReferenceSystem = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);
            final ValueRequirement       zValueRequirement      = ValueRequirement.Mandatory;
            final ValueRequirement       mValueRequirement      = ValueRequirement.Mandatory;

            final GeometryColumnDefinition geometryColumnDefinition = new GeometryColumnDefinition(columnName,
                                                                                                   geometryType,
                                                                                                   zValueRequirement,
                                                                                                   mValueRequirement,
                                                                                                   "comment");

            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet(tableName,
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             spatialReferenceSystem,
                                                             "id",
                                                             geometryColumnDefinition);

            final GeometryColumn geometryColumn = gpkg.features()
                                                      .getGeometryColumn(featureSet);

            assertEquals("Incorrect table name",
                         tableName,
                         geometryColumn.getTableName());

            assertEquals("Incorrect column name",
                         columnName,
                         geometryColumn.getColumnName());

            assertEquals("Incorrect geometry type",
                         geometryType,
                         geometryColumn.getGeometryType());

            assertEquals("Incorrect spatial reference system identifier",
                         spatialReferenceSystem.getIdentifier(),
                         geometryColumn.getSpatialReferenceSystemIdentifier());

            assertEquals("Incorrect z value requirement",
                         zValueRequirement,
                         geometryColumn.getZRequirement());

            assertEquals("Incorrect m value requirement",
                         mValueRequirement,
                         geometryColumn.getMRequirement());
        }
    }

    /**
     * Test getAttributeColumns() with a null FeatureSet
     */
    @Test(expected = IllegalArgumentException.class)
    public void getAttributeColumnsNullFeatureSet() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().getAttributeColumns(null);
        }
    }

    /**
     * Test getAttributeColumns()
     */
    @Test
    public void getAttributeColumns() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            final String              name           = "attribute1";
            final String              type           = SqlType.TEXT.toString();
            final EnumSet<ColumnFlag> flags          = EnumSet.of(ColumnFlag.NotNull);
            final String              checkExpresson = null;
            final ColumnDefault       defaultValue   = ColumnDefault.from("f'oo");
            final String              comment        = "comment";

            final ColumnDefinition attributeDefinition = new ColumnDefinition(name,
                                                                              type,
                                                                              flags,
                                                                              checkExpresson,
                                                                              defaultValue,
                                                                              comment);

            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             geometryColumn,
                                                             attributeDefinition);

            final Column column = gpkg.features().getAttributeColumns(featureSet).get(0);

            assertEquals("getName() didn't return the correct value",
                         name,
                         column.getName());

            assertEquals("getType() didn't return the correct value",
                         type,
                         column.getType());

            assertEquals("getFlags() didn't return the correct value",
                         flags,
                         column.getFlags());

            // TODO: There is a minor issue here that's not being addressed. String literal column defaults are formatted to include single quotes on either side, and escape the single quotes in between.
            assertEquals("getDefaultValue() didn't return the correct value",
                         defaultValue.sqlLiteral(),
                         column.getDefaultValue());
        }
    }

    /**
     * Test getFeatureSet() with a GeoPackage that doesn't have a geometry column table
     */
    @Test
    public void getFeatureSetNoFeatureSets() throws IOException, SQLException, ConformanceException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            assertNull("getFeatureSet() should return null when a GeoPackage that doesn't have a geometry column table",
                       gpkg.features().getFeatureSet("foo"));

        }
    }

    /**
     * Test getFeatureSet() with a table name that isn't a feature set
     */
    @Test
    public void getFeatureSetBadTableName() throws IOException, SQLException, ConformanceException, ClassNotFoundException
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

            assertNull("getFeatureSet() should return null for a non feature set table",
                       gpkg.features().getFeatureSet(GeoPackageCore.ContentsTableName));
        }
    }

    /**
     * Test getFeatureSets()
     */
    @Test
    public void getFeatureSets() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            assertEquals("getFeatureSets() returned the wrong number of results",
                         0,
                         gpkg.features().getFeatureSets().size());

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

            assertEquals("getFeatureSets() returned the wrong number of results",
                         1,
                         gpkg.features().getFeatureSets().size());
        }
    }

    /**
     * Test getFeatureSets(srs)
     */
    @Test
    public void getFeatureSetsWithSrs() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final GeometryColumnDefinition geometryColumn = new GeometryColumnDefinition("geometry",
                                                                                         GeometryType.Point.toString(),
                                                                                         ValueRequirement.Mandatory,
                                                                                         ValueRequirement.Mandatory,
                                                                                         "comment");

            final SpatialReferenceSystem srs4326 = gpkg.core().getSpatialReferenceSystem("EPSG", 4326);

            gpkg.features()
                .addFeatureSet("mytable",
                               "identifier",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               srs4326,
                               "id",
                               geometryColumn);

            gpkg.features()
                .addFeatureSet("mytable2",
                               "identifier2",
                               "description",
                               new BoundingBox(0.0, 0.0, 0.0, 0.0),
                               gpkg.core().getSpatialReferenceSystem("NONE", -1),
                               "id",
                               geometryColumn);

            assertEquals("getFeatureSets() returned the wrong number of results",
                         1,
                         gpkg.features().getFeatureSets(srs4326).size());
        }
    }

    /**
     * Test getFeatures()
     */
    @Test
    public void getFeatures() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"),
                                                             new ColumnDefinition("color",
                                                                                  SqlType.TEXT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null),
                                                             new ColumnDefinition("size",
                                                                                  SqlType.INT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final List<String> attributeColumnNames = Arrays.asList("color", "size");

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);
            final String     color0 = "blue";
            final int        size0  = 0;

            gpkg.features().addFeature(geometryColumn,
                                       point0,
                                       attributeColumnNames,
                                       Arrays.asList(color0, size0));

            final WkbPointZM point1 = new WkbPointZM(1.0, 1.0, 1.0, 1.0);
            final String     color1 = "red";
            final int        size1  = 1;

            gpkg.features().addFeature(geometryColumn,
                                       point1,
                                       attributeColumnNames,
                                       Arrays.asList(color1, size1));

            final List<Feature> features = gpkg.features().getFeatures(featureSet);

            assertEquals("getFeatures() returned the incorrect number of features",
                         2,
                         features.size());

            final Feature feature0 = features.get(0);

            assertEquals("getFeatures() returned an incorrect geometry",
                         point0,
                         feature0.getGeometry());

            assertEquals("getFeatures returned an incorrect attribute",
                         color0,
                         feature0.getAttributes().get("color"));

            assertEquals("getFeatures returned an incorrect attribute",
                         size0,
                         feature0.getAttributes().get("size"));

            final Feature feature1 = features.get(1);

            assertEquals("getFeatures() returned an incorrect geometry",
                         point1,
                         feature1.getGeometry());

            assertEquals("getFeatures returned an incorrect attribute",
                         color1,
                         feature1.getAttributes().get("color"));

            assertEquals("getFeatures returned an incorrect attribute",
                         size1,
                         feature1.getAttributes().get("size"));
        }
    }

    /**
     * Test getFeatures() with a null feature set
     */
    @Test(expected = IllegalArgumentException.class)
    public void getFeaturesWithNull() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().getFeatures(null);
        }
    }

    /**
     * Test getFeature() with a null feature set
     */
    @Test(expected = IllegalArgumentException.class)
    public void getFeatureWithNull() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().getFeature(null, 0);
        }
    }

    /**
     * Test getFeatures()
     */
    @Test
    public void getFeature() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"),
                                                             new ColumnDefinition("color",
                                                                                  SqlType.TEXT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null),
                                                             new ColumnDefinition("size",
                                                                                  SqlType.INT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final List<String> attributeColumnNames = Arrays.asList("color", "size");

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);
            final String     color0 = "blue";
            final int        size0  = 0;

            final int featureId = gpkg.features()
                                      .addFeature(geometryColumn,
                                                  point0,
                                                  attributeColumnNames,
                                                  Arrays.asList(color0, size0))
                                      .getIdentifier();


            final Feature feature0 = gpkg.features().getFeature(featureSet, featureId);

            assertEquals("getFeature() returned an incorrect geometry",
                         point0,
                         feature0.getGeometry());

            assertEquals("getFeature returned an incorrect attribute",
                         color0,
                         feature0.getAttributes().get("color"));

            assertEquals("getFeature returned an incorrect attribute",
                         size0,
                         feature0.getAttributes().get("size"));

            assertNull("getFeature should have returned null for a bad feature identifier",
                       gpkg.features().getFeature(featureSet, featureId+1));
        }
    }

    /**
     * Test visitFeatures()
     */
    @Test
    public void visitFeatures() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"),
                                                             new ColumnDefinition("color",
                                                                                  SqlType.TEXT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null),
                                                             new ColumnDefinition("size",
                                                                                  SqlType.INT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final List<String> attributeColumnNames = Arrays.asList("color", "size");

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);
            final String     color0 = "blue";
            final int        size0  = 0;

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            attributeColumnNames,
                            Arrays.asList(color0, size0));


            gpkg.features().visitFeatures(featureSet,
                                          feature -> { assertEquals("getFeature() returned an incorrect geometry",
                                                                    point0,
                                                                    feature.getGeometry());

                                                       assertEquals("getFeature returned an incorrect attribute",
                                                                    color0,
                                                                    feature.getAttributes().get("color"));

                                                       assertEquals("getFeature returned an incorrect attribute",
                                                                    size0,
                                                                    feature.getAttributes().get("size"));

                                                     });


        }
    }

    /**
     * Test visitFeatures() with a null consumer
     */
    @Test(expected = IllegalArgumentException.class)
    public void visitFeaturesNullConsumer() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"),
                                                             new ColumnDefinition("color",
                                                                                  SqlType.TEXT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null),
                                                             new ColumnDefinition("size",
                                                                                  SqlType.INT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null));

            gpkg.features().visitFeatures(featureSet, null);
        }
    }

    /**
     * Test visitFeatures() with a null feature set
     */
    @Test(expected = IllegalArgumentException.class)
    public void visitFeaturesNullFeatureSet() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().visitFeatures(null, feature -> {});
        }
    }

    /**
     * Test addFeature() with a null geometryColumn
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureNullGeometryColumn() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features()
                .addFeature(null,
                            new WkbPoint(0.0, 0.0),
                            Collections.emptyList(),
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureNullGeometry() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeature(geometryColumn,
                            null,
                            Collections.emptyList(),
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with a null attribute column list
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureNullAttributeColumnNames() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            null,
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with a null attribute value collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureNullAttributeValues() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            Collections.emptyList(),
                            null);
        }
    }

    /**
     * Test addFeature() with attribute column names and values differing in length
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureAttributeListsDiffer() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            Arrays.asList("foo"),
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with the wrong geometry type
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureWrongGeometryType() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.LineString.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            Collections.emptyList(),
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with the wrong geometry dimensionality
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeatureWrongDimensions() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final WkbPoint point0 = new WkbPoint(0.0, 0.0);

            gpkg.features()
                .addFeature(geometryColumn,
                            point0,
                            Collections.emptyList(),
                            Collections.emptyList());
        }
    }

    /**
     * Test addFeature() with an unrecognized geometry type
     */
    @Test(expected = RuntimeException.class)
    public void addFeatureUnrecognizedGeometryType() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          "POINT",
                                                                                          ValueRequirement.Prohibited,
                                                                                          ValueRequirement.Prohibited,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final Geometry geometry = new UnrecognizedGeometry();

            final int featureId = gpkg.features()
                                      .addFeature(geometryColumn,
                                                  geometry,
                                                  Collections.emptyList(),
                                                  Collections.emptyList())
                                      .getIdentifier();

            gpkg.features().getFeature(featureSet, featureId);
        }
    }

    /**
     * Test addFeature() with an unrecognized geometry type
     */
    @Test(expected = WellKnownBinaryFormatException.class)
    public void addFeatureUnrecognizedGeometryType2() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          "FOO",
                                                                                          ValueRequirement.Prohibited,
                                                                                          ValueRequirement.Prohibited,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final Geometry geometry = new UnrecognizedGeometry2();

            final int featureId = gpkg.features()
                                      .addFeature(geometryColumn,
                                                  geometry,
                                                  Collections.emptyList(),
                                                  Collections.emptyList())
                                      .getIdentifier();

            gpkg.features().getFeature(featureSet, featureId);
        }
    }

    /**
     * Test addFeatures()
     */
    @Test
    public void addFeatures() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"),
                                                             new ColumnDefinition("color",
                                                                                  SqlType.TEXT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null),
                                                             new ColumnDefinition("size",
                                                                                  SqlType.INT.toString(),
                                                                                  null,
                                                                                  null,
                                                                                  ColumnDefault.None,
                                                                                  null));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            final List<String> attributeColumnNames = Arrays.asList("color", "size");

            final WkbPointZM point0 = new WkbPointZM(0.0, 0.0, 0.0, 0.0);
            final String     color0 = "blue";
            final int        size0  = 0;

            final WkbPointZM point1 = new WkbPointZM(1.0, 1.0, 1.0, 1.0);
            final String     color1 = "red";
            final int        size1  = 1;

            gpkg.features()
                .addFeatures(geometryColumn,
                             attributeColumnNames,
                             Arrays.asList(Pair.of(point0, Arrays.asList(color0, size0)),
                                           Pair.of(point1, Arrays.asList(color1, size1))));

            final List<Feature> features = gpkg.features().getFeatures(featureSet);

            assertEquals("getFeatures() returned the incorrect number of features",
                         2,
                         features.size());

            final Feature feature0 = features.get(0);   // TODO it's probably not correct to rely on the order of query results

            assertEquals("getFeatures() returned an incorrect geometry",
                         point0,
                         feature0.getGeometry());

            assertEquals("getFeatures returned an incorrect attribute",
                         color0,
                         feature0.getAttributes().get("color"));

            assertEquals("getFeatures returned an incorrect attribute",
                         size0,
                         feature0.getAttributes().get("size"));

            final Feature feature1 = features.get(1);   // TODO it's probably not correct to rely on the order of query results

            assertEquals("getFeatures() returned an incorrect geometry",
                         point1,
                         feature1.getGeometry());

            assertEquals("getFeatures returned an incorrect attribute",
                         color1,
                         feature1.getAttributes().get("color"));

            assertEquals("getFeatures returned an incorrect attribute",
                         size1,
                         feature1.getAttributes().get("size"));
        }
    }

    /**
     * Test addFeatures() with a null geometry column
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullGeometryColumn() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            gpkg.features()
                .addFeatures(null,
                             Collections.emptyList(),
                             Collections.emptyList());
        }
    }

    /**
     * Test addFeatures() with a null attribute column names collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullAttributeColumnNames() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             null,
                             Collections.emptyList());
        }
    }

    /**
     * Test addFeatures() with a null feature iterable
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullFeatures() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Collections.emptyList(),
                             null);
        }
    }

    /**
     * Test addFeatures() with a null feature
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullFeature() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Collections.emptyList(),
                             Arrays.asList(null, null));
        }
    }

    /**
     * Test addFeatures() with a null geometry
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullGeometry() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Collections.emptyList(),
                             Arrays.asList(Pair.of(null, Collections.emptyList())));
        }
    }

    /**
     * Test addFeatures() with a null attribute collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesNullAttributes() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Collections.emptyList(),
                             Arrays.asList(Pair.of(new WkbPoint(1.0, 1.0), null)));
        }
    }

    /**
     * Test addFeatures() with a incorrect number of attribute values
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesWrongNumberOfAttributes() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Arrays.asList("foo"),
                             Arrays.asList(Pair.of(new WkbPoint(1.0, 1.0), Collections.emptyList())));
        }
    }

    /**
     * Test addFeatures() with the wrong geometry type
     */
    @Test(expected = IllegalArgumentException.class)
    public void addFeaturesWrongGeometryType() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet("mytable",
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             "id",
                                                             new GeometryColumnDefinition("geometry",
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Mandatory,
                                                                                          ValueRequirement.Mandatory,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            gpkg.features()
                .addFeatures(geometryColumn,
                             Collections.emptyList(),
                             Arrays.asList(Pair.of(new WkbLineString(new Coordinate(0.0, 0.0)), Collections.emptyList())));
        }
    }

    /**
     * Test registerGeometryFactory()
     */
    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void registerGeometryFactory() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().registerGeometryFactory(0, byteBuffer -> null);
        }
    }

    /**
     * Test registerGeometryFactory() with a negative geometry type code
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGeometryFactoryNegativeGeometryCode() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().registerGeometryFactory(-1, byteBuffer -> null);
        }
    }

    /**
     * Test registerGeometryFactory() with an out of range geometry type code
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGeometryFactoryOutOfRangeGeometryCode() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().registerGeometryFactory(Long.MAX_VALUE, byteBuffer -> null);
        }
    }

    /**
     * Test registerGeometryFactory() with a null geometry factory
     */
    @Test(expected = IllegalArgumentException.class)
    public void registerGeometryFactoryNullFactory() throws IOException, ConformanceException, SQLException, ClassNotFoundException
    {
        try(final GeoPackage gpkg = new GeoPackage(TestUtility.getRandomFile()))
        {
            gpkg.features().registerGeometryFactory(0, null);
        }
    }

    /**
     * Test createGeometry() for a bad WKB header (too few bytes)
     */
    @Test(expected = WellKnownBinaryFormatException.class)
    public void createGeometryBadWellKnownBinaryHeader() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        final int featureId;

        final String tableName = "mytable";
        final String geometryColumnName = "geometry";
        final String identifierColumnName = "id";

        final File file = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(file))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet(tableName,
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             identifierColumnName,
                                                             new GeometryColumnDefinition(geometryColumnName,
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Prohibited,
                                                                                          ValueRequirement.Prohibited,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            featureId = gpkg.features()
                            .addFeature(geometryColumn,
                                        new WkbPoint(0.0, 0.0),
                                        Collections.emptyList(),
                                        Collections.emptyList()).getIdentifier();
        }

        try(final ByteOutputStream stream = new ByteOutputStream())
        {
            final WkbGeometryCollection<WkbGeometry> dummyGeometryCollection = new WkbGeometryCollection<>();

            BinaryHeader.writeBytes(stream,
                                    dummyGeometryCollection,
                                    -1);

            stream.write((byte)0);    // byte order
            //noinspection NumericCastThatLosesPrecision
            stream.write((int)dummyGeometryCollection.getTypeCode()); // geometry type code

            stream.write(1); // intentionally misrepresent the number of points this collection contains

            stream.write((byte)0);  // write one extra byte of garbage

            //noinspection CallToDriverManagerGetConnection
            try(final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toURI()))
            {
                JdbcUtility.update(connection,
                                   String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                                                 tableName,
                                                 geometryColumnName,
                                                 identifierColumnName),
                                   preparedStatement -> { preparedStatement.setBytes(1, stream.array());
                                                          preparedStatement.setInt  (2, featureId);
                                                        });
            }
        }

        try(final GeoPackage gpkg = new GeoPackage(file, GeoPackage.OpenMode.Open))
        {
            final FeatureSet featureSet = gpkg.features().getFeatureSet(tableName);

            gpkg.features().getFeatures(featureSet);
        }
    }

    /**
     * Test createGeometry() for buffer underflow on bad WKB data
     */
    @Test(expected = WellKnownBinaryFormatException.class)
    public void createGeometryBufferUnderflow() throws IOException, ConformanceException, SQLException, ClassNotFoundException, WellKnownBinaryFormatException
    {
        final int featureId;

        final String tableName = "mytable";
        final String geometryColumnName = "geometry";
        final String identifierColumnName = "id";

        final File file = TestUtility.getRandomFile();

        try(final GeoPackage gpkg = new GeoPackage(file))
        {
            final FeatureSet featureSet = gpkg.features()
                                              .addFeatureSet(tableName,
                                                             "identifier",
                                                             "description",
                                                             new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                                             gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                                             identifierColumnName,
                                                             new GeometryColumnDefinition(geometryColumnName,
                                                                                          GeometryType.Point.toString(),
                                                                                          ValueRequirement.Prohibited,
                                                                                          ValueRequirement.Prohibited,
                                                                                          "comment"));

            final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

            featureId = gpkg.features()
                            .addFeature(geometryColumn,
                                        new WkbPoint(0.0, 0.0),
                                        Collections.emptyList(),
                                        Collections.emptyList()).getIdentifier();
        }

        try(final ByteOutputStream stream = new ByteOutputStream())
        {
            final WkbMultiPoint dummyGeometryCollection = new WkbMultiPoint();

            BinaryHeader.writeBytes(stream,
                                    dummyGeometryCollection,
                                    -1);

            stream.write((byte)0);    // byte order
            //noinspection NumericCastThatLosesPrecision
            stream.write((int)dummyGeometryCollection.getTypeCode()); // geometry type code

            stream.write(10); // intentionally misrepresent the number of points this collection contains

            //noinspection CallToDriverManagerGetConnection
            try(final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.toURI()))
            {
                JdbcUtility.update(connection,
                                   String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                                                 tableName,
                                                 geometryColumnName,
                                                 identifierColumnName),
                                   preparedStatement -> { preparedStatement.setBytes(1, stream.array());
                                                          preparedStatement.setInt  (2, featureId);
                                                        });
            }
        }

        try(final GeoPackage gpkg = new GeoPackage(file, GeoPackage.OpenMode.Open))
        {
            final FeatureSet featureSet = gpkg.features().getFeatureSet(tableName);

            gpkg.features().getFeatures(featureSet);
        }
    }

    private static class BaseInnerGeometry extends Geometry
    {
        @Override
        public boolean equals(final Object obj)
        {
            return false;
        }

        @Override
        public int hashCode()
        {
            return 0;
        }

        @Override
        public long getTypeCode()
        {
            return 0;
        }

        @Override
        public String getGeometryTypeName()
        {
            return null;
        }

        @Override
        public boolean hasZ()
        {
            return false;
        }

        @Override
        public boolean hasM()
        {
            return false;
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
        {

        }

        @Override
        public Envelope createEnvelope()
        {
            return null;
        }
    }

    private static class UnrecognizedGeometry extends BaseInnerGeometry
    {
        @Override
        public long getTypeCode()
        {
            return 999999;
        }

        @Override
        public String getGeometryTypeName()
        {
            return "POINT";   // intentionally wrong to force BinaryType.Standard
        }

        @Override
        public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
        {
            this.writeWellKnownBinaryHeader(byteOutputStream); // Checks byteOutputStream for null
        }

        @Override
        public Envelope createEnvelope()
        {
            return Envelope.Empty;
        }
    }

    private static class UnrecognizedGeometry2 extends BaseInnerGeometry
    {
        @Override
        public long getTypeCode()
        {
            return 999999;
        }

        @Override
        public String getGeometryTypeName()
        {
            return "FOO";   // intentionally wrong to force BinaryType.Standard
        }

        @Override
        public void writeWellKnownBinary(final ByteOutputStream byteOutputStream)
        {
            this.writeWellKnownBinaryHeader(byteOutputStream); // Checks byteOutputStream for null
        }

        @Override
        public Envelope createEnvelope()
        {
            return Envelope.Empty;
        }
    }
}
