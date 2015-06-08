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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import utility.TestUtility;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.extensions.Scope;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.verification.ConformanceException;

/**
 * Unit tests for GeoPackageNetworkExtensions
 *
 * @author Mary Carome
 *
 */
@SuppressWarnings({"static-method"})
public class GeoPackageNetworkExtensionTest
{
	/**
	 * Tests getExtensionName
	 */
	@Test
    public void testGetExtensionName()
    {
        final File testFile = TestUtility.getRandomFile(10);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final String networkName = "rgi_network";
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    assertTrue(String.format("GeopackageNetworkExtensionName method getExtensionName returns %s instead of ", networkExtension.getExtensionName(), networkName),
       	    		   networkExtension.getExtensionName().equals(networkName));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
    }

	/**
	 * Tests getDefinition
	 */
	@Test
	public void testGetDefinition()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final String definition = "definition";
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    assertTrue(String.format("GeopackageNetworkExtensionName method getDefintion returns %s instead of %s", networkExtension.getDefinition(), definition),
       	    		   networkExtension.getDefinition().equals(definition));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getDefinition
	 */
	@Test
	public void testGetScope()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    assertTrue(String.format("GeopackageNetworkExtensionName method getScope returns %s instead of %s", networkExtension.getScope(), Scope.ReadWrite),
       	    		   networkExtension.getScope().equals(Scope.ReadWrite));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests that getNodeAttributesTableName name throws an IllegalArgumentException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentGetNodeAttributesTableName1()
	{
		final Network network = null;
        GeoPackageNetworkExtension.getNodeAttributesTableName(network);
        fail("Expected GeoPackageNetworkExtension method getNodeAttributesTableName(Network) to throw an exception when passed a null Network");
	}

	/**
	 * Tests that getNodeAttributesTableName name throws an IllegalArgumentException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentGetNodeAttributesTableName2()
	{
		final String network = null;
        GeoPackageNetworkExtension.getNodeAttributesTableName(network);
        fail("Expected GeoPackageNetworkExtension method getNodeAttributesTableName(String) to throw an exception when passed a null String");
	}

	/**
	 * Test that getNodeAttributesTableName correctly return the table name
	 * when passed a network
	 */
	@Test
	public void getNodeAttributesTableName1()
	{
 	    final String tableName = "my_table_node_attributes";
        final Network network = new Network("my_table", "test", "test", "test", new BoundingBox(0,0,0,0), 0);

   	    assertTrue(String.format("GeoPackageNetworkExtension method getNodeAttributesTableName returned %s, but %s was expected",
   	    		                  GeoPackageNetworkExtension.getNodeAttributesTableName(network),
   	    		                  tableName),
   	    GeoPackageNetworkExtension.getNodeAttributesTableName(network).equals(tableName));
	}

	/**
	 * Test that getNodeAttributesTableName correctly return the table name
	 * when passed a non-null String
	 */
	@Test
	public void getNodeAttributesTableName2()
	{
 	    final String tableName = "my_table_node_attributes";

   	    assertTrue(String.format("GeoPackageNetworkExtension method getNodeAttributesTableName returned %s, but %s was expected",
   	    		                  GeoPackageNetworkExtension.getNodeAttributesTableName("my_table"),
   	    		                  tableName),
   	    GeoPackageNetworkExtension.getNodeAttributesTableName("my_table").equals(tableName));
	}

