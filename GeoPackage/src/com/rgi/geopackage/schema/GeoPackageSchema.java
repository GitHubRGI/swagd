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

package com.rgi.geopackage.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.activation.MimeType;

import utility.DatabaseUtility;
import utility.SelectBuilder;

import com.rgi.common.util.jdbc.ResultSetStream;
import com.rgi.geopackage.core.Content;
import com.rgi.geopackage.verification.VerificationIssue;
import com.rgi.geopackage.verification.VerificationLevel;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageSchema
{
    /**
     * Constructor
     *
     * @param databaseConnection
     *             The open connection to the database that contains a GeoPackage
     */
    public GeoPackageSchema(final Connection databaseConnection)
    {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Schema requirements this GeoPackage failed to meet
     *
     * @param verificationLevel
     *             Controls the level of verification testing performed
     * @return The schema GeoPackage requirements this GeoPackage fails to conform to
     * @throws SQLException throws if the method {@link SchemaVerifier#SchemaVerifier} throws an SQLException
     */
    public Collection<VerificationIssue> getVerificationIssues(final VerificationLevel verificationLevel) throws SQLException
    {
        return new SchemaVerifier(this.databaseConnection, verificationLevel).getVerificationIssues();
    }

    /**
     * Add an entry to the geopackage_data_columns table
     *
     * @param table
     *            Content table
     * @param columnName
     *            Name of the table column
     * @param name
     *            A human-readable identifier (e.g. short name) for the
     *            columnName content
     * @param title
     *            A human-readable formal title for the columnName content
     * @param description
     *            A human-readable description for the tableName content
     * @param mimeType
     *            <a href=
     *            "http://www.iana.org/assignments/media-types/index.html"
     *            >MIME</a> type of columnName if BLOB type, or NULL for other
     *            types
     * @param constraintName
     *            Case sensitive column value constraint name specified by
     *            reference to gpkg_data_column_constraints.constraint name
     * @return Returns the newly added {@link DataColumn} object
     * @throws SQLException
     *             throws if unable to create the Data Columns Table and unable
     *             to insert values into the Data Columns Table along with other
     *             various SQLExceptions
     */
    public DataColumn addDataColumn(final Content  table,
                                    final String   columnName,
                                    final String   name,
                                    final String   title,
                                    final String   description,
                                    final MimeType mimeType,
                                    final String   constraintName) throws SQLException
    {
        if(table == null)
        {
            throw new IllegalArgumentException("Table may not be null");
        }

        if(columnName == null || columnName.isEmpty())
        {
            throw new IllegalArgumentException("Column name may not be null or empty");
        }

        // TODO check to make sure the table exists and that the column belongs to that table

        final DataColumn existingDataColumn = this.getDataColumn(table, columnName);

        if(existingDataColumn != null)
        {
            return existingDataColumn;
        }

        try
        {
            this.createDataColumnsTableNoCommit();  // Create the data columns table if it doesn't exist

            final String insertDataColumnSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                                                             GeoPackageSchema.DataColumnsTableName,
                                                             "table_name",
                                                             "column_name",
                                                             "name",
                                                             "title",
                                                             "description",
                                                             "mime_type",
                                                             "constraint_name");

            try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertDataColumnSql))
            {
                preparedStatement.setString(1, table.getTableName());
                preparedStatement.setString(2, columnName);
                preparedStatement.setString(3, name);
                preparedStatement.setString(4, title);
                preparedStatement.setString(5, description);
                preparedStatement.setObject(6, mimeType.toString());
                preparedStatement.setObject(7, constraintName);

                preparedStatement.executeUpdate();
            }

            final DataColumn dataColumn = new DataColumn(table.getTableName(),
                                                         columnName,
                                                         name,
                                                         title,
                                                         description,
                                                         mimeType.toString(),
                                                         constraintName);
            this.databaseConnection.commit();

            return dataColumn;
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }

    }

    /**
     * Adds an entry to the GeoPackage's data column constraints table
     *
     * @param constraintName
     *            Case sensitive name of constraint
     * @param constraintType
     *            Constraint type
     * @param value
     *            Specified case sensitive value for enum or glob or
     *            <code>null</code> for range constraintType
     * @param minimum
     *            Minimum value for "range" or <code>null</code> for "enum" or
     *            "glob" constraintType
     * @param minimumIsInclusive
     *            <code>false</code> if minimum value is exclusive,
     *            <code>true</code> if minimum value is inclusive, or
     *            <code>null</code> for "enum" or "glob" constraintType
     * @param maximum
     *            Maximum value for "range" or <code>null</code> for "enum" or
     *            "glob" constraintType
     * @param maximumIsInclusive
     *            <code>false</code> if maximum value is exclusive,
     *            <code>true</code> if maximum value is inclusive, or
     *            <code>null</code> for "enum" or "glob" constraintType
     * @param description
     *            For ranges and globs, describes the constraint; for enums,
     *            describes the enum value.
     * @return Returns the newly added {@link DataColumnConstraint} object
     * @throws SQLException
     *             throws if unable to create the Data Columns Constraints Table
     *             and unable to insert values into the Data Columns Constraints
     *             Table along with other various SQLExceptions
     */
    public DataColumnConstraint addDataColumnConstraint(final String  constraintName,
                                                        final Type    constraintType,
                                                        final String  value,
                                                        final Number  minimum,
                                                        final Boolean minimumIsInclusive,
                                                        final Number  maximum,
                                                        final Boolean maximumIsInclusive,
                                                        final String  description) throws SQLException
    {
        if(constraintName == null || constraintName.isEmpty())
        {
            throw new IllegalArgumentException("Constraint name may not be null or empty");
        }

        if(constraintType == null)
        {
            throw new IllegalArgumentException("Constraint type may not be null");
        }

        if(constraintType == Type.Range && value != null)
        {
            throw new IllegalArgumentException("Value must be null for range constraints types");   // Requirement 63
        }

        if(constraintType == Type.Range &&
           (minimum == null ||
            maximum == null ||
            minimum.doubleValue() >= maximum.doubleValue()))
        {
            throw new IllegalArgumentException("Minimum and maximum may not be null, and minimum must be strictly less than maximum for range constraints types");   // Requirement 64
        }

        if(constraintType == Type.Range &&
           (minimumIsInclusive == null ||
            maximumIsInclusive == null))
        {
            throw new IllegalArgumentException("Inclusivity parameters may not be null, for range constraints types");   // Requirement 65
        }

        if((constraintType == Type.Enum ||
            constraintType == Type.Glob) &&
           (minimum            != null ||
            maximum            != null ||
            minimumIsInclusive != null ||
            maximumIsInclusive != null))
        {
            throw new IllegalArgumentException("Minimum, maximum and inclusivity parameters must be null for enum and glob constraint types");  // Requirement 66
        }

        if((constraintType == Type.Enum ||
            constraintType == Type.Glob) &&
           value == null)
        {
            throw new IllegalArgumentException("Value may not be null for enum and glob constraint types");  // Requirement 67
        }

        final DataColumnConstraint existingDataColumnConstraint = this.getDataColumnConstraint(constraintName,
                                                                                               constraintType,
                                                                                               value);

        if(existingDataColumnConstraint != null)
        {
            return existingDataColumnConstraint;
        }

        try
        {
            this.createDataColumnConstraintsTableNoCommit();  // Create the data column constraints table if it doesn't exist

            final String insertDataColumnSql = String.format("INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                                                             GeoPackageSchema.DataColumnConstraintsTableName,
                                                             "constraint_name",
                                                             "constraint_type",
                                                             "value",
                                                             "min",
                                                             "minIsInclusive",
                                                             "max",
                                                             "maxIsInclusive",
                                                             "description");

            try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(insertDataColumnSql))
            {
                preparedStatement.setString (1, constraintName);
                preparedStatement.setString (2, constraintType.toString());
                preparedStatement.setString (3, value);
                preparedStatement.setObject (4, minimum);
                preparedStatement.setObject (5, minimumIsInclusive);
                preparedStatement.setObject (6, maximum);
                preparedStatement.setObject (7, maximumIsInclusive);
                preparedStatement.setObject (8, description);

                preparedStatement.executeUpdate();
            }

            final DataColumnConstraint dataColumnConstraint = new DataColumnConstraint(constraintName,
                                                                                       constraintType.toString(),
                                                                                       value,
                                                                                       minimum,
                                                                                       minimumIsInclusive,
                                                                                       maximum,
                                                                                       maximumIsInclusive,
                                                                                       description);
            this.databaseConnection.commit();

            return dataColumnConstraint;
        }
        catch(final Exception ex)
        {
            this.databaseConnection.rollback();
            throw ex;
        }
    }

    /**
     * Gets an entry in the GeoPackage data column table that matches the
     * supplied criteria
     *
     * @param table
     *            Content table
     * @param columnName
     *            Name of the table column
     * @return Returns the a {@link DataColumn} that matches the supplied
     *         criteria, or null if there isn't a match
     * @throws SQLException
     *             throws if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             or if other various SQLExceptions occur
     */
    public DataColumn getDataColumn(final Content table, final String columnName) throws SQLException
    {
        if(table == null)
        {
            throw new IllegalArgumentException("Content table may not be null");
        }

        if(columnName == null || columnName.isEmpty())
        {
            throw new IllegalArgumentException("Column name may not be null or empty");
        }

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            return null;
        }

        final String dataColumnQuerySql = String.format("SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ? AND %s = ? LIMIT 1;",
                                                        "name",
                                                        "title",
                                                        "description",
                                                        "mime_type",
                                                        "constraint_name",
                                                        GeoPackageSchema.DataColumnsTableName,
                                                        "table_name",
                                                        "column_name");


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(dataColumnQuerySql))
        {
            preparedStatement.setString(1, table.getTableName());
            preparedStatement.setString(2, columnName);

            try(ResultSet result = preparedStatement.executeQuery())
            {
                if(result.isBeforeFirst())
                {
                    return new DataColumn(table.getTableName(),
                                          columnName,
                                          result.getString(1),  // name
                                          result.getString(2),  // title
                                          result.getString(3),  // description
                                          result.getString(4),  // mime type
                                          result.getString(5)); // constraint name
                }
            }

            return null;
        }
    }

    /**
     * Gets an entry in the GeoPackage data column constraints table that
     * matches the supplied criteria
     *
     * @param constraintName
     *            Case sensitive name of constraint
     * @param constraintType
     *            Constraint type
     * @param value
     *            Specified case sensitive value for enum or glob or
     *            <code>null</code> for range constraintType
     * @return Returns the a {@link DataColumnConstraint} that matches the
     *         supplied criteria, or null if there isn't a match
     * @throws SQLException
     *             throws if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             or if other various SQLExceptions occur
     */
    public DataColumnConstraint getDataColumnConstraint(final String  constraintName,
                                                        final Type    constraintType,
                                                        final String  value) throws SQLException
    {
        if(constraintName == null || constraintName.isEmpty())
        {
            throw new IllegalArgumentException("Constraint name may not be null or empty");
        }

        if(constraintType == null)
        {
            throw new IllegalArgumentException("Constraint type may not be null");
        }

        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnConstraintsTableName))
        {
            return null;
        }

        try(final SelectBuilder selectStatement = new SelectBuilder(this.databaseConnection,
                                                                    GeoPackageSchema.DataColumnConstraintsTableName,
                                                                    Arrays.asList("min",
                                                                                  "minIsInclusive",
                                                                                  "max",
                                                                                  "maxIsInclusive",
                                                                                  "description"),
                                                                    Arrays.asList(new AbstractMap.SimpleImmutableEntry<>("constraint_name", constraintName),
                                                                                  new AbstractMap.SimpleImmutableEntry<>("constraint_type", constraintType),
                                                                                  new AbstractMap.SimpleImmutableEntry<>("value",           value)));
            final ResultSet result = selectStatement.executeQuery())
        {
            if(result.isBeforeFirst())
            {
                return new DataColumnConstraint(constraintName,
                                                constraintType.toString(),
                                                value,
                                                (Number)  result.getObject(1),   // minimum
                                                          result.getBoolean(2),  // minimum is inclusive
                                                (Number)  result.getObject(3),   // maximum
                                                          result.getBoolean(4),  // maximum is inclusive
                                                          result.getString(5));  // description
            }
        }

        return null;
    }

    /**
     * Gets every entry in the GeoPackage data columns table
     *
     * @return Returns a collection of {@link DataColumn} objects that
     *         represents all of the entries in the GeoPackage data columns
     *         table
     * @throws SQLException
     *             throws if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             or if other various SQLExceptions occur
     */
    public Collection<DataColumn> getDataColumns() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            return Collections.emptyList();
        }

        final String dataColumnQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s FROM %s;",
                                                        "table_name",
                                                        "column_name",
                                                        "name",
                                                        "title",
                                                        "description",
                                                        "mime_type",
                                                        "constraint_name",
                                                        GeoPackageSchema.DataColumnsTableName);


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(dataColumnQuerySql);
            ResultSet         results           = preparedStatement.executeQuery())
        {
            return ResultSetStream.getStream(results)
                                  .map(result -> { try
                                                   {
                                                       return new DataColumn(result.getString(1),  // table name
                                                                             result.getString(2),  // column name
                                                                             result.getString(3),  // name
                                                                             result.getString(4),  // title
                                                                             result.getString(5),  // description
                                                                             result.getString(6),  // mime type
                                                                             result.getString(7)); // constraint name
                                                   }
                                                   catch(final SQLException ex)
                                                   {
                                                       return null;
                                                   }
                                                 })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());
        }
    }

    /**
     * Gets every entry in the GeoPackage data column constraints table
     *
     * @return Returns a collection of {@link DataColumnConstraint} objects that
     *         represents all of the entries in the GeoPackage data column
     *         constraints table
     * @throws SQLException
     *             throws if the method
     *             {@link DatabaseUtility#tableOrViewExists(Connection, String)}
     *             or if other various SQLExceptions occur
     */
    public Collection<DataColumnConstraint> getDataColumnConstraints() throws SQLException
    {
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnConstraintsTableName))
        {
            return Collections.emptyList();
        }

        final String dataColumnConstraintsQuerySql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s FROM %s;",
                                                                   "constraint_name",
                                                                   "constraint_type",
                                                                   "value",
                                                                   "min",
                                                                   "minIsInclusive",
                                                                   "max",
                                                                   "maxIsInclusive",
                                                                   "description",
                                                                   GeoPackageSchema.DataColumnConstraintsTableName);


        try(PreparedStatement preparedStatement = this.databaseConnection.prepareStatement(dataColumnConstraintsQuerySql);
            ResultSet         results           = preparedStatement.executeQuery())
        {
            return ResultSetStream.getStream(results)
                                  .map(result -> { try
                                                   {
                                                       return new DataColumnConstraint(         result.getString(1),  // constraint name
                                                                                                result.getString(2),  // constraint type
                                                                                                result.getString(3),  // value
                                                                                       (Number) result.getObject(4),  // minimum
                                                                                  cast((Integer)result.getObject(5)), // minimum is inclusive
                                                                                       (Number) result.getObject(6),  // maximum
                                                                                  cast((Integer)result.getObject(7)), // maximum is inclusive
                                                                                                result.getString(8)); // description
                                                   }
                                                   catch(final SQLException ex)
                                                   {
                                                       return null;
                                                   }
                                                 })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());
        }
    }

    private static Boolean cast(final Integer integer)
    {
        return integer == null ? null : integer != 0;
    }


    /**
     * Creates the GeoPackage data columns table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createDataColumnsTableNoCommit() throws SQLException
    {
        // Create the data columns table
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnsTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getDataColumnsTableCreationSql());
            }
        }
    }

    /**
     * Creates the GeoPackage data column constraints table
     * <br>
     * <br>
     * <b>**WARNING**</b> this does not do a database commit. It is expected
     * that this transaction will always be paired with others that need to be
     * committed or rollback as a single transaction.
     *
     * @throws SQLException
     */
    protected void createDataColumnConstraintsTableNoCommit() throws SQLException
    {
        // Create the data column constraints table
        if(!DatabaseUtility.tableOrViewExists(this.databaseConnection, GeoPackageSchema.DataColumnConstraintsTableName))
        {
            try(Statement statement = this.databaseConnection.createStatement())
            {
                statement.executeUpdate(this.getDataColumnConstraintsTableCreationSql());
            }
        }
    }

    @SuppressWarnings("static-method")
    protected String getDataColumnsTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_data_columns_cols
        // http://www.geopackage.org/spec/#gpkg_data_columns_sql
        return "CREATE TABLE " + GeoPackageSchema.DataColumnsTableName + "\n"                                                                                             +
                "(table_name      TEXT NOT NULL, -- Name of the tiles or feature table\n"                                                                                 +
                " column_name     TEXT NOT NULL, -- Name of the table column\n"                                                                                           +
                " name            TEXT,          -- A human-readable identifier (e.g. short name) for the columnName content\n"                                           +
                " title           TEXT,          -- A human-readable formal title for the columnName content\n"                                                           +
                " description     TEXT,          -- A human-readable description for the tableName content\n"                                                             +
                " mime_type       TEXT,          -- MIME type of columnName if BLOB type, or NULL for other types\n"                                                      +
                " constraint_name TEXT,          -- Case sensitive column value constraint name specified by reference to gpkg_data_column_constraints.constraint name\n" +
                " CONSTRAINT pk_gdc PRIMARY KEY (table_name, column_name),\n"                                                                                             +
                " CONSTRAINT fk_gdc_tn FOREIGN KEY (table_name) REFERENCES gpkg_contents(table_name));";
    }

    @SuppressWarnings("static-method")
    protected String getDataColumnConstraintsTableCreationSql()
    {
        // http://www.geopackage.org/spec/#gpkg_data_column_constraints_cols
        // http://www.geopackage.org/spec/#gpkg_data_column_constraints_sql
        return "CREATE TABLE " + GeoPackageSchema.DataColumnConstraintsTableName + "\n"                                                     +
                "(constraint_name TEXT NOT NULL, -- Case sensitive name of constraint\n"                                                    +
                " constraint_type TEXT NOT NULL, -- Lowercase type name of constraint: 'range', 'enum', or 'glob'\n"                        +
                " value           TEXT,          -- Specified case sensitive value for enum or glob or null for range constraintType\n"     +
                " min             NUMERIC,       -- Minimum value for 'range' or null for 'enum' or 'glob' constraintType\n"                +
                " minIsInclusive  BOOLEAN,       -- false if minimum value is exclusive, or true if minimum value is inclusive\n"           +
                " max             NUMERIC,       -- Maximum value for 'range' or null for 'enum' or 'glob' constraintType\n"                +
                " maxIsInclusive  BOOLEAN,       -- false if maximum value is exclusive, or true if maximum value is inclusive\n"           +
                " description     TEXT,          -- For ranges and globs, describes the constraint; for enums, describes the enum value.\n" +
                " CONSTRAINT gdcc_ntv UNIQUE (constraint_name, constraint_type, value));";
    }

    private final Connection databaseConnection;

    /**
     * The name of the Data Columns Table "gpkg_data_columns"
     * http://www.geopackage.org/spec/#_data_columns
     */
    public final static String DataColumnsTableName           = "gpkg_data_columns";
    /**
     * The name of the Data Column Constraints Table "gpkg_data_column_constraints"
     * http://www.geopackage.org/spec/#_data_columns_constraints
     */
    public final static String DataColumnConstraintsTableName = "gpkg_data_column_constraints";
}
