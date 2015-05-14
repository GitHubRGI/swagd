import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
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

            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            //networkExtension.addNetwork();

            final int a = 2;
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
