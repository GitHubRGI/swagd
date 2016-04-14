package com.rgi.geopackage.features.geometry.m;

import com.rgi.geopackage.features.geometry.Geometry;
import com.rgi.geopackage.features.geometry.z.EnvelopeZ;

/**
 * @author Luke Lambert
 */
public abstract class WkbGeometryM extends Geometry
{
    @Override
    public final boolean hasZ()
    {
        return false;
    }

    @Override
    public final boolean hasM()
    {
        return true;
    }

    public abstract EnvelopeM createEnvelopeM();

    public static final long GeometryTypeDimensionalityBase = 2000;
}
