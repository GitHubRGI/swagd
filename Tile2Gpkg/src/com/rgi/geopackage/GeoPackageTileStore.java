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

package com.rgi.geopackage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.CoordinateReferenceSystem;
import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.profile.TileProfile;
import com.rgi.common.tile.profile.TileProfileFactory;
import com.rgi.common.tile.store.TileStore;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageTileStore implements TileStore
{
    public GeoPackageTileStore(final GeoPackage geoPackage,
                               final TileSet    tileSet) throws SQLException
    {
        if(geoPackage == null)
        {
            throw new IllegalArgumentException("GeoPackage may not be null");
        }

        if(tileSet == null)
        {
            throw new IllegalArgumentException("Tile set may not be null or empty");
        }

        final SpatialReferenceSystem srs = geoPackage.core().getSpatialReferenceSystem(tileSet.getSpatialReferenceSystemIdentifier());

        this.geoPackage                = geoPackage;
        this.tileSet                   = tileSet;
        this.coordinateReferenceSystem = new CoordinateReferenceSystem(srs.getOrganization(), srs.getOrganizationSrsId());
        this.tileProfile               = TileProfileFactory.create(this.coordinateReferenceSystem);

        this.tileMatricies = geoPackage.tiles()
                                       .getTileMatrices(tileSet)
                                       .stream()
                                       .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                 tileMatrix -> tileMatrix));
    }

    @Override
    public BoundingBox calculateBounds() throws TileStoreException
    {
        try
        {
            return this.geoPackage.tiles()
                                  .getTileMatrixSet(this.tileSet)
                                  .getBoundingBox();
        }
        catch(final Exception ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public long countTiles() throws TileStoreException
    {
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
    public long calculateSize() throws TileStoreException
    {
        return this.geoPackage.getFile().getTotalSpace();
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
            final com.rgi.geopackage.tiles.Tile tile = this.geoPackage.tiles().getTile(this.tileSet, coordinate, zoomLevel);

            if(tile == null)
            {
                return null;
            }

            try(ByteArrayInputStream imageInputStream = new ByteArrayInputStream(tile.getImageData()))
            {
                return ImageIO.read(imageInputStream);
            }
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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.getCoordinateReferenceSystem()))
        {
            throw new IllegalArgumentException("Coordinate's coordinate reference system does not match the tile store's coordinate reference system");
        }

        final String outputFormat = "PNG";  // TODO how do we want to pick this ?

        try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            if(!ImageIO.write(image, outputFormat, outputStream))
            {
                throw new TileStoreException(String.format("No appropriate image writer found for format '%s'", outputFormat));
            }

            TileMatrix tileMatrix = null;

            if(!this.tileMatricies.containsKey(zoomLevel))
            {
                tileMatrix = this.addTileMatrix(zoomLevel, image.getHeight(), image.getWidth());
                this.tileMatricies.put(zoomLevel, tileMatrix);
            }
            else
            {
                tileMatrix = this.tileMatricies.get(zoomLevel);
            }

            this.geoPackage
                .tiles()
                .addTile(this.tileSet,
                         tileMatrix,
                         coordinate,
                         zoomLevel,
                         outputStream.toByteArray());
        }
        catch(final Exception ex)
        {
           throw new TileStoreException(ex);
        }
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem()
    {
        return this.coordinateReferenceSystem;
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

    private TileMatrix addTileMatrix(final int zoomLevel, final int pixelHeight, final int pixelWidth) throws SQLException
    {
        final int tileDimension = (int)Math.pow(2.0, zoomLevel);    // Assumes zoom*2 convension, with 1 tile at zoom level 0

        final Dimension2D dimensions = this.tileProfile.getTileDimensions(zoomLevel);

        return this.geoPackage.tiles()
                              .addTileMatrix(this.tileSet,
                                             zoomLevel,
                                             tileDimension,
                                             tileDimension,
                                             pixelHeight,
                                             pixelWidth,
                                             dimensions.getHeight() / pixelHeight,
                                             dimensions.getWidth()  / pixelWidth);
    }

    private final GeoPackage                geoPackage;
    private final TileSet                   tileSet;
    private final CoordinateReferenceSystem coordinateReferenceSystem;
    private final TileProfile               tileProfile;
    private final Map<Integer, TileMatrix>  tileMatricies;
}