	/**
	 * Tests getNetwork throws an IllegalArgumentException
	 * when passed a null networkTableName
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testGetNetworkException()
	{
        final File testFile = TestUtility.getRandomFile(3);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final String networkName = null;
       	    networkExtension.getNetwork(networkName);
       	    fail("Expected GeoPackageNetworkExtension method getNetwork to throw an illegal argument exception when given a null networkTableName");

        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getNetwork returns null when no Network
	 * has been added to the GeoPackage
	 */
	@Test
	public void testGetNetworkNull()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final String networkName = "my_network";
       	    assertNull("Expected GeoPackageNetworkExtension method getNetwork() to return null when no network has been added",
       	    		    networkExtension.getNetwork(networkName));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getNetwork returns the
	 * correct network
	 */
	@Test
	public void testGetNetwork()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final String name = "my_table";
       	    final String identifier = "test";
       	    final String description = "empty";
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);
       	    final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);

       	    networkExtension.addNetwork(name, identifier, description, box, srs);
       	    assertTrue("GeopackageNetworkExtension method getNetwork did not return the correct Network",
       	    		   networkExtension.getNetwork(name).equals(name,  Network.NetworkContentType, identifier, description, box, srs.getIdentifier()));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException1()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);

       	    networkExtension.addNetwork(null, "test", "empty", box, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a null tableName");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException2()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);

       	    networkExtension.addNetwork("", "test", "empty", box, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given an empty tableaName");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException3()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);

       	    networkExtension.addNetwork("123abc", "test", "empty", box, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given an invalid table");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException4()
	{
        final File testFile = TestUtility.getRandomFile(7);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);

       	    networkExtension.addNetwork("gpkg_", "test", "empty", box, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a table Name that starts with 'gpkg_'");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException5()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

       	    networkExtension.addNetwork("my_table", "test", "empty", null, gpkg.core().getSpatialReferenceSystem(-1));
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when given a null BoundingBox");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork throws an IllegalArgumentException
     */
	@Test(expected = IllegalArgumentException.class)
	public void testAddNetworkException6()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final String name = "my_table";
       	    final String identifier = "test";
       	    final String description = "empty";
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);
       	    final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);

       	    networkExtension.addNetwork(name, identifier, description, box, srs);
       	    networkExtension.addNetwork(name, identifier, description, box, srs);
            fail("Expected GeoPackageNetworkExtension method addNetwork to throw an IllegalArgumentException when adding two networks with the same name");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

    /**
     * Tests addNetwork correctly adds networks
     * with different names
     */
	@Test
	public void testAddNetwork()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
       	    final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
       	    final BoundingBox box = new BoundingBox(12, 23, 20, 55);
       	    final SpatialReferenceSystem srs = gpkg.core().getSpatialReferenceSystem(-1);

       	    networkExtension.addNetwork("my_table", "test", "empty", box, srs);
       	    assertTrue("GeoPackageNetworkExtension method addNetwork did not add/return the correct Network",
       	    		   networkExtension.addNetwork("my_second_table", "test, too", "empty", box, srs).equals("my_second_table", Network.NetworkContentType, "test, too", "empty", box, srs.getIdentifier()));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getEdge throws an IllegalArgumentException
	 * when the Network is null
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testGetEdgeException()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        	networkExtension.getEdge(null, 0, 12);
        	fail("Expected GeoPackageNetworkExtension method getEdge(Network, int, int) to throw an IllegalArgumentException when given a null Network");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getEdge when the edge is not
	 * in the given Network
	 */
	@Test
	public void testGetEdge1()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        	final Network network = networkExtension.addNetwork("my_table", "test", "empty", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
        	assertNull("Expected GeoPackageNetworkExtension method getEdge(Network, int, int) to return null", networkExtension.getEdge(network, 0, 12));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getEdge when the edge is
	 * in the given Network
	 */
	@Test
	public void testGetEdge2()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        	final Network network = networkExtension.addNetwork("my_table", "test", "empty", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
        	final int from = 12;
        	final int to = 23;
        	networkExtension.addEdge(network,from , to);
            networkExtension.addEdge(network, 23, 12);
            networkExtension.addEdge(network, 0, 2);

        	final Edge edge = networkExtension.getEdge(network, from, to);
        	assertTrue(String.format("Expected GeoPackageNetworkExtension method getEdge to return %s, but (%s, %s) was returned", printEdge(edge), from, to),
        			   edge.getFrom() == from && edge.getTo() == to);
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests that getEntries throws an
	 * Exception when passed a null network
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testGetEntriesException()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            networkExtension.getEntries(null, 0);
            fail("Expected GeoPackageNetworkExtension getEntries(Network, int) to throw an IllegalArgumentException when passed a null network");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getEntries returns an empty list
	 * when given a node not in the network
	 */
	@Test
	public void testGetEntries1()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
            final Network network = networkExtension.addNetwork("my_table", "blah", "foo", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
            networkExtension.addEdge(network, 12, 10);
            networkExtension.addEdge(network, 23, 15);
            networkExtension.addEdge(network, 33, 12);

            assertTrue("Expected GeoPackageNetworkExtension method getEntries(Network, int) to return an empty list.",
            		   networkExtension.getEntries(network, 13).size() == 0);
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getEntries
	 */
	@Test
	public void testGetEntries2()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
            final Network network = networkExtension.addNetwork("my_table", "blah", "foo", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
            networkExtension.addEdge(network, 12, 10);
            networkExtension.addEdge(network, 23, 10);
            networkExtension.addEdge(network, 33, 10);
            networkExtension.addEdge(network, 12, 15);

            final List<Integer> list = networkExtension.getEntries(network, 10);
            assertTrue("Expected GeoPackageNetworkExtension method getEntries(Network, int) to return an nonempty list.",
            		   list.size() == 3);
            assertTrue("GeoPackageNetworkExtension method getEntries(Network, int) did not return the correct list of node identifiers",
            		   list.contains(12) && list.contains(23) && list.contains(33));
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getExits throws an Exception when
	 * passed a null Network
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetExitsException()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);

            networkExtension.getExits(null, 0);
            fail("Expected GeoPackageNetworkExtension getExits(Network, int) to throw an IllegalArgumentException when passed a null Network");
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getExits return an empty list when given
	 * a node not in the network
	 */
	@Test
	public void testGetExits1()
	{
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
            final Network network = networkExtension.addNetwork("my_table", "blah", "foo", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
            networkExtension.addEdge(network, 12, 10);
            networkExtension.addEdge(network, 23, 15);
            networkExtension.addEdge(network, 33, 12);

            assertTrue("Expected GeoPackageNetworkExtension method getEntries(Network, int) to return an empty list.",
            		   networkExtension.getExits(network, 13).size() == 0);
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
	}

	/**
	 * Tests getExits
	 */
    @Test
    public void testGetExits2()
    {
        final File testFile = TestUtility.getRandomFile(5);
        try(GeoPackage gpkg = new GeoPackage(testFile))
        {
        	final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
            final Network network = networkExtension.addNetwork("my_table", "blah", "foo", new BoundingBox(0,0,0,0), gpkg.core().getSpatialReferenceSystem(-1));
            networkExtension.addEdge(network, 12, 10);
            networkExtension.addEdge(network, 12, 15);
            networkExtension.addEdge(network, 12, 44);
            networkExtension.addEdge(network, 54, 354);

            final List<Integer> list = networkExtension.getExits(network, 12);
            assertTrue("Expected GeoPackageNetworkExtension method getExits(Network, int) to return an empty list.",
            		   list.size() == 3);
            assertTrue("GeoPackageNetworkExtension method getEntries(Network, int) did not return the correct list of node identifiers",
         		       list.contains(12) && list.contains(23) && list.contains(33)););
        }
        catch (ClassNotFoundException | SQLException | ConformanceException | IOException | BadImplementationException e)
		{
			e.printStackTrace();
		}
        finally
        {
        	TestUtility.deleteFile(testFile);
        }
    }

	/**
	 * Private method to print edges
	 * @param edge
	 * @return String representation of Edge in (from, to) format
	 */
	private static String printEdge(final Edge edge)
	{
		return String.format("(%s, %s)", edge.getFrom(), edge.getTo());
	}
}
