package com.rgi.geopackage.metadata;


/**
 * GeoPackage Metadata
 *
 * <blockquote cite="http://www.geopackage.org/spec/#_metadata_table" type="cite">
 * The first component of GeoPackage metadata is the gpkg_metadata table that
 * MAY contain metadata in MIME encodings structured in accordance with any
 * authoritative metadata specification, such as ISO 19115, ISO 19115-2, ISO
 * 19139, Dublin Core, CSDGM, DDMS, NMF/NMIS, etc. The GeoPackage
 * interpretation of what constitutes "metadata" is a broad one that includes
 * UML models encoded in XMI, GML Application Schemas, ISO 19110 feature
 * catalogues, OWL and SKOS taxonomies, etc.
 * </blockquote>
 *
 * @author Luke Lambert
 *
 * @see <a href="http://www.geopackage.org/spec/#_metadata_table">OGC® GeoPackage Encoding Standard - 2.4.2. Metadata Table</a>
 *
 */
public class Metadata
{
    /**
     * Constructor
     *
     * @param identifier
     *             Metadata primary key
     * @param scope
     *             Metadata scope
     * @param standardUri
     *             URI reference to the metadata structure definition authority
     * @param mimeType
     *             MIME encoding of metadata
     * @param metadata
     *             Metadata text
     */
    protected Metadata(final int    identifier,
                       final String scope,
                       final String standardUri,
                       final String mimeType,
                       final String metadata)
    {
        this.identifier  = identifier;
        this.scope       = scope;
        this.standardUri = standardUri;
        this.mimeType    = mimeType;
        this.metadata    = metadata;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the scope
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * @return the standardUri
     */
    public String getStandardUri()
    {
        return this.standardUri;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType()
    {
        return this.mimeType;
    }

    /**
     * @return the metadata
     */
    public String getMetadata()
    {
        return this.metadata;
    }

    private final int   identifier;
    private final String scope;
    private final String standardUri;
    private final String mimeType;
    private final String metadata;
}
