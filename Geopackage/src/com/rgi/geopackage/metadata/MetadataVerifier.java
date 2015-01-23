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

package com.rgi.geopackage.metadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rgi.geopackage.verification.Assert;
import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.DatabaseUtility;
import com.rgi.geopackage.verification.AssertionError;
import com.rgi.geopackage.verification.ColumnDefinition;
import com.rgi.geopackage.verification.ForeignKeyDefinition;
import com.rgi.geopackage.verification.Requirement;
import com.rgi.geopackage.verification.Severity;
import com.rgi.geopackage.verification.TableDefinition;
import com.rgi.geopackage.verification.Verifier;

public class MetadataVerifier extends Verifier
{
    private boolean hasMetadataTable;
    private boolean hasMetadataReferenceTable;
    private List<Metadata> metadataValues;
    
    public class Metadata
    {
        int id;
        String md_scope;
    }
    
    public MetadataVerifier(final Connection sqliteConnection) throws SQLException
    {
        super(sqliteConnection);
        
        this.hasMetadataTable          = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataTableName);
        this.hasMetadataReferenceTable = DatabaseUtility.tableOrViewExists(this.getSqliteConnection(), GeoPackageMetadata.MetadataReferenceTableName);
        this.metadataValues = getMetadataValues();
        
        
    }

    /**
     * <div class="title">Requirement 68</div> <blockquote> A GeoPackage MAY
     * contain a table named gpkg_metadata. If present it SHALL be defined per
     * clause 2.4.2.1.1 <a
     * href="http://www.geopackage.org/spec/#metadata_table_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_cols">Metadata Table
     * Definition</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_sql">gpkg_metadata
     * Table Definition SQL</a>. </blockquote> </div>
     * @throws SQLException 
     * @throws AssertionError 
     */
    @Requirement (number   = 68,
                  text     = "A GeoPackage MAY contain a table named gpkg_metadata."
                              + " If present it SHALL be defined per clause 2.4.2.1.1 "
                              + "Table Definition, Metadata Table Definition and gpkg_metadata "
                              + "Table Definition SQL. ",
                  severity = Severity.Error)
    public void Requirement68() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            this.verifyTable(MetadataTableDefinition);
        }
    }
    
    /**
     * <div class="title">Requirement 69</div> <blockquote> Each
     * <code>md_scope</code> column value in a <code>gpkg_metadata</code> table
     * or updateable view SHALL be one of the name column values from <a
     * href="http://www.geopackage.org/spec/#metadata_scopes">Metadata
     * Scopes</a>. </blockquote> </div>
     * @throws AssertionError 
     */
    @Requirement (number    = 69,
                  text      = "Each md_scope column value in a gpkg_metadata table or "
                              + "updateable view SHALL be one of the name column values from Metadata Scopes. ",
                  severity  = Severity.Warning)
    public void Requirement69() throws AssertionError
    {
        if(this.hasMetadataTable)
        {
            List<Metadata> invalidMetadataValues = this.metadataValues.stream()
                                                                     .filter(metadata -> MetadataVerifier.validMdScope(metadata.md_scope))
                                                                     .collect(Collectors.toList());
            
            Assert.assertTrue(String.format("The following md_scope(s) are invalid values in the gpkg_metadata table: %s", 
                                            invalidMetadataValues.stream()
                                                                 .map(value -> value.md_scope)
                                                                 .collect(Collectors.joining(", "))), 
                             invalidMetadataValues.isEmpty());
        }
    }
    
    /**
     * <div class="title">Requirement 70</div> <blockquote> A GeoPackage that
     * contains a <code>gpkg_metadata</code> table SHALL contain a
     * <code>gpkg_metadata_reference</code> table per clause 2.4.3.1.1 <a href=
     * "http://www.geopackage.org/spec/#metadata_reference_table_table_definition"
     * >Table Definition</a>, <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_reference_cols"
     * >Metadata Reference Table Definition (Table Name:
     * gpkg_metadata_reference)</a> and <a
     * href="http://www.geopackage.org/spec/#gpkg_metadata_reference_sql"
     * >gpkg_metadata_reference Table Definition SQL</a>. </blockquote> </div>
     * 
     * @throws AssertionError
     * @throws SQLException
     */
    @Requirement (number   = 70,
                  text     = "A GeoPackage that contains a gpkg_metadata table SHALL contain a "
                              + "gpkg_metadata_reference table per clause 2.4.3.1.1 Table Definition, "
                              + "Metadata Reference Table Definition (Table Name: gpkg_metadata_reference) "
                              + "and gpkg_metadata_reference Table Definition SQL. ",
                  severity = Severity.Warning)
    public void Requirement70() throws AssertionError, SQLException
    {
        if(this.hasMetadataTable)
        {
            Assert.assertTrue("This contains a gpkg_metadata table but not a gpkg_metadata_reference table.  "
                               + "Either drop the gpkg_metadata table or add a gpkg_metadata_reference table",
                              hasMetadataReferenceTable);
            
            this.verifyTable(MetadataVerifier.MetadataReferenceTableDefinition);
        }
    }
    
    private List<Metadata> getMetadataValues()
    {
        String query = "SELECT md_scope, id FROM gpkg_metadata;";
        
        try(Statement stmt            = this.getSqliteConnection().createStatement();
            ResultSet metadataValueRS = stmt.executeQuery(query))
            {
                return ResultSetStream.getStream(metadataValueRS)
                                      .map(resultSet -> {  try
                                                           {
                                                                Metadata metadata = new Metadata();
                                                                metadata.id = resultSet.getInt("id");
                                                                metadata.md_scope = resultSet.getString("md_scope");
                                                                
                                                                return metadata;
                                                           }
                                                           catch(SQLException ex)
                                                           {
                                                               return null;
                                                           }
                                                        })
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());
                
            }
            catch(SQLException ex)
            {
                return Collections.emptyList();
            }
    }
    private static boolean validMdScope(final String mdScope)
    {
        return Stream.of(Scope.values()).anyMatch(scope -> scope.toString().equalsIgnoreCase(mdScope));
    }
    
    private static final TableDefinition MetadataTableDefinition;
    private static final TableDefinition MetadataReferenceTableDefinition;
    static
    {
        final Map<String, ColumnDefinition> metadataTableColumns = new HashMap<>();

        metadataTableColumns.put("id",               new ColumnDefinition("INTEGER", true,  true, false, null));
        metadataTableColumns.put("md_scope",         new ColumnDefinition("TEXT",    true, false, false, "\\s*[dataset]\\s*"));//TODO check if regex works as intended
        metadataTableColumns.put("md_standard_uri",  new ColumnDefinition("TEXT",    true, false, false, null));
        metadataTableColumns.put("mime_type",        new ColumnDefinition("TEXT",    true, false, false, "\\s*[text\\\\xml]\\s*"));
        metadataTableColumns.put("metadata",         new ColumnDefinition("TEXT",    true, false, false, "\\s*''\\s*|\\s*\"\"\\s*"));

        MetadataTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataTableName,
                                                      metadataTableColumns);
        
        final Map<String, ColumnDefinition> metadataReferenceTableColumns = new HashMap<>();

        metadataReferenceTableColumns.put("reference_scope",  new ColumnDefinition("TEXT",     true,  false, false, null));
        metadataReferenceTableColumns.put("table_name",       new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("column_name",      new ColumnDefinition("TEXT",     false, false, false, null));
        metadataReferenceTableColumns.put("row_id_value",     new ColumnDefinition("INTEGER",  false, false, false, null));
        metadataReferenceTableColumns.put("timestamp",        new ColumnDefinition("DATETIME", true,  false, false, "\\s*strftime\\s*\\(\\s*['\"]%Y-%m-%dT%H:%M:%fZ['\"]\\s*,\\s*['\"]now['\"]\\s*\\)\\s*"));
        metadataReferenceTableColumns.put("md_file_id",       new ColumnDefinition("INTEGER",  true,  false, false, null));
        metadataReferenceTableColumns.put("md_parent_id",     new ColumnDefinition("INTEGER",  false, false, false, null));

        MetadataReferenceTableDefinition = new TableDefinition(GeoPackageMetadata.MetadataReferenceTableName,
                                                               metadataReferenceTableColumns,
                                                               new HashSet<>(Arrays.asList(new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_parent_id", "id"), 
                                                                                           new ForeignKeyDefinition(GeoPackageMetadata.MetadataTableName, "md_file_id", "id"))));


        
    }

}
