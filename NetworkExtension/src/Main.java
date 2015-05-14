import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.rgi.common.BoundingBox;
import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.DataType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
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

            final Network myNetwork = networkExtension.addNetwork("mynetwork",
                                                                  "Super Important Routing Stuff",
                                                                  "routing stuff. super important",
                                                                  new BoundingBox(0, 0, 0, 0),
                                                                  gpkg.core().getSpatialReferenceSystem(-1));

            final AttributeDescription longitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                     "longitude",
                                                                                                     DataType.Real,
                                                                                                     "longitude",
                                                                                                     AttributedType.Node);

            final AttributeDescription latitudeAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "latitude",
                                                                                                    DataType.Real,
                                                                                                    "latitude",
                                                                                                    AttributedType.Node);

            final AttributeDescription distanceAttribute = networkExtension.addAttributeDescription(myNetwork,
                                                                                                    "distance",
                                                                                                    DataType.Real,
                                                                                                    "distance",
                                                                                                    AttributedType.Edge);

            loadNodeAttributes(networkExtension,
                               new File("C:/Users/corp/Desktop/sample data/networks/contour.1/contour.1.edge"),
                               myNetwork,
                               Arrays.asList(longitudeAttribute, latitudeAttribute));

            final int a = 2;
        }
        catch(final ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException ex)
        {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    /**
     * Puts a file in the Triangle utility node format
     * (https://www.cs.cmu.edu/~quake/triangle.node.html) into a network
     *
     * @param networkExtension
     * @param triangleFormatNodes
     * @param network
     * @param attributeDescriptions
     * @throws IOException
     * @throws SQLException
     */
    private static void loadNodeAttributes(final GeoPackageNetworkExtension networkExtension, final File triangleFormatNodes, final Network network, final List<AttributeDescription> attributeDescriptions) throws SQLException, IOException
    {
        final Function<String, Pair<Integer, List<Object>>> lineToPair = line -> { final String[] pieces = line.trim().split("\\s+");

                                                                                   return new Pair<>(Integer.valueOf(pieces[0]),                         // vertex # (node id)
                                                                                                     Arrays.asList((Object)Integer.valueOf(pieces[1]),   // x (longitude)
                                                                                                                   (Object)Integer.valueOf(pieces[2]))); // y (latitude)
                                                                                 };

        try(Stream<Pair<Integer, List<Object>>> pairs = Files.lines(triangleFormatNodes.toPath())
                                                                                       .skip(1) // the first line is a header
                                                                                       .map(lineToPair))
        {
            networkExtension.addAttributes(attributeDescriptions, pairs::iterator);
        }


    }

    private void loadEdges()
    {

    }

    private String firstLine(final File file) throws IOException
    {
        try(Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.US_ASCII))
        {
            return lines.findFirst().orElseThrow(() -> new IOException("Empty file"));
        }
    }
}
