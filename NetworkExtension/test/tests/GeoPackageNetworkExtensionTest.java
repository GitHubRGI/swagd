///* The MIT License (MIT)
// *
// * Copyright (c) 2015 Reinventing Geospatial, Inc.
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//package tests;
//
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.Test;
//
//import utility.TestUtility;
//
//import com.rgi.common.BoundingBox;
//import com.rgi.common.Pair;
//import com.rgi.geopackage.GeoPackage;
//import com.rgi.geopackage.core.SpatialReferenceSystem;
//import com.rgi.geopackage.extensions.Scope;
//import com.rgi.geopackage.extensions.implementation.BadImplementationException;
//import com.rgi.geopackage.extensions.network.AttributeDescription;
//import com.rgi.geopackage.extensions.network.AttributedType;
//import com.rgi.geopackage.extensions.network.DataType;
//import com.rgi.geopackage.extensions.network.Edge;
//import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
//import com.rgi.geopackage.extensions.network.Network;
//import com.rgi.geopackage.verification.ConformanceException;
//
///**
// * Unit tests for GeoPackageNetworkExtensions
// *
// * @author Mary Carome
// *
// */
//@SuppressWarnings({"static-method"})
//public class GeoPackageNetworkExtensionTest
//{
//    /**
//     * Tests getExtensionName
//     */
//    @Test
//    public void testGetExtensionName()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final String networkName = "rgi_network";
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            assertTrue(String.format("GeopackageNetworkExtensionName method getExtensionName returns %s instead of ", networkExtension.getExtensionName(), networkName),
//                       networkExtension.getExtensionName().equals(networkName));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getDefinition
//     */
//    @Test
//    public void testGetDefinition()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final String definition = "definition";
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            assertTrue(String.format("GeopackageNetworkExtensionName method getDefintion returns %s instead of %s", networkExtension.getDefinition(), definition),
//                       networkExtension.getDefinition().equals(definition));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getDefinition
//     */
//    @Test
//    public void testGetScope()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            assertTrue(String.format("GeopackageNetworkExtensionName method getScope returns %s instead of %s", networkExtension.getScope(), Scope.ReadWrite),
//                       networkExtension.getScope().equals(Scope.ReadWrite));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeCount throws an exception
//     * when given a null Network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeCountException()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.getEdgeCount(null);
//            fail("Expected getEdgeCount to throw an IllegalArgumentException when given a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeCount correctly returns the
//     * number of edges in an empty network
//     */
//    @Test
//    public void testGetEdgeCount1()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id:", "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            assertTrue(String.format("GeoPackageNetworkExtension method getEdgeCount returned %s, but %s was expected",
//                                     networkExtension.getEdgeCount(network),
//                                     0),
//                       networkExtension.getEdgeCount(network) == 0);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeCount correctly returns the
//     * number of edges in a nonempty network
//     */
//    @Test
//    public void testGetEdgeCount2()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id:", "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Iterable<Pair<Integer, Integer>> edges = Arrays.asList(new Pair<>(12, 23),
//                                                                   new Pair<>(22, 45),
//                                                                   new Pair<>(345, 677),
//                                                                   new Pair<>(234, 456));
//            networkExtension.addEdges(network, edges);
//
//            assertTrue(String.format("GeoPackageNetworkExtension method getEdgeCount returned %s, but %s was expected",
//                                     networkExtension.getEdgeCount(network),
//                                     4),
//                       networkExtension.getEdgeCount(network) == 4);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getNodeCount throws an exception
//     * when given a null Network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeCountException()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.getNodeCount(null);
//            fail("Expected getNodeCount to throw an IllegalArgumentException when given a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getNodeCount correctly returns the
//     * number of nodes in an empty network
//     */
//    @Test
//    public void testGetNodeCount1()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id:", "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            assertTrue(String.format("GeoPackageNetworkExtension method getNodeCount returned %s, but %s was expected",
//                                     networkExtension.getNodeCount(network),
//                                     0),
//                       networkExtension.getNodeCount(network) == 0);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getNodeCount correctly returns the
//     * number of nodes in an empty network
//     */
//    @Test
//    public void testGetNodeCount2()
//    {
//        final File testFile = TestUtility.getRandomFile(10);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id:", "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//
//            final Iterable<Pair<Integer, Integer>> edges = Arrays.asList(new Pair<>(12, 23),
//                                                                         new Pair<>(12, 42),
//                                                                         new Pair<>(34, 56));
//            networkExtension.addEdges(network, edges);
//
//            assertTrue(String.format("GeoPackageNetworkExtension method getNodeCount returned %s, but %s was expected",
//                                     networkExtension.getNodeCount(network),
//                                     0),
//                       networkExtension.getNodeCount(network) == 5);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//    /**
//     * Tests that getNodeAttributesTableName throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentGetNodeAttributesTableName1()
//    {
//        final Network network = null;
//        GeoPackageNetworkExtension.getNodeAttributesTableName(network);
//        fail("Expected GeoPackageNetworkExtension method getNodeAttributesTableName(Network) to throw an exception when passed a null Network");
//    }
//
//    /**
//     * Tests that getNodeAttributesTableName throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void illegalArgumentGetNodeAttributesTableName2()
//    {
//        final String network = null;
//        GeoPackageNetworkExtension.getNodeAttributesTableName(network);
//        fail("Expected GeoPackageNetworkExtension method getNodeAttributesTableName(String) to throw an exception when passed a null String");
//    }
//
//    /**
//     * Test that getNodeAttributesTableName correctly returns the table name
//     * when passed a network
//     */
//    @Test
//    public void getNodeAttributesTableName1()
//    {
//        final File testFile = TestUtility.getRandomFile(3);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String tableName = "my_table";
//            final String expected = "my_table_node_attributes";
//            final Network network = networkExtension.addNetwork(tableName,
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            assertTrue(String.format("GeoPackageNetworkExtension method getNodeAttributesTableName returned %s, but %s was expected",
//                                     GeoPackageNetworkExtension.getNodeAttributesTableName(network),
//                                     expected),
//                       GeoPackageNetworkExtension.getNodeAttributesTableName(network).equals(expected));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Test that getNodeAttributesTableName correctly returns the table name
//     * when passed a non-null String
//     */
//    @Test
//    public void getNodeAttributesTableName2()
//    {
//        final String tableName = "my_table_node_attributes";
//
//        assertTrue(String.format("GeoPackageNetworkExtension method getNodeAttributesTableName returned %s, but %s was expected",
//                                 GeoPackageNetworkExtension.getNodeAttributesTableName("my_table"),
//                                 tableName),
//        GeoPackageNetworkExtension.getNodeAttributesTableName("my_table").equals(tableName));
//    }
//
//    /**
//     * Tests getNetwork throws an IllegalArgumentException
//     * when passed a null networkTableName
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetNetworkException()
//    {
//        final File testFile = TestUtility.getRandomFile(3);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String networkName = null;
//            networkExtension.getNetwork(networkName);
//            fail("Expected GeoPackageNetworkExtension method getNetwork to throw an illegal argument exception when given a null networkTableName");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNetwork returns null when no Network
//     * has been added to the GeoPackage
//     */
//    @Test
//    public void testGetNetworkNull()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String networkName = "my_network";
//            assertNull("Expected GeoPackageNetworkExtension method getNetwork(String) to return null when no network has been added",
//            networkExtension.getNetwork(networkName));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNetwork returns the
//     * correct network
//     */
//    @Test
//    public void testGetNetwork()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String name = "my_table";
//            final String identifier = "test";
//            final String description = "empty";
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//            final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);
//
//            networkExtension.addNetwork(name, identifier, description, box, srs);
//            assertTrue("GeopackageNetworkExtension method getNetwork(String) did not return the correct Network",
//                        networkExtension.getNetwork(name).equals(name,  Network.NetworkContentType, identifier, description, box, srs.getIdentifier()));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException1()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//
//            networkExtension.addNetwork(null,
//                                        "test",
//                                        "empty",
//                                        box,
//                                        gpkg.core().getSpatialReferenceSystem(-1));
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a null tableName");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException2()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//
//            networkExtension.addNetwork("",
//                                        "test",
//                                        "empty",
//                                        box,
//                                        gpkg.core().getSpatialReferenceSystem(-1));
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given an empty tableaName");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException3()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//
//            networkExtension.addNetwork("123abc",
//                                        "test",
//                                        "empty",
//                                        box,
//                                        gpkg.core().getSpatialReferenceSystem(-1));
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given an invalid table");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException4()
//    {
//        final File testFile = TestUtility.getRandomFile(7);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//
//            networkExtension.addNetwork("gpkg_",
//                                        "test",
//                                        "empty",
//                                        box,
//                                        gpkg.core().getSpatialReferenceSystem(-1));
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a table Name that starts with 'gpkg_'");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException5()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.addNetwork("my_table",
//                                        "test",
//                                        "empty",
//                                        null,
//                                        gpkg.core().getSpatialReferenceSystem(-1));
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a null BoundingBox");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddNetworkException6()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String name = "my_table";
//            final String identifier = "test";
//            final String description = "empty";
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//            final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);
//
//            networkExtension.addNetwork(name, identifier, description, box, srs);
//            networkExtension.addNetwork(name, identifier, description, box, srs);
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when adding two networks with the same name");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNetworkException7()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final String identifier = "test";
//            final String description = "empty";
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//            final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);
//
//            networkExtension.addNetwork("my_table_node_attributes", identifier, description, box, srs);
//            networkExtension.addNetwork("my_table", identifier, description, box, srs);
//            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when adding a network whose node attributes table already exists");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNetwork correctly adds networks
//     * with different names
//     */
//    @Test
//    public void testAddNetwork()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final BoundingBox box = new BoundingBox(12, 23, 20, 55);
//            final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);
//
//            networkExtension.addNetwork("my_table",
//                                        "test",
//                                        "empty",
//                                        box,
//                                        srs);
//            assertTrue("GeoPackageNetworkExtension method addNetwork did not add/return the correct Network",
//                       networkExtension.addNetwork("my_second_table", "test, too", "empty", box, srs).equals("my_second_table", Network.NetworkContentType, "test, too", "empty", box, srs.getIdentifier()));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdge throws an IllegalArgumentException
//     * when the Network is null
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetEdgeException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            networkExtension.getEdge(null, 0, 12);
//            fail("Expected GeoPackageNetworkExtension method getEdge(Network, int, int) to throw an IllegalArgumentException when given a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdge throws an IllegalArgumentException when
//     * the edge is not in the network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "empty",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.getEdge(network, 0, 12);
//            fail("Expected GeoPackageNetworkExtension method getEdge to throw an IllegalArgumentException when the edge is not in the network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdge when the edge is
//     * in the given Network
//     */
//    @Test
//    public void testGetEdge()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "empty",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final int from = 12;
//            final int to = 23;
//            networkExtension.addEdge(network,from , to);
//            networkExtension.addEdge(network, 23, 12);
//            networkExtension.addEdge(network, 0, 2);
//
//            final Edge edge = networkExtension.getEdge(network, from, to);
//            assertTrue(String.format("Expected GeoPackageNetworkExtension method getEdge to return %s, but (%s, %s) was returned", printEdge(edge), from, to),
//                       edge.getFrom() == from && edge.getTo() == to);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEntries throws an
//     * Exception when passed a null network
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetEntriesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.getEntries(null, 0);
//            fail("Expected GeoPackageNetworkExtension getEntries(Network, int) to throw an IllegalArgumentException when passed a null network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//// This test is not passing right now, need to modify getEntries so that it throws an exception when the node is not in the network
////    /**
////     * Tests getEntries throws an Exception
////     * when given a node not in the network
////     */
////    @Test(expected = IllegalArgumentException.class)
////    public void testGetEntriesException2()
////    {
////        final File testFile = TestUtility.getRandomFile(5);
////        try(GeoPackage gpkg = new GeoPackage(testFile))
////        {
////            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
////            final Network network = networkExtension.addNetwork("my_table",
////                                                                "blah",
////                                                                "foo",
////                                                                new BoundingBox(0,0,0,0),
////                                                                gpkg.core().getSpatialReferenceSystem(-1));
////            networkExtension.addEdge(network, 12, 10);
////            networkExtension.addEdge(network, 23, 15);
////            networkExtension.addEdge(network, 33, 12);
////
////            networkExtension.getEntries(network, 13);
////            fail("Expected GeoPackageNetworkExtension method getEntries(Network, int) to return an empty list.");
////        }
////        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
////        {
////            e.printStackTrace();
////        }
////        finally
////        {
////            TestUtility.deleteFile(testFile);
////        }
////    }
//
//    /**
//     * Tests getEntries
//     */
//    @Test
//    public void testGetEntries1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "blah",
//                                                                "foo",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//            networkExtension.addEdge(network, 12, 10);
//            networkExtension.addEdge(network, 23, 10);
//            networkExtension.addEdge(network, 33, 10);
//            networkExtension.addEdge(network, 12, 15);
//
//            final List<Edge> list = networkExtension.getEntries(network, 10);
//            assertTrue("Expected GeoPackageNetworkExtension method getEntries(Network, int) to return an nonempty list of size 3.",
//                       list.size() == 3);
//            assertTrue("GeoPackageNetworkExtension method getEntries did not return the correct list of Edges",
//                        list.get(0).getFrom() == 12 &&
//                        list.get(1).getFrom() == 23 &&
//                        list.get(2).getFrom() == 33);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getExits throws an Exception when
//     * passed a null Network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetExitsException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.getExits(null, 0);
//            fail("Expected GeoPackageNetworkExtension getExits(Network, int) to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//// This test is not passing right now, need to modify getExits so that it throws an exception when the node is not in the network
////    /**
////     * Tests getExits return an empty list when given
////     * a node not in the network
////     */
////    @Test(expected = IllegalArgumentException.class)
////    public void testGetExitsException2()
////    {
////        final File testFile = TestUtility.getRandomFile(5);
////        try(GeoPackage gpkg = new GeoPackage(testFile))
////        {
////            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
////            final Network network = networkExtension.addNetwork("my_table",
////                                                                "blah",
////                                                                "foo",
////                                                                new BoundingBox(0,0,0,0),
////                                                                gpkg.core().getSpatialReferenceSystem(-1));
////
////            networkExtension.addEdge(network, 12, 10);
////            networkExtension.addEdge(network, 23, 15);
////            networkExtension.addEdge(network, 33, 12);
////
////            networkExtension.getExits(network, 13);
////            fail("Expected GeoPackageNetworkExtension method getEntries(Network, int) to throw an IllegalArgumentException when the node is not in the network.");
////        }
////        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
////        {
////            e.printStackTrace();
////        }
////        finally
////        {
////            TestUtility.deleteFile(testFile);
////        }
////    }
//
//    /**
//     * Tests getExits
//     */
//    @Test
//    public void testGetExits1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "blah",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addEdge(network, 12, 10);
//            networkExtension.addEdge(network, 12, 15);
//            networkExtension.addEdge(network, 12, 44);
//            networkExtension.addEdge(network, 54, 354);
//
//            final List<Edge> list = networkExtension.getExits(network, 12);
//            assertTrue("Expected GeoPackageNetworkExtension method getExits(Network, int) to return an nonempty list of size 3.",
//                       list.size() == 3);
//            assertTrue("GeoPackageNetworkExtension method getExits did not return the correct list of Edges",
//                        list.get(0).getTo() == 10 &&
//                        list.get(1).getTo() == 15 &&
//                        list.get(2).getTo() == 44);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests visitEdges throws an
//     * Exception when given a null Network
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testVisitEdgesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final List<Edge> edges = new ArrayList<>();
//
//            networkExtension.visitEdges(null, edge-> edges.add(edge));
//            fail("Expected GeoPackageNetworkExtension method visitEdges to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests visitEdges throws an
//     * Exception when given a null Network
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testVisitEdgesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "blah",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.visitEdges(network, null);
//            fail("Expected GeoPackageNetworkExtension method visitEdges to throw an IllegalArgumentException when passed a null Consumer");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests visitEdges
//     */
//    @Test
//    public void testVisitEdges()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "blah",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Edge e1 = networkExtension.addEdge(network, 2, 3);
//            final Edge e2 = networkExtension.addEdge(network, 0, 55);
//            final Edge e3 = networkExtension.addEdge(network, 23, 90);
//
//            final List<Edge> edges = new ArrayList<>();
//            networkExtension.visitEdges(network, edge-> edges.add(edge));
//            assertTrue("GeoPackageNetworkExtension method visitEdges did not visit all edges in the network",
//                       edges.size() == 3 &&
//                       edgesEqual(e1, edges.get(0)) &&
//                       edgesEqual(e2, edges.get(1)) &&
//                       edgesEqual(e3, edges.get(2)));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addEdge throws an Exception when
//     * given a null Network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddEdgeException()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.addEdge(null, 12, 10);
//            fail("Expected GeoPackageNetworkExtension method addEdge to throw an IllegalArgumentException when passed a null Consumer");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Test addEdge returns the correct edge
//     */
//    @Test
//    public void testAddEdge()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "blah",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Edge edge = networkExtension.addEdge(network, 12, 54);
//            assertTrue("GeoPackageNetworkExtension method addEdge did not return the correct edge",
//                        edge.getFrom() == 12 &&
//                        edge.getTo() == 54);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Test addEdges throws an Exception
//     * when given a null network
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddEdgesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.addEdges(null, new ArrayList<Pair<Integer, Integer>>());
//            fail("Expected GeoPackageNetworkExtension method addEdges(Network, Interable) to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Test addEdges throws an Exception
//     * when given a null collection of edges
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddEdgesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "empty",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addEdges(network, null);
//            fail("Expected GeoPackageNetworkExtension method addEdges(Network, Interable) to throw an IllegalArgumentException when passed a null edge Collection");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addEdges properly adds
//     * all edges in the Iterable collection it is given
//     */
//    @Test
//    public void testAddEdges()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("mine",
//                                                                "test",
//                                                                "empty",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final List<Pair<Integer, Integer>> edges = new ArrayList<>();
//            Pair<Integer, Integer> pair;
//            for (int i = 2; i < 6; i++)
//            {
//                pair = new Pair<>(i, i*i);
//                edges.add(pair);
//            }
//            networkExtension.addEdges(network, edges);
//            for (int i = 2; i < 6; i++)
//            {
//                assertTrue(String.format("GeoPackageNetworkExtension method addEdges did not add %s, %s) to the given Network",i, i*i),
//                           networkExtension.getEdge(network, i, i*i) != null);
//            }
//
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributedEdges throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributedEdgesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("table_name",
//                                                                "id", "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network, "name", "units", DataType.Real, "description", AttributedType.Node);
//
//            networkExtension.addAttributedEdges(null, test);
//            fail("Expected GeoPackageNetworkExtension method addAttributedEdges to throw an IllegalArgumentException when given a null attributed edges collection");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributedEdges throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributedEdgesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("table_name",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "name",
//                                                                                       "units",
//                                                                                       DataType.Real,
//                                                                                       "description",
//                                                                                       AttributedType.Node);
//
//            final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges = new ArrayList<>();
//
//            networkExtension.addAttributedEdges(attributedEdges, test);
//            fail("Expected GeoPackageNetworkExtension method addAttributedEdges to throw an IllegalArgumentException when given an empty attributed edges collection");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributedEdges throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributedEdgesException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "test data",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "elev",
//                                                                                       "foo",
//                                                                                       DataType.Real,
//                                                                                       "test",
//                                                                                       AttributedType.Edge);
//
//            final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges = new ArrayList<>();
//            Pair<Pair<Integer, Integer>, List<Object>> pair;
//            Pair<Integer, Integer> edge;
//            for (int i = 2; i < 4; i++)
//            {
//                edge = new Pair<>(i, i*i);
//                pair = new Pair<>(edge, Arrays.asList((Object)122.2,
//                                                      (Object)20));
//                attributedEdges.add(pair);
//            }
//
//            networkExtension.addAttributedEdges(attributedEdges, test);
//            fail("Expected GeoPackageNetworkExtension method addAttributedEdges method to throw an IllegalArgumentException" +
//                 "when the number of AttributeDescriptions does not match the number of attributes for each edge");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributedEdges
//     */
//    @Test
//    public void testAddAttributedEdges()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "test data",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "length",
//                                                                                       "foo",
//                                                                                       DataType.Integer,
//                                                                                       "test",
//                                                                                       AttributedType.Edge);
//
//            final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges = new ArrayList<>();
//            Pair<Pair<Integer, Integer>, List<Object>> pair;
//            Pair<Integer, Integer> edge;
//            for (int i = 2; i < 5; i++)
//            {
//                edge = new Pair<>(i, i*i);
//                pair = new Pair<>(edge, Arrays.asList((Object)(i*122)));
//                attributedEdges.add(pair);
//            }
//
//            networkExtension.addAttributedEdges(attributedEdges, test);
//            final Edge e1 = networkExtension.getEdge(network, 2, 4);
//            final Edge e2 = networkExtension.getEdge(network, 3, 9);
//            final Edge e3 = networkExtension.getEdge(network, 4, 16);
//
//            assertTrue("GeoPackageNetworkExtension method addAttributedEdges did not add attributed edges correctly",
//                       (Integer)networkExtension.getEdgeAttribute(e1, test) == 244);
//            assertTrue("GeoPackageNetworkExtension method addAttributedEdges did not add attributed edges correctly",
//                       (Integer)networkExtension.getEdgeAttribute(e2, test) == 366);
//            assertTrue("GeoPackageNetworkExtension method addAttributedEdges did not add attributed edges correctly",
//                       (Integer)networkExtension.getEdgeAttribute(e3, test) == 488);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests updateEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testUpdateEdgeAttributesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final List<Object> values = new ArrayList<>();
//            final Network network = networkExtension.addNetwork("table_name",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "name",
//                                                                                       "units",
//                                                                                       DataType.Real,
//                                                                                       "description",
//                                                                                       AttributedType.Node);
//
//            networkExtension.updateEdgeAttributes(null, values, test);
//            fail("Expected GeoPackageNetworkExtension method updateEdgeAttributes to throw an IllegalArgumentException when given a null Edge");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests updateEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testUpdateEdgeAttributesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("table_name",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "name",
//                                                                                       "units",
//                                                                                       DataType.Real,
//                                                                                       "description",
//                                                                                       AttributedType.Node);
//
//            final Edge edge = networkExtension.addEdge(network, 0, 23);
//            final List<Object> values = null;
//
//            networkExtension.updateEdgeAttributes(edge, values, test);
//            fail("Expected GeoPackageNetworkExtension method updateEdgeAttributes to throw an IllegalArgumentException when given a null values Collection");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests updateEdgeAttribute throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testUpdateEdgeAttributesException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("table_name",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription test = networkExtension.addAttributeDescription(network,
//                                                                                       "name",
//                                                                                       "units",
//                                                                                       DataType.Real,
//                                                                                       "description",
//                                                                                       AttributedType.Node);
//
//            final Edge edge = networkExtension.addEdge(network, 0, 23);
//            final List<Object> values = new ArrayList<>();
//
//            networkExtension.updateEdgeAttributes(edge, values, test);
//            fail("Expected GeoPackageNetworkExtension method updateEdgeAttributes to throw an IllegalArgumentException when the attribute description list and values collection have different lengths");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests updateEdgeAttributes with edges
//     * already in the table
//     */
//    @Test
//    public void testUpdateEdgeAttributes1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "none",
//                                                                "test",
//                                                                new BoundingBox(0,0,0,0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription length = networkExtension.addAttributeDescription(network,
//                                                                                         "length",
//                                                                                         "meters",
//                                                                                         DataType.Real,
//                                                                                         "blah",
//                                                                                         AttributedType.Edge);
//
//            final AttributeDescription slope = networkExtension.addAttributeDescription(network,
//                                                                                        "slope",
//                                                                                        "feet",
//                                                                                        DataType.Integer,
//                                                                                        "foo",
//                                                                                        AttributedType.Edge);
//
//            final List<Object> values = Arrays.asList((Object)122.56,
//                                                         (Object)23);
//            final List<Object> newValues = Arrays.asList((Object)55.56,
//                                                         (Object)200);
//
//            final List<Pair<Pair<Integer, Integer>, List<Object>>> attributedEdges = new ArrayList<>();
//            attributedEdges.add(new Pair<>(new Pair<>(12, 23), values));
//            attributedEdges.add(new Pair<>(new Pair<>(45, 54), values));
//            networkExtension.addAttributedEdges(attributedEdges, length, slope);
//
//            final Edge edge1 = networkExtension.getEdge(network, 45, 54);
//            networkExtension.updateEdgeAttributes(edge1, newValues, length, slope);
//
//            assertTrue("GeopackageNetworkExtension method updateEdgeAttributes(Edge, List, AttributedDescription did not update the given edge",
//                       (Double)networkExtension.getEdgeAttribute(edge1, length) == 55.56 &&
//                       (Integer)networkExtension.getEdgeAttribute(edge1, slope)== 200);
//
//            final Edge edge2 = networkExtension.getEdge(network, 12, 23);
//
//            assertTrue("GeopackageNetworkExtension method updateEdgeAttributes(Edge, List, AttributedDescription update an additional edge",
//                    (Double)networkExtension.getEdgeAttribute(edge2, length) == 122.56 &&
//                    (Integer)networkExtension.getEdgeAttribute(edge2, slope)== 23);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDescriptions throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetAttributeDescriptionsException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.getAttributeDescriptions(null, AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension(Network, AttributedType to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDescriptions throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetAttributeDescriptionsException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "none",
//                                                                "test",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.getAttributeDescriptions(network, null);
//            fail("Expected GeoPackageNetworkExtension(Network, AttributedType to throw an IllegalArgumentException when passed a null AttributedType");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDescriptions returns an empty list
//     * when applied to a network with no attributes
//     */
//    @Test
//    public void testGetAttributeDescriptions1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "none",
//                                                                "test",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final List<AttributeDescription> list = networkExtension.getAttributeDescriptions(network, AttributedType.Edge);
//            assertTrue("Expected GeoPackageNetworkExtension method getAttributeDescriptions(Network, AttributedType) to return an empty list",
//                       list.size() == 0);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDescriptions returns a list
//     * of attributes for the given type
//     */
//    @Test
//    public void testGetAttributeDescriptions2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "none",
//                                                                "test",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network,
//                                                     "one",
//                                                     "seconds",
//                                                     DataType.Real,
//                                                     "foo",
//                                                     AttributedType.Edge);
//
//            networkExtension.addAttributeDescription(network,
//                                                     "two",
//                                                     "calories",
//                                                     DataType.Real,
//                                                     "bar",
//                                                     AttributedType.Edge);
//
//           networkExtension.addAttributeDescription(network,
//                                                    "three",
//                                                    "fake",
//                                                    DataType.Blob,
//                                                    "foobar",
//                                                    AttributedType.Node);
//
//
//            final List<AttributeDescription> list = networkExtension.getAttributeDescriptions(network, AttributedType.Edge);
//            assertTrue("Expected GeoPackageNetworkExtension method getAttributeDescriptions(Network, AttributedType) to return a list of size 2",
//                       list.size() == 2);
//            assertTrue("GeoPackageNetworkExtension method getAttributeDescriptions(Network, AttributedType) did not return the correct list",
//                       list.get(0).getName().equals("one") &&
//                       list.get(1).getName().equals("two"));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDesciption throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetAttributeDescriptionException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            networkExtension.getAttributeDescription(null, "name", AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method getAttributeDescription(Network, String, AttributedType) to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDesciption throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetAttributeDescriptionException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "test",
//                                                                "none",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.getAttributeDescription(network, null, AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtensionmethod getAttributeDescription(Network, String, AttributedType) to throw an IllegalArgumentException when passed a null String");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDesciption throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetAttributeDescriptionException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "test",
//                                                                "none",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.getAttributeDescription(network, "name", null);
//            fail("Expected GeoPackageNetworkExtensionmethod getAttributeDescription(Network, String, AttributedType) to throw an IllegalArgumentException when passed a null AttributedType");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDesciption on a network
//     * with no attributes
//     */
//    @Test
//    public void testGetAttributeDescription1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "test",
//                                                                "none",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.getAttributeDescription(network, "name", AttributedType.Edge);
//            assertNull(attribute);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getAttributeDesciption on a network
//     * with multiple attributes
//     */
//    @Test
//    public void testGetAttributeDescription2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "test",
//                                                                "none",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network,
//                                                     "foobar",
//                                                     "none",
//                                                     DataType.Real,
//                                                     "empty",
//                                                     AttributedType.Edge);
//
//            networkExtension.addAttributeDescription(network,
//                                                     "uno",
//                                                     "dos",
//                                                     DataType.Real,
//                                                     "tres",
//                                                     AttributedType.Edge);
//
//            networkExtension.addAttributeDescription(network,
//                                                     "une",
//                                                     "deux",
//                                                     DataType.Real,
//                                                     "tois",
//                                                     AttributedType.Edge);
//
//            final AttributeDescription attribute = networkExtension.getAttributeDescription(network, "foobar", AttributedType.Edge);
//            assertTrue("GeoPackageNetworkExtension method getAttributeDescription(Network, String, AttributeType) did not return the correct AttributeDescription",
//                       attribute.getName().equals("foobar") &&
//                       attribute.getUnits().equals("none") &&
//                       attribute.getDescription().equals("empty") &&
//                       attribute.getAttributedType().equals(AttributedType.Edge));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            networkExtension.addAttributeDescription(null,
//                                                     "name",
//                                                     "units",
//                                                     DataType.Real,
//                                                     "description",
//                                                     AttributedType.Edge);
//
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null Network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network,
//                                                     null,
//                                                     "units",
//                                                     DataType.Real,
//                                                     "description",
//                                                     AttributedType.Edge);
//
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null name");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network,
//                                                     "",
//                                                     "units",
//                                                     DataType.Real,
//                                                     "description",
//                                                     AttributedType.Edge);
//
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed an empty name");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException4()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", null, DataType.Real, "description", AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null units");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException5()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", "", DataType.Real, "description", AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed an empty units");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException6()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", "units", null, "description", AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null DataType");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException7()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", "units", DataType.Real, null, AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null description");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException8()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", "units", DataType.Real, "", AttributedType.Edge);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed an empty units");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testAddAttributeDescriptionException9()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network, "name", "units", DataType.Real, "description", null);
//            fail("Expected GeoPackageNetworkExtension method addAttributeDescription(Network, String, String, DataType, String, AttributedType) to throw an IllegalArgumentException when passed a null AttributedType");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addAttributeDescription correctly
//     * adds AttributeDescriptions
//     */
//    @Test
//    public void testAddAttributeDescription()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            networkExtension.addAttributeDescription(network,
//                                                     "name",
//                                                     "units",
//                                                     DataType.Real,
//                                                     "description",
//                                                     AttributedType.Edge);
//
//            final AttributeDescription attribute = networkExtension.getAttributeDescription(network, "name", AttributedType.Edge);
//            assertTrue("GeoPackageNetworkExtension method addAttributeDescription did not correctly add an attribute description",
//                       attribute != null &&
//                       attribute.getName().equals("name") &&
//                       attribute.getUnits().equals("units") &&
//                       attribute.getDataType().equals(DataType.Real) &&
//                       attribute.getDescription().equals("description") &&
//                       attribute.getAttributedType().equals(AttributedType.Edge));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute throws an IllegalArgumetnException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributeException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attributeDescription = networkExtension.addAttributeDescription(network,
//                                                                                                       "name",
//                                                                                                       "units",
//                                                                                                       DataType.Real,
//                                                                                                       "description",
//                                                                                                       AttributedType.Edge);
//
//            networkExtension.getEdgeAttribute(null, attributeDescription);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttribute to throw an IllegalArgumentException when given a null edge");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute throws an IllegalArgumetnException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributeException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("network",
//                                                                "name",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Edge edge = networkExtension.addEdge(network, 12, 21);
//
//            networkExtension.getEdgeAttribute(edge, null);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttribute to throw an IllegalArgumentException when given a null AttributeDescription");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributeException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "edges",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attributeDescription = networkExtension.addAttributeDescription(network,
//                                                                                                       "elevation",
//                                                                                                       "feet",
//                                                                                                       DataType.Real,
//                                                                                                       "none",
//                                                                                                       AttributedType.Node);
//            final Edge edge = networkExtension.addEdge(network, 12, 54);
//            networkExtension.getEdgeAttribute(edge, attributeDescription);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when the AttributeDescription belongs to a node.");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttribute throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributeException4()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "none",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.addAttributeDescription(network,
//                                                                                            "height",
//                                                                                            "feet",
//                                                                                            DataType.Integer,
//                                                                                            "none",
//                                                                                           AttributedType.Edge);
//
//            final AttributeDescription fakeAttribute = networkExtension.addAttributeDescription(network,
//                                                                                                "height",
//                                                                                                "meters",
//                                                                                                DataType.Text,
//                                                                                                "none",
//                                                                                                AttributedType.Node);
//
//            final Edge edge = networkExtension.addEdge(network, 10, 12);
//            networkExtension.updateEdgeAttributes(edge, Arrays.asList((Object)23), attribute);
//
//            networkExtension.getEdgeAttribute(edge, fakeAttribute);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when data type does not match the AttributeDescription");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute returns null
//     * when given an edge that does not have the given attribute
//     */
//    @Test
//    public void testGetEdgeAttribute1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "edges",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attributeDescription = networkExtension.addAttributeDescription(network,
//                                                                                                       "length",
//                                                                                                       "feet",
//                                                                                                       DataType.Real,
//                                                                                                       "none",
//                                                                                                       AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network, 0, 1);
//            assertNull("Expected GeoPackageNetworkExtension method getEdgeAttribute to return null.", networkExtension.getEdgeAttribute(edge, attributeDescription));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute returns the
//     * correct attribute value
//     */
//    @Test
//    public void testGetEdgeAttribute2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "edges",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attributeDescription = networkExtension.addAttributeDescription(network,
//                                                                                                       "length",
//                                                                                                       "feet",
//                                                                                                       DataType.Real,
//                                                                                                       "none",
//                                                                                                       AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network, 12, 15);
//            networkExtension.updateEdgeAttributes(edge, Arrays.asList((Object)23.3), attributeDescription);
//            assertTrue("GeoPackageNetworkExtension method getEdgeAttribute did not return the correct value for the given edge",
//                       (Double)networkExtension.getEdgeAttribute(edge, attributeDescription) == 23.3);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribute returns null when
//     * the edge is not in the table
//     */
//    @Test
//    public void testGetEdgeAttributeException5()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "test",
//                                                                "edges",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Network network2 = networkExtension.addNetwork("my_table_two",
//                                                                 "testing",
//                                                                 "edges",
//                                                                 new BoundingBox(0, 0, 0, 0),
//                                                                 gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attributeDescription = networkExtension.addAttributeDescription(network2,
//                                                                                                       "elevation",
//                                                                                                       "feet",
//                                                                                                       DataType.Real,
//                                                                                                       "none",
//                                                                                                       AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network, 12, 54);
//            assertNull("Expected GeoPackageNetworkExtension method getEdgeAttribute to return null when the given edge is not the network table",
//                       networkExtension.getEdgeAttribute(edge, attributeDescription));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "name",
//                                                                                             "units",
//                                                                                             DataType.Real,
//                                                                                             "descripiton",
//                                                                                             AttributedType.Edge);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "nombre",
//                                                                                             "units",
//                                                                                             DataType.Real,
//                                                                                             "descripiton",
//                                                                                             AttributedType.Edge);
//
//            networkExtension.getEdgeAttributes(null, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when given a null Edge");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Edge edge = networkExtension.addEdge(network, 12, 32);
//
//            networkExtension.getEdgeAttributes(edge);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when given no AttributeDescriptions");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributesException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Edge edge = networkExtension.addEdge(network, 23, 567);
//
//            final AttributeDescription[] attributes = null;
//
//            networkExtension.getEdgeAttributes(edge, attributes);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when given null AttributeDescriptions");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributesException4()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network1 = networkExtension.addNetwork("one",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Network network2 = networkExtension.addNetwork("two",
//                                                                 "id2",
//                                                                 "description",
//                                                                 new BoundingBox(0, 0, 0, 0),
//                                                                 gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network1,
//                                                                                             "name",
//                                                                                             "units",
//                                                                                             DataType.Real,
//                                                                                             "description",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network2,
//                                                                                             "name",
//                                                                                             "units",
//                                                                                             DataType.Real,
//                                                                                             "description",
//                                                                                             AttributedType.Node);
//
//            final Edge edge = networkExtension.addEdge(network1, 1, 12);
//
//            networkExtension.getEdgeAttributes(edge, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when given AttributeDescriptions for different tables");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getEdgeAttributes throws an IllegalArgumentException
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributesException5()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("one",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "name",
//                                                                                             "units",
//                                                                                             DataType.Real,
//                                                                                             "description",
//                                                                                             AttributedType.Node);
//
//            final Edge edge = networkExtension.addEdge(network, 12, 222);
//
//            networkExtension.getEdgeAttributes(edge, attribute1);
//            fail("Expected GeoPackageNetworkExtension method getEdgeAttributes to throw an IllegalArgumentException when given an AttributeDescription for a Node");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribues correctly returns a list of attributes
//     */
//    @Test
//    public void testGetEdgeAttributes1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Edge);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network, 0, 100);
//            networkExtension.updateEdgeAttributes(edge,
//                                                  Arrays.asList((Object)10.20,
//                                                                (Object)"object"),
//                                                  attribute1,
//                                                  attribute2 );
//
//            final List<Object> attributes = networkExtension.getEdgeAttributes(edge, attribute1, attribute2);
//
//            assertTrue("GeoPackageNetworkExtension method getEdgeAttributes did not return the correct list of attributes",
//                       attributes.size()         == 2     &&
//                       (Double)attributes.get(0) == 10.20 &&
//                       ((String)attributes.get(1)).equals("object"));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribues throws an IllegalArgumentException
//     * when the given edge is not in the table
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetEdgeAttributes2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final Network network2 = networkExtension.addNetwork("Squirrel",
//                                                                 "UP",
//                                                                 "Adventure is out there",
//                                                                 new BoundingBox(0, 0, 0, 0),
//                                                                 gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Edge);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network2, 12, 232);
//            networkExtension.getEdgeAttributes(edge, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension did not throw an IllegalArgumentException when given an edge not in the table");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getEdgeAttribues returns a list of nulls when given an edge that
//     * does not have the given attributes
//     */
//    @Test
//    public void testGetEdgeAttributes3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Edge);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Edge);
//            final Edge edge = networkExtension.addEdge(network, 12, 53);
//
//            final List<Object> list =  networkExtension.getEdgeAttributes(edge, attribute1, attribute2);
//            assertTrue("GeoPackageNetworkExtension did not return a list of nulls when given an edge that does not have the given attributes",
//                       list.size() == 2 &&
//                       list.get(0) == null &&
//                       list.get(1) == null);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes throws an IllegalArgumentException when
//     * given node is not in the table
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeAttributesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Node);
//
//            networkExtension.getNodeAttributes(0, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension to throw an IllegalArgumentException when given a node not in the network");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes
//     */
//    @Test
//    public void testGetNodeAttributes1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Node);
//
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(12, Arrays.asList((Object)10.20,
//                                                                                                                        (Object)"word")));
//            networkExtension.addNodes(nodeAttributePairs , attribute1, attribute2);
//            final List<Object> list = networkExtension.getNodeAttributes(12, attribute1, attribute2);
//            assertTrue("GeoPackageNetworkExtensions method getNodeAttributes(int, AttributeDescriptions...) did not return the correct list of attributes",
//                       list.size() == 2 &&
//                       (Double)list.get(0) == 10.20 &&
//                       ((String)list.get(1)).equals("word"));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes throws an IllegalArgumentException when
//     * the nodeIdentifiers is empty
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeAttributesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final List<Integer> nodeIdentifiers = new ArrayList<>();
//
//            final AttributeDescription[] attributeDescriptions = null;
//
//            networkExtension.getNodeAttributes(nodeIdentifiers, attributeDescriptions);
//            fail("Expected GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) to throw an IllegalArgumentException");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes throws an IllegalArgumentException when
//     * the nodeIdentifiers is null
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeAttributesException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final List<Integer> nodeIdentifiers = null;
//
//            final AttributeDescription[] attributeDescriptions = null;
//
//            networkExtension.getNodeAttributes(nodeIdentifiers, attributeDescriptions);
//            fail("Expected GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) to throw an IllegalArgumentException");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes throws an IllegalArgumentException when
//     * given no AttributeDescriptions
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeAttributesException4()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final List<Integer> nodeIdentifiers = Arrays.asList(12, 23);
//
//            networkExtension.getNodeAttributes(nodeIdentifiers);
//            fail("Expected GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) to throw an IllegalArgumentException");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests getNodeAttributes throws an IllegalArgumentException when
//     * given no AttributeDescriptions
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void testGetNodeAttributesException5()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final List<Integer> nodeIdentifiers = Arrays.asList(12, 23);
//
//            final AttributeDescription[] attributes = null;
//
//            networkExtension.getNodeAttributes(nodeIdentifiers, attributes);
//            fail("Expected GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) to throw an IllegalArgumentException");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that getNodeAttributes correctly returns the attributes of each
//     * node in the given list
//     */
//    @Test
//    public void testGetNodeAttributes2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Node);
//
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(12, Arrays.asList((Object)10.20,
//                                                                                                                        (Object)"word")),
//                                                                                           new Pair<>(57, Arrays.asList((Object)22.33,
//                                                                                                                        (Object)"palabra")),
//                                                                                           new Pair<>(50, Arrays.asList((Object)123.45,
//                                                                                                                        (Object)"mot")));
//            networkExtension.addNodes(nodeAttributePairs, attribute1, attribute2);
//
//            final List<List<Object>> list = networkExtension.getNodeAttributes(Arrays.asList(12, 57,50), attribute1, attribute2);
//            assertTrue("GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) did not return the correct number of Lists of attributes",
//                       list.size() == 3);
//
//            final List<Object> node1 = list.get(0);
//            assertTrue("GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) did not return the correct attribues for the first node",
//                       node1.size() == 2 &&
//                       (Double)node1.get(0)== 10.20 &&
//                       ((String)node1.get(1)).equals("word"));
//
//            final List<Object> node2 = list.get(1);
//            assertTrue("GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) did not return the correct attribues for the second node",
//                       node2.size() == 2 &&
//                       (Double)node2.get(0)== 22.33 &&
//                       ((String)node2.get(1)).equals("palabra"));
//
//            final List<Object> node3 = list.get(2);
//            assertTrue("GeoPackageNetworkExtension method getNodeAttributes(List, AttributeDescriptions...) did not return the correct attribues for the third node",
//                       node3.size() == 2 &&
//                       (Double)node3.get(0)== 123.45 &&
//                       ((String)node3.get(1)).equals("mot"));
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that addNodeAttributes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodeAttributesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Node);
//            networkExtension.addNodeAttributes(0, null, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension method addNodeAttributes to throw an IllegalArgumentException when given a null Values collection");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests that addNodeAttributes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodeAttributesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("MINE",
//                                                                "Finding Nemo",
//                                                                "Just keep swimming",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                             "one",
//                                                                                             "test",
//                                                                                             DataType.Real,
//                                                                                             "none",
//                                                                                             AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "two",
//                                                                                             "still test",
//                                                                                             DataType.Text,
//                                                                                             "nada",
//                                                                                             AttributedType.Node);
//            final List<Object> values = new ArrayList<>();
//
//            networkExtension.addNodeAttributes(0, values, attribute1, attribute2);
//            fail("Expected GeoPackageNetworkExtension method addNodeAttributes to throw an IllegalArgumentException when the attributes list and values list are different sizes");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
////    /**
////     * Tests addNodeAttributes throws an IllegalArgumentException when
////     * the given node is not in the network
////     */
////    @Test(expected = IllegalArgumentException.class)
////    public void testAddNodeAttributesException3()
////    {
////        final File testFile = TestUtility.getRandomFile(5);
////        try(final GeoPackage gpkg = new GeoPackage(testFile))
////        {
////            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
////
////            final Network network = networkExtension.addNetwork("MINE",
////                                                                "Finding Nemo",
////                                                                "Just keep swimming",
////                                                                new BoundingBox(0, 0, 0, 0),
////                                                                gpkg.core().getSpatialReferenceSystem(-1));
////
////            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
////                                                                                             "one",
////                                                                                             "test",
////                                                                                             DataType.Real,
////                                                                                             "none",
////                                                                                             AttributedType.Node);
////
////            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
////                                                                                             "two",
////                                                                                             "still test",
////                                                                                             DataType.Text,
////                                                                                             "nada",
////                                                                                             AttributedType.Node);
////            final List<Object> values = Arrays.asList((Object)123.45, (Object)"test");
////
////
////            networkExtension.addNodeAttributes(0, values, attribute1, attribute2);
////        }
////        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
////        {
////            e.printStackTrace();
////        }
////        finally
////        {
////            TestUtility.deleteFile(testFile);
////        }
////    }
//
//    /**
//     * Tests that addNodeAttributes correctly adds
//     * the attributes to the given node
//     */
//    @Test
//    public void testAddNodeAttributes()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute1 = networkExtension.addAttributeDescription(network,
//                                                                                            "test",
//                                                                                            "units",
//                                                                                            DataType.Integer,
//                                                                                            "description",
//                                                                                            AttributedType.Node);
//
//            final AttributeDescription attribute2 = networkExtension.addAttributeDescription(network,
//                                                                                             "test_too",
//                                                                                             "units",
//                                                                                             DataType.Integer,
//                                                                                             "description",
//                                                                                             AttributedType.Node);
//
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(1, Arrays.asList((Object)12, (Object)21)));
//            networkExtension.addNodes(nodeAttributePairs, attribute1, attribute2);
//
//            networkExtension.addNodeAttributes(1, Arrays.asList((Object)555,
//                                                                (Object)444),
//                                               attribute1,
//                                               attribute2);
//
//            assertTrue("GeoPackageNetworkExtension method addNodeAttributes did not correctly update the node's attribute",
//                       (Integer)networkExtension.getNodeAttributes(1, attribute1, attribute2).get(0) == 555 &&
//                       (Integer)networkExtension.getNodeAttributes(1, attribute1, attribute2).get(1) == 444);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//    /**
//     * Tests addNodes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodesException1()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = new ArrayList<>();
//            final AttributeDescription[] attributeDescriptions = null;
//
//            networkExtension.addNodes(nodeAttributePairs , attributeDescriptions);
//            fail("Expected GeoPackageNetworkExtension method addNodes to throw an IllegalArgumentException when given a null AttributeDescriptions");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNodes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodesException2()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("name",
//                                                                "id",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.addAttributeDescription(network,
//                                                                                            "one",
//                                                                                            "test",
//                                                                                            DataType.Real,
//                                                                                            "none",
//                                                                                            AttributedType.Node);
//
//            networkExtension.addNodes(null , attribute);
//            fail("Expected GeoPackageNetworkExtension method addNodes to throw an IllegalArgumentException when given a null List of node attribute pairs");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNodes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodesException3()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "identifier",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.addAttributeDescription(network,
//                                                                                            "attribute",
//                                                                                            "units",
//                                                                                            DataType.Blob,
//                                                                                            "description",
//                                                                                            AttributedType.Node);
//
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(1, null));
//
//            networkExtension.addNodes(nodeAttributePairs, attribute);
//            fail("Expected GeoPackageNetworkExtension method addNodes to throw an IllegalArgumentException when the given nodes have null for their values");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNodes throws an IllegalArgumentException
//     */
//    @Test (expected = IllegalArgumentException.class)
//    public void testAddNodesException4()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "identifier",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.addAttributeDescription(network,
//                                                                                            "attribute",
//                                                                                            "units",
//                                                                                            DataType.Blob,
//                                                                                            "description",
//                                                                                            AttributedType.Node);
//            final List<Object> values = Arrays.asList((Object)12, (Object)"test");
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(1, values));
//
//            networkExtension.addNodes(nodeAttributePairs, attribute);
//            fail("Expected GeoPackageNetworkExtension method addNodes to throw an IllegalArgumentException when the given nodes have null for their values");
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /**
//     * Tests addNodes correctly addsNodes to the
//     * network of the given attributes
//     */
//    @Test
//    public void testAddNodes()
//    {
//        final File testFile = TestUtility.getRandomFile(5);
//        try(final GeoPackage gpkg = new GeoPackage(testFile))
//        {
//            final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
//
//            final Network network = networkExtension.addNetwork("my_table",
//                                                                "identifier",
//                                                                "description",
//                                                                new BoundingBox(0, 0, 0, 0),
//                                                                gpkg.core().getSpatialReferenceSystem(-1));
//
//            final AttributeDescription attribute = networkExtension.addAttributeDescription(network,
//                                                                                            "attribute",
//                                                                                            "units",
//                                                                                            DataType.Integer,
//                                                                                            "description",
//                                                                                            AttributedType.Node);
//
//            final Iterable<Pair<Integer, List<Object>>> nodeAttributePairs = Arrays.asList(new Pair<>(1, Arrays.asList((Object)12)),
//                                                                                           new Pair<>(2, Arrays.asList((Object)14)),
//                                                                                           new Pair<>(3, Arrays.asList((Object)16)));
//
//            networkExtension.addNodes(nodeAttributePairs, attribute);
//
//            assertTrue("GeoPackageNetworkExtension method addNodes did not correctly add nodes to the network",
//                       (Integer)networkExtension.getNodeAttributes(1, attribute).get(0) == 12);
//            assertTrue("GeoPackageNetworkExtension method addNodes did not correctly add nodes to the network",
//                       (Integer)networkExtension.getNodeAttributes(2, attribute).get(0) == 14);
//            assertTrue("GeoPackageNetworkExtension method addNodes did not correctly add nodes to the network",
//                       (Integer)networkExtension.getNodeAttributes(3, attribute).get(0) == 16);
//        }
//        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            TestUtility.deleteFile(testFile);
//        }
//    }
//
//    /*
//     * Private method to print an Edge
//     */
//    private static String printEdge(final Edge edge)
//    {
//        return String.format("(%s, %s)", edge.getFrom(), edge.getTo());
//    }
//
//    /*
//     * Private method to compare edges
//     */
//    private static boolean edgesEqual(final Edge e1, final Edge e2)
//    {
//        return e1.getFrom() == e2.getFrom() && e1.getTo() == e2.getTo();
//    }
//}
