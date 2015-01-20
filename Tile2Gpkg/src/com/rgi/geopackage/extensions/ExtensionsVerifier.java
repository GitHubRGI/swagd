/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.geopackage.extensions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.UniqueDefinition;
import com.rgi.geopackage.verification.Verifier;

public class ExtensionsVerifier extends Verifier
{
	private class ExtensionData
	{
		String tableName;
		String columnName;
		String extensionName;
	}



	private boolean hasGpkgExtensionsTable;
	private Map<ExtensionData, String> gpkgExtensionsDataAndColumnName;

    public ExtensionsVerifier(final Connection sqliteConnection)
    {
        super(sqliteConnection);

        try
        {
           this.hasGpkgExtensionsTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageExtensions.ExtensionsTableName);
        }
        catch(final SQLException ex)
        {
            this.hasGpkgExtensionsTable = false;
        }

        if(this.hasGpkgExtensionsTable)
        {

    		final String query = "SELECT table_name, column_name, extension_name FROM gpkg_extensions;";

    		try(Statement stmt				    = this.getSqliteConnection().createStatement();
    		    ResultSet tableNameColumnNameRS = stmt.executeQuery(query))
    		{
    			this.gpkgExtensionsDataAndColumnName = ResultSetStream.getStream(tableNameColumnNameRS)
																	    .map(resultSet ->{    try
																							  {
																	    						  final ExtensionData extensionData = new ExtensionData();
																	    						  extensionData.tableName     = resultSet.getString("table_name");
																	    						  extensionData.columnName    = resultSet.getString("column_name");
																			   					  extensionData.extensionName = resultSet.getString("extension_name");
																								  return new AbstractMap.SimpleImmutableEntry<>(extensionData, extensionData.columnName);
																							  }
																		    				  catch(final SQLException ex)
																		    				  {
																		    					  return null;
																		    				  }
																	   					})
																	   	.filter(Objects::nonNull)
												                        .collect(Collectors.toMap(entry -> entry.getKey(),
												                                                  entry -> entry.getValue()));
    		}
    		catch(final SQLException ex)
    		{
    		  this.gpkgExtensionsDataAndColumnName = Collections.emptyMap();
    		}
        }
    }

    /**
     * <div class="title">Requirement 78</div>
	 * <blockquote>
	 * A GeoPackage MAY contain a table or updateable view named gpkg_extensions.
	 * If present this table SHALL be defined per clause 2.5.2.1.1
	 * <a href="http://www.geopackage.org/spec/#extensions_table_definition">Table Definition</a>,
	 * <a href="http://www.geopackage.org/spec/#gpkg_extensions_cols">GeoPackage Extensions Table or
	 * View Definition (Table or View Name: gpkg_extensions)</a> and
	 * <a href="http://www.geopackage.org/spec/#gpkg_extensions_sql">gpkg_extensions Table Definition SQL</a>.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     * @throws AssertionError
     */
    @Requirement (number   = 78,
    		      text     = "A GeoPackage MAY contain a table or updateable view named gpkg_extensions."
    		      				+ " If present this table SHALL be defined per clause 2.5.2.1.1 Table Definition, "
    		      				+ "GeoPackage Extensions Table or View Definition (Table or View Name: gpkg_extensions) "
    		      				+ "and gpkg_extensions Table Definition SQL. ",
    		      severity = Severity.Error)
    public void Requirement78() throws AssertionError, SQLException
    {
    	if(this.hasGpkgExtensionsTable)
    	{
    		this.verifyTable(ExtensionsVerifier.ExtensionsTableDefinition);
    	}
    }


    /**
     * <div class="title">Requirement 79</div>
	 * <blockquote>
	 * Every extension of a GeoPackage SHALL be registered in a corresponding row in the gpkg_extensions table.
	 * The absence of a gpkg_extensions table or the absence of rows in gpkg_extnsions table SHALL both indicate
	 * the absence of extensions to a GeoPackage.
	 * </blockquote>
	 * </div>
     */
    @Requirement(number   = 79,
    			 text     = "Every extension of a GeoPackage SHALL be registered in a corresponding row "
    			 				+ "in the gpkg_extensions table. The absence of a gpkg_extensions table or "
    			 				+ "the absence of rows in gpkg_extnsions table SHALL both indicate the absence "
    			 				+ "of extensions to a GeoPackage.",
    			 severity = Severity.Warning)
    public void Requirement79()
    {
    	// TODO

    }

    /**
     * <div class="title">Requirement 80</div>
	 * <blockquote>
	 * Values of the <code>gpkg_extensions</code> <code>table_name</code>
	 * column SHALL reference values in the <code>gpkg_contents</code>
	 * <code>table_name</code> column or be NULL.
	 * They SHALL NOT be NULL for rows where the <code>column_name
	 * </code> value is not NULL.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     */
    @Requirement(number   = 80,
    			 text     = "Every extension of a GeoPackage SHALL be registered in a corresponding row "
    			 				+ "in the gpkg_extensions table. The absence of a gpkg_extensions table or "
    			 				+ "the absence of rows in gpkg_extnsions table SHALL both indicate the absence "
    			 				+ "of extensions to a GeoPackage.",
    			 severity = Severity.Warning)
    public void Requirement80() throws SQLException
    {
    	if(this.hasGpkgExtensionsTable)
    	{
			for(final ExtensionData extensionData: this.gpkgExtensionsDataAndColumnName.keySet())
			{
				final String columnName = this.gpkgExtensionsDataAndColumnName.get(extensionData);
				final boolean validEntry = extensionData.tableName == null ? columnName == null: true;  // if table name is null then so must column name
				Assert.assertTrue("The value in table_name can only be null if column_name is also null.",
						          validEntry);
			}
			//check that the table_name in GeoPackage Extensions references a table in sqlite master
    		final String query2 = "SELECT DISTINCT ge.table_name AS ge_table, "
    									  + "sm.tbl_name   AS sm_table "
    							  + "FROM  gpkg_extensions AS ge "
    							  + "LEFT OUTER JOIN sqlite_master AS sm ON "
    							  										  + "ge.table_name = sm.tbl_name";
    		try(Statement stmt2                = this.getSqliteConnection().createStatement();
    		    ResultSet tablesReferencedInSM = stmt2.executeQuery(query2))
		    {
    			final Map<String, String> tablesMatchesFound = ResultSetStream.getStream(tablesReferencedInSM)
	    															    .map(resultSet ->{    try
							    															  {
		    																   					   final String geTableName = resultSet.getString("ge_table");
		    																   					   final String smTableName = resultSet.getString("sm_table");
							    																   return new AbstractMap.SimpleImmutableEntry<>(geTableName,smTableName);
							    															  }
		    															    				  catch(final SQLException ex)
		    															    				  {
		    															    					  return null;
		    															    				  }
	    															   					})
	    															   	.filter(Objects::nonNull)
	    		                                                        .collect(Collectors.toMap(entry -> entry.getKey(),
	                                                                                              entry -> entry.getValue()));

    			for(final String geTable: tablesMatchesFound.keySet())
    			{
    				Assert.assertTrue(String.format("The table %s does not exist in the sqlite master table. "
    													+ "Either create table %s or delete this entry.",
    												geTable,
    												geTable),
    								  ExtensionsVerifier.equals(geTable, tablesMatchesFound.get(geTable)));
    			}
		    }

    	}

    }

    /**
     * <div class="title">Requirement 81</div>
	 * <blockquote>
	 * The <code>column_name</code> column value in a <code>gpkg_extensions</code>
	 * row SHALL be the name of a column in the table specified by the <code>table_name</code>
	 *  column value for that row, or be NULL.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     */
    @Requirement (number   = 81,
    			  text     = "The column_name column value in a gpkg_extensions row SHALL"
    			  				+ " be the name of a column in the table specified by the "
    			  				+ "table_name column value for that row, or be NULL.",
    			  severity = Severity.Warning)
    public void Requirement81() throws SQLException
    {
    	if(this.hasGpkgExtensionsTable && !this.gpkgExtensionsDataAndColumnName.isEmpty())
    	{
    		for(final ExtensionData extensionData: this.gpkgExtensionsDataAndColumnName.keySet())
    		{
    			final String columnName = extensionData.columnName;

    			if(extensionData.tableName != null && columnName != null)
    			{
    				final String query = String.format("PRAGMA table_info(%s);", extensionData.tableName);

    				try(Statement stmt      = this.getSqliteConnection().createStatement();
    					ResultSet tableInfo = stmt.executeQuery(query))
    				{
    					final boolean columnExists = ResultSetStream.getStream(tableInfo)
    														  .anyMatch(resultSet -> { try
																					   {
																						  return resultSet.getString("name").equals(columnName);
																					   }
																					   catch(final SQLException ex)
																					   {
																						  return false;
																					   }
																					  });

    					Assert.assertTrue(String.format("The column %s does not exist in the table %s. "
    												      + "Please either add this column to this table "
    												      + "or delete the record in gpkg_extensions.",
    												    columnName,
    												    extensionData.tableName),
    							          columnExists);
    				}
    			}
    		}
    	}
    }

    /**
     * <div class="title">Requirement 82</div>
	 * <blockquote>
	 * Each <code>extension_name</code> column value in a <code>gpkg_extensions</code>
	 * row SHALL be a unique case sensitive value of the form &lt;author&gt;_&lt;extension_name&gt;
	 * where &lt;author&gt; indicates the person or organization that developed and
	 * maintains the extension. The valid character set for <author> SHALL be [a-zA-Z0-9].
	 * The valid character set for &lt;extension_name&gt; SHALL be [a-zA-Z0-9_].
	 * An <code>extension_name</code> for the �gpkg� author name SHALL be one of those defined in
	 * this encoding standard or in an OGC Best Practices Document that extends it.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     */
    @Requirement (number 	= 82,
    			  text      = "Each extension_name column value in a gpkg_extensions row SHALL be a "
    					  		+ "unique case sensitive value of the form <author>_<extension_name> "
    					  		+ "where <author> indicates the person or organization that developed "
    					  		+ "and maintains the extension. The valid character set for <author> "
    					  		+ "SHALL be [a-zA-Z0-9]. The valid character set for <extension_name> "
    					  		+ "SHALL be [a-zA-Z0-9_]. An extension_name for the �gpkg� author name "
    					  		+ "SHALL be one of those defined in this encoding standard or in an OGC "
    					  		+ "Best Practices Document that extends it.",
    			  severity  = Severity.Warning)
    public void Requirement82() throws SQLException
    {
    	if(this.hasGpkgExtensionsTable)
    	{

    		final Set<String> invalidExtensionNames = this.gpkgExtensionsDataAndColumnName.keySet()
    																				  .stream()
    																				  .map(extensionData -> ExtensionsVerifier.verifyExtensionName(extensionData.extensionName))
    																				  .filter(data -> data != null)
    																				  .collect(Collectors.toSet());

    		Assert.assertTrue("The following extension_name(s) are invalid: \n".concat(
    																	 String.join(invalidExtensionNames.stream()
    																			 						  .map(extensionName -> extensionName)
    																			 						  .filter(Objects::nonNull)
    																			 						  .collect(Collectors.joining(", ")), "\n")),
    						  invalidExtensionNames.isEmpty());

    	}
    }

    /**
     * <div class="title">Requirement 83</div>
	 * <blockquote>
	 * The definition column value in a <code>gpkg_extensions</code>
	 *  row SHALL contain or reference the text that results from documenting
	 *  an extension by filling out the GeoPackage Extension Template in
	 *  <a href="http://www.geopackage.org/spec/#extension_template">
	 *  GeoPackage Extension Template (Normative)</a>.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     */
    @Requirement (number    = 83,
    		      text      = "The definition column value in a gpkg_extensions row SHALL "
    		      				+ "contain or reference the text that results from documenting "
    		      				+ "an extension by filling out the GeoPackage Extension Template "
    		      				+ "in GeoPackage Extension Template (Normative).",
    		      severity  = Severity.Warning)
    public void Requirement83() throws SQLException
    {
    	//TODO: Ask about how restrictive we want the strings to be
    	if(this.hasGpkgExtensionsTable)
    	{
    		final String query = "SELECT table_name "
    					 + "FROM gpkg_extensions "
    					 + "WHERE definition NOT LIKE 'Annex%' "
    					 + "AND   definition NOT LIKE 'http%' "
    					 + "AND   definition NOT LIKE 'mailto%' "
    					 + "AND   definition NOT LIKE 'Extension Title%';";

    		try(Statement stmt                    = this.getSqliteConnection().createStatement();
    			ResultSet invalidDefinitionValues = stmt.executeQuery(query))
    		{
    				final List<String> invalidDefinitions = ResultSetStream.getStream(invalidDefinitionValues)
    															     .map(resultSet ->
																				   { try
																				     {
																					   return resultSet.getString("table_name");
																				     }
																				     catch(final SQLException ex)
																				     {
																				    	return null;
																				     }
																				   })
																	 .filter(Objects::nonNull)
																     .collect(Collectors.toList());

    				Assert.assertTrue(String.format("The following table_name values in gpkg_extension table have invalid values for the definition column: %s.",
    						    					invalidDefinitions.stream()
    						    									  .collect(Collectors.joining(", "))),
    						          invalidDefinitions.isEmpty());

    		}
    	}
    }



    /**
     * <div class="title">Requirement 84</div>
	 * <blockquote>
	 * The scope column value in a <code>gpkg_extensions</code> row SHALL be
	 * lowercase "read-write" for an extension that affects both readers and writers,
	 * or "write-only" for an extension that affects only writers.
	 * </blockquote>
	 * </div>
     * @throws SQLException
     */
    @Requirement(number   = 84,
    			 text     = "The scope column value in a gpkg_extensions row SHALL be lowercase "
    			 			+ "\"read-write\" for an extension that affects both readers and writers, "
    			 			+ "or \"write-only\" for an extension that affects only writers. ",
    			 severity = Severity.Warning)
    public void Requirement84() throws SQLException
    {
    	if(this.hasGpkgExtensionsTable)
    	{
    		final String query = "SELECT scope FROM gpkg_extensions WHERE scope != 'read-write' AND scope != 'write-only'";

    		try(Statement stmt               = this.getSqliteConnection().createStatement();
    			ResultSet invalidScopeValues = stmt.executeQuery(query))
    		{
    			final List<String> invalidScope = ResultSetStream.getStream(invalidScopeValues)
														   .map(resultSet -> { try
															 				  {
															 					return resultSet.getString("scope");
															 				  }
															 				  catch(final SQLException ex)
																			  {
																			  	return null;
																			  }
														  					 })
													       .filter(Objects::nonNull)
													       .collect(Collectors.toList());

    			Assert.assertTrue(String.format("There is(are) value(s) in the column scope in gpkg_extensions"
    												+ " table that is not 'read-write' or 'write-only' in all "
    												+ "lowercase letters. The following value(s)  is(are) incorrect: %s",
    								            invalidScope.stream().collect(Collectors.joining(", "))),
    					          invalidScope.isEmpty());
    		}

    	}
    }


    private static String verifyExtensionName(final String extensionName)
	{
		final String author[] = extensionName.split("_", 2);

		if (author.length != 2)
		{
			return extensionName;
		}
		if (author[0].matches("gpkg")               ||
		    !author[0].matches("[a-zA-Z0-9]+")      ||
		    !author[1].matches("[a-zA-Z0-9_]+"))
		{
			return extensionName;
		}
        return null;
	}


    private static boolean equals(final String first, final String second)
    {
    	return first == null ? second == null : first.equals(second);
    }

    private static final TableDefinition ExtensionsTableDefinition;

    static
    {
        final Map<String, ColumnDefinition> extensionsTableColumns = new HashMap<>();

        extensionsTableColumns.put("table_name",      new ColumnDefinition("TEXT", false, false, false, null));
        extensionsTableColumns.put("column_name",     new ColumnDefinition("TEXT", false, false, false, null));
        extensionsTableColumns.put("extension_name",  new ColumnDefinition("TEXT", true,  false, false, null));
        extensionsTableColumns.put("definition",      new ColumnDefinition("TEXT", true,  false, false, null));
        extensionsTableColumns.put("scope",           new ColumnDefinition("TEXT", true,  false, false, null));

        ExtensionsTableDefinition = new TableDefinition(GeoPackageExtensions.ExtensionsTableName,
		                                                extensionsTableColumns,
		                                                Collections.emptySet(),
		                                                new HashSet<>(Arrays.asList(new UniqueDefinition("table_name", "column_name", "extension_name"))));
    }
}
