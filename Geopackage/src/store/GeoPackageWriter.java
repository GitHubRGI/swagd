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
import java.util.Map;
import java.util.stream.Collectors;

import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.scheme.MatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;

public class GeoPackageWriter extends GeoPackageTileStore implements TileStoreWriter
{
    public GeoPackageWriter(final GeoPackage geoPackage, final TileSet tileSet) throws SQLException
    {
        super(geoPackage, tileSet);

        this.tileScheme = new ZoomTimesTwo(-1, -2, 1, 1, GeoPackageTiles.Origin);   // TODO fix me

        this.tileMatricies = geoPackage.tiles()
                                       .getTileMatrices(tileSet)
                                       .stream()
                                       .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                 tileMatrix -> tileMatrix));
    }

    @Override
    public void addTile(final int row, final int column, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        try
        {
            this.geoPackage
                .tiles()
                .addTile(this.tileSet,
                         this.getTileMatrix(zoomLevel, image.getHeight(), image.getWidth()),
                         new RelativeTileCoordinate(row, column, zoomLevel),
                         ImageUtility.bufferedImageToBytes(image, OutputImageFormat));
        }
        catch(final SQLException | IOException ex)
        {
           throw new TileStoreException(ex);
        }
    }

    @Override
    public void addTile(final CrsCoordinate coordinate, final int zoomLevel, final BufferedImage image) throws TileStoreException
    {
        if(coordinate == null)
        {
            throw new IllegalArgumentException("Coordinate may not be null");
        }

        if(image == null)
        {
            throw new IllegalArgumentException("Image may not be null");
        }

        if(!coordinate.getCoordinateReferenceSystem().equals(this.tileProfile.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        try
        {
            this.geoPackage
                .tiles()
                .addTile(this.tileSet,
                         this.getTileMatrix(zoomLevel, image.getHeight(), image.getWidth()),
                         coordinate,
                         zoomLevel,
                         ImageUtility.bufferedImageToBytes(image, OutputImageFormat));
        }
        catch(final SQLException | IOException ex)
        {
           throw new TileStoreException(ex);
        }
    }

    private TileMatrix getTileMatrix(final int zoomLevel, final int imageHeight, final int imageWidth) throws SQLException
    {
        TileMatrix tileMatrix = null;

        if(!this.tileMatricies.containsKey(zoomLevel))
        {
            tileMatrix = this.addTileMatrix(zoomLevel, imageHeight, imageWidth);
            this.tileMatricies.put(zoomLevel, tileMatrix);
        }
        else
        {
            tileMatrix = this.tileMatricies.get(zoomLevel);
        }

        return tileMatrix;
    }

    private TileMatrix addTileMatrix(final int zoomLevel, final int pixelHeight, final int pixelWidth) throws SQLException
    {
        final MatrixDimensions matrixDimensions = this.tileScheme.dimensions(zoomLevel);

        final Dimension2D dimensions = this.tileProfile.getTileDimensions(zoomLevel);

        return this.geoPackage.tiles()
                              .addTileMatrix(this.tileSet,
                                             zoomLevel,
                                             matrixDimensions.getWidth(),
                                             matrixDimensions.getHeight(),
                                             pixelHeight,
                                             pixelWidth,
                                             dimensions.getWidth()  / pixelWidth,
                                             dimensions.getHeight() / pixelHeight);
    }

    private final Map<Integer, TileMatrix> tileMatricies;
    private final TileScheme               tileScheme;

    private static final String OutputImageFormat = "PNG";  // TODO how do we want to pick this ?
}
