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

import com.rgi.common.BoundingBox;

/**
 * @author Luke Lambert
 *
 * @param <T> Extends {@link Content}
 */
@FunctionalInterface
public interface ContentFactory<T extends Content>
{
    /**
     * @param tableName
     *            The name of the content table. The table name must begin with
     *            a letter (A..Z, a..z) or an underscore (_) and may only be
     *            followed by letters, underscores, or numbers, and may not
     *            begin with the prefix "gpkg_"
     * @param identifier
     *            A human-readable identifier (e.g. short name) for the
     *            tableName content
     * @param description
     *            A human-readable description for the tableName content
     * @param lastChange
     *            Timestamp value in ISO 8601 format as defined by the
     *            strftime function %Y-%m-%dT%H:%M:%fZ format string applied
     *            to the current time
     * @param minimumX
     *             Bounding box minimum easting or longitude for all content
     * @param maximumX
     *             Bounding box minimum northing or latitude for all content
     * @param minimumY
     *             Bounding box maximum easting or longitude for all content
     * @param maximumY
     *             Bounding box maximum northing or latitude for all content
     * @param spatialReferenceSystemIdentifier
     *             The spatial reference system version number (otherwise known as identifier)
     * @return a Content object with the following parameters
     */
    public T create(final String  tableName,
                    final String  dataType,
                    final String  identifier,
                    final String  description,
                    final String  lastChange,
                    final Double  minimumX,
                    final Double  maximumX,
                    final Double  minimumY,
                    final Double  maximumY,
                    final Integer spatialReferenceSystemIdentifier);
}
