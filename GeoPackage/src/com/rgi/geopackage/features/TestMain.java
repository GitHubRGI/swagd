package com.rgi.geopackage.features;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Luke Lambert
 */
public final class TestMain
{
    private TestMain() {}

    public static void main(final String[] args)
    {
        //final File gpkgFile = new File("test.gpkg");
        //
        //if(gpkgFile.exists())
        //{
        //    if(!gpkgFile.delete())
        //    {
        //        throw new RuntimeException("Unable to delete test file " + gpkgFile.getName());
        //    }
        //}
        //
        //try(final GeoPackage gpkg = new GeoPackage(gpkgFile, VerificationLevel.None, GeoPackage.OpenMode.Create))
        //{
        //    final FeatureSet featureSet = gpkg.features().addFeatureSet("features",
        //                                                                "features",
        //                                                                "description",
        //                                                                new BoundingBox(0.0, 0.0, 0.0, 0.0),
        //                                                                gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
        //                                                                new GeometryColumnDefinition("the_geom",
        //                                                                                             GeometryType.Point.toString(),
        //                                                                                             ValueRequirement.Prohibited,
        //                                                                                             ValueRequirement.Prohibited,
        //                                                                                             "who doesn't love points?"));
        //
        //    final GeometryColumn geometryColumn = gpkg.features().getGeometryColumnName(featureSet);
        //
        //    gpkg.features().addFeature(geometryColumn,
        //                               new WktPoint(1.0, 1.0),
        //                               Collections.emptyMap());
        //}

        final File gpkgFile = new File("C:/Users/corp/Desktop/sample data/geometry/usterritories_1.gpkg");

        try(final GeoPackage gpkg = new GeoPackage(gpkgFile, VerificationLevel.None, GeoPackage.OpenMode.Open))
        {
            final Collection<FeatureSet> featureSets = gpkg.features().getFeatureSets();

            for(final FeatureSet featureSet : featureSets)
            {
                //final FeatureSet featureSet = gpkg.features().getFeatureSet("streets");

                System.out.format("%s (table: %s)\n",
                                  featureSet.getIdentifier(),
                                  featureSet.getTableName());

                final GeometryColumn geometryColumn = gpkg.features().getGeometryColumn(featureSet);

                System.out.format("Geometry column: %s, type: %s, z: %s, m: %s\n",
                                  geometryColumn.getColumnName(),
                                  geometryColumn.getGeometryType(),
                                  geometryColumn.getZRequirement().toString(),
                                  geometryColumn.getMRequirement().toString());

                final StringBuilder tableHeader = new StringBuilder();
                final StringBuilder rowFormater = new StringBuilder();

                tableHeader.append(featureSet.getPrimaryKeyColumnName());

                rowFormater.append('%');
                rowFormater.append(featureSet.getPrimaryKeyColumnName().length());
                rowFormater.append("s\t");

                tableHeader.append('\t');
                tableHeader.append(featureSet.getGeometryColumnName());

                rowFormater.append('%');
                rowFormater.append(Math.max(4, featureSet.getGeometryColumnName().length()));
                rowFormater.append("s\t");

                for(final String attributeColumnName : featureSet.getAttributeColumnNames())
                {
                    tableHeader.append('\t');
                    tableHeader.append(attributeColumnName);

                    rowFormater.append('%');
                    rowFormater.append(attributeColumnName.length());
                    rowFormater.append("s\t");
                }

                rowFormater.append('\n');

                System.out.println(tableHeader);

                try
                {
                    gpkg.features().visitFeatures(featureSet,
                                                  feature -> { //feature.getGeometry().getTypeCode()

                                                               final Map<String, Object> attributes = feature.getAttributes();

                                                               final List<String> columns = new ArrayList<>(2 + featureSet.getAttributeColumnNames().size());

                                                               columns.add(Integer.toString(feature.getIdentifier()));
                                                               columns.add(Long.toString(feature.getGeometry().getTypeCode()));

                                                               columns.addAll(featureSet.getAttributeColumnNames()
                                                                                        .stream()
                                                                                        .map(attributeColumnName -> attributes.get(attributeColumnName)
                                                                                                                              .toString())
                                                                                        .collect(Collectors.toList()));


                                                               System.out.format(rowFormater.toString(),
                                                                                 columns.toArray());
                                                             });
                }
                catch(final WellKnownBinaryFormatException ex)
                {
                    ex.printStackTrace();
                }

                System.out.println();
            }
        }
        catch(final IOException | SQLException | ConformanceException | ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }
}
