package com.rgi.geopackage.features.geometry;

import com.rgi.geopackage.features.WellKnownBinaryFormatException;

import java.nio.ByteBuffer;

/**
 * @author Luke Lambert
 */
@FunctionalInterface
public interface GeometryFactory
{
    Geometry create(final ByteBuffer byteBuffer) throws WellKnownBinaryFormatException;
}
