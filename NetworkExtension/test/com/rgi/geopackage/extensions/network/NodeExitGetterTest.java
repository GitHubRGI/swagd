package com.rgi.geopackage.extensions.network;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.verification.ConformanceException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/*
 * Created by steven.lander on 11/2/15.
 */
public class NodeExitGetterTest
{
    @Before
    public void setUp() throws IOException
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch(final ClassNotFoundException exception)
        {
            // Could not register driver
            throw new IOException(exception);
        }
    }

    public static GeoPackage getGeoPackage(final File gpkgFile) throws IOException
    {
        try
        {
            return new GeoPackage(gpkgFile, GeoPackage.OpenMode.Open);
        }
        catch(final ClassNotFoundException | ConformanceException | SQLException exception)
        {
            System.out.println("Could not open test GeoPackage.");
            throw new IOException(exception);
        }
    }

    public static Connection getConnection(final File gpkgFile) throws IOException
    {
        try
        {
            return DriverManager.getConnection("jdbc:sqlite:" + gpkgFile.getAbsolutePath());
        }
        catch(final SQLException exception)
        {
            System.out.println("Could not get DB instance.");
            throw new IOException(exception);
        }
    }

    public static GeoPackageNetworkExtension getNetworkExtension(final GeoPackage gpkg) throws IOException
    {
        try
        {
            return gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        }
        catch(final BadImplementationException exception)
        {
            System.out.println("Could not get network extension.");
            throw new IOException(exception);
        }
    }

    public static Network getNetwork(final GeoPackageNetworkExtension networkExtension) throws IOException
    {
        try
        {
            return networkExtension.getNetwork("alaska2");
        }
        catch(final SQLException exception)
        {
            System.out.println("Could not get network.");
            throw new IOException(exception);
        }
    }

    @Test
    public void testOpen()
    {
        try(final GeoPackage gpkg = new GeoPackage(this.gpkgFile, GeoPackage.OpenMode.Open);
            final Connection connection = NodeExitGetterTest.getConnection(this.gpkgFile))
        {
            final GeoPackageNetworkExtension networkExtension = NodeExitGetterTest.getNetworkExtension(gpkg);
            final Network network = NodeExitGetterTest.getNetwork(networkExtension);

            edgeAttributeDescriptions.add(networkExtension.getAttributeDescription(network, "alaska2", AttributedType.Edge));
            final NodeExitGetter nge = new NodeExitGetter(connection,
                                                          network,
                                                          networkExtension.getAttributeDescriptions(network, AttributedType.Node),
                                                          networkExtension.getAttributeDescriptions(network, AttributedType.Edge));
        }
        catch(final IOException | ClassNotFoundException | ConformanceException | SQLException ignored)
        {
            fail("Failed to open the node exit getter.");
        }
    }

    @Test
    public void testOpenGetExitsOne()
    {
        try(final GeoPackage gpkg = new GeoPackage(this.gpkgFile, GeoPackage.OpenMode.Open);
            final Connection connection = NodeExitGetterTest.getConnection(this.gpkgFile))
        {
            final Network network = NodeExitGetterTest.getNetwork(NodeExitGetterTest.getNetworkExtension(gpkg));
            final NodeExitGetter nge = new NodeExitGetter(connection, network, null, null);
            nge.close();
        }
        catch(final IOException | ClassNotFoundException | ConformanceException | SQLException ignored)
        {
            fail("Get exits returned an invalid number of exits.");
        }
    }

    @Test
    public void testOpenGetExitsTwo()
    {
        try(final GeoPackage gpkg = new GeoPackage(this.gpkgFile, GeoPackage.OpenMode.Open);
            final Connection connection = NodeExitGetterTest.getConnection(this.gpkgFile))
        {
            final Network network = NodeExitGetterTest.getNetwork(NodeExitGetterTest.getNetworkExtension(gpkg));
            final NodeExitGetter nge = new NodeExitGetter(connection, network, null, null);

            // -1 is an invalid node identifier
            final List<AttributedEdge> results = nge.getExits(-1);
            assertNull("Exits from node 1 should be 3.", results);
        }
        catch(final IOException | ClassNotFoundException | ConformanceException | SQLException ignored)
        {
            fail("An invalid exit identifier produced non-null results.");
        }
    }

    /*@Test
    public void testProcessResult()
    {
        final Supplier<String> sqlSupplier = () ->
        {
            return "SELECT count(id) from alaska2_node_attributes;";
        };
        try(final NodeExitGetter nge = new NodeExitGetter(this.connection, this.network, null, null);
            final PreparedStatement preparedStatement = this.connection.prepareStatement(sqlSupplier.get());
            final ResultSet resultSet = preparedStatement.executeQuery())
        {

        }
        catch(final SQLException ignored)
        {
            fail("Could not create a new node exit getter.");
        }
    }*/

    private final File gpkgFile = new File(ClassLoader.getSystemResource("testNetwork.gpkg").getFile());
    private GeoPackageNetworkExtension networkExtension;
    private Connection connection;
    private Network network;
}
