import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;


/**
 *
 * @author Mary Carome
 *
 */
public class NetworkUtility
{
    private static final File geoPackageFile = new File("contour.1.gpkg");

    public static void main(final String[] args)
    {
        try(GeoPackage gpkg = new GeoPackage(geoPackageFile))
        {
            final double longitude = 0.0;
            final double latitude  = 0.0;

            snap(gpkg, "mynetwork", longitude, latitude);
        }
        catch(ClassNotFoundException | ConformanceException | IOException | SQLException | BadImplementationException e)
        {
            e.printStackTrace();
        }
    }

    private static int snap(final GeoPackage gpkg,
                            final String     networkTable,
                            final double     longitude,
                            final double     latitude) throws ClassNotFoundException, SQLException, ConformanceException, IOException, BadImplementationException
    {
        final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        final Network network = networkExtension.getNetwork(networkTable);

        final AttributeDescription longitudeDesription = networkExtension.getAttributeDescription(network, "longitude", AttributedType.Node);
        final AttributeDescription latitudeDesription  = networkExtension.getAttributeDescription(network, "latitude",  AttributedType.Node);

        final Pair<Integer, Double> closest = networkExtension.accumulateNodes(network,
                                                                               Pair.of(null, Double.MAX_VALUE),
                                                                               (nodeIdentifier, attributes) -> { final double nodeLongitude = (Double)attributes.get(0);
                                                                                                                 final double nodeLatitude  = (Double)attributes.get(1);

                                                                                                                 final double longitudeDifference = nodeLongitude - longitude;
                                                                                                                 final double latitudeDifference  = nodeLatitude  - latitude;

                                                                                                                 final double distanceSquared = (longitudeDifference*longitudeDifference) + (latitudeDifference*latitudeDifference);

                                                                                                                 return Pair.of(nodeIdentifier, distanceSquared);
                                                                                                               },
                                                                               (currentValue, newValue) -> (currentValue.getRight() < newValue.getRight()) ? newValue : currentValue,
                                                                               longitudeDesription,
                                                                               latitudeDesription);
        return closest.getLeft();
    }
}
