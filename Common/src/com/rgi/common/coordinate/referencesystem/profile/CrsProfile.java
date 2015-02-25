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
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;

/**
 * A collection of metadata for a specific coordinate reference system, and
 * methods used to interact with tiling systems.
 *
 * @author Luke Lambert
 *
 */
public interface CrsProfile
{
    /**
     * Returns the bounds of the world in the units of the coordinate reference
     * system.
     *
     * @return The bounds of the world in the units of the coordinate reference system
     */
    public BoundingBox getBounds();

    /**
     * Determines what tile the coordinate lies in
     *
     * @param coordinate
     *             Coordinate in the same reference system as this tile profile
     * @param bounds
     *             The area, in the units of the coordinate reference system,
     *             that represents the valid area for tiling numbering (bounds
     *             of the tile matrix dimensions)
     * @param dimensions
     *             Height and width of the tile matrix
     * @param tileOrigin
     *             Specifies where tile (0, 0) is in the tile matrix
     * @return Returns the tile that the coordinate corresponds to
     */
    public Coordinate<Integer> crsToTileCoordinate(final CrsCoordinate        coordinate,
                                                   final BoundingBox          bounds,
                                                   final TileMatrixDimensions dimensions,
                                                   final TileOrigin           tileOrigin);

    /**
     * Determines the coordinate, in the units of the coordinate reference
     * system, for the specified tile. The tile origin specifies corner of the
     * the tile that will be represented by the coordinate.
     *
     * @param column
     *             Horizontal portion of the tile's coordinate
     * @param row
     *             Vertical portion of the tile's coordinate
     * @param bounds
     *             The area, in the units of the coordinate reference system,
     *             that represents the valid area for tiling numbering (bounds
     *             of the tile matrix dimensions)
     * @param dimensions
     *             Height and width of the tile matrix
     * @param tileOrigin
     *             Specifies where tile (0, 0) is in the tile matrix
     *
     * @return Returns the coordinate that the tile corresponds to
     */
    public CrsCoordinate tileToCrsCoordinate(final int                  column,
                                             final int                  row,
                                             final BoundingBox          bounds,
                                             final TileMatrixDimensions dimensions,
                                             final TileOrigin           tileOrigin);

    /**
     * @return Returns the {@link CoordinateReferenceSystem} object that
     * corresponds to this profile
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * @return Returns the name of the coordinate reference system that
     * corresponds to this profile (e.g. "Web Mercator")
     */
    public String getName();

    /**
     * @return Returns the well known text (<a
     * href=http://www.opengeospatial.org/standards/sfa">WKT</a>) defined by
     * <a href="http://www.opengeospatial.org/">Open Geospatial Consortium</a>
     * for the coordinate reference system that corresponds to this profile
     */
    public String getWellKnownText();

    /**
     * @return Returns a human readable description for the coordinate
     * reference system that corresponds to this profile
     */
    public String getDescription();

    /**
     * Transform a coordinate from the coordinate reference system of this
     * profile to Global Geodetic (EPSG:4326)
     * <br>
     * <b>This is a temporary stopgap</b> implemented in lieu of a general
     * coordinate transformation mechanism. This method will be deprecated
     * and removed in future releases.
     *
     * @param coordinate
     *             Coordinate in the same reference system as this profile
     * @return Returns the equivalent coordinate in the global geodetic
     * coordinate reference system
     */
    public Coordinate<Double> toGlobalGeodetic(final Coordinate<Double> coordinate);

    /**
     * The maximum acceptable number of decimal places to be used in coordinate
     * comparison
     *
     * @return Returns the number of decimal places
     */
    public int getPrecision();
}
