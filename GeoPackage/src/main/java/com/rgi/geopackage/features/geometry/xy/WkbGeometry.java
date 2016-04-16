package com.rgi.geopackage.features.geometry.xy;

import com.rgi.geopackage.features.geometry.Geometry;

/**
 * @author Luke Lambert
 */
public abstract class WkbGeometry extends Geometry
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
