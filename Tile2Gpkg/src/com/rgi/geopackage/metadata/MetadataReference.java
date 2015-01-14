package com.rgi.geopackage.metadata;


/**
 * Links metadata in the gpkg_metadata table to data in the feature, and tiles tables.
 *
 * @see <a href="http://www.geopackage.org/spec/#_metadata_reference_table">OGC® GeoPackage Encoding Standard - 2.4.3. Metadata Reference Table</a>
 *
 * @author Luke Lambert
 *
 */
public class MetadataReference
{
    /**
     * Constructor
     *
     * @param referenceScope
     *             Lowercase metadata reference scope; one of ‘geopackage’, ‘table’,‘column’, ’row’, ’row/col’
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
    protected MetadataReference(final String  referenceScope,
                                final String  tableName,
                                final String  columnName,
                                final Integer rowIdentifier,
                                final String  timestamp,
                                final int     fileIdentifier,
                                final Integer parentIdentifier)
    {
        this.referenceScope   = referenceScope;
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
    public String getReferenceScope()
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
    public String getTimestamp()
    {
        return this.timestamp;
    }

    /**
     * @return the fileIdentifier
     */
    public int getFileIdentifier()
    {
        return this.fileIdentifier;
    }

    /**
     * @return the parentIdentifier
     */
    public Integer getParentIdentifier()
    {
        return this.parentIdentifier;
    }
    private final String   referenceScope;
    private final String  tableName;
    private final String  columnName;
    private final Integer rowIdentifier;
    private final String  timestamp;
    private final int     fileIdentifier;
    private final Integer parentIdentifier;
}
