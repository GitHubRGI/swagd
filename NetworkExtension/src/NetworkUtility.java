//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.HashMap;
//
//import com.rgi.geopackage.GeoPackage;
//import com.rgi.geopackage.extensions.implementation.BadImplementationException;
//import com.rgi.geopackage.extensions.network.Edge;
//import com.rgi.geopackage.extensions.network.Network;
//import com.rgi.geopackage.verification.ConformanceException;
//
///**
// *
// * @author Mary Carome
// *
// */
//public class NetworkUtility
//{
//    private static final File geoPackageFile = new File("test3.gpkg");
//
//    public static void main(final String[] args)
//    {
//        try (GeoPackage gpkg = new GeoPackage(geoPackageFile))
//        {
//            System.out.println("Edges: " + edges(gpkg, "mynetwork"));
//            System.out.println("Nodes: " + nodes(gpkg, "mynetwork"));
//        }
//        catch (ClassNotFoundException | ConformanceException | IOException | SQLException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Gets the number of edges in the Network
//     *
//     * @param gpkg the GeoPackage
//     * @return the number of edges in gpkg
//     * @throws IOException
//     * @throws ConformanceException
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     * @throws BadImplementationException
//     */
//    public static int edges(final GeoPackage gpkg, final String networkTable) throws ClassNotFoundException, SQLException, ConformanceException, IOException, BadImplementationException
//    {
//        final HashMap<Integer, Edge> edges = new HashMap<>();
//        final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//        final Network network = networkExtension.getNetwork(networkTable);
//
//        networkExtension.visitEdges(network, t->
//                                                 {
//                                                     if(!edges.containsKey(t.getIdentifier()))
//                                                     {
//                                                         edges.put(t.getIdentifier(), t);
//                                                     }
//                                                 });
//        return edges.size();
//    }
//
//    /**
//     * Determines the number of nodes in the Network
//     *
//     * @param packageFile
//     * @return number of nodes in the Network
//     * @throws ClassNotFoundException
//     * @throws SQLException
//     * @throws ConformanceException
//     * @throws IOException
//     * @throws BadImplementationException
//     */
//    public static int nodes(final GeoPackage gpkg, final String networkTable) throws ClassNotFoundException, SQLException, ConformanceException, IOException, BadImplementationException
//    {
//        final HashMap<Integer, Integer> nodeValues = new HashMap<>();
//        final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//        final Network network = networkExtension.getNetwork(networkTable);
//
//        networkExtension.visitEdges(network, t ->
//                                                 {
//                                                     if(!nodeValues.containsKey(t.getFrom()))
//                                                     {
//                                                         nodeValues.put(t.getFrom(), 0);
//                                                     }
//                                                     if(!nodeValues.containsKey(t.getFrom()))
//                                                     {
//                                                         nodeValues.put(t.getFrom(), 0);
//                                                     }
//                                                  });
//        return nodeValues.size();
//    }
//}
//
