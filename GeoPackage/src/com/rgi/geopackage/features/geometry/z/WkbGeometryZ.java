package com.rgi.geopackage.features.geometry.z;

import com.rgi.geopackage.features.geometry.Geometry;

/**
 * @author Luke Lambert
 */
public abstract class WkbGeometryZ extends Geometry
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

    public abstract EnvelopeZ createEnvelopeZ();

    public static final long GeometryTypeDimensionalityBase = 1000;
}
