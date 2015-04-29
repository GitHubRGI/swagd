package com.rgi.geopackage.extensions.network;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.Extension;
import com.rgi.geopackage.verification.ConformanceException;
import com.rgi.geopackage.verification.VerificationLevel;

public class Main
{
    public static void main(final String[] args)
    {
                final Set<String> poo = (new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
                                                                           .setScanners(new SubTypesScanner(false)))).getAllTypes();


        poo.stream()
           .sorted()
           .forEach(tipe -> System.out.println(tipe));

        final File file = new File("test.gpkg");

        if(file.exists())
        {
            file.delete();
        }

        try(final GeoPackage gpkg = new GeoPackage(file, VerificationLevel.None, OpenMode.Create))
        {
            Extension extension = gpkg.extensions().getExtension(null, null, "rgi_network");

            if(extension == null)
            {
                extension = GeoPackageNetworkExtension.enableExtension(gpkg.extensions());
            }

            final GeoPackageNetworkExtension foo = gpkg.extensions().getExtensionImplementation(extension, GeoPackageNetworkExtension.class);

            final int a = 2;
        }
        catch(ClassNotFoundException | SQLException | ConformanceException | IOException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
