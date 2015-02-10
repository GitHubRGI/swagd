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
     *             Unique identifier for each Spatial Reference System within a GeoPackage
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

        // The testing of this.description vs other.description is intentionally left out.
        return this.name              .equals(otherSrs.name)                   &&
               this.identifier        == otherSrs.identifier                   &&
               this.organization      .equalsIgnoreCase(otherSrs.organization) &&
               this.organizationSrsId == otherSrs.organizationSrsId            &&
               this.definition        .equals(otherSrs.definition);
    }

    @Override
    public int hashCode()
    {
        // description is intentionally left out
        return this.name.hashCode()         ^
               this.identifier              ^
               this.organization.hashCode() ^
               this.organizationSrsId       ^
               this.definition.hashCode();
    }

    /**
     * @param inName Spatial Reference System name
     * @param inIdentifier Spatial Reference System identifier
     * @param inOrganization Spatial Reference System organization
     * @param inOrganizationSrsId Spatial Reference System Identifier
     * @param inDefinition Spatial Reference System definition
     * @return returns true if the Spatial Reference System equals the parameter values; otherwise returns false;
     */
    public boolean equals(final String inName,
                          final int    inIdentifier,
                          final String inOrganization,
                          final int    inOrganizationSrsId,
                          final String inDefinition)
    {
        // The testing of this.description vs other.description is intentionally left out.
        return this.name              .equals(inName)                   &&
               this.identifier        == inIdentifier                   &&
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
     * @return Unique identifier for each Spatial Reference System within a GeoPackage
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
