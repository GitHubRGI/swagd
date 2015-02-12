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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import com.rgi.common.BoundingBox;
import com.rgi.common.Dimensions;
import com.rgi.common.coordinate.Coordinate;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileMatrixSet;
import com.rgi.geopackage.tiles.TileSet;

public class GeoPackageReader extends GeoPackageTileStore implements TileStoreReader
{
    public GeoPackageReader(final GeoPackage geoPackage, final TileSet tileSet) throws SQLException
    {
        super(geoPackage, tileSet);

        this.tileMatricies = geoPackage.tiles()
                                       .getTileMatrices(tileSet)
                                       .stream()
                                       .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                 tileMatrix -> tileMatrix));
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        // TODO lazy precalculation ?
        try
        {
            final TileMatrixSet tileMatrixSet = this.geoPackage.tiles().getTileMatrixSet(this.tileSet);
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

    @Override
    public Stream<TileHandle> stream()
    {
        try
        {
            return this.geoPackage
                       .tiles()
                       .getTiles(this.tileSet)
                       .map(tileCoordinate -> this.getTileHandle(tileCoordinate.getZoomLevel(),
                                                                 tileCoordinate.getColumn(),
                                                                 tileCoordinate.getRow()));
        }
        catch(final SQLException ex)
        {
            return Stream.empty();
        }
    }

    @Override
    public String getImageType()
    {
        final TileHandle tile = this.stream().findFirst().orElse(null);

        if(tile != null)
        {
            try
            {
                final BufferedImage image = tile.getImage();
                if(image != null)
                {
                    final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(image);

                    if(imageReaders.hasNext())
                    {
                        final ImageReader imageReader = imageReaders.next();

                        final String[] names = imageReader.getOriginatingProvider().getFormatNames();
                        if(names != null && names.length > 0)
                        {
                            return names[0];
                        }
                    }
                }
            }
            catch(final TileStoreException ex)
            {
                // Fall through to return null
            }
        }

        return null;
    }

    @Override
    public Dimensions getImageDimensions()
    {
        final TileHandle tile = this.stream().findFirst().orElse(null);

        if(tile == null)
        {
            return null;
        }

        try
        {
            final BufferedImage image = tile.getImage();
            return new Dimensions(image.getHeight(), image.getWidth());
        }
        catch(final TileStoreException ex)
        {
            return null;
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

    private TileHandle getTileHandle(final int zoomLevel, final int column, final int row)
    {
        final TileMatrix tileMatrix = GeoPackageReader.this.tileMatricies.get(zoomLevel);
        final TileMatrixDimensions matrix = new TileMatrixDimensions(tileMatrix.getMatrixHeight(), tileMatrix.getMatrixWidth());

        return new TileHandle()
               {
                    @Override
                    public int getZoomLevel()
                    {
                        return zoomLevel;
                    }

                    @Override
                    public int getColumn()
                    {
                        return column;
                    }

                    @Override
                    public int getRow()
                    {
                        return row;
                    }

                    @Override
                    public TileMatrixDimensions getMatrix() throws TileStoreException
                    {
                        return matrix;
                    }

                    @Override
                    public BoundingBox getBounds() throws TileStoreException
                    {
                        final Coordinate<Double> upperLeft  = GeoPackageReader.this.crsProfile.tileToCrsCoordinate(row,   column,   matrix, GeoPackageTiles.Origin);
                        final Coordinate<Double> lowerRight = GeoPackageReader.this.crsProfile.tileToCrsCoordinate(row+1, column+1, matrix, GeoPackageTiles.Origin);

                        return new BoundingBox(lowerRight.getY(),
                                               upperLeft.getX(),
                                               upperLeft.getY(),
                                               lowerRight.getX());
                    }

                    @Override
                    public BufferedImage getImage() throws TileStoreException
                    {
                        return GeoPackageReader.this.getTile(row, column, zoomLevel);
                    }
               };
    }

    private final Map<Integer, TileMatrix> tileMatricies;
}
