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
        return true;
    }

    @Override
    public final boolean hasM()
    {
        return false;
    }

    /**
     * Creates an envelope with x, y, and z components
     *
     * @return an envelope with x, y, and z components
     */
    public abstract EnvelopeZ createEnvelopeZ();

    /**
     * Base type value for all geometries that extend this type
     */
    public static final long GeometryTypeDimensionalityBase = 1000;
}
