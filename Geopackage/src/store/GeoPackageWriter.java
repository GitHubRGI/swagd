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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import com.rgi.common.Dimension2D;
import com.rgi.common.coordinates.CrsCoordinate;
import com.rgi.common.tile.scheme.MatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.common.util.ImageUtility;
import com.rgi.common.util.MimeTypeUtility;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;

public class GeoPackageWriter extends GeoPackageTileStore implements TileStoreWriter
{
    /**
     * Constructor
     *
     * @param geoPackage
     *             Tile container
     * @param tileSet
     *             Specific set that tiles will associated with
     * @param imageOutputFormat
     *             Image format for used for output
     * @throws SQLException
     */
    public GeoPackageWriter(final GeoPackage geoPackage,
                            final TileSet    tileSet,
                            final MimeType   imageOutputFormat) throws SQLException
    {
        this(geoPackage, tileSet, imageOutputFormat, null);
    }

    /**
     * Constructor
     *
     * @param geoPackage
     *             Tile container
     * @param tileSet
     *             Specific set that tiles will associated with
     * @param imageOutputFormat
     *             Image format for used for output
     * @param imageWriteOptions
     *             Controls details of the image writing process.  If null, a default ImageWriteParam used instead
     * @throws SQLException
     */
    public GeoPackageWriter(final GeoPackage      geoPackage,
                            final TileSet         tileSet,
                            final MimeType        imageOutputFormat,
                            final ImageWriteParam imageWriteOptions) throws SQLException
    {
        super(geoPackage, tileSet);

        if(imageOutputFormat == null)
        {
            throw new IllegalArgumentException("Image output format may not be null");
        }

        if(!GeoPackageWriter.SupportedImageFormats.contains(imageOutputFormat))
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

    private TileMatrix addTileMatrix(final int zoomLevel, final int pixelHeight, final int pixelWidth) throws SQLException
    {
        final MatrixDimensions matrixDimensions = this.tileScheme.dimensions(zoomLevel);

        final Dimension2D dimensions = this.crsProfile.getTileDimensions(zoomLevel);

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
    private final ImageWriter              imageWriter;
    private final ImageWriteParam          imageWriteOptions;
    private final TileScheme               tileScheme;

    private static final Set<MimeType> SupportedImageFormats = MimeTypeUtility.createMimeTypeSet("image/jpeg", "image/png");
}
