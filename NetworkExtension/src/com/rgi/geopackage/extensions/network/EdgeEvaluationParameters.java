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

import com.rgi.geopackage.extensions.network.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author  Luke Lambert
 */
public class EdgeEvaluationParameters
{
    public EdgeEvaluationParameters(final Node         from,
                                    final Node         to,
                                    final List<Object> edgeAttributes)
    {
        this.from           = from;
        this.to             = to;
        this.edgeAttributes = new ArrayList<>(edgeAttributes);
    }

    public Node getFrom()
    {
        return this.from;
    }

    public Node getTo()
    {
        return this.to;
    }

    public List<Object> getEdgeAttributes()
    {
        return Collections.unmodifiableList(this.edgeAttributes);
    }

    private final Node         from;
    private final Node         to;
    private final List<Object> edgeAttributes;
}