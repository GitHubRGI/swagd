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

package com.rgi.geopackage.extensions.network;

import com.rgi.common.BoundingBox;
import com.rgi.geopackage.core.Content;

/**
 * An object representation of an "rgi_network" GeoPackage extension network
 * table
 *
 * @author Luke Lambert
 *
 */
public class Network extends Content
{
    /**
     * Constructor
     *
     * @param tableName
     *             The name of the tiles, feature, or extension specific
     *             content table
     * @param identifier
     *             A human-readable identifier (e.g. short name) for the
     *             tableName content
     * @param description
     *             A human-readable description for the tableName content
     * @param lastChange
     *             Date value in ISO 8601 format as defined by the strftime
     *             function %Y-%m-%dT%H:%M:%fZ format string applied to the
     *             current time
     * @param boundingBox
     *             Bounding box for all content in tableName
     * @param spatialReferenceSystem
     *             Spatial Reference System (SRS) identifier
     */
    protected Network(final String      tableName,
                      final String      identifier,
                      final String      description,
                      final String      lastChange,
                      final BoundingBox boundingBox,
                      final int         spatialReferenceSystemIdentifier)
    {
        super(tableName,
              Network.NetworkContentType,
              identifier,
              description,
              lastChange,
              boundingBox,
              spatialReferenceSystemIdentifier);
    }

    /**
     * The "rgi_network" GeoPackage extension network data type for all
     * network tables
     */
    public static final String NetworkContentType = "network";
}
