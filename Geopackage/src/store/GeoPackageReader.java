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
import java.io.File;
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
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.TileOrigin;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.store.TileHandle;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.util.ImageUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.Tile;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileMatrixSet;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;

/**
 * @author Luke Lambert
 *
 */
public class GeoPackageReader implements AutoCloseable, TileStoreReader
{
    /**
     * @param geoPackageFile
     *            Handle to a new or existing GeoPackage file
     * @param tileSetTableName
     *            Name for the new tile set's table in the GeoPackage database
     * @throws ClassNotFoundException
     *             when the SQLite JDBC driver cannot be found
     * @throws ConformanceException
     *             when the verifyConformance parameter is true, and if there
     *             are any conformance violations with the severity
     *             Severity.Error
     * @throws IOException
     *             when openMode is set to OpenMode.Create, and the file already
     *             exists, openMode is set to OpenMode.Open, and the file does
     *             not exist, or if there is a file read error
     * @throws SQLException
     *             in various cases where interaction with the JDBC connection
     *             fails
     */
    public GeoPackageReader(final File geoPackageFile, final String tileSetTableName) throws ClassNotFoundException, ConformanceException, IOException, SQLException
    {
        if(geoPackageFile == null)
        {
            throw new IllegalArgumentException("GeoPackage file may not be null");
        }

        if(tileSetTableName == null)
        {
            throw new IllegalArgumentException("Tile set may not be null or empty");
        }

        this.geoPackage = new GeoPackage(geoPackageFile, OpenMode.Open);

        try
        {
            this.tileSet    = this.geoPackage.tiles().getTileSet(tileSetTableName);

            if(this.tileSet == null)
            {
                throw new IllegalArgumentException("Table name does not specify a valid GeoPackage tile set");
            }

            final SpatialReferenceSystem srs = this.geoPackage.core().getSpatialReferenceSystem(this.tileSet.getSpatialReferenceSystemIdentifier());

            if(srs == null)
            {
                throw new IllegalArgumentException("SRS may not be null");
            }

            this.crsProfile = CrsProfileFactory.create(srs.getOrganization(), srs.getOrganizationSrsId());

            this.zoomLevels = this.geoPackage.tiles().getTileZoomLevels(this.tileSet);

            this.tileMatrixSet = this.geoPackage.tiles().getTileMatrixSet(this.tileSet);

            this.tileMatricies = this.geoPackage.tiles()
                                           .getTileMatrices(this.tileSet)
                                           .stream()
                                           .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                     tileMatrix -> tileMatrix));

            this.tileScheme = zoomLevel -> { if(GeoPackageReader.this.tileMatricies.containsKey(zoomLevel))
                                             {
                                                 final TileMatrix tileMatrix = GeoPackageReader.this.tileMatricies.get(zoomLevel);
                                                 return new TileMatrixDimensions(tileMatrix.getMatrixWidth(), tileMatrix.getMatrixHeight());
                                             }

                                             return new TileMatrixDimensions(0, 0);
                                           };
        }
        catch(final IllegalArgumentException | SQLException ex)
        {
            this.close();
            throw ex;
        }
    }

    @Override
    public void close() throws SQLException
    {
        this.geoPackage.close();
    }

    @Override
    public BoundingBox getBounds() throws TileStoreException
    {
        return this.tileMatrixSet.getBoundingBox();
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
    public BufferedImage getTile(final int column, final int row, final int zoomLevel) throws TileStoreException
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
            return getImage(this.geoPackage
                                .tiles()
                                .getTile(this.tileSet,
                                         coordinate,
                                         this.crsProfile.getPrecision(),
                                         zoomLevel));
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
        return this.zoomLevels;
    }

    @Override
    public Stream<TileHandle> stream() throws TileStoreException
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
            throw new TileStoreException(ex);
        }
    }

    @Override
    public Stream<TileHandle> stream(final int zoomLevel) throws TileStoreException
    {
        try
        {
            return this.geoPackage
                       .tiles()
                       .getTiles(this.tileSet, zoomLevel)
                       .map(tileCoordinate -> this.getTileHandle(tileCoordinate.getZoomLevel(),
                                                                 tileCoordinate.getColumn(),
                                                                 tileCoordinate.getRow()));
        }
        catch(final SQLException ex)
        {
            throw new TileStoreException(ex);
        }
    }

    @Override
    public String getImageType() throws TileStoreException
    {
        final TileHandle tile = this.stream().findFirst().orElse(null);

        if(tile != null)
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

        return null;
    }

    @Override
    public Dimensions<Integer> getImageDimensions() throws TileStoreException
    {
        final TileHandle tile = this.stream().findFirst().orElse(null);

        if(tile != null)
        {
            final BufferedImage image = tile.getImage();
            return new Dimensions<>(image.getWidth(), image.getHeight());
        }

        return null;
    }

    @Override
    public String getName()
    {
        return String.format("%s-%s",
                             this.geoPackage.getFile().getName(),
                             this.tileSet.getIdentifier());
    }

    @Override
    public TileScheme getTileScheme()
    {
        return this.tileScheme;
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
        final TileMatrixDimensions matrix = new TileMatrixDimensions(tileMatrix.getMatrixWidth(), tileMatrix.getMatrixHeight());

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
                    public CrsCoordinate getCrsCoordinate() throws TileStoreException
                    {
                        return GeoPackageReader.this
                                               .crsProfile
                                               .tileToCrsCoordinate(column,
                                                                    row,
                                                                    this.getBounds(),
                                                                    matrix,
                                                                    GeoPackageTiles.Origin);
                    }

                    @Override
                    public CrsCoordinate getCrsCoordinate(final TileOrigin corner) throws TileStoreException
                    {
                        return GeoPackageReader.this
                                               .crsProfile
                                               .tileToCrsCoordinate(column + corner.getHorizontal(),
                                                                    row    - (1 - corner.getVertical()),    // same as: column - (GeoPackageTiles.Origin.getVertical() - corner.getHorizontal()) because GeoPackageTiles.Origin.getVertical() is always 0
                                                                    this.getBounds(),
                                                                    matrix,
                                                                    GeoPackageTiles.Origin);
                    }

                    @Override
                    public BoundingBox getBounds() throws TileStoreException
                    {
                        final Coordinate<Double> upperLeft  = GeoPackageReader.this.crsProfile.tileToCrsCoordinate(column,   row,   this.getBounds(), matrix, GeoPackageTiles.Origin);
                        final Coordinate<Double> lowerRight = GeoPackageReader.this.crsProfile.tileToCrsCoordinate(column+1, row+1, this.getBounds(), matrix, GeoPackageTiles.Origin);

                        return new BoundingBox(upperLeft.getX(),
                                               lowerRight.getY(),
                                               lowerRight.getX(),
                                               upperLeft.getY());
                    }

                    @Override
                    public BufferedImage getImage() throws TileStoreException
                    {
                        return GeoPackageReader.this.getTile(column, row, zoomLevel);
                    }


               };
    }

    protected final GeoPackage   geoPackage;
    protected final TileSet      tileSet;
    protected final CrsProfile   crsProfile;
    protected final TileScheme   tileScheme;
    protected final Set<Integer> zoomLevels;

    private final Map<Integer, TileMatrix> tileMatricies;
    private final TileMatrixSet tileMatrixSet;
}
