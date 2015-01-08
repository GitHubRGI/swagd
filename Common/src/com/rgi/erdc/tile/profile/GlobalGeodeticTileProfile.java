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

package com.rgi.erdc.tile.profile;

import com.rgi.erdc.BoundingBox;
import com.rgi.erdc.CoordinateReferenceSystem;
import com.rgi.erdc.Dimension2D;
import com.rgi.erdc.coordinates.AbsoluteTileCoordinate;
import com.rgi.erdc.coordinates.Coordinate;
import com.rgi.erdc.coordinates.CrsTileCoordinate;
import com.rgi.erdc.tile.TileOrigin;

/**
 * @author Luke Lambert
 *
 */
public class GlobalGeodeticTileProfile extends TileProfile
{
    /**
     * Constructor
     */
    public GlobalGeodeticTileProfile()
    {
        super(7);
    }

    @Override
    public AbsoluteTileCoordinate crsToAbsoluteTileCoordinate(final Coordinate<Double> coordinate, final int zoomLevel, final TileOrigin origin)
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Meter coordinate may not be null");
        }

        if(zoomLevel < 0 || zoomLevel > 31)
        {
            throw new IllegalArgumentException("Zoom level must be in the range [0, 32)");
        }

        if(origin == null)
        {
            throw new IllegalArgumentException("Origin may not be null");
        }

        final double tileSubdivision = Math.pow(2.0, zoomLevel);

        // Round off the fractional tile
        return new AbsoluteTileCoordinate((int)((coordinate.getY() + Bounds.getHeight()/2.0) / Bounds.getHeight() * tileSubdivision),
                                          (int)((coordinate.getX() + Bounds.getWidth() /2.0) / Bounds.getWidth()  * tileSubdivision),
                                          zoomLevel,
                                          TileOrigin.LowerLeft).transform(origin);
    }

    @Override
    public CrsTileCoordinate absoluteToCrsCoordinate(final AbsoluteTileCoordinate absoluteTileCoordinate)
    {
        if(absoluteTileCoordinate == null)
        {
            throw new IllegalArgumentException("Tile coordinate may not be null");
        }

        final double tileSubdivision = Math.pow(2.0, absoluteTileCoordinate.getZoomLevel());

        return new CrsTileCoordinate((absoluteTileCoordinate.getY() * Bounds.getHeight() / tileSubdivision) - Bounds.getHeight() / 2.0,
                                     (absoluteTileCoordinate.getX() * Bounds.getWidth()  / tileSubdivision) - Bounds.getWidth()  / 2.0,
                                     absoluteTileCoordinate.getZoomLevel(),
                                     this.getCoordinateReferenceSystem());
    }

    @Override
    public Dimension2D getTileDimensions(final int zoomLevel)
    {
        final double height = GlobalGeodeticTileProfile.Bounds.getHeight() / Math.pow(2.0, zoomLevel);
        final double width  = GlobalGeodeticTileProfile.Bounds.getWidth()  / Math.pow(2.0, zoomLevel);

        return new Dimension2D(height, width);
    }

	@Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return SphericalMercatorTileProfile.CoordinateReferenceSystem;
    }

	public static Coordinate<Double> coordinateToGeographic(final Coordinate<Double> coordinate)
    {
        return coordinate;
    }

	@Override
    public BoundingBox getBounds()
    {
        return GlobalGeodeticTileProfile.Bounds;
    }

    public final static CoordinateReferenceSystem CoordinateReferenceSystem = new CoordinateReferenceSystem("EPSG", 3857);

    /**
     * The Unprojected bounds of this tile profile, in degrees
     *
     * TODO: what defines the bounds?  The information doesn't seem to be specified in the datum
     */
    public static final BoundingBox Bounds = new BoundingBox(-90.0, -180.0, 90.0, 180.0);
}
