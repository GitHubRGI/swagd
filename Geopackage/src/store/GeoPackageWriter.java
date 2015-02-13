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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.rgi.common.BoundingBox;
import com.rgi.common.coordinate.CoordinateReferenceSystem;
import com.rgi.common.coordinate.CrsCoordinate;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfile;
import com.rgi.common.coordinate.referencesystem.profile.CrsProfileFactory;
import com.rgi.common.tile.scheme.TileMatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.ImageUtility;
import com.rgi.common.util.MimeTypeUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.core.SpatialReferenceSystem;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;
import com.rgi.geopackage.verification.ConformanceException;

public class GeoPackageWriter implements AutoCloseable, TileStoreWriter
{
    /**
     * @param geoPackageFile
     *             Handle to a new or existing GeoPackage file
     * @param tileSetTableName
     *             Name for the new tile set's table in the GeoPackage database
     * @param tileSetIdentifier
     *             A human-readable identifier (e.g. short name) for the tile set
     * @param tileSetDescription
     *             A human-readable description of the tile set
     * @param boundingBox
     *             Minimum bounds of the tile set, in spatial reference system units
     * @param coordinateReferenceSystem
     *             Coordinate reference system of the tile set
     * @param crsName
     *             Name of the coordinate reference system
     * @param crsWktDefinition
     *             Well-known Text (WKT) representation of the coordinate reference system
     * @param crsDescription
     *             Human readable description of the coordinate reference system
     * @param tileScheme
     *             Contains the mechanism to calculate the relationship between the tile matrix dimensions at valid zoom levels
     * @param imageOutputFormat
     *             Image format for used for output
     * @param imageWriteOptions
     *             Controls details of the image writing process.  If null, a default ImageWriteParam used instead
     * @throws FileAlreadyExistsException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws ConformanceException
     */
    public GeoPackageWriter(final File                      geoPackageFile,
                            final String                    tileSetTableName,
                            final String                    tileSetIdentifier,
                            final String                    tileSetDescription,
                            final BoundingBox               tileSetBounds,
                            final CoordinateReferenceSystem coordinateReferenceSystem,
                            final String                    crsName,
                            final String                    crsWktDefinition,
                            final String                    crsDescription,
                            final TileScheme                tileScheme,
                            final MimeType                  imageOutputFormat,
                            final ImageWriteParam           imageWriteOptions) throws FileAlreadyExistsException, ClassNotFoundException, FileNotFoundException, SQLException, ConformanceException
    {
        this.geoPackage = new GeoPackage(geoPackageFile, OpenMode.OpenOrCreate);

        final SpatialReferenceSystem spatialReferenceSystem = this.geoPackage.core()
                                                                             .addSpatialReferenceSystem(crsName,
                                                                                                        coordinateReferenceSystem.getIdentifier(),
                                                                                                        coordinateReferenceSystem.getAuthority(),
                                                                                                        coordinateReferenceSystem.getIdentifier(),
                                                                                                        crsWktDefinition,
                                                                                                        crsDescription);
        this.tileSet = this.geoPackage.tiles()
                                      .addTileSet(tileSetTableName,
                                                  tileSetIdentifier,
                                                  tileSetDescription,
                                                  tileSetBounds,
                                                  spatialReferenceSystem);

        this.crsProfile = CrsProfileFactory.create(spatialReferenceSystem.getOrganization(),
                                                   spatialReferenceSystem.getOrganizationSrsId());

        if(imageOutputFormat == null)
        {
            throw new IllegalArgumentException("Image output format may not be null");
        }

        if(!MimeTypeUtility.contains(GeoPackageWriter.SupportedImageFormats, imageOutputFormat))
        {
            throw new IllegalArgumentException(String.format("Image output type '%s' is inappropriate for this tile store. Valid formats are: %s",
                                                             imageOutputFormat.toString(),
                                                             GeoPackageWriter.SupportedImageFormats
                                                                             .stream()
                                                                             .map(mimeType -> mimeType.toString())
                                                                             .collect(Collectors.joining(", ", "'", "'"))));
        }

        try
        {
            this.imageWriter = ImageIO.getImageWritersByMIMEType(imageOutputFormat.toString()).next();
        }
        catch(final NoSuchElementException ex)
        {
            throw new IllegalArgumentException(String.format("Mime type '%s' is not a supported for image writing by your Java environment", imageOutputFormat.toString()));
        }

        this.imageWriteOptions = imageWriteOptions != null ? imageWriteOptions
                                                           : this.imageWriter.getDefaultWriteParam();

        this.tileScheme = tileScheme;

        // TODO verify the origin is correct
        //new ZoomTimesTwo(0, 0, 1, 1, GeoPackageTiles.Origin);   // TODO fix me

        this.tileMatricies = this.geoPackage.tiles()
                                       .getTileMatrices(this.tileSet)
                                       .stream()
                                       .collect(Collectors.toMap(tileMatrix -> tileMatrix.getZoomLevel(),
                                                                 tileMatrix -> tileMatrix));
    }

    @Override
    public void close() throws Exception
    {
        this.geoPackage.close();
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
                         ImageUtility.bufferedImageToBytes(image, this.imageWriter, this.imageWriteOptions));
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

        if(!coordinate.getCoordinateReferenceSystem().equals(this.crsProfile.getCoordinateReferenceSystem()))
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
                         ImageUtility.bufferedImageToBytes(image, this.imageWriter, this.imageWriteOptions));
        }
        catch(final SQLException | IOException ex)
        {
           throw new TileStoreException(ex);
        }
    }

    @Override
    public Set<MimeType> getSupportedImageFormats()
    {
        return GeoPackageWriter.SupportedImageFormats;
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

    private TileMatrix addTileMatrix(final int zoomLevel, final int tilePixelHeight, final int tilePixelWidth) throws SQLException
    {
        final TileMatrixDimensions tileMatrixDimensions = this.tileScheme.dimensions(zoomLevel);

        final BoundingBox tileSetBounds = this.tileSet.getBoundingBox();

        return this.geoPackage.tiles()
                              .addTileMatrix(this.tileSet,
                                             zoomLevel,
                                             tileMatrixDimensions.getWidth(),
                                             tileMatrixDimensions.getHeight(),
                                             tilePixelHeight,
                                             tilePixelWidth,
                                             tileSetBounds.getWidth()  / tileMatrixDimensions.getWidth()  / tilePixelWidth,
                                             tileSetBounds.getHeight() / tileMatrixDimensions.getHeight() / tilePixelHeight);
    }

    private final GeoPackage               geoPackage;
    private final TileSet                  tileSet;
    private final CrsProfile               crsProfile;
    private final Map<Integer, TileMatrix> tileMatricies;
    private final ImageWriter              imageWriter;
    private final ImageWriteParam          imageWriteOptions;
    private final TileScheme               tileScheme;

    private static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet("image/jpeg", "image/png");
}
