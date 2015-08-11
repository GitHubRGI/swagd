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

package com.rgi.geopackage.extensions.routing;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Luke Lambert
 */
public class Route
{

    public Route(final Collection<Collection<Object>> nodesAttributes,
                 final Collection<Collection<Object>> edgesAttributes,
                 final Collection<Double>             edgeCosts)
    {
        this.nodesAttributes = nodesAttributes;
        this.edgesAttributes = edgesAttributes;
        this.edgeCosts       = edgeCosts;
    }

    public Collection<Collection<Object>> getNodesAttributes()
    {
        return Collections.unmodifiableCollection(this.nodesAttributes);
    }

    public Collection<Collection<Object>> getEdgesAttributes()
    {
        return Collections.unmodifiableCollection(this.edgesAttributes);
    }

    public Collection<Double> getEdgeCosts()
    {
        return Collections.unmodifiableCollection(this.edgeCosts);
    }

    public double getTotalCost()
    {
        return this.edgeCosts.stream().reduce(0.0, Double::sum);
    }

    private final Collection<Collection<Object>> nodesAttributes;
    private final Collection<Collection<Object>> edgesAttributes;
    private final Collection<Double>             edgeCosts;
}
