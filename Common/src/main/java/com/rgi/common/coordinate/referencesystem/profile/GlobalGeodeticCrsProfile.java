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

package com.rgi.common.coordinate.referencesystem.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;

/**
 * Global Geodetic (WGS 84) implementation of a coordinate reference system
 * profile.  Global Geodetic is also known as World Geodetic System 84.
 *
 * @author Luke Lambert
 *
 */
public class GlobalGeodeticCrsProfile extends ProportionalCrsProfile
{

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return GlobalGeodeticCrsProfile.CoordinateReferenceSystem;
    }

    @Override
    public BoundingBox getBounds()
    {
        return GlobalGeodeticCrsProfile.Bounds;
    }

    @Override
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate)
    {
        return coordinate;
    }

    @Override
    public String getName()
    {
        return "World Geodetic System (WGS) 1984";
    }

    @Override
    public String getWellKnownText()
    {
        return "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]";
    }

    @Override
    public String getDescription()
    {
        return "World Geodetic System 1984";
    }

    @Override
    public int getPrecision()
    {
        return 7;
    }

    private final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);

    /**
     * The Unprojected bounds of this tile profile, in degrees
     *
     * TODO: what defines the bounds?  The information doesn't seem to be specified in the datum
     */
    public static final BoundingBox Bounds = new BoundingBox(-180.0, -90.0, 180.0, 90.0);
}
