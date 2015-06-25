import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.referencesystem.profile.SphericalMercatorCrsProfile;


/**
 *
 * @author Mary Carome
 *
 */
public class NetworkUtility
{
    //private static final File geoPackageFile = new File("test3.gpkg");

    public static void main(final String[] args)
    {
        final SphericalMercatorCrsProfile profile = new SphericalMercatorCrsProfile();

        final Coordinate<Double> min = profile.toGlobalGeodetic(new Coordinate<>(574780.00, 4575780.00));

        System.out.println(min.toString());

        final Coordinate<Double> max = profile.toGlobalGeodetic(new Coordinate<>(587812.94, 4585000.00));

        System.out.println(max.toString());
    }
}

