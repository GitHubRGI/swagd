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

import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.Network;

/**
 * @author Luke Lambert
 *
 */
public class RoutingNetworkDescription
{
    /**
     * Constructor
     *
     * @param network
     *             Routing network being searched for the closest node
     * @param longitudeDescription
     *             Routing network attribute description for the horizontal
     *             portion of a node's coordinate
     * @param latitudeDescription
     *             Routing network attribute description for the vertical
     *             portion of a node's coordinate
     * @param elevationDescription
     *             Routing network attribute description for the elevation
     *             portion of a node's coordinate. This value may be null if
     *             the network is only in two dimensions
     * @param distanceDescription
     *             Routing network attribute description for the distance
     *             between an edge's two nodes
     */
    public RoutingNetworkDescription(final Network              network,
                                     final AttributeDescription longitudeDescription,
                                     final AttributeDescription latitudeDescription,
                                     final AttributeDescription elevationDescription,
                                     final AttributeDescription distanceDescription)
    {
        if(network == null)
        {
            throw new IllegalArgumentException("Network may not be null");
        }

        if(longitudeDescription == null)
        {
            throw new IllegalArgumentException("Longitude attribute description may not be null");
        }

        if(latitudeDescription == null)
        {
            throw new IllegalArgumentException("Latitude attribute description may not be null");
        }

        if(distanceDescription == null)
        {
            throw new IllegalArgumentException("Distance attribute description may not be null");
        }

        if(longitudeDescription.getAttributedType() != AttributedType.Node)
        {
            throw new IllegalArgumentException("The longitude attribute description must refer to a node");
        }

        if(latitudeDescription.getAttributedType() != AttributedType.Node)
        {
            throw new IllegalArgumentException("The latitude attribute description must refer to a node");
        }

        if(elevationDescription != null && elevationDescription.getAttributedType() != AttributedType.Node)
        {
            throw new IllegalArgumentException("The latitude attribute description must refer to a node");
        }

        if(distanceDescription.getAttributedType() != AttributedType.Edge)
        {
            throw new IllegalArgumentException("The distance attribute description must refer to an edge");
        }

        this.network              = network;
        this.longitudeDescription = longitudeDescription;
        this.latitudeDescription  = latitudeDescription;
        this.elevationDescription = elevationDescription;
        this.distanceDescription  = distanceDescription;
    }

    /**
     * @return the network
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /**
     * @return the longitudeDescription
     */
    public AttributeDescription getLongitudeDescription()
    {
        return this.longitudeDescription;
    }

    /**
     * @return the latitudeDescription
     */
    public AttributeDescription getLatitudeDescription()
    {
        return this.latitudeDescription;
    }

    /**
     * @return the elevationDescription
     */
    public AttributeDescription getElevationDescription()
    {
        return this.elevationDescription;
    }

    /**
     * @return the distanceDescription
     */
    public AttributeDescription getDistanceDescription()
    {
        return this.distanceDescription;
    }

    @Override
    public String toString()
    {
        return this.network.getIdentifier();
    }

    private final Network              network;
    private final AttributeDescription longitudeDescription;
    private final AttributeDescription latitudeDescription;
    private final AttributeDescription elevationDescription;
    private final AttributeDescription distanceDescription;
}
