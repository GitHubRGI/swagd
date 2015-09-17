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

import java.util.Collections;
import java.util.List;

/**
 * @author Luke Lambert
 */
public class Route
{
    public Route(final List<List<Object>> nodesAttributes,
                 final List<List<Object>> edgesAttributes,
                 final List<Integer>      edgeIdentifiers,
                 final List<Double>       edgeCosts)
    {
        this.nodesAttributes = nodesAttributes;
        this.edgesAttributes = edgesAttributes;
        this.edgeIdentifiers = edgeIdentifiers;
        this.edgeCosts       = edgeCosts;
    }

    public List<List<Object>> getNodesAttributes()
    {
        return Collections.unmodifiableList(this.nodesAttributes);
    }

    public List<Integer> getEdgeIdentifiers()
    {
        return Collections.unmodifiableList(this.edgeIdentifiers);
    }

    public List<List<Object>> getEdgesAttributes()
    {
        return Collections.unmodifiableList(this.edgesAttributes);
    }

    public List<Double> getEdgeCosts()
    {
        return Collections.unmodifiableList(this.edgeCosts);
    }

    public double getTotalCost()
    {
        return this.edgeCosts.stream().mapToDouble(Double::doubleValue).sum();
    }

    private final List<List<Object>> nodesAttributes;
    private final List<Integer>      edgeIdentifiers;
    private final List<List<Object>> edgesAttributes;
    private final List<Double>       edgeCosts;
}
