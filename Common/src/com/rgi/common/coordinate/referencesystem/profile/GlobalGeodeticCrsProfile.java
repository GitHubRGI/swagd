/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.common.coordinate.referencesystem.profile;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;

/**
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

    /**
     * @param coordinate coordinate in current Coordinate Reference System
     * @return coordinate in Global Geodetic
     */
    public static Coordinate<Double> coordinateToGeographic(final Coordinate<Double> coordinate)
    {
        return coordinate;
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

    private final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 4326);

    /**
     * The Unprojected bounds of this tile profile, in degrees
     *
     * TODO: what defines the bounds?  The information doesn't seem to be specified in the datum
     */
    public static final BoundingBox Bounds = new BoundingBox(-90.0, -180.0, 90.0, 180.0);
}
