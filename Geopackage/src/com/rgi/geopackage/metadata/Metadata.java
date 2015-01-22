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

    private final int   identifier;
    private final String scope;
    private final String standardUri;
    private final String mimeType;
    private final String metadata;
}
