/*
 * The MIT License (MIT)
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

package com.rgi.store.routingnetworks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a network node
 *
 * @author Luke Lambert
 */
public class Node
{
    /**
     * Constructor
     *
     * @param identifier  Unique node identifier
     * @param x           Horizontal part of the node's coordinate
     * @param y           Vertical part of the node's coordinate
     * @param elevation   Z part of the node's coordinate
     * @param attributes  List of other attributes associated with the node
     */
    public Node(final int          identifier,
                final double       x,
                final double       y,
                final Double       elevation,
                final List<Object> attributes)
    {
        this.identifier = identifier;
        this.x          = x;
        this.y          = y;
        this.elevation  = elevation;
        this.attributes = new ArrayList<>(attributes);
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public Double getElevation()
    {
        return this.elevation;
    }

    public List<Object> getAttributes()
    {
        return Collections.unmodifiableList(this.attributes);
    }

    private final int          identifier;
    private final double       x;
    private final double       y;
    private final Double       elevation;
    private final List<Object> attributes;
}
