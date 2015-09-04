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

import com.rgi.geopackage.GeoPackage;

import java.util.Collections;
import java.util.List;

/**
 * Representation of a node from a {@link GeoPackage} "rgi_network" network
 *
 * @author Luke Lambert
 *
 */
public class AttributedNode
{
    /**
     * Constructor
     *
     * @param identifier
     *             Unique node identifier
     * @param attributeValues
     *             Some or all of the nodes attributes
     */
    public AttributedNode(final int identifier,
                          final List<Object> attributeValues)
    {
        this.identifier      = identifier;
        this.attributeValues = attributeValues == null ? Collections.emptyList() : attributeValues;
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public List<Object> getAttributes()
    {
        return Collections.unmodifiableList(this.attributeValues);
    }

    /**
     * Gets the node's attribute as described by the supplied index
     *
     * @param attributeIndex
     *             Index of the attribute to retrieve. These values correspond
     *             to order in which the attributes were requested.
     * @return value at the specified index
     */
    public Object getAttribute(final int attributeIndex)
    {
        return this.attributeValues.get(attributeIndex);
    }

    private final int          identifier;
    private final List<Object> attributeValues; // TODO This should be Map<AttributeDescription, Object>, but I'm concerned about performance.  Once we're happy with performance numbers in routing, we should make this change and see what, if any, performance impact it has.
}
