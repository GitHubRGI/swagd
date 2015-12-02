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

package com.rgi.geopackage.features;

import com.rgi.geopackage.features.geometry.Geometry;

import java.util.Map;

/**
 * @author Luke Lambert
 *
 */
public class Feature
{

    /**
     * @param identifier
     * @param geometry
     * @param attributes
     */
    protected Feature(final int                 identifier,
                      final Geometry geometry,
                      final Map<String, Object> attributes)
    {
        this.identifier = identifier;
        this.geometry   = geometry;
        this.attributes = attributes;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier()
    {
        return this.identifier;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry()
    {
        return this.geometry;
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes()
    {
        return this.attributes;
    }

    private final int                 identifier;
    private final Geometry            geometry;
    private final Map<String, Object> attributes;
}
