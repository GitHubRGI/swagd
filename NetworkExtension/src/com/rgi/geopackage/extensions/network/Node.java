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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a node from a {@link GeoPackage} "rgi_network" network
 *
 * @author Luke Lambert
 *
 */
public class Node
{
    protected Node(final int identifier)
    {
        this(identifier, Collections.emptyList());
    }

    protected Node(final int       identifier,
                   final Object... attributeValues)
    {
        this(identifier, Arrays.asList(attributeValues));
    }

    protected Node(final int                identifier,
                   final Collection<Object> attributeValues)
    {
        if(attributeValues == null)
        {
            throw new IllegalArgumentException("Attribute values collection may not be null");
        }

        this.identifier      = identifier;
        this.attributeValues = new ArrayList<>(attributeValues);
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public Object getAttribute(final int attributeIndex)
    {
        return this.attributeValues.get(attributeIndex);
    }

    private final int          identifier;
    private final List<Object> attributeValues; // TODO This should be Map<AttributeDescription, Object>, but I'm concerned about performance.  Once we're happy with performance numbers in routing, we should make this change and see what, if any, performance impact it has.
}
