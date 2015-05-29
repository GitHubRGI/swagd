import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
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
    private static final File geoPackageFile = new File("test.gpkg");
    public static void main(final String[] args)
    {
        try
        {
            System.out.println("edges: " + edges(geoPackageFile));
            System.out.println("nodes: " + nodes(geoPackageFile));
        } catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Gets the number of edges in a GeoPackage
     *
     * @param gpkg the GeoPackage
     * @return the number of edges in gpkg
     * @throws IOException
     * @throws ConformanceException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws BadImplementationException
     */
    public static int edges(final File packageFile) throws ClassNotFoundException, SQLException, ConformanceException, IOException, BadImplementationException
    {
        int edges = 0;
        try (final Connection db = DriverManager.getConnection("jdbc:sqlite:" + packageFile.getPath())) // Initialize the database connection
        {
            final String query = "Select COUNT(id) FROM mynetwork";
            try(final PreparedStatement stmt = db.prepareStatement(query))
            {
                try (ResultSet rs = stmt.executeQuery())
                {
                    edges = rs.getInt(1);
                }
            }
        }
        return edges;
    }

    public static int nodes(final File packageFile) throws ClassNotFoundException, SQLException, ConformanceException, IOException, BadImplementationException
    {
        final HashMap<Integer, Integer> nodeValues = new HashMap<>();
        try(final GeoPackage gpkg = new GeoPackage(packageFile, OpenMode.Open))
        {
            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            final Network network = networkExtension.getNetwork("mynetwork");
            networkExtension.visitEdges(network, t ->
			                                         {
				                                         if(!nodeValues.containsKey(t.getFrom()))
				                                         {
				                                        	 nodeValues.put(t.getFrom(), 0);
				                                         }
				                                         if(!nodeValues.containsKey(t.getFrom()))
				                                         {
				                                        	 nodeValues.put(t.getFrom(), 0);
				                                         }
			                                         });
        }
        return nodeValues.size();
    }
}
