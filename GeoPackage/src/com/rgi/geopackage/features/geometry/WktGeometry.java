package com.rgi.geopackage.features.geometry;

/**
 * @author Luke Lambert
 */
public abstract class WktGeometry extends Geometry
{
    @Override
    public final boolean hasZ()
    {
        return false;
    }

    @Override
    public final boolean hasM()
    {
        return false;
    }
}
