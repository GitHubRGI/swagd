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
package common.util.functional.jdbc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import utility.TestUtility;

import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.common.util.jdbc.JdbcUtility;
import com.rgi.android.common.util.jdbc.ResultSetFunction;
import com.rgi.android.common.util.jdbc.ResultSetPredicate;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.verification.ConformanceException;


/**
 *
 * @author Mary Carome
 *
 */
@SuppressWarnings({"javadoc", "static-method"})
@RunWith(RobolectricTestRunner.class)
public class JdbcUtilityTest
{
    /**
     * Tests that anyMatch correctly return True or False depending on the
     * ResultSetPredicate it is given
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test
    public void anyMatchTest() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            try
            {
                final String name1 = "Mary";
                final String name2 = "Joe";
                final String name3 = "Bob";
                final String name4 = "Marley";
                final String name5 = "Bo";
                final String description = "foo";

                gpkg.core().addSpatialReferenceSystem(name1, "RGI", 10, "blah", description);
                gpkg.core().addSpatialReferenceSystem(name2, "AGC", 20, "test", description);
                gpkg.core().addSpatialReferenceSystem(name3, "WGS", 30, "foo", description);
                gpkg.core().addSpatialReferenceSystem(name4, "ESPG", 40, "still", description);

                final Connection con = TestUtility.getConnection(testFile);

                try
                {
                    final String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
                    final Statement stmt = con.createStatement();
                    try
                    {
                        final ResultSet rs = stmt.executeQuery(query);
                        try
                        {
                            final boolean results1 = JdbcUtility.anyMatch(rs,
                                                                          new ResultSetPredicate()
                                                                          {
                                                                              @Override
                                                                              public boolean apply(final ResultSet resultSet) throws SQLException
                                                                              {
                                                                                  return resultSet.getString("srs_name").equals(name4);
                                                                              }
                                                                          });

                            final boolean results2 = JdbcUtility.anyMatch(rs,
                                                                          new ResultSetPredicate()
                                                                          {
                                                                              @Override
                                                                              public boolean apply(final ResultSet resultSet) throws SQLException
                                                                              {
                                                                                  return resultSet.getString("srs_name").equals(name5);
                                                                              }
                                                                          });

                            assertTrue("Expected JdbcUtility method anyMatch(ResultSet, ResultSetPredicate) to return true.", results1);
                            assertFalse("Expected JdbcUtility method anyMatch(ResultSet, ResultSetPredicate) to return false.", results2);
                        }
                        finally
                        {
                            rs.close();
                        }
                    }
                    finally
                    {
                        stmt.close();
                    }
                }
                finally
                {
                    con.close();
                }
            }
            finally
            {
                gpkg.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests that anyMatch throws an IllegalArgumentException when given null
     * instead of a valid ResultSetPredicate
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionAnyMatch() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            gpkg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        JdbcUtility.anyMatch(rs, null);
                        fail("Expected JdbcUtility method anyMatch(ResultSet, ResultSetPredicate) to throw an IllegalArgumentException when passed a null ResultSetPredicate.");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests anyMatch throws an IllegalArgumentException when the resultSet is
     * closed
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionAnyMatchClosedSet() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            gpkg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        rs.close();
                        JdbcUtility.anyMatch(rs, null);
                        fail("Expected JdbcUtility method anyMatch(ResultSet, ResultSetPredicate) to throw an IllegalArgumentException when given a closed ResultSet.");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests anyMatch throws an IllegalArgumnetException when given null for the
     * result set
     *
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionAnyMatchNullSet() throws SQLException
    {
        JdbcUtility.anyMatch(null,
                             new ResultSetPredicate()
                             {
                                 @Override
                                 public boolean apply(final ResultSet resultSet) throws SQLException
                                 {
                                     return false;
                                 }
                             });

        fail("Expected JdbcUtility method anyMatch(ResultSet, ResultSetPredicate) to throw an IllegalArgumentException when given a null ResultSet");
    }

    /**
     * Tests that map correctly returns a List<T> containing the correct
     * elements based on the ResultSetFunction it is given
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws SQLException
     */
    @Test
    public void mapTest() throws IOException, ClassNotFoundException, ConformanceException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            try
            {
                final String name1 = "Mary";
                final String name2 = "Joe";
                final String name3 = "Bob";
                final String name4 = "Marley";
                final String description = "foo";

                gpkg.core().addSpatialReferenceSystem(name1, "RGI", 10, "blah", description);
                gpkg.core().addSpatialReferenceSystem(name2, "AGC", 20, "test", description);
                gpkg.core().addSpatialReferenceSystem(name3, "WGS", 30, "foo", description);
                gpkg.core().addSpatialReferenceSystem(name4, "ESPG", 40, "still", description);

                final Connection con = TestUtility.getConnection(testFile);

                try
                {
                    final String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
                    final Statement stmt = con.createStatement();
                    try
                    {
                        final ResultSet rs = stmt.executeQuery(query);
                        try
                        {
                            final List<String> results = JdbcUtility.map(rs,
                                                                         new ResultSetFunction<String>()
                                                                         {
                                                                             @Override
                                                                             public String apply(final ResultSet resultSet) throws SQLException
                                                                             {
                                                                                 return resultSet.getString("srs_name");
                                                                             }
                                                                         });

                            assertTrue("Expected JdbcUtility method map(ResultSet, ResultSetFunction<T>) to return a List of size 4.", results.size() == 4);
                            final String error = String.format("Expected JDBC utlity to method map(ResultSet, ResultSetFunction<T>) to return a list containing: %s, %s, %s, and %s", name1, name2, name3, name4);
                            assertTrue(error, results.get(0).equals(name1) &&
                                              results.get(1).equals(name2) &&
                                              results.get(2).equals(name3) &&
                                              results.get(3).equals(name4));
                        }
                        finally
                        {
                            rs.close();
                        }
                    }
                    finally
                    {
                        stmt.close();
                    }
                }
                finally
                {
                    con.close();
                }
            }
            finally
            {
                gpkg.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests that map throws an IllegalArgumentException when given null instead
     * of a valid ResultSetFunction<T>
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapFunction() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);


        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            gpkg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        JdbcUtility.map(rs, null);
                        fail("Expected JdbcUtility method map(ResultSet, ResultSetFunction<T>) to throw an IllegalArgumentException when given a null ResultSetFUnction<T>");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests that map throws an IllegalArgumentException when given null instead
     * of a valid ResultSet
     *
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapRS() throws SQLException
    {
        JdbcUtility.map(null,
                        new ResultSetFunction<String>()
                        {
                            @Override
                            public String apply(final ResultSet resultSet) throws SQLException
                            {
                                return "test";
                            }
                        });

            fail("Expected JdbcUtility method map(ResultSet, ResultSetFunction<T>) to throw an IllegalArgumentException when given a null ResultSet.");
    }

