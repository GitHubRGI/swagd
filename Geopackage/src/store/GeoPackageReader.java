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

package store;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrixSet;
import com.rgi.geopackage.tiles.TileSet;

public class GeoPackageReader extends GeoPackageTileStore implements TileStoreReader
{
    public GeoPackageReader(final GeoPackage geoPackage, final TileSet tileSet) throws SQLException
    {
        super(geoPackage, tileSet);
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        // TODO lazy precalculation ?
        try
        {
            TileMatrixSet tileMatrixSet = this.geoPackage.tiles().getTileMatrixSet(this.tileSet);
            if (tileMatrixSet == null)
            {
                throw new IllegalArgumentException("Tile Matrix Set cannot be null");
            }
            return tileMatrixSet.getBoundingBox();
        }
        catch(final Exception ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public long countTiles() throws TileStoreException
    {
        // TODO lazy precalculation ?
        try
        {
            return this.geoPackage.core().getRowCount(this.tileSet);
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public long getByteSize() throws TileStoreException
    {
        // TODO lazy precalculation ?
        return this.geoPackage.getFile().getTotalSpace();
    }

    @Override
    public BufferedImage getTile(final int row, final int column, final int zoomLevel) throws TileStoreException
    {
        try
        {
            return getImage(this.geoPackage
                                .tiles()
                                .getTile(this.tileSet,
                                         new RelativeTileCoordinate(row, column, zoomLevel)));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public BufferedImage getTile(final CrsCoordinate coordinate, final int zoomLevel) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        try
        {
            return getImage(this.geoPackage.tiles().getTile(this.tileSet, coordinate, zoomLevel));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.crsProfile.getCoordinateReferenceSystem();
    }

    @Override
    public Set<Integer> getZoomLevels() throws TileStoreException
    {
        try
        {
            return this.geoPackage.tiles()
                                  .getTileZoomLevels(this.tileSet);
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    private static BufferedImage getImage(final Tile tile) throws TileStoreException
    {
        if(tile == null)
        {
            return null;
        }

        try
        {
            return ImageUtility.bytesToBufferedImage(tile.getImageData());
        }
        catch(final IOException ex)
        {
            throw new TileStoreException(ex);
        }
    }
}
