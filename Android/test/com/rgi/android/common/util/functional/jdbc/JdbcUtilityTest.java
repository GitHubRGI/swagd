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
package com.rgi.android.common.util.functional.jdbc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

import javax.swing.filechooser.FileSystemView;

import org.junit.Test;

import com.rgi.android.common.util.functional.Predicate;
import com.rgi.android.geopackage.GeoPackage;
import com.rgi.android.geopackage.core.GeoPackageCore;
import com.rgi.android.geopackage.core.SpatialReferenceSystem;
import com.rgi.android.geopackage.tiles.TilesVerifier;
import com.rgi.android.geopackage.utility.DatabaseUtility;
import com.rgi.android.geopackage.verification.ConformanceException;

/**
 * 
 * @author Mary Carome
 *
 */
public class JdbcUtilityTest {
	private final Random randomGenerator = new Random();

	/**
	 * Tests that anyMatch correctly return True or False
	 * depending on the ResultSetPredicate it is given
	 * 
	 * @throws ClassNotFoundException
	 * @throws ConformanceException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test 
	public void anyMatchTest() throws ClassNotFoundException, ConformanceException, IOException, SQLException{
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			String name1 = "Mary";
			String name2 = "Joe";
			String name3 = "Bob";
			final String name4 = "Marley";
			final String name5 = "Bo";
			String organization1 = "RGI";
			String organization2 = "AGC";
			String organization3 = "WGS";
			String organization4 = "EPSG";
			int organizationSrsId1 = 10;
			int organizationSrsId2 = 20;
			int organizationSrsId3 = 30;
			int organizationSrsId4 = 40;
			String definition1 = "blah";
			String definition2 = "test";
			String definition3 = "bar";
			String definition4 = "still testing";
			String description = "foo";
			
			gpkg.core().addSpatialReferenceSystem(name1, organization1, organizationSrsId1, definition1, description);
			gpkg.core().addSpatialReferenceSystem(name2, organization2, organizationSrsId2, definition2, description);
			gpkg.core().addSpatialReferenceSystem(name3, organization3, organizationSrsId3, definition3, description);
			gpkg.core().addSpatialReferenceSystem(name4, organization4, organizationSrsId4, definition4, description);
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						boolean results1 = JdbcUtility.anyMatch(rs, new ResultSetPredicate(){

							@Override
							public boolean apply(ResultSet resultSet)
									throws SQLException {
								return resultSet.getString("srs_name").equals(name4);
							}});
						boolean results2 = JdbcUtility.anyMatch(rs, new ResultSetPredicate(){

							@Override
							public boolean apply(ResultSet resultSet)
									throws SQLException {
								return resultSet.getString("srs_name").equals(name5);
							}});
						assertTrue(results1);
						assertFalse(results2);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
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
	public void illegalArgumentExceptionAnyMatch()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						JdbcUtility.anyMatch(rs, null);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
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
	public void illegalArgumentExceptionAnyMatchClosedSet()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						rs.close();
						JdbcUtility.anyMatch(rs, null);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
	}

	/**
	 * Tests anyMatch throws an IllegalArgumnetException when given null for the
	 * result set
	 * 
	 * @throws SQLException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentExceptionAnyMatchNullSet() throws SQLException {
		try {
			JdbcUtility.anyMatch(null, new ResultSetPredicate() {
				@Override
				public boolean apply(ResultSet resultSet) throws SQLException {
					return false;
				}
			});
		} finally {
		}
	}

	/**
	 * Tests that map correctly returns a List<T> containing
	 * the correct elements based on the ResultSetFunction it is given
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ConformanceException
	 * @throws SQLException
	 */
	@Test 
	public void mapTest() throws IOException, ClassNotFoundException, ConformanceException, SQLException{
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			String name1 = "Mary";
			String name2 = "Joe";
			String name3 = "Bob";
			String name4 = "Marley";
			String organization1 = "RGI";
			String organization2 = "AGC";
			String organization3 = "WGS";
			String organization4 = "EPSG";
			int organizationSrsId1 = 10;
			int organizationSrsId2 = 20;
			int organizationSrsId3 = 30;
			int organizationSrsId4 = 40;
			String definition1 = "blah";
			String definition2 = "test";
			String definition3 = "bar";
			String definition4 = "still testing";
			String description = "foo";
			
			gpkg.core().addSpatialReferenceSystem(name1, organization1, organizationSrsId1, definition1, description);
			gpkg.core().addSpatialReferenceSystem(name2, organization2, organizationSrsId2, definition2, description);
			gpkg.core().addSpatialReferenceSystem(name3, organization3, organizationSrsId3, definition3, description);
			gpkg.core().addSpatialReferenceSystem(name4, organization4, organizationSrsId4, definition4, description);
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						List<String> results = JdbcUtility.map(rs, new ResultSetFunction<String>() {

							@Override
							public String apply(ResultSet resultSet)
									throws SQLException {
								return resultSet.getString("srs_name");
							}
						});
						assertTrue(results.size() == 4);
						assertTrue(results.get(0).equals(name1));
						assertTrue(results.get(1).equals(name2));
						assertTrue(results.get(2).equals(name3));
						assertTrue(results.get(3).equals(name4));
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
	}
	/**
	 * Tests that map throws an IllegalArgumentException when given null instead
	 * of a valid ResultSet
	 * 
	 * @throws ClassNotFoundException
	 * @throws ConformanceException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentExceptionMapFunction()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						JdbcUtility.map(rs, null);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
	}

	/**
	 * Tests that map throws an IllegalArgumentException when given null instead
	 * of a valid ResultSet
	 * 
	 * @throws SQLException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void illegalArgumentExceptionMapRS() throws SQLException {
		try {
			JdbcUtility.map(null, new ResultSetFunction<String>() {

				@Override
				public String apply(ResultSet resultSet) throws SQLException {
					// TODO Auto-generated method stub
					return "test";
				}
			});
		} finally {
		}
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
	public void illegalArgumentExceptionMapRSClosed()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						rs.close();
						JdbcUtility.map(rs, null);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
	}
	
	/**
	 * Tests mapFilter correctly returns a list 
	 * @throws ClassNotFoundException
	 * @throws ConformanceException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void testMapFilter() throws ClassNotFoundException, ConformanceException, IOException, SQLException{
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			final String name1 = "Mary";
			final String name2 = "Joe";
			String name3 = "Bob";
			String name4 = "Marley";
			String organization1 = "RGI";
			String organization2 = "AGC";
			String organization3 = "WGS";
			String organization4 = "EPSG";
			int organizationSrsId1 = 10;
			int organizationSrsId2 = 20;
			int organizationSrsId3 = 30;
			int organizationSrsId4 = 40;
			String definition1 = "blah";
			String definition2 = "test";
			String definition3 = "bar";
			String definition4 = "still testing";
			String description = "foo";
			
			gpkg.core().addSpatialReferenceSystem(name1, organization1, organizationSrsId1, definition1, description);
			gpkg.core().addSpatialReferenceSystem(name2, organization2, organizationSrsId2, definition2, description);
			gpkg.core().addSpatialReferenceSystem(name3, organization3, organizationSrsId3, definition3, description);
			gpkg.core().addSpatialReferenceSystem(name4, organization4, organizationSrsId4, definition4, description);
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = String.format("Select srs_name FROM %s WHERE description = '%s'", GeoPackageCore.SpatialRefSysTableName, description);
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						List<String> results = JdbcUtility.mapFilter(rs, new ResultSetFunction<String>() {

							@Override
							public String apply(ResultSet resultSet)
									throws SQLException {
								return resultSet.getString("srs_name");
							}
						}, new Predicate<String>(){

							@Override
							public boolean apply(String t) {
								return t.equals(name1) || t.equals(name2);
							}});
						assertTrue(results.size() == 2);
						assertTrue(results.get(0).equals(name1));
						assertTrue(results.get(1).equals(name2));
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
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
	public void illegalArgumentExceptionMapFilterRS()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gpkg = new GeoPackage(testFile);
		try {
			gpkg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						JdbcUtility.mapFilter(null,
								new ResultSetFunction<String>() {

									@Override
									public String apply(ResultSet resultSet)
											throws SQLException {
										return "test";
									}
								}, new Predicate<String>() {
									@Override
									public boolean apply(final String tableName) {
										return true;
									}
								});
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
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
	public void illegalArgumentExceptionMapFilterFunction() throws IOException,
			ClassNotFoundException, ConformanceException, SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gkpg = new GeoPackage(testFile);
		try {
			gkpg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						JdbcUtility.mapFilter(rs, null,
								new Predicate<String>() {
									@Override
									public boolean apply(final String tableName) {
										return true;
									}
								});
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
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
	public void illegalArgumentExceptionMapFilterPred()
			throws ClassNotFoundException, ConformanceException, IOException,
			SQLException {
		File testFile = this.getRandomFile(10);
		GeoPackage gkpg = new GeoPackage(testFile);
		try {
			gkpg.close();
			Connection con = this.getConnection(testFile.getAbsolutePath());
			try {
				String query = "Select * from gpkg_contents;";
				Statement stmt = con.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						JdbcUtility.mapFilter(rs,
								new ResultSetFunction<String>() {

									@Override
									public String apply(ResultSet resultSet)
											throws SQLException {
										// TODO Auto-generated method stub
										return "test";
									}
								}, null);
					} finally {
						rs.close();
					}
				} finally {
					stmt.close();
				}
			} finally {
				con.close();
			}
		} finally {
			this.deleteFile(testFile);
		}
	}

	/*
	 * Private helper methods for the unit testing
	 */
	private String getRanString(final int length) {
		final String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		final char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(this.randomGenerator.nextInt(characters
					.length()));
		}
		return new String(text);
	}

	private void deleteFile(final File testFile) {
		if (testFile.exists()) {
			if (!testFile.delete()) {
				throw new RuntimeException(String.format(
						"Unable to delete testFile. testFile: %s", testFile));
			}
		}
	}

	private File getRandomFile(final int length) {
		File testFile;

		do {
			final String filename = FileSystemView.getFileSystemView()
					.getDefaultDirectory().getAbsolutePath()
					+ "/" + this.getRanString(length) + ".gpkg";
			testFile = new File(filename);
		} while (testFile.exists());

		return testFile;
	}

	private Connection getConnection(final String filePath)
			throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC"); // Register the driver

		return DriverManager.getConnection("jdbc:sqlite:" + filePath);
	}
}
