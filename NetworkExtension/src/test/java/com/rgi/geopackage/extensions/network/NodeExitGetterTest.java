/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.geopackage.extensions.network;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.verification.ConformanceException;
import org.junit.Before;
import org.junit.Test;
import utility.TestUtility;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/*
 * Created by steven.lander on 11/2/15.
 */
public class NodeExitGetterTest
{
    @Before
    public void setUp() throws IOException, URISyntaxException
    {
        this.gpkgFile = TestUtility.loadFileFromDisk("testNetwork.gpkg");
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
            return DriverManager.getConnection("jdbc:sqlite:" + gpkgFile.toURI());
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

            final NodeExitGetter nge = new NodeExitGetter(connection,
                                                          network,
                                                          networkExtension.getAttributeDescriptions(network, AttributedType.Node),
                                                          null);
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

            // There should be 3 exits from node 1 in the network
            assertEquals("Exits from node 1 should be 3.", 3, nge.getExits(1).size());
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

    private File gpkgFile;
    private GeoPackageNetworkExtension networkExtension;
    private Connection connection;
    private Network network;
}
