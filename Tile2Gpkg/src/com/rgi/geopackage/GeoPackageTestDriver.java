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

import java.io.File;

import com.rgi.common.BoundingBox;
import com.rgi.common.tile.profile.GlobalGeodeticTileProfile;
import com.rgi.common.tile.scheme.MatrixDimensions;
import com.rgi.common.tile.scheme.TileScheme;
import com.rgi.common.tile.scheme.ZoomTimesTwo;
import com.rgi.common.tile.store.TmsTileStore;
import com.rgi.geopackage.tiles.GeoPackageTiles;
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
        GeoPackageTestDriver.createGeoPackageFromFolder(new File("C:/Users/corp/Desktop/geopackage sample data/NormalZoom2"),    new ZoomTimesTwo(1, 1, GeoPackageTiles.Origin));
        GeoPackageTestDriver.createGeoPackageFromFolder(new File("C:/Users/corp/Desktop/geopackage sample data/NotNormalZoom2"), new ZoomTimesTwo(2, 3, GeoPackageTiles.Origin));
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

                final TileSet tileSet = gpkg.tiles().addTileSet(folderName,
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



                }

//                class Tile
//                {
//                    public Tile(final String filename)
//                    {
//                        this.z = Integer.parseInt(filename.substring(0, 0));
//                        this.x = Integer.parseInt(filename.substring(1, 1));
//                        this.y = Integer.parseInt(filename.substring(2, 2));
//
//                        this.filename = filename;
//                    }
//
//                    public int z;
//                    public int x;
//                    public int y;
//
//                    public String filename;
//                }
//
//                final Map<Integer, List<Tile>> tiles = Stream.of(directory.listFiles())
//                                                             .map(file -> new Tile(file.getName()))
//                                                             .collect(Collectors.groupingBy(tile -> tile.z));
//
//                for(final Entry<Integer, List<Tile>> zoomLevel : tiles.entrySet())
//                {
//                    final TileMatrix tileMatrix = gpkg.tiles()
//                                                      .addTileMatrix(tileSet,
//                                                                     0,
//                                                                     1,
//                                                                     1,
//                                                                     1,
//                                                                     1,
//                                                                     360.0,
//                                                                     180.0);
//
//                gpkg.tiles().addTile(tileSet,
//                                     tileMatrix,
//                                     new CrsCoordinate(-90.0, -180.0, "EPSG", 4326),
//                                     0,
//                                     new byte[] {});
//                }


            }
        }
        catch(final Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
