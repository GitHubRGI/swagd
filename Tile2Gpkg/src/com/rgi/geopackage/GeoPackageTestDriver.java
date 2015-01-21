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
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import com.rgi.common.BoundingBox;
import com.rgi.common.tile.profile.GlobalGeodeticTileProfile;
import com.rgi.common.tile.scheme.MatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TmsTileStore;
import com.rgi.geopackage.tiles.GeoPackageTiles;
import com.rgi.geopackage.tiles.RelativeTileCoordinate;
import com.rgi.geopackage.tiles.TileMatrix;
import com.rgi.geopackage.tiles.TileSet;

/**
 * A simple driver to test changes of the GeoPackage API implementation.  Not intended for release or production use.
 *
 * @author Luke Lambert
 *
 */
public class GeoPackageTestDriver
{
    public static void main(final String[] args)
    {
        GeoPackageTestDriver.createGeoPackageFromFolder(new File("C:/Users/corp/Desktop/geopackage sample data/NormalZoom2"),    new ZoomTimesTwo(1, 31, 1, 1, GeoPackageTiles.Origin));
        // This won't work until I figure out how I want to set up a tile store with a tile scheme
        //GeoPackageTestDriver.createGeoPackageFromFolder(new File("C:/Users/corp/Desktop/geopackage sample data/NotNormalZoom2"), new ZoomTimesTwo(2, 3, GeoPackageTiles.Origin));
    }

    private static void createGeoPackageFromFolder(final File directory, final TileScheme tileScheme)
    {
        if(directory == null || !directory.isDirectory() || !directory.exists())
        {
            throw new IllegalArgumentException("Directory cannot be null, it must specify a directory, and it must exist");
        }

        final String folderName = directory.toPath().getFileName().toString();

        final File gpkgfFile = new File(String.format("%s/%s.gpkg",
                                                 directory,
                                                 folderName));

        if(gpkgfFile.exists())
        {
            if(!gpkgfFile.delete())
            {
                throw new RuntimeException("Unable to delete old gpkg");
            }
        }

        try
        {
            try(final GeoPackage gpkg = new GeoPackage(gpkgfFile))
            {
                final TmsTileStore tmsTileStore = new TmsTileStore(new GlobalGeodeticTileProfile(),
                                                                   directory.toPath());

                final BoundingBox boundingBox = new BoundingBox(0.0, 0.0, 90.0, 180.0);

                final TileSet tileSet = gpkg.tiles()
                                            .addTileSet(folderName,
                                                        "test tiles",
                                                        String.format("GeoPackage with numbered test tiles, %dx%d tiles at zoom 0, and a \"zoom times two\" convention for subsequent levels.",
                                                                      tileScheme.dimensions(0).getHeight(),
                                                                      tileScheme.dimensions(0).getWidth()),
                                                        boundingBox,
                                                        gpkg.core()
                                                            .getSpatialReferenceSystem(tmsTileStore.getCoordinateReferenceSystem()
                                                                                                   .getIdentifier()));

                final int pixelWidth  = 256;
                final int pixelHeight = 256;

                final String outputFormat = "JPG";

                for(final int zoomLevel : tmsTileStore.getZoomLevels())
                {
                    final MatrixDimensions matrixDimensions = tileScheme.dimensions(zoomLevel);

                    final TileMatrix tileMatrix = gpkg.tiles()
                                                      .addTileMatrix(tileSet,
                                                                     zoomLevel,
                                                                     matrixDimensions.getWidth(),
                                                                     matrixDimensions.getHeight(),
                                                                     pixelWidth,
                                                                     pixelHeight,
                                                                     (boundingBox.getWidth()  / matrixDimensions.getWidth())  / pixelWidth,
                                                                     (boundingBox.getHeight() / matrixDimensions.getHeight()) / pixelHeight);

                    for(int row = 0; row < matrixDimensions.getHeight(); ++row)
                    {
                        for(int column = 0; column < matrixDimensions.getWidth(); ++column)
                        {
                            final BufferedImage image = tmsTileStore.getTile(row, column, zoomLevel);

                            if(image != null)
                            {
                                try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
                                {
                                    if(!ImageIO.write(image, outputFormat, outputStream))
                                    {
                                        System.err.format("No appropriate image writer found for format '%s'", outputFormat);
                                    }



                                    gpkg.tiles().addTile(tileSet,
                                                         tileMatrix,
                                                         new RelativeTileCoordinate(row,
                                                                                    column,
                                                                                    zoomLevel),
                                                         outputStream.toByteArray());
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(final Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
