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

package com.rgi.android.geopackage.metadata;

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
 * @see <a href="http://www.geopackage.org/spec/#_metadata_table">OGC® GeoPackage Encoding Standard - 2.4.2. Metadata Table</a>
 *
 * @author Luke Lambert
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

    private final int    identifier;
    private final String scope;
    private final String standardUri;
    private final String mimeType;
    private final String metadata;
}
