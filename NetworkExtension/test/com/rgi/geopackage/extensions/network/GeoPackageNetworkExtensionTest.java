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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import utility.TestUtility;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.GeoPackage;
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
	public void getNodeAttributesTableName2()
	{
 	    final String tableName = "my_table_node_attributes";

   	    assertTrue(String.format("GeoPackageNetworkExtension method getNodeAttributesTableName returned %s, but %s was expected",
   	    		                  GeoPackageNetworkExtension.getNodeAttributesTableName("my_table"),
   	    		                  tableName),
   	    		   GeoPackageNetworkExtension.getNodeAttributesTableName("my_table").equals(tableName));
	}
}
