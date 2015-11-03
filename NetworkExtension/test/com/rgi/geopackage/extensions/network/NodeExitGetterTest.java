package com.rgi.geopackage.extensions.network;

import com.mockrunner.mock.jdbc.MockConnection;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.verification.ConformanceException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by steven.lander on 11/2/15.
 */
public class NodeExitGetterTest
{
    @Before
    public void setUp() throws IOException
    {
        final File gpkgFile = new File(ClassLoader.getSystemResource("testNetwork.gpkg").getFile());
        try(final GeoPackage gpkg = new GeoPackage(gpkgFile, GeoPackage.OpenMode.Open))
        {
            // Create the connection
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + gpkgFile.getAbsolutePath());

            // Create the network
            this.networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
            this.network = this.networkExtension.getNetwork("alaska2");
        }
        catch(final ClassNotFoundException | ConformanceException | SQLException | BadImplementationException ignored)
        {
            System.out.println("Cannot initialize the test network.");
        }
    }

    @Test
    public void testOpen()
    {
        final Collection<AttributeDescription> nodeAttributeDescriptions = Collections.emptyList();
        final Collection<AttributeDescription> edgeAttributeDescriptions = Collections.emptyList();
        try
        {
            // There should be 3 exits from node 1 in the network
            nodeAttributeDescriptions.add(this.networkExtension.getAttributeDescription(this.network, "alaska2", AttributedType.Node));
            edgeAttributeDescriptions.add(this.networkExtension.getAttributeDescription(this.network, "alaska2", AttributedType.Edge));
            final NodeExitGetter nge = new NodeExitGetter(this.connection, this.network, nodeAttributeDescriptions, edgeAttributeDescriptions);
        }
        catch(final SQLException ignored)
        {
            fail("Failed to open the node exit getter.");
        }
    }

    @Test
    public void testOpenGetExitsOne()
    {
        try(final NodeExitGetter nge = new NodeExitGetter(this.connection, this.network, null, null))
        {
            // There should be 3 exits from node 1 in the network
            assertEquals("Exits from node 1 should be 3.", 3, nge.getExits(1).size());
        }
        catch(final SQLException ignored)
        {
            fail("Get exits returned an invalid number of exits.");
        }
    }

    @Test
    public void testOpenGetExitsTwo()
    {
        try(final NodeExitGetter nge = new NodeExitGetter(this.connection, this.network, null, null))
        {
            // There should be 3 exits from node 1 in the network
            final List<AttributedEdge> results = nge.getExits(-1);
            assertNull("Exits from node 1 should be 3.", results);
        }
        catch(final SQLException ignored)
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

    private GeoPackageNetworkExtension networkExtension;
    private Connection connection;
    private Network network;
}
