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
     * @param network
     * @param longitudeDescription
     * @param latitudeDescription
     * @param distanceDescription
     */
    public RoutingNetworkDescription(final Network              network,
                                     final AttributeDescription longitudeDescription,
                                     final AttributeDescription latitudeDescription,
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

        if(distanceDescription.getAttributedType() != AttributedType.Edge)
        {
            throw new IllegalArgumentException("The distance attribute description must refer to an edge");
        }

        this.network              = network;
        this.longitudeDescription = longitudeDescription;
        this.latitudeDescription  = latitudeDescription;
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
     * @return the distanceDescription
     */
    public AttributeDescription getDistanceDescription()
    {
        return this.distanceDescription;
    }

    private final Network              network;
    private final AttributeDescription longitudeDescription;
    private final AttributeDescription latitudeDescription;
    private final AttributeDescription distanceDescription;
}