    /**
     * Tests that map throws an IllegalArgumentException when give a ResultSet
     * that is closed
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapRSClosed() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final GeoPackage gpkg = new GeoPackage(testFile);
        try
        {
            gpkg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        rs.close();
                        JdbcUtility.map(rs, null);
                        fail("Expected JdbcUtility method map(ResultSet, ResultSetFunction<T>) to throw an IllegalArgumentException when given a closed ResultSet.");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests mapFilter correctly returns a list
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test
    public void testMapFilter() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);
        final GeoPackage gpkg = new GeoPackage(testFile);

        try
        {
            try
            {
                final String name1 = "Mary";
                final String name2 = "Joe";
                final String name3 = "Bob";
                final String name4 = "Marley";
                final String description = "foo";

                gpkg.core().addSpatialReferenceSystem(name1, "RGI", 10, "blah", description);
                gpkg.core().addSpatialReferenceSystem(name2, "AGC", 20, "test", description);
                gpkg.core().addSpatialReferenceSystem(name3, "WGS", 30, "foo", description);
                gpkg.core().addSpatialReferenceSystem(name4, "ESPG", 40, "still", description);

                final Connection con = TestUtility.getConnection(testFile);

                try
                {
                    final String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
                    final Statement stmt = con.createStatement();
                    try
                    {
                        final ResultSet rs = stmt.executeQuery(query);
                        try
                        {
                            final List<String> results = JdbcUtility.mapFilter(rs,
                                                                               new ResultSetFunction<String>()
                                                                               {
                                                                                   @Override
                                                                                   public String apply(final ResultSet resultSet) throws SQLException
                                                                                   {
                                                                                       return resultSet.getString("srs_name");
                                                                                   }
                                                                               },
                                                                               new Predicate<String>()
                                                                               {
                                                                                   @Override
                                                                                   public boolean apply(final String t)
                                                                                   {
                                                                                       return t.equals(name1) || t.equals(name2);
                                                                                   }
                                                                               });

                            assertTrue("Expected JdbcUtility method mapFilter(ResultSet, ResultSetFunction<T>, ResultSetPredicate<T>) to return a list of size 2",
                                       results.size() == 2);
                            assertTrue(String.format("Expected JdbcUtility method mapFilter(ResultSet, ResultSetFunction<T>, ResultSetPredicate<T>) to return a list containing: %s and %s",name1, name2),
                                       results.get(0).equals(name1) && results.get(1).equals(name2));
                        }
                        finally
                        {
                            rs.close();
                        }
                    }
                    finally
                    {
                        stmt.close();
                    }
                }
                finally
                {
                    con.close();
                }
            }
            finally
            {
                gpkg.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests mapFilter throws an exception when given null instead of a valid
     * ResultSet
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapFilterRS() throws SQLException
    {
        JdbcUtility.mapFilter(null,
                              new ResultSetFunction<String>()
                              {
                                  @Override
                                  public String apply(final ResultSet resultSet) throws SQLException
                                  {
                                      return "test";
                                  }
                              },
                              new Predicate<String>()
                              {
                                  @Override
                                  public boolean apply(final String tableName)
                                  {
                                      return true;
                                  }
                              });

        fail("Expected JdbcUtility method mapFilter (ResultSet, ResultSetFunction<T>, ResultSetPredicate<T>) to throw an IllegalArgumentException when given a null ResultSet.");
    }

    /**
     * Tests that mapFilter throws an IllegalArgumentException when given null
     * instead of a valid ResultSetFunction<T>
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapFilterFunction() throws IOException, ClassNotFoundException, ConformanceException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final GeoPackage gkpg = new GeoPackage(testFile);
        try
        {
            gkpg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        JdbcUtility.mapFilter(rs,
                                              null,
                                              new Predicate<String>()
                                              {
                                                  @Override
                                                  public boolean apply(final String tableName)
                                                  {
                                                      return true;
                                                  }
                                              });

                        fail("Expected JdbcUtility method mapFilter (ResultSet, ResultSetFunction<T>, ResultSetPredicate<T>) to throw an IllegalArgumentException when given a null ResultSetFunction.");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            } finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }

    /**
     * Tests mapFilter throws an IllegalArgumentException when given null
     * instead of a valid Predicate<T>
     *
     * @throws ClassNotFoundException
     * @throws ConformanceException
     * @throws IOException
     * @throws SQLException
     */
    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionMapFilterPred() throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        final File testFile = TestUtility.getRandomFile(10);

        final GeoPackage gkpg = new GeoPackage(testFile);
        try
        {
            gkpg.close();

            final Connection con = TestUtility.getConnection(testFile);

            try
            {
                final String query = "Select * from gpkg_contents;";
                final Statement stmt = con.createStatement();
                try
                {
                    final ResultSet rs = stmt.executeQuery(query);
                    try
                    {
                        JdbcUtility.mapFilter(rs,
                                              new ResultSetFunction<String>()
                                              {
                                                  @Override
                                                  public String apply(final ResultSet resultSet) throws SQLException
                                                  {
                                                      return "test";
                                                  }
                                              },
                                              null);

                        fail("Expected JdbcUtility method mapFilter (ResultSet, ResultSetFunction<T>, ResultSetPredicate<T>) to throw an IllegalArgumentException when given a null ResultSetPredicate.");
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                finally
                {
                    stmt.close();
                }
            }
            finally
            {
                con.close();
            }
        }
        finally
        {
            TestUtility.deleteFile(testFile);
        }
    }
}
