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

package com.rgi.geopackage.core;

/**
 *  A class based representation of entries to a GeoPackage's Spatial Reference
 *  System table.
 *
 * @author Luke Lambert
 *
 */
public class SpatialReferenceSystem
{
    /**
     * Constructor
     *
     * @param name
     *             Human readable name of this spatial reference system
     * @param identifier
     *             Unique identifier for each Spatial reference system within a GeoPackage
     * @param organization
     *             Case-insensitive name of the defining organization e.g. EPSG or epsg
     * @param organizationSrsId
     *             Numeric ID of the spatial reference system assigned by the organization
     * @param definition
     *             Well-known Text (WKT) representation of the spatial reference system
     * @param description
     *             Human readable description of this spatial reference system
     */
    protected SpatialReferenceSystem(final String name,
                                     final int    identifier,
                                     final String organization,
                                     final int    organizationSrsId,
                                     final String definition,
                                     final String description)
    {
        if(name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("Name may not be null or empty");
        }

        if(organization == null || organization.isEmpty())
        {
            throw new IllegalArgumentException("Organization may not be null or empty");
        }

        if(definition == null || definition.isEmpty())
        {
            throw new IllegalArgumentException("Definition may not be null or empty");
        }

        // TODO: it'd be nice to do an additional check to see if 'definition' was a conformant WKT SRS

        this.name              = name;
        this.identifier        = identifier;
        this.organization      = organization;
        this.organizationSrsId = organizationSrsId;
        this.description       = description;
        this.definition        = definition.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");   // This removes all whitespace from the string that's not inside
                                                                                                    // of quotes. This is to ensure later comparison
                                                                                                    // (.equal/.hashCode) isn't tripped up by differences in white
                                                                                                    // space.  Credit for regex goes to Bart Kiers, posted:
                                                                                                    // https://stackoverflow.com/a/9584469/16434
                                                                                                    // Bart includes another version of the regex posted that handles
                                                                                                    // escaped double quotes, but it appears that the WKT
                                                                                                    // specification does not allow for any escaped characters in
                                                                                                    // their "<quoted name>"s definition.  See:
                                                                                                    // http://portal.opengeospatial.org/files/?artifact_id=25355
                                                                                                    // section 7.2.1, "BNF Introduction".
    }

    @Override
    public boolean equals(final Object other)
    {
        if(other == null || other instanceof SpatialReferenceSystem == false)
        {
            return false;
        }

        final SpatialReferenceSystem otherSrs = (SpatialReferenceSystem)other;

        // The testing of this.description v.s. other.description is intentionally left out.
        return this.name              .equals(otherSrs.name)                   &&
               this.identifier        == otherSrs.identifier                   &&
               this.organization      .equalsIgnoreCase(otherSrs.organization) &&
               this.organizationSrsId == otherSrs.organizationSrsId            &&
               this.definition        .equals(otherSrs.definition);
    }

    @Override
    public int hashCode()
    {
        // Description is intentionally left out
        return this.name.hashCode()         ^
               this.identifier              ^
               this.organization.hashCode() ^
               this.organizationSrsId       ^
               this.definition.hashCode();
    }

    /**
     * @param inName
     *             Spatial reference system name
     * @param inOrganization
     *             Spatial reference system organization
     * @param inOrganizationSrsId
     *             Spatial reference system Identifier
     * @param inDefinition
     *             Spatial reference system definition
     * @return returns true if the Spatial reference system equals the
     *             parameter values; otherwise returns false;
     */
    public boolean equals(final String inName,
                          final String inOrganization,
                          final int    inOrganizationSrsId,
                          final String inDefinition)
    {
        // The testing of this.description v.s. other.description is intentionally left out.
        return this.name              .equals(inName)                   &&
               this.organization      .equalsIgnoreCase(inOrganization) &&
               this.organizationSrsId == inOrganizationSrsId            &&
               this.definition        .equals(inDefinition);
    }

    /**
     * @return Human readable name of this spatial reference system
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return Unique identifier for each spatial reference system within a GeoPackage
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return Case-insensitive name of the defining organization e.g. EPSG or epsg
     */
    public String getOrganization()
    {
        return this.organization;
    }

    /**
     * @return Numeric ID of the spatial reference system assigned by the organization
     */
    public int getOrganizationSrsId()
    {
        return this.organizationSrsId;
    }

    /**
     * @return Well-known Text (WKT) representation of the spatial reference system
     */
    public String getDefinition()
    {
        return this.definition;
    }

    /**
     * @return Human readable description of this spatial reference system
     */
    public String getDescription()
    {
        return this.description;
    }

    private final String name;
    private final int    identifier;
    private final String organization;
    private final int    organizationSrsId;
    private final String definition;
    private final String description;
}
