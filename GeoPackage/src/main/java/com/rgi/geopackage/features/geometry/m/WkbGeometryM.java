package com.rgi.geopackage.features.geometry.m;

import com.rgi.geopackage.features.geometry.Geometry;

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

    /**
     * Creates an envelope with x, y, and m components
     *
     * @return an envelope with x, y, and m components
     */
    public abstract EnvelopeM createEnvelopeM();

    /**
     * Base type value for all geometries that extend this type
     */
    public static final long GeometryTypeDimensionalityBase = 2000;
}
