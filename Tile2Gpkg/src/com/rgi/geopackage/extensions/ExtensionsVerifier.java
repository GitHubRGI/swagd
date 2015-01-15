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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.tiles.TilePyramidUserDataTableDefinition;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.Verifier;

public class ExtensionsVerifier extends Verifier
{
	private boolean hasGpkgExtensionsTable;
	
    public ExtensionsVerifier(final Connection sqliteConnection)
    {
        super(sqliteConnection);
        
        try
        {
           this.hasGpkgExtensionsTable = DatabaseUtility.tableOrViewExists(getSqliteConnection(), GeoPackageExtensions.ExtensionsTableName);
        }
        catch(SQLException ex)
        {
            this.hasGpkgExtensionsTable = false;
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
    		this.verifyTable(ExtensionsTableDefinition);
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
    		String query = "SELECT table_name, column_name FROM gpkg_extensions;";
    		
    		try(Statement stmt				    = this.getSqliteConnection().createStatement();
    		    ResultSet tableNameColumnNameRS = stmt.executeQuery(query))
    		{
    			while(tableNameColumnNameRS.next())
    			{
    				String tableName  = tableNameColumnNameRS.getString("table_name");
    				String columnName = tableNameColumnNameRS.getString("column_name");
    				
    				//TODO:create a method that will compare and pass if both values are null or if they both match
    				
    			}
    			
    		}
    				
    	}
    	
    }
    
    
    
    
    private static final TableDefinition ExtensionsTableDefinition;
    static
    {
        final Map<String, ColumnDefinition> extensionsTableColumns = new HashMap<>();

        extensionsTableColumns.put("table_name",      new ColumnDefinition("TEXT", false, true,  true,  null));
        extensionsTableColumns.put("column_name",     new ColumnDefinition("TEXT", false, true,  true,  null));
        extensionsTableColumns.put("extension_name",  new ColumnDefinition("TEXT", true,  false, true,  null));
        extensionsTableColumns.put("definition",      new ColumnDefinition("TEXT", true,  false, false, null));
        extensionsTableColumns.put("scope",           new ColumnDefinition("TEXT", true,  false, false, null));

        ExtensionsTableDefinition = new TableDefinition(GeoPackageExtensions.ExtensionsTableName,
		                                                extensionsTableColumns,
		                                                Collections.emptySet());
    }
}
