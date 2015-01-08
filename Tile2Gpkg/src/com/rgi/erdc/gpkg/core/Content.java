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

package com.rgi.erdc.gpkg.core;

import java.util.Date;

import com.rgi.erdc.BoundingBox;

/**
 * @author Luke Lambert
 *
 */
public class Content
{
    /**
     * Constructor
     *
     * @param tableName
     *             The name of the tiles, feature, or extension specific content table
     * @param dataType
     *             Type of data stored in the table: "features" per clause Features, "tiles" per clause Tiles, or an implementer-defined value for other data tables per clause in an Extended GeoPackage.
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS)
     */
    protected Content(final String                 tableName,
                      final String                 dataType,
                      final String                 identifier,
                      final String                 description,
                      final Date                   lastChange,
                      final BoundingBox            boundingBox,
                      final SpatialReferenceSystem spatialReferenceSystem)
    {
        if(tableName == null || tableName.isEmpty())
        {
            throw new IllegalArgumentException("Tile set name may not be null");
        }

        if(!tableName.matches("^[_a-zA-Z]\\w*"))
        {
            throw new IllegalArgumentException("The tile set's name must begin with a letter (A..Z, a..z) or an underscore (_) and may only be followed by letters, underscores, or numbers");
        }

        if(tableName.startsWith("gpkg_"))
        {
            throw new IllegalArgumentException("The tile set's name may not start with the reserved prefix 'gpkg_'");
        }

        if(dataType == null || dataType.isEmpty())
        {
            throw new IllegalArgumentException("Data type cannot be null, or empty.");
        }

        if(boundingBox == null)
        {
            throw new IllegalArgumentException("Bounding box cannot be mull.");
        }

        this.tableName              = tableName;
        this.dataType               = dataType;
        this.identifier             = identifier;
        this.description            = description;
        this.lastChange             = lastChange;
        this.boundingBox            = boundingBox;
        this.spatialReferenceSystem = spatialReferenceSystem;
    }

    /**
     * @return The name of the tiles, feature, or extension specific content table
     */
    public String getTableName()
    {
        return this.tableName;
    }

    /**
     * @return Type of data stored in the table: "features" per clause Features, "tiles" per clause Tiles, or an implementer-defined value for other data tables per clause in an Extended GeoPackage.
     */
    public String getDataType()
    {
        return this.dataType;
    }

    /**
     * @return A human-readable identifier (e.g. short name) for the tableName content
     */
    public String getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return A human-readable description for the tableName content
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return Date value in ISO 8601 format as defined by the strftime function %Y-%m-%dT%H:%M:%fZ format string applied to the current time
     */
    public Date getLastChange()
    {
        return this.lastChange;
    }

    /**
     * @return Bounding box for all content in tableName
     */
    public BoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    /**
     * @return Spatial Reference System (SRS)
     */
    public SpatialReferenceSystem getSpatialReferenceSystem()
    {
        return this.spatialReferenceSystem;
    }

    public boolean equals(final String                 inTableName,
                          final String                 inDataType,
                          final String                 inIdentifier,
                          final String                 inDescription,
                          final BoundingBox            inBoundingBox,
                          final SpatialReferenceSystem inSpatialReferenceSystem)
    {
        return        this.tableName       .equals(inTableName)   &&
                      this.dataType        .equals(inDataType)    &&
                      this.boundingBox     .equals(inBoundingBox) &&
               equals(this.identifier,             inIdentifier)  &&
               equals(this.description,            inDescription) &&
               equals(this.spatialReferenceSystem, inSpatialReferenceSystem);
    }

    private static <T> boolean equals(final T first, final T second)
    {
        return first == null ? second == null
                             : first.equals(second);
    }

    private final String                 tableName;
    private final String                 dataType;
    private final String                 identifier;
    private final String                 description;
    private final Date                   lastChange;
    private final BoundingBox            boundingBox;
    private final SpatialReferenceSystem spatialReferenceSystem;
}
