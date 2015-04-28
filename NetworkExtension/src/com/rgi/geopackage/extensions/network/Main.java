package com.rgi.geopackage.extensions.network;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

public class Main
{
    public static void main(final String[] args)
    {
        final File file = new File("test.gpkg");

        if(file.exists())
        {
            file.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Create))
        {

        }
        catch(ClassNotFoundException | SQLException | ConformanceException | IOException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
