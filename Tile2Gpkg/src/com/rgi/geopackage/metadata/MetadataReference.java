package com.rgi.geopackage.metadata;

import java.util.Date;

public class MetadataReference
{
    /**
     * @param referenceScope
     *             Lowercase metadata reference scope; one of 'geopackage', 'table','column', 'row', 'row/col'
     * @param tableName
     *             Name of the table to which this metadata reference applies, or NULL for referenceScope of 'geopackage'
     * @param columnName
     *             Name of the column to which this metadata reference applies; NULL for referenceScope of 'geopackage','table' or 'row', or the name of a column in the tableName table for referenceScope of 'column' or 'row/col'
     * @param rowIdentifier
     *             NULL for referenceScope of 'geopackage', 'table' or 'column', or the rowed of a row record in the table_name table for referenceScope of 'row' or 'row/col'
     * @param timestamp
     *             Timestamp value
     * @param fileIdentifier
     *             gpkg_metadata table identifier column value for the metadata to which this gpkg_metadata_reference applies
     * @param parentIdentifier
     *             gpkg_metadata table identifier column value for the hierarchical parent gpkg_metadata for the gpkg_metadata to which this gpkg_metadata_reference applies, or NULL if file identifier forms the root of a metadata hierarchy
     */
    public MetadataReference(final String   referenceScope,
                             final String   tableName,
                             final String   columnName,
                             final Integer  rowIdentifier,
                             final Date     timestamp,
                             final Metadata fileIdentifier,
                             final Metadata parentIdentifier)
    {
        this.referenceScope = ReferenceScope.fromText(referenceScope);

        if(this.referenceScope == ReferenceScope.GeoPackage && tableName != null)
        {
            throw new IllegalArgumentException("Reference scopes of 'geopackage' must have null for the associated table name, and other reference scope values must have non-null table names");    // Requirement 72
        }

        if(!ReferenceScope.isColumnScope(this.referenceScope) && columnName != null)
        {
            throw new IllegalArgumentException("Reference scopes 'geopackage', 'table' or 'row' must have a null column name. Reference scope values of 'column' or 'row/col' must have a non-null column name"); // Requirement 73
        }

        if(ReferenceScope.isRowScope(this.referenceScope) && rowIdentifier == null)
        {
            throw new IllegalArgumentException(String.format("Reference scopes of 'geopackage', 'table' or 'column' must have a null row identifier.  Reference scopes of 'row' or 'row/col', must contain a reference to a row record in the '%s' table",
                                                             tableName)); // Requirement 74
        }

        if(tableName != null && tableName.isEmpty())
        {
            throw new IllegalArgumentException("If table name is non-null, it may not be empty");
        }

        if(columnName != null && columnName.isEmpty())
        {
            throw new IllegalArgumentException("If column name is non-null, it may not be empty");
        }

        if(timestamp == null)
        {
            throw new IllegalArgumentException("Timestamp may not be null");
        }

        if(fileIdentifier == null)
        {
            throw new IllegalArgumentException("File identifier may not be null");
        }

        this.tableName        = tableName;
        this.columnName       = columnName;
        this.rowIdentifier    = rowIdentifier;
        this.timestamp        = timestamp;
        this.fileIdentifier   = fileIdentifier;
        this.parentIdentifier = parentIdentifier;
    }

    /**
     * @return the referenceScope
     */
    public ReferenceScope getReferenceScope()
    {
        return this.referenceScope;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return the columnName
     */
    public String getColumnName()
    {
        return this.columnName;
    }

    /**
     * @return the rowIdentifier
     */
    public Integer getRowIdentifier()
    {
        return this.rowIdentifier;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp()
    {
        return this.timestamp;
    }

    /**
     * @return the fileIdentifier
     */
    public Metadata getFileIdentifier()
    {
        return this.fileIdentifier;
    }

    /**
     * @return the parentIdentifier
     */
    public Metadata getParentIdentifier()
    {
        return this.parentIdentifier;
    }
    private final ReferenceScope referenceScope;
    private final String         tableName;
    private final String         columnName;
    private final Integer        rowIdentifier;
    private final Date           timestamp;
    private final Metadata       fileIdentifier;
    private final Metadata       parentIdentifier;
}
