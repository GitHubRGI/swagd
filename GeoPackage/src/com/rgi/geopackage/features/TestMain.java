package com.rgi.geopackage.features;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Luke Lambert
 */
public final class TestMain
{
    private TestMain() {}

    public static void main(final String[] args)
    {
        final File gpkgFile = new File("test.gpkg");

        if(gpkgFile.exists())
        {
            if(!gpkgFile.delete())
            {
                throw new RuntimeException("Unable to delete test file " + gpkgFile.getName());
            }
        }

        try(final GeoPackage gpkg = new GeoPackage(gpkgFile, VerificationLevel.None, GeoPackage.OpenMode.Create))
        {
            gpkg.features().addFeatureSet("features",
                                          "features",
                                          "description",
                                          new BoundingBox(0.0, 0.0, 0.0, 0.0),
                                          gpkg.core().getSpatialReferenceSystem("EPSG", 4326),
                                          new GeometryColumnDefinition("the_geom",
                                                                       GeometryType.Point.toString(),
                                                                       ValueRequirement.Prohibited,
                                                                       ValueRequirement.Prohibited,
                                                                       "who doesn't love points?"));
        }
        catch(final IOException | SQLException | ConformanceException | ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
    }
}
